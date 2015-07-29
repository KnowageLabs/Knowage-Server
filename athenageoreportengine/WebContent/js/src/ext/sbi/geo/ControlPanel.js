/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


Ext.ns("Sbi.geo");

/**
 * @class Sbi.geo.ControlPanel
 * @extends Ext.Panel
 *
 * The control panel
 */
Sbi.geo.ControlPanel = function(config) {
	this.validateConfigObject(config);
	this.adjustConfigObject(config);

	// init properties...
	var defaultSettings = {
		id						:'controlPanel',
		region      			: 'east',
		split       			: false,
		width       			: 365,
		collapsible 			: true,
		collapsed   			: config.layerPanelConf.collapsed,
		autoScroll				: true,
		layout					: 'fit',
		margins     			: '0 0 0 0',
		cmargins    			: '0 0 0 0',
		collapseMode			: 'none',
        hideCollapseTool		: true,
		hideBorders				: true,
		border					: false,
		frame					: false,
		singleSelectionIndicator: true
	};

	if (Sbi.settings && Sbi.settings.geo && Sbi.settings.geo.controlPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.geo.controlPanel);
	}

	var c = Ext.apply(defaultSettings, config || {});
	Ext.apply(this, c);

	// init events...
	//	this.addEvents();

	this.initServices();
	this.init();

	c = Ext.apply(c, {
	     items: this.innerPanel
	});

	// constructor
    Sbi.geo.ControlPanel.superclass.constructor.call(this, c);
};


