/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.eva.commons.core.models.IVariant;
import uk.ac.ebi.eva.commons.core.models.IVariantSourceEntry;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.pipeline.VariantSourceEntry;
import uk.ac.ebi.eva.dbsnpimporter.models.LocusType;
import uk.ac.ebi.eva.dbsnpimporter.models.Orientation;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;
import uk.ac.ebi.eva.dbsnpimporter.test.TestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.SubSnpCoreFieldsToVariantProcessor.DBSNP_BUILD_KEY;

public class SubSnpCoreFieldsToVariantProcessorTest {

    public static final int DBSNP_BUILD = 150;

    public static final String DBSNP_BATCH = "some_study";

    private SubSnpCoreFieldsToVariantProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new SubSnpCoreFieldsToVariantProcessor(DBSNP_BUILD);
    }

    @Test
    public void testSnp() throws Exception {
        SubSnpCoreFields subSnpCoreFields =
                new SubSnpCoreFields(26201546L, Orientation.FORWARD, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                     1766472L, 1766472L, Orientation.FORWARD, LocusType.SNP, "4", 91223961L,
                                     91223961L, "T", "T", "A", "T/A", "NC_006091.4:g.91223961T>A", 91223961L,
                                     91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>A", 1766472L,
                                     1766472L, Orientation.FORWARD, null, DBSNP_BATCH);
        Variant variant = new Variant("4", 91223961L, 91223961L, "T", "A");
        variant.setMainId("rs" + 13677177L);
        variant.setDbsnpIds(TestUtils.buildIds(26201546L, 13677177L));
        Map<String, String> attributes = Collections.singletonMap(DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        VariantSourceEntry sourceEntry = new VariantSourceEntry(String.valueOf(DBSNP_BATCH),
                                                                String.valueOf(DBSNP_BATCH), new String[0], null, null,
                                                                attributes, null);

        variant.addSourceEntry(sourceEntry);

        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testReverseSnp() throws Exception {
        SubSnpCoreFields subSnpCoreFields =
                new SubSnpCoreFields(26201546L, Orientation.REVERSE, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                     1766472L, 1766472L, Orientation.FORWARD, LocusType.SNP, "4", 91223961L,
                                     91223961L, "A", "A", "G", "T/C", "NC_006091.4:g.91223961T>A", 91223961L,
                                     91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>A", 1766472L,
                                     1766472L, Orientation.FORWARD, null, DBSNP_BATCH);
        Variant variant = new Variant("4", 91223961L, 91223961L, "A", "G");
        variant.setMainId("rs" + 13677177L);
        variant.setDbsnpIds(TestUtils.buildIds(26201546L, 13677177L));
        Map<String, String> attributes = Collections.singletonMap(DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        VariantSourceEntry sourceEntry = new VariantSourceEntry(String.valueOf(DBSNP_BATCH),
                                                                String.valueOf(DBSNP_BATCH), new String[0], null, null,
                                                                attributes, null);
        variant.addSourceEntry(sourceEntry);

        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testInsertion() throws Exception {
        SubSnpCoreFields subSnpCoreFields =
                new SubSnpCoreFields(26201546L, Orientation.FORWARD, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                     1766472L, 1766472L, Orientation.FORWARD, LocusType.INSERTION, "4", 91223960L,
                                     91223961L, "-", "-", "A", "-/A", "NC_006091.4:g.91223961T>A", 91223961L,
                                     91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>A", 1766472L,
                                     1766472L, Orientation.FORWARD, null, DBSNP_BATCH);
        Variant variant = new Variant("4", 91223961L, 91223961L, "", "A");
        variant.setMainId("rs" + 13677177L);
        variant.setDbsnpIds(TestUtils.buildIds(26201546L, 13677177L));
        Map<String, String> attributes = Collections.singletonMap(DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        VariantSourceEntry sourceEntry = new VariantSourceEntry(String.valueOf(DBSNP_BATCH),
                                                                String.valueOf(DBSNP_BATCH), new String[0], null, null,
                                                                attributes, null);
        variant.addSourceEntry(sourceEntry);

        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testDeletion() throws Exception {
        SubSnpCoreFields subSnpCoreFields =
                new SubSnpCoreFields(26201546L, Orientation.FORWARD, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                     1766472L, 1766472L, Orientation.FORWARD, LocusType.DELETION, "4", 91223961L,
                                     91223961L, "A", "A", "-", "A/-", "NC_006091.4:g.91223961T>A", 91223961L,
                                     91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>A", 1766472L,
                                     1766472L, Orientation.FORWARD, null, DBSNP_BATCH);
        Variant variant = new Variant("4", 91223961L, 91223961L, "A", "");
        variant.setMainId("rs" + 13677177L);
        variant.setDbsnpIds(TestUtils.buildIds(26201546L, 13677177L));
        Map<String, String> attributes = Collections.singletonMap(DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        VariantSourceEntry sourceEntry = new VariantSourceEntry(String.valueOf(DBSNP_BATCH),
                                                                String.valueOf(DBSNP_BATCH), new String[0], null, null,
                                                                attributes, null);
        variant.addSourceEntry(sourceEntry);

        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testGenotypesBiAllelicReverse() throws Exception {
        //Test allele reverse orientation
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(3173433, Orientation.FORWARD, 2228714L, Orientation.REVERSE,
                                                                 "NC_003074.8", 23412070L, 23412070L, Orientation.FORWARD,
                                                                 LocusType.SNP, "3", 23412070L, 23412070L, "C", "C",
                                                                 "T", "G/A", "NC_003074.8:g.23412070C>T", 23412070L, 23412070L, Orientation.FORWARD, "NC_003074.8:g.23412070C>T",
                                                                 23412070L, 23412070L, Orientation.FORWARD, "G/G,A/A, G/ A, A |G, ./.", "batch");
        Variant variant = new Variant("3", 23412070L, 23412070L, "C", "T");
        variant.setMainId("rs" + 2228714);
        variant.setDbsnpIds(TestUtils.buildIds(3173433, 2228714));
        Map<String, String> attributes = Collections.singletonMap(DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        VariantSourceEntry variantSourceEntry = new VariantSourceEntry(subSnpCoreFields.getBatch(),
                                                                       subSnpCoreFields.getBatch(),
                                                                       subSnpCoreFields.getSecondaryAlternatesInForwardStrand(),
                                                                       null, null,
                                                                       attributes, null);
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "0/0"));
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "1/1"));
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "0/1"));
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "1|0"));
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "./."));
        variant.addSourceEntry(variantSourceEntry);
        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testGenotypesBiAllelicForward() throws Exception {
        //Test allele forward orientation
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(492296696, Orientation.REVERSE, 2228714L, Orientation.REVERSE,
                                                                 "NC_003074.8", 23412070L, 23412070L, Orientation.FORWARD,
                                                                 LocusType.SNP, "3", 23412070L, 23412070L, "G", "G",
                                                                 "A", "T/C", "NC_003074.8:g.23412070C>T", 23412070L, 23412070L, Orientation.REVERSE, "NC_003074.8:g.23412070C>T",
                                                                 23412070L, 23412070L, Orientation.REVERSE, "C/T, T/T, C/C, ./., T/C, C, T", "batch");
        Variant variant = new Variant("3", 23412070L, 23412070L, "C", "T");
        variant.setMainId("rs" + 2228714L);
        variant.setDbsnpIds(TestUtils.buildIds(492296696, 2228714L));
        Map<String, String> attributes = Collections.singletonMap(DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        VariantSourceEntry variantSourceEntry = new VariantSourceEntry(subSnpCoreFields.getBatch(),
                                                                       subSnpCoreFields.getBatch(),
                                                                       subSnpCoreFields.getSecondaryAlternatesInForwardStrand(),
                                                                       null, null,
                                                                       attributes, null);
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "0/1"));
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "1/1"));
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "0/0"));
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "./."));
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "1/0"));
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "0"));
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "1"));
        variant.addSourceEntry(variantSourceEntry);
        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testGenotypesHyphenated() throws Exception {
        //Test hyphenated genotypes
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(492296696, Orientation.REVERSE, 2228714L, Orientation.REVERSE,
                                                                 "NC_003074.8", 23412070L, 23412070L, Orientation.FORWARD,
                                                                 LocusType.SNP, "3", 23412070L, 23412070L, "G", "G",
                                                                 "", "C/-", "NC_003074.8:g.23412070C>T", 23412070L, 23412070L, Orientation.REVERSE, "NC_003074.8:g.23412070C>T",
                                                                 23412070L, 23412070L, Orientation.REVERSE, "-/ - ", "batch");
        Variant variant = new Variant("3", 23412070L, 23412070L, "C", "");
        variant.setMainId("rs" + 2228714L);
        variant.setDbsnpIds(TestUtils.buildIds(492296696, 2228714L));
        Map<String, String> attributes = Collections.singletonMap(DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        VariantSourceEntry variantSourceEntry = new VariantSourceEntry(subSnpCoreFields.getBatch(),
                                                                       subSnpCoreFields.getBatch(),
                                                                       subSnpCoreFields.getSecondaryAlternatesInForwardStrand(),
                                                                       null, null,
                                                                       attributes, null);
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "1/1"));
        variant.addSourceEntry(variantSourceEntry);
        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testGenotypesMultiAllelicReverse() throws Exception {
        //Test multi-allelic genotypes - reverse orientation
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(492296696, Orientation.REVERSE, 2228714L, Orientation.REVERSE,
                                                                 null, 23412070L, 23412073L, Orientation.REVERSE,
                                                                 LocusType.SNP, "3", 23412070L, 23412073L, "TAC", "TAC",
                                                                 "GGC", "GTA/GCC", "", 23412070L, 23412073L, Orientation.FORWARD, "",
                                                                 23412070L, 23412073L, Orientation.FORWARD, "GTA |GCC, GCC|GCC, GTA|GTA", "batch");
        Variant variant = new Variant("3", 23412070L, 23412071L, "TA", "GG");
        variant.setMainId("rs" + 2228714L);
        variant.setDbsnpIds(TestUtils.buildIds(492296696, 2228714L));
        Map<String, String> attributes = Collections.singletonMap(DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        VariantSourceEntry variantSourceEntry = new VariantSourceEntry(subSnpCoreFields.getBatch(),
                                                                       subSnpCoreFields.getBatch(),
                                                                       subSnpCoreFields.getSecondaryAlternatesInForwardStrand(),
                                                                       null, null,
                                                                       attributes, null);
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "0|1"));
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "1|1"));
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "0|0"));
        variant.addSourceEntry(variantSourceEntry);
        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testGenotypesMultiAllelicForward() throws Exception {
        //Test multi-allelic genotypes - forward orientation
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(492296696, Orientation.REVERSE, 2228714L, Orientation.REVERSE,
                                                                 null, 23412070L, 23412073L, Orientation.FORWARD,
                                                                 LocusType.SNP, "3", 23412070L, 23412073L,"TAC", "TAC",
                                                                 "GGC", "TAC/GGC/CCT", "", 23412070L, 23412073L, Orientation.FORWARD, "",
                                                                 23412070L, 23412073L, Orientation.FORWARD, "TAC |GGC, GGC|GGC, TAC/TAC, CCT/GGC", "batch");
        Variant variant = new Variant("3", 23412070L, 23412071L, "TA", "GG");
        variant.setMainId("rs" + 2228714L);
        variant.setDbsnpIds(TestUtils.buildIds(492296696, 2228714L));
        Map<String, String> attributes = Collections.singletonMap(DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        VariantSourceEntry variantSourceEntry = new VariantSourceEntry(subSnpCoreFields.getBatch(),
                                                                       subSnpCoreFields.getBatch(),
                                                                       subSnpCoreFields.getSecondaryAlternatesInForwardStrand(),
                                                                       null, null,
                                                                       attributes, null);
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "0|1"));
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "1|1"));
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "0/0"));
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "2/1"));
        variant.addSourceEntry(variantSourceEntry);
        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testGenotypesNullEmpty() throws Exception {
        //Test genotypes with null and empty values
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(492296696, Orientation.REVERSE, 2228714L, Orientation.REVERSE,
                                                                 null, 23412070L, 23412073L, Orientation.FORWARD,
                                                                 LocusType.SNP, "3", 23412070L, 23412073L,"TAC", "TAC",
                                                                 "GGC", "TAC/GGC/CCT", "", 23412070L, 23412073L, Orientation.FORWARD, "",
                                                                 23412070L, 23412073L, Orientation.FORWARD, null, "batch");
        Variant variant = new Variant("3", 23412070L, 23412071L, "TA", "GG");
        variant.setMainId("rs" + 2228714L);
        variant.setDbsnpIds(TestUtils.buildIds(492296696, 2228714L));
        Map<String, String> attributes = Collections.singletonMap(DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        VariantSourceEntry variantSourceEntry = new VariantSourceEntry(subSnpCoreFields.getBatch(),
                                                                       subSnpCoreFields.getBatch(),
                                                                       subSnpCoreFields.getSecondaryAlternatesInForwardStrand(),
                                                                       null, null,
                                                                       attributes, null);
        variant.addSourceEntry(variantSourceEntry);
        assertVariantEquals(variant, processor.process(subSnpCoreFields));
        subSnpCoreFields.setRawGenotypesString("");
        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testIUPACGenotypes() throws Exception {
        //Test genotypes with IUPAC codes in them
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(492296696, Orientation.REVERSE, 2228714L, Orientation.REVERSE,
                                                                 null, 23412070L, 23412073L, Orientation.FORWARD,
                                                                 LocusType.SNP, "3", 23412070L, 23412073L,"TAC", "TAC",
                                                                 "GGC", "TAC/GGC/CCT", "", 23412070L, 23412073L, Orientation.FORWARD, "",
                                                                 23412070L, 23412073L, Orientation.FORWARD, "TAC |GGN, U / N, TAC/TAC", "batch");
        Variant variant = new Variant("3", 23412070L, 23412071L, "TA", "GG");
        variant.setMainId("rs" + 2228714L);
        variant.setDbsnpIds(TestUtils.buildIds(492296696, 2228714L));
        Map<String, String> attributes = Collections.singletonMap(DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        VariantSourceEntry variantSourceEntry = new VariantSourceEntry(subSnpCoreFields.getBatch(),
                                                                       subSnpCoreFields.getBatch(),
                                                                       subSnpCoreFields.getSecondaryAlternatesInForwardStrand(),
                                                                       null, null,
                                                                       attributes, null);
        variantSourceEntry.addSampleData(createGenotypeMap("GT", ".|."));
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "./."));
        variantSourceEntry.addSampleData(createGenotypeMap("GT", "0/0"));
        variant.addSourceEntry(variantSourceEntry);
        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    private static Map<String, String> createGenotypeMap (String key, String value) throws Exception {
        Map<String, String> genotypeMap = new HashMap<>();
        genotypeMap.put(key, value);
        return  genotypeMap;
    }

    private void assertVariantEquals(IVariant variant, IVariant processedVariant) throws Exception {
        assertEquals(variant, processedVariant);
        assertEquals(variant.getMainId(), processedVariant.getMainId());
        assertEquals(variant.getIds(), processedVariant.getIds());
        assertEquals(variant.getDbsnpIds(), processedVariant.getDbsnpIds());

        assertEquals(1, processedVariant.getSourceEntries().size());
        IVariantSourceEntry sourceEntry = variant.getSourceEntries().iterator().next();
        IVariantSourceEntry actualSourceEntry = processedVariant.getSourceEntries().iterator().next();

        assertEquals(sourceEntry.getFileId(), actualSourceEntry.getFileId());
        assertEquals(sourceEntry.getStudyId(), actualSourceEntry.getStudyId());
        assertArrayEquals(sourceEntry.getSecondaryAlternates(), actualSourceEntry.getSecondaryAlternates());
        assertEquals(sourceEntry.getAttributes(), actualSourceEntry.getAttributes());
        assertEquals(sourceEntry.getSamplesData(), actualSourceEntry.getSamplesData());
    }
}
