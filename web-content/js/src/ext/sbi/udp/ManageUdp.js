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
 * Authors - Monica Franceschini (monica.franceschini@eng.it)
 */
Ext.ns("Sbi.udp");

Sbi.udp.ManageUdp = function(config) {

	var paramsList = {MESSAGE_DET: "UDP_LIST"};
	var paramsSave = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "UDP_DETAIL"};
	var paramsDel = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "UDP_DELETE"};
	
	this.configurationObject = {};
	
	this.configurationObject.manageListService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_UDP_ACTION'
		, baseParams: paramsList
	});
	this.configurationObject.saveItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_UDP_ACTION'
		, baseParams: paramsSave
	});
	this.configurationObject.deleteItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_UDP_ACTION'
		, baseParams: paramsDel
	});
	this.types = config.types;
	this.families = config.families;
	
	this.initConfigObject();
	config.configurationObject = this.configurationObject;
	config.tabPanelWidth ='70%'; // 520;
	config.gridWidth = '30%'; //470;
	
	var c = Ext.apply({}, config || {}, {});

	Sbi.udp.ManageUdp.superclass.constructor.call(this, c);	 
	
	this.rowselModel.addListener('rowselect',function(sm, row, rec) { 
		this.getForm().loadRecord(rec);  
     }, this);
	   
};
Ext.extend(Sbi.udp.ManageUdp, Sbi.widgets.ListDetailForm, {
	configurationObject: null
	, gridForm:null
	, mainElementsStore:null
	, types: null
	, families: null

	,initConfigObject:function(){
	    this.configurationObject.fields = ['id'
	                         	          , 'label'
	                        	          , 'name'
	                        	          , 'description'
	                        	          , 'multivalue'
	                        	          , 'type'
	                        	          , 'family'
	                        	          ];
		
		this.configurationObject.emptyRecToAdd = new Ext.data.Record({
										  id: 0,
										  label:'', 
										  name:'', 
										  description:'',
										  multivalue:'',
										  type:'',
										  family: ''
										 });
		
		this.configurationObject.gridColItems = [
		                                         {header: LN('sbi.udp.label'), width: 130, sortable: true, dataIndex: 'label'},
											     {id:'name',header: LN('sbi.udp.name'), width: 140, sortable: true, locked:false, dataIndex: 'name'},
										         {header: LN('sbi.udp.type'), width: 70, sortable: true, dataIndex: 'type'},
										         {header: LN('sbi.udp.family'), width: 70, sortable: true, dataIndex: 'family'}
		                                        ];
		
		this.configurationObject.panelTitle = LN('sbi.udp.udpManagement');
		this.configurationObject.listTitle = LN('sbi.udp.udpList');
		
		this.initTabItems();
    }

	,initTabItems: function(){
		
		   this.typesStore = new Ext.data.SimpleStore({
	 	        fields: ['type'],
	 	        data: this.types,
	 	        autoLoad: false
	 	    });
	 	    
	 	   this.familiesStore = new Ext.data.SimpleStore({
		        fields: ['family'],
		        data: this.families,
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
 	          	 regexText : LN('sbi.udp.validString'),
 	               fieldLabel: LN('sbi.udp.name'),
 	               allowBlank: false,
 	               validationEvent:true,
 	               name: 'name'
 	           };
 			  
 	   var detailFieldLabel = {
 	          	 maxLength:20,
 	          	 minLength:1,
 	          	 regexText : LN('sbi.udp.validString'),
 	             fieldLabel: LN('sbi.udp.label'),
 	             allowBlank: false,
 	             validationEvent:true,
 	             name: 'label'
 	           };	  
 		   
 	   var detailFieldDescr = {
 	          	 maxLength:1000,
 	          	 width : 250,
 	             height : 80,
 	          	 regexText : LN('sbi.udp.validString'),
 	             fieldLabel:LN('sbi.udp.description'),
 	             validationEvent:true,
 	             xtype: 'textarea',
 	             name: 'description'
 	           };
 	 		   
 	   var detailFieldMultiValue = {
           	  name: 'multivalue',
              fieldLabel: LN('sbi.udp.multivalue'),
              displayField: 'multivalue',   // what the user sees in the popup
              valueField: 'multivalue',     // what is passed to the 'change' event
              typeAhead: true,
              forceSelection: true,
              mode: 'local',
              triggerAction: 'all',
              selectOnFocus: true,
              allowBlank: true,
              validationEvent:true,
              xtype: 'checkbox'
         };
 	 	 			  
 	   var detailFieldTypes = {
           	  name: 'type',
              store: this.typesStore,
              fieldLabel: LN('sbi.udp.type'),
              displayField: 'type',   // what the user sees in the popup
              valueField: 'type',     // what is passed to the 'change' event
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
 		   
 	   var detailFieldFamily =  {
           	  name: 'family',
              store: this.familiesStore,
              fieldLabel: LN('sbi.udp.family'),
              displayField: 'family',   // what the user sees in the popup
              valueField: 'family',     // what is passed to the 'change' event
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
		             labelWidth: 100,
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
		             items: [detailFieldId, detailFieldLabel, detailFieldName, detailFieldDescr,
		                     detailFieldMultiValue, detailFieldTypes, detailFieldFamily]
		    	}
		    }];
	}

	,save : function() {
		var values = this.getForm().getValues();
		var idRec = values['id'];
		var newRec;

		if (values['multivalue'] === 'on'){
			values['multivalue'] = true;		
		}else{
			values['multivalue'] = false;
		}
		
		if(idRec ==0 || idRec == null || idRec === ''){
			newRec =new Ext.data.Record({
					label :values['label'],
			        name :values['name'],
			        description :values['description'],
			        multivalue :values['multivalue'],
			        type :values['type'],
			        family :values['family']
			});	  
		}else{
			var newRec;
			var length = this.mainElementsStore.getCount();
			for(var i=0;i<length;i++){
	   	        var tempRecord = this.mainElementsStore.getAt(i);
	   	        if(tempRecord.data.id==idRec){
	   	        	newRec = tempRecord;
				}			   
	   	    }	
			newRec.set('label',values['label']);
			newRec.set('name',values['name']);
			newRec.set('description',values['description']);
			newRec.set('multivalue',values['multivalue']);
			newRec.set('type',values['type']);		
			newRec.set('family',values['family']);
		}

     var params = {
    	label : newRec.data.label,	 
     	name : newRec.data.name,
     	description : newRec.data.description,
     	multivalue : newRec.data.multivalue,
     	type : newRec.data.type,
     	family : newRec.data.family
     };
     if(idRec){
     	params.id = newRec.data.id;
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
			                        title: LN('sbi.udp.error'),
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
