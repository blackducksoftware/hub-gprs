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
				"tag" : "v31a"
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
					"version": "every",
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
						<#-- Compute arguments -->
						<#assign args="${hub_detect_arguments}">
						<#if project_name?hasContent>
							<#assign args="${args} \\\"--detect.project.name='${project_name}'\\\"">
						</#if>
						<#if version_name?hasContent>
							<#assign args="${args} \\\"--detect.project.version.name='${version_name}'\\\"">
						<#else>
							<#assign args="${args} '--detect.project.version.name=\\\"Pull Request $(cat .git/id)\\\"'"/>
						</#if>
							"path": "sh",
							"args": [
								"-exc",
								"wget http://hub-scm-ui:13666/runBuild.sh\n sh runBuild.sh\n"
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
						"SPRING_APPLICATION_JSON": "{\"blackduck.hub.url\":\"${hub_url}\",\"blackduck.hub.username\":\"${hub_username}\",\"blackduck.hub.password\":\"${hub_password}\", \"blackduck.hub.trust.cert\":true,\"detect.policy.check\":true}",
						"PROJECT_BUILD_COMMAND": "${build_command}",
						"HUB_URL": "${hub_url}",
						"HUB_DETECT_ARGS": "${args}",
						"BUILD_ID": "${build_id}"
					},
					"on_failure": {
						"put": "codebase-pr",
						"params": {
							"path": "codebase-result",
							"status": "failure",
							"title": "Black Duck Hub"
						}
					},
					"on_success": {
						"put": "codebase-pr",
						"params": {
							"path": "codebase-result",
							"status": "success",
							"title": "Black Duck Hub"
						}
					}
				}
			]
		}
	]
}