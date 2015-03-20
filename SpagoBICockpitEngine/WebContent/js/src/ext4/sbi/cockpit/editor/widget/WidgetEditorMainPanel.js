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

	c = {
	    activeTab: 0,
	    items: [this.genericConfPanel,this.customConfPanel]
	};

	Sbi.cockpit.editor.widget.WidgetEditorMainPanel.superclass.constructor.call(this, c);

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
    	if (this.rendered)
			this.setActiveTab(i);
	}
	/*
	 * @method
	 *
	 * Initialize the GUI
	 */
	, init: function(){
		this.customConfPanel = new Sbi.cockpit.editor.widget.WidgetEditorCustomConfPanel();
		this.genericConfPanel = new Sbi.cockpit.editor.widget.WidgetEditorGenericConfPanel();
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

	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

});