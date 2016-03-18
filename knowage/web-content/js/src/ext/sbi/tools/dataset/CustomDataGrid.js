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
 * Authors - Giulio Gavardi
 */
Ext.ns("Sbi.tools.dataset");

Sbi.tools.dataset.CustomDataGrid = function(config) { 
	
	// Let's pretend we rendered our grid-columns with meta-data from our ORM framework.
	//these are grid values for range type threshold value
	this.userColumns =  [
	    {
	    	header: LN('sbi.generic.name'), 
	    	width: 250, 
			id:'name',
			sortable: true, 
			dataIndex: 'name',  
			editor: new Ext.form.TextField({
				 maxLength:20,
				 allowBlank: false,
	             validationEvent:true
			})
	    },
	    {
	    	header: LN('sbi.generic.value'), 
	    	width: 400, //250, 
			id:'value',
			sortable: true, 
			dataIndex: 'value',  
			editor: new Ext.form.TextField({
				 maxLength:20,
				 allowBlank: false,
	             validationEvent:true
			})
	    }
	];
    
	 var cm = new Ext.grid.ColumnModel({
	        columns: this.userColumns
	    });
	 
	 this.store = new Ext.data.JsonStore({
		    fields: ['name'
     	          , 'value'
      	          ],
		    data:{}
		});
	 
	 var tb = new Ext.Toolbar({
		 fieldLabel: 'CustomData',
		 //html: '<h1>'+LN('sbi.ds.customData')+'</h1>',
	    	buttonAlign : 'left',
	    	items:[new Ext.Toolbar.Button({
	            text: LN('sbi.attributes.add'),
	            iconCls: 'icon-add',
	            handler: this.onAdd,
	            width: 60,
	            scope: this
	        }), '-', new Ext.Toolbar.Button({
	            text: LN('sbi.attributes.delete'),
	            iconCls: 'icon-remove',
	            handler: this.onDelete,
	            width: 40,
	            scope: this
	        }), '-'
	    	]
	    });
	 
		 var sm = new Ext.grid.RowSelectionModel({
	         singleSelect: true
	     });

	    // create the editor grid
	    var grid = {
	    	xtype: 'grid',
	        store: this.store,
	        layout: 'fit',
	        cm: cm,
	        sm: sm,
	        width: '90%', //480,
	        height: 350, //180,
	        //autoExpandColumn: 'label', // column with this id will be expanded
	        frame: true,
	        clicksToEdit: 2,
	        tbar: tb,
	        maxHeight : 400, //230,
	        autoHeight : true,
	        collapsible : true,
	    	//fieldLabel : ''
	        title : LN('sbi.ds.customData')
	        //forceLayout: true
	    };

    var c = Ext.apply( {}, config, grid);

    // constructor
    Sbi.tools.dataset.CustomDataGrid.superclass.constructor.call(this, c);
    
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

Ext.extend(Sbi.tools.dataset.CustomDataGrid, Ext.grid.EditorGridPanel, {
  
	
  	currentRowRecordEdited:null
  	,store:null
  	,userColumns:null

  	,loadItems: function(pars){
		var tempArray = [];
		for ( var x in pars ){
			var key = x;
			var value = pars[key];
			tempArray.push({ "name" : key , "value" : value })
		}
		this.store.loadData(tempArray);
	}

    ,onAdd: function (btn, ev) {
        var emptyRecToAdd = new Ext.data.Record({
              //type: 'String'     
			 });   
        this.store.insert(0,emptyRecToAdd);
    }
    
    ,onDelete: function() {   	
        var rec = this.getSelectionModel().getSelected();
        this.store.remove(rec);
        this.store.commitChanges();
     }
    // no more used
//    ,getDataArray: function(){
//	    var arrayPars = new Array();
//			var storePars = this.getStore();
//			var length = storePars.getCount();
//			for(var i = 0;i< length;i++){
//				var item = storePars.getAt(i);
//				var data = item.data;
//				arrayPars.push(data);
//			}
//		return arrayPars;
//	}
    ,getDataString: function(){
    	var objToReturn = {};
		var storePars = this.getStore();
		var length = storePars.getCount();
		for(var i = 0;i< length;i++){
				var item = storePars.getAt(i);
				var data = item.data;
				objToReturn[data.name] = data.value;
				//arrayPars.push(data);
		}
		return objToReturn;
	}


});

