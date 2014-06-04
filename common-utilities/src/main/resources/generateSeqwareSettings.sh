#!/bin/bash

#set -e
#set -o pipefail

die () {
    echo >&2 "$@"
    exit 1
}

BASE_DIR=$1

SW_REST_URL=$2

SCHEDULING_SYSTEM=$3

SCHEDULING_HOST=$4

LOCK_ID=${5:-seqware_testing}

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
SW_CLUSTER=${SCHEDULING_HOST}
SW_DAX_DIR=${SW_DAX_DIR}
SW_PEGASUS_CONFIG_DIR=${SW_PEGASUS_CONFIG_DIR}
SW_METADATA_METHOD=webservice
SW_BUNDLE_DIR=${SW_BUNDLE_DIR}
SW_BUNDLE_REPO_DIR=${SW_BUNDLE_REPO_DIR}
SW_REST_URL=${SW_REST_URL}
SW_REST_USER=admin@admin.com
SW_REST_PASS=admin
EOF
echo "${seqware_settings}" > "${SEQWARE_SETTINGS_FILE}"
chmod 600 "${SEQWARE_SETTINGS_FILE}"

PEGASUS_PROPERTIES_FILE="${SW_PEGASUS_CONFIG_DIR}/properties"
PEGASUS_CATALOG_REPLICA_FILE="${SW_PEGASUS_CONFIG_DIR}"/rc.data
PEGASUS_CATALOG_SITE_FILE="${SW_PEGASUS_CONFIG_DIR}"/sites.xml3
PEGASUS_CATALOG_TRANSFORMATION_FILE="${SW_PEGASUS_CONFIG_DIR}"/tc.data

read -r -d '' pegasus_properties <<EOF
pegasus.catalog.replica = SimpleFile
pegasus.catalog.replica.file = ${PEGASUS_CATALOG_REPLICA_FILE}
pegasus.catalog.site = XML3
pegasus.catalog.site.file = ${PEGASUS_CATALOG_SITE_FILE}
pegasus.catalog.transformation = File
pegasus.catalog.transformation.file = ${PEGASUS_CATALOG_TRANSFORMATION_FILE}
dagman.retry=5
pegasus.catalog.transformation.mapper = All
pegasus.exitcode.scope=all
pegasus.transfer.refiner=Bundle
pegasus.transfer.links = true
pegasus.job.priority=10
pegasus.transfer.*.priority=100
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
        <grid  type="gt5" contact="${SCHEDULING_HOST}/jobmanager-fork" scheduler="Fork" jobtype="auxillary"/>
        <grid  type="gt5" contact="${SCHEDULING_HOST}/jobmanager-sge" scheduler="SGE" jobtype="compute"/>
        <head-fs>
            <scratch>
                <shared>
                    <file-server protocol="gsiftp" url="gsiftp://${SCHEDULING_HOST}" mount-point="${PEGASUS_WORKING_DIR}"/>
                    <internal-mount-point mount-point="${PEGASUS_WORKING_DIR}"/>
                </shared>
            </scratch>
            <storage>
                <shared>
                    <file-server protocol="gsiftp" url="gsiftp://${SCHEDULING_HOST}" mount-point="/"/>
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
    <site  handle="${SCHEDULING_HOST}" arch="x86_64" os="LINUX" osrelease="" osversion="" glibc="">
        <grid  type="gt5" contact="${SCHEDULING_HOST}/jobmanager-fork" scheduler="Fork" jobtype="auxillary"/>
        <grid  type="gt5" contact="${SCHEDULING_HOST}/jobmanager-sge" scheduler="SGE" jobtype="compute"/>
        <head-fs>
            <scratch>
                <shared>
                    <file-server protocol="gsiftp" url="gsiftp://${SCHEDULING_HOST}" mount-point="${PEGASUS_WORKING_DIR}"/>
                    <internal-mount-point mount-point="${PEGASUS_WORKING_DIR}"/>
                </shared>
            </scratch>
            <storage>
                <shared>
                    <file-server protocol="gsiftp" url="gsiftp://${SCHEDULING_HOST}" mount-point="/"/>
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

OOZIE_APP_PATH="hdfs://${SCHEDULING_HOST}:8020/user/"$(whoami)"/"

OOZIE_WORK_DIR="${BASE_DIR}"/oozieTmp
mkdir "${OOZIE_WORK_DIR}"

read -r -d '*' seqware_settings <<EOF
SW_DEFAULT_WORKFLOW_ENGINE=oozie-sge
SW_METADATA_METHOD=webservice
SW_REST_URL=${SW_REST_URL}
SW_REST_USER=admin@admin.com
SW_REST_PASS=admin
SW_BUNDLE_DIR=${SW_BUNDLE_DIR}
SW_BUNDLE_REPO_DIR=${SW_BUNDLE_REPO_DIR}
OOZIE_URL=http://${SCHEDULING_HOST}:11000/oozie
OOZIE_APP_ROOT=seqware_workflow
OOZIE_APP_PATH=${OOZIE_APP_PATH}
OOZIE_JOBTRACKER=${SCHEDULING_HOST}:8021
OOZIE_NAMENODE=hdfs://${SCHEDULING_HOST}:8020
OOZIE_QUEUENAME=default
OOZIE_WORK_DIR=${OOZIE_WORK_DIR}
OOZIE_SGE_MAX_MEMORY_PARAM_FORMAT=-l h_vmem=\${maxMemory}M
OOZIE_SGE_THREADS_PARAM_FORMAT=
HBASE.ZOOKEEPER.QUORUM=${SCHEDULING_HOST}
HBASE.ZOOKEEPER.PROPERTY.CLIENTPORT=2181
HBASE.MASTER=${SCHEDULING_HOST}:60000
MAPRED.JOB.TRACKER=${SCHEDULING_HOST}:8021
FS.DEFAULTFS=hdfs://${SCHEDULING_HOST}:8020
FS.HDFS.IMPL=org.apache.hadoop.hdfs.DistributedFileSystem
SW_LOCK_ID=${LOCK_ID}
OOZIE_BATCH_THRESHOLD=10
OOZIE_BATCH_SIZE=5
EOF

echo "${seqware_settings}" > "${SEQWARE_SETTINGS_FILE}"
chmod 600 "${SEQWARE_SETTINGS_FILE}"

else
    die "Unsupported method"
fi

echo "${SEQWARE_SETTINGS_FILE}"