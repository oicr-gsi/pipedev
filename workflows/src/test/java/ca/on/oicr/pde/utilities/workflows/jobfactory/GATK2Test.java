package ca.on.oicr.pde.utilities.workflows.jobfactory;

import ca.on.oicr.pde.utilities.workflows.jobfactory.GATK2.Resource;
import java.util.Arrays;
import net.sourceforge.seqware.pipeline.workflowV2.model.Job;
import net.sourceforge.seqware.pipeline.workflowV2.model.Workflow;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author mtaschuk
 */
public class GATK2Test{

    private GATK2 gatk2;
    
//    public GATK2Test(String testName) {
//        super(testName);
//    }

    @BeforeMethod
    protected void setUp() throws Exception {
        //super.setUp();
        gatk2 = new GATK2(new Workflow());
    }

//    @Override
//    protected void tearDown() throws Exception {
//        super.tearDown();
//    }

    /**
     * Test of realignerTargetCreator method, of class gatk2.
     */
    @Test
    public void testRealignerTargetCreator() {
        System.out.println("> realignerTargetCreator");
        Job result = gatk2.realignerTargetCreator("java", "GenomeAnalysisTK.jar",
                3000, "/tmp", "reference.fa", "chr1", Arrays.asList("gold.vcf"), "input.bam", "output.bam", null);
        String expected = "java -Xmx1000M -Djava.io.tmpdir=/tmp -jar GenomeAnalysisTK.jar "
                + "-T RealignerTargetCreator -R reference.fa -I input.bam "
                + "-o output.bam -l INFO -known gold.vcf -L chr1";
        CommandChecker.checkEm(result.getCommand().getArguments(), expected);
        Assert.assertEquals(result.getMaxMemory(), "3000");
    }

    /**
     * Test of indelRealigner method, of class gatk2.
     */
    @Test
    public void testIndelRealigner() {
        System.out.println("> indelRealigner");

        Job result = gatk2.indelRealigner("java", "GenomeAnalysisTK.jar", 3000, "/tmp",
                "reference.fa", "chr1", "interval", "gold.vcf", "input.bam", "output.bam", null);
        String expected = "java -Xmx1000M -Djava.io.tmpdir=/tmp -jar GenomeAnalysisTK.jar "
                + "-T IndelRealigner -R reference.fa -targetIntervals interval -known gold.vcf "
                + "-I input.bam -o output.bam -l INFO -compress 0 -L chr1";
        CommandChecker.checkEm(result.getCommand().getArguments(), expected);
        Assert.assertEquals(result.getMaxMemory(), "3000");
    }

    /**
     * Test of baseRecalibrator method, of class gatk2.
     */
    @Test
    public void testBaseRecalibrator() {
        System.out.println("> baseRecalibrator");

        Job result = gatk2.baseRecalibrator("java", "GenomeAnalysisTK.jar", 3000, "/tmp",
                "reference.fa", Arrays.asList("goldindels.vcf", "goldsnps.vcf"),
                Arrays.asList("input1.bam", "input2.bam"), "recal", null);
        String expected = "java -Xmx1000M -Djava.io.tmpdir=/tmp -jar GenomeAnalysisTK.jar "
                + "-T BaseRecalibrator -R reference.fa -o recal -l INFO "
                + "-knownSites goldindels.vcf -knownSites goldsnps.vcf "
                + "-I input1.bam -I input2.bam";
        CommandChecker.checkEm(result.getCommand().getArguments(), expected);
        Assert.assertEquals(result.getMaxMemory(), "3000");
    }

    /**
     * Test of applyBaseRecalibration method, of class gatk2.
     */
    @Test
    public void testApplyRecalibration() {
        System.out.println("> applyRecalibration");
        Job result = gatk2.applyBaseRecalibration("java", "GenomeAnalysisTK.jar", 3000, "/tmp",
                "reference.fa", "chr1", "recal", "input.bam", "output.bam", null);
        String expected = "java -Xmx1000M -Djava.io.tmpdir=/tmp -jar GenomeAnalysisTK.jar "
                + "-T PrintReads -R reference.fa -I input.bam -BQSR recal -o output.bam "
                + "-l INFO -L chr1";
        CommandChecker.checkEm(result.getCommand().getArguments(), expected);
        Assert.assertEquals(result.getMaxMemory(), "3000");
    }

    @Test
    public void testReduceReads() {
        System.out.println("> reduceReads");
        Job result = gatk2.reduceReads("java", "GenomeAnalysisTK.jar", 3000,
                "/tmp", "reference.fa", "chr1", "input.bam", "output.bam", null);
        String expected = "java -Xmx1000M -Djava.io.tmpdir=/tmp -jar GenomeAnalysisTK.jar "
                + "-T ReduceReads -R reference.fa -I input.bam -o output.bam -L chr1";
        CommandChecker.checkEm(result.getCommand().getArguments(), expected);
        Assert.assertEquals(result.getMaxMemory(), "3000");
    }

    @Test
    public void testHaplotypeCaller() {
        System.out.println("> haplotypeCaller");
        Job result = gatk2.haplotypeCaller("java", "GenomeAnalysisTK.jar", 3000, "/tmp",
                "reference.fa", "chr1", "DISCOVERY", "EMIT_VARIANTS_ONLY", 10, 30,
                "GATK_public.key", "dbsnp.vcf", "input.bam", "output.vcf", null);
        String expected = "java -Xmx1000M -Djava.io.tmpdir=/tmp -jar GenomeAnalysisTK.jar "
                + "-T HaplotypeCaller -R reference.fa -I input.bam "
                + "--genotyping_mode DISCOVERY -stand_emit_conf 10 -stand_call_conf 30 "
                + "-o output.vcf --dbsnp dbsnp.vcf --output_mode EMIT_VARIANTS_ONLY -L chr1 -et NO_ET -K GATK_public.key";
        CommandChecker.checkEm(result.getCommand().getArguments(), expected);
        Assert.assertEquals(result.getMaxMemory(), "3000");
    }

