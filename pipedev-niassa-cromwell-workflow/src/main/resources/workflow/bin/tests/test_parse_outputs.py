import pytest

from utils import parse_workflow_output, ParsingException

wdl_outputs = {"workflow.file": {"limsKeys": [1, 2, 3],
                                 "annotations": {"r": "1"},
                                 "metatype": "meta/type"},
               "workflow.optionalFile": {"limsKeys": [1, 2, 3],
                                         "annotations": {"r": "1"},
                                         "metatype": "meta/type"},
               "workflow.files": {"limsKeys": [1, 2, 3],
                                  "annotations": {"r": "1"},
                                  "metatype": "meta/type"},
               "workflow.filePairs": {"limsKeys": [1, 2, 3],
                                      "annotations": {"size": "should be set by workflow",
                                                      "r": 1}},
               "workflow.outputGroups1": [{"key": {"sample": "1"}},
                                          {"key": {"sample": "2"}}],
               "workflow.outputGroups2": [{"key": {"sample": "1"},
                                           "outputs": {
                                               "file1": {
                                                   "limsKeys": [1, 2, 3],
                                                   "annotations": {"r": "1"},
                                                   "metatype": "meta/type1"},
                                               "file2": {
                                                   "limsKeys": [1, 2, 3],
                                                   "annotations": {"r": "1"},
                                                   "metatype": "meta/type2"}}
                                           },
                                          {"key": {"sample": "2"},
                                           "outputs": {
                                               "file1": {
                                                   "limsKeys": [4, 5, 6],
                                                   "annotations": {"r": "1"},
                                                   "metatype": "meta/type1"},
                                               "file2": {
                                                   "limsKeys": [4, 5, 6],
                                                   "annotations": {"r": "1"},
                                                   "metatype": "meta/type2"}
                                           }
                                           }],
               "workflow.outputGroups3": [{"key": {"sample": "1"}},
                                          {"key": {"sample": "2"}}],
               "workflow.outputGroups4": [{"key": {"sample": "1"},
                                           "outputs": {
                                               "file": {
                                                   "limsKeys": [1, 2, 3],
                                                   "annotations": {"size": "should be set by workflow", "r": "1"},
                                                   "metatype": "meta/type"}}},
                                          {"key": {"sample": "2"},
                                           "outputs": {
                                               "file": {
                                                   "limsKeys": [4, 5, 6],
                                                   "annotations": {"size": "should be set by workflow", "r": "1"},
                                                   "metatype": "meta/type"}}
                                           }],
               "workflow.outputGroups5": [{"key": {"key1": "1", "key2": "a"}},
                                          {"key": {"key1": "2", "key2": "b"}}]
               }


def test_file_string():
    workflow_outputs = {"workflow.file": "/path/to/1"}
    result = parse_workflow_output(workflow_outputs, None)
    assert len(result) == 1
    assert len(result["workflow.file"].get("files")) == 1
    assert len(result["workflow.file"].get("limskeys")) == 0
    assert len(result["workflow.file"].get("annotations")) == 0
    assert result["workflow.file"].get("metatype", []) is None


def test_file_string_with_wdl_outputs():
    workflow_outputs = {"workflow.file": "/path/to/1"}
    result = parse_workflow_output(workflow_outputs, wdl_outputs)
    assert len(result) == 1
    assert len(result["workflow.file"].get("files", [])) == 1
    assert result["workflow.file"].get("limskeys") == [1, 2, 3]
    assert result["workflow.file"].get("annotations") == {"r": "1"}
    assert result["workflow.file"].get("metatype") == "meta/type"


def test_optonal_file_string():
    workflow_outputs = {"workflow.optionalFile": "null"}
    result = parse_workflow_output(workflow_outputs, None)
    assert len(result) == 0


def test_optional_file_string_with_wdl_outputs():
    workflow_outputs = {"workflow.optionalFile": "null"}
    result = parse_workflow_output(workflow_outputs, wdl_outputs)
    assert len(result) == 0


def test_file_array():
    workflow_outputs = {"workflow.files": ["/path/to/1", "/path/to/2"]}
    result = parse_workflow_output(workflow_outputs, None)
    assert len(result) == 1
    assert len(result["workflow.files"].get("files")) == 2
    assert len(result["workflow.files"].get("limskeys")) == 0
    assert len(result["workflow.files"].get("annotations")) == 0
    assert result["workflow.files"].get("metatype") is None


