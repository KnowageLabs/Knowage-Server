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
Ext.ns("Sbi.kpi");

Sbi.kpi.ManageModelInstancesViewPort = function(config) { 
	var paramsResList = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "MODELINST_RESOURCE_LIST"};
	var paramsResSave = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "MODELINST_RESOURCE_SAVE"};

	var conf = config;
	this.resListService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_MODEL_INSTANCES_ACTION'
		, baseParams: paramsResList
	});	
	this.resSaveService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_MODEL_INSTANCES_ACTION'
		, baseParams: paramsResSave
	});	
	this.resourcesStore = new Ext.data.JsonStore({
    	autoLoad: false    	  
    	, root: 'rows'
		, url: this.resListService	
		, fields: ['resourceId', 'resourceName', 'resourceCode', 'resourceType', 'modelInstId']

	});
	var paramsTree = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "MODELINSTS_COPY_MODEL"};
	var paramsSaveRoot = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "MODELINSTS_SAVE_ROOT"};
	
	this.modelTreeService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_MODEL_INSTANCES_ACTION'
		, baseParams: paramsTree
	});	
	this.saveRootService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_MODEL_INSTANCES_ACTION'
		, baseParams: paramsSaveRoot
	});
	//DRAW center element
	conf.hideContextMenu = false;
	conf.showModelUuid = true;
	this.manageModelInstances = new Sbi.kpi.ManageModelInstances(conf, this);

	//DRAW west element
    this.modelInstancesGrid = new Sbi.kpi.ManageModelInstancesGrid(conf, this);
   //DRAW east element
    this.manageModelsTree = new Sbi.kpi.ManageModelsTree(conf, this.modelInstancesGrid);
    conf.readonlyStrict = true;
    conf.dropToItem = 'kpinameField';
    this.manageKpis = new Sbi.kpi.ManageKpisGrid(conf, this.manageModelInstances);
    
    this.resourcesTab = new Ext.Panel({
        title: LN('sbi.modelinstances.resourcesTab')
	        , id : 'resourcesTab'
	        , layout: 'fit'
	        , autoScroll: true
	        , width: 300
	        , items: []
	        , itemId: 'resourcesTab'
	        , scope: this
	});
    this.initResourcesGridPanel();
    this.initPanels();
    
    
	var c = Ext.apply({}, config || {}, this.viewport);
	
	Sbi.kpi.ManageModelInstancesViewPort.superclass.constructor.call(this, c);	 		

};

