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
