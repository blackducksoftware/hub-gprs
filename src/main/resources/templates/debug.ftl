<html>
<#include "common/head.ftl"/>
<body>
	<#include "common/navbar.ftl"/>
	
	<div class="page_content" id="error_content">
		<h1 align="center">Debug Information</h1>
		<p align="center">
			<textArea rows="27" cols="100" style="font-family: courier, monospace">${debugInfo}</textArea>
		</p>
	</div>
	<#include "common/footer.ftl"/>
</body>
</html>