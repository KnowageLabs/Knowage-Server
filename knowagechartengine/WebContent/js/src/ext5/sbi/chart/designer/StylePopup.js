Ext.define('Sbi.chart.designer.StylePopup',{
	extend : 'Ext.form.Panel',
	floating : true,
	draggable : true,
	closable : true,
	closeAction : 'hide',
    modal: true,
	bodyPadding : 10,
	config : {
		bindFontAlign:null,
		bindColor:null,
		bindFont:null,
		bindFontDim:null,
		bindFontStyle:null,
		bindBorderWidth:null,
		bindBackgroundColor:null,
		isFontAlignMandatory: false,
		isFontSizeMandatory: false,
		isFontFamilyMandatory: false,
		isFontStyleMandatory: false,
		isBorderWidthMandatory: false,
		isFontColorMandatory: false,
		isBackgroundColorMandatory: false
	},
	
	items : [],
	constructor: function(config) {
        this.callParent(config);
        this.viewModel = config.viewModel;
        this.title = config.title && config.title != null ? config.title: this.title;
        Ext.apply(this.config,config);
        
        var globalScope = this;
        
        if(this.config.bindFontAlign) {
        	var align = Ext.create('Sbi.chart.designer.FontAlignCombo',{
        		viewModel: this.viewModel,
        		bind : this.config.bindFontAlign,
        		fieldLabel : (globalScope.config.isFontAlignMandatory) ?  
        				LN('sbi.chartengine.configuration.title.alignment') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
        				: LN('sbi.chartengine.configuration.title.alignment'),
        	});
        	this.add(align);
        }
        
		var font = Ext.create('Sbi.chart.designer.FontCombo',{
			viewModel: this.viewModel,
			bind : this.config.bindFont,
			fieldLabel : (globalScope.config.isFontFamilyMandatory) ?  
					LN('sbi.chartengine.configuration.font') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
    				: LN('sbi.chartengine.configuration.font')
		});
		this.add(font);

		var dim = Ext.create('Sbi.chart.designer.FontDimCombo',{
        	viewModel: this.viewModel,
        	bind : this.config.bindFontDim,
        	fieldLabel : (globalScope.config.isFontSizeMandatory) ?  
        			LN('sbi.chartengine.configuration.fontsize') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
    				: LN('sbi.chartengine.configuration.fontsize')
        });
		this.add(dim);
        
		/**
		 * Added for the Heatmap (danristo :: danilo.ristovski@mht.net) 
		 */
		if(this.config.bindFontAlign) {
		var style = Ext.create('Sbi.chart.designer.FontStyleCombo',{
        	viewModel: this.viewModel,
        	bind : this.config.bindFontStyle,
        	fieldLabel : (globalScope.config.isFontStyleMandatory) ?  
        			LN('sbi.chartengine.configuration.fontstyle') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
    				: LN('sbi.chartengine.configuration.fontstyle')
        });
		this.add(style);
		}
		
		if(this.config.bindBorderWidth){
			var borderWidth = Ext.create('Ext.form.field.Number',{
				fieldLabel : LN('sbi.chartengine.configuration.borderwidth'),
				viewModel: this.viewModel,
				bind:  this.config.bindBorderWidth,
				emptyText: LN("sbi.chartengine.configuration.legendBorderWith.emptyText"),
	        	fieldLabel : (globalScope.config.isBorderWidthMandatory) ?  
	        			LN('sbi.chartengine.configuration.borderwidth') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
	    				: LN('sbi.chartengine.configuration.borderwidth')
			})
			this.add(borderWidth);
		};

		var color = Ext.create('Sbi.chart.designer.ColorPickerContainer',{    		
			viewModel: this.viewModel,
			fieldBind : this.config.bindColor,
			bind : this.config.bindColor,
			isColorMandatory: this.config.isFontColorMandatory
		});
		this.add(color);
		
		if(this.config.bindBackgroundColor){
			var bkgrColor = Ext.create('Sbi.chart.designer.ColorPickerContainer',{    		
				viewModel: this.viewModel,
				fieldBind : this.config.bindBackgroundColor,
				//customLabel : LN('sbi.chartengine.configuration.backgroundcolor'),
	        	fieldLabel : (globalScope.config.isBackgroundColorMandatory) ?  
	        			LN('sbi.chartengine.configuration.backgroundcolor') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
	    				: LN('sbi.chartengine.configuration.backgroundcolor')
			});
			this.add(bkgrColor);
		}
    }
});