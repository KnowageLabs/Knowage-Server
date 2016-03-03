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

Ext.define('Sbi.layouts.editor.main.LayoutEditor', {
	extend: 'Ext.Panel'
    , layout: 'fit'

	, config:{
		  services: null
		, layouts: null
		, contextMenu: null
		, border: false
//		, autoScroll: true
	}

	/**
	 * @property {Sbi.layouts.editor.main.LayoutEditor} layoutContainerPanel
	 * The container of layout options
	 */
	, layoutsContainerPanel: null

	, constructor : function(config) {
		Sbi.trace("[LayoutEditor.constructor]: IN");
		this.initConfig(config);
		this.initPanels(config);
		this.callParent(arguments);
		Sbi.trace("[LayoutEditor.constructor]: OUT");
	}

	,  initComponent: function() {
	        Ext.apply(this, {
	            items:[{
						id: 'layoutContainerPanel',
//						region: 'center',
						layout: 'fit',
						autoScroll: true,
						split: true,
						items: [this.layoutContainerPanel]
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
		this.initLayoutPanel(config);
	}


	, initLayoutPanel: function(config) {
		this.layoutContainerPanel = Ext.create('Sbi.layouts.editor.main.LayoutEditorTabsPanel',{layouts: this.layouts});
	}

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method
	 * Returns the layouts list
	 */

	, getLayoutsList: function(){
		return this.layoutContainerPanel.getLayoutsList();
	}

	/**
	 * @method
	 * Set the layouts list
	 */

	, setLayoutsList: function(f){
		this.layoutsContainerPanel.setLayoutsList(f);
	}

	/**
	 * @method
	 * Reset the layouts list
	 */

	, removeAllLayouts: function(){
		this.layoutsContainerPanel.removeAllLayouts();
	}

	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------
});
