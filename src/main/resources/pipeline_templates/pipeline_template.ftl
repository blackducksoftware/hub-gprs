{
	"groups": null,
	"resources": [
		{
			"name": "codebase-pr",
			"webhook_token": "",
			"type": "github-pull-request",
			"source": {
				"access_token": "${api_token}",
				"api_endpoint": "${api_endpoint}",
				<#if (no_ssl_verify)?hasContent >
				"no_ssl_verify" : "${no_ssl_verify}",
				"skip_ssl_verification" : "${no_ssl_verify}",
				</#if>
				<#if private_key?hasContent>
				"private_key" : "${private_key?replace('\n','\\n')?replace('\r','')}",
					<#if address?hasContent>
					"uri" : "git@${address}:${Repository}.git",
					</#if>
				<#elseIf address?hasContent>
				"uri" : "https://${address}/${Repository}",
				</#if>
				"repo": "${Repository}"
			},
			"check_every": "",
			"tags": null
		}
	],
	"resource_types": [
		{
			"name": "github-pull-request",
			"type": "docker-image",
			"source": {
				"repository": "yevster/github-pr",
				"tag" : "latest"
			},
			"privileged": false,
			"tags": null
		}
	],
	"jobs": [
		{
			"name": "hub-detect",
			"plan": [
				{
					"get": "codebase-pr",
					"trigger": true
				},
				{
					"task": "build_and_scan",
					"config": {
						"platform": "linux",
						"image_resource": {
							"type": "docker-image",
							"source": {
								"repository": "${build_image}",
								"tag": "${build_image_tag}"
							}
						},
						"run": {
							"path": "sh",
							"args": [
								"-exc",
								"cp -r ./codebase-pr/.git ./codebase-result\nchmod -R a+r ./codebase-result/.git\n cd codebase-pr\nwget https://blackducksoftware.github.io/hub-detect/hub-detect.sh\nchmod +x ./hub-detect.sh\n${build_command}\nstatus=0\n./hub-detect.sh \\\"--detect.project.version.name=Pull Request $(cat .git/id)\\\"||status=$?\necho '${hub_url}' > ../codebase-result/.build_url\nexit $status"
							]
						},
						"inputs": [
							{
								"name": "codebase-pr"
							}
						],
						"outputs": [
							{
								"name": "codebase-result"
							}
						]
					},
					"params": {
						"SPRING_APPLICATION_JSON": "{\"blackduck.hub.url\":\"${hub_url}\",\"blackduck.hub.username\":\"${hub_username}\",\"blackduck.hub.password\":\"${hub_password}\", \"blackduck.hub.trust.cert\":true,\"detect.policy.check\":true}"
					},
					"on_failure": {
						"put": "codebase-pr",
						"params": {
							"path": "codebase-result",
							"status": "failure",
							"title": "Black Duck Hub",
							"description": "Policy violation(s) found."
						}
					},
					"on_success": {
						"put": "codebase-pr",
						"params": {
							"path": "codebase-result",
							"status": "success",
							"title": "Black Duck Hub",
							"description": "No policy violations found."
						}
					}
				}
			]
		}
	]
}