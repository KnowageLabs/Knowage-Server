/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 
 
  

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
 * Authors - Chiara Chiarelli
 */
Ext.ns("Sbi.tools");

Sbi.tools.ParametersFillGrid = function(config) { 
		
	// create the editor grid
	    var grid = {
	    	xtype: 'grid',
	    	title:  LN('sbi.ds.fillPars'),
	        width: 450,
	        height: 250,
	        autoScroll: true,
	        source: config.pars,
	        forceLayout: true,
	        frame: true,
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
    Sbi.tools.ParametersFillGrid.superclass.constructor.call(this, c);
    
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

Ext.extend(Sbi.tools.ParametersFillGrid, Ext.grid.PropertyGrid, {
  
	fillParameters: function(parsList){

		//	remove preceding content
		this.store.removeAll(true);
	
		if(parsList){
			for(i = 0; i<parsList.length;i++){
				var singlePar = parsList[i];
				
				var valueToInsert = null;
			
				var typeData = singlePar.type;
				if(!typeData){
					typeData = 'STRING';
				}
				else{
					typeData = typeData.toUpperCase();
				}
				//convert value to the right type
				if(typeData == 'STRING'){
					if(valueToInsert == null){
						valueToInsert = '';
					}
				}else if (typeData == 'NUMBER'){
					if(valueToInsert == null){
						valueToInsert = parseFloat(0);	
					}
					else{
						valueToInsert = parseFloat(valueToInsert);
					}						
				} else {
					// case text
					if(valueToInsert == null){
						valueToInsert = '';
					}
				}
		
				var tempRecord = new Ext.data.Record({"name": singlePar.name,"type": typeData, "value": valueToInsert});		
				this.store.add(tempRecord);
			}
		}
	  this.getView().refresh();
	}
	
	// return array with values in grid
	,getParametersValues: function(){
	    var arrayPars = new Array();
			var storePars = this.getStore();
			var length = storePars.getCount();
			for(var i = 0;i< length;i++){
				var item = storePars.getAt(i);
				var data = item.data;
				arrayPars.push(data);
			}
		return arrayPars;
	}
	
});

