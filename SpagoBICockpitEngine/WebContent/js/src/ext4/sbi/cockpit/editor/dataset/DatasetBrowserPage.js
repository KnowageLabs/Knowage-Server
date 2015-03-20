/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.editor.dataset");

/**
 * @class Sbi.cockpit.editor.dataset.DatasetBrowserPage
 * @extends Ext.Panel
 *
 * bla bla bla bla bla ...
 */

/**
 * @cfg {Object} config The configuration object passed to the cnstructor
 */
Sbi.cockpit.editor.dataset.DatasetBrowserPage = function(config) {

	Sbi.trace("[DatasetBrowserPage.constructor]: IN");

	// init properties...
	var defaultSettings = {
		itemId: 0
		, layout: "fit"
		, frame: false
		, border: false
	};
	var settings = Sbi.getObjectSettings('Sbi.cockpit.editor.dataset.DatasetBrowserPage', defaultSettings);
	var c = Ext.apply(settings, config || {});

	Sbi.trace("[DatasetBrowserPage.constructor]: config [" + Sbi.toSource(c) + "]");

	Ext.apply(this, c);

	this.init();

	c.items = [this.datasetsBrowserPanel];

	Sbi.cockpit.editor.dataset.DatasetBrowserPage.superclass.constructor.call(this, c);

	Sbi.trace("[DatasetBrowserPage.constructor]: OUT");
};

Ext.extend(Sbi.cockpit.editor.dataset.DatasetBrowserPage, Ext.Panel, {

	datasetsBrowserPanel: null

	/**
	 * used just for initialization.
	 */
	, usedDatasets: null

	, originallySelectedDataset: null

	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method
	 * Sets the list of used dataset's label.
	 *
	 * @param {String[]} datasets The dataset's label list.
	 */
	, setUsedDatasets: function(datasets) {
		this.datasetsBrowserPanel.setUsedDatasets(datasets);
	}

	, getValidationErrorMessages: function() {
		Sbi.trace("[DatasetBrowserPage.getValidationErrorMessage]: IN");
		var msg = null;

		var selectedDatasets = this.datasetsBrowserPanel.getSelection();
		/* by the introduction of Selection Widget, dataset selection is not mandatory */
//		if(selectedDatasets.length === 0) {
//			msg = "Per procedere e' necessario selezionare un dataset";
//		}

		Sbi.trace("[DatasetBrowserPage.getValidationErrorMessage]: OUT");

		return msg;
	}

	, isValid: function() {
		Sbi.trace("[DatasetBrowserPage.isValid]: IN");

		var isValid = this.getValidationErrorMessages() === null;

		Sbi.trace("[DatasetBrowserPage.isValid]: OUT");

		return isValid;
	}

	, applyPageState: function(state) {
		Sbi.trace("[WidgetEditor.applyPageState]: IN");
		state =  state || {};
		state.selectedDatasetLabel = this.datasetsBrowserPanel.getSelection()[0];
		if(this.originallySelectedDataset !== null && this.originallySelectedDataset !== state.selectedDatasetLabel) {
			state.unselectedDatasetLabel = this.originallySelectedDataset;
		} else {
			state.unselectedDatasetLabel = null;
		}

		Sbi.trace("[WidgetEditor.applyPageState]: OUT");
		return state;
	}

	, setPageState: function(state) {
		Sbi.trace("[WidgetEditor.setPageState]: IN");
		Sbi.trace("[WidgetEditor.setPageState]: state parameter is equal to [" + Sbi.toSource(state) + "]");

		this.datasetsBrowserPanel.resetSelection();
		Sbi.trace("[WidgetEditor.setPageState]: dataset selection cleared");

		state = state || {};
		if(Sbi.isValorized(state.dataset)) {
			this.originallySelectedDataset = state.dataset;
			Sbi.trace("[WidgetEditor.setPageState]: originally selected dataset [" + this.originallySelectedDataset + "]");
			this.datasetsBrowserPanel.select(state.dataset, true); // true to refresh also the underlying GUI control
			Sbi.trace("[WidgetEditor.setPageState]: selected dataset [" + state.dataset + "]");
		} else {
			Sbi.trace("[WidgetEditor.setPageState]: no dataset to select");
		}
		Sbi.trace("[WidgetEditor.setPageState]: OUT");
	}

	, resetPageState: function() {
		Sbi.trace("[WidgetEditor.resetPageState]: IN");
		this.originallySelectedDataset = null;
		Sbi.trace("[WidgetEditor.setPageState]: originally selected dataset [null]");
		this.datasetsBrowserPanel.resetSelection();
		this.datasetsBrowserPanel.resetToolbarOptions();

		Sbi.trace("[WidgetEditor.resetPageState]: OUT");
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, init: function(){

		this.datasetsBrowserPanel = new Sbi.widgets.DatasetsBrowserPanel({
			usedDatasets: this.usedDatasets
		});
		delete this.usedDatasets;

		return this.datasetsBrowserPanel;
	}


	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------
//	, onSelect: function(c){
//		//removes old selection from the storeManager (if exists)
//		if (c.label != c.oldLabel && this.widgetManager.getStoreByLabel(c.oldLabel) != null) {
//			this.widgetManager.removeStore(c.oldLabel);
//		}
//		//adds the dataset to the storeManager (throught the WidgetManager)
//		//this.widget.dataset = c.label;
//		var storeConfig = {};
//		storeConfig.dsLabel = c.label; //storeConfig.dsLabel = this.widget.dataset;
//	    this.widgetManager.addStore(storeConfig);
//	}
});
