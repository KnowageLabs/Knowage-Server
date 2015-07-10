/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.tools.dataset.CkanDataSetsWizard', {
	extend: 'Sbi.widgets.wizard.WizardWindow'

	,onShow: function() {
			this.uploadFileButtonHandler();
	}	
		
	,config: {	
		fieldsStep1: null,
		fieldsStep2: null,
		fieldsStep3: null,
		fieldsStep4: null,
		categoriesStore: null,
		height: 440, 
		datasetGenericPropertiesStore: null,
		datasetPropertiesStore: null,
		datasetValuesStore: null,
		scopeStore: null,
		record: {},
		isNew:true, 
		user:'',
		fileUpload:null,
		metaInfo:null,
		isOwner: false,
		userCanPersist: false,
		tablePrefix: '',
		ckanId: '',
		ckanUrl: '',
		ckanFormat: '',
		ckanName: '',
		ckanDescription: '',
		services: null,
		isTabbedPanel:false //if false rendering as 'card layout (without tabs)
	}

	, constructor: function(config) {
		thisPanel = this;
		thisPanel.fileUploaded = false; //default value
		this.initConfig(config);
		if (this.record.owner !== undefined && this.record.owner !== this.user) {
			this.isOwner = false;
		}else{
			this.isOwner = true;
		}
		
		this.initServices();
		this.configureSteps();
		
		config.title =  LN('sbi.ds.wizard'); 	
		config.bodyPadding = 10;   
		config.tabs = this.initSteps();
		config.buttons = this.initWizardBar();

		this.callParent(arguments);
		
		this.addListener('cancel', this.closeWin, this);
		this.addListener('navigate', this.navigate, this);
		this.addListener('confirm', this.save, this);		
		
		this.addEvents('save','delete','getMetaValues','getDataStore');	
		
	}
	
	, initServices : function(baseParams) {
		this.services = [];
		
		if(baseParams == undefined){
			baseParams ={};
		}
		baseParams.url = this.ckanUrl;
		baseParams.id = this.ckanId;
		baseParams.format = this.ckanFormat
		baseParams.showOnlyOwner = true;

		this.services["download"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'ckan-management/download',
			baseParams: baseParams
		});
	}
	
	, configureSteps : function(){
   
		this.fieldsStep1 =  this.getFieldsTab1(); 
			
		this.fieldsStep2 =  this.getFieldsTab2(); 
		
		this.fieldsStep3 =  this.getFieldsTab3();
		
		this.fieldsStep4 = this.getFieldsTab4();
	}
	, initSteps: function(){
		
		var steps = [];
		var item1Label = LN('sbi.ds.wizard.detail');
		var item2Label = item1Label + ' -> ' + LN('sbi.ds.wizard.metadata');
		var item3Label = item2Label + ' -> ' + LN('sbi.ds.wizard.validation');
		//var item4Label = item3Label + ' -> ' + LN('sbi.ds.wizard.general');
		steps.push({itemId:'0', title:item1Label, items: this.fieldsStep1});
		steps.push({itemId:'1', title:item2Label, items: this.fieldsStep2});
		steps.push({itemId:'2', title:item3Label, items: this.fieldsStep3});
		//steps.push({itemId:'3', title:item4Label, items: this.fieldsStep4});
		return steps;
	}
	
	, getFieldsTab4: function(){
		var hidePersistFields = "true";
		if ((this.userCanPersist != undefined) && (this.userCanPersist == 'true')){
			hidePersistFields = "false";
		}
		
		//30 characters are the maximum for Oracle tables
		var tableNamePrefix = this.tablePrefix+this.user+"_";
		var maxTableNameLength = 30 - (tableNamePrefix.length);
		
		//General tab
		var toReturn = [];
		
		toReturn = [{label:"Id", name:"id",type:"text",hidden:"true", value:this.record.id},
		            {label: LN('sbi.ds.dsTypeCd'), name:"type",type:"text",hidden:"true", value:'File'},
		            {label: LN('sbi.ds.label'), name:"label", type:"text",hidden:"true", /*mandatory:true, readOnly:(this.isNew || this.isOwner)?false:true,*/ value:this.record.label}, 
		            {label: LN('sbi.ds.name'), name:"name", type:"text", mandatory:true, readOnly:(!this.isOwner), value:this.ckanName},
		            {label: LN('sbi.ds.description'), name:"description", type:"textarea", readOnly:(!this.isOwner), value:this.ckanDescription},
		            {label: LN('sbi.ds.isPersisted'), name:"persist", type:"checkbox", value:false, hidden: hidePersistFields},
		            {label: LN('sbi.ds.persistTablePrefix'), name:"tablePrefix",type:"text",hidden: hidePersistFields,readOnly:true, value:tableNamePrefix, id:"tablePrefixId", disabled:true, hideBorder:true },       
		            {label: LN('sbi.ds.persistTableName'), name:"tableName", type:"text", hidden: hidePersistFields, id:"tableNameId", disabled: true, maxLength: maxTableNameLength, msgTarget:'under' }
		            ];
		
		var fields = Sbi.tools.dataset.CkanDataSetsWizard.superclass.createStepFieldsGUI(toReturn);
		
		//listener for persist checkbox
		for(var i=0; i < fields.length; i++){
			if (fields[i].name == 'persist'){
				fields[i].on("change", function(cb, checked) {
					if (checked){
						 Ext.getCmp('tableNameId').validationEvent = true;
				         Ext.getCmp('tableNameId').allowBlank = false;
					     Ext.getCmp('tableNameId').setDisabled(!checked);
					     Ext.getCmp('tableNameId').regexText = LN('sbi.ds.persistTableName.error');
					     Ext.getCmp('tableNameId').regex = /^([a-zA-Z0-9_]+)$/;
				         Ext.getCmp('tableNameId').validate();
				         
					     Ext.getCmp('tablePrefixId').setDisabled(!checked);
					} else {
						 Ext.getCmp('tableNameId').validationEvent = false;
				         Ext.getCmp('tableNameId').allowBlank = true;
					     Ext.getCmp('tableNameId').setDisabled(!checked);
					     Ext.getCmp('tableNameId').regex = '';
				         Ext.getCmp('tableNameId').validate();
				         
					     Ext.getCmp('tablePrefixId').setDisabled(!checked);
					}
				 });
				break;
			}
		}
		
		return fields;
	}
	
	, getFieldsTab1: function(){
		
		this.cmbCategory = new Ext.form.ComboBox({
			id: 'dsCategoryCombo',
			fieldLabel: LN('sbi.ds.catType'),
			store :this.categoriesStore,
			name : 'catTypeVn',			
			displayField : 'VALUE_DS',
			valueField :  'VALUE_ID',
			width : 300,
			typeAhead : true, forceSelection : true,
			mode : 'local',
			hidden: true,
			triggerAction : 'all',
			selectOnFocus : true, 
			editable : false,
			readOnly:!this.isOwner,
			value: this.record.catTypeId,
			style:'padding:5px',
			listeners: {
			    afterrender: function(combo) {
			    	if (!this.rawValue || this.rawValue == ''){
				        var recordSelected = combo.getStore().getAt(0);                     
				        combo.setValue(recordSelected.get('VALUE_ID'));
				        
				        if (combo.getStore().data.length < 2){
				        	combo.hidden = true;
				        } else {
				        	combo.hidden = false;
				        }
				        				        
			    	}
			    }
			}
		});
		
		//checkbox for limited preview
		this.checkLimitPreview = new Ext.form.Checkbox({
			boxLabel  : LN('sbi.ds.wizard.limitPreview'),
            name      : 'limitPreview',
            id        : 'limitPreview',
            hidden	  : true,
            value	  : true,
    		margin: 5

        })
		
		this.containerPanel = new Ext.panel.Panel({
			border: false,
			layout: {
		        type: 'table',
		        columns: 2
		    },
			items:[this.cmbCategory,this.checkLimitPreview]
		})
		//upload details tab
		this.fileUpload = new Sbi.tools.dataset.FileDatasetPanel({fromExt4:true, isOwner: this.isOwner});
		if (this.record !== undefined){
			this.fileUpload.setFormState(this.record);
		}
		
		//var uploadField = this.fileUpload.getComponent('fileUploadPanel').getComponent('fileUploadField');
		var uploadField = this.fileUpload.getComponent('fileUploadPanel').getComponent('buttonsPanel').getComponent('fileUploadField');
		uploadField.hide();
		var detailText = this.fileUpload.getComponent('fileUploadPanel').getComponent('fileDetailText');
		var msgStart = LN('sbi.ds.wizard.ckan.startMsg');
		detailText.setText(msgStart);
		var uploadButton = this.fileUpload.getComponent('fileUploadPanel').getComponent('fileUploadButton');
		uploadButton.hide();
		uploadButton.setHandler(this.uploadFileButtonHandler,this);
		var loadText = LN('sbi.ds.wizard.ckan.load');
		uploadButton.setText(loadText);
		uploadButton.enable();
		
		var toReturn = new  Ext.FormPanel({
			  id: 'datasetForm',
			  fileUpload: true, // this is a multipart form!!
			  isUpload: true,
			  method: 'POST',
			  enctype: 'multipart/form-data',
	          labelAlign: 'left',
	          bodyStyle:'padding:20px',
	          width: '100%', 
	          height: 330,
	          border: false,
	          trackResetOnLoad: true,
	          items: [this.containerPanel, this.fileUpload]
	      }); 
		
		
		return toReturn;
	}
	
	, getFieldsTab2: function(){
		//metadata tab
		var config = {};
		config.meta = this.record.meta;
		config.datasetGenericPropertiesStore = this.datasetGenericPropertiesStore;
		config.datasetPropertiesStore = this.datasetPropertiesStore;
		config.datasetValuesStore = this.datasetValuesStore;
		config.isOwner = this.isOwner;
		//this.metaInfo = new Sbi.tools.dataset.ManageDatasetFieldMetadata(config);
		this.metaInfo = new Sbi.tools.dataset.DatasetMetadataMainPage(config);
		return this.metaInfo;
	}
	
	, getFieldsTab3: function() {
		//dataset validation tab
		var config = {};
		//create empty panel
		this.validateDatasetInfo = new Sbi.tools.dataset.ValidateDataset(config);
		return this.validateDatasetInfo;
	}

	
	, initWizardBar: function() {
		var bar = this.callParent();
		for (var i=0; i<bar.length; i++){
			var btn = bar[i];
			if (btn.id === 'confirm'){
				if (!this.isOwner) {					
					btn.disabled = true;
				}
			}				
		}
		return bar;
	}
	
	, navigate: function(panel, direction){		
        // This routine could contain business logic required to manage the navigation steps.
        // It would call setActiveItem as needed, manage navigation button state, handle any
         // branching logic that might be required, handle alternate actions like cancellation
         // or finalization, etc.  A complete wizard implementation could get pretty
         // sophisticated depending on the complexity required, and should probably be
         // done as a subclass of CardLayout in a real-world implementation.
		 var layout = panel.getLayout();
		 var newTabId;
		 if (this.isTabbedPanel){
			 newTabId  = parseInt(this.wizardPanel.getActiveTab().itemId);
		 }else{
			newTabId  = parseInt(this.wizardPanel.layout.getActiveItem().itemId);
		 }
		 
		 var oldTabId = newTabId;
		 var numTabs  = (this.wizardPanel.items.length-1);
		 var isTabValid = true;
		 if (direction == 'next'){
			 newTabId += (newTabId < numTabs)?1:0;	
			 if (newTabId == 0){
				 isTabValid = this.validateTab0();					
			 }
			 if (newTabId == 1){
				isTabValid = this.validateTab1();
				if (isTabValid){						
					var values = Sbi.tools.dataset.CkanDataSetsWizard.superclass.getFormState();
					//added manually category because it's moved on the first tab (it's necessary for correct validation action)
					values[this.cmbCategory.name] = this.cmbCategory.getValue();	
					var fileValues = this.fileUpload.getFormState();
					Ext.apply(values, fileValues);
					if (this.record.meta !== undefined){
						var metaValues = this.metaInfo.getFormState();
						values.meta = metaValues;
					}
					//If true a new file is uploaded
					values.fileUploaded = this.fileUploaded;
					this.fireEvent('getMetaValues', values);
					
					var categoryName = Ext.getCmp('dsCategoryCombo').getRawValue();
					this.metaInfo.setDatasetCategory(categoryName);
					
				}
			 }
			 if (newTabId == 2){				 
				 var values = Sbi.tools.dataset.CkanDataSetsWizard.superclass.getFormState();
				 //added manually category because it's moved on the first tab (it's necessary for correct validation action)
				 values[this.cmbCategory.name] = this.cmbCategory.getValue();	
				 var fileValues = this.fileUpload.getFormState();
				 Ext.apply(values, fileValues);
				 //If true a new file is uploaded
				 values.fileUploaded = this.fileUploaded;
				 //Scope of the dataset (default is private otherwise is public-shared)
				 var valueScope = (this.record.isPublic==true)?'true':'false' ;
				 values.isPublicDS = valueScope;
				 var datasetMetadata = this.fieldsStep2.getFormState();
				 values.datasetMetadata = Ext.JSON.encode(datasetMetadata) ;
				 values.limitPreview = this.checkLimitPreview.getValue();
				 this.fieldsStep3.createDynamicGrid(values);
			 }
		 }else{			
			newTabId -= (newTabId <= numTabs)?1:0;					
		 }
		 if (isTabValid){
			 if (this.isTabbedPanel){
				 this.wizardPanel.setActiveTab(newTabId);
			 }else{
				 this.wizardPanel.layout.setActiveItem(newTabId);
			 }
			 Ext.getCmp('move-prev').setDisabled(newTabId==0);
			 Ext.getCmp('move-next').setDisabled(newTabId==numTabs);
		 	 Ext.getCmp('confirm').setVisible(!(parseInt(newTabId)<parseInt(numTabs)));
		 }			 
	}
	
	, closeWin: function(){				
		this.destroy();
	}

	, save : function(){
		if (this.validateTab0() && this.validateTab1()){
			var values = Sbi.tools.dataset.CkanDataSetsWizard.superclass.getFormState();
			if (values['label'] == undefined || values['label'] == ''){
				//defining a new label for the dataset
				var d = new Date();
				values['label'] = 'ds__' + d.getTime()%10000000; 
			}
			//added manually category because it's moved on the first tab (it's necessary for correct validation action)
			values[this.cmbCategory.name] = this.cmbCategory.getValue();	
			var fileValues = this.fileUpload.getFormState();
			Ext.apply(values, fileValues);
			if (this.metaInfo !== undefined){
				var metaValues = this.metaInfo.getFormState();
				values.meta = metaValues;
			}
			//If true a new file is uploaded
			values.fileUploaded = thisPanel.params.fileUploaded;
			//Scope of the dataset (default is private otherwise is public-shared)
			var valueScope = (this.record.isPublic==true)?'true':'false' ;
			values.isPublicDS = valueScope;
			//values.fileName = this.ckanUrl;
			values.type = 'Ckan';
			values.ckanUrl = this.ckanUrl;
			values.ckanId = this.ckanId;
			this.fireEvent('save', values);
		}
	}
	
	, validateTab0: function(){		
		if (!Sbi.tools.dataset.CkanDataSetsWizard.superclass.validateForm()){
			Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.ds.mandatoryFields'), '');
			return false;
		}
		return true;
	}
	
	, validateTab1: function(){
		var categ = this.cmbCategory;	
		if (categ == undefined || categ.getValue() == ""){
			Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.ds.mandatoryFields'), '');
			return false;
		}
		var fileName = this.fileUpload.fileNameField;
		if (fileName == undefined || fileName.value == ""){
			Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.ds.mandatoryUploadFile'), '');
			return false;
		}
		var fileType =  this.fileUpload.fileType;
		if (fileType == undefined || fileType.value == null || fileType.value == ""){
			Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.ds.mandatoryFields'), '');
			return false;
		}
		if (fileType.value == 'CSV'){
			var csvDelimiterCombo = this.fileUpload.csvDelimiterCombo;
			var csvQuoteCombo = this.fileUpload.csvQuoteCombo;
			var csvEncodingCombo = this.fileUpload.csvEncodingCombo
			if (csvDelimiterCombo == undefined || csvDelimiterCombo.value == null|| csvDelimiterCombo.value == "" ||
				csvQuoteCombo == undefined || csvQuoteCombo.value == null || csvQuoteCombo.value == "" ||
				csvEncodingCombo == undefined || csvEncodingCombo.value == null || csvEncodingCombo.value == ""){
				Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.ds.mandatoryFields'), '');
				return false;
			}
		}
		
		return true;		
	}
	
	//handler for the upload file button
	,uploadFileButtonHandler: function(btn, e) {
		Sbi.debug("[CkanDatasetWizard.uploadFileButtonHandler]: IN");
		
		Sbi.debug("[CkanDatasetWizard.uploadFileButtonHandler]: Start downloading CKAN resource...");
		var fileDetail = null;
		
		Ext.MessageBox.wait(LN('sbi.generic.wait'));
		Ext.Ajax.request({ 
			url: this.services['download'],
			async: false,
			success: function(responseObject){
				Ext.MessageBox.updateProgress(1);
				Ext.MessageBox.hide();
				if(responseObject !== undefined  && responseObject.responseText !== undefined) {
					if(responseObject.responseText !== '{}') {
						fileDetail = Ext.decode(responseObject.responseText);
					} else {
						Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.ds.wizard.ckan.downloadError'), '');
						this.closeWin();
					}
				}
			},
			failure: function(responseObject){
				Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.ds.wizard.ckan.downloadError'), '');
				this.closeWin();
			}
		});
		
		if(fileDetail.hasOwnProperty('errors')) {
			Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.ds.wizard.ckan.downloadError'), '');
			this.closeWin();
		}
		else {
		
//		this.fileUpload.getComponent('fileUploadPanel').getComponent('fileType').setValue(fileDetail.filetype);
//		this.fileUpload.getComponent('fileUploadPanel').getComponent('fileNameField').setValue(fileDetail.filename);
//		this.fileUpload.getComponent('fileUploadPanel').getComponent('buttonsPanel').getComponent('fileUploadField').setValue(fileDetail.filepath);		
//		console.log(this.fileUpload.getComponent('fileUploadPanel').getComponent('buttonsPanel').getComponent('fileUploadField'));
				
		Ext.getCmp('fileNameField').setValue(fileDetail.filename); //hidden field
		Ext.getCmp('fileDetailText').setText(LN('sbi.ds.wizard.ckan.dataset') +" " + LN('sbi.ds.wizard.successLoad') );
//		Ext.getCmp('fileUploadField').hide();
//		Ext.getCmp('fileUploadButton').hide();
				
		this.fileUpload.activateFileTypePanel(fileDetail.filetype);
		this.fileUploaded = true;
		}
		Sbi.debug("[CkanDatasetWizard.uploadFileButtonHandler]: OUT");
	}

	, goBack: function(n){
		 var newTabId;
		 if (this.isTabbedPanel){
			 newTabId  = parseInt(this.wizardPanel.getActiveTab().itemId)-n;
		 }else{
			 newTabId  = parseInt(this.wizardPanel.layout.getActiveItem().itemId)-n;
		 }
		 var numTabs  = (this.wizardPanel.items.length-1);	
		 if (this.isTabbedPanel){
			 this.wizardPanel.setActiveTab(newTabId);
		 }else{
			 this.wizardPanel.layout.setActiveItem(newTabId);
		 }
		 Ext.getCmp('move-prev').setDisabled(newTabId==0);
		 Ext.getCmp('move-next').setDisabled(newTabId==numTabs);
	}
	
	, disableButton: function(btn){
		 Ext.getCmp(btn).setDisabled(true);		
	}	
	
	, enableButton: function(btn){
		 Ext.getCmp(btn).setDisabled(false);
	}
	
});
