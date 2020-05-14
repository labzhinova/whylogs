#!/usr/bin/env python3
"""
created 5/7/20 by ibackus
TODO:
    * Date parsing compatible with EasyDateTimeParser (Java)

"""
CSV_READER_BATCH_SIZE = int(1e4)
OUTPUT_DATE_FORMAT = "%Y/%m/%d"


def write_protobuf(vals: list, fname):
    """
    Write a list of objects with a `to_protobuf()` method to a binary file.

    `vals` must be iterable
    """
    print("Writing to protobuf binary file: {}".format(fname))
    with open(fname, 'wb') as fp:
        for x in vals:

            msg = x.to_summary()
            n = msg.ByteSize()
            fp.write(bytes(n))
            print(f"Writing protobuf message of len: {n}")
            fp.write(msg.SerializeToString())


def df_to_records(df):
    """
    Convert a dataframe to a list of dictionaries, one per row, dropping null
    values
    """
    import pandas as pd
    return [{k: v for k, v in m.items() if pd.notnull(v)}
            for m in df.to_dict(orient='records')]

def csv_reader(f, date_format: str=None, **kwargs):
    """
    Wrapper for `pandas.read_csv` to return an iterator to return dict
    records for a CSV file

    See also `pandas.read_csv`

    Parameters
    ----------
    f : str, path object, or file-like object
        File to read from.  See `pandas.read_csv` documentation
    date_format : str
        If specified, string format for the date.  See `pd.datetime.strptime`
    **kwargs : passed to `pandas.read_csv`
    """
    import pandas as pd
    # NOTE TO SELF:
    date_parser = None
    if date_format is not None:
        date_parser = lambda x: pd.datetime.strptime(x, date_format)
    opts = {
        'chunksize': CSV_READER_BATCH_SIZE,
        'date_parser': date_parser
    }
    opts.update(kwargs)

    for batch in pd.read_csv(f, **opts):
        records = df_to_records(batch)
        for record in records:
            yield record


class Profiler:
    def __init__(self, ):
        ...


def run(input_path, datetime=None, delivery_stream=None, fmt=None,
        limit=-1, output_path=None, region=None, separator=None,
        overwrite=False):
    """
    Run the profiler on CSV data

    Parameters
    ----------
    input_path : str
        Input CSV file
    datetime : str
        Column containing timestamps.  If missing, we assume the dataset is
        running in batch mode
    delivery_stream : str
        [IGNORED] The delivery stream name
    fmt : str
        Format of the datetime column, used if `datetime` is specified.
        If not specified, the format will be attempt to be inferred.
    limit : int
        Limit the number of entries to processes
    output_path : str
        Specify the output path.  By default, the program will write to
        a file in the same input folder using the CSV file as a basename
    region : str
        [IGNORED] AWS region name for Firehose
    separator : str
        Record separator
    overwrite : bool
        Overwrite existing output files
    """
    datetime_col = datetime  # don't shadow the standard module name
    from whylabs.logging.core import DatasetProfile
    from whylabs.logging.core.data import DatasetSummaries
    from google.protobuf.json_format import MessageToJson
    from datetime import datetime
    import os

    # Parse arguments
    name = os.path.basename(input_path)
    parse_dates = False
    if datetime_col is not None:
        parse_dates = [datetime_col]
    nrows = None
    if limit > 0:
        nrows = limit
    if output_path is None:
        import random
        import time
        parent_folder = os.path.dirname(os.path.realpath(input_path))
        basename = os.path.splitext(os.path.basename(input_path))[0]
        epoch_minutes = int(time.time()/60)
        output_base = "{}.{}-{}-{}".format(
            basename, epoch_minutes, random.randint(100000, 999999),
            random.randint(100000, 999999))
        output_path = os.path.join(parent_folder, output_base + '.json')

    output_base = output_path
    if output_base.endswith('.json'):
        output_base = output_base[0:-len('.json')]
    binary_output_path = output_base + '.bin'
    for fname in (output_path, binary_output_path):
        if not overwrite and os.path.exists(fname):
            raise ValueError("Output file {} exists".format(fname))

    # Process records
    reader = csv_reader(input_path, fmt, parse_dates=parse_dates,
                        nrows=nrows, sep=separator)
    profiles = {}
    for record in reader:
        dt = record.get(datetime_col, datetime.utcnow())
        assert isinstance(dt, datetime)
        dt_str = dt.strftime(OUTPUT_DATE_FORMAT)
        try:
            ds = profiles[dt_str]
        except KeyError:
            ds = DatasetProfile(name, dt)
            profiles[dt_str] = ds
        ds.track(record)

    print("Finished collecting statistics")

    # Build summaries for the JSON output
    summaries = DatasetSummaries(
        profiles={k: v.to_summary() for k, v in profiles.items()}
    )
    with open(output_path, 'wt') as fp:
        print("Writing JSON summaries to: {}".format(output_path))
        fp.write(MessageToJson(summaries))

    write_protobuf(profiles.values(), binary_output_path)
    return profiles


if __name__ == '__main__':
    import argh
    argh.dispatch_command(run)
