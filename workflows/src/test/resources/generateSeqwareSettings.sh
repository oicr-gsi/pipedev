#!/bin/bash

#set -e
#set -o pipefail

die () {
    echo >&2 "$@"
    exit 1
}

[[ "$#" == 4 ]] || die "4 arguments required, $# provided"

BASE_DIR=$1

SW_REST_URL=$2

SCHEDULING_SYSTEM=$3

SW_CLUSTER=$4

SW_BUNDLE_DIR="${BASE_DIR}"/provisionedBundles
SW_BUNDLE_REPO_DIR="${BASE_DIR}"/bundleRepo

if [ ! -d "$BASE_DIR" ]; then
    echo "Base directory ${BASE_DIR} does not exist."
    exit 1
fi

mkdir "${SW_BUNDLE_DIR}"
mkdir "${SW_BUNDLE_REPO_DIR}"


SEQWARE_SETTINGS_FILE="${BASE_DIR}"/seqware_settings

if [[ "$SCHEDULING_SYSTEM" == "pegasus" ]]
then

SW_PEGASUS_CONFIG_DIR="${BASE_DIR}"/pegasus
mkdir "${SW_PEGASUS_CONFIG_DIR}"

PEGASUS_WORKING_DIR="${BASE_DIR}"/working
mkdir "${PEGASUS_WORKING_DIR}"

SW_DAX_DIR="${BASE_DIR}"/dax
mkdir "${SW_DAX_DIR}"

read -r -d '' seqware_settings <<EOF
#
# SEQWARE PIPELINE SETTINGS
#
# the name of the cluster as defined in the Pegasus sites.xml config file
SW_CLUSTER=${SW_CLUSTER}
# the directory used to store the generated DAX workflow documents before submission to the cluster
SW_DAX_DIR=${SW_DAX_DIR}
# the directory containing all the Pegasus config files this instance of SeqWare should use
SW_PEGASUS_CONFIG_DIR=${SW_PEGASUS_CONFIG_DIR}
# SeqWare MetaDB communication method, can be "database" or "webservice" or "none"
SW_METADATA_METHOD=webservice
# a directory to copy bundles to for archiving/installing
SW_BUNDLE_DIR=${SW_BUNDLE_DIR}
# the central repository for installed bundles
SW_BUNDLE_REPO_DIR=${SW_BUNDLE_REPO_DIR}

#
# SEQWARE WEBSERVICE SETTINGS
#
# the base URL for the RESTful SeqWare API
SW_REST_URL=${SW_REST_URL}
# the username and password to connect to the REST API, this is used by SeqWare Pipeline to write back processing info to the DB
SW_REST_USER=admin@admin.com
SW_REST_PASS=admin

#
# AMAZON CLOUD SETTINGS
#
# used by tools reading and writing to S3 buckets (dependency data/software bundles, inputs, outputs, etc)
# most likely not used here at OICR
##AWS_ACCESS_KEY=lksjdflksjdklf
##AWS_SECRET_KEY=slkdjfeoiksdlkjflksjejlfkjeloijxelkj
EOF
echo "${seqware_settings}" > "${SEQWARE_SETTINGS_FILE}"
chmod 600 "${SEQWARE_SETTINGS_FILE}"

PEGASUS_PROPERTIES_FILE="${SW_PEGASUS_CONFIG_DIR}/properties"
PEGASUS_CATALOG_REPLICA_FILE="${SW_PEGASUS_CONFIG_DIR}"/rc.data
PEGASUS_CATALOG_SITE_FILE="${SW_PEGASUS_CONFIG_DIR}"/sites.xml3
PEGASUS_CATALOG_TRANSFORMATION_FILE="${SW_PEGASUS_CONFIG_DIR}"/tc.data

read -r -d '' pegasus_properties <<EOF
##########################
# PEGASUS USER PROPERTIES
##########################

## SELECT THE REPLICAT CATALOG MODE AND URL
pegasus.catalog.replica = SimpleFile
pegasus.catalog.replica.file = ${PEGASUS_CATALOG_REPLICA_FILE}

