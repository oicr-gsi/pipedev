JobMonitor is a small application that monitors a Sun Grid Engine or Open Grid Engine for jobs with
particular names, waits until those jobs complete, and collects some information about each job. It 
was designed for use with SeqWare (http://seqware.io) but is not dependent on that infrastructure.

## Introduction

SGE job monitor was designed to be used with so-called "one step workflows" : SeqWare workflows that
have one job that launches jobs on the SGE cluster. The job usually consists of a script written in 
bash, Perl, R or Python that submits qsubs. The only requirement is that the script prepend or 
append a "unique job string" to every job name. This unique string is usually a UUID.

Once the job monitor script is launched, it polls every 5 seconds to check on the status of jobs 
with the appropriate unique string. When the jobs disappear from qstat, it waits for the jobs to 
appear in qacct and records the exit status.

The output file contains the job ID, job name and SGE exit status (not the process exit status).


## Options

    Option               Description                           
    ------               -----------                           
    -b, --begin-time     The earliest start time for jobs to be
                            summarized, in the format [[CC]YY]  
                            MMDDhhmm[.SS]                       
    -o, --output-file    A location for an output file         
                            describing the finished jobs        
    --unique-job-string  A unique string that is attached to   
                            all jobs of interest.               

    Must include parameter: --unique-job-string

## Build

To build, use Maven:

    mvn clean install

The JAR with all dependencies is located in the target directory under `sge-job-monitor-<VERSION>-jar-with-dependencies.jar`.

## Run

Use Java 7:

    java -jar sge-job-monitor-jar-with-dependencies.jar --unique-job-string abcd --output-file log.txt

## Support

For help with this application, please file an issue on the [oicr-gsi/pipedev](https://github.com/oicr-gsi/pde-dev) project.
