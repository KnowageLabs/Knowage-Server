/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit");

/**
 * @class Sbi.cockpit.MainPanel
 * @extends Ext.Panel
 *
 * The main panel of SpagoBI's cockpit engine.
 */

/**
 * @cfg {Object} config The configuration object passed to the constructor
 */
Sbi.cockpit.MainPanel = function(config) {

	this.validateConfigObject(config);
	this.adjustConfigObject(config);


	// init properties...
	var defaultSettings = {
			hideBorders: true
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.core', defaultSettings);
	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	this.initServices();
	this.init();
			
	//In visualization mode the .css class is different: without the background image
	//if(Sbi.config.docAuthor != '' && Sbi.user.userId != Sbi.config.docAuthor)
	//---> now we check the environment, not the doc author for visualization mode
	if(Sbi.config.environment === 'DOCBROWSER' && this.isViewDocumentMode())
	{
		c = Ext.apply(c, {
			id: "mainPanel",
			bodyCls : "mainPanelVisualizationMode",
	        items    : [this.widgetContainer]
		});
	}
	else
	{
		//Current user is the author of doc..we are not in visualization mode
		c = Ext.apply(c, {
			id: "mainPanel",
			bodyCls : "mainPanel",
	        items    : [this.widgetContainer]
		});
	}

	// constructor
	Sbi.cockpit.MainPanel.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.cockpit.MainPanel, Ext.Panel, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
    services: null

    /**
     * @property {Sbi.cockpit.core.WidgetContainer} widgetContainer
     * The container that manage the layout off all the widget contained in this cockpit
     */
    , widgetContainer: null

    /**
     * @property {Object} lastSavedAnalysisState
     * The last saved analysis state. Could be useful to check if the cockpit has been modified
     * and if necessary revert to last saved state. It could be null if the cockpit has not been
     * previously saved (i.e. cockpit creation)
     */
    , lastSavedAnalysisState: null

    /**
	 * @property {Ext.Window} associationEditorWizard
	 * The wizard that manages the associations definition
	 */
	, associationEditorWizard: null
	
    /**
	 * @property {Ext.Window} fontEditorWizard
	 * The wizard that manages fonts definition
	 */
	, fontEditorWizard: null
	
    /**
	 * @property {Ext.Window} layoutEditorWizard
	 * The wizard that manages layout definition
	 */
	, layoutEditorWizard: null

	/**
	 * @property {Ext.Window} filterEditorWizard
	 * The wizard that manages the filters definition
	 */
	, filterEditorWizard: null

	/**
	 * @property {Ext.Window} associationsWindow
	 * The window that shows the selections defined
	 */
	, selectionsWindow: null
	
	/**
	 * @property {Ext.Window} viewSelectionsWindow
	 * The simple window (not a widget) that shows the selections defined, in doc browser mode
	 */
	, viewSelectionsWindow: null

    , msgPanel: null

    // TODO remove from global
    , saved: null


    // =================================================================================================================
	// METHODS
	// =================================================================================================================

	/**
	 * @method
	 *
	 * Controls that the configuration object passed in to the class constructor contains all the compulsory properties.
	 * If it is not the case an exception is thrown. Use it when there are properties necessary for the object
	 * construction for whom is not possible to find out a valid default value.
	 *
	 * @param {Object} the configuration object passed in to the class constructor
	 *
	 * @return {Object} the config object received as input
	 */
	, validateConfigObject: function(config) {

	}

	/**
	 * @method
	 *
	 * Modify the configuration object passed in to the class constructor adding/removing properties. Use it for example to
	 * rename a property or to filter out not necessary properties.
	 *
	 * @param {Object} the configuration object passed in to the class constructor
	 *
	 * @return {Object} the modified version config object received as input
	 *
	 */
	, adjustConfigObject: function(config) {
		config = config || {};
		if(Sbi.isValorized(config.analysisState)) {
			config.lastSavedAnalysisState = config.analysisState;
			delete config.analysisState;
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method
	 * @deprecated
	 *
	 * Returns the analysis state of this engine encoded as string. This method is usually called
	 * by parent container to generate the template to store in SpagoBI database when the document is saved
	 * by the user.
	 *
	 * Replaced by #validateAnalysisState
	 */
	, validate: function (successHandler, failureHandler, scope) {
		Sbi.trace("[MainPanel.validate]: IN");

		var templeteStr = this.getTemplate();
		Sbi.trace("[MainPanel.validate]: template = " + templeteStr);

		Sbi.trace("[MainPanel.validate]: OUT");
		return templeteStr;
	}

	/**
	 * @method
	 * Returns the cockpit current template that is equal to the current analysisState
	 * encoded as string
	 *
	 * @return {String} The current template
	 */
	, getTemplate: function() {
		Sbi.trace("[MainPanel.getTemplate]: IN");
		var template = this.getAnalysisState();
		var templeteStr = Ext.JSON.encode(template);
		Sbi.trace("[MainPanel.getTemplate]: OUT");
		return templeteStr;
	}

	/**
	 * @method
	 * Convert the template received as argument into a JSON object and the use it to set the current
	 * analysis state of the cockpit.
	 *
	 * @param {String} template The template
	 */
	, setTemplate: function(template) {
		Sbi.trace("[MainPanel.setTemplate]: IN");
		if(Ext.isString(template)) {
			var analysisState = Ext.JSON.decode(template);
			this.setAnalysisState(analysisState);
		} else {
			Sbi.trace("[MainPanel.setTemplate]: Input parameter [template] is not of type [string]");
		}
		Sbi.trace("[MainPanel.setTemplate]: OUT");
	}

	/**
	 * @method
	 *
	 * Returns weather the current analysis state is valid or not. Some engine during editing phase can
	 * allow inconsistent states. This method is usually called to deciede if the document can be saved or
	 * not.
	 */
	, isValidAnalysisState: function() {
		// in cockpit engine all possible editing states are valid
		return true;
	}

	, validateAnalysisState: function(successHandler, failureHandler, scope) {
		var returnState = true;
		var analysisState =  this.getAnalysisState();

		successHandler = successHandler || function(){return true;};
		failureHandler = failureHandler || function(){return true;};

		if(this.isValidAnalysisState()) {
			if( successHandler.call(scope || this, analysisState) === false) {
				returnState = false;
			}
		} else { // impossible to go into this branch because the cockpit is allways valid :)
			// get the list of validation error messages
			var validationErrors = [];
			validationErrors.push("Error 1 caused by problem A");
			validationErrors.push("Error 2 caused by problem B");
			if( failureHandler.call(scope || this, analysisState, validationErrors) === false) {
				returnState = false;
			}
		}

		if(returnState) {
			return analysisState;
		} else {
			return null;
		}
	}

	/**
	 * @method
	 *
	 * Returns the current analysis state. For the cockpit engine it is equal to #widgetContainer configuration
	 * and Sbi.storeManager configuration
	 *
	 * @return {Object} The analysis state.
	 */
	, getAnalysisState: function () {
		Sbi.trace("[MainPanel.getAnalysisState]: IN");
		var analysisState = {};

		analysisState.widgetsConf = this.widgetContainer.getConfiguration();
		analysisState.storesConf = Sbi.storeManager.getConfiguration();

		Sbi.trace("[MainPanel.getAnalysisState]: OUT");
		return analysisState;
	}

	, resetAnalysisState: function() {
		this.widgetContainer.resetConfiguration();
		Sbi.storeManager.resetConfiguration();
		//Sbi.storeManager.resetAssociations();
	}

	/**
	 * @method
	 */
	, setAnalysisState: function(analysisState) {
		Sbi.trace("[MainPanel.setAnalysisState]: IN");
		Sbi.storeManager.setConfiguration(analysisState.storesConf);
		this.widgetContainer.setConfiguration(analysisState.widgetsConf);
		Sbi.trace("[MainPanel.setAnalysisState]: OUT");
	}

	, isDocumentSaved: function() {
		if(Sbi.isNotValorized(this.documentSaved)) {
			this.documentSaved = !Ext.isEmpty(Sbi.config.docLabel);
		}

		return this.documentSaved ;
	}

	, isDocumentNotSaved: function() {
		return !this.isDocumentSaved();
	}

	, closeDocument : function() {
		Sbi.trace("[MainPanel.closeDocument]: IN");
		
		
		var url = Sbi.config.contextName + '/servlet/AdapterHTTP?ACTION_NAME=CREATE_DOCUMENT_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE';

		Sbi.trace("[MainPanel.closeDocument]: go back to [" + Sbi.config.environment + "]");

		if (Sbi.config.environment == "MYANALYSIS") {
			sendMessage({newUrl:url},'closeDocument');
		} else if (Sbi.config.environment == "DOCBROWSER") {
			if (typeof sendMessage == 'function'){
				sendMessage({},'closeDocument');
			}else{
				url = Sbi.config.contextName + '/servlet/AdapterHTTP?ACTION_NAME=DOCUMENT_USER_BROWSER_START_ACTION';
				window.location = url;
			}
		} else {
			window.location = url;
		}

		Sbi.trace("[MainPanel.closeDocument]: IN");
	}

	, showSaveDocumentWin: function() {
		this.showSaveDocumentWindow(false);
	}

	, showSaveDocumentAsWin: function() {
		this.showSaveDocumentWindow(true);
	}

	, showSaveDocumentWindow: function(insert){
		Sbi.trace("[MainPanel.showSaveDocumentWindow]: IN");
		if(this.saveWindow != null){
			this.saveWindow.close();
			this.saveWindow.destroy();
		}

		var template = this.getTemplate();
		Sbi.trace("[MainPanel.showSaveDocumentWindow]: template is equal to [" + template + "]");

		var documentWindowsParams = {
			'OBJECT_TYPE': 'DOCUMENT_COMPOSITE',
			'OBJECT_TEMPLATE': template,
			'typeid': 'COCKPIT'
		};

		var formState = {};
		formState.visibility = true; //default for insertion
		formState.OBJECT_FUNCTIONALITIES  = Sbi.config.docFunctionalities;

		if (insert === true) {
			formState.docLabel = 'cockpit__' + Math.floor((Math.random()*1000000000)+1);
			documentWindowsParams.MESSAGE_DET= 'DOC_SAVE';
			Sbi.trace("[MainPanel.showSaveDocumentWindow]: Document [" + formState.docLabel + "] will be created");
		} else {
			formState.docLabel = Sbi.config.docLabel;
			formState.docName = Sbi.config.docName;
			formState.docDescr = Sbi.config.docDescription;
			formState.visibility = Sbi.config.docIsVisible;
			formState.isPublic = Sbi.config.docIsPublic;
			documentWindowsParams.MESSAGE_DET= 'MODIFY_COCKPIT';
			Sbi.trace("[MainPanel.showSaveDocumentWindow]: Document [" + formState.docLabel + "] will be updated");
		}
		documentWindowsParams.formState = formState;
		documentWindowsParams.isInsert = insert;
		documentWindowsParams.fromMyAnalysis = Sbi.config.fromMyAnalysis;

		this.saveWindow = new Sbi.widgets.SaveDocumentWindow(documentWindowsParams);

		this.saveWindow.on('savedocument', this.onSaveDocument, this);
		//this.saveWindow.on('closeDocument', this.returnToMyAnalysis, this);

		this.saveWindow.show();

		Sbi.trace("[MainPanel.showSaveDocumentWindow]: OUT");
	}

	//-----------------------------------------------------------------------------------------------------------------
	// utility methods
	// -----------------------------------------------------------------------------------------------------------------

	, onAddWidget: function() {
		// add an empty widget in the default region of the container
		var addedComponent = this.widgetContainer.addWidgetContainerComponent();
		//TODO check if this call needs to fire an event
		this.widgetContainer.showWidgetEditorWizard(addedComponent);
	}

	, onClearSelections: function() {
		var widgetManager = this.widgetContainer.getWidgetManager();
		widgetManager.clearSelections();
	}

	, onShowSelectionsWindow: function(){
		var config = {};
		config.widgetManager = this.widgetContainer.getWidgetManager();
		config.selections = this.widgetContainer.getWidgetManager().getSelections() || [];
		Sbi.trace("[MainPanel.onShowSelectionsWindow]: config.selections is equal to [" + Sbi.toSource(config.selections) + "]");
		Sbi.trace("[MainPanel.onShowSelectionsWindow]: instatiating the window");

		var selectionWidget = new Sbi.cockpit.widgets.selection.SelectionWidget();
//		config = {};
//		config.wtype = "selection";
//		config.wconf = {
//				wtype : "selection"
//		};
//		config.wlayout = null;

		this.widgetContainer.addSelectionWidget(selectionWidget, config.wlayout);
	}
	
	, onShowSelectionsView: function(){
		
		Sbi.trace("[MainPanel.onShowSelectionsView]: START");
		
		var thePanel = this;
		
		if(this.viewSelectionsWindow === undefined || this.viewSelectionsWindow === null) {
		
			var config = {};
			var selectionFields = ['association', 'values'];
			var selectionData = [];
			
			var selections = this.widgetContainer.getWidgetManager().getSelections() || [];
			//Sbi.trace("[MainPanel.onShowSelectionsView]: config.selections is equal to [" + Sbi.toSource(selections) + "]");
			//Sbi.trace("[MainPanel.onShowSelectionsView]: instatiating the popup");
			
			for(var widgetId in selections)  {
				
	    		var selectionsOnWidget = selections[widgetId];
	    		//Sbi.trace("[MainPanel.onShowSelectionsView]: selections on widget: [" + Sbi.toSource(selectionsOnWidget) + "]");
	    		
	    		for(var fieldHeader in selectionsOnWidget) {
	    			
	    			if(selectionsOnWidget[fieldHeader].values && selectionsOnWidget[fieldHeader].values.length > 0) {
	    				
	    				selectionDataEl = [fieldHeader, selectionsOnWidget[fieldHeader].values];
	    				//Sbi.trace("[MainPanel.onShowSelectionsView]: selectionDataEl: [" + Sbi.toSource(selectionDataEl) + "]");
	    				selectionData.push(selectionDataEl);
	    			}
	    		}
			}
			
			//Sbi.trace("[MainPanel.onShowSelectionsView]: selectionData: [" + Sbi.toSource(selectionData) + "]");
	
			var selectionsStore = new Ext.data.ArrayStore({
				fields : selectionFields
				, data : selectionData
			});
	
			this.viewSelectionsWindow = Ext.create('Ext.window.Window', {
			    title: LN('sbi.cockpit.mainpanel.btn.viewselections'),
			    height: 300,
			    width: 650,
			    layout: {
			        type: 'vbox',
			        align : 'stretch'
			    },
			    closeAction: 'destroy',
			    closable: false,
			    items: [{ 
			        xtype: 'grid',
			        border: false,
			        flex: 1,
			        columns: [{header: LN('sbi.cockpit.mainpanel.btn.associations'), dataIndex: 'association', flex:1},
			                  {header: LN('sbi.cockpit.core.selections.list.columnValues'), dataIndex: 'values', flex:1}],
			        store: selectionsStore
			    },
			    {
		            xtype: 'button',
		            text : LN('sbi.ds.wizard.close'),
		            flex: 0.1,
		            handler: function(){
		            	thePanel.viewSelectionsWindow.destroy();
		            	thePanel.viewSelectionsWindow = null;
		            }
			    }]
			}).show();
			
		} else {
			this.viewSelectionsWindow.destroy();
			//this.viewSelectionsWindow.destroy();
			this.viewSelectionsWindow = null;
		}
		
		
		Sbi.trace("[MainPanel.onShowSelectionsView]: END");
	}

	, onSelectionsWindowCancel: function(wizard) {
		Sbi.trace("[MainPanel.onSelectionsWindowCancel]: IN");
		this.selectionsWindow.close();
		this.selectionsWindow.destroy();
		Sbi.trace("[MainPanel.onSelectionsWindowCancel]: OUT");
	}

	, onSelectionsWindowCancelSingle: function() {
		Sbi.trace("[MainPanel.onSelectionsWindowCancel]: IN");
		alert("MainPanel");
		Sbi.trace("[MainPanel.onSelectionsWindowCancel]: OUT");
	}


	, onCleanCache: function(){
		Sbi.storeManager.cleanCache();
	}

	, onShowAssociationEditorWizard: function(){
		if (Sbi.storeManager.getStoreIds().length == 0){
			alert('Per gestire le associazioni è necessario creare prima dei widget!');
			return;
		}
		var config = {};
		config.stores = Sbi.storeManager.getStoreIds();
		Sbi.trace("[MainPanel.onShowAssociationEditorWizard]: config.stores is equal to [" + Sbi.toSource(config.stores) + "]");

		config.associations = Sbi.storeManager.getAssociations();
		Sbi.trace("[MainPanel.onShowAssociationEditorWizard]: config.associations is equal to [" + Sbi.toSource(config.associations) + "]");

   		Sbi.trace("[MainPanel.onShowAssociationEditorWizard]: instatiating the editor");
   		this.associationEditorWizard = Ext.create('Sbi.data.AssociationEditorWizard', config);
   		this.associationEditorWizard.on("submit", this.onAssociationEditorWizardSubmit, this);
   		this.associationEditorWizard.on("cancel", this.onAssociationEditorWizardCancel, this);
    	Sbi.trace("[MainPanel.onShowAssociationEditorWizard]: editor succesfully instantiated");

		this.associationEditorWizard.show();
	}

	, onAssociationEditorWizardCancel: function(wizard) {
		Sbi.trace("[MainPanel.onAssociationEditorWizardCancel]: IN");
		this.associationEditorWizard.close();
		this.associationEditorWizard.destroy();
		Sbi.trace("[MainPanel.onAssociationEditorWizardCancel]: OUT");
	}

	, onAssociationEditorWizardSubmit: function(wizard) {
		Sbi.trace("[MainPanel.onAssociationEditorWizardSubmit]: IN");
		var wizardState = wizard.getWizardState();
		if (Sbi.isValorized(wizardState.associations)){

			if(wizardState.associations.length == 0){
				Sbi.storeManager.setAssociationConfigurations(wizardState.associations);
				this.associationEditorWizard.close();
				this.associationEditorWizard.destroy();
				Sbi.trace("[MainPanel.onAssociationEditorWizardSubmit]: setted relation group [" + Sbi.toSource(wizardState.associations) + "] succesfully added to store manager");

			}
			else{
				// check association is valid
				var jsonObj = Ext.JSON.encode(wizardState.associations);
				// clean id from character not valid for rest service
				jsonObj = jsonObj.replace(/#/g,'');
				var params = {association: jsonObj};

				Ext.Ajax.request({
					url: Sbi.config.serviceReg.getServiceUrl('checkAssociation', {
						pathParams: params
					}),
					method: 'POST',
					params: {
						requestParam: 'notInRequestBody'
					},
					success : function(result){

						var JSONResult = Ext.JSON.decode(result.responseText);
						// if not valid ask user if wants to
						if(JSONResult.valid == 'false' || JSONResult.valid == false){

							var associationEditorWizard = this.associationEditorWizard;

							Ext.Msg.show({
								   title: LN('sbi.data.editor.association.AssociationEditor.warning'),
								   msg: LN('sbi.data.editor.association.AssociationEditor.notValidAssociation'),
								   buttons: Ext.Msg.YESNO,
								   icon: Ext.MessageBox.QUESTION,
								   modal: true,
								   fn: function(btn) {
										if(btn === 'yes') {
											Sbi.trace("[MainPanel.checkAssociation]: onAssociationEditorWizardSubmit");
											Sbi.storeManager.setAssociationConfigurations(wizardState.associations);
											associationEditorWizard.close();
											associationEditorWizard.destroy();
											Sbi.trace("[MainPanel.onAssociationEditorWizardSubmit]: setted relation group [" + Sbi.toSource(wizardState.associations) + "] succesfully added to store manager");
											}
								   		}


						 });
						}
						else{		// valid case
							Sbi.trace("[MainPanel.checkAssociation]: onAssociationEditorWizardSubmit");
							Sbi.storeManager.setAssociationConfigurations(wizardState.associations);
							this.associationEditorWizard.close();
							this.associationEditorWizard.destroy();
							Sbi.trace("[MainPanel.onAssociationEditorWizardSubmit]: setted relation group [" + Sbi.toSource(wizardState.associations) + "] succesfully added to store manager");
						}

					},
					failure: function(){
						Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.data.editor.association.AssociationEditor.errorCheckingAssociation'), 'Service Error');
					},
					scope: this
				});


			}
		}
		Sbi.trace("[MainPanel.onAssociationEditorWizardSubmit]: OUT");
	}
	
	//FONT SECTION - START
	
	, onShowFontEditorWizard: function(){
		var config = {};
//		config.storesList = Sbi.storeManager.getStoreIds();
//		Sbi.trace("[MainPanel.onShowFontEditorWizard]: config.stores is equal to [" + Sbi.toSource(config.stores) + "]");
		config.fonts = Sbi.storeManager.getFonts();
		Sbi.trace("[MainPanel.onShowFontsEditorWizard]: config.fonts is equal to [" + Sbi.toSource(config.fonts) + "]");
   		Sbi.trace("[MainPanel.onShowFontEditorWizard]: instatiating the editor");
   		this.fontEditorWizard = Ext.create('Sbi.fonts.FontEditorWizard', config);
   		this.fontEditorWizard.on("submit", this.onFontEditorWizardSubmit, this);
   		this.fontEditorWizard.on("cancel", this.onFontEditorWizardCancel, this);
    	Sbi.trace("[MainPanel.onShowFontEditorWizard]: editor succesfully instantiated");

		this.fontEditorWizard.show();
	}
	
	, onFontEditorWizardCancel: function(wizard) {
		Sbi.trace("[MainPanel.onFontEditorWizardCancel]: IN");
		this.fontEditorWizard.close();
		this.fontEditorWizard.destroy();
		Sbi.trace("[MainPanel.onFontEditorWizardCancel]: OUT");
	}

	, onFontEditorWizardSubmit: function(wizard) {
		Sbi.trace("[MainPanel.onFontEditorWizardSubmit]: IN");
		var wizardState = wizard.getWizardState();
		if (Sbi.isValorized(wizardState.fonts)){
			Sbi.storeManager.setFontConfigurations(wizardState.fonts);
			Sbi.trace("[MainPanel.onFontEditorWizardSubmit]: setted font group [" + Sbi.toSource(wizardState.fonts) + "] succesfully added to store manager");
		}
		this.fontEditorWizard.close();
		this.fontEditorWizard.destroy();
		Sbi.trace("[MainPanel.onFontEditorWizardSubmit]: OUT");
	}
	
	//FONT SECTION - END
	
	//LAYOUT SECTION - START
	
	, onShowLayoutEditorWizard: function(){
		var config = {};
		config.layouts = Sbi.storeManager.getLayouts();
		Sbi.trace("[MainPanel.onShowLayoutEditorWizard]: config.fonts is equal to [" + Sbi.toSource(config.layouts) + "]");
   		Sbi.trace("[MainPanel.onShowLayoutEditorWizard]: instatiating the editor");
   		this.layoutEditorWizard = Ext.create('Sbi.layouts.LayoutEditorWizard', config);
   		this.layoutEditorWizard.on("submit", this.onLayoutEditorWizardSubmit, this);
   		this.layoutEditorWizard.on("cancel", this.onLayoutEditorWizardCancel, this);
    	Sbi.trace("[MainPanel.onShowLayoutEditorWizard]: editor succesfully instantiated");

		this.layoutEditorWizard.show();
	}
	
	, onLayoutEditorWizardCancel: function(wizard) {
		Sbi.trace("[MainPanel.onLayoutEditorWizardCancel]: IN");
		this.layoutEditorWizard.close();
		this.layoutEditorWizard.destroy();
		Sbi.trace("[MainPanel.onLayoutEditorWizardCancel]: OUT");
	}

	, onLayoutEditorWizardSubmit: function(wizard) {
		Sbi.trace("[MainPanel.onLayoutEditorWizardSubmit]: IN");
		var wizardState = wizard.getWizardState();
		if (Sbi.isValorized(wizardState.layouts)){
			Sbi.storeManager.setLayoutConfigurations(wizardState.layouts);
			Sbi.trace("[MainPanel.onLayoutEditorWizardSubmit]: setted font group [" + Sbi.toSource(wizardState.layouts) + "] succesfully added to store manager");
		}
		this.layoutEditorWizard.close();
		this.layoutEditorWizard.destroy();
		Sbi.trace("[MainPanel.onLayoutEditorWizardSubmit]: OUT");
	}
	
	//LAYOUT SECTION - END

	, onShowFilterEditorWizard: function(){
		var config = {};
		config.filters = Sbi.storeManager.getParameters();
		Sbi.trace("[MainPanel.onShowFilterEditorWizard]: config.filters is equal to [" + Sbi.toSource(config.filters) + "]");
		Sbi.trace("[MainPanel.showFilterEditorWizard]: instatiating the editor");
		this.filterEditorWizard = Ext.create('Sbi.filters.FilterEditorWizard',config);
		this.filterEditorWizard.on("submit", this.onFilterEditorWizardSubmit, this);
		this.filterEditorWizard.on("cancel", this.onFilterEditorWizardCancel, this);
//    	this.filterEditorWizard.on("apply", this.onFilterEditorWizardApply, this);
    	Sbi.trace("[MainPanel.filterEditorWizard]: editor succesfully instantiated");

		this.filterEditorWizard.show();
	}

	, onFilterEditorWizardCancel: function(wizard) {
		Sbi.trace("[MainPanel.onFilterEditorWizardCancel]: IN");
		this.filterEditorWizard.close();
		this.filterEditorWizard.destroy();
		Sbi.trace("[MainPanel.onFilterEditorWizardCancel]: OUT");
	}

	, onFilterEditorWizardSubmit: function(wizard) {
		Sbi.trace("[MainPanel.onFilterEditorWizardSubmit]: IN");
		var wizardState = wizard.getWizardState();
		if (Sbi.isValorized(wizardState.filters)){
			Sbi.storeManager.setParameterConfigurations(wizardState.filters);
			Sbi.trace("[MainPanel.onFilterEditorWizardSubmit]: setted filter group [" + Sbi.toSource(wizardState.filters) + "] succesfully added to store manager");
		}
		this.filterEditorWizard.close();
		this.filterEditorWizard.destroy();
		Sbi.trace("[MainPanel.onFilterEditorWizardSubmit]: OUT");
	}



	, onShowSaveDocumentWindow: function() {
		this.showSaveDocumentWin();
	}

	, onShowSaveDocumentAsWindow: function() {
		this.showSaveDocumentAsWin();
	}

	, onSaveDocument: function(win, closeDocument, params) {
		Sbi.trace("[MainPanel.onSaveDocument]: IN");
		this.documentSaved = true;

		// show save button (the button that allow to perform save as)
		var itemEl = Ext.getCmp('save');
		if(itemEl && itemEl !== null) {
			itemEl.setVisible(true);
		}

		Sbi.trace("[MainPanel.onSaveDocument]: Input parameter [closeDocument] is equal to [" + closeDocument + "]");
		if(closeDocument === true) {
			this.closeDocument();
		}
		Sbi.trace("[MainPanel.onSaveDocument]: OUT");
	}

	, onDebug: function() {
		this.debug();
	}
	
	, isViewDocumentMode: function() {
		return (Sbi.config.documentMode === 'VIEW'); 
	}

	//-----------------------------------------------------------------------------------------------------------------
	// init methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method
	 *
	 * Initialize the following services exploited by this component.
	 *
	 */
	, initServices: function() {
		this.services = this.services || new Array();
	}


	/**
	 * @method
	 *
	 * Initialize the GUI
	 */
	, init: function() {
		this.initToolbar();
		this.initWidgetContainer();
	}

	, initToolbar: function() {

		var tbItems = ['->'];

		//Now DocBrowser is the visualization mode. Hidden condition modified
		
		tbItems.push(  new Ext.Button({
			id: 'add'
     		, iconCls: 'icon_add_widget'
			, tooltip: LN('sbi.cockpit.mainpanel.btn.addWidget')
			, scope: this
			, handler:  this.onAddWidget
			//, hidden: Sbi.config.docAuthor != '' && Sbi.user.userId != Sbi.config.docAuthor
			, hidden: Sbi.config.environment === 'DOCBROWSER' && this.isViewDocumentMode()
		 }));


		tbItems.push(  new Ext.Button({
			id: 'cleanCache'
     		, iconCls: 'icon_clean_cache_widget'
			, tooltip: LN('sbi.cockpit.mainpanel.btn.cleanCacheWidget')
			, scope: this
			, handler:  this.onCleanCache
			//, hidden: Sbi.config.docAuthor != '' && Sbi.user.userId != Sbi.config.docAuthor
//			, hidden: Sbi.config.environment === 'DOCBROWSER' && this.isViewDocumentMode()
		 }));

		tbItems.push(  new Ext.Button({
			id: 'associatation'
     		, iconCls: 'icon_associations'
			, tooltip: LN('sbi.cockpit.mainpanel.btn.associations')
			, scope: this
			, handler:  this.onShowAssociationEditorWizard
			//, hidden: Sbi.config.docAuthor != '' && Sbi.user.userId != Sbi.config.docAuthor
			, hidden: Sbi.config.environment === 'DOCBROWSER' && this.isViewDocumentMode()
		 }));


		tbItems.push(  new Ext.Button({
			id: 'addSelection'
     		, iconCls: 'icon_add_selection'
			, tooltip: LN('sbi.cockpit.mainpanel.btn.selections')
			, scope: this
			, handler:  this.onShowSelectionsWindow
			//, hidden: Sbi.config.docAuthor != '' && Sbi.user.userId != Sbi.config.docAuthor
			, hidden: Sbi.config.environment === 'DOCBROWSER' && this.isViewDocumentMode()
		 }));
		
		tbItems.push(  new Ext.Button({
			id: 'viewSelections'
     		, iconCls: 'icon_view_selection'
			, tooltip: LN('sbi.cockpit.mainpanel.btn.viewselections')
			, scope: this
			, handler:  this.onShowSelectionsView
			, hidden: Sbi.config.environment !== 'DOCBROWSER' || Sbi.config.documentMode === 'EDIT'
		 }));

		tbItems.push(  new Ext.Button({
			id: 'delSelection'
     		, iconCls: 'icon_delete_selection'
			, tooltip: LN('sbi.cockpit.mainpanel.btn.clearselections')
			, scope: this
			, handler:  this.onClearSelections
		 }));
		
		tbItems.push(  new Ext.Button({
			id: 'fontBtn'
     		, iconCls: 'icon_font'
			, tooltip: LN('sbi.cockpit.mainpanel.btn.fonts')
			, scope: this
			, handler:  this.onShowFontEditorWizard
			, hidden: Sbi.config.environment === 'DOCBROWSER' && this.isViewDocumentMode()
		 }));
		
		tbItems.push(  new Ext.Button({
			id: 'layoutBtn'
     		, iconCls: 'icon_layout'
			, tooltip: LN('sbi.cockpit.mainpanel.btn.layouts')
			, scope: this
			, handler:  this.onShowLayoutEditorWizard
			, hidden: Sbi.config.environment === 'DOCBROWSER' && this.isViewDocumentMode()
		 }));


		if (Sbi.isValorized(Sbi.config.isTechnicalUser) && Sbi.config.isTechnicalUser == 'true'){
			tbItems.push(  new Ext.Button({
				id: 'paramters'
	     		, iconCls: 'icon_parameters'
				, tooltip: LN('sbi.cockpit.mainpanel.btn.parameters')
				, scope: this
				, handler:  this.onShowFilterEditorWizard
				, hidden: Sbi.config.environment === 'DOCBROWSER' && this.isViewDocumentMode()
			 }));
		}

		tbItems.push(	new Ext.Button({
			id: 'save'
     		, iconCls: 'icon-save'
			, tooltip: 'Save'
			, scope: this
			, handler:  this.onShowSaveDocumentWindow
			//, hidden: this.isDocumentNotSaved() || (Sbi.config.docAuthor != '' && Sbi.user.userId != Sbi.config.docAuthor)
			, hidden: this.isDocumentNotSaved() || Sbi.config.environment === 'DOCBROWSER' && this.isViewDocumentMode()
		 }));

		tbItems.push( new Ext.Button({
		 	id: 'saveAs'
	 		, iconCls: 'icon-saveas'
	 		, tooltip: 'Save As'
	 		, scope: this
	 		, handler:  this.onShowSaveDocumentAsWindow
	 		//, hidden: Sbi.config.docAuthor != '' && Sbi.user.userId != Sbi.config.docAuthor
	 		, hidden: Sbi.config.environment === 'DOCBROWSER' && this.isViewDocumentMode()
	 	}));

		/*
		tbItems.push(new Ext.Button({
		 		id: 'debug'
			 	   	   , text: 'Debug'
			 	       , scope: this
			 		   , handler:  this.onDebug
			 	 }));
		*/

		this.tbar = new Ext.Toolbar({
		    items: tbItems,
		    height: 30
		});
	}

	, initWidgetContainer: function() {
		Sbi.trace("[MainPanel.initWidgetContainer]: IN");

		var conf = {};
		if(Sbi.isValorized(this.lastSavedAnalysisState)) {
			conf = this.lastSavedAnalysisState.widgetsConf;
		}
		this.widgetContainer = new Sbi.cockpit.core.WidgetContainer(conf);

		Sbi.trace("[MainPanel.initWidgetContainer]: widget panel succesfully created");

		Sbi.trace("[MainPanel.initWidgetContainer]: OUT");
	}


	//-----------------------------------------------------------------------------------------------------------------
	// test methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method
	 * @private
	 *
	 */
	, debug: function() {
		Sbi.trace("[MainPanel.debug]: IN");

		// to be sure to have the conf pretty printed also on old browser that dont support
        // JSON object natively it is possible to include json2.jd by Douglas Crockford (
        // https://github.com/douglascrockford/JSON-js)
        var confStr = (typeof JSON === 'object')
        				? JSON.stringify(this.getAnalysisState(), null, 2)
        				: Ext.JSON.encode(this.getAnalysisState());



        var win = new Ext.Window({
        		layout:'fit',
                width:500,
                height:300,
                //closeAction:'hide',
                plain: true,
                title: "Cockpit configuration",
                items: new Ext.form.TextArea({
                	border: false
                	, value: confStr
                    , name: 'configuration'
                }),

                buttons: [
                {
                	text: 'Close',
                    handler: function(){
                    	win.close();
                    }
                }]
        });
        win.show();


//		testFunctions = [];
//		for(p in this) {
//			if( p.indexOf("Test", p.length - "Test".length) !== -1 ) {
//				if(Ext.isFunction(this[p]))
//				testFunctions.push(p);
//			}
//		}
//
//		for(var i = 0; i < testFunctions.length; i++) {
//			this.setUp();
//			try {
//				this[testFunctions[i]]();
//				alert("Test [" + testFunctions[i] + "] succesfully executed");
//			} catch(e) {
//				alert("Test [" + testFunctions[i] + "] not passed: " + e);
//			}
//
//			this.tearDown();
//		}

		Sbi.trace("[MainPanel.debug]: OUT");
	}


	, setUp: function() {
		Sbi.trace("[MainPanel.setUp]: IN");
		var template = '{"widgetsConf":{"widgets":[{"storeId":"ds__462040106","wtype":"table","wconf":{"wtype":"table","visibleselectfields":[{"id":"Comune","alias":"Comune","funct":"NONE","iconCls":"attribute","nature":"attribute","values":"[]","precision":"","options":{}},{"id":"numero","alias":"numero","funct":"NONE","iconCls":"measure","nature":"measure","values":"[]","precision":"2","options":{}}]},"wstyle":{},"wlayout":{"region":{"width":"0.20","height":"0.86","x":"0.01","y":"0.06"}}},{"storeId":"ds__4705859","wtype":"table","wconf":{"wtype":"table","visibleselectfields":[{"id":"ABITANTI","alias":"ABITANTI","funct":"NONE","iconCls":"measure","nature":"measure","values":"[]","precision":"2","options":{}},{"id":"GG","alias":"GG","funct":"NONE","iconCls":"measure","nature":"measure","values":"[]","precision":"2","options":{}}]},"wstyle":{},"wlayout":{"region":{"width":"0.18","height":"0.85","x":"0.22","y":"0.06"}}},{"storeId":"ds__745200072","wtype":"table","wconf":{"wtype":"table","visibleselectfields":[{"id":"Comune","alias":"Comune","funct":"NaN","iconCls":"attribute","nature":"attribute","values":"[]"},{"id":"Femmine corsi a tempo pieno","alias":"Femmine corsi a tempo pieno","funct":"NaN","iconCls":"measure","nature":"measure","values":"[]"},{"id":"Femmine corsi per apprendisti","alias":"Femmine corsi per apprendisti","funct":"NaN","iconCls":"measure","nature":"measure","values":"[]"},{"id":"Femmine Totale","alias":"Femmine Totale","funct":"NaN","iconCls":"measure","nature":"measure","values":"[]"}]},"wstyle":{},"wlayout":{"region":{"width":"0.32","height":"0.43","x":"0.42","y":"0.21"}}}]},"storesConf":{"stores":[{"storeId":"ds__462040106"},{"storeId":"ds__4705859"},{"storeId":"ds__745200072"}],"associations":[{"id":"#0","description":"ds__4705859.BIRRA_SFRU=ds__745200072.Totale corsi a tempo pieno","fields":[{"store":"ds__4705859","column":"BIRRA_SFRU"},{"store":"ds__745200072","column":"Totale corsi a tempo pieno"}]}]},"associationsConf":[{"id":"#0","description":"ds__4705859.BIRRA_SFRU=ds__745200072.Totale corsi a tempo pieno","fields":[{"store":"ds__4705859","column":"BIRRA_SFRU"},{"store":"ds__745200072","column":"Totale corsi a tempo pieno"}]}]}';
		this.setTemplate(template);
		Sbi.trace("[MainPanel.setUp]: OUT");
	}

	, tearDown: function() {
		Sbi.trace("[MainPanel.tearDown]: IN");
		this.resetAnalysisState();
		Sbi.trace("[MainPanel.tearDown]: OUT");
	}


	/**
	 * @method
	 * @private
	 *
	 * Test setTemplate method of MainPanel class
	 */
	, initTest: function() {
		this.assertEqual(this.widgetContainer.getWidgetsCount(), 3, "Widgets count is wrong");
		this.assertEqual(Sbi.storeManager.getStoresCount(), 3, "Stores count is wrong");
	}

	/**
	 * @method
	 * @private
	 *
	 * Test resetAnalysisState method of MainPanel class
	 */
	, resetTest: function() {
		this.resetAnalysisState();
		this.assertEqual(this.widgetContainer.getWidgetsCount(), 0, "Widgets count is wrong");
		this.assertEqual(Sbi.storeManager.getStoresCount(), 0, "Stores count is wrong");
	}

	/**
	 * @method
	 * @private
	 *
	 * Test removeWidget method of WidgetContainer class
	 */
	, removeWidgetTest: function() {
		var widget = this.widgetContainer.getWidgetManager().getWidgets()[0];
		this.widgetContainer.removeWidget(widget);

		this.assertEqual(this.widgetContainer.getWidgetsCount(), 2, "Widgets count is wrong");
		this.assertEqual(this.widgetContainer.components.getCount(), 2, "Components count is wrong");
		this.assertEqual(Sbi.storeManager.getStoresCount(), 2, "Stores count is wrong");
	}

	// assets

	, assertEqual: function(x, y, msg) {
		if(x !== y) {
			var msg = msg? msg + ": " : "";
			msg += "expected value is  [" + x + "] while actual value is [" + y + "]";
			throw msg;
		}
	}
});