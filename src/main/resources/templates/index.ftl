<html>
<#include "common/head.ftl"/>
<body>
	<#include "common/navbar.ftl"/>
	<div class="page_content" id="page_content">
		<h1 align="center">Welcome to Pull Request Scanner!</h1>
		<p align="center">
			To start, configure one or more <a href="#"
				onclick="loadPageInMain('/sources')">SCMs</a>. Then, configure one
			or more <a href="#" onclick="loadPageInMain('/builds')">repositories.</a>
		</p>
		<p align="center">
			<br />
			<br />
			<img src="/img/blueberries.jpg">
		</p>

	</div>
	
	<div id="modalView">
	</div>
	<#include "common/footer.ftl"/>
</body>
</html>