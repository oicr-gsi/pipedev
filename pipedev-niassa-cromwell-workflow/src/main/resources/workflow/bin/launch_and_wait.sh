#!/bin/bash
set -eu
set -o pipefail

function try {
  local RETRY_MAX=$1
  shift

  local RETRY_WAIT=$1
  shift

  local COUNT=0
  until "$@"; do
    EXIT=$?
    COUNT=$(($COUNT + 1))
    if [ $COUNT -lt $RETRY_MAX ]; then
      echo "Try ${COUNT} of ${RETRY_MAX} exited with exit code = ${EXIT}, waiting ${RETRY_WAIT}s" >&2
      sleep $RETRY_WAIT
    else
      echo "Final try ${COUNT} of ${RETRY_MAX} exited with exit code = ${EXIT}." >&2
      return $exit
    fi
  done
  return 0
}

JAVA=""
SEQWARE_JAR=""
JQ=""
NIASSA_HOST=""
WORKFLOW_RUN_SWID=""
PROCESSING_SWID=""
CROMWELL_JAR=""
CROMWELL_HOST=""
WORKFLOW=""
INPUTS=""
CROMWELL_WORKFLOW_ID_PATH=""
OPTIONS=""
DEPS_ZIP=""
POLLING_INTERVAL="30"

while (( "$#" )); do
    [[ -z "${2+not_set}" ]] && echo "Missing value for ${1}" && exit 1
    case "${1}" in
		--java-path) JAVA="${2}"; shift 2 ;;
		--seqware-jar-path) SEQWARE_JAR="${2}"; shift 2 ;;
		--jq-path) JQ="${2}"; shift 2 ;;
		--niassa-host) NIASSA_HOST="${2}"; shift 2 ;;
		--workflow-run-swid) WORKFLOW_RUN_SWID="${2}"; shift 2 ;;
		--processing-swid) PROCESSING_SWID="${2}"; shift 2 ;;
		--cromwell-jar-path) CROMWELL_JAR="${2}"; shift 2 ;;
		--cromwell-host) CROMWELL_HOST="${2}"; shift 2 ;;
		--workflow) WORKFLOW="${2}"; shift 2 ;;
		--inputs) INPUTS="${2}"; shift 2 ;;
		--cromwell-workflow-id-path) CROMWELL_WORKFLOW_ID_PATH="${2}"; shift 2 ;;
		--options) OPTIONS="${2}"; shift 2 ;;
		--deps-zip) DEPS_ZIP="${2}"; shift 2 ;;
		--polling-interval) POLLING_INTERVAL="${2}"; shift 2 ;;
		--*) echo "Unsupported option ${1}" >&2 ; exit 1 ;;
		*) break ;;
    esac
done

[[ -z "${JAVA}" ]] && echo "--java-path is not set" >&2 && exit 1
[[ -z "${SEQWARE_JAR}" ]] && echo "--seqware-jar-path is not set" >&2 && exit 1
[[ -z "${JQ}" ]] && echo "--jq-path is not set" >&2 && exit 1
[[ -z "${NIASSA_HOST}" ]] && echo "--niassa-host is not set" >&2 && exit 1
[[ -z "${WORKFLOW_RUN_SWID}" ]] && echo "--workflow-run-swid is not set" >&2 && exit 1
[[ -z "${PROCESSING_SWID}" ]] && echo "--processing-swid is not set" >&2 && exit 1
[[ -z "${CROMWELL_JAR}" ]] && echo "--cromwell-jar-path is not set" >&2 && exit 1
[[ -z "${CROMWELL_HOST}" ]] && echo "--cromwell-host is not set" >&2 && exit 1
[[ -z "${WORKFLOW}" ]] && echo "--workflow is not set" >&2 && exit 1
[[ -z "${INPUTS}" ]] && echo "--inputs is not set" >&2 && exit 1
[[ -z "${CROMWELL_WORKFLOW_ID_PATH}" ]] && echo "--cromwell-workflow-id-path is not set" >&2 && exit 1

LABELS_JSON='{\"niassa-workflow-run-id\": \"'${WORKFLOW_RUN_SWID}'\",\"external_id\":\"'${NIASSA_HOST}/workflowruns/${WORKFLOW_RUN_SWID}'\"}'

SUBMIT_CMD="${JAVA} -XX:+UseSerialGC -Xmx1g -jar ${CROMWELL_JAR} submit ${WORKFLOW} --inputs ${INPUTS} --host ${CROMWELL_HOST} --labels <(echo ${LABELS_JSON})"

if [[ -n ${OPTIONS} ]]; then
  SUBMIT_CMD+=" --options ${OPTIONS}"
fi

if [[ -n ${DEPS_ZIP} ]]; then
  SUBMIT_CMD+=" --imports ${DEPS_ZIP}"
fi

SUBMIT_TEXT=$(eval ${SUBMIT_CMD})
echo "${SUBMIT_TEXT}"

WORKFLOW_ID=$(sed -n 's#^\(.*Workflow \)\(.*\)\( submitted to .*\)$#\2#p' <<< "${SUBMIT_TEXT}")
if [[ -z "${WORKFLOW_ID}" ]]; then
  echo "Unable to parse workflow id." >&2
  exit 1
else
  echo "Workflow id = ${WORKFLOW_ID}"
  echo "${WORKFLOW_ID}" > "${CROMWELL_WORKFLOW_ID_PATH}"
fi

# annotate workflow run with cromwell workflow id
echo "Annotating ${WORKFLOW_RUN_SWID}"
try 3 20 "${JAVA}" \
-XX:+UseSerialGC -Xmx500M \
-classpath "${SEQWARE_JAR}" \
net.sourceforge.seqware.pipeline.runner.PluginRunner \
--plugin net.sourceforge.seqware.pipeline.plugins.AttributeAnnotator \
-- \
--workflow-run-accession "${WORKFLOW_RUN_SWID}" \
--key "cromwell-workflow-id" \
--value "${WORKFLOW_ID}"

STATUS="Pending"
while [[ "${STATUS}" == "Pending" || "${STATUS}" == "Submitted" || "${STATUS}" == "Running" ]]; do
  echo "Workflow id = ${WORKFLOW_ID} status = ${STATUS}"
  sleep "${POLLING_INTERVAL}"
  STATUS_TEXT=$(try 3 20 curl --fail -s -X GET ${CROMWELL_HOST}/api/workflows/v1/${WORKFLOW_ID}/status)
  STATUS=$(echo ${STATUS_TEXT} | ${JQ} --raw-output --exit-status '.status')
done

echo "Workflow id = ${WORKFLOW_ID} status = ${STATUS}"
if [[ "${STATUS}" != "Succeeded" ]]; then
  exit 1
else
  exit 0
fi
