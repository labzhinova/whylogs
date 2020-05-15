"""
"""
from whylabs.logs.core.data import Counters
from google.protobuf.wrappers_pb2 import Int64Value


class CountersTracker:
    """
    Counting tracker

    Parameters
    ----------
    count : int, optional
        Current number of parameters
    true_count : int, optional
        Number of boolean values
    null_count : int, optional
        Number of nulls encountered
    """
    def __init__(self, count=0, true_count=0, null_count=0):
        self.count = count
        self.true_count = true_count
        self.null_count = null_count

    def increment_count(self):
        self.count += 1

    def increment_bool(self):
        self.true_count += 1

    def increment_null(self):
        self.null_count += 1

    def to_protobuf(self):
        return Counters(
            count=self.count,
            true_count=Int64Value(value=self.true_count),
            null_count=Int64Value(value=self.null_count),
        )

    @staticmethod
    def from_protobuf(message: Counters):
        return CountersTracker(
            count=message.count,
            true_count=message.true_count.value,
            null_count=message.null_count.value,
        )
