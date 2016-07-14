(function() {
	var documentExecutionModule = angular.module('documentExecutionModule');
	
	documentExecutionModule.service('docExecute_exportService', function(sbiModule_translate,sbiModule_config,
			execProperties,sbiModule_user,$http,sbiModule_dateServices, documentExecuteServices, sbiModule_download) {
		
		var dee = this;
		
		dee.getExportationUrl = function(format,paramsExportType,actionPath){
			console.log('sbiModule_config.contextName ' , sbiModule_config);
			var urlService = sbiModule_config.host+actionPath+'?';
			var sbiContext = 'SBICONTEXT='+sbiModule_config.contextName
			var docName = '&documentName='+execProperties.executionInstance.OBJECT_LABEL;
			var sbiExeRole = '&SBI_EXECUTION_ROLE='+execProperties.selectedRole.name;
			var country = '&SBI_COUNTRY='+sbiModule_config.curr_country;
			var idDocument = '&document='+ execProperties.executionInstance.OBJECT_ID;
			var language = '&SBI_LANGUAGE='+sbiModule_config.curr_language;
			var host = '&SBI_HOST='+sbiModule_config.host;
			var dateFormat = '&dateformat='+sbiModule_config.serverDateFormat;
			var controller ='&SBI_SPAGO_CONTROLLER='+sbiModule_config.adapterPathNoContext;
			var userID = '&user_id='+sbiModule_user.userId;
			var sbiExeId= '&SBI_EXECUTION_ID=' + execProperties.executionInstance.SBI_EXECUTION_ID;
			var isFromCross = '&isFromCross=false';
			var sbiEnv = '&SBI_ENVIRONMENT=DOCBROWSER';
			var outputType = '&outputType='+ format;				
			var paramsFilter='';
			if(execProperties.parametersData.documentParameters && execProperties.parametersData.documentParameters.length>0){
				var paramsArr = execProperties.parametersData.documentParameters;
				for(var i=0; i<paramsArr.length; i++){
					var currParam = paramsArr[i];
					if(currParam.parameterValue && currParam.parameterValue!=''){
						//date
						if(currParam.type=="DATE"){
							var dateParam = sbiModule_dateServices.formatDate(currParam.parameterValue, sbiModule_config.serverDateFormat);
							paramsFilter=paramsFilter+'&'+currParam.urlName+'='+dateParam;
						}else if(currParam.type=="DATE_RANGE"){
							var dateParam = sbiModule_dateServices.formatDate(currParam.parameterValue, sbiModule_config.serverDateFormat);
							if(paramsArr[i].datarange && paramsArr[i].datarange.opt){
								var rangeArr = paramsArr[i].datarange.opt.split('_');
								var rangeType = documentExecuteServices.getRangeCharacter(rangeArr[0]);
								var rangeQuantity = rangeArr[1];
								paramsFilter=paramsFilter+'&'+currParam.urlName+'='+dateParam+'_'+rangeQuantity+rangeType;
							}
						}
						else{
							if(!currParam.multivalue) {
								paramsFilter=paramsFilter+'&'+currParam.urlName+'='+currParam.parameterValue;
							}
							else {
								var multivalue = "{;{"+currParam.parameterValue.join([separator = ';'])+"}"+currParam.type+"}";
								paramsFilter=paramsFilter+'&'+currParam.urlName+'='+multivalue;
							}
						}
					}
				}
			}
			var exportationUrl =  sbiContext + docName + sbiExeRole + country + idDocument + language + host + dateFormat + controller + userID + sbiExeId + isFromCross + sbiEnv + outputType + paramsFilter + paramsExportType;
			var url = encodeURIComponent(exportationUrl).replace(/'/g,"%27").replace(/"/g,"%22").replace(/%3D/g,"=").replace(/%26/g,"&");
			return urlService + url;
		};
				
		dee.exportDocumentChart = function(exportType){
			var frame = window.frames["documentFrame"];
			frame.contentWindow.exportChart(exportType);
		};	

		dee.exportGeoTo = function (format, contentUrl) {	
			console.log('ENGINE LABEL : ' + execProperties.executionInstance.ENGINE_LABEL);
			if(execProperties.executionInstance.ENGINE_LABEL=='knowagegisengine'){
				//GIS
				var frame = window.frames["documentFrame"];
				frame.contentWindow.downlf();
			}else{
				//GEO (knowagegeoengine)
				var paramsExportType = '&ACTION_NAME=DRAW_MAP_ACTION&inline=false';
				window.open(dee.getExportationUrl(format,paramsExportType,'') , 'name', 'resizable=1,height=750,width=1000');
			}
		};


		dee.exportQbeTo = function(mimeType, contentUrl){
			var paramsExportType = '&ACTION_NAME=EXPORT_RESULT_ACTION&MIME_TYPE='+format+'&RESPONSE_TYPE=RESPONSE_TYPE_ATTACHMENT';
//			window.open(dee.getExportationUrl(format,paramsExportType,'knowageqbeengine/servlet/AdapterHTTP') , 'name', 'resizable=1,height=750,width=1000');
			window.open(dee.getExportationUrl(format,paramsExportType, sbiModule_config.qbeEngineContextName + '/servlet/AdapterHTTP') , 'name', 'resizable=1,height=750,width=1000');
		};
			
			
		dee.exportOlapTo= function (format, contentUrl) {
			var frame = window.frames["documentFrame"];
			frame.contentWindow.downlf(format);
		};
			

		dee.exportReportTo = function(format, contentUrl) {	
//			window.open(dee.getExportationUrl(format,'', '/knowagebirtreportengine/BirtReportServlet') , 'name', 'resizable=1,height=750,width=1000');
			window.open(dee.getExportationUrl(format,'', sbiModule_config.birtReportEngineContextName + '/BirtReportServlet') , 'name', 'resizable=1,height=750,width=1000');
		};
			
		dee.exportCockpitTo = function(exportType, mimeType){
			var data = {};
			var hostArr = sbiModule_config.host.split(":");
			data.username = sbiModule_user.userId;
			data.documentId = execProperties.executionInstance.OBJECT_ID;
			data.documentLabel = execProperties.executionInstance.OBJECT_LABEL;
			data.type= mimeType;
			data.port = hostArr[2];//8080
			data.ip=hostArr[1].replace("//" , ""); //localhost
			data.protocol= hostArr[0]; //http
			data.context=sbiModule_config.contextName.replace("/", ""); //sbiModule_config.contextName 'knowage'
			data.loginUrl= sbiModule_config.contextName;
			data.role= execProperties.selectedRole.name;
			
			// getting cockpit selections
			var documentFrame = document.getElementById("documentFrame"); // document iframe reference
			
			var csvData = null;
			if(exportType.toLowerCase() != 'pdf') {
				csvData = dee.getCockpitCsvData(documentFrame);
				
				if(csvData != null) {
					data.csvData = csvData;
				}
			}
			
			var cockpitSelectionsContainer = null;
			if(documentFrame.contentWindow && documentFrame.contentWindow.document) {
				cockpitSelectionsContainer = 
					documentFrame.contentWindow.document.getElementById("cockpitSelectionsContainer")
			}
			
			if ( cockpitSelectionsContainer ) {
				var cockpitSelections = cockpitSelectionsContainer.innerHTML;
				
				var testCockpitSelections = null;
				try {
					testCockpitSelections = JSON.parse(cockpitSelections);
				}
				catch(err) {
					cockpitSelections = '';
				}
				
				data.cockpitSelections = cockpitSelections;
			}
			
			var config={"responseType": "arraybuffer"};
			
			var requestUrl = sbiModule_config.host;
			
			if(exportType.toLowerCase() == 'xlsx') {
				requestUrl += '/highcharts-export-web/capture';
			} else {
				requestUrl += '/highcharts-export-web/capturepdf';
			}
			
			var requestConf = {
					method: 'POST',
					url: requestUrl,
					data: data,
//					config:config,
					responseType: 'arraybuffer',
					headers: {'Content-Type': 'application/x-www-form-urlencoded'},
					transformRequest: function(obj) {
						var str = [];
						for(var p in obj)
							str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
						return str.join("&");
					}
			};
			
			if(exportType.toLowerCase() != 'xlsx') {
				requestConf.transformResponse = function (data) {
					var blob;
					if (data) {
						blob = new Blob([data], {
							type: mimeType
						});
					}
					return blob;
				};
			}
			
			$http(requestConf)
			.then(function successCallback(response) {
				sbiModule_download.getBlob(
						response.data,
						execProperties.executionInstance.OBJECT_LABEL,
						mimeType,
						exportType);
				
			}, function errorCallback(response) {});			
		};	
			
		dee.exportationHandlers = {	
			'CHART': [
				 {'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportDocumentChart('PDF')} }
				 ,{'description' : sbiModule_translate.load('sbi.execution.JpgExport') , 'iconClass':'fa fa-file-image-o', 'func': function() {dee.exportDocumentChart('JPG')} }
			],
			'DOCUMENT_COMPOSITE': [
			    {'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportCockpitTo('pdf','application/pdf')} }
			    ,{'description' : sbiModule_translate.load('sbi.execution.XlsExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportCockpitTo('xls','application/vnd.ms-excel')} }
			    ,{'description' : sbiModule_translate.load('sbi.execution.XlsxExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportCockpitTo('xlsx','application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')} }
//			    {'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportCockpitTo('pdf')} }
//			    ,{'description' : sbiModule_translate.load('sbi.execution.XlsExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportCockpitTo('xls')} }
//			    ,{'description' : sbiModule_translate.load('sbi.execution.XlsxExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportCockpitTo('xlsx')} }
			],
			'REPORT': [
				{'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportReportTo('PDF')} }
				,{'description' : sbiModule_translate.load('sbi.execution.XlsExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportReportTo('XLS')} }
				,{'description' : sbiModule_translate.load('sbi.execution.XlsxExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportReportTo('XLSX')} }
				,{'description' : sbiModule_translate.load('sbi.execution.rtfExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportReportTo('RTF')} }
				,{'description' : sbiModule_translate.load('sbi.execution.docExport') , 'iconClass':'fa fa-file-word-o', 'func': function() {dee.exportReportTo('DOC')} }
				,{'description' : sbiModule_translate.load('sbi.execution.docxExport') , 'iconClass':'fa fa-file-word-o', 'func': function() {dee.exportReportTo('DOCX')} }
				,{'description' : sbiModule_translate.load('sbi.execution.CsvExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportReportTo('CSV')} }
				,{'description' : sbiModule_translate.load('sbi.execution.XmlExport') , 'iconClass':'fa fa-file-code-o', 'func': function() {dee.exportReportTo('XML')} }
				,{'description' : sbiModule_translate.load('sbi.execution.JpgExport') , 'iconClass':'fa fa-file-image-o', 'func': function() {dee.exportReportTo('JPG')} }
				,{'description' : sbiModule_translate.load('sbi.execution.txtExport') , 'iconClass':'fa fa-file-text-o', 'func': function() {dee.exportReportTo('TXT')} }
				,{'description' : sbiModule_translate.load('sbi.execution.pptExport') , 'iconClass':'fa fa-file-text-o', 'func': function() {dee.exportReportTo('PPT')} }
				,{'description' : sbiModule_translate.load('sbi.execution.pptxExport') , 'iconClass':'fa fa-file-text-o', 'func': function() {dee.exportReportTo('PPTX')} }
				],
			'OLAP': [
		         {'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportOlapTo('PDF')} }
		         ,{'description' : sbiModule_translate.load('sbi.execution.XlsExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportOlapTo('XLS')} }
		         ],
	        'DASH': [
	              	   {'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportChartTo('PDF')} }
	              	   ],
      	    'MAP': [
		        {'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportGeoTo('pdf')} }
		       // ,{'description' : sbiModule_translate.load('sbi.execution.JpgExport') , 'iconClass':'fa fa-file-image-o', 'func': function() {dee.exportGeoTo('jpeg')} }
			],
			'DATAMART': [
				{'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportQbeTo('application/pdf')} }
				,{'description' : sbiModule_translate.load('sbi.execution.XlsExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportQbeTo('application/vnd.ms-excel')} }
				,{'description' : sbiModule_translate.load('sbi.execution.XlsxExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportQbeTo('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')} }
				,{'description' : sbiModule_translate.load('sbi.execution.rtfExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportQbeTo('application/rtf')} }
				,{'description' : sbiModule_translate.load('sbi.execution.CsvExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportQbeTo('text/csv')} }
				,{'description' : sbiModule_translate.load('sbi.execution.jrxmlExport') , 'iconClass':'fa fa-file-text-o', 'func': function() {dee.exportQbeTo('text/jrxml')} }
				,{'description' : sbiModule_translate.load('sbi.execution.jsonExport') , 'iconClass':'fa fa-file-text-o', 'func': function() {dee.exportQbeTo('application/json')} }
			],
			'NETWORK': [
			            {'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportNetworkTo('pdf')} }
			            ,{'description' : sbiModule_translate.load('sbi.execution.PngExport') , 'iconClass':'fa fa-file-image-o', 'func': function() {dee.exportNetworkTo('png')} }
			            ,{'description' : sbiModule_translate.load('sbi.execution.GraphmlExport') , 'iconClass':'fa fa-file-image-o', 'func': function() {dee.exportNetworkTo('graphml')} }
			            ]
		};
		
		dee.getCockpitCsvData = function(documentFrame) {
			if ((documentFrame 
					&& documentFrame.contentWindow 
					&& documentFrame.contentWindow.Ext
					&& documentFrame.contentWindow.Ext.data
					&& documentFrame.contentWindow.Ext.data.StoreManager)
					&&(
							documentFrame.contentWindow.cockpitPanel
							&& documentFrame.contentWindow.cockpitPanel.widgetContainer
							&& documentFrame.contentWindow.cockpitPanel.widgetContainer.getWidgetManager
							)){
				
				// copied and adapted from "/knowage/web-content/js/src/ext/sbi/execution/toolbar/ExportersMenu.js"
				var csvData = [];
				var csvDataCount = 0;
			    var cockpitPanel = documentFrame.contentWindow.cockpitPanel;
			    var storeManager = documentFrame.contentWindow.Ext.data.StoreManager;
			    
			    var widgetManager = documentFrame.contentWindow.cockpitPanel.widgetContainer.getWidgetManager();
			    
//			    var sheets = cockpitPanel.config.lastSavedAnalysisState.widgetsConf;
//			    for (var i = 0; i < sheets.length; i++){
//			    	var widgets = sheets[i].sheetConf.widgets;
			    	var widgets = widgetManager.getWidgets();
			    	for (var j = 0; j < widgets.length; j++){
			    		var widget = widgets[j];
			    		if(widget.wtype == 'table'){
//			    			var store = storeManager.lookup(widget.storeId);
			    			var store = widget.getStore();
			    			
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
//			    }
			    
			    if(csvData){
			    	csvData = btoa(csvData);
			    }
			    
			    return csvData;
			} else {
				return null;
			}
		};
	});
})();