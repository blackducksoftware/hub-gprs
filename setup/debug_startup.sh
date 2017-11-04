#!/bin/bash

export UI_STARTUP_OPTS='-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=22666'
DEBUG_COMPOSE=./.debug_docker-compose.yml
rm -f $DEBUG_COMPOSE
cp docker-compose.yml $DEBUG_COMPOSE
sed -i -e 's/\"8666:8666\"/\"8666:8666\",\"22666:22666\",\"13666:13666\"/g' $DEBUG_COMPOSE
sed -i -e 's/#debugonly//g' $DEBUG_COMPOSE
rm -f "${DEBUG_COMPOSE}-e"
SOURCE_DOCKER_COMPOSE_FILE=$DEBUG_COMPOSE
. startup.sh

