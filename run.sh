#!/usr/bin/env bash

set -e
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. ${DIR}/version.sh
echo run ${IMAGENAME}

podman run --rm ${IMAGENAME}