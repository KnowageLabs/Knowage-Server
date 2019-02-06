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
		 *  setup some basic informations in order to invoke Knowage server's services
		 */
		Sbi.sdk.services.setBaseUrl({
	        protocol: 'http'     
	        , host: 'localhost'
	        , port: '8080'
	        , contextPath: 'knowage'
	        , controllerPath: 'servlet/AdapterHTTP'  
	    });
		
		doLogin = function() {
			var userEl = document.getElementById('user');
			var passwordEl = document.getElementById('password');
			var user = userEl.value;
			var password = passwordEl.value;

		    /*
		    * the callback invoked uppon request termination
		    *
			* @param xhr the XMLHttpRequest object
		    */
			var cb = function(xhr) {
		    	var description = document.getElementById('introduction');
				var authenticationEl =  document.getElementById('authentication');
				var examplesEl =  document.getElementById('examples');
				authenticationEl.style.display = "none";
				description.style.display = "none";
				examplesEl.style.display = "inline";
			};

		   /*
		    * authentication function
		    *
			* @param credentials the list of parameters to pass to the authentication servics (i.e. user & password)
		    * @param headers an array containing the headers of the request
			* @param callbackOk the callback function to be called after success response
			* @param callbackError (optional) the callback function to be called after error response
		    */
			Sbi.sdk.cors.api.authenticate({
		    	credentials: 'user=' + user + '&password=' + password
		    	, headers: [{
		    		name: 'Content-Type',
		    		value: 'application/x-www-form-urlencoded'
		    	}]
		    	, callbackOk: cb
		    })
		}
		
		doLoginByToken = function() {
			var tokenEl = document.getElementById('token');
			var token = tokenEl.value;
			
			/*
		    * the callback invoked uppon request termination
		    *
			* @param xhr the XMLHttpRequest object
		    */
			var cb = function(xhr) {
		    	var description = document.getElementById('introduction');
				var authenticationEl =  document.getElementById('authentication');
				var examplesEl =  document.getElementById('examples');
				authenticationEl.style.display = "none";
				description.style.display = "none";
				examplesEl.style.display = "inline";
			};
						
			Sbi.sdk.cors.api.authenticateByToken({
				token: 'token=' + token
		    	, headers: [{
		    		name: 'Content-Type',
		    		value: 'application/x-www-form-urlencoded'
		    	}]
		    	, callbackOk: cb
		    });
		}
	</script>
</head>


<body>
<h2>Welcome to Knowage SDK - JS API demo - Using CORS</h2>
<div id="introduction">
<br/>
<b>Introduction</b><br/>
In order to use javascript API you have to authenticate in some way.<br/>
In Knowage SDK, if you use CORS there are two ways to do that:
<ul>
<li> Basic Authentication </li>
<li> Use the session of an already authenticated user.
In this case it is also available the function <i>Sbi.sdk.cors.api.authenticate</i> that let you authenticate inside Knowage. Use the login form to authenticate using this function.
Some new examples (that need the user authenticated in order to work) will appear.</li>
</ul>
<br/>
</div>

<div id="authentication" style="display:inline">
<span><b>Login in Knowage using athenticate function of Sbi.sdk.cors.api namespace.</b></span>
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
<b>Examples without Basic Authentication</b>
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
<!--	<dt> <a target="_blank" href="example5.jsp">Example 5 : injectDocument into existing div using ExtJs UI</a>
	<dd>  Use <i>injectDocument</i> function to inject into an existing div an html string that contains the definition of an iframe 
	pointing to the execution service. <br>The html string is generated internally using <i>getDocumentHtml</i> function. In this example 
	differently from the previous the new execution module, fully based on ajax technology, is invoked.
	<p>-->
 <dt> <a target="_blank" href="example5.jsp">Example 5 : injectDocument into non-existing div</a>
	<dd>  Use <i>injectDocument</i> function to inject into a div an html string that contains the definition of an iframe 
	pointing to the execution service. <br>In this example the specified target div does not exist so it is created on the fly by the function.
	<p>
	<dt> <a target="_blank" href="example6_cors.jsp">Example 6 : getDataSetList using CORS</a>
	<dd>  Use <i>getDataSetList</i> function to retrieve the list of all datasets.
	<p>
	<dt> <a target="_blank" href="example7_cors.jsp">Example 7 : executeDataSet using CORS</a>
	<dd>  Use <i>executeDataSet</i> function to get the content of a specific dataset.
	      <p>
    <dt> <a target="_blank" href="example8.jsp">Example 8 : getDocuments</a>
    <dd>  Use <i>getDocumentsList</i> function to get the list of visible documents.
</dl>
</div>
<!-- 
<div id="examples_with_authentication">
<b>Example with basic authentication</b><br/>
These examples use Basic Authentication header, so it is not necessary to be logged in Knowage in order to see them working</b>
<dl>
	<dt> <a target="_blank" href="example6_cors_basic.jsp">Example 6.2 : getDataSetList with Basic Authentication</a>
	<dd>  Use <i>getDataSetList</i> function to retrieve the list of all datasets.
	<p>
	<dt> <a target="_blank" href="example7_cors_basic.jsp">Example 7.2 : executeDataSet with Basic Authentication</a>
	<dd>  Use <i>executeDataSet</i> function to get the content of a specific dataset.
</dl>
</div>
 -->
</body>
</html>