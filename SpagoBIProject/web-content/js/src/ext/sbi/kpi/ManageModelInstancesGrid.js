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

Sbi.kpi.ManageModelInstancesGrid = function(config, ref) { 
	var paramsList = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "MODELINSTS_LIST"};
	var paramsDel = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "MODELINSTS_NODE_DELETE"};
	
	this.configurationObject = {};
	
	this.configurationObject.manageListService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_MODEL_INSTANCES_ACTION'
			, baseParams: paramsList
		});	
	this.configurationObject.deleteItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_MODEL_INSTANCES_ACTION'
		, baseParams: paramsDel
	});
	this.refTree = ref;
	this.initConfigObject();
	config.configurationObject = this.configurationObject;


	var c = Ext.apply({}, config || {}, {});

	Sbi.kpi.ManageModelInstancesGrid.superclass.constructor.call(this, c);	
	
	this.addEvents('selected');
	this.addEvents('closeList');
};

Ext.extend(Sbi.kpi.ManageModelInstancesGrid, Sbi.widgets.ListGridPanel, {
	
	configurationObject: null
	, treeConfigObject: null
	, gridForm:null
	, mainElementsStore:null
	, referencedCmp : null
	, emptyRecord: null
	
	,initConfigObject:function(){
		this.configurationObject.rowIdentificationString = 'modelId';
		this.configurationObject.idKey = 'modelInstId';
		this.configurationObject.referencedCmp = this.referencedCmp;
		

	    this.configurationObject.fields = ['modelInstId'
		                     	          , 'name'
		                     	          , 'label'
		                     	          , 'text'
		                     	          , 'description'
		                     	          , 'code'

		                     	          , 'kpiInstId'
		                     	          , 'kpiName'
		                     	          , 'kpiCode'
		                     	          , 'kpiId'
		                     	          , 'kpiInstThrId'
		                     	          , 'kpiInstThrName'
		                     	          , 'kpiInstTarget'
		                     	          , 'kpiInstWeight'
		                     	          , 'kpiInstChartTypeId'
		                     	          , 'modelUuid'
		                     	          
		                     	          , 'kpiInstPeriodicity'
		                     	          , 'kpiInstSaveHistory'
		                     	          
		                     	          , 'modelId'
		                     	          , 'modelText'
			                     	      , 'modelCode'
			                     	      , 'modelName'
			                     	      , 'modelDescr'
			                     	      , 'modelType'
			                     	      , 'modelTypeDescr'
			                     	      , 'resourceName'
			                     	      , 'resourceCode'
			                     	      , 'resourceType'
			                     	      , 'resourceId'
			                     	      
			                     	      , 'isNewRec'
		                    	          ];
		
		this.configurationObject.gridColItems = [
		                                         {id:'modelInstId',	header: LN('sbi.generic.name'), width: 115, sortable: true, locked:false, dataIndex: 'name'},
		                                         {header: LN('sbi.modelinstances.code'), width: 110, sortable: true, dataIndex: 'modelCode'}
		                                        ];
		
		this.configurationObject.panelTitle = LN('sbi.modelinstances.panelTitle');
		this.configurationObject.listTitle = LN('sbi.modelinstances.listTitle');
		this.configurationObject.filter = false;
    }
	
    //OVERRIDING save method
	,save : function() {
		alert('Save');
    }


	, deleteSelectedItem: function(itemId, index) {
		//from tree node
		var node = this.refTree.manageModelInstances.mainTree.getSelectionModel().getSelectedNode();
		if(itemId === undefined){
			itemId = node.attributes.modelInstId;
			
		}

		Ext.MessageBox.confirm(
				LN('sbi.generic.pleaseConfirm'),
				LN('sbi.generic.confirmDelete'),            
	            function(btn, text) {
	                if (btn=='yes') {
	                	if (itemId != null) {	
							Ext.Ajax.request({
					            url: this.services['deleteItemService'],
					            params: {'modelInstId': itemId},
					            method: 'GET',
					            success: function(response, options) {
									if (response !== undefined) {
										var deleteRow = this.rowselModel.getSelected();
										this.mainElementsStore.remove(deleteRow);
										this.mainElementsStore.commitChanges();
										if(this.mainElementsStore.getCount()>0){
											this.rowselModel.selectRow(0);
										}else{
											this.addNewItem();
										}
										alert(LN('sbi.generic.resultMsg'));
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
	}
	, launchAddModelInstWindow : function() {

		var conf = {};
		conf.notDraggable = true;
		conf.readonly = true;
		
		var manageModels = new Sbi.kpi.ManageAddModelPanel(conf);

		this.modelsWin = new Ext.Window({
			title: LN('sbi.lookup.Select') ,   
            layout      : 'fit',
            width       : 800,
            y:			20,
            closeAction :'close',
            modal 		: true,
            plain       : true,
            scope		: this,
            constrain   : true,
            constrainHeader : true,
            items       : [manageModels]
		});
		
		manageModels.modelsGrid.on('selected', function(rec){
							this.modelsWin.close();
							this.addModelInstanceRecord(rec);
							}, this);
		
		manageModels.modelsGrid.on('copy', function(rec){
							this.modelsWin.close();
							this.addModelInstanceTree(rec);
							}, this);
		this.modelsWin.show();
		this.modelsWin.doLayout();
		
		this.modelsWin.on('close', function(){
							this.fireEvent('closeList');
							}, this);

	}
	, addModelInstanceRecord: function(rec){
		this.mainElementsStore.add(rec);
		this.mainElementsStore.commitChanges();
		this.rowselModel.selectRecords([rec]);

		if(rec.get('code')){
			rec.set('modelCode', rec.get('code'));
			rec.set('isNewRec', true);

		}
		//fills node detail and tabs by rowclick
		this.fireEvent('rowclick', rec, this);
	}
	, addModelInstanceTree: function(rec){
		this.mainElementsStore.add(rec);
		this.mainElementsStore.commitChanges();
		this.rowselModel.selectRecords([rec]);

		if(rec.get('code')){
			rec.set('modelCode', rec.get('code'));
		}
		//fills node detail and tabs by rowclick
		//this.fireEvent('rowclick', this);
		this.fireEvent('copytree', rec, this);

	}
	, addNewItem : function(){

		this.launchAddModelInstWindow();


	}

});
