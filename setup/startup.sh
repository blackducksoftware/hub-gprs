#!/bin/bash
export CONCOURSE_EXTERNAL_URL=http://$(hostname):8080
nohup docker-compose up > hub-scm.log 2>&1 &


