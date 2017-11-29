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

<html>
<#include "/common/head.ftl"/>
<body>
	<#include "/common/navbar.ftl"/>
	<div class="page_content" id="page_content">
		<h1>Files</h1>
		<div id="messageDiv" class="message">
			<#if message??>
				${message}
			</#if>
		</div>
		<br/>
		<br/>
		<table class="listTable">
			<#list files as file>
				<tr><td>${file.name}<a href="#" ><img src="/img/trash.svg" onclick="deleteHtml(encodeURI('/files/${file.id}?${_csrf.parameterName}=${_csrf.token}'))" ></a></td></tr>
			</#list>
		</table>
		<br/>
		<br/>
		<hr/>
		<h5>Upload New File</h5>
		<div>
			<form method="POST" id="newFileForm" enctype="multipart/form-data" action="/files">
				<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
						<table class="formTable">
					<tr><td class="formFieldName">File to upload:</td><td><input type="file" name="file" /></td></tr>
					<tr><td class="formFieldName">Name:</td><td><input type="text" name="name" /></td></tr>
					<tr><td></td><td><input type="submit"/></td></tr>
				</table>
			</form>
		</div>
	</div>
	
	<#include "common/footer.ftl"/>
</body>
</html>
