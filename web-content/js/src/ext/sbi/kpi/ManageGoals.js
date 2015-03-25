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
 * Authors - Alberto Ghedin
 */
Ext.ns("Sbi.kpi");

Sbi.kpi.ManageGoals = function(config, ref) { 
	this.ouId = '1';
	this.selectedGrantId = '-2';

	this.kpiTreeRoot ={
		text : 'root',
		modelId: '-1'
	};
	this.goalTreeRoot = {
		text : 'root',
		nodeCount: '1'
	};

	var paramsOUChildList = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "OU_CHILDS_LIST"};
	this.configurationObject = {};
	this.configurationObject.manageTreeService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_OUS_ACTION'
			, baseParams: paramsOUChildList
	});	
	
	var paramsGoalChildList = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "GOAL_NODE_CHILD"};
	this.configurationObject.manageGoalTreeService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_GOALS_ACTION'
			, baseParams: paramsGoalChildList
	});	
	
	var paramsGoalTreeRootService = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "OU_GOAL_ROOT"};
	this.configurationObject.manageGoalTreeRootService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_GOALS_ACTION'
			, baseParams: paramsGoalTreeRootService
	});	
	
	var paramsGoal = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "KPI_GOAL_ROOT_NODE"};
	this.configurationObject.manageGoalService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_GOALS_ACTION'
			, baseParams: paramsGoal
	});	
	
	var paramsSaveGoals = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "INSERT_GOAL_NODES"};
	this.configurationObject.saveGoalService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_GOALS_ACTION'
			, baseParams: paramsSaveGoals
	});	
	
	var paramsEreseGoalNode = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "ERESE_GOAL_NODE"};
	this.configurationObject.ereseGoalNodeService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_GOALS_ACTION'
			, baseParams: paramsEreseGoalNode
	});	
		
	var paramsSaveGoal = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "INSERT_GOAL_NODE"};
	this.configurationObject.saveGoalItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_GOALS_ACTION'
			, baseParams: paramsSaveGoal
	});	
	
	var paramsSaveGoalDetails = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "INSERT_GOAL_DETAILS"};
	this.configurationObject.saveGoalDetailsService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_GOALS_ACTION'
			, baseParams: paramsSaveGoalDetails
	});	
	
	var paramsGoalName = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "UPDATE_GOAL_NAME"};
	this.configurationObject.manageGoalNameService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_GOALS_ACTION'
			, baseParams: paramsGoalName
	});	
	
	
	
	this.config = config;
	this.addEvents();
	var thisPanel = this;
	this.initOUPanel(config);
	this.initGoalPanel(config);

	var c = {
			id: 'goalPanel',
			title: 'Goal Definition',
			layout: 'border',
			border: false,
			items: [
			        {
			        	id: 'OUTab',
			        	title: 'OU',
			        	region: 'west',
			        	width: 275,
			        	collapseMode:'mini',
			        	autoScroll: true,
			        	split: true,
			        	layout: 'fit',
			        	items: [this.ouTree]
			        },
			        {
			        	id: 'goalPanelTab',	  
			        	region: 'center',
			        	split: true,
			        	collapseMode:'mini',
			        	autoScroll: true,
			        	layout: 'fit',
			        	items: [this.goalDetailsCardPanel]
			        }
			        ]
	};
    Sbi.kpi.ManageGoals.superclass.constructor.call(this, c);	
	
};