## SELECT THE SITE CATALOG MODE AND FILE
pegasus.catalog.site = XML3
pegasus.catalog.site.file = ${PEGASUS_CATALOG_SITE_FILE}


## SELECT THE TRANSFORMATION CATALOG MODE AND FILE
pegasus.catalog.transformation = File
pegasus.catalog.transformation.file = ${PEGASUS_CATALOG_TRANSFORMATION_FILE}

## USE DAGMAN RETRY FEATURE FOR FAILURES
dagman.retry=5

## STAGE ALL OUR EXECUTABLES OR USE INSTALLED ONES
pegasus.catalog.transformation.mapper = All

## CHECK JOB EXIT CODES FOR FAILURE
pegasus.exitcode.scope=all

## OPTIMZE DATA & EXECUTABLE TRANSFERS
pegasus.transfer.refiner=Bundle
pegasus.transfer.links = true

# JOB Priorities
pegasus.job.priority=10
pegasus.transfer.*.priority=100

#JOB CATEGORIES
pegasus.dagman.projection.maxjobs 5
EOF
echo "${pegasus_properties}" > "${PEGASUS_PROPERTIES_FILE}"

read -r -d '' pegasus_rc_data <<EOF
EOF
echo "${pegasus_rc_data}" > "${PEGASUS_CATALOG_REPLICA_FILE}"

read -r -d '' pegasus_sites_xml3 <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!-- generated: "$(date -R)" -->
<!-- generated by: "$(whoami)" -->
<sitecatalog xmlns="http://pegasus.isi.edu/schema/sitecatalog" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://pegasus.isi.edu/schema/sitecatalog http://pegasus.isi.edu/schema/sc-3.0.xsd" version="3.0">
    <site  handle="local" arch="x86_64" os="LINUX" osrelease="" osversion="" glibc="">
        <grid  type="gt5" contact="${SW_CLUSTER}/jobmanager-fork" scheduler="Fork" jobtype="auxillary"/>
        <grid  type="gt5" contact="${SW_CLUSTER}/jobmanager-sge" scheduler="SGE" jobtype="compute"/>
        <head-fs>
            <scratch>
                <shared>
                    <file-server protocol="gsiftp" url="gsiftp://${SW_CLUSTER}" mount-point="${PEGASUS_WORKING_DIR}"/>
                    <internal-mount-point mount-point="${PEGASUS_WORKING_DIR}"/>
                </shared>
            </scratch>
            <storage>
                <shared>
                    <file-server protocol="gsiftp" url="gsiftp://${SW_CLUSTER}" mount-point="/"/>
                    <internal-mount-point mount-point="/"/>
                </shared>
            </storage>
        </head-fs>
        <replica-catalog  type="LRC" url="rlsn://smarty.isi.edu"/>
        <profile namespace="env" key="GLOBUS_LOCATION" >/.mounts/labs/seqware/public/globus/default</profile>
        <profile namespace="env" key="JAVA_HOME" >/.mounts/labs/seqware/public/java/default</profile>
        <profile namespace="env" key="LD_LIBRARY_PATH" >/.mounts/labs/seqware/public/globus/default/lib</profile>
        <profile namespace="env" key="PEGASUS_HOME" >/.mounts/labs/seqware/public/pegasus/default</profile>
        <profile namespace="env" key="SEQWARE_SETTINGS" >${SEQWARE_SETTINGS_FILE}</profile>
    </site>
    <site  handle="${SW_CLUSTER}" arch="x86_64" os="LINUX" osrelease="" osversion="" glibc="">
        <grid  type="gt5" contact="${SW_CLUSTER}/jobmanager-fork" scheduler="Fork" jobtype="auxillary"/>
        <grid  type="gt5" contact="${SW_CLUSTER}/jobmanager-sge" scheduler="SGE" jobtype="compute"/>
        <head-fs>
            <scratch>
                <shared>
                    <file-server protocol="gsiftp" url="gsiftp://${SW_CLUSTER}" mount-point="${PEGASUS_WORKING_DIR}"/>
                    <internal-mount-point mount-point="${PEGASUS_WORKING_DIR}"/>
                </shared>
            </scratch>
            <storage>
                <shared>
                    <file-server protocol="gsiftp" url="gsiftp://${SW_CLUSTER}" mount-point="/"/>
                    <internal-mount-point mount-point="/"/>
                </shared>
            </storage>
        </head-fs>
        <replica-catalog  type="LRC" url="rlsn://smarty.isi.edu"/>
        <profile namespace="env" key="GLOBUS_LOCATION" >/.mounts/labs/seqware/public/globus/default</profile>
        <profile namespace="env" key="JAVA_HOME" >/.mounts/labs/seqware/public/java/default</profile>
        <profile namespace="env" key="LD_LIBRARY_PATH" >/.mounts/labs/seqware/public/globus/default/lib</profile>
        <profile namespace="env" key="PEGASUS_HOME" >/.mounts/labs/seqware/public/pegasus/default</profile>
        <profile namespace="env" key="SEQWARE_SETTINGS" >${SEQWARE_SETTINGS_FILE}</profile>
    </site>
