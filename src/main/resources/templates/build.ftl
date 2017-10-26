
<h1>
	<#if build??>Edit<#else>Add</#if> repository</h1>
<div id="messageDiv" class="message">
	<#if message??>
   			${message}
		</#if>
</div>
<br />

<form action="/builds<#if build??>/${build.id}</#if>"
	method="<#if build??>PUT<#else>POST</#if>" id="newDeploymentForm">
	<input type="hidden" name="${_csrf.parameterName}"
		value="${_csrf.token}" />


	<table class="formTable">
		<tr>
			<td class="formFieldName">Source:</td>
			<td><select name="source_id" required="true">
					<#list sources as source>
					<option value="${source.id}"<#if build?? && build.source.id == source.id>selected = "selected"</#if>>${source.name}</option>
					</#list>
				</select>
			</td>
		</tr>
		<#list allBuildParams as paramDef>
			<tr>
				<td class="formFieldName">
					${paramDef.friendlyName}:
				</td>
				<td>
					<input type="text" name="${paramDef.name}" class="param" value="<#if build??>${build.properties[paramDef.name]}<#else>${paramDef.defaultValue}</#if>"/>
				</td>
			</tr>
		</#list>
		<tr>
			<td class="formFieldName">Build type:</td>
				<td><select name="build_type" id="build_type_select" required="true">
					<#list buildTypes as buildType>
					<option value="${buildType}" <#if build?? && build.buildType == buildType>selected = "selected"</#if>>${buildType.friendlyName}</option>
					</#list>
				</select>
				<#-- Auto-populate the build image and tag based on selected build type -->
				<script type="text/javascript">
					$('#build_type_select')
							.change(
									function() {
										<#list buildTypes as buildType>
										if ($(this).val() == '${buildType}') {
											$('#build_image').val(
													'${buildType.image}');
											$('#build_image_tag').val(
													'${buildType.imageTag}');
											$('#build_command').val('${buildType.defaultBuildCommand}')
										}
										</#list>
									});
				</script>
			</td>
		</tr>
		</table>
		<br/><br/>
		
		
		<div id="advancedContent" style="width:600px"  class="panel panel-default" >
		  	<div class="panel-heading">
	  			<h3 class="panel-title">Advanced</h3>
  			</div>
		  	<div class="panel-body">
				<table class="formTable">
				<tr>
					<td class="formFieldName">
						Build image
					</td>
					<td>
						<input type="text" name="build_image" id="build_image" class="param" <#if build??>value="${build.image}"</#if>)/>
					</td>
				</tr>
				<tr>
					<td class="formFieldName">
						Build image tag
					</td>
					<td>
						<input type="text" name="build_image_tag" id="build_image_tag" class="param" <#if build??>value="${build.imageTag}"</#if>/>
					</td>
				</tr>
				<tr>
					<td class="formFieldName">
						Build command
					</td>
					<td>
						<input type="text" name="build_command" id="build_command" <#if build??>value="${build.buildCommand}"</#if> class="param"/>
					</td>
				</tr>
				<!-- Project and version name (if not default -->
				<tr><td colspan="2"><br/><br/>Leave the following blank to allow Hub-Detect to determine these values:</td></tr>
				<tr>
					<td class="formFieldName">
						Project Name
					</td>
					<td>
						<input type="text" name="project_name" id="project_name" <#if build?? && build.projectName??>value="${build.projectName}"</#if> class="param"/>
					</td>
				</tr>
				<tr>
					<td class="formFieldName">
						Version Name
					</td>
					<td>
						<input type="text" name="version_name" id="version_name" <#if build?? && build.versionName??>value="${build.versionName}"</#if> class="param"/>
					</td>
				</tr>
				
				</table>
		  </div>
		</div>

</form>
<button name="submitButton"
	onclick="submitFormAndGoto($('#newDeploymentForm'), $('#messageDiv'),'/builds')">Submit</button>
<#-- If no image is specified (from an existing build), set the image and tag boxes to the value corresponding to the default build type-->	
<script type="text/javascript">
	if (!$('#build_image').val()){
		$(document).ready(function(){
			$('#build_type_select').trigger('change');
		});
	}
</script>
