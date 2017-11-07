<html>
<#include "common/head.ftl"/>
<body>
	<#if (hideNavbar!'') != "true">
	<div class="navbar">
		<img class="navbar_logo"
			src="/img/NewBlackDuckLogo_WhiteColor_Website.png" />
		<ul class="navbar">
			<li><a href="/" class="navbar_link">Main</a></li>
		</ul>
	</div>
	</#if>
	
	<div class="page_content" id="error_content">
		<h1 align="center">An error has occurred!</h1>
		<h2 align="center">${status!''}: ${error!''}</h2>
		<p align="center">
			${message!''}
		</p>
	</div>
</body>
</html>