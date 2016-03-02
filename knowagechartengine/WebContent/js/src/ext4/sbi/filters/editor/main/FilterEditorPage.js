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

Ext.define('Sbi.filters.editor.main.FilterEditorPage', {
	  extend: 'Ext.Panel'
	, layout: 'fit'

	, config:{
		    storesList: null
		  , filters: null
		  , itemId: 0
		  , border: false
	}

	/**
	 * @property {Sbi.filters.editor.main.FilterEditor} filterEditorPanel
	 *  Container of the editor component
	 */
	 , filterEditorPanel: null

	 , constructor : function(config) {
		Sbi.trace("[FilterEditorPage.constructor]: IN");
		this.initConfig(config);
		this.init();
		this.callParent(arguments);
		Sbi.trace("[FilterEditorPage.constructor]: OUT");
	 }

	 , initComponent: function() {
	     Ext.apply(this, {
	         items: [this.filterEditorPanel]
	     });
	     this.callParent();
	 }


	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

//	, updateValues: function(values) {
//		Sbi.trace("[FilterEditorPage.updateValues]: IN");
//
//		Sbi.trace("[FilterEditorPage.updateValues]: Input parameter values is equal to [" + Sbi.toSource(values) + "]");
//		this.filterEditorPanel.controlPanel.updateValues(values);
//		Sbi.trace("[FilterEditorPage.updateValues]: OUT");
//	}

//	, getValidationErrorMessages: function() {
//		Sbi.trace("[AssociationEditorPage.getValidationErrorMessage]: IN");
//		var msg = null;
//
//		// TODO check if the designer is properly defined
//
//		Sbi.trace("[AssociationEditorPage.getValidationErrorMessage]: OUT");
//
//		return msg;
//	}

//	, isValid: function() {
//		Sbi.trace("[AssociationEditorPage.isValid]: IN");
//
//		var isValid = this.getValidationErrorMessages() === null;
//
//		Sbi.trace("[AssociationEditorPage.isValid]: OUT");
//
//		return isValid;
//	}

	, applyPageState: function(state) {
		Sbi.trace("[FilterEditorPage.applyPageState]: IN");
		state =  state || {};
		if(this.filterEditorPanel) {
			state.filters = this.filterEditorPanel.getFiltersList();
		}
		Sbi.trace("[FilterEditorPage.applyPageState]: OUT");
		return state;
	}

	, setPageState: function(state) {
		Sbi.trace("[FilterEditorPage.setPageState]: IN");
		Sbi.trace("[FilterEditorPage.setPageState]: state parameter is equal to [" + Sbi.toSource(state, true) + "]");

		this.filterEditorPanel.setFiltersList(state);

		Sbi.trace("[FilterEditorPage.setPageState]: OUT");
	}

	, resetPageState: function() {
		Sbi.trace("[FilterEditorPage.resetPageState]: IN");
		this.filterEditorPanel.removeAllAssociations();
		Sbi.trace("[FilterEditorPage.resetPageState]: OUT");
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, init: function(){
		this.filterEditorPanel = Ext.create('Sbi.filters.editor.main.FilterEditor',{storesList: this.storesList
																				  , filters: this.filters});
		return this.filterEditorPanel;
	}


	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

});
