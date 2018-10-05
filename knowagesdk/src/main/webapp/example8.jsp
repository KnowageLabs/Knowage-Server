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
	<!--  <script type="text/javascript" src="/knowagesdk/js/sbisdk-all-production.js"></script>-->
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
		
 		execTest8 = function() {
		    Sbi.sdk.api.getDocuments({
		    	callback: function( json, args, success ) {
		    		if (success){
		    			
		    			var str = "";
		    			
		    			for (var key in json){
			    			str += "<tr><td>" + json[key].label + "</td><td>" + json[key].name + "</td><td>" + json[key].description + "</td></tr>";
		    			}
		    			
		    			document.getElementById('documents').innerHTML = str;
		    		}
				}});
		};
	</script>
</head>


<body>
<h2>Example 8 : getDocuments</h2>
<hr>
<b>Description: </b> Use <i>getDocuments</i> function to retrieve the list of all documentss
<p>
<b>Code: </b>
<p>
<BLOCKQUOTE>
<PRE>
execTest8 = function() {
    Sbi.sdk.api.getDocuments({
    	callback: function( json, args, success ) {
    		if (success){
    			var str = "";
    			
    			for (var key in json){
	    			str += "&lt;tr&gt;&lt;td&gt;" + json[key].label + "&lt;/td&gt;&lt;td&gt;" + json[key].name + "&lt;/td&gt;&lt;td&gt;" + json[key].description + "&lt;/td&gt;&lt;/tr&gt;";
    			}
    			
    			document.getElementById('documents').innerHTML = str;
    		}
		}});
};
</PRE>
</BLOCKQUOTE>
<hr>
<table>
	<th>Label</th>
	<th>Name</th>
	<th>Description</th>
	
	<tbody id="documents">
	</tbody>
</table>

<script type="text/javascript">
	execTest8();
</script>
<hr>

</body>
</html>