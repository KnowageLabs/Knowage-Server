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
 * Authors - Monica Franceschini
 */

/*
 * Elements expected in the config parameter:
  
 *  config.manageListService: Service that returns the list of items
 *	config.saveItemService: Service that saves an item
 *	config.deleteItemService: Service that deletes an item
 *  Services Example:
    var paramsList = {MESSAGE_DET: "RESOURCES_LIST"};
	var paramsDel = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "RESOURCE_DELETE"};
	
	config.manageListService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_RESOURCES_ACTION'
		, baseParams: paramsList
	});


 *	config.fields: array of fields that are returned by the manageListService
 *  Example: ['id', 'name', 'code', 'description', 'typeCd']
    
 *	config.emptyRecToAdd: Empty record to add
 *  Example:
    new Ext.data.Record({id: 0,
						 name:'', 
						 code:'', 
						 description:'',
						 typeCd: '' });:
						 
 *	config.gridColItems: Columns to be put in the grid
 *  Example:
    [{id:'name',header: LN('sbi.generic.name'), width: 50, sortable: true, locked:false, dataIndex: 'name'},
     {header: LN('sbi.generic.code'), width: 150, sortable: true, dataIndex: 'code'}]	
     
 *  config.panelTitle: Title of the whole list-detail form
 *  config.listTitle: Title of the list
 *  Example:
    config.panelTitle = LN('sbi.generic.panelTitle');
    config.listTitle =  LN('sbi.generic.listTitle');  				 
 *  config.readonlyStrict : no buttons
 *  config.readonly : only inline select button
 *  config.idKey : identifier string for the row
 *  config.dragndropGroup : drag and drop group to apply
 *  config.referencedCmp : referenced componenet 
 */
Ext.ns("Sbi.widgets");

Sbi.widgets.ListGridPanel = function(config) {
	
	
	var conf = config.configurationObject;
	
	this.services = new Array();
	this.services['manageListService'] = conf.manageListService;
	this.services['deleteItemService'] = conf.deleteItemService;
	
	this.rowIdentificationString = conf.rowIdentificationString;
	this.gridColItems = conf.gridColItems;
	this.panelTitle = conf.panelTitle;
	this.listTitle = conf.listTitle;  	  
	this.idKeyForGrid = conf.idKey;
	this.ddGroup = conf.dragndropGroup;
	this.reference = conf.referencedCmp;
	this.drawSelectColumn = conf.drawSelectColumn; 
	this.readonly = config.readonly;
	this.readonlyStrict = config.readonlyStrict;
	this.addcopycolumn = config.addcopycolumn;
	this.singleSelection = config.singleSelection;
	this.filter = conf.filter;
	
	this.mainElementsStore = new Ext.data.JsonStore({
    	autoLoad: false    	  
    	, fields: conf.fields
    	, root: 'rows'
		, url: this.services['manageListService']		
	});
	this.initWidget();

	this.mainElementsStore.load();	
   	
	var c = Ext.apply({}, config, this.mainGrid);
   	
   	Sbi.widgets.ListGridPanel.superclass.constructor.call(this,c);	
  //to be addedd at the end!
   	this.mainGrid.addEvents('selected');	
   //	if(this.addcopycolumn){
   		this.mainGrid.addEvents('copy');	
   //	}
};

