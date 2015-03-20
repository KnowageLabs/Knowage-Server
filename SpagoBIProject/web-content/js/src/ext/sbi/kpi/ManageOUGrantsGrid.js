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

Sbi.kpi.ManageOUGrantsGrid = function(config, ref) { 
	var paramsList = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "GRANT_LIST"};
	var paramsDel = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "OU_GRANT_ERESE"};
	this.configurationObject = {};

	this.services = new Array();

	this.configurationObject.manageListService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_OUS_ACTION'
			, baseParams: paramsList
	});	
	this.configurationObject.deleteItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_OUS_ACTION'
			, baseParams: paramsDel
	});
	this.configurationObject.synchronizeOUsService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'SYNCHRONIZE_OUS_ACTION'
			, baseParams: {LIGHT_NAVIGATOR_DISABLED: 'TRUE'}
	});

	this.referencedCmp = ref;
	this.initConfigObject();
	config.configurationObject = this.configurationObject;


	var c = Ext.apply({}, config || {}, {});

	Sbi.kpi.ManageOUGrantsGrid.superclass.constructor.call(this, c);	

	this.tb.addSeparator();
	this.tb.addButton(new Ext.Toolbar.Button({
		iconCls: 'icon-refresh' 
			, text: LN('sbi.grants.synchronize.ous.btn.text')
			, scope: this
			, handler : this.synchronizeOUs
	}));

	this.addEvents('selected');
	this.addEvents('closeList');
}

Ext.extend(Sbi.kpi.ManageOUGrantsGrid, Sbi.widgets.ListGridPanel, {

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
		                                   , 'hierarchy'
		                                   , 'modelinstance'
		                                   , 'isAvailable'
		                                   ];

		this.configurationObject.gridColItems = [
		                                         {id:'name', header: LN('sbi.generic.name'), width: 110, sortable: true, locked:false, dataIndex: 'name'},
		                                         {header: LN('sbi.generic.label'), width: 110, sortable: true, dataIndex: 'label'}
		                                         ];

		this.configurationObject.listTitle = LN('sbi.grants.listTitle');

	}

	//OVERRIDING save method
	,save : function() {
		alert('Save');
	}
	
	
	, deleteSelectedItem: function(itemId) {
		var deleteRow = this.rowselModel.getSelected();
		if(deleteRow.data.id == ''){
			this.mainElementsStore.remove(deleteRow);
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
									params: {'grantId': itemId},
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
	
	, addModelInstanceRecord: function(rec){
		this.mainElementsStore.add(rec);
		this.rowselModel.selectLastRow();
		this.fireEvent('rowclick', rec, this);
		//reference component is viewport
		this.referencedCmp.ManageOUGrants.setActiveTab(0);
	}
	
	, addNewItem : function(){
		// if a new grant exists, but it was not saved yet, do not create a new item, but focus on the new grant
		var newRecordIndex = this.mainElementsStore.findExact('id', '');
		if (newRecordIndex != -1) {
			this.rowselModel.selectRow(newRecordIndex);
			var rec = this.mainElementsStore.getAt(newRecordIndex);
			this.fireEvent('rowclick', rec, this);
			return;
		}
		
		var record = {
				id:'', 
				label: '', 
				name:'',
				description:'',
				startdate:'',
				enddate:'', 
				hierarchy:'', 
				modelinstance: ''
		};

		var records = new Array();
		records.push(new Ext.data.Record(record));
		this.addModelInstanceRecord(records);	
	}
	
	, synchronizeOUs: function() {
		Ext.MessageBox.wait(LN('sbi.grants.synchronize.ous.wait.msg'), LN('sbi.grants.synchronize.ous.wait.title'));
		Ext.Ajax.request({
			url: this.configurationObject.synchronizeOUsService
			, success: function(response, options) {
				if ( response !== undefined && response.responseText !== undefined) {
					var content = Ext.util.JSON.decode( response.responseText );
					if (content.message == '') {
						Ext.MessageBox.show({
							title: LN('sbi.grants.synchronize.ous.performed.title')
							, msg: LN('sbi.grants.synchronize.ous.performed.msg')
							, icon: Ext.MessageBox.INFO
							, buttons: Ext.MessageBox.OK
						});
					} else {
						Sbi.exception.ExceptionHandler.showWarningMessage(content.message, LN('sbi.grants.synchronize.ous.performed.title'));
					}
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
				}
			}
			, scope: this
			, failure: Sbi.exception.ExceptionHandler.handleFailure      
		});
	}

	, updateRecord : function (record, newData) {
		record.set('id', newData.id);
		record.set('label', newData.label);
		record.set('name', newData.name);
		record.set('description', newData.description);
		record.set('startdate', newData.startdate);
		record.set('enddate', newData.enddate);
		record.set('hierarchy', newData.hierarchy);
		record.set('modelinstance', newData.modelinstance);
	}
	
});
