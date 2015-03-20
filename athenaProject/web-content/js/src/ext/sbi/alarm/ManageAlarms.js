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
Ext.ns("Sbi.alarms");

Sbi.alarms.ManageAlarms = function(config) { 

	var paramsList = {MESSAGE_DET: "ALARMS_LIST"};
	var paramsSave = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "ALARM_INSERT"};
	var paramsDel = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "ALARM_DELETE"};
	
	this.configurationObject = {};
	
	this.configurationObject.manageListService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_ALARMS_ACTION'
		, baseParams: paramsList
	});
	this.configurationObject.saveItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_ALARMS_ACTION'
		, baseParams: paramsSave
	});
	this.configurationObject.deleteItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_ALARMS_ACTION'
		, baseParams: paramsDel
	});
	
	this.initConfigObject();
	
	config.configurationObject = this.configurationObject;
	config.singleSelection = true;
	
	var c = Ext.apply({}, config || {}, {});
	
	Sbi.alarms.ManageAlarms.superclass.constructor.call(this, c);	 
	
	this.rowselModel.addListener('rowselect',function(sm, row, rec) { 
		this.getForm().loadRecord(rec);  
		this.fillOptions(row, rec);   
        this.fillContacts(row, rec); 
  		this.fillKpis(row, rec);     
  		//this.tabs.setActiveTab(1);
	}, this);

};