    @Test
    public void testUnifiedGenotyper() {
        System.out.println("> unifiedGenotyper");
        Job result = gatk2.unifiedGenotyper("java", "GenomeAnalysisTK.jar", 3000, "/tmp",
                "reference.fa", "chr1", 2, "BOTH", 10, 30,
                "GATK_public.key", "dbsnp.vcf", "input.bam", "output.vcf", null);
        String expected = "java -Xmx1000M -Djava.io.tmpdir=/tmp -jar GenomeAnalysisTK.jar "
                + "-T UnifiedGenotyper -R reference.fa -I input.bam "
                + "-ploidy 2 -glm BOTH -stand_emit_conf 10 -stand_call_conf 30 "
                + "--computeSLOD -o output.vcf --dbsnp dbsnp.vcf -L chr1 -et NO_ET -K GATK_public.key";
        CommandChecker.checkEm(result.getCommand().getArguments(), expected);
        Assert.assertEquals(result.getMaxMemory(), "3000");
    }

    @Test
    public void testVariantRecalibrator() {
        System.out.println("> variantRecalibrator");
        Job result = gatk2.variantRecalibrator("java", "GenomeAnalysisTK.jar", 3000, "/tmp",
                "reference.fa", "chr1", "SNP", 99.9, "GATK_public.key", 0.01, 1000,
                Arrays.asList(gatk2.new Resource("name", true, true, true, 12.3, "name.vcf")),
                Arrays.asList("DP", "QD", "FS"), "input.vcf", "output.recal",
                "output.tranches", null);
        String expected = "java -Xmx1000M -Djava.io.tmpdir=/tmp -jar GenomeAnalysisTK.jar "
                + "-T VariantRecalibrator -R reference.fa "
                + "-mode SNP -tranche 99.9 -percentBad 0.01 -minNumBad 1000 "
                + "-input input.vcf -recalFile output.recal -tranchesFile output.tranches "
                + "-resource:name,known=true,training=true,truth=true,prior=12.3 name.vcf "
                + "-an DP -an QD -an FS "
                + "-L chr1 "
                + "-et NO_ET -K GATK_public.key";
        CommandChecker.checkEm(result.getCommand().getArguments(), expected);
        Assert.assertEquals(result.getMaxMemory(), "3000");
    }

    @Test
    public void testSnpVariantRecalibrator() {
        System.out.println("> snpVariantRecalibrator");
        Job result = gatk2.snpVariantRecalibrator("java", "GenomeAnalysisTK.jar",
                3000, "/tmp", "reference.fa", "chr1", 99.9, "GATK_public.key",
                "hapmap.vcf", "omni.vcf", "1000G.vcf", "dbsnp.vcf", "input.vcf", "output.recal", "output.tranches", null);

        String expected = "java -Xmx1000M -Djava.io.tmpdir=/tmp -jar GenomeAnalysisTK.jar "
                + "-T VariantRecalibrator -R reference.fa "
                + "-mode SNP -tranche 99.9 -percentBad 0.01 -minNumBad 1000 "
                + "-input input.vcf -recalFile output.recal -tranchesFile output.tranches "
                + "-resource:hapmap,known=false,training=true,truth=true,prior=15.0 hapmap.vcf "
                + "-resource:omni,known=false,training=true,truth=false,prior=12.0 omni.vcf "
                + "-resource:1000G,known=false,training=true,truth=false,prior=10.0 1000G.vcf "
                + "-resource:dbsnp,known=true,training=false,truth=false,prior=2.0 dbsnp.vcf "
                + "-an DP -an QD -an FS -an MQRankSum -an ReadPosRankSum "
                + "-L chr1 "
                + "-et NO_ET -K GATK_public.key";
        CommandChecker.checkEm(result.getCommand().getArguments(), expected);
        Assert.assertEquals(result.getMaxMemory(), "3000");
    }

    @Test
    public void testIndelVariantRecalibrator() {
        System.out.println("> indelVariantRecalibrator");
        Job result = gatk2.indelVariantRecalibrator("java", "GenomeAnalysisTK.jar",
                3000, "/tmp", "reference.fa", "chr1", 99.9, "GATK_public.key",
                "millsdevine.vcf","1000K.indels.vcf", "dbsnp.vcf", "input.vcf", "output.recal", "output.tranches", null);

        String expected = "java -Xmx1000M -Djava.io.tmpdir=/tmp -jar GenomeAnalysisTK.jar "
                + "-T VariantRecalibrator -R reference.fa "
                + "-mode INDEL -tranche 99.9 -percentBad 0.01 -minNumBad 1000 "
                + "-input input.vcf -recalFile output.recal -tranchesFile output.tranches "
                + "-resource:mills,known=true,training=true,truth=true,prior=12.0 millsdevine.vcf "
                + "-resource:1000G,known=false,training=true,truth=false,prior=10.0 1000K.indels.vcf "
                + "-resource:dbsnp,known=true,training=false,truth=false,prior=2.0 dbsnp.vcf "
                + "-an DP -an FS -an MQRankSum -an ReadPosRankSum "
                + "-L chr1 "
                + "-et NO_ET -K GATK_public.key "
                + "--maxGaussians 4 ";
        CommandChecker.checkEm(result.getCommand().getArguments(), expected);
        Assert.assertEquals(result.getMaxMemory(), "3000");
    }
}
