#!/usr/bin/python

# clean.py - Given an the files we are provided this will break each json entry
# into lines so that it is easier to work with. (all our code will rely on this
# having been done)
#
# Usage:
#   python clean.py path/to/inputfile path/to/outputfile

import sys
import re


SLICE_SIZE = int(1e8)


def split_into_lines(source, out):

    invalid = re.compile(r'}{')

    for slice in iter(lambda: source.read(SLICE_SIZE), ''):
        out.write(invalid.sub('}\n{', slice))


if __name__ == "__main__":
    source = open(sys.argv[1], 'r')
    out = open(sys.argv[2], 'w')

    split_into_lines(source, out)

    source.close()
    out.close()