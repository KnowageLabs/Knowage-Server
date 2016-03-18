/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 
  

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
Ext.ns("Sbi.kpi");

Sbi.kpi.ManageModelsGrid = function(config, ref) { 
	
	var readonly = config.readonly;
	
	var paramsList = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "MODELS_LIST"};
	var paramsDel = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "MODEL_NODE_DELETE"};
	
	this.configurationObject = {};
	
	this.configurationObject.manageListService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_MODELS_ACTION'
			, baseParams: paramsList
		});	
	this.configurationObject.deleteItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_MODELS_ACTION'
		, baseParams: paramsDel
	});
	this.referencedCmp = ref;
	this.initConfigObject();
	config.configurationObject = this.configurationObject;
	
	config.readonly = readonly;
	
	config.addcopycolumn = true;
	var c = Ext.apply({}, config || {}, {});

	Sbi.kpi.ManageModelsGrid.superclass.constructor.call(this, c);	 	
};

Ext.extend(Sbi.kpi.ManageModelsGrid, Sbi.widgets.ListGridPanel, {
	
	configurationObject: null
	, treeConfigObject: null
	, gridForm:null
	, mainElementsStore:null
	, referencedCmp : null
	, emptyRecord: null
	

	,initConfigObject:function(){
		this.configurationObject.rowIdentificationString = 'modelId';
		this.configurationObject.idKey = 'modelId';
		this.configurationObject.referencedCmp = this.referencedCmp;
		this.configurationObject.dragndropGroup = 'grid2kpi';
	    this.configurationObject.fields = ['modelId'
		                     	          , 'name'
		                    	          , 'code'
		                    	          , 'parentId'
		                    	          , 'label'
		                    	          , 'type'
		                    	          , 'typeId'
		                    	          , 'typeDescr'
		                    	          , 'kpi'
		                    	          , 'kpiId'
		                    	          , 'leaf'
		                    	          , 'text'
		                    	          , 'id'
		                    	          , 'error'
		                    	          , 'description'
		                    	          , 'udpValues'
		                    	          ];
		
		this.configurationObject.gridColItems = [
		                                         {id:'modelId',header: LN('sbi.generic.name'), width: 120, sortable: true, locked:false, dataIndex: 'name'},
		                                         {header: LN('sbi.generic.code'), width: 120, sortable: true, dataIndex: 'code'}
		                                        ];
		
		this.configurationObject.panelTitle = LN('sbi.models.panelTitle');
		this.configurationObject.listTitle = LN('sbi.models.listTitle');
		
    }
	
    //OVERRIDING save method
	,save : function() {
		alert('Save');
    }
	, deleteSelectedItem: function(itemId, index) {
		Ext.MessageBox.confirm(
				LN('sbi.generic.pleaseConfirm'),
				LN('sbi.generic.confirmDelete'),            
	            function(btn, text) {
	                if (btn=='yes') {
	                	if (itemId != null) {	
							Ext.Ajax.request({
					            url: this.services['deleteItemService'],
					            params: {'modelId': itemId},
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
	, addNewItem : function(){
		//SELECTS first domainCd = 'MODEL_ROOT' from combo detail
		var idxRootType = this.referencedCmp.typesStore.find('domainCd', 'MODEL_ROOT');
		
		var recDomain = this.referencedCmp.typesStore.getAt(idxRootType);		
		
		///new tree root node
		var newroot = this.referencedCmp.createNewRootNode();
		newroot.attributes.type = recDomain.data.typeCd;
		newroot.attributes.typeId = recDomain.data.typeId;
		newroot.attributes.typeDescr = recDomain.data.typeDs;

		this.referencedCmp.mainTree.setRootNode(newroot);
		
		this.referencedCmp.mainTree.getSelectionModel().select(newroot);
		this.referencedCmp.mainTree.doLayout();
			 
		//new empty record in the grid
		this.emptyRecord =  new Ext.data.Record({id: 0,
			 name:'...', 
			 code:'...'});
		this.mainElementsStore.add(this.emptyRecord);
		if(this.fields !== undefined){
			this.emptyRecord.markDirty() 
		}

	}

});
