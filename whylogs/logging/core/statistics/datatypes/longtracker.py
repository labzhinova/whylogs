#!/usr/bin/env python3
"""
created 5/7/20 by ibackus 
"""
import math
from whylogs.logging.core.data import LongsMessage


class LongTracker:
    def __init__(self):
        self.reset()

    def reset(self):
        # NOTE: math.inf is a float, giving a possible issue with a return
        # type.  There is no maximum integer value in python3
        # TODO: Should we replace this with a very large integer value?
        self.min = math.inf
        self.max = -math.inf
        self.sum = 0
        self.count = 0

    def getMean(self):
        try:
            return self.sum/self.count
        except ZeroDivisionError:
            return None

    def update(self, value):
        if value > self.max:
            self.max = value
        if value < self.min:
            self.min = value
        self.count += 1
        self.sum += value

    def toProtobuf(self):
        return LongsMessage(
            count=self.count,
            max=self.max,
            min=self.min,
            sum=self.sum,
        )

    @staticmethod
    def fromProtobuf(message):
        x = LongTracker()
        x.count = message.count
        x.max = message.max
        x.min = message.min
        x.sum = message.sum
        return x
