#!/usr/bin/env python3

import argparse
import csv
import os
import subprocess
import sys

from utils import *

if not sys.version_info >= (3, 6):
    raise ExecutionException("Python 3.6+ is required")

parser = argparse.ArgumentParser(
    formatter_class=argparse.ArgumentDefaultsHelpFormatter)
parser.add_argument("--java-path",
                    help="Path to Java 8 executable",
                    required=True)
parser.add_argument("--seqware-jar-path",
                    help="Path to SeqWare distribution executable",
                    required=True)
parser.add_argument("--workflow-run-swid",
                    help="Workflow run ID to associate WDL output(s) to",
                    required=True)
parser.add_argument("--processing-swid",
                    help="Processing ID to associate WDL output(s) to",
                    required=True)
parser.add_argument("--cromwell-host",
                    help="Cromwell URL to get output metadata from",
                    required=True)
parser.add_argument("--cromwell-workflow-id-path",
                    help="Path to file containing the Cromwell workflow id",
                    required=True)
parser.add_argument("--output-dir",
                    help="The permanent output directory to provision files to",
                    required=True)
parser.add_argument("--wdl-outputs-path",
                    help="Path to the wdl outputs file",
                    required=False)

args = parser.parse_args()

with open(args.cromwell_workflow_id_path, "r") as f:
    (cromwell_id,) = f.read().splitlines()

workflow_status = get_cromwell_status(args.cromwell_host, cromwell_id)
if workflow_status != "Succeeded":
    raise ExecutionException("Workflow status: " + workflow_status)

wdl_outputs = None
if args.wdl_outputs_path:
    with open(args.wdl_outputs_path, "r") as f:
        wdl_outputs = json.load(f)

workflow_outputs = get_cromwell_outputs(args.cromwell_host, cromwell_id)

print("Workflow outputs:")
print(json.dumps(workflow_outputs, indent=4))
outputs = parse_workflow_output(workflow_outputs, wdl_outputs)
print("Parsed workflow outputs:")
print(json.dumps(outputs, indent=4))

commands = []
count = 0
unique_file_names = {}
for id, output in outputs.items():
    metatype = output.get("metatype", None)
    annotations = output.get("annotations", {})
    limskeys = output.get("limskeys", [])
    files = output.get("files", None)
    if files is None:
        raise ExecutionException(f"[id={id}] files list is empty")

    annotations_file = None
    if annotations:
        annotations_file = f"{id}_annotations.csv"
        with open(annotations_file, 'w') as f:
            w = csv.writer(f, delimiter='\t')
            for k, v in annotations.items():
                w.writerow([k, v])

    for file in files:
        if file == "null":
            print("Skipping processing of optional file")
            continue

        file_name = os.path.basename(file)
        if file_name in unique_file_names:
            raise ExecutionException(
                f"Duplicate file name detected. {file_name} maps to {unique_file_names[file_name]} and {file}")
        else:
            unique_file_names[file_name] = file

        if metatype is None:
            metatype = get_metatype(file_name)

        output_file = args.output_dir + file_name

        if limskeys:
            parentIds = limskeys
        else:
            parentIds = [args.processing_swid]

        processing_file = f"s{args.workflow_run_swid}_{args.processing_swid}_provision_out_{count}_processing_accession"
        count = count + 1

        command = [args.java_path,
                   "-XX:+UseSerialGC",
                   "-Xmx500M",
                   "-classpath",
                   args.seqware_jar_path,
                   "net.sourceforge.seqware.pipeline.runner.Runner",
                   "--metadata"]

        for parentId in parentIds:
            command.append("--metadata-parent-accession")
            command.append(str(parentId))

        command.extend(["--metadata-workflow-run-ancestor-accession",
                        args.workflow_run_swid,
                        "--metadata-processing-accession-file",
                        processing_file,
                        "--metadata-processing-accession-file-lock",
                        f"{processing_file}.lock",
                        "--module",
                        "net.sourceforge.seqware.pipeline.modules.utilities.ProvisionFiles",
                        "--",
                        "--input-file-metadata",
                        f"pfo::{metatype}::{file}",
                        "--output-file",
                        output_file,
                        "--force-copy"])

        if annotations_file:
            command.append("--annotation-file")
            command.append(annotations_file)

        commands.append(command)

for command in commands:
    if args.workflow_run_swid == "0":
        print(f"Workflow run swid == 0 (dry run mode), would have executed: {' '.join(command)}")
    else:
        print(f"Executing: {' '.join(command)}")
        try:
            command_output = subprocess.run(command,
                                            stdout=subprocess.PIPE,
                                            stderr=subprocess.PIPE,
                                            check=True,
                                            encoding='UTF-8')
            print(f"stdout:\n{command_output.stdout}")
            print(f"stderr:\n{command_output.stderr}")
        except subprocess.CalledProcessError as e:
            print(f"stdout:\n{e.stdout}")
            print(f"stderr:\n{e.stderr}")
            raise e
