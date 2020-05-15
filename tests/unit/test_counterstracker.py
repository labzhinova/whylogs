"""
"""
from whylabs.logs.core.statistics import CountersTracker


def test_tracker():
    c = CountersTracker()
    assert c.__dict__ == {'count': 0, 'true_count': 0, 'null_count': 0}

    opts = {'count': 1, 'true_count': 3, 'null_count': 4}
    c = CountersTracker(**opts)
    assert c.__dict__ == opts


def test_increment():
    c = CountersTracker()

    for _ in range(3):
        c.increment_count()

    for _ in range(2):
        c.increment_bool()

    for _ in range(1):
        c.increment_null()

    assert c.count == 3
    assert c.true_count == 2
    assert c.null_count == 1


def test_protobuf():
    c = CountersTracker(count=1, true_count=2, null_count=3)
    msg = c.to_protobuf()
    c2 = CountersTracker.from_protobuf(msg)
    assert c.__dict__ == c2.__dict__