def test_file_array_with_wdl_outputs():
    workflow_outputs = {"workflow.files": ["/path/to/1", "/path/to/2"]}
    result = parse_workflow_output(workflow_outputs, wdl_outputs)
    assert len(result) == 1
    assert len(result["workflow.files"].get("files", [])) == 2
    assert result["workflow.files"].get("limskeys") == [1, 2, 3]
    assert result["workflow.files"].get("annotations") == {"r": "1"}
    assert result["workflow.files"].get("metatype") == "meta/type"


def test_array_file_annotation_pairs():
    workflow_outputs = {"workflow.filePairs": [
        {"left": "/path/to/1", "right": {"size": "1"}},
        {"left": "/path/to/2", "right": {"size": "2"}}]}
    result = parse_workflow_output(workflow_outputs, None)
    assert len(result) == 2
    assert all([len(r.get("files")) == 1 for _, r in result.items()])
    assert all([r.get("limskeys") == [] for _, r in result.items()])
    assert result["workflow.filePairs[0]"].get("annotations") == {"size": "1"}
    assert result["workflow.filePairs[1]"].get("annotations") == {"size": "2"}
    assert all([r.get("metatype") is None for _, r in result.items()])


def test_array_file_annotation_pairs():
    workflow_outputs = {"workflow.filePairs": [
        {"left": "/path/to/1", "right": {"size": "1"}},
        {"left": ["/path/to/2", "/path/to/3"], "right": {"size": "2"}}]}
    result = parse_workflow_output(workflow_outputs, None)
    assert len(result) == 2
    assert len(result["workflow.filePairs[0]"].get("files")) == 1
    assert len(result["workflow.filePairs[1]"].get("files")) == 2
    assert all([r.get("limskeys") == [] for _, r in result.items()])
    assert result["workflow.filePairs[0]"].get("annotations") == {"size": "1"}
    assert result["workflow.filePairs[1]"].get("annotations") == {"size": "2"}
    assert all([r.get("metatype") is None for _, r in result.items()])


def test_array_file_annotation_pairs_with_wdl_outputs():
    workflow_outputs = {"workflow.filePairs": [
        {"left": "/path/to/1", "right": {"size": "1"}},
        {"left": ["/path/to/2", "/path/to/3"], "right": {"size": "2"}}]}
    result = parse_workflow_output(workflow_outputs, wdl_outputs)
    assert len(result) == 2
    assert len(result["workflow.filePairs[0]"].get("files")) == 1
    assert len(result["workflow.filePairs[1]"].get("files")) == 2
    assert all([r.get("limskeys") == [1, 2, 3] for _, r in result.items()])
    assert result["workflow.filePairs[0]"].get("annotations") == {"size": "1", "r": 1}
    assert result["workflow.filePairs[1]"].get("annotations") == {"size": "2", "r": 1}
    assert all([r.get("metatype") is None for _, r in result.items()])


def test_output_struct1():
    workflow_outputs = {"workflow.outputGroups1": [
        {"sample": "1", "file": "/path/to/1", "file2": "/path/to/2"},
        {"sample": "2", "file": "/path/to/3", "file3": "null"}]}
    result = parse_workflow_output(workflow_outputs, None)
    assert len(result) == 5  # this includes parsing "sample" as file path string


def test_output_struct1_with_wdl_outputs():
    workflow_outputs = {"workflow.outputGroups1": [
        {"sample": "1", "file": "/path/to/1", "file2": "/path/to/2"},
        {"sample": "2", "file": "/path/to/3", "file3": "null"}]}
    result = parse_workflow_output(workflow_outputs, wdl_outputs)
    assert len(result) == 3
    assert all([len(r.get("files")) == 1 for _, r in result.items()])
    assert all([len(r.get("limskeys")) == 0 for _, r in result.items()])
    assert all([r.get("metatype") is None for _, r in result.items()])


def test_output_struct2():
    workflow_outputs = {"workflow.outputGroups2": [
        {"sample": "1", "file": "/path/to/1", "file2": "/path/to/2"},
        {"sample": "2", "file": "/path/to/3", "file3": "null"}]}
    result = parse_workflow_output(workflow_outputs, None)
    assert len(result) == 5  # this includes incorrectly parsing "sample" key as file path string


def test_output_struct2_with_wdl_outputs():
    workflow_outputs = {"workflow.outputGroups2": [
        {"sample": "1", "file1": "/path/to/1", "file2": "/path/to/2"},
        {"sample": "2", "file1": "/path/to/3", "file2": "null"}]}
    result = parse_workflow_output(workflow_outputs, wdl_outputs)
    assert len(result) == 3
    assert all([len(r.get("files")) == 1 for _, r in result.items()])
    assert result["workflow.outputGroups2[0].file1"].get("limskeys") == [1, 2, 3]
    assert result["workflow.outputGroups2[0].file2"].get("limskeys") == [1, 2, 3]
    assert result["workflow.outputGroups2[1].file1"].get("limskeys") == [4, 5, 6]
    assert result["workflow.outputGroups2[0].file1"].get("metatype") == "meta/type1"
    assert result["workflow.outputGroups2[0].file2"].get("metatype") == "meta/type2"
    assert result["workflow.outputGroups2[1].file1"].get("metatype") == "meta/type1"


