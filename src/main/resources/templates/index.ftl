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