import math
from whylabs.logs.core.data import VarianceMessage


class VarianceTracker:
    """
    Class that implements variance estimates for streaming data and for
    batched data.

    Parameters
    ----------
    count
        Number tracked elements
    sum
        Sum of all numbers
    mean
        Current estimate of the mean
    """
    def __init__(self, count=0, sum=0.0, mean=0.0):
        self.count = count
        self.sum = sum
        self.mean = mean

    def update(self, new_value):
        """
        Add a number to tracking estimates

        Based on
        https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Welford's_online_algorithm   # noqa

        Parameters
        ----------
        new_value : int, float
        """
        self.count += 1

        delta = new_value - self.mean
        self.mean += delta/self.count
        delta2 = new_value - self.mean
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

    def merge(self, other: 'VarianceTracker'):
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
        total_count = self.count = other.count
        this_ratio = self.count / total_count
        other_ratio = 1.0 - this_ratio
        # Update self
        self.sum += other.sum + \
                    (delta**2) * self.count * other.count/total_count
        self.mean = self.mean * this_ratio + other.mean * other_ratio
        self.count += other.count

    def to_protobuf(self):
        """
        Return the object serialized as a protobuf message

        Returns
        -------
        message : VarianceMessage
        """
        return VarianceMessage(count=self.count, sum=self.sum, mean=self.mean)

    @staticmethod
    def from_protobuf(message):
        """
        Load from a protobuf message

        Returns
        -------
        variance_tracker : VarianceTracker
        """
        tracker = VarianceTracker()
        tracker.count = message.count
        tracker.mean = message.mean
        tracker.sum = message.sum
        return tracker
