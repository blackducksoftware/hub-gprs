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
