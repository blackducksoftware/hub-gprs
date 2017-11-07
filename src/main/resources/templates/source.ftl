<h1><#if source??>Edit<#else>Add</#if> repository</h1>
<div id="messageDiv" class="message">
	<#if message??>
   			${message}
		</#if>
	</div>
<br/>
<form action="/sources<#if source??>/${source.id}</#if>" method="<#if source??>PUT<#else>POST</#if>" id="sourceEditForm">

	<input type="hidden" name="${_csrf.parameterName}"
		value="${_csrf.token}" />
	<#if source??>
		<input type="hidden" name="id" value="${source.id}" />
	</#if>
	<input type="hidden" name="type" value="<#if source??>${source.type}<#else>GITHUB</#if>"/>
<table class="formTable">
	<tr><td class="formFieldName">
	Name: 
	</td><td><input type="text" class="param" name="name" <#if source??>value="${source.name}"</#if>/>
	</td></tr>	
	<#-- Currently, all properties for all types are visible, without type selection. 
	Once new types are added, a dropdown will have to determine which ones are visible -->
	<#list sourceTypes as cur_sourceType>
		<#list cur_sourceType.sourceParameterDefinitions as propDef>
			<#assign qualifiedPropName = "${cur_sourceType}_${propDef.name}">
			<tr id="${propDef.name}">
				<td class="formFieldName">
					${propDef.friendlyName}:
				</td>
				<td>
					<#if propDef.type=="PASSWORD">
						<input type="password" class="param" name="${qualifiedPropName}" value="<#if source??>${source.properties[propDef.name]!''}<#else>${propDef.defaultValue}</#if>" />
					<#elseif propDef.type=="TRUE_FALSE">
						<input type="checkbox" name="${qualifiedPropName}" <#if source?? && source.properties[propDef.name]=="true">checked="checked"<#elseif "true"==propDef.defaultValue>checked="checked">checked="checked"</#if>/>
					<#elseif propDef.type=="LARGE_TEXT">
						<textArea rows="6" cols="65" class="param" name="${qualifiedPropName}" ><#if source??>${source.properties[propDef.name]!''}<#else>${propDef.defaultValue}</#if></textArea>
					<#else>
						<input type="text" class="param" name="${qualifiedPropName}" value="<#if source??>${source.properties[propDef.name]!''}<#else>${propDef.defaultValue}</#if>" />	
					</#if>
				</td>
			</tr>
		</#list>
	</#list>
	
</table>
</form>
<p>
<button name="submitButton"
	onclick="submitForm($('#sourceEditForm'), $('#messageDiv'))">Submit</button>
</p>