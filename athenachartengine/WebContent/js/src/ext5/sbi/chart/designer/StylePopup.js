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
		bindBackgroundColor:null
	},
	
	items : [],
	constructor: function(config) {
        this.callParent(config);
        this.viewModel = config.viewModel;
        this.title = config.title && config.title != null ? config.title: this.title;
        Ext.apply(this.config,config);
        
        if(this.config.bindFontAlign) {
        	var align = Ext.create('Sbi.chart.designer.FontAlignCombo',{
        		viewModel: this.viewModel,
        		bind : this.config.bindFontAlign
        	});
        	this.add(align);
        }
        
		var font = Ext.create('Sbi.chart.designer.FontCombo',{
			viewModel: this.viewModel,
			bind : this.config.bindFont
		});
		this.add(font);

		var dim = Ext.create('Sbi.chart.designer.FontDimCombo',{
        	viewModel: this.viewModel,
        	bind : this.config.bindFontDim
        });
		this.add(dim);
        
		var style = Ext.create('Sbi.chart.designer.FontStyleCombo',{
        	viewModel: this.viewModel,
        	bind : this.config.bindFontStyle
        });
		this.add(style);
		
		if(this.config.bindBorderWidth){
			var borderWidth = Ext.create('Ext.form.field.Number',{
				fieldLabel : LN('sbi.chartengine.configuration.borderwidth'),
				viewModel: this.viewModel,
				bind:  this.config.bindBorderWidth
			})
			this.add(borderWidth);
		};

		var color = Ext.create('Sbi.chart.designer.ColorPickerContainer',{    		
			viewModel: this.viewModel,
			fieldBind : this.config.bindColor,
			bind : this.config.bindColor
		});
		this.add(color);
		
		if(this.config.bindBackgroundColor){
			var bkgrColor = Ext.create('Sbi.chart.designer.ColorPickerContainer',{    		
				viewModel: this.viewModel,
				fieldBind : this.config.bindBackgroundColor,
				customLabel : LN('sbi.chartengine.configuration.backgroundcolor')
			});
			this.add(bkgrColor);
		}
    }
});