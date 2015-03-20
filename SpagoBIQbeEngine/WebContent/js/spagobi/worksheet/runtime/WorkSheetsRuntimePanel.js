/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  

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
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors - Alberto Ghedin (alberto.ghedin@eng.it)
 */
Ext.ns("Sbi.worksheet.runtime");

Sbi.worksheet.runtime.WorkSheetsRuntimePanel = function(template, config) { 
	
	var defaultSettings = {
		title: LN('sbi.worksheet.runtime.worksheetruntimepanel.title')
	};

	if(Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.runtime.workSheetsRuntimePanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.runtime.workSheetsRuntimePanel);
	}

	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.initPanels(template);

	c = Ext.apply(c, {
		id: 'runtimeworksheet',
		border: false,
		layout: 'fit',
		autoScroll: true,
		items: [this.sheetsContainerPanel]
	}); 
	this.addEvents('contentexported');
	Sbi.worksheet.runtime.WorkSheetsRuntimePanel.superclass.constructor.call(this, c);	
};

/**
 * @class Sbi.worksheet.runtime.WorkSheetsRuntimePanel
 * @extends Ext.Panel
 * 
 * WorkSheetsRuntimePanel
 */
Ext.extend(Sbi.worksheet.runtime.WorkSheetsRuntimePanel, Ext.Panel, {
	sheetsContainerPanel: null,

	initPanels: function(template){
		this.sheetsContainerPanel = new Sbi.worksheet.runtime.RuntimeSheetsContainerPanel({},template);		
		this.sheetsContainerPanel.on('contentexported',function(){this.fireEvent('contentexported');}, this);
	},

	exportContent: function(mimeType, fromDesigner, metadata, parameters){
		
		this.sheetsContainerPanel.exportContent(mimeType, fromDesigner, metadata, parameters);
	}
	
	,getAdditionalData: function(){
		if( this.sheetsContainerPanel){
			return this.sheetsContainerPanel.getAdditionalData();
		}
		
	}
});
