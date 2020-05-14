"""
"""
from whylabs.logging.core import ColumnProfile


def test_track():
    c = ColumnProfile('col')
    data = [1, 2, 3, 'string 1', 'string 2', '3', 4.0, '3.95', '3.95st']
    for val in data:
        c.track(val)
    nt = c.number_tracker
    assert nt.floats.count == 6
    assert nt.ints.count == 0
    assert nt.floats.min == 1.0
    assert nt.floats.max == 4.0


def test_protobuf():
    c = ColumnProfile('col')
    for val in [1, 2, 3]:
        c.track(val)
    msg = c.to_protobuf()
    c1 = ColumnProfile.from_protobuf(msg)
    assert c1.column_name == c.column_name == 'col'
    assert hasattr(c1, 'number_tracker')


def test_summary():
    from google.protobuf.json_format import MessageToDict
    c = ColumnProfile('col')
    for n in [1, 2, 3]:
        c.track(n)
    summary = c.to_summary()
    summary_dict = MessageToDict(summary)
    true_val = {
        'numberSummary': {
            'count': '3',
            'min': 1.0,
            'max': 3.0,
            'mean': 2.0,
            'stddev': 1.0,
            'uniqueCount': {
                'estimate': 3.0,
                'upper': 3.0,
                'lower': 3.0
            }
        }
    }
    assert summary_dict == true_val
