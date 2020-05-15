"""
"""
import datasketches
from whylabs.logs.core.data import StringsMessage, StringsSummary
from whylabs.logs.core.summaryconverters import from_sketch, from_string_sketch

MAX_ITEMS_SIZE = 32
MAX_SUMMARY_ITEMS = 100


class StringTracker:
    def __init__(self, count=None, items=None, theta_sketch=None):
        if count is None:
            count = 0
        if items is None:
            items = datasketches.frequent_strings_sketch(MAX_ITEMS_SIZE)
        if theta_sketch is None:
            theta_sketch = datasketches.update_theta_sketch()
        self.count = count
        self.items = items
        self.theta_sketch = theta_sketch

    def update(self, value: str):
        if value is None:
            return

        self.count += 1
        self.theta_sketch.update(value)
        self.items.update(value)

    def to_protobuf(self):
        return StringsMessage(
            count=self.count,
            items=self.items.serialize(),
            theta=self.theta_sketch.serialize(),
        )

    @staticmethod
    def from_protobuf(message: StringsMessage):
        return StringTracker(
            count=message.count,
            items=datasketches.frequent_strings_sketch.deserialize(
                message.items),
            theta_sketch=datasketches.theta_sketch.deserialize(message.theta)
        )

    def to_summary(self):
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

