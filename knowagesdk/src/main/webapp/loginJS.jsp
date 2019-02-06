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
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Login</title>
	<style>
	body, p { font-family:Tahoma; font-size:10pt; padding-left:30; }
	pre { font-size:8pt; }
	</style>
	
<!--
 <script type="text/javascript" src="/knowagesdk/js/sbisdk-all-production.js"></script>
-->
 
<script type="text/javascript" src="/knowagesdk/js/commons.js"></script>
       <script type="text/javascript" src="/knowagesdk/js/ajax.js"></script>
       <script type="text/javascript" src="/knowagesdk/js/jsonp.js"></script>
       <script type="text/javascript" src="/knowagesdk/js/cors.js"></script>
       <script type="text/javascript" src="/knowagesdk/js/services.js"></script>      
       <script type="text/javascript" src="/knowagesdk/js/api_jsonp.js"></script>
       <script type="text/javascript" src="/knowagesdk/js/api_cors.js"></script>
       <script type="text/javascript" src="/knowagesdk/js/api.js"></script>

	<script type="text/javascript">

		/*
		 *  setup some basic informations in order to invoke SpagoBI server's services
		 */
		Sbi.sdk.services.setBaseUrl({
	        protocol: 'http'     
	        , host: 'localhost'
	        , port: '8080'
	        , contextPath: 'knowage'
	        , controllerPath: 'servlet/AdapterHTTP'  
	    });


        /*
         *  This is the timeout to be considered in case the call fails and the error handler is invoked.
         *  Default values is 30 seconds!!! That's why we need to set it to an reasonable value.
         *  If it is set to 0 ms, an error occurs.
         *  TODO: investigate why it cannot be set to 0
         */
		Sbi.sdk.jsonp.timeout = 5000;
		
		doLogin = function() {
			var userEl = document.getElementById('user');
			var passwordEl = document.getElementById('password');
			var user = userEl.value;
			var password = passwordEl.value;

		    /*
		    * the callback invoked uppon request termination
		    *
			* @param result the server response
			* @param args parameters sent to the server with the original request
			* @param seccess true if the service has been executed by the server in a succesfull way
		    */ 
		    var cb = function(result, args, success) {
		        
				if(success === true) {
					var authenticationEl =  document.getElementById('authentication');
					var examplesEl =  document.getElementById('examples');
					authenticationEl.style.display = "none";
					examplesEl.style.display = "inline";
				} else {
					alert('ERROR: Wrong username or password');
				}
		    };

		   /*
		    * authentication function
		    *
			* @param params the list of parameters to pass to the authentication servics (i.e. user & password)
			* @param callback the callback definition (i.e. fn: the function to call; scope: the scope of invocation; 
			*        args: parameters to append to the callback invocation)
		    */ 
		    Sbi.sdk.api.authenticate({ 
				params: {
					user: user
					, password: password
				}
				
				, callback: {
					fn: cb
					, scope: this
					//, args: {arg1: 'A', arg2: 'B', ...}
				}
			});
		}
		
		doLoginByToken = function() {
			var tokenEl = document.getElementById('token');
			var token = tokenEl.value;
			
			/*
		    * the callback invoked uppon request termination
		    *
			* @param xhr the XMLHttpRequest object
		    */
			var cb = function(result, args, success) {
		        
				if(success === true) {
					var authenticationEl =  document.getElementById('authentication');
					var examplesEl =  document.getElementById('examples');
					authenticationEl.style.display = "none";
					examplesEl.style.display = "inline";
				} else {
					alert('ERROR: Wrong token');
				}
		    };
						
			Sbi.sdk.api.authenticateByToken({
				params: {
					token: token
				}
				, callback: {
					fn: cb
					, scope: this
					//, args: {arg1: 'A', arg2: 'B', ...}
				}
		    });
		}
	</script>
</head>


<body>
<h2>Welcome to SpagoBI SDK - JS API demo</h2>
<br/>

<div id="authentication" style="display:inline">
<span><b>Login with biuser/biuser</b></span>
<form  id="authenticationForm">
Name: <input type="text" id='user' name="user" size="30" value="biuser"/><br/>
Password: <input type="password" id="password" name="password" size="30" value="biuser"/><br/>
<input type="button" value="Login" onclick="javascript:doLogin()"/>
</form>
<br/>
<br/>
<span><b>Login in Knowage using JWT token.</b></span>
<form id="authenticationFormByToken">
JWT token: <input type="text" id="token" name="token" size="110"/><br/>
<input type="button" value="Login" onclick="javascript:doLoginByToken()"/>
</form>
<br/>
</div>

<div id="examples" style="display:none">
<b>Examples</b>
<dl>
	<dt> <a target="_blank" href="example1.jsp">Example 1 : getDocumentUrl</a>
	<dd> Use <i>getDocumentUrl</i> function to create the invocation url used to call execution service asking for a 
	specific execution (i.e. document + execution role + parameters).
	<p>
	<dt> <a target="_blank" href="example2.jsp">Example 2 : getDocumentHtml</a>
	<dd> Use <i>getDocumentHtml</i> function to get an html string that contains the definition of an iframe 
	pointing to the execution service. <br>The src property of the iframe is internally populated using <i>getDocumentUrl</i> function.
	<p>
	<dt> <a target="_blank" href="example3.jsp">Example 3 : injectDocument into existing div</a>
	<dd>  Use <i>injectDocument</i> function to inject into an existing div an html string that contains the definition of an iframe 
	pointing to the execution service. <br>The html string is generated internally using <i>getDocumentHtml</i> function.
    <p>
    <dt> <a target="_blank" href="example4.jsp">Example 4 : injectDocument into non-existing div</a>
    <dd>  Use <i>injectDocument</i> function to inject into a div an html string that contains the definition of an iframe 
    pointing to the execution service. <br>In this example the specified target div does not exist so it is created on the fly by the function.
    <p>

<!--  <dt> <a target="_blank" href="example5.jsp">Example 5 : injectDocument into existing div using ExtJs UI</a>
	<dd>  Use <i>injectDocument</i> function to inject into an existing div an html string that contains the definition of an iframe 
	pointing to the execution service. <br>The html string is generated internally using <i>getDocumentHtml</i> function. In this example 
	differently from the previous the new execution module, fully based on ajax technology, is invoked.
	<p>
	 -->
	<dt> <a target="_blank" href="example6.jsp">Example 6 : getDataSetList</a>
	<dd>  Use <i>getDataSetList</i> function to retrieve the list of all datasets.
	<p>
	<dt> <a target="_blank" href="example7.jsp">Example 7 : executeDataSet</a>
	<dd>  Use <i>executeDataSet</i> function to get the content of a specific dataset.
	   <p>
    <dt> <a target="_blank" href="example8.jsp">Example 8 : getDocuments</a>
    <dd>  Use <i>getDocumentsList</i> function to get the list of visible documents.

</dl>
</div>

</body>
</html>