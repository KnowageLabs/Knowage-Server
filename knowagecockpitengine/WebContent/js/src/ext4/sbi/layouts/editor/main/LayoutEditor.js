/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

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
