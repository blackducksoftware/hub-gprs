#!/bin/bash
SOURCE_DOCKER_COMPOSE_FILE="${SOURCE_DOCKER_COMPOSE_FILE:-docker-compose.yml}"

COMPOSE_FILE=.docker-compose-run.yml
rm -f $COMPOSE_FILE
cp "${SOURCE_DOCKER_COMPOSE_FILE}" "${COMPOSE_FILE}"
if [ ! -z "${HUB_SCM_BUILD_LOG_DIR}" ]; then
    echo "Setting up log writing in ${HUB_SCM_BUILD_LOG_DIR}"
    sed -i -e "s/#log//g" $COMPOSE_FILE
fi
nohup docker-compose -f "${COMPOSE_FILE}" -p hub_scm_ui up > hub-scm.log 2>&1 &


