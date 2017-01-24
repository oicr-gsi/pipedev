# pipedev file provenance client

The pipedev-file-provenance-client is used to aggregate lane, sample, and analysis provenance into file provenance records.

The client retrieves data from provenance providers defined in the "provenance settings" file and produces a TSV file of file provenance records.



##Build

```
mvn clean install
```


##Test

To run the tests, a postgres database and user with create db privileges is required.

```
mvn clean install \
-DskipITs=false \
-DdbHost=localhost \
-DdbUser=myuser \
-DdbPort=54323
```

##Usage

The current options for the client can be viewed by providing the "--help" argument:
```
java -jar pipedev-file-provenance-client-2.0-jar-with-dependencies.jar --help

Option (* = required)  Description                           
---------------------  -----------                           
--all                  Get all records rather than only the  
                         records that pass the default       
                         filters: [processing status =       
                         success, workflow run status =      
                         completed, skip = false]            
--experiment           Filter/select file provenance records 
                         by experiment                       
--file                 Filter/select file provenance records 
                         by file                             
--file-meta-type       Filter/select file provenance records 
                         by file_meta_type                   
--help                                                       
--ius                  Filter/select file provenance records 
                         by ius                              
--lane                 Filter/select file provenance records 
                         by lane                             
--organism             Filter/select file provenance records 
                         by organism                         
* --out                File provenance report TSV output file
                         path                                
--processing           Filter/select file provenance records 
                         by processing                       
--processing-status    Filter/select file provenance records 
                         by processing_status                
--root-sample          Filter/select file provenance records 
                         by root_sample                      
--sample               Filter/select file provenance records 
                         by sample                           
--sample-ancestor      Filter/select file provenance records 
                         by sample_ancestor                  
--sequencer-run        Filter/select file provenance records 
                         by sequencer_run                    
--settings             Provider settings json file (default: 
                         ~/.provenance/settings.json)        
--skip                 Filter/select file provenance records 
                         by skip                             
--study                Filter/select file provenance records 
                         by study                            
--workflow             Filter/select file provenance records 
                         by workflow                         
--workflow-run         Filter/select file provenance records 
                         by workflow_run                     
--workflow-run-status  Filter/select file provenance records 
                         by workflow_run_status  
```

An example provenance provider settings file:
```
[ {
  "type" : "ca.on.oicr.gsi.provenance.SeqwareMetadataLimsMetadataProvenanceProvider",
  "provider" : "seqware",
  "providerSettings" : {
    "SW_METADATA_METHOD" : "webservice",
    "SW_REST_URL" : "http://localhost:8889/seqware-webservice",
    "SW_REST_USER" : "admin@admin.com",
    "SW_REST_PASS" : "admin"
  }
}, {
  "type" : "ca.on.oicr.gsi.provenance.SeqwareMetadataAnalysisProvenanceProvider",
  "provider" : "seqware",
  "providerSettings" : {
    "SW_METADATA_METHOD" : "webservice",
    "SW_REST_URL" : "http://localhost:8889/seqware-webservice",
    "SW_REST_USER" : "admin@admin.com",
    "SW_REST_PASS" : "admin"
  }
} ]
```

Getting the file provenance records that pass the default filters (processing status = success, workflow run status = completed, skip = false]:
```
java -jar pipedev-file-provenance-client-2.0-jar-with-dependencies.jar --settings /path/to/settings.json --out /tmp/fpr.tsv
```

Or getting all file provenance records (no filters):
```
java -jar pipedev-file-provenance-client-2.0-jar-with-dependencies.jar --settings /path/to/settings.json --out /tmp/fpr_all.tsv --all
```