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

<%@page import="it.eng.spagobi.services.oauth2.Oauth2SsoService"%>
<%@page import="it.eng.spagobi.security.OAuth2.OAuth2Client"%>
<%@page import="it.eng.spagobi.security.OAuth2.OAuth2Config"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="org.json.JSONObject"%>

<%
OAuth2Config oauth2Config = OAuth2Config.getInstance();
%>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <title>OIDC implicit flow</title>
  </head>
  <body>
    <script type="text/javascript" nonce="<%= request.getAttribute("cspNonce") %>">
    
    const authorizeEndpoint = '<%= StringEscapeUtils.escapeJavaScript(oauth2Config.getAuthorizeUrl()) %>';
    const clientId = '<%= StringEscapeUtils.escapeJavaScript(oauth2Config.getClientId()) %>';
    const scope = '<%= StringEscapeUtils.escapeJavaScript(oauth2Config.getScopes()) %>';
    const redirectUri = '<%= StringEscapeUtils.escapeJavaScript(oauth2Config.getRedirectUrl()) %>';
    const nonce = '<%= StringEscapeUtils.escapeJavaScript((String) session.getAttribute(Oauth2SsoService.NONCE)) %>';
    const authorization_endpoint = new URL(authorizeEndpoint);
    
    window.sessionStorage.setItem("client_id", clientId);
    window.sessionStorage.setItem("oidc_origin", authorization_endpoint.origin);
    window.sessionStorage.setItem("redirect_uri", redirectUri);
    window.sessionStorage.setItem("nonce", nonce);

    if (window.location.hash) {
        var args = new URLSearchParams(window.location.hash.substring(1));
        var id_token = args.get("id_token");
        var state = args.get("state");
        var session_state = args.get("session_state");
        
        if (id_token && state) {
        	if (window.sessionStorage.getItem("state") !== state){
        	    throw Error("Probable session hijacking attack!");
        	}

    		// storing id_token for later usage (on logout)
            window.sessionStorage.setItem("id_token", id_token);
            window.sessionStorage.setItem("session_state", session_state);
    		// we don't need state anymore
            window.sessionStorage.removeItem("state");

			var lastRedirectUri = window.location.href.split("?")[0];
			var args = new URLSearchParams({
	            PAGE: "LoginPage",
	            NEW_SESSION: "TRUE",
	            id_token: id_token
			});
			window.location = lastRedirectUri + "?" + args;
        } else {
          startOauth2Flow();
        }
      } else {
        startOauth2Flow();
      }

     function startOauth2Flow() {
     	var state = generateRandomString(64);
     	
     	window.sessionStorage.setItem("state", state);

         var args = new URLSearchParams({
             response_type: "code id_token",
             client_id: clientId,
             state: state,
             nonce: nonce,
             redirect_uri: redirectUri,
             scope: scope
         });
         window.location = authorizeEndpoint + "?" + args;
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