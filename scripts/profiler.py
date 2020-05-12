#!/usr/bin/env python3
"""
created 5/7/20 by ibackus
TODO:
    * Date parsing compatible with EasyDateTimeParser (Java)

"""
# import pandas as pd
# import argh

class Profiler:
    def __init__(self, ):
        ...


def run(input_path, datetime=None, delivery_stream=None, fmt=None,
        limit=-1, output_path=None, region=None, separator=None):
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
        The delivery stream name
    fmt : str
        Format of the datetime column.  Must be specified if the datetime
        column is specified.  For epoch second use 'epoch' and 'epochMillis'
        for milliseconds
    limit : int
        Limit the number of entries to processes
    output_path : str
        Specify the output path.  By default, the program will write to
        a file in the same input folder using the CSV file as a basename
    region : str
        AWS region name for Firehose
    separator : str
        Record separator
    """
    print(locals())


if __name__ == '__main__':
    import argh
    argh.dispatch_command(run)
