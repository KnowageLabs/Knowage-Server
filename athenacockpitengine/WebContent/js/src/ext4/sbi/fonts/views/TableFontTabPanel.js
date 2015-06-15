/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 * Table font options panel
 * 
 *     
 *  @author
 *  Giorgio Federici (giorgio.federici@eng.it)
 */


Ext.define('Sbi.fonts.views.TableFontTabPanel', {
	extend: 'Ext.form.Panel',
	
	layout: 	{ type: 'table', columns: 1 },
    title: 	LN('sbi.cockpit.font.editor.wizard.table'),
    id: 'tableFonts',
	
	config:{		
		fonts: null
	}

	
	/**
	 * @property tableGeneralFontOptions
	 * General font options
	 */
	, tableGeneralFontOptions: null
	
	/**
	 * @property tableHeaderFontOptions
	 * Table header font options
	 */
	, tableHeaderFontOptions: null
	
	/**
	 * @property tableRowsFontOptions
	 * Table rows font options
	 */
	, tableRowsFontOptions: null

	, constructor : function(config) {
		Sbi.trace("[TableFontTabPanel.constructor]: IN");
		
		this.initConfig(config);
		this.init();
		this.callParent(arguments);

		Sbi.trace("[TableFontTabPanel.constructor]: OUT");
	}

	, initComponent: function() {
        Ext.apply(this, {
            items: [this.tableGeneralFontOptions, 
                    this.tableHeaderFontOptions, 
                    this.tableRowsFontOptions]	
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
		this.initFontEditorTableTab();
	}
	
	, initFontEditorTableTab: function(){
		
		var fontSizeStore =  Ext.create('Sbi.fonts.stores.FontSizeStore',{});
		
		var fontFamilyStore = Ext.create('Sbi.fonts.stores.FontFamilyStore', {});
		
		var fontDecorationStore = Ext.create('Sbi.fonts.stores.FontDecorationStore', {});
		
		var fontWeightStore = Ext.create('Sbi.fonts.stores.FontWeightStore', {});
		
		var hexColorReg = new RegExp("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
		
		/* table font general options */
		
		var fontTypeCombo = Ext.create('Ext.form.ComboBox',{
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.widgetFontType'),
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			typeAhead: 		true,
			lazyRender:		true,
			store: 			fontFamilyStore, 
			valueField: 	'name',
			displayField: 	'description',
			name:			'fontType',
			labelWidth:		110,
			width:			245

		});
		
		var fontSizeCombo = Ext.create('Ext.form.ComboBox',{
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.widgetFontSize'),
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			typeAhead: 		true,
			lazyRender:		true,
			store: 			fontSizeStore, 
			valueField: 	'name',
			displayField: 	'description',
			name:			'fontSize',
			labelWidth:		120,
			width:			170

		});
		
		this.tableGeneralFontOptions = 
		{
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.designer.fontConf.tableFontGeneralOpts')
	        , margin: 			10
	    	, items: 			[fontTypeCombo, fontSizeCombo]	
			, width:			600
		};
		
		
		/* table font header options */
		
		 var headerFontSizeCombo = Ext.create('Ext.form.ComboBox',{
				fieldLabel: 	LN('sbi.cockpit.designer.fontConf.widgetFontSize'),
				queryMode:      'local',
				triggerAction:  'all',
				forceSelection: true,
				editable:       false,
				allowBlank: 	true,
				typeAhead: 		true,
				lazyRender:		true,
				store: 			fontSizeStore,    
				valueField: 	'name',
				displayField: 	'description',
				name:			'headerFontSize',
				labelWidth:		130,
				width:			180,
			});
		 
		
		 var headerFontColorText = Ext.create('Ext.form.field.Text',{
				 fieldLabel: 		LN('sbi.cockpit.designer.fontConf.fontColor'),
				 name: 				'headerFontColor',
		         allowBlank: 		true,
		         regex: 			hexColorReg,
		         regextText: 		'Not a valid HEX color',
		    	 enforceMaxLength: 	true,
		 		 maxLength: 		7,
		 		 msgTarget: 		'side',
	 			labelWidth:			140,
				width:				250,
				afterLabelTextTpl : '<span class="help" data-qtip="'
					+ LN('sbi.cockpit.designer.fontConf.fontColorTip')
	            	+ '">&nbsp;&nbsp;&nbsp;&nbsp;</span>',
		 });
		 
		 var headerFontWeightCombo = Ext.create('Ext.form.ComboBox',{
				fieldLabel: 	LN('sbi.cockpit.designer.fontConf.fontWeight'),
				queryMode:      'local',
				triggerAction:  'all',
				forceSelection: true,
				editable:       false,
				allowBlank: 	true,
				typeAhead: 		true,
				lazyRender:		true,
				store: 			fontWeightStore, 
				valueField: 	'name',
				displayField: 	'description',
				name:			'headerFontWeight',
				labelWidth:		130,
				width:			245

			});
		 
		 var headerFontDecorationCombo = Ext.create('Ext.form.ComboBox',{
				fieldLabel: 	LN('sbi.cockpit.designer.fontConf.fontDecoration'),
				queryMode:      'local',
				triggerAction:  'all',
				forceSelection: true,
				editable:       false,
				allowBlank: 	true,
				typeAhead: 		true,
				lazyRender:		true,
				store: 			fontDecorationStore, 
				valueField: 	'name',
				displayField: 	'description',
				name:			'headerFontDecoration',
				labelWidth:		140,
				width:			255

			});
		
		 
		this.tableHeaderFontOptions = 
		{
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.designer.fontConf.tableHeaderFontOptions')
	    	, margin: 			10
	    	, items: 			[headerFontSizeCombo, headerFontColorText, headerFontWeightCombo, headerFontDecorationCombo]	
			, width:			600
		};
		
		 
		 /* table font rows options */
		 
		 var rowsFontSizeCombo = Ext.create('Ext.form.ComboBox',{
				fieldLabel: 	LN('sbi.cockpit.designer.fontConf.widgetFontSize'),
				typeAhead: 		true,
				triggerAction: 'all',
				lazyRender:		true,
				queryMode:      'local',
				forceSelection: true,
				editable:       false,
				allowBlank: 	true,
				store: 			fontSizeStore,    
				valueField: 	'name',
				displayField: 	'description',
				name:			'rowsFontSize',
				labelWidth:		130,
				width:			180
			});
		 
		 var rowsFontColorText = Ext.create('Ext.form.field.Text',{
			 fieldLabel: 		LN('sbi.cockpit.designer.fontConf.fontColor'),
			 name: 				'rowsFontColor',
	         allowBlank: 		true,
	         regex: 			hexColorReg,
	         regextText: 		'Not a valid HEX color',
	    	 enforceMaxLength: 	true,
	 		 maxLength: 		7,
	 		 msgTarget: 		'side',
			 labelWidth:		140,
			 width:				250,
			 afterLabelTextTpl : '<span class="help" data-qtip="'
	         	+ LN('sbi.cockpit.designer.fontConf.fontColorTip')
	         	+ '">&nbsp;&nbsp;&nbsp;&nbsp;</span>',
		 });
		 
		 var rowsFontWeightCombo = Ext.create('Ext.form.ComboBox',{
				fieldLabel: 	LN('sbi.cockpit.designer.fontConf.fontWeight'),
				queryMode:      'local',
				triggerAction:  'all',
				forceSelection: true,
				editable:       false,
				allowBlank: 	true,
				typeAhead: 		true,
				lazyRender:		true,
				store: 			fontWeightStore, 
				valueField: 	'name',
				displayField: 	'description',
				name:			'rowsFontWeight',
				labelWidth:		130,
				width:			245

			});
		 
		var rowsFontDecorationCombo = Ext.create('Ext.form.ComboBox',{
				fieldLabel: 	LN('sbi.cockpit.designer.fontConf.fontDecoration'),
				queryMode:      'local',
				triggerAction:  'all',
				forceSelection: true,
				editable:       false,
				allowBlank: 	true,
				typeAhead: 		true,
				lazyRender:		true,
				store: 			fontDecorationStore, 
				valueField: 	'name',
				displayField: 	'description',
				name:			'rowsFontDecoration',
				labelWidth:		140,
				width:			255

			});
		 
		this.tableRowsFontOptions = 
		{
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.designer.fontConf.tableRowsFontOptions')
	    	, margin: 			10
	    	, items: 			[rowsFontSizeCombo, rowsFontColorText, rowsFontWeightCombo, rowsFontDecorationCombo]	
			, width:			600
		};

	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
	
	, loadFontsConfiguration: function(){
		
		var form = this.getForm();		
		form.setValues(this.fonts);
	}
	
	, beforeRender: function () {
		
		this.loadFontsConfiguration();
		this.callParent(arguments);
    }
	
});

