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
/* 	configuration parameters
 *  config.hideContextMenu : to hide right click context menu
 *  config.showModelUuid : to show radio choose to get ModelUUid
 * */
Ext.ns("Sbi.kpi");

Sbi.kpi.ManageModelInstances = function(config, ref) { 
	
	var hideContextMenu = config.hideContextMenu;//to hide right click context menu
	this.showModelUuid = config.showModelUuid;
	
	var paramsList = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "MODELINSTS_NODES_LIST"};
	var paramsSave = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "MODELINSTS_NODES_SAVE"};
	var paramsDel = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "MODELINSTS_NODE_DELETE"};
	var periodsList = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "PERIODICTIES_LIST"};
	var paramsKpiRestore = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "MODELINSTS_KPI_RESTORE"};
	
	this.configurationObject = {};
	
	this.configurationObject.manageTreeService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_MODEL_INSTANCES_ACTION'
		, baseParams: paramsList
	});	
	this.configurationObject.saveTreeService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_MODEL_INSTANCES_ACTION'
		, baseParams: paramsSave
	});
	this.configurationObject.deleteTreeService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_MODEL_INSTANCES_ACTION'
		, baseParams: paramsDel
	});
	this.configurationObject.kpiRestore = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_MODEL_INSTANCES_ACTION'
		, baseParams: paramsKpiRestore
	});
	this.configurationObject.periodicitiesList = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_PERIODICITIES_ACTION'
		, baseParams: periodsList
	});
	//reference to viewport container
	this.referencedCmp = ref;
	this.initConfigObject();
	config.configurationObject = this.configurationObject;
	
	config.hideContextMenu = hideContextMenu;
	
	var c = Ext.apply({}, config || {}, {});

	Sbi.kpi.ManageModelInstances.superclass.constructor.call(this, c);	 	
	
};

