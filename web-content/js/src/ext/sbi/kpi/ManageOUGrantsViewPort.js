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

Sbi.kpi.ManageOUGrantsViewPort = function(config) { 
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
	this.ManageOUGrants = new Sbi.kpi.ManageOUGrants(conf, this);
	this.ManageOUGrants.on('changeOU_KPI',function(kpi,ou){this.displayTree(kpi,ou);},this);
	this.ManageOUGrants.on('formchange', this.updateGrid, this);


	this.manageOUGrantsGrid = new Sbi.kpi.ManageOUGrantsGrid(conf, this);

	this.ManageOUGrants.on('saved', function(newGrantId, oldGrantId) {
		// retrieve the record modified using its old id (remember that new grant have id = -1)
		var recordIndex = this.manageOUGrantsGrid.getStore().findExact('id', oldGrantId);
		if (recordIndex != -1) {
			var rec = this.manageOUGrantsGrid.getStore().getAt(recordIndex);
			// update the grant id (in case of a new record)
			if (newGrantId != oldGrantId) {
				rec.set('id', newGrantId);
			}
			rec.commit();
		}
		
		//enable the second tab if the grant is new and the grant has been saved
		if(this.ManageOUGrants.tabItems[1].disabled){
			this.ManageOUGrants.tabItems[1].enable();
		}
		
	}, this);

	conf.readonlyStrict = true;
	conf.dropToItem = 'kpinameField';

	this.initPanels();

	var c = Ext.apply({}, config || {}, this.viewport);

	Sbi.kpi.ManageOUGrantsViewPort.superclass.constructor.call(this, c);	 		

};

