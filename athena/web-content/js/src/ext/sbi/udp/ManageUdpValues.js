/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  

/**
 * Object name
 * 
 * [description]
 * 
 * 
 * Public Properties
 * 
 * [list]
 * 
 * 
 * Public Methods
 * 
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors - Giulio Gavardi
 */
Ext.ns("Sbi.kpi");

Sbi.kpi.ManageUdpValues = function(config) { 
		
	// create the editor grid
	    var grid = {
	    	xtype: 'grid',
	        width: 380,
	        autoHeight: true,
	        source: config.udpEmptyList,
	        forceLayout: true,
	        deferRowRender : false,
	        propertyNames: {
	            tested: 'QA',
	            borderWidth: 'Border Width'
	        },
	        viewConfig : {
	            forceFit: true,
	            scrollOffset: 2 // the grid will never have scrollbars
	        }
	        };

    var c = Ext.apply( {}, config, grid);
    
    // constructor
    Sbi.kpi.ManageUdpValues.superclass.constructor.call(this, c);
    
    this.on('beforeedit', function(e) {
    	var t = Ext.apply({}, e);
    	var col = t.column;
    	this.currentRowRecordEdited = t.row;	
    	
    }, this);
    
    this.on('afteredit', function(e) {
    	
    	var col = e.column;
    	var row = e.row;	
    	
    }, this);

};

Ext.extend(Sbi.kpi.ManageUdpValues, Ext.grid.PropertyGrid, {
  
	// fills value if present: if not present set defaults type in order to have right editors
	fillUdpValues:function(udpValues){

		//	remove preceding content
		this.store.removeAll(true);
		// get selected kpi udp values
	
		var udpEmptyList = this.udpEmptyList;
		var udpList = this.udpList;
		
		// if not defined give a default
		if(!udpValues){
			udpValues = new Array();
		}
		
		if(udpList){
		// fill udp st if present with values associated		
		for(i = 0; i<udpList.length;i++){
			var udp = udpList[i];
			// check if udp.label has a value associated for present KPI
			// check if udo.label is present among udpValues
			var valueToInsert = null;
			var stop = false;
			for ( var j = 0; j < udpValues.length && stop == false; j++) {
				var udpVal = udpValues[j];
				if(udpVal.label == udp.label || udpVal.name == udp.label){
					valueToInsert = udpVal.value;					
					stop = true;
				}
			}
		
			// get UDP type
			var typeData = udp.dataTypeCd;
			if(!typeData){
				typeData = 'TEXT';
			}
			else{
				typeData = typeData.toUpperCase();
			}
			//convert value to th right type
			if(typeData == 'INTEGER'){
					if(valueToInsert == null){
						valueToInsert = parseFloat(0);	
					}
					else{
						valueToInsert = parseFloat(valueToInsert);
					}
			}else if (typeData == 'BOOLEAN'){
				var boolToInsert;
				if(valueToInsert === 'true' || valueToInsert === true){
				    boolToInsert = true;
				}else{
					boolToInsert = false;
				}
				valueToInsert = boolToInsert;						
			} else {
				// case text
				if(valueToInsert == null){
					valueToInsert = '';
				}
			}
	
			var tempRecord = new Ext.data.Record({"name": udp.label,"value": valueToInsert});		
			this.store.add(tempRecord);
		}
		this.doLayout();
	  }
	}
	,
	// return array with values in grid
	saveUdpValues:function(type){
	     var arrayUdps = new Array();
			var storeUdps = this.getStore();
			for(var i = 0;i< storeUdps.getCount();i++){
				var item = storeUdps.getAt(i);
				var data = item.data;
				// want to add type and family to this record
				data.familyId = type;
				var stop = false;
				for ( var j = 0; j < this.udpList.length && stop == false; j++) {
					var udpVal = this.udpList[j];
					if(udpVal.label == data.name){
						data.dataTypeCd = udpVal.dataTypeCd;
						stop = true;
					}
				}
				arrayUdps.push(data);
			}
		return arrayUdps;
	}
	, clearGrid: function(){
		this.fillUdpValues(new Array());
	}
});

