<html>
<#include "common/head.ftl"/>
<body>
<div class="navbar">
	<img class="navbar_logo"
		src="/img/NewBlackDuckLogo_WhiteColor_Website.png" />
</div>
<div class="page_content">
	<h1>Login</h1>
	<p>Please enter your Hub credentials:</p>
	<p align="center">
	<form action = "/login" method="POST">

<input type="hidden" name="${_csrf.parameterName}"
		value="${_csrf.token}" />
	<table class="formTable">
		<tr>
			<td class="formFieldName">Username:</td>
			<td><input type="text" name="username" /></td>
		</tr>
		<tr>
			<td class="formFieldName">Password:</td>
			<td><input type="password" name="password" /></td>
		</tr>
	</table>
	<br/><br/>
	<input type="submit" value="Login">
	</form>
	</p>

</div>
<#include "common/footer.ftl"/>
</body>
</html>