/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.data.AssociationEditorWizardPanel', {
		extend: 'Sbi.widgets.WizardPanel'

	, config:{
		stores: null
	  , associations: null
	}

	/**
	 * @property {Sbi.cockpit.editor.association.AssociationshipEditorPage} associationEditorPage
	 * The page that manages association editing
	 */
	, associationEditorPage: null
	, frame: false
	, border: false

	, constructor : function(config) {
		Sbi.trace("[AssociationEditorWizardPanel.constructor]: IN");
		this.initConfig(config);
		this.callParent(arguments);
		Sbi.trace("[AssociationEditorWizardPanel.constructor]: OUT");
	}


	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, getAssociationEditorPage: function() {
		return this.associationEditorPage;
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, initPages: function(){
		Sbi.trace("[AssociationEditorWizardPanel.initPages]: IN");

		this.pages = new Array();

		this.initAssociationEditorPage();
		this.pages.push(this.associationEditorPage);

		Sbi.trace("[AssociationEditorWizardPanel.initPages]: association editor page succesfully adedd");

		Sbi.trace("[AssociationEditorWizardPanel.initPages]: OUT");

		return this.pages;
	}

	, initButtons: function(){
		Sbi.trace("[AssociationEditorWizardPanel.initButtons]: IN");

		this.buttons = new Array();

		this.buttons.push('->');

		this.buttons.push({
			hidden: false
	        , text:  LN('sbi.ds.wizard.confirm')
	        , handler: this.onSubmit
	        , scope: this
	    });

		this.buttons.push({
			text:  LN('sbi.ds.wizard.cancel')
	        , handler: this.onCancel
	        , scope: this
	    });

		Sbi.trace("[AssociationEditorWizardPanel.initButtons]: association editor buttons succesfully adedd");

		Sbi.trace("[AssociationEditorWizardPanel.initButtons]: OUT");

		return this.buttons;
	}

	, initAssociationEditorPage: function() {
		Sbi.trace("[AssociationEditorWizardPanel.initAssociationEditorPage]: IN");

		this.associationEditorPage = Ext.create('Sbi.data.editor.association.AssociationEditorPage',{
			stores: this.stores
		  , associations: this.associations
		});
		Sbi.trace("[AssociationEditorWizardPanel.initAssociationEditorPage]: IN");
		return this.associationEditorPage;
	}



	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

});
