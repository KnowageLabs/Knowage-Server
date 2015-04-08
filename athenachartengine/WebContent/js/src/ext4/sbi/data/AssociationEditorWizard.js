/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.data.AssociationEditorWizard', {
	extend: 'Ext.Window'
	, layout:'fit'

	, config:{
		title: LN('sbi.cockpit.association.editor.wizard.title')
		, width: 1000
		, height: 510
		, closable: true
		, closeAction: 'close' //'hide'
		, modal: true
	}


	/**
	 * @property {Sbi.data.AssociationEditorWizardPanel} editorMainPanel
	 *  Container of the wizard panel
	 */
	, editorMainPanel: null

	, constructor : function(config) {
		Sbi.trace("[AssociationEditorWizard.constructor]: IN");
		this.initConfig(config);
		this.init(config);
		this.initEvents();
		this.callParent(arguments);
		Sbi.trace("[AssociationEditorWizard.constructor]: OUT");
	}

	, initComponent: function() {

        Ext.apply(this, {
            items: [this.editorMainPanel]
        });

        this.callParent();
    }

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, getAssociationEditorPage: function() {
		return this.editorMainPanel.getAssociationEditorPage();
	}


	, getWizardState: function() {
		return this.editorMainPanel.getWizardState();
	}

	, setWizardState: function(editorState) {
		Sbi.trace("[AssociationEditorWizard.setWizardState]: IN");
		Sbi.trace("[AssociationEditorWizard.setWizardState]: wizard new configuration is equal to [" + Sbi.toSource(editorState) + "]");
		this.editorMainPanel.setWizardState(editorState);
		Sbi.trace("[AssociationEditorWizard.setWizardState]: OUT");
	}

	, resetWizardState: function() {
		Sbi.trace("[AssociationEditorWizard.resetWizardState]: IN");
		this.editorMainPanel.resetWizardState();
		Sbi.trace("[AssociationEditorWizard.resetWizardState]: OUT");
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, init: function(c){
		Sbi.trace("[AssociationEditorWizard.init]: IN");
		this.editorMainPanel = Ext.create('Sbi.data.AssociationEditorWizardPanel',{
			stores: Ext.apply(c.stores, [])
		  , associations:  Ext.apply(c.associations, [])
//			stores:  undefined
//		  , associations: undefined
		});
		this.editorMainPanel.on('cancel', this.onCancel, this);
		this.editorMainPanel.on('submit', this.onSubmit, this);

		Sbi.trace("[AssociationEditorWizard.init]: OUT");
	}

	, initEvents: function() {
		this.addEvents(
			/**
			* @event indicatorsChanged
			* Fires when data inserted in the wizard is canceled by the user
			* @param {AssociationEditorWizard} this
			*/
			'cancel'
			/**
			* @event apply
			* Fires when data inserted in the wizard is applied by the user
			* @param {AssociationEditorWizard} this
			*/
			, 'apply'
			/**
			* @event submit
			* Fires when data inserted in the wizard is submitted by the user
			* @param {AssociationEditorWizard} this
			*/
			, 'submit'
		);
	}


	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

	, onCancel: function(){
		this.fireEvent("cancel", this);
	}

	, onApply: function(){
		this.fireEvent("apply", this);
	}

	, onSubmit: function(editorPanel){
		this.fireEvent("submit", this);
	}

});
