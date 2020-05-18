# WhyLogs Python

Python port of the [WhyLogs Java library](https://gitlab.com/whylabs/whylogs-java)

## Installation
Currently, `python 3.7` is recommended.

1. Clone the repo and cd into the directory
2. Install with pip.
   - For a dev installation with development requirements, it's recommended to create a fresh conda environment or virtualenv
     ```
     # Development installation
     pip install -v -e .[dev]
     ```
   - Standard installation:
     ```
     # Standard installation
     pip install .
     ```
 

## Tests
Testing is handled with the `pytest` framework.
You can run all the tests by running `pytest -vvs tests/` from the parent directory.

## Scripts
See the `scripts/` directory for some example scripts for interacting with `whylogs-python`
