package ca.on.oicr.pde.deciders;

/**
 * A holder class for attributes from OICR's Geospiza LIMS.
 * For more information about these sample attributes, see
 * <a href="https://wiki.oicr.on.ca/display/SEQWARE/Geospiza+Imported+Sample+Attributes">https://wiki.oicr.on.ca/display/SEQWARE/Geospiza+Imported+Sample+Attributes</a>.
 *
 * @author mtaschuk@oicr.on.ca
 */
public enum Lims {

    /**
     * Geospiza reaction id associated with this sample. If a sample has this then 
     * it's a library that was run on the instrument (vs. another member of the sample hierarchy)
     */
    REACTION_ID("geo_reaction_id"),
    /**
     *  Refers to the experimental design, whole genome (WG) or whole exome (EX). 
     * This information can also be found by looking at the experiment and then 
     * linking to experiment_library_design
     */
    LIBRARY_TEMPLATE_TYPE("geo_library_source_template_type"),
    /**
     * A character code to indicate the type of sample; primary tumor (P), reference (R), xenograft (X), or cell line (C).
     */
    TISSUE_TYPE("geo_tissue_type"),
    /**
     * Tissue Origin is represented by an uppercase and lowercase character. 
     * For example, Pancreas would be Pa, Breast Br, Lung Lu, Large Intestine 
     * LI, etc. Unknown tissue origin is represented by two lowercase n's. For example nn.
     */
    TISSUE_ORIGIN("geo_tissue_origin"),
    /**
     * Tissue Preparation can be represented by Fresh Frozen, FFPE (Formalin-fixed, Paraffin-embedded) and Blood.
     */
    TISSUE_PREP("geo_tissue_preparation"),
    /**
     * Geospiza template id used to created this sample. Geospiza uses the word 'template' instead of 'sample'.
     */
    TEMPLATE_ID("geo_template_id"),
    /**
     *  Are described with text values like Agilent SureSelect ICGC/Sanger Exon and Illumina TruSeq Exome. There are approximately 10 distinct targeted resequencing values.
     */
    TARGETED_RESEQUENCING("geo_targeted_resequencing"),
    /**
     * Geospiza template type described with text values like gDNA and Illumina PE Library. There are approximately 50 distinct template types.
     */
    TEMPLATE_TYPE("geo_template_type"),
    //        QUBIT_CONCENTRATION("geo_qubit_concentration"),
    //        NANODROP_CONCENTRATION("geo_nanodrop_concentration"),
    //        QPCR_PERCENT_HUMAN("geo_qpcr_percentage_human"),
    //        STR_RESULT("geo_str_result"),
    //        RECEIVE_DATE("geo_receive_date"),
    //        PURPOSE("geo_purpose"),
    /**
     *  A number provided by pathology indicating the region of the tissue the sample was taken from.
     */
    TISSUE_REGION("geo_tissue_region"),
    /**
     * The label on the tube provided by the sample collaborator.
     */
    TUBE_ID("geo_tube_id"),
    /**
     * Third party kit used to construct sample.
     */
    PREP_KIT("geo_prep_kit"),
    /**
     * Identifier which has meaning only for the original PI and their team. 
     * The PI may request that SeqWare treat samples with the same geo_group_id 
     * as identical, though the reason for this will only be understood by the 
     * PI and their team.  Do not use this value when aggregating across projects 
     * since the semantics of this attribute vary from project to project. 
     */
    GROUP_ID("geo_group_id"),
    /**
     *  Description of geo_group_id provided by PI. This text field is entered by hand and can not be relied on for programatic processing.
     */
    GROUP_DESC("geo_group_id_description"),
    /**
     *  a number code indicating the size of the band cut from the gel in base pairs. 
     * Typical examples are 300, 500, 2K (2kb insert for MP library), 120 (smRNA 
     * library). Note that the size is NOT updated based on observed fragment 
     * distribution once sequenced - just specify the size estimated from the 
     * gel cut. An unknown library size code is represented by two lowercase n's. 
     * For example nn.
     */
    LIBRARY_SIZE("geo_library_size_code"),
    /**
     * Describes the library type (adapters) used (SE, PE, MP).
     */
    LIBRARY_TYPE("geo_library_type");
    private final String attribute;

    Lims(String attribute) {
        this.attribute = attribute;
    }

    /**
     * Get the Geospiza string representation of the Lims enum.
     * @return Geospiza Lims string
     */
    public String getAttributeTitle() {
        return attribute;
    }
}
