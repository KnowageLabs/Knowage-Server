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

