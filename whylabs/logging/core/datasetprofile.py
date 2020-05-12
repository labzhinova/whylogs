#!/usr/bin/env python3
"""
created 5/7/20 by ibackus 
"""
from whylabs.logging.core import ColumnProfile
from whylabs.logging.core.iterator import ColumnsChunkSegmentIterator
from whylabs.logging.core.data import DatasetSummary, DatasetMetadataSegment, \
    MessageSegment, DatasetProfileMessage
from uuid import uuid4


class _Pair:
    def __init__(self, column: ColumnProfile):
        self.name = column.columnName
        self.statistics = column.toColumnSummary()


class DatasetProfile:
    """
    Parameters
    ----------
    name : str
        Name of the dataset (e.g. timestamp string)
    timestamp : datetime.datetime
        Timestamp
    """
    # Note: we define the `Pair` here as a "nested" class to mimic the Java
    # implementation
    Pair = _Pair

    def __init__(self, name, timestamp, columns=None):
        self.name = name
        self.timestamp = timestamp
        if columns is None:
            columns = {}
        self.columns = columns

    @property
    def timestamp_ms(self):
        # TODO: Implement proper timestamp conversion
        return 1588978362910  # Some made up timestamp in milliseconds

    def track(self, columns, data=None):
        """
        Parameters
        ----------
        columns : str, dict
            Either the name of a column, or a dictionary specifying column
            names and the data (value) for each column
            If a string, `data` must be supplied.  Otherwise, `data` is
            ignored.
        data : object, None
            Value to track.  Specify if `columns` is a string.
        """
        if data is not None:
            self._trackSingleColumn(columns, data)
        else:
            for columnName, data in columns.items():
                self._trackSingleColumn(columnName, data)

    def _trackSingleColumn(self, columnName, data):
        try:
            prof = self.columns[columnName]
        except KeyError:
            prof = ColumnProfile(columnName)
            self.columns[columnName] = prof
        prof.track(data)

    def toSummary(self):
        column_summaries = {name: colprof.toSummary()
                            for name, colprof in self.columns.items()}
        return DatasetSummary(
            name=self.name,
            timestamp=self.timestamp_ms,
            columns=column_summaries
        )

    def _column_message_iterator(self):
        for col in self.columns.items():
            yield col.toProtobuf()

    def toChunkIterator(self):
        # Generate unique identifier
        marker = self.name + str(uuid4())

        # Generate metadata
        metadataSegment = MessageSegment(
            metadata=DatasetMetadataSegment(
                name=self.name,
                timestamp=self.timestamp_ms,
                marker=self.marker,
            )
        )

        yield metadataSegment

        chunkedColumns = self._column_message_iterator()
        for msg in ColumnsChunkSegmentIterator(chunkedColumns, marker):
            yield MessageSegment(columns=msg)

    def toProtobuf(self):
        return DatasetProfileMessage(
            name=self.name,
            timestamp=self.timestamp_ms,
            columns={k: v.toProtobuf() for k, v in self.columns.items()},
        )

    @staticmethod
    def fromProtobuf(message):
        # TODO: convert message timestamp from ms to datetime object
        dt = message.timestamp
        return DatasetProfile(
            message.name,
            dt,
            columns={k: ColumnProfile.fromProtbuf(v)
                     for k, v in message.columns.items()},
        )
