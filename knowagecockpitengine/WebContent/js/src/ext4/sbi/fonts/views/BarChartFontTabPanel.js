/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 * Bar chart font options panel
 * 
 *     
 *  @author
 *  Giorgio Federici (giorgio.federici@eng.it)
 */


Ext.define('Sbi.fonts.views.BarChartFontTabPanel', {
	extend: 'Ext.form.Panel',
	
	layout: 	{ type: 'table', columns: 1 },
    id: 'barChartFonts',
	
	config:{
		title: 	LN('sbi.cockpit.font.editor.wizard.barchart'),
		fonts: null
	}

	
	/**
	 * @property chartGeneralFontOptions
	 * General font options
	 */
	, chartGeneralFontOptions: null
	
	/**
	 * @property chartFontSizeOptions
	 * Size font options
	 */
	, chartFontSizeOptions: null

	, constructor : function(config) {
		Sbi.trace("[BarChartFontTabPanel.constructor]: IN");
		
		this.initConfig(config);
		this.init();
		this.callParent(arguments);

		Sbi.trace("[BarChartFontTabPanel.constructor]: OUT");
	}

	, initComponent: function() {
		
        Ext.apply(this, {
            items: [this.chartGeneralFontOptions, 
                    this.chartFontSizeOptions]
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
		
		this.initFontEditorBarChartTab();

	}
	
	, initFontEditorBarChartTab: function(){
		
		var fontFamilyStore = Ext.create('Sbi.fonts.stores.FontFamilyStore', {});
		
		var fontSizeStore = Ext.create('Sbi.fonts.stores.FontSizeStore',{ });
		
		var fontTypeCombo = Ext.create('Ext.form.ComboBox',
		{
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.widgetFontType'),
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			typeAhead: 		true,
			store: 			fontFamilyStore,  
			valueField: 	'name',
			displayField: 	'description',
			name:			'fontType',
			labelWidth:		110,
			width:			245

		});
	    
	   
	    
	    var fontSizeCombo = Ext.create('Ext.form.ComboBox', {
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.widgetFontSize'),
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			typeAhead: 		true,
			store: 			fontSizeStore,    
			valueField: 	'name',
			displayField: 	'description',
			name:			'fontSize',
			labelWidth:		120,
			width:			170

		});
	    
	    this.chartGeneralFontOptions = 
		{
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.designer.fontConf.chartGeneralFontOpts')
	    	, margin: 			10
	    	, items: 			[fontTypeCombo, fontSizeCombo]	
			, width:			600
		};
		
		
    	var legendFontSizeCombo = Ext.create('Ext.form.ComboBox', 
    	{
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.legendFontSize'),
			typeAhead: 		true,
			triggerAction: 'all',
			queryMode:      'local',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			store: 			fontSizeStore,    
			valueField: 	'name',
			displayField: 	'description',
			name:			'legendFontSize',
			labelWidth:		60,
			width:			110
		});
		
		var axisTitleFontSizeCombo = Ext.create('Ext.form.ComboBox',
		{
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.axisTitleFontSize'),
			typeAhead: 		true,
			triggerAction: 'all',
			queryMode:      'local',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			store: 			fontSizeStore,    
			valueField: 	'name',
			displayField: 	'description',
			name:			'axisTitleFontSize',
			labelWidth:		100,
			width:			150
		});
		
		var tooltipLabelFontSizeCombo = Ext.create('Ext.form.ComboBox',
		{
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.tooltipLabelFontSize'),
			typeAhead: 		true,
			triggerAction: 'all',
			queryMode:      'local',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			store: 			fontSizeStore,    
			valueField: 	'name',
			displayField: 	'description',
			name:			'tooltipLabelFontSize',
			labelWidth:		60,
			width:			110
		});
		
		var axisLabelsFontSizeCombo = Ext.create('Ext.form.ComboBox',
		{
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.axisLabelsFontSize'),
			typeAhead: 		true,
			triggerAction: 'all',
			queryMode:      'local',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			store: 			fontSizeStore,    
			valueField: 	'name',
			displayField: 	'description',
			name:			'axisLabelsFontSize',
			labelWidth:		100,
			width:			150
		});
		
		
		this.chartFontSizeOptions = 
		{
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.designer.fontConf.chartFontSizeOpts')
	    	, margin: 			10
	    	, items: 			[legendFontSizeCombo, axisTitleFontSizeCombo, tooltipLabelFontSizeCombo, axisLabelsFontSizeCombo]	
			, width:			600
		}; 
		
	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
    
	, loadFontsConfiguration: function(){
		
		var form = this.getForm();
		if(!Sbi.isEmptyObject(this.fonts)){
			form.setValues(this.fonts);
		}
	}
	
	, beforeRender: function () {
		
		this.loadFontsConfiguration();
		this.callParent(arguments);
    }
});

