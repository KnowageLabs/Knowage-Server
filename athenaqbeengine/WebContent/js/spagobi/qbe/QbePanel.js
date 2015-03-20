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
 *  [list]
 * 
 * 
 * Public Events
 * 
 *  [list]
 * 
 * Authors
 * 
 * - Andrea Gioia (andrea.gioia@eng.it)
 */

Ext.ns("Sbi.qbe");

Sbi.qbe.QbePanel = function(config) {

	var c = Ext.apply({
		// set default values here
		displayQueryBuilderPanel: true
		, displayFormBuilderPanel: false
		, displayWorksheetPanel: true
	}, config || {});

	
	if(!Sbi.cache){
		Sbi.cache = {};
	}
	
	if(!Sbi.cache.memory){
		Sbi.cache.memory = new Sbi.widgets.Cache({});
	}
	
	this.services = new Array();
	var params = {};
	this.services['getFirstQuery'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_FIRST_QUERY_ACTION'
			, baseParams: params
	});
	this.services['saveAnalysisState'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'SAVE_ANALYSIS_STATE_ACTION'
			, baseParams: params
	});
	this.services['getWorkSheetState'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_WORKSHEET_PREVIEW_ACTION'
			, baseParams: params
	});
	this.services['setWorkSheetState'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'SET_WORKSHEET_DEFINITION_ACTION'
			, baseParams: params
	});
	/*
	this.services['getAmbiguousFields'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_AMBIGUOUS_FIELDS_ACTION'
			, baseParams: params
	});
	*/

	this.addEvents('save');
	this.addEvents('returnToMyAnalysis');


	if (c.initialQueriesCatalogue) {
		this.setInitialQueriesCatalogue(c.initialQueriesCatalogue);
	}

	this.queryEditorPanel = null;
	this.queryResultPanel = new Sbi.widgets.DataStorePanel(Ext.apply(c, {
		id : 'DataStorePanel'
	}));
	this.queryResultPanel.on('activate', this.resultPanelActivateHandler, this);
	
	this.worksheetPanel = null;
	
	this.worksheetDesignerPanel = null;

	var items = [];
	var qbeItems = [];

	if (c.displayQueryBuilderPanel) {
		this.queryEditorPanel = new Sbi.qbe.QueryBuilderPanel(Ext.apply(c, {
			id : 'QueryBuilderPanel'
		}));
		qbeItems.push( this.queryEditorPanel );
	}

	qbeItems.push( this.queryResultPanel );
	this.qbeCardsPanel = new Sbi.qbe.QbeCardsPanel(Ext.apply(c, {
		id : 'QbeCardsPanel' // WORKAROUND: without this we had a strange behaviour!!! when activating Worksheet panel, it was still not visible!!
		, items : qbeItems
	}));
	// in case there's a cross navigation and user is power user, we activate the result panel directly
	if (qbeItems.length > 1 && config.isFromCross) {
		this.qbeCardsPanel.on('render', function() {
			this.qbeCardsPanel.getLayout().setActiveItem(1);
		}, this);
	}
	
	items.push(this.qbeCardsPanel);


	if (c.displayWorksheetPanel) {

		var worksheetDesignerConfig = c.worksheet || {};
		this.worksheetDesignerPanel = new Sbi.worksheet.designer.WorksheetDesignerPanel(Ext.apply(worksheetDesignerConfig, {
			id : 'WorksheetDesignerPanel'
		}));
		
	}
	


	if (c.displayWorksheetPanel) {
		this.worksheetPreviewPanel = new Sbi.worksheet.runtime.WorkSheetPreviewPage({
			id : 'WorkSheetPreviewPage',
			closable: false
		});
		
		/*
		 * Workaround (Work-around) : the following instruction is needed because in some cases, in IE, events are suspended!!!
		 * This was causing https://spagobi.eng.it/jira/browse/SPAGOBI-1291 : IE bug: When designing a Worksheet starting from a Qbe document, Worksheet preview is not displayed
		 */
		if (Ext.isIE) {
			this.worksheetPreviewPanel.resumeEvents();
		}

		this.worksheetPreviewPanel.on('activate', function() {
			
			this.worksheetDesignerPanel.validate(
					function(){
						this.setWorksheetState(this.refreshWorksheetPreview, Sbi.exception.ExceptionHandler.handleFailure, this);
					}
					, this.worksheetDesignerPanel.showValidationErrors
					, this);
			
		}, this);

		//items.push(this.worksheetPreviewPanel);
	}

	if (c.displayFormBuilderPanel && c.formbuilder !== undefined && c.formbuilder.template !== undefined) {
		this.formBuilderPage = new Sbi.formbuilder.FormPanel({template: c.formbuilder.template});
		items.push(this.formBuilderPage);
	}

	/*
	if (!c.displayQueryBuilderPanel) {
		// if user is a read-only user, do not instantiate and show the QueryBuilderPanel
		// and execute first query on catalog
		this.loadFirstQuery();
	}
	*/

	
	if (c.displayWorksheetPanel) {		
		var worksheetDesignerConfig = c.worksheet || {};
		this.worksheetPanel = new Sbi.worksheet.designer.WorksheetPanel(Ext.apply(worksheetDesignerConfig, {
			id : 'WorksheetPanel'
			, worksheetDesignerPanel : this.getWorksheetDesignerPanel()
			, worksheetPreviewPanel : this.getWorksheetPreviewPanel()
		}));
		items.push(this.worksheetPanel);
	}
	
	
	this.tabs = new Ext.TabPanel({
		border : false
		, activeTab : 0
		, items : items
		, hideMode : !Ext.isIE ? 'nosize' : 'display'
	});
		

	if (this.queryEditorPanel != null) {
		this.queryEditorPanel.on('save', function(meta){
			this.saveQuery(meta);
		}, this);
		/*
		 * work-around: forcing the layout recalculation on west/center/est region panels on tab change
		 * TODO: try to remove it when upgrading Ext library
		 */
		this.tabs.on('tabchange', function () {

			var anActiveTab = this.tabs.getActiveTab();
						

			if (anActiveTab.centerRegionPanel !== undefined) {
				anActiveTab.centerRegionPanel.doLayout();
			}
			if (anActiveTab.westRegionPanel !== undefined) {
				anActiveTab.westRegionPanel.doLayout();
			}
			if (anActiveTab.eastRegionPanel !== undefined) {
				anActiveTab.eastRegionPanel.doLayout();
			}

			if(config.isFromCross) {
				if(anActiveTab.selectGridPanel != null && anActiveTab.selectGridPanel.dropTarget === null) {
					anActiveTab.selectGridPanel.dropTarget = new Sbi.qbe.SelectGridDropTarget(anActiveTab.selectGridPanel);
				}

				if(anActiveTab.filterGridPanel != null && anActiveTab.filterGridPanel.dropTarget === null) {
					anActiveTab.filterGridPanel.dropTarget = new Sbi.qbe.FilterGridDropTarget(anActiveTab.filterGridPanel);
				}

				if(anActiveTab.havingGridPanel != null && anActiveTab.havingGridPanel.dropTarget === null) {
					anActiveTab.havingGridPanel.dropTarget = new Sbi.qbe.HavingGridDropTarget(anActiveTab.havingGridPanel);
				}

				if(anActiveTab.filtersTemplatePanel != null && anActiveTab.filtersTemplatePanel.staticOpenFiltersEditorPanel != null
						&& anActiveTab.filtersTemplatePanel.staticOpenFiltersEditorPanel.dropTarget === null) {
					anActiveTab.filtersTemplatePanel.staticOpenFiltersEditorPanel.dropTarget = 
						new Sbi.formbuilder.StaticOpenFiltersEditorPanelDropTarget(anActiveTab.filtersTemplatePanel.staticOpenFiltersEditorPanel);
				}
			}
		}, this);
	}

	c = Ext.apply(c, {
		layout: 'fit',
		autoScroll: true, 
		margins:'0 4 4 0',
		items: [this.tabs] 
	});

	// constructor
	Sbi.qbe.QbePanel.superclass.constructor.call(this, c);

	
	this.tabs.on("tabchange",function(object,tab){
		try{
			if(tab.id == 'WorksheetPanel'){
				sendMessage({button: "saveworksheet", property:"visibility", value:"true"},"managebutton");
			}else{
				sendMessage({button: "saveworksheet", property:"visibility", value:"false"},"managebutton");
			}	
		}catch (e){}

	},this)
	
	/*
	if (config.isFromCross) {
		this.loadFirstQuery();
	}
	*/
};

