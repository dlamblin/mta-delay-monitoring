#!/usr/bin/env python

## REQUIRES simplejson, protobuf, and a copy of pbjson.py from https://github.com/NextTuesday/py-pb-converters

# path... src/github.com/dlamblin/mta-delay-monitoring/build/generated/source/proto/main/python/nyct_subway_pb2.py
import os

base_dir = os.path.dirname(__file__) or '.'
import sys
sys.path.insert(0, os.path.join(base_dir, 'build', 'generated', 'source', 'proto', 'main', 'python'))
import simplejson
# import build.generated.source.proto.main.python.gtfs_realtime_pb2 as gtfs__realtime__pb2
# from build.generated.source.proto.main.python.nyct_subway_pb2 import TripReplacementPeriod
import build.generated.source.proto.main.python.gtfs_realtime_pb2 as gtfs_realtime_pb2
import build.generated.source.proto.main.python.nyct_subway_pb2 as nyct_subway_pb2
import pbjson

file_to_open = sys.argv[1] or 'gtfs'
gtfsFeedMessage = gtfs_realtime_pb2.FeedMessage
try:
    f = open(file_to_open, "rb") # read binary
    b = f.read()
    print("\n\tBINARY GTFS: " + b.hex()[:20] + "... " + str(len(b)) + " bytes.\n")
    gtfsFeedMessage.ParseFromString(self=gtfsFeedMessage, serialized=b)
    f.close()
except IOError:
    print (file_to_open + ": Could not open file.  Creating a new one.")

print('Okay the Protocol Buffer that was generated claims to have a syntax error so...')


print(pbjson.pb2json(gtfsFeedMessage))