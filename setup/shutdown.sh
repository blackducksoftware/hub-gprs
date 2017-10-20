#!/bin/bash
CONTAINERS_TO_STOP=$(docker ps -f label=hub-scm -q)
if [ -z "${CONTAINERS_TO_STOP}" ]; then
    echo "Application not running"
    exit 1
else
    echo Shutting down Hub-SCM integration...
    docker stop ${CONTAINERS_TO_STOP}
fi