Ext.extend(Sbi.widgets.ListGridPanel, Ext.grid.GridPanel, {
	
	 panelTitle: null
	, listTitle: null
	, mainElementsStore:null
	, colModel:null
	, gridColItems: null
	, mainGrid: null
	, rowselModel:null
	, idKeyForGrid : 'id'
	, ddGroup : null
	, reference : null
	, readonly: null
	, readonlyStrict: null
	, addcopycolumn : null
	, singleSelection : true
	, rowIdentificationString: this
	
	,initWidget: function(){
	
	    this.selectColumn = new Ext.grid.ButtonColumn({
		       header:  ' '
		       ,iconCls: 'icon-select'
		       ,scope: this
		       ,clickHandler: function(e, t) {
		          var index = this.grid.getView().findRowIndex(t);	          
		          var selectedRecord = this.grid.store.getAt(index);
		          var itemId = selectedRecord.get(this.rowIdentificationString);
		          this.grid.fireEvent('selected', selectedRecord);
		       }
	    	   ,tooltip: LN('sbi.generic.select')	    		   
		       ,width: 25
		       ,renderer : function(v, p, record){
		           return '<center><img class="x-mybutton-'+this.id+' grid-button ' +this.iconCls+'" width="16px" height="16px" src="'+Ext.BLANK_IMAGE_URL+'"/></center>';
		       }
	     }); 
        
        this.deleteColumn = new Ext.grid.ButtonColumn({
 	       header:  ' '
 	       ,iconCls: 'icon-remove'
 	       ,scope: this
 	       ,initialConfig: this.idKeyForGrid
 	       ,clickHandler: function(e, t) {   
        	//this.grid is called since this is the only name that can be used. Look at ButtonColumn.js
 	          var index = this.grid.getView().findRowIndex(t);	
        	  var selectedRecord =	this.grid.getSelectionModel().getSelected();
 	          var itemId = selectedRecord.get(this.initialConfig);
 	          this.grid.fireEvent('delete', itemId, index);
 	       }
 	       ,width: 25
 	       ,tooltip: LN('sbi.generic.delete')	
 	       ,renderer : function(v, p, record){
 	           return '<center><img class="x-mybutton-'+this.id+' grid-button ' +this.iconCls+'" width="16px" height="16px" src="'+Ext.BLANK_IMAGE_URL+'"/></center>';
 	       }
         });

        this.copyColumn = new Ext.grid.ButtonColumn({
		       header:  ' '
		       ,iconCls: 'icon-copytree'
		       ,scope: this
		       ,clickHandler: function(e, t) {
		          var index = this.grid.getView().findRowIndex(t);	          
		          var selectedRecord = this.grid.store.getAt(index);
		          var itemId = selectedRecord.get(this.rowIdentificationString);
		          this.grid.fireEvent('copy', selectedRecord);
		       }
	    	   ,tooltip: LN('sbi.modelinstances.copyalltree')	    		   
		       ,width: 25
		       ,renderer : function(v, p, record){
		           return '<center><img class="x-mybutton-'+this.id+' grid-button ' +this.iconCls+'" width="16px" height="16px" src="'+Ext.BLANK_IMAGE_URL+'"/></center>';
		       }

          });	

        if(this.readonly){
        	this.gridColItems.push(this.selectColumn); 
        	if(this.addcopycolumn){
        		this.gridColItems.push(this.copyColumn); 
        	}
        }else{
        	if(!this.readonlyStrict){
        		this.gridColItems.push(this.deleteColumn); 
        	}        	 
        }
        this.colModel = new Ext.grid.ColumnModel(this.gridColItems);

        
        this.toolbarAddBtn = new Ext.Toolbar.Button({
 	            text: LN('sbi.generic.add'),
 	            iconCls: 'icon-add',
 	            handler: this.addNewItem,
 	            width: 30,
 	            ref : this.renference,
 	            scope: this
 	            });
        
 	    this.tb = new Ext.Toolbar({
 	    	buttonAlign : 'right',
 	    	items:[this.toolbarAddBtn]
 	    });
 	    
 	   
 	   var pagingBar = new Ext.PagingToolbar({
	        pageSize: 16,
	        store: this.mainElementsStore,
	        displayInfo: true,
	        displayMsg: '', 
	        scope: this,
	        emptyMsg: "No topics to display"	        
	    }); 	   
 	  
 	   var pluginsToAdd;
 	   if(this.readonly){
 		   	if(this.addcopycolumn){
 		   		pluginsToAdd = [this.selectColumn, this.copyColumn];
        	}else{
        		pluginsToAdd = [this.selectColumn];
        	}
        }else{
        	if(!this.readonlyStrict){
        		pluginsToAdd = this.deleteColumn; 
        	}
        	
        }
 	   
 	  this.rowselModel = new Ext.grid.RowSelectionModel({
           singleSelect: this.singleSelection
       });
 	  
 	  var filteringToolbar = new Sbi.widgets.FilteringToolbarLight(
 			  		{store: this.mainElementsStore,
		 			columnName:[['name',LN('sbi.generic.name')]],
			   		cls: 'no-pad',
			   		columnValue: this.gridColItems[0].dataIndex});
	  if(this.filter === undefined || ! this.filter){
		 filteringToolbar.setVisible(false);
	  }

 	  this.mainGrid = new Ext.grid.GridPanel({
	                  ds: this.mainElementsStore,   	                  
	                  colModel: this.colModel,
	                  plugins: pluginsToAdd ,
	                  selModel: this.rowselModel,
	                  layout: 'fit',
	                  scope: this,
	                  title: this.listTitle,
		              bbar: pagingBar,
	                  tbar: this.tb,
	                  fbar : [filteringToolbar],
	                  stripeRows: false,
	                  enableDragDrop: true,
	                  ddGroup: this.ddGroup,
	                  footerStyle:'background-color: #D0D0D0; padding: 0; margin: 0; border: 0px;',
	                  listeners: {
   							'delete': {
					     		fn: this.deleteSelectedItem,
					      		scope: this
					    	} ,
					    	'select': {
					     		fn: this.sendSelectedItem,
					      		scope: this
					    	} ,
                  			viewready: function(g) {//g.getSelectionModel().selectRow(0); 
					    		
					    	} 
                         }
	                  });
 	   if(this.readonly || this.readonlyStrict){
 		  this.tb.setVisible(false);
 		  this.toolbarAddBtn.setVisible(false);
       }



	}

	, addNewItem : function(){
		alert('Add method Needs to be written');
	}
	
	, deleteSelectedItem: function(itemId, index) {
		alert('Delete method Needs to be written');
	}
	
	//METHOD TO BE OVERRIDDEN IN EXTENDED ELEMENT!!!!!
	,save : function() {		
		alert('Abstract Method: it needs to be overridden');
    }

});
