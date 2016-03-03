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