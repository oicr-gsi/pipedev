#!/bin/bash

#set -o errexit
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

if [[ "$SCHEDULING_SYSTEM" == "oozie" ]]
then

OOZIE_APP_PATH="hdfs://${SCHEDULING_HOST}:8020/user/$(whoami)/"

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
OOZIE_SGE_THREADS_PARAM_FORMAT=-pe smp \${threads}
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