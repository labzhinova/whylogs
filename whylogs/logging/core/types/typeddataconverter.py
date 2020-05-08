#!/usr/bin/env python3
"""
created 5/7/20 by ibackus

TODO: implement this using something other than yaml
"""
import yaml


class TypedDataConverter:
    @staticmethod
    def convert(data):
        if isinstance(data, str):
            data = yaml.safe_load(data)
        return data
