#!/usr/bin/env python3
"""
created 5/11/20 by ibackus 
"""
from whylabs.logging.core.data import ColumnsChunkSegment

COLUMN_CHUNK_MAX_LEN_IN_BYTES = int(1e6) - 10

def ColumnsChunkSegmentIterator(iterator, marker):
    # Initialize
    maxChunkLength = COLUMN_CHUNK_MAX_LEN_IN_BYTES
    contentLength = 0
    message = ColumnsChunkSegment(marker=marker)

    # Loop over columns
    for columnMessage in iterator:
        messageLen = columnMessage.ByteSize()
        candidateContentSize = contentLength + messageLen
        canItemBeAppended = candidateContentSize <= maxChunkLength
        if canItemBeAppended:
            message.columns.append(columnMessage)
            contentLength = candidateContentSize
        else:
            yield message
            message = ColumnsChunkSegment(marker=marker)
            message.columns.append(columnMessage)
            contentLength = messageLen

    # Take care of any remaining messages
    if len(message.columns) > 0:
        yield message
