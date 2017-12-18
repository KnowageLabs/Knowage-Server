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
  *  [list]
  * 
  * 
  * Public Events
  * 
  *  contentloaded: fired after the data has been loaded
  * 
  * Authors
  * 
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */

Ext.ns("Sbi.crosstab");

Sbi.crosstab.StaticCrosstabPreviewPanel = function(config) {
	
	// override CrosstabPreviewPanel service definition 
	this.services = this.services || new Array();
	var params = {OUTPUT_TYPE : "HTML"};
	this.services['loadCrosstab'] = this.services['loadCrosstab'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'LOAD_CROSSTAB_ACTION'
		, baseParams: params
	});
	
    Sbi.crosstab.StaticCrosstabPreviewPanel.superclass.constructor.call(this, config);
    
};

Ext.extend(Sbi.crosstab.StaticCrosstabPreviewPanel, Sbi.crosstab.CrosstabPreviewPanel, {

	// override
	refreshCrossTab: function(serviceResponseText) {
		this.removeAll(true);
		var c = {
			htmlData : serviceResponseText  // Static cross-table needs an HTML table coming with service response
			, bodyCssClass : 'crosstab'
		};
		this.crosstab = new Sbi.crosstab.core.HTMLCrossTab(c);
		this.add(this.crosstab);
		this.hideMask();
		this.doLayout();
	}

	,
	exportContent: function() {
		var exportedCrosstab = {SHEET_TYPE: 'STATIC_CROSSTAB', CROSSTABDEFINITION: this.crosstabDefinition};
		return exportedCrosstab;
	}
	
});