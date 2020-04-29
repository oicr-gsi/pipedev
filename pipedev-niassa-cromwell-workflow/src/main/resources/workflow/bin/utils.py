import json
import time
import urllib.request as req


class ParsingException(Exception):
    pass


class ExecutionException(Exception):
    pass


def get_cromwell_status(cromwell_url, cromwell_id):
    url = cromwell_url + "/api/workflows/v1/" + cromwell_id + "/status"
    result = json.load(req.urlopen(url, timeout=10))
    return result["status"]


def get_cromwell_metadata(cromwell_url, cromwell_id):
    url = cromwell_url + "/api/workflows/v1/" + cromwell_id + "/metadata"
    result = json.load(req.urlopen(url, timeout=10))
    return result


def get_cromwell_outputs(cromwell_url, cromwell_id):
    url = cromwell_url + "/api/workflows/v1/" + cromwell_id + "/outputs"
    result = json.load(req.urlopen(url, timeout=10))
    return result["outputs"]


def parse_string_output(id, value, wdl_output):
    metatype = None
    annotations = {}
    limskeys = []
    if wdl_output:
        metatype = wdl_output.get("metatype", None)
        annotations = wdl_output.get("annotations", {})
        limskeys = wdl_output.get("limsKeys", [])
    return [{"id": id, "files": [value], "metatype": metatype, "limskeys": limskeys,
             "annotations": annotations}]


def parse_list_of_string_output(id, value, wdl_output):
    metatype = None
    annotations = {}
    limskeys = []
    if wdl_output:
        metatype = wdl_output.get("metatype", None)
        annotations = wdl_output.get("annotations", {})
        limskeys = wdl_output.get("limsKeys", [])
    return [{"id": id, "files": value, "metatype": metatype, "limskeys": limskeys,
             "annotations": annotations}]


def parse_list_of_object_output(id, value, wdl_output):
    if wdl_output is not None:
        wdl_output_key_fields = None
        if type(wdl_output) is list:
            # make sure wdl_output looks like what we expect object with "key" field at minimum
            if not all([type(o) is dict and "key" in o.keys() for o in wdl_output]):
                raise ParsingException(
                    f"[id={id}] wdl_output is not an object or \"key\" field is missing in {str(wdl_output)}")

            # get wdl_output key set to join against
            wdl_output_key_fields = {tuple(o["key"].keys()) for o in wdl_output}
            if len(wdl_output_key_fields) != 1:
                raise ParsingException(
                    f"[id={id}] expected one key set, found key set = {str(wdl_output_key_fields)}")
            wdl_output_key_fields = [key for s in wdl_output_key_fields for key in s]

            wdl_output_dict = {}
            # create a dict of key set to wdl_output
            for o in wdl_output:
                wdl_output_key = tuple(map(o["key"].get, wdl_output_key_fields))
                if wdl_output_key in wdl_output_dict:
                    raise ParsingException(
                        f"[id={id}] duplicate wdl_output found with key = {str(wdl_output_key)}")
                wdl_output_dict[tuple(map(o["key"].get, wdl_output_key_fields))] = o

    results = []
    for idx, list_value in enumerate(value):
        wdl_output_o = None
        if wdl_output:
            if wdl_output_key_fields is not None:
                missing_keys = set(wdl_output_key_fields) - set(list_value.keys())
                if missing_keys:
                    raise ParsingException(
                        f"[id={id}] workflow output is missing key(s) {str(missing_keys)} that are defined in wdl_outputs")
                result_key = tuple(map(list_value.get, wdl_output_key_fields))
                wdl_output_o = wdl_output_dict.get(result_key, None)
                if wdl_output_o is None:
                    raise ParsingException(
                        f"[id={id}] workflow output with key = {str(result_key)} is missing in wdl_output")
                # remove the key(s) from the object, we don't want to parse them any further
                [list_value.pop(wdl_output_key_field) for wdl_output_key_field in wdl_output_key_fields]
            else:
                # we're processing a pair
                wdl_output_o = wdl_output

        # call parse_output on the remainder of the list result object
        results.extend(parse_output(f"{id}[{idx}]", list_value, wdl_output_o))
    return results


def parse_pair_output(id, value, wdl_output):
    metatype = None
    annotations = {}
    limskeys = []
    if wdl_output:
        metatype = wdl_output.get("metatype", None)
        annotations = wdl_output.get("annotations", {})
        limskeys = wdl_output.get("limsKeys", [])
    left_type = type(value["left"])
    right_type = type(value["right"])
    if left_type is str and right_type is dict:
        return [{"id": id, "files": [value["left"]], "metatype": metatype,
                 "annotations": {**annotations, **value["right"]}, "limskeys": limskeys}]
    elif left_type is list and right_type is dict:
        if all([type(v) is str for v in value["left"]]):
            return [{"id": id, "files": value["left"], "metatype": metatype,
                     "annotations": {**annotations, **value["right"]}, "limskeys": limskeys}]
        else:
            raise ParsingException(f"[id={id}] unsupported left pair value = {value['left']}")
    else:
        raise ParsingException(
            f"[id={id}] unable to handle left pair type = {left_type} and right pair type = {right_type}")


