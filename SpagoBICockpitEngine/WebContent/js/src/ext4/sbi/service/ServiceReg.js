/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

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

