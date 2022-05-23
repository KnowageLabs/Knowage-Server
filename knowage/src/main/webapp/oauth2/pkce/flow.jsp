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

<%--
From https://github.com/curityio/pkce-javascript-example
--%>

<%@page import="it.eng.spagobi.security.OAuth2.OAuth2Config"%>

<%
OAuth2Config oauth2Config = OAuth2Config.getInstance();
%>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>OAuth2 PKCE flow</title>
  </head>
  <body>
    <script>
    const authorizeEndpoint = "<%= oauth2Config.getAuthorizeUrl() %>";
    const tokenEndpoint = "<%= oauth2Config.getAccessTokenUrl() %>";
    const clientId = "<%= oauth2Config.getClientId() %>";
    const redirectUri = "<%= oauth2Config.getRedirectUrl() %>";

        if (window.location.search) {
            var args = new URLSearchParams(window.location.search);
            var code = args.get("code");
            var state = args.get("state");
            
            if (code) {
            	if (window.sessionStorage.getItem("state") !== state){
            	    throw Error("Probable session hijacking attack!");
            	}
            	
                var xhr = new XMLHttpRequest();

                xhr.onload = function() {
                    var response = xhr.response;

                    if (xhr.status == 200) {
                    	// storing id_token for later usage (on logout)
                    	window.sessionStorage.setItem("id_token", response.id_token);
                    	
                    	var lastRedirectUri = window.location.href.split('?')[0];
                    	var args = new URLSearchParams({
                    		PAGE : "LoginPage",
                    		NEW_SESSION : "TRUE",
                            access_token: response.access_token
                        });
                        window.location = lastRedirectUri + "?" + args;
                    } else {
                        alert("Error: " + response.error_description + " (" + response.error + ")");
                    }

                    document.getElementById("result").innerHTML = message;
                };
                xhr.responseType = 'json';
                xhr.open("POST", tokenEndpoint, true);
                xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
                xhr.send(new URLSearchParams({
                    client_id: clientId,
                    code_verifier: window.sessionStorage.getItem("code_verifier"),
                    grant_type: "authorization_code",
                    redirect_uri: redirectUri,
                    code: code,
                    state: state
                }));
            } else {
            	startOauth2Flow();
            }
        }

        function startOauth2Flow() {
        	var state = generateRandomString(64);
            var codeVerifier = generateRandomString(128);

            generateCodeChallenge(codeVerifier).then(function(codeChallenge) {
            	window.sessionStorage.setItem("state", state);
                window.sessionStorage.setItem("code_verifier", codeVerifier);

                var args = new URLSearchParams({
                    response_type: "code",
                    client_id: clientId,
                    code_challenge_method: "S256",
                    code_challenge: codeChallenge,
                    state: state,
                    redirect_uri: redirectUri,
                    scope: "openid profile"
                });
                window.location = authorizeEndpoint + "/?" + args;
            });
        }

        async function generateCodeChallenge(codeVerifier) {
            var digest = await crypto.subtle.digest("SHA-256",
                new TextEncoder().encode(codeVerifier));

            return btoa(String.fromCharCode(...new Uint8Array(digest)))
                .replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_')
        }

        function generateRandomString(length) {
            var text = "";
            var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

            for (var i = 0; i < length; i++) {
                text += possible.charAt(Math.floor(Math.random() * possible.length));
            }

            return text;
        }
        
    </script>
  </body>
</html>