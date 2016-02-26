Ext.define('Sbi.chart.designer.SerieStylePopup', {
	extend: 'Ext.form.Panel',
//	requires: [
//	    'Sbi.chart.designer.components.ColorPicker',
//	    'Sbi.chart.designer.FontCombo',
//	    'Sbi.chart.designer.FontStyleCombo',
//	    'Sbi.chart.designer.FontDimCombo',
//	    'Sbi.chart.designer.FontAlignCombo'
//	],
	
	id: 'serieStylePopup',
    title: LN('sbi.chartengine.designer.seriesstyleconf'),
    layout: 'border',
    bodyPadding: 5,
	floating: true,
    draggable: true,
    closable : true,
    closeAction: 'destroy',
    
    /**
     * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
//    width: 500,
    
    /**
     * Providing these possibilities for the Serie style configuration popup:
     * (1) 	Resizing of the popup window for both axes (vertically and horizontally)
     * (2) 	The height is static and when initially rendered it has the value defined
     * 		in the parameter specified here (afterwards, user can resize it). But in both
     * 		situations, the height of the popup will remain constants, so the expansion of
     * 		fieldsets within it will just enable the inner vertical scrollbar.
     * (3) 	The width of the initial popup (until user potentially change it) is reduced.
     * 
     * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
    width: Sbi.settings.chart.structureStep.axisAndSerieStyleConfigPopup.width,
    height: Sbi.settings.chart.structureStep.axisAndSerieStyleConfigPopup.height,
    resizable: Sbi.settings.chart.structureStep.axisAndSerieStyleConfigPopup.resizable,
    overflowY: Sbi.settings.chart.structureStep.axisAndSerieStyleConfigPopup.overflowY,
    
    modal: true,
	config: {
		store: '',
		rowIndex: ''
	},
	
	/* * * * * * * Internal components * * * * * * * */
	serieFieldSet: null,
	tooltipFieldSet: null,
	
	serieNameTextField: null,
	serieTypesComboBox: null,
	serieOrderComboBox: null,
	serieColorPicker: null,
	serieShowValue: null,
	serieShowAbsValue: null,
	serieShowPercentage:null,

	seriePrecisionNumberField: null,
	seriePrefixCharTextField: null,
	seriePostfixCharTextField: null,
	
	/**
	 * This item is going to be removed since the serie tooltip HTML template
	 * is handled by the velocity model of the appropriate chart type (this is
	 * done staticly, "under the hood").
	 * 
	 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
//	tooltipTemplateHtml: null,
	
	tooltipColor: null,
	tooltipBackgroundColor: null,
	tooltipAlignComboBox: null,
	tooltipFontComboBox: null,
	tooltipFontWeightStylesComboBox: null,
	tooltipFontSizeComboBox: null,
	tooltipBorderWidthNumberfield:null,
	tooltipBorderRadiusNumberfield:null,
	/* * * * * * * END Internal components * * * * * * * */

	layout: 'anchor',
    defaults: {
        anchor: '100%',
    },

    // The fields
    defaultType: 'textfield',
	constructor: function(config) {
		var LABEL_WIDTH = 115;
		
		this.callParent(config);		
		
		// Esc key pressing closes the modal
		this.keyMap = new Ext.util.KeyMap(Ext.getBody(), [{
			key: Ext.EventObject.ESC,
			defaultEventAction: 'preventDefault',
			scope: this,
			fn: function() {
				this.destroy()
			}
		}]);
		
		var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
		var chartLibrary = Sbi.chart.designer.Designer.chartLibNamesConfig[chartType.toLowerCase()];
		
		/**
	     * https://production.eng.it/jira/browse/KNOWAGE-490
	     * When we are creating a chart from the Cockpit Engine, we need to use
	     * a different height, because inside the Wizard, there is less space
	     * 
	     * @author Giorgio Federici (giofeder, giorgio.federici@eng.it)
	     */
		
		if(Sbi.chart.designer.ChartUtils.isCockpitEngine){
			this.setHeight(Sbi.settings.chart.structureStep.cockpitAxisAndSerieStyleConfigPopup.height);
		}
		
		
		store = config.store,
	
		rowIndex = config.rowIndex;
		var dataAtRow = store.getAt(rowIndex);	
			
		this.serieFieldSet = Ext.create('Ext.form.FieldSet', {
			collapsible: true,
			title: LN('sbi.chartengine.designer.series'),
			defaults: {anchor: '100%',
				labelAlign : 'left',
				labelWidth : LABEL_WIDTH
			},
			layout: 'anchor',
			items : []
		});
			
		this.tooltipFieldSet = Ext.create('Ext.form.FieldSet', {
			collapsible: true,
			/**
		     * https://production.eng.it/jira/browse/KNOWAGE-490
		     * When we are creating a chart from the Cockpit Engine, the tooltip
		     * fieldset should be collapsed
		     * 
		     * @author Giorgio Federici (giofeder, giorgio.federici@eng.it)
		     */
			collapsed: Sbi.chart.designer.ChartUtils.isCockpitEngine,
			title: LN('sbi.chartengine.designer.tooltip'),
			defaults: {anchor: '100%',
				labelAlign : 'left',
				labelWidth : LABEL_WIDTH,
			},
			hidden : ChartUtils.isSerieTooltipConfigurationDisabled(),
			layout: 'anchor',
			items : []
		});
		
		/* * * * * * * * * * SERIE FIELDS  * * * * * *  * * * * */
		var serieName = dataAtRow.get('axisName');
		this.serieNameTextField = Ext.create('Ext.form.field.Text', {
			name: 'serieName',
			value: (serieName && serieName.trim() != '') ? serieName.trim() : '',
			fieldLabel: LN('sbi.generic.name'),
			selectOnFocus: true,
			allowBlank: true,
			emptyText: LN("sbi.chartengine.structure.serieStyleConfig.serie.name.emptyText")
		});
		this.serieFieldSet.add(this.serieNameTextField);
	    if(chartType.toUpperCase()=="WORDCLOUD" || chartType.toUpperCase()=="PARALLEL" || chartType.toUpperCase()=="CHORD" ){
	    	this.serieNameTextField.hide();
	    }
		
		var serieType = null;
		var serieTypes = null;
		
		if(chartType.toUpperCase() == 'PIE') {
			serieType = dataAtRow.get('serieType') && dataAtRow.get('serieType').toUpperCase() == 'PIE'? dataAtRow.get('serieType') : '';
			serieTypes = [
				{name: LN('sbi.chartengine.designer.charttype.notype'), value:''},
				{name: LN('sbi.chartengine.designer.charttype.pie'), value:'pie'} 
			];
		} else {
			serieType = dataAtRow.get('serieType') && dataAtRow.get('serieType').toUpperCase() != 'PIE'? dataAtRow.get('serieType') : '';
			serieTypes = [
				{name: LN('sbi.chartengine.designer.charttype.notype'), value:''},
				{name: LN('sbi.chartengine.designer.charttype.bar'), value:'bar'},
				{name: LN('sbi.chartengine.designer.charttype.line'), value:'line'},
				{name: LN('sbi.chartengine.designer.charttype.area'), value:'area'},
            ];
		}
		
		this.serieTypesComboBox = Ext.create('Ext.form.ComboBox', {
			store: {
				store: 'array',
				fields: ['name', 'value'],
				data: serieTypes
			},
			value: (serieType && serieType.trim() != '') ? serieType.trim() : '',
			valueField: 'value',
			displayField: 'name',
			fieldLabel : LN('sbi.chartengine.designer.seriestype'),
			editable: false,
			emptyText: LN("sbi.chartengine.structure.serieStyleConfig.serie.type.emptyText")
		});				
		
		/**
		 * Disable combo box for the series type of the serie item (that we picked
		 * and put inside the Y-axis (serie) panel) for these chart types (since we
		 * do not need them). Even there are numerous chart types that do not need this
		 * option (this combo box), but we will just specify those following chart types 
		 * since for those we have the option of changing the serie configuration for 
		 * separate serie items inside.
		 * Y-axis panel(s)
		 * (danilo.ristovski@mht.net)
		 */
		if ((chartType == "CHORD"
			|| chartType == "GAUGE" 
				|| chartType == "PIE" 
					|| chartType == "RADAR" 
						|| chartType == "SCATTER"
							|| chartType=="WORDCLOUD"
								|| chartType=="PARALLEL")
				||(chartLibrary == 'chartJs')){
		
			this.serieTypesComboBox.hide();
		}	
		
		this.serieFieldSet.add(this.serieTypesComboBox);		
		
		var serieOrder = dataAtRow.get('serieOrderType');
		this.serieOrderComboBox = Ext.create('Sbi.chart.designer.SeriesOrderCombo', {
			value: (serieOrder && serieOrder.trim() != '') ? serieOrder.trim() : '',
		});
		this.serieFieldSet.add(this.serieOrderComboBox);
				
		if(chartType=="WORDCLOUD" || chartType=="PARALLEL" || chartType=="CHORD" ){
			this.serieOrderComboBox.hide();
		}
		
		var serieColor = dataAtRow.get('serieColor').replace('#', '');
		
		this.serieColorPicker = Ext.create('Sbi.chart.designer.components.ColorPicker',{
			id: "serieColorFieldSet",
			fieldLabel : LN('sbi.chartengine.designer.color'),
			emptyText: LN('sbi.chartengine.configuration.seriescolor.emptyText'),
			labelWidth : LABEL_WIDTH,
			value: serieColor
		});
		
		this.serieFieldSet.add(this.serieColorPicker);
		
		/**
		 * This parameters does not play any role when chart is of type PIE
		 * because series (pie segments) are going to take colors that are 
		 * specified inside the color palette of the Designer. This is 
		 * parameter useful for e.g. BAR and LINE chart types.
		 * @author: danristo (danilo.ristovski@mht.net)  
		 */
		// TODO: I think there are more chart types whose serie popup should be refined !!!		
		if (chartType == "PIE" || chartType=="WORDCLOUD" || chartType=="PARALLEL" || chartType=="CHORD")
		{			
			this.serieFieldSet.getComponent("serieColorFieldSet").hide();
		}		
		
		/**
		 * The logic of detecting the boolean state of the checkbox is
		 * changed so it can handle boolean value and the string value
		 * of boolean state as well ("false" instead of false).
		 * 
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		var serieShowValue = dataAtRow.get('serieShowValue');
		
		if (serieShowValue == "false" || serieShowValue == false 
				|| serieShowValue == "" || serieShowValue == undefined)
		{
			serieShowValue = false;
		}
		else
		{
			serieShowValue = true;
		}
		
		var showValue = dataAtRow.get('serieShowValue');
		this.serieShowValue = Ext.create('Ext.form.field.Checkbox',{
			checked: serieShowValue,
			labelSeparator: '',
			hidden: (chartLibrary == 'chartJs' || chartType == 'PIE' || chartType == 'WORDCLOUD' ||  chartType == 'PARALLEL' || chartType == 'CHORD'),
			fieldLabel: LN('sbi.chartengine.designer.showvalue'),
		});		

		/**
		 * The logic of detecting the boolean state of the checkbox is
		 * changed so it can handle boolean value and the string value
		 * of boolean state as well ("false" instead of false).
		 * 
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		var serieShowAbsValue = dataAtRow.get('serieShowAbsValue');
		
		if (serieShowAbsValue == "false" || serieShowAbsValue == false 
				|| serieShowAbsValue == "" || serieShowAbsValue == undefined)
		{
			serieShowAbsValue = false;
		}
		else
		{
			serieShowAbsValue = true;
		}
		
		this.serieShowAbsValue = Ext.create('Ext.form.field.Checkbox',{
			checked: serieShowAbsValue,
			labelSeparator: '',
			hidden: (chartLibrary == 'chartJs' || chartType != 'PIE'),
			fieldLabel: LN('sbi.chartengine.designer.showAbsValue'),
		});		
		
		/**
		 * The logic of detecting the boolean state of the checkbox is
		 * changed so it can handle boolean value and the string value
		 * of boolean state as well ("false" instead of false).
		 * 
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		var serieShowPercentage = dataAtRow.get('serieShowPercentage');
		
		if (serieShowPercentage == "false" || serieShowPercentage == false 
				|| serieShowPercentage == "" || serieShowPercentage == undefined)
		{
			serieShowPercentage = false;
		}
		else
		{
			serieShowPercentage = true;
		}
		
		this.serieShowPercentage = Ext.create('Ext.form.field.Checkbox',{
			checked: serieShowPercentage,
			labelSeparator: '',
			hidden: (chartType != 'PIE' || chartType == "WORDCLOUD" || chartType == 'PARALLEL' || chartType== 'CHORD'),
			fieldLabel: LN('sbi.chartengine.designer.showPercentage'),
		});	
	
		this.serieFieldSet.add(this.serieShowValue);
		this.serieFieldSet.add(this.serieShowAbsValue);
		this.serieFieldSet.add(this.serieShowPercentage);
		
		var seriePrecision = dataAtRow.get('seriePrecision');
		
		this.seriePrecisionNumberField = Ext.create('Ext.form.field.Number', {
			id: "seriePrecisionNumberField",
			fieldLabel: LN('sbi.chartengine.designer.precision'),
			selectOnFocus: true,
			value: seriePrecision ? seriePrecision : '',
			maxValue: 10,
			minValue: 0,
			emptyText: LN("sbi.chartengine.structure.serieStyleConfig.serie.precision.emptyText")
		});
		
		this.serieFieldSet.add(this.seriePrecisionNumberField);
		
		
		var prefixChar = dataAtRow.get('seriePrefixChar');
		
		this.seriePrefixCharTextField = Ext.create('Ext.form.field.Text', {
			id: "seriePrefixCharTextField",
			name: 'name',
			value: (prefixChar && prefixChar.trim() != '') ? prefixChar.trim() : '',
			hidden: (chartLibrary == 'chartJs'),
			fieldLabel: LN('sbi.chartengine.designer.prefixtext'),
			selectOnFocus: true,
			allowBlank: true,
			emptyText: LN("sbi.chartengine.structure.serieStyleConfig.serie.prefixText.emptyText")
		});
		
		this.serieFieldSet.add(this.seriePrefixCharTextField);
		
		
		var postfixChar = dataAtRow.get('seriePostfixChar'); 
		
		this.seriePostfixCharTextField = Ext.create('Ext.form.field.Text', {
			id: "seriePostfixCharTextField",
			name: 'name',
			value: (postfixChar && postfixChar.trim() != '') ? postfixChar.trim() : '',
			hidden: (chartLibrary == 'chartJs'),
			fieldLabel: LN('sbi.chartengine.designer.postfixtext'),
			selectOnFocus: true,
			allowBlank: true,
			emptyText: LN("sbi.chartengine.structure.serieStyleConfig.serie.postfixText.emptyText")
		});
		
		this.serieFieldSet.add(this.seriePostfixCharTextField);
		
		var globalScope = this;
		
		if(chartLibrary == 'chartJs') {
			/**
			 * Lines are commented by Danilo.
			 * 
			 * 'serieFormatOrPrecision' field is deprecated. Now we have only
			 * precision with prefix and postffix as optional parts of the format
			 * of the serie that is going to be represented.
			 */
//			this.serieFormatOrPrecision.setValue("precision");
//			this.serieFormatOrPrecision.setDisabled(true);
			
			globalScope.seriePrefixCharTextField.setValue("");
			globalScope.seriePostfixCharTextField.setValue("");
			
			globalScope.seriePrefixCharTextField.hide();
			globalScope.seriePostfixCharTextField.hide();
		}
		
		var serieTooltipColor = dataAtRow.get('serieTooltipColor') ? 
								dataAtRow.get('serieTooltipColor').replace('#', '') : 
								"";
		
		this.tooltipColor = Ext.create('Sbi.chart.designer.components.ColorPicker',{
			fieldLabel : LN('sbi.chartengine.designer.tooltip.color'),
			emptyText: LN('sbi.chartengine.configuration.seriestooltipcolor.emptyText'),
			labelWidth : LABEL_WIDTH,
			value: serieTooltipColor
		});
		
		this.tooltipFieldSet.add(this.tooltipColor);

		var serieTooltipBackgroundColor = 	dataAtRow.get('serieTooltipBackgroundColor') ? 
											dataAtRow.get('serieTooltipBackgroundColor').replace('#', '') : 
											"";
		
		this.tooltipBackgroundColor = Ext.create('Sbi.chart.designer.components.ColorPicker',{
			fieldLabel : LN('sbi.chartengine.designer.backgroundcolor'),
			emptyText: LN('sbi.chartengine.configuration.seriestooltipbackgroundcolor.emptyText'),
			labelWidth : LABEL_WIDTH,
			value: serieTooltipBackgroundColor
		});
		this.tooltipFieldSet.add(this.tooltipBackgroundColor);
		
		var serieTooltipAlign = dataAtRow.get('serieTooltipAlign');
		this.tooltipAlignComboBox = Ext.create('Sbi.chart.designer.FontAlignCombo', {
			value: (serieTooltipAlign && serieTooltipAlign.trim() != '') ? serieTooltipAlign.trim() : '',
			fieldLabel : LN('sbi.chartengine.designer.tooltip.align'),
		});
		this.tooltipFieldSet.add(this.tooltipAlignComboBox);
		
		
		var serieTooltipFont = dataAtRow.get('serieTooltipFont');
		this.tooltipFontComboBox = Ext.create('Sbi.chart.designer.FontCombo', {
			value: (serieTooltipFont && serieTooltipFont.trim() != '') ? serieTooltipFont.trim() : '',
			fieldLabel : LN('sbi.chartengine.axisstylepopup.font'),
		});
		this.tooltipFieldSet.add(this.tooltipFontComboBox);
		
		var serieTooltipFontWeight = dataAtRow.get('serieTooltipFontWeight');
		this.tooltipFontWeightStylesComboBox = Ext.create('Sbi.chart.designer.FontStyleCombo', {
			value: (serieTooltipFontWeight && serieTooltipFontWeight.trim() != '') ? serieTooltipFontWeight.trim() : '',
			fieldLabel : LN('sbi.chartengine.axisstylepopup.fontweight'),
		});
		this.tooltipFieldSet.add(this.tooltipFontWeightStylesComboBox);
		
		var serieTooltipFontSize = dataAtRow.get('serieTooltipFontSize');		
		this.tooltipFontSizeComboBox = Ext.create('Sbi.chart.designer.FontDimCombo', {
			value: (serieTooltipFontSize && serieTooltipFontSize.trim() != '') ? serieTooltipFontSize.trim() : '',
			fieldLabel : LN('sbi.chartengine.axisstylepopup.fontsize'),
		});
		this.tooltipFieldSet.add(this.tooltipFontSizeComboBox);
		
		console.log(dataAtRow);
		console.log(dataAtRow.get('serieTooltipBorderWidth'));
		console.log(dataAtRow.get('serieTooltipBorderRadius'));
		
		var serieTooltipBorderWidth = dataAtRow.get('serieTooltipBorderWidth');
		this.tooltipBorderWidthNumberfield = Ext.create('Ext.form.field.Number', {
			id: "serieTolltipBorderWidth",
			fieldLabel: LN('sbi.chartengine.configuration.serieStyleConf.tooltip.borderWidth'),
			hidden: chartType != 'CHORD',
			selectOnFocus: true,
			value: serieTooltipBorderWidth,
			maxValue: 10,
			minValue: 0,
			emptyText: LN("sbi.chartengine.structure.serieStyleConfig.tooltip.borderWidth.emptyText")
		});
		
		this.tooltipFieldSet.add(this.tooltipBorderWidthNumberfield);
		
		var serieTooltipBorderRadius = dataAtRow.get('serieTooltipBorderRadius');
		this.tooltipBorderRadiusNumberfield = Ext.create('Ext.form.field.Number', {
			id: "serieTolltipBorderRadius",
			fieldLabel: LN('sbi.chartengine.configuration.serieStyleConf.tooltip.borderRadius'),
			hidden: chartType != 'CHORD',
			selectOnFocus: true,
			value: serieTooltipBorderRadius,
			maxValue: 10,
			minValue: 0,
			emptyText: LN("sbi.chartengine.structure.serieStyleConfig.tooltip.borderRadius.emptyText")
		});
		
		this.tooltipFieldSet.add(this.tooltipBorderRadiusNumberfield);
		
		this.add(this.serieFieldSet);
		this.add(this.tooltipFieldSet);
		
//		if(chartType=="WORDCLOUD"){
//		  this.tooltipFieldSet.hide();	
//		}
	},
	
    writeConfigsAndExit: function() {
    	
    	/**
    	 * If there is some not valid entry, display the message in order
    	 * to inform the user about that.
    	 * 
    	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
    	 */
    	var errorMessages = "";
    	
		var dataAtRow = store.getAt(rowIndex);
		var serieName = this.serieNameTextField.getValue();
		var serieType = this.serieTypesComboBox.getValue();
		var serieOrder = this.serieOrderComboBox.getValue();
		var serieColor = this.serieColorPicker.getColor();
		var showValue = this.serieShowValue.getValue();
		var showAbsValue = this.serieShowAbsValue.getValue();
		var showPercentage = this.serieShowPercentage.getValue();
		var serieTooltipColor = this.tooltipColor.getColor();
		var serieTooltipBackgroundColor = this.tooltipBackgroundColor.getColor();
		var serieTooltipAlign = this.tooltipAlignComboBox.getValue();
		var serieTooltipFont = this.tooltipFontComboBox.getValue();
		var serieTooltipFontWeight = this.tooltipFontWeightStylesComboBox.getValue();
		var serieTooltipFontSize = '' + this.tooltipFontSizeComboBox.getValue(); //Save as string 
		
		var serieTooltipBorderWidth=this.tooltipBorderWidthNumberfield.getValue();
		
		var serieTooltipBorderRadius=this.tooltipBorderRadiusNumberfield.getValue();
		
		var seriePrecision = this.seriePrecisionNumberField.getValue();
		
		/**
		 * Validation for color elements inside the SerieStylePopup when closing event.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		var colorPicker = Sbi.chart.designer.components.ColorPicker;
		
		if (serieColor && serieColor!=null)
		{
			var serieColorTemp = (serieColor.indexOf("#")==0) ? serieColor.replace('#', '') : serieColor;
			
			if (!colorPicker.validateValue(serieColorTemp))
			{
				errorMessages += Sbi.locale.sobstituteParams
				(
					LN("sbi.chartengine.validation.structure.axisStyleConfPopupOpened.colorValuesInvalid"),
					
					[
					 	LN("sbi.chartengine.designer.color"),
					 	LN("sbi.chartengine.designer.series")						
					]
				);
			}
		}
		
		if (serieTooltipColor && serieTooltipColor!=null)
		{
			var serieTooltipColorTemp = (serieTooltipColor.indexOf("#")==0) ? serieTooltipColor.replace('#', '') : serieTooltipColor;
			
			if (!colorPicker.validateValue(serieTooltipColorTemp))
			{
				errorMessages += Sbi.locale.sobstituteParams
				(
					LN("sbi.chartengine.validation.structure.axisStyleConfPopupOpened.colorValuesInvalid"),
					
					[
					 	LN("sbi.chartengine.designer.tooltip.color"),
					 	LN("sbi.chartengine.designer.tooltip")						
					]
				);
			}
		}
		
		if (serieTooltipBackgroundColor && serieTooltipBackgroundColor!=null)
		{
			var serieTooltipBackgroundColorTemp = (serieTooltipBackgroundColor.indexOf("#")==0) ? serieTooltipBackgroundColor.replace('#', '') : serieTooltipBackgroundColor;
			
			if (!colorPicker.validateValue(serieTooltipBackgroundColorTemp))
			{
				errorMessages += Sbi.locale.sobstituteParams
				(
					LN("sbi.chartengine.validation.structure.axisStyleConfPopupOpened.colorValuesInvalid"),
					
					[
					 	LN("sbi.chartengine.designer.backgroundcolor"),
					 	LN("sbi.chartengine.designer.tooltip")						
					]
				);
			}
		}
		
		/**
		 * Validation for value of precision if this modality is picked.
		 */
		if (seriePrecision < 0)
		{
			errorMessages += Sbi.locale.sobstituteParams
			(
				LN("sbi.chartengine.structure.axisStylePopup.seriePrecisionLessThanMin"),
				
				[
				 	Ext.getCmp("seriePrecisionNumberField").minValue				
				]
			);
		}
		else
		{
			dataAtRow.set('seriePrecision', seriePrecision);
			
			var prefixChar = this.seriePrefixCharTextField.getValue();
			dataAtRow.set('seriePrefixChar', prefixChar);
			
			var postfixChar = this.seriePostfixCharTextField.getValue();
			dataAtRow.set('seriePostfixChar', postfixChar);
		}
		
		if (errorMessages!="")
		{
			Ext.Msg.show
			(
				{
					title:	Sbi.locale.sobstituteParams
					(
							LN("sbi.chartengine.validation.structure.axisAndSeriesStyleConfigPopup.messageWarning.headerTitle"),
							
							[
								LN("sbi.chartengine.designer.seriesstyleconf")
							]
					),
				
					message : errorMessages,
					icon : Ext.Msg.WARNING,
					closable : true,
					buttons : Ext.Msg.OK
				}
			);
		}			
		else
		{
			dataAtRow.set('axisName', serieName);
			dataAtRow.set('serieType', serieType);
			dataAtRow.set('serieOrderType', serieOrder);
			dataAtRow.set('serieColor', serieColor);
			dataAtRow.set('serieShowValue', showValue);
			dataAtRow.set('serieShowAbsValue', showAbsValue);
			dataAtRow.set('serieShowPercentage', showPercentage);
			dataAtRow.set('serieTooltipColor', serieTooltipColor);
			dataAtRow.set('serieTooltipBackgroundColor', serieTooltipBackgroundColor);
			dataAtRow.set('serieTooltipAlign', serieTooltipAlign);
			dataAtRow.set('serieTooltipFont', serieTooltipFont);
			dataAtRow.set('serieTooltipFontWeight', serieTooltipFontWeight);
			dataAtRow.set('serieTooltipFontSize', serieTooltipFontSize);
			dataAtRow.set('serieTooltipBorderWidth', serieTooltipBorderWidth);
			dataAtRow.set('serieTooltipBorderRadius', serieTooltipBorderRadius);
			this.destroy();
		}
		
	},
	
	items: [],
	// Cancel and Save buttons
    buttons: [{
        text: LN('sbi.generic.cancel'),
        handler: function(btn, elem2 ) {
			Ext.getCmp('serieStylePopup').destroy();
        }
    }, {
        text: LN('sbi.generic.save'),
        handler: function() {
			Ext.getCmp('serieStylePopup').writeConfigsAndExit();
        }
    }],
});