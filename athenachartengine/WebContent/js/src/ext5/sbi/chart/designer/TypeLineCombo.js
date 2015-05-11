Ext.define('Sbi.chart.designer.TypeLineCombo', {
    extend:'Ext.form.ComboBox',
    store: {
        fields: [ 'name','value' ],
        data: [ {
        	name : 'Solid',
        	value : 'solid'
    	}, {
			name : 'Dashed',
			value : 'dashed'
    	}, {
    		name : 'Dotted',
    		value : 'dotted'
    	}, ]
    },
    editable : false,
    displayField: 'name',
    valueField: 'value',
    fieldLabel : 'Type line',
    queryMode : 'local',
});