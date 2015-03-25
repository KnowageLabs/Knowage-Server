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
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */


Ext.ns("Sbi.worksheet");

Sbi.worksheet.WorksheetFromDatasetPanel = function(config) {

	var defaultSettings = {
	};
		
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.worksheetfromdatasetpanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.worksheetfromdatasetpanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.init();
	
	c = Ext.apply(c, {
		layout : 'card'
		, activeItem : 0
		, hideMode: !Ext.isIE ? 'nosize' : 'display'
		, items : [
	           this.datasetsListPanel
	           , this.worksheetEditor
		]
		, border : false
	});
	
	// constructor
    Sbi.worksheet.WorksheetFromDatasetPanel.superclass.constructor.call(this, c);
    
};

Ext.extend(Sbi.worksheet.WorksheetFromDatasetPanel, Ext.Panel, {
	
	datasetsListPanel : null
	, worksheetEditor : null
	, worksheetEngineBaseUrl : null
	, qbeEngineBaseUrl : null
	
	,
	init : function () {
		this.datasetsListPanel = new Sbi.worksheet.DatasetsListPanel({
			title : LN('sbi.worksheet.worksheetfromdatasetpanel.choosedataset.msg')
			, layout : 'fit'
			, border : false
			, extraButtons : ['->', {
				text : LN('sbi.worksheet.worksheetfromdatasetpanel.buttons.gotoworksheet')
				, handler : this.moveToWorksheet
				, scope : this
			}, {
				text : LN('sbi.worksheet.worksheetfromdatasetpanel.buttons.gotoqbe')
				, handler : this.moveToQbe
				, scope : this
			}]
		});
		this.worksheetEditor = new Sbi.worksheet.WorksheetEditorIframePanelExt3({
			defaultSrc: 'about:blank'
			, businessMetadata : null
			, border : false
			, datasetLabel : null
			, datasetParameters : null
			, buttons : [{
				text : LN('sbi.generic.back')
				, handler : this.moveToDatasetsListPage
				, scope : this
			}]
		});
	}

	,
	moveToWorksheet : function () {
		
		var selectedRecord = this.datasetsListPanel.getSelectedRecord();
		if (selectedRecord == null) {
			Sbi.exception.ExceptionHandler.showWarningMessage(
					LN('sbi.worksheet.worksheetfromdatasetpanel.nodatasetselected.msg')
					, LN('sbi.generic.warning'));
			return;
		}
		var pars = selectedRecord.get('pars');
		if (pars.length > 0) {
			Sbi.exception.ExceptionHandler.showWarningMessage(
					'Sorry but selected dataset has parameters and cannot be used'
					, LN('sbi.generic.warning'));
			return;
		}
		var datasetLabel = selectedRecord.get('label');
		var datasourceLabel = this.getDatasourceLabel(selectedRecord);
		if (datasourceLabel == null) {
			return;
		}
		this.getLayout().setActiveItem( 1 );
		this.worksheetEditor.setSrc( this.worksheetEngineBaseUrl + '&dataset_label=' + datasetLabel + '&datasource_label=' + datasourceLabel );
		this.worksheetEditor.setDatasetLabel(datasetLabel);
		this.worksheetEditor.setDatasourceLabel(datasourceLabel);
		this.worksheetEditor.setEngine('WORKSHEET');
	}
	
	,
	moveToQbe : function () {
		
		var selectedRecord = this.datasetsListPanel.getSelectedRecord();
		if (selectedRecord == null) {
			Sbi.exception.ExceptionHandler.showWarningMessage(
					LN('sbi.worksheet.worksheetfromdatasetpanel.nodatasetselected.msg')
					, LN('sbi.generic.warning'));
			return;
		}
		var pars = selectedRecord.get('pars');
		if (pars.length > 0) {
			Sbi.exception.ExceptionHandler.showWarningMessage(
					'Sorry but selected dataset has parameters and cannot be used'
					, LN('sbi.generic.warning'));
			return;
		}
		var datasetLabel = selectedRecord.get('label');
		var datasourceLabel = this.getDatasourceLabel(selectedRecord);
		if (datasourceLabel == null) {
			return;
		}
		this.getLayout().setActiveItem( 1 );
		this.worksheetEditor.setSrc( this.qbeEngineBaseUrl + '&dataset_label=' + datasetLabel + '&selected_datasource_label=' + datasourceLabel );
		this.worksheetEditor.setDatasetLabel(datasetLabel);
		this.worksheetEditor.setDatasourceLabel(datasourceLabel);
		this.worksheetEditor.setEngine('QBE');
		
	}
	
	,
	moveToDatasetsListPage : function () {
		this.getLayout().setActiveItem( 0 );
		this.datasetsListPanel.refresh();
	}
	
	,
	getDatasourceLabel : function (selectedRecord) {
		var datasetType = selectedRecord.get('dsTypeCd');
		switch (datasetType) {
		  case "Qbe":
			  return selectedRecord.get('qbeDataSource');
			  break;
		  case "Query":
			  return selectedRecord.get('dataSource');
			  break;
		  default:
				Sbi.exception.ExceptionHandler.showWarningMessage(
						'Sorry but selected dataset isn\' supported for ad-hoc reporting'
						, LN('sbi.generic.warning'));
				return null;
		}
	}

});