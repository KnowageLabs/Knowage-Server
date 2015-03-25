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
 * Authors - Chiara Chiarelli
 */
Ext.ns("Sbi.tools");

Sbi.tools.ManageDatasetParameters = function(config) { 
	
	this.typesStore = new Ext.data.SimpleStore({
        fields: ['type'],
        data: [['String'],['Number'],['Raw'],['Generic']],
        autoLoad: false
    });

	// Let's pretend we rendered our grid-columns with meta-data from our ORM framework.
	//these are grid values for range type threshold value
	this.userColumns =  [
	    {
	    	header: LN('sbi.generic.name'), 
	    	width: 230, 
			id:'name',
			sortable: true, 
			dataIndex: 'name',  
			editor: new Ext.form.TextField({
				 maxLength:20,
				 allowBlank: false,
	             validationEvent:true
			})
	    },{
			header: LN('sbi.generic.type'), 
			width: 220, 
			id:'type',
			sortable: true, 
			dataIndex: 'type',  		
			editor: new Ext.form.ComboBox({
	        	  name: 'type',
	              store: this.typesStore,
	              displayField: 'type',   // what the user sees in the popup
	              valueField: 'type',        // what is passed to the 'change' event
	              typeAhead: true,
	              forceSelection: true,
	              mode: 'local',
	              triggerAction: 'all',
	              selectOnFocus: true,
	              editable: false,
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
     	          , 'type'
      	          ],
		    data:{}
		});
	 
	 var tb = new Ext.Toolbar({
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
//	        layout: 'fit',
//	        autoHeight: true,
	        autoScroll: true,
	        cm: cm,
	        sm: sm,
	        width: 370,
	        height: 200, //120,
	        //autoExpandColumn: 'label', // column with this id will be expanded
	        frame: true,
	        clicksToEdit: 2,
	        tbar: tb,
	        boxMaxHeight: 200,
	        boxMinHeight: 100
	    };

    var c = Ext.apply( {}, config, grid);

    // constructor
    Sbi.tools.ManageDatasetParameters.superclass.constructor.call(this, c);
    
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

Ext.extend(Sbi.tools.ManageDatasetParameters, Ext.grid.EditorGridPanel, {
  
	
  	currentRowRecordEdited:null
  	,store:null
  	,userColumns:null

  	,loadItems: function(pars){
		this.store.loadData(pars);
	}

    ,onAdd: function (btn, ev) {
        var emptyRecToAdd = new Ext.data.Record({
              type: 'String'     
			 });   
        this.store.insert(0,emptyRecToAdd);
        //this.refreshHeightOnAdd();
    }
    ,onDelete: function() {   	
        var rec = this.getSelectionModel().getSelected();
        this.store.remove(rec);
        this.store.commitChanges();
        //this.refreshHeightOnDelete();
     }
    , refreshHeightOnAdd: function(){
    	var currHeight = this.getHeight();
        var numero = this.store.getCount();
    	var newHeight = currHeight;
    	if(currHeight<=150){
    		newHeight+=20;
    	}
    	this.setHeight(newHeight);
    }
    , refreshHeightOnDelete: function(){
    	var currHeight = this.getHeight();
    	var newHeight = currHeight;
        var numero = this.store.getCount();

        if(currHeight>120 && numero <4){
    		newHeight-=22;
    	}
    	this.setHeight(newHeight);
    }
    ,getParsArray: function(){
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

