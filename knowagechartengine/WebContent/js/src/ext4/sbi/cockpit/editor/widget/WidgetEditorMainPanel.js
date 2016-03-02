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