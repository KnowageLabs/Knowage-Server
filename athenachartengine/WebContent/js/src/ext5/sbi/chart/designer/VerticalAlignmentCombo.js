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
	    
	    store: 
	    {
	        fields: [ 'name','value' ],
	       
	        data: 
        	[ 		    	
		    	{
		    		name : "Top",	// TODO: LN()
		    		value : 'top'
		    	}, 
		    	
		    	{
		    		name : "Middle", // TODO: LN()
		    		value : 'middle'
		    	}, 
		    	
		    	{
		    		name : "Bottom",// TODO: LN()
		    		value : 'bottom'
		    	} 
	    	]
	    },
	    
	    editable : false,
	    displayField: 'name',
	    valueField: 'value',
	    fieldLabel : "Vertical align", // TODO: LN()
	    queryMode : 'local',
	}
);