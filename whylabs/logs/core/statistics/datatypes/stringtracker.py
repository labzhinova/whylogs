# import datasketches
from datasketches import frequent_strings_sketch, update_theta_sketch, \
    theta_sketch
from whylabs.logs.core.data import StringsMessage, StringsSummary
from whylabs.logs.core.summaryconverters import from_sketch, from_string_sketch
from whylabs.logs.util import dsketch

MAX_ITEMS_SIZE = 32
MAX_SUMMARY_ITEMS = 100


class StringTracker:
    """
    Track statistics for strings

    Parameters
    ----------
    count
        Total number of processed values
    items
        Sketch for tracking string counts
    theta_sketch
        Sketch for approximate cardinality tracking
    """
    def __init__(self,
                 count: int=None,
                 items: frequent_strings_sketch=None,
                 theta_sketch: update_theta_sketch=None):
        if count is None:
            count = 0
        if items is None:
            items = frequent_strings_sketch(MAX_ITEMS_SIZE)
        if theta_sketch is None:
            theta_sketch = update_theta_sketch()
        self.count = count
        self.items = items
        self.theta_sketch = theta_sketch

    def update(self, value: str):
        """
        Add a string to the tracking statistics.

        If `value` is `None`, nothing will be done
        """
        if value is None:
            return

        self.count += 1
        self.theta_sketch.update(value)
        self.items.update(value)

    def to_protobuf(self):
        """
        Return the object serialized as a protobuf message

        Returns
        -------
        message : StringsMessage
        """
        return StringsMessage(
            count=self.count,
            items=self.items.serialize(),
            theta=self.theta_sketch.serialize(),
        )

    @staticmethod
    def from_protobuf(message: StringsMessage):
        """
        Load from a protobuf message

        Returns
        -------
        string_tracker : StringTracker
        """
        return StringTracker(
            count=message.count,
            items=dsketch.deserialize_frequent_strings_sketch(message.items),
            theta_sketch=theta_sketch.deserialize(message.theta)
        )

    def to_summary(self):
        """
        Generate a summary of the statistics

        Returns
        -------
        summary : StringsSummary
            Protobuf summary message.
        """
        if self.count == 0:
            return None
        unique_count = from_sketch(self.theta_sketch)
        opts = dict(
            unique_count=unique_count,
        )
        if unique_count.estimate < MAX_SUMMARY_ITEMS:
            frequent_strings = from_string_sketch(self.items)
            if frequent_strings is not None:
                opts['frequent'] = frequent_strings

        return StringsSummary(**opts)
