package ca.on.oicr.pde.deciders;

import java.io.File;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import net.sourceforge.seqware.common.hibernate.FindAllTheFiles.Header;
import net.sourceforge.seqware.common.module.FileMetadata;
import net.sourceforge.seqware.common.module.ReturnValue;

/**
 * A Wrapper around all of the metadata relating to a file.
 *
 * @author mtaschuk
 */
public class FileAttributes {

    private String path;
    private String metatype;
    private String md5;
    private Long fileSize;
    private String study;
    private String experiment;
    private String librarySample;
    private String donor;
    private String barcode;
    private Integer lane;
    private String sequencerRun;
    private Map<Lims, String> sampleAttributes;
    private Map<String, String> otherAttributes;
    private String group;
    private Map<Lims, String> alternateAttributes;

    public Map<Lims, String> getAlternateAttributes() {
        return alternateAttributes;
    }

    /**
     * Get the value of group
     *
     * @return the value of group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Set the value of group
     *
     * @param group new value of group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Retrieves an attribute value from the full list of attributes using the
     * Header class (originally from the StudyReporter).
     *
     * @param header the key for the attribute.
     * @return the value corresponding to the header key, or null if none.
     */
    public String getOtherAttribute(Header header) {
        if (header == null) {
            return null;
        }
        return otherAttributes.get(header.getTitle());
    }

    /**
     * Retrieves an attribute value from the full list of attributes using an
     * arbitrary String.
     *
     * @param string the key for the attribute.
     * @return the value corresponding to string key, or null if none.
     */
    public String getOtherAttribute(String string) {
        return otherAttributes.get(string);
    }

    /**
     * Return an iterator over all attributes associated with this file. Used in
     * combination with getOtherAttribute(String), you may query all of the
     * metadata associated with this file.
     *
     * @return all of attribute keys associated with this file.
     */
    public Iterator<String> iterator() {
        return otherAttributes.keySet().iterator();
    }

    /**
     * Searches for a particular value in the given Lims attribute and returns
     * the full value of the given Lims key. A Lims attribute may have more than
     * one value associated with a key. Multiple values are returned as a
     * comma-separated string
     *
     * @return the full value of the Lims attribute. Returns null if a) the key
     * does not exist or b) the key does not contain the given attribute.
     *
     */
    public String getLimsValue(Lims key, String searchString) {
        if (sampleAttributes.containsKey(key)) {
            if (sampleAttributes.get(key).contains(searchString)) {
                return sampleAttributes.get(key);
            }
        }
        return null;
    }

    /**
     * Returns the value of a Lims attribute.
     *
     * @param key One of the Lims keys
     * @return the value of the Lims attribute, or null if not set
     */
    public String getLimsValue(Lims key) {
        return sampleAttributes.get(key);
    }

    /**
     * Returns the barcode used in sequencing the contents of this file.
     *
     * @return the barcode
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * Returns the donor that contributed the sample described in this file.
     * More precisely, it returns the most generic sample associated with this
     * file. For example, for a file containing sample ABCD_001_Pa_R_700_EX, the
     * donor will be ABCD_001.
     *
     * @return the donor name
     */
    public String getDonor() {
        return donor;
    }

    /**
     * Returns the lane number that this library was sequenced on.
     *
     * @return the lane number
     */
    public Integer getLane() {
        return lane;
    }

    /**
     * Returns the name of the sequencer run where this library was seqeuenced.
     *
     * @return the sequencer run name
     */
    public String getSequencerRun() {
        return sequencerRun;
    }

    /**
     * Returns the name of the sample library used in sequencing. More
     * precisely, it returns the most specific sample name associated with this
     * file, e.g. ABCD_001_Pa_R_700_EX.
     *
     * @return the sample library
     */
    public String getLibrarySample() {
        return librarySample;
    }

    /**
     * Returns the experiment associated with this library and file. The
     * experiment describes the method of sequencing.
     *
     * @return the experiment name
     */
    public String getExperiment() {
        return experiment;
    }

    /**
     * Returns the study associated with the library and file. For example, in
     * sample ABCD_001_Pa_R_700_EX, the study would be ABCD, or a longer more
     * descriptive name.
     *
     * @return the study name
     */
    public String getStudy() {
        return study;
    }

