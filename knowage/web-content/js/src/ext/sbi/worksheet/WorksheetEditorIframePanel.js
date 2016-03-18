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
  *  [list]
  * 
  * 
  * Public Events
  * 
  *  [list]
  * 
  * Authors
  * 
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */


Ext.ns("Sbi.worksheet");

Sbi.worksheet.WorksheetEditorIframePanel = function(config) {

	var defaultSettings = {
		autoLoad : true
        , loadMask : true
	};
		
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.worksheeteditoriframepanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.worksheeteditoriframepanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.init();
	
	c = Ext.apply(c, {
		defaultSrc : this.defaultSrc
        , tbar : this.toolbar
        , disableMessaging : false
        , frameConfig : {
			disableMessaging : false
        }	        
		, listeners : {
		
			'message:contentexported': {
        		fn: function(srcFrame, message) {
        	    	if (this.loadMask != null) {
        	    		this.hideMask();
        	    	}  
        		}
        		, scope: this
        	}
		
			, 'message:worksheetexporttaberror': {
						
        		fn: function(srcFrame, message) {
        			// must show warning message BEFORE mask management, otherwise the message will not be displayed, don't know why...
        			Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.worksheet.export.previewtab.msg'), LN('sbi.worksheet.export.previewtab.title'));
        	    	if (this.loadMask != null) {
        	    		this.hideMask();
        	    	}  
        		}
        		, scope: this
        	}
        	
		}
	});
	
	// constructor
    Sbi.worksheet.WorksheetEditorIframePanel.superclass.constructor.call(this, c);
    
};

