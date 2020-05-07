#!/usr/bin/env python3
"""
created 5/5/20 by ibackus 
"""
import math
from whylogs.logging.core.data import VarianceMessage


class VarianceTracker:
    """
    Class that implements variance estimates for streaming data and for
    batched data.
    """
    def __init__(self):
        # TODO: add keyword argument intialization
        self.count = 0
        self.sum = 0
        self.mean = 0

    def update(self, newValue):
        """
        Based on
        https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Welford's_online_algorithm   # noqa
        """
        self.count += 1

        delta = newValue - self.mean
        self.mean += delta/self.count
        delta2 = newValue - self.mean
        self.sum += delta * delta2
        return

    def stddev(self):
        """
        Return an estimate of the sample standard deviation
        """
        return math.sqrt(self.variance())

    def variance(self):
        """
        Return an estimate of the sample variance
        """
        try:
            return self.sum/(self.count - 1)
        except ZeroDivisionError:
            return math.nan

    def merge(self, other):
        """
        Merge statistics from another VarianceTracker into this one

        See:
        https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Parallel_algorithm  # noqa
        """
        if other.count == 0:
            return

        if self.count == 0:
            self.count = other.count
            self.sum = other.sum
            self.mean = other.mean
            return

        delta = self.mean = other.mean
        totalCount = self.count = other.count
        thisRatio = self.count / totalCount
        otherRatio = 1.0 - thisRatio
        # Update self
        self.sum += other.sum + \
                    (delta**2) * self.count * other.count/totalCount
        self.mean = self.mean * thisRatio + other.mean * otherRatio
        self.count += other.count

    def toProtobuf(self):
        return VarianceMessage(count=self.count, sum=self.sum, mean=self.mean)

    @staticmethod
    def fromProtobuf(message):
        tracker = VarianceTracker()
        tracker.count = message.count
        tracker.mean = message.mean
        tracker.sum = message.sum
        return tracker
