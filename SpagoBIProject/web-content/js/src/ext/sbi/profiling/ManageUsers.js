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
Ext.ns("Sbi.profiling");

Sbi.profiling.ManageUsers = function(config) { 
	
	var paramsList = { MESSAGE_DET: "USERS_LIST"};
	var paramsSave = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', MESSAGE_DET: "USER_INSERT"};
	var paramsDel = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', MESSAGE_DET: "USER_DELETE"};
	
	this.configurationObject = {};
	
	this.configurationObject.manageListService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_USER_ACTION'
		, baseParams: paramsList
	});
	this.configurationObject.saveItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_USER_ACTION'
		, baseParams: paramsSave
	});
	this.configurationObject.deleteItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_USER_ACTION'
		, baseParams: paramsDel
	});
	
	this.initConfigObject();
	this.configurationObject.filter = true;
	this.configurationObject.columnName = [['userId', LN('sbi.users.userId')],
	                                       ['fullName', LN('sbi.users.fullName')]
	                	                   ];
	
	config.configurationObject = this.configurationObject;
	config.singleSelection = true;
	
	
	var c = Ext.apply({}, config || {}, {});

	Sbi.profiling.ManageUsers.superclass.constructor.call(this, c);	 
	
	this.rowselModel.addListener('rowselect',function(sm, row, rec) { 
			rec.set('confirmpwd', ''); 
			this.getForm().loadRecord(rec);  			
			this.detailFieldPwd.disable(); 
       	    this.detailFieldConfirmPwd.disable();       	    
       	    Sbi.config.passwordAbilitated = false;
	       	this.changePwdButton.show();
		  	this.fillAttributes(row, rec);
		  	this.fillRoles(row, rec); 
		  	this.enableUserId(rec);
     }, this);
};

