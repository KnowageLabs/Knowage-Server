Ext.define('Sbi.chart.designer.StylePopup',{
	extend : 'Ext.form.Panel',
    //session: true,
	width : 300,
	height : 180,
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
		bindFontStyle:null
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
        
		var color = Ext.create('Sbi.chart.designer.ColorPickerContainer',{    		
			viewModel: this.viewModel,
			fieldBind : this.config.bindColor,
			bind : this.config.bindColor
		});
		var font = Ext.create('Sbi.chart.designer.FontCombo',{
			viewModel: this.viewModel,
			bind : this.config.bindFont
		});
        var dim = Ext.create('Sbi.chart.designer.FontDimCombo',{
        	viewModel: this.viewModel,
        	bind : this.config.bindFontDim
        });
        var style = Ext.create('Sbi.chart.designer.FontStyleCombo',{
        	viewModel: this.viewModel,
        	bind : this.config.bindFontStyle
        });
        
		this.add(color);
		this.add(font);
		this.add(dim);
		this.add(style);
    }
});