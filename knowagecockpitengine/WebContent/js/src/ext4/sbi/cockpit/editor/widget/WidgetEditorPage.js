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

Sbi.cockpit.editor.widget.WidgetEditorPage = function(config) {

	Sbi.trace("[WidgetEditorPage.constructor]: IN");

	// init properties...
	var defaultSettings = {
		itemId: 1
		, layout: 'fit'
	};
	var settings = Sbi.getObjectSettings('Sbi.cockpit.editor.widget.WidgetEditorPage', defaultSettings);
	var c = Ext.apply(settings, config || {});

	Sbi.trace("[WidgetEditorPage.constructor]: config [" + Sbi.toSource(c)+ "]");

	Ext.apply(this, c);

	this.init();

	c.items = [this.widgetEditorPanel];
	//c.items = [{html: "this.widgetEditorPanel"}];

	Sbi.cockpit.editor.widget.WidgetEditorPage.superclass.constructor.call(this, c);

	Sbi.trace("[WidgetEditorPage.constructor]: OUT");
};

/**
 * @class Sbi.cockpit.editor.widget.WidgetEditorPage
 * @extends Ext.Panel
 *
 * bla bla bla bla bla ...
 */

/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.cockpit.editor.widget.WidgetEditorPage, Ext.Panel, {

	widgetEditorPanel: null

	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, updateValues: function(values) {
		Sbi.trace("[WidgetEditorPage.updateValues]: IN");

		Sbi.trace("[WidgetEditorPage.updateValues]: Input parameter values is equal to [" + Sbi.toSource(values) + "]");
		if(this.widgetType === Sbi.constants.cockpit.chart){
			this.widgetEditorPanel.mainPanel.updateValues(values);
		}else{
			this.widgetEditorPanel.controlPanel.updateValues(values);
		}
		
		Sbi.trace("[WidgetEditorPage.updateValues]: OUT");
	}

	, getValidationErrorMessages: function() {
		Sbi.trace("[DatasetBrowserPage.getValidationErrorMessage]: IN");
		var msg = null;

		// TODO check if the designer is properly defined

		Sbi.trace("[DatasetBrowserPage.getValidationErrorMessage]: OUT");

		return msg;
	}

	, isValid: function() {
		Sbi.trace("[WidgetEditorPage.isValid]: IN");

		var isValid = this.getValidationErrorMessages() === null;

		Sbi.trace("[WidgetEditorPage.isValid]: OUT");

		return isValid;
	}

	, applyPageState: function(state, running) {
		Sbi.trace("[WidgetEditorPage.applyPageState]: IN");
		state =  state || {};
		if(this.widgetEditorPanel.mainPanel.customConfPanel.designer) {
			state.wtype = this.widgetEditorPanel.mainPanel.customConfPanel.designer.getDesignerType();
			state.wconf = this.widgetEditorPanel.mainPanel.customConfPanel.designer.getDesignerState(running);
			
		}
		if(Sbi.isValorized(this.widgetEditorPanel.mainPanel.genericConfPanel)){
			state.wgeneric = this.widgetEditorPanel.mainPanel.genericConfPanel.getFormState();
		}
		
		Sbi.trace("[WidgetEditorPage.applyPageState]: OUT");
		return state;
	}

	, setPageState: function(state) {
		Sbi.trace("[WidgetEditorPage.setPageState]: IN");
		Sbi.trace("[WidgetEditorPage.setPageState]: state parameter is equal to [" + Sbi.toSource(state, true) + "]");
		if(Sbi.isValorized(this.widgetEditorPanel.mainPanel.customConfPanel)){			
			this.widgetEditorPanel.mainPanel.customConfPanel.setDesigner(state.wconf);
		}
		if(Sbi.isValorized(this.widgetEditorPanel.mainPanel.genericConfPanel)){
			this.widgetEditorPanel.mainPanel.genericConfPanel.setFormState(state.wgeneric);
		}		

		Sbi.trace("[WidgetEditorPage.setPageState]: OUT");
	}

	, resetPageState: function() {
		Sbi.trace("[WidgetEditorPage.resetPageState]: IN");
		
		if(Sbi.isValorized(this.widgetEditorPanel.mainPanel.customConfPanel)){
			this.widgetEditorPanel.mainPanel.customConfPanel.removeAllDesigners();
		}
		if(Sbi.isValorized(this.widgetEditorPanel.mainPanel.genericConfPanel)){
			this.widgetEditorPanel.mainPanel.genericConfPanel.resetFormState();
		}
		
		this.widgetEditorPanel.mainPanel.setDefaultActiveTab();
		
		if(Sbi.isValorized(this.widgetEditorPanel.controlPanel)){
			this.widgetEditorPanel.controlPanel.designerPalettePanel.expand();
		}		
		
		Sbi.trace("[WidgetEditorPage.resetPageState]: OUT");
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, init: function(){
		this.widgetEditorPanel = new Sbi.cockpit.editor.widget.WidgetEditor({wcId: this.wcId, widgetType: this.widgetType});
		return this.widgetEditorPanel;
	}


	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

});
