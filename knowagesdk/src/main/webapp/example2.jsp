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
	        , controllerPath: 'servlet/AdapterHTTP'  
	    });
		
		execTest2 = function() {
		    var html = Sbi.sdk.api.getDocumentHtml({
                documentLabel: 'TESTA_SDK'
                    , documentName: 'document name'
				, executionRole: '/spagobi/user'
				, parameters: {par: 'test', par2: 'test2'}
				, displayToolbar: true
		        , canResetParameters: false
				, iframe: {
		    		height: '500px'
		    		, width: '100%'
					, style: 'border: 0px;'
				}
			});
		    document.getElementById('targetDiv').innerHTML = html;
		};
	</script>
</head>


<body>
<h2>Example 2 : getDocumentHtml</h2>
<hr>
<b>Description: </b> Use <i>getDocumentHtml</i> function to get an html string that contains the definition of an iframe 
	pointing to the execution service. The src property of the iframe is internally populated using <i>getDocumentUrl</i> function.
<p>
<b>Code: </b>
<p>
<BLOCKQUOTE>
<PRE>
example2Function = function() {
	var html = Sbi.sdk.api.getDocumentHtml({
        documentLabel: 'TESTA_SDK'
        , documentName: 'document name'
		, executionRole: '/spagobi/user'
		, parameters:{par: 'test', par2: 'test2'}
		, displayToolbar: true
		, canResetParameters: false
		, iframe: {
			height: '500px'
		    , width: '100%'
			, style: 'border: 0px;'
		}
	});
	document.getElementById('targetDiv').innerHTML = html;
};
</PRE>
</BLOCKQUOTE>
<hr>
<div height="300px" width="800px" id='targetDiv'></div>
<hr>

<script type="text/javascript">
	execTest2();
</script>
</body>
</html>