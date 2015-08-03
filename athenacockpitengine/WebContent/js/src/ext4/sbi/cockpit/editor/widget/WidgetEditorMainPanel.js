/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.editor.widget");

Sbi.cockpit.editor.widget.WidgetEditorMainPanel = function(config) {

	var defaultSettings = {
		name:'WidgetEditorMainPanel'
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.editor.widget.WidgetEditorMainPanel', defaultSettings);

	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	this.init();

	if(this.widgetType === Sbi.constants.cockpit.chart){
		c = {
			    activeTab: 0,
			    items: [this.customConfPanel]
			};
		
	} else {
		c = {
			    activeTab: 0,
			    items: [this.genericConfPanel,this.customConfPanel]
			};
	}

	Sbi.cockpit.editor.widget.WidgetEditorMainPanel.superclass.constructor.call(this, c);

	//We don't want to show Custom Conf. tab when it's the only tab for the Chart Engine Widget
	if(this.widgetType === Sbi.constants.cockpit.chart){
		this.child(this.customConfPanel).tab.hide();
	}

};

Ext.extend(Sbi.cockpit.editor.widget.WidgetEditorMainPanel, Ext.tab.Panel, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
	services: null

	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	, setDefaultActiveTab: function(){
		if (this.rendered)
			this.setActiveTab(0);
	}
    , setActiveTabPar: function(i){
//    	if (this.rendered)
			this.setActiveTab(i);
	}
	/*
	 * @method
	 *
	 * Initialize the GUI
	 */
	, init: function(){
		if(this.widgetType === Sbi.constants.cockpit.chart){
			this.customConfPanel = new Sbi.cockpit.editor.widget.WidgetEditorCustomConfPanel({wcId: this.wcId, widgetType: this.widgetType});
		} else{
			this.customConfPanel = new Sbi.cockpit.editor.widget.WidgetEditorCustomConfPanel({wcId: this.wcId, widgetType: this.widgetType});
			this.genericConfPanel = new Sbi.cockpit.editor.widget.WidgetEditorGenericConfPanel({wcId: this.wcId});
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
	, setDesigner: function (widgetConf) {
		this.customConfPanel.setDesigner(widgetConf);
	}

	, removeAllDesigners: function() {
		this.removeDesigner();
	}

	, removeDesigner: function() {
		this.customConfPanel.removeDesigner();
	}

	, updateValues: function(values){
		Sbi.trace("[WidgetEditorMainPanel.updateValues]: IN");
		if(this.widgetType === Sbi.constants.cockpit.chart && values && values.selectedDatasetLabel) {
			
			var widgetConf = {};
			widgetConf.wtype = this.widgetType;
			widgetConf.wdigetChartDataset = values.selectedDatasetLabel
			
			if(!Sbi.isValorized(this.customConfPanel.designer)){
				this.customConfPanel.addDesigner(widgetConf);
			} else{
				if(this.customConfPanel.designer.wdigetChartDataset !== values.selectedDatasetLabel){
					this.customConfPanel.setDesigner(widgetConf);
				}
			}
		}
		Sbi.trace("[WidgetEditorMainPanel.updateValues]: OUT");
	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

});