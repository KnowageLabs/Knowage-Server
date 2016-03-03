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
