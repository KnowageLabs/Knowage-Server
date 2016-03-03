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

Ext.define('Sbi.filters.FilterEditorWizard', {
	extend: 'Ext.Window'
	, layout:'fit'
	, config:{title: LN('sbi.cockpit.filter.editor.wizard.title')
			  , width: 1000
			  , height: 500
			  , closable: false
			  , closeAction: 'close'
//			  , plain: true
			  , modal: true
			  , storesList: null
			  , filters: null
	}

	/**
	 * @property {Sbi.data.FilterEditorWizardPanel} editorMainPanel
	 *  Container of the wizard panel
	 */
	, editorMainPanel: null

	, constructor : function(config) {
		Sbi.trace("[FilterEditorWizard.constructor]: IN");
		this.initConfig(config);
		this.init(config);
		this.initEvents();
		this.callParent(arguments);
		Sbi.trace("[FilterEditorWizard.constructor]: OUT");
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

	, getFiltersEditorPage: function() {
		return this.editorMainPanel.getFilterEditorPage();
	}


	, getWizardState: function() {
		return this.editorMainPanel.getWizardState();
	}

	, setWizardState: function(editorState) {
		Sbi.trace("[FilterEditorWizard.setWizardState]: IN");
		Sbi.trace("[FilterEditorWizard.setWizardState]: wizard new configuration is equal to [" + Sbi.toSource(editorState) + "]");
		this.editorMainPanel.setWizardState(editorState);
		Sbi.trace("[FilterEditorWizard.setWizardState]: OUT");
	}

	, resetWizardState: function() {
		Sbi.trace("[FilterEditorWizard.resetWizardState]: IN");
		this.editorMainPanel.resetWizardState();
		Sbi.trace("[FilterEditorWizard.resetWizardState]: OUT");
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, init: function(c){
		Sbi.trace("[FilterEditorWizard.init]: IN");
		this.editorMainPanel = Ext.create('Sbi.filters.FilterEditorWizardPanel',{
			storesList: c.storesList
		  , filters: c.filters
		});
		this.editorMainPanel.on('cancel', this.onCancel, this);
		this.editorMainPanel.on('submit', this.onSubmit, this);

		Sbi.trace("[FilterEditorWizard.init]: OUT");
	}

	, initEvents: function() {
		this.addEvents(
			/**
			* @event indicatorsChanged
			* Fires when data inserted in the wizard is canceled by the user
			* @param {FilterEditorWizard} this
			*/
			'cancel'
			/**
			* @event apply
			* Fires when data inserted in the wizard is applied by the user
			* @param {FilterEditorWizard} this
			*/
			, 'apply'
			/**
			* @event submit
			* Fires when data inserted in the wizard is submitted by the user
			* @param {FilterEditorWizard} this
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
