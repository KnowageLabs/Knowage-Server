Ext.define('Sbi.chart.designer.TypeLineCombo', {
    extend:'Ext.form.ComboBox',
    store: {
        fields: [ 'name','value' ],
        data: [ {
        	name : LN('sbi.chartengine.axisstylepopup.typeline.solid'),
        	value : 'solid'
    	}, {
			name : LN('sbi.chartengine.axisstylepopup.typeline.dashed'),
			value : 'dashed'
    	}, {
    		name : LN('sbi.chartengine.axisstylepopup.typeline.dotted'),
    		value : 'dotted'
    	}, ]
    },
    editable : false,
    displayField: 'name',
    valueField: 'value',
    fieldLabel : LN('sbi.chartengine.axisstylepopup.typeline'),
    queryMode : 'local',
});