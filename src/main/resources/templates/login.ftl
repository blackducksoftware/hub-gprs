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