Ext.define('Sbi.chart.designer.StylePopup',{
	extend : 'Ext.form.Panel',
    //session: true,
	width : 300,
	height : 180,
	floating : true,
	draggable : true,
	closable : true,
	closeAction : 'hide',
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
        
        var align = Ext.create('Sbi.chart.designer.FontAlignCombo',{
        	bind : this.config.bindFontAlign
        });
		var color = Ext.create('Sbi.chart.designer.ColorPickerContainer',{    		
			viewModel: this.viewModel,
			fieldBind : this.config.bindColor
		});
		var font = Ext.create('Sbi.chart.designer.FontCombo',{
			fieldBind : this.config.bindFont
		});
        var dim = Ext.create('Sbi.chart.designer.FontDimCombo',{
        	fieldBind : this.config.bindFontDim
        });
        var style = Ext.create('Sbi.chart.designer.FontStyleCombo',{
        	fieldBind : this.config.bindFontStyle
        });
        
        this.add(align);
		this.add(color);
		this.add(font);
		this.add(dim);
		this.add(style);
    }
});