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

Sbi.kpi.ManageGoalsGrid = function(config, ref) { 
	var paramsList = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "GOALS_LIST"};
	var paramsDel = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "GOAL_ERESE"};
	this.configurationObject = {};

	this.services = new Array();

	this.configurationObject.manageListService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_GOALS_ACTION'
			, baseParams: paramsList
	});	
	this.configurationObject.deleteItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_GOALS_ACTION'
			, baseParams: paramsDel
	});


	this.referencedCmp = ref;
	this.initConfigObject();
	config.configurationObject = this.configurationObject;


	var c = Ext.apply({}, config || {}, {});

	Sbi.kpi.ManageGoalsGrid.superclass.constructor.call(this, c);	

	this.addEvents('selected');
	this.addEvents('closeList');
}

Ext.extend(Sbi.kpi.ManageGoalsGrid, Sbi.widgets.ListGridPanel, {

	configurationObject: null
	, treeConfigObject: null
	, gridForm:null
	, mainElementsStore:null
	, referencedCmp : null
	, emptyRecord: null

	,initConfigObject:function(){
		this.configurationObject.rowIdentificationString = 'id';
		this.configurationObject.idKey = 'id';
		this.configurationObject.referencedCmp = this.referencedCmp;


		this.configurationObject.fields = ['id'
		                                   , 'label'
		                                   , 'name'
		                                   , 'description'
		                                   , 'startdate'
		                                   , 'enddate'
		                                   , 'grantid'
		                                   , 'grantname'
		                                   ];

		this.configurationObject.gridColItems = [
		                                         {id:'name', header: LN('sbi.generic.name'), width: 110, sortable: true, locked:false, dataIndex: 'name'},
		                                         {header: LN('sbi.generic.label'), width: 110, sortable: true, dataIndex: 'label'}
		                                         ];

		this.configurationObject.listTitle = LN('sbi.goals.listTitle');

	}

	//OVERRIDING save method
	,save : function() {
		alert('Save');
	}
	
	
	, deleteSelectedItem: function(itemId) {
		var deleteRow = this.rowselModel.getSelected();
		if(deleteRow.data.id.length==0){
			this.mainElementsStore.remove(deleteRow);
			this.mainElementsStore.commitChanges();
			if(this.mainElementsStore.getCount()>0){
				this.rowselModel.selectRow(0);
			}else{
				this.addNewItem();
			}
		}else{
			Ext.MessageBox.confirm(
					LN('sbi.generic.pleaseConfirm'),
					LN('sbi.generic.confirmDelete'),            
					function(btn, text) {
						if (btn=='yes') {
							if (itemId != null) {	
								Ext.Ajax.request({
									url: this.services['deleteItemService'],
									params: {'goalId': itemId},
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
											Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.generic.resultMsg'),'');
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
	}
	
	, addRecord: function(rec){
		this.mainElementsStore.add(rec);
		this.rowselModel.selectLastRow();
		this.fireEvent('newItem', rec, this);
		//reference component is viewport
		this.referencedCmp.tabs.setActiveTab(0);
	}
	
	, addNewItem : function(){

		var record = {
				id:'', 
				label: '', 
				name:'',
				description:'',
				startdate:'',
				enddate:'', 
				grantid:'',
				grantname:''
		};

		var records = new Array();
		records.push(new Ext.data.Record(record));
		this.addRecord(records);	
	}

});
