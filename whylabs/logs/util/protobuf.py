"""
"""
from whylabs.logs.util import varint
import google.protobuf.message


def _varint_delim_reader(fp):
    msg_bytes = varint.decode_stream(fp)
    while msg_bytes is not None:
        if msg_bytes <= 0:
            raise RuntimeError("Invalid message size: {}".format(msg_bytes))
        msg_raw = fp.read(msg_bytes)
        yield msg_raw
        msg_bytes = varint.decode_stream(fp)


def _varint_delim_iterator(f):
    """
    Return an iterator to read delimited protobuf messages.  The iterator will
    return protobuf messages one by one as raw `bytes` objects.
    """
    if isinstance(f, str):
        with open(f, 'rb') as fp:
            for msg in _varint_delim_reader(fp):
                yield msg
    else:
        for msg in _varint_delim_reader(f):
            yield msg


def multi_msg_reader(f, msg_class):
    """
    Return an iterator to iterate through protobuf messages in a multi-message
    protobuf file.

    See also: `write_multi_msg()`

    Parameters
    ----------
    f : str, file-object
        Filename or open file object to read from
    msg_class : class
        The Protobuf message class, gets instantiated with a call to
        `msg_class()`

    Returns
    -------
    msg_iterator
        Iterator which returns protobuf messages
    """
    for raw_msg in _varint_delim_iterator(f):
        msg = msg_class()
        msg = msg.FromString(raw_msg)
        yield msg


def read_multi_msg(f, msg_class):
    """
    Wrapper for `multi_msg_reader` which reads all the messages and returns
    them as a list.
    """
    return [x for x in multi_msg_reader(f, msg_class)]


def _encode_one_msg(msg: google.protobuf.message):
    n = msg.ByteSize()
    return varint.encode(n) + msg.SerializeToString()


def _write_multi_msg(msgs: list, fp):
    for msg in msgs:
        fp.write(_encode_one_msg(msg))


def write_multi_msg(msgs: list, f):
    """
    Write a list (or iterator) of protobuf messages to a file.

    The multi-message file format is a binary format with:

        <varint MessageBytesSize><message>

    Which is repeated, where the len(message) in bytes is `MessageBytesSize`

    Parameters
    ----------
    msgs : list, iterable
        Protobuf messages to write to disk
    f : str, file-object
        Filename or open binary file object to write to
    """
    if isinstance(f, str):
        with open(f, 'wb') as fp:
            _write_multi_msg(msgs, fp)
    else:
        # Assume we have an already open file
        _write_multi_msg(msgs, f)
