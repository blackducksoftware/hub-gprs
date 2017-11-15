#!/bin/sh

#Do we have Java available? If not, download it from application

if [ -z "$(which java)" ] || [ -z "${JAVA_HOME}" ]; then
	wget http://hub-scm-ui:13666/jdk.tar.gz
	mkdir /jdk
	tar xvzf jdk.tar.gz -C /jdk
	export JAVA_HOME="/jdk/$(ls /jdk)"
	export PATH="${JAVA_HOME}/bin:${PATH}"
fi


#Copy PR info into the result directory
cp -r ./codebase-pr/.git ./codebase-result
chmod -R a+r ./codebase-result/.git
cd codebase-pr

echo "Building: $(cat .git/id)"

#Get hub-detect
wget https://blackducksoftware.github.io/hub-detect/hub-detect.sh
chmod +x ./hub-detect.sh

#Get injected files
wget "http://hub-scm-ui:13666/buildFiles/${BUILD_ID}" -O inject.tar
START_DIR="$(pwd)"
cd / 
tar xvf "${START_DIR}/inject.tar"
cd "$START_DIR"


#Run the the build first
$PROJECT_BUILD_COMMAND

#Run hub-detect
status=0

#Capture the output of hub-detect
script -c "./hub-detect.sh ${HUB_DETECT_ARGS}" -e hub-detect.log  

status=$?
echo "Hub-detect completed".

#Extract details link
detailUrl=$(grep 'To see your results, follow the URL:' hub-detect.log | sed -e 's#.*follow\ the\ URL\:\ \(\)#\1#')
violationResult="$(grep 'Policy Status: IN_VIOLATION' hub-detect.log)"

resultStatus="No violations found.";
if [ ! -z "${violationResult}" ]; then
	resultStatus="Policy violation(s) found." 
elif [ "$status" -ne "0" ]; then
	resultStatus="Scan failed, violation status unknown."
fi

echo "$resultStatus"
echo "$resultStatus" > ../codebase-result/.status_description

#Populate the details link in the result
echo "${detailUrl}" > ../codebase-result/.build_url 


exit $status
