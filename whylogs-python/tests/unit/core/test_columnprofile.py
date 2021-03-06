from whylabs.logs.core import ColumnProfile
from testutil import compare_frequent_items
import json
import pytest


def test_track():
    c = ColumnProfile('col')
    data = [1, 2, 3, 'string 1', 'string 2', '3', 4.0, '3.95', '3.95st', None, True]
    for val in data:
        c.track(val)
    nt = c.number_tracker
    assert nt.floats.count == 6
    assert nt.ints.count == 0
    assert nt.floats.min == 1.0
    assert nt.floats.max == 4.0

    assert c.counters.count == len(data)
    assert c.counters.null_count == 1
    assert c.counters.true_count == 1


def test_protobuf():
    c = ColumnProfile('col')
    for val in [1, 2, 3]:
        c.track(val)
    msg = c.to_protobuf()
    c1 = ColumnProfile.from_protobuf(msg)
    assert c1.column_name == c.column_name == 'col'
    assert hasattr(c1, 'number_tracker')
    msg2 = c1.to_protobuf()
    # We cannot do a straight equality comparison for serialized frequent
    # strings objects
    compare_frequent_items(
        c1.number_tracker.frequent_numbers.get_frequent_items(),
        c.number_tracker.frequent_numbers.get_frequent_items()
    )
    msg.numbers.frequent_numbers.sketch = bytes()
    msg2.numbers.frequent_numbers.sketch = bytes()


def test_summary():
    from whylabs.logs.util.protobuf import message_to_dict
    c = ColumnProfile('col')
    for n in [1, 2, 3]:
        c.track(n)
    summary = c.to_summary()
    actual_val = message_to_dict(summary)
    expected_val = {
        "counters": {
            "count": "3",
        },
        "schema": {
            "inferredType": {
                "type": "INTEGRAL",
                "ratio": 1.0
            },
            "typeCounts": {
                "INTEGRAL": "3"
            }
        },
        "numberSummary": {
            "count": "3",
            "min": 1.0,
            "max": 3.0,
            "mean": 2.0,
            "stddev": 1.0,
            "isDiscrete": False,
            "histogram": {
                "start": 1.0,
                "end": 3.0000003,
                "counts": [
                    "3"
                ],
                "max": 3.0,
                "min": 1.0,
                "bins": [
                    1.0,
                    3.0000003
                ],
                "n": "3",
                "width": 0.0
            },
            "quantiles": {
                'quantiles': [0.0, 0.01, 0.05, 0.25, 0.5, 0.75, 0.95, 0.99, 1.0],
                'quantileValues': [1.0, 1.0, 1.0, 1.0, 2.0, 3.0, 3.0, 3.0, 3.0]
            },
            "uniqueCount": {
                "estimate": 3.0,
                "upper": 3.0,
                "lower": 3.0
            }
        }
    }
    # Top-level unique count needs to be approximately equal
    expected_unique = {
        'estimate': 3.000000014901161,
        'lower': 3.0,
        'upper': 3.0001498026537594
    }
    actual_unique = actual_val.pop('uniqueCount')
    assert actual_unique == pytest.approx(expected_unique, 0.0001)

    # Cannot do a straightforward comparison of frequent number counts, since
    # their orders can vary
    actual_freq = actual_val['numberSummary']['frequentNumbers']
    actual_val['numberSummary'].pop('frequentNumbers')
    counts = []
    for num_list in (actual_freq['longs'], actual_freq['doubles']):
        for xi in num_list:
            val = xi['value']
            if isinstance(val, str):
                # Parse JSON encoded int64
                val = json.loads(val)
            count = xi['estimate']
            if isinstance(count, str):
                # Parse JSON encoded int64
                count = json.loads(count)
            counts.append((val, count))
    expected_counts = {
        (1, 1),
        (2, 1),
        (3, 1)
    }
    assert len(counts) == len(expected_counts)
    assert set(counts) == expected_counts

    # Cannot do a straightforward frequentItems count since order is ambiguous
    actual_freq = actual_val.pop('frequentItems')
    assert set(actual_freq.keys()) == {'items'}
    expected = [
        ('1', '1'),
        ('2', '1'),
        ('3', '1')
    ]
    assert len(actual_freq['items']) == len(expected)
    counts = []
    for v in actual_freq['items']:
        counts.append((v['jsonValue'], v['estimate']))
    assert set(counts) == set(expected)

    # Compare the messages, excluding the frequent numbers counters
    assert actual_val == expected_val


def test_merge():
    col = ColumnProfile("test")
    vals = [1, 1.0, "string", True, False, None]
    for v in vals:
        col.track(v)

    merged = col.merge(col)
    assert merged.counters.count == 12
    assert merged.counters.null_count == 2
    assert merged.counters.true_count == 4
    assert merged.number_tracker.ints.count == 0
    assert merged.number_tracker.floats.count == 4
    assert merged.string_tracker.count == 2
