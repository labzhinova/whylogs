#1/bin/bash
DIRECTORY="whylogs/logging/core/data/"
echo "Compiling protobuf files in directory: $DIRECTORY"
protoc -I $DIRECTORY --python_out=$DIRECTORY $DIRECTORY/*.proto
echo "Fixing relative import problems in generated python files"
2to3 $DIRECTORY/*_pb2.py -w -n
