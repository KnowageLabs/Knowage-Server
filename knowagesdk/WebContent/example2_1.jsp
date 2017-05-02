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
<title>Example 2_1: Authenticate and Execute Example</title>
<style>
body,p {
	font-family: Tahoma;
	font-size: 10pt;
	padding-left: 30;
}

pre {
	font-size: 8pt;
}
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
<script type="text/javascript"
	src="/knowagesdk/js/lib/angular/angular_1.4/angular.min.js"></script>

<script type="text/javascript">

        /*
         *  setup some basic informations in order to invoke Knowage server's services
         */
         
         var user = 'biuser';
         var password = 'biuser';
         
        Sbi.sdk.services.setBaseUrl({
            protocol: 'http'     
            , host: 'localhost'
            , port: '8080'
            , contextPath: 'knowage'
            , controllerPath: 'servlet/AdapterHTTP'  
        });
        
        
        execTest2 = function() {
            var html = Sbi.sdk.api.getDocumentHtml({
                documentLabel: 'par_document_label'
                , executionRole: '/spagobi/user'
                , parameters: {ELAB_ID: 'ELA_T_ANVOPOD_1', TIPO_AN_ID: 'ANVOFATT'}
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
      
            /*
            * the callback invoked uppon request termination
            *
            * @param result the server response
            * @param args parameters sent to the server with the original request
            * @param seccess true if the service has been executed by the server in a succesfull way
            */ 
            var cb = function(result, args, success) {
                
                if(success === true) {
                	execTest2();
                } else {
                    alert('ERROR: Wrong username or password');
                }
            };

            

					doLoginAndExecute = function() {

						Sbi.sdk.api.authenticate({
							params : {
								user : user,
								password : password
							}

							,
							callback : {
								fn : cb,
								scope : this
							}
						});
					};
				</script>
</head>

<body>
	<h2>Example 2_1 : authenticate and getDocumentHtml</h2>
	<hr>
	<b>Description: </b> Do Authentication and Use
	<i>getDocumentHtml</i> function to get an html string that contains the
	definition of an iframe pointing to the execution service. The src
	property of the iframe is internally populated using
	<i>getDocumentUrl</i> function.
	<hr>
	<div height="300px" width="800px" id='targetDiv'></div>
	<hr>

	<script type="text/javascript">
doLoginAndExecute();
</script>
</body>
</html>