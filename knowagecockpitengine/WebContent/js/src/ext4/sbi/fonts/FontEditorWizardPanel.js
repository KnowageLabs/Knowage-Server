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

Ext.define('Sbi.fonts.FontEditorWizardPanel', {
		extend: 'Sbi.widgets.WizardPanel'

	, config:{
		fonts: null
	  , frame: false
	  , border: false
	}

	/**
	 * @property {Sbi.fonts.editor.main.FontEditorPage} fontEditorPage
	 * The page that manages font editing
	 */
	, fontEditorPage: null

	, constructor : function(config) {
		Sbi.trace("[FontEditorWizardPanel.constructor]: IN");
		this.initConfig(config);
		this.callParent(arguments);
		Sbi.trace("[FontEditorWizardPanel.constructor]: OUT");
	}


	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, getFontEditorPage: function() {
		return this.fontEditorPage;
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, initPages: function(){
		Sbi.trace("[FontEditorWizardPanel.initPages]: IN");

		this.pages = new Array();

		this.initFontEditorPage();
		this.pages.push(this.fontEditorPage);

		Sbi.trace("[FontEditorWizardPanel.initPages]: font editor page succesfully adedd");

		Sbi.trace("[FontEditorWizardPanel.initPages]: OUT");

		return this.pages;
	}

	, initButtons: function(){
		Sbi.trace("[FontEditorWizardPanel.initButtons]: IN");

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

		Sbi.trace("[FontEditorWizardPanel.initButtons]: fonts editor buttons succesfully adedd");

		Sbi.trace("[FontEditorWizardPanel.initButtons]: OUT");

		return this.buttons;
	}

	, initFontEditorPage: function() {
		Sbi.trace("[FontEditorWizardPanel.initFontEditorPage]: IN");
		this.fontEditorPage = Ext.create('Sbi.fonts.editor.main.FontEditorPage',{
			  fonts: this.fonts
		});
		Sbi.trace("[FontEditorWizardPanel.initFontEditorPage]: IN");
		return this.fontEditorPage;
	}



	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

});
