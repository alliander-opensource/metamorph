#!/bin/bash

# SPDX-FileCopyrightText: 2023 Bart Kleijngeld
#
# SPDX-License-Identifier: Apache-2.0

# Suggested Git `pre-commit` hook script for this project.

## Update license/copyright metadata with REUSE.
COPYRIGHT="Bart Kleijngeld"
YEAR=`date +%Y`
LICENSE="Apache-2.0"

echo "Adding license and copyright related metadata... "

git ls-files \
| grep -v -e CODE_OF_CONDUCT.md \
          -e .github \
	  -e META-INF \
| xargs \
reuse annotate \
    --copyright "$COPYRIGHT" \
    --year "$YEAR" \
    --license "$LICENSE" \
    --skip-unrecognised \
    --merge-copyrights
# | grep -v -e '^Skipped ' \
        #   -e '^Successfully changed header '
