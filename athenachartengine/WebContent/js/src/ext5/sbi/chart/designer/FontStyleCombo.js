Ext.define('Sbi.chart.designer.FontStyleCombo', {
    extend:'Ext.form.ComboBox',
    store: {
        fields: [ 'name','value' ],
        data: [ {
    		name : LN('sbi.chartengine.configuration.fontstyle.nostyle'),
    		value : ''
    	}, {
    		name : LN('sbi.chartengine.configuration.fontstyle.b'),
    		value : 'bold'
    	}, {
    		name : LN('sbi.chartengine.configuration.fontstyle.n'),
    		value : 'normal'
    	}, {
    		name : LN('sbi.chartengine.configuration.fontstyle.i'),
    		value : 'italic'
    	}, {
    		name : LN('sbi.chartengine.configuration.fontstyle.u'),
    		value : 'underline'
    	} ]
    },
    editable : false,
    displayField: 'name',
    valueField: 'value',
    fieldLabel : LN('sbi.chartengine.configuration.fontstyle'),
    queryMode : 'local',
});