Ext.extend(Sbi.worksheet.WorksheetEditorIframePanel, Ext.ux.ManagedIframePanel, {
	
	businessMetadata : null
	, datasetLabel : null
	, datasetParameters : null
	, datasourceLabel : null
	, engine : null // QBE/WORKSHEET
	
	,
	getDatasetLabel : function () {
		return this.datasetLabel;
	}

	,
	setDatasetLabel : function (datasetLabel) {
		this.datasetLabel = datasetLabel;
	}
	
	,
	getDatasourceLabel : function () {
		return this.datasourceLabel;
	}

	,
	setDatasourceLabel : function (datasourceLabel) {
		this.datasourceLabel = datasourceLabel;
	}
	
	,
	init : function () {
		this.initToolbar();
	}

	,
	initToolbar : function () {

		var saveButton = new Ext.Toolbar.Button({
			iconCls : 'icon-saveas' 
			, scope : this
    	    , handler : this.saveHandler
		});
		
	    var exportMenu = new Ext.menu.Menu({
			   items: [{
					text: LN('sbi.execution.PdfExport')
					, iconCls: 'icon-pdf' 
					, scope: this
					, width: 15
					, handler : function() { this.exportWorksheet('application/pdf'); }
			   }, {
					text: LN('sbi.execution.XlsExport')
					, iconCls: 'icon-xls' 
					, scope: this
					, width: 15
					, handler : function() { this.exportWorksheet('application/vnd.ms-excel'); }
			   }, {
					text: LN('sbi.execution.XlsxExport')
					, iconCls: 'icon-xlsx' 
					, scope: this
					, width: 15
					, handler : function() { this.exportWorksheet('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'); }
			   }]
		});
	    
	    var exportMenuButton = new Ext.Toolbar.MenuButton({
			   id: Ext.id()
			   , tooltip: 'Exporters'
			   , path: 'Exporters'	
			   , iconCls: 'icon-export' 	
			   , menu: exportMenu
			   , width: 15
			   , cls: 'x-btn-menubutton x-btn-text-icon bmenu '
		});	
		
		var items = ['->', saveButton];  //, exportMenuButton];
		this.toolbar = new Ext.Toolbar({
			  items: items
		});
	}
	
	,
	exportWorksheet : function (mimeType) {
    	var thePanel = this.getFrame().getWindow().workSheetPanel;
    	var template = thePanel.validate();	
    	if (template == null){
    		return;
    	}
    	
    	// must convert metadata into an array for the export service action
		var metadata = [];
		for (var name in this.businessMetadata) {
			var value = this.businessMetadata[name];
			metadata.push({
				meta_name : name
				, meta_content : value
				, meta_type : 'SHORT_TEXT'   // required for the export service action
			});
		}
    	
		// must convert parameters into an array for the export service action
		var parameters = [];
		for (var name in this.datasetParameters) {
			var value = this.datasetParameters[name];
			parameters.push({
				name : name
				, value : value
				, description : value    // required for the export service action
			});
		}
		
    	thePanel.exportContent(mimeType, metadata, parameters);
	}
	
	,
	saveHandler: function () {
		if (this.engine == 'QBE') {
			this.saveQbe();
		} else {
			this.saveWorksheet();
		}
	}
	
	,
	saveQbe : function () {
		try {
			if (!Sbi.user.functionalities.contains('BuildQbeQueriesFunctionality')) {
				// If user is not a Qbe power user, he can only save worksheet
				this.saveWorksheet();
			} else {
				// If the user is a Qbe power user, he can save both current query and worksheet definition.
				// We must get the current active tab in order to understand what must be saved.
				var qbeWindow = this.getFrame().getWindow();
				var qbePanel = qbeWindow.qbe;
				var anActiveTab = qbePanel.tabs.getActiveTab();
				var activeTabId = anActiveTab.getId();
				var isBuildingWorksheet = (activeTabId === 'WorksheetDesignerPanel' || activeTabId === 'WorkSheetPreviewPage');
				if (isBuildingWorksheet) {
					// save worksheet as document
					this.saveWorksheet();
				} else {
					// save query as new dataset
					var queryDefinition = this.getQbeQueryDefinition();
					var saveDatasetWindow = new Sbi.execution.toolbar.SaveDatasetWindow( { queryDefinition : queryDefinition } );
					saveDatasetWindow.on('save', function(theWindow, formState) { theWindow.close(); }, this);
					saveDatasetWindow.show();
				}
			}
		} catch (err) {
			alert('Sorry, cannot perform operation.');
			throw err;
		}
	}
	
	
	,
	getQbeQueryDefinition : function () {
		Sbi.debug('[WorksheetEditorIframePanel.getQbeQueryDefinition]: IN');
		var qbeWindow = this.getFrame().getWindow();
		Sbi.debug('[WorksheetEditorIframePanel.getQbeQueryDefinition]: got window');
		var qbePanel = qbeWindow.qbe;
		Sbi.debug('[WorksheetEditorIframePanel.getQbeQueryDefinition]: got qbe panel object');
		var queries = qbePanel.getQueriesCatalogueAsString();
		Sbi.debug('[WorksheetEditorIframePanel.getQbeQueryDefinition]: got queries as string');
		queries = Ext.util.JSON.decode(queries);
		Sbi.debug('[WorksheetEditorIframePanel.getQbeQueryDefinition]: got queries as JSON object');
		var toReturn = {};
		toReturn.queries = queries;
		toReturn.datasourceLabel = this.getDatasourceLabel();
		toReturn.sourceDatasetLabel = this.getDatasetLabel();
		return toReturn;
	}
	
	,
	saveWorksheet : function() {

		var theWindow = this.getFrame().getWindow();
		Sbi.debug('[WorksheetEditorIframePanel.saveWorksheet]: got window');
		
		//the worksheet has been constructed starting from a qbe document
		var thePanel = theWindow.qbe;
		Sbi.debug('[WorksheetEditorIframePanel.saveWorksheet]: qbe panel is ' + thePanel);
		if (thePanel == null) {
			Sbi.debug('[WorksheetEditorIframePanel.saveWorksheet]: qbe panel is null, getting woskheet panel ...');
			//the worksheet is alone with out the qbe
			thePanel = theWindow.workSheetPanel;
			Sbi.debug('[WorksheetEditorIframePanel.saveWorksheet]: woskheet panel is ' + thePanel);
		}
		
    	var template = thePanel.validate();	
    	if (template == null){
    		return;
    	}
    	var templateJSON = Ext.util.JSON.decode(template);
		var wkDefinition = templateJSON.OBJECT_WK_DEFINITION;
		var worksheetQuery = templateJSON.OBJECT_QUERY;
		var documentWindowsParams = {
				'OBJECT_TYPE': 'WORKSHEET',
				//'template': wkDefinition,
				'OBJECT_WK_DEFINITION': wkDefinition,
				'OBJECT_QUERY': worksheetQuery,
				'business_metadata': this.businessMetadata,
				'MESSAGE_DET': 'DOC_SAVE_FROM_DATASET',
				'dataset_label': this.getDatasetLabel(),
				'selected_datasource_label': this.getDatasourceLabel(),
				'typeid': 'WORKSHEET' 
		};
		this.win_saveDoc = new Sbi.execution.SaveDocumentWindow(documentWindowsParams);
		this.win_saveDoc.show();
    
    }
	
});