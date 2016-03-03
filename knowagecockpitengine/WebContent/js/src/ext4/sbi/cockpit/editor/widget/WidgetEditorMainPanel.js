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

//	if(this.widgetType === Sbi.constants.cockpit.chart){
//		c = {
//			    activeTab: 0,
//			    items: [this.customConfPanel],
//			    //We don't want to show Custom Conf. tab when it's the only tab for the Chart Engine Widget
//				tabBar: {
//					hidden: true
//				}
//			};
//		
//	} else {
//		c = {
//			    activeTab: 0,
//			    items: [this.genericConfPanel,this.customConfPanel]
//			};
//	}
	
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
//    	if (this.rendered)
			this.setActiveTab(i);
	}
	/*
	 * @method
	 *
	 * Initialize the GUI
	 */
	, init: function(){
//		if(this.widgetType === Sbi.constants.cockpit.chart){
//			this.customConfPanel = new Sbi.cockpit.editor.widget.WidgetEditorCustomConfPanel({wcId: this.wcId, widgetType: this.widgetType});
//		} else{
//			this.customConfPanel = new Sbi.cockpit.editor.widget.WidgetEditorCustomConfPanel({wcId: this.wcId, widgetType: this.widgetType});
//			this.genericConfPanel = new Sbi.cockpit.editor.widget.WidgetEditorGenericConfPanel({wcId: this.wcId});
//		}
		this.customConfPanel = new Sbi.cockpit.editor.widget.WidgetEditorCustomConfPanel({wcId: this.wcId, widgetType: this.widgetType});
		this.genericConfPanel = new Sbi.cockpit.editor.widget.WidgetEditorGenericConfPanel({wcId: this.wcId, widgetType: this.widgetType});
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