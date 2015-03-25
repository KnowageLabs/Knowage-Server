/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.tools.dataset.QbeDataSetsWizard', {
	extend: 'Sbi.widgets.wizard.WizardWindow'

	, config: {	
		fieldsStep1: null,
		fieldsStep2: null,
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
		isTabbedPanel:false, //if false rendering as 'card layout (without tabs)
		qbeEditDatasetUrl : '',
		qbeIFrame : null
	}

	, constructor: function(config) {
		this.initConfig(config);
		if (this.record.owner !== undefined && this.record.owner !== this.user) {
			this.isOwner = false;
		}else{
			this.isOwner = true;
		}
		
		this.configureSteps();
		
		config.title =  LN('sbi.ds.wizard'); 	
		config.bodyPadding = 0;   
		config.tabs = this.initSteps();
		config.buttons = this.initWizardBar();

		this.callParent(arguments);
		
		this.addListener('cancel', this.closeWin, this);
		this.addListener('navigate', this.navigate, this);
		this.addListener('confirm', this.save, this);		
		
		this.addEvents('save','delete','getMetaValues','getDataStore');	
		
	}
	
	, configureSteps : function(){
   
		this.fieldsStep1 =  this.getFieldsTab1(); 
			
		this.fieldsStep2 =  this.getFieldsTab2(); 
		
	}
	
	, initSteps: function(){
		
		var steps = [];
		var item1Label = LN('sbi.tools.dataset.qbedatasetswizard.query');
		var item2Label = item1Label + ' -> ' + LN('sbi.ds.wizard.general');
		steps.push({
			itemId : '0'
			, title : item1Label
			, items : this.fieldsStep1
			, border : false
			, layout : 'fit'
		});
		steps.push({
			itemId :'1'
			, title :item2Label
			, items : Sbi.tools.dataset.QbeDataSetsWizard.superclass.createStepFieldsGUI(this.fieldsStep2)
			, border : false
			, bodyPadding : 20
		});
		return steps;
	}
	
	, getFieldsTab2: function(){
		//General tab
		var toReturn = [];
		
		toReturn = [{label:"Id", name:"id",type:"text",hidden:"true", value:this.record.id},
         {label: LN('sbi.ds.dsTypeCd'), name:"type",type:"text",hidden:"true", value:this.record.dsTypeCd || 'Qbe'},
         {label: LN('sbi.ds.label'), name:"label", type:"text",hidden:"true", /*mandatory:true, readOnly:(this.isNew || this.isOwner)?false:true,*/ value:this.record.label}, 
         {label: LN('sbi.ds.name'), name:"name", type:"text", mandatory:true, readOnly:(!this.isOwner), value:this.record.name},
         {label: LN('sbi.ds.description'), name:"description", type:"textarea", readOnly:(!this.isOwner), value:this.record.description}];
		toReturn.push({label:LN('sbi.ds.catType'), name:"catTypeVn", type:"combo", valueCol:"VALUE_ID", descCol:"VALUE_DS", readOnly:!this.isOwner, value:this.record.catTypeId, data:this.categoriesStore});
		var valueScope = (this.record.isPublic==true)?'true':'false' ;
		toReturn.push({label:LN('sbi.ds.scope'), name:"isPublicDS", type:"combo", valueCol:"field", descCol:"value", readOnly:!this.isOwner, value:valueScope, data:this.scopeStore});
		
		return toReturn;
	}
	
	, getFieldsTab1: function() {
		var datasetLabel = this.record.label;
		this.qbeIFrame = Ext.create('Sbi.widgets.EditorIFramePanelContainer', {});
		var url = this.qbeEditDatasetUrl + '&dataset_label=' + datasetLabel;
		Sbi.debug("[QbeDatasetWizard.getFieldsTab1]: url = [" + url + "]");
		this.qbeIFrame.on("afterrender", function (thePanel) {
			thePanel.load(url); 
		}, this);
		return this.qbeIFrame;
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
		 var activeTabId;
		 if (this.isTabbedPanel) {
			 activeTabId = parseInt(this.wizardPanel.getActiveTab().itemId);
		 } else {
			 activeTabId = parseInt(this.wizardPanel.layout.getActiveItem().itemId);
		 }
		 
		 var newTabId;
		 var numTabs  = (this.wizardPanel.items.length-1);
		 if (direction == 'next') {
			newTabId = activeTabId + ( (activeTabId < numTabs) ? 1 : 0 );
		 } else {			
			newTabId = activeTabId - ( (activeTabId <= numTabs) ? 1 : 0 );					
		 }
		 if (this.isTabbedPanel) {
			 this.wizardPanel.setActiveTab(newTabId);
		 } else {
			 this.wizardPanel.layout.setActiveItem(newTabId);
		 }
		 Ext.getCmp('move-prev').setDisabled(newTabId==0);
		 Ext.getCmp('move-next').setDisabled(newTabId==numTabs);
	 	 Ext.getCmp('confirm').setVisible(!(parseInt(newTabId)<parseInt(numTabs)));
	}
	
	, closeWin: function(){				
		this.destroy();
	}

	, save : function() {
		var values = Sbi.tools.dataset.QbeDataSetsWizard.superclass.getFormState();
		values.qbeDataSource = this.record.qbeDataSource;  // the user cannot change datasource
		Sbi.debug("[QbeDatasetWizard.save]: qbeDataSource = [" + values.qbeDataSource + "]");
		values.qbeDatamarts = this.record.qbeDatamarts;    // the user cannot change datamarts
		Sbi.debug("[QbeDatasetWizard.save]: qbeDatamarts = [" + values.qbeDatamarts + "]");
		values.qbeJSONQuery = this.getJSONQuery();
		Sbi.debug("[QbeDatasetWizard.save]: qbeJSONQuery = [" +values.qbeJSONQuery + "]");
		this.fireEvent('save', values);
	}

	,
	getJSONQuery : function () {
		var qbeWindow = this.qbeIFrame.iframe.getWin();
		Sbi.debug("[QbeDatasetWizard.getJSONQuery]: got qbeWindow = [" + qbeWindow + "]");
		var qbePanel = qbeWindow.qbe;
		Sbi.debug("[QbeDatasetWizard.getJSONQuery]: got qbePanel = [" + qbePanel + "]");
		var qbeJSONString = qbePanel.getQueriesCatalogueAsString();
		Sbi.debug("[QbeDatasetWizard.getJSONQuery]: got qbeJSONString = [" + qbeJSONString + "]");
		return qbeJSONString;
	}

	, goBack: function(n){
		 var newTabId;
		 if (this.isTabbedPanel) {
			 newTabId = parseInt(this.wizardPanel.getActiveTab().itemId)-n;
		 } else {
			 newTabId = parseInt(this.wizardPanel.layout.getActiveItem().itemId)-n;
		 }
		 var numTabs  = (this.wizardPanel.items.length-1);	
		 if (this.isTabbedPanel){
			 this.wizardPanel.setActiveTab(newTabId);
		 }else{
			 this.wizardPanel.layout.setActiveItem(newTabId);
		 }
		 Ext.getCmp('move-prev').setDisabled(newTabId==0);
		 Ext.getCmp('move-next').setDisabled(newTabId==numTabs);
//		 Ext.getCmp('confirm').setDisabled(newTabId<numTabs);
	}
	
	, disableButton: function(btn){
		 Ext.getCmp(btn).setDisabled(true);		
	}	
	
	, enableButton: function(btn){
		 Ext.getCmp(btn).setDisabled(false);
	}
	
});
