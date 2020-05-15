"""
"""
from whylabs.logs.core.data import InferredType, SchemaMessage, SchemaSummary
Type = InferredType.Type


class SchemaTracker:
    UNKNOWN_TYPE = InferredType(type=Type.UNKNOWN)
    CANDIDATE_MIN_FRAC = 0.7

    def __init__(self, type_counts: dict=None):
        if type_counts is None:
            type_counts = {}
        self.type_counts = type_counts

    def track(self, item_type):
        try:
            self.type_counts[item_type] += 1
        except KeyError:
            self.type_counts[item_type] = 1

    def get_count(self, item_type):
        return self.type_counts.get(item_type, 0)

    def infer_type(self):
        total_count = sum(self.type_counts.values())
        if total_count == 0:
            return SchemaTracker.UNKNOWN_TYPE

        candidate = self._get_most_popular_type(total_count)
        if candidate.ratio > SchemaTracker.CANDIDATE_MIN_FRAC:
            return candidate

        # Integral is considered a subset of fractional here
        fractional_count = sum([self.type_counts.get(k, 0) for k in
                                (Type.INTEGRAL, Type.FRACTIONAL)])

        if candidate.type == Type.STRING \
                and self.type_counts.get(Type.STRING, 0) > fractional_count:
            # treat everything else as "String" except UNKNOWN
            coerced_count = sum(
                [self.type_counts.get(k, 0) for k in
                 (Type.INTEGRAL, Type.FRACTIONAL, Type.STRING, Type.BOOLEAN)])
            actual_ratio = float(coerced_count)/total_count
            return InferredType(type=Type.STRING, ratio=actual_ratio)

        if candidate.ratio >= 0.5:
            # Not a string, but something else with a majority
            actual_count = self.type_counts[candidate.type]
            if candidate.type == Type.FRACTIONAL:
                actual_count = fractional_count
            return InferredType(type=candidate.type,
                                ratio=float(actual_count)/total_count)

        fractional_ratio = float(fractional_count)/total_count
        if fractional_ratio >= 0.5:
            return InferredType(type=Type.FRACTIONAL, ratio=fractional_ratio)

        # Otherwise, assume everything is the candidate type
        return InferredType(type=candidate.type, ratio=1.0)

    def _get_most_popular_type(self, total_count):
        item_type = Type.UNKNOWN
        count = -1
        for candidate_type, candidate_count in self.type_counts.items():
            if candidate_count > count:
                item_type = candidate_type
                count = candidate_count

        ratio = float(count)/total_count
        return InferredType(type=item_type, ratio=ratio)

    def to_protobuf(self):
        return SchemaMessage(typeCounts=self.type_counts,
                             inferred_type=self.infer_type())

    @staticmethod
    def from_protobuf(message):
        return SchemaTracker(type_counts=message.typeCounts)

    def to_summary(self):
        type_counts = self.type_counts
        # Convert the integer keys to their corresponding string names
        type_counts_with_names = {Type.Name(k): v
                                  for k, v in type_counts.items()}
        return SchemaSummary(
            inferred_type=self.infer_type(),
            type_counts=type_counts_with_names,
        )
