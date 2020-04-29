from utils import get_metatype


def test_unknown():
    assert get_metatype("unknown.file.extension") == "application/octet-stream"


def test_specific():
    assert get_metatype("file.tar.gz") == "application/tar-gzip"


def test_general():
    assert get_metatype("file.gz") == "application/txt-gz"
