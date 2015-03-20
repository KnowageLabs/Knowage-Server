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

Sbi.profiling.ManageRoles = function(config) { 

	var paramsList = {MESSAGE_DET: "ROLES_LIST"};
	var paramsSave = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "ROLE_INSERT"};
	var paramsDel = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "ROLE_DELETE"};
	
	this.configurationObject = {};
	
	this.configurationObject.manageListService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_ROLES_ACTION'
		, baseParams: paramsList
	});
	this.configurationObject.saveItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_ROLES_ACTION'
		, baseParams: paramsSave
	});
	this.configurationObject.deleteItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_ROLES_ACTION'
		, baseParams: paramsDel
	});
	
	// Meta Model Categories Services
	
	this.configurationObject.getMetaModelCategoriesService = Sbi.config.serviceRegistry.getRestServiceUrl({
		serviceName: 'domains/listValueDescriptionByType'
		, baseParams: {
				LIGHT_NAVIGATOR_DISABLED: 'TRUE',
				DOMAIN_TYPE:"BM_CATEGORY",
				EXT_VERSION: "3"
			}
	});

	
	

	var configSecurity = {};
	configSecurity.isInternalSecurity = config.isInternalSecurity;
	this.initConfigObject(configSecurity);
	config.configurationObject = this.configurationObject;
	config.singleSelection = true;
	config.configurationObject.gridWidth = 470;

	var c = Ext.apply({}, config || {}, {});

	Sbi.profiling.ManageRoles.superclass.constructor.call(this, c);	 
	
	this.rowselModel.addListener('rowselect',function(sm, row, rec) { 
		this.getForm().loadRecord(rec);  
		this.fillChecks(row, rec);
		this.enableChecks(null, rec, null);					
     }, this);

};

