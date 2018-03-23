#!/bin/bash
./shutdown.sh

#Delete the built UI image.
IMAGE_TO_DELETE=$(docker image ls blackducksoftware/hub-scm-ui -q)

if [ ! -z "${IMAGE_TO_DELETE}" ]; then 
    docker image rm -f ${IMAGE_TO_DELETE}
fi

# Update downloads as needed
. downloadDependencies.sh

#Build UI image
URL_SETTING=$(cat .env | grep HUB_URL)
UNPREFIXED_URL=$(cut -d '/' -f 3 <<< "${URL_SETTING}")
cd ..
./gradlew clean build  -x test --refresh-dependencies
docker build . -f setup/ui_dockerfile -t blackducksoftware/hub-scm-ui --build-arg HUB_URL="${UNPREFIXED_URL}" --build-arg=HUB_DETECT_VERSION="${HUB_DETECT_VERSION}"
cd setup

./startup.sh
