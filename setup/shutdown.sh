#!/bin/bash
CONTAINERS_TO_STOP=$(docker ps -f label=hub-scm -q)
if [ -z "${CONTAINERS_TO_STOP}" ]; then
    echo "Application not running"
    exit 1
else
    #Obliterate the UI container entirely, so it starts from a clean slate
    docker rm -f $(docker ps -f label=hub-scm-ui -q)
    #Stop everything else
    echo Shutting down Hub-SCM integration...
    docker stop ${CONTAINERS_TO_STOP}
fi
