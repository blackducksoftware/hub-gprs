<h1>SCMs</h1>
<div id="messageDiv" class="message">
	<#if message??>
   			${message}
	</#if>
</div>

<br/>
<table class="listTable">
	<#list sources as source>
		<tr><td><a href="#" onclick="loadPageInMain('/sources/${source.id}')">${source.name}</a>  <a href="#" ><img src="/img/trash.svg" onclick="deleteAndGoto(encodeURI('/sources/${source.id}?${_csrf.parameterName}=${_csrf.token}'), $('#messageDiv'), '/sources')" ></a></td></tr>
	</#list>
</table>

<p>
<br/>
	<input type="button" onclick="loadPageInMain('/newSource')" value="New"/]>
<p>