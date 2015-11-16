/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.fonts.editor.main.FontEditorPage', {
	  extend: 'Ext.Panel'
	, layout: 'fit'

	, config:{
		  fonts: null
		  , itemId: 0
		  , border: false
	}

	/**
	 * @property {Sbi.fonts.editor.main.FontEditor} fontEditorPanel
	 *  Container of the editor component
	 */
	 , fontEditorPanel: null
	
	 , constructor : function(config) {
		Sbi.trace("[FontEditorPage.constructor]: IN");
		this.initConfig(config);
		this.init();
		this.callParent(arguments);
		Sbi.trace("[FontEditorPage.constructor]: OUT");
	 }
	
	 , initComponent: function() {
	     Ext.apply(this, {
	         items: [this.fontEditorPanel]
	     });
	     this.callParent();
	 }


	// =================================================================================================================
	// METHODS
	// =================================================================================================================
	
	// -----------------------------------------------------------------------------------------------------------------
	// public methods
	// -----------------------------------------------------------------------------------------------------------------



	, applyPageState: function(state) {
		Sbi.trace("[FontEditorPage.applyPageState]: IN");
		state =  state || {};
		if(this.fontEditorPanel) {
			state.fonts = this.fontEditorPanel.getFontsList();
		}
		Sbi.trace("[FontEditorPage.applyPageState]: OUT");
		return state;
	}

	, setPageState: function(state) {
		Sbi.trace("[FontEditorPage.setPageState]: IN");
		Sbi.trace("[FontEditorPage.setPageState]: state parameter is equal to [" + Sbi.toSource(state, true) + "]");
	
		this.fontEditorPanel.setFontsList(state);
	
		Sbi.trace("[FontEditorPage.setPageState]: OUT");
	}

	, resetPageState: function() {
		Sbi.trace("[FontEditorPage.resetPageState]: IN");
		this.fontEditorPanel.removeAllFonts();
		Sbi.trace("[FontEditorPage.resetPageState]: OUT");
	}

	// -----------------------------------------------------------------------------------------------------------------
	// init methods
	// -----------------------------------------------------------------------------------------------------------------
	
	, init: function(){
		this.fontEditorPanel = Ext.create('Sbi.fonts.editor.main.FontEditor',{fonts: this.fonts});
		return this.fontEditorPanel;
	}
	
	
	// -----------------------------------------------------------------------------------------------------------------
	// utility methods
	// -----------------------------------------------------------------------------------------------------------------

});
