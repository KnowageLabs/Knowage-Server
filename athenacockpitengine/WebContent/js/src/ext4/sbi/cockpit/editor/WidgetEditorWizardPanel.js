/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.editor");

/**
 * @class Sbi.xxx.Xxxx
 * @extends Ext.util.Observable
 *
 * bla bla bla bla bla ...
 */

/**
 * @cfg {Object} config The configuration object passed to the cnstructor
 */

Sbi.cockpit.editor.WidgetEditorWizardPanel = function(config) {

	Sbi.trace("[WidgetEditorWizardPanel.constructor]: IN");

	// init properties...
	var defaultSettings = {
		frame: false,
		border: false
	};
	var settings = Sbi.getObjectSettings('Sbi.cockpit.editor.WidgetEditorWizardPanel', defaultSettings);
	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	//c.activeItem = (config && config.widget && config.widget.dataset)?1:0 //sets to 1 if the dataset was already selected
	Sbi.trace("[WidgetEditorWizardPanel.constructor]: initial active page is [" + c.activeItem + "]");

	Sbi.cockpit.editor.WidgetEditorWizardPanel.superclass.constructor.call(this, c);

	Sbi.trace("[WidgetEditorWizardPanel.constructor]: OUT");
};

Ext.extend(Sbi.cockpit.editor.WidgetEditorWizardPanel, Sbi.widgets.WizardPanel, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	/**
     * @property {String[]} usedDatasets
     * The list of the labels of used datasets. It is used only for the initialization of #datasetsBrowserPage
     * After the inizialization it is removed. In order to get this list after initialization
     * use #getDatasetBrowserPage method as shown in the following example:
     * <pre><code>
wizardPanel.getDatasetBrowserPage().getUsedDatasets();
</code></pre>
     */
	usedDatasets: null

	/**
     * @property {Sbi.cockpit.editor.dataset.DatasetBrowserPage} datasetsBrowserPage
     * The page that manages dataset selection (by default it is the first, index 0)
     */
	, datasetsBrowserPage: null

	/**
     * @property {Sbi.cockpit.editor.widget.WidgetEditorPage} widgetEditorPage
     * The page that manages widget editing (by default it is the second, index 1)
     */
	, widgetEditorPage: null

	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, getDatasetBrowserPage: function() {
		//return this.getPage(0);
		return this.datasetsBrowserPage;
	}

	, getWidgetEditorPage: function() {
		//return this.getPage(1);
		return this.widgetEditorPage;
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, initPages: function(){
		Sbi.trace("[WidgetEditorWizardPanel.initPages]: IN");

		this.pages = new Array();

		this.initDatasetBrowserPage();
		this.pages.push(this.datasetsBrowserPage);
		Sbi.trace("[WidgetEditorWizardPanel.initPages]: dataset browser page succesfully adedd");

		this.initWidgetEditorPage();
		this.pages.push(this.widgetEditorPage);
		Sbi.trace("[WidgetEditorWizardPanel.initPages]: widget editor page succesfully adedd");

		Sbi.trace("[WidgetEditorWizardPanel.initPages]: OUT");

		return this.pages;
	}

	, initDatasetBrowserPage: function() {
		Sbi.trace("[WidgetEditorWizardPanel.initDatasetBrowserPage]: IN");
		this.datasetsBrowserPage = new Sbi.cockpit.editor.dataset.DatasetBrowserPage({
			usedDatasets: this.usedDatasets
		});
		Sbi.trace("[WidgetEditorWizardPanel.initDatasetBrowserPage]: OUT");
		return this.datasetsBrowserPage;
	}

	, initWidgetEditorPage: function() {
		Sbi.trace("[WidgetEditorWizardPanel.initWidgetEditorPage]: IN");
		this.widgetEditorPage = new Sbi.cockpit.editor.widget.WidgetEditorPage({
			// nothing to declare here
		});
		//this.widgetEditorPage = new Ext.Panel({html: "this.widgetEditorPage"});
		Sbi.trace("[WidgetEditorWizardPanel.initWidgetEditorPage]: IN");
		return this.widgetEditorPage;
	}



	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

});
