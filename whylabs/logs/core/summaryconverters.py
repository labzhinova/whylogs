#!/usr/bin/env python3
"""
created 5/7/20 by ibackus 
"""
from whylabs.logs.core.data import UniqueCountSummary, \
    FrequentStringsSummary
from datasketches import update_theta_sketch, frequent_strings_sketch, \
    frequent_items_error_type


def from_sketch(sketch: update_theta_sketch, num_std_devs=1):
    return UniqueCountSummary(
        estimate=sketch.get_estimate(),
        upper=sketch.get_upper_bound(num_std_devs),
        lower=sketch.get_lower_bound(num_std_devs)
    )


def from_string_sketch(sketch: frequent_strings_sketch):
    frequent_items = sketch.get_frequent_items(
        frequent_items_error_type.NO_FALSE_NEGATIVES)
    # Note: frequent items is a list of tuples containing info about the
    # most frequent strings and their count:
    # [(string, est_count, lower bound, upper bound)]
    if len(frequent_items) == 0:
        return

    items = [{'value': x[0], 'estimate': x[1]} for x in frequent_items]
    return FrequentStringsSummary(items=items)


