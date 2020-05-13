#!/usr/bin/env python3
"""
created 5/7/20 by ibackus
"""
from whylabs.logging.core.types import TypedDataConverter
from whylabs.logging.core.statistics import NumberTracker
from whylabs.logging.core.summaryconverters import from_number_tracker
from whylabs.logging.core.data import ColumnSummary, ColumnMessage


class ColumnProfile:
    """
    Parameters
    ----------
    name : str
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

    def __init__(self, name, number_tracker=None):
        self.column_name = name
        # self.counters = CountersTracker()
        # self.schemaTracker = SchemaTracker()
        if number_tracker is None:
            number_tracker = NumberTracker()
        self.number_tracker = number_tracker
        # self.stringTracker = StringTracker()

    def track(self, value):
        typed_data = TypedDataConverter.convert(value)
        if isinstance(typed_data, (float, int)):
            self.number_tracker.track(typed_data)
        elif isinstance(typed_data, bool):
            # TODO: implement boolean counter
            ...

    def to_summary(self):
        schema = 123
        # TODO: implement the real schema/type checking
        opts = {}
        if schema is not None:
            if isinstance(schema, str):
                ...
            elif isinstance(schema, (float, int)):
                numbersSummary = from_number_tracker(self.number_tracker)
                if numbersSummary is not None:
                    opts['number_summary'] = numbersSummary
        return ColumnSummary(**opts)

    def to_protobuf(self):
        return ColumnMessage(
            name=self.column_name,
            # counters=self.counters.toProtonbuf(),
            # schema=self.schemaTracker.to_protobuf(),
            numbers=self.number_tracker.to_protobuf(),
            # strings=self.stringTracker.to_protobuf(),
        )

    @staticmethod
    def from_protobuf(message):
        return ColumnProfile(
            message.name,
            # counters=<class>.from_protobuf(),
            # schemaTracker=<class>.from_protobuf(),
            numberTracker=NumberTracker.from_protobuf(message.numbers),
        )
