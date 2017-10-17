#!/bin/bash

#Shut down and delete any running containers from the integration.

CONTAINERS_TO_DELETE=$(docker ps -a -f label=hub-scm -q)
if [ ! -z "${CONTAINERS_TO_DELETE}" ]; then
    echo Shutting down Hub-SCM integration...
    docker rm -f ${CONTAINERS_TO_DELETE}
    docker volume prune -f
fi

#Delete the built UI image.
IMAGE_TO_DELETE=$(docker image ls blackducksoftware/hub-scm-ui -q)

if [ ! -z "${IMAGE_TO_DELETE}" ]; then 
    docker image rm -f ${IMAGE_TO_DELETE}
fi

#Delete the created keys
if [ -d keys ]; then
    rm -fR keys
fi
