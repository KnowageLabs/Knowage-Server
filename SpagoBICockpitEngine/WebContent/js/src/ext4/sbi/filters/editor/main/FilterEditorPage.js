/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

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
