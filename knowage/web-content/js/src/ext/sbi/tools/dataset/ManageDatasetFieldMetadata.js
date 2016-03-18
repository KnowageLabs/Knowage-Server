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
 * Authors - Alberto Ghedin
 */
Ext.ns("Sbi.tools");

Sbi.tools.ManageDatasetFieldMetadata = function(config) { 

	
	this.border =  false;
	this.frame = false;
	

	//Add to the dom the select used from the combo..
	//it is referenced by Id from the transform
	var selectElement = document.getElementById("fieldTypeSelect");
	if(!selectElement){
		var select = '<select name="fieldTypeSelect" id="fieldTypeSelect" style="display: none;">'+
    	'<option value="ATTRIBUTE">ATTRIBUTE</option>'+
    	'<option value="MEASURE">MEASURE</option>'+
    	'</select>';
		var bodyElement = document.getElementsByTagName('body');
		Ext.DomHelper.append(bodyElement[0].id, select );
	}

	this.fieldsColumns =  [
	    {
	    	header: LN('sbi.ds.field.name'), 
	    	//width: 140,  
			id:'name',
			sortable: true, 
			dataIndex: 'displayedName' 
	    },{
        	header: LN('sbi.ds.field.metadata'),
            dataIndex: 'fieldType',
           // width: 140, 
            editor: new Ext.form.ComboBox({
            	typeAhead: true,
                triggerAction: 'all',
                // transform the data already specified in html
                transform: 'fieldTypeSelect',
                lazyRender: true,
                listClass: 'x-combo-list-small'
            })
        }			
	];
    
	 var cm = new Ext.grid.ColumnModel({
	        columns: this.fieldsColumns
	    });
	 
	 this.fieldStore = new Ext.data.JsonStore({
		    id : 'name',
		    fields: ['displayedName','name', 'fieldType','type' ],
		    idIndex: 0,
		    data:{}
		});
	 
		 
		 var sm = new Ext.grid.RowSelectionModel({
	         singleSelect: true
	     });

	    // create the editor grid
	    var grid = {
	    	xtype: 'grid',
	        store: this.fieldStore,
	        layout: 'fit',
	        cm: cm,
	        sm: sm,
	        frame: false,
	        autoscroll: true,
	        viewConfig: {
	            forceFit: true
	        },
	        width:280
	    };

    var c = Ext.apply( {}, config, grid);

    // constructor
    Sbi.tools.ManageDatasetFieldMetadata.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.tools.ManageDatasetFieldMetadata, Ext.grid.EditorGridPanel, {
  
	fieldsColumns:null,
	emptyStore: true

  	,loadItems: function(fieldsColumns, record){
  		this.record = record;
  		if(fieldsColumns){
  			//Temporary workaround because fieldsColumns is now an object with a new structure after changing DataSetJSONSerializer
  			if ((fieldsColumns.columns != undefined) && (fieldsColumns.columns != null)){
  				var columnsArray = new Array();
  				
  				var columnsNames = new Array();
  				//create columns list
  				for (var i = 0; i < fieldsColumns.columns.length; i++) {
  					var element = fieldsColumns.columns[i];
  					columnsNames.push(element.column); 
  				}
  				
  				columnsNames = this.removeDuplicates(columnsNames);
  				
  				
  				for (var i = 0; i < columnsNames.length; i++) {
  					var columnObject = {displayedName:'', name:'',fieldType:'',type:''};
  					var currentColumnName = columnsNames[i];
  					//this will remove the part before the double dot if the column is in the format ex: it.eng.spagobi.Customer:customerId
  					if (currentColumnName.indexOf(":") != -1){
  					    var arr = currentColumnName.split(':');
  					     
  	  					columnObject.displayedName = arr[1];
  					} else {
  	  					columnObject.displayedName = currentColumnName;
  					}

  					columnObject.name = currentColumnName;
  					for (var j = 0; j < fieldsColumns.columns.length; j++) {
  	  					var element = fieldsColumns.columns[j];
  	  					if (element.column == currentColumnName){
  	  						if(element.pname.toUpperCase() == 'type'.toUpperCase()){
  	  							columnObject.type = element.pvalue;
  	  						}
  	  						else if(element.pname.toUpperCase() == 'fieldType'.toUpperCase()){
  	  							columnObject.fieldType = element.pvalue;
  	  						}
  	  					}
  					}
  					columnsArray.push(columnObject);
	  			}			
  				
  				this.fieldStore.loadData(columnsArray);
  				// end workaround ---------------------------------------------------
  			} else {
  	  			this.fieldStore.loadData(fieldsColumns);
  			}			
  			this.emptyStore = false;
  		}else{
  			this.emptyStore = true;
  		}
	}

	,removeDuplicates: function(array) {
	    var index = {};
	   
	    for (var i = array.length - 1; i >= 0; i--) {
	        if (array[i] in index) {
	            // remove this item
	            array.splice(i, 1);
	        } else {
	            // add this value to index
	            index[array[i]] = true;
	        }
	    }
	    return array;
	}

	,getValues: function(){
		var data = this.fieldStore.data.items;
		var values =[];
		for(var i=0; i<data.length; i++){
			values.push(data[i].data);
		}
		return values;
	}

	,updateRecord: function(){

		this.record.data.meta = this.getValues();
	}

});

