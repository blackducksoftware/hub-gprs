#!/bin/bash
export UI_STARTUP_OPTS='-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=22666'
DEBUG_COMPOSE=./debug_docker-compose.yml
cp -f docker-compose.yml $DEBUG_COMPOSE
sed -i -e 's/\"8666:8666\"/\"8666:8666\",\"22666:22666\"/g' $DEBUG_COMPOSE
nohup docker-compose -f $DEBUG_COMPOSE up > hub-scm.log 2>&1 &


