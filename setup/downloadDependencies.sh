#!/bin/bash

export HUB_DETECT_VERSION=2.2.0

if [ ! -d ./download ]; then
    mkdir download
fi;

#Download the JRE/JDK
if [ ! -e ./download/*jdk*.tar.gz ]; then
    wget http://cdn.azul.com/zulu/bin/zulu8.25.0.1-jdk8.0.152-linux_x64.tar.gz -O download/jdk.tar.gz
fi

#Download hub-detect

if [ ! -e ./download/hub-detect.sh ]; then
    wget "https://blackducksoftware.github.io/hub-detect/hub-detect.sh" -O download/hub-detect.sh
    wget "https://test-repo.blackducksoftware.com/artifactory/bds-integrations-release/com/blackducksoftware/integration/hub-detect/${HUB_DETECT_VERSION}/hub-detect-${HUB_DETECT_VERSION}.jar" -O download/hub-detect-${HUB_DETECT_VERSION}.jar
fi




