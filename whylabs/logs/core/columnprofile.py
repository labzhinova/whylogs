#!/usr/bin/env python3
"""
created 5/7/20 by ibackus
"""
from whylabs.logs.core.types import TypedDataConverter
from whylabs.logs.core.statistics import NumberTracker
from whylabs.logs.core.summaryconverters import from_string_sketch
from whylabs.logs.core.statistics import from_number_tracker
from whylabs.logs.core.data import ColumnSummary, ColumnMessage, InferredType
from whylabs.logs.core.statistics import CountersTracker, SchemaTracker
from whylabs.logs.core.statistics.datatypes import StringTracker
_TYPES = InferredType.Type
_NUMERIC_TYPES = set([_TYPES.FRACTIONAL, _TYPES.INTEGRAL])


class ColumnProfile:
    """
    Parameters
    ----------
    name : str
        Name of the column profile

    TODO:
        * _NUMERIC_TYPES for number type checking
        * SchemaTracker
        * Proper TypedDataConverter type checking
        * Multi-threading/parallelism
    """
    _NUMERIC_TYPES = None  # TODO: Implement

    def __init__(self, name, number_tracker=None, string_tracker=None,
                 schema_tracker=None, counters=None):
        # Handle default values
        if counters is None:
            counters = CountersTracker()
        if number_tracker is None:
            number_tracker = NumberTracker()
        if string_tracker is None:
            string_tracker = StringTracker()
        if schema_tracker is None:
            schema_tracker = SchemaTracker()
        # Assign values
        self.column_name = name
        self.number_tracker = number_tracker
        self.string_tracker = string_tracker
        self.schema_tracker = schema_tracker
        self.counters = counters

    def track(self, value):
        self.counters.increment_count()
        if value is None:
            self.counters.increment_null()
            return

        # TODO: ignore this if we already know the data type
        if isinstance(value, str):
            self.string_tracker.update(value)

        # TODO: Implement real typed data conversion
        typed_data = TypedDataConverter.convert(value)
        dtype = TypedDataConverter.get_type(typed_data)
        self.schema_tracker.track(dtype)

        if isinstance(typed_data, bool):
            # Note: bools are sub-classes of ints in python, so we should check
            # for bool type first
            self.counters.increment_bool()
        elif isinstance(typed_data, (float, int)):
            self.number_tracker.track(typed_data)

    def to_summary(self):
        schema = None
        if self.schema_tracker is not None:
            schema = self.schema_tracker.to_summary()
        # TODO: implement the real schema/type checking
        opts = dict(
            counters=self.counters.to_protobuf()
        )
        if schema is not None:
            opts['schema'] = schema
            dtype = schema.inferred_type.type
            if dtype == _TYPES.STRING:
                if self.string_tracker is not None:
                    string_summary = self.string_tracker.to_summary()
                    opts['string_summary'] = string_summary

            elif dtype in _NUMERIC_TYPES:
                numbers_summary = from_number_tracker(self.number_tracker)
                if numbers_summary is not None:
                    opts['number_summary'] = numbers_summary
        return ColumnSummary(**opts)

    def to_protobuf(self):
        return ColumnMessage(
            name=self.column_name,
            counters=self.counters.to_protobuf(),
            schema=self.schema_tracker.to_protobuf(),
            numbers=self.number_tracker.to_protobuf(),
            strings=self.string_tracker.to_protobuf(),
        )

    @staticmethod
    def from_protobuf(message):
        return ColumnProfile(
            message.name,
            counters=CountersTracker.from_protobuf(message.counters),
            schema_tracker=SchemaTracker.from_protobuf(message.schema),
            number_tracker=NumberTracker.from_protobuf(message.numbers),
            string_tracker=StringTracker.from_protobuf(message.strings),
        )
