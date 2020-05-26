from logging import getLogger

from whylabs.logs.util.data import getter, remap, get_valid_filename
from whylabs.logs.core.data import ColumnsChunkSegment
from whylabs.logs.core import ColumnProfile
from whylabs.logs.core.data import DatasetSummary, DatasetMetadataSegment, \
    MessageSegment, DatasetProfileMessage
from whylabs.logs.core.types.typeddataconverter import TYPES
from uuid import uuid4
import datetime


COLUMN_CHUNK_MAX_LEN_IN_BYTES = int(1e6) - 10
TYPENUM_COLUMN_NAMES = {k: 'type_' + k.lower() + '_count' for k in
                        TYPES.keys()}


SCALAR_NAME_MAPPING = {
    'counters': {
        'count': 'count',
        'null_count': {'value': 'null_count'},
        'true_count': {'value': 'bool_count'},
    },
    'number_summary': {
        'count': 'numeric_count',
        'max': 'max',
        'mean': 'mean',
        'min': 'min',
        'stddev': 'stddev',
        'unique_count': {
            'estimate': 'nunique_numbers',
            'lower': 'nunique_numbers_lower',
            'upper': 'nunique_numbers_upper'
        }
    },
    'schema': {
        'inferred_type': {
            'type': 'inferred_dtype',
            'ratio': 'dtype_fraction',
        },
        'type_counts': TYPENUM_COLUMN_NAMES,
    },
    'string_summary': {
        'unique_count': {
            'estimate': 'nunique_str',
            'lower': 'nunique_str_lower',
            'upper': 'ununique_str_upper'
        }
    }
}


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

    def flat_summary(self):
        """
        Generate and flatten a summary of the statistics

        Returns
        -------
        summary : pd.DataFrame
            Per-column summary statistics
        hist : dict
            Dictionary of histograms with (column name, histogram) key, value
            pairs.  Histograms are formatted as a `pandas.Series`
        frequent_strings : dict
            Dictionary of frequent string counts with (column name, counts)
            key, val pairs.  `counts` are a pandas Series.
        """
        summary = self.to_summary()
        return flatten_summary(summary)

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


def flatten_summary(dataset_summary: DatasetSummary):
    """
    Flatten a DatasetSummary

    Parameters
    ----------
    dataset_summary : DatasetSummary
        Summary to flatten

    Returns
    -------
    A dictionary with the following keys:

    summary : pd.DataFrame
        Per-column summary statistics
    hist : pd.Series
        Series of histogram Series with (column name, histogram) key, value
        pairs.  Histograms are formatted as a `pandas.Series`
    frequent_strings : pd.Series
        Series of frequent string counts with (column name, counts)
        key, val pairs.  `counts` are a pandas Series.
    """
    hist = flatten_dataset_histograms(dataset_summary)
    frequent_strings = flatten_dataset_frequent_strings(dataset_summary)
    summary = get_dataset_frame(dataset_summary)
    return {
        'summary': summary,
        'hist': hist,
        'frequent_strings': frequent_strings
    }


def flatten_dataset_histograms(dataset_summary):
    """
    Flatten histograms from a dataset summary
    """
    histograms = {}

    for col_name, col in dataset_summary.columns.items():
        try:
            hist = getter(getter(col, 'number_summary'), 'histogram')
            if len(hist.bins) > 1:
                histograms[col_name] = {
                    'bin_edges': list(hist.bins),
                    'counts': list(hist.counts),
                }
        except KeyError:
            continue
    return histograms


def flatten_dataset_frequent_strings(dataset_summary):
    """
    Flatten frequent strings summaries from a dataset summary
    """
    frequent_strings = {}

    for col_name, col in dataset_summary.columns.items():
        try:
            item_summary = getter(getter(col, 'string_summary'), 'frequent')\
                .items
            items = {}
            for item in item_summary:
                items[item.value] = int(item.estimate)
            if len(items) > 0:
                frequent_strings[col_name] = items
        except KeyError:
            continue

    return frequent_strings


def get_dataset_frame(dataset_summary, mapping: dict=None):
    """
    Get a dataframe from scalar values flattened from a dataset summary

    Returns
    -------
    summary : pd.DataFrame
        Scalar values, flattened and re-named according to `mapping`
    """
    import pandas as pd
    if mapping is None:
        mapping = SCALAR_NAME_MAPPING
    col_out = {}
    for k, col in dataset_summary.columns.items():
        col_out[k] = remap(col, SCALAR_NAME_MAPPING)
    scalar_summary = pd.DataFrame(col_out).T
    scalar_summary.index.name = 'column'
    return scalar_summary.reset_index()


def write_flat_dataset_summary(summary, prefix: str, dataframe_fmt: str='csv'):
    """
    Utility to write a flattened dataset summary to disk.

    Parameters
    ----------
    summary : dict, DatasetSummary
        The dataset summary.  Either a `DatasetSummary` protobuf message or the
        the dictionary output of `flatten_summary()`
    prefix : str
        Output path prefix, used to construct output file names.
    dataframe_fmt : str
        File format for the summary table.  Other files will be output as
        json.  Formats: 'csv', 'parquet'

    Returns
    -------
    filenames : dict
        Dictionary containing output paths
    """
    import json
    if not isinstance(summary, dict):
        summary = flatten_summary(summary)
    logger = getLogger(__name__)

    # Extract the already flattened summary data
    df = summary['summary']
    hist = summary['hist']
    strings = summary['frequent_strings']

    # Save summary table
    df_name = f'{prefix}_summary.{dataframe_fmt}'
    if dataframe_fmt == 'csv':
        df.to_csv(df_name, index=False)
    elif dataframe_fmt == 'parquet':
        df.to_parquet(df_name, engine='pyarrow', compression='snappy')
    else:
        raise ValueError(f"Unrecognized format: {dataframe_fmt}")

    # Save per-column histograms
    hist_name = f'{prefix}_histogram.json'
    json.dump(hist, open(hist_name, 'wt'), indent=4)
    logger.debug(f'Saved histograms to: {hist_name}')

    # Save per-column string counts
    strings_name = f'{prefix}_strings.json'
    json.dump(strings, open(strings_name, 'wt'), indent=4)
    logger.debug(f'Saved frequent string counts to: {strings_name}')

    return {
        'dataframe': df_name,
        'histogram': hist_name,
        'strings': strings_name
    }


def write_flat_summaries(summaries, prefix: str,
                         dataframe_fmt: str='csv'):
    """
    Utility to write flattened `DatasetSummaries` to disk.

    Parameters
    ----------
    summaries : DatasetSummaries
        DatasetSummaries protobuf message
    prefix : str
        Output path prefix, used to construct output file names.
    dataframe_fmt : str
        File format for the summary tables.  Other files will be output as
        json.  Formats: 'csv', 'parquet'

    Returns
    -------
    filenames : dict
        Dictionary containing output filenames
    """
    from collections import defaultdict
    fnames = defaultdict(list)
    for name, summary in summaries.profiles.items():
        fullprefix = prefix + '_' + get_valid_filename(name)
        x = write_flat_dataset_summary(summary, fullprefix, dataframe_fmt)
        for k, v in x.items():
            fnames[k].append(v)
    return dict(fnames)
