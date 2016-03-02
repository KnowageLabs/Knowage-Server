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