/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.geo.ControlPanel, Ext.Panel, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
	services: null

	/**
     * @property {Array} thematizationOptions
     * The configuration of the enabled thematizers
     */
	, thematizationOptions: [{
			id: 'map-zone'
			, label: LN('sbi.geo.controlpanel.map')+'<span>'+LN('sbi.geo.controlpanel.zone')+'</span>'
			, className: 'map-zone'
		}, {
			id: 'map-point'
				, label:  LN('sbi.geo.controlpanel.map') + '<span>'+ LN('sbi.geo.controlpanel.point')+'</span>'
				, className: 'map-point'
		}/*, {
			id: 'map-comparation'
			, label: 'Mappa di <span>comparazione</span>'
			, className: 'map-comparation'
		}, {
			id: 'map-heat'
			, label: 'Mappa di <span>calore</span>'
			, className: 'map-heat'
		}*/
	]

	/**
	 * @property {Array} selectedThematizationOptionId
	 * The identifier of the selected thematizer
	 */
	, selectedThematizationOptionId: 'map-zone'


	, filterFileds: null
	, filterLabels: null
	, filterValues: null
	, pendingFilters: false

	//CONSTANTS
	, DEFAULT_NAME: LN('sbi.geo.controlpanel.defaultname')
	, DEFAULT_DESCRIPTION: LN('sbi.geo.controlpanel.defaultdescr')


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
		if(config.layerPanelConf == null || config.layerPanelConf == undefined ){
			config.layerPanelConf = {collapsed:false};
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
    // accessor methods
	// -----------------------------------------------------------------------------------------------------------------


	, getAnalysisConf: function() {
		return this.thematizer.getFormState();
	}

	, setMapPrivate: function() {
		var privateEl =  Ext.get("div-private");
		var publicEl =  Ext.get("div-public");
		Ext.fly(publicEl).removeClass('checked');
		Ext.fly(privateEl).addClass('checked');
	}

	, setMapPublic: function() {
		var privateEl =  Ext.get("div-private");
		var publicEl =  Ext.get("div-public");
		Ext.fly(privateEl).removeClass('checked');
		Ext.fly(publicEl).addClass('checked');
	}

	/**
	 * @method
	 * Set a new list of indicators usable to generate the thematization
	 *
	 * @param {Array} indicators new indicators list. each element is an array of two element:
	 * the first is the indicator name while the second one is the indicator text
	 * @param {String} indicator the name of the selected indicator. Must be equal to one of the
	 * names of the indicators passed in as first parameter. It is optional. If not specified
	 * the first indicators of the list will be selected.
	 * @param {boolean} riseEvents true to rise an event in order to regenerate the thematization, false
	 * otherwise. Optional. By default false.
	 */
	, setIndicators: function(indicators, indicator, riseEvents) {
		Sbi.trace("[ControlPanel.setIndicators] : IN");

		Sbi.trace("[ControlPanel.setIndicators] : New indicators number is equal to [" + indicators.length + "]");

    	this.indicators = indicators;
    	var newStore = new Ext.data.SimpleStore({
            fields: ['value', 'text'],
            data : this.indicators
        });

    	//updating indicatorsDiv
    	this.refreshIndicatorsDiv();

        Sbi.trace("[ControlPanel.setIndicators] : OUT");
    }

	 /**
     * @method
     * Gets filters values
     *
     * @return {Array} an array of objects. Each object in the array is composed by three
     * properties: fieldHeader, fieldName and value. The field is the header of the filtered column.
     * The value is the value of the filter to apply.
     */
    , getFiltersState : function(){
    	Sbi.trace("[ControlPanel.getFiltersState] : IN");

    	var filtersState = new Array();

    	if(this.filterFields){
			for(var i=0; i<this.filterFields.length; i++){
				filtersState.push({
					fieldHeader: this.filterFields[i].fieldHeader,
					fieldName: this.filterFields[i].fieldName,
					value: this.filterFields[i].getValue()
				});
			}
		} else {
			Sbi.watn("[ControlPanel.getFiltersState] : there are no filter fields");
		}

		Sbi.trace("[ControlPanel.getFiltersState] : OUT");

		return filtersState;
    }


    /**
     * Create the filter panel
     *
     * @param {Array} filterDescriptors an array of objects. Each object is equal to the field
     * on which the filter is applied plus the property values that contais all the possible values
     * for the filter (i.e. the distinct values contained in the store on that field).
     */
	, createFiltersPanel: function(filterDescriptors){

		Sbi.trace("[ControlPanel.createFiltersPanel]: IN");

		this.resetFiltersPanel();


		if(filterDescriptors && filterDescriptors.length > 0) {
			var filtersDiv = Ext.get("filtersDiv");
			Ext.DomHelper.append(filtersDiv, "<h2>" +LN('sbi.geo.controlpanel.filters')+ "</h2>");
		}

		for(var i=0; i<filterDescriptors.length; i++){

			filterDescriptors[i].values.unshift("");

			var filterLabel = this.createFilterLabel(filterDescriptors[i]);
			this.filterLabels.push(filterLabel);
			filterLabel.render("filtersDiv");

			var filterField = this.createFilterField(filterDescriptors[i]);
			this.filterFields.push(filterField);
			filterField.render("filtersDiv");
		}

		if(this.pendingFilters === true) {
			this.applyFilters(false);
			this.pendingFilters = false;
		}


		if((Sbi.config.docLabel=="" && this.filterDescriptors && this.filterDescriptors.length>0)){
			this.setDefaultsValuesToFiltersButton =  this.createFilterButton();
		}

		this.doLayout();

		Sbi.trace("[ControlPanel.createFiltersPanel]: OUT");
	}

	/**
	 * @method
	 *
	 * reset filter div
	 */
	, resetFiltersPanel: function() {
    	Sbi.debug("[ControlPanel.resetFiltersPanel]: IN");

    	if(!this.filterFields) {
			Sbi.debug("[ControlPanel.resetFiltersPanel]: no filters to remove");
			this.filterFields = new Array();
			this.filterLabels = new Array();
		} else {
			Sbi.trace("[ControlPanel.resetFiltersPanel]: removeing div");
			var filtersDiv = Ext.get("filtersDiv").dom;
			filtersDiv.parentNode.removeChild(filtersDiv);
			Sbi.trace("[ControlPanel.resetFiltersPanel]: div removed");

			var divHtmlFragment = '<div class="filters" id="filtersDiv" ></div>';

			Sbi.trace("[ControlPanel.resetFiltersPanel]: adding div");
			var indicatorsElement = Ext.get("indicatorsDiv");
			Ext.DomHelper.insertAfter(indicatorsElement, divHtmlFragment);
			Sbi.trace("[ControlPanel.resetFiltersPanel]: div added");

			this.filterFields = new Array();
			this.filterLabels = new Array();

			this.doLayout();
		}
		Sbi.debug("[ControlPanel.resetFiltersPanel]: OUT");
    }

    , createFilterField: function(filterDescriptor) {

    	Sbi.trace("[ControlPanel.createFilterField]: IN");

    	var store = new Ext.data.SimpleStore({
        	fields: ['val'],
            data : 	filterDescriptor.values
        });

    	var filterField = new Ext.form.ComboBox  ({
            fieldLabel: filterDescriptor.header,
            name: filterDescriptor.name,
            fieldName: filterDescriptor.name,
            fieldHeader: filterDescriptor.header,
            width:  300,
            editable: false,
            mode: 'local',
            allowBlank: true,
            valueField: 'val',
            displayField: 'val',
            emptyText: 'Select a value',
            triggerAction: 'all',
            store: store,
            tpl: '<tpl for="."><div class="x-combo-list-item">{val}&nbsp;</div></tpl>'
        });

    	filterField.on("select", this.applyFilters, this);
    	filterField.on("afterrender", this.onFilterFieldRendered, this);

    	Sbi.trace("[ControlPanel.createFilterField]: OUT");

    	return filterField;
    }

    , createFilterLabel: function(filterDescriptor) {
    	var filterLabel = new Ext.form.DisplayField({
			fieldLabel : filterDescriptor.header,
			name: filterDescriptor.header,
			width:  300,
			allowBlank : false,
			readOnly:true,
			hidden: false
		});
		filterLabel.setValue(filterDescriptor.header);

		return filterLabel;
    }

    , createFilterButton: function() {
    	var setDefaultsValuesToFiltersButton =  new Ext.Button({
	    	text: LN('sbi.geo.analysispanel.filter.default'),
	        width: 30,
	        handler: function() {
	        	this.saveDefaultFiltersValue();
	        	Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.geo.analysispanel.filter.default.ok'));
       		},
       		scope: this
		});
		setDefaultsValuesToFiltersButton.render("filtersDiv");

		return setDefaultsValuesToFiltersButton;
    }

    , onFilterFieldRendered: function(combo) {
    	Sbi.debug("[ControlPanel.onFilterFieldRendered]: IN");

    	var selectedValue = null;

    	if(this.filterValues != null) {
    		Sbi.debug("[ControlPanel.onFilterFieldRendered]: there are already some selected filters");
    		for(var i = 0; i < this.filterValues.length; i++) {
    			Sbi.debug("[ControlPanel.onFilterFieldRendered]: [" + Sbi.toSource(this.filterValues[i]) + "]");
    			Sbi.debug("[ControlPanel.onFilterFieldRendered]: [" + this.filterValues[i].fieldHeader + "] == [" + combo.fieldHeader + "]");
    			if(this.filterValues[i].fieldHeader === combo.fieldHeader) {
    				selectedValue = this.filterValues[i].value;
    				break;
    			}
    		}
    	} else {
    		Sbi.debug("[ControlPanel.onFilterFieldRendered]: there are no filters selected already");
    	}

    	if(selectedValue != null) {
    		this.pendingFilters = true;
    		Sbi.debug("[ControlPanel.onFilterFieldRendered]: apply value [" + selectedValue + "] to filter [" + combo.fieldHeader + "]");
    		combo.setValue(selectedValue);
    		//this.applyFilters(false);
    	}

    	Sbi.debug("[ControlPanel.onFilterSelected]: OUT");
    }

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

    /**
     * Save the default value of the filter in the filter as filterDefaultValue
     */
    , saveDefaultFiltersValue:function(){
    	Sbi.trace("[ControlPanel.saveDefaultFiltersValue] : IN");
		if(this.filters){
			for(var i=0; i<this.filters.length; i++){
				var filter = this.filters[i];
				filter.filterDefaultValue =  filter.getValue();
			}
		}
		Sbi.trace("[ControlPanel.saveDefaultFiltersValue] : OUT");
    }

    /**
     * Execute the filters
     */
    , applyFilters: function(refreshThematization){
    	Sbi.trace("[ControlPanel.applyFilters] : IN");

    	this.filterValues = this.getFiltersState();

    	//filter the store
    	for(tName in this.mapComponnet.thematizers) {
    		var thematizer = this.mapComponnet.thematizers[tName];
    		thematizer.filterStore(this.filterValues);
    	}

    	//update the thematization
    	if(refreshThematization !== false) {
    		this.mapComponnet.getActiveThematizer().thematize({resetClassification: true});
    	}

    	Sbi.trace("[ControlPanel.applyFilters] : OUT");
    }


	, showFeedbackWindow: function(){
		if(this.feedbackWindow != null){
			this.feedbackWindow.destroy();
			this.feedbackWindow.close();
		}

		this.messageField = new Ext.form.TextArea({
			fieldLabel: LN('sbi.geo.controlpanel.feedback.label'),
            width: '100%',
            name: 'message',
            maxLength: 2000,
            height: '100%',
            autoCreate: {tag: 'textArea', type: 'text',  autocomplete: 'off', maxlength: '2000'}
		});

		this.sendButton = new Ext.Button({
			xtype: 'button',
			handler: function() {
				var msgToSend = this.messageField.getValue();
				sendMessage({'label': Sbi.config.docLabel, 'msg': msgToSend},'sendFeedback');
       		},
       		scope: this ,
       		text:LN('sbi.geo.controlpanel.feedback.btn.send'),
	        width: '100%'
		});


		var feedbackWindowPanel = new Ext.form.FormPanel({
			layout: 'form',
			bodyStyle: 'padding:5px',
			defaults: {
	            xtype: 'textfield'
	        },

	        items: [this.messageField]
		});


		this.feedbackWindow = new Ext.Window({
			modal		: true,
            layout      : 'fit',
	        width		: 550,
	        height		: 210,
            closeAction :'destroy',
            plain       : true,
            title		: LN('sbi.geo.controlpanel.feedback.title'),
            buttons		: [this.sendButton],
            items       : [feedbackWindowPanel]

		});

		this.feedbackWindow.show();
	}

	, showMeasureCatalogueWindow: function(){
		if(this.measureCatalogueWindow==null){

			var measureCatalogueConfig = {showBottomToolbar: false}

			var activeThemtizer = this.mapComponnet.getActiveThematizer();
			if(activeThemtizer.storeConfig
					&& activeThemtizer.storeConfig.params
					&& activeThemtizer.storeConfig.params.labels ){
				var selectedMeasureMap = {};
				for(var i = 0; i < activeThemtizer.storeConfig.params.labels.length; i++) {
					selectedMeasureMap[activeThemtizer.storeConfig.params.labels[i]] = activeThemtizer.storeConfig.params.labels[i];
				}
				measureCatalogueConfig.selectedMeasures = selectedMeasureMap;
			} else {
				Sbi.debug("[ControlPanel.showMeasureCatalogueWindow] : no measure already slected");
			}


			this.measureCatalogue = new Sbi.geo.tools.MeasuresCataloguePanel(measureCatalogueConfig);
			this.measureCatalogue.on('storeLoad', this.onStoreLoad, this);

			this.measureCatalogueWindow = new Ext.Window({
				modal		: true,
	            layout      : 'fit',
		        width		: 700,
		        height		: 350,
	            closeAction :'hide',
	            plain       : true,
	            title		: LN('sbi.tools.catalogue.measures.window.title'),
	            buttons		: [{
	            	text    : LN('sbi.tools.catalogue.measures.join.btn')
	    			, tooltip : LN('sbi.tools.catalogue.measures.join.tooltip')
				    , scope : this
				    , handler : function() { this.measureCatalogue.executeJoin(); }
	    	    }],
	            items       : [this.measureCatalogue]
			});
		}


		this.measureCatalogueWindow.show();
	}

	, showSaveMapWindow: function(){
		this.showSaveWindow(false);
	}

	, showSaveMapAsWindow: function(){
		this.showSaveWindow(true);
	}

	, showSaveWindow: function(isInsert){

		if(this.saveWindow != null){
			this.saveWindow.destroy();
			this.saveWindow.close();
		}

		var template = this.controlledPanel.validate();
		if (template == null) {
    		alert("Impossible to get template");
    		return;
    	}

    	Sbi.debug('[ControlPanel.showSaveWindow]: ' + template);

		var documentWindowsParams = {
				'OBJECT_TYPE': 'MAP',
				'OBJECT_TEMPLATE': template,
				'typeid': 'GEOREPORT'
		};

		var formState = {};
		//gets the input values (name, desccription,..)
		var el = Ext.get('docMapName');
		if ((el != null) && (el !== undefined ) && (el.getValue() !== '' )){
			formState.docName = el.getValue();
		}/*else{
			alert('Nome documento obbligatorio');
		}*/
		var el = Ext.get('docMapDesc');
		if ((el != null) && (el !== undefined )){
			formState.docDescr = el.getValue();
		}
		var el = Ext.get('scopePublic');
		if ((el != null) && (el !== undefined )){
			formState.scope = (el.dom.checked)?"true":"false";
		}else{
			formState.scope = "false"; //default
		}
		formState.scope.visibility = Sbi.config.visibility;
		formState.OBJECT_COMMUNITIES  = Sbi.config.docCommunities;
		formState.OBJECT_FUNCTIONALITIES  = Sbi.config.docFunctionalities;

		if (isInsert){
			formState.docLabel = 'map__' + Math.floor((Math.random()*1000000000)+1);
			if (Sbi.config.docDatasetLabel)
				documentWindowsParams.dataset_label= Sbi.config.docDatasetLabel;
			documentWindowsParams.MESSAGE_DET= 'DOC_SAVE_FROM_DATASET';
		}else{
			formState.docLabel = Sbi.config.docLabel;
			documentWindowsParams.MESSAGE_DET= 'MODIFY_GEOREPORT';
		}
		documentWindowsParams.formState = formState;
		documentWindowsParams.isInsert = isInsert;

		this.saveWindow = new Sbi.tools.documents.SaveDocumentWindow(documentWindowsParams);
		this.saveWindow.addListener('syncronizePanel', this.onSyncronizePanel, this);
		this.saveWindow.show();

	}


	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method
	 *
	 * Initialize the following services exploited by this component:
	 *
	 *    - none
	 */
	, initServices: function() {
		Sbi.debug("[ControlPanel.initServices]: IN");
		Sbi.debug("[ControlPanel.initServices]: there are no service to initialize");
		Sbi.debug("[ControlPanel.initServices]: OUT");
	}


	/**
	 * @method
	 *
	 * Initialize the GUI
	 */
	, init: function() {
		Sbi.debug("[ControlPanel.init]: IN");

		this.isFinalUser = (Sbi.template.role.indexOf('user') >= 0);
		Sbi.debug("[ControlPanel.init]: variable isFinalUser is equal to [" + this.isFinalUser + "]");

		this.isOwner = (Sbi.config.userId === Sbi.config.docAuthor)?true:false;
		Sbi.debug("[ControlPanel.init]: variable isOwner is equal to [" + this.isOwner + "]");

		this.isInsertion = (Sbi.config.docLabel === '')?true:false;
		Sbi.debug("[ControlPanel.init]: variable isInsertion is equal to [" + this.isInsertion + "]");

		this.innerPanel = new Ext.Panel({
			layout: 'fit',
			autoScroll: true,

//			html:  ' <main  id="main"> ' +
//			    '<div id="panel" >' +
//		    		'Keep calm!' +
//			    '</div>' +
//		    '</main>'
			html: ' <main class="main main-map" id="main"> ' +
					    '<div id="panel" class="panel">' +
					    	'<form class="panel-form" action="#" method="post">' +
					             '<div class="scroll" id="scroll">' +
					               '<div class="scroll-content" id="containerPanel">' +
					               		this.getThematizersDiv() +
					               		this.getIndicatorsDiv() +
					               		'<div class="filters" id="filtersDiv" ></div>'+
					                    this.getPermissionDiv() +
					               '</div>' +
					            '</div>' +  this.getPanelButtonsDiv() +
					        '</form>' +
					    '</div>' +
					'</main>'
		});

		this.innerPanel.on('render', function() {
			//Handle indicatorsChanged event for updating indicators
			this.mapComponnet.getActiveThematizer().on('indicatorsChanged', function(thematizer, indicators, selectedIndicator){
				this.setIndicators(indicators, selectedIndicator, false);
			}, this);

			//Handle filtersChanged event for adding filters combo
			this.mapComponnet.getActiveThematizer().on('filtersChanged', function(thematizer, filters){
				this.createFiltersPanel(filters);
			}, this);

			this.initInnerPannelCallbacks.defer(2000, this);
		}, this);

		Sbi.debug("[ControlPanel.init]: OUT");
	}

	, getThematizersDiv: function(){

		var mapName = (Sbi.config.docName !== "")?Sbi.config.docName: this.DEFAULT_NAME;
		var mapDescription = (Sbi.config.docDescription !== "")?Sbi.config.docDescription: this.DEFAULT_DESCRIPTION;

		var toReturn = '' +
		 '<div class="map-description" id="mapDescriptionDiv">' +
	         '<input  type="text" id="docMapName" class="mapTitle" value="' + mapName + '" /> '+
	         '<textarea rows="2" cols="40" id="docMapDesc" class="mapDescription" />' + mapDescription + ' </textarea>'+
	 		'<p id="author" class="published">'+LN('sbi.geo.controlpanel.publishedby')+'<a id="authorButton" class="authorButton">' + Sbi.config.docAuthor + '</a>'+
	 			'<span class="separator">/</span> <a id="feedback_mail" href="#" class="feedback">'+LN('sbi.geo.controlpanel.sendfeedback')+'</a></p>' +
	     '</div>' +
	     '<ul id="mapType" class="map-type">' +
	     	this.getThematizationOptionsList() +
	     '</ul>' ;

		return toReturn;
	}

	, getThematizationOptionsList: function() {
		var toReturn = '';

		var activeClass;
		var activeThematizer = this.mapComponnet.activeThematizerName;
		if(activeThematizer === "choropleth") {
			activeClass = "map-zone";
		} else if(activeThematizer === "proportionalSymbols") {
			activeClass = "map-point";
		} else {
			alert("[ControlPanel.getThematizationOptionsList]: active thematizer [" + activeThematizer + "] not valid");
		}

		var list = [];
		list.push('selected');

		var activeThematizationOption = null;
		for(var i = 0; i < this.thematizationOptions.length; i++) {
			var cName = this.thematizationOptions[i].className;

			if(cName === activeClass) {
				activeThematizationOption = this.thematizationOptions[i];
				continue;
			} else {
				list.push(this.thematizationOptions[i]);
			}
		}
		list[0] = activeThematizationOption;


		for(var i = 0; i < list.length; i++) {
			var cName = list[i].className;
			var expandButton = '';
			if(i === 0) {
				cName += ' active';
				expandButton ='<span class="arrow"></span>';
			} else if(i === list.length-1) {
				cName += ' last';
			}
			toReturn += '<li id="li-' + list[i].id + '" class="' + cName + '">' +
//							'<a href="#">' + list[i].label + '' + expandButton + '</a>' +
							'<a>' + list[i].label + '' + expandButton + '</a>' +
						'</li>';
		}
		return toReturn;
	}

	/**
	 * @method
	 *
	 * @return returns the html fragment used to render the indicator's selction panel
	 */
	, getIndicatorsDiv: function(){
		Sbi.trace("[ControlPanel.getIndicatorsDiv]: IN");
		if ( this.indicators != null &&  this.indicators !== undefined)	{
			var indicator = null;
			if(this.analysisConf.indicator) {
				indicator = this.analysisConf.indicator;
			} else {
				indicator = this.indicators[0][0];
				Sbi.warn("[ControlPanel.getIndicatorsDiv]: Selected indicator not specified. The first indicator of the list will be selected");
			}

			var toReturn = '' +
			'<div class="indicators" id="indicatorsDiv">' +
		    	'<h2>'+LN('sbi.geo.controlpanel.indicators')+'</h2>' +
		        '<ul id="ul-indicators" class="group">';
				for(var i=0; i< this.indicators.length; i++){
					var indEl = this.indicators[i];
					clsName = null;
					if(indicator === indEl[0]) {
						clsName = 'first';
					} else {
						clsName = 'disabled';
					}
					toReturn += '<li class="'+clsName+'" id="indicator'+i+'">' +
					'<span class="button" onclick="javascript:Ext.getCmp(\'controlPanel\').onIndicatorSelected(\'indicator'+i+'\',\''+indEl[0]+'\');">'+
//						'<a href="#" class="tick"></a>'+ indEl[1]+
						'<a  class="tick"></a>'+ indEl[1]+
					'</span>' +
				'</li>';
				}
		       toReturn +=	'</ul>';

		       if(this.mapComponnet.getActiveThematizer().storeType === 'physicalStore') {
		    	   toReturn += '<span id="addIndicatorButton" class="btn-2">'+LN('sbi.generic.select')+'</span>';
		       } else {
		    	   toReturn += '<span id="addIndicatorButton" class="btn-2">'+LN('sbi.generic.add')+'</span>';
		       }


		       toReturn += '</div>';
		} else {
			var toReturn = '' +
			'<div class="indicators" id="indicatorsDiv">' +
		    	'<h2>'+LN('sbi.geo.controlpanel.indicators')+'</h2>' +
		        '<ul id="ul-indicators" class="group">' +
		       	'</ul>';

			   if(this.mapComponnet.getActiveThematizer().storeType === 'physicalStore') {
		    	   toReturn += '<span id="addIndicatorButton" class="btn-2">'+LN('sbi.generic.select')+'</span>';
		       } else {
		    	   toReturn += '<span id="addIndicatorButton" class="btn-2">'+LN('sbi.generic.add')+'</span>';
		       }

			toReturn += '</div>';
		}

		Sbi.trace("[ControlPanel.getIndicatorsDiv]: OUT");

		return toReturn;
	}

	, resetIndicatorDiv: function() {
		Sbi.trace("[ControlPanel.resetIndicatorDiv]: IN");
		var indicatorsDiv = Ext.get("indicatorsDiv").dom;
		indicatorsDiv.parentNode.removeChild(indicatorsDiv);
		Sbi.trace("[ControlPanel.resetIndicatorDiv]: OUT");
	}

	/**
	 * @method
	 *
	 * @return
	 */
	, refreshIndicatorsDiv: function(){
		Sbi.trace("[ControlPanel.refreshIndicatorsDiv]: IN");

		var containerPanel = Ext.get("containerPanel").dom;

		this.resetIndicatorDiv();
		var indicatorsDiv = this.getIndicatorsDiv();
		this.renderIndicatorsDiv(indicatorsDiv);
		this.initCallbackOnGuiItemClick("addIndicatorButton", this.showMeasureCatalogueWindow, "add indicator button");

		var indicatorsUl = Ext.get('ul-indicators').dom.childNodes;
		if ((indicatorsUl[0] != null) && (indicatorsUl[0] !== undefined)){
			var indicatorSelected = false;
			for(var i = 0; i < this.indicators.length; i++) {
				if(this.analysisConf.indicator === this.indicators[i][0]) {
					var elementId = indicatorsUl[i].id;
					indicatorsUl[i].className = 'disabled';
					var indEl = this.indicators[i];
					this.onIndicatorSelected(elementId, indEl[0]);
					indicatorSelected = true;
					break;
				}
			}
			if(indicatorSelected == false) {
				var elementId = indicatorsUl[0].id;
				indicatorsUl[0].className = 'disabled';
				var indEl = this.indicators[0];
				this.onIndicatorSelected(elementId, indEl[0]);
			}
		}

		Sbi.trace("[ControlPanel.refreshIndicatorsDiv]: OUT");
	}

	/**
	 * @method
	 *
	 */
	, renderIndicatorsDiv: function(divHtmlFragment) {
		Sbi.trace("[ControlPanel.renderIndicatorsDiv]: IN");
		var mapTypeElement = Ext.get("mapType");
		Ext.DomHelper.insertAfter(mapTypeElement, divHtmlFragment);
		Sbi.trace("[ControlPanel.renderIndicatorsDiv]: OUT");
	}






	, getPermissionDiv: function(){
		Sbi.trace("[ControlPanel.getPermissionDiv]: IN");

		var toReturn = '';
		if (Sbi.settings.georeport.georeportPanel.controlPanelConf && Sbi.settings.georeport.georeportPanel.controlPanelConf.scopeInfoEnabled &&
				 Sbi.settings.georeport.georeportPanel.controlPanelConf.scopeInfoEnabled == true){

			if ( (Sbi.config.userId === Sbi.config.docAuthor) || !this.isFinalUser){
				toReturn = '<div class="map-permissions">' +
			    	'<div class="radio">' +
			        	'<span class="label">'+LN('sbi.geo.controlpanel.permissionlabel')+'</span>' ;

				if (Sbi.config.docIsPublic == 'false'){
					toReturn += '' +
						'<div  id="div-private" class="radio-option checked">' +
				        	'<input id="scopePrivate" type="radio" name="permissions" value="0" checked />' +
				            '<label for="permissions-1">'+LN('sbi.geo.controlpanel.permissionprivate')+'</label>' +
			            '</div>' +
			            '<div  id="div-public" class="radio-option ">' +
				        	'<input id="scopePublic" type="radio" name="permissions" value="1" />' +
				            '<label for="permissions-2">&nbsp;'+LN('sbi.geo.controlpanel.permissionpublic')+'</label>' +
				        '</div>';
				} else {
					toReturn += '' +
						'<div id="div-private" class="radio-option ">' +
				        	'<input id="scopePrivate" type="radio" name="permissions" value="0"  />' +
				            '<label for="permissions-1">'+LN('sbi.geo.controlpanel.permissionprivate')+'</label>' +
			            '</div>' +
			            '<div id="div-public" class="radio-option checked">' +
				        	'<input id="scopePublic" type="radio" name="permissions" value="1" checked />' +
				            '<label for="permissions-2">&nbsp;'+LN('sbi.geo.controlpanel.permissionpublic')+'</label>' +
				        '</div>';
				}
			}
		}
		Sbi.trace("[ControlPanel.getPermissionDiv]: OUT");

		return toReturn;
	}

	, getPanelButtonsDiv: function(){
		var toReturn = '' ;

		if (Sbi.settings.georeport.georeportPanel.saveWindow && Sbi.settings.georeport.georeportPanel.saveWindow.showSaveButton &&
				 Sbi.settings.georeport.georeportPanel.saveWindow.showSaveButton == true){
			if (this.isOwner){
				toReturn += ''+
					'<!-- // Mapper modify own map -->' +
			        '<div id="panel-buttons-container" class="panel-buttons-container map-owner">' +
			             '<div class="panel-buttons">' +
			                 //'<a href="#" class="btn-2">Annulla</a>' +
			                 '<input type="submit" id="btn-cancel" class="btn-2" value="'+LN('sbi.generic.cancel')+'" />' +
	//		                 '<input type="submit" id="btn-modify-map" class="btn-1" value="Aggiorna" />' +
			                 '<a href="#" id="btn-modify-map" class="btn-1">'+LN('sbi.generic.modify')+'</a>'  +
			             '</div>' +
			             '<p>'+LN('sbi.generic.save')+' <a  id="btn-new-map" href="#">'+LN('sbi.generic.newmap')+'</a></p>' +
			         '</div>';
			}else if (!this.isInsertion){
				toReturn += ''+
				     '<!-- // Mapper modify sombody else map -->' +
				     '<div id="panel-buttons-container" class="panel-buttons-container">' +
				         '<div class="panel-buttons">' +
	//			             '<a href="#" class="btn-2">Annulla</a>' +
				             '<input type="submit" id="btn-cancel" class="btn-2" value="'+LN('sbi.generic.cancel')+'" />' +
				             '<a href="#" id="btn-new-map" class="btn-1">'+LN('sbi.generic.savenewmap')+'</a>'  +
	//			             '<input type="submit" id="btn-new-map" class="btn-1" value="Salva nuova mappa" />' +
				         '</div>' +
				     '</div>';
			}else if (this.isInsertion){
				toReturn += ''+
					'<!-- // Mapper new map -->' +
			         '<div id="panel-buttons-container" class="panel-buttons-container">' +
			             '<div class="panel-buttons">	' +
	//		                 '<input type="submit" id="btn-modify-map" class="btn-1" value="salva" />' +
			                 '<a href="#" id="btn-new-map" class="btn-1">'+LN('sbi.generic.save')+'</a>'  +
			             '</div>' +
			         '</div>';
			}
		}
		return toReturn;
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init callbacks
	// -----------------------------------------------------------------------------------------------------------------

	, initCallbackOnGuiItemClick: function(itemId, callbackFn, itemName) {
		Sbi.trace("[ControlPanel.initCallbackOnGuiItemClick]: IN");

		var itemEl = Ext.get(itemId);
		if(itemEl && itemEl !== null) {
			itemEl.on('click', callbackFn, this);
			Sbi.debug("[ControlPanel.initCallbackOnAuthorButton]: Callback on " + (itemName!=undefined?itemName: itemId) + " succesfully registered");
		} else {
			Sbi.warn("[ControlPanel.initCallbackOnAuthorButton]: Impossible to ragister callback on " + (itemName!=undefined?itemName: itemId));
		}

		Sbi.trace("[ControlPanel.initCallbackOnAuthorButton]: OUT");
	}



	, initMapThematizationTypeCallbacks: function() {
		var thisPanel = this;

		var flyUlEl = Ext.select('.map-type');

		var elMapZone = Ext.get("li-map-zone");
		if(elMapZone && elMapZone !== null) {
			elMapZone.on('click', function() {
					this.onThematizerSelected(elMapZone, flyUlEl);
			}, thisPanel);
		}

		var elMapComparation = Ext.get("li-map-comparation");
		if(elMapComparation && elMapComparation !== null) {
			elMapComparation.on('click', function() {
					this.onThematizerSelected(elMapComparation, flyUlEl);
			}, thisPanel);
		}

		var elMapPoint = Ext.get("li-map-point");
		if(elMapPoint && elMapPoint !== null) {
			elMapPoint.on('click', function() {
					this.onThematizerSelected(elMapPoint, flyUlEl);
			}, thisPanel);
		}

		var elMapHeat = Ext.get("li-map-heat");
		if(elMapHeat && elMapHeat !== null) {
			elMapHeat.on('click', function() {
					this.onThematizerSelected(elMapHeat, flyUlEl);
			}, thisPanel);
		}

		this.closeMapH = null;
		this.openMapH = null;
	}

	, isThematizationDivOpen: function() {
		var el1 =  Ext.get("mapType");
		return Ext.fly(el1).hasClass('open');
	}

	, toggleThematizationDiv: function() {
		var el1 =  Ext.get("mapType");

		if (this.closeMapH == null){
			this.closeMapH =  Ext.fly(el1).getHeight();
			this.openMapH = this.closeMapH * this.thematizationOptions.length;
		}
		if (this.isThematizationDivOpen()) {
			Ext.fly(el1).dom.style.height = this.closeMapH-1;
			Ext.fly(el1).removeClass('open');
		} else {
			Ext.fly(el1).dom.style.height = this.openMapH;
			Ext.fly(el1).addClass('open');
		}
	}

	, initInnerPannelCallbacks: function() {

		Sbi.trace("[ControlPanel.initInnerPannelCallbacks]: IN");

		var thisPanel = this;

		this.initCallbackOnGuiItemClick("authorButton", function(){}, "author button");
		this.initCallbackOnGuiItemClick("feedback_mail", this.showFeedbackWindow, "feedback button");
		this.initCallbackOnGuiItemClick("scopePrivate", this.setMapPrivate, "private radio button");
		this.initCallbackOnGuiItemClick("scopePublic", this.setMapPublic, "public radio button");


		this.initMapThematizationTypeCallbacks();

		this.initCallbackOnGuiItemClick("btn-new-map", this.showSaveMapAsWindow, "save map button");
		this.initCallbackOnGuiItemClick("btn-modify-map", this.showSaveMapWindow, "modify map button");

		this.initCallbackOnGuiItemClick("addIndicatorButton", this.showMeasureCatalogueWindow, "add indicator button");

		//Initialize thematizerControlPanel form state
		var targetLayer = this.mapComponnet.getActiveThematizer().getLayer();
		Sbi.trace("[ControlPanel.initInnerPannelCallbacks] : target layer contains [" + targetLayer.features + "] features");
    	if(targetLayer != null && targetLayer.features.length > 0) {
    		Sbi.trace("[ControlPanel.initInnerPannelCallbacks] : target layer already loaded");
    		this.setAnalysisConf( this.analysisConf );
    	} else {
    		Sbi.trace("[ControlPanel.initInnerPannelCallbacks] : target layer not already loaded");
    		this.mapComponnet.getActiveThematizer().on('layerloaded', function(){
    			Sbi.trace("[ControlPanel.onlayerLoaded]: IN");
    			if(this.notAlreadyLoaded !== true) {
    				Sbi.debug("[AnalysisControlPanel]: [layerloaded] event fired");
    				this.setAnalysisConf( this.analysisConf );
    				this.notAlreadyLoaded = true;
    			}
    			Sbi.trace("[ControlPanel.onlayerLoaded]: OUT");

    		}, this);
    	}


		Sbi.trace("[ControlPanel.initInnerPannelCallbacks]: OUT");
	}


	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

	, onIndicatorSelected: function(elementId, indicator) {

		Sbi.trace("[ControlPanel.onIndicatorSelected]: IN");

		this.mapComponnet.getActiveThematizer().thematize({indicator: indicator});

		var el = Ext.get(elementId);
		if ((el != null) && (el !== undefined )){
			var currentClass = el.dom.className;
			//single selection / multiple selection management
			if (this.singleSelectionIndicator == true){
				if (currentClass == 'disabled'){

					var indicatorsUl = Ext.get('ul-indicators').dom.childNodes;
					//enable this element and disable all others
					for(var i=0; i< indicatorsUl.length; i++){
						if (indicatorsUl[i].id == elementId){
							indicatorsUl[i].className = 'first';
						} else {
							indicatorsUl[i].className = 'disabled';
						}
					}

				} else {
					//disable the only active indicator
					el.dom.className = 'disabled';
				}
			} else {
				if (currentClass == 'first'){
					el.dom.className = 'disabled';
				} else {
					el.dom.className = 'first';
				}
			}

		}

		Sbi.trace("[ControlPanel.onIndicatorSelected]: OUT");
	}

	, onThematizerSelected: function(el, list){
		Sbi.trace("[ControlPanel.onThematizerSelected]: IN");

		this.toggleThematizationDiv();

		if (el.id != list.item(0).first().id){
			var oldThematizer = this.mapComponnet.getActiveThematizer();
			var newThematizer = null;

			if(oldThematizer.layer && oldThematizer.layer.features && oldThematizer.layer.features.length > 0) {
				oldThematizer.features = oldThematizer.layer.features;
			}

			var thematizationOptions = {};
			if(el.id === 'li-map-zone') {
				Sbi.trace("[ControlPanel.onThematizerSelected]: activating choropleth thematizer...");
				var t = this.mapComponnet.thematizers['choropleth'];
				var isFirstActivation = t.isFirstActivation;

				this.mapComponnet.activateThematizer('choropleth');
				newThematizer = this.mapComponnet.getActiveThematizer();

				if(isFirstActivation) {
					Sbi.trace("[ControlPanel.onThematizerSelected]: is the first activation");
					thematizationOptions = this.getThematizerOptions(this.analysisConf);
				}
			} else if (el.id === 'li-map-point'){
				Sbi.trace("[ControlPanel.onThematizerSelected]: activating proportionalSymbols thematizer...");
				var t = this.mapComponnet.thematizers['proportionalSymbols'];
				var isFirstActivation = t.isFirstActivation;

				this.mapComponnet.activateThematizer('proportionalSymbols');
				newThematizer = this.mapComponnet.getActiveThematizer();

				if(isFirstActivation) {
					Sbi.trace("[ControlPanel.onThematizerSelected]: is the first activation");
					thematizationOptions = this.getThematizerOptions(this.analysisConf);
				}
			}

			thematizationOptions.indicator = oldThematizer.indicator;
			newThematizer.thematize(thematizationOptions);


			this.selectedThematizationOptionId =  el.id;

			var items = Ext.query('.active');
			Ext.each(items, function (item) {
		        item = Ext.get(item);
		        item.removeClass('active');
		        item.addClass('last');
		    }, this);

			//change position of arrow
			var dh = Ext.DomHelper;
			Ext.fly(el).addClass('active');
			var lItems = Ext.fly(el).down('a');
			dh.append(lItems, '<span class=\"arrow\" />');

			//refresh the list items (move the selected elem as first)
			//var len = list.item(0).dom.childElementCount;
			var len = 0;
			var newList = [el.dom];
			var listItem = list.item(0).dom.firstChild;
			while (listItem) {
				len++;
				if (listItem !== undefined && el.id != listItem.id){
					newList.push(listItem);
				} else {
					//alert("[" +el.id + "] != [" + listItem.id + "]");
				}

				listItem = listItem.nextSibling;
			}

			//clear
			this.clearList(list);
			//add
			this.addElemsToList(list, newList);
		}

		Sbi.trace("[ControlPanel.onThematizerSelected]: OUT");
	}

	, clearList: function(list){
		for(var z=0;z<list.item(0).dom.childElementCount;z++){
			list.item(0).dom.childNodes[z].remove();
		}
	}

	, addElemsToList: function(list, elems){
		for(var z=0;z<elems.length;z++){
			list.item(0).appendChild(elems[z]);
		}

	}

	, openIndicatorDetail: function(el){
		alert("openIndicatorDetail: " + el);
	}


	, getThematizerOptions: function(analysisConf) {
		//This inizialize the required options for thematizerControlPanel
		Sbi.debug("[getThematizerOptions.setAnalysisConf]: IN");

		Sbi.debug("[getThematizerOptions.setAnalysisConf]: analysisConf = " + Sbi.toSource(analysisConf));

		var formState = Ext.apply({}, analysisConf || {});

		var thematizerOptions = {};

		if(this.mapComponnet.activeThematizerName === "choropleth") {
			formState.method = formState.method || 'CLASSIFY_BY_QUANTILS';


			formState.classes =  formState.classes || 5;

			formState.fromColor =  formState.fromColor || '#FFFF99';
			formState.toColor =  formState.toColor || '#FF6600';

			if(formState.indicator && this.indicatorContainer === 'layer') {
				formState.indicator = formState.indicator.toUpperCase();
			}
			if(!formState.indicator && this.indicators && this.indicators.length > 0) {
				formState.indicator = this.indicators[0][0];
			}

			thematizerOptions.method = Sbi.geo.stat.Classifier[formState.method];
			thematizerOptions.classes = formState.classes;
			thematizerOptions.colors = new Array(2);
			thematizerOptions.colors[0] = new Sbi.geo.utils.ColorRgb();
			thematizerOptions.colors[0].setFromHex(formState.fromColor);
			thematizerOptions.colors[1] = new Sbi.geo.utils.ColorRgb();
			thematizerOptions.colors[1].setFromHex(formState.toColor);
			thematizerOptions.indicator = formState.indicator;
		} else if(this.mapComponnet.activeThematizerName === "proportionalSymbols") {
			thematizerOptions.minRadiusSize = formState.minRadiusSize || 2;
			thematizerOptions.maxRadiusSize = formState.maxRadiusSize || 20;
			thematizerOptions.indicator = formState.indicator;
		}

		Sbi.debug("[getThematizerOptions.setAnalysisConf]: OUT");
		return thematizerOptions;
	}

	, setAnalysisConf: function(analysisConf) {
		Sbi.debug("[ControlPanel.setAnalysisConf]: IN");

		var thematizerOptions = this.getThematizerOptions(analysisConf);
		this.mapComponnet.getActiveThematizer().thematize(thematizerOptions);

		Sbi.debug("[ControlPanel.setAnalysisConf]: OUT");
	}



	, onStoreLoad: function(measureCatalogue, options, store, meta) {
		Sbi.trace("[ControlPanel.onStoreLoad]: IN");

		this.measureCatalogueWindow.hide();

		for(thematizerName in this.mapComponnet.thematizers) {
			var thematizer = this.mapComponnet.thematizers[thematizerName];
			thematizer.setData(store, meta);

			thematizer.storeType = 'virtualStore';

			Sbi.debug("[ControlPanel.onStoreLoad]: options.url = " + options.url);
			Sbi.debug("[ControlPanel.onStoreLoad]: options.params = " + Sbi.toSource(options.params, true));

			thematizer.storeConfig = {
				url: options.url
				, params: options.params
			};
		}

		this.refreshIndicatorsDiv();
		Sbi.trace("[ControlPanel.onStoreLoad]: OUT");
	}

	, onSyncronizePanel: function(p) {
		var newName =  p.docName.getEl().getValue();
		var newDescr =  p.docDescr.getEl().getValue();
		Ext.get('docMapName').dom.value = newName;
		Ext.get('docMapDesc').dom.value = newDescr;
		var idxComm = p.docCommunity.store.find('functCode',p.docCommunity.value );
		if (idxComm == -1) //search with name (if element combo isn't selected)
			idxComm = p.docCommunity.store.find('name',p.docCommunity.value );
		if(idxComm > -1){
			var recComm = p.docCommunity.store.getAt(idxComm).data;
			Sbi.config.docCommunities =  recComm["functId"] + "||" + recComm["functCode"] + "__"  + recComm["name"];
			if (Sbi.config.docFunctionalities.indexOf(recComm["functId"]) == -1 )
				Sbi.config.docFunctionalities.push(recComm["functId"]);
		}else{
			Sbi.config.docCommunities = "||__" ; //reset value
		}
		this.setIsPublicValue(p.isPublic.value);
		//after insert redefines the buttons div (for modify)
		if (this.isInsertion = true && p.docLabel.value !== undefined && p.docLabel.value !== null &&
				p.docLabel.value !==""){
			this.isInsertion = false;
			this.isOwner = true;
			var buttonsDiv = Ext.get("panel-buttons-container").dom;
			//remove old buttons div
			buttonsDiv.parentNode.removeChild(buttonsDiv);
			//create new buttons div
			buttonsDiv = this.getPanelButtonsDiv();
			var precElement = Ext.get("scroll");
			var dh = Ext.DomHelper;
			dh.insertAfter(precElement,buttonsDiv);
			//redefines callback on buttons
			this.initCallbackOnGuiItemClick("btn-new-map", this.showSaveMapAsWindow, "save map button");
			this.initCallbackOnGuiItemClick("btn-modify-map", this.showSaveMapWindow, "modify map button");
			Sbi.config.docLabel = p.docLabel.value;
		}


	}

	, setIsPublicValue: function(v){
		//v is the value ("true"/"false") for public visibility
		var el1div =  Ext.get("div-private");
		var el2div =  Ext.get("div-public");
		var el1 = Ext.get("scopePrivate");
		var el2 = Ext.get("scopePublic");
		if (Ext.fly(el1div) && Ext.fly(el2div)){
//			if (Ext.fly(el1div).hasClass('checked')){
			if (v == "true"){
				el1.dom.checked = false;
				el2.dom.checked = true;
				Ext.fly(el1div).removeClass('checked');
				Ext.fly(el2div).addClass('checked');
			}else{
				el1.dom.checked=true;
				el2.dom.checked=false;
				Ext.fly(el2div).removeClass('checked');
				Ext.fly(el1div).addClass('checked');
			}
		}
	}



	// =================================================================================================================
	// EVENTS
	// =================================================================================================================

	//this.addEvents(
	/**
     * @event eventone
     * Fired when ...
     * @param {Sbi.xxx.Xxxx} this
     * @param {Ext.Toolbar} ...
     */
	//'eventone'
	/**
     * @event eventtwo
     * Fired before ...
     * @param {Sbi.xxx.Xxxx} this
     * @param {Object} ...
     */
	//'eventtwo'
	//);
});