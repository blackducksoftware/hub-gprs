#!/bin/bash

export HUB_DETECT_VERSION=5.5.1
export EXPECTED_JDK_MD5=9c02c89f37d217d229203e59b57246a8

compareMd5Sum(){
    file=$1
    expected=$2
    actual="$(cat ""${file}"" 2>/dev/null | md5)"
    if [ "$expected" != "$actual" ]; then
        echo 'bad'
    fi
}

if [ ! -d ./download ]; then
    mkdir download
fi;

if [ -e ./download/jdk.tar.gz ] && [ "$(compareMd5Sum ./download/jdk.tar.gz $EXPECTED_JDK_MD5)" = "bad" ]; then
    echo Incorrect prior JDK download detected $(cat ./download/jdk.tar.gz | md5). Re-downloading...
    rm ./download/jdk.tar.gz
fi

#Download the JRE/JDK
if [ ! -e ./download/*jdk*.tar.gz ]; then
    wget https://cdn.azul.com/zulu/bin/zulu8.28.0.1-jdk8.0.163-linux_x64.tar.gz -O download/jdk.tar.gz
    downloadSuccess=$?
    if [ "${downloadSuccess}" -ne "0" ]; then
        >&2 echo JDK Download failed.
        rm -f download/jdk.tar.gz
        exit 1
    fi
fi

#Download synopsys-detect
if [ ! -e ./download/synopsys-detect.sh ] || [ ! -e ./download/synopsys-detect-${HUB_DETECT_VERSION}.jar ]; then
    rm -f download/synopsys-detect*
    wget "https://detect.synopsys.com/detect.sh" -O download/synopsys-detect.sh && wget "https://repo.blackducksoftware.com/artifactory/bds-integrations-release/com/synopsys/integration/synopsys-detect/${HUB_DETECT_VERSION}/synopsys-detect-${HUB_DETECT_VERSION}.jar" -O download/synopsys-detect-${HUB_DETECT_VERSION}.jar
    downloadSuccess=$?
    if [ "${downloadSuccess}" -ne "0" ]; then
        >&2 echo Synopsys-Detect download failed.
        rm -f download/synopsys-detect*
        exit 1
    fi
fi
