(function() {
	var documentExecutionModule = angular.module('documentExecutionModule');

	documentExecutionModule.service('docExecute_exportService', function(sbiModule_translate,sbiModule_config,
			execProperties,sbiModule_user,sbiModule_restServices,$http,sbiModule_dateServices, documentExecuteServices, sbiModule_download, $q, $rootScope, sbiModule_messaging,multipartForm,$sce,$mdPanel,$mdToast) {

		var dee = this;

		dee.exporting = false;
		dee.isExporting = function(){
			return dee.exporting;
		}


		// for jasper substitute outputType to previous URL,
		// in order not to lose subreport infos that are inserted by driver
		dee.getJasperExportationUrl = function(format,paramsExportType,actionPath){
			var previousUrl = execProperties.documentUrl;
			var newUrl = previousUrl;

			var indexOutputType = previousUrl.search("outputType");
			if(indexOutputType>0){
				var indexAnd = previousUrl.indexOf("&",indexOutputType);
				var toSubstitute = previousUrl.substring(indexOutputType, indexAnd);
				var toSubstituteWith = "outputType="+format;
				newUrl = newUrl.replace(toSubstitute, toSubstituteWith);
			}
			else{
				newUrl+="&outputType="+format;
			}
			return sbiModule_config.host + newUrl;
		}

		dee.getExportationUrl = function(format,paramsExportType,actionPath){
			// https://production.eng.it/jira/browse/KNOWAGE-1443
			// actionPath has no '/' at the beginning
			var exportUrl;
			// if is to export a jasper report get url from
			if(actionPath.indexOf("knowagejasperreportengine") != -1 ){
				exportUrl = dee.getJasperExportationUrl(format,paramsExportType,actionPath);
			}
			else{
				// other kind of export

				if(!actionPath.startsWith('/')){
					actionPath = '/' + actionPath;
				}

				var urlService = null;
				if(actionPath.indexOf('?') >=0 ){
					urlService = sbiModule_config.host + actionPath+'&';
				}
				else{
					urlService = sbiModule_config.host + actionPath+'?';
				}

				var sbiContext = 'SBICONTEXT='+sbiModule_config.contextName
				var docName = '&documentName='+execProperties.executionInstance.OBJECT_LABEL;
				var sbiExeRole = '&SBI_EXECUTION_ROLE='+execProperties.selectedRole.name;
				var country = '&SBI_COUNTRY='+sbiModule_config.curr_country;
				var idDocument = '&document='+ execProperties.executionInstance.OBJECT_ID;
				var language = '&SBI_LANGUAGE='+sbiModule_config.curr_language;
				var host = '&SBI_HOST='+sbiModule_config.host;
				var dateFormat = '&dateformat='+sbiModule_config.serverDateFormat;
				var controller ='&SBI_SPAGO_CONTROLLER='+sbiModule_config.adapterPathNoContext;
				var userUniqueIdentifier = '&user_id='+sbiModule_user.userUniqueIdentifier;
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
				var exportationUrl =  sbiContext + docName + sbiExeRole + country + idDocument + language + host + dateFormat + controller + userUniqueIdentifier + sbiExeId + isFromCross + sbiEnv + outputType + paramsFilter + paramsExportType;
				var url = encodeURIComponent(exportationUrl).replace(/'/g,"%27").replace(/"/g,"%22").replace(/%3D/g,"=").replace(/%26/g,"&");
				exportUrl = urlService + url;
			}
			return exportUrl;
		};

		dee.exportDocumentChart = function(exportType,mimeType){
			dee.exporting = true;
			dee.getBackendRequestParams(exportType, mimeType).then(function(parameters){
				dee.buildBackendRequestConf(exportType, mimeType, parameters).then(function(requestConf){
					$http(requestConf)
					.then(function successCallback(response) {
						var mimeType = response.headers("Content-type");
						var fileAndExtension = response.headers("Content-Disposition")
						sbiModule_download.getBlob(
								response.data,
								execProperties.executionInstance.OBJECT_LABEL,
								mimeType,
								exportType, mimeType,fileAndExtension);
						dee.exporting = false;
					}, function errorCallback(response) {
						dee.exporting = false;
						sbiModule_messaging.showErrorMessage(response.errors[0].message, 'Error');
					});
				},function(e){
					dee.exporting = false;
					sbiModule_messaging.showErrorMessage(e, 'Error');
				});
			},function(e){
				dee.exporting = false;
				sbiModule_messaging.showErrorMessage(e, 'Error');
			});
		};

		dee.exportGeoTo = function (format, contentUrl) {
			console.log('ENGINE LABEL : ' + execProperties.executionInstance.ENGINE_LABEL);
			if(execProperties.executionInstance.ENGINE_LABEL=='knowagegisengine'){
				//GIS
				var frame = window.frames["documentFrame"];
				frame.downlf();
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
			frame.downlf(format);
		};


		dee.exportBirtReportTo = function(format, contentUrl) {
//			window.open(dee.getExportationUrl(format,'', '/knowagebirtreportengine/BirtReportServlet') , 'name', 'resizable=1,height=750,width=1000');
			window.open(dee.getExportationUrl(format,'', sbiModule_config.birtReportEngineContextName + '/BirtReportServlet') , 'name', 'resizable=1,height=750,width=1000');
		};
		dee.exportJasperReportTo = function(format, contentUrl) {
//			window.open(dee.getExportationUrl(format,'', '/knowagebirtreportengine/BirtReportServlet') , 'name', 'resizable=1,height=750,width=1000');
			window.open(dee.getExportationUrl(format,'', sbiModule_config.jasperReportEngineContextName + '/JasperReportServlet') , 'name', 'resizable=1,height=750,width=1000');
		};

		dee.exportCockpitTablesToAPDF = function($sce){
			var accessibleTables = document.getElementById("documentFrame").contentWindow.document.getElementsByTagName("accessible-angular-table");
			var finalHtml="";
			var jobId;

			for(var i=0;i<accessibleTables.length;i++){
				finalHtml += accessibleTables[i].innerHTML;
			}

			var formData = {};
			formData.file = finalHtml;
			formData.fileName = "table.html";
			formData.size = "4";


			multipartForm.post("2.0/exportAccessibleDocument/HTMLPDF/startconversion",formData).success(

					function(data,status,headers,config){
						if(data.hasOwnProperty("errors")){

							console.log("[UPLOAD]: DATA HAS ERRORS PROPERTY!");

						}else{


							sbiModule_download.getLink("/restful-services/2.0/exportAccessibleDocument/HTMLPDF/getResult/"+data)
						}
					}).error(function(data, status, headers, config) {
								console.log("[UPLOAD]: FAIL!"+status);
							});
		}

		dee.exportCockpitTablesToAMP3 = function($sce){
			var accessibleTables = document.getElementById("documentFrame").contentWindow.document.getElementsByTagName("accessible-angular-table");
			var finalHtml="";
			var jobId;

			for(var i=0;i<accessibleTables.length;i++){
				finalHtml += accessibleTables[i].innerHTML;
			}

			var formData = {};
			formData.file = finalHtml;
			formData.fileName = "table.html";

			var handleErrors = function(data, status, headers, config){
				sbiModule_restServices.errorHandler("Error while trying to convert file","Error")
				console.log("[UPLOAD]: FAIL!"+status);
				}

				var handleDownloadSuccess = function(data, status, headers, config){
					if(data.hasOwnProperty("errors")){
						handleErrors(data, status, headers, config);
					}else{
						sbiModule_download.getLink("/restful-services/2.0/exportAccessibleDocument/TXTMP3/getResult/"+data);
					}
				}

				var handleTxtSuccess = function(data, status, headers, config){
					if(data.hasOwnProperty("errors")){
						handleErrors(data, status, headers, config);
					}else{

						sbiModule_restServices.promiseGet("2.0/exportAccessibleDocument/HTMLTXT/getResult/"+data,"",null).
						then(function(data, status, headers, config){handleSuccess(data, status, headers, config)}),
						(function(data, status, headers, config){handleErrors(txtfile, status, headers, config)});

					}
				}

				var handleSuccess = function(data, status, headers, config){

					if(data.hasOwnProperty("errors")){
						handleErrors(data, status, headers, config);
					}else{
						var formData = {};
						formData.file = data.data;
						formData.fileName = "table.txt";
						formData.audiolanguage = sbiModule_config.curr_language+sbiModule_config.curr_country;
						formData.speedoptions = "-8";
						formData.formatoptions = "1";
						multipartForm.post("2.0/exportAccessibleDocument/TXTMP3/startconversion",formData).
						success(function(data, status, headers, config){handleDownloadSuccess(data, status, headers, config)}).
						error(function(data, status, headers, config){handleErrors(data, status, headers, config)});

					}
				}

			multipartForm.post("2.0/exportAccessibleDocument/HTMLTXT/startconversion",formData).
			success(function(data, status, headers, config){handleTxtSuccess(data, status, headers, config)}).
			error(function(data, status, headers, config){handleErrors(data, status, headers, config)});



		}


		dee.exportDocCompTo = function(exportType, mimeType){
			dee.exporting = true;

			window.open(dee.getExportationUrl(exportType,'',sbiModule_config.adapterPath+'?ACTION_NAME=EXPORT_DOCUMENT_COMPOSITION_PDF&inline=false') , 'name', 'resizable=1,height=750,width=1000');
		};


		dee.exportCockpitTo = function(exportType, mimeType){
			dee.exporting = true;

			dee.getBackendRequestParams(exportType, mimeType).then(function(parameters){
				dee.buildBackendRequestConf(exportType, mimeType, parameters)
				.then(function(requestConf){
					var exportingToast = sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.execution.executionpage.toolbar.export.exporting"), 'Success!', 0);
					$http(requestConf)
					.then(function successCallback(response) {
						var mimeType = response.headers("Content-type");
						var fileAndExtension = response.headers("Content-Disposition")
						$mdToast.hide(exportingToast);
						dee.exporting = false;
						sbiModule_download.getBlob(
								response.data,
								execProperties.executionInstance.OBJECT_LABEL,
								mimeType,
								exportType, mimeType,fileAndExtension);
					}, function errorCallback(response) {
						$mdToast.cancel(exportingToast);
						dee.exporting = false;
						sbiModule_messaging.showErrorMessage(response.errors[0].message, 'Error');
					});
				},function(e){
					dee.exporting = false;
					sbiModule_messaging.showErrorMessage(e, 'Error');
				});
			},function(e){
				dee.exporting = false;
				sbiModule_messaging.showErrorMessage(e, 'Error');
			});
		};

		dee.getBackendRequestParams = function(exportType, mimeType){
			var deferred = $q.defer();
			var eleToAtt=document.body;
			if(exportType.toLowerCase() == 'pdf'){
				var config = {
					attachTo: eleToAtt,
					locals :{deferred:deferred},
					controller: function($scope,mdPanelRef,sbiModule_translate,deferred,$mdDialog){
						$scope.translate = sbiModule_translate;

						var iframe = document.getElementById('documentFrame');
						var gridsterContainer = iframe.contentDocument.getElementById('gridsterContainer');
						var width = gridsterContainer.scrollWidth;
						var height = gridsterContainer.scrollHeight;
						var zoomFactor = 2.0;

						$scope.parameters = {
							pdfWidth: width,
							pdfHeight: height,
							pdfWaitTime: 30,
							pdfZoomFactor: zoomFactor,
							pdfPageOrientation: 'landscape',
							pdfFrontPage: true,
							pdfBackPage: true
						}

						$scope.closeDialog=function(){
							mdPanelRef.close();
							$scope.$destroy();
							deferred.reject();
						}

						$scope.exportPdf=function(){
							var parameters = {};
							angular.copy($scope.parameters, parameters);
							deferred.resolve(parameters);
							mdPanelRef.close();
							$scope.$destroy();
						}
					},
					disableParentScroll: true,
					templateUrl: sbiModule_config.contextName + '/js/src/angular_1.4/tools/documentexecution/templates/popupPdfExportParametersDialogTemplate.html',
					position: $mdPanel.newPanelPosition().absolute().center(),
					trapFocus: true,
//					zIndex: 150,
					fullscreen :false,
					clickOutsideToClose: true,
					escapeToClose: false,
					focusOnOpen: false,
					onRemoving :function(){}
				};
				$mdPanel.open(config);
			}else{
				deferred.resolve({});
			}

			return deferred.promise;
		};

		dee.buildRequestConf = function(exportType, mimeType){
			var deferred = $q.defer();
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
			if(exportType.toLowerCase() == 'xls' || exportType.toLowerCase() == 'xlsx') {
				dee.getCockpitCsvData(documentFrame).then(function(csvData){
					if(csvData != null) {
						data.csvData = csvData;
					}
					deferred.resolve(requestConf);
				},function(e){
					deferred.reject(e);
				});
			}else{
				deferred.resolve(requestConf);
			}
			return deferred.promise;
		};

		dee.buildBackendRequestConf = function(exportType, mimeType, parameters){
			var deferred = $q.defer();

			var eleToAtt=document.body;


			var requestUrl = sbiModule_config.host;
			requestUrl += execProperties.documentUrl;
			requestUrl += '&outputType=' + encodeURIComponent(exportType);

			for (var parameter in parameters) {
			    if (parameters.hasOwnProperty(parameter)) {
			    	requestUrl += '&' + parameter + '=' + encodeURIComponent(parameters[parameter]);
			    }
			}

			var requestConf = {
					method: 'GET',
					url: requestUrl,
					responseType: 'arraybuffer',
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
			if(exportType.toLowerCase() == 'xls' || exportType.toLowerCase() == 'xlsx') {
				dee.getCockpitCsvData(documentFrame).then(function(csvData){
					if(csvData != null) {
						data.csvData = csvData;
					}
					deferred.resolve(requestConf);
				},function(e){
					deferred.reject(e);
				});
			}else{
				deferred.resolve(requestConf);
			}
			return deferred.promise;
		};

//		dee.exportationHandlers = {
//			'CHART': [
//				 {'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportDocumentChart('PDF')} }
//				 ,{'description' : sbiModule_translate.load('sbi.execution.JpgExport') , 'iconClass':'fa fa-file-image-o', 'func': function() {dee.exportDocumentChart('JPG')} }
//			],
//			'DOCUMENT_COMPOSITE': [
//			    {'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportCockpitTo('pdf','application/pdf')} }
//			    ,{'description' : sbiModule_translate.load('sbi.execution.XlsExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportCockpitTo('xls','application/vnd.ms-excel')} }
//			    ,{'description' : sbiModule_translate.load('sbi.execution.XlsxExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportCockpitTo('xlsx','application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')} }
////			    {'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportCockpitTo('pdf')} }
////			    ,{'description' : sbiModule_translate.load('sbi.execution.XlsExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportCockpitTo('xls')} }
////			    ,{'description' : sbiModule_translate.load('sbi.execution.XlsxExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportCockpitTo('xlsx')} }
//			],
//			'REPORT': [
//				{'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportReportTo('PDF')} }
//				,{'description' : sbiModule_translate.load('sbi.execution.XlsExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportReportTo('XLS')} }
//				,{'description' : sbiModule_translate.load('sbi.execution.XlsxExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportReportTo('XLSX')} }
//				,{'description' : sbiModule_translate.load('sbi.execution.rtfExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportReportTo('RTF')} }
//				,{'description' : sbiModule_translate.load('sbi.execution.docExport') , 'iconClass':'fa fa-file-word-o', 'func': function() {dee.exportReportTo('DOC')} }
//				,{'description' : sbiModule_translate.load('sbi.execution.docxExport') , 'iconClass':'fa fa-file-word-o', 'func': function() {dee.exportReportTo('DOCX')} }
//				,{'description' : sbiModule_translate.load('sbi.execution.CsvExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportReportTo('CSV')} }
//				,{'description' : sbiModule_translate.load('sbi.execution.XmlExport') , 'iconClass':'fa fa-file-code-o', 'func': function() {dee.exportReportTo('XML')} }
//				,{'description' : sbiModule_translate.load('sbi.execution.JpgExport') , 'iconClass':'fa fa-file-image-o', 'func': function() {dee.exportReportTo('JPG')} }
//				,{'description' : sbiModule_translate.load('sbi.execution.txtExport') , 'iconClass':'fa fa-file-text-o', 'func': function() {dee.exportReportTo('TXT')} }
//				,{'description' : sbiModule_translate.load('sbi.execution.pptExport') , 'iconClass':'fa fa-file-text-o', 'func': function() {dee.exportReportTo('PPT')} }
//				,{'description' : sbiModule_translate.load('sbi.execution.pptxExport') , 'iconClass':'fa fa-file-text-o', 'func': function() {dee.exportReportTo('PPTX')} }
//				],
//			'OLAP': [
//		         {'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportOlapTo('PDF')} }
//		         ,{'description' : sbiModule_translate.load('sbi.execution.XlsExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportOlapTo('XLS')} }
//		         ],
//	        'DASH': [
//	              	   {'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportChartTo('PDF')} }
//	              	   ],
//      	    'MAP': [
//		        {'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportGeoTo('pdf')} }
//		       // ,{'description' : sbiModule_translate.load('sbi.execution.JpgExport') , 'iconClass':'fa fa-file-image-o', 'func': function() {dee.exportGeoTo('jpeg')} }
//			],
//			'DATAMART': [
//				{'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportQbeTo('application/pdf')} }
//				,{'description' : sbiModule_translate.load('sbi.execution.XlsExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportQbeTo('application/vnd.ms-excel')} }
//				,{'description' : sbiModule_translate.load('sbi.execution.XlsxExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportQbeTo('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')} }
//				,{'description' : sbiModule_translate.load('sbi.execution.rtfExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportQbeTo('application/rtf')} }
//				,{'description' : sbiModule_translate.load('sbi.execution.CsvExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportQbeTo('text/csv')} }
//				,{'description' : sbiModule_translate.load('sbi.execution.jrxmlExport') , 'iconClass':'fa fa-file-text-o', 'func': function() {dee.exportQbeTo('text/jrxml')} }
//				,{'description' : sbiModule_translate.load('sbi.execution.jsonExport') , 'iconClass':'fa fa-file-text-o', 'func': function() {dee.exportQbeTo('application/json')} }
//			],
//			'NETWORK': [
//			            {'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportNetworkTo('pdf')} }
//			            ,{'description' : sbiModule_translate.load('sbi.execution.PngExport') , 'iconClass':'fa fa-file-image-o', 'func': function() {dee.exportNetworkTo('png')} }
//			            ,{'description' : sbiModule_translate.load('sbi.execution.GraphmlExport') , 'iconClass':'fa fa-file-image-o', 'func': function() {dee.exportNetworkTo('graphml')} }
//			            ]
//		};



		dee.getExporters = function(engine, type) {
			 return $q(function(resolve, reject) {
				var exportationHandlers = {};

				sbiModule_restServices.promiseGet('2.0/exporters',engine)
				.then(function(response) {
					console.log("[GET]: SUCCESS!");
					exportationHandlers = response.data.exporters;
					//define the final data version
					var exportersJSON = [];
					for (e in exportationHandlers){
						var exp = exportationHandlers[e];
						var expJSON = {};
						dee.setExporterDescription(exp.name, expJSON);
						dee.setExporterIconClass(exp.name, expJSON);
						dee.setExporterFunc(exp.name, exp.engineType, exp.engineDriver, expJSON);
						exportersJSON.push(expJSON);
					}
					resolve(exportersJSON);
				},function(e){
					reject(e);
				});

			});
		}

		dee.setExporterDescription = function(type, expObj) {
			expObj.description =  sbiModule_translate.load('sbi.execution.' + type + 'Export');
		}

		dee.setExporterIconClass = function(type, expObj) {
			var iconClass = "fa fa-file-";

			switch (type) {
			case "PDF":

			case "APDF":
				iconClass += "pdf";
				break;
			case "AMP3":
				iconClass +="audio";
				break;
			case "XLS":
			case "XLSX":
			case "RTF":
			case "CSV":
				iconClass += "excel";
				break;
			case "JPG":
			case "PNG":
			case "GRAPHML":
				iconClass += "image";
				break;
			case "DOC":
			case "DOCX":
				iconClass += "word";
				break;
			case "XML":
				iconClass += "code";
				break;
			case "TXT":
			case "PPT":
			case "PPTX":
			case "JRXML":
			case "JSON":
				iconClass += "text";
				break;
			default:

			}
			iconClass += "-o";

			expObj.iconClass = iconClass;
		}

		dee.setExporterFunc = function(type, engineType, engineDriver, expObj) {

			switch (engineType) {
			case "CHART":
				expObj.func = function(){
					dee.exportDocumentChart(type)
				};
				break;
			case "DOCUMENT_COMPOSITE":
				switch (type) {
				case "XLS":
					expObj.func = function(){
					if(engineDriver!=""){
						dee.exportCockpitTo('xls','application/vnd.ms-excel');
					}
					else{
						dee.exportDocCompTo('xls','application/vnd.ms-excel');
					}
				};
				break;
				case "XLSX":
					expObj.func = function(){
					if(engineDriver!=""){
						dee.exportCockpitTo('xlsx','application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')
					}
					else{
						dee.exportDocCompTo('xlsx','application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')
					}
				};
				break;
				case "PDF":
					expObj.func = function(){
					if(engineDriver!=""){
						dee.exportCockpitTo('pdf','application/pdf')}
					else{
						dee.exportDocCompTo('pdf','application/pdf')};

				};
				break;
				case "JPG":
					expObj.func = function(){
					if(engineDriver!=""){
						dee.exportCockpitTo('JPG')}
					else{
						dee.exportDocCompTo('JPG')};
				};

					break;
				case "APDF":
					expObj.func = function(){dee.exportCockpitTablesToAPDF()};
					break;
				case "AMP3":
					expObj.func = function(){dee.exportCockpitTablesToAMP3()};
					break;

				default:

				}
				break;
			case "REPORT":
				expObj.func = function(){
					if(engineDriver.indexOf("Jasper")>=0){
						dee.exportJasperReportTo(type);
					}
					else{
						dee.exportBirtReportTo(type);
					}
				};
				break;
			case "OLAP":
				expObj.func = function(){dee.exportOlapTo(type)};
				break;
			case "DASH":
				expObj.func = function(){dee.exportChartTo(type)};
				break;
			case "MAP":
				expObj.func = function(){dee.exportGeoTo(type.toLowerCase())};
				break;
			case "DATAMART":
				switch (type) {
				case "XLS":
					expObj.func = function(){dee.exportQbeTo('application/vnd.ms-excel')};
					break;
				case "XLSX":
					expObj.func = function(){dee.exportQbeTo('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')};
					break;
				case "PDF":
					expObj.func = function(){dee.exportQbeTo('application/pdf')};
					break;
				case "RTF":
					expObj.func = function(){dee.exportQbeTo('application/rtf')};
					break;
				case "CSV":
					expObj.func = function(){dee.exportQbeTo('text/csv')};
					break;
				case "JRXML":
					expObj.func = function(){dee.exportQbeTo('text/jrxml')};
					break;
				case "JSON":
					expObj.func = function(){dee.exportQbeTo('application/json')};
					break;
				default:

				}
				break;
			case "NETWORK":
				expObj.func = function(){dee.exportNetworkTo( type.toLowerCase())};
				break;
			default:

			}
		}

		dee.getCockpitCsvData = function(documentFrame) {
			var deferred=$q.defer();
			if (documentFrame
				&& documentFrame.contentWindow
				&& documentFrame.contentWindow.angular){

				// copied and adapted from "/knowage/web-content/js/src/ext/sbi/execution/toolbar/ExportersMenu.js"
				// S.Lupo 06/oct/2016 - modified to work with angular cockpit
				var def=$q.defer();
				documentFrame.contentWindow.angular.element(document).find('iframe').contents().find('body').scope().exportCsv(def)
				.then(function(csvData){
					if(csvData){
				    	csvData = btoa(csvData);
				    }
				    deferred.resolve(csvData);
				},function(e){
					deferred.reject(e);
				});
			} else {
				deferred.resolve(null);
			}
			return deferred.promise;
		};
	});
})();