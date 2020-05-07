#!/usr/bin/env python3
"""
created 5/5/20 by ibackus 
"""
import datasketches
from whylogs.logging.core.statistics.datatypes import VarianceTracker
from whylogs.logging.core.data import NumbersMessage


class NumberTracker:
    """

    """
    def __init__(self):
        # TODO: add keyword argument intialization
        # Our own trackers.  TODO: implement our own trackers
        self.variance = VarianceTracker()
        self.doubles = None
        self.longs = None

        # Sketches
        self.thetaSketch = datasketches.update_theta_sketch()
        self.histogram = None

    def track(self, number):
        self.variance.update(number)
        self.thetaSketch.update(number)
        # TODO: histogram update
        # TODO: update self.doubles and self.longs

    def toProtobuf(self):
        """
        """
        msg = NumbersMessage(
            variance=self.variance.toProtobuf(),
            theta=self.thetaSketch.serialize(),
            # TODO: add doubles, longs, and histogram
        )
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
        # TODO: de-serialize histogram, and implement "doubles/longs" trackers

        return tracker


