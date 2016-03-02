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

Ext.define('Sbi.filters.FilterEditorWizardPanel', {
		extend: 'Sbi.widgets.WizardPanel'

	, config:{
	   storesList: null
	  , filters: null
	  , frame: false
	  , border: false
	}

	/**
	 * @property {Sbi.filters.editor.main.FilterEditorPage} filterEditorPage
	 * The page that manages filters editing
	 */
	, filterEditorPage: null

	, constructor : function(config) {
		Sbi.trace("[FilterEditorWizardPanel.constructor]: IN");
		this.initConfig(config);
		this.callParent(arguments);
		Sbi.trace("[FilterEditorWizardPanel.constructor]: OUT");
	}


	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, getFilterEditorPage: function() {
		return this.filterEditorPage;
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, initPages: function(){
		Sbi.trace("[FilterEditorWizardPanel.initPages]: IN");

		this.pages = new Array();

		this.initFilterEditorPage();
		this.pages.push(this.filterEditorPage);

		Sbi.trace("[FilterEditorWizardPanel.initPages]: filter editor page succesfully adedd");

		Sbi.trace("[FilterEditorWizardPanel.initPages]: OUT");

		return this.pages;
	}

	, initButtons: function(){
		Sbi.trace("[FilterEditorWizardPanel.initButtons]: IN");

		this.buttons = new Array();

		this.buttons.push('->');

		this.buttons.push({
			id: 'submit'
			, hidden: false
	        , text:  LN('sbi.ds.wizard.confirm')
	        , handler: this.onSubmit
	        , scope: this
//	        , disabled: (this.activeItem == 0)?false:true
//	        , disabled: true
	    });

		this.buttons.push({
			id: 'cancel'
	        , text:  LN('sbi.ds.wizard.cancel')
	        , handler: this.onCancel
	        , scope: this
	    });

		Sbi.trace("[FilterEditorWizardPanel.initButtons]: association editor buttons succesfully adedd");

		Sbi.trace("[FilterEditorWizardPanel.initButtons]: OUT");

		return this.buttons;
	}

	, initFilterEditorPage: function() {
		Sbi.trace("[FilterEditorWizardPanel.initFilterEditorPage]: IN");
		this.filterEditorPage = Ext.create('Sbi.filters.editor.main.FilterEditorPage',{
			storesList: this.storesList
		  , filters: this.filters
		});
		Sbi.trace("[FilterEditorWizardPanel.initFilterEditorPage]: IN");
		return this.filterEditorPage;
	}



	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

});
