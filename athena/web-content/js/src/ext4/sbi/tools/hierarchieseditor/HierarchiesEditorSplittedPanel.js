/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 *  @author
 *  Marco Cortella (marco.cortella@eng.it)
 */

Ext.define('Item', {
    extend: 'Ext.data.Model',
    fields: ['text','leafId','leafParentCode','originalLeafParentCode','leafParentName','beginDt','endDt',
             'signLev1','signLev2','signLev3','signLev4','signLev5','signLev6','signLev7','signLev8','signLev9',
             'signLev10','signLev11','signLev12','signLev13','signLev14','signLev15']
})


Ext.define('Sbi.tools.hierarchieseditor.HierarchiesEditorSplittedPanel', {
    extend: 'Sbi.widgets.compositepannel.SplittedPanel'

    ,config: {
    	isAdmin:'' 	
    }

	, constructor: function(config) {
		thisPanel = this;
		
		Ext.tip.QuickTipManager.init();
		
		this.isAdmin = config.isAdmin;
		console.log("User is Admin?: "+this.isAdmin);
		
		this.customTreeInMemorySaved = false;
		this.initServices();		
		this.createAutomaticHierarchiesPanel();
		this.createCustomHierarchiesPanel();	
		
		//Main Objects **************************************
	   
		this.mainTitle = LN('sbi.hierarchies.editor');
		
		this.leftPanel =  Ext.create('Ext.panel.Panel', {
		    bodyPadding: 5,  
			title: LN('sbi.hierarchies.automatic'),
			items: [this.automaticHierarchiesComboPanel]
		});
		
		this.rightPanel =  Ext.create('Ext.panel.Panel', {
		    bodyPadding: 5,  	
			title: LN('sbi.hierarchies.custom'),
			items: [this.newCustomHierarchyPanel,this.customHierarchiesGrid]
		});
		
		//***************************************************
   	  	
		this.treeContextMenu = Ext.create('Sbi.tools.hierarchieseditor.HierarchiesEditorContextMenu',{});
		
		this.callParent(arguments);
		
		//invokes before each ajax request 
	    Ext.Ajax.on('beforerequest', this.showMask, this);   
	    // invokes after request completed 
	    Ext.Ajax.on('requestcomplete', this.hideMask, this);            
	    // invokes if exception occured 
	    Ext.Ajax.on('requestexception', this.hideMask, this); 

	}

	
	/******************************
	 * Initializations
	 *******************************/
	
	, createAutomaticHierarchiesPanel: function(){
		this.hierarchiesStore;
		//Automatic Hierarchies Combos
		this.dimensionsStore = this.createDimensionsStore();
		
		this.comboDimensions = new Ext.form.ComboBox({
			id: 'dimensionsCombo',
			fieldLabel: LN('sbi.hierarchies.dimensions'),
			store :this.dimensionsStore,
			displayField : 'DIMENSION_NM',
			valueField :  'DIMENSION_NM',
			width : 300,
			typeAhead : true, forceSelection : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true, 
			editable : false,
			style:'padding:5px',
			listeners: {
				select:{
		               fn:function(combo, value) {
		            	   //populate hierarchies combo
		            	   this.comboHierarchies.setDisabled(false);
		            	   this.comboHierarchies.clearValue();
		            	   this.dateHierachies.setDisabled(false);
		            	   var dimensionName = value[0].get('DIMENSION_NM');
		            	   this.hierarchiesStore = this.createHierarchiesComboStore(dimensionName);
		            	   this.comboHierarchies.bindStore(this.hierarchiesStore);
		            	   //delete existing trees rendered
		            	   if ((Ext.getCmp('automaticTreePanel') != null) & (Ext.getCmp('automaticTreePanel') != undefined)){
			            	   this.leftPanel.remove(Ext.getCmp('automaticTreePanel'));
		            	   }
		            	   if ((Ext.getCmp('customTreePanel') != null) & (Ext.getCmp('customTreePanel') != undefined)){
			             	  	this.rightPanel.remove(Ext.getCmp('customTreePanel'));
		            	   }

		            	   //populate customHierarchies grid 
		            	   this.customHierarchiesGridStore = this.createCustomHierarchiesGridStore(dimensionName);
		            	   this.customHierarchiesGrid.reconfigure(this.customHierarchiesGridStore);
		            	   this.customHierarchiesGrid.setTitle(LN('sbi.hierarchies.custom.for')+" "+dimensionName);
		               }
		           }
		        ,scope:this   
			}
		});
		
		this.comboHierarchies = new Ext.form.ComboBox({
			id: 'hierarchiesCombo',
			fieldLabel: LN('sbi.hierarchies.hierarchies'),
	        queryMode: 'local',
	        displayField : 'HIERARCHY_NM',
			valueField :  'HIERARCHY_NM',
			width : 300,
			typeAhead : true,
			triggerAction : 'all',
			editable : false,
			style:'padding:5px',
			disabled: true,
//			listeners: {
//				select:{
//		               fn:function(combo, value) {
//		            	  var hierarchy = value[0].get('HIERARCHY_NM');
//		            	  var dimension = this.comboDimensions.getValue();
//		            	  this.automaticHierarchiesTreeStore = this.createAutomaticHierarchyTreeStore(dimension, hierarchy);
//		            	  this.leftPanel.remove(Ext.getCmp('automaticTreePanel'));
//		            	  var myTreePanel = this.createTreePanel(this.automaticHierarchiesTreeStore);
//		            	  this.leftPanel.add(myTreePanel);
//		            	  myTreePanel.expandAll();
//
//		               }
//		           }
//		        ,scope:this  
//			}
		});
		
		this.dateHierachies = new Ext.form.DateField({
			id: 'hierarchiesDate',
			fieldLabel: LN('sbi.hierarchies.date'),
			style:'padding:5px',
			width : 300,
			disabled: true, 
			format: Sbi.config.localizedDateFormat,
			value: new Date() //default value is today
		});
		
		this.refreshButton = Ext.create('Ext.Button', {
		    renderTo: document.body,
		    text    :  LN('sbi.hierarchies.refresh'),
		    scale   : 'small',
		    width 	: 300,
		 //   icon	: 'icon-refresh', //'hierarchies_management',  
		    listeners: {
				click:{
		               fn:function() {
		            	  var hierarchy = this.comboHierarchies.getValue();
		            	  var dimension = this.comboDimensions.getValue();
		            	  var dateHierarchy =  Ext.Date.format(this.dateHierachies.getValue(),Sbi.config.clientServerDateFormat);
		            	  if (hierarchy == null || hierarchy == undefined || 
		            		  dimension == null || dimension == undefined ||
		            		  dateHierarchy == null || dateHierarchy == undefined ){
		            		  Ext.Msg.alert(LN('sbi.hierarchies.drag.wrong.action'), LN('sbi.hierarchies.new.create.wrong'));
		            		  return;
		            	  }
		            	  this.automaticHierarchiesTreeStore = this.createAutomaticHierarchyTreeStore(dimension, hierarchy, dateHierarchy);
		            	  this.leftPanel.remove(Ext.getCmp('automaticTreePanel'));
//		            	 alert(this.automaticHierarchiesTreeStore.flatten().length);
//		            	  if (this.automaticHierarchiesTreeStore.getRootNode() != null){
		            		  var myTreePanel = this.createTreePanel(this.automaticHierarchiesTreeStore);
		            		  this.leftPanel.add(myTreePanel);
//			            	  myTreePanel.expandAll();
//		            	  }else{
//		            		  var warningPanel = this.createWarningPanel();
//		            		  this.leftPanel.add(warningPanel);
//		            		  this.doLayout();
//		            	  }		            	 
		               }
		           }
		        ,scope:this  
			}
		});

		this.automaticHierarchiesComboPanel =  Ext.create('Ext.panel.Panel', {
			layout: {
				   type: 'vbox',
				   align: 'center',
				   pack: 'center'
			},
	        bodyStyle:'padding:20px',
	        height: 185,
			items:[this.comboDimensions,this.comboHierarchies,this.dateHierachies, this.refreshButton]
		});		
	}
	
	//----------------------------------------------------
	
	, createCustomHierarchiesPanel: function(){
		//New Custom Hierarchy Panel
		
	    this.newCustomHierarchyTypeStore = new Ext.data.Store({
	        fields: ['type', 'name'],
	        data: [{
	            "type": "SEMIMANUAL",
	            "name": LN('sbi.hierarchies.type.semimanual')
	        }, {
	            "type": "MANUAL",
	            "name": LN('sbi.hierarchies.type.manual')
	        }]
	    });
	    
	    //Technical Hierarchies can be created only by Admin
	    if(this.isAdmin == "true"){
	    	this.newCustomHierarchyTypeStore.add({
	            "type": "TECHNICAL",
	            "name": LN('sbi.hierarchies.type.technical')
	        })
	    }
		
		this.newCustomHierarchyTypeCombo = new Ext.form.ComboBox({
	        fieldLabel: LN('sbi.hierarchies.new.type'),
	        store: this.newCustomHierarchyTypeStore,
	        queryMode: 'local',
	        displayField : 'name',
	        valueField: 'type',
	        labelWidth: 130,
			width : 300,
			typeAhead : true,
			triggerAction : 'all',
			editable : false,
	    });
		//select first value by default
		this.newCustomHierarchyTypeCombo.select(this.newCustomHierarchyTypeCombo.getStore().getAt(0));
		
		this.newCustomHierarchyButton = new Ext.Button({
	        text: LN('sbi.hierarchies.new.create'),
            margin: '0 0 0 10',
	        listeners: {
	            click: function() {	    
	               var hierarchy = this.comboHierarchies.getValue();
	               var dimension = this.comboDimensions.getValue();
	               var dateHierarchy =  Ext.Date.format(this.dateHierachies.getValue(),Sbi.config.clientServerDateFormat);
            	   if (hierarchy == null || hierarchy == undefined || 
	            		  dimension == null || dimension == undefined ||
	            		  dateHierarchy == null || dateHierarchy == undefined ){
	            		  Ext.Msg.alert(LN('sbi.hierarchies.drag.wrong.action'), LN('sbi.hierarchies.new.create.wrong'));
	            		  return;
	            	} 
            	
            	   //Create New Hierarchy popup window   
            	   //------------------------------------------------------
           		   this.isInsert = true;

            	   var hierarchyType = (!this.isInsert)? rec.data.HIERARCHY_TP : this.newCustomHierarchyTypeCombo.getValue();
            	   
            	   this.customHierarchyCode = new Ext.form.Text({
            		   name: 'code',
            		   fieldLabel: LN('sbi.generic.code'),
            		   labelWidth: 130,
            		   width : 300,
            		   allowBlank: false,
            		   enforceMaxLength: true,
            		   maxLength: 20,
            		   value: ''
            	   });

            	   this.customHierarchyName = new Ext.form.Text({
            		   name: 'name',
            		   fieldLabel: LN('sbi.generic.name'),
            		   labelWidth: 130,
            		   width : 300,
            		   allowBlank: false,
            		   enforceMaxLength: true,
            		   maxLength: 100,
            		   value: ''
            	   });

            	   this.customHierarchyDescription = new Ext.form.TextArea({
            		   name: 'description',
            		   fieldLabel: LN('sbi.generic.descr'),
            		   labelWidth: 130,
            		   width : 300,
            		   allowBlank: false,
            		   enforceMaxLength: true,
            		   maxLength: 255,
            		   value: ''
            	   });

            	   this.scopeComboStore = new Ext.data.Store({
            		   fields: ['type', 'name'],
            		   data: [{
            			   "type": "ALL",
            			   "name": "All"
            		   }, {
            			   "type": "OWNER",
            			   "name": "Owner"
            		   }, {
            			   "type": "POWER",
            			   "name": "Power"
            		   }]
            	   });

            	   this.scopeCombo = new Ext.form.ComboBox({
            		   fieldLabel: LN('sbi.hierarchies.scope'),
            		   store: this.scopeComboStore,
            		   queryMode: 'local',
            		   displayField : 'name',
            		   valueField: 'type',
            		   labelWidth: 130,
            		   width : 300,
            		   typeAhead : true,
            		   triggerAction : 'all',
            		   editable : false
            	   });
            	   //select first value by default
            	   this.scopeCombo.select(this.scopeCombo.getStore().getAt(0));


            	   var win = new Ext.Window(
            			   {
            				   layout: 'fit',
            				   width: 400,
            				   height: 250,
            				   modal: true,
            				   closeAction: 'destroy',
            				   title:LN('sbi.hierarchies.new.create'),
            				   items: new Ext.Panel(
            						   {

            							   bodyStyle:'padding:20px',
            							   items: [this.customHierarchyCode,this.customHierarchyName,this.customHierarchyDescription,this.scopeCombo,]
            						   }),
            						   buttons:[
            						            {
            						            	text:'OK',
            						            	handler:function() {
            						            		//editing a tree structure in memory
            						            		this.customTreeInMemorySaved = false;
            						            		//get info parameters from popup
            						            		this.newCustomHierarchyConfig = {};
            						            		this.newCustomHierarchyConfig.code = this.customHierarchyCode.getValue();
            						            		this.newCustomHierarchyConfig.name = this.customHierarchyName.getValue();
            						            		this.newCustomHierarchyConfig.description = this.customHierarchyDescription.getValue();
            						            		this.newCustomHierarchyConfig.scope = this.scopeCombo.getValue();
            						            		this.newCustomHierarchyConfig.dimension = this.comboDimensions.getValue();
            						            		this.newCustomHierarchyConfig.type = hierarchyType;
            						            		this.newCustomHierarchyConfig.isInsert = this.isInsert;

            							            	//this.saveCustomHierarchyButton.setVisible(true);
            							            	this.saveCustomHierarchyButton.setDisabled(false);
//            							            	this.cancelCustomHierarchyButton.setVisible(true);
            							            	this.cancelCustomHierarchyButton.setDisabled(false);
//            							        		this.saveChangesCustomHierarchyButton.setVisible(false);
//            							        		this.saveChangesCustomHierarchyButton.setDisabled(true);
            							            	this.createCustomHierarchyEmptyPanel(this.newCustomHierarchyConfig);
            							            	this.disableGUIElements();
            							            	
            						            		win.close();
            						            	}
            						            ,scope:this 
            						            },
            						            {
            						            	text:LN('sbi.general.cancel'),
            						            	handler:function() {
            						            		win.close();
            						            	}
            						            }
            						            ]
            			   });
            	   win.show();	            	   
            	   //--------------------------------------------------------------


	            }
				,scope:this 
	        }
	    })
		
		
		this.saveCustomHierarchyButton = new Ext.Button({
			id: 'saveCustomHierarchyButton',
	        text: LN('sbi.generic.update'),
            margin: '0 0 0 10',
	        listeners: {
	            click: function() {	            	
	            	this.saveCustomHierarchy();
	            	this.enableGUIElements();
	            }
				,scope:this 
	        }
	    })
//		this.saveCustomHierarchyButton.setVisible(false);
		this.saveCustomHierarchyButton.setDisabled(true);
		this.cancelCustomHierarchyButton = new Ext.Button({
			id: 'cancelCustomHierarchyButton',
	        text: LN('sbi.general.cancel'),
            margin: '0 0 0 10',
	        listeners: {
	            click: function() {	            	
//	            	this.saveCustomHierarchyButton.setVisible(false);
	            	this.saveCustomHierarchyButton.setDisabled(true);
//	            	this.cancelCustomHierarchyButton.setVisible(false);	 
//	            	this.cancelCustomHierarchyButton.setDisabled(true);	 
	            	this.cancelCustomHierarchy();
	            	this.enableGUIElements();
           	
	            }
				,scope:this 
	        }
	    })
//		this.cancelCustomHierarchyButton.setVisible(false);
		this.cancelCustomHierarchyButton.setDisabled(true);
		
		this.saveChangesCustomHierarchyButton = new Ext.Button({
			visible: false, // hidden because replaced by generic save button
			autoRender: true, 
			id: 'saveChangesCustomHierarchyButton',
	        text: LN('sbi.hierarchies.save.changes'),
            margin: '0 0 0 10',
	        listeners: {
	            click: function() {	            	
	            	this.modifyCustomHierarchy();
	            }
				,scope:this 
	        }
	    })
//		this.saveChangesCustomHierarchyButton.setVisible(false);
//		this.saveChangesCustomHierarchyButton.setDisabled(true);
		this.newCustomHierarchyPanel =  Ext.create('Ext.panel.Panel', {
	        bodyStyle:'padding:3px',
			layout:'table',
			layoutConfig: {
		        columns: 5
		    },
//			items:[this.newCustomHierarchyTypeCombo,this.newCustomHierarchyButton,this.saveCustomHierarchyButton,this.cancelCustomHierarchyButton,this.saveChangesCustomHierarchyButton]
		    items:[this.newCustomHierarchyTypeCombo,this.newCustomHierarchyButton,this.saveCustomHierarchyButton,this.cancelCustomHierarchyButton]
		});	
		
		
		 
		//Custom Hierarchies Grid
		
		//empty store only for initialization
		this.customHierarchiesGridStore = new Ext.data.Store({
	        storeId: 'customHierarchiesStore',
	        fields: ['HIERARCHY_CD', 'HIERARCHY_NM', 'HIERARCHY_TP']

	    });
		
		this.customHierarchiesGrid = new Ext.grid.Panel( {
	        title: LN('sbi.hierarchies.custom'),
	        store: Ext.data.StoreManager.lookup('customHierarchiesStore'),
	        columns: [{
	            header: 'Code',
	            dataIndex: 'HIERARCHY_CD',
	            flex: 1,
	            renderer: this.renderTip
	        },{
	            header: 'Name',
	            dataIndex: 'HIERARCHY_NM',
	            flex: 1,
	            renderer: this.renderTip
	        }, {
	            header: 'Type',
	            dataIndex: 'HIERARCHY_TP',
	            width: 100,
	            renderer: this.renderTip
	        }, {
				//SHOW TREE BUTTON
	        	menuDisabled: true,
				sortable: false,
				xtype: 'actioncolumn',
				width: 20,
				columnType: "decorated",
				items: [{
					tooltip: LN('sbi.hierarchies.show.tree'),
					iconCls   : 'button-detail',  
					handler: function(grid, rowIndex, colIndex) {
						thisPanel.isInsert = false;
						thisPanel.selectedRecord =  grid.store.getAt(rowIndex);
						thisPanel.onShowCustomHierarchyTree(thisPanel.selectedRecord);
					}
				}]
			}
	        , {
				//DELETE BUTTON
	        	menuDisabled: true,
				sortable: false,
				xtype: 'actioncolumn',
				width: 20,
				columnType: "decorated",
				items: [{
					tooltip: LN('sbi.hierarchies.custom.delete'),
					iconCls   : 'button-remove',  
					handler: function(grid, rowIndex, colIndex) {								
						var selectedRecord =  grid.store.getAt(rowIndex);
						thisPanel.onDeleteCustomHierarchyTree(selectedRecord);
					}
				}]
			}
	        ],
	        height: 150,
	        width: '100%',
	    })		
	}
	
	//----------------------------------------
	
	, createCustomHierarchiesGridStore: function(dimension){
		var baseParams = {}
		baseParams.dimension = dimension;
		
		
		this.services["getCustomHierarchies"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'hierarchies/getCustomHierarchies',
			baseParams: baseParams
		});
		
		var gridStore = new Ext.data.Store({
	        storeId: 'customHierarchiesStore',
	        fields: ['HIERARCHY_CD', 'HIERARCHY_NM', 'HIERARCHY_TP','HIERARCHY_DS','HIERARCHY_SC'],
	        proxy: {
	            type: 'ajax',
	            url: this.services["getCustomHierarchies"],
	            reader: {
	                type: 'json'
	            }
	        }
	        
	    });
		
		gridStore.load();
		
		return gridStore;
	}
	
	, createDimensionsStore: function(){
		Ext.define("DimensionsModel", {
    		extend: 'Ext.data.Model',
            fields: ["DIMENSION_NM","DIMENSION_DS"]
    	});
    	
    	var dimensionsStore=  Ext.create('Ext.data.Store',{
    		model: "DimensionsModel",
    		proxy: {
    			type: 'ajax',
    			url:  this.services['getDimensions'],
    			reader: {
    				type:"json"
    			}
    		}
    	});
    	dimensionsStore.load();
    	
    	return dimensionsStore;
	}
	
	, createHierarchiesComboStore: function(dimension){
		Ext.define("HierarchiesModel", {
    		extend: 'Ext.data.Model',
            fields: ["HIERARCHY_NM"]
    	});
		
		var baseParams = {}
		baseParams.dimension = dimension;
		
		this.services["getHierarchiesOfDimension"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'hierarchies/hierarchiesOfDimension',
			baseParams: baseParams
		});
		
		var hierarchiesStore=  Ext.create('Ext.data.Store',{
    		model: "HierarchiesModel",
    		proxy: {
    			type: 'ajax',
    			url:  this.services['getHierarchiesOfDimension'],
    			reader: {
    				type:"json"
    			}
    		}
    	});
		hierarchiesStore.load();
    	
    	return hierarchiesStore;		
	}
	
	, createAutomaticHierarchyTreeStore: function(dimension, hierarchy, dateHierarchy){
		var baseParams = {}
		baseParams.dimension = dimension;
		baseParams.hierarchy = hierarchy;
		baseParams.dateHierarchy = dateHierarchy;
		
		this.services["getAutomaticHierarchyTree"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'hierarchies/getAutomaticHierarchyTree',
			baseParams: baseParams
		});

		var automaticHierarchyTreeStore = new Ext.data.TreeStore({
			model:'Item',
			proxy: {
				type: 'ajax',
				url: this.services["getAutomaticHierarchyTree"],
				reader: {
					type: 'json'
				}
			},
			sorters: [{
				property: 'leaf',
				direction: 'ASC'
			}, {
				property: 'text',
				direction: 'ASC'
			}]		
			,autoload:true
		});
		return automaticHierarchyTreeStore;
		
	}	
	
	, createCustomHierarchyTreeStore: function(dimension, hierarchy){
		var baseParams = {}
		baseParams.dimension = dimension;
		baseParams.hierarchy = hierarchy;

		
		this.services["getCustomHierarchyTree"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'hierarchies/getCustomHierarchyTree',
			baseParams: baseParams
		});

		var customHierarchyTreeStore = new Ext.data.TreeStore({
			model:'Item',
			proxy: {
				type: 'ajax',
				url: this.services["getCustomHierarchyTree"],
				reader: {
					type: 'json'
				}
			}
			,autoload:true
			
		});
		return customHierarchyTreeStore;
		
	}	

	/**************************************
	 * Private methods
	 **************************************/
	//deprecated : substituted by saveCustomHierarchy
	/*
	, modifyCustomHierarchy: function(){
		
		var customTreePanel = Ext.getCmp('customTreePanel');
		var myStore = customTreePanel.getStore();   
		//this is a fake root node
		var rootNode =  myStore.getRootNode();
		//this is the real root node
		rootNode = rootNode.getChildAt(0);
		var myJson= this.getJson(rootNode)
		var params = {}
		params.code = this.selectedHierarchyCode;
		params.name = this.selectedHierarchyName;
		params.dimension = this.selectedDimensionName;
		params.root = Ext.encode(myJson);
		params.description = this.selectedHierarchyDescription;
		params.scope = this.selectedHierarchyScope;
		params.type = this.selectedHierarchyType
		
		//check the end nodes
		if (!this.checkLeafNodes(rootNode)){
			//invalid hierarchy
			Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.hierarchies.custom.error.leaf'), LN('sbi.generic.error'));
		} else {
			//valid hierarchy
			Ext.MessageBox.confirm(LN('sbi.generic.pleaseConfirm'), LN('sbi.hierarchies.save.changes.confirm'), 
					function(btn, text){
					if (btn=='yes') {
						//Call ajax function
						Ext.Ajax.request({
							url: this.services["modifyCustomHierarchy"],
							params: params,			
							success : function(response, options) {				
								if(response !== undefined  && response.responseText !== undefined && response.statusText=="OK") {
									if(response.responseText!=null && response.responseText!=undefined){
										if(response.responseText.indexOf("error.mesage.description")>=0){
											Sbi.exception.ExceptionHandler.handleFailure(response);
										}else{		
											Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.hierarchies.save.correct'));
											this.customHierarchiesGridStore.load();		     
							            }
									}
								} else {
									Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
								}
							},
							scope: this,
							failure: Sbi.exception.ExceptionHandler.handleFailure      
						});
					}
				},
				this);
		}

	}
	*/
	
	, disableGUIElements: function(){
		//Disable some GUI elements when editing a custom hierarchy to prevent inconsistency
		this.comboHierarchies.setDisabled(true);
		this.comboDimensions.setDisabled(true);
		this.dateHierachies.setDisabled(true);
		this.newCustomHierarchyTypeCombo.setDisabled(true);
		this.newCustomHierarchyButton.setDisabled(true);
		this.customHierarchiesGrid.setDisabled(true);
		this.selectedRecord = undefined; // reset selection previous records
	}
	
	, enableGUIElements: function(){
		//Enable some GUI elements after custom hierarchy save or cancel
		this.comboHierarchies.setDisabled(false);
		this.comboDimensions.setDisabled(false);
		this.dateHierachies.setDisabled(false);
		this.newCustomHierarchyTypeCombo.setDisabled(false);
		this.newCustomHierarchyButton.setDisabled(false);
		this.customHierarchiesGrid.setDisabled(false);

	}
	
	, saveCustomHierarchy: function(){

		//Open confirm Save MessageBox
		Ext.MessageBox.confirm(
				LN('sbi.generic.pleaseConfirm'),
				LN('sbi.hierarchies.save.changes.confirm'),
				function(btn, text){
					if (btn=='yes') {
						
						//get selectedRecord of the grid if it selected
						var rec = this.selectedRecord; //through the detail icon
						if (!rec){
							rec = this.customHierarchiesGrid.getSelectionModel().getSelection(); //through the selected record of the grid
							if (rec.length > 0) rec = rec[0];
							else rec = null;
						}			
					
						//check the end nodes
						var customTreePanel = Ext.getCmp('customTreePanel');
						if (customTreePanel == undefined || customTreePanel == null){
							Ext.Msg.alert(LN('sbi.hierarchies.drag.wrong.action'), LN('sbi.hierarchies.save.wrong'));
							return;
						}
						var myStore = customTreePanel.getStore();        	
						var rootNode =  myStore.getRootNode();
						if (!this.checkLeafNodes(rootNode)){
							//invalid hierarchy
							Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.hierarchies.custom.error.leaf'), LN('sbi.generic.error'));
							return;
						}
						
						//valid hierarchy
						var params = {};
						if (this.isInsert == true){
							//creating a new hierarchy
							if ((this.newCustomHierarchyConfig != undefined) && (this.newCustomHierarchyConfig != null )){
								params = this.newCustomHierarchyConfig;
							}
						}
						else if (this.isInsert == false){
							//editing an existing hierarchy
							params.isInsert = this.isInsert;
							params.code = rec.get("HIERARCHY_CD");
							params.description = rec.get("HIERARCHY_DS");
							params.name = rec.get("HIERARCHY_NM");
							params.scope = rec.get("HIERARCHY_SC");
							params.type = rec.get("HIERARCHY_TP");
							params.dimension = this.comboDimensions.getValue();

							//this is the real root node
							rootNode = rootNode.getChildAt(0);
						}
						var myJson= this.getJson(rootNode, params.type);
						//add hierarchy structure to params
						params.root = Ext.encode(myJson);
						
						params.customTreeInMemorySaved = this.customTreeInMemorySaved;
						
						
						//Only Admin can save/modify TECHNICAL Hierarchies
						if((params.type == "TECHNICAL") && (this.isAdmin == "false")){
    						Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.hierarchies.save.wrong.technical'), LN('sbi.generic.warning'));
						} else {
							//Call Save Custom Hierarchy service
	            			Ext.Ajax.request({
	            				url: this.services["saveCustomHierarchy"],
	            				params: params,			
	            				success : function(response, options) {				
	            					if(response !== undefined  && response.responseText !== undefined && response.statusText=="OK") {
	            						if(response.responseText!=null && response.responseText!=undefined){
	            							if(response.responseText.indexOf("error.mesage.description")>=0){
	            								Sbi.exception.ExceptionHandler.handleFailure(response);
	            							}else{		
	            								Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.hierarchies.save.correct'));
	            								this.customHierarchiesGridStore.load();
//	                				            	this.saveCustomHierarchyButton.setVisible(false);
	            				            	//this.saveCustomHierarchyButton.setDisabled(true);
//	                				            	this.cancelCustomHierarchyButton.setVisible(false);	
	            				            	this.cancelCustomHierarchyButton.setDisabled(true);
	            				            	this.customTreeInMemorySaved = true;
	            				            }
	            						}
	            					} else {
	            						Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
	            					}
	            				},
	            				scope: this,
	            				failure: Sbi.exception.ExceptionHandler.handleFailure      
	            			});							
						}
						

					}
				},
				this
			);
		
		
		
		/*
		this.customHierarchyCode = new Ext.form.Text({
			name: 'code',
	        fieldLabel: LN('sbi.generic.code'),
	        labelWidth: 130,
			width : 300,
	        allowBlank: false,
	        enforceMaxLength: true,
	        maxLength: 45,
	        value: (!this.isInsert)?rec.data.HIERARCHY_CD : ''
		});
		
		this.customHierarchyName = new Ext.form.Text({
			name: 'name',
	        fieldLabel: LN('sbi.generic.name'),
	        labelWidth: 130,
			width : 300,
	        allowBlank: false,
	        enforceMaxLength: true,
	        maxLength: 45,
	        value: (!this.isInsert)?rec.data.HIERARCHY_NM : ''
		});
		
		this.customHierarchyDescription = new Ext.form.Text({
			name: 'description',
	        fieldLabel: LN('sbi.generic.descr'),
	        labelWidth: 130,
			width : 300,
	        allowBlank: false,
	        enforceMaxLength: true,
	        maxLength: 45,
	        value: (!this.isInsert)? rec.data.HIERARCHY_DS : ''
		});
		
	    this.scopeComboStore = new Ext.data.Store({
	        fields: ['type', 'name'],
	        data: [{
	            "type": "ALL",
	            "name": "All"
	        }, {
	            "type": "OWNER",
	            "name": "Owner"
	        }, {
	            "type": "POWER",
	            "name": "Power"
	        }]
	    });
		
		this.scopeCombo = new Ext.form.ComboBox({
	        fieldLabel: LN('sbi.hierarchies.scope'),
	        store: this.scopeComboStore,
	        queryMode: 'local',
	        displayField : 'name',
	        valueField: 'type',
	        labelWidth: 130,
			width : 300,
			typeAhead : true,
			triggerAction : 'all',
			editable : false
	    });
		//select first value by default
		this.scopeCombo.select(this.scopeCombo.getStore().getAt(0));
		 */
		/*
		var win = new Ext.Window(
			    {
			        layout: 'fit',
			        width: 400,
			        height: 200,
			        modal: true,
			        closeAction: 'destroy',
			        title:LN('sbi.hierarchies.custom.save'),
			        items: new Ext.Panel(
			        {
						
						bodyStyle:'padding:20px',
			        	items: [this.customHierarchyCode,this.customHierarchyName,this.customHierarchyDescription,this.scopeCombo,]
			        }),
			        buttons:[
			                 {
			                	 text:'OK',
			                	 handler:function() {
			                		 var customTreePanel = Ext.getCmp('customTreePanel');
			                		 var myStore = customTreePanel.getStore();        	
			                		 var rootNode =  myStore.getRootNode();
			                		 if (!this.isInsert){
			                			 //this is the real root node
			                			 rootNode = rootNode.getChildAt(0);
			                		 }
			                		 var hierarchyType = (!this.isInsert)? rec.data.HIERARCHY_TP : this.newCustomHierarchyTypeCombo.getValue();
			                		 var myJson= this.getJson(rootNode, hierarchyType);
			                		 
			                		 var params = {};
			                		 params.root = Ext.encode(myJson);
			                		 params.code = this.customHierarchyCode.getValue();
			                		 params.name = this.customHierarchyName.getValue();
			                		 params.description = this.customHierarchyDescription.getValue();
			                		 params.scope = this.scopeCombo.getValue();
			                		 params.dimension = this.comboDimensions.getValue();
			                		 params.type = hierarchyType;
			                		 params.isInsert = this.isInsert;
			                		 
			                		 //Call ajax function
			                			Ext.Ajax.request({
			                				url: this.services["saveCustomHierarchy"],
			                				params: params,			
			                				success : function(response, options) {				
			                					if(response !== undefined  && response.responseText !== undefined && response.statusText=="OK") {
			                						if(response.responseText!=null && response.responseText!=undefined){
			                							if(response.responseText.indexOf("error.mesage.description")>=0){
			                								Sbi.exception.ExceptionHandler.handleFailure(response);
			                							}else{		
			                								Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.hierarchies.save.correct'));
			                								this.customHierarchiesGridStore.load();
//				                				            	this.saveCustomHierarchyButton.setVisible(false);
			                				            	this.saveCustomHierarchyButton.setDisabled(true);
//				                				            	this.cancelCustomHierarchyButton.setVisible(false);	
			                				            	this.cancelCustomHierarchyButton.setDisabled(true);
			                				            }
			                						}
			                					} else {
			                						Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
			                					}
			                				},
			                				scope: this,
			                				failure: Sbi.exception.ExceptionHandler.handleFailure      
			                			});

			                		 //console.log(Ext.encode(myJson));
			                		 win.close();
			                	 }
			                 	 ,scope:this 
			                 },
			                 {
			                	 text:LN('sbi.general.cancel'),
			                	 handler:function() {
			                		 win.close();
			                	 }
			                 }
			      ]
			    });
		win.show();	
		*/		
	}
	
	, cancelCustomHierarchy: function(){
		if ((Ext.getCmp('customTreePanel') != null) & (Ext.getCmp('customTreePanel') != undefined)){
			this.rightPanel.remove(Ext.getCmp('customTreePanel'));
		}
		if ((Ext.getCmp('customTreePanelTemp') != null) & (Ext.getCmp('customTreePanelTemp') != undefined)){
			this.rightPanel.remove(Ext.getCmp('customTreePanelTemp'));
		}

		this.resetCurrentAutomaticHierarchy();

	}
	
	, resetCurrentAutomaticHierarchy: function(){
		//Reload current Automatic Hierarchy selected
		var dateHierarchy = this.dateHierachies.getValue();
		var hierarchy = this.comboHierarchies.getValue();
		var dimension = this.comboDimensions.getValue();
		var dateHierarchy =  Ext.Date.format(this.dateHierachies.getValue(),Sbi.config.clientServerDateFormat);
   	    this.automaticHierarchiesTreeStore = this.createAutomaticHierarchyTreeStore(dimension, hierarchy, dateHierarchy);
		this.leftPanel.remove(Ext.getCmp('automaticTreePanel'));
		var myTreePanel = this.createTreePanel(this.automaticHierarchiesTreeStore);
		this.leftPanel.add(myTreePanel);
//		myTreePanel.expandAll();
	}
	
	, createCustomHierarchyEmptyPanel: function(config){
		if ((Ext.getCmp('customTreePanel') != null) & (Ext.getCmp('customTreePanel') != undefined)){
			this.rightPanel.remove(Ext.getCmp('customTreePanel'));
		}
		this.resetCurrentAutomaticHierarchy();
		   
		this.selectedHierarchyType = null;
		var store = Ext.create('Ext.data.TreeStore', {
			model:'Item',
			root: {
				editable: false,
				text: config.name,
				draggable: false,
				id: config.code,
				leafId:'',
				leafParentCode:'',
				originalLeafParentCode:'',
				leafParentName:'',
				children:[]
			}
		});
		/*
		var store = new Ext.data.TreeStore(
				root: {
					editable: false,
					text: config.name,
					draggable: false,
					id: config.code,
					children:[]
				});
		*/
		var customTreePanel = thisPanel.createCustomTreePanel(store,true);
		thisPanel.rightPanel.add(customTreePanel);	
		
		
		//OLD CODE TO REMOVE
		/*
		if ((Ext.getCmp('customTreePanel') != null) & (Ext.getCmp('customTreePanel') != undefined)){
        	  	this.rightPanel.remove(Ext.getCmp('customTreePanel'));
		   }
		   
		   if ((Ext.getCmp('customTreePanelTemp') != null) & (Ext.getCmp('customTreePanelTemp') != undefined)){
			   this.rightPanel.remove(Ext.getCmp('customTreePanelTemp'));
		   }
		   
		   this.resetCurrentAutomaticHierarchy();
		   
		   this.selectedHierarchyType = null;

		   //TreePanel initialized as simple panel for tree creation
		   this.treePanelRight = Ext.create('Ext.panel.Panel', {
			   id: 'customTreePanelTemp',
			   layout: {
				   type: 'vbox',
				   align: 'center',
				   pack: 'center'
			   },
			   frame: false,
			   border:true,
			   height: 200,

			   items: [
			           {
			        	   xtype: 'label',
			        	   html: '<b>'+LN('sbi.hierarchies.drag.root')+'</b>'
			           }
			   ]
			   ,listeners: {
				   'afterrender': function () {					  
					   var customTreePanelTempDropTarget = new Ext.dd.DropTarget(thisPanel.treePanelRight.getEl(), {
						   ddGroup    : 'DDhierarchiesTrees',
						   copy       : false,
						   notifyDrop : function(ddSource, e, data){
							   //console.log('drop');
							   var droppedNode = data.records[0];
							   if (!droppedNode.isLeaf()){
								   var store = new Ext.data.TreeStore();
								   var nodeClone = thisPanel.cloneNode(droppedNode); 

								   store.setRootNode(nodeClone);
								   var customTreePanel = thisPanel.createCustomTreePanel(store,true);
								   thisPanel.rightPanel.add(customTreePanel);	
								   //remove nodes from the original tree source (avoid duplicates)
								   data.records[0].remove(); 
								   Ext.getCmp('customTreePanelTemp').setVisible(false);
								   return true;
							   } else {
								   Ext.Msg.alert(LN('sbi.hierarchies.drag.wrong.action'), LN('sbi.hierarchies.drag.wrong'));
							   }

							   return false;

						   }
					   }); 

				   }
			   }           
		   });		


		   
		   this.rightPanel.add(this.treePanelRight);
		   
		   */
	}
	 
	, onShowCustomHierarchyTree: function(selectedRecord){
		this.customTreeInMemorySaved = false; 
		this.selectedHierarchyCode = selectedRecord.get('HIERARCHY_CD');
		this.selectedHierarchyName = selectedRecord.get('HIERARCHY_NM');
		this.selectedHierarchyType = selectedRecord.get('HIERARCHY_TP');
		this.selectedHierarchyDescription = selectedRecord.get('HIERARCHY_DS');
		this.selectedHierarchyScope = selectedRecord.get('HIERARCHY_SC');


		this.selectedDimensionName = this.comboDimensions.getValue();
		
		var customTreeStore = this.createCustomHierarchyTreeStore(this.selectedDimensionName,this.selectedHierarchyCode)
  	  	this.rightPanel.remove(Ext.getCmp('customTreePanel'));
		var customTreePanel = this.createCustomTreePanel(customTreeStore,false);
		this.rightPanel.add(customTreePanel);
		this.saveCustomHierarchyButton.setDisabled(false); //replace the saveChangesCustomHierarchyButton
//		customTreePanel.expandAll();
//		this.saveChangesCustomHierarchyButton.setDisabled(false);
//		this.saveChangesCustomHierarchyButton.show();
//		this.doLayout();

	}
	
	, onDeleteCustomHierarchyTree: function(selectedRecord){
		var hierarchyCode = selectedRecord.get('HIERARCHY_CD');
		var dimensionName = this.comboDimensions.getValue();
		var hierarchyType = selectedRecord.get('HIERARCHY_TP')
		
		if((hierarchyType == "TECHNICAL") && (this.isAdmin == "false")){
			Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.hierarchies.save.wrong.technical'), LN('sbi.generic.warning'));
		} else {
			var params = {};
			params.code = hierarchyCode;
			params.dimension = dimensionName;
			Ext.MessageBox.confirm(LN('sbi.generic.pleaseConfirm'), LN('sbi.generic.confirmDelete'), 
				function(btn, text){
				if (btn=='yes') {
					Ext.Ajax.request({
						url: this.services["deleteCustomHierarchy"],
						params: params,
						success : function(response, options) {
							if(response !== undefined  && response.responseText !== undefined && response.statusText=="OK") {
								if(response.responseText!=null && response.responseText!=undefined){
									if(response.responseText.indexOf("error.mesage.description")>=0){
										Sbi.exception.ExceptionHandler.handleFailure(response);
									}else{						
										Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.hierarchies.delete.ok'));
	    								this.customHierarchiesGridStore.load();
	    								this.rightPanel.remove(Ext.getCmp('customTreePanel'));
									}
								}
							} else {
								Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
							}
						},
						scope: this,
						failure: Sbi.exception.ExceptionHandler.handleFailure      
					})
				}
			},
			this);
		}


	}
	
	, createCustomTreePanel: function(store,rootVisible){
		return new Ext.tree.Panel({
	        id: 'customTreePanel',
	        layout: 'fit',
	        autoScroll: true,
	        height: 400,
	        store: store,
	        rootVisible: rootVisible,
	        multiSelect: true,
	        frame: false,
	        border:false,
	        bodyStyle: {border:0},
	        bodyStyle:'padding:20px',
	        viewConfig: {
	         plugins: {
	               ptype: 'treeviewdragdrop',
	               ddGroup: 'DDhierarchiesTrees',
	               enableDrag: true,
	               enableDrop: true
	         }
	        ,loadMask:true
	        ,listeners: {           	
	        	//listeners for drag & drop management           	
	        	viewready: function (tree) {      
	        		tree.plugins[0].dropZone.notifyDrop = function(dd, e, data){
	        			
	        			var ddOnSameTree;
	        			//check if we are performing drag&drop on the same tree
	        			if (dd.id.indexOf("customTreePanel") > -1){
	        				ddOnSameTree = true;
	        			} else {
	        				ddOnSameTree = false;
	        			}
	        			
	        			var customHierarchyType;
	        			if ((thisPanel.selectedHierarchyType == undefined) || (thisPanel.selectedHierarchyType == null)){
	        				//making a new hierarchy
		        			customHierarchyType = thisPanel.newCustomHierarchyTypeCombo.getValue();
	        			} else {
	        				//editing existing hierarchy
	        				customHierarchyType = thisPanel.selectedHierarchyType;
	        				//check if dropped nodes already exists in the tree
	        				if (!ddOnSameTree){
			        			var currentNodes = thisPanel.treeTraversal(store.getRootNode(),new Array());
			        			var newNodes = thisPanel.treeTraversal(data.records[0],new Array())
		        				for (var i = 0; i < newNodes.length; i++) {
		        					if (thisPanel.contains(currentNodes,newNodes[i])){
		    	        				Ext.Msg.alert(LN('sbi.hierarchies.drag.wrong.action'), LN('sbi.hierarchies.drag.node.exists'));
		        						return false;
		        					}
		        			    }
	        				}
	        			}
	        			var isLeafNode = data.records[0].isLeaf();

	        			if  (((customHierarchyType != "SEMIMANUAL" && customHierarchyType != "TECHNICAL") || (!isLeafNode) )
	        					|| (!isLeafNode && ddOnSameTree)) {
	        				//Original code from Ext source
	        				if(this.lastOverNode){
	        					this.onNodeOut(this.lastOverNode, dd, e, data);
	        					this.lastOverNode = null;
	        				}
	        				var n = this.getTargetFromEvent(e);
	        				return n ?
	        						this.onNodeDrop(n, dd, e, data) :
	        							this.onContainerDrop(dd, e, data);
	        			} else {
	        				Ext.Msg.alert(LN('sbi.hierarchies.drag.wrong.action'), LN('sbi.hierarchies.drag.cannot.move'));
	        				return false;
	        			}
	        		}	
	        	},
	        	beforedrop: function(node, data, overModel, dropPosition, dropFunction, options) {
	        		//alert("BeforeDrop!");
	        	},
	        	itemcontextmenu : function(view, r, node, index, e) {
	        		e.stopEvent();
	        		this.selectedNode = node;
	        		thisPanel.treeContextMenu.showAt(e.getXY());
	        		return false;
	        	}	        	
	        }	        
	        }
		});
			
	}
	
	, createTreePanel: function(store){
		var automaticTree = new Ext.tree.Panel({
	        id: 'automaticTreePanel',
	        layout: 'fit',
	        store: store,
	        autoScroll: true,
	        height: 400,
	        rootVisible: false,
	        frame: false,
	        border:false,
	        multiSelect: true,
	        bodyStyle: {border:0},
	        bodyStyle:'padding:20px',
//	        emptyText: 'Hierarchies not found for the period specified, try with another date.',
	        viewConfig: {
	            plugins: {
	               ptype: 'treeviewdragdrop',
	               ddGroup: 'DDhierarchiesTrees',
	               enableDrag: true,
	               enableDrop: false,
	               copy: false
	            },
	            loadMask:true
	        }
	    });
		return automaticTree;
			
	}
	
	, createWarningPanel: function(){
		var warning = new Ext.Panel({
	        id: 'automaticWarning',
	        layout: 'vbox',
	        align: 'center',
			pack: 'center',
	        height: 400,
	        items: [{
                xtype:'label',
                text:  'Hierarchies not found for the period specified, try with another date.',
                name: 'lblWarning',
                style: 'font-weight:bold;color:grey;text-align:center;vertical-align:middle;',
                height: 100,
                margin: '20 20 20 20'
//                anchor:'93%'
             }]
	    });
		return warning;
	}
	
    , renderTip: function(val, meta, rec, rowIndex, colIndex, store) {
        meta.tdAttr = 'data-qtip="'+ 
        	"<b>"+LN('sbi.generic.code')+": </b>"+rec.get('HIERARCHY_CD') + "<br> "+
        	"<b>"+LN('sbi.generic.name')+": </b>"+rec.get('HIERARCHY_NM') + "<br> "+
        	"<b>"+LN('sbi.generic.type')+": </b>"+rec.get('HIERARCHY_TP')+"<br> " +
        	"<b>"+LN('sbi.generic.descr')+": </b>"+rec.get('HIERARCHY_DS')+"<br> " +
        	"<b>"+LN('sbi.hierarchies.scope')+": </b>"+rec.get('HIERARCHY_SC')
        	+'"';

        return val;
    }
	
	//clone a Ext.data.NodeInterface with deep copy
	, cloneNode: function(node) {
		var result = node.copy(),
		len = node.childNodes ? node.childNodes.length : 0,
				i;
		// Move child nodes across to the copy if required
		for (i = 0; i < len; i++)
			result.appendChild(this.cloneNode(node.childNodes[i]));
		return result;
	}
	//explore each node of the tree starting from the passed root
	, treeTraversal: function(node,array){
		array.push(node.data.id);
		
		node.eachChild(function(child) {
			thisPanel.treeTraversal(child,array); // handle the child recursively
		});
        
        //console.log(array);
        return array;
	}
	/**
	 * check if the nodes at the ending position are real leaf nodes
	 * returns true if all end nodes are leafs
	 */
	, checkLeafNodes: function(rootNode){
		var endNodes = this.getEndNodes(rootNode, new Array());
		for (var i = 0; i < endNodes.length; i++){
			if (!endNodes[i].isLeaf()){
				//not a real leaf node
				return false;
			}
		}
		return true;
	}
	//get only the nodes that don't have children
	, getEndNodes: function(node,array){
		if (!node.hasChildNodes()){
			array.push(node);
		}
		
		node.eachChild(function(child) {
			thisPanel.getEndNodes(child,array); // handle the child recursively
		});
        
        //console.log(array);
        return array;
	}
	
	//check if the passed array a contains an object obj
	, contains: function(a, obj) {
	    var i = a.length;
	    while (i--) {
	       if (a[i] === obj) {
	           return true;
	       }
	    }
	    return false;
	}
	
	//Transform the Tree structure in a JSON form that can be converted to a string
	//node is the rootNode
	,getJson: function(node,hierarchyType) {
		// Should deep copy so we don't affect the tree
		var json = node.data;

		json.children = [];
		for (var i=0; i < node.childNodes.length; i++) {
			if ((hierarchyType != undefined) && ((hierarchyType == 'MANUAL') || (hierarchyType == 'TECHNICAL'))){
				var jsonNode = this.getJson(node.childNodes[i],hierarchyType);
				//for manual hierarchy get the new parent reference
				if(node.childNodes[i] !== undefined && node.childNodes[i].parentNode != null){
					jsonNode.leafParentCode = jsonNode.parentId;
					jsonNode.leafParentName = node.childNodes[i].parentNode.data.text;
					json.children.push( jsonNode );
				}
			}else{			
				json.children.push( this.getJson(node.childNodes[i]) );
			}		
		}
		return json;
	}
	
	/***********************************
	 * REST services for Ajax calls
	 ***********************************/
	, initServices : function(baseParams) {
		this.services = [];
		
		if(baseParams == undefined){
			baseParams ={};
		}
		
		this.services["getDimensions"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'hierarchies/dimensions',
			baseParams: baseParams
		});
		
		this.services["getHierarchiesOfDimension"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'hierarchies/hierarchiesOfDimension',
			baseParams: baseParams //must specify a dimension parameter
		});
		
		this.services["getAutomaticHierarchyTree"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'hierarchies/getAutomaticHierarchyTree',
			baseParams: baseParams //must specify a dimension and hierarchy parameters
		});
		
		this.services["getCustomHierarchies"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'hierarchies/getCustomHierarchies',
			baseParams: baseParams //must specify a dimension parameter
		});
		
		this.services["getCustomHierarchyTree"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'hierarchies/getCustomHierarchyTree',
			baseParams: baseParams //must specify a dimension and hierarchy parameters
		});
		
		this.services["saveCustomHierarchy"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'hierarchies/saveCustomHierarchy',
			baseParams: baseParams 
		});
		
		this.services["deleteCustomHierarchy"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'hierarchies/deleteCustomHierarchy',
			baseParams: baseParams 
		});
		
		this.services["modifyCustomHierarchy"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'hierarchies/modifyCustomHierarchy',
			baseParams: baseParams 
		});		
		
	}	
	
	/**
	 * Opens the loading mask 
	*/
    , showMask : function(){
    	this.un('afterlayout',this.showMask,this);
    	if (this.loadMask == null) {    		
    		this.loadMask = new Ext.LoadMask(Ext.getBody(), {msg: "  Wait...  "});
    	}
    	if (this.loadMask){
    		this.loadMask.show();
    	}
    }

	/**
	 * Closes the loading mask
	*/
	, hideMask: function() {
    	if (this.loadMask && this.loadMask != null) {	
    		this.loadMask.hide();
    	}
	} 

});

