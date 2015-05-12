/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.fonts.editor.main.FontEditor', {
	extend: 'Ext.Panel'
    , layout: 'fit'

	, config:{
		  services: null
		, fonts: null
		, contextMenu: null
		, border: false
//		, autoScroll: true
	}

	/**
	 * @property {Sbi.fonts.editor.main.FontEditor} fontContainerPanel
	 * The container of font options
	 */
	, fontContainerPanel: null

	, constructor : function(config) {
		Sbi.trace("[FontEditor.constructor]: IN");
		this.initConfig(config);
		this.initPanels(config);
		this.callParent(arguments);
//		this.addEvents('addAssociation','addAssociationToList');
		Sbi.trace("[FontEditor.constructor]: OUT");
	}

	,  initComponent: function() {
	        Ext.apply(this, {
	            items:[{
						id: 'fontContainerPanel',
//						region: 'center',
						layout: 'fit',
						autoScroll: true,
						split: true,
						items: [this.fontContainerPanel]
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
		this.initFontPanel(config);
	}


	, initFontPanel: function(config) {
		this.fontContainerPanel = Ext.create('Sbi.fonts.editor.main.FontEditorTabsPanel',{fonts: this.fonts});
	}

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method
	 * Returns the fonts list
	 */

	, getFontsList: function(){
		return this.fontContainerPanel.getFontsList();
	}

	/**
	 * @method
	 * Set the fonts list
	 */

	, setFontsList: function(f){
		this.fontsContainerPanel.setFontsList(f);
	}

	/**
	 * @method
	 * Reset the fonts list
	 */

	, removeAllFonts: function(){
		this.fontsContainerPanel.removeAllFonts();
	}

	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------
});