Ext.extend(Sbi.profiling.ManageUsers, Sbi.widgets.ListDetailForm, {
	
	configurationObject: null
	, gridForm:null
	, mainElementsStore:null
	, detailTab: null
	, rolesTab: null
	, attrsTab: null
	
	, attributesGridPanel: null
	, attributesStore: null

	, rolesGrid: null
	, rolesStore: null
	, rolesEmptyStore: null
	, attributesEmptyStore: null
	, changePwdButton: null
	, detailFieldPwd: null
	, detailFieldConfirmPwd: null

	,initConfigObject:function(){
	    this.configurationObject.fields = ['userId'
	                 	    			  , 'id'
	                	    	          , 'fullName'
	                	    	          , 'pwd'
	                	    	          , 'confirmpwd'
	                	    	          , 'userRoles'
	                	    	          , 'userAttributes'
	                	    	          ];
		
		this.configurationObject.emptyRecToAdd = new Ext.data.Record({
										userId:'', 
										fullName:'', 
										pwd:'',
										userRoles:'',
										userAttributes:'',
										id: 0
									});
		
		this.configurationObject.gridColItems = [
		                                         {id:'userId', header: LN('sbi.users.userId'), width: 200, sortable: true, dataIndex: 'userId'},
		                                         {header: LN('sbi.users.fullName'), width: 200, sortable: true, dataIndex: 'fullName'}
		                                         ];
		
		this.configurationObject.panelTitle = LN('sbi.users.manageUsers');
		this.configurationObject.listTitle = LN('sbi.users.usersList');
		
		this.initTabItems();
    }

	,initTabItems: function(){
		
		this.attributesStore = new Ext.data.JsonStore({
	        fields : [ 'id', 'name', 'value' ]
	    });
		this.attributesStore.loadData(config.attributesEmpyList);
	    
	    this.rolesStore = new Ext.data.JsonStore({
	    	id: 'id'
			, data:{}
	        , fields : [ 'id', 'name', 'description', 'checked' ]
	    });
		this.rolesStore.loadData(config.rolesEmptyList);	
	    
	    this.attributesEmptyStore = config.attributesEmpyList;
	    this.rolesEmptyStore = config.rolesEmptyList;

		this.initDetailTab();
		this.initRolesTab();
		this.initAttrTab();

	    this.configurationObject.tabItems = [ this.detailTab, this.rolesTab, this.attrsTab];
	}
	
	,initDetailTab: function() {
		
		   var detailFieldId = {
	                 name: 'id',
	                 hidden: true
	             };
	 	   
	 	   var detailFieldName = {
	                 fieldLabel:  LN('sbi.users.fullName'),
	                 name: 'fullName',
	                 allowBlank: false,
	                 validationEvent:true,
	            	 maxLength:255,
	            	 minLength:1,
	            	 //regex : new RegExp("^([a-zA-Z0-9_\x2D\s\x2F])+$", "g"),
	            	 regexText : LN('sbi.users.wrongFormat')
	             };
	  			  
	  	   var detailFieldUserId = {
	                 fieldLabel: LN('sbi.users.userId'),
	                 name: 'userId',
	                 allowBlank: false,
	                 validationEvent:true,
	            	 maxLength:100,
	            	 minLength:1,
	            	// regex : new RegExp("^([a-zA-Z1-9_\x2D])+$", "g"),
	            	 regexText : LN('sbi.users.wrongFormat')
	             };	  
	  	   
	  	   this.detailFieldPwd = new Ext.form.TextField({
	                 fieldLabel: LN('sbi.users.pwd'),
	                 name: 'pwd',
	                 itemId: 'pwdId',
	                 inputType: 'password',
	                 validationEvent:true,
	            	 maxLength:160,
	            	 minLength:1
	             });	
	  	   
	  	   this.detailFieldConfirmPwd = new Ext.form.TextField({
	                 fieldLabel:  LN('sbi.users.confPwd'),		                 
	                 name: 'confirmpwd',
	                 itemId: 'confirmpwdId',
	                 inputType: 'password',
	                 validationEvent:true,
	            	 maxLength:160,
	            	 minLength:1
	             });	
	  	   
	  	 this.changePwdButton = new Ext.Toolbar.Button({
		             text: LN('sbi.users.changePwd'),
		             id: 'changePwd',
		             iconCls: 'icon-refresh',		                 
		             style: Ext.isIE ? {} : {	
		            	 position: 'absolute'
		        	 	,top: '95px'
		            	,left: '385px'
		            	,zIndex: '180'
			         },
		             handler: function(){
			        	 this.detailFieldPwd.enable(); 
			        	 this.detailFieldConfirmPwd.enable();
			        	 Sbi.config.passwordAbilitated = true;
		             }
			         ,scope: this
		         });
		
		 this.detailTab = new Ext.Panel({
	        title: LN('sbi.alarms.details')
	        , id: 'detail'
	        , layout: 'fit'
	        , items: {
	 		   	     id: 'alarm-detail',   	              
	 		   	     columnWidth: 0.4,
		             xtype: 'fieldset',
		             labelWidth: 100,
		             defaults: {width: 240, border:false},    
		             defaultType: 'textfield',
		             autoHeight: true,
		             autoScroll  : true,
		             bodyStyle: Ext.isIE ? 'padding:0 0 5px 15px;' : 'padding:10px 15px;',
		             border: false,
		             buttons: [this.changePwdButton],
		             style: {
		                 "margin-left": "10px", 
		                 "margin-right": Ext.isIE6 ? (Ext.isStrict ? "-10px" : "-13px") : "0"  
		             },
		             items: [detailFieldId, detailFieldUserId, detailFieldName, this.detailFieldPwd, this.detailFieldConfirmPwd]
	    	}
	    });
	}
	
	,initRolesTab: function() {

		this.smRoles = new Ext.grid.CheckboxSelectionModel( 
					{header: ' ',singleSelect: false, scope:this, dataIndex: 'id'} 
				);
		
        this.cmRoles = new Ext.grid.ColumnModel([
	         {header: LN('sbi.roles.headerName'), width: 45, sortable: true, dataIndex: 'name'},
	         {header: LN('sbi.roles.headerDescr'), width: 65, sortable: true, dataIndex: 'description'}
	         ,this.smRoles 
	    ]);

		this.rolesGrid = new Ext.grid.GridPanel({
			  store: this.rolesStore
			, id: 'roles-form'
   	     	, cm: this.cmRoles
   	     	, sm: this.smRoles
   	     	, frame: false
   	     	, border:false  
   	     	, collapsible:false
   	     	, loadMask: true
   	     	, viewConfig: {
   	        	forceFit:true
   	        	, enableRowBody:true
   	        	, showPreview:true
   	     	}
			, scope: this
		});
		
		this.rolesTab = new Ext.Panel({
	        title: LN('sbi.users.roles')
	        , id : 'rolesList'
	        , layout: 'fit'
	        , autoScroll: true
	        , items: [this.rolesGrid]
	        , itemId: 'roles'
	        , scope: this
	    });
	}
	
	,initAttrTab: function() {
	    
		 this.attributesGridPanel = new Ext.grid.EditorGridPanel({
	            id: 'attributes-form',
	            store : this.attributesStore,
	            autoHeight : true,
	            columns : [ {          	
	                header : LN('sbi.roles.headerName'),
	                width : 70,
	                sortable : true,
	                dataIndex : 'name'
	            }, {           	
	                header : LN('sbi.users.headerValue'),
	                width : 70,
	                sortable : true,
	                dataIndex : 'value',
	                editor : new Ext.form.TextField({}) 
	            } ],
	            viewConfig : {
	                forceFit : true,
	                scrollOffset : 2
	            // the grid will never have scrollbars
	            },
	            singleSelect : true,
	            clicksToEdit : 2
	        });
		
        this.attrsTab = new Ext.Panel({
            title: LN('sbi.users.attributes')
            , id : 'attrList'
            , autoScroll: true
            , items : [ this.attributesGridPanel ]
            , itemId: 'attributes'
            , layout: 'fit'
        });
	}
	
	,
	fillRoles : function(row, rec) {
		this.rolesGrid.selModel.clearSelections();
		var userRolesArray = rec.data.userRoles;
		var length = rec.data.userRoles.length;
		for ( var i = 0; i < length; i++) {
			if (userRolesArray[i].checked) {
				var roleId = userRolesArray[i].id;
				var store = this.rolesGrid.getStore();
				var index = store.indexOfId(roleId);
				this.rolesGrid.getSelectionModel().selectRow(index, true);
			}
		}
	}
	
     ,fillAttributes : function(row, rec, emptyData) {	 
        this.attributesGridPanel.store.removeAll();
        if(emptyData){
        	this.attributesStore.loadData(config.attributesEmpyList);
        }else{
	     	var tempArr = rec.data.userAttributes;
	     	var length = rec.data.userAttributes.length;
	     	for(var i=0;i<length;i++){
	     		var tempRecord = new Ext.data.Record({"value":tempArr[i].value,"name":tempArr[i].name,"id":tempArr[i].id });
	     		this.attributesStore.add(tempRecord);	
	     		this.attributesStore.commitChanges();
	     	}	
        }
     }
	
	,save : function() {
		   
	    var values = this.getForm().getValues();     	
        
		if(Sbi.config.passwordAbilitated && !(values['pwd'] === values['confirmpwd'])){
			alert(LN('sbi.users.pwdNotMatching'))	
		}else{
			
			var newRec = null;
	      	var idRec = values['id'];
	      	
			var params = {
	        	userId : values['userId'],
	        	fullName : values['fullName']  
	        }
	        params.id = values['id'];			
			
			if(Sbi.config.passwordAbilitated && (values['pwd'] === values['confirmpwd'])){
				if(values['pwd'] != undefined){
					params.pwd = values['pwd'] ;
				}
			}
        
	        var rolesSelected = this.rolesGrid.selModel.getSelections();
	        var lengthR = rolesSelected.length;
	        var roles =new Array();
	        for(var i=0;i<lengthR;i++){
	         	var role ={'name':rolesSelected[i].get("name"),'id':rolesSelected[i].get("id"),'description':rolesSelected[i].get("description"),'checked':true};
	 				roles.push(role);
	           }
		       params.userRoles =  Ext.util.JSON.encode(roles);
	  
	       	   var userRoles =new Array();
		       var tempArr = this.rolesGrid.store;
	           var length = this.rolesGrid.store.data.length;
	
	           for(var i=0;i<length;i++){
	           		var selected = false;
	           		for(var j=0;j<lengthR;j++){
	           			if(rolesSelected[j].get("id")===tempArr.getAt(i).get("id")){
	       				selected = true;
	       				var role ={'name':tempArr.getAt(i).get("name"),'id':tempArr.getAt(i).get("id"),'description':tempArr.getAt(i).get("description"),'checked':true};
							userRoles.push(role);
	       				break;
	       			}
	            }
	            if(!selected){
	          		var role ={'name':tempArr.getAt(i).get("name"),'id':tempArr.getAt(i).get("id"),'description':tempArr.getAt(i).get("description"),'checked':false};
	 				userRoles.push(role);
				}
		    }	
	  
	        var modifAttributes = this.attributesStore.getModifiedRecords();
	        var lengthA = modifAttributes.length;
	        var attrs =new Array();
	        for(var i=0;i<lengthA;i++){
	         	var attr ={'name':modifAttributes[i].get("name"),'id':modifAttributes[i].get("id"),'value':modifAttributes[i].get("value")};
				attrs.push(attr);
	        }
	        params.userAttributes =  Ext.util.JSON.encode(attrs);      
	        
	        
	   	    var userAttributes = new Array();
	        var tempArr =  this.attributesGridPanel.store;
	        var length =  this.attributesGridPanel.store.data.length;
	
	        for(var i=0;i<length;i++){
	      		var attr ={'name':tempArr.getAt(i).get("name"),'id':tempArr.getAt(i).get("id"),'value':tempArr.getAt(i).get("value")};
				userAttributes.push(attr);
		    }	
	
			if(idRec ==0 || idRec == null || idRec === ''){
	          newRec =new Ext.data.Record({'userId': values['userId'],'fullName': values['fullName'],'pwd':values['pwd']});	  
	          newRec.set('userRoles', userRoles);
			  newRec.set('userAttributes', userAttributes);        
	        }else{
				var record;
				var length = this.mainElementsStore.getCount();
				for(var i=0;i<length;i++){
		   	        var tempRecord = this.mainElementsStore.getAt(i);
		   	        if(tempRecord.data.id==idRec ){
		   	        	record = tempRecord;
					}			   
		   	    }	
				record.set('userId',values['userId']);
				record.set('fullName',values['fullName']);
				if(values['pwd'] != undefined){
					record.set('pwd',values['pwd']);
				}
				
				record.set('userRoles',userRoles);
				record.set('userAttributes',userAttributes);				      
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
			                        title: LN('sbi.roles.error'),
			                        msg: content,
			                        width: 150,
			                        buttons: Ext.MessageBox.OK
			                   });
			      			}else{
			      			    
								var idTemp = content.id;
								if(newRec!==null){
									newRec.set('id', idTemp);
									this.mainElementsStore.add(newRec);
								}
								this.attributesStore.commitChanges();
								this.rolesStore.commitChanges();
								this.mainElementsStore.commitChanges();
								if(newRec!==null){
									this.rowselModel.selectLastRow(true);
					            }
								this.detailFieldPwd.disable(); 
						   	 	this.detailFieldConfirmPwd.disable();
						   	    Sbi.config.passwordAbilitated = false;
						   	 	
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
						Sbi.exception.ExceptionHandler.showErrorMessage('Error while saving User', 'Service Error');
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
		                    title: LN('sbi.attributes.validationError'),
		                    msg: errMessage,
		                    width: 400,
		                    buttons: Ext.MessageBox.OK
		               });
		      		}else{
		                Ext.MessageBox.show({
		                    title:LN('sbi.roles.error'),
		                    msg: 'Error in Saving User',
		                    width: 150,
		                    buttons: Ext.MessageBox.OK
		               });
		      		}
	            }
	            ,scope: this
	   		 });			
		}	
    }
	, addNewItem : function(){
		var emptyRecToAdd =new Ext.data.Record({userId:'', 
											fullName:'', 
											pwd:'',
											userRoles:'',
											userAttributes:'',
											id: 0
											});
		this.enableUserId(emptyRecToAdd);
		this.getForm().loadRecord(emptyRecToAdd); 
        this.fillRoles(0, emptyRecToAdd); 
  		this.fillAttributes(0, emptyRecToAdd, true);  

		this.tabs.setActiveTab(0);
		this.detailFieldPwd.enable(); 
   	 	this.detailFieldConfirmPwd.enable();
   	 	Sbi.config.passwordAbilitated = true;
		this.changePwdButton.hide();      
	}

	,
	onDeleteItemFailure : Sbi.exception.ExceptionHandler.handleFailure
		
	, 
	enableUserId: function(rec){
		var userElems = this.detailTab.items.items[0].items.items;
		var userId = null;
		
		for (key in userElems) {
			var elem = userElems[key];
			if (elem.name == 'userId'){
				userId = userElems[key];				
				if (rec.get('userId') !== undefined &&  rec.get('userId') == 'public_user'){								
					//userId.setDisabled(true);		
					userId.getEl().dom.setAttribute('readOnly', true);
					this.detailFieldPwd.setDisabled(true); 
			   	 	this.detailFieldConfirmPwd.setDisabled(true);
				    this.changePwdButton.hide();					 
					
			    }else{
//					 userId.setDisabled(false);
//			    	 userId.getEl().dom.setAttribute('readOnly', false);
			    	 userId.getEl().dom.removeAttribute('readOnly');
					 this.detailFieldPwd.setDisabled(false); 
				   	 this.detailFieldConfirmPwd.setDisabled(false);
					 this.changePwdButton.show();
				}
				break;
			}			
		}
	}
});
