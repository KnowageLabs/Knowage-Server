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

Sbi.sdk.namespace('Sbi.sdk.services');


Sbi.sdk.apply(Sbi.sdk.services, {

    services: null

    , baseUrl:  {
		protocol: 'http'
		, host: 'localhost'
	    , port: '8080'
	    , contextPath: 'knowage'
	    , controllerPath: 'servlet/AdapterHTTP'
	}

    , initServices: function() {
        this.services = {};
        this.services.authenticate = {
            type: 'ACTION',
            name: 'LOGIN_ACTION_WEB',
            baseParams: {NEW_SESSION: 'TRUE'}
        };
        
        this.services.authenticateByToken = {
			type: 'ACTION',
	        name: 'LOGIN_ACTION_BY_TOKEN',
	        baseParams: {NEW_SESSION: 'TRUE'}
        };

        this.services.execute = {
            type: 'ACTION',
            name: 'EXECUTE_DOCUMENT_ANGULAR_ACTION',
            baseParams: {NEW_SESSION: 'TRUE'}
        };

        this.services.adHocReporting = {
    		type: 'ACTION',
            name: 'AD_HOC_REPORTING_START_ACTION',
            baseParams: {NEW_SESSION: 'TRUE'}
        };
    }

    , setBaseUrl: function(u) {
        Sbi.sdk.apply(this.baseUrl, u || {});
    }

    , getServiceUrl: function(serviceName, p) {
        var urlStr = null;

        if(this.services === null) {
            this.initServices();
        }

        if(this.services[serviceName] === undefined) {
            alert('ERROR: Service [' + serviceName + '] does not exist');
        } else {
            urlStr = '';
            urlStr = this.baseUrl.protocol + '://' + this.baseUrl.host + ":" + this.baseUrl.port + '/' + this.baseUrl.contextPath + '/' + this.baseUrl.controllerPath;
            var params;
            if(this.services[serviceName].type === 'PAGE'){
            	params = {PAGE: this.services[serviceName].name};
            } else {
            	params = {ACTION_NAME: this.services[serviceName].name};
            }

            Sbi.sdk.apply(params, p || {}, this.services[serviceName].baseParams || {});
            var paramsStr = Sbi.sdk.urlEncode(params);
            urlStr += '?' + paramsStr;
        }

        return urlStr;
    }
});

