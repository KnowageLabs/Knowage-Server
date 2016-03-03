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

Ext.define('Sbi.layouts.LayoutEditorWizard', {
	extend: 'Ext.Window'
	, layout:'fit'
	, config:{title: LN('sbi.cockpit.layouts.editor.wizard.title')
			  , width: 1000
			  , height: 500
			  , closable: true
			  , modal: true
			  , layouts: null
	}

	/**
	 * @property {Sbi.layout.LayoutEditorWizardPanel} editorMainPanel
	 *  Container of the wizard panel
	 */
	, editorMainPanel: null

	, constructor : function(config) {
		Sbi.trace("[LayoutEditorWizard.constructor]: IN");
		this.initConfig(config);
		this.init(config);
		this.initEvents();
		this.callParent(arguments);
		Sbi.trace("[LayoutEditorWizard.constructor]: OUT");
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

	, getLayoutEditorPage: function() {
		return this.editorMainPanel.getLayoutEditorPage();
	}


	, getWizardState: function() {
		return this.editorMainPanel.getWizardState();
	}

	, setWizardState: function(editorState) {
		Sbi.trace("[LayoutEditorWizard.setWizardState]: IN");
		Sbi.trace("[LayoutEditorWizard.setWizardState]: wizard new configuration is equal to [" + Sbi.toSource(editorState) + "]");
		this.editorMainPanel.setWizardState(editorState);
		Sbi.trace("[LayoutEditorWizard.setWizardState]: OUT");
	}

	, resetWizardState: function() {
		Sbi.trace("[LayoutEditorWizard.resetWizardState]: IN");
		this.editorMainPanel.resetWizardState();
		Sbi.trace("[LayoutEditorWizard.resetWizardState]: OUT");
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, init: function(c){
		Sbi.trace("[LayoutEditorWizard.init]: IN");
		this.editorMainPanel = Ext.create('Sbi.layouts.LayoutEditorWizardPanel',{
			layouts: c.layouts
			});
		this.editorMainPanel.on('cancel', this.onCancel, this);
		this.editorMainPanel.on('submit', this.onSubmit, this);

		Sbi.trace("[LayoutEditorWizard.init]: OUT");
	}

	, initEvents: function() {
		this.addEvents(
			/**
			* @event indicatorsChanged
			* Fires when data inserted in the wizard is canceled by the user
			* @param {LayoutEditorWizard} this
			*/
			'cancel'
			/**
			* @event apply
			* Fires when data inserted in the wizard is applied by the user
			* @param {LayoutEditorWizard} this
			*/
			, 'apply'
			/**
			* @event submit
			* Fires when data inserted in the wizard is submitted by the user
			* @param {LayoutEditorWizard} this
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