Ext.extend(Sbi.kpi.ManageOUGrantsViewPort, Ext.Viewport, {
	ManageOUGrants: null,
	manageOUGrantsGrid: null,
	resourcesTab : null,
	centerTabbedPanel: null,
	viewport: null,
	lastRecSelected: null

	, initPanels : function() {
		
		this.manageOUGrantsGrid.addListener('rowclick', this.sendSelectedItem, this);	
		this.manageOUGrantsGrid.addListener('copytree', this.copyModelTree,  this);	

		this.tabs = new Ext.Panel({
			title: LN('sbi.grants.panelTitle')
			, id : 'modeinstTab'
			, layout: 'fit'
			, autoScroll: true
			, items: [this.ManageOUGrants]
			, itemId: 'modInstTab'
			, scope: this
		});

		this.viewport = {
				layout: 'border'
					, height:560
					, autoScroll: true
					, items: [
					          {
					        	  id: 'modelInstancesList00',
					        	  region: 'west',
					        	  width: 275,
					        	  height:560,
					        	  collapseMode:'mini',
					        	  autoScroll: true,
					        	  split: true,
					        	  layout: 'fit',
					        	  items:[this.manageOUGrantsGrid]
					          },
					          {
					        	  id: 'main00',	  
					        	  region: 'center',
					        	  width: 300,
					        	  height:560,
					        	  split: true,
					        	  collapseMode:'mini',
					        	  autoScroll: true,
					        	  layout: 'fit',
					        	  items: [this.tabs]
					          }
					          ]
		};
		
		this.ManageOUGrants.setDisabled(true);
	}

	, sendSelectedItem: function(grid, rowIndex, e){
		
		// Workaround (work-around): this is needed in order to synchronize grid with the grant detail form
		// since the 'change' event is not raised from LookupField
		this.updateGrid(this.ManageOUGrants, this.ManageOUGrants.getGrantFormValues());
		
		//this.manageOUGrantsGrid.getStore().reload();
		//this.manageOUGrantsGrid.getView().refresh();
		this.ManageOUGrants.setDisabled(false);
		var rec = this.manageOUGrantsGrid.rowselModel.getSelected();
		if(rec.data.isAvailable == false){
			alert(LN('sbi.grants.unavailable.during.save'));
			this.ManageOUGrants.setActiveTab(0);
			var index = this.manageOUGrantsGrid.getStore().indexOf(rec);
			var row = this.manageOUGrantsGrid.getView().getRow(index);
			Ext.fly(row).frame("ff0000");
			var cell = this.manageOUGrantsGrid.getView().getCell(index,0);	
			var cell1 = this.manageOUGrantsGrid.getView().getCell(index,1);	
			Ext.fly(cell).highlight('FF0000',{attr:'color', duration:10 }); 
			Ext.fly(cell1).highlight('00FF00',{attr:'color', duration:10 }); 
			return;
		}

		this.ManageOUGrants.detailFieldLabel.setValue(rec.data.label);
		this.ManageOUGrants.detailFieldName.setValue(rec.data.name);
		this.ManageOUGrants.detailFieldDescr.setValue(rec.data.description);
		this.ManageOUGrants.detailFieldFrom.setRawValue(rec.data.startdate);
		this.ManageOUGrants.detailFieldTo.setRawValue(rec.data.enddate);
		this.ManageOUGrants.detailFieldOUHierarchy.setValue(rec.data.hierarchy.id);
		this.ManageOUGrants.detailFieldOUHierarchy.setRawValue(rec.data.hierarchy.name);
		this.ManageOUGrants.detailFieldKpiHierarchy.setValue(rec.data.modelinstance.modelInstId);
		this.ManageOUGrants.detailFieldKpiHierarchy.setRawValue(rec.data.modelinstance.modelText);
		this.ManageOUGrants.selectedGrantId = rec.data.id;
		this.ManageOUGrants.loadTrees();
		this.ManageOUGrants.setActiveTab(0);
		//disable the second tab if the grant is new..
		//this because we need to save the grant before add the grant nodes
		if( rec.data.id=='' ){
			this.ManageOUGrants.tabItems[1].disable();
		}else if (this.ManageOUGrants.tabItems[1].disabled){
			this.ManageOUGrants.tabItems[1].enable();
		}
	}
	
	, displayTree: function(kpi, ou){
		var newOURoot = this.displayOuTree(ou);
		var newKpiRoot = this.displayKpiTree(kpi);
		this.ManageOUGrants.treePanel.doLayout();

		//disable the root and select the first child
		newOURoot.on('expand', function(node){
			if(node.childNodes !=undefined && node.childNodes !=null && node.childNodes.length>0){
				this.ManageOUGrants.leftTree.getSelectionModel().select(node.childNodes[0]);
				this.ManageOUGrants.updateKpisCheck(node.childNodes[0]);	
				node.disable();
			}else{
				this.ManageOUGrants.leftTree.getSelectionModel().select(node);
				this.ManageOUGrants.updateKpisCheck(node);				
			}	
		}, this); 
		
//		if(ou.modelinstancenodes == undefined 
//			|| ou.modelinstancenodes == null 
//			|| ou.modelinstancenodes.length == 0){
//			//add all model inst nodes to ou root
//			this.ManageOUGrants.checkForRoot(newOURoot, newKpiRoot);
//		}
	}
	
	, displayKpiTree: function(rec){
		this.ManageOUGrants.rootNodeRightText = rec.text;
		this.ManageOUGrants.rootNodeRightId = rec.modelInstId;
		var newroot = this.ManageOUGrants.createKPIRootNodeByRec(rec);
		this.ManageOUGrants.rightTree.setRootNode(newroot);
		return newroot;
	}
	
	, displayOuTree: function(rec){
		this.ManageOUGrants.rootNodeLeftText = rec.label;
		this.ManageOUGrants.rootNodeLeftId = rec.id;
		var newroot2 = this.ManageOUGrants.createRootNodeByRec(rec);
		this.ManageOUGrants.leftTree.setRootNode(newroot2);
		return newroot2;
	}
	
	, updateGrid : function (ouDetailPanel, formState) {
		var recordIndex = this.manageOUGrantsGrid.getStore().findExact('id', formState.id);
		if (recordIndex != -1) {
			var rec = this.manageOUGrantsGrid.getStore().getAt(recordIndex);
			this.manageOUGrantsGrid.updateRecord(rec, formState);
		}
	}

});
