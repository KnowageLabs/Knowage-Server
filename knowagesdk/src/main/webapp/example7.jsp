<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>


<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<style>
	table, td {
	    border: 1px solid black;
	}
	
	th {
		background-color: gray;
	}
	</style>
<!-- <script type="text/javascript" src="js/sbisdk-all-production.js"></script> -->
    <script type="text/javascript" src="/knowagesdk/js/commons.js"></script>
       <script type="text/javascript" src="/knowagesdk/js/ajax.js"></script>
       <script type="text/javascript" src="/knowagesdk/js/jsonp.js"></script>
       <script type="text/javascript" src="/knowagesdk/js/cors.js"></script>
       <script type="text/javascript" src="/knowagesdk/js/services.js"></script>      
       <script type="text/javascript" src="/knowagesdk/js/api_jsonp.js"></script>
       <script type="text/javascript" src="/knowagesdk/js/api_cors.js"></script>
       <script type="text/javascript" src="/knowagesdk/js/api.js"></script>

	<script type="text/javascript">

		Sbi.sdk.services.setBaseUrl({
	        protocol: 'http'     
	        , host: 'localhost'
	        , port: '8080'
	        , contextPath: 'knowage' 
	    });
		
 		execTest7 = function() {
		
 			Sbi.sdk.api.executeDataSet({
		    	datasetLabel: 'DS'
		    	, parameters: {
		    		par_year: 2011,
		    		par_family: 'Food'
		    	}
		    	, callback: function( json, args, success ) {
		    		if (success){
		    			var str = "<th>Id</th>";
		    			
		    			var fields = json.metaData.fields;
		    			for(var fieldIndex in fields) {
		    				if (fields[fieldIndex].hasOwnProperty('header'))
		    					str += '<th>' + fields[fieldIndex]['header'] + '</th>';
		    			}
		    			
		    			str += '<tbody>';
		    			
		    			var rows = json.rows;
		    			for (var rowIndex in rows){
		    				str += '<tr>';
		    				for (var colIndex in rows[rowIndex]) {
		    					str += '<td>' + rows[rowIndex][colIndex] + '</td>';
		    				}
		    				str += '</tr>';
		    			}
		    			
		    			str += '</tbody>';
		    			
		    			document.getElementById('results').innerHTML = str;
		    		}
				}});
		};
	</script>
</head>


<body>
<h2>Example 7 : executeDataSet</h2>
<hr>
<b>Description: </b> Use <i>executeDataSet</i> function to get the content of a specific dataset
<p>
<b>Code: </b>
<p>
<BLOCKQUOTE>
<PRE>
execTest7 = function() {
    Sbi.sdk.api.executeDataSet({
    	datasetLabel: 'DS'
    	, parameters: {
    		par_year: 2011,
    		par_family: 'Food'
    	}
    	, callback: function( json, args, success ) {
    		if (success){
    			var str = "&lt;th&gt;Id&lt;/th&gt;";
    			
    			var fields = json.metaData.fields;
    			for(var fieldIndex in fields) {
    				if (fields[fieldIndex].hasOwnProperty('header'))
    					str += '&lt;th&gt;' + fields[fieldIndex]['header'] + '&lt;/th&gt;';
    			}
    			
    			str += '&lt;tbody&gt;';
    			
    			var rows = json.rows;
    			for (var rowIndex in rows){
    				str += '&lt;tr&gt;';
    				for (var colIndex in rows[rowIndex]) {
    					str += '&lt;td&gt;' + rows[rowIndex][colIndex] + '&lt;/td&gt;';
    				}
    				str += '&lt;/tr&gt;';
    			}
    			
    			str += '&lt;/tbody&gt;';
    			
    			document.getElementById('results').innerHTML = str;
    		}
		}});
};
</PRE>
</BLOCKQUOTE>
<hr>
<table id="results">

</table>

<script type="text/javascript">
	execTest7();
</script>
<hr>

</body>
</html>