def parse_object_output(id, value, wdl_output):
    results = []
    for k, v in value.items():
        wdl_output_o = None
        if wdl_output:
            wdl_output_o = wdl_output
            if "outputs" in wdl_output:
                wdl_output_o = wdl_output["outputs"].get(k, None)
            if wdl_output_o is None:
                raise ParsingException(f"[id={id}] workflow output {k} is missing in wdl_output")
        results.extend(parse_output(f"{id}.{k}", v, wdl_output_o))
    return results


def parse_output(id, value, wdl_output):
    # optional value
    if value == "null":
        return []

    value_type = type(value)
    if value_type is str:
        return parse_string_output(id, value, wdl_output)
    elif value_type is list:
        inner_value_type = set([type(x) for x in value])
        if len(inner_value_type) > 1:
            raise ParsingException(f"[id={id}] expected a list of a single type, found {str(inner_value_type)}")
        else:
            (inner_value_type,) = inner_value_type
        if inner_value_type is str:
            return parse_list_of_string_output(id, value, wdl_output)
        elif inner_value_type is dict:
            return parse_list_of_object_output(id, value, wdl_output)
        else:
            raise ParsingException(f"[id={id}] unsupported list of type = {inner_value_type}")
    elif value_type is dict:
        if "left" in value.keys() and "right" in value.keys():
            return parse_pair_output(id, value, wdl_output)
        else:
            return parse_object_output(id, value, wdl_output)
    else:
        raise ParsingException(
            f"[id={id}] unsupported workflow output value = {str(value)} of type = {str(value_type)}")


def parse_workflow_output(workflow_outputs, wdl_outputs):
    outputs = []
    for (key, value) in workflow_outputs.items():
        print("Processing " + key)
        if wdl_outputs is not None:
            if key in wdl_outputs:
                out = parse_output(key, value, wdl_outputs[key])
            else:
                raise ParsingException(f"wdl_outputs is missing for {key}")
        else:
            out = parse_output(key, value, None)
        outputs.extend(out)
    if len(set(e["id"] for e in outputs)) != len(outputs):
        raise ParsingException(f"Duplicate output ids found")
    return {e["id"]: e for e in outputs}


# this is an priority ordered list of file extensions to metatype
# e.g. the file extension ".txt.gz" should be returned before ".gz"
default_extension_to_metatype_map = [
    (".bam", "application/bam"),
    (".bai", "application/bam-index"),
    (".g.vcf.gz", "application/g-vcf-gz"),
    (".json", "text/json"),
    (".pdf", "application/pdf"),
    (".tar.gz", "application/tar-gzip"),
    (".tgz", "application/tar-gzip"),
    (".tbi", "application/tbi"),
    (".vcf.gz", "application/vcf-gz"),
    (".zip", "application/zip-report-bundle"),
    (".fastq.gz", "chemical/seq-na-fastq-gzip"),
    (".fastq", "chemical/seq-na-fastq"),
    (".png", "image/png"),
    (".bed", "text/bed"),
    (".BedGraph", "text/bed"),
    (".fpkm_tracking", "text/fpkm-tracking"),
    (".gtf", "text/gtf"),
    (".html", "text/html"),
    (".vcf", "text/vcf"),
    (".txt.gz", "application/txt-gz"),
    (".gz", "application/txt-gz"),
    (".out", "txt/plain"),
    (".log", "txt/plain"),
    (".txt", "txt/plain"),
    (".junction", "txt/junction"),
    (".seg", "application/seg"),
    (".Rdata", "application/rdata"),
    (".RData", "application/rdata"),
    ("", "application/octet-stream")
]


def get_metatype(filename, extension_to_metatype_map=default_extension_to_metatype_map):
    for (extension, metatype) in extension_to_metatype_map:
        if filename.endswith(extension):
            return metatype
    raise ParsingException(f"Unable to get metatype for {filename}")


def retry(func, *func_args, times=3, delay=20, allowed_exceptions=(), **kwargs):
    for i in range(times):
        try:
            result = func(*func_args, **kwargs)
            if result:
                return result
        except allowed_exceptions as e:
            print(e)
            if hasattr(e, 'stdout'):
                print(f"stdout:\n{e.stdout}")
            if hasattr(e, 'stderr'):
                print(f"stderr:\n{e.stderr}")
            pass
        print(f"Function={func.__name__} failed - retry {i + 1}/{times}")
        time.sleep(delay)
    raise Exception(
        f"Function={func.__name__} with args {func_args} has failed")
