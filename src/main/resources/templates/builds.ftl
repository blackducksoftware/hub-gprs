<html>
<#include "common/head.ftl"/>
<body>
<#include "common/navbar.ftl"/>
<div class="page_content" id="page_content">
	<h1>Repositories</h1>
	<div id="messageDiv" class="message">
		<#if message??>
	   			${message}
			</#if>
	</div>
	<br />
	<p>
	<table class="listTable">
		<#list builds as build>
			<#assign buildStatus=statuses[build.id?string]>
			<tr><td><a href="#" onclick="loadPageInMain('/builds/${build.id}')">${build.name}</a> &nbsp; <span id="status_${build.id?string}"></span> <a href="#" ><img src="/img/trash.svg" onclick="deleteHtml(encodeURI('/builds/${build.id}?${_csrf.parameterName}=${_csrf.token}'))" ></a>
			<a href="#"  style="position:relative; left:-10px" onclick="postHtml(encodeURI('/builds/${build.id}/trigger?${_csrf.parameterName}=${_csrf.token}'))"><img src="/img/playbutton.svg" height="20" width="20" alt="Build latest PR"/></a>
			</td></tr>
		</#list>
	</table>
	</p>
	<p><br/>
		<input type="button" onclick="loadPageInMain('/newBuild')" value="New"/>
	</p>
	
	<script type="text/javascript">
		function refreshStatuses(){
			<#list builds as build>
	        $.get("/builds/${build.id}/status", function (result) {
	        //<span class="build_status_${buildStatus?lower_case}">${buildStatus.friendlyName}</span>
	            $('#status_${build.id}').html(result.toLowerCase().replace('_',' '));
	            $('#status_${build.id}').attr("class", "build_status_"+result.toLowerCase());
	        	});
	  	  	</#list>
		}
		$(document).ready(function () {
			refreshStatuses();
			doUntilLoadInMain(refreshStatuses, 1000);
		});
	</script>
</div>
</body>
</html>