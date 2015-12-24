Ext.define('Sbi.chart.designer.FontAlignCombo', {
    extend :'Ext.form.ComboBox',
    
    store: {
        fields: [ 'name','value' ],
        data: [ {
        	name : LN('sbi.chartengine.configuration.alignment.l'),
        	value : 'left'
		}, {
			name : LN('sbi.chartengine.configuration.alignment.c'),
			value : 'center'
		}, {
			name : LN('sbi.chartengine.configuration.alignment.r'),
			value : 'right'
		} ]
    },
    editable : false,
    displayField: 'name',
    valueField: 'value',
    fieldLabel : LN('sbi.chartengine.configuration.alignment'),
    queryMode : 'local',
    emptyText: LN("sbi.chartengine.configuration.alignment.emptyText"),
    
    /**
     * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
    width: Sbi.settings.chart.configurationStep.widthOfFields,
    
    /**
     * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
    listeners: 
	{
    	change: function(sender, newValue, oldValue, opts)
    	{
            if (newValue=="")	// empty by style
            	this.fireEvent("fontAlignEmpty");
            else
            	this.fireEvent("fontAlignPicked");
    	}
	}
});