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
			<tr><td><a href="#" onclick="loadPageInMain('/builds/${build.id}')">${build.name}</a></td><td><span id="status_${build.id?string}"></span></td>
			<td>
			<#--Delete Icon--><a href="#" class="listButton"><img src="/img/trash.svg" title="Stop monitoring repository" onclick="deleteHtml(encodeURI('/builds/${build.id}?${_csrf.parameterName}=${_csrf.token}'))" class="listButton"></a>
			<#--Clone Icon--><a href="#" class="listButton" ><img src="/img/clone.svg" title="Clone repository settings" onclick="loadInModal('/builds/${build.id}/clone', 'Clone Repository', null)" class="listButton"></a>
			<#--Trigger icon--><a href="#" class="listButton" onclick="postHtml(encodeURI('/builds/${build.id}/trigger?${_csrf.parameterName}=${_csrf.token}'))"><img src="/img/playbutton.svg" title="Trigger scan of latest PR" class="listButton" alt="Build latest PR"/></a>
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
<#include "common/footer.ftl"/>

<!-- Modal -->
	<div id="modalView" class="modal fade" role="dialog">
		<div class="modal-dialog">
			<!-- Modal content-->
			<div class="modal-content" style="width:500px">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">&times;</button>
					<h4 class="modal-title" id="modalViewTitle">Clone Repository</h4>
				</div>
				<div class="modal-body" id="modalViewBody">
				</div>
				<div class="modal-footer" id="modalFooter">
					<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
					<button type="button" class="btn btn-primary" onclick="$('#cloneForm').submit()">Submit</button>			
				</div>
			</div>
		</div>
		
</body>
</html>