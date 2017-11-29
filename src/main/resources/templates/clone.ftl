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
