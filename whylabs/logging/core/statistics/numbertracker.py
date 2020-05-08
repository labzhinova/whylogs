#!/usr/bin/env python3
"""
created 5/5/20 by ibackus 
"""
import datasketches
from whylabs.logging.core.statistics.datatypes import VarianceTracker, \
    LongTracker, DoubleTracker
from whylabs.logging.core.data import NumbersMessage


class NumberTracker:
    """

    """
    def __init__(self):
        # TODO: add keyword argument intialization
        # Our own trackers
        self.variance = VarianceTracker()
        self.doubles = DoubleTracker()
        self.longs = LongTracker()

        # Sketches
        self.thetaSketch = datasketches.update_theta_sketch()
        # TODO: find a way to implement the histogram sketch
        self.histogram = None

    def track(self, number):
        """

        """
        self.variance.update(number)
        self.thetaSketch.update(number)
        # TODO: histogram update
        # Update doubles/longs counting
        dValue = float(number)
        if self.doubles.count > 0:
            self.doubles.update(dValue)
        # Note: this type checking is fragile in python.  May want to include
        # numpy.integer in the type check
        elif isinstance(number, int):
            self.longs.update(number)
        else:
            self.doubles.addLongs(self.longs)
            self.longs.reset()
            self.doubles.update(dValue)

    def toProtobuf(self):
        """
        """
        opts = dict(
            variance=self.variance.toProtobuf(),
            theta=self.thetaSketch.serialize(),
            # TODO: add histogram
        )
        if self.doubles.count > 0:
            opts['doubles'] = self.doubles.toProtobuf()
        elif self.longs.count > 0:
            opts['longs'] = self.longs.toProtobuf()
        msg = NumbersMessage(**opts)
        return msg

    @staticmethod
    def fromProtobuf(message):
        """
        """
        theta_sketch = datasketches.update_theta_sketch.deserialize(
            message.theta)
        tracker = NumberTracker()
        tracker.thetaSketch = theta_sketch
        tracker.variance = VarianceTracker.fromProtobuf(message.variance)
        # TODO: de-serialize histogram
        if message.HasField('doubles'):
            tracker.doubles = DoubleTracker.fromProtobuf(message.doubles)
        if message.HasField('longs'):
            tracker.longs = LongTracker.fromProtobuf(message.longs)
        return tracker