Ext.extend(Sbi.profiling.ManageRoles, Sbi.widgets.ListDetailForm, {
	
	configurationObject: null
	, gridForm:null
	, mainElementsStore:null
	, detailTab:null
	, authorizationTab:null
	, checkGroup: null

	,initConfigObject:function(configSecurity){
	    this.configurationObject.fields = ['id'
	                         	          , 'name'
	                        	          , 'description'
	                        	          , 'code'
	                        	          , 'typeCd'
	                        	          , 'savePersonalFolder'
	                        	          , 'saveMeta'
	                        	          , 'saveRemember'
	                        	          , 'saveSubobj'
	                        	          , 'seeMeta'
	                        	          , 'seeNotes'
	                        	          , 'seeSnapshot'
	                        	          , 'seeSubobj'
	                        	          , 'seeViewpoints'
	                        	          , 'sendMail'
	                        	          , 'buildQbe'
	                        	          , 'doMassiveExport'
	                        	          , 'manageUsers'
	                        	          , 'editWorksheet'
	                        	          , 'seeDocBrowser'
	                        	          , 'seeFavourites'
	                        	          , 'seeSubscriptions'
	                        	          , 'seeMyData'
	                        	          , 'seeToDoList'
	                        	          , 'createDocument'
	                        	          , 'bmCategories'
	                        	          , 'kpiCommentEditAll'
	                        	          , 'kpiCommentEditMy'
	                        	          , 'kpiCommentDelete'
	                        	          , 'createSocialAnalysis'
	                        	          , 'viewSocialAnalysis'
	                        	          , 'hierarchiesManagement'
	                        	          , 'enableDatasetPersistence'
	                        	        ];
		
		this.configurationObject.emptyRecToAdd = new Ext.data.Record({
											id: 0,
											name:'', 
											label:'', 
											description:'',
											typeCd:'',
											code:'',
											saveSubobj: true,
											seeSubobj:true,
											seeViewpoints:true,
											seeSnapshot:true,
											seeNotes:true,
											sendMail:true,
											savePersonalFolder:true,
											saveRemember:true,
											seeMeta:true,
											saveMeta:true,
											buildQbe:true,
											manageUsers:false,
											editWorksheet: true,
											seeDocBrowser:true,
		                        	        seeFavourites:true,
		                        	        seeSubscriptions:true,
		                        	        seeMyData:true,
		                        	        seeToDoList:true,
		                        	        createDocument:true,
		                        	        kpiCommentEditAll: true,
		                        	        kpiCommentEditMy: true,
		                        	        kpiCommentDelete: true,
		                        	        createSocialAnalysis: true,
		                        	        viewSocialAnalysis: true,
		                        	        hierarchiesManagement: true,
		                        	        enableDatasetPersistence: true,
											bmCategories: []
										});
		
		this.configurationObject.gridColItems = [
					{id:'name',header: LN('sbi.attributes.headerName'), width: 200, sortable: true, locked:false, dataIndex: 'name'},
					{header:  LN('sbi.attributes.headerDescr'), width: 220, sortable: true, dataIndex: 'description'}
				];
		
		this.configurationObject.panelTitle = LN('sbi.roles.rolesManagement');
		this.configurationObject.listTitle = LN('sbi.roles.rolesList');
		
		/*create buttons toolbar's list (Add and Synchronize buttons)*/
		if (configSecurity.isInternalSecurity !== undefined && configSecurity.isInternalSecurity == false) {
			var tbButtonsArray = new Array();
			tbButtonsArray.push(new Ext.Toolbar.Button({
		            text: LN('sbi.roles.rolesSynchronization'),
		            iconCls: 'icon-refresh',
		            handler: this.synchronize,
		            width: 30,
		            scope: this	            
		            }));
			this.configurationObject.tbListButtonsArray = tbButtonsArray;
		}
		this.initTabItems();
    }

	,initTabItems: function(){
		
		this.initDetailtab();
		this.initChecksTab();
		this.initBusinessModelTab();
		this.configurationObject.tabItems = [ this.detailTab, this.authorizationTab, this.businessModelsTab];
	}

	,initDetailtab: function() {

		this.typesStore = new Ext.data.JsonStore({
 	        fields: ['typeCd', 'valueNm'],
 	        data: config,
 	        listeners: {
	                'load': {
                        fn: function( store, records, options) {
                             for (i=0; i< records.length; i++){ 
                            	 var a = LN(records[i].data.valueNm);                            	 
                            	 var b = records[i].data.typeCd;                            	 
                            	 
                            	 records[i].set('typeCd1', b);
                            	 records[i].set('valueNm1', a);
                            	 records[i].commit();
                             }
                             
                        }
	                }
	        },
 	        autoLoad: false
 	    });
		
		//START list of detail fields
	 	   var detailFieldId = {
	                 name: 'id',
	                 hidden: true
	       };
	 		   
	 	   var detailFieldName = {
	            	 maxLength:100,
	            	 minLength:1,
	            	 //regex : new RegExp("^([a-zA-Z1-9_\x2F])+$", "g"),
	            	 regexText : LN('sbi.roles.alfanumericString'),
	                 fieldLabel: LN('sbi.roles.headerName'),
	                 allowBlank: false,
	                 validationEvent:true,
	                 //preventMark: true,
	                 name: 'name'
	             };
	 			  
	 	   var detailFieldCode = {
	            	 maxLength:20,
	            	 minLength:0,
	            	 //regex : new RegExp("^([A-Za-z0-9_])+$", "g"),
	            	 regexText : LN('sbi.roles.alfanumericString2'),
	                 fieldLabel:LN('sbi.roles.headerCode'),
	                 validationEvent:true,
	                 name: 'code'
	             };	  
	 		   
	 	   var detailFieldDescr = {
	            	 maxLength:160,
	            	 minLength:1,
	            	 //regex : new RegExp("^([a-zA-Z1-9_\x2F])+$", "g"),
	            	 regexText : LN('sbi.roles.alfanumericString'),
	                 fieldLabel: LN('sbi.roles.headerDescr'),
	                 validationEvent:true,
	                 name: 'description'
	             };


	 	   var detailFieldNodeType =  new Ext.form.ComboBox({
	 		   		  id: 'comboTypeCd',
	            	  name: 'typeCd',
	            	  hiddenName: 'typeCd',
	                  store: this.typesStore,
	                  fieldLabel: LN('sbi.roles.headerRoleType'),
	                  displayField: 'valueNm1',   // what the user sees in the popup
	                  valueField: 'typeCd1',      // what is passed to the 'change' event
	                  typeAhead: true,
	                  forceSelection: true,
	                  mode: 'local',
	                  triggerAction: 'all',
	                  selectOnFocus: false,
	                  editable: false,
	                  allowBlank: false,
	                  validationEvent:true,
	                  tpl: '<tpl for="."><div ext:qtip="{typeCd1}" class="x-combo-list-item">{valueNm1}</div></tpl>'
	             });  
	 	  
	 	   detailFieldNodeType.on('select',this.enableChecks, this);

	 	  //END list of detail fields
	 	   
	 	  this.detailTab = new Ext.Panel({
		        title: LN('sbi.roles.details')
		        , autoScroll  : true
		        , id: 'detail'
		        , layout: 'fit'
		        , items: {
		 		   	     id: 'role-detail',   	              
		 		   	     columnWidth: 0.4,
		 		   	     autoWidth: true,
			             xtype: 'fieldset',
			             labelWidth: 110,
			             defaults: {width: 220, border:false},    
			             defaultType: 'textfield',
			             autoHeight: true,
			             border: false,
			             bodyStyle: Ext.isIE ? 'padding:0 0 5px 15px;' : 'padding:10px 15px;',
			             border: false,
			             style: {
			                 "margin-left": "20px", 
			                 "margin-right": Ext.isIE6 ? (Ext.isStrict ? "-20px" : "-23px") : "20"  
			             },
			             items: [ detailFieldId, detailFieldName, detailFieldCode, 
			                      detailFieldDescr, detailFieldNodeType]
		    	}
		    });

	}
	
	/* ------------------------------------------------
	 * Business Model Categories Panel Initialization
	 * ------------------------------------------------
	 */
	
	,initBusinessModelTab: function() {
		//Invoke Service to get Categories List
		
		this.categoryStore = new Ext.data.JsonStore(
				{
					url : this.configurationObject.getMetaModelCategoriesService,
					autoLoad : true,
					root : 'domains',
					fields : [ 'VALUE_NM', 'VALUE_ID' ],
					restful : true
				});
		
		
		//create internal panel for checkbox
		this.businessModelsCheckGroup = {
		           xtype:'fieldset'
		           ,id: 'businessModelsCheckGroup'
		           //,columnWidth: 0.8
		           ,autoHeight: true
		           ,autoWidth: true
		           ,items :[]
				   ,border: false
		 	    };

		
		//Create the main panel Tab
		this.businessModelsTab = new Ext.Panel({
		        title: LN('sbi.roles.businessModels')
		        //, width: 430
		        , autoScroll: true
		        , items: [this.businessModelsCheckGroup]
		        , itemId: 'businessModelsCategoriesTab'
		        , layout: 'fit'
		    });
		
		var thisPanel = this;
		var checkBoxConfigs = [];
		this.categoryStore.load({
		    callback: function () {
		    	if (this.getRange().length > 0){
					this.getRange().forEach(function(record){
						checkBoxConfigs.push({ //pushing into array
					        id:record.data.VALUE_ID,
					        boxLabel:record.data.VALUE_NM
					    });

					});
					var myCheckboxgroup = new Ext.form.CheckboxGroup({
				        id:'businessModelsCategoriesCheckGroup',
				        fieldLabel: LN('sbi.roles.businessModels.categories'),
				        columns:1,
				        items:checkBoxConfigs,
				        boxMinWidth  : 150,
			            boxMinHeight  : 100,
			            hideLabel  : false
				    });

			        thisPanel.businessModelsTab.getComponent('businessModelsCheckGroup').add( myCheckboxgroup);
		    	}

				


		     }
		 });
		
	

		

	}
	//----------------------------------------------------------
	
	,initChecksTab: function(){
		
		 /*====================================================================
 	     * CheckGroup Is able to
 	     *====================================================================*/

 	    this.checkGroup = {
           xtype:'fieldset'
           ,id: 'checks-form'
           ,columnWidth: 0.8
           ,autoHeight: true
           ,autoWidth: true
           ,border: false
           ,items :[
				{
		            xtype: 'checkboxgroup',
		            itemId: 'isAbleToSave',
		            columns: 1,
		            boxMinWidth  : 150,
		            boxMinHeight  : 100,
		            hideLabel  : false,
		            fieldLabel: LN('sbi.roles.save')
		            ,items: [
		                {boxLabel: LN('sbi.roles.savePersonalFolder'), name: 'savePersonalFolder', checked:'savePersonalFolder',inputValue: 1},
		                {boxLabel: LN('sbi.roles.saveMeta'), name: 'saveMeta', checked:'saveMeta',inputValue: 1},
		                {boxLabel: LN('sbi.roles.saveRemember'), name: 'saveRemember', checked:'saveRemember',inputValue: 1},
		                {boxLabel: LN('sbi.roles.saveSubobj'), name: 'saveSubobj', checked:'saveSubobj',inputValue: 1}
		            ]
		        },
		        {
		            xtype: 'checkboxgroup',
		            itemId: 'isAbleToSee',
		            columns: 1,
		            boxMinWidth  : 150,
		            boxMinHeight  : 100,
		            hideLabel  : false,
		            fieldLabel: LN('sbi.roles.see'),
		            items: [
		                {boxLabel: LN('sbi.roles.seeMeta'), name: 'seeMeta', checked: 'seeMeta', inputValue: 1},
		                {boxLabel: LN('sbi.roles.seeNotes'), name: 'seeNotes', checked:'seeNotes',inputValue: 1},
		                {boxLabel: LN('sbi.roles.seeSnapshot'), name: 'seeSnapshot', checked:'seeSnapshot',inputValue: 1},
		                {boxLabel: LN('sbi.roles.seeSubobj'), name: 'seeSubobj', checked:'seeSubobj',inputValue: 1},
		                {boxLabel: LN('sbi.roles.seeViewpoints'), name: 'seeViewpoints', checked:'seeViewpoints',inputValue: 1}
		            ]
		        },
		        {
		            xtype: 'checkboxgroup',
		            columns: 1,
		            boxMinWidth  : 150,
		            hideLabel  : false,
		            fieldLabel: LN('sbi.roles.send'),
		            itemId: 'isAbleToSend',
		            //height:200,
		            items: [
		                {boxLabel: LN('sbi.roles.sendMail'), name: 'sendMail', checked:'sendMail',inputValue: 1}
		            ]
		        },
		        {
		            xtype: 'checkboxgroup',
		            columns: 1,
		            boxMinWidth  : 150,
		            hideLabel  : false,
		            fieldLabel: LN('sbi.roles.build'),
		            itemId: 'isAbleToBuild',
		            items: [
		                {boxLabel: LN('sbi.roles.buildQbe'), name: 'buildQbe', checked:'buildQbe',inputValue: 1}
		            ]
		        },
		        {
		            xtype: 'checkboxgroup',
		            columns: 1,
		            boxMinWidth  : 150,
		            hideLabel  : false,
		            fieldLabel: LN('sbi.roles.export'),
		            itemId: 'isAbleToDo',
		            items: [
		                {boxLabel: LN('sbi.roles.doMassiveExport'), name: 'doMassiveExport', checked:'doMassiveExport',inputValue: 1}
		            ]
		        },
		        {
		            xtype: 'checkboxgroup',
		            columns: 1,
		            boxMinWidth  : 150,
		            hideLabel  : false,
		            fieldLabel: LN('sbi.roles.manage'),
		            itemId: 'isAbleToManage',
		            items: [
		                {boxLabel: LN('sbi.roles.manageUsers'), name: 'manageUsers', checked:'manageUsers',inputValue: 1}
		            ]
		        },
		        {
		            xtype: 'checkboxgroup',
		            columns: 1,
		            boxMinWidth  : 150,
		            hideLabel  : false,
		            fieldLabel: LN('sbi.roles.edit'),
		            itemId: 'isAbleToEditWorksheet',
		            items: [
		                {boxLabel: LN('sbi.roles.worksheet'), name: 'editWorksheet', checked:'editWorksheet',inputValue: 1}
		            ]
		        },
		        {
		            xtype: 'checkboxgroup',
		            columns: 1,
		            boxMinWidth  : 150,
		            hideLabel  : false,
		            fieldLabel: LN('sbi.roles.edit'),
		            itemId: 'isAbleTokpiCommentEditAll',
		            items: [
		                {boxLabel: LN('sbi.roles.allKpiComment'), name: 'kpiCommentEditAll', checked:'kpiCommentEditAll',inputValue: 1}
		            ]
		        },
		        {
		            xtype: 'checkboxgroup',
		            columns: 1,
		            boxMinWidth  : 150,
		            hideLabel  : false,
		            fieldLabel: LN('sbi.roles.edit'),
		            itemId: 'isAbleTokpiCommentEditMy',
		            items: [
		                {boxLabel: LN('sbi.roles.myKpiComment'), name: 'kpiCommentEditMy', checked:'kpiCommentEditMy',inputValue: 1}
		            ]
		        },
		        {
		            xtype: 'checkboxgroup',
		            columns: 1,
		            boxMinWidth  : 150,
		            hideLabel  : false,
		            fieldLabel: LN('sbi.roles.delete'),
		            itemId: 'isAbleTokpiCommentDelete',
		            items: [
		                {boxLabel: LN('sbi.roles.kpiComment'), name: 'kpiCommentDelete', checked:'kpiCommentDelete',inputValue: 1}
		            ]
		        },
		        {
		            xtype: 'checkboxgroup',
		            columns: 1,
		            boxMinWidth  : 150,
		            hideLabel  : false,
		            fieldLabel: LN('sbi.roles.enable'),
		            itemId: 'isAbleToEnableDatasetPersistence',
		            items: [
		                    {boxLabel: LN('sbi.roles.enableDatasetPersistence'), name: 'enableDatasetPersistence', checked:'enableDatasetPersistence',inputValue: 1}
		            ]
		        },
		        {
		            xtype: 'checkboxgroup',
		            itemId: 'finalUserCan',
		            columns: 1,
		            boxMinWidth  : 150,
		            boxMinHeight  : 100,
//		            hideLabel  : false,
		            fieldLabel: LN('sbi.roles.finalUserCan'),
		            items: [
		                {boxLabel: LN('sbi.roles.seeDocumentBrowser'), name: 'seeDocBrowser', checked: 'seeDocBrowser', inputValue: 1},
		                {boxLabel: LN('sbi.roles.seeMyData'), name: 'seeMyData', checked:'seeMyData',inputValue: 1},
		                {boxLabel: LN('sbi.roles.seeFavourites'), name: 'seeFavourites', checked:'seeFavourites',inputValue: 1},
		                {boxLabel: LN('sbi.roles.seeSubscriptions'), name: 'seeSubscriptions', checked:'seeSubscriptions',inputValue: 1},
		                {boxLabel: LN('sbi.roles.seeToDoList'), name: 'seeToDoList', checked:'seeToDoList',inputValue: 1},
		                {boxLabel: LN('sbi.roles.createDocument'), name: 'createDocument', checked:'createDocument',inputValue: 1},
		                {boxLabel: LN('sbi.roles.createSocialAnalysis'), name: 'createSocialAnalysis', checked:'createSocialAnalysis',inputValue: 1},
		                {boxLabel: LN('sbi.roles.viewSocialAnalysis'), name: 'viewSocialAnalysis', checked:'viewSocialAnalysis',inputValue: 1},
		                {boxLabel: LN('sbi.roles.hierarchiesManagement'), name: 'hierarchiesManagement', checked:'hierarchiesManagement',inputValue: 1}
		                
		            ]
		        }
           ]
 	    };
 	    
 	    this.authorizationTab = new Ext.Panel({
	        title: LN('sbi.roles.authorizations')
	        , items: this.checkGroup
	        , itemId: 'checks'
	        , layout: 'fit'
	        , autoScroll: true
	    });
 	    

	}
	
	,fillChecks : function(row, rec) {
		Ext.getCmp('checks-form').items.each(function(item){	   	                   		  
        		  if(item.getItemId() == 'isAbleToSave'){
            		  item.setValue('saveMeta', rec.get('saveMeta'));
            		  item.setValue('saveRemember', rec.get('saveRemember'));
            		  item.setValue('saveSubobj', rec.get('saveSubobj'));	   	              
            		  item.setValue('savePersonalFolder', rec.get('savePersonalFolder'));
        		  }else if(item.getItemId() == 'isAbleToSee'){
            		  item.setValue('seeMeta', rec.get('seeMeta'));
            		  item.setValue('seeNotes', rec.get('seeNotes'));
            		  item.setValue('seeSnapshot', rec.get('seeSnapshot'));	   	              
            		  item.setValue('seeSubobj', rec.get('seeSubobj'));
            		  item.setValue('seeViewpoints', rec.get('seeViewpoints'));
        		  }else if(item.getItemId() == 'isAbleToSend'){
            		  item.setValue('sendMail', rec.get('sendMail'));
        		  }else if(item.getItemId() == 'isAbleToBuild'){
            		  item.setValue('buildQbe', rec.get('buildQbe'));
        		  }else if(item.getItemId() == 'isAbleToDo'){
            		  item.setValue('doMassiveExport', rec.get('doMassiveExport'));
        		  }else if(item.getItemId() == 'isAbleToManage'){
            		  item.setValue('manageUsers', rec.get('manageUsers'));
        		  }else if(item.getItemId() == 'isAbleToEditWorksheet'){
            		  item.setValue('editWorksheet', rec.get('editWorksheet'));
        		  }else if(item.getItemId() == 'isAbleTokpiCommentDelete'){
            		  item.setValue('kpiCommentDelete', rec.get('kpiCommentDelete'));
        		  }else if(item.getItemId() == 'isAbleTokpiCommentEditMy'){
            		  item.setValue('kpiCommentEditMy', rec.get('kpiCommentEditMy'));
        		  }else if(item.getItemId() == 'isAbleTokpiCommentEditAll'){
            		  item.setValue('kpiCommentEditAll', rec.get('kpiCommentEditAll'));
            	  }else if(item.getItemId() == 'isAbleToEnableDatasetPersistence'){
        			  item.setValue('enableDatasetPersistence', rec.get('enableDatasetPersistence'));
        		  }else  if(item.getItemId() == 'finalUserCan'){
        			  item.setValue('seeDocBrowser', rec.get('seeDocBrowser'));
        			  item.setValue('seeMyData', rec.get('seeMyData'));
        			  item.setValue('seeSubscriptions', rec.get('seeSubscriptions'));           
        			  item.setValue('seeFavourites', rec.get('seeFavourites'));
        			  item.setValue('seeToDoList', rec.get('seeToDoList'));
        			  item.setValue('createDocument', rec.get('createDocument'));
        			  item.setValue('createSocialAnalysis', rec.get('createSocialAnalysis'));
        			  item.setValue('viewSocialAnalysis', rec.get('viewSocialAnalysis'));
        			  item.setValue('hierarchiesManagement', rec.get('hierarchiesManagement'));
            	  }        		  
     	  });
		
		
		//fill checks for Business Model Categories
		var bmCategoriesArray = rec.get('bmCategories');

		var businessModelsCheckGroup = Ext.getCmp('businessModelsCheckGroup');
		//Force rendering check boxes if not already rendered
		businessModelsCheckGroup.doLayout();

		var businessModelsCategoriesCheckGroup = businessModelsCheckGroup.getComponent('businessModelsCategoriesCheckGroup');
		var bmCheckBoxes;
		if ((businessModelsCategoriesCheckGroup != undefined) && (businessModelsCategoriesCheckGroup.items != undefined) && (businessModelsCategoriesCheckGroup.items.items != undefined)){
			bmCheckBoxes = businessModelsCategoriesCheckGroup.items.items
		}
		
		if ((bmCheckBoxes != null) && (bmCheckBoxes !== undefined)){
			bmCheckBoxes.forEach(function(item){
		    	//set default to false
				item.setValue('false');

				//for each checkbox item
				for (var i = 0; i < bmCategoriesArray.length; i++) {
				    if(item.getItemId() == bmCategoriesArray[i]){	  			  
			      		item.setValue('true');
			  		}

				}

			});
		}
        	 
       }
	
	


	//OVERRIDING ADD METHOD
	, addNewItem : function(){

		var emptyRecToAdd = new Ext.data.Record({
								id: 0,
								name:'', 
								label:'', 
								description:'',
								typeCd:'',
								code:'',
								saveSubobj: true,
								seeSubobj:true,
								seeViewpoints:true,
								seeSnapshot:true,
								seeNotes:true,
								sendMail:true,
								savePersonalFolder:true,
								saveRemember:true,
								seeMeta:true,
								saveMeta:true,
								buildQbe:true,
								doMassiveExport:true,
								manageUsers:false,
								editWorksheet: true,
								seeDocBrowser:true,
                    	        seeFavourites:true,
                    	        seeSubscriptions:true,
                    	        seeMyData:true,
                    	        seeToDoList:true,
                    	        createDocument:true,
                    	        kpiCommentEditAll:true,
                    	        kpiCommentEditMy:true,
                    	        kpiCommentDelete:true,
                    	        createSocialAnalysis: true,
                    	        viewSocialAnalysis: true,
                    	        hierarchiesManagement: true,
                    	        enableDatasetPersistence: true,
								bmCategories: []
							});
		
		this.getForm().loadRecord(emptyRecToAdd); 
		this.fillChecks(0, emptyRecToAdd);

		this.tabs.setActiveTab(0);
	}
	

, fillRecord : function(record){
		
		var values = this.getForm().getValues();	
 
        var savePf =values['savePersonalFolder'];
        var saveSo =values['saveSubobj'];
        var seeSo =values['seeSubobj'];
        var seeV =values['seeViewpoints'];
        var seeSn =values['seeSnapshot'];
        var seeN =values['seeNotes'];
        var sendM =values['sendMail'];
        var saveRe =values['saveRemember'];
        var seeMe =values['seeMeta'];
        var saveMe =values['saveMeta'];
        var builQ =values['buildQbe'];             
        var doMassiveExport =values['doMassiveExport'];
        var manageUsers =values['manageUsers'];  
        var editWorksheet =values['editWorksheet'];
        var seeDocBrowser =values['seeDocBrowser'];  
        var seeMyData =values['seeMyData'];  
        var seeFavourites =values['seeFavourites'];  
        var seeSubscriptions =values['seeSubscriptions'];  
        var seeToDoList =values['seeToDoList'];  
        var createDocument =values['createDocument'];
        var createSocialAnalysis =values['createSocialAnalysis'];
        var viewSocialAnalysis =values['viewSocialAnalysis'];
        var hierarchiesManagement =values['hierarchiesManagement'];
        var kpiCommentEditAll =values['kpiCommentEditAll'];  
        var kpiCommentEditMy =values['kpiCommentEditMy'];  
        var kpiCommentDelete =values['kpiCommentDelete'];
        var enableDatasetPersistence =values['enableDatasetPersistence'];  

		if(savePf == 1){
        	record.set('savePersonalFolder', true);
        }else{
        	record.set('savePersonalFolder', false);
        }
        if(saveSo == 1){
        	record.set('saveSubobj', true);
        }else{
        	record.set('saveSubobj', false);
        }
        if(seeSo == 1){
        	record.set('seeSubobj', true);
        }else{
        	record.set('seeSubobj', false);
        }
        if(seeV == 1){
        	record.set('seeViewpoints', true);
        }else{
        	record.set('seeViewpoints', false);
        }
        if(seeSn == 1){
        	record.set('seeSnapshot', true);
        }else{
        	record.set('seeSnapshot', false);
        }
        if(seeN == 1){
        	record.set('seeNotes', true);
        }else{
        	record.set('seeNotes', false);
        }
        if(sendM == 1){
        	record.set('sendMail', true);
        }else{
        	record.set('sendMail', false);
        }
        if(saveRe == 1){
        	record.set('saveRemember', true);
        }else{
        	record.set('saveRemember', false);
        }
        if(seeMe == 1){
        	record.set('seeMeta', true);
        }else{
        	record.set('seeMeta', false);
        }
        if(saveMe == 1){
        	record.set('saveMeta', true);
        }else{
        	record.set('saveMeta', false);
        }
        if(builQ == 1){
        	record.set('buildQbe', true);
        }else{
        	record.set('buildQbe', false);
        }
        if(doMassiveExport == 1){
        	record.set('doMassiveExport', true);
        }else{
        	record.set('doMassiveExport', false);
        }
        if(manageUsers == 1){
        	record.set('manageUsers', true);
        }else{
        	record.set('manageUsers', false);
        }
        if(editWorksheet == 1){
        	record.set('editWorksheet', true);
        }else{
        	record.set('editWorksheet', false);
        }
        if(seeDocBrowser == 1){
        	record.set('seeDocBrowser', true);
        }else{
        	record.set('seeDocBrowser', false);
        }
        if(seeMyData == 1){
        	record.set('seeMyData', true);
        }else{
        	record.set('seeMyData', false);
        }
        if(seeFavourites == 1){
        	record.set('seeFavourites', true);
        }else{
        	record.set('seeFavourites', false);
        }
        if(seeSubscriptions == 1){
        	record.set('seeSubscriptions', true);
        }else{
        	record.set('seeSubscriptions', false);
        }
        if(seeToDoList == 1){
        	record.set('seeToDoList', true);
        }else{
        	record.set('seeToDoList', false);
        }
        if(createDocument == 1){
        	record.set('createDocument', true);
        }else{
        	record.set('createDocument', false);
        }
        if(kpiCommentEditAll == 1){
        	record.set('kpiCommentEditAll', true);
        }else{
        	record.set('kpiCommentEditAll', false);
        }
        if(kpiCommentEditMy == 1){
        	record.set('kpiCommentEditMy', true);
        }else{
        	record.set('kpiCommentEditMy', false);
        }
        if(kpiCommentDelete == 1){
        	record.set('kpiCommentDelete', true);
        }else{
        	record.set('kpiCommentDelete', false);
        }
        if(createSocialAnalysis == 1){
        	record.set('createSocialAnalysis', true);
        }else{
        	record.set('createSocialAnalysis', false);
        }
        if(viewSocialAnalysis == 1){
        	record.set('viewSocialAnalysis', true);
        }else{
        	record.set('viewSocialAnalysis', false);
        }
        if(hierarchiesManagement == 1){
        	record.set('hierarchiesManagement', true);
        }else{
        	record.set('hierarchiesManagement', false);
        }
        if(enableDatasetPersistence == 1){
        	record.set('enableDatasetPersistence', true);
        }else{
        	record.set('enableDatasetPersistence', false);
        }
        
        //Find selected business models categories
		var bmCategoriesArray = [];
		var bmItems = Ext.getCmp('businessModelsCheckGroup').getComponent('businessModelsCategoriesCheckGroup');
		if (bmItems !== undefined && bmItems !== null &&
			bmItems.items !== undefined && bmItems.items !== null	){
			var bmCheckBoxes = bmItems.items.items;
			if ((bmCheckBoxes != null) && (bmCheckBoxes !== undefined)){
				bmCheckBoxes.forEach(function(item){
			    	//if is checked
					if(item.getValue()){
						bmCategoriesArray.push(item.getItemId());
					}
	
				});
			}
			record.set('bmCategories',bmCategoriesArray);
		}
        
		return record;		
	}
	
	,save : function() {
		var values = this.getForm().getValues();
		var idRec = values['id'];
		var newRec;
	
		if(idRec ==0 || idRec == null || idRec === ''){
			newRec =new Ext.data.Record({
					name :values['name'],
			        description :values['description'],
			        typeCd :values['typeCd'],
			        code :values['code']
			});	  

			newRec = this.fillRecord(newRec);
			
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
			record.set('description',values['description']);
			record.set('typeCd',values['typeCd']);
			record.set('code',values['code']);
			
			newRec = this.fillRecord(record);
			
		}

        var params = {
        	name : newRec.data.name,
        	description : newRec.data.description,
        	typeCd : newRec.data.typeCd,
        	code : newRec.data.code,
			saveSubobj: newRec.data.saveSubobj,
			seeSubobj:newRec.data.seeSubobj,
			seeViewpoints:newRec.data.seeViewpoints,
			seeSnapshot:newRec.data.seeSnapshot,
			seeNotes:newRec.data.seeNotes,
			sendMail:newRec.data.sendMail,
			savePersonalFolder:newRec.data.savePersonalFolder,
			saveRemember:newRec.data.saveRemember,
			seeMeta:newRec.data.seeMeta,
			saveMeta:newRec.data.saveMeta,
			buildQbe:newRec.data.buildQbe,
			doMassiveExport:newRec.data.doMassiveExport,
			manageUsers:newRec.data.manageUsers,
			editWorksheet: newRec.data.editWorksheet,
			seeDocBrowser: newRec.data.seeDocBrowser,
			seeMyData: newRec.data.seeMyData,
			seeFavourites: newRec.data.seeFavourites,
			seeSubscriptions: newRec.data.seeSubscriptions,
			seeToDoList: newRec.data.seeToDoList,
			createDocument: newRec.data.createDocument,
			createSocialAnalysis: newRec.data.createSocialAnalysis,
			viewSocialAnalysis: newRec.data.viewSocialAnalysis,
			hierarchiesManagement: newRec.data.hierarchiesManagement,
			kpiCommentEditAll: newRec.data.kpiCommentEditAll,
			kpiCommentEditMy: newRec.data.kpiCommentEditMy,
			kpiCommentDelete: newRec.data.kpiCommentDelete,
			enableDatasetPersistence: newRec.data.enableDatasetPersistence,
			bmCategories: newRec.data.bmCategories
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
			                        title: LN('sbi.roles.error'),
			                        msg: content,
			                        width: 150,
			                        buttons: Ext.MessageBox.OK
			                   });
			      		}else{
			      			var roleID = content.id;
			      			if(roleID != null && roleID !==''){
			      				newRec.set('id', roleID);
			      				this.mainElementsStore.add(newRec);  
			      			}
			      			this.mainElementsStore.commitChanges();
			      			if(roleID != null && roleID !==''){
								this.rowselModel.selectLastRow(true);
				            }
			      			
			      			Ext.MessageBox.show({
			                        title: LN('sbi.attributes.result'),
			                        msg: LN('sbi.roles.resultMsg'),
			                        width: 200,
			                        buttons: Ext.MessageBox.OK
			                });

			      		}      				 

		      		} else {
		      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
		      		}
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage('Error while saving Role', 'Service Error');
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
	                    title: LN('sbi.roles.error'),
	                    msg: 'Error while Saving Role',
	                    width: 150,
	                    buttons: Ext.MessageBox.OK
	               });
	      		}
            }
            ,scope: this
        });
    }
	
	,synchronize : function() {
		var syncUrl = Sbi.config.serviceRegistry.getServiceUrl({
					  serviceName: 'MANAGE_ROLES_ACTION'
					, baseParams: {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "ROLES_SYNCHRONIZATION"}
			});
		
        Ext.Ajax.request({
            url: syncUrl,
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
			      			this.mainElementsStore.load();
			      			Ext.MessageBox.show({
			                        title: LN('sbi.roles.result'),
			                        msg: LN('sbi.roles.resultMsg'),
			                        width: 200,
			                        buttons: Ext.MessageBox.OK
			                });
			      		}      				 

		      		} else {
		      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
		      		}
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage('Error while synchronize Roles', 'Service Error');
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
	                    title: LN('sbi.roles.error'),
	                    msg: 'Error while synchronize Roles',
	                    width: 150,
	                    buttons: Ext.MessageBox.OK
	               });
	      		}
            }
            ,scope: this
        });
    }
	
	, enableChecks: function(combo, rec, idx){
		var userFuncs = this.authorizationTab.items.items[0].items.items;
		var userChecks = null;
		for (key in userFuncs) {
			var elem = userFuncs[key];
			 if (elem.itemId == 'finalUserCan'){
				 userChecks = userFuncs[key];
				 break;
			 }
		}
		
		if (rec.get('typeCd') !== undefined && rec.get('typeCd') == 'USER'){
		    userChecks.setDisabled(false);		    
		}else{					
		    userChecks.setDisabled(true);
		}	
	}

});
