<html>
<#include "common/head.ftl"/>
<body>
	<div class="navbar">
		<img class="navbar_logo"
			src="/img/NewBlackDuckLogo_WhiteColor_Website.png" />
		<ul class="navbar">
			<li><a href="/" class="navbar_link">Main</a></li>
		</ul>
	</div>
	
	<div class="page_content" id="error_content">
		<h1 align="center">Debug Information</h1>
		<p align="center">
			<textArea rows="27" cols="100" style="font-family: courier, monospace">${debugInfo}</textArea>
		</p>
	</div>
	<#include "common/footer.ftl"/>
</body>
</html>