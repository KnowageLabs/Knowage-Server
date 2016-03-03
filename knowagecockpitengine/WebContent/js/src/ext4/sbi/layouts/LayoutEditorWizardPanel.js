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

Ext.define('Sbi.layouts.LayoutEditorWizardPanel', {
		extend: 'Sbi.widgets.WizardPanel'

	, config:{
		layouts: null
	  , frame: false
	  , border: false
	}

	/**
	 * @property {Sbi.layouts.editor.main.LayoutEditorPage} layoutEditorPage
	 * The page that manages layout editing
	 */
	, layoutEditorPage: null

	, constructor : function(config) {
		Sbi.trace("[LayoutEditorWizardPanel.constructor]: IN");
		this.initConfig(config);
		this.callParent(arguments);
		Sbi.trace("[LayoutEditorWizardPanel.constructor]: OUT");
	}


	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, getLayoutEditorPage: function() {
		return this.layoutEditorPage;
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, initPages: function(){
		Sbi.trace("[LayoutEditorWizardPanel.initPages]: IN");

		this.pages = new Array();

		this.initLayoutEditorPage();
		this.pages.push(this.layoutEditorPage);

		Sbi.trace("[LayoutEditorWizardPanel.initPages]: layout editor page succesfully adedd");

		Sbi.trace("[LayoutEditorWizardPanel.initPages]: OUT");

		return this.pages;
	}

	, initButtons: function(){
		Sbi.trace("[LayoutEditorWizardPanel.initButtons]: IN");

		this.buttons = new Array();

		this.buttons.push('->');

		this.buttons.push({
			id: 'submit'
			, hidden: false
	        , text:  LN('sbi.ds.wizard.confirm')
	        , handler: this.onSubmit
	        , scope: this
	    });

		this.buttons.push({
			id: 'cancel'
	        , text:  LN('sbi.ds.wizard.cancel')
	        , handler: this.onCancel
	        , scope: this
	    });

		Sbi.trace("[LayoutEditorWizardPanel.initButtons]: layouts editor buttons succesfully adedd");

		Sbi.trace("[LayoutEditorWizardPanel.initButtons]: OUT");

		return this.buttons;
	}

	, initLayoutEditorPage: function() {
		Sbi.trace("[LayoutEditorWizardPanel.initLayoutEditorPage]: IN");
		this.layoutEditorPage = Ext.create('Sbi.layouts.editor.main.LayoutEditorPage',{
			  layouts: this.layouts
		});
		Sbi.trace("[LayoutEditorWizardPanel.initLayoutEditorPage]: IN");
		return this.layoutEditorPage;
	}



	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

});
