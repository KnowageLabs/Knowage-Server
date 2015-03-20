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
 * Authors - Davide Zerbetto (davide.zerbetto@eng.it)
 */
Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.SeriesGroupingPanel = function(config) { 

	var defaultSettings = {
		title: LN('sbi.worksheet.designer.seriesgroupingpanel.title')
		, frame: true
		, emptyMsg: LN('sbi.worksheet.designer.seriesgroupingpanel.emptymsg')
	};
		
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.seriesGroupingPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.seriesGroupingPanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);

	// constructor	
	Sbi.worksheet.designer.SeriesGroupingPanel.superclass.constructor.call(this, c);
	
};

Ext.extend(Sbi.worksheet.designer.SeriesGroupingPanel, Sbi.worksheet.designer.ChartCategoryPanel, {
	    
	getSeriesGroupingAttribute : function () {
		return this.getCategory();
	}

	,
	setSeriesGroupingAttribute : function (attribute) {
		return this.setCategory(attribute);
	}
	
});