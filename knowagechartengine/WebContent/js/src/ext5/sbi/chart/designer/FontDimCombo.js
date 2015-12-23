Ext.define('Sbi.chart.designer.FontDimCombo', {
    extend :'Ext.form.ComboBox',
    store: {
        fields: [ 'name' ],
        data: [['8px'],['9px'],['10px'],['11px'],['12px'],
               ['14px'],['16px'],['18px'],['20px'],['22px'],
               ['24px'],['26px'],['28px'],['36px'],['48px'],['72px']]
    },
    editable : false,
    displayField : 'name',
    valueField : 'name',
    fieldLabel : LN('sbi.chartengine.configuration.fontsize'),
    emptyText: LN("sbi.chartengine.configuration.fontSize.emptyText"),
    
    /**
     * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
    width: Sbi.settings.chart.configurationStep.widthOfFields,
    
    listeners : 
    {
        change: function(sender, newValue, oldValue, opts) 
        {    
            if (newValue=="")	// empty by style
            	this.fireEvent("fontSizeEmpty");
            else
            	this.fireEvent("fontSizePicked");        	
        }
    }
});