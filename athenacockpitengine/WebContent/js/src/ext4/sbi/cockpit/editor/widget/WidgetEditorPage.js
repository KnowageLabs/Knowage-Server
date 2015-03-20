/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

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
		this.widgetEditorPanel.controlPanel.updateValues(values);
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
			state.wgeneric = this.widgetEditorPanel.mainPanel.genericConfPanel.getFormState();
		}
		Sbi.trace("[WidgetEditorPage.applyPageState]: OUT");
		return state;
	}

	, setPageState: function(state) {
		Sbi.trace("[WidgetEditorPage.setPageState]: IN");
		Sbi.trace("[WidgetEditorPage.setPageState]: state parameter is equal to [" + Sbi.toSource(state, true) + "]");
		this.widgetEditorPanel.mainPanel.customConfPanel.setDesigner(state.wconf);
		this.widgetEditorPanel.mainPanel.genericConfPanel.setFormState(state.wgeneric);

		Sbi.trace("[WidgetEditorPage.setPageState]: OUT");
	}

	, resetPageState: function() {
		Sbi.trace("[WidgetEditorPage.resetPageState]: IN");
		this.widgetEditorPanel.mainPanel.customConfPanel.removeAllDesigners();
		this.widgetEditorPanel.mainPanel.genericConfPanel.resetFormState();
		this.widgetEditorPanel.mainPanel.setDefaultActiveTab();
		this.widgetEditorPanel.controlPanel.designerPalettePanel.expand();
		Sbi.trace("[WidgetEditorPage.resetPageState]: OUT");
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, init: function(){
		this.widgetEditorPanel = new Sbi.cockpit.editor.widget.WidgetEditor();
		return this.widgetEditorPanel;
	}


	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

});