/**
 * @class Sbi.qbe.QbePanel
 * @extends Ext.Panel
 * 
 * Main QBE panel
 */
Ext.extend(Sbi.qbe.QbePanel, Ext.Panel, {

	services: null
	, qbeCardsPanel: null
	, queryResultPanel: null
	, queryEditorPanel: null
	, worksheetPanel: null
	, worksheetDesignerPanel: null
	, worksheetPreviewPanel: null
	, initialQueriesCatalogue: null // used as a queries repository variable when the queryEditorPanel is not displayed
	, tabs: null
	, query: null
	, previousWorksheetDefinition: null


	// public methods

	, setQuery: function(q) {
	query = q;
	this.queryEditorPanel.setQuery(q);
}

, getSQLQuery: function(callbackFn, scope) {
	this.queryEditorPanel.getSQLQuery(callbackFn, scope);
}

, getQueries: function() {
	return this.queryEditorPanel.getQueries();
}

//private methods
, loadFirstQuery: function() {
	Ext.Ajax.request({
		url: this.services['getFirstQuery'],
		params: {},
		success : function(response, opts) {
			try {
				var firstQuery = Ext.util.JSON.decode( response.responseText );
				if (!Sbi.user.isPowerUser && firstQuery.fields.length == 0) {
					// user is NOT a power user and the first query is empty: most likely the user is executing 
					// a Qbe document, not a saved query
		        	Ext.MessageBox.show({
		           		title: LN('sbi.qbe.qbepanel.emptyquerytitle')
		           		, msg: LN('sbi.qbe.qbepanel.emptyquerymessage')
		           		, buttons: Ext.MessageBox.OK
		           		, icon: Ext.MessageBox.WARNING
		           		, modal: false
		       		});
		        	return;
				}
				this.checkPromptableFilters(firstQuery);
			} catch (err) {
				Sbi.exception.ExceptionHandler.handleFailure();
			}
		},
		scope: this,
		failure: Sbi.exception.ExceptionHandler.handleFailure      
	});
}

//check if there are some promptable filters before starting query execution
, checkPromptableFilters: function(query) {
	var freeFilters = this.getPromptableFilters(query);
	if (freeFilters.length > 0) {
		var freeConditionsWindow = new Sbi.qbe.FreeConditionsWindow({
			freeFilters: freeFilters
			, modal : true
		});
		freeConditionsWindow.on('apply', function (formState) {
			// make last values persistent on filter grid panel
			if (this.queryEditorPanel != null) {
				this.queryEditorPanel.filterGridPanel.setPromptableFiltersLastValues(formState);
				this.queryEditorPanel.havingGridPanel.setPromptableFiltersLastValues(formState);
			}
			this.executeQuery(query, formState);
		}, this);
		freeConditionsWindow.on('savedefaults', function (formState) {
			// make default values persistent on filter grid panel
			if (this.queryEditorPanel != null) {
				this.queryEditorPanel.filterGridPanel.setPromptableFiltersDefaultValues(formState);
				this.queryEditorPanel.havingGridPanel.setPromptableFiltersDefaultValues(formState);
			}
		}, this);
		freeConditionsWindow.show();
	} else {
		this.executeQuery(query);
	}
}

, executeQuery: function(query, promptableFilters) {
	var newPromptableFilters = { promptableFilters : Ext.encode(promptableFilters)};
	this.queryResultPanel.execQuery(query, newPromptableFilters);
}

/*
, executeQuery: function(query, promptableFilters) {
	this.checkAmbiguousFields(query, promptableFilters);
}

, checkAmbiguousFields: function(query, promptableFilters) {
	// call the server to get ambiguous fields
	Ext.Ajax.request({
		url: this.services['getAmbiguousFields'],
		params: {id: query.id},
		success : this.onAmbiguousFieldsLoaded.createDelegate(this, [query, promptableFilters], true),
		scope: this,
		failure: Sbi.exception.ExceptionHandler.handleFailure      
	});
}

, onAmbiguousFieldsLoaded : function (response, opts, query, promptableFilters) {
	try {
		var ambiguousFields = Ext.util.JSON.decode( response.responseText );
		if (ambiguousFields.length == 0) {
			this.doExecuteQuery(query, promptableFilters);
		} else {
			ambiguousFields = this.mergeAmbiguousFieldsWithCache(query, ambiguousFields);
			var relationshipsWindow = new Sbi.qbe.RelationshipsWizardWindow({
				ambiguousFields : ambiguousFields
				, closeAction : 'close'
				, modal : true
			});
			relationshipsWindow.show();
			relationshipsWindow.on('apply', this.onAmbiguousFieldsSolved.createDelegate(this, [query, promptableFilters], true), this);
		}
	} catch (err) {
		Sbi.exception.ExceptionHandler.handleFailure();
	}
}

,
mergeAmbiguousFieldsWithCache : function (query, ambiguousFields) {
	var cached = this.getAmbiguousFieldsFromCache(query);
	var ambiguousFieldsObj = new Sbi.qbe.AmbiguousFields({ ambiguousFields : ambiguousFields });
	var cachedObj = new Sbi.qbe.AmbiguousFields({ ambiguousFields : cached });
	ambiguousFieldsObj.merge(cachedObj);
	return ambiguousFieldsObj.getAmbiguousFieldsAsJSONArray();
}

,
putAmbiguousFieldsSolvedOnCache : function (query, ambiguousFieldsSolved) {
	Sbi.cache.memory.put(query.id, ambiguousFieldsSolved);
}

,
getAmbiguousFieldsFromCache : function (query) {
	var cached = Sbi.cache.memory.get(query.id);
	return cached;
}

, onAmbiguousFieldsSolved : function (theWindow, ambiguousFieldsSolved, query, promptableFilters) {
	theWindow.close();
	this.putAmbiguousFieldsSolvedOnCache(query, ambiguousFieldsSolved);
	this.doExecuteQuery(query, promptableFilters, ambiguousFieldsSolved);
}

, doExecuteQuery: function(query, promptableFilters, ambiguousFieldsSolved) {
	var newPromptableFilters = { promptableFilters : Ext.encode(promptableFilters)};
	this.queryResultPanel.execQuery(query, newPromptableFilters, ambiguousFieldsSolved);
}
*/

, getPromptableFilters : function(query) {
	var filters = [];
	if (query.filters != null && query.filters.length > 0) {
		for(i = 0; i < query.filters.length; i++) {
			var filter =  query.filters[i];
			if (filter.promptable) {
				filters.push(filter);
			}
		}
	}
	if (query.havings != null && query.havings.length > 0) {
		for(i = 0; i < query.havings.length; i++) {
			var filter = query.havings[i];
			if (filter.promptable) {
				filters.push(filter);
			}
		}
	}
	return filters;
}

, saveQuery: function(meta) {
	this.saveAnalysisState(meta, function(response, options) {
		// for old gui
		try {
			var content = Ext.util.JSON.decode( response.responseText );
			content.text = content.text || "";
			parent.loadSubObject(window.name, content.text);
		} catch (ex) {}

		// for new gui
		// build a JSON object containing message and ID of the saved  object
		try {
			// get the id of the subobject just inserted, decode string, need to call metadata window
			var responseJSON = Ext.util.JSON.decode( response.responseText )
			var id = responseJSON.text;
			var msgToSend = 'Sub Object Saved!!';

			//sendMessage({'id': id, 'meta' : meta.metadata, 'msg': msgToSend},'subobjectsaved');
			//alert('id '+id+' message '+msgToSend);
			sendMessage({'id': id, 'msg': msgToSend},'subobjectsaved');
		} catch (ex) {}
		// show only if not showing metadata windows
		/*if( meta.metadata == false ){
			Ext.Msg.show({
				   title:LN('sbi.qbe.queryeditor.querysaved'),
				   msg: LN('sbi.qbe.queryeditor.querysavedsucc'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.INFO
			});
		}*/
	}, this);
}

, saveAnalysisState: function(meta, callback, scope) {

	var params = Ext.apply({}, meta);

	var doSave = function() {
		Ext.Ajax.request({
			url: this.services['saveAnalysisState'],
			success: callback,
			failure: Sbi.exception.ExceptionHandler.handleFailure,	
			scope: scope,
			params: params
		});  
	};

	this.queryEditorPanel.queryCataloguePanel.commit(function() {
		if(Sbi.config.queryValidation.isEnabled) {
			this.queryEditorPanel.queryCataloguePanel.validate(doSave, this);
		} else {
			doSave();
		}

	}, this);		
}

,
openSaveDataSetWizard: function(fromMyAnalysis) {
	if (fromMyAnalysis != undefined && fromMyAnalysis != null && fromMyAnalysis == 'TRUE'){
		this.fromMyAnalysis = fromMyAnalysis;
	}
	var queries = this.getQueriesCatalogue();
	var saveDatasetWindow = new Sbi.qbe.SaveDatasetWindow( { queries : queries } );
	saveDatasetWindow.on('save', function(theWindow, formState) { 
		theWindow.close(); 
		if (this.fromMyAnalysis != undefined && this.fromMyAnalysis != null && this.fromMyAnalysis == 'TRUE'){
			this.fireEvent('save') 
		}
	}, this);
	
	saveDatasetWindow.show();
}

/*
 * This method is invoked by Sbi.execution.DocumentExecutionPage on SpagoBI core!!!
 * See SpagoBI/js/src/ext/sbi/execution/DocumentExecutionPage.js, retrieveQbeCrosstabData method
 */
, getCrosstabDataEncoded: function () {

	var crosstabData = this.worksheetPreviewPanel.serializeCrossTab(); // TODO manage crosstab export
	var crosstabDataEncoded = Ext.util.JSON.encode(crosstabData);
	return crosstabDataEncoded;

}

,
getParameters: function () {
	return this.queryEditorPanel.getParameters();
}

,
setParameters: function (parameters) {
	this.queryEditorPanel.setParameters(parameters);
}

,
getInitialQueriesCatalogue: function () {
	return this.initialQueriesCatalogue;
}

,
setInitialQueriesCatalogue: function (initialQueriesCatalogue) {
	this.initialQueriesCatalogue = initialQueriesCatalogue;
}

, 
getQueriesCatalogue: function () {
	if (this.queryEditorPanel == null) {
		// query designer panel not displayed, returns the initial catalogue
		return this.getInitialQueriesCatalogue();
	} else {
		// query designer panel displayed
		var toReturn = {};
		toReturn.catalogue = {};
		toReturn.catalogue.queries = this.getQueries();
		toReturn.version = Sbi.config.queryVersion;
		return toReturn;
	}
}

,
getQueriesCatalogueAsString: function () {
	var catalogue = this.getQueriesCatalogue();
	var toReturn = Ext.util.JSON.encode(catalogue);
	return toReturn;
}

,
setQueriesCatalogue: function (queriesCatalogue) {
	if (this.queryEditorPanel != null) {
		this.queryEditorPanel.setQueriesCatalogue(queriesCatalogue);
	} else {
		alert('Query builder panel not instantiated, you cannot invoke setQueriesCatalogue method');
	}
}

,
setWorksheetState : function (successFn, failureFn, scope) {
	
	var worksheetDefinition = this.worksheetDesignerPanel.getWorksheetDefinition();
	this.addSheetAdditionalData(worksheetDefinition,this.worksheetDesignerPanel.worksheetTemplate);
	var params = {
			'worksheetdefinition':  Ext.encode(worksheetDefinition)
	};
	Ext.Ajax.request({
		url: this.services['setWorkSheetState'],
		success: successFn,
		failure: failureFn,
		scope: scope,
		params: params
	});   
}

,
refreshWorksheetPreview : function () {

	var worksheetDefinition = this.worksheetDesignerPanel.getWorksheetDefinition();
	var isEqual = false;
	if(this.previousWorksheetDefinition!= null){
		isEqual = this.compareWorksheetDefinitions(this.previousWorksheetDefinition, worksheetDefinition);
	}		
	if(!isEqual){
		this.worksheetPreviewPanel.getFrame().setSrc(this.services['getWorkSheetState']);
		this.previousWorksheetDefinition = worksheetDefinition;
	}
}

, validate : function () {
	return 	this.worksheetDesignerPanel.validate(this.getWorksheetTemplateAsString, this.worksheetDesignerPanel.showValidationErrors, this );	
}

, getWorksheetTemplateAsString : function () {

	// check validation before retrieving template
//	var errorArray = this.worksheetDesignerPanel.validate();
	//	if(errorArray && errorArray.length>0){
	//		this.worksheetDesignerPanel.showValidationErrors(errorArray);		
	//		return;
	//	}

	var queriesCatalogue = this.getQueriesCatalogue();
	var worksheetDefinition = null;
	if (this.worksheetDesignerPanel.rendered === true) {
		// get the current worksheet designer state
		worksheetDefinition = this.worksheetDesignerPanel.getWorksheetDefinition();
	} else {
		// get the initial worksheet template
		worksheetDefinition = this.worksheetDesignerPanel.worksheetTemplate;
	}
	try{
		this.addAdditionalData(worksheetDefinition);
	} catch(e) {
		//if an exception occours do not add the additional data
		Sbi.error("Error adding the additional data");
	}
	
	var template = Ext.util.JSON.encode({
		'OBJECT_WK_DEFINITION' : worksheetDefinition,
		'OBJECT_QUERY' : queriesCatalogue
	});
	return template;
}

, addAdditionalData : function(sheetTemplate){

	if(this.worksheetPreviewPanel.rendered === true){
		var frame = this.worksheetPreviewPanel.getFrame();
		var window = frame.getWindow();
		var panel = window.workSheetPanel;
		if(panel){
			var additionalData = panel.getAdditionalData();
			if(additionalData!=undefined && additionalData!=null){
				var sheets = sheetTemplate.sheets;
				for(var i=0; i<sheets.length; i++){
					if(additionalData[i]!=undefined && 	additionalData[i]!=null && 	additionalData[i].data!=undefined && 
									additionalData[i].data!=null && !Ext.isEmpty(additionalData[i].data) &&  sheets[i].content.crosstabDefinition!=undefined && 
												sheets[i].content.crosstabDefinition!=null){
						if (additionalData[i].data.crosstabDefinition) {
							var crosstabDefinition = additionalData[i].data.crosstabDefinition;
							if (crosstabDefinition.calculatedFields) {
								sheets[i].content.crosstabDefinition.calculatedFields = crosstabDefinition.calculatedFields;
							}
							if (crosstabDefinition.additionalData) {
								sheets[i].content.crosstabDefinition.additionalData = crosstabDefinition.additionalData;
							}
						}
					}
				}	
			}
		}

	}else{
		this.addSheetAdditionalData(sheetTemplate,this.worksheetDesignerPanel.worksheetTemplate);
	}
}


, addSheetAdditionalData: function(designerTemplate, documentTemplate){
	if(designerTemplate!=undefined && designerTemplate!=null && documentTemplate!=undefined && documentTemplate!=null){
		var designerTemplateSheets = designerTemplate.sheets;
		var documentTemplateSheets = documentTemplate.sheets;
		if(documentTemplateSheets!=undefined && documentTemplateSheets!=null && designerTemplateSheets!=undefined && designerTemplateSheets!=null){
			for(var i=0; i<designerTemplateSheets.length; i++){
				for(var y=0; y<documentTemplateSheets.length; y++){
					if(designerTemplateSheets[i].name == documentTemplateSheets[y].name){
						if(designerTemplateSheets[i].content.crosstabDefinition!=undefined && designerTemplateSheets[i].content.crosstabDefinition!=null && documentTemplateSheets[y].content.crosstabDefinition!=undefined && documentTemplateSheets[y].content.crosstabDefinition!=null){
							designerTemplateSheets[i].content.crosstabDefinition.calculatedFields =documentTemplateSheets[y].content.crosstabDefinition.calculatedFields;
							designerTemplateSheets[i].content.crosstabDefinition.additionalData =documentTemplateSheets[y].content.crosstabDefinition.additionalData;
						}
						break;
					}
				}	
			}	
		}
	}
}

, exportContent: function(mimeType){

	if(this.isWorksheetPageActive()){
		if( this.worksheetPreviewPanel!=undefined && this.worksheetPreviewPanel!=null && 
				this.worksheetPreviewPanel.getFrame()!=undefined && this.worksheetPreviewPanel.getFrame()!=null &&
				this.worksheetPreviewPanel.getFrame().getWindow()!=undefined && this.worksheetPreviewPanel.getFrame().getWindow()!=null &&
				this.worksheetPreviewPanel.getFrame().getWindow().workSheetPanel!=undefined && this.worksheetPreviewPanel.getFrame().getWindow().workSheetPanel!=null){
					this.worksheetPreviewPanel.getFrame().getWindow().workSheetPanel.un('contentexported', this.sendMessageToParentFrame,this);	
					this.worksheetPreviewPanel.getFrame().getWindow().workSheetPanel.on('contentexported', this.sendMessageToParentFrame,this);	
					this.worksheetPreviewPanel.getFrame().getWindow().workSheetPanel.exportContent(mimeType, true);
					
			}
	}else{
		sendMessage({}, 'worksheetexporttaberror');
	}
}

, sendMessageToParentFrame: function(){
	sendMessage({}, 'contentexported');
}

, isWorksheetPageActive: function(){
	return this.tabs.getActiveTab().id=='WorkSheetPreviewPage';
}
,  getWorksheetDesignerPanel: function(){
	return this.worksheetDesignerPanel;
}
,  getWorksheetPreviewPanel: function(){
	return this.worksheetPreviewPanel;
}
, compareWorksheetDefinitions: function(w1, w2){
	var result=true;
	var cont1 = 0;
	var cont2 = 0;
	for(var k in w1) {
		cont1++; } 
	for(var f in w2){ 
		cont2++; }
	if(cont1 != cont2){
		result = false;
	}		
	if(result == true){
		for(var p in w1){
			if(result != false){
			if(w1[p]){
				if(typeof(w1[p])=='object'){
					if(w2[p] != undefined){
						if(w1[p] == null && w2[p] == null){
							result = true;
							break;
						}	
						else{
							result = this.compareWorksheetDefinitions(w1[p], w2[p]);
						}
					}
					else {
						result = false;
						break;
					}
				}
				else if(typeof(w1[p])=='function'){
					result = true;					
					break;
				}
				else{
					if(w2[p] != undefined){
						if(w1[p]!=w2[p]){
							result = false;
							break;
						}
					}
					else{
						result = false;
						break;
					}
				}
			} else {
				if (w2[p]){
					result = false;
				break;
				}
			}
		}
	}
	}
	return result;
}

	,
	resultPanelActivateHandler: function() {
		if (this.queryEditorPanel != null) {
			// case of power user and normal execution
			this.queryEditorPanel.applyChanges();
			this.queryEditorPanel.queryCataloguePanel.commit(function() {
				var query = this.queryEditorPanel.queryCataloguePanel.getSelectedQuery()
				this.checkPromptableFilters(query);
			}, this);
		} else {
			// case of non-power user or cross navigation execution
			this.loadFirstQuery();
		}
	}

});