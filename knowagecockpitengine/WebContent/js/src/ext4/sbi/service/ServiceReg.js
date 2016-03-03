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

/**
 *
 * A service proxy object
 *
 *
 *  @author
 *  Andrea Gioia (andrea.gioia@eng.it)
 */

Ext.define('Sbi.service.ServiceReg', {
	extend: 'Ext.util.Observable'

	, config: {
		serviceBaseConfs: {}
		, services: {}
	}

	, constructor : function(config) {
		this.initConfig(config);
		this.callParent();
	}

	, addServiceBaseConf: function(name, baseConf) {
		this.serviceBaseConfs[name] = baseConf;
	}

	, registerService: function(serviceName, serviceConf, serviceBaseConfs) {
		if(Sbi.isValorized(serviceBaseConfs)) {
			if(Ext.isString(serviceBaseConfs)) {
				serviceBaseConfs = this.serviceBaseConfs[serviceBaseConfs];
				if(Sbi.isValorized(serviceBaseConfs) == false) {
					Sbi.warn("[ServiceReg.registerService]: Impossible to find a serviceBaseConf named [" + serviceBaseConfs + "]");
				}
			}
		}

		var conf = Ext.apply({}, serviceConf || {}, serviceBaseConfs || {});
		var service = Ext.create('Sbi.service.RestService', conf);
		this.services[serviceName] = service;
		return service;
	}

	, getService: function(serviceName) {
		return this.services[serviceName];
	}

	, getServiceUrl: function(serviceName, options) {
		var serviceUrl = null;
		var service = this.services[serviceName];
		if(service) {
			serviceUrl = service.getServiceUrl(options);
		} else {
			Sbi.warn("[ServiceReg.callService]: service [" + serviceName + "] does not exist. Available service are [" + Sbi.toSource(this.services, true) + "]");
		}
		return serviceUrl;
	}

	, callService: function(serviceName, options) {
		var serviceCalled = true;

		var service = this.getService(serviceName);
		if(service) {
			service.doRequest(options);
		} else {
			serviceCalled = false;
			Sbi.warn("[ServiceReg.callService]: service [" + serviceName + "] does not exist. Available service are [" + Sbi.toSource(this.services, true) + "]");
		}
		return serviceCalled;
	}

});

