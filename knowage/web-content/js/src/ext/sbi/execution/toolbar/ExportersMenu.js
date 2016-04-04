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
 
var highChartForm; 
Ext.ns("Sbi.execution.toolbar");

Sbi.execution.toolbar.ExportersMenu = function(config) {
	
	this.validateConfigObject(config);
	this.adjustConfigObject(config);
	
	// init properties...
	var defaultSettings = {
		tooltip: 'Exporters'
		, path: 'Exporters'	
		, iconCls: 'icon-export' 	
		//, width: 15
		, cls: 'x-btn-menubutton x-btn-text-icon bmenu '
		, revertToButtonIfSingleItemMenu: false
		, baseMenuItemConfig: {
			text: LN('sbi.execution.GenericExport')
			, group: 'group_2'//ok, where's group_1?
			, iconCls: 'icon-pdf'  // use a generic icon here
			, scope: this
			, width: 15
			, handler : Ext.emptyFn
			, href: ''   
		}
	};
	

	
	
	if (Sbi.settings && Sbi.settings.toolbar && Sbi.settings.toolbar.exportersmenu) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.toolbar.exportersmenu);
	}
	
	var c = Ext.apply(defaultSettings, config || {});	
	Ext.apply(this, c);
	

	this.initServices();
	this.init();
	
	if(this.menuItemsConfig && this.menuItemsConfig.length > 0) {
		c.items = this.menuItemsConfig;
	} 
	
	// constructor
	Sbi.execution.toolbar.ExportersMenu.superclass.constructor.call(this, c);
	
	if ( this.documentType == 'DATAMART' || this.documentType == 'SMART_FILTER' ) {
		this.on('mouseexit', function(item) {item.hide();}, this);
		this.on('beforeshow', function(thisMenu){
			var documentWindow = this.getDocumentWindow();
			var documentPanel = documentWindow.qbe;
			if(documentPanel==null){//smart filter
				documentPanel = documentWindow.Sbi.formviewer.formEnginePanel;
			}
			var isBuildingWorksheet =  documentPanel.isWorksheetPageActive();
			var newItems; 
			thisMenu.removeAll(false);
			if (isBuildingWorksheet) {
				newItems = this.initMenuItemsConfig('WORKSHEET');
			} else {
				newItems = this.initMenuItemsConfig('DATAMART');
			}
			for(var i =0; i<newItems.length; i++){
				thisMenu.add(newItems[i]);
			}
		}, this);
	}
};

/**
 * @class Sbi.execution.toolbar.ExportersMenu
 * @extends Ext.Toolbar.MenuButton
 * 
 * The exporter menu. Contains all the exportation format supported by the executed docuement's type
 */

/**
 * @cfg {Object} config
 */
