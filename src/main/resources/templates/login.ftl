<html>
<head>
<title>Hub SCM Integration</title>
<meta http-equiv="cache-control" content="max-age=0" />
<meta http-equiv="cache-control" content="no-cache" />
<meta http-equiv="expires" content="0" />
<meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT" />
<meta http-equiv="pragma" content="no-cache" />

<link rel="icon" type="image/png" href="/img/blackduckiefavi_360.png">

<link rel="stylesheet" href="/css/scmint-ui.css" />
<link rel="stylesheet" href="/css/loader.css" />
<link rel="stylesheet"
	href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">

<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.2.1/themes/default/style.min.css" />

<!-- Latest compiled and minified CSS -->
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
	integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
	crossorigin="anonymous" />

<!-- Optional theme -->
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css"
	integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp"
	crossorigin="anonymous" />

<script src="https://code.jquery.com/jquery-3.2.1.min.js"
	integrity="sha256-hwg4gsxgFZhOsEEamdOYGBf13FyQuiTwlAQgxVSNgt4="
	crossorigin="anonymous"></script>


<!-- Latest compiled and minified JavaScript -->
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
	integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
	crossorigin="anonymous"></script>

<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
<script src="/js/scmint-ui.js"></script>

</head>
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
</body>
</html>