    /**
     * Returns the size of the file in bytes if it exists.
     *
     * @return the size of the file or null if it does not exist
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * Returns the md5 checksum if it exists.
     *
     * @return the md5 checksum or null if it does not exist
     */
    public String getMd5() {
        return md5;
    }

    /**
     * Returns the meta-type of the file. See <a
     * href="https://docs.google.com/spreadsheet/ccc?key=0An-x7dcdlF7AdGhjdjRTU0toZkJXNlNRb1NROXdfLWc#gid=0">here</a>
     * for a list of common meta-types in SeqWare
     *
     * @return the meta-type of the file
     *
     */
    public String getMetatype() {
        return metatype;
    }

    /**
     * Returns the absolute file path of the file.
     *
     * @return the file path
     */
    public String getPath() {
        return path;
    }

    public FileAttributes() {
    }

    public FileAttributes(ReturnValue returnValue, FileMetadata fm) {
        metatype = fm.getMetaType();
        path = fm.getFilePath();
        md5 = fm.getMd5sum();
        fileSize = fm.getSize();

        Map<String, String> atts = returnValue.getAttributes();

        study = assignString(atts, Header.STUDY_TITLE);
        experiment = assignString(atts, Header.EXPERIMENT_NAME);
        librarySample = assignString(atts, Header.SAMPLE_NAME);
        donor = assignLastInString(atts, Header.PARENT_SAMPLE_NAME, ":");
        lane = assignInt(atts, Header.LANE_NUM);
        sequencerRun = assignString(atts, Header.SEQUENCER_RUN_NAME);
        barcode = assignString(atts, Header.IUS_TAG);
        sampleAttributes = new EnumMap<Lims, String>(Lims.class);

        for (Lims limsAtt : Lims.values()) {
            for (String geoString : atts.keySet()) {
                String value = extractAttribute(geoString, limsAtt.getAttributeTitle(), atts.get(geoString));
                String oldVal = sampleAttributes.get(limsAtt);
                if (value != null) {
                    if (oldVal == null) {
                        sampleAttributes.put(limsAtt, value.trim());
                    } else if (!oldVal.contains(value)) {
                        sampleAttributes.put(limsAtt, oldVal + "," + value.trim());
                    }
                }
            }
        }
        otherAttributes = atts;
        otherAttributes.put("metatype", metatype);
        otherAttributes.put("file_path", path);
        otherAttributes.put("md5sum", md5);
        otherAttributes.put("fileSize", fileSize == null ? null : fileSize.toString());
    }

    protected String extractAttribute(String key, String title, String value) {
        String toRet = null;
        if (key.matches(".*" + title + "[^_]*")) {
            toRet = value;
        }
        if (toRet != null) {
            toRet = toRet.trim();
        }
        return toRet;
    }

    private String assignLastInString(Map<String, String> atts, Header header, String delimiter) {
        String variable = null;
        String lastSt = atts.get(header.getTitle());
        if (lastSt != null) {
            String[] arr = lastSt.split(delimiter);
            variable = arr[arr.length - 1];
        }
        if (variable != null) {
            variable = variable.trim();
        }
        return variable;
    }

    private String assignString(Map<String, String> atts, Header header) {
        String variable = atts.get(header.getTitle());
        if (variable != null) {
            variable = variable.trim();
        }
        return variable;
    }

    private Integer assignInt(Map<String, String> atts, Header header) {
        Integer variable = null;
        String intSt = atts.get(header.getTitle());
        if (intSt != null) {
            variable = Integer.parseInt(intSt);
        }
        return variable;
    }

    /**
     * Returns the absolute path of the file. Equivalent to getPath().
     *
     * @see #getPath()
     * @return the absolute path of the file
     */
    @Override
    public String toString() {
        return path;
    }

    /**
     * Return the filename.
     *
     * @see #getPath()
     * @return the name of the file as defined by path
     */
    public String basename() {
        return new File(path).getName();
    }

    /**
     * Return the directory the file is in.
     *
     * @see #getPath()
     * @return the absolute path of the directory where the file is in, defined
     * by path
     */
    public String dirname() {
        return new File(path).getParent();
    }
}
