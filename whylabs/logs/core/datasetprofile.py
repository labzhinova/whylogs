from whylabs.logs.core.data import ColumnsChunkSegment
from whylabs.logs.core import ColumnProfile
from whylabs.logs.core.data import DatasetSummary, DatasetMetadataSegment, \
    MessageSegment, DatasetProfileMessage
from uuid import uuid4
import datetime

COLUMN_CHUNK_MAX_LEN_IN_BYTES = int(1e6) - 10


class DatasetProfile:
    """
    Statistics tracking for a dataset.

    A dataset refers to a collection of columns.

    Parameters
    ----------
    name : str
        Name of the dataset (e.g. timestamp string)
    timestamp : datetime.datetime
        Timestamp of the dataset
    columns : dict
        Dictionary lookup of `ColumnProfile`s
    """
    def __init__(self, name: str, timestamp: datetime.datetime,
                 columns: dict=None):
        self.name = name
        self.timestamp = timestamp
        if columns is None:
            columns = {}
        self.columns = columns

    @property
    def timestamp_ms(self):
        """
        Return the timestamp value in epoch milliseconds
        """
        # TODO: Implement proper timestamp conversion
        return 1588978362910  # Some made up timestamp in milliseconds

    def track(self, columns, data=None):
        """
        Add value(s) to tracking statistics for column(s)

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
            self._track_single_column(columns, data)
        else:
            for column_name, data in columns.items():
                self._track_single_column(column_name, data)

    def _track_single_column(self, column_name, data):
        try:
            prof = self.columns[column_name]
        except KeyError:
            prof = ColumnProfile(column_name)
            self.columns[column_name] = prof
        prof.track(data)

    def to_summary(self):
        """
        Generate a summary of the statistics

        Returns
        -------
        summary : DatasetSummary
            Protobuf summary message.
        """
        column_summaries = {name: colprof.to_summary()
                            for name, colprof in self.columns.items()}
        return DatasetSummary(
            name=self.name,
            timestamp=self.timestamp_ms,
            columns=column_summaries
        )

    def _column_message_iterator(self):
        for col in self.columns.items():
            yield col.to_protobuf()

    def chunk_iterator(self):
        """
        Generate an iterator to iterate over chunks of data
        """
        # Generate unique identifier
        marker = self.name + str(uuid4())

        # Generate metadata
        yield MessageSegment(
            metadata=DatasetMetadataSegment(
                name=self.name,
                timestamp=self.timestamp_ms,
                marker=self.marker,
            )
        )

        chunked_columns = self._column_message_iterator()
        for msg in columns_chunk_iterator(chunked_columns, marker):
            yield MessageSegment(columns=msg)

    def to_protobuf(self):
        """
        Return the object serialized as a protobuf message

        Returns
        -------
        message : DatasetProfileMessage
        """
        return DatasetProfileMessage(
            name=self.name,
            timestamp=self.timestamp_ms,
            columns={k: v.to_protobuf() for k, v in self.columns.items()},
        )

    @staticmethod
    def from_protobuf(message):
        """
        Load from a protobuf message

        Returns
        -------
        dataset_profile : DatasetProfile
        """
        # TODO: convert message timestamp from ms to datetime object
        dt = message.timestamp
        return DatasetProfile(
            message.name,
            dt,
            columns={k: ColumnProfile.from_protobuf(v)
                     for k, v in message.columns.items()},
        )


def columns_chunk_iterator(iterator, marker: str):
    """
    Create an iterator to return column messages in batches

    Parameters
    ----------
    iterator
        An iterator which returns protobuf column messages
    marker
        Value used to mark a group of column messages
    """
    # Initialize
    max_len = COLUMN_CHUNK_MAX_LEN_IN_BYTES
    content_len = 0
    message = ColumnsChunkSegment(marker=marker)

    # Loop over columns
    for col_message in iterator:
        message_len = col_message.ByteSize()
        candidate_content_size = content_len + message_len
        if candidate_content_size <= max_len:
            # Keep appending columns
            message.columns.append(col_message)
            content_len = candidate_content_size
        else:
            yield message
            message = ColumnsChunkSegment(marker=marker)
            message.columns.append(col_message)
            content_len = message_len

    # Take care of any remaining messages
    if len(message.columns) > 0:
        yield message
