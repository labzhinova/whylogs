#!/usr/bin/env python3
"""
created 5/7/20 by ibackus

TODO:
    * counter tracker
    * schema tracker
    * string tracker
    * numeric types
    * multi-threading/parallelism
    * python version of `TypedDataConverter`
"""
from whylogs.logging.core.types import TypedDataConverter
from whylogs.logging.core.statistics import NumberTracker
from whylogs.logging.core.SummaryConverters import fromNumberTracker
from whylogs.logging.core.data import ColumnSummary


class ColumnProfile:
    """
    Parameters
    ----------
    columnName : str
        Name of the column profile
    """
    _NUMERIC_TYPES = None  # TODO: Implement

    def __init__(self, columnName):
        self.columnName = columnName
        # self.counters = CountersTracker()
        # self.schemaTracker = SchemaTracker()
        self.numberTracker = NumberTracker()
        # self.stringTracker = StringTracker()

    def track(self, value):
        # TODO: Implement TypedDataConverter type checking
        typedData = TypedDataConverter.convert(value)
        if isinstance(typedData, (float, int)):
            self.numberTracker.track(typedData)
        elif isinstance(typedData, bool):
            # TODO: implement boolean counter
            ...

    def toColumnSummary(self):
        schema = 123
        # TODO: implement the real schema/type checking
        opts = {}
        if schema is not None:
            if isinstance(schema, str):
                ...
            elif isinstance(schema, (float, int)):
                numbersSummary = fromNumberTracker(self.numberTracker)
                if numbersSummary is not None:
                    opts['number_summary'] = numbersSummary
        return ColumnSummary(**opts)
