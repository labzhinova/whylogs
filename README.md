# WhyLogs Python

Python port of the [WhyLogs Java library](https://gitlab.com/whylabs/whylogs-java)

## Setup
#### Install requirements to current python environment 
```./requirements.sh```
 
#### Build protobuf files  
Run `build_proto.sh` from a terminal to generate the python protobuf interface files.
This only needs to be done when the `.proto` files are changed.


## Tests
Testing is handled with the `pytest` framework.
You can run all the tests by running `pytest -vvs tests/` from the parent directory.

## Scripts
See the `scripts/` directory for some example scripts for interacting with `whylogs-python`
