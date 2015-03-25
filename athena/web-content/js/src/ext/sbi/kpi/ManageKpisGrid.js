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

Sbi.kpi.ManageKpisGrid = function(config, ref) {
	
	var readonlyStrict = config.readonlyStrict; 
	var dropToItem = config.dropToItem; 
	var paramsList = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "KPIS_LIST"};

	this.configurationObject = {};
	
	this.configurationObject.manageListService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_KPIS_ACTION'
		, baseParams: paramsList
	});

	
	this.referencedCmp = ref;
	this.initConfigObject();
	config.configurationObject = this.configurationObject;

	config.readonlyStrict = readonlyStrict;
	config.dropToItem = dropToItem;
	config.multipleSelection = true;
	var c = Ext.apply({}, config || {}, {});

	Sbi.kpi.ManageKpisGrid.superclass.constructor.call(this, c);	
	
	this.addEvents('selected');
};

Ext.extend(Sbi.kpi.ManageKpisGrid, Sbi.widgets.ListGridPanel, {
	
	configurationObject: null
	, treeConfigObject: null
	, gridForm:null
	, mainElementsStore:null
	, referencedCmp : null
	, emptyRecord: null

	,initConfigObject:function(){
		this.configurationObject.rowIdentificationString = 'modelId';
		this.configurationObject.idKey = 'id';
		this.configurationObject.referencedCmp = this.referencedCmp;
		
	    this.configurationObject.fields = ['id'
		                     	          , 'name'
		                    	          , 'code'
		                    	          , 'description'   
		                    	          , 'weight' 
		                    	          , 'dataset'
		                    	          , 'threshold'
		                    	          , 'documents'
		                    	          , 'interpretation'
		                    	          , 'algdesc'
		                    	          , 'inputAttr'
		                    	          , 'modelReference'
		                    	          , 'targetAudience'
		                    	          , 'kpiTypeCd'
		                    	          , 'metricScaleCd'
		                    	          , 'measureTypeCd'
		                    	          ];

		this.configurationObject.gridColItems = [
		                                         {id:'name',header: LN('sbi.generic.name'), width: 137, sortable: true, locked:false, dataIndex: 'name'},
		                                         {header: LN('sbi.generic.code'), width: 135, sortable: true, dataIndex: 'code'}
		                                        ];
		
		this.configurationObject.panelTitle = LN('sbi.kpis.panelTitle');
		this.configurationObject.listTitle = LN('sbi.kpis.listTitle');
		this.configurationObject.dragndropGroup ='kpiGrid2kpiForm';
		this.configurationObject.filter = true;

    }

	
	
});