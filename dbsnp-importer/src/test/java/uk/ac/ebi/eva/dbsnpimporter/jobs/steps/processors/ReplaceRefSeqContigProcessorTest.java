package uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.eva.dbsnpimporter.ContigMapping;
import uk.ac.ebi.eva.dbsnpimporter.models.LocusType;
import uk.ac.ebi.eva.dbsnpimporter.models.Orientation;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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
public class ReplaceRefSeqContigProcessorTest {

    private static final String REFSEQ_CONTIG = "NT_example";

    private static final String EQUIVALENT_GENBANK_CONTIG = "GK_example";

    private static final String CHROMOSOME = "4";

    private ReplaceRefSeqContigProcessor replaceRefSeqContigProcessor;

    @Before
    public void setUp() throws Exception {
        Map<String, String> contigMap = new HashMap<>();
        contigMap.put(EQUIVALENT_GENBANK_CONTIG, REFSEQ_CONTIG);
        ContigMapping contigMapping = new ContigMapping(contigMap);
        replaceRefSeqContigProcessor = new ReplaceRefSeqContigProcessor(contigMapping);
    }

    @Test
    public void replaceContigIfChromosomeRegionIsNotValid() throws Exception {
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                 REFSEQ_CONTIG, 1L, 1L, Orientation.FORWARD,
                                                                 LocusType.SNP, "4", null, null, "T", "T", "A", "T/A",
                                                                 "",
                                                                 null, null, Orientation.FORWARD, null, null, null,
                                                                 Orientation.FORWARD, "batch");

        SubSnpCoreFields processed = replaceRefSeqContigProcessor.process(subSnpCoreFields);

        assertEquals(EQUIVALENT_GENBANK_CONTIG, processed.getVariantCoreFields().getChromosome());
    }

    @Test
    public void dontReplaceContigIfChromosomeRegionIsValid() throws Exception {
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                 REFSEQ_CONTIG, 1L, 1L, Orientation.FORWARD,
                                                                 LocusType.SNP, CHROMOSOME, 1L, 1L, "T", "T", "A",
                                                                 "T/A", "",
                                                                 null, null, Orientation.FORWARD, null, null, null,
                                                                 Orientation.FORWARD, "batch");

        SubSnpCoreFields processed = replaceRefSeqContigProcessor.process(subSnpCoreFields);

        assertEquals(CHROMOSOME, processed.getVariantCoreFields().getChromosome());
    }
}
