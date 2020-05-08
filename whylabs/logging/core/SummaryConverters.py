#!/usr/bin/env python3
"""
created 5/7/20 by ibackus 
"""
from whylabs.logging.core.statistics import NumberTracker
from whylabs.logging.core.data import NumberSummary, UniqueCountSummary
from datasketches import update_theta_sketch


def fromSketch(sketch: update_theta_sketch, num_std_devs=1):
    return UniqueCountSummary(
        estimate=sketch.get_estimate(),
        upper=sketch.get_upper_bound(num_std_devs),
        lower=sketch.get_lower_bound(num_std_devs)
    )


def fromNumberTracker(numberTracker: NumberTracker):
    """
    Construct a `NumberSummary` message from a `NumberTracker`
    """
    if numberTracker is None:
        return

    if numberTracker.variance.count == 0:
        return

    stddev = numberTracker.variance.stddev()
    doubles = numberTracker.doubles.toProtobuf()
    if doubles.count > 0:
        mean = doubles.getMean()
        min = doubles.min
        max = doubles.max
    else:
        mean = numberTracker.longs.getMean()
        min = float(numberTracker.longs.min)
        max = float(numberTracker.longs.max)

    # TODO: implement histogram
    # TODO: implement unique count from theta sketch
    uniqueCount = fromSketch(numberTracker.thetaSketch)

    return NumberSummary(
        count=numberTracker.variance.count,
        stddev=stddev,
        min=min,
        max=max,
        mean=mean,
        # histogram=histogram,
        unique_count=uniqueCount,
    )
