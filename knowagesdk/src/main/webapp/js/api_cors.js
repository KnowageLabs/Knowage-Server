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
Sbi.sdk.namespace('Sbi.sdk.cors.api');


/**
* @namespace Sbi.sdk.cors
*/

/**
 * There are three main advantages on using CORS over jsonp:
 * <ul>
 *  <li>all the methods are available while in jsonp only GET request can be done;</li>
 *  <li>if an error occurs it is possible to manage it with CORS, while in jsonp it is only possible to set a timeout;</li>
 *  <li>jsonp has security problems (see later for an example).</li>
 * </ul>
 * @see {@link Sbi.sdk.api}
 * @namespace Sbi.sdk.cors.api
 **/
Sbi.sdk.apply(Sbi.sdk.cors.api, {

	elId: 0
	, dataSetList: {}

	, authenticate: function ( config ) {
		var serviceUrl = Sbi.sdk.services.getServiceUrl('authenticate', config.params);
		Sbi.sdk.cors.asyncRequest({
			method: 'POST',
			url: serviceUrl,
			headers: config.headers,
			callbackOk: config.callbackOk,
			callbackError: config.callbackError,
			body: config.credentials
		});
	}

	/**
	 * It returns the list of datasets. This time config contains two callback functions: callbackOk and callbackError. This is because with CORS it is possible to know when the server returns an error, so we can define the behavior to have if there is an error. However, it is not mandatory to specify the callbackError function: there is a default one that create a pop up with status code and text describing the error.
	 * @example
 	 * execTest6 = function() {
	 *    Sbi.sdk.cors.api.getDataSetList({
	 *    	callbackOk: function(obj) {
	 *    		str = '';
	 *
	 *    		for (var key in obj){
	 *    			str += "<tr><td>" + obj[key].label + "</td><td>" + obj[key].name + "</td><td>" + obj[key].description + "</td></tr>";
	 *  			}
	 *
	 *  			document.getElementById('datasets').innerHTML = str;
	 *		}
	 *    });
	 *	};
	 * @method Sbi.sdk.cors.api.getDataSetList
	 * @param {Object} config - the configuration
	 * @param {ResponseCallback} config.callbackOk - function to be called after the ok response is returned by the server
	 * @param {ResponseCallback} [config.callbackError] - function to be called after the error response is returned by the server
	 */
	, getDataSetList: function ( config ) {
		var baseUrl = Sbi.sdk.services.baseUrl;
		var serviceUrl = baseUrl.protocol + '://' + baseUrl.host + ":" + baseUrl.port + '/' + baseUrl.contextPath + '/restful-services/2.0/datasets';

		var headers = [];
		if (config.basicAuthentication !== undefined) {
			var ba = config.basicAuthentication;
			var encoded = btoa(ba.userName + ':' + ba.password);

			headers[0] = {
				name: 'Authorization',
				value: 'Basic ' + encoded
			}
		}

		Sbi.sdk.cors.asyncRequest({
			method: 'GET',
			url: serviceUrl,
			headers: headers,
			callbackOk: config.callbackOk,
			callbackError: config.callbackError
		});
	}

	/**
	 * It executes a dataset. This time config contains two callback functions: callbackOk and callbackError. This is because with CORS it is possible to know when the server returns an error, so we can define the behavior to have if there is an error. However, it is not mandatory to specify the callbackError function: there is a default one that create a pop up with status code and text describing the error.
	 * @example
	 * execTest7 = function() {
	 *    Sbi.sdk.cors.api.executeDataSet({
	 *    	datasetLabel: 'DS_DEMO_EXTCHART'
	 *    	, parameters: {
	 *    		par_year: 1998,
	 *    		par_family: 'Food'
	 *    	}
	 *    	, callbackOk: function(obj) {
	 *    		var str = "<th>Id</th>";
	 *
	 *  			var fields = obj.metaData.fields;
	 *  			for(var fieldIndex in fields) {
	 *  				if (fields[fieldIndex].hasOwnProperty('header'))
	 *  					str += '<th>' + fields[fieldIndex]['header'] + '</th>';
	 *  			}
	 *
	 *  			str += '<tbody>';
	 *
	 *  			var rows = obj.rows;
	 *  			for (var rowIndex in rows){
	 *  				str += '<tr>';
	 *  				for (var colIndex in rows[rowIndex]) {
	 *  					str += '<td>' + rows[rowIndex][colIndex] + '</td>';
	 *  				}
	 *  				str += '</tr>';
	 *  			}
	 *
	 *  			str += '</tbody>';
	 *
	 *  			document.getElementById('results').innerHTML = str;
	 *		}});
	 *};
	 * @method Sbi.sdk.cors.api.executeDataSet
	 * @param {Object} config - the configuration
	 * @param {String} config.documentLabel - the document label
	 * @param {Object} [config.parameters] - the values of dataset parameters
	 * @param {ResponseCallback} config.callbackOk - function to be called after the ok response is returned by the server
	 * @param {ResponseCallback} [config.callbackError] - function to be called after the error response is returned by the server
	 */
	, executeDataSet: function ( config ) {
		var baseUrl = Sbi.sdk.services.baseUrl;
		var serviceUrl = baseUrl.protocol + '://' + baseUrl.host + ":" + baseUrl.port + '/' + baseUrl.contextPath + '/restful-services/2.0/datasets/';
		serviceUrl += config.datasetLabel + '/content';

		if (config.parameters !== undefined) {
			var first = true;

			for(var parameter in config.parameters) {
				if (first) {
					serviceUrl += '?';
					first = false;
				}
				else serviceUrl += '&';

				serviceUrl += parameter + '=' + config.parameters[parameter];
			}
		}

		var headers = [];
		if (config.basicAuthentication !== undefined) {
			var ba = config.basicAuthentication;
			var encoded = btoa(ba.userName + ':' + ba.password);

			headers[0] = {
				name: 'Authorization',
				value: 'Basic ' + encoded
			}
		}

		Sbi.sdk.cors.asyncRequest({
			method: 'GET',
			url: serviceUrl,
			headers: headers,
			callbackOk: config.callbackOk,
			callbackError: config.callbackError
		});
	}

	/**
	 * It returns the list of Documents. This time config contains two callback functions: callbackOk and callbackError. This is because with CORS it is possible to know when the server returns an error, so we can define the behavior to have if there is an error. However, it is not mandatory to specify the callbackError function: there is a default one that create a pop up with status code and text describing the error.
	 * @example
 	 * execTest6 = function() {
	 *    Sbi.sdk.cors.api.getDocuments({
	 *    	callbackOk: function(obj) {
	 *    		str = '';
	 *
	 *    		for (var key in obj){
	 *    			str += "<tr><td>" + obj[key].label + "</td><td>" + obj[key].name + "</td><td>" + obj[key].description + "</td></tr>";
	 *  			}
	 *
	 *  			document.getElementById('documentss').innerHTML = str;
	 *		}
	 *    });
	 *	};
	 * @method Sbi.sdk.cors.api.getDocuments
	 * @param {Object} config - the configuration
	 * @param {ResponseCallback} config.callbackOk - function to be called after the ok response is returned by the server
	 * @param {ResponseCallback} [config.callbackError] - function to be called after the error response is returned by the server
	 */
	, getDocuments: function ( config ) {
		var baseUrl = Sbi.sdk.services.baseUrl;
		var serviceUrl = baseUrl.protocol + '://' + baseUrl.host + ":" + baseUrl.port + '/' + baseUrl.contextPath + '/restful-services/2.0/documents';

		var headers = [];
		if (config.basicAuthentication !== undefined) {
			var ba = config.basicAuthentication;
			var encoded = btoa(ba.userName + ':' + ba.password);

			headers[0] = {
				name: 'Authorization',
				value: 'Basic ' + encoded
			}
		}

		Sbi.sdk.cors.asyncRequest({
			method: 'GET',
			url: serviceUrl,
			headers: headers,
			callbackOk: config.callbackOk,
			callbackError: config.callbackError
		});
	}

});