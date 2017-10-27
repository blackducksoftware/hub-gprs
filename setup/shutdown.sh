#!/bin/bash
CONTAINERS_TO_STOP=$(docker ps -f label=hub-scm -q)
if [ -z "${CONTAINERS_TO_STOP}" ]; then
    echo "Application not running"
    exit 1
else
    echo Shutting down Hub-SCM integration...
    docker stop -t 2 ${CONTAINERS_TO_STOP}
fi

CONTAINERS_TO_WIPE=$(docker ps -a -f label=hub-scm-wipe -q)
if [ ! -z "${CONTAINERS_TO_WIPE}" ]; then
    docker rm --force ${CONTAINERS_TO_WIPE}
fi