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
	
</head><body>
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