/*
 * This decider provides a 'nested grouping' feature that allow grouping first 
 * by then by template type (geo_library_source_template_type) and finally by group id (geo_goup_id)
 */
package ca.on.oicr.pde.deciders;

import java.util.*;
import net.sourceforge.seqware.common.module.FileMetadata;
import net.sourceforge.seqware.common.hibernate.FindAllTheFiles.Header;
import net.sourceforge.seqware.common.module.ReturnValue;
import net.sourceforge.seqware.common.util.Log;
import net.sourceforge.seqware.pipeline.deciders.BasicDecider;

/**
 *
 * @author pruzanov@oicr.on.ca
 */

public class NestedGroupingDecider extends BasicDecider {
  
    private String path   = "./";
    private String folder = "seqware-results";
    private String templateType;
    private String sampleName;
 
    public NestedGroupingDecider() {
        super();
        parser.accepts("ini-file", "Optional: the location of the INI file.").withRequiredArg();
        parser.accepts("verbose", "Optional: output all SeqWare info.").withRequiredArg();
        parser.accepts("output-path", "Optional: the path where the files should be copied to after analysis. output-prefix in INI file.").withRequiredArg();
	parser.accepts("output-folder", "Optional: the folder to put the output into relative to the output-path. Corresponds to output-dir in INI file.").withRequiredArg();
	parser.accepts("template-type", "Optional: Temple type for grouping samples.").withRequiredArg();
	parser.accepts("meta-types", "Optional: Meta-types accepted by this decider. The default is application/bam.").withRequiredArg();
    }

    @Override
    public ReturnValue init() {
        Log.debug("INIT");

	this.setMetaType(Arrays.asList("application/bam"));
	//allows anything defined on the command line to override the 'defaults' here.
        ReturnValue val = super.init();

        if (this.options.has("output-path")) {
             path = options.valueOf("output-path").toString();
              if (!path.endsWith("/")) {
                 path += "/";
              }
        }
        if (this.options.has("output-folder")) {
            folder = options.valueOf("output-folder").toString();
        }

        if (this.options.has("verbose")) {
            Log.setVerbose(true);
        }

	// The below if block handles MIME types for the files, current implementation allows only one MIME type
	if (this.options.has("meta-types")) {
	    String metaTypes = options.valueOf("meta-types").toString();
	    Log.debug("meta-types parameter passed setting metaType to " +  metaTypes);
	    this.setMetaType(Arrays.asList(metaTypes));
	}

        return val;
    }

    protected String handleGroupByAttribute(String attribute, String template, String group_id) {
        //group by parent name, group_id  and template type
	String[] parentNames = attribute.split(":");
	String groupBy = "";
	String [] myFilters = {parentNames[parentNames.length - 1],template,group_id};
        
	for (int i = 0; i < myFilters.length; i++) {
         if (null != myFilters[i]) {
          if (groupBy.length() > 1)
             groupBy = groupBy.concat(":" + myFilters[i]);
          else
             groupBy = groupBy.concat(myFilters[i]);
         }
	}
        

	return groupBy;
    }

    @Override
    protected boolean checkFileDetails(ReturnValue returnValue, FileMetadata fm) {
        Log.debug("CHECK FILE DETAILS:" + fm);

        if (this.options.has("template-type")) {
	    if (!returnValue.getAttribute(Header.SAMPLE_TAG_PREFIX.getTitle() + "geo_library_source_template_type").equals(this.options.valueOf("template-type"))) {
                return false;
	}
        
        }

	this.templateType = returnValue.getAttribute(Header.SAMPLE_TAG_PREFIX.getTitle() + "geo_library_source_template_type");
	this.sampleName   = returnValue.getAttribute(Header.SAMPLE_NAME.getTitle());
        return super.checkFileDetails(returnValue, fm);
    }

    @Override
    public Map<String, List<ReturnValue>> separateFiles(List<ReturnValue> vals, String groupBy) {
        //get files from study
        Map<String, List<ReturnValue>> map = new HashMap<String, List<ReturnValue>>();

        //group files according to the designated header (e.g. sample SWID)
        for (ReturnValue r : vals) {
            String currVal = r.getAttributes().get(groupBy);
            String template = r.getAttribute(Header.SAMPLE_TAG_PREFIX.getTitle() + "geo_library_source_template_type");
            String group_id = r.getAttribute(Header.SAMPLE_TAG_PREFIX.getTitle() + "geo_group_id");

            currVal = handleGroupByAttribute(currVal, template, group_id);

            List<ReturnValue> vs = map.get(currVal);
            if (vs == null) {
                vs = new ArrayList<ReturnValue>();
            }
            vs.add(r);
	    map.put(currVal, vs);

        }
     return map;
    }


    @Override
    protected Map<String, String> modifyIniFile(String commaSeparatedFilePaths, String commaSeparatedParentAccessions) {
        Log.debug("INI FILE:" + commaSeparatedFilePaths);

	Map<String, String> iniFileMap = new TreeMap<String, String>();
        iniFileMap.put("input_files", commaSeparatedFilePaths);
	iniFileMap.put("output_prefix",this.path);
	iniFileMap.put("output_dir", this.folder);
	iniFileMap.put("template_type",this.templateType);
        iniFileMap.put("sample_name",this.sampleName);
	
        return iniFileMap;
    }
}
