
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
		
		<div id="fileInjection" style="width:600px"  class="panel panel-default" >
		  	<div class="panel-heading">
	  			<h3 class="panel-title">File Injection</h3>
  			</div>
  			<div class="panel-body">
				<#-- Show currently added files -->
				<#if build??>
					<table class="formTable">
					<#list build.fileInjections as file>
						<tr>
						<td>${file.fileContent.name}</td><td>&#8594;</td><td style="font-family: Courier New, monospace"> ${file.targetPath}</td><td><img src="/img/trash.svg" /></td>
						</tr>
					</#list>
					</table>
				<#else>
					No file injections configured.
				</#if>
				<#-- Add new files -->
				<br/>
				<#if injectionCandidates?size != 0 >			
					<p><br/><a href="#" id="addFile"><img src="/img/plus.svg" height="20px" width="20px" style="left:20px"></a></p>
				<#else> 
					No <#if build?? && build.fileInjections?size gt 0 >additional </#if>files available to inject.
				</#if>
					
			</div>
		</div>
		
				<script type="text/javascript">
				$(document).ready(function(){
					var counter = 1;
					
					$("#addFile").click(function(){
					    //Let's make a copy to work with
					    var originalDiv = $("#addFileDiv");
					    var cloneDiv = originalDiv.clone(); 
					
					    //Renaming cloneDiv
					    cloneDiv.attr('id','addFileDiv'+counter);
					
					    //Renaming inputs in  cloneDiv
						$("[name='newFileContent']",cloneDiv).attr('id','newFileContent'+counter);
					    $("[name='newFileContent']",cloneDiv).attr('name','newFileContent'+counter);
					    $("[name='newFileTarget']",cloneDiv).attr('name','newFileTarget'+counter);
					
					    //Append the new file form to wherever the button is
					    $("#addFile").before(cloneDiv);
					    cloneDiv.css("visibility", 'visible');
					    
    						// Trigger a change event to populate the default target
						$("#newFileContent"+counter).trigger('change');
					
					    //Increment counter
					    counter++;      
					});
				});
				</script>
				

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

<#-- Prototype of new file injection information form -->
<div id="addFileDiv" style="visibility: hidden; border-style: solid; border-radius: 4px; border-color: #cccccc; border-width: 1px; margin: 5px">	
	<table class="formTable">
		<tr>
			<td class="formFieldName">File:</td>
			<td>	<select name="newFileContent"  onchange="$(this).parents('table').find(':text')[0].value='~/'+$(this).find('option:selected').text()">
					<#list injectionCandidates as file>
					<option value="${file.id}">${file.name}</option>
					</#list>
				</select>
			</td>
		</tr>
		<tr>
			<td class="formFieldName">Target:</td>
			<td> <input type="text" value="~" name="newFileTarget" class="param"/>
		</tr>
	</table>		
</div>