</sitecatalog>
EOF
echo "${pegasus_sites_xml3}" > "${PEGASUS_CATALOG_SITE_FILE}"

read -r -d '' pegasus_tc_data <<EOF
EOF
echo "${pegasus_tc_data}" > "${PEGASUS_CATALOG_TRANSFORMATION_FILE}"

elif [[ "$SCHEDULING_SYSTEM" == "oozie" ]]
then

OOZIE_APP_PATH="hdfs://hsqwstage-node1.hpc.oicr.on.ca:8020/user/"$(whoami)"/"

OOZIE_WORK_DIR="${BASE_DIR}"/oozieTmp
mkdir "${OOZIE_WORK_DIR}"

read -r -d '*' seqware_settings <<EOF
SW_DEFAULT_WORKFLOW_ENGINE=oozie-sge
SW_METADATA_METHOD=webservice
SW_REST_URL=http://hsqwstage-www1.hpc.oicr.on.ca:8080/seqware-webservice-1.0.6
SW_REST_USER=admin@admin.com
SW_REST_PASS=admin

SW_BUNDLE_DIR=${SW_BUNDLE_DIR}
SW_BUNDLE_REPO_DIR=${SW_BUNDLE_REPO_DIR}

OOZIE_URL=http://hsqwstage-node1.hpc.oicr.on.ca:11000/oozie
OOZIE_APP_ROOT=seqware_workflow
OOZIE_APP_PATH=${OOZIE_APP_PATH}
OOZIE_JOBTRACKER=hsqwstage-node1.hpc.oicr.on.ca:8021
OOZIE_NAMENODE=hdfs://hsqwstage-node1.hpc.oicr.on.ca:8020
OOZIE_QUEUENAME=default
OOZIE_WORK_DIR=${OOZIE_WORK_DIR}

OOZIE_SGE_MAX_MEMORY_PARAM_FORMAT=-l h_vmem=\${maxMemory}M
#OOZIE_SGE_THREADS_PARAM_FORMAT=-l slots=\${threads}
OOZIE_SGE_THREADS_PARAM_FORMAT=

HBASE.ZOOKEEPER.QUORUM=hsqwstage-node1.hpc.oicr.on.ca
HBASE.ZOOKEEPER.PROPERTY.CLIENTPORT=2181
HBASE.MASTER=hsqwstage-node1.hpc.oicr.on.ca:60000
MAPRED.JOB.TRACKER=hsqwstage-node1.hpc.oicr.on.ca:8021
#FS.DEFAULT.NAME=hdfs://hsqwstage-node1.hpc.oicr.on.ca:8020
FS.DEFAULTFS=hdfs://hsqwstage-node1.hpc.oicr.on.ca:8020
FS.HDFS.IMPL=org.apache.hadoop.hdfs.DistributedFileSystem
EOF

echo "${seqware_settings}" > "${SEQWARE_SETTINGS_FILE}"
chmod 600 "${SEQWARE_SETTINGS_FILE}"

else
    die "Unsupported method"
fi

echo "${SEQWARE_SETTINGS_FILE}"