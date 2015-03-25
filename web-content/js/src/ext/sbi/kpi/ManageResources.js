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
 * Authors - Chiara Chiarelli (chiara.chiarelli@eng.it)
 */
Ext.ns("Sbi.kpi");

Sbi.kpi.ManageResources = function(config) {
	 
	var paramsList = {MESSAGE_DET: "RESOURCES_LIST"};
	var paramsSave = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "RESOURCE_INSERT"};
	var paramsDel = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "RESOURCE_DELETE"};
	
	this.configurationObject = {};
	
	this.configurationObject.manageListService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_RESOURCES_ACTION'
		, baseParams: paramsList
	});
	this.configurationObject.saveItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_RESOURCES_ACTION'
		, baseParams: paramsSave
	});
	this.configurationObject.deleteItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_RESOURCES_ACTION'
		, baseParams: paramsDel
	});
	
	this.initConfigObject();
	config.configurationObject = this.configurationObject;
	config.singleSelection = true;
	
	var c = Ext.apply({}, config || {}, {});

	Sbi.kpi.ManageResources.superclass.constructor.call(this, c);	 
	
	this.rowselModel.addListener('rowselect',function(sm, row, rec) { 
		this.getForm().loadRecord(rec);  
     }, this);
};

Ext.extend(Sbi.kpi.ManageResources, Sbi.widgets.ListDetailForm, {
	
	configurationObject: null
	, gridForm:null
	, mainElementsStore:null

	,initConfigObject:function(){
	    this.configurationObject.fields = ['id'
		                     	          , 'name'
		                    	          , 'code'
		                    	          , 'description'   
		                    	          , 'tablename' 
		                    	          , 'columnname' 
		                    	          , 'typeCd'
		                    	          ];
		
		this.configurationObject.emptyRecToAdd = new Ext.data.Record({
										  id: 0,
										  name:'', 
										  code:'', 
										  description:'',
										  tablename:'',
										  columnname:'',
										  typeCd: ''
										 });
		
		this.configurationObject.gridColItems = [
		                                         {id:'name',header: LN('sbi.generic.name'), width: 160, sortable: true, locked:false, dataIndex: 'name'},
		                                         {header: LN('sbi.generic.code'), width: 160, sortable: true, dataIndex: 'code'},
		                                         {header: LN('sbi.generic.type'), width: 90, sortable: true, dataIndex: 'typeCd'}
		                                        ];
		
		this.configurationObject.panelTitle = LN('sbi.resources.panelTitle');
		this.configurationObject.listTitle = LN('sbi.resources.listTitle');
		
		this.initTabItems();
    }

	,initTabItems: function(){

		//Store of the combobox
 	    this.typesStore = new Ext.data.SimpleStore({
 	        fields: ['typeCd'],
 	        data: config.nodeTypesCd,
 	        autoLoad: false
 	    });
 	    
 	   //START list of detail fields
 	   var detailFieldId = {
               name: 'id',
               hidden: true
           };
 		   
 	   var detailFieldName = {
          	 maxLength:40,
        	 minLength:1,
        	 //regex : new RegExp("^([a-zA-Z1-9_\x2F])+$", "g"),
        	 regexText : LN('sbi.roles.alfanumericString'),
             fieldLabel: LN('sbi.generic.name'),
             allowBlank: false,
             validationEvent:true,
             name: 'name'
         };
 			  
 	   var detailFieldCode = {
          	 maxLength:45,
        	 minLength:1,
        	//regex : new RegExp("^([A-Za-z0-9_])+$", "g"),
        	 regexText : LN('sbi.roles.alfanumericString2'),
             fieldLabel:LN('sbi.generic.code'),
             allowBlank: false,
             validationEvent:true,
             name: 'code'
         };	  
 		   
 	   var detailFieldDescr = {
          	 maxLength:400,
          	 xtype: 'textarea',
       	     width : 300,
             height : 150,
        	 regexText : LN('sbi.roles.alfanumericString'),
             fieldLabel: LN('sbi.generic.descr'),
             validationEvent:true,
             name: 'description'
         };
 	 		   
 	   var detailFieldTabName = {
          	 maxLength:40,
        	// regex : new RegExp("^([a-zA-Z1-9_\x2F])+$", "g"),
        	 regexText : LN('sbi.roles.alfanumericString'),
             fieldLabel: LN('sbi.resources.tablename'),
             validationEvent:true,
             name: 'tablename'
         };
 	 	 			  
 	   var detailFieldColName = {
          	 maxLength:40,
        	// regex : new RegExp("^([a-zA-Z1-9_\x2F])+$", "g"),
        	 regexText : LN('sbi.roles.alfanumericString'),
             fieldLabel: LN('sbi.resources.columnname'),
             validationEvent:true,
             name: 'columnname'
         }; 
 		   
 	   var detailFieldNodeType =  {
        	  name: 'typeCd',
              store: this.typesStore,
              width : 150,
              fieldLabel: LN('sbi.generic.type'),
              displayField: 'typeCd',   // what the user sees in the popup
              valueField: 'typeCd',        // what is passed to the 'change' event
              typeAhead: true,
              forceSelection: true,
              mode: 'local',
              triggerAction: 'all',
              selectOnFocus: true,
              editable: false,
              allowBlank: false,
              validationEvent:true,
              xtype: 'combo'
          };  
 	  //END list of detail fields

 	   this.configurationObject.tabItems = [{
		        title: LN('sbi.generic.details')
		        , itemId: 'detail'
		        , width: 430
		        , items: {
			   		 id: 'items-detail',   	
		 		   	 itemId: 'items-detail',   	              
		 		   	 columnWidth: 0.4,
		             xtype: 'fieldset',
		             labelWidth: 110,
		             defaults: {width: 250, border:false},    
		             defaultType: 'textfield',
		             autoHeight: true,
		             autoScroll  : true,
		             bodyStyle: Ext.isIE ? 'padding:0 0 5px 15px;' : 'padding:10px 15px;',
		             border: false,
		             style: {
		                 "margin-left": "10px", 
		                 "margin-right": Ext.isIE6 ? (Ext.isStrict ? "-10px" : "-13px") : "0"  
		             },
		             items: [detailFieldId, detailFieldName, detailFieldCode, detailFieldDescr,
		                     detailFieldTabName, detailFieldColName, detailFieldNodeType]
		    	}
		    }];
	}
	
    //OVERRIDING save method
	,save : function() {
		var values = this.getForm().getFieldValues();
		var idRec = values['id'];
		var newRec;
	
		if(idRec ==0 || idRec == null || idRec === ''){
			newRec =new Ext.data.Record({
					name: values['name'],
					code: values['code'],
			        description: values['description'],			        
			        tablename: values['tablename'],	
			        columnname: values['columnname'],
			        typeCd: values['typeCd']
			});	  
			
		}else{
			var record;
			var length = this.mainElementsStore.getCount();
			for(var i=0;i<length;i++){
	   	        var tempRecord = this.mainElementsStore.getAt(i);
	   	        if(tempRecord.data.id==idRec){
	   	        	record = tempRecord;
				}			   
	   	    }	
			record.set('name',values['name']);
			record.set('code',values['code']);
			record.set('description',values['description']);
			record.set('tablename',values['tablename']);
			record.set('columnname',values['columnname']);
			record.set('typeCd',values['typeCd']);		
		}

        var params = {
        	name :  values['name'],
        	code : values['code'],
        	description : values['description'],
        	tablename : values['tablename'],
        	columnname : values['columnname'],
        	typeCd : values['typeCd']	
        };
        
        if(idRec){
        	params.id = idRec;
        }
        
        Ext.Ajax.request({
            url: this.services['saveItemService'],
            params: params,
            method: 'GET',
            success: function(response, options) {
				if (response !== undefined) {			
		      		if(response.responseText !== undefined) {

		      			var content = Ext.util.JSON.decode( response.responseText );
		      			if(content.responseText !== 'Operation succeded') {
			                    Ext.MessageBox.show({
			                        title: LN('sbi.generic.error'),
			                        msg: content,
			                        width: 150,
			                        buttons: Ext.MessageBox.OK
			                   });
			      		}else{
			      			var itemId = content.id;			      			
			      			
			      			if(newRec != null && newRec != undefined && itemId != null && itemId !==''){
			      				newRec.set('id', itemId);
			      				this.mainElementsStore.add(newRec);  
			      			}
			      			this.mainElementsStore.commitChanges();
			      			if(newRec != null && newRec != undefined && itemId != null && itemId !==''){
								this.rowselModel.selectLastRow(true);
				            }
			      			
			      			Ext.MessageBox.show({
			                        title: LN('sbi.generic.result'),
			                        msg: LN('sbi.generic.resultMsg'),
			                        width: 200,
			                        buttons: Ext.MessageBox.OK
			                });
			      		}      				 

		      		} else {
		      			Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.serviceResponseEmpty'), LN('sbi.generic.serviceError'));
		      		}
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.savingItemError'), LN('sbi.generic.serviceError'));
				}
            },
            failure: function(response) {
	      		if(response.responseText !== undefined) {
	      			var content = Ext.util.JSON.decode( response.responseText );
	      			var errMessage ='';
					for (var count = 0; count < content.errors.length; count++) {
						var anError = content.errors[count];
	        			if (anError.localizedMessage !== undefined && anError.localizedMessage !== '') {
	        				errMessage += anError.localizedMessage;
	        			} else if (anError.message !== undefined && anError.message !== '') {
	        				errMessage += anError.message;
	        			}
	        			if (count < content.errors.length - 1) {
	        				errMessage += '<br/>';
	        			}
					}

	                Ext.MessageBox.show({
	                    title: LN('sbi.generic.validationError'),
	                    msg: errMessage,
	                    width: 400,
	                    buttons: Ext.MessageBox.OK
	               });
	      		}else{
	                Ext.MessageBox.show({
	                    title: LN('sbi.generic.error'),
	                    msg: LN('sbi.generic.savingItemError'),
	                    width: 150,
	                    buttons: Ext.MessageBox.OK
	               });
	      		}
            }
            ,scope: this
        });
    }

});
