package ca.on.oicr.pde.deciders.readgroups;

/**
 * ReadGroup data class.
 *
 * @author mlaszloffy
 */
public interface ReadGroups {

    /**
     * DNA preparation library identifier.
     *
     * @return @RG LB
     */
    public String getLibraryReadGroup();

    /**
     * Platform/technology used to produce the read.
     *
     * @return @RG PL
     */
    public String getPlatformReadGroup();

    /**
     * Sample (name that will be displayed in vcf columns).
     *
     * @return @RG SM
     */
    public String getSampleReadGroup();

    /**
     * Platform Unit.
     *
     * @return @RG PU
     */
    public String getPlatformUnitReadGroup();

    /**
     * Read group identifier.
     *
     * @return @RG ID
     */
    public String getId();

}