def test_output_struct2_with_wdl_outputs_missing_id_key():
    workflow_outputs = {"workflow.outputGroups2": [
        {"sample": "1", "file1": "/path/to/1", "file2": "/path/to/2"},
        {"sample": "2222222", "file1": "/path/to/3", "file2": "null"}]}
    with pytest.raises(ParsingException):
        parse_workflow_output(workflow_outputs, wdl_outputs)


def test_output_struct2_with_wdl_outputs_missing_outputs_key():
    workflow_outputs = {"workflow.outputGroups2": [
        {"sample": "1", "file1": "/path/to/1", "file2": "/path/to/2"},
        {"sample": "2", "file1": "/path/to/3", "file3": "null"}]}
    with pytest.raises(ParsingException):
        parse_workflow_output(workflow_outputs, wdl_outputs)


def test_output_struct3():
    workflow_outputs = {"workflow.outputGroups3": [
        {"sample": "1", "file": {"left": "/path/to/1", "right": {"size": "1"}}},
        {"sample": "2", "file": {"left": ["/path/to/2", "/path/to/3"], "right": {"size": "1"}}}]}
    result = parse_workflow_output(workflow_outputs, None)
    assert len(result) == 4  # this includes incorrectly parsing "sample" key as file path string


def test_output_struct3_with_wdl_outputs():
    workflow_outputs = {"workflow.outputGroups3": [
        {"sample": "1", "file": {"left": "/path/to/1", "right": {"size": "1"}}},
        {"sample": "2", "file": {"left": ["/path/to/2", "/path/to/3"], "right": {"size": "2"}}}]}
    result = parse_workflow_output(workflow_outputs, wdl_outputs)
    assert len(result) == 2
    assert len(result["workflow.outputGroups3[0].file"].get("files")) == 1
    assert len(result["workflow.outputGroups3[1].file"].get("files")) == 2
    assert result["workflow.outputGroups3[0].file"].get("limskeys") == []
    assert result["workflow.outputGroups3[1].file"].get("limskeys") == []
    assert result["workflow.outputGroups3[0].file"].get("annotations") == {"size": "1"}
    assert result["workflow.outputGroups3[1].file"].get("annotations") == {"size": "2"}
    assert all([r.get("metatype") is None for _, r in result.items()])


def test_output_struct4():
    workflow_outputs = {"workflow.outputGroups4": [
        {"sample": "1", "file": {"left": "/path/to/1", "right": {"size": "1"}}},
        {"sample": "2", "file": {"left": ["/path/to/2", "/path/to/3"], "right": {"size": "1"}}}]}
    result = parse_workflow_output(workflow_outputs, None)
    assert len(result) == 4  # this includes incorrectly parsing "sample" key as file path string


def test_output_struct4_with_wdl_outputs():
    workflow_outputs = {"workflow.outputGroups4": [
        {"sample": "1", "file": {"left": "/path/to/1", "right": {"size": "1"}}},
        {"sample": "2", "file": {"left": ["/path/to/2", "/path/to/3"], "right": {"size": "2"}}}]}
    result = parse_workflow_output(workflow_outputs, wdl_outputs)
    assert len(result) == 2
    assert len(result["workflow.outputGroups4[0].file"].get("files")) == 1
    assert len(result["workflow.outputGroups4[1].file"].get("files")) == 2
    assert result["workflow.outputGroups4[0].file"].get("limskeys") == [1, 2, 3]
    assert result["workflow.outputGroups4[1].file"].get("limskeys") == [4, 5, 6]
    assert result["workflow.outputGroups4[0].file"].get("annotations") == {"size": "1", "r": "1"}
    assert result["workflow.outputGroups4[1].file"].get("annotations") == {"size": "2", "r": "1"}
    assert all([r.get("metatype") == "meta/type" for _, r in result.items()])


def test_output_struct5():
    workflow_outputs = {"workflow.outputGroups5": [
        {"key1": "1", "key2": "a", "file": "/path/to/1"},
        {"key1": "2", "key2": "b", "file": "/path/to/1"}]}
    result = parse_workflow_output(workflow_outputs, wdl_outputs)
    assert len(result) == 2
