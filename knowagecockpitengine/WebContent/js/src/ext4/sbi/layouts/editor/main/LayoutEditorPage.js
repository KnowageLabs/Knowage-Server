/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.layouts.editor.main.LayoutEditorPage', {
	  extend: 'Ext.Panel'
	, layout: 'fit'

	, config:{
		  layouts: null
		  , itemId: 0
		  , border: false
	}

	/**
	 * @property {Sbi.layouts.editor.main.LayoutEditor} layoutEditorPanel
	 *  Container of the editor component
	 */
	 , layoutEditorPanel: null
	
	 , constructor : function(config) {
		Sbi.trace("[LayoutEditorPage.constructor]: IN");
		this.initConfig(config);
		this.init();
		this.callParent(arguments);
		Sbi.trace("[LayoutEditorPage.constructor]: OUT");
	 }
	
	 , initComponent: function() {
	     Ext.apply(this, {
	         items: [this.layoutEditorPanel]
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
		Sbi.trace("[LayoutEditorPage.applyPageState]: IN");
		state =  state || {};
		if(this.layoutEditorPanel) {
			state.layouts = this.layoutEditorPanel.getLayoutsList();
		}
		Sbi.trace("[LayoutEditorPage.applyPageState]: OUT");
		return state;
	}

	, setPageState: function(state) {
		Sbi.trace("[LayoutEditorPage.setPageState]: IN");
		Sbi.trace("[LayoutEditorPage.setPageState]: state parameter is equal to [" + Sbi.toSource(state, true) + "]");
	
		this.layoutEditorPanel.setLayoutsList(state);
	
		Sbi.trace("[LayoutEditorPage.setPageState]: OUT");
	}

	, resetPageState: function() {
		Sbi.trace("[LayoutEditorPage.resetPageState]: IN");
		this.layoutEditorPanel.removeAllLayouts();
		Sbi.trace("[LayoutEditorPage.resetPageState]: OUT");
	}

	// -----------------------------------------------------------------------------------------------------------------
	// init methods
	// -----------------------------------------------------------------------------------------------------------------
	
	, init: function(){
		this.layoutEditorPanel = Ext.create('Sbi.layouts.editor.main.LayoutEditor',{layouts: this.layouts});
		return this.layoutsEditorPanel;
	}
	
	
	// -----------------------------------------------------------------------------------------------------------------
	// utility methods
	// -----------------------------------------------------------------------------------------------------------------

});