Ext.extend(Sbi.kpi.ManageModelInstances, Sbi.widgets.TreeDetailForm, {
	
	configurationObject: null
	, gridForm:null
	, mainElementsStore:null
	, root:null
	, referencedCmp : null
	, droppedSubtreeToSave: new Array()
	, kpitreeLoader : null
	, newRootNode: null
	, existingRootNode: null
	, showModelUuid : false

	,initConfigObject: function(){

		this.configurationObject.panelTitle = LN('sbi.modelinstances.panelTitle');
		this.configurationObject.listTitle = LN('sbi.modelinstances.listTitle');

		this.initTabItems();

    }

	,initTabItems: function(){
		
		this.kpitreeLoader =new Ext.tree.TreeLoader({
			dataUrl: this.configurationObject.manageTreeService,
	        createNode: function(attr) {
			
	            if (attr.modelInstId) {
	                attr.id = attr.modelInstId;
	            }

	    		if (attr.kpiInstId !== undefined && attr.kpiInstId !== null
	    				&& attr.kpiInstId != '') {
	    			attr.iconCls = 'has-kpi';
	    		}
	    		if (attr.error !== undefined && attr.error !== false) {
	    			attr.cls = 'has-error';
	    		}
	    		var attrKpiCode = '';
	    		if(attr.kpiCode !== undefined){
	    			attrKpiCode = ' - '+attr.kpiCode;
	    		}
	    		attr.qtip = attr.modelCode+' - '+attr.name+ attrKpiCode;
	            return Ext.tree.TreeLoader.prototype.createNode.call(this, attr);
	        }

		});
		//Store of the combobox
 	    this.typesStore = new Ext.data.SimpleStore({
 	        fields: ['typeId', 'typeCd', 'typeDs', 'domainCd'],
 	        data: config.nodeTypesCd,
 	        autoLoad: false
 	    });
		/*DETAIL FIELDS*/
		   
	 	   this.detailFieldLabel = new Ext.form.TextField({
	        	 minLength:1,
	             fieldLabel:LN('sbi.generic.label'),
	             allowBlank: false,
	             enableKeyEvents: true,
	             //validationEvent:true,
	             name: 'label'
	         });	  
	 	   
	 	   this.detailFieldName = new Ext.form.TextField({
	          	 maxLength:100,
	        	 minLength:1,
	             fieldLabel: LN('sbi.generic.name'),
	             allowBlank: false,
	             enableKeyEvents: true,
	             //validationEvent:true,
	             name: 'name'
	         });
  
	 		   
	 	   this.detailFieldDescr = new Ext.form.TextArea({
	          	 maxLength:400,
	       	     width : 250,
	             height : 80,
	             enableKeyEvents: true,
	             fieldLabel: LN('sbi.generic.descr'),
	             //validationEvent:true,
	             name: 'description'
	         });

	 	   /*END*/
	 	  this.kpiInstItems = new Ext.Panel({
		        title: LN('sbi.modelinstances.kpiInstance')
			        , layout: 'fit'
			        , autoScroll: true
			        , items: []
			        , itemId: 'kpiInstItemsTab'
			        , scope: this
			});
	 	  
	 	  this.initSourcePanel();
	 	  this.initKpiPanel();
	 	  
	 	  this.configurationObject.tabItems = [{
		        title: LN('sbi.generic.details')
		        , itemId: 'detail'
		        , width: 430
		        , items: [{

		 		   	 itemId: 'items-detail1',   	              
		 		   	 columnWidth: 0.8,
		             xtype: 'fieldset',
		             labelWidth: 90,
		             defaults: {width: 140, border:false},    
		             defaultType: 'textfield',
		             autoHeight: true,
		             autoScroll  : true,
		             bodyStyle: Ext.isIE ? 'padding:15 0 5px 10px;' : 'padding:10px 15px;',
		             border: false,
		             style: {
		                 //"background-color": "#f1f1f1",
		                 "margin-right": Ext.isIE6 ? (Ext.isStrict ? "-10px" : "-13px") : "0"  
		             },
		             items: [this.detailFieldLabel, this.detailFieldName,  this.detailFieldDescr]
		    	}]
		    }, {
		        title: LN('sbi.modelinstances.kpiInstance')
			        , itemId: 'kpi_model'
			        , width: 430
			        , bodyStyle: Ext.isIE ? 'padding:15 0 5px 10px;' : 'padding:10px 15px;'
			        , items: [this.kpiInstTypeFieldset ,
			                  this.kpiInstFieldset, 
			                  this.kpiInstFieldset2]
			    },{
			        title: LN('sbi.modelinstances.srcNode')
				        , itemId: 'src_model'
				        , width: 430
				        , bodyStyle: Ext.isIE ? 'padding:15 0 5px 10px;' : 'padding:10px 15px;'
				        , items: [{	
				 		   	 itemId: 'src-detail',   	              
				 		   	 columnWidth: 0.4,
				             xtype: 'fieldset',
				             labelWidth: 90,
				             defaults: {width: 140, border:false},    
				             defaultType: 'textfield',
				             autoHeight: true,
				             autoScroll  : true,
				             bodyStyle: Ext.isIE ? 'padding:0 0 5px 15px;' : 'padding:10px 15px;',
				             border: false,
				             style: {
				                 //"background-color": "#f1f1f1",
				                 "margin-right": Ext.isIE6 ? (Ext.isStrict ? "-10px" : "-13px") : "0"  
				             },
				             items: [this.srcModelName,
							         this.srcModelCode,
							         this.srcModelDescr,
							         this.srcModelType,
							         this.srcModelTypeDescr ]
				    	}]
				    }
			    ];
	 	  
	}
	, initSourcePanel: function() {
	 	   this.srcModelName = new Ext.form.TextField({
	             fieldLabel:LN('sbi.generic.name'),
	             readOnly:true,
	             style: '{ color: #ffffff; border: 1px solid white; font-style: italic;}',
	             name: 'srcname'
	         });	  

	 	   this.srcModelCode = new Ext.form.TextField({
	             readOnly: true,
	             
	             fieldLabel: LN('sbi.generic.code'),
	             style: '{  color: #ffffff; border: 1px solid white; font-style: italic;}',
	             name: 'srccode'
	         });

	 	   this.srcModelDescr = new Ext.form.TextArea({
	 		   	 readOnly: true,
	 		   	
	          	 maxLength:400,
	       	     width : 250,
	             height : 80,
	             style: '{  color: #ffffff; border: 1px solid #fff; font-style: italic;}',
	             fieldLabel: LN('sbi.generic.descr'),
	             name: 'srcdescription'
	         });

	 	     this.srcModelType = new Ext.form.TextField({
	 		   	 readOnly: true,
	 		   	
	 		   	style: '{  color: #ffffff; border: 1px solid #fff; font-style: italic;}',
	             fieldLabel: LN('sbi.generic.nodetype'),
	             name: 'srctype'
	         });

		 	 this.srcModelTypeDescr = new Ext.form.TextField({
	             readOnly: true,
	             
	             style: '{  color: #ffffff; border: 1px solid #fff; font-style: italic;}',
	             fieldLabel: LN('sbi.generic.nodedescr'),
	             name: 'srctypeDescr'
	         });

	}
	, launchPeriodicityWindow : function() {
		
		var conf = {};
		
		var managePeriodicities = new Sbi.kpi.ManagePeriodicities(conf);
	
		this.perWin = new Ext.Window({
			title: LN('sbi.modelinstances.periodicitiesList') ,   
            layout      : 'fit',
            width       : 400,
            height      : 300,
            closeAction :'close',
            modal		: true,
            plain       : true,
            scope		: this,
            constrain   : true,
            constrainHeader : true,
            items       : [managePeriodicities]
		});
		
		this.perWin.show();
		this.perWin.on('close', function(panel){
			var st = Ext.StoreMgr.lookup('kpiperiodicitystore');
			st.load();
		}, this);
		
	}
	,launchThrWindow : function() {
		
		var conf = {};
		conf.nodeTypesCd = config.thrTypes;
		conf.drawSelectColumn = true;

		
		var manageThresholds = new Sbi.kpi.ManageThresholds(conf);
	
		this.thrWin = new Ext.Window({
			title: LN('sbi.lookup.Select') ,   
            layout      : 'fit',
            width       : 1000,
            height      : 400,
            closeAction :'close',
            plain       : true,
            scope		: this,
            constrain   : true,
            constrainHeader : true,
            items       : [manageThresholds]
		});
		manageThresholds.on('selectEvent', function(itemId ,index, code){
												this.thrWin.close();
												Ext.getCmp('kpiThresholdF').setValue(code);	
												Ext.getCmp('kpiThresholdF').fireEvent('changeThr', code);
											}, this);
		this.thrWin.show();
	}
	, editNodeAttribute: function(field, event) {	
		if( this.selectedNodeToEdit === undefined ||  this.selectedNodeToEdit === null){
			this.selectedNodeToEdit = this.mainTree.getSelectionModel().getSelectedNode();
		}
		var node = this.selectedNodeToEdit;
		if (node !== undefined && node !== null) {
			node.attributes.toSave = true;
			var fName = field.name;

			node.attributes[fName] = field.getValue();
			if(fName == 'name'){
				var rec = this.referencedCmp.modelInstancesGrid.getSelectionModel().getSelected();
				rec.data.name = field.getValue();
				this.referencedCmp.modelInstancesGrid.mainElementsStore.commitChanges();
				this.referencedCmp.modelInstancesGrid.getView().refresh();
				var val = node.attributes.text;
				var aPosition = val.indexOf(" - ");
				var name = "";
				var code = "";
				if (aPosition !== undefined && aPosition != -1) {
					name = val.substr(aPosition + 3);
					code = val.substr(0, aPosition);
					if (field.getName() == 'name') {
						name = field.getValue();
					} 
				}
				var text = code + " - " + name;
				node.setText(text);
			}
		}
	}
	
	, editThreshold: function(code){
		this.selectNode(null);
		var node = this.selectedNodeToEdit;
		if(node === undefined || node === null){
			node = this.mainTree.getSelectionModel().getSelectedNode();
		}
		node.attributes.kpiInstThrName = code;
		
		
	}

	,selectNode : function(field) {
		
		/*utility to store node that has been edited*/
		this.selectedNodeToEdit = this.mainTree.getSelectionModel().getSelectedNode();
		
		if(this.selectedNodeToEdit.attributes.toSave === undefined || this.selectedNodeToEdit.attributes.toSave === false){
			var size = this.nodesToSave.length;
			this.nodesToSave[size] = this.selectedNodeToEdit;
		}//else skip because already taken
		
	}
	, initKpiPanel: function() {

		if(this.showModelUuid){
			this.kpiModelType = new Ext.form.RadioGroup({
	            fieldLabel: LN('sbi.generic.type'),	             
	    	    id:'kpiModelType',
	    	    xtype: 'radiogroup',
	    	    readonly: true,
	    	    columns: 2,
	    	    items: [
	    	        {boxLabel: 'UUID', id:'uuid',name: 'kpiTypeRadio', inputValue: 1},
	    	        {boxLabel: 'Kpi Instance', id:'kpiinst',name: 'kpiTypeRadio', inputValue: 2}
	    	    ]
	    	});
			this.kpiModelType.addListener('change', this.changeKpiPanel , this);
		}
		this.kpiInstTypeFieldset = new Ext.form.FieldSet({
		   	columnWidth: 1,
            labelWidth: 90,   
            autoHeight: true,
            autoScroll  : true,
            border: false,
            /*            bodyStyle: Ext.isIE ? 'padding:0 0 5px 5px;' : 'padding: 5px;',            
            style: {
            	"background-color": "#f1f1f1",
                "margin-right": Ext.isIE6 ? (Ext.isStrict ? "-10px" : "-13px") : "0"  
            },*/

            items: [ ]
		});
		if(this.showModelUuid){
			this.kpiInstTypeFieldset.add(this.kpiModelType);
		}else{
			this.kpiInstTypeFieldset.hide();
			this.kpiInstTypeFieldset.setSize(0,0);
		}
 	    this.kpiName = new Ext.form.TextField({
 	    	 columnWidth: .75,
 	    	 id: 'kpinameField',
 	    	 enableKeyEvents: true,
             fieldLabel:LN('sbi.generic.kpi'),
             style: '{ color: #74B75C; border: 1px solid #74B75C; font-style: italic}',
             readOnly: true,
             width: 30,
             name: 'kpiName'
         });	  
 	    this.kpiName.addListener('render', this.configureDD2, this);
 	    this.kpiName.addListener('focus', this.kpiFiledNotify, this);

 	    this.kpiClearBtn = new Ext.Button({
			iconCls: 'icon-clear'
			, tooltip: LN('sbi.generic.deleteKpi')
    		, style: '{border:none; width: 30px; border:none; margin-left: 5px; float: left;}'
			, scope: this
			, handler: this.clearKpi
			, columnWidth: .25
		});
		this.kpiPanel = new Ext.Panel({
			fieldLabel:LN('sbi.generic.kpi'),
			labelWidth: 90,
            defaults: {width: 140, border:false},   
            layout : 'column',
			items: [this.kpiName,
			        this.kpiClearBtn]
			, width: 30
		});

 	 	 this.kpiThreshold = new Ext.form.TriggerField({
 		     triggerClass: 'x-form-search-trigger',
 		     fieldLabel: LN('sbi.kpis.threshold'),
 		     name: 'kpiInstThrName',
 		     id: 'kpiThresholdF'
 		    });
 	 	 this.kpiThreshold.onTriggerClick = this.launchThrWindow;

		this.kpiWeight = new Ext.form.NumberField({
            readOnly: false,
            enableKeyEvents: true,
            fieldLabel: LN('sbi.kpis.weight'),
            name: 'kpiInstWeight'
        });
		
		this.kpiTarget = new Ext.form.NumberField({
            readOnly: false,
            enableKeyEvents: true,
            fieldLabel: LN('sbi.modelinstances.target'),
            name: 'kpiInstTarget'
        });

		this.perReader = new Ext.data.JsonReader({
			    totalProperty: 'total',
			    successProperty: 'success',
			    idProperty: 'idPr',
			    root: 'rows',
			    messageProperty: 'message'  // <-- New "messageProperty" meta-data
			}, [
			    {name: 'idPr'},
			    {name: 'name'}
		]);
	    
		this.periodicityStore = new Ext.data.Store({
			proxy: new Ext.data.HttpProxy({
					url: this.configurationObject.periodicitiesList
					
			})
			, root: 'rows'
		   	, reader: this.perReader 
	        , fields: ['idPr', 'name']        
	        , storeId: 'kpiperiodicitystore'	       
	        , autoLoad: false
	    });  
		this.periodicityStore.load();
		Ext.StoreMgr.register (this.periodicityStore);

		this.kpiPeriodicity = new Ext.form.ComboBox({
			columnWidth: .75,			
      	    name: 'kpiInstPeriodicity',
            store: this.periodicityStore,
            fieldLabel: LN('sbi.modelinstances.periodicity'),
            displayField: 'name',   // what the user sees in the popup
            valueField: 'idPr',        // what is passed to the 'change' event
            typeAhead: true,
            forceSelection: false,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus: true,
            editable: false
        }); 

		this.periodicityPanel = new Ext.Panel({
			fieldLabel:LN('sbi.modelinstances.periodicity'),
			labelWidth: 90,
            defaults: {width: 140, border:false},   
            layout : 'column',
			items: [this.kpiPeriodicity,
			        new Ext.Button({
						tooltip: LN('sbi.modelinstances.periodicityAdd')
						, style: '{border:none; width: 30px; border:none; margin-left: 5px;}'
						, scope: this
						, iconCls :'icon-add'
						, handler: this.launchPeriodicityWindow
						, columnWidth: .25
					})]
			, width: 30
		});
		//---------------chart type------------------------------
 	    //Store of the kpi chart types combobox
	    this.chartTypeStore = new Ext.data.SimpleStore({
	        fields: ['kpiChartTypeId', 'kpiChartTypeCd'],
	        data: config.kpiChartTypes,
	        autoLoad: false
	    });
		this.kpiChartType =  new Ext.form.ComboBox({
	      	    name: 'kpiInstChartTypeId',
	            store: this.chartTypeStore,
	            fieldLabel: LN('sbi.modelinstances.chartType'),
	            displayField: 'kpiChartTypeCd',   // what the user sees in the popup
	            valueField: 'kpiChartTypeId',        // what is passed to the 'change' event
	            typeAhead: true,
	            mode: 'local',
	            triggerAction: 'all',
	            selectOnFocus: true,
	            editable: false
	        }); 
		//alternate if uuid
		this.kpiLabel = new Ext.form.TextField({
             readOnly: false,
             enableKeyEvents: true,
             fieldLabel: LN('sbi.generic.label'),
             name: 'modelUuid'
         });
		
        this.kpiSaveHistory = new Ext.form.Checkbox({
            fieldLabel     : LN('sbi.modelinstances.saveHistory'),
            labelSeparator : ' ',
            boxLabel       : ' ',
            inputValue     : true
        });

		this.kpiRestoreDefault = new Ext.Button({
			iconCls :'icon-add',
			text: LN('sbi.modelinstances.restoreDefault'),
			handler: this.restoreDefaults

		});
		this.kpiRestoreDefaultBtn = new Ext.Button({
			tooltip: LN('sbi.modelinstances.restoreDefault')						
				, scope: this
				, iconCls :'icon-refresh'
				, handler: this.restoreDefaults
				, columnWidth: .3
				, text: LN('sbi.modelinstances.restoreDefault')
			});
		this.kpiRestoreDefault = new Ext.Panel({
            layout : 'column',
			items: [this.kpiRestoreDefaultBtn
			        ]
			, width: 30
		});
		this.kpiInstFieldset = new Ext.form.FieldSet({
	             labelWidth: 90,
	             defaults: {width: 140, border:false},    
	             autoHeight: true,
	             autoScroll  : true,
	             border: false,
	             style: {
	                 "margin-right": Ext.isIE6 ? (Ext.isStrict ? "-10px" : "-13px") : "0"  
	             },
	             items: [
	                     this.kpiPanel,
	                     this.kpiThreshold,
	                     this.kpiWeight,
	                     this.kpiTarget,
	                     this.periodicityPanel,
	                     this.kpiChartType,
	                     this.kpiSaveHistory ,
	                     this.kpiRestoreDefault
	                     
	                     ]
	    	});
		
			this.kpiInstFieldset2 = new Ext.form.FieldSet({
			   	 columnWidth: 0.4,
	            labelWidth: 90,
	            defaults: {width: 140, border:false},    
	            autoHeight: true,
	            autoScroll  : true,
	            //bodyStyle: Ext.isIE ? 'padding:0 0 5px 15px;' : 'padding:10px 15px;',
	            border: false,
	            style: {
	                //"background-color": "#D3DAED",
	                "margin-right": Ext.isIE6 ? (Ext.isStrict ? "-10px" : "-13px") : "0"  
	            },
	            items: [
	                    this.kpiLabel]
			});

			this.kpiInstFieldset.setVisible(false);
			this.kpiInstFieldset2.setVisible(false);
			


	}
	, clearKpi: function() {
		//checks for periodicity:
		var periodicityVal = this.kpiPeriodicity.getValue();
		if(periodicityVal !== undefined && periodicityVal != null && periodicityVal != ''){
			//if periodicity is set--> confirm and delete periodicity
			Ext.Msg.confirm(LN('sbi.generic.confirmDelete'), LN('sbi.modelinstances.confirm.periodicity.deletion'), function(btn, text){
			    if (btn == 'yes'){

					this.kpiName.setValue('');
					this.kpiPeriodicity.clearValue();
					var node = this.mainTree.getSelectionModel().getSelectedNode() ;
					if(node !== undefined && node !== null){
						node.attributes.kpiName = '';
						node.attributes.kpiId = '';
						node.attributes.kpiInstId = '';
						node.attributes.iconCls = '';
						node.attributes.kpiInstPeriodicity ='';
						Ext.fly(node.getUI().getIconEl() ).replaceClass('has-kpi', '');
					}
			    }else{
			    	return;
			    }
			}, this);

		}else{
			this.kpiName.setValue('');
			var node = this.mainTree.getSelectionModel().getSelectedNode() ;
			if(node !== undefined && node !== null){
				node.attributes.kpiName = '';
				node.attributes.kpiId = '';
				node.attributes.kpiInstId = '';
				node.attributes.iconCls = '';
				Ext.fly(node.getUI().getIconEl() ).replaceClass('has-kpi', '');
			}
		}
	}
	, kpiFiledNotify : function() {
		this.kpiName.getEl().highlight('#E27119');
		var node = this.mainTree.getSelectionModel().getSelectedNode() ;
		var tooltip = new Ext.ToolTip({
	        target: 'kpinameField',
	        anchor: 'right',
	        trackMouse: true,
	        html: LN('sbi.modelinstances.DDKpiMsg')
	    });

	}
	, configureDD2: function() {
		  var fieldDropTargetEl =  this.kpiName.getEl().dom; 
		  var formPanelDropTarget = new Ext.dd.DropTarget(fieldDropTargetEl, {
			    ddGroup  : 'kpiGrid2kpiForm',
			    overClass: 'over',
			    scope: this,
			    initialConfig: this,
			    notifyEnter : function(ddSource, e, data) {
			  		this.initialConfig.kpiName.getEl().frame("00AE00");

			    },
			    notifyDrop  : function(ddSource, e, data){
			      var selectedRecord = ddSource.dragData.selections[0];
			      this.initialConfig.kpiName.setValue(selectedRecord.get('name')); 
			      var node = this.initialConfig.mainTree.getSelectionModel().getSelectedNode() ;
			      if(node !== undefined && node != null){
			    	  var nodesList = this.initialConfig.nodesToSave;
			    	  var exists = nodesList.indexOf(node);
			    	  if(exists == -1){
						  var size = nodesList.length;
						  this.initialConfig.nodesToSave[size] = node;
						  node.attributes.toSave = true;
			    	  }
			    	  
				      node.attributes.kpiId = selectedRecord.get('id');
				      node.attributes.kpiName = selectedRecord.get('name');
				      node.attributes.kpiInstThrName = selectedRecord.get('threshold');
				      node.attributes.kpiInstWeight = selectedRecord.get('weight');
				      node.attributes.iconCls = 'has-kpi';
				      Ext.fly(node.getUI().getIconEl() ).replaceClass('', 'has-kpi');

			      }
			      Ext.fly(this.getEl()).frame("ff0000");
			      return(true);
			    }
		}, this);
		  

	}
	, fillSourceModelPanel: function(sel, node) {
//		alert('fill source Panel');
		if(node !== undefined && node != null){
			this.srcModelName.setValue(node.attributes.modelName);
			this.srcModelCode.setValue(node.attributes.modelCode);
			this.srcModelDescr.setValue(node.attributes.modelDescr);
			this.srcModelType.setValue(LN(node.attributes.modelType));
			this.srcModelTypeDescr.setValue(LN(node.attributes.modelTypeDescr));
		}
	}
	, changeKpiPanel: function(radioGroup, radio){
		if(radio.getItemId() == 'kpiinst'){
			
			this.kpiInstFieldset.setVisible(true);
			this.kpiInstFieldset2.setVisible(false);			

			this.kpiInstFieldset.doLayout();

		}else if(radio.getItemId() == 'uuid'){

			this.kpiInstFieldset.setVisible(false);
			this.kpiInstFieldset2.setVisible(true);

			this.kpiInstFieldset2.doLayout();

		}
		this.kpiInstTypeFieldset.setVisible(true);
		this.kpiInstTypeFieldset.doLayout();
		
	}
	, fillKpiPanel: function(sel, node) {
		if(node !== undefined && node != null){

			var hasKpiAssoc = node.attributes.kpiName;
			var hasKpiModelUuid = node.attributes.modelUuid;
			var hasKpi = node.attributes.kpiId;
			if(!this.showModelUuid){
				hasKpiAssoc = 'true';
			}
			
			if(hasKpiAssoc !== undefined && hasKpiAssoc != null){
				
				this.kpiName.setValue(node.attributes.kpiName);
				this.kpiThreshold.setValue(node.attributes.kpiInstThrName);
				this.kpiTarget.setValue(node.attributes.kpiInstTarget);
				this.kpiWeight.setValue(node.attributes.kpiInstWeight);
				this.kpiChartType.setValue(node.attributes.kpiInstChartTypeId);
				this.kpiPeriodicity.setValue(node.attributes.kpiInstPeriodicity);
				this.kpiSaveHistory.setValue(node.attributes.kpiInstSaveHistory);

				this.kpiInstFieldset.setVisible(true);
				this.kpiInstFieldset2.setVisible(false);
				if(this.showModelUuid){
					this.kpiModelType.onSetValue( 'kpiinst', true);
				}
				
				this.kpiInstFieldset.doLayout();
	
			}else if(hasKpiModelUuid !== undefined && hasKpiModelUuid != null){
				this.kpiLabel.setValue(node.attributes.modelUuid);
				
				this.kpiInstFieldset.setVisible(false);
				this.kpiInstFieldset2.setVisible(true);
				if(this.showModelUuid){
					this.kpiModelType.onSetValue( 'uuid', true);
				}
				this.kpiInstFieldset2.doLayout();
	
			}else if(hasKpi !== undefined && hasKpi != null){
				if(node.attributes.kpi){
					//dropped node from model tree
					this.kpiName.setValue(node.attributes.kpi);
				}else{
					//new node
					this.kpiName.setValue(node.attributes.kpiName);
				}

				this.kpiInstFieldset.setVisible(true);
				this.kpiInstFieldset2.setVisible(false);	
				if(this.showModelUuid){
					this.kpiModelType.onSetValue( 'kpiinst', true);
				}
				this.kpiInstFieldset.doLayout();
	
			}else{
				this.clearKpiInstanceTabForm();
			}
			this.kpiInstTypeFieldset.setVisible(true);
			this.kpiInstTypeFieldset.doLayout();
		}

	}
	, clearKpiInstanceTabForm: function(){
		this.kpiName.setValue(null);
		this.kpiThreshold.setValue(null);
		this.kpiTarget.setValue(null);
		this.kpiWeight.setValue(null);
		this.kpiChartType.setValue(null);
		this.kpiPeriodicity.setValue(null);
		this.kpiSaveHistory.setValue(false);

		this.kpiLabel.setValue(null);
	}

    //OVERRIDING save method
	,save : function() {	
		
    	var jsonStr = '[';

		Ext.each(this.nodesToSave, function(node, index) {
			if(node instanceof Ext.tree.TreeNode){
				jsonStr += Ext.util.JSON.encode(node.attributes);
				if(this.nodesToSave != undefined && index !== this.nodesToSave.length-1){
					jsonStr +=',';
				}
			}
		});

		jsonStr += ']';
		
		var jsonDroppedStr = '[';
		//extracts dropped nodes
		var JsonSer = new Sbi.widgets.JsonTreeSerializer(this.mainTree);

		Ext.each(this.droppedSubtreeToSave, function(node, index) {
			if(node instanceof Ext.tree.TreeNode){	
				//alert(node.attributes.name);
				jsonDroppedStr += JsonSer.nodeToString(node);
				if(this.droppedSubtreeToSave != undefined && index !== this.droppedSubtreeToSave.length-1){
					jsonDroppedStr +=',';
				}
			}
		}, this);
		
		jsonDroppedStr += ']';
		
		var jsonRoot = '';

		if(this.newRootNode !== undefined && this.newRootNode != null){
			jsonRoot = Ext.util.JSON.encode(this.newRootNode.attributes);
		}
		if(this.existingRootNode !== undefined && this.existingRootNode != null){
			jsonRoot = Ext.util.JSON.encode(this.existingRootNode.attributes);
		}

		var params = {
			nodes : jsonStr,
			droppedNodes : jsonDroppedStr,
			rootNode : jsonRoot
		};	

		
		
		Ext.Ajax.request( {
			url : this.services['saveTreeService'],
			success : function(response, options) {
				if(response.responseText !== undefined) {
	      			var content = Ext.util.JSON.decode( response.responseText );
	      			if(content !== undefined && content !== null){
	      				var hasErrors = false;
	      				for (var key in content) {

		      				  var value = content[key];
		      				  var nodeSel = this.mainTree.getNodeById(key);
		      				  //response returns key = guiid, value = 'KO' if operation fails, or modelInstId if operation succeded
		      				  if(nodeSel !== undefined && nodeSel !== null){
			      				  if(value  == 'KO'){
			      					  hasErrors= true;
			 		      			  ///contains error gui ids      						  
		      						  nodeSel.attributes.error = true;
		      						  Ext.fly(nodeSel.getUI().getEl()).applyStyles('{ border: 1px solid red; font-weight: bold; font-style: italic; color: #cd2020; text-decoration: underline; }');
			      				  }else{
			      					  nodeSel.attributes.error = false; 
			      					  nodeSel.attributes.modelInstId = value; 
			      					  Ext.fly(nodeSel.getUI().getEl()).applyStyles('{ border: 0; font-weight: normal; font-style: normal; text-decoration: none; }');
			      					  this.fireEvent('parentsave-complete', nodeSel);
			      				  }
		      				  }
		      		    }
	      				
	      				if(hasErrors){
	      					alert(LN('sbi.generic.savingItemError'));
	      					
	      				}else{
	      					///success no errors!
	      					this.cleanAllUnsavedNodes();
	      					alert(LN('sbi.generic.resultMsg'));
		      				this.referencedCmp.modelInstancesGrid.mainElementsStore.load();
	      				}
	      			}else{
	      				alert(LN('sbi.generic.savingItemError'));
	      			}
				}else{
      				this.cleanAllUnsavedNodes();
      				alert(LN('sbi.generic.resultMsg'));
      				this.referencedCmp.modelInstancesGrid.mainElementsStore.load();
				}
      			this.mainTree.doLayout();
      			this.referencedCmp.modelInstancesGrid.getView().refresh();
				this.referencedCmp.modelInstancesGrid.doLayout();
				
				this.newRootNode = null;
				this.existingRootNode = null;
				
      			return;
			},
			scope : this,
			failure : function(response) {
				if(response.responseText !== undefined) {
					alert(LN('sbi.generic.savingItemError'));
				}
			},
			params : params
		});

		this.referencedCmp.manageModelsTree.importCheck.setValue(false);
    }

	,fillDetail : function(sel, node) {
		if(node !== undefined && node != null){
			
			var isDDNode = node.attributes.modelInstId;			
			
			var val = node.text;//name value
			if (val != null && val !== undefined) {
				var name = node.attributes.name;	
				this.detailFieldDescr.setValue(node.attributes.description);
				this.detailFieldName.setValue(name);
				if(isDDNode){

					this.detailFieldLabel.enable();
					this.detailFieldLabel.setValue(node.attributes.label);
				}else{
					
					if(!node.attributes.isNewRec){
						this.detailFieldLabel.disable();
					}else{
						this.detailFieldLabel.enable();
					}
					
					
				}
			}
		}
	}
	
	,renderTree : function(tree) {
		tree.getLoader().nodeParameter = 'modelInstId';
		tree.getRootNode().expand(false, /*no anim*/false);
	}

	,setListeners : function() {
		this.mainTree.getSelectionModel().addListener('selectionchange',
				this.fillDetail, this);		
		this.mainTree.getSelectionModel().addListener('selectionchange',
				this.fillKpiPanel, this);
		this.mainTree.getSelectionModel().addListener('selectionchange',
				this.fillSourceModelPanel, this);		
		
		this.mainTree.addListener('render', this.renderTree, this);

		/* form fields editing */
		this.detailFieldName.addListener('focus', this.selectNode, this);
		this.detailFieldName.addListener('keyup', this.editNodeAttribute, this);

		this.detailFieldDescr.addListener('focus', this.selectNode, this);
		this.detailFieldDescr.addListener('keyup', this.editNodeAttribute, this);

		this.detailFieldLabel.addListener('focus', this.selectNode, this);
		this.detailFieldLabel.addListener('keyup', this.editNodeAttribute, this);
		
		this.kpiThreshold.addListener('focus', this.selectNode, this);
		this.kpiThreshold.addListener('change', this.editNodeAttribute, this);
		this.kpiThreshold.addListener('changeThr', this.editThreshold, this);
		
		this.kpiTarget.addListener('focus', this.selectNode, this);
		this.kpiTarget.addListener('keyup', this.editNodeAttribute, this);
		
		this.kpiWeight.addListener('focus', this.selectNode, this);
		this.kpiWeight.addListener('keyup', this.editNodeAttribute, this);
		
		this.kpiChartType.addListener('focus', this.selectNode, this);
		this.kpiChartType.addListener('change', this.editNodeAttribute, this);
		
		this.kpiPeriodicity.addListener('focus', this.selectNode, this);
		this.kpiPeriodicity.addListener('change', this.editNodeAttribute, this);
		
		this.kpiName.addListener('focus', this.selectNode, this);
		this.kpiName.addListener('keyup', this.editNodeAttribute, this);
		
		this.kpiLabel.addListener('focus', this.selectNode, this);
		this.kpiLabel.addListener('keyup', this.editNodeAttribute, this);
		
		this.kpiSaveHistory.addListener('focus', this.selectNode, this);
		this.kpiSaveHistory.addListener('keyup', this.editNodeAttribute, this);

		this.kpiRestoreDefaultBtn.addListener('click', this.selectNode, this);
		this.kpiClearBtn.addListener('click', this.selectNode, this);


	}
	, restoreDefaults: function() {

		//re-fills kpi instance form with kpi values
		var node = this.mainTree.getSelectionModel().getSelectedNode();
		
		//gets new node values by ajax request
		var params = {
				kpiId: node.attributes.kpiId
			};

		Ext.Ajax.request( {
			url : this.configurationObject.kpiRestore,
			success : function(response, options) {
				if(response.responseText !== undefined) {
	      			var content = Ext.util.JSON.decode( response.responseText );
	      			if(content !== undefined && content !== null){

	      				this.fillKpiForRestore(content, node);
	      			}
				}
      			return;
			},
			scope : this,
			params : params
		});
	}	
	, fillKpiForRestore: function(content, node) {
		//sets node attribute
		if(node !== undefined && node != null){
			this.kpiName.setValue(content.name);
			node.attributes.kpiName=content.name;
			node.attributes.kpiId=content.id;
			
			this.kpiThreshold.setValue(content.threshold);
			node.attributes.kpiInstThrName=content.threshold;
			
			this.kpiTarget.setValue(content.targetAudience);
			node.attributes.kpiInstTarget=content.targetAudience;
			
			this.kpiWeight.setValue(content.weight);
			node.attributes.kpiInstWeight=content.weight;
			
			this.kpiChartType.setValue('');
			node.attributes.kpiInstChartTypeId = '';
			
			this.kpiPeriodicity.setValue('');
			node.attributes.kpiInstPeriodicity='';
			
			this.kpiSaveHistory.setValue(false);

			this.kpiInstFieldset.setVisible(true);
			this.kpiInstFieldset2.setVisible(false);
			if(this.showModelUuid){
				this.kpiModelType.onSetValue( 'kpiinst', true);
			}
			this.kpiInstFieldset.doLayout();

			node.attributes.toSave = true;
		}
	}

	,createRootNodeByRec: function(rec) {
			var iconClass = '';
			var cssClass = '';
			if (rec.get('kpiInstId') !== undefined && rec.get('kpiInstId') != null
					&& rec.get('kpiInstId') != '') {
				iconClass = 'has-kpi';
			}
			if (rec.get('error') !== undefined && rec.get('error') != false) {
				cssClass = 'has-error';
			}
    		var attrKpiCode = '';
    		if(rec.get('kpiCode') !== undefined){
    			attrKpiCode = ' - '+rec.get('kpiCode');
    		}

    		var tip = rec.get('modelCode')+' - '+rec.get('name')+ attrKpiCode;

    		
			var node = new Ext.tree.AsyncTreeNode({
		        text		: this.rootNodeText,
		        expanded	: true,
		        leaf		: false,
				modelInstId : this.rootNodeId,
				id			: this.rootNodeId,
				label		: rec.get('label'),
				description	: rec.get('description'),
				kpiInst		: rec.get('kpiInstId'),
				name		: rec.get('name'),
				modelName   : rec.get('modelName'),
				modelCode   : rec.get('modelCode'),
				modelDescr  : rec.get('modelDescr'),
				modelType   : rec.get('modelType'),
				modelId     : rec.get('modelId'),
				modelTypeDescr: rec.get('modelTypeDescr'),
				kpiName		: rec.get( 'kpiName'),
				kpiId		: rec.get( 'kpiId'),
				kpiInstThrId: rec.get( 'kpiInstThrId'),
				kpiInstThrName: rec.get( 'kpiInstThrName'),
				kpiInstTarget: rec.get( 'kpiInstTarget'),
				kpiInstWeight: rec.get( 'kpiInstWeight'),
				modelUuid	: rec.get( 'modelUuid'),
				kpiInstChartTypeId: rec.get( 'kpiInstChartTypeId'),			      
				kpiInstPeriodicity: rec.get( 'kpiInstPeriodicity'),
				iconCls		: iconClass,
				cls			: cssClass,
		        draggable	: false,
		        qtip		: tip,
		        toSave: true,
		        isNewRec :  rec.get( 'isNewRec')
		    });

			return node;
	}
	, cleanAllUnsavedNodes: function() {		
		Ext.each(this.nodesToSave, function(node, index) {
			node.attributes.toSave = false; 
					
		});
		this.nodesToSave = new Array();
		
		Ext.each(this.droppedSubtreeToSave, function(node, index) {
			node.attributes.toSave = false; 
					
		});
		this.droppedSubtreeToSave = new Array();
	}    
	,dropNodeBehavoiur: function(e) {
  
			/*
			* tree - The TreePanel
		    * target - The node being targeted for the drop
		    * data - The drag data from the drag source
		    * point - The point of the drop - append, above or below
		    * source - The drag source
		    * rawEvent - Raw mouse event
		    * dropNode - Drop node(s) provided by the source OR you can supply node(s) to be inserted by setting them on this object.
		    * cancel - Set this to true to cancel the drop.
		    */
	// e.target.appendChild(e.dropNode);
		   // e.data.selections is the array of selected records
		if(!Ext.isArray(e.data.selections)) {	
			   //simulates drag&drop but copies the node
			   
			   var importSub = this.referencedCmp.manageModelsTree.importCheck;

			   var copiedNode ;
			   var parentNode = e.target;
			   
			   if(this.referencedCmp.manageModelsTree.importCheck.checked){
				   importSub = true;
				   //imports children
				   copiedNode = new Ext.tree.AsyncTreeNode(e.dropNode.attributes); 

			   }else{
				   importSub = false;
				   copiedNode = new Ext.tree.TreeNode(e.dropNode.attributes); 
			   }

			   e.cancel = true;
			   //if parents have same depth --> enable kind of drop else forbid
			   if(this.checkNodeParent(copiedNode, e.dropNode.parentNode, e.target)){

				   //check that nodes don't go under uncles 
				   copiedNode.attributes.toSave = true;
				   copiedNode.attributes.parentId = parentNode.attributes.modelInstId;

				   if(importSub){
					   copiedNode.expand(true);
				   }

				   parentNode.leaf=false;
				   parentNode.appendChild(copiedNode);	

			       var ddLength = this.droppedSubtreeToSave.length;

			       this.droppedSubtreeToSave[ddLength] = copiedNode;
			       this.referencedCmp.manageModelsTree.importCheck.setValue(false);

			   }else{
				   alert(LN('sbi.modelinstances.DDHierarchy'));
			   }		   
		   }
	    // if we get here the drop is automatically cancelled by Ext
	    }
		, checkNodeParent : function (node, srcParent, targetParent){
			   var srcNodeDepth = srcParent.getDepth();
			   var targetNodeDepth = targetParent.getDepth();
			   
			   if(srcNodeDepth == targetNodeDepth){				   
				   //check modelId of parent nodes
				   var srcParentModelId = srcParent.attributes.modelId;	
				   var targetParentModelId = targetParent.attributes.modelId;	
				   
				   if(srcParentModelId == targetParentModelId){
					   return true;
				   }
			   }
			   return false;
		}
		, initContextMenu : function() {

			this.menu = new Ext.menu.Menu( {
				items : [{
							text : LN('sbi.modelinstances.remodeNode'),
							iconCls : 'icon-remove',
							handler : function() {
								this.deleteItem(this.ctxNode);
							},
							scope : this
						} ]
			});

		}
		, deleteItem : function(node) {
			
			if (node === undefined || node == null) {
				alert(LN('sbi.modelinstances.selectNode'));
				return;
			}
			//if model instance already exists
			if(node.attributes.modelInstId){
				Ext.MessageBox.confirm(
						LN('sbi.generic.pleaseConfirm'),
						LN('sbi.generic.confirmDelete'),            
			            function(btn, text) {
			                if (btn=='yes') {
			                	if (node != null) {	
									Ext.Ajax.request({
							            url: this.services['deleteTreeService'],
							            params: {'modelInstId': node.attributes.modelInstId},
							            //method: 'GET',
							            success: function(response, options) {
											if (response !== undefined) {
												this.mainTree.getSelectionModel().clearSelections(false);
												node.remove();
												this.mainTree.doLayout();
											} else {
												Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.deletingItemError'), LN('sbi.generic.serviceError'));
											}
							            },
							            failure: function() {
							                Ext.MessageBox.show({
							                    title: LN('sbi.generic.error'),
							                    msg: LN('sbi.generic.deletingItemError'),
							                    width: 150,
							                    buttons: Ext.MessageBox.OK
							               });
							            }
							            ,scope: this
						
									});
								} else {
									Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.generic.error.msg'),LN('sbi.generic.warning'));
								}
			                }
			            },
			            this
					);
			}else{
				this.mainTree.getSelectionModel().clearSelections(false);
				node.remove();
				this.mainTree.doLayout();
			}
			
		}
});
