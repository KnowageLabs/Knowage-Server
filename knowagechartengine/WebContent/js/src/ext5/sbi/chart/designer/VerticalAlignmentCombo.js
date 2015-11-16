/**
 * Combobox for items that are related to position of the vertical alignment
 * of item that uses it. Vertical alignment can be either top, bottom or
 * center.
 * 
 * @author: danristo (danilo.ristovski@mht.net)
 */

Ext.define
(
	'Sbi.chart.designer.VerticalAlignmentCombo', 
	
	{
	    extend:'Ext.form.ComboBox',
	    
	    config:
	    {
	    	isMandatory: this.isMandatory
	    },
	    
	    store: 
	    {
	        fields: [ 'name','value' ],
	       
	        data: 
        	[ 		    	
		    	{
		    		name : LN("sbi.chartengine.configuration.title.verticalAlignCombo.top"),	
		    		value : 'top'
		    	}, 
		    	
		    	{
		    		name : LN("sbi.chartengine.configuration.title.verticalAlignCombo.middle"),
		    		value : 'middle'
		    	}, 
		    	
		    	{
		    		name : LN("sbi.chartengine.configuration.title.verticalAlignCombo.bottom"),
		    		value : 'bottom'
		    	} 
	    	]
	    },
	    
	    editable : false,
	    displayField: 'name',
	    valueField: 'value',
	    fieldLabel : LN("sbi.chartengine.configuration.title.verticalAlignCombo"), 
	    queryMode : 'local',
	}
);