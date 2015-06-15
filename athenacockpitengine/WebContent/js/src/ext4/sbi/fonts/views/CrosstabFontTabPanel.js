/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 * Crosstab font options panel
 * 
 *     
 *  @author
 *  Giorgio Federici (giorgio.federici@eng.it)
 */


Ext.define('Sbi.fonts.views.CrosstabFontTabPanel', {
	extend: 'Ext.form.Panel',
	
	layout: 	{ type: 'table', columns: 1 },
    title: 	LN('sbi.cockpit.font.editor.wizard.crosstab'),
    id: 'crosstabFonts',
	
	config:{		
		fonts: null
	}

	/**
	 * @property tableGeneralFontOptions
	 * General font options
	 */
	, tableGeneralFontOptions: null, 
    
	/**
	 * @property tdLevelFontOptions
	 * TD Level css class font options
	 */
	tdLevelFontOptions: null,
	
	/**
	 * @property tdMemberFontOptions
	 * TD Member css class font options
	 */
    tdMemberFontOptions: null, 

	/**
	 * @property tdDataFontOptions
	 * TD data font options
	 */
    tdDataFontOptions: null
	
	
	, constructor : function(config) {
		Sbi.trace("[CrosstabFontTabPanel.constructor]: IN");
		
		this.initConfig(config);
		this.init();
		this.callParent(arguments);

		Sbi.trace("[CrosstabFontTabPanel.constructor]: OUT");
	}

	, initComponent: function() {
        Ext.apply(this, {
            items: [this.tableGeneralFontOptions, 
                    this.tdLevelFontOptions, 
                    this.tdMemberFontOptions, 
                    this.tdDataFontOptions]
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
		this.initFontEditorCrosstabTab();
	}
	
	, initFontEditorCrosstabTab: function(){
		
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
	        , title: 			LN('sbi.cockpit.designer.fontConf.crosstabFontGeneralOpts')
	        , margin: 			10
	    	, items: 			[fontTypeCombo, fontSizeCombo]	
			, width:			600
		};
		
		
		/* crosstab font headers options */		
		 
		 var tdLevelFontSizeCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdLevelFontSize',
				labelWidth:		130,
				width:			180
			});
		 
		 var tdLevelFontColorText = Ext.create('Ext.form.field.Text',{
			 fieldLabel: 		LN('sbi.cockpit.designer.fontConf.fontColor'),
			 name: 				'tdLevelFontColor',
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
		 
		 var tdLevelFontWeightCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdLevelFontWeight',
				labelWidth:		130,
				width:			245

			});
		 
		 var tdLevelFontDecorationCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdLevelFontDecoration',
				labelWidth:		140,
				width:			255

			});
		 
		 this.tdLevelFontOptions = 
		 {
				 xtype: 				'fieldset'
				, fieldDefaults: 	{ margin: 5}
				, layout: 			{type: 'table', columns: 2}
		        , collapsible: 		true
		        , collapsed: 		false
		        , title: 			LN('sbi.cockpit.designer.fontConf.crosstabHeadersFontOpts')
		    	, margin: 			10
		    	, items: 			[tdLevelFontSizeCombo, tdLevelFontColorText, tdLevelFontWeightCombo, tdLevelFontDecorationCombo]	
				, width:			600
		};
		
		 
		 /* measures font options */
		 
		 var tdMemberFontSizeCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdMemberFontSize',
				labelWidth:		130,
				width:			180,
			});
		 
		
		 var tdMemberFontColorText = Ext.create('Ext.form.field.Text',{
				 fieldLabel: 		LN('sbi.cockpit.designer.fontConf.fontColor'),
				 name: 				'tdMemberFontColor',
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
		 
		 var tdMemberFontWeightCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdMemberFontWeight',
				labelWidth:		130,
				width:			245

			});
		 
		 var tdMemberFontDecorationCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdMemberFontDecoration',
				labelWidth:		140,
				width:			255

			});
		
		 
		this.tdMemberFontOptions = 
		{
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.designer.fontConf.measuresHeadersFontOpts')
	    	, margin: 			10
	    	, items: 			[tdMemberFontSizeCombo, tdMemberFontColorText, tdMemberFontWeightCombo, tdMemberFontDecorationCombo]	
			, width:			600
		};
		
		 
		/* data font options */		
		 
		 var tdDataFontSizeCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdDataFontSize',
				labelWidth:		130,
				width:			180
			});
		 
		 var tdDataFontColorText = Ext.create('Ext.form.field.Text',{
			 fieldLabel: 		LN('sbi.cockpit.designer.fontConf.fontColor'),
			 name: 				'tdDataFontColor',
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
		 
		 var tdDataFontWeightCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdDataFontWeight',
				labelWidth:		130,
				width:			245

			});
		 
		 var tdDataFontDecorationCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdDataFontDecoration',
				labelWidth:		140,
				width:			255

			});
		 
		this.tdDataFontOptions = 
		{
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.designer.fontConf.measuresFontOpts')
	    	, margin: 			10
	    	, items: 			[tdDataFontSizeCombo, tdDataFontColorText, tdDataFontWeightCombo, tdDataFontDecorationCombo]	
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

