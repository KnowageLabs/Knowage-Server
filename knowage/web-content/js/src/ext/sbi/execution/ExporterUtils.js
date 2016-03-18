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

Ext.ns("Sbi.execution");

Sbi.execution.ExporterUtils = {
	
	executionPanel : null  // instance of Sbi.execution.ExecutionPanel
	, documentsBrowser : null // instance of Sbi.browser.DocBrowserContainer
	
	,
	setExecutionPanel : function ( executionPanel ) {
		this.executionPanel = executionPanel;
	}
	
	,
	getExecutionPanel : function () {
		if (this.executionPanel != null) {
			return this.executionPanel;
		} else {
			var browser = this.getDocumentsBrowser();
			return browser.getActiveExecutionPanel();
		}
	}
	
	,
	setDocumentsBrowser : function ( documentsBrowser ) {
		this.documentsBrowser = documentsBrowser;
	}
	
	,
	getDocumentsBrowser : function () {
		return this.documentsBrowser;
	}
	
	,
	exportCurrentDocument : function ( outputType ) {

		Sbi.debug('[ExporterUtils.exportCurrentDocument]: IN');
		var executionPanel = this.getExecutionPanel();
		if (executionPanel == null) {
			Sbi.error('[ExporterUtils.exportCurrentDocument]: Execution panel not set');
			Sbi.debug('[ExporterUtils.exportCurrentDocument]: OUT');
			return;
		}
		var document = executionPanel.document;
		Sbi.debug('[ExporterUtils.exportCurrentDocument]: document retrieved ' + document);
		
		var docExecPage = executionPanel.activeDocument.documentExecutionPage;
		Sbi.debug('[ExporterUtils.exportCurrentDocument]: document execution page retrieved ' + docExecPage);
		
		var toolbar = docExecPage.toolbar;
		Sbi.debug('[ExporterUtils.exportCurrentDocument]: toolbar retrieved ' + toolbar);
		if (toolbar == null) {
			Sbi.debug('[ExporterUtils.exportCurrentDocument]: toolbar is null');
			docExecPage.toolbarHiddenPreference= false;
			config.executionToolbarConfig = docExecPage.initialConfig.executionToolbarConfig || {};
			config.executionToolbarConfig.callFromTreeListDoc = docExecPage.initialConfig.callFromTreeListDoc;

			toolbar = new Sbi.execution.toolbar.DocumentExecutionPageToolbar(config.executionToolbarConfig);
			Sbi.debug('[ExporterUtils.exportCurrentDocument]: new toolbar istantiated');
			docExecPage.toolbar = toolbar;
		}
		var executionInstance = docExecPage.executionInstance;
		Sbi.debug('[ExporterUtils.exportCurrentDocument]: execution instance retrieved ' + executionInstance);
		toolbar.controller= docExecPage;
		var exporter = new Sbi.execution.toolbar.ExportersMenu(
			{
			    toolbar: toolbar
				, executionInstance: executionInstance
			}
		);
		Sbi.debug('[ExporterUtils.exportCurrentDocument]: exporter created');
		var docType = document.typeCode;
		Sbi.debug('[ExporterUtils.exportCurrentDocument]: document type is [' + docType + ']');
		var exportUrl = exporter.getExportationUrl(outputType, docType);
		Sbi.debug('[ExporterUtils.exportCurrentDocument]: export url is [' + exportUrl + ']');
		if (exportUrl != undefined) {
			//window.open(exportUrl);
			Sbi.debug('[ExporterUtils.exportCurrentDocument]: creating export form ...');
			var randUUID = Math.random();
			var formId = 'export-form-' + randUUID;
			var dh = Ext.DomHelper;
			var form = dh.append(Ext.getBody(), {
			    id: formId
			    , tag: 'form'
			    , method: 'post'
			    , cls: 'export-form'
			    , target: '_blank'
			});
			Sbi.debug('[ExporterUtils.exportCurrentDocument]: export form created');
			form.action = exportUrl;
			form.submit();
			Sbi.debug('[ExporterUtils.exportCurrentDocument]: OUT');
			return;
		} else {
			// other types of document that doesn't use export url
			// ex: CHART, WORKSHEET, NETWORK
			if (docType == 'CHART' || docType == 'DASH') {
				Sbi.debug('[ExporterUtils.exportCurrentDocument]: exporting chart ...');
				exporter.exportChartTo(outputType);
			} else if (docType == 'NETWORK') {
				Sbi.debug('[ExporterUtils.exportCurrentDocument]: exporting network analysis ...');
				if(outputType == 'PDF') {outputType = 'pdf'; }
				if(outputType == 'PNG') {outputType = 'png'; }
				if(outputType == 'GRAPHML') {outputType = 'graphml'; }
				
				exporter.exportNetworkTo(outputType);
			} else if (docType == 'WORKSHEET') {
				Sbi.debug('[ExporterUtils.exportCurrentDocument]: exporting worksheet ...');
				if (outputType == 'PDF') {
					outputType = 'application/pdf';
				} else if (outputType == 'XLS') {
					outputType = 'application/vnd.ms-excel';
				} else if (outputType == 'XLSX') {
					outputType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
				}
				
				exporter.exportWorksheetsTo(outputType);
			}
			
		}
		Sbi.debug('[ExporterUtils.exportCurrentDocument]: OUT');
		return;
	}

};