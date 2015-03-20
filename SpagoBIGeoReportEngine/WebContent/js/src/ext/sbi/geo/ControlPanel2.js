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
Sbi.geo.controlPanel = function(config) {
	this.validateConfigObject(config);
	this.adjustConfigObject(config);
	
	// init properties...
	var defaultSettings = {
		id:'controlPanel',
		region      : 'east',
		split       : false,
		width       : 365,
		collapsible : true,
		collapsed   : false,
		autoScroll	: true,
		layout		: 'fit',
		margins     : '0 0 0 0',
		cmargins    : '0 0 0 0',
		collapseMode: 'none',
        hideCollapseTool: true,
		hideBorders: true,
		border		: false,
		frame: false,
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
    Sbi.geo.controlPanel.superclass.constructor.call(this, c);
};


/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.geo.controlPanel, Ext.Panel, {
    
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	
	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
	services: null
	  
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

	, selectedThematizationOptionId: 'map-zone'
	
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
		
	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // accessor methods
	// -----------------------------------------------------------------------------------------------------------------
	
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
     * Gets filters values
     */
    , getFilters:function(){
    	Sbi.trace("[ControlPanel.getFilters] : IN.. Get the values of the filters"); 
    	var filters =new Array();
		if(this.filters){
			for(var i=0; i<this.filters.length; i++){
				var filter = this.filters[i];
				filters.push({
					field: filter.name,
					value: filter.getValue()
				});
			}
		}
		Sbi.trace("[ControlPanel.getFilters] : OUT");
		return filters;
    }	
    
    /**
     * Create the filter comboboxes
     * @param filters the fiters definition
     */
	, setFilters: function(filters){
		//Adding comboboxes for filters
		Sbi.debug("[ControlPanel.setFilters]: IN");
		
		if(!this.filters){
			this.filters = new Array();
		}

		/*
		if(this.setDefaultsValuesToFiltersButton){
			this.remove(setDefaultsValuesToFiltersButton, true);
		}
		*/
	
		//remove the old filters
		for(var i=0; i<this.filters.length; i++){
			this.remove(this.filters[i],true);
		}
		this.filters = new Array();
		
		//build the new filters
		for(var i=0; i<filters.length; i++){
			
			Sbi.debug("[ControlPanel.setFilters]: Filter [" + filters[i].name + "] have [" + filters[i].values.length + "] possible values");
			
			var filterDef = filters[i];
			var filter =new Ext.form.ComboBox  ({
	            fieldLabel: filterDef.header,
	            name: filterDef.name,
	            editable: false,
	            mode: 'local',
	            allowBlank: true,
	            valueField: 'val',
	            displayField: 'val',
	            emptyText: 'Select a value',
	            triggerAction: 'all',
	            store: new Ext.data.SimpleStore({
	            	fields: ['val'],
	                data : 	filterDef.values
	            }),
	            listeners: {
	                'select': {
	                    fn: function() {
	                    	this.filterDataSet();
	                    },
	                    scope: this
	                }
	            }
	        });

			this.filters.push(filter);
			
			var filterLabel = new Ext.form.DisplayField({
				fieldLabel : filterDef.header,
				width:  300,
				allowBlank : false,
				name: filterDef.header,
				readOnly:true,
				hidden: false
			});
			filterLabel.setValue(filterDef.header);
			
			filterLabel.render("filtersDiv");
			filter.render("filtersDiv");
		}
		
		
		if((Sbi.config.docLabel=="" && this.filters && this.filters.length>0)){
			this.setDefaultsValuesToFiltersButton =  new Ext.Button({
		    	text: LN('sbi.geo.analysispanel.filter.default'),
		        width: 30,
		        handler: function() {
		        	this.saveDefaultFiltersValue();
		        	Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.geo.analysispanel.filter.default.ok'));
           		},
           		scope: this
			});
			this.setDefaultsValuesToFiltersButton.render("filtersDiv");
		}
		
		
		this.doLayout();
		
		
		Sbi.debug("[ControlPanel.setFilters]: OUT");
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
    , filterDataSet: function(){
    	Sbi.trace("[ControlPanel.filterDataSet] : IN");      
    	//get filter values
    	var filters = this.getFilters();
    	//filter the store
    	this.mapComponnet.getActiveThematizer().filterStore(filters);
    	//update the thematization
    	//this.thematizerControlPanel.thematize(false, {resetClassification: true});
    	this.mapComponnet.getActiveThematizer().thematize({resetClassification: true});
    	Sbi.trace("[ControlPanel.filterDataSet] : OUT");      
    }
	
    
	, showFeedbackWindow: function(){
		if(this.feedbackWindow != null){			
			this.feedbackWindow.destroy();
			this.feedbackWindow.close();
		}
		
		this.messageField = new Ext.form.TextArea({
			fieldLabel: 'Message text',
            width: '100%',
            name: 'message',
            maxLength: 2000,
            height: 100,
            autoCreate: {tag: 'textArea', type: 'text',  autocomplete: 'off', maxlength: '2000'}
		});
		
		this.sendButton = new Ext.Button({
			xtype: 'button',
			handler: function() {
				var msgToSend = this.messageField.getValue();
				sendMessage({'label': Sbi.config.docLabel, 'msg': msgToSend},'sendFeedback');
       		},
       		scope: this ,
       		text:'Send',
	        width: '100%'
		});

		
		var feedbackWindowPanel = new Ext.form.FormPanel({
			layout: 'form',
			defaults: {
	            xtype: 'textfield'
	        },

	        items: [this.messageField,this.sendButton]
		});
		
		
		this.feedbackWindow = new Ext.Window({
            layout      : 'fit',
	        width		: 700,
	        height		: 170,
            closeAction :'destroy',
            plain       : true,
            title		: 'Send Feedback',
            items       : [feedbackWindowPanel]
		});
		
		this.feedbackWindow.show();
	}
	
	, showMeasureCatalogueWindow: function(){
		if(this.measureCatalogueWindow==null){
			var measureCatalogue = new Sbi.geo.tools.MeasureCataloguePanel();
			measureCatalogue.on('storeLoad', this.onStoreLoad, this);
			
			this.measureCatalogueWindow = new Ext.Window({
	            layout      : 'fit',
		        width		: 700,
		        height		: 350,
	            closeAction :'hide',
	            plain       : true,
	            title		: LN('sbi.tools.catalogue.measures.window.title'), //OpenLayers.Lang.translate('sbi.tools.catalogue.measures.window.title'),
	            items       : [measureCatalogue]
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
					            '</div>' + this.getPanelButtonsDiv() + 
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
				this.setFilters(filters);
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
	 		'<p id="author" class="published">'+LN('sbi.geo.controlpanel.publishedby')+'<a id="authorButton" class="authorButton" href="#">' + Sbi.config.docAuthor + '</a>'+
	 			'<span class="separator">/</span> <a id="feedback_mail" href="#" class="feedback">'+LN('sbi.geo.controlpanel.sendfeedback')+'</a></p>' +
	     '</div>' +
	     '<ul id="mapType" class="map-type">' + 
	     	this.getThematizationOptionsList() +
	     '</ul>' ;
		
		return toReturn;
	}	

	, getThematizationOptionsList: function() {
		var toReturn = '';
		for(var i = 0; i < this.thematizationOptions.length; i++) {
			var cName = this.thematizationOptions[i].className;
			var expandButton = '';
			if(i === 0) {
				cName += ' active';
				expandButton ='<span class="arrow"></span>';
			} else if(i === this.thematizationOptions.length-1) {
				cName += ' last';
			}
			toReturn += '<li id="li-' + this.thematizationOptions[i].id + '" class="' + cName + '">' + 
							'<a href="#">' + this.thematizationOptions[i].label + '' + expandButton + '</a>' + 
						'</li>';
		}
		return toReturn;
	}

	, getIndicatorsDiv: function(){
		if ( this.indicators != null &&  this.indicators !== undefined){
			
			var toReturn = '' +
			'<div class="indicators" id="indicatorsDiv">' +
		    	'<h2>'+LN('sbi.geo.controlpanel.indicators')+'</h2>' +
		        '<ul id="ul-indicators" class="group">';		
				for(var i=0; i< this.indicators.length; i++){
					var indEl = this.indicators[i];
					var clsName = (i==0)?'first':'disabled';
					toReturn += ''+
					'<li class="'+clsName+'" id="indicator'+i+'"><span class="button">'+
						'<a href="#" class="tick" onclick="javascript:Ext.getCmp(\'controlPanel\').onIndicatorSelected(\'indicator'+i+'\',\''+indEl[0]+'\');"></a>'+ indEl[1]+
		            '</li>' ;	
				}
		       toReturn +=''+
		       	'</ul>' +
		        '<span id="addIndicatorButton" class="btn-2">'+LN('sbi.generic.add')+'</span>' +
		    '</div>';
		} else {
			var toReturn = '' +
			'<div class="indicators" id="indicatorsDiv">' +
		    	'<h2>Indicatori</h2>' +
		        '<ul id="ul-indicators" class="group">' +		
		       	'</ul>' +
		        '<span id="addIndicatorButton" class="btn-2">'+LN('sbi.generic.add')+'</span>' +
		    '</div>';
		}
		
		return toReturn;
	}
	
	, refreshIndicatorsDiv: function(){
		var containerPanel = Ext.get("containerPanel").dom;
		var indicatorsDiv = Ext.get("indicatorsDiv").dom;
		//remove old indicators div
		indicatorsDiv.parentNode.removeChild(indicatorsDiv); 
		//create new indicators div
		indicatorsDiv = this.getIndicatorsDiv();
		var mapTypeElement = Ext.get("mapType");
		var dh = Ext.DomHelper;	
		dh.insertAfter(mapTypeElement,indicatorsDiv);
		
		//Re-add handler on addIndicatorButton
		var elAddIndicator = Ext.get("addIndicatorButton");
		if(elAddIndicator && elAddIndicator !== null) {
			elAddIndicator.on('click', function() {
				this.showMeasureCatalogueWindow();
			},this);
			Sbi.debug("[ControlPanel.refreshIndicatorsDiv]: Registered handler on [addIndicatorButton] ");
		} else {
			Sbi.debug("[ControlPanel.refreshIndicatorsDiv]: Impossible to find element [addIndicatorButton] ");
			alert('Impossible to find element [addIndicatorButton]');
		}		
		
		//Activate first indicator as default
		var indicatorsUl = Ext.get('ul-indicators').dom.childNodes;
		if ((indicatorsUl[0] != null) && (indicatorsUl[0] !== undefined)){
			var elementId = indicatorsUl[0].id;
			indicatorsUl[0].className = 'disabled';
			var indEl = this.indicators[0];
			this.onIndicatorSelected(elementId, indEl[0]);
		}
	}
	
	, getPermissionDiv: function(){
		var toReturn = '';
		
		if (Sbi.config.userId === Sbi.config.docAuthor || !this.isFinalUser){
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
		return toReturn;
	}
	
	, getPanelButtonsDiv: function(){
		var toReturn = '' ;
				
		if (this.isOwner){
			toReturn += ''+
				'<!-- // Mapper modify own map -->' +
		        '<div class="panel-buttons-container map-owner">' +
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
			     '<div class="panel-buttons-container">' +
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
		
		var closeMapH = null;
		var openMapH = null;
		var elMapType = Ext.get("mapType");	
		if(elMapType && elMapType !== null) {
			elMapType.on('click', function() {				
				
				var el1 =  Ext.get("mapType");
				
				if (closeMapH == null){
					closeMapH =  Ext.fly(el1).getHeight();
					openMapH = closeMapH * this.thematizationOptions.length;
				}
				if (Ext.fly(el1).hasClass('open')){
					Ext.fly(el1).dom.style.height = closeMapH-1; 
					Ext.fly(el1).removeClass('open');
				}else{
					Ext.fly(el1).dom.style.height = openMapH; 
					Ext.fly(el1).addClass('open');
				}
			}, thisPanel);
		} else {
			//alert('Impossible to find element [maptype]');
		}
	}
	

	
	, initInnerPannelCallbacks: function() {
		
		Sbi.trace("[ControlPanel.initInnerPannelCallbacks]: IN");
		
		var thisPanel = this;
		
		this.initCallbackOnGuiItemClick("authorButton", function(){}, "author button");
		this.initCallbackOnGuiItemClick("feedback_mail", this.showFeedbackWindow, "feedback button");
		this.initCallbackOnGuiItemClick("permissions-1", this.setMapPrivate, "private radio button");
		this.initCallbackOnGuiItemClick("permissions-2", this.setMapPublic, "public radio button");
		
	
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

	, onThematizerSelected: function(el, list){
		Sbi.trace("[ControlPanel.onThematizerSelected]: IN");
		
		if (el.id != list.item(0).first().id){
			
			alert("Selected thematizer [" + el.id + "]");
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
			var len = list.item(0).dom.childElementCount;
			var newList = [el.dom];
			for(var z=0;z<len;z++){
				var optEl =list.item(0).dom.childNodes[z];							
				if (optEl !== undefined && el.id != optEl.id){
					newList.push(optEl);
				}
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
	
	, setAnalysisConf: function(analysisConf) {
		//This inizialize the required options for thematizerControlPanel
		Sbi.debug("[ControlPanel.setAnalysisConf]: IN");
		
		Sbi.debug("[ControlPanel.setAnalysisConf]: analysisConf = " + Sbi.toSource(analysisConf));
		
		var formState = Ext.apply({}, analysisConf || {});
		
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
		
		var thematizerOptions = {};
		thematizerOptions.method = Sbi.geo.stat.Classifier[formState.method];
		thematizerOptions.classes = formState.classes;
		thematizerOptions.colors = new Array(2);
		thematizerOptions.colors[0] = new Sbi.geo.utils.ColorRgb();
		thematizerOptions.colors[0].setFromHex(formState.fromColor);
		thematizerOptions.colors[1] = new Sbi.geo.utils.ColorRgb();
		thematizerOptions.colors[1].setFromHex(formState.toColor);
		thematizerOptions.indicator = formState.indicator;
		
		this.mapComponnet.getActiveThematizer().thematize(thematizerOptions);
		
		Sbi.debug("[ControlPanel.setAnalysisConf]: OUT");
	}
	
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
	, onStoreLoad: function(measureCatalogue, options, store, meta) {
		this.measureCatalogueWindow.close();
		this.mapComponnet.getActiveThematizer().setData(store, meta);
		// TODO verify
		//this.thematizerControlPanel.storeType = 'virtualStore';
		var s = "";
		for(o in options) s += o + ";"
		Sbi.debug("[ControlPanel.onStoreLoad]: options.url = " + options.url);
		Sbi.debug("[ControlPanel.onStoreLoad]: options.params = " + Sbi.toSource(options.params));
		// TODO verify
//		this.thematizerControlPanel.storeConfig = {
//			url: options.url
//			, params: options.params
//		};
		
		this.refreshIndicatorsDiv();
	}

	, onSyncronizePanel: function(p) {
		var newName =  p.docName.getEl().getValue();
		var newDescr =  p.docDescr.getEl().getValue();
		Ext.get('docMapName').dom.value = newName;
		Ext.get('docMapDesc').dom.value = newDescr;

		this.setIsPublicValue(p.isPublic.getEl().getValue());

	}
	
	, setIsPublicValue: function(v){
		var el1div =  Ext.get("div-private");
		var el2div =  Ext.get("div-public");
		var el1 = Ext.get("scopePrivate");
		var el2 = Ext.get("scopePublic");
		if (Ext.fly(el1div).hasClass('checked')){
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