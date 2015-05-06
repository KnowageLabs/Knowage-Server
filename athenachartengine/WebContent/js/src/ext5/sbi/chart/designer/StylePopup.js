Ext.define('Sbi.chart.designer.StylePopup',{
	extend : 'Ext.form.Panel',
	width : 300,
	height : 180,
	floating : true,
	draggable : true,
	closable : true,
	closeAction : 'hide',
	bodyPadding : 10,
	items : [{
		xtype : 'fieldcontainer',
		layout : 'hbox',
		items : []
	}],			
	constructor: function(config) {
        this.callParent(config);
        var align = Ext.create('Sbi.chart.designer.FontAlignCombo');
		var color = Ext.create('Sbi.chart.designer.FontColor');
		var font = Ext.create('Sbi.chart.designer.FontCombo');
        var dim = Ext.create('Sbi.chart.designer.FontDimCombo');
        var style = Ext.create('Sbi.chart.designer.FontStyleCombo');
        
        this.add(align);
		this.add(color);
		this.add(font);
		this.add(dim);
		this.add(style);
    }
});