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

Ext.define('Sbi.fonts.FontEditorWizard', {
	extend: 'Ext.Window'
	, layout:'fit'
	, config:{title: LN('sbi.cockpit.font.editor.wizard.title')
			  , width: 1000
			  , height: 500
			  , closable: true
			  , modal: true
			  , fonts: null
	}

	/**
	 * @property {Sbi.font.FontEditorWizardPanel} editorMainPanel
	 *  Container of the wizard panel
	 */
	, editorMainPanel: null

	, constructor : function(config) {
		Sbi.trace("[FontEditorWizard.constructor]: IN");
		this.initConfig(config);
		this.init(config);
		this.initEvents();
		this.callParent(arguments);
		Sbi.trace("[FontEditorWizard.constructor]: OUT");
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

	, getFontEditorPage: function() {
		return this.editorMainPanel.getFontEditorPage();
	}


	, getWizardState: function() {
		return this.editorMainPanel.getWizardState();
	}

	, setWizardState: function(editorState) {
		Sbi.trace("[FontEditorWizard.setWizardState]: IN");
		Sbi.trace("[FontEditorWizard.setWizardState]: wizard new configuration is equal to [" + Sbi.toSource(editorState) + "]");
		this.editorMainPanel.setWizardState(editorState);
		Sbi.trace("[FontEditorWizard.setWizardState]: OUT");
	}

	, resetWizardState: function() {
		Sbi.trace("[FontEditorWizard.resetWizardState]: IN");
		this.editorMainPanel.resetWizardState();
		Sbi.trace("[FontEditorWizard.resetWizardState]: OUT");
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, init: function(c){
		Sbi.trace("[FontEditorWizard.init]: IN");
		this.editorMainPanel = Ext.create('Sbi.fonts.FontEditorWizardPanel',{
			fonts: c.fonts
			});
		this.editorMainPanel.on('cancel', this.onCancel, this);
		this.editorMainPanel.on('submit', this.onSubmit, this);

		Sbi.trace("[FontEditorWizard.init]: OUT");
	}

	, initEvents: function() {
		this.addEvents(
			/**
			* @event indicatorsChanged
			* Fires when data inserted in the wizard is canceled by the user
			* @param {FontEditorWizard} this
			*/
			'cancel'
			/**
			* @event apply
			* Fires when data inserted in the wizard is applied by the user
			* @param {FontEditorWizard} this
			*/
			, 'apply'
			/**
			* @event submit
			* Fires when data inserted in the wizard is submitted by the user
			* @param {FontEditorWizard} this
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
