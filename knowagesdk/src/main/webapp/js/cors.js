/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
Sbi.sdk.namespace('Sbi.sdk.cors');

Sbi.sdk.apply(Sbi.sdk.cors, {
	/*
	 * The default callback function used when the server returns an error.
	 *
	 * @param obj the object contained in the response body
	 * @param status the status code of the response
	 * @param statusText the status text of the response
	 * */
	defaultCallbackError:  function (obj, status, statusText){
		var message = 'Error: ';
		if (status !== undefined) message += status + ' - ' + statusText;

		if (obj.errors !== undefined)
			message += '\n\n' + obj.errors[0].localizedMessage;

		alert(message);
	}

	/*
	 * It performs an async request.
	 *
	 * @param config it must contain the method (GET, POST etc..), the url, callbackOk that is the callback to be called if the server returns
	 * the aspected response. Optionally it can contains body, headers and callbackError (the callback function used when the server returns an error).
	 *
	 * To be used if the response is a Json string
	 * */
	, asyncRequest: function(config){
		var xhr = new XMLHttpRequest();

		if ("withCredentials" in xhr) { //The browser support XMLHttpRequest. Chrome, Firefox, Safari and new versions of Opera and Internet Explorer support it
			xhr.open(config.method, config.url, true);
		}
		else if (typeof XDomainRequest != "undefined") { //The browser is Internet Explorer 8 or 9 (they use XDomainRequest instead of XMLHttpRequest)
			xhr = new XDomainRequest();
			xhr.open(config.method, config.url);
		}
		else { //The browser doesn't support CORS
			alert('Your browser does not support CORS.');
			return;
		}

		xhr.onerror = function() {
			alert('Error while trying to contact server');
		}

		var callbackError;
		if (config.callbackError) callbackError = config.callbackError;
		else callbackError = this.defaultCallbackError;

		xhr.onload = function() {
			var obj = null;

			if (xhr.response !== "") obj = JSON.parse(xhr.response);

			if (xhr.status == '200' || xhr.status == '201'){

				/* A REST service could return a status code 200 even if there are errors
				 * (in such a case errors will be inside the Json object in the response body) */
				if (obj.errors !== undefined){
					callbackError(obj);
				} else {
					config.callbackOk(obj);
				}


			}
			else {
				callbackError(obj, xhr.status, xhr.statusText);
			}

		}

		for (var index in config.headers){
			var header = config.headers[index];
			xhr.setRequestHeader(header.name, header.value);
		}

		//It enables the use of cookies
		xhr.withCredentials = 'true';

		if (config.body !== "undefined"){
			xhr.send(config.body);
		}
		else xhr.send();
	}
});