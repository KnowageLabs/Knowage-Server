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
Ext.ns("Sbi.alarm");

Sbi.alarm.ManageContacts = function(config) { 

	var paramsList = {MESSAGE_DET: "CONTACTS_LIST"};
	var paramsSave = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "CONTACT_INSERT"};
	var paramsDel = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "CONTACT_DELETE"};
	
	this.configurationObject = {};
	
	this.configurationObject.manageListService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_CONTACTS_ACTION'
		, baseParams: paramsList
	});
	this.configurationObject.saveItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_CONTACTS_ACTION'
		, baseParams: paramsSave
	});
	this.configurationObject.deleteItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_CONTACTS_ACTION'
		, baseParams: paramsDel
	});
	
	this.initConfigObject();
	config.configurationObject = this.configurationObject;
	config.singleSelection = true;
	
	var c = Ext.apply({}, config || {}, {});

	Sbi.alarm.ManageContacts.superclass.constructor.call(this, c);	 
	
	this.rowselModel.addListener('rowselect',function(sm, row, rec) { 
		this.getForm().loadRecord(rec);  
     }, this);

};
Ext.extend(Sbi.alarm.ManageContacts, Sbi.widgets.ListDetailForm, {

	configurationObject: null
	, gridForm:null
	, mainElementsStore:null

	,initConfigObject:function(){
	    this.configurationObject.fields = ['id'
	                         	          , 'name'
	                        	          , 'email'
	                        	          , 'resources'
	                        	          , 'mobile'
	                        	          ];
		
		this.configurationObject.emptyRecToAdd = new Ext.data.Record({
											id: 0,
											name:'', 
											mobile:'', 
											email:'',
											resources:''
											});
		
		this.configurationObject.gridColItems = [
		                                         {id:'name',header: LN('sbi.alarmcontact.name'), width: 170, sortable: true, locked:false, dataIndex: 'name'},
		                                         {id:'email',header:  LN('sbi.alarmcontact.email'), width: 240, sortable: true, locked:false, dataIndex: 'email'}
		                                        ];
		
		this.configurationObject.panelTitle = LN('sbi.alarmcontact.contactsManagement');
		this.configurationObject.listTitle = LN('sbi.alarmcontact.contactsList');
		
		this.initTabItems();
    }

	,initTabItems: function(){
		//Store of the combobox
	   this.typesStore = new Ext.data.SimpleStore({
	        fields: ['resources'],
	        data: config,
	        autoLoad: false
	    });
		
 	    
 	   var detailFieldId = {
               name: 'id',
               hidden: true
           };
 	   
 	   var detailFieldName = {
          	 maxLength:100,
          	 minLength:1,
          	 //regex : new RegExp("^([a-zA-Z1-9_\x2F\s])+$", "g"),
          	 regexText : LN('sbi.alarmcontact.validString'),
             fieldLabel: LN('sbi.alarmcontact.name'),
             allowBlank: false,
             validationEvent:true,
             name: 'name'
           };
  			  
  	   var detailFieldEmail = {
          	 maxLength:100,
        	 minLength:1,
        	 //regex : new RegExp("/^([\w]+)(.[\w]+)*@([\w-]+\.){1,5}([A-Za-z]){2,4}$/", "g"),
        	 regexText : LN('sbi.alarmcontact.validEmailString'),
             fieldLabel: LN('sbi.alarmcontact.email'),
             validationEvent:true,
             name: 'email'
         };	  
  	   
  	   var detailFieldCell = {
          	 maxLength:50,
        	 minLength:0,
        	 //regex : new RegExp("^([0-9/])+$", "g"),
        	 regexText : LN('sbi.alarmcontact.validMobileString'),
             fieldLabel:LN('sbi.alarmcontact.mobile'),
             validationEvent:true,
             name: 'mobile'
         };	
  	   
  	   var detailFieldResources = {
         	  name: 'resources',
              store: this.typesStore,
              fieldLabel: LN('sbi.alarmcontact.resources'),
              displayField: 'resources',   // what the user sees in the popup
              valueField: 'resources',        // what is passed to the 'change' event
              typeAhead: true,
              forceSelection: true,
              mode: 'local',
              triggerAction: 'all',
              selectOnFocus: true,
              emptyText: '-',
              editable: false,
              allowBlank: true,
              validationEvent:true,
              xtype: 'combo'
         };	
 	    
 	   this.configurationObject.tabItems = [{
	        title: LN('sbi.alarmcontact.detail')
	        , itemId: 'detail'
	        , width: 430
	        , items: {
		   		id: 'items-detail',   	
	 		   	itemId: 'items-detail',   	              
	 		   	columnWidth: 0.4,
	             xtype: 'fieldset',
	             labelWidth: 110,
	             defaults: {width: 280, border:false},    
	             defaultType: 'textfield',
	             autoHeight: true,
	             autoScroll  : true,
	             border: false,
	             style: {
	                 "margin-left": "25px", 
	                 "margin-right": Ext.isIE6 ? (Ext.isStrict ? "-15px" : "-13px") : "15"  
	             },
	             items: [detailFieldId, detailFieldName, detailFieldEmail, detailFieldCell, detailFieldResources]	    	
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
					name :values['name'],
			        email :values['email'],
			        resources :values['resources'],
			        mobile :values['mobile']
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
			newRec.set('name',values['name']);
			newRec.set('email',values['email']);
			newRec.set('resources',values['resources']);
			newRec.set('mobile',values['mobile']);		
		}

     var params = {
     	name : newRec.data.name,
     	resources : newRec.data.resources,
     	email : newRec.data.email,
     	mobile : newRec.data.mobile
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
			                        title: LN('sbi.alarmcontact.error'),
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
		      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
		      		}
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage('Error while saving Contact', 'Service Error');
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
	                    title: LN('sbi.alarmcontact.validationError'),
	                    msg: errMessage,
	                    width: 400,
	                    buttons: Ext.MessageBox.OK
	               });
	      		}else{
	                Ext.MessageBox.show({
	                    title: LN('sbi.alarmcontact.error'),
	                    msg: 'Error while Saving Contact',
	                    width: 150,
	                    buttons: Ext.MessageBox.OK
	               });
	      		}
         }
         ,scope: this
     });
	}

});

