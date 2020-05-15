#!/usr/bin/env python3
"""
created 5/5/20 by ibackus 
"""
import datasketches

from whylabs.logs.core.data import NumberSummary
from whylabs.logs.core.summaryconverters import from_sketch
from whylabs.logs.core.statistics.datatypes import VarianceTracker, \
    IntTracker, FloatTracker
from whylabs.logs.core.data import NumbersMessage


class NumberTracker:
    """

    """
    def __init__(self, variance=None, floats=None, ints=None):
        # Our own trackers
        if variance is None:
            variance = VarianceTracker()
        if floats is None:
            floats = FloatTracker()
        if ints is None:
            ints = IntTracker()
        self.variance = variance
        self.floats = floats
        self.ints = ints

        # Sketches
        self.theta_sketch = datasketches.update_theta_sketch()
        # TODO: find a way to implement the histogram sketch
        self.histogram = None

    def track(self, number):
        """

        """
        self.variance.update(number)
        self.theta_sketch.update(number)
        # TODO: histogram update
        # Update floats/ints counting
        f_value = float(number)
        if self.floats.count > 0:
            self.floats.update(f_value)
        # Note: this type checking is fragile in python.  May want to include
        # numpy.integer in the type check
        elif isinstance(number, int):
            self.ints.update(number)
        else:
            self.floats.add_integers(self.ints)
            self.ints.reset()
            self.floats.update(f_value)

    def to_protobuf(self):
        """
        """
        opts = dict(
            variance=self.variance.to_protobuf(),
            theta=self.theta_sketch.serialize(),
            # TODO: add histogram
        )
        if self.floats.count > 0:
            opts['doubles'] = self.floats.to_protobuf()
        elif self.ints.count > 0:
            opts['longs'] = self.ints.to_protobuf()
        msg = NumbersMessage(**opts)
        return msg

    @staticmethod
    def from_protobuf(message):
        """
        """
        theta_sketch = datasketches.update_theta_sketch.deserialize(
            message.theta)
        tracker = NumberTracker()
        tracker.theta_sketch = theta_sketch
        tracker.variance = VarianceTracker.from_protobuf(message.variance)
        # TODO: de-serialize histogram
        if message.HasField('doubles'):
            tracker.floats = FloatTracker.from_protobuf(message.doubles)
        if message.HasField('longs'):
            tracker.ints = IntTracker.from_protobuf(message.longs)
        return tracker


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
        mean = number_tracker.floats.mean()
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