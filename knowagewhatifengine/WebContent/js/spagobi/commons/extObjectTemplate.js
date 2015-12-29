/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */


/**
  * Object name
  *
  * [description]
  *
  *
  * Public Properties
  *
  * [list]
  *
  *
  * Public Methods
  *
  *  [list]
  *
  *
  * Public Events
  *
  *  [list]
  *
  * Authors
  *
  * - name (mail)
  */

Ext.ns("Sbi.whatIf");

Sbi.whatIf.WhatIf = function(config) {

	var c = Ext.apply({
		// set default values here
	}, config || {});

	this.services = new Array();
	var params = {};
	this.services['loadDataStore'] =  Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'EXEC_QUERY_ACTION'
		, baseParams: params
	});

	this.addEvents();

	this.initThis();
	this.initThat();

	c = Ext.apply(c, {
		title: 'Results',
		layout: 'fit',
		items: [this.grid]
	})

	// constructor
    Sbi.whatIf.WhatIf.superclass.constructor.call(this, c);

    this.addEvents();
};

Ext.extend(Sbi.whatIf.WhatIf, Ext.util.Observable, {

    services: null


    // public methods
});