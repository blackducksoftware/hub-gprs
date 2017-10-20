#!/bin/bash
export CONCOURSE_EXTERNAL_URL=http://$(hostname):8080
export HUB_SCM_BUILD_LOG_DIR=/tmp/hubscm/logs
nohup docker-compose up > hub-scm.log 2>&1 &


