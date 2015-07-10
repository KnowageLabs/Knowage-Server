/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.tools.dataset.DataSetsBrowser', {
	extend : 'Ext.Panel'

	,
	config : {
		modelName : "Sbi.tools.dataset.DataSetModel",
		dataView : null,
		user : '',
		typeDoc:null,
		datasetsServicePath: '',
		autoScroll:true,
		displayToolbar: true,
		PUBLIC_USER: 'public_user',
	    //id:'this',
	    isTech: false, //for only certified datasets
	    qbeEditDatasetUrl : '',
	    userCanPersist: '',
		tablePrefix:''
	}

	,
	constructor : function(config) {
		this.initConfig(config);
		this.initServices();
		this.initStore();
		this.initToolbar();
		this.initViewPanel();
		this.items = [this.bannerPanel,this.viewPanel];
		this.callParent(arguments);
//		this.doLayout();
		
		this.addEvents('order');
	}

	,
	initServices : function(baseParams) {
		this.services = [];
		
		if(baseParams == undefined){
			baseParams ={};
		}
		baseParams.isTech = this.config.isTech;
		baseParams.showOnlyOwner = Sbi.settings.mydata.showOnlyOwner;

		/*
		this.services["list"] = Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName : this.datasetsServicePath,
			baseParams : baseParams
		});
		*/
		this.initDefaultFilter(); //initialize the correct 'list' service depending on the default filter set
		
		this.services["getCategories"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'domainsforfinaluser/listValueDescriptionByType',
			baseParams: baseParams
		});
		this.services["save"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'selfservicedataset/save',
			baseParams: baseParams
		});
		this.services["delete"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'selfservicedataset/delete',
			baseParams: baseParams
		});
		this.services["testDataSet"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'selfservicedataset/testDataSet',
			baseParams: baseParams
		});
		this.services["share"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'selfservicedataset/share',
			baseParams: baseParams
		});
		/*
		this.services["getDataStore"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'selfservicedataset/getDataStore',
			baseParams: baseParams
		});
		*/
	}
	,initDefaultFilter: function(){
		if (Sbi.settings.mydata.defaultFilter != undefined){
			var defaultFilter = Sbi.settings.mydata.defaultFilter;
			this.activateFilter(defaultFilter);
		} else {
			//using old initialization
			this.services["list"] = Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName : this.datasetsServicePath,
				baseParams : baseParams
			});
		}
	}
	
	,
	initStore : function(baseParams) {
		Sbi.debug('DataViewPanel bulding the store...');
		
		this.filteredProperties = [ "label", "name","description","fileName","fileType", "catTypeCd","owner" ];
		
		this.sorters = [{property : 'dateIn', direction: 'DESC', description: LN('sbi.ds.moreRecent')}, 
		                {property : 'label', direction: 'ASC', description:  LN('sbi.ds.label')}, 
		                {property : 'name', direction: 'ASC', description: LN('sbi.ds.name')}, 
		                {property : 'fileName', direction: 'ASC', description:  LN('sbi.ds.fileName')},	
		                {property : 'fileType', direction: 'ASC', description: LN('sbi.ds.file.type')}, 
		                {property : 'catTypeCd', direction: 'ASC', description: LN('sbi.ds.catType')},						
						{property : 'owner', direction: 'ASC', description: LN('sbi.ds.owner')}];
		

		this.storeConfig = Ext.apply({
			model : this.getModelName(),
			filteredProperties : this.filteredProperties, 
			sorters: [],
			proxy: {
		        type: 'ajax'
		        , url: this.services["list"]
	         	, reader : {
	        		type : 'json',
	        		root : 'root'
	        	}
		     }
		}, {});

		// creates and returns the store
		Sbi.debug('DataViewPanel store built.');

		this.store = Ext.create('Sbi.widgets.store.InMemoryFilteredStore', this.storeConfig);				
		this.store.load({});
		
		this.categoriesStore = this.createCategoriesStore();
		this.datasetGenericPropertiesStore = this.createDatasetGenericPropertiesStore();
		this.datasetPropertiesStore = this.createDatasetMetadataPropertiesStore();
		this.datasetValuesStore = this.createDatasetMetadataValuesStore();


		this.scopeStore = Ext.create('Ext.data.Store', {
		    fields: ['field', 'value'],
		    data : [
		        {"field":"true", "value":"Public"},
		        {"field":"false", "value":"Private"}
		    ]
		});
		
		this.sortersCombo = this.createSortersStore({sorters: this.sorters});		
		
	}
	
	, initToolbar: function() {
		
		if (this.displayToolbar) {
			
			var bannerHTML = this.createBannerHtml({});
			this.bannerPanel = new Ext.Panel({
				id: 'bannerDs',
				height: 105,
				border:0,
			   	autoScroll: false,
			   //	style:"position:'absolute';z-index:800000;float:left;width:100%;",
			   	html: bannerHTML
			});	
		}
	}
	
    , changeToolbar: function(searchOnCkan) {
		
		if (this.displayToolbar) {
			if(searchOnCkan){
				Ext.get('search').dom.removeAttribute("onkeyup");	
				Ext.get('searchButton').dom.setAttribute("onclick", "javascript:Ext.getCmp(\'this\').showDataset( \'CkanDataSet\', Ext.get('search').dom.value)");
			}
			else {
				//Ext.get('searchForm').dom.setAttribute("action", "#");
				Ext.get('search').dom.setAttribute("onkeyup", "javascript:Ext.getCmp(\'this\').filterStore(this.value)");
				Ext.get('searchButton').dom.removeAttribute("onclick");
			}
		}
	}
	
	,
	initViewPanel: function() {
		var config = {};
		config.services = this.services;
		config.store = this.store;
		config.actions = this.actions;
		config.user = this.user;
		config.fromMyDataCtx = this.displayToolbar;
		config.ckanFilter = 'NOFILTER';
	    config.ckanCounter = 0;
	    config.CKAN_COUNTER_STEP = 200;
		this.viewPanel = Ext.create('Sbi.tools.dataset.DataSetsView', config);
		this.viewPanel.on('detail', this.modifyDataset, this);
		this.viewPanel.on('delete', this.deleteDataset, this);
		this.viewPanel.on('share', this.shareDataset, this);
		//this.viewPanel.on('bookmark', this.bookmarkDataset, this);
		this.viewPanel.on('info', this.infoCkan, this);
//		this.viewPanel.on('executeDocument',function(docType, inputType,  record){
//			this.fireEvent('executeDocument',docType, inputType,  record);
//		},this);
		this.viewPanel.on('executeDocument', this.executeDocument, this);
	}
	
	, activateFilter: function(datasetType){
		if (datasetType == 'MyDataSet'){			
			baseParams ={};
			baseParams.isTech = false;
			baseParams.showOnlyOwner = true;
			baseParams.typeDoc = this.typeDoc;

			this.services["list"] = Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName : 'selfservicedataset',
				baseParams : baseParams
			});		
			
			
		} else if (datasetType == 'EnterpriseDataSet'){			
			baseParams ={};
			baseParams.isTech = true;
			baseParams.showOnlyOwner = false;
			baseParams.typeDoc = this.typeDoc;

			this.services["list"] = Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName : 'certificateddatasets',
				baseParams : baseParams
			});
	
			
		} else if (datasetType == 'SharedDataSet'){
			baseParams ={};
			baseParams.isTech = false;
			baseParams.showOnlyOwner = false;
			baseParams.typeDoc = this.typeDoc;

			this.services["list"] = Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName : 'certificateddatasets',
				baseParams : baseParams
			});
			
		} else if (datasetType == 'CkanDataSet'){
			baseParams ={};
			baseParams.isTech = false;
			baseParams.showOnlyOwner = true;
			baseParams.typeDoc = this.typeDoc;
			baseParams.ckanDs = true;
			baseParams.ckanFilter = arguments[1];
			baseParams.ckanOffset = arguments[2];

			this.services["list"] = Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName : 'certificateddatasets',
				baseParams : baseParams
			});
		
			
		} else if (datasetType == 'AllDataSet'){

			baseParams ={};
			baseParams.isTech = false;
			baseParams.showOnlyOwner = false;
			baseParams.typeDoc = this.typeDoc;
			baseParams.allMyDataDs = true;

			this.services["list"] = Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName : 'certificateddatasets',
				baseParams : baseParams
			});
		
		}
	}
	
	, createCategoriesStore: function(){
		Ext.define("CategoriesModel", {
    		extend: 'Ext.data.Model',
            fields: ["VALUE_NM","VALUE_DS","VALUE_ID"]
    	});
    	
    	var categoriesStore=  Ext.create('Ext.data.Store',{
    		model: "CategoriesModel",
    		proxy: {
    			type: 'ajax',
    			extraParams : {DOMAIN_TYPE:"CATEGORY_TYPE"},
    			url:  this.services['getCategories'],
    			reader: {
    				type:"json"
    			}
    		}
    	});
    	categoriesStore.load();
    	
    	return categoriesStore;
	}
	
	, createDatasetMetadataPropertiesStore: function(){
		Ext.define("DatasetMetadataPropertiesModel", {
    		extend: 'Ext.data.Model',
            fields: ["VALUE_NM","VALUE_DS","VALUE_ID"]
    	});
    	
    	var datasetPropertiesStore=  Ext.create('Ext.data.Store',{
    		model: "DatasetMetadataPropertiesModel",
    		proxy: {
    			type: 'ajax',
    			extraParams : {DOMAIN_TYPE:"DS_META_PROPERTY"},
    			url:  this.services['getCategories'],
    			reader: {
    				type:"json"
    			}
    		}
    	});
    	datasetPropertiesStore.load();
    	
    	return datasetPropertiesStore;
	}
	
	, createDatasetGenericPropertiesStore: function(){
		Ext.define("DatasetMetadataGenericPropertiesModel", {
    		extend: 'Ext.data.Model',
            fields: ["VALUE_NM","VALUE_DS","VALUE_ID"]
    	});
    	
    	var datasetGenericPropertiesStore=  Ext.create('Ext.data.Store',{
    		model: "DatasetMetadataGenericPropertiesModel",
    		proxy: {
    			type: 'ajax',
    			extraParams : {DOMAIN_TYPE:"DS_GEN_META_PROPERTY"},
    			url:  this.services['getCategories'],
    			reader: {
    				type:"json"
    			}
    		}
    	});
    	datasetGenericPropertiesStore.load();
    	
    	return datasetGenericPropertiesStore;
	}
	
	, createDatasetMetadataValuesStore: function(){
		Ext.define("DatasetMetadataValuesModel", {
    		extend: 'Ext.data.Model',
            fields: ["VALUE_NM","VALUE_DS","VALUE_ID"]
    	});
    	
    	var datasetValuesStore=  Ext.create('Ext.data.Store',{
    		model: "DatasetMetadataValuesModel",
    		proxy: {
    			type: 'ajax',
    			extraParams : {DOMAIN_TYPE:"DS_META_VALUE"},
    			url:  this.services['getCategories'],
    			reader: {
    				type:"json"
    			}
    		}
    	});
    	datasetValuesStore.load();
    	
    	return datasetValuesStore;
	}
	
	, createSortersStore: function(config){		
		var ordersStore = Ext.create('Ext.data.Store', {
		    fields: ["property","direction","description"],
		    data : config.sorters
		});
    	
		ordersStore.load();
    	
    	return ordersStore;
	}
	
	,
	executeDocument : function( docType, inputType, record){
		if(record !== undefined) {
			if(record.data.dsTypeCd == 'Ckan' && record.data.meta == ""){
				Sbi.debug("Forwarding action to addNewDatasetFromCkan()");
				this.addNewDatasetFromCkan(record.data);
			} else {
				this.fireEvent('executeDocument', docType, inputType, record);
			}
		}
	}
	
	,
	addNewDataset : function() {		 
		var config =  {};
		config.categoriesStore = this.categoriesStore;
		config.datasetGenericPropertiesStore = this.datasetGenericPropertiesStore;
		config.datasetPropertiesStore = this.datasetPropertiesStore;
		config.datasetValuesStore = this.datasetValuesStore;
		config.scopeStore = this.scopeStore;
		config.user = this.user;
		config.isNew = true;
		config.userCanPersist = this.userCanPersist;
		config.tablePrefix = this.tablePrefix;
		this.wizardWin =  Ext.create('Sbi.tools.dataset.DataSetsWizard',config);	
		this.wizardWin.on('save', this.saveDataset, this);
		this.wizardWin.on('getMetaValues', this.getMetaValues, this);
    	this.wizardWin.show();
	}
	
	,
	addNewDatasetFromCkan : function(rec) {		 
		var config =  {};
		config.categoriesStore = this.categoriesStore;
		config.datasetGenericPropertiesStore = this.datasetGenericPropertiesStore;
		config.datasetPropertiesStore = this.datasetPropertiesStore;
		config.datasetValuesStore = this.datasetValuesStore;
		config.scopeStore = this.scopeStore;
		config.user = this.user;
		config.isNew = true;
		config.userCanPersist = this.userCanPersist;
		config.tablePrefix = this.tablePrefix;

		config.ckanUrl = rec.configuration.Resource.url;
		config.ckanId = rec.configuration.ckanId;
		config.ckanFormat = rec.configuration.Resource.format;
		config.ckanName = rec.configuration.Resource.name;
		config.ckanDescription = rec.configuration.Resource.description;
		this.wizardWin =  Ext.create('Sbi.tools.dataset.CkanDataSetsWizard',config);	
		this.wizardWin.on('save', this.saveDataset, this);
		this.wizardWin.on('getMetaValues', this.getMetaValues, this);
    	this.wizardWin.show();
	}
	
	, 
	modifyDataset: function(rec){
		if (rec != undefined){
			if(rec.dsTypeCd == 'Ckan' && rec.meta == ""){
				Sbi.debug("Forwarding action to addNewDatasetFromCkan()");
				this.addNewDatasetFromCkan(rec);
			} else {
				var config =  {};
				config.categoriesStore = this.categoriesStore;
				config.datasetGenericPropertiesStore = this.datasetGenericPropertiesStore;
				config.datasetPropertiesStore = this.datasetPropertiesStore;
				config.datasetValuesStore = this.datasetValuesStore;
				config.scopeStore = this.scopeStore;
				config.user = this.user;
				config.record = rec;
				config.isNew = false;
				config.qbeEditDatasetUrl = this.qbeEditDatasetUrl;
				config.userCanPersist = this.userCanPersist;
				config.tablePrefix = this.tablePrefix;
				switch (rec.dsTypeCd) {
					case 'File' : 
						this.wizardWin = Ext.create('Sbi.tools.dataset.DataSetsWizard', config);	
						this.wizardWin.on('save', this.saveDataset, this);
						this.wizardWin.on('delete', this.deleteDataset, this);
						this.wizardWin.on('getMetaValues', this.getMetaValues, this);
				    	this.wizardWin.show();
						break;
					case 'Ckan' : 
						config.ckanUrl = rec.ckanUrl;
						config.ckanId = rec.ckanId;
						config.ckanFormat = rec.fileType;
						config.ckanName = rec.name;
						config.ckanDescription = rec.description;
						this.wizardWin = Ext.create('Sbi.tools.dataset.CkanDataSetsWizard', config);	
						this.wizardWin.on('save', this.saveDataset, this);
						this.wizardWin.on('delete', this.deleteDataset, this);
						this.wizardWin.on('getMetaValues', this.getMetaValues, this);
				    	this.wizardWin.show();
						break;
					case 'Qbe' :
						config.width = this.getWidth() - 50,
						config.height = this.getHeight() - 50,
						this.wizardWin = Ext.create('Sbi.tools.dataset.QbeDataSetsWizard', config);	
						this.wizardWin.on('save', this.saveDataset, this);
						this.wizardWin.on('delete', this.deleteDataset, this);
						this.wizardWin.on('getMetaValues', this.getMetaValues, this);
				    	this.wizardWin.show();
				    	break;
				}
			}
		}
	}
	
	, 
	shareDataset: function(rec){
		if (rec != undefined){
			var params =  {};
			params.id = rec.id;
			
			
			//Sbi.exception.ExceptionHandler.showInfoMessage('Dataset shared');
			Ext.Ajax.request({
				url: this.services["share"],
				params: params,			
				success : function(response, options) {				
					if(response !== undefined  && response.responseText !== undefined && response.statusText=="OK") {
						if(response.responseText!=null && response.responseText!=undefined){
							if(response.responseText.indexOf("error.mesage.description")>=0){
								Sbi.exception.ExceptionHandler.handleFailure(response);
							}else{						
								this.store.load({reset:true});
								this.viewPanel.refresh();
								var result = JSON.parse(response.responseText);
								if (result.isPublic) {
									Sbi.exception.ExceptionHandler.showInfoMessage('Dataset shared');
								} else {
									Sbi.exception.ExceptionHandler.showInfoMessage('Dataset unshared');
								}
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
		
	,
	saveDataset: function(values){
		var metaConfiguration = values.meta || [];
		delete values.meta;
		var params = values;
		params.meta = Ext.JSON.encode(metaConfiguration) ;
		Ext.Ajax.request({
			url: this.services["save"],
			params: params,			
			success : function(response, options) {				
				if(response !== undefined  && response.responseText !== undefined && response.statusText=="OK") {
					if(response.responseText!=null && response.responseText!=undefined){
						if(response.responseText.indexOf("error.mesage.description")>=0){
							Sbi.exception.ExceptionHandler.handleFailure(response);
						}else{						
							this.store.load({reset:true});
							this.wizardWin.destroy();						
							this.viewPanel.refresh();
							Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.ds.saved'));
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
	
	,
	deleteDataset: function(values){
		Ext.MessageBox.confirm(
				LN('sbi.generic.pleaseConfirm'),
				LN('sbi.generic.confirmDelete'),
				function(btn, text){
					if (btn=='yes') {
						Ext.Ajax.request({
							url: this.services["delete"],
							params: values,
							success : function(response, options) {
								if(response !== undefined  && response.responseText !== undefined && response.statusText=="OK") {
									if(response.responseText!=null && response.responseText!=undefined){
										if(response.responseText.indexOf("error.mesage.description")>=0){
											Sbi.exception.ExceptionHandler.handleFailure(response);
										}else{						
											this.store.load({reset:true});										
											this.viewPanel.refresh();			
											Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.ds.deleted'));
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
				this
			);
	}
	
	,
	getMetaValues: function(values){
		var metaConfiguration = values.meta || [];
		delete values.meta;
		var params = values;
		params.meta = Ext.JSON.encode(metaConfiguration) ;
		Ext.Ajax.request({
			url: this.services["testDataSet"],
			params: params,			
			success : function(response, options) {				
				if(response !== undefined  && response.responseText !== undefined && response.statusText=="OK") {
					if(response.responseText!=null && response.responseText!=undefined){
						if(response.responseText.indexOf("error.mesage.description")>=0){
							this.wizardWin.disableButton('confirm');
							this.wizardWin.goBack(1);
							Sbi.exception.ExceptionHandler.handleFailure(response);
						}else{			
							var newMeta = response.responseText;
							var newMetaDecoded =  Ext.decode(newMeta);				 
							this.wizardWin.metaInfo.updateData(newMetaDecoded.datasetColumns);
							this.wizardWin.metaInfo.updateGridData(newMetaDecoded.meta);
							if (this.wizardWin.isOwner){
								this.wizardWin.enableButton('confirm');
							}
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
	
	,
	infoCkan: function(rec){
		
		var detail = rec.configuration.Package;
		var keyArray = Ext.Object.getKeys(detail);
		var msgBase = "sbi.mydata.ckan.info."
		var infoData = [];
		
		keyArray.forEach( function (key)
		{
			var val = detail[key];
			if(val){
				var key = msgBase.concat(key);
				var obj = {"name":LN(key), "value":val, "referenceTo":"Package"};
				infoData.push(obj);
			}
		});
		
		detail = rec.configuration.Resource;
		keyArray = Ext.Object.getKeys(detail);
		
		keyArray.forEach( function (key)
		{
			var val = detail[key];
				if(val){
				var key = msgBase.concat(key);
				var obj = {"name":LN(key), "value":val, "referenceTo":"Resource"};
				infoData.push(obj);
			}
		});
		
		detail = rec.configuration.Owner;
		keyArray = Ext.Object.getKeys(detail);
		
		keyArray.forEach( function (key)
		{
			var val = detail[key];
			if(val){
				var key = msgBase.concat(key);
				var obj = {"name":LN(key), "value":val, "referenceTo":"Owner"};
				infoData.push(obj);
			}
		});
		
		// wrapped in closure to prevent global vars.
	    Ext.define('Detail', {
	        extend: 'Ext.data.Model',
	        fields: ['name', 'value', 'referenceTo']
	    });
	
	    var Details = Ext.create('Ext.data.Store', {
	        storeId: 'restaraunts',
	        model: 'Detail',
	        sorters: ['referenceTo','name','value'],
	        groupField: 'referenceTo',
	        data: infoData
	    });
	    
	    var groupingFeature = Ext.create('Ext.grid.feature.Grouping',{
	        groupHeaderTpl: '{name} details'
	    });
	    
	    var details_window= new Ext.Window({
			frame: false,
			style:"background-color: white",
			id:'ckan_info_window',            				
			layout:'fit',
			autoScroll: true,
			items: {
				xtype: 'grid',
		        store: Details,
		        width: 700,
		        height: 300,
		        collapsible: false,
		        frame: false,
                hideHeaders: true,
                viewConfig: {
                    stripeRows: false
                },
		        features: [groupingFeature],
		        columns: [{
		            text: 'Name',
		            flex: 1,
		            dataIndex: 'name'
		        },{
		            text: 'Cuisine',
		            flex: 2,
		            dataIndex: 'value'
		        }],
			},
			width:710,
			height:300,
			closeAction:'destroy',
			buttonAlign : 'left',
			modal: true,
			title: LN('sbi.mydata.info')
		});	
		details_window.show();
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
	
	, filterStore: function(filterString) {
		this.store.load({filterString: filterString});
	}
	
	, sortStore: function(value) {			
		var sortEls = Ext.get('sortList').dom.childNodes;
		//move the selected value to the first element
		for(var i=0; i< sortEls.length; i++){
			if (sortEls[i].id == value){					
				sortEls[i].className = 'active';
				break;
			} 
		}
		//append others elements
		for(var i=0; i< sortEls.length; i++){
			if (sortEls[i].id !== value){
				sortEls[i].className = '';		
			}
		}
		

		for (sort in this.sorters){
			var s = this.sorters[sort];
			if (s.property == value){
				this.store.sort(s.property, s.direction);
				break;
			}
		}
		
		this.viewPanel.refresh();
	}	
	
	//Show only the dataset of the passed type
	, showDataset: function(datasetType) {
		//alert(datasetType);
		var tabEls = Ext.get('list-tab').dom.childNodes;
		
		//Change active dataset type on toolbar
		for(var i=0; i< tabEls.length; i++){
			//nodeType == 1 is  Node.ELEMENT_NODE
			if (tabEls[i].nodeType == 1){
				if (tabEls[i].id == datasetType){
					if(datasetType == 'CkanDataSet') {
						// bug fix for CKAN search
						tabEls[i].className = 'active'; //set class name
					} else {
						tabEls[i].className += ' active '; //append class name to existing others
					}
				} else {
					tabEls[i].className = tabEls[i].className.replace( /(?:^|\s)active(?!\S)/g , '' ); //remove active class
				}
			}
		}
		//Change content of DatasetView
		if(datasetType == 'CkanDataSet') {
			this.ckanFilter = arguments[1];
			this.ckanCounter = 0;
			this.CKAN_COUNTER_STEP = 200;
			this.activateFilter(datasetType, this.ckanFilter, this.ckanCounter);
		}
		else {
			this.activateFilter(datasetType);
		}
		if (datasetType == 'MyDataSet'){
			this.changeToolbar(false);
			this.createButtonVisibility(true);
		} else if (datasetType == 'EnterpriseDataSet'){
			this.changeToolbar(false);
			this.createButtonVisibility(false);
		} else if (datasetType == 'SharedDataSet'){
			this.changeToolbar(false);
			this.createButtonVisibility(false);
		} else if (datasetType == 'CkanDataSet'){
			this.changeToolbar(true);
			this.createButtonVisibility(false);
		} else if (datasetType == 'AllDataSet'){
			this.changeToolbar(false);
			this.createButtonVisibility(true);
		}	
		
		this.storeConfig = Ext.apply({
			model : this.getModelName(),
			filteredProperties : this.filteredProperties, 
			sorters: [],
			proxy: {
		        type: 'ajax'
		        , url: this.services["list"]
	         	, reader : {
	        		type : 'json',
	        		root : 'root'
	        	}
		     }
		}, {});

		this.store = Ext.create('Sbi.widgets.store.InMemoryFilteredStore', this.storeConfig);				
		
		//load store and refresh datasets view
		this.store.load(function(records, operation, success) {
		    //console.log('***DATASETS BROWSER loaded records***');
		});
		this.viewPanel.bindStore(this.store);
		this.ckanCounter += this.CKAN_COUNTER_STEP;
		this.viewPanel.refresh();

	}
	
	//Show more dataset of the passed type
	, moreDataset: function() {
		var activeDatasets = Ext.get('CkanDataSet').dom.getAttribute('class');
		if(activeDatasets == "active") {
			//Change content of DatasetView
			this.activateFilter('CkanDataSet', this.ckanFilter, this.ckanCounter);
			
			this.storeConfig = Ext.apply({
				model : this.getModelName(),
				filteredProperties : this.filteredProperties, 
				sorters: [],
				proxy: {
			        type: 'ajax'
			        , url: this.services["list"]
		         	, reader : {
		        		type : 'json',
		        		root : 'root'
		        	}
			     }
			}, {});
			
			var tempStore = Ext.create('Sbi.widgets.store.InMemoryFilteredStore', this.storeConfig);				
			// loading more datasets
			tempStore.load({scope: this, callback: this.storeMoreDataset});
		} else {
			// datasets paging only enabled for CKAN
			Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.ds.wizard.ckan.noMoreDataset'), '');
		}
	}
		
	, storeMoreDataset: function(records, operation, success) {
	    if(records.length > 0) {
		    this.store.loadRecords(records, {addRecords: true})
		    this.ckanCounter += this.CKAN_COUNTER_STEP;
			this.viewPanel.refresh();
	    } else {
	    	Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.ds.wizard.ckan.noMoreDataset'), '');
	    }
	}
	
	, createButtonVisibility: function(visible){
		var dh = Ext.DomHelper;	
		if (visible == true){
			//check if button already present
			var button = Ext.get('newDataset');
			if (!button){
				//add button
		        if (this.user !== '' && this.user !== this.PUBLIC_USER){
		        	var createButton = ' <a id="newDataset" href="#" onclick="javascript:Ext.getCmp(\'this\').addNewDataset(\'\')" class="btn-add"><span class="highlighted">'+LN('sbi.generic.create')+'</span> '+LN('sbi.browser.document.dataset')+'<span class="plus">+</span></a> ';
		        	var actionsDiv = Ext.get('list-actions').dom;
		        	dh.insertHtml('afterBegin',actionsDiv,createButton);
		        }
			}
		} else {
			//remove button if exist
			if (Ext.get('newDataset') != null && Ext.get('newDataset') != undefined){
				var button = Ext.get('newDataset').dom;
				if (button){
					button.parentNode.removeChild(button);
				}				
			}
		}
	}

	, createBannerHtml: function(communities){
    	var communityString = '';
    	//hidden 'ALL' button for favourites (until they aren't managed)
//        for(i=0; i< communities.root.length; i++){
//        	var funct = communities.root[i].functId;
//        	communityString += '<li><a href="#" onclick="javascript:Ext.getCmp(\'this\').loadFolder('+funct+', null)">';
//        	communityString += communities.root[i].name;
//        	communityString +='</a></li>';
//        }

        var createButton = '';
        if ( (Sbi.settings.mydata.defaultFilter == 'MyDataSet') || (Sbi.settings.mydata.defaultFilter == 'AllDataSet') ){
            if (this.user !== '' && this.user !== this.PUBLIC_USER && this.typeDoc == 'null'){
            	createButton += ' <a id="newDataset" href="#" onclick="javascript:Ext.getCmp(\'this\').addNewDataset(\'\')" class="btn-add"><span class="highlighted">'+LN('sbi.generic.create')+'</span> '+LN('sbi.browser.document.dataset')+'<span class="plus">+</span></a> ';
            }
        }

        
        var activeClass = '';
        var bannerHTML = ''+
//     		'<div class="aux"> '+
     		'<div class="main-datasets-list"> '+
    		'    <div class="list-actions-container"> '+ //setted into the container panel
    		'		<ul class="list-tab" id="list-tab"> ';
        if (Sbi.settings.mydata.showMyDataSetFilter){	
        	if (Sbi.settings.mydata.defaultFilter == 'MyDataSet'){
        		activeClass = 'active';
        	} else {
        		activeClass = '';
        	}
        	bannerHTML = bannerHTML+	
        	'	    	<li class="first '+activeClass+'" id="MyDataSet"><a href="#" onclick="javascript:Ext.getCmp(\'this\').showDataset( \'MyDataSet\')">'+LN('sbi.mydata.mydataset')+'</a></li> '; 
        }	
        if (Sbi.settings.mydata.showEnterpriseDataSetFilter){
        	if (Sbi.settings.mydata.defaultFilter == 'EnterpriseDataSet'){
        		activeClass = 'active';
        	} else {
        		activeClass = '';
        	}
        	bannerHTML = bannerHTML+	
    		'	    	<li class="'+activeClass+'" id="EnterpriseDataSet"><a href="#" onclick="javascript:Ext.getCmp(\'this\').showDataset( \'EnterpriseDataSet\')">'+LN('sbi.mydata.enterprisedataset')+'</a></li> ';    
        }
         if (Sbi.settings.mydata.showSharedDataSetFilter){
         	if (Sbi.settings.mydata.defaultFilter == 'SharedDataSet'){
        		activeClass = 'active';
        	} else {
        		activeClass = '';
        	}
         	bannerHTML = bannerHTML+	
     		'	    	<li class="'+activeClass+'" id="SharedDataSet"><a href="#" onclick="javascript:Ext.getCmp(\'this\').showDataset( \'SharedDataSet\')">'+LN('sbi.mydata.shareddataset')+'</a></li> ';    	
         }
         if (Sbi.settings.mydata.showCkanDataSetFilter){
          	if (Sbi.settings.mydata.defaultFilter == 'CkanDataSet'){
         		activeClass = 'active';
         	} else {
         		activeClass = '';
         	}
          	bannerHTML = bannerHTML+	
      		'	    	<li class="'+activeClass+'" id="CkanDataSet"><a href="#" onclick="javascript:Ext.getCmp(\'this\').showDataset( \'CkanDataSet\', \'NOFILTER\')">'+LN('sbi.mydata.ckandataset')+'</a></li> ';    	
          }
         if (Sbi.settings.mydata.showAllDataSetFilter){
          	if (Sbi.settings.mydata.defaultFilter == 'AllDataSet'){
        		activeClass = 'active';
        	} else {
        		activeClass = '';
        	}
          	bannerHTML = bannerHTML+	
    		'	    	<li id="AllDataSet" class="last '+activeClass+'"><a href="#" onclick="javascript:Ext.getCmp(\'this\').showDataset( \'AllDataSet\')">'+LN('sbi.mydata.alldataset')+'</a></li> ';    		    		    		    		        	 
         }
//    		'	    	<li class="active first"><a href="#" onclick="javascript:Ext.getCmp(\'this\').loadFolder(null, null, \'ALL\')">'+LN('sbi.generic.all')+'</a></li> '+
//    					communityString+
//    		'	        <li class="favourite last"><a href="#">'+LN('sbi.browser.document.favourites')+'</a></li> '+

	        bannerHTML = bannerHTML+
	            '		</ul> '+
	    		'	    <div id="list-actions" class="list-actions"> '+
	    					createButton +
	    		'	        <form id="searchForm" action="#" method="get" class="search-form"> '+
	    		'	            <fieldset> '+
	    		'	                <div class="field"> '+
	    		'	                    <label for="search">'+LN('sbi.browser.document.searchDatasets')+'</label> '+
	    		'	                    <input type="text" name="search" id="search" onclick="this.value=\'\'" onkeyup="javascript:Ext.getCmp(\'this\').filterStore(this.value)" value="'+LN('sbi.browser.document.searchKeyword')+'" /> '+
	    		'	                </div> '+
	    		'	                <div class="submit"> '+
	    		'	                    <input id="searchButton" type="text" value="Cerca" /> '+
	    		'	                </div> '+
	    		'	            </fieldset> '+
	    		'	        </form> '+
	    		'	         <ul class="order" id="sortList">'+
	    		'	            <li id="dateIn" class="active"><a href="#" onclick="javascript:Ext.getCmp(\'this\').sortStore(\'dateIn\')">'+LN('sbi.ds.moreRecent')+'</a> </li> '+
	//    		'	            <li id="label"><a href="#" onclick="javascript:Ext.getCmp(\'this\').sortStore(\'label\')">'+LN('sbi.ds.label')+'</a></li> '+
	    		'	            <li id="name"><a href="#" onclick="javascript:Ext.getCmp(\'this\').sortStore(\'name\')">'+LN('sbi.ds.name')+'</a></li> '+
	    		'	            <li id="owner"><a href="#" onclick="javascript:Ext.getCmp(\'this\').sortStore(\'owner\')">'+LN('sbi.ds.owner')+'</a></li> '+
	    		'	        </ul> '+
	    		'	    </div> '+
	    		'	</div> '+
	    		'</div>' ;
//        var dh = Ext.DomHelper;
//        var b = this.bannerPanel.getEl().update(bannerHTML);

        return bannerHTML;
    }
});
  