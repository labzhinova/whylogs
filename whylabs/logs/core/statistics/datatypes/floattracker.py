#!/usr/bin/env python3
"""
created 5/7/20 by ibackus 
"""
import math
from whylabs.logs.core.data import DoublesMessage


class FloatTracker:
    def __init__(self):
        self.min = math.inf
        self.max = -math.inf
        self.sum = 0.0
        self.count = 0

    def add_integers(self, tracker):
        """
        Copy data from a IntTracker into this object, overwriting the current
        values.

        Parameters
        ----------
        tracker : IntTracker
        """
        if tracker is not None and tracker.count != 0:
            # Copy data over from the ints, casting as floats
            self.min = float(tracker.min)
            self.max = float(tracker.max)
            self.sum = float(tracker.sum)
            self.count = tracker.count

    def mean(self):
        return self.sum/self.count

    def update(self, value):
        # Python: force the value to be a float
        value = float(value)
        if value > self.max:
            self.max = value
        if value < self.min:
            self.min = value
        self.count += 1
        self.sum += value

    def to_protobuf(self):
        return DoublesMessage(
            count=self.count,
            max=self.max,
            min=self.min,
            sum=self.sum
        )

    @staticmethod
    def from_protobuf(message):
        x = FloatTracker()
        x.count = message.count
        x.max = message.max
        x.min = message.min
        x.sum = message.sum
        return x
