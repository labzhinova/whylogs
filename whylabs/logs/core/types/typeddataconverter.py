#!/usr/bin/env python3
"""
created 5/7/20 by ibackus

TODO: implement this using something other than yaml
"""
import yaml
from whylabs.logs.core.data import InferredType
_TYPES = InferredType.Type


class TypedDataConverter:
    @staticmethod
    def convert(data):
        if isinstance(data, str):
            data = yaml.safe_load(data)
        return data

    @staticmethod
    def get_type(typed_data):
        if typed_data is None:
            dtype = _TYPES.NULL
        elif isinstance(typed_data, bool):
            dtype = _TYPES.BOOLEAN
        elif isinstance(typed_data, float):
            dtype = _TYPES.FRACTIONAL
        elif isinstance(typed_data, int):
            dtype = _TYPES.INTEGRAL
        elif isinstance(typed_data, str):
            dtype = _TYPES.STRING
        return dtype
