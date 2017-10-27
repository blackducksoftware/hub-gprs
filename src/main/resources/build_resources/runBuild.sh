#!/bin/sh

#Copy PR info into the result directory
cp -r ./codebase-pr/.git ./codebase-result
chmod -R a+r ./codebase-result/.git
cd codebase-pr

echo "Building: $(cat .git/id)"

wget https://blackducksoftware.github.io/hub-detect/hub-detect.sh
chmod +x ./hub-detect.sh

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

#Populate the details link in the result
echo "${detailUrl}" > ../codebase-result/.build_url 

exit $status
