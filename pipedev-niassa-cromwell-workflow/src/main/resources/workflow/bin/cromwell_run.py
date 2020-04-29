#!/usr/bin/env python3

import argparse
import os
import re
import subprocess
import sys
from urllib.error import URLError

from utils import *

if not sys.version_info >= (3, 6):
    raise Exception("Python 3.6+ is required")

parser = argparse.ArgumentParser(
    formatter_class=argparse.ArgumentDefaultsHelpFormatter)
parser.add_argument("--java-path",
                    help="Path to Java 8 executable",
                    required=True)
parser.add_argument("--seqware-jar-path",
                    help="Path to SeqWare distribution executable",
                    required=True)
parser.add_argument("--niassa-host",
                    help="Niassa URL to annotate Cromwell workflow with",
                    required=True)
parser.add_argument("--workflow-run-swid",
                    help="Workflow run ID to associate WDL output(s) to",
                    required=True)
parser.add_argument("--processing-swid",
                    help="Processing ID to associate WDL output(s) to",
                    required=True)
parser.add_argument("--cromwell-jar-path",
                    help="Path to Cromwell jar",
                    required=True)
parser.add_argument("--cromwell-host",
                    help="Cromwell URL to get output metadata from",
                    required=True)
parser.add_argument("--cromwell-workflow-id-path",
                    help="Path to file to write the Cromwell workflow id to",
                    required=True)
parser.add_argument("--polling-interval",
                    help="The number of seconds to wait to poll the Cromwell workflow status",
                    required=False,
                    default=30)
parser.add_argument("--wdl-workflow",
                    help="The WDL workflow to submit to Cromwell",
                    required=True)
parser.add_argument("--wdl-inputs",
                    help="The WDL inputs/parameters to submit to Cromwell",
                    required=True)
parser.add_argument("--wdl-options",
                    help="The WDL options to submit to Cromwell",
                    required=False)
parser.add_argument("--wdl-deps-zip",
                    help="The WDL dependencies file to submit to Cromwell",
                    required=False)
parser.add_argument("--symlink-cromwell-working-directory",
                    help="Symlinks to cromwell working directory into the current directory",
                    required=False,
                    action='store_false')  # aka default=True

args = parser.parse_args()

labels = {"niassa-workflow-run-id": args.workflow_run_swid,
          "external_id": f"{args.niassa_host}/workflowruns/{args.workflow_run_swid}"}
labels_path = "labels.json"
with open(labels_path, "w") as file:
    json.dump(labels, file)

submit_cmd = [args.java_path,
              "-XX:+UseSerialGC",
              "-Xmx1g",
              "-jar",
              args.cromwell_jar_path,
              "submit",
              args.wdl_workflow,
              "--inputs",
              args.wdl_inputs,
              "--host",
              args.cromwell_host,
              "--labels",
              labels_path]
if args.wdl_options:
    submit_cmd.append("--options")
    submit_cmd.append(args.wdl_options)

if args.wdl_deps_zip:
    submit_cmd.append("--imports")
    submit_cmd.append(args.wdl_deps_zip)

try:
    print(f"Executing: {' '.join(submit_cmd)}")
    submit_cmd_output = subprocess.run(submit_cmd,
                                       stdout=subprocess.PIPE,
                                       stderr=subprocess.PIPE,
                                       check=True,
                                       encoding='UTF-8',
                                       timeout=600)
    print(f"stdout:\n{submit_cmd_output.stdout}")
    print(f"stderr:\n{submit_cmd_output.stderr}")
except subprocess.CalledProcessError as e:
    print(f"stdout:\n{e.stdout}")
    print(f"stderr:\n{e.stderr}")
    raise e

match = re.search(".*Workflow(.*)submitted to.*", submit_cmd_output.stdout)
if match:
    cromwell_workflow_id = match.group(1).strip()
else:
    raise Exception(f"Unable to get parse Cromwell submit output "
                    f"to find cromwell-workflow-id.\n"
                    f"stdout:\n{submit_cmd_output.stdout}\n"
                    f"stderr:\n{submit_cmd_output.stderr}")

with open(args.cromwell_workflow_id_path, "w") as file:
    file.write(cromwell_workflow_id)

seqware_annotate_cmd = [args.java_path,
                        "-XX:+UseSerialGC",
                        "-Xmx500M",
                        "-classpath",
                        args.seqware_jar_path,
                        "net.sourceforge.seqware.pipeline.runner.PluginRunner",
                        "--plugin",
                        "net.sourceforge.seqware.pipeline.plugins.AttributeAnnotator",
                        "--",
                        "--workflow-run-accession",
                        args.workflow_run_swid,
                        "--key",
                        "cromwell-workflow-id",
                        "--value",
                        cromwell_workflow_id]

if args.workflow_run_swid == "0":
    print(f"Workflow run swid == 0 (dry run mode), would have executed: {' '.join(seqware_annotate_cmd)}")
else:
    print(f"Executing: {' '.join(seqware_annotate_cmd)}")
    retry(subprocess.run,
          seqware_annotate_cmd,
          stdout=subprocess.PIPE,
          stderr=subprocess.PIPE,
          check=True,
          encoding='UTF-8',
          allowed_exceptions=(subprocess.CalledProcessError),
          times=5,
          delay=60)

# wait until cromwell has processed the workflow submission
status = "Pending"
while status in ["Pending", "Submitted"]:
    print(f"Workflow id = {cromwell_workflow_id} status = {status}")
    time.sleep(int(args.polling_interval))
    status = retry(get_cromwell_status,
                   args.cromwell_host,
                   cromwell_workflow_id,
                   allowed_exceptions=(URLError),
                   times=5,
                   delay=20)

# get the cromwell working directory
workflow_metadata = retry(get_cromwell_metadata,
                          args.cromwell_host,
                          cromwell_workflow_id,
                          allowed_exceptions=(URLError),
                          times=5,
                          delay=20)
working_directory = workflow_metadata.get("workflowRoot")
print(f"Workflow id = {cromwell_workflow_id} working directory = {working_directory}")
if args.symlink_cromwell_working_directory:
    if working_directory:
        if os.path.islink("cromwell_working_directory"):
            os.unlink("cromwell_working_directory")
        os.symlink(working_directory, "cromwell_working_directory")

# wait while the workflow is running
while status in ["Running"]:
    print(f"Workflow id = {cromwell_workflow_id} status = {status}")
    time.sleep(int(args.polling_interval))
    status = retry(get_cromwell_status,
                   args.cromwell_host,
                   cromwell_workflow_id,
                   allowed_exceptions=(URLError),
                   times=5,
                   delay=20)

print(f"Workflow id = {cromwell_workflow_id} status = {status}")

if status not in ["Succeeded"]:
    raise Exception(f"Workflow id = {cromwell_workflow_id} status = {status}")
