/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.filters.editor.main.FilterEditor', {
	extend: 'Ext.Panel'
    , layout: 'fit'

	, config:{
		  services: null
		, storesList: null
		, filters: null
		, contextMenu: null
		, border: false
//		, autoScroll: true
	}

	/**
	 * @property {Sbi.filters.editor.main.FilterEditorList} filterContainerPanel
	 * The container of datasets
	 */
	, filtersContainerPanel: null

	, constructor : function(config) {
		Sbi.trace("[FilterEditor.constructor]: IN");
		this.initConfig(config);
		this.initPanels(config);
		this.callParent(arguments);
//		this.addEvents('addAssociation','addAssociationToList');
		Sbi.trace("[FilterEditor.constructor]: OUT");
	}

	,  initComponent: function() {
	        Ext.apply(this, {
	            items:[{
						id: 'filtersContainerPanel',
//						region: 'center',
						layout: 'fit',
						autoScroll: true,
						split: true,
						items: [this.filtersContainerPanel]
						}]
	        });
	        this.callParent();
	    }

	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
	, initializeEngineInstance : function (config) {

	}

	, initPanels: function(config){
		this.initFiltersPanel(config);
	}


	, initFiltersPanel: function(config) {
		this.filtersContainerPanel = Ext.create('Sbi.filters.editor.main.FilterEditorList',{storesList: this.storesList
																						  , filters: this.filters});
	}

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method
	 * Returns the Filters list
	 */

	, getFiltersList: function(){
		return this.filtersContainerPanel.getFiltersList();
	}

	/**
	 * @method
	 * Set the filters list
	 */

	, setFiltersList: function(f){
		this.filtersContainerPanel.setFiltersList(f);
	}

	/**
	 * @method
	 * Reset the filters list
	 */

	, removeAllFilters: function(){
		this.filtersContainerPanel.removeAllFilters();
	}

	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------
});