Ext.extend(Sbi.execution.toolbar.ExportersMenu, Ext.menu.Menu, {
    
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	
	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
	services: null
	
	/**
     * @property {Ext.Toolbar} toolbar
     * The parent toolabar (i.e. the toolbar that conatis this menu)
     */
	, toolbar: null
	
	, menuItemsConfig: null
	, exporters: null
	, documentType: null
	
	, exportationFormats: {
		'PDF' : {
			description: LN('sbi.execution.PdfExport')
			, iconCls: 'icon-pdf' 
		}, 
		'XLS' : {
			description: LN('sbi.execution.XlsExport')
			, iconCls: 'icon-xls' 
		},
		'XLSX' : {
			description: LN('sbi.execution.XlsxExport')
			, iconCls: 'icon-xlsx' 
		},
		'RTF' : {
			description: LN('sbi.execution.rtfExport')
			, iconCls: 'icon-rtf' 
		},
		'DOC' : {
			description: LN('sbi.execution.docExport')
			, iconCls: 'icon-rtf' 
		},
		'CSV' : {
			description: LN('sbi.execution.CsvExport')
			, iconCls: 'icon-csv' 
		},
		'XML' : {
			description: LN('sbi.execution.XmlExport')
			, iconCls: 'icon-xml' 
		},
		'GRAPHML' : {
			description: LN('sbi.execution.GraphmlExport')
			, iconCls: 'icon-graphml' 
		},
		'JPG' : {
			description: LN('sbi.execution.JpgExport')
			, iconCls: 'icon-jpg' 
		},
		'PNG' : {
			description: LN('sbi.execution.PngExport')
			, iconCls: 'icon-png' 
		},
		'TXT' : {
			description: LN('sbi.execution.txtExport')
			, iconCls: 'icon-txt' 
		},
		'PPT' : {
			description: LN('sbi.execution.pptExport')
			, iconCls: 'icon-ppt'  
		},
		'JRXML' : {
			description: LN('sbi.execution.jrxmlExport')
			, iconCls: 'icon-jrxml' 
		},
		'JSON' : {
			description: LN('sbi.execution.jsonExport')
			, iconCls:'icon-json'
		}
	}

	, exportationHandlers: {
		'REPORT': {
			  'PDF' : function() { this.exportReportTo('PDF'); }
			, 'XLS' : function() { this.exportReportTo('XLS'); }	
			, 'XLSX' : function() { this.exportReportTo('XLSX'); }
			, 'RTF' : function() { this.exportReportTo('RTF'); }
			, 'DOC' : function() { this.exportReportTo('DOC'); }
			, 'CSV' : function() { this.exportReportTo('CSV'); }
			, 'XML' : function() { this.exportReportTo('XML'); }
			, 'JPG' : function() { this.exportReportTo('JPG'); }
			, 'TXT' : function() { this.exportReportTo('TXT'); }
			, 'PPT' : function() { this.exportReportTo('PPT'); }
		},
		'OLAP': {
			 'PDF' : function() { this.exportOlapTo('PDF'); }
			,'XLS' : function() { this.exportOlapTo('XLS'); }
		},
		'DASH'	: {
			'PDF' : function() { this.exportChartTo('PDF'); }
		},
		'CHART': {
			 'PDF' : function() { this.exportChartTo('PDF'); }
			,'JPG' : function() { this.exportChartTo('JPG'); }
		},
		'MAP': {
			 'PDF' : function() { this.exportGeoTo('pdf');  }
			,'JPG' : function() { this.exportGeoTo('jpeg'); }
		},
		'DOCUMENT_COMPOSITE': {
//			 'PDF' : function() { this.exportCompositeDocumentTo('PDF'); }
			 'PDF'    : function() { this.exportCockpitTo('pdf','application/pdf'); }
			,'XLS'    : function() { this.exportCockpitTo('xls','application/vnd.ms-excel'); }
			,'XLSX'    : function() { this.exportCockpitTo('xlsx','application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'); }
		},
		'DATAMART': {
			 'PDF'    : function() { this.exportQbeTo('PDF'); }
			,'XLS'    : function() { this.exportQbeTo('XLS'); }
			,'XLSX'   : function() { this.exportQbeTo('XLSX'); }
			,'RTF'    : function() { this.exportQbeTo('RTF'); }
			,'CSV'    : function() { this.exportQbeTo('CSV'); }
			,'JRXML'  : function() { this.exportQbeTo('JRXML'); }
			,'JSON'   : function() { this.exportQbeTo('JSON'); }
		},
		//'SMART_FILTER': null,
		'WORKSHEET': {
			 'PDF'    : function() { this.exportWorksheetsTo('application/pdf'); }
			,'XLS'    : function() { this.exportWorksheetsTo('application/vnd.ms-excel'); }
			,'XLSX'    : function() { this.exportWorksheetsTo('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'); }
		},
		'NETWORK': {
			 'PDF'     : function() { this.exportNetworkTo('pdf'); }
			,'PNG'     : function() { this.exportNetworkTo('png'); }
			,'GRAPHML' : function() { this.exportNetworkTo('graphml'); }
		}
	}
	
	, baseMenuItemConfig: null

	
   
	// =================================================================================================================
	// METHODS
	// =================================================================================================================
	, getExportationUrl: function(format, documentType, contentUrl){
			Sbi.debug('[ExportersMenu.getExportationUrl] : format = [' + format + '], documentType = [' + documentType + ']');
		 	if (!this.exportationHandlers[documentType]) {
		 		Sbi.error('[ExportersMenu.getExportationUrl] : no available exporters for documentType = [' + documentType + ']');
		 		alert('No available exporters for this document');
		 		throw new Error('No available exporters for document type ' + documentType);
		 	}
		 	if (!this.exportationHandlers[documentType][format]) {
		 		Sbi.error('[ExportersMenu.getExportationUrl] : format [' + format + '] not supported for documentType = [' + documentType + ']');
		 		alert('Format ' + format + ' not supported for this document');
		 		throw new Error('Format ' + format + ' not supported for document type ' + documentType);
		 	}
		 	
			if(documentType != null && documentType == 'REPORT'){
				var documentUrl = null;
				if(contentUrl == null){
					documentUrl = this.getDocumentUrl();
				}
				else{
					documentUrl = contentUrl;
				}
				var exportationUrl = this.getUrlWithAddedParameters(documentUrl, {'outputType': format}, true);
				if(exportationUrl == null) {
					alert("Impossible to build exportation url");
					return;
				}
				return exportationUrl;
			}else if(documentType != null && documentType == 'OLAP'){
				var documentUrl = null;
				if(contentUrl == null){
					documentUrl = this.getDocumentUrl();
				}
				else{
					documentUrl = contentUrl;
				}

				var documentBaseUrl = this.getBaseUrlPart(documentUrl);
			    var exportationBaseUrl = this.getUrlWithReplacedEndpoint(documentBaseUrl, 'Print', false);
			    
			    parameters = {cube: '01'};
			    if (format == "PDF") parameters.type = '1';
			    else if(format == "XLS") parameters.type = '0';
			    var exportationUrl = this.getUrlWithAddedParameters(exportationBaseUrl, parameters, false);
			    return exportationUrl;
			}/*else if(documentType != null && (documentType == 'DASH' || documentType == 'CHART')){
			    var documentUrl = this.getDocumentUrl();
			    var documentBaseUrl = this.getBaseUrlPart(documentUrl);
			    var exportationBaseUrl = this.getUrlWithReplacedEndpoint(documentBaseUrl, 'Print', false);
			    
			    parameters = {cube: '01'};
			    if (format == "PDF") parameters.type = '1';
			    else if(format == "XLS") parameters.type = '0';
			    var exportationUrl = this.getUrlWithAddedParameters(exportationBaseUrl, parameters, false);
			    return exportationUrl;
			}*/else if(documentType != null && documentType == 'MAP'){
				if(format == 'PDF') {format = 'pdf';  }
				if(format == 'JPG') {format = 'jpeg'; }
				
				var documentUrl = null;
				if(contentUrl == null){
					documentUrl = this.getDocumentUrl();
				}
				else{
					documentUrl = contentUrl;
				}
				
				var documentBaseUrl = this.getBaseUrlPart(documentUrl);
				var exportationUrl = this.getUrlWithAddedParameters(documentBaseUrl, {
					ACTION_NAME: 'DRAW_MAP_ACTION'
					, SBI_EXECUTION_ID: this.executionInstance.SBI_EXECUTION_ID
					, outputFormat: format
					, inline: false
				}, false);
			    return exportationUrl;
			}else if(documentType != null && documentType == 'DOCUMENT_COMPOSITE'){
					var documentWindow = this.getDocumentWindow();
					
					var newPars='';
					//var isHighchart = false;
					//var isExtChart = false;
					var randUUID = Math.random();
					var idxElements = 0;
					for (var i=0; i<documentWindow.frames.length; i++) {
						
						childFrame = documentWindow.frames[i];
						
						//if the iframe contains a console document, it's not exported!
						if (childFrame.Sbi !== undefined && childFrame.Sbi.console !== undefined  ){
							continue;
						}
						
						var fullName = childFrame.name;
						var cutName= fullName.substring(7);
						var urlNotEncoded= childFrame.location.href;
						
						// I have to substitute %25 in '%' and %20 in ' ' 
						urlNotEncoded = urlNotEncoded.replace(/%25/g,'%');
						urlNotEncoded = urlNotEncoded.replace(/%20/g,' ');
						
						// encodeURIComponent is a standard js function. see: http://www.w3schools.com/jsref/jsref_encodeURIComponent.asp
						var urlEncoded = encodeURIComponent(urlNotEncoded);
						
						newPars += '&TRACE_PAR_' + cutName + '=' + urlEncoded;
						
						//for highcharts and ext charts documents gets the SVG and send it as a hidden form
						if (childFrame.chartPanel !== undefined && childFrame.chartPanel.chart !== undefined) {
							var svg = '';
							if (childFrame.chartPanel.chartsArr !== undefined){
								isHighchart = true;
								svg = this.getHighchartSvg(childFrame);
							}else{
								isExtChart = true;
								svg = this.getExtchartSvg(childFrame);
							}
							Ext.DomHelper.useDom = true; // need to use dom because otherwise an html string is composed as a string concatenation,
							 // but, if a value contains a " character, then the html produced is not correct!!!
							 // See source of DomHelper.append and DomHelper.overwrite methods
							 // Must use DomHelper.append method, since DomHelper.overwrite use HTML fragments in any case.
							 var dh = Ext.DomHelper;
							 highChartForm = document.getElementById('export-chart-form__'+ randUUID);
							 if (highChartForm === undefined || highChartForm === null) {
							     highChartForm = dh.append(Ext.getBody(), { // creating the hidden form
											  id: 'export-chart-form__' + randUUID
											  , tag: 'form'
											  , method: 'post'
										  });
							 }	  
							 
							 dh.append(highChartForm, {		// creating the hidden input in form
									tag: 'input'
									, type: 'hidden'
									, name: 'SVG_' + cutName
									, value: ''  // do not put value now since DomHelper.overwrite does not work properly!!
									});
							 
							// putting the chart data into hidden input
							//form.elements[i].value =  Ext.encode(svg);     
							highChartForm.elements[idxElements].value = svg;  
							idxElements ++;
							
						}				
					}//for 
					var urlExporter = (format=='PDF'?this.services['toDCPdf']:this.services['toDCXls']) + '&OBJECT_ID=' + this.executionInstance.OBJECT_ID;
					urlExporter += newPars;
					return urlExporter;
				if(exportationUrl == null) {
					alert("Impossible to build exportation url");
					return;
				}
				return exportationUrl;
					
			}else if(documentType != null && documentType == 'DATAMART'){
				if(format == 'PDF') {format = 'application/pdf'; }
				if(format == 'XLS') {format = 'application/vnd.ms-excel'; }
				if(format == 'XLSX') {format = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'; }
				if(format == 'RTF') {format = 'application/rtf'; }
				if(format == 'CSV') {format = 'text/csv'; }
				if(format == 'JRXML') {format = 'text/jrxml'; }
				if(format == 'JSON' ) {format = 'application/json'; }
				
				var documentUrl = null;
				if(contentUrl == null){
					documentUrl = this.getDocumentUrl();
				}
				else{
					documentUrl = contentUrl;
				}
				
			    var exportationUrl = this.getUrlWithAddedParameters(documentUrl, {
			    	ACTION_NAME: 'EXPORT_RESULT_ACTION'
			    	, SBI_EXECUTION_ID: this.executionInstance.SBI_EXECUTION_ID
			    	, MIME_TYPE: format
			    	, RESPONSE_TYPE: 'RESPONSE_TYPE_ATTACHMENT'
			    }, false);
			    return exportationUrl;
			}
	 }
	// -----------------------------------------------------------------------------------------------------------------
    // accessor methods
	// -----------------------------------------------------------------------------------------------------------------
	
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
		if(Ext.isEmpty(config)) {
			Sbi.error('[ExportersMenu.constructor] : Input parameter [config] cannot be empty');
			throw {msg: 'Input parameter [config]  passed to the costructor of calss [ExportersMenu] cannot be empty'};
		}
		
		if(Ext.isEmpty(config.toolbar)) {
			Sbi.error('[ExportersMenu.constructor] : Input parameter [config.toolbar] cannot be empty');
			throw {msg: 'Input parameter [config.toolbar]  passed to the costructor of calss [ExportersMenu] cannot be empty'};
		}
		
		if(Ext.isEmpty(config.executionInstance)) {
			Sbi.error('[ExportersMenu.constructor] : Input parameter [config.executionInstance] cannot be empty');
			throw {msg: 'Input parameter [config.executionInstance] passed to the costructor of calss [ExportersMenu] cannot be empty'};
		}
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
		if(config && config.executionInstance && config.executionInstance.document) {
			config.exporters = config.executionInstance.document.exporters;
			config.documentType = config.executionInstance.document.typeCode;
		}
	}
	
	/**
	 * @method
	 * 
	 * @param {String} documentType the type of executed document
	 * @param {String} format the target exportation format
	 * 
	 * @return {Function} the handler function for the specified document type and format pair if exists. undefined otherwise
	 */
	, getExportationHandler: function(documentType, format) {
		var handler = undefined;
		
		handler = function() {alert('No handler available for the specified exportation format')};
		
		var handlers = this.exportationHandlers[documentType];
		if(handlers) {
			handler = handlers[format];
		}
		
		return handler;
	}
	
	/**
	 * @method
	 * 
	 * @return {Ext.Toolbar} returns the parent #toolbar (i.e. the toolbar that contains this menu). Note: the toolbar
	 * must implement the followinh methods:
	 *  - <code>getDocumentUrl</code> that returns the url of executed document
	 *  - <code>getDocumentWindow</code> that returns the window that contains the executed document
	 */
	, getToolbar: function() {
		this.toolbar;
	}
	
	/**
	 * @method
	 * 
	 * @return {String} returns the url of executed document. Equals to <code>getToolbar().getDocumentUrl()</code>. Null if
	 * <code>getToolbar()</code> of <code>getToolbar().getDocumentUrl()</code> return null.
	 */
	, getDocumentUrl: function() {
		var url = null;
		if(this.toolbar && this.toolbar.getDocumentUrl) {
			url = this.toolbar.getDocumentUrl();
		}
		return url;
	}
	
	/**
	 * @method
	 * 
	 * @return {String} returns the url of executed document. Equals to <code>getToolbar().getDocumentWindow()</code>. Null if
	 * <code>getToolbar()</code> of <code>getToolbar().getDocumentWindow()</code> return null.
	 */
	, getDocumentWindow: function() {
		var window = null;
		if(this.toolbar && this.toolbar.getDocumentWindow) {
			window = this.toolbar.getDocumentWindow();
		}
		return window;
	}
	
	/**
	 * @deprecated
	 * 
	 * at the moment is used only by #exportWorksheetsTo to retrieve parametersPanel's formState. Finde a way
	 * to remove this dependencies
	 */
	, getController: function() {
		var controller = null;
		if(this.toolbar && this.toolbar.controller) {
			controller = this.toolbar.controller
		}
		return controller;
	}
	
	/**
	 * @method 
	 * 
	 * @return true if the menu does not contains any elements (= no exporter available for the 
	 * executed docuement)
	 */
	, isEmpty: function() {
		return this.menuItemsConfig === null || this.menuItemsConfig.length === 0;
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
		var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', SBI_EXECUTION_ID: null};
		
		this.services = this.services || new Array();
		
		this.services['toPdf'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'EXPORT_PDF'
			, baseParams: params
		});

		/* TODO erase obsolete services
		this.services['toDCPdf'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'EXPORT_DOCUMENT_COMPOSITION'
			, baseParams: params
		});
		
		this.services['toDCXls'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'EXPORT_DOCUMENT_COMPOSITION_XLS'
			, baseParams: params
		});*/
		
		this.services['getMetadataService'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName : 'GET_METADATA_ACTION',
			baseParams : params
		});
	}


	/**
	 * @method 
	 * 
	 * Initialize the GUI
	 */
	, init: function() {
		try {
			if(this.exporters == null){
				return;
			}		
			this.initMenuItemsConfig(this.documentType);	
		} catch (e){
			Sbi.error('[ExportersMenu.init] : An unexpected error occured during inizialization. The root cause of the error is : ' + e.msg );
		}
		
	}
	
	
	/**
	 * @method
	 * 
	 * Initialize the #menuItemsConfig property
	 * 
	 * @return {Array} The #menuItemsConfig property after the initialization
	 */
	, initMenuItemsConfig: function(documentType) {
		
		if(Ext.isEmpty(documentType, false)) throw {msg: 'Input parameter [documentType] of method [ExportersMenu.initMenuItemsConfig] cannot be empty'}
		
		Sbi.debug('[ExportersMenu.createMenu] : Creating menu items for an exporter menu associated to a document of type [' + documentType + ']');
		
		this.menuItemsConfig = new Array();
		
		for(i=0; i < this.exporters.length; i++) {
			
			var itemConfig = null;
						
			if ( this.exportationFormats[ this.exporters[i] ] ){ // if exportation format is supported
				itemConfig = Ext.apply(this.baseMenuItemConfig, {
					text: this.exportationFormats[ this.exporters[i] ].description
					, iconCls: this.exportationFormats[ this.exporters[i] ].iconCls 
					, handler : this.getExportationHandler(documentType, this.exporters[i])
		        });
				Sbi.debug('[ExportersMenu.createMenu] : Menu item for exportation format [' + this.exporters[i] + '] added succesfully to the exporter menu associated to a document of type [' + documentType + ']');
			} else {
				Sbi.warn('[ExportersMenu.createMenu] : Impossible to create menu item for exportation format [' + this.exporters[i] + '] because documents of type [' + documentType + '] do not support it');
			}
			// TODO: manage the case in which the format or the handler is not supported
			
			
			if(itemConfig != null) {
				this.menuItemsConfig.push(	
					new Ext.menu.Item(itemConfig)
				); 
			}
		} 
		
		Sbi.debug('[ExportersMenu.createMenu] : Succesfully created [' + this.menuItemsConfig.length + '] menu items out of [' + this.exporters.length + '] requested exportation format');
	
		return this.menuItemsConfig;
	}
	
	
	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
	
	// -----------------------------------------------------------------------------------------------------------------
    // export handler methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method
	 * 
	 * @param {format} target exportation format
	 */
	, exportReportTo: function(format, contentUrl) {

		var exportationUrl =  this.getExportationUrl(format, 'REPORT', contentUrl);
		window.open(exportationUrl, 'name', 'resizable=1,height=750,width=1000');
	}
	
	/**
	 * @method
	 * 
	 * @param {format} target exportation format
	 */
	, exportOlapTo: function (format, contentUrl) {

	    var exportationUrl = this.getExportationUrl(format, 'OLAP', contentUrl);	    
		window.open(exportationUrl,'name','resizable=1,height=750,width=1000');
	}
	
	/**
	 * @method
	 * 
	 * @param {format} target exportation format
	 */
	, exportChartTo: function (format) {
		var documentWindow = this.getDocumentWindow();
		if(documentWindow) {
			documentWindow.exportChart(format);
		}		
	}
	
	/**
	 * @method
	 * 
	 * @param {format} target exportation format
	 */
	, exportCockpitTo: function (format, mimeType) {
		var csvData;
		if(format != 'pdf'){
			csvData = [];
			var csvDataCount = 0;
		    var documentWindow = this.getDocumentWindow();
		    var cockpitPanel = documentWindow.cockpitPanel;
		    var storeManager = documentWindow.Ext.data.StoreManager;
		    var sheets = cockpitPanel.config.lastSavedAnalysisState.widgetsConf;
		    for (var i = 0; i < sheets.length; i++){
		    	var widgets = sheets[i].sheetConf.widgets;
		    	for (var j = 0; j < widgets.length; j++){
		    		var widget = widgets[j];
		    		if(widget.wtype == 'table'){
		    			var store = storeManager.lookup(widget.storeId);
		    			var metas = Object.keys(store.fieldsMeta);
		    			var metaIndex = [];
		    			csvData[csvDataCount] = '';
		    			for(var k = 0; k < metas.length; k++){
		    				var meta = store.fieldsMeta[metas[k]];
		    				csvData[csvDataCount] += meta.header + ';'
		    				metaIndex[k] = meta.dataIndex;
		    			}
		    			csvData[csvDataCount] += '\n';
		    			
		    			var allRecords = store.snapshot || store.data;
		    			for(var k = 0; k < allRecords.items.length; k++){
		    				for(var w = 0; w < metaIndex.length; w++){
		    					csvData[csvDataCount] += allRecords.items[k].raw[metaIndex[w]]+';';
		    				}
		    				csvData[csvDataCount] += '\n';
		    			}
		    			csvData[csvDataCount] = btoa(csvData[csvDataCount]);
		    			csvDataCount++;
		    		}
		    	}
		    }
		    if(false){
			    var widgetContainerList = cockpitPanel.widgetContainerList;
			    csvData = [];
			    var csvDataCount = 0;
			    for (var i = 0; i < widgetContainerList.length; i++){
			    	var widgets = widgetContainerList[i].widgetManager.widgets.items;
			    	for (var j = 0; j < widgets.length; j++){
			    		var widget = widgets[j];
			    		if(widget.wtype == 'table'){
			    			var store = widget.grid.store;
			    			var metas = Object.keys(store.fieldsMeta);
			    			var metaIndex = [];
			    			csvData[csvDataCount] = '';
			    			for(var k = 0; k < metas.length; k++){
			    				var meta = store.fieldsMeta[metas[k]];
			    				csvData[csvDataCount] += meta.header + ';'
			    				metaIndex[k] = meta.dataIndex;
			    			}
			    			csvData[csvDataCount] += '\n';
			    			
			    			var allRecords = store.snapshot || store.data;
			    			for(var k = 0; k < allRecords.items.length; k++){
			    				for(var w = 0; w < metaIndex.length; w++){
			    					csvData[csvDataCount] += allRecords.items[k].raw[metaIndex[w]]+';';
			    				}
			    				csvData[csvDataCount] += '\n';
			    			}
			    			csvData[csvDataCount] = btoa(csvData[csvDataCount]);
			    			csvDataCount++;
			    		}
			    	}
			    }
		    }
		    if(csvData){
		    	csvData = btoa(csvData);
		    }
		}
		var win = window.open('', 'exportWindow');
		var baseUrl = Sbi.config.serviceRegistry.baseUrl;
	    var dh = Ext.DomHelper;
	    var spec = {
    	    id: 'form',
    	    tag: 'form',
    	    method: 'POST',
    	    action: baseUrl.protocol+"://"+baseUrl.host+":"+baseUrl.port+"/highcharts-export-web/capture",
    	    target: 'exportWindow',
    	    children: [
    	        {tag: 'input', type: 'hidden', name: 'username', value: Sbi.user.userId},
    	        {tag: 'input', type: 'hidden', name: 'documentLabel', value: this.executionInstance.OBJECT_LABEL},
    	        {tag: 'input', type: 'hidden', name: 'type', value: mimeType},
    	        {tag: 'input', type: 'hidden', name: 'port', value: baseUrl.port},
    	        {tag: 'input', type: 'hidden', name: 'ip', value: baseUrl.host},
    	        {tag: 'input', type: 'hidden', name: 'protocol', value: baseUrl.protocol},
    	        {tag: 'input', type: 'hidden', name: 'context', value: baseUrl.contextPath},
    	        {tag: 'input', type: 'hidden', name: 'loginUrl', value: Sbi.config.loginUrl}
    	    ]
    	};
    	var form = dh.append(win.document.body, spec);
    	if(format != 'pdf'){
    		dh.append(form, {tag: 'input', type: 'hidden', name: 'csvData', value: csvData}, true);
    	}
    	var currentRole = this.executionInstance.ROLE?this.executionInstance.ROLE:'';
    	if(currentRole){
    		dh.append(form, {tag: 'input', type: 'hidden', name: 'role', value: currentRole}, true);
    	}
    	form.submit();
	    
	}
	
	/**
	 * @method
	 * 
	 * @param {format} target exportation format
	 */
	, exportGeoTo: function (format, contentUrl) {	

		var exportationUrl = this.getExportationUrl(format, 'MAP', contentUrl);	 
		window.open(exportationUrl,'name','resizable=1,height=750,width=1000');
	}
	
	/**
	 * @method
	 * 
	 * @TODO terrible code. refactor when possible!
	 * @TODO I think this is an obsolete code
	 */
	, exportCompositeDocumentTo: function (format) {
		isHighchart = false;
		isExtChart = false;
		var urlExporter = this.getExportationUrl(format, 'DOCUMENT_COMPOSITE');	
		
		//window.open(urlExporter,'exportWindow','resizable=1,height=650,width=800');

		if (isHighchart || isExtChart){
			window.open(null,'exportWindow','resizable=1,height=650,width=800');
			highChartForm.action = urlExporter;
			//form.target = '_blank'; // result into a new browser tab
			highChartForm.target = 'exportWindow'; // result into a popup
			highChartForm.submit();
		} else {
			window.open(urlExporter,'exportWindow','resizable=1,height=650,width=800');
		}
		
		Ext.DomHelper.useDom = false; //reset configuration for dom management
	}		
	
	, getHighchartSvg: function (childFrame) {
		var svgArr = [],
   	    top = 0,
  	    width = 0,
  	    svg = '';
		//in case of multiple charts redefines the svg object as a global (transforms each single svg in a group tag <g>)
		 for (var c=0; c < childFrame.chartPanel.chartsArr.length; c++){
			var singleChart = childFrame.chartPanel.chartsArr[c];
		    if (singleChart !== undefined && singleChart !== null){
	          	var singleSvg = singleChart.getSVG();
	          	singleSvg = singleSvg.replace('<svg', '<g transform="translate(0,' + top + ')" ');
	          	singleSvg = singleSvg.replace('</svg>', '</g>');
	
	            top += singleChart.chartHeight;
	            width = Math.max(width, singleChart.chartWidth);
	
	            svgArr.push(singleSvg);
	         }
		}
		//defines the global svg (for master/detail chart)
       svg = '<svg height="'+ top +'" width="' + width + '" version="1.1" xmlns="http://www.w3.org/2000/svg">';
       for (var s=0; s < svgArr.length; s++){
       	svg += svgArr[s];
       }
       svg += '</svg>';	   
		
	   return svg;
	}
	
	, getExtchartSvg: function (childFrame) {
		var chartPanel = childFrame.chartPanel;
		var svg = chartPanel.chart.save({type:'image/svg'});	          	
		svg = svg.substring(svg.indexOf("<svg"));

      	var tmpSvg = svg.replace("<svg","<g transform='translate(10,50)'");
		tmpSvg = tmpSvg.replace("</svg>", "</g>");
		
		svg = "<svg height='100%' width='100%' version='1.1' xmlns='http://www.w3.org/2000/svg'>";
		svg += tmpSvg;
      	
      	//adds title and subtitle
      	if (chartPanel.title){
      		var titleStyle = chartPanel.title.style;
      		titleStyle = titleStyle.replace("color","fill");
      		svg += "<text x='10'  y='25' style='" + titleStyle +"'>"+chartPanel.title.text+"</text>";
      	}
      	if (chartPanel.subtitle){
      		var subtitleStyle = chartPanel.subtitle.style;
      		subtitleStyle = subtitleStyle.replace("color","fill");
      		svg += "<text x='10' y='45' style='" + subtitleStyle +"'>"+chartPanel.subtitle.text+"</text>";	          		
      	}
      				
		svg += "</svg>";
		
	    return svg;
	}
	
	/**
	 * @method
	 * 
	 * @param {mimeType} target exportation mimeType
	 */
	, exportQbeTo: function (mimeType, contentUrl) {
		
		var documentUrl = this.getDocumentUrl();
	    var exportationUrl = this.getExportationUrl(mimeType, 'DATAMART', contentUrl);	 
	   
	    if(Ext.isIE6) {
		    var form = document.getElementById('export-form');
			if(!form) {
				var dh = Ext.DomHelper;
				form = dh.append(Ext.getBody(), {
				    id: 'export-form'
				    , tag: 'form'
				    , method: 'post'
				    , cls: 'export-form'
				});
			}
			
			form.action = exportationUrl;
			form.submit();
	    } else {
	    	window.open(exportationUrl, 'name','resizable=1,height=750,width=1000');
	    }
	}
	
	, exportWorksheetsTo: function (mimeType, records) {
		
		Sbi.debug('[ExportersMenu.exportWorksheetsTo] : IN');
		
		
		try {
			
			this.fireEvent('showmask','Exporting..');
			
			if(!records) {
				if(Sbi.user.functionalities.contains('SeeMetadataFunctionality')){
					Sbi.debug('[ExportersMenu.exportWorksheetsTo] : Loading records...');
					
					var urlForMetadata = this.services['getMetadataService'];
					urlForMetadata += "&OBJECT_ID=" + this.executionInstance.OBJECT_ID;
					if (this.executionInstance.SBI_SUBOBJECT_ID) {
						urlForMetadata += "&SUBOBJECT_ID=" + this.executionInstance.SBI_SUBOBJECT_ID;
					}
				
					var metadataStore = new Ext.data.JsonStore({
				        autoLoad: false,
				        fields: [
				           'meta_id'
				           , 'biobject_id'
				           , 'subobject_id'
				           , 'meta_name'
				           , 'meta_type'
				           , 'meta_content'
				           , 'meta_creation_date'
				           , 'meta_change_date'
				        ]
				        , url: urlForMetadata
				    });
				    metadataStore.on('load', function(store, records, options ) {
				    	Sbi.debug('[ExportersMenu.exportWorksheetsTo] : Record succefully loaded');
				    	this.exportWorksheetsTo(mimeType, records);
			    	}, this);
				    
				    metadataStore.load();
				}else{
					Sbi.debug('[ExportersMenu.exportWorksheetsTo] : User is not able to see metadata informations.');
					this.exportWorksheetsTo(mimeType, []);
				}
			} else {
				
				Sbi.debug('[ExportersMenu.exportWorksheetsTo] : exporting records...');
				var metadata = [];
				if(Sbi.user.functionalities.contains('SeeMetadataFunctionality')){
					for(var i = 0; i < records.length; i++) {
						var record = records[i];
						metadata.push(record.data);
					}
				}
				var documentWindow = this.getDocumentWindow();
				var documentPanel = documentWindow.qbe;
				if(documentPanel==null){
					//the worksheet has been constructed starting from a smart filter document
					documentPanel = documentWindow.Sbi.formviewer.formEnginePanel;
				}
				if(documentPanel==null){
					//the worksheet is alone with out the qbe
					documentPanel = documentWindow.workSheetPanel;
				}
				
				var parameters = [];
				var formState = this.getController().getParameterValues();
				for(f in formState) {
					if(f.indexOf('_field_visible_description') == -1) {
						var p = {name: f, value: formState[f]};
						var description = formState[f + '_field_visible_description'];
						if(description) p.description = description;
						parameters.push(p);
					}
				}
				
				documentPanel.exportContent(mimeType, false, metadata, parameters);
				Sbi.debug('[ExportersMenu.exportWorksheetsTo] : recods exported');
			}
		} catch (err) {
			alert('Sorry, cannot perform operation');
			throw err;
		}
		
		Sbi.debug('[ExportersMenu.exportWorksheetsTo] : OUT');
	}
	
	, exportKpiTo: function () {
		var urlExporter = this.services['toPdf'] + '&OBJECT_ID=' + this.executionInstance.OBJECT_ID;
		window.open(urlExporter,'name','resizable=1,height=750,width=1000');
	}	
	
	 , exportNetworkTo: function(type){
		 var documentWindow = this.getDocumentWindow();
		 if(documentWindow) {
			 var networkPanel = documentWindow.network;
			 networkPanel.exportNetwork(type);
		 }
	 }
	
	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------
	 
	 
	 
	/**
	 * @deprecated
	 */
	, changeDocumentExecutionUrlParameter: function(parameterName, parameterValue) {
		
		var parameters = {};
		parameters[parameterName] = parameterValue;
		this.addParameters(url, parameters, true);
	}
	
	/**
	 * Create a new url equal to the one passed in plus the parameters list recieved as input
	 * 
	 * @param {String} url the url to modify
	 * @param {Object} parameters the parameters to add
	 * @param {Boolean} append if the new parameters must be appended to the old ones or not. true by defualt
	 * 
	 * @return {String} the modified url
	 */
	, getUrlWithAddedParameters: function(url, parameters, append) {
	    
		Sbi.debug('[ExportMenu.getUrlWithAddedParameters: original url is equal to [' + url + ']');
		if(!url) {
			Sbi.warn('[ExportMenu.getUrlWithAddedParameters: original input parameter [url] cannor be null');
			return null;
		}
		
		if(append === undefined) append = true;
		
		var baseUrl = this.getBaseUrlPart(url);
		Sbi.debug('[ExportMenu.getUrlWithAddedParameters: base url is equal to [' + baseUrl + ']');
	    
		if(append === true) {
			var originalParameters = this.getParameters(url) || {};
			Sbi.debug('[ExportMenu.getUrlWithAddedParameters: original parameters are equal to [' + Sbi.toSource(originalParameters) + ']');
			parameters = Ext.apply(originalParameters, parameters);
		}
		Sbi.debug('[ExportMenu.getUrlWithAddedParameters: parameters to add are equal to [' + Sbi.toSource(parameters) + ']');
		var queryPart = Ext.urlEncode(parameters);
		Sbi.debug('[ExportMenu.getUrlWithAddedParameters: urlq query part is equal to [' + queryPart + ']');
		
		Sbi.debug('[ExportMenu.getUrlWithAddedParameters: modified url is equal to [' + baseUrl + '?' + queryPart + ']');
		
	    return  baseUrl + '?' + queryPart;
	}
	
	/**
	 * @method
	 * 
	 * Create a new url equal to the one passed with a changed endpoint (i.e. the last part in the url path
	 * just before the query part; ex. http://domain:port/a/b/c/endpoint?x=1&y=2)
	 * 
	 * @param {String} url the url to modify
	 * @param {String} endpoint the new endpoint
	 * @param {Boolean} keepParameters true to keep the query part of the original url
	 */
	, getUrlWithReplacedEndpoint: function(url, endpoint, keepParameters) {
		
		if(keepParameters === undefined) {
			keepParameters = false;
		}
		var baseUrl = this.getBaseUrlPart(url);
		var lastIndexOfSlash = baseUrl.lastIndexOf('/');
		parentOfBaseUrl = baseUrl.substring(0,lastIndexOfSlash) 
		var newUrl = parentOfBaseUrl + "/" + endpoint;
		if(keepParameters === true) {
			newUrl += "?" + this.getQueryPart(url);
		}
		return newUrl;
	}
	
	, getBaseUrlPart: function(url) {
		var baseUrl = url;
		if(baseUrl) {
			var urlParts = baseUrl.split('?');
			baseUrl = urlParts[0];
		}
	    return baseUrl;
	}
	
	, getQueryPart: function(url) {
		var queryPart = null;
		if(url && url.indexOf('?') > 0) {
			var urlParts = url.split('?');
			queryPart = urlParts[1];
		}
	    return queryPart;
	}
	
	, getParameters: function(url) {
		var parameters = null;
		var queryPart = this.getQueryPart(url); 
		if(queryPart) {
			queryPart = queryPart.replace(/\+/g, " ");
			parameters = Ext.urlDecode(queryPart);
		}
	    
	    return parameters;
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