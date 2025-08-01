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

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>OAuth2 PKCE flow</title>
  </head>
  <body>
    <script type="text/javascript" nonce="<%= request.getAttribute("cspNonce") %>">  
    	start();
        
        async function start() {
        	if (window.location.search) {
                var args = new URLSearchParams(window.location.search);
                var code = args.get("code");
                var state = args.get("state");
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
                		    code_verifier: window.sessionStorage.getItem("code_verifier"),
                		    grant_type: "authorization_code",
                		    redirect_uri: oauth2Config.redirectUrl,
                		    code: code,
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
        	const state = generateRandomString(64);
        	const verifier = generateRandomString(128);
         	const challenge = await generateCodeChallenge(verifier);
            sessionStorage.setItem("state", state);
        	sessionStorage.setItem("code_verifier", verifier);
        	var args = new URLSearchParams({
                response_type: "code",
                client_id: oauth2Config.clientId,
                code_challenge_method: "S256",
                code_challenge: challenge,
                state: state,
                redirect_uri: oauth2Config.redirectUrl,
                scope: "openid profile"
            });
            window.location = oauth2Config.authorizeUrl + "?" + args;
            
        }
        

     // Funzione per generare una stringa casuale (code_verifier)
     function generateRandomString(length) {
      const charset = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~';
      let result = '';
      const array = new Uint8Array(length);
      window.crypto.getRandomValues(array);
      for (let i = 0; i < array.length; i++) {
      result += charset[array[i] % charset.length];
      }
      return result;
     }


	  // Funzione per generare il code_challenge (SHA256 + base64url)
	  async function generateCodeChallenge(codeVerifier) {
	   const encoder = new TextEncoder();
	   const data = encoder.encode(codeVerifier);
	   const digest = await window.crypto.subtle.digest('SHA-256', data);
	   const base64url = btoa(String.fromCharCode(...new Uint8Array(digest)))
	   .replace(/\+/g, '-')
	   .replace(/\//g, '_')
	   .replace(/=+$/, '');
	   return base64url;
	  }

        
    </script>
  </body>
</html>