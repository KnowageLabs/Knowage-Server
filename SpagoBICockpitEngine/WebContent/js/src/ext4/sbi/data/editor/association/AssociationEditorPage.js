/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.data.editor.association.AssociationEditorPage', {
	  extend: 'Ext.Panel'
	, layout: 'fit'

	, config:{
			stores: null
		  , associations: null
		  , itemId: 0
		  , border: false
	}

	/**
	 * @property {Sbi.data.editor.association.AssociationEditor} associationEditorPanel
	 *  Container of the editor component
	 */
	 , associationEditorPanel: null

	 , constructor : function(config) {
		Sbi.trace("[AssociationEditorPage.constructor]: IN");
		this.initConfig(config);
		this.init();
		this.callParent(arguments);
		Sbi.trace("[AssociationEditorPage.constructor]: OUT");
	 }

	 , initComponent: function() {
	     Ext.apply(this, {
	         items: [this.associationEditorPanel]
	     });
	     this.callParent();
	 }


	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, updateValues: function(values) {
		Sbi.trace("[AssociationEditorPage.updateValues]: IN");

		Sbi.trace("[AssociationEditorPage.updateValues]: Input parameter values is equal to [" + Sbi.toSource(values) + "]");
		this.associationEditorPanel.controlPanel.updateValues(values);
		Sbi.trace("[AssociationEditorPage.updateValues]: OUT");
	}

	, getValidationErrorMessages: function() {
		Sbi.trace("[AssociationEditorPage.getValidationErrorMessage]: IN");
		var msg = null;

		// TODO check if the designer is properly defined

		Sbi.trace("[AssociationEditorPage.getValidationErrorMessage]: OUT");

		return msg;
	}

	, isValid: function() {
		Sbi.trace("[AssociationEditorPage.isValid]: IN");

		var isValid = this.getValidationErrorMessages() === null;

		Sbi.trace("[AssociationEditorPage.isValid]: OUT");

		return isValid;
	}

	, applyPageState: function(state) {
		Sbi.trace("[AssociationEditorPage.applyPageState]: IN");
		state =  state || {};
		if(this.associationEditorPanel) {
			var associations = this.associationEditorPanel.getAssociationsList();
			state.associations = this.encodeAssociations(associations);
		}
		Sbi.trace("[AssociationEditorPage.applyPageState]: OUT");
		return state;
	}

	, setPageState: function(state) {
		Sbi.trace("[AssociationEditorPage.setPageState]: IN");
		Sbi.trace("[AssociationEditorPage.setPageState]: state parameter is equal to [" + Sbi.toSource(state, true) + "]");

		state.associations = this.decodeAssociations(state.associations);
		this.associationEditorPanel.setAssociationsList(state);

		Sbi.trace("[AssociationEditorPage.setPageState]: OUT");
	}

	, resetPageState: function() {
		Sbi.trace("[AssociationEditorPage.resetPageState]: IN");
		this.associationEditorPanel.removeAllAssociations();
		Sbi.trace("[AssociationEditorPage.resetPageState]: OUT");
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, init: function(){
		this.associationEditorPanel = Ext.create('Sbi.data.editor.association.AssociationEditor',{
			stores: this.stores
			, associations: this.associations
		});
		return this.associationEditorPanel;
	}


	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

	, decodeAssociations: function(associations){
		Sbi.trace("[AssociationEditorPage.decodeAssociations]: IN");

		Sbi.trace("[AssociationEditorPage.decodeAssociations]: input associations are equal to " +
				"[" + Sbi.toSource(associations) + "]");

		var decodedAssociations = [];

		for (var i = 0 ; i < associations.length; i++){
			var association = associations[i];

			var config = {};
			config.id = association.id;
			config.description = association.description;
			config.fields = config.fields;

			decodedAssociations.push(config);
		}

		Sbi.trace("[AssociationEditorPage.decodeAssociations]: decoded associations are equal to " +
				"[" + Sbi.toSource(decodedAssociations) + "]");

		Sbi.trace("[AssociationEditorPage.decodeAssociations]: OUT");

		return decodedAssociations;
	}

	/**
	 * @method
	 *
	 * Convert the association list as returned from #associationEditorPanel to a list of association encoded
	 * in the format expected by the storeManager
	 */
	, encodeAssociations: function(associations){
		Sbi.trace("[AssociationEditorPage.encodeAssociations]: IN");

		Sbi.trace("[AssociationEditorPage.encodeAssociations]: input associations are equal to " +
				"[" + Sbi.toSource(associations) + "]");

		var encodedAssociations = [];

		for (var i = 0 ; i < associations.length; i++){
			var association = associations[i];

			var config = {};
			config.id = association.id;
			config.description = association.description;
			config.fields = this.getAssociationFields(association);

			encodedAssociations.push(config);
		}

		Sbi.trace("[AssociationEditorPage.encodeAssociations]: encoded associations are equal to " +
				"[" + Sbi.toSource(encodedAssociations) + "]");

		Sbi.trace("[AssociationEditorPage.encodeAssociations]: OUT");
		return encodedAssociations;
	}

	/**
	 * @deprecated
	 */
	, getAssociationFields: function(association){

		Sbi.trace("[AssociationEditorPage.getAssociationFields]: IN");
		Sbi.trace("[AssociationEditorPage.getAssociationFields]: Associations object: " +  Sbi.toSource(association));

		var fields = [];
		var lst = association.description.split('=');

		for (var i=0; i<lst.length; i++){
			for (var i=0; i<lst.length; i++){
				var el = lst[i].split('.');
				var field = {};
				field.store = el[0];
				field.column = el[1];
				var lbl = '#'+i;
				fields.push(field);
			}
		}
		Sbi.trace("[AssociationEditorPage.getAssociationFields]: OUT");
		return fields;
	}

});
