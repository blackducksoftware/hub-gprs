#!/bin/bash
export LC_CTYPE=C

. downloadDependencies.sh

if [ -z "$JAVA_HOME" ]; then
    echo "JAVA_HOME is not set. Please set it to the location of your JDK."
    exit 127
fi

"${JAVA_HOME}"/bin/javac -version
if [ $? -ne 0 ]; then
    echo "Unable to run find javac at ${JAVA_HOME}/bin/javac. Please make sure your JAVA_HOME is set to your JDK (not JRE) location".
    exit 127
fi

echo -n "Enter the hostname of your Black Duck installation and press [ENTER]: "
read hubHost

echo -n "Enter the port of your Black Duck installation (typically 443) and press [ENTER]: "
read hubPort

echo -n "Enter the username of your Black Duck installation and press [ENTER]: "
read hubUsername

read -s -p "Enter the password of your Black Duck installation and press [ENTER]: " hubPassword


echo HUB_URL=https://${hubHost}:${hubPort} > .env
echo HUB_USERNAME=${hubUsername} >> .env
echo "HUB_PASSWORD=${hubPassword}" >> .env

mkdir -p keys/web keys/worker keys/ui

ssh-keygen -t rsa -f ./keys/web/tsa_host_key -N ''
ssh-keygen -t rsa -f ./keys/web/session_signing_key -N ''
ssh-keygen -t rsa -f ./keys/worker/worker_key -N ''

cp ./keys/worker/worker_key.pub ./keys/web/authorized_worker_keys
cp ./keys/web/tsa_host_key.pub ./keys/worker


echo ENCRYPTION_KEY="$(cat /dev/urandom | tr -dc 'a-zA-Z0-9\(\)\!\@' | fold -w 32 | head -n 1)" >> .env
echo CONCOURSE_PASSWRD="$(cat /dev/urandom | tr -dc 'a-zA-Z0-9\(\)\!\@' | fold -w 32 | head -n 1)" >> .env
echo DB_PASSWRD="$(cat /dev/urandom | tr -dc 'a-zA-Z0-9\(\)\!\@' | fold -w 32 | head -n 1)" >> .env


#Build UI image
cd ..
./gradlew build  -x test
docker build . -f setup/ui_dockerfile -t blackducksoftware/hub-scm-ui --build-arg HUB_URL="${hubHost}:${hubPort}" --build-arg HUB_DETECT_VERSION="${HUB_DETECT_VERSION}"
cd setup