Ext.extend(Sbi.alarms.ManageAlarms, Sbi.widgets.ListDetailForm, {

	configurationObject: null
	, services:null
	, mainElementsStore:null
	, kpiStore: null
	, thresholdsStore: null
	, contactsStore: null
	, alarmsEmptyStore: null
	, kpisEmptyStore: null
	, contactsEmptyStore: null
	, kpiGrid:null
	
	, detailTab: null
    , kpiTab: null
    , contactsTab: null
    , kpiCheckColumn: null
	
	, contactsGridPanel: null
	, tresholdsCombo: null

	,initConfigObject:function(){
	    this.configurationObject.fields = ['id'
	                 	    			  , 'name'
	                	    	          , 'description'
	                	    	          , 'label'
	                	    	          , 'modality'
	                	    	          , 'singleEvent'
	                	    	          , 'autoDisabled'
	                	    	          , 'text'
	                	    	          , 'url'
	                	    	          , 'contacts'
	                	    	          , 'kpi'
	                	    	          , 'threshold'
	                	    	          ];
		
		this.configurationObject.emptyRecToAdd = new Ext.data.Record({
										id:0, 
										name:'', 
										description:'',
										label:'',
										modality:'MAIL',
										singleEvent: false,
										autoDisabled: false,
										text:'',
										url: '',
										contacts: []
										});
		
		this.configurationObject.gridColItems = [
		                                         {header: LN('sbi.alarms.alarmLabel'), width: 200, sortable: true, dataIndex: 'label'},
		                                         {header: LN('sbi.alarms.alarmName'), width: 200, sortable: true, dataIndex: 'name'}
		                                        ];
		
		this.configurationObject.panelTitle = LN('sbi.alarms.manageAlarms');
		this.configurationObject.listTitle = LN('sbi.alarms.alarmsList');
		
		this.initTabItems();
    },
    
    fillOptions : function(row, rec) {	 
       	var singleEvent = rec.get('singleEvent');
       	var autoDisabled = rec.get('autoDisabled');
       	var modality = rec.get('modality');
       	
       	this.detailTab.items.each(function(item){	  
       		if(item.getItemId() == 'alarm-detail'){ 
       		  item.items.each(function(item){	  
           		   if(item.getItemId() == 'options'){
           		   		item.setValue('singleEvent', singleEvent);
           		   		item.setValue('autoDisabled', autoDisabled);
     		   }else if(item.getItemId() == 'modality'){
               		  	if(modality =='SMS'){
   	                  		item.onSetValue( 'sms',true);
   	                  	}else{
   	                  		item.onSetValue( 'mail',true);
   	                  	}
     		   }
              });
       		}  
	 	});	      
	 },
	
	fillContacts : function(row, rec) {	 
 	    var tempArr = rec.data.contacts;
       	var length = rec.data.contacts.length;
       	this.contactsGridPanel.selModel.clearSelections();
       	for(var i=0;i<length;i++){
       			var tempRecord = new Ext.data.Record({
       											  "id":tempArr[i].id, "name":tempArr[i].name, 
       											  "email":tempArr[i].email, "mobile":tempArr[i].mobile, 
       											  "resources":tempArr[i].resources
       											  });     			
			    if(tempArr[i].checked){
			    	var contactId = tempRecord.get('id');	
			    	this.contactsGridPanel.fireEvent('recToSelect', contactId, i);
			    }
       	}		   	                  	
    },
	
	 fillKpis : function(row, rec) {
		 
		 	var tempAttrArr = config.kpisEmptyList;
	  		var length = tempAttrArr.length;
	  		var checkedArr = new Array();
	  		var kpiInstIdSelected = null;
	  		var selectedRowIndex = null;
	  		for(var i=0;i<length;i++){	
	  			var tempRecord = new Ext.data.Record(
	  					{"kpiName":tempAttrArr[i].kpiName, "kpiModel":tempAttrArr[i].kpiModel,"id":tempAttrArr[i].id }
	  					);
	  			if(tempAttrArr[i].id === rec.data.kpi){			
	  				checkedArr.push(tempRecord);	  
	  				kpiInstIdSelected = tempAttrArr[i].id;
	  				selectedRowIndex = i;
	  			}
	  		 }

			this.kpiCheckColumn.clearSelections();
			if(selectedRowIndex!=null){
				this.kpiCheckColumn.selectRow(selectedRowIndex);
			}
			
			if(kpiInstIdSelected!=null){
				var kpiInstId = kpiInstIdSelected;
				this.tresholdsCombo.store.removeAll();
				this.tresholdsCombo.clearValue();

				var paramsTresholds = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', MESSAGE_DET: "TRESHOLDS_LIST"};	
				var loadThr = Sbi.config.serviceRegistry.getServiceUrl({
					serviceName: 'MANAGE_ALARMS_ACTION'
					, baseParams: paramsTresholds
				 });	
				
				Ext.Ajax.request({
			          url: loadThr,
			          params: {id: kpiInstId},
			          method: 'GET',
			          success: function(response, options) {   	
						if (response !== undefined) {		
			      			var content = Ext.util.JSON.decode( response.responseText );
			      			if(content !== undefined) {	   
			      				this.tresholdsCombo.store.loadData(content);
			      				this.tresholdsCombo.store.commitChanges();
			      				this.tresholdsCombo.setValue(rec.data.threshold);	
			      			}
						 } 	
			          }
			          ,scope: this
			    });
		    }else{
		    	this.tresholdsCombo.store.removeAll();
		    	this.tresholdsCombo.store.commitChanges();
		    	this.tresholdsCombo.clearValue();
		    }
			this.kpiGrid.getView().refresh();
	}

	
	,initDetailTab: function() {
		
		   var detailFieldId = {
                name: 'id',
                hidden: true
               };
	 	   
	 	   var detailFieldName = {
	                 fieldLabel:  LN('sbi.alarms.alarmName'),
	                 name: 'name',
	                 allowBlank: false,
	                 validationEvent:true,
	            	 maxLength:50,
	            	 minLength:1,
	            	 //regex : new RegExp("^([a-zA-Z0-9_\x2D\s\x2F])+$", "g"),
	            	 regexText : LN('sbi.users.wrongFormat')
	             };
	  			  
	  	   var detailFieldLabel = {
	                 fieldLabel: LN('sbi.alarms.alarmLabel'),
	                 name: 'label',
	                 allowBlank: false,
	                 validationEvent:true,
	            	 maxLength:50,
	            	 minLength:1,
	            	 //regex : new RegExp("^([a-zA-Z1-9_\x2D])+$", "g"),
	            	 regexText : LN('sbi.users.wrongFormat')
	             };	  
	  	   
	  	   var detailFieldDescr = {
	                 fieldLabel: LN('sbi.alarms.alarmDescr'),
	                 name: 'description',
	                 allowBlank: true,
	                 xtype: 'textarea',
	                 height : 80,
	                 validationEvent:true,
		             maxLength:200
	             };	
	  	   
	  	   var detailFieldModality = {
		            xtype: 'radiogroup',
		            itemId: 'modality',
		            name: 'mod',
		            boxMinWidth  : 50,
		            boxMaxWidth  : 200,
		            boxMinHeight  : 100,
		            fieldLabel: LN('sbi.alarms.alarmModality'),
		            items: [
	             		{boxLabel: LN('sbi.alarms.MAIL'),id:'mail',name: 'modality', inputValue: 1, checked: true},
				        {boxLabel: LN('sbi.alarms.SMS'),id:'sms',name: 'modality', inputValue: 2}	
		            ]
		         };	
	  	   
	  	 var detailFieldOptions = new Ext.form.CheckboxGroup({
	            xtype: 'checkboxgroup',
	            itemId: 'options',
	            columns: 2,
	            boxMinWidth  : 200,
	            boxMaxWidth  : 200,
	            boxMinHeight  : 100,
	            hideLabel  : false,
	            fieldLabel: LN('sbi.alarms.options'),
	            items: [
	                {boxLabel: LN('sbi.alarms.alarmSingleEvent'), name: 'singleEvent', checked:false},
	                {boxLabel: LN('sbi.alarms.alarmAutoDisabled'), name: 'autoDisabled', checked:false}
	            ]
             });	
	  	 
	  	var detailFieldUrl = {
                fieldLabel:  LN('sbi.alarms.alarmMailUrl'),
                name: 'url',
                allowBlank: true,
                validationEvent:true,
	            	 maxLength:20
            };	
	  	
	  	var detailFieldMailText = {
                fieldLabel:  LN('sbi.alarms.alarmMailText'),
                xtype: 'textarea',
                height : 80,
                name: 'text',
                allowBlank: true,
                validationEvent:true,
	            maxLength:1000
        };	
		
		this.detailTab = new Ext.Panel({
	        title: LN('sbi.alarms.details')
	        , id: 'detail'
	        , layout: 'fit'
	        , items: {
	 		   	     id: 'alarm-detail',   	              
	 		   	     columnWidth: 0.4,
		             xtype: 'fieldset',
		             labelWidth: 100,
		             defaults: {width: 320, border:false},    
		             defaultType: 'textfield',
		             autoHeight: true,
		             autoScroll  : true,
		             bodyStyle: Ext.isIE ? 'padding:0 0 5px 15px;' : 'padding:10px 15px;',
		             border: false,
		             style: {
		                 "margin-left": "10px", 
		                 "margin-right": Ext.isIE6 ? (Ext.isStrict ? "-10px" : "-13px") : "0"  
		             },
		             items: [detailFieldId, detailFieldName, detailFieldLabel, detailFieldDescr, 
		                     detailFieldModality, detailFieldOptions, detailFieldUrl, detailFieldMailText]
	    	}
	    });
	}
	
	,initKpiTab: function() {
		   
     	 this.kpiCheckColumn = new Ext.grid.CheckboxSelectionModel( 
     			 	{header: ' ',singleSelect: true, scope:this, dataIndex: 'id'} 
     			 );
		 this.kpiCheckColumn.on('rowselect', this.onKpiSelect, this);
		 
	     this.kpiCm = new Ext.grid.ColumnModel({
	         // specify any defaults for each column
	         defaults: {
	             sortable: true // columns are not sortable by default           
	         },
	         columns: [
	             {
	                 id: 'id',
	                 header: LN('sbi.alarms.kpiInstanceIdHeader'),
	                 dataIndex: 'id',
	                 width: 85
	             }, {
	                 header: LN('sbi.alarms.kpiModelHeader'),
	                 dataIndex: 'kpiModel',
	                 width: 170
	             }, {
	                 header: LN('sbi.alarms.kpiNameHeader'),
	                 dataIndex: 'kpiName',
	                 width: 170
	             },
	             this.kpiCheckColumn // the plugin instance
	         ]
	     });
	     
		this.kpiGrid = new Ext.grid.EditorGridPanel ({
			id: 'kpi-grid',
			store: this.kpiStore,
			autoHeight: true,
			//height: 370,
			cm: this.kpiCm,
			sm: this.kpiCheckColumn,
			//deferRowRender : false,
			stripeRows: true,
			forceLayout:true,
			frame: true,
			autoScroll: true,
           /* viewConfig : {
	            //forceFit : true,
	            scrollOffset : 2
	        // the grid will never have scrollbars
	        },*/
	        singleSelect : true,
	        scope:this,
	        clicksToEdit : 2
		}); 
		this.kpiGrid.setAutoScroll(true);	
		
		this.tresholdsCombo = new Ext.form.ComboBox(
			{
				 id: 'tresholds-combo',
                 fieldLabel:  LN('sbi.alarms.alarmKpiThreshold'),                 
                 name: 'alarmKpiThreshold',
                 width: 200,
	             store: this.thresholdsStore,
	             forceReload:true,
	             displayField:'label',
	             valueField: 'idThrVal',
	             typeAhead: true,
	             mode: 'local',
	             triggerAction: 'all',
	             emptyText:'Select a treshold...',
	             selectOnFocus:true
            }
		);
		
     	this.kpiTab = new Ext.Panel({
		        title: LN('sbi.alarms.kpis')
		        , id : 'alarmKpi'
		        , layout: 'form'
		        , autoScroll: true
		        , itemId: 'kpis'
		        , scope: this
		        , forceLayout: true
	            , bodyStyle: Ext.isIE ? 'padding:0 0 5px 15px;' : 'padding:10px 15px;'
			    , border: false
		        , items: [
		            this.tresholdsCombo,this.kpiGrid
		        ]
		    });
	}
	
	,initContactsTab: function() {
		
		this.initContactsGridPanel();
	    
        this.contactsTab = new Ext.Panel({
		        title: LN('sbi.alarms.contacts')
		        , autoScroll: true
		        , id : 'contactsList'
	            , items : [ this.contactsGridPanel ]
		        , itemId: 'contacts'
		        , layout: 'fit'
				, autoWidth: true
		    });
	}
	
	, initContactsGridPanel : function() {
	       
    	this.smContacts = new Ext.grid.CheckboxSelectionModel( {header: ' ',singleSelect: false, scope:this, dataIndex: 'id'} );
		
        this.cmContacts = new Ext.grid.ColumnModel([
	         {header: LN('sbi.alarmcontact.name'), width: 45, sortable: true, dataIndex: 'name'},
	         {header: LN('sbi.alarmcontact.email'), width: 45, sortable: true, dataIndex: 'email'},
	         {header: LN('sbi.alarmcontact.mobile'), width: 45, sortable: true, dataIndex: 'mobile'},
	         {header: LN('sbi.alarmcontact.resources'), width: 45, sortable: true, dataIndex: 'resources'},
	         this.smContacts 
	    ]);

		this.contactsGridPanel = new Ext.grid.GridPanel({
			  store: this.contactsStore
			, id: 'contacts-form'
   	     	, cm: this.cmContacts
   	     	, sm: this.smContacts
   	     	, frame: false
   	     	, border:false 
   	     	, height: 450
   	     	, collapsible:false
   	     	, loadMask: true
   	     	, viewConfig: {
   	        	forceFit:true
   	        	, enableRowBody:true
   	        	, showPreview:true
   	     	}
			, scope: this
		});
		
		this.contactsGridPanel.addEvents('recToSelect');	
		
		this.contactsGridPanel.on('recToSelect', function(roleId, index){
			Ext.getCmp("contacts-form").selModel.selectRow(index,true);
		});

		this.doLayout();
	}
	
    ,initTabItems: function(){
		
		this.kpiStore = new Ext.data.JsonStore({	
			 data:{}
	        , fields : [ 'id', 'kpiName', 'kpiModel' ]
	    });
		this.kpiStore.loadData(config.kpisEmptyList);		
	    
	    this.thresholdsStore = new Ext.data.JsonStore({
	    	id: 'idThrVal',
	    	root: 'samples',
	        fields : ['idThrVal', 'label']
	    });
	    
	    this.contactsStore = new Ext.data.JsonStore({
	    	data:{},
	        fields : [ 'id', 'name', 'email', 'mobile', 'resources' ]
	    });
	    this.contactsStore.loadData(config.contactsEmpyList);
	    
	    this.kpisEmptyStore = config.kpisEmptyList;
	    this.tresholdsEmptyStore = config.tresholdsList;
	    this.contactsEmptyStore = config.contactsEmpyList;
		
	    this.initDetailTab();
	    this.initKpiTab();
	    this.initContactsTab();
 
 	    this.configurationObject.tabItems = [ this.detailTab
 	                              		   , this.kpiTab
 	                           		       , this.contactsTab
 	                           		       ];
	}
	
	,onKpiSelect: function(){
		//loads tresholds
		var sm = this.kpiGrid.getSelectionModel();
		var row = sm.getSelected();
	
		this.kpiInstId = row.data.id;
		this.thresholdsStore.removeAll();
		this.tresholdsCombo.clearValue();
		
		var paramsTresholds = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', MESSAGE_DET: "TRESHOLDS_LIST"};	
		var loadThr = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'MANAGE_ALARMS_ACTION'
			, baseParams: paramsTresholds
		 });
		
		Ext.Ajax.request({
	          url: loadThr,
	          params: {id: this.kpiInstId},
	          method: 'GET',
	          success: function(response, options) {
	          	
				if (response !== undefined) {		
	      			var content = Ext.util.JSON.decode( response.responseText );

	      			if(content !== undefined) {	     				
	      				this.tresholdsCombo.getStore().loadData(content);
	      				this.tresholdsCombo.getStore().commitChanges();
	      			}
				 } 	
	          }
	          ,scope: this
	    });
	}
	

	//OVERRIDING save method
	,save : function() {
		   
	   var values = this.getForm().getValues();
       var newRec = null;
       var idRec = values['id'];

	   var params = {
	      	name : values['name'],
	      	description : values['description'],
	      	text : values['text'],
	      	url : values['url'],
	      	label : values['label'] 
	   }
	   
	   if(idRec){
       	params.id = idRec;
       }
       
       var mod = 'MAIL';
       if(values['modality']==2){
          params.modality ='SMS';
          mod =	'SMS';       
       }else{
       	  params.modality ='MAIL';
       }
       
       var autoDis = false;
       if(values['autoDisabled']=='on'){
          params.autoDisabled = true;	 
          autoDis = true;         
       }else{
       	  params.autoDisabled = false;
       }
       
       var singleEv = false;
       if(values['singleEvent']=='on'){
          params.singleEvent = true;     
          singleEv = true;         
       }else{
       	  params.singleEvent = false;
       }

      var contactsSelected = Ext.getCmp("contacts-form").selModel.getSelections();
      var lengthR = contactsSelected.length;
      var contacts =new Array();
      for(var i=0;i<lengthR;i++){
        var contact ={'name':contactsSelected[i].get("name"),'id':contactsSelected[i].get("id"),
        			  'mobile':contactsSelected[i].get("mobile"),'resources':contactsSelected[i].get("resources"),
        			  'email':contactsSelected[i].get("email"),'checked':true};
		contacts.push(contact);
      }
      params.contacts =  Ext.util.JSON.encode(contacts);

      var alarmContacts = new Array();
      var tempArr = Ext.getCmp("contacts-form").store;
      var length = Ext.getCmp("contacts-form").store.data.length;

      for(var i=0;i<length;i++){
        var selected = false;
        for(var j=0;j<lengthR;j++){
        	if(contactsSelected[j].get("id")===tempArr.getAt(i).get("id")){
        		selected = true;
        		var contact ={'name':tempArr.getAt(i).get("name"),'id':tempArr.getAt(i).get("id"),
        					  'mobile':tempArr.getAt(i).get("mobile"),'resources':tempArr.getAt(i).get("resources"),
        					  'email':tempArr.getAt(i).get("email"),'checked':true};
				alarmContacts.push(contact);
        		break;
        	}
        }
        if(!selected){
        	var contact ={'name':tempArr.getAt(i).get("name"),'id':tempArr.getAt(i).get("id"),
        				  'mobile':tempArr.getAt(i).get("mobile"),'resources':tempArr.getAt(i).get("resources"),
        				  'email':tempArr.getAt(i).get("email"),'checked':false};
			alarmContacts.push(contact);
		}
       }	
        
      //kpi
       var kpiSelected = Ext.getCmp("kpi-grid").getSelectionModel().getSelected();
	   var noKpi = true;
       var kpiId;
       if(kpiSelected == undefined || kpiSelected == null ){
       		noKpi = true;
       }else{
       	   noKpi = false;
	       kpiId = kpiSelected.get("id");
	       params.kpi = kpiId;
	   }
	  
	   var noThr = true;
	   //var thrId = Ext.getCmp("tresholds-combo").value;
	   var thrId = this.tresholdsCombo.getValue();

	   if(thrId == undefined || thrId == null || thrId ==''){
	   		noThr = true;
       }else{
       	  noThr = false;
	       //threshold       	      
	      params.threshold = thrId;  
	   }

   	   if(idRec ==0 || idRec == null || idRec === ''){
	       newRec =new Ext.data.Record({'name': values['name'],'description': values['description'],'text':values['text']
	       							   ,'url': values['url'],'label': values['label'],'modality':mod,'autoDisabled':autoDis,'singleEvent':singleEv
	       							   ,'contacts': alarmContacts,'kpi': kpiId,'threshold': thrId});	       
	   }else{
			var record;
			var length = this.mainElementsStore.getCount();
			for(var i=0;i<length;i++){
	   	        var tempRecord = this.mainElementsStore.getAt(i);
	   	        if(tempRecord.data.id==idRec ){
	   	        	record = tempRecord;
				}			   
	   	    }	
			record.set('name',values['name']);
			record.set('description',values['description']);
			record.set('text',values['text']);
			record.set('url',values['url']);
			record.set('modality',mod);
			record.set('autoDisabled',autoDis);
			record.set('singleEvent',singleEv);			
			record.set('contacts', alarmContacts);
			record.set('kpi', kpiId);
			record.set('threshold', thrId);         
	  }

	  if(noKpi || noThr){
	    Ext.MessageBox.confirm(
		     	LN('sbi.alarms.confirm'),
	            LN('sbi.alarms.noThrOrKpiI'),            
	            function(btn, text) {
	                if (btn=='yes') {
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
					                        title: LN('sbi.alarms.error'),
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
										this.contactsStore.commitChanges();
										this.mainElementsStore.commitChanges();		
										if(newRec!==null){
											this.rowselModel.selectLastRow(true);
							            }
										Ext.MessageBox.show({
						                        title: LN('sbi.attributes.result'),
						                        msg: 'Operation succeded',
						                        width: 200,
						                        buttons: Ext.MessageBox.OK
						                });	
					      			 }
						      	}else{
						      		Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
						      	}
							}else{
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
				                   title: LN('sbi.alarms.validationError'),
				                   msg: errMessage,
				                   width: 400,
				                   buttons: Ext.MessageBox.OK
				              });
				     		}else{
				               Ext.MessageBox.show({
				                   title:LN('sbi.alarms.error'),
				                   msg: 'Error in Saving User',
				                   width: 150,
				                   buttons: Ext.MessageBox.OK
				              });
				     		}
				          }
				          ,scope: this
				       });	
	                }
	            },
	            this
			);  
		}else{
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
		                        title: LN('sbi.alarms.error'),
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
							this.contactsStore.commitChanges();
							this.mainElementsStore.commitChanges();	
							if(newRec!==null){
								this.rowselModel.selectLastRow(true);
				            }
				            	
							Ext.MessageBox.show({
				                        title: LN('sbi.attributes.result'),
				                        msg: 'Operation succeded',
				                        width: 200,
				                        buttons: Ext.MessageBox.OK
				                });		
		      			 }
			      	}else{
			      		Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
			      	}
				}else{
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
	                   title: LN('sbi.alarms.validationError'),
	                   msg: errMessage,
	                   width: 400,
	                   buttons: Ext.MessageBox.OK
	              });
	     		}else{
	               Ext.MessageBox.show({
	                   title:LN('sbi.alarms.error'),
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
	
	//OVERRIDING ADD METHOD
	, addNewItem : function(){

		var emptyRecToAdd =new Ext.data.Record({id:0, 
											name:'', 
											description:'',
											label:'',
											modality:'MAIL',
											singleEvent: false,
											autoDisabled: false,
											text:'',
											url: '',
											contacts: []
											});
	
		this.getForm().loadRecord(emptyRecToAdd); 
		this.fillOptions(0, emptyRecToAdd);   
        this.fillContacts(0, emptyRecToAdd); 
  		this.fillKpis(0, emptyRecToAdd);  

		this.tabs.setActiveTab(0);
	}
	

});
