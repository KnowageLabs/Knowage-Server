Ext.define('Sbi.chart.designer.FontCombo',{
    extend : 'Ext.form.ComboBox',
    
    store: {
        fields: [ 'name' ],
        
        sorters: [{
            property: 'name',
            direction: 'ASC'
        }],
        
        data: [ [ 'Arial' ], [ 'Times New Roman' ], [ 'Tahoma' ], [ 'Verdana' ], ["Impact"],
        		["Calibri"], ["Cambria"], ["Georgia"], ["Gungsuh"] ]
    },
    displayField : 'name',
    valueField : 'name',
    fieldLabel : LN('sbi.chartengine.configuration.font'),
    tdCls : '',
    editable : false,
    emptyText: LN("sbi.chartengine.configuration.fontFamily.emptyText"),
    
    listeners : {
        change: function(sender, newValue, oldValue, opts) {
        	
            this.inputEl.setStyle('font-family', newValue);
            
            /**
             * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
             */
            if (newValue=="")	// empty by style
            	this.fireEvent("fontFamilyEmpty");
            else
            	this.fireEvent("fontFamilyPicked");
        }
    }
});