Ext.extend(Sbi.kpi.ManageGoals, Ext.Panel, {
	ouTree: null
	,goalPanel: null
	,goalTreePanel: null
	,goalTreeRoot: null
	,goalDetailsCardPanel: null
	,goalDetailsPanel: null
	,goalDetailsFormPanel: null
	,goalDetailsFormPanelGoal: null
	,goalDetailskpiPanel: null
	,kpiTreeRoot: null
	,goalId: null
	,ouHierarchyId: null
	,ouId: null	

	,initOUPanel: function(conf){
		conf.rootNodeText = 'root';
		conf.rootNodeId = this.ouId;
		this.ouTree =new Sbi.kpi.ManageGoalsOUTree(conf, {});
		this.ouTree.doLayout();
		this.ouTree.on('afterLayout',this.selectOUPanelRoot, this);
		this.ouTree.getSelectionModel().addListener('selectionchange', this.updateGoalAfterOUChange, this);
	}
	
	, updateGoalAfterOUChange: function(sel, node){
		this.ouId = node.id;
		this.updateGoalDetailsTree(sel, node);
		this.updateGoalDetailsKpi(sel, node);
	}
	
	, selectOUPanelRoot: function(tree){
		tree.getSelectionModel().select(tree.getRootNode());
		tree.un('afterLayout',this.selectOUPanelRoot, this);
	}

	,initGoalPanel: function(conf){
		this.initGoalDetailsPanel(conf);
		this.initGoalTreePanel();
		this.goalPanel = new Ext.Panel({
    		layout: 'border',
    		border: false,
			items: [
			          {
			        	  id: 'goalPanelTree',
			        	  title: 'Goal',
			        	  region: 'north',
			        	  height: 150,
			        	  collapseMode:'mini',
			        	  border: false,
			        	  autoScroll: true,
			        	  split: true,
			        	  layout: 'fit',
			        	  items: [this.goalTreePanel]
			          },
			          {
			        	  id: 'goalPanelDetails',
			        	  title: 'Details',
			        	  region: 'center',
			        	  border: false,
			        	  split: true,
			        	  collapseMode:'mini',
			        	  autoScroll: true,
			        	  layout: 'fit',
			        	  items: [this.goalDetailsPanel]
			          }
			          ]
    	});
		
		this.goalsTreeDefined = false;
		
		var button = new Ext.Panel({
			buttonAlign: 'center',
			bodyCfg: {tag:'center'},
			bodyStyle:'padding-top: 25%; background-color:#F1F1F1;',
			items: new Ext.Button({
				text: LN('sbi.goals.define.goal'),
	            handler: this.openGoalPanel,
	            scope: this
			})
		});
		
		this.goalDetailsCardPanel = new Ext.Panel({
		    layout:'card',
		    activeItem: 1, 
		    defaults: {
		        border:false
		    },
		    items: [button,this.goalPanel]
		});
	}
	
	,openGoalPanel: function(){
    	this.goalsTreeDefined = true;
    	this.goalDetailsCardPanel.getLayout().setActiveItem(1);
		var encodedNode = {
			name : 'root',
			goalDesc: '',
			nodeCount: '1'
		};
    	this.addNewGoalNode(encodedNode, this.goalTreePanel.getRootNode(), true);
    	
	}

	,initGoalTreePanel: function(){
		var c= {};
		var thisPanel = this;
		var treeLoader =new Ext.tree.TreeLoader({
			nodeParameter: 'nodeId',
			dataUrl: thisPanel.configurationObject.manageGoalTreeService,
			createNode: function(attr) {
				
				if (attr.nodeId) {
					attr.id = attr.nodeId;
				}
				if (attr.ou!=null) {
					attr.text = attr.name;
					attr.qtip = attr.label;
				}
			
				var node = Ext.tree.TreeLoader.prototype.createNode.call(this, attr);
				node.on('append',function(tree,node,child,index){
					tree.nodeCount = tree.nodeCount+1;
					child.nodeCount = tree.nodeCount;
				},this);

				return node;
			}
		}); 
		
		c.treeLoader = treeLoader;
		c.rootNode = this.goalTreeRoot; 
		this.goalTreePanel = new Sbi.widgets.ConfigurableTree(c);
		this.goalTreePanel.on('afterlayout',function(){
			this.goalTreePanel.getSelectionModel().select(this.goalTreePanel.getRootNode());
			this.loadKpiTreeRoot();
		},this)
		this.goalTreePanel.getSelectionModel().addListener('selectionchange', 
				function(sel, node){
					if(node!=null){
						this.goalDetailsFormPanelGoal.setValue(node.attributes.goalDesc);
						this.previousGoalTreeNodeSelected= node;
					}
					this.updateGoalDetailsKpi();
					this.loadKpiTreeRoot();
				}, this);
		this.goalTreePanel.on('addedNewItem', function(node,parent){
				var encodedNode = {
					name : node.attributes.text,
					goalDesc: node.attributes.goalDesc,
					nodeCount: node.attributes.nodeCount,
					fatherCountNode: parent.attributes.id
				};
				this.addNewGoalNode(encodedNode, node);
			} ,this);
		this.goalTreePanel.on('removedItem', function(node){
			this.removeGoalNode(node);
		} ,this);
		this.goalTreePanel.on('textchange', function(node, text){
			this.updateGoalName(node,text);
		} ,this);

	}
	
	, addNewGoalNode: function(encodedNode, node, root){
		Ext.Ajax.request({
			url: this.configurationObject.saveGoalItemService,
			params: {'goalNode': Ext.encode(encodedNode),'goalId': this.goalId, 'ouId': this.ouId},
			method: 'GET',
			success: function(response, options) {
				if (response !== undefined && response.responseText!== undefined) {
					var goalNode = Ext.util.JSON.decode(response.responseText);
					if(goalNode.nodeId!== undefined){
						node.attributes.id=goalNode.nodeId;
					}
					
					if(root){
				    	var rootNode  = {
								nodeType : 'async',
								text : encodedNode.name,
								id:  goalNode.nodeId,
								goalDesc: encodedNode.goalDesc,
								nodeCount: 0,
								attributes:{}
							};
				    	rootNode.attributes.id = goalNode.nodeId;
						this.updateGoalRoot(rootNode);
					}
					this.loadKpiTreeRoot();
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.savingItemError'), LN('sbi.generic.serviceError'));
				}
			},
			failure: function() {
				Ext.MessageBox.show({
					title: LN('sbi.generic.error'),
					msg: LN('sbi.generic.savingItemError'),
					width: 150,
					buttons: Ext.MessageBox.OK
				});

			}
			,scope: this

		});
	}
	
	, removeGoalNode: function(node){
		Ext.Ajax.request({
			url: this.configurationObject.ereseGoalNodeService,
			params: {'id': node.attributes.id},
			method: 'POST',
			success: function(response, options) {
				Ext.MessageBox.show({
					msg: LN('sbi.goals.removed'),
					width: 150,
					buttons: Ext.MessageBox.OK
				});
				if(node.id==this.goalTreePanel.getRootNode().id){
					this.goalDetailsCardPanel.getLayout().setActiveItem(0);
				}
			},
			failure: function() {
				Ext.MessageBox.show({
					title: LN('sbi.generic.error'),
					msg: LN('sbi.generic.savingItemError'),
					width: 150,
					buttons: Ext.MessageBox.OK
				});

			}
			,scope: this

		});
	}
	
	,initGoalDetailsPanel: function(conf){
		this.initGoalDetailsFormPanel();
		this.initGoalDetailsKpiPanel(conf);
		var thisPanel = this;
		var tbSave = new Ext.Toolbar( {
			buttonAlign : 'right',
			items : [ 
			         new Ext.Toolbar.Button( {
			        	 text : LN('sbi.generic.update'),
			        	 iconCls : 'icon-save',
			        	 handler : this.save,
			        	 width : 30,
			        	 scope : thisPanel
			         })
			         ]
		});
		
		this.goalDetailsPanel = new Ext.Panel({
			tbar: tbSave,
    		layout: 'border',
    		border: false,
			items: [
			          {
			        	  id: 'goalPanelDetailsGoal',
			        	  region: 'north',
			        	  height:60,
			        	  collapseMode:'mini',
			        	  //split: true,
			        	  layout: 'fit',
			        	  items: [this.goalDetailsFormPanel]
			          },
			          {
			        	  id: 'goalPanelDetailsKPI',
			        	  region: 'center',
			        	  split: true,
			        	  collapseMode:'mini',
			        	  autoScroll: true,
			        	  layout:'anchor',
			        	  items: [this.goalDetailskpiPanel]
			          }
			          ]
    	});
	}
	
	,initGoalDetailsFormPanel: function(){
		
		this.goalDetailsFormPanelGoal = new Ext.form.TextArea(	{
			fieldLabel: 'Goal',
			name: 'goal',
			style: 'width: 100%;',
			height: 50,
			allowBlank:false,
			enableKeyEvents: true
		});
		
		
		this.goalDetailsFormPanelGoal.on('keyup', function(t,e){
			if (this.previousGoalTreeNodeSelected!=null){
				this.previousGoalTreeNodeSelected.attributes.goalDesc = this.goalDetailsFormPanelGoal.getValue();
			}
		},this);
		
		this.goalDetailsFormPanel = new Ext.FormPanel({
			border: false,
			labelWidth: 75, 
			bodyStyle:'padding:5px 5px 0',
			items:[	this.goalDetailsFormPanelGoal]
		});
	}
	
	
	,initGoalDetailsKpiPanel: function(conf){
		var goalNodeId = null;
		this.selectedOUNode = '-1';
		
		var paramsOUChildList = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "KPI_ACTIVE_CHILDS_LIST"};
		
		conf.manageTreeService = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'MANAGE_OUS_ACTION'
				, baseParams: paramsOUChildList
		});	
		
		if(this.goalTreePanel!=null && this.goalsTreeDefined && this.goalTreePanel.getSelectionModel().getSelectedNode()!=null){
			goalNodeId = this.goalTreePanel.getSelectionModel().getSelectedNode().attributes.id;
		}
		conf.treeLoaderBaseParameters = {'grantId': this.selectedGrantId, 'ouNodeId': this.selectedOUNode, 'goalNodeId': goalNodeId}
		conf.rootNode = this.kpiTreeRoot;
		
		if(Sbi.settings && Sbi.settings.kpi && Sbi.settings.kpi.goalModelInstanceTreeUI) {
			Ext.apply(conf,Sbi.settings.kpi.goalModelInstanceTreeUI);
		}
		
		this.goalDetailskpiPanel= new Sbi.widgets.ModelInstanceTree.createGoalModelInstanceTree(conf);

		this.goalDetailskpiPanel.doLayout();
		this.doLayout();
	}
	
	,updateGoalDetailsKpi: function(sel, node){
		var goalNodeId = null;
		var conf=this.config;
		conf.checkbox= true;
		if(node!=null){
			this.selectedOUNode = ''+node.id;
		}
		
		if(this.goalTreePanel!=null && this.goalsTreeDefined && this.goalTreePanel.getSelectionModel().getSelectedNode()!=null){
			goalNodeId = this.goalTreePanel.getSelectionModel().getSelectedNode().attributes.id;
		}
		conf.treeLoaderBaseParameters = {'grantId': this.selectedGrantId, 'ouNodeId': this.selectedOUNode, 'goalNodeId': goalNodeId}
		this.goalDetailskpiPanel.loader.baseParams = conf.treeLoaderBaseParameters;
	}
	
	,updateGoalDetailsKpiRoot: function(root){
		this.kpiTreeRoot = root;
		this.goalDetailskpiPanel.setRootNode(root);
		this.goalDetailskpiPanel.getRootNode().expand(false, /*no anim*/false);
	}
	
	,updateGoalDetailsTree: function(sel, node){
		var thisPanel = this;
		Ext.Ajax.request({
			url: this.configurationObject.manageGoalTreeRootService,
			params: {'ouId': node.id, 'goalId': thisPanel.goalId},
			method: 'POST',
			success: function(response, options) {
				if (response !== undefined && response.responseText!== undefined) {
					var goalNode = Ext.util.JSON.decode(response.responseText);
					
					if(goalNode.name!== undefined){
						var root = {
								nodeType : 'async',
								text : goalNode.name,
								id:  goalNode.nodeId,
								goalDesc: goalNode.goalDesc,
								nodeCount: 0,
								attributes:{}
							};
						this.goalsTreeDefined=true;
						root.attributes.id = goalNode.nodeId;
						thisPanel.updateGoalRoot(root);
						thisPanel.goalDetailsFormPanelGoal.setValue(goalNode.goalDesc);
						this.goalDetailsCardPanel.getLayout().setActiveItem(1);
					}else{//no goalNode is defined for this ou and goal
						this.goalsTreeDefined = false;
						this.goalDetailsCardPanel.getLayout().setActiveItem(0);
					}
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.savingItemError'), LN('sbi.generic.serviceError'));
				}
			},
			failure: function() {
				Ext.MessageBox.show({
					title: LN('sbi.generic.error'),
					msg: LN('sbi.generic.savingItemError'),
					width: 150,
					buttons: Ext.MessageBox.OK
				});
				
			}
			,scope: this
	
		});
	}
	
	,updateGoalName: function(node, name){
		Ext.Ajax.request({
			url: this.configurationObject.manageGoalNameService,
			params: {'goalId': node.attributes.id, 'newName': name},
			method: 'POST',
			success: function(response, options) {
				if (response == undefined) {
					Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.savingItemError'), LN('sbi.generic.serviceError'));
				}
			},
			failure: function() {
				Ext.MessageBox.show({
					title: LN('sbi.generic.error'),
					msg: LN('sbi.generic.savingItemError'),
					width: 150,
					buttons: Ext.MessageBox.OK
				});
			}
			,scope: this
	
		});
	}
	
	,updateGoalRoot: function(root){
		this.goalTreePanel.setRootNode(root);
		this.goalTreePanel.nodeCount=0;
		this.goalTreePanel.getRootNode().expand(false, /*no anim*/false);
		this.goalTreePanel.getSelectionModel().select(this.goalTreePanel.getRootNode());
	}	
	
	,updatePanel: function(grant,ouRootName,ouId){

		this.selectedGrantId = grant;
		this.ouId = ouId;
		
		this.ouTreeRoot = {
			id: this.ouId,
			text: ouRootName,
			disabled : true
		}	
		
		var treeLoaderBaseParameters = {'grantId': this.selectedGrantId};
		this.ouTree.loader.baseParams = treeLoaderBaseParameters;
		
		this.ouTree.setRootNode(this.ouTreeRoot);
		this.ouTree.getRootNode().on('expand', function(node){
			if(node.childNodes !=undefined && node.childNodes !=null && node.childNodes.length>0){
				this.ouTree.getSelectionModel().select(node.childNodes[0]);
			}
		}, this);
		this.ouTree.getRootNode().expand(false, /*no anim*/false);
		
		if(this.ouTree.getRootNode().rendered){
			this.ouTree.getSelectionModel().select(this.ouTree.getRootNode());
			this.updateGoalAfterOUChange(this.ouTree.getRootNode(),this.ouTree.getRootNode());
		}
	}
	
	,loadKpiTreeRoot: function(){
		var thisPanel = this;
		var goalNodeId = null;
		if(this.goalTreePanel!=null && this.goalsTreeDefined && this.goalTreePanel.getSelectionModel().getSelectedNode()!=null){
			goalNodeId = this.goalTreePanel.getSelectionModel().getSelectedNode().attributes.id;
		}
		try {
			goalNodeId = parseInt(goalNodeId);
			if(isNaN(goalNodeId)){
				goalNodeId = parseInt(this.goalTreePanel.getSelectionModel().getSelectedNode().id);
				if(isNaN(goalNodeId)){
					return;
				}
			}
		}catch (ee){
			return;
		}
		Ext.Ajax.request({
			url: this.configurationObject.manageGoalService,
			params: {'grantId': thisPanel.selectedGrantId, 'goalNodeId': goalNodeId, 'ouNodeId': this.ouId },
			method: 'POST',
			success: function(response, options) {
				if (response !== undefined && response.responseText!== undefined) {
					var kpiInstRoot = Ext.util.JSON.decode( response.responseText );
		    		var attrKpiCode = '';
		    		if(kpiInstRoot.kpiCode !== undefined){
		    			attrKpiCode = ' - '+kpiInstRoot.kpiCode;
		    		}
					var root = {
							nodeType : 'async',
							text : kpiInstRoot.modelCode+' - '+kpiInstRoot.name+ attrKpiCode,
							modelId : kpiInstRoot.modelInstId,
							id:  kpiInstRoot.modelInstId,
							modelInstId: kpiInstRoot.modelInstId, 
							weight1: kpiInstRoot.weight1,
							weight2: kpiInstRoot.weight2,
							threshold1: kpiInstRoot.threshold1, 
							sign1: kpiInstRoot.sign1,
							sign2: kpiInstRoot.sign2, 
							threshold2: kpiInstRoot.threshold2,
							kpiInstActive: kpiInstRoot.kpiInstActive
						}
					thisPanel.updateGoalDetailsKpiRoot(root);
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.savingItemError'), LN('sbi.generic.serviceError'));
				}
			},
			failure: function() {
				Ext.MessageBox.show({
					title: LN('sbi.generic.error'),
					msg: LN('sbi.generic.savingItemError'),
					width: 150,
					buttons: Ext.MessageBox.OK
				});
			}
			,scope: this
	
		});
	}

	, save: function(){
		this.saveGoalNodeDetails();
	}
		
	, serializeKpiGoalNode: function(node){
		var toreturn = new Array();
		if(node.getUI().isChecked()){
			var encodedNode = {
				modelInstId: node.attributes.modelInstId, 
				weight1: document.getElementById("weight1"+node.attributes.modelInstId).value,
				weight2: document.getElementById("weight2"+node.attributes.modelInstId).value,
				threshold1: document.getElementById("threshold1"+node.attributes.modelInstId).value,
				sign1: document.getElementById("sign1"+node.attributes.modelInstId).value,
				sign2: document.getElementById("sign2"+node.attributes.modelInstId).value,
				threshold2: document.getElementById("threshold2"+node.attributes.modelInstId).value
			};
			toreturn.push(encodedNode);
		}
		for(var i=0; i<node.childNodes.length; i++){
			toreturn = toreturn.concat(this.serializeKpiGoalNode(node.childNodes[i]));
		}
		return toreturn; 
	}
	
	, serializeGoalNode: function(node, fatherCountNode){
		var toreturn = new Array();
		var encodedNode = {
			name : node.attributes.text,
			goalDesc: node.attributes.goalDesc,
			nodeCount: node.attributes.nodeCount,
			id: node.attributes.id,
			fatherCountNode: fatherCountNode
		};
		toreturn.push(encodedNode);
		for(var i=0; i<node.childNodes.length; i++){
			toreturn = toreturn.concat(this.serializeGoalNode(node.childNodes[i], node.attributes.nodeCount));
		}
		return toreturn; 
	}
	
	,saveGoalNodeDetails: function(){
		var thisPanel = this;
		var goalDetails={
			goalNode: this.serializeGoalNode(this.goalTreePanel.getSelectionModel().getSelectedNode())[0],
			kpis: this.serializeKpiGoalNode(this.goalDetailskpiPanel.getRootNode())
		};

		Ext.Ajax.request({
			url: this.configurationObject.saveGoalDetailsService,
			params: {'goalDetails':  Ext.encode(goalDetails)},
			method: 'POST',
			success: function(response, options) {
				if (response !== undefined) {
					Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.generic.resultMsg'),'');
					thisPanel.fireEvent('saved');
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.savingItemError'), LN('sbi.generic.serviceError'));
				}
			},
			failure: function() {
				Ext.MessageBox.show({
					title: LN('sbi.generic.error'),
					msg: LN('sbi.generic.savingItemError'),
					width: 150,
					buttons: Ext.MessageBox.OK
				});
			}
			,scope: this
	
		});
	}

});


