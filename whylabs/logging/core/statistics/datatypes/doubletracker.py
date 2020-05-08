#!/usr/bin/env python3
"""
created 5/7/20 by ibackus 
"""
import math
from whylabs.logging.core.data import DoublesMessage


class DoubleTracker:
    def __init__(self):
        self.min = math.inf
        self.max = -math.inf
        self.sum = 0.0
        self.count = 0

    def addLongs(self, longs):
        """
        Copy data from a LongTracker into this object, overwriting the current
        values.

        Parameters
        ----------
        longs : LongTracker
        """
        if longs is not None and longs.count != 0:
            # Copy data over from the longs, casting as floats
            self.min = float(longs.min)
            self.max = float(longs.max)
            self.sum = float(longs.sum)
            self.count = longs.count

    def getMean(self):
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

    def toProtobuf(self):
        return DoublesMessage(
            count=self.count,
            max=self.max,
            min=self.min,
            sum=self.sum
        )

    @staticmethod
    def fromProtobuf(message):
        x = DoubleTracker()
        x.count = message.count
        x.max = message.max
        x.min = message.min
        x.sum = message.sum
        return x
