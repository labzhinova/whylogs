#!/usr/bin/env python3
"""
created 5/7/20 by ibackus 
"""
from whylabs.logging.core import ColumnProfile

class DatasetProfile:
    """
    Parameters
    ----------
    name : str
        Name of the dataset (e.g. timestamp string)
    timestamp : datetime.datetime
        Timestamp
    """
    def __init__(self, name, timestamp):
        self.name = name
        self.timestamp = timestamp
        self.columns = {}

    def track(self, columnName, data):
        self._trackSingleColumn(columnName, data)

    def _trackSingleColumn(self, columnName, data):
        try:
            prof = self.columns[columnName]
        except KeyError:
            prof = ColumnProfile(columnName)
            self.columns[columnName] = prof
        prof.track(data)
