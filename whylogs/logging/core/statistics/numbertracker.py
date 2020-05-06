#!/usr/bin/env python3
"""
created 5/5/20 by ibackus 
"""
import datasketches


class NumberTracker:
    def __init__(self):
        # Our own trackers.  TODO: implement our own trackers
        self.variance = None
        self.doubles = None
        self.longs = None

        # Sketches
        self.thetaSketch = datasketches.update_theta_sketch()
        self.histogram = None

    def track(self, number):
        self.thetaSketch.update(number)

    def toProtobuf(self):
        raise NotImplementedError

    def fromProtobuf(self):
        raise NotImplementedError