#!/usr/bin/env python3
"""
created 5/7/20 by ibackus 
"""
from whylabs.logging.core.statistics import NumberTracker
from whylabs.logging.core.data import NumberSummary, UniqueCountSummary
from datasketches import update_theta_sketch


def from_sketch(sketch: update_theta_sketch, num_std_devs=1):
    return UniqueCountSummary(
        estimate=sketch.get_estimate(),
        upper=sketch.get_upper_bound(num_std_devs),
        lower=sketch.get_lower_bound(num_std_devs)
    )


def from_number_tracker(number_tracker: NumberTracker):
    """
    Construct a `NumberSummary` message from a `NumberTracker`
    """
    if number_tracker is None:
        return

    if number_tracker.variance.count == 0:
        return

    stddev = number_tracker.variance.stddev()
    doubles = number_tracker.floats.to_protobuf()
    if doubles.count > 0:
        mean = doubles.mean()
        min = doubles.min
        max = doubles.max
    else:
        mean = number_tracker.ints.mean()
        min = float(number_tracker.ints.min)
        max = float(number_tracker.ints.max)

    # TODO: implement histogram
    # TODO: implement unique count from theta sketch
    unique_count = from_sketch(number_tracker.theta_sketch)

    return NumberSummary(
        count=number_tracker.variance.count,
        stddev=stddev,
        min=min,
        max=max,
        mean=mean,
        # histogram=histogram,
        unique_count=unique_count,
    )
