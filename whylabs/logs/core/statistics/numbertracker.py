"""
TODO:
    * Implement histograms
"""
import datasketches

from whylabs.logs.core.data import NumberSummary
from whylabs.logs.core.summaryconverters import from_sketch, \
    from_kll_floats_sketch
from whylabs.logs.core.statistics.datatypes import VarianceTracker, \
    IntTracker, FloatTracker
from whylabs.logs.core.data import NumbersMessage
from whylabs.logs.util import dsketch

# Parameter controlling histogram accuracy.  Larger = more accurate
DEFAULT_HIST_K = 256


class NumberTracker:
    """
    Class to track statistics for numeric data.

    Parameters
    ----------
    variance
        Tracker to follow the variance
    floats
        Float tracker for tracking all floats
    ints
        Integer tracker

    Attributes
    ----------
    variance
        See above
    floats
        See above
    ints
        See above
    theta_sketch : `datasketches.update_theta_sketch`
        Sketch which tracks approximate cardinality
    """
    def __init__(self,
                 variance: VarianceTracker=None,
                 floats: FloatTracker=None,
                 ints: IntTracker=None,
                 theta_sketch: datasketches.update_theta_sketch=None,
                 histogram: datasketches.kll_floats_sketch=None,
                 ):
        # Our own trackers
        if variance is None:
            variance = VarianceTracker()
        if floats is None:
            floats = FloatTracker()
        if ints is None:
            ints = IntTracker()
        if theta_sketch is None:
            theta_sketch = datasketches.update_theta_sketch()
        if histogram is None:
            histogram = datasketches.kll_floats_sketch(DEFAULT_HIST_K)
        self.variance = variance
        self.floats = floats
        self.ints = ints
        self.theta_sketch = theta_sketch
        self.histogram = histogram

    def track(self, number):
        """
        Add a number to statistics tracking

        Parameters
        ----------
        number : int, float
            A numeric value
        """
        self.variance.update(number)
        self.theta_sketch.update(number)
        # TODO: histogram update
        # Update floats/ints counting
        f_value = float(number)
        self.histogram.update(f_value)
        if self.floats.count > 0:
            self.floats.update(f_value)
        # Note: this type checking is fragile in python.  May want to include
        # numpy.integer in the type check
        elif isinstance(number, int):
            self.ints.update(number)
        else:
            self.floats.add_integers(self.ints)
            self.ints.set_defaults()
            self.floats.update(f_value)

    def to_protobuf(self):
        """
        Return the object serialized as a protobuf message
        """
        opts = dict(
            variance=self.variance.to_protobuf(),
            theta=self.theta_sketch.serialize(),
            histogram=self.histogram.serialize(),
        )
        if self.floats.count > 0:
            opts['doubles'] = self.floats.to_protobuf()
        elif self.ints.count > 0:
            opts['longs'] = self.ints.to_protobuf()
        msg = NumbersMessage(**opts)
        return msg

    @staticmethod
    def from_protobuf(message: NumbersMessage):
        """
        Load from a protobuf message

        Returns
        -------
        number_tracker : NumberTracker
        """
        opts = dict(
            theta_sketch = datasketches.update_theta_sketch.deserialize(
                message.theta),
            variance=VarianceTracker.from_protobuf(message.variance),
            histogram=dsketch.deserialize_kll_floats_sketch(message.histogram),
        )
        if message.HasField('doubles'):
            opts['floats'] = FloatTracker.from_protobuf(message.doubles)
        if message.HasField('longs'):
            opts['ints'] = IntTracker.from_protobuf(message.longs)
        return NumberTracker(**opts)


def from_number_tracker(number_tracker: NumberTracker):
    """
    Construct a `NumberSummary` message from a `NumberTracker`

    Parameters
    ----------
    number_tracker
        Number tracker to serialize

    Returns
    -------
    summary : NumberSummary
        Summary of the tracker statistics
    """
    if number_tracker is None:
        return

    if number_tracker.variance.count == 0:
        return

    stddev = number_tracker.variance.stddev()
    doubles = number_tracker.floats.to_protobuf()
    if doubles.count > 0:
        mean = number_tracker.floats.mean()
        min = doubles.min
        max = doubles.max
    else:
        mean = number_tracker.ints.mean()
        min = float(number_tracker.ints.min)
        max = float(number_tracker.ints.max)

    unique_count = from_sketch(number_tracker.theta_sketch)
    histogram = from_kll_floats_sketch(number_tracker.histogram)

    return NumberSummary(
        count=number_tracker.variance.count,
        stddev=stddev,
        min=min,
        max=max,
        mean=mean,
        histogram=histogram,
        unique_count=unique_count,
    )