Ext.extend(Sbi.kpi.ManageModelInstancesViewPort, Ext.Viewport, {
	manageModelInstances: null,
	modelInstancesGrid: null,
	manageModelsTree: null,
	manageKpis: null,
	resourcesTab : null,
	centerTabbedPanel: null,
	viewport: null,
	lastRecSelected: null,
	isNewRec: true

	,initPanels : function() {
		this.modelInstancesGrid.addListener('rowclick', this.sendSelectedItem, this);	
		this.modelInstancesGrid.addListener('copytree', this.copyModelTree,  this);	
		this.modelInstancesGrid.addListener('closeList', this.closeListPanel,  this);	
		this.manageModelsTree.addListener('render', this.configureDD, this);


		this.modelInstancesTreeTab = new Ext.Panel({
	        title: LN('sbi.modelinstances.treeTitle')
		        , id : 'modeinstTab'
		        , layout: 'fit'
		        , autoScroll: true
		        , items: [this.manageModelInstances]
		        , itemId: 'modInstTab'
		        , scope: this
		});
		this.tabs = new Ext.TabPanel({
	           enableTabScroll : true
	           , activeTab : 0
	           , autoScroll : true
	           //NB: Important trick: to render all content tabs on page load
	           , deferredRender: false
	           , width: 450
	          // , height: 560
	           , itemId: 'tabs'
			   , items: [this.modelInstancesTreeTab, this.resourcesTab]

			});
		
           
		this.viewport = {
				layout: 'border'
				, id: 'modelInstViewport00'
				//, height:560
				, autoScroll: true
				, items: [
			         {
			           id: 'modelInstancesList00',
			           region: 'west',
			           width: 275,
			        //   height:560,
			           collapseMode:'mini',
			           autoScroll: true,
			           split: true,
			           layout: 'fit',
			           items:[this.modelInstancesGrid]
			          },
				    {
			           id: 'main00',	  
				       region: 'center',
				       //width: 300,
				     //  height:560,
				       split: true,
				       collapseMode:'mini',
				       autoScroll: true,
				       layout: 'fit',
				       items: [this.tabs]
				    }, {
				        region: 'east',
				        split: true,
				        width: 300,
				       // height:560,
				        id: 'modelsTree00',
				        collapsed:false,
				        collapseMode:'mini',
				        //autoScroll: true,
				        layout:'border',
				        defaults: {
				            split: true
				        },

				        items:[
								{
									region:'center',
									collapseMode:'mini',
									layout: 'fit',
				                    items:[this.manageModelsTree]
				                },{
				                	region:'east', 
				                	collapsed: true,
				                	collapseMode:'mini',
				                    width: 280,
				                   // height: 560,
				                    minSize: 100,
				                    layout: 'fit',
				                    autoScroll: true,
				                    items:[this.manageKpis],
				                    listeners : {
				                        beforeCollapse: function(cmp){
				                            //expand model instances list
				                            var toCollapse = Ext.getCmp('modelInstancesList00');
				                            toCollapse.expand();
				                            var toExpand = Ext.getCmp('modelsTree00');
				                            toExpand.setWidth(300);
				                            toExpand.doLayout();
				                            Ext.getCmp('modelInstViewport00').doLayout();
				                        },
				                        beforeExpand: function(cmp){
				                            //collapse model instances list
				                            var toCollapse = Ext.getCmp('modelInstancesList00');
				                            toCollapse.collapse();
				                            var toExpand = Ext.getCmp('modelsTree00');
				                            toExpand.setWidth(500);
				                            toExpand.doLayout();
				                            Ext.getCmp('modelInstViewport00').doLayout();
				                        }
				                    }
				                }    
				        ]
				    }
				]
				

			};


	}

	,sendSelectedItem: function(grid, rowIndex, e){
	
		var rec = this.modelInstancesGrid.getSelectionModel().getSelected();

		//if unsaved changes
		if(this.manageModelInstances.nodesToSave.length > 0){
			//if there are modification on current selection
			Ext.MessageBox.confirm(
					LN('sbi.generic.pleaseConfirm'),
					LN('sbi.generic.confirmChangeNode'),            
		            function(btn, text) {
	
		                if (btn=='yes') {
	
		                	this.manageModelInstances.cleanAllUnsavedNodes();	        			
		        			this.displayTree(rec);
			        		if(rec != this.lastRecSelected){
			        			this.lastRecSelected = rec;
			        		}
	
		                }else{
		                	grid.getSelectionModel().selectRecords([this.lastRecSelected]);
		                	
		                }
	
		            },
		            this
				);
		}else{
			this.manageModelInstances.cleanAllUnsavedNodes();	    
			var analyzedRec = this.recordAnalyze(rec);
			
			this.displayTree(analyzedRec);
			this.dispalyResourcesGridPanel(analyzedRec);
			
			if(analyzedRec != this.lastRecSelected){
				this.lastRecSelected = analyzedRec;
			}
		}
	
	}
	, saveModelRoot: function(rec){

		var params = {
			modelId : rec.data.modelId
		};
		Ext.Ajax.request( {
			url : this.saveRootService,
			success : function(response, options) {
				if(response.responseText !== undefined) {
					//alert(response.responseText);
	      			var content = Ext.util.JSON.decode( response.responseText );
	      			
	      			if(content !== undefined && content !== null){
	      				this.manageModelInstances.cleanAllUnsavedNodes();
	      				
	      				this.modelInstancesGrid.getSelectionModel().selectRecords([rec]);
	      				
	      				this.manageModelInstances.rootNodeText = rec.get('text');
	      				this.manageModelInstances.rootNodeId = content.root[0];
	      				
	      				this.doAfterSaveOnRecRender(rec, content);

	      				this.modelInstancesGrid.mainElementsStore.commitChanges();
	      				
	      				var newroot = this.manageModelInstances.createRootNodeByRec(rec);
	      				
	      				this.doAfterSaveOnNodeRender(newroot, content);


	      				this.manageModelInstances.mainTree.setRootNode(newroot);

	      				this.manageModelInstances.mainTree.getSelectionModel().select(newroot);
	      				
	      				///not as root
	      				this.manageModelInstances.existingRootNode = newroot;
	      				this.manageModelInstances.newRootNode = null;
	      				this.lastRecSelected = rec;
	      				this.isNewRec = false;
	      				
      					alert(LN('sbi.generic.resultMsg'));

	      			}else{
	      				alert(LN('sbi.generic.savingItemError'));
	      			}
				}else{
					this.manageModelInstances.cleanAllUnsavedNodes();
      				alert(LN('sbi.generic.resultMsg'));
      				this.modelInstancesGrid.mainElementsStore.load();
				}

				this.manageModelInstances.newRootNode = null;
      			return true;
			},
			scope : this,
			failure : function(response) {
				if(response.responseText !== undefined) {
					alert(LN('sbi.generic.savingItemError'));
				}
			},
			params : params
		});
	}
	, doAfterSaveOnRecRender: function(rec, content){
		rec.data.modelInstId = content.root[0];
		rec.data.label = content.rootlabel[0];
		rec.data.name = content.rootname[0];
		rec.set('name', content.rootname[0]);
		rec.data.text = content.roottext[0];
		rec.data.toSave = false;
		
		rec.commit();

	}
	, doAfterSaveOnNodeRender: function(newroot, content){
		newroot.attributes.modelInstId = content.root[0];
		newroot.attributes.label = content.rootlabel[0];
		newroot.attributes.name = content.rootname[0];
		newroot.text = content.roottext[0];
		newroot.attributes.isNewRec = false;
		newroot.attributes.toSave = false;
	}
	, copyModelTree: function(rec){

		var params = {
			modelId : rec.data.modelId
		};
		Ext.Ajax.request( {
			url : this.modelTreeService,
			success : function(response, options) {
				if(response.responseText !== undefined) {
					//alert(response.responseText);
	      			var content = Ext.util.JSON.decode( response.responseText );
	      			
	      			if(content !== undefined && content !== null){
	      				
      					this.manageModelInstances.cleanAllUnsavedNodes();
      					alert(LN('sbi.generic.resultMsg'));
	      				
	      				this.modelInstancesGrid.getSelectionModel().selectRecords([rec]);
	      				//if everithing ok--> use it to display tree
	      				
	      				this.manageModelInstances.rootNodeText = rec.get('text');
	      				this.manageModelInstances.rootNodeId = content.root[0];

	      				this.doAfterSaveOnRecRender(rec, content);
	      				
	      				this.modelInstancesGrid.mainElementsStore.commitChanges();
	      				
	      				//main instances tree - center
	      				var newroot = this.manageModelInstances.createRootNodeByRec(rec);
	      				
	      				this.doAfterSaveOnNodeRender(newroot, content);
	      				
	      				this.manageModelInstances.mainTree.setRootNode(newroot);
	      				this.manageModelInstances.mainTree.getSelectionModel().select(newroot);
	      				
	      				this.manageModelInstances.existingRootNode = newroot;
	      				this.manageModelInstances.newRootNode = null;
	      				this.lastRecSelected = rec;
	      				
	      				
	      			
	      			}else{
	      				alert(LN('sbi.generic.savingItemError'));
	      			}
				}else{
					this.manageModelInstances.cleanAllUnsavedNodes();
      				alert(LN('sbi.generic.resultMsg'));
      				this.referencedCmp.modelInstancesGrid.mainElementsStore.load();
      				return true;
				}
				this.manageModelInstances.mainTree.doLayout();
				this.modelInstancesGrid.getView().refresh();
				this.modelInstancesGrid.doLayout();

      			return true;
			},
			scope : this,
			failure : function(response) {
				if(response.responseText !== undefined) {
					alert(LN('sbi.generic.savingItemError'));
				}
			},
			params : params
		});
	}
	, closeListPanel: function(){
        var toCollapse = Ext.getCmp('modelInstancesList00');
        toCollapse.collapse();
	}
	, recordAnalyze: function(rec){

		//checks if model instance id is defined
		var modelInstID = rec.get('modelInstId');
		if(modelInstID === undefined){

			//new model instance --> data coming from model
			var analyzedRec = new Ext.data.Record ({
				 modelInstId : '',				 
				 kpiInstId : '',
				 name : rec.get('name'),
				 description : rec.get('description'),
				 modelId : rec.get('modelId') ,
				 modelName : rec.get('name'),
				 modelCode : rec.get('code'),
				 modelDescr : rec.get('description'),
				 modelType : rec.get('type'),
				 modelTypeDescr : rec.get('typeDescr'),
				 text : rec.get('text'),
				 modelText : rec.get('text'),
				 kpiName : rec.get('kpi'),
				 kpiId : rec.get('kpiId'),
				 error: false,
				 //iconCls: icon,
				 toSave : true,
				 isNewRec: true
				 
			});
			return analyzedRec;
		}
		return rec;
	}
	, displayTree: function(rec){

		this.manageModelInstances.rootNodeText = rec.get('text');
		this.manageModelInstances.rootNodeId = rec.get('modelInstId');

		//main instances tree - center
		var newroot = this.manageModelInstances.createRootNodeByRec(rec);
		this.manageModelInstances.mainTree.setRootNode(newroot);
		//if new model instance
		
		if(rec.get('modelInstId') == ''){
			this.manageModelInstances.newRootNode = newroot;
			if(this.isNewRec){
				this.saveModelRoot(rec);
			}

		}else{
			this.manageModelInstances.existingRootNode = newroot;
		}
		
		this.manageModelInstances.mainTree.getSelectionModel().select(newroot);
		this.manageModelInstances.mainTree.doLayout();

		//model tree - left modelId
		this.manageModelsTree.rootNodeText = rec.get('modelText');
		this.manageModelsTree.rootNodeId = rec.get('modelId');

		var newroot2 = this.manageModelsTree.createRootNodeByRec(rec);
		this.manageModelsTree.modelsTree.setRootNode(newroot2);
		
		this.manageModelsTree.modelsTree.getSelectionModel().select(newroot2);
		this.manageModelsTree.modelsTree.doLayout();

	}
	, initResourcesGridPanel : function() {

    	this.smResources = new Ext.grid.CheckboxSelectionModel( {header: ' ',
    															singleSelect: false, 
    															scope:this, 
    															dataIndex: 'resourceId'} );
		
        this.cmResources = new Ext.grid.ColumnModel([
	         {header: LN('sbi.generic.name'), width: 40, sortable: true, dataIndex: 'resourceName'},
	         {header: LN('sbi.generic.code'), width: 60, sortable: true, dataIndex: 'resourceCode'}
	         ,{header: LN('sbi.generic.type'), width: 60, sortable: true, dataIndex: 'resourceType'}
	         ,this.smResources
	    ]);
 	    this.tb = new Ext.Toolbar({
 	    	buttonAlign : 'right',
 	    	items:[new Ext.Toolbar.Button({
 	            text: LN('sbi.generic.update'),
 	            iconCls: 'icon-save',
 	            handler: this.saveResources,
 	            width: 30,
 	            scope: this
 	            })
 	    	]
 	    });

		this.resourcesGrid = new Ext.grid.GridPanel({
			store: this.resourcesStore 
			, id: 'resources-grid-checks'
   	     	, cm: this.cmResources
   	     	, sm: this.smResources
   	     	, frame: false
   	     	, border:false  
   	     	, layout: 'fit'
   	     	, collapsible:false
   	     	, deferRowRender:false
   	     	, loadMask: true
   	     	, tbar: this.tb
   	     	, viewConfig: {
   	        	forceFit:true
   	        	, enableRowBody:true
   	        	, showPreview:true
   	     	}
			, scope: this
		});
		this.resourcesGrid.superclass.constructor.call(this);
		
		this.resourcesStore.on('load',function(){
			Ext.getCmp("resources-grid-checks").selModel.clearSelections();

		    var arRec = Ext.getCmp("resources-grid-checks").store.queryBy(function(record,id){
		    	if(record.data.modelInstId !== undefined && record.data.modelInstId != ''){
		    		return true;
		    	}
	            return false;
		    }).items;
		    
		    arRecLen = arRec.length;
		    for(i=0;i<arRecLen;i++){
		        var arRow= Ext.getCmp("resources-grid-checks").store.indexOf(arRec[i]);
		        Ext.getCmp("resources-grid-checks").selModel.selectRow(arRow);
		    }
		});  
		this.resourcesStore.load();
		this.resourcesTab.add(this.resourcesGrid);
		this.resourcesGrid.doLayout();

	}
	, dispalyResourcesGridPanel : function(rec) {

		if(rec !== undefined && rec != null){
			var params = {
	        	modelInstId : rec.data.modelInstId
	        }
	        
	        Ext.Ajax.request({
	            url: this.resListService,
	            params: params,
	            method: 'GET',
	            success: function(response, options) {
					if (response !== undefined) {			
			      		if(response.responseText !== undefined) {
			      			Ext.getCmp("resources-grid-checks").selModel.clearSelections();
			      			var content = Ext.util.JSON.decode( response.responseText );	
			      			Ext.each(content.rows, function(row, index) {
			      				
			    				var modelInstId = row.modelInstId;

			    				if(modelInstId != undefined && modelInstId == params.modelInstId){

			    					Ext.getCmp("resources-grid-checks").selModel.selectRow(index, true);
			    				}

			    			});

			      		}
					}
	            }
	            ,scope: this
	        });	
			
			
		}

	}
	, configureDD: function() {
		  var nodeTreePanelDropTarget = new Ext.tree.TreeDropZone(this.manageModelInstances.mainTree, {
		    ddGroup  : 'tree2tree',
		    dropAllowed : true,
		    overClass: 'over',
		    copy: true,
		    scope: this,
		    initialConfig: this.manageModelsTree
		  });

	}

	, saveResources: function() {
		this.modelInstance = this.modelInstancesGrid.getSelectionModel().getSelected();
		if(this.modelInstance !== undefined && this.modelInstance != null){
			//alert(this.modelInstance.data.modelInstId);
			if(this.modelInstance.data.modelInstId === undefined){
				alert(LN('sbi.modelinstances.saveFirstMsg'));
				return;
			}
		}
		//loads selected resources
		var sm = this.resourcesGrid.getSelectionModel();
		var rows = sm.getSelections();

		
		var jsonStr = '[';
		if(rows != undefined && rows != null && rows.length >0){
			Ext.each(rows, function(row, index) {

				this.resId = row.data.resourceId;
				jsonStr += '{id: '+row.data.resourceId+'}';
				if(row != undefined && index !== rows.length-1){
					jsonStr +=',';		
				}
			});
		}
		jsonStr += ']';
			
		var params = {
				ids : jsonStr,
				modelInstId: this.modelInstance.data.modelInstId
		};
		Ext.Ajax.request({
	          url: this.resSaveService,
	          params: params,
	          method: 'GET',
	          success: function(response, options) {
	          	
				if (response !== undefined) {		
	      			var content = Ext.util.JSON.decode( response.responseText );
	      			alert(LN('sbi.generic.resultMsg'));
				 } 	
	          }
	          ,failure : function(response) {
					if(response.responseText !== undefined) {
						alert(LN('sbi.generic.savingItemError'));
					}
				}
	          ,scope: this
	    });
		
	}
});
