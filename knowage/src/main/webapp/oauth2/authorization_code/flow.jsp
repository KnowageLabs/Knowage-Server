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
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>OAuth2 standard authorization code flow</title>
  </head>
  <body>
    <script type="text/javascript" nonce="<%= request.getAttribute("cspNonce")%>">
    	
    	start();
        
        async function start() {
        	
	        	if (window.location.search) {
                    var args = new URLSearchParams(window.location.search);
                    var state = args.get("state");
                    var code = args.get("code");
                    
                    if (code) {
                    	const oauth2Config = await fetchConfig();
                    	if (window.sessionStorage.getItem("state") !== state){
                    	    throw Error("Probable session hijacking attack!");
                    	}
                    	fetch(oauth2Config.accessTokenUrl, {
                    		  method: 'POST',
                    		  headers: {
                    		    'Content-Type': 'application/x-www-form-urlencoded'
                    		  },
                    		  body: new URLSearchParams({
                    		    client_id: oauth2Config.clientId,
                    		    grant_type: "authorization_code",
                    		    redirect_uri: oauth2Config.redirectUrl,
                    		    code: code,
                    		    state: state,
                    		    client_secret: oauth2Config.clientSecret
                    		  })
                    		})
                    		.then(response => response.json().then(data => ({ status: response.status, body: data })))
                    		.then(({ status, body }) => {
                    		  if (status === 200) {
                    		    // storing id_token for later usage (on logout)
                    		    window.sessionStorage.setItem("id_token", body.id_token);

                    		    const lastRedirectUri = window.location.href.split('?')[0];
                    		    const args = new URLSearchParams({
                    		      PAGE: "LoginPage",
                    		      NEW_SESSION: "TRUE",
                    		      access_token: body.access_token
                    		    });

                    		    window.location = lastRedirectUri + "?" + args;
                    		  } else {
                    		    alert(`Error: ${body.error_description} (${body.error})`);
                    		  }

                    		})
                    		.catch(error => {
                    		  console.error("Errore nella richiesta fetch:", error);
                    		});

                    } else {
                    	saveExtraQueryParameters();
                    	startOauth2Flow();
                    }
                }
        	
        }
		
        
        async function fetchConfig() {
        	const response = await fetch('/knowage/restful-services/oauth2configservice', {
    	        method: 'GET'
    	    })
        	
        	if (!response.ok) {
	               throw new Error("Errore nella chiamata oauth2configservice");
	        }
        	const config = await response.json();
    	    return config;
        }
        
        async function startOauth2Flow() {
        	const oauth2Config = await fetchConfig();
        	
        	var state = generateRandomString(64);

           	window.sessionStorage.setItem("state", state);

            var args = new URLSearchParams({
                response_type: "code",
                client_id: oauth2Config.clientId,
                state: state,
                redirect_uri: oauth2Config.redirectUrl,
                scope: oauth2Config.scopes
            });
            window.location = oauth2Config.authorizeUrl + "?" + args;
        }

        function generateRandomString(length) {
            var text = "";
            var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

            for (var i = 0; i < length; i++) {
                text += possible.charAt(Math.floor(Math.random() * possible.length));
            }

            return text;
        }
        
        function saveExtraQueryParameters() {
        	var args = new URLSearchParams();
        	if (window.location.search) {
                args = new URLSearchParams(window.location.search);
                // removing unnecessary parameters
                args.delete('PAGE');
                args.delete('NEW_SESSION');
                args.delete('ACTION_NAME');
                args.delete('LIGHT_NAVIGATOR_DISABLED');
        	}
        	if (args.size > 0) {
        		// storing extra-query-parameters (combined with previous ones) for later usage (when user is redirected into knowage)
        		var previousExtraParameters = getSavedExtraQueryParameters();
        		if (!previousExtraParameters) {
        			window.sessionStorage.setItem("extra-query-parameters", args);
        		} else {
            		let combined = new URLSearchParams({
              		  ...Object.fromEntries(new URLSearchParams(previousExtraParameters)),
              		  ...Object.fromEntries(args)
              		});
              		window.sessionStorage.setItem("extra-query-parameters", combined);
        		}
        	}
        }
        
        function getSavedExtraQueryParameters() {
        	return window.sessionStorage.getItem("extra-query-parameters");
        }
        
    </script>
  </body>
</html>