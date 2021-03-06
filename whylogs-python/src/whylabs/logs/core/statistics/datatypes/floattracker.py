import math

from whylabs.logs.proto import DoublesMessage


class FloatTracker:
    """
    Track statistics for floating point numbers

    Parameters
    ---------
    min
        Current min value
    max
        Current max value
    sum
        Sum of the numbers
    count
        Total count of numbers
    """
    def __init__(self, min: float=None, max: float=None,
                 sum: float=None, count: int=None):
        if min is None:
            min = math.inf
        if max is None:
            max = -math.inf
        if sum is None:
            sum = 0.0
        if count is None:
            count = 0
        self.min = min
        self.max = max
        self.sum = sum
        self.count = count

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
        """
        Calculate the current mean
        """
        try:
            return self.sum/self.count
        except ZeroDivisionError:
            return math.nan

    def update(self, value: float):
        """
        Add a number to the tracking statistics
        """
        # Python: force the value to be a float
        value = float(value)
        if value > self.max:
            self.max = value
        if value < self.min:
            self.min = value
        self.count += 1
        self.sum += value

    def merge(self, other):
        this_copy = FloatTracker(self.min, self.max, self.sum, self.count)
        if other.min < this_copy.min:
            this_copy.min = other.min
        if other.max > this_copy.max:
            this_copy.max = other.max
        this_copy.sum += other.sum
        this_copy.count += other.count
        return this_copy

    def to_protobuf(self):
        """
        Return the object serialized as a protobuf message

        Returns
        -------
        message : DoublesMessage
        """
        return DoublesMessage(
            count=self.count,
            max=self.max,
            min=self.min,
            sum=self.sum
        )

    @staticmethod
    def from_protobuf(message):
        """
        Load from a protobuf message

        Returns
        -------
        number_tracker : FloatTracker
        """
        return FloatTracker(
            min=message.min,
            max=message.max,
            sum=message.sum,
            count=message.count
        )
