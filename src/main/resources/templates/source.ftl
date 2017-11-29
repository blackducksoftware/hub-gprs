<#--
Copyright (C) 2017 Black Duck Software, Inc.
http://www.blackducksoftware.com/

Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements. See the NOTICE file
distributed with this work for additional information
regarding copyright ownership. The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License.
 -->

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