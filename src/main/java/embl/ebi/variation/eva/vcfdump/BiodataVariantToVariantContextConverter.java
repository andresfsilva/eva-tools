/*
 *
 *  * Copyright 2016 EMBL - European Bioinformatics Institute
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package embl.ebi.variation.eva.vcfdump;

import embl.ebi.variation.eva.vcfdump.cellbasewsclient.CellbaseWSClient;
import embl.ebi.variation.eva.vcfdump.exception.CellbaseSequenceDownloadError;
import htsjdk.variant.variantcontext.*;
import org.broadinstitute.variant.vcf.VCFConstants;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.VariantSourceEntry;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by pagarcia on 27/06/2016.
 */
public class BiodataVariantToVariantContextConverter {

    public static final String GENOTYPE_KEY = "GT";
    private final VariantContextBuilder variantContextBuilder;
    private final List<VariantSource> sources;
    private CellbaseWSClient cellbaseClient;
    private String regionSequence;
    private Map<String, Map<String,String>> studiesSampleNamesEquivalences;
    private static final int NO_CALL_ALLELE_INDEX = 2;

    public BiodataVariantToVariantContextConverter(List<VariantSource> sources, CellbaseWSClient cellbaseWSClient,
                                                   Map<String, Map<String,String>> studiesSampleNamesEquivalences)
    {
        this.sources = sources;
        this.cellbaseClient = cellbaseWSClient;
        this.studiesSampleNamesEquivalences = studiesSampleNamesEquivalences;
        variantContextBuilder = new VariantContextBuilder();
    }

    public VariantContext transform(Variant variant, Region region) throws CellbaseSequenceDownloadError {
        String[] allelesArray = getAllelesArray(variant, region);

        Set<Genotype> genotypes = getGenotypes(variant, allelesArray);

        VariantContext variantContext = variantContextBuilder
                .chr(variant.getChromosome())
                // TODO: check start and end for indels
                .start(variant.getStart())
                .stop(variant.getEnd())
                .noID()
                .alleles(allelesArray)
                .unfiltered()
                .genotypes(genotypes).make();
        return variantContext;
    }

    private String[] getAllelesArray(Variant variant, Region region) throws CellbaseSequenceDownloadError {
        String[] allelesArray;
        // if there are indels, we cannot use the normalized alleles, (hts forbids empty alleles) so we have to take them from cellbase
        if (variant.getReference().isEmpty() || variant.getAlternate().isEmpty()) {
            String contextNucleotide;
            if (region != null) {
                contextNucleotide = getContextNucleotideFromCellbaseCachingRegions(variant, variant.getStart(), region);
            } else {
                contextNucleotide = getContextNucleotideFromCellbase(variant, variant.getStart());
            }

            allelesArray = new String[] {contextNucleotide + variant.getReference(), contextNucleotide + variant.getAlternate()};
            variant.setEnd(variant.getStart() + allelesArray[0].length() - 1);
        } else {
            allelesArray = new String[] {variant.getReference(), variant.getAlternate()};
        }

        return allelesArray;
    }

    private String getContextNucleotideFromCellbase(Variant variant, Integer start) throws CellbaseSequenceDownloadError {
        if (cellbaseClient != null) {
            String contextNucleotide;
            int contextNucleotidePosition = start - 1;
            try {
                contextNucleotide = cellbaseClient.getSequence(new Region(variant.getChromosome(), contextNucleotidePosition, contextNucleotidePosition));
                return contextNucleotide;
            } catch (Exception e) {
                throw new CellbaseSequenceDownloadError("Error getting from Cellbase sequence for Region " + variant.getChromosome() + ":" +
                        contextNucleotidePosition + "-" + contextNucleotidePosition, e);
            }
        } else {
            throw new IllegalArgumentException(String.format(
                    "CellBase was not provided, needed to fill empty alleles in variant %s:%d:%s>%s", variant.getChromosome(),
                    variant.getStart(), variant.getReference(), variant.getAlternate()));
        }
    }

    private String getContextNucleotideFromCellbaseCachingRegions(Variant variant, int start, Region region) throws CellbaseSequenceDownloadError {
        if (cellbaseClient != null) {
            if (regionSequence == null) {
                // if an indel start is the first nucleotide of the region, we will need the previous nucleotide, so we are adding
                // the preceding nucleotide to the region (region.getStart()-1)
                int regionStart = region.getStart() - 1;
                int regionEnd = region.getEnd();
                try {
                    regionSequence = cellbaseClient.getSequence(new Region(variant.getChromosome(), regionStart, regionEnd));
                } catch (Exception e) {
                    throw new CellbaseSequenceDownloadError("Error getting from Cellbase sequence for Region " + variant.getChromosome() +
                            ":" + regionStart + "-" + regionEnd, e);
                }
            }
            String nucleotide = getNucleotideFromRegionSequence(start, region.getStart(), regionSequence);
            return nucleotide;
        } else {
            throw new IllegalArgumentException(String.format(
                    "CellBase was not provided, needed to fill empty alleles in variant %s:%d:%s>%s",
                    variant.getChromosome(), variant.getStart(), variant.getReference(), variant.getAlternate()));
        }
    }

    private String getNucleotideFromRegionSequence(int start, int regionStart, String regionSequence) {
        int relativePosition = start - regionStart;
        return regionSequence.substring(relativePosition, relativePosition + 1);
    }

    private Set<Genotype> getGenotypes(Variant variant, String[] allelesArray) {
        Set<Genotype> genotypes = new HashSet<>();

        Allele[] variantAlleles =
                {Allele.create(allelesArray[0], true), Allele.create(allelesArray[1]), Allele.create(Allele.NO_CALL, false)};

        for (VariantSource source : sources) {
            List<VariantSourceEntry> variantStudyEntries =
                    variant.getSourceEntries().values().stream().filter(s -> s.getStudyId().equals(source.getStudyId())).collect(Collectors.toList());
            for (VariantSourceEntry variantStudyEntry : variantStudyEntries) {
                genotypes = getStudyGenotypes(genotypes, variantAlleles, variantStudyEntry);
            }
        }
        return genotypes;
    }

    private Set<Genotype> getStudyGenotypes(Set<Genotype> genotypes, Allele[] variantAlleles, VariantSourceEntry variantStudyEntry) {
        for (Map.Entry<String, Map<String, String>> sampleEntry : variantStudyEntry.getSamplesData().entrySet()) {
            String gt = sampleEntry.getValue().get(GENOTYPE_KEY);
            org.opencb.biodata.models.feature.Genotype genotype = new org.opencb.biodata.models.feature.Genotype(gt, variantAlleles[0].getBaseString(), variantAlleles[1].getBaseString());
            List<Allele> genotypeAlleles = new ArrayList<>(2);
            for (int index : genotype.getAllelesIdx()) {
                if (index == -1 || index > NO_CALL_ALLELE_INDEX) {
                    index = NO_CALL_ALLELE_INDEX;
                }
                genotypeAlleles.add(variantAlleles[index]);
            }

            genotypes.add(new GenotypeBuilder().name(getFixedSampleName(variantStudyEntry.getStudyId(), sampleEntry.getKey())).alleles(genotypeAlleles).phased(genotype.isPhased()).make());
        }
        return genotypes;
    }

    private String getFixedSampleName(String studyId, String sampleName) {
        // this method returns the "studyId appended" sample name if there are sample name conflicts
        if (studiesSampleNamesEquivalences != null) {
            return studiesSampleNamesEquivalences.get(studyId).get(sampleName);
        } else {
            return sampleName;
        }
    }

    public void cleanCachedRegionSequence() {
        regionSequence = null;
    }
}