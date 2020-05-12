#!/usr/bin/env python3
"""
created 5/7/20 by ibackus
"""
from whylabs.logging.core.types import TypedDataConverter
from whylabs.logging.core.statistics import NumberTracker
from whylabs.logging.core.SummaryConverters import fromNumberTracker
from whylabs.logging.core.data import ColumnSummary, ColumnMessage


class ColumnProfile:
    """
    Parameters
    ----------
    columnName : str
        Name of the column profile

    TODO:
        * _NUMERIC_TYPES for number type checking
        * CountersTracker
        * SchemaTracker
        * StringTracker
        * Proper TypedDataConverter type checking
        * Multi-threading/parallelism
    """
    _NUMERIC_TYPES = None  # TODO: Implement

    def __init__(self, columnName, numberTracker=None):
        self.columnName = columnName
        # self.counters = CountersTracker()
        # self.schemaTracker = SchemaTracker()
        if numberTracker is None:
            numberTracker = NumberTracker()
        self.numberTracker = numberTracker
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

    def toProtobuf(self):
        return ColumnMessage(
            name=self.columnName,
            # counters=self.counters.toProtonbuf(),
            # schema=self.schemaTracker.toProtobuf(),
            numbers=self.numberTracker.toProtobuf(),
            # strings=self.stringTracker.toProtobuf(),
        )

    @staticmethod
    def fromProtbuf(message):
        return ColumnProfile(
            message.name,
            # counters=<class>.fromProtobuf(),
            # schemaTracker=<class>.fromProtobuf(),
            numberTracker=NumberTracker.fromProtobuf(message.numbers),
        )
