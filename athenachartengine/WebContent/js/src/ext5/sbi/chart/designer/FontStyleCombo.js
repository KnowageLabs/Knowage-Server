Ext.define('Sbi.chart.designer.FontStyleCombo', {
    extend:'Ext.form.ComboBox',
    store: {
        fields: [ 'name','value' ],
        data: [ {
    		name : 'Bold',
    		value : 'bold'
    	}, {
    		name : 'Normal',
    		value : 'normal'
    	}, {
    		name : 'Italico',
    		value : 'italic'
    	}, {
    		name : 'Sottolineato',
    		value : 'underline'
    	} ]
    },
    editable : false,
    displayField: 'name',
    valueField: 'value',
    fieldLabel : 'Stile',
    queryMode : 'local',
});