<form method="post" action="/builds/${build.id}/clone" id="cloneForm" >
	<input type="hidden" name="${_csrf.parameterName}"
		value="${_csrf.token}" />
	<p>Please enter the ${build.source.type.buildIdentifierProperty?lower_case} values for clones of ${build.name};</p>
	<table class="listTable" id="clonesTable">
		<tr id="row0">
			<td>
				<input type="text" class="param" name="cloneValues"/>
			</td>
		</tr>
	</table>
</form>
<p><br/><a href="#" id="addRow"><img src="/img/plus.svg" height="20px" width="20px" style="left:20px"></a></p>
	<script type="text/javascript">
	$(document).ready(function(){
		var counter = 0;
		$("#addRow").click(function(){
		    //Clone the last row
		   var rowToClone=$("#row"+counter);
		   var clonedRow=rowToClone.clone();
		   counter++;
		   clonedRow.attr('id', 'row'+counter);
		   rowToClone.after(clonedRow);
		   clonedRow.find("input").val("");   
	    });
	}); 
    </script>
