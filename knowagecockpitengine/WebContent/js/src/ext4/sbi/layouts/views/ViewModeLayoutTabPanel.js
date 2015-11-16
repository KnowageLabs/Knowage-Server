/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 * View mode layout options panel
 * 
 *     
 *  @author
 *  Giorgio Federici (giorgio.federici@eng.it)
 */


Ext.define('Sbi.layouts.views.ViewModeLayoutTabPanel', {
	extend: 'Ext.form.Panel',
	
	layout: 	{ type: 'table', columns: 1 },
    id: 'viewModeLayouts',
	
	config:{
		title: 	LN('sbi.cockpit.layouts.editor.wizard.viewmode'),
		layouts: null
	}

	
	/**
	 * @property widgetWindowStyleOptions
	 * Widget window style options
	 */
	, widgetWindowStyleOptions: null

	, constructor : function(config) {
		Sbi.trace("[ViewModeLayoutTabPanel.constructor]: IN");
		
		this.initConfig(config);
		this.init();
		this.callParent(arguments);

		Sbi.trace("[ViewModeLayoutTabPanel.constructor]: OUT");
	}

	, initComponent: function() {
		
        Ext.apply(this, {
            items: [this.widgetWindowStyleOptions]
        });
        this.callParent();
	}

	// =================================================================================================================
	// METHODS
	// =================================================================================================================

    // -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
	/**
	 * @method
	 *
	 * Initialize the GUI
	 */
	, init: function() {
		
		this.initLayoutEditorViewModeTab();

	}
	
	, initLayoutEditorViewModeTab: function(){
	
		var showHeaderCheck = Ext.create('Ext.form.Checkbox',
		{
				name: 'showHeader'
				, labelWidth: 120
				, checked: false
				, fieldLabel: LN('sbi.cockpit.layouts.editor.wizard.viewmode.showBorder')
		});
		
		
		this.widgetWindowStyleOptions = 
		{
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.layouts.editor.wizard.viewmode.widgetWindowStyle')
	    	, margin: 			10
	    	, items: 			[showHeaderCheck]	
			, width:			600
		}; 
		
	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
    
	, loadLayoutsConfiguration: function(){
		
		var form = this.getForm();
		if(!Sbi.isEmptyObject(this.layouts)){
			form.setValues(this.layouts);
		}
	}
	
	, beforeRender: function () {
		
		this.loadLayoutsConfiguration();
		this.callParent(arguments);
    }
});

