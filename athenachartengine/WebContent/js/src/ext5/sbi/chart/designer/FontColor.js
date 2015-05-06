Ext.define('Sbi.chart.designer.FontColor', {
	extend : 'Ext.container.Container',
	layout : 'hbox',
	margin: '5 0',
	items : [ ],

	constructor : function(config) {
		this.callParent(config);
		var picker = Ext.create('Sbi.chart.designer.ColorPicker');
		var field = Ext.create('Ext.form.Field',{
			readOnly : true,
			fieldLabel : 'Colore',
		});
		this.add(field);
		this.add(picker);
	}
});