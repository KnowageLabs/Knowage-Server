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
