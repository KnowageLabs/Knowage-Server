(function() {
	var documentExecutionModule = angular.module('documentExecutionModule');
	
	documentExecutionModule.service('documentExecuteServices', function($mdToast,execProperties,sbiModule_restServices,sbiModule_config,$filter) {
		var documentExecuteServicesObj = {
//			decodeRequestStringToJson: function (str) {
//				var hash;
//				var parametersJson = {};
//				var hashes = str.slice(str.indexOf('?') + 1).split('&');
//				for (var i = 0; i < hashes.length; i++) {
//					hash = hashes[i].split('=');
//					parametersJson[hash[0]] = (/^\[.*\]$/).test(hash[1])?
//						JSON.parse(hash[1]) : hash[1] ;
//				}
//				return parametersJson;
//			},
			
				
			decodeRequestStringToJson: function (str) {
				var parametersJson = {};
				
				var arrParam = str.split('%26');
				for(var i=0; i<arrParam.length; i++){
					var arrJsonElement = arrParam[i].split('%3D');
					parametersJson[arrJsonElement[0]]=arrJsonElement[1];
				}
				return parametersJson;
			},
			
			showToast: function(text, time) {
				var timer = time == undefined ? 6000 : time;
				$mdToast.show($mdToast.simple().content(text).position('top').action(
				'OK').highlightAction(false).hideDelay(timer));
			},

			buildStringParameters : function (documentParameters) {
				console.log('buildStringParameters ' , documentParameters);
				
				var jsonDatum =  {};
				if(documentParameters.length > 0) {
					for(var i = 0; i < documentParameters.length; i++ ) {
						var parameter = documentParameters[i];
						var valueKey = parameter.urlName;
						var descriptionKey = parameter.urlName + "_field_visible_description";					
						var jsonDatumValue = null;
						var jsonDatumDesc = null;
						
						if(parameter.valueSelection.toLowerCase() == 'lov') {
							//TREE MODIFY (see with benedetto)
							if(parameter.selectionType.toLowerCase() == 'tree'){
//								if(parameter.multivalue) {
									var paramArrayTree = [];
									var paramStrTree = "";
									
									for(var z = 0; parameter.parameterValue && z < parameter.parameterValue.length; z++) {
										if(z > 0) {
											paramStrTree += ";";
										}
										
//										paramArrayTree[z] = parameter.parameterValue[z].value;
//										paramStrTree += parameter.parameterValue[z].value;
										paramArrayTree[z] = parameter.parameterValue[z];
										//old
										//paramStrTree += parameter.parameterValue[z];
										//modify description tree
										paramStrTree += parameter.parameterDescription[parameter.parameterValue[z]];
										
									}
									
									jsonDatumValue = paramArrayTree;
									jsonDatumDesc = paramStrTree;
//								} else {
//									jsonDatumValue = parameter.parameterValue? parameter.parameterValue.value : '';
//									jsonDatumDesc = parameter.parameterValue? parameter.parameterValue.value : '';
//								}
								
								
							} else {
//								parameter.parameterValue = parameter.parameterValue || [];
								if(			
										/*
										parameter.parameterValue 
										&& Array.isArray(parameter.parameterValue) 
										&& */
										parameter.multivalue) {
									
									parameter.parameterValue = parameter.parameterValue || [];
									
									jsonDatumValue = parameter.parameterValue;
									jsonDatumDesc = jsonDatumValue.join(";");
								} else {
//									jsonDatumValue = (typeof parameter.parameterValue === 'undefined')? '' : parameter.parameterValue;
									jsonDatumValue = parameter.parameterValue != undefined? parameter.parameterValue : '';
									jsonDatumDesc = jsonDatumValue;
								}
							}
						} else if(parameter.valueSelection.toLowerCase() == 'map_in'){
							if(parameter.parameterValue && parameter.multivalue) {
								parameter.parameterValue = parameter.parameterValue || [];
								
//								jsonDatumValue = parameter.parameterValue;
								jsonDatumValue = parameter.parameterValue.length > 0 ? 
										("'" + parameter.parameterValue.join("','") + "'") 
										: "";
								jsonDatumDesc = jsonDatumValue;
							} else {
								jsonDatumValue = (typeof parameter.parameterValue === 'undefined')? '' : parameter.parameterValue;
								jsonDatumDesc = jsonDatumValue;
							}
						} else {
							//DATE
							if(parameter.type=='DATE'){
								//TODO : parse method !!
								dateToSubmit = $filter('date')(parameter.parameterValue, 
										this.parseDateTemp(sbiModule_config.localizedDateFormat));
								console.log('date to sub ' + dateToSubmit);
								jsonDatumValue=dateToSubmit;
								jsonDatumDesc=dateToSubmit;							
							}else{
								jsonDatumValue = (typeof parameter.parameterValue === 'undefined')? '' : parameter.parameterValue;
								jsonDatumDesc = jsonDatumValue;
							}							
						}
						jsonDatum[valueKey] = jsonDatumValue;
						jsonDatum[descriptionKey] = jsonDatumDesc;
					}
				}		
				//console.log('jsonDAtum ' , jsonDatum);
				return jsonDatum;
			},

			
			parseDateTemp : function(date){
				result = "";
				if(date == "d/m/Y"){
					result = "dd/MM/yyyy";
				}
				if(date =="m/d/Y"){
					result = "MM/dd/yyyy"
				}
				return result;
			},
					
			recursiveChildrenChecks : function(parameterValue,parameterDescription,childrenArray) {
				childrenArray = childrenArray || [];
				for(var i = 0; i < childrenArray.length; i++) {
					var childItem = childrenArray[i];
					if(childItem.checked && childItem.checked == true) {
//						parameterValue.push(childItem);
						parameterValue.push(childItem.value);
						parameterDescription[childItem.value]=childItem.description;
					}
					
					if(!childItem.leaf) {
						documentExecuteServicesObj.recursiveChildrenChecks(parameterValue,parameterDescription,childItem.children);
					}
				}
			},
			
			resetParameterInnerLovData: function(childrenArray) { 
				childrenArray = childrenArray || [];
				
				for(var i = 0; i < childrenArray.length; i++) {
					var childItem = childrenArray[i];
					childItem.checked = false;
					
					if(!childItem.leaf) {
						documentExecuteServicesObj.resetParameterInnerLovData(childItem.children);
					}
				}
			},
			
			resetParameter: function(parameter) {
				if(parameter.valueSelection.toLowerCase() == 'lov') {
					if(parameter.selectionType.toLowerCase() == 'tree') {
						if(parameter.multivalue) {
							parameter.parameterValue = [];
							
							documentExecuteServicesObj.resetParameterInnerLovData(parameter.children);
						} else {
							parameter.parameterValue = '';
						}
					} else {
						if(parameter.multivalue) {
							parameter.parameterValue = [];
//							for(var j = 0; j < parameter.defaultValues.length; j++) {
//								var defaultValue = parameter.defaultValues[j];
//								defaultValue.isSelected = false;
//							}
						} else {
							parameter.parameterValue = '';
						}
					}
				} else {
					parameter.parameterValue = '';
				}
			},
						
//			showParameterHtml: function(parameter) {	
			setParameterValueResult: function(parameter) {	
				if(parameter.selectionType.toLowerCase() == 'tree') {
					if(parameter.multivalue) {
						var toReturn = '';
						
						parameter.parameterValue =  [];
						parameter.parameterDescription =  {};
						
						documentExecuteServicesObj.recursiveChildrenChecks(parameter.parameterValue,parameter.parameterDescription, parameter.children);
						
						for(var i = 0; i < parameter.parameterValue.length; i++) {
							var parameterValueItem = parameter.parameterValue[i];
							
							if(i > 0) {
								toReturn += ",<br/>";
							}
//							toReturn += parameterValueItem.value;
							toReturn += parameterValueItem;
						}
						
						return toReturn;
						
					} else {
						parameter.parameterValue = (parameter.parameterValue)?
								[parameter.parameterValue] : []
						parameter.parameterDescription = (parameter.parameterDescription)?
								parameter.parameterDescription : {}
								
						return (parameter.parameterValue && parameter.parameterValue.value)?
								parameter.parameterValue.value : '';
					}
				} else {
					if(parameter.multivalue) {
						parameter.parameterValue = parameter.parameterValue || [];
						var toReturn = parameter.parameterValue.join(",<br/>");
						return toReturn;
					} else {
						parameter.parameterValue = parameter.parameterValue || '';
						return parameter.parameterValue;
					}
				}
			}
		};
		
		return documentExecuteServicesObj;
	});
	
	documentExecutionModule.service('docExecute_pageviewService', function() {
			this.currentView ='DOCUMENT' ;				
			this.setCurrentView = function(currentView) {
				this.currentView = currentView;
			};
			this.getCurrentView = function() {
				return this.currentView;
			};
	});
	
	
	
	documentExecutionModule.service('docExecute_exportService', function(sbiModule_translate,sbiModule_config,
			execProperties,sbiModule_user,$http,sbiModule_dateServices) {
		
		var dee = this;
		
			dee.getExportationUrl= function(format,paramsExportType,actionPath){
				console.log('sbiModule_config.contextName ' , sbiModule_config);
				var urlService = sbiModule_config.host+actionPath+'?';
				var sbiContext = 'SBICONTEXT='+sbiModule_config.contextName
				var docName = '&documentName='+execProperties.executionInstance.OBJECT_LABEL;
				var sbiExeRole = '&SBI_EXECUTION_ROLE='+execProperties.selectedRole.name;
				var country = '&SBI_COUNTRY='+sbiModule_config.curr_country;
				var idDocument = '&document='+ execProperties.executionInstance.OBJECT_ID;
				var language = '&SBI_LANGUAGE='+sbiModule_config.curr_language;
				var host = '&SBI_HOST='+sbiModule_config.host;
				var dataFormat = '&dataformat='+sbiModule_config.serverDateFormat;
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
						if(paramsArr[i].parameterValue && paramsArr[i].parameterValue!=''){
							//date
							if(paramsArr[i].type=="DATE"){
								var dateParam = sbiModule_dateServices.formatDate(paramsArr[i].parameterValue, sbiModule_config.serverDateFormat);
								paramsFilter=paramsFilter+'&'+paramsArr[i].urlName+'='+dateParam;
							}else{
								paramsFilter=paramsFilter+'&'+paramsArr[i].urlName+'='+paramsArr[i].parameterValue;
								//paramsFilter=paramsFilter+'&'+paramsArr[i].urlName+'_description=';
							}
							
							
						}
					}
				}
				var exportationUrl =  sbiContext + docName + sbiExeRole + country + idDocument + language + host + dataFormat + controller + userID + sbiExeId + isFromCross + sbiEnv + outputType + paramsFilter + paramsExportType;
				var url = encodeURIComponent(exportationUrl).replace(/'/g,"%27").replace(/"/g,"%22").replace(/%3D/g,"=").replace(/%26/g,"&");
				return urlService + url;
			}
				
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
				window.open(dee.getExportationUrl(format,paramsExportType,'knowageqbeengine/servlet/AdapterHTTP') , 'name', 'resizable=1,height=750,width=1000');
				
				
			}
			
	//		dee.exportWorksheetsTo = function(mimeType, records){}
			
			
			
			dee.exportOlapTo= function (format, contentUrl) {
				var paramsExportType = '&cube=01';
				if (format == "PDF") paramsExportType = paramsExportType + '&type=1';
			    else if(format == "XLS") paramsExportType = paramsExportType + '&type=0';
				window.open(dee.getExportationUrl(format,paramsExportType,'knowagejpivotengine/Print') , 'name', 'resizable=1,height=750,width=1000');
			};
			
			
			dee.exportReportTo = function(format, contentUrl) {	
				window.open(dee.getExportationUrl(format,'','/knowagebirtreportengine/BirtReportServlet') , 'name', 'resizable=1,height=750,width=1000');
			};
			
			dee.exportCockpitTo = function(exportType){
				var data = {};
				var hostArr = sbiModule_config.host.split(":");
				data.username = sbiModule_user.userId;
				data.documentLabel = execProperties.executionInstance.OBJECT_LABEL;
				data.type= 'application/'+exportType;
				data.port = hostArr[2];//8080
				data.ip=hostArr[1].replace("//" , ""); //localhost
				data.protocol= hostArr[0]; //http
				data.context=sbiModule_config.contextName.replace("/", ""); //sbiModule_config.contextName 'knowage'
				data.loginUrl= sbiModule_config.contextName;
				data.role= execProperties.selectedRole.name;
				var config={"responseType": "arraybuffer"};
				$http({
					  method: 'POST',
					  url: sbiModule_config.host+'/highcharts-export-web/capture',
					  data: data,
					  //config:config,
					  headers: {'Content-Type': 'application/x-www-form-urlencoded'},
					  transformRequest: function(obj) {
					        var str = [];
					        for(var p in obj)
					        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
					        return str.join("&");
					    },
					}).then(function successCallback(response) {
						$scope.download.getBlob(data,execProperties.executionInstance.OBJECT_LABEL,'application/'+exportType,exportType);
					  }, function errorCallback(response) {
					    // called asynchronously if an error occurs
					    // or server returns response with an error status.
					  });			
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
			],
			'REPORT': [
				{'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportReportTo('PDF')} }
				,{'description' : sbiModule_translate.load('sbi.execution.XlsExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportReportTo('XLS')} }
				,{'description' : sbiModule_translate.load('sbi.execution.XlsxExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportReportTo('XLSX')} }
				,{'description' : sbiModule_translate.load('sbi.execution.rtfExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportReportTo('RTF')} }
				,{'description' : sbiModule_translate.load('sbi.execution.docExport') , 'iconClass':'fa fa-file-word-o', 'func': function() {dee.exportReportTo('DOC')} }
				,{'description' : sbiModule_translate.load('sbi.execution.CsvExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportReportTo('CSV')} }
				,{'description' : sbiModule_translate.load('sbi.execution.XmlExport') , 'iconClass':'fa fa-file-code-o', 'func': function() {dee.exportReportTo('XML')} }
				,{'description' : sbiModule_translate.load('sbi.execution.JpgExport') , 'iconClass':'fa fa-file-image-o', 'func': function() {dee.exportReportTo('JPG')} }
				,{'description' : sbiModule_translate.load('sbi.execution.txtExport') , 'iconClass':'fa fa-file-text-o', 'func': function() {dee.exportReportTo('TXT')} }
				,{'description' : sbiModule_translate.load('sbi.execution.pptExport') , 'iconClass':'fa fa-file-text-o', 'func': function() {dee.exportReportTo('PPT')} }
		],
		'OLAP': [
			{'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportOlapTo('PDF')} }
			,{'description' : sbiModule_translate.load('sbi.execution.XlsExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportOlapTo('XLS')} }
		],
		'DASH'	: [
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
		'WORKSHEET': [
			{'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportWorksheetsTo('application/pdf')} }
			,{'description' : sbiModule_translate.load('sbi.execution.XlsExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportWorksheetsTo('application/vnd.ms-excel')} }
			,{'description' : sbiModule_translate.load('sbi.execution.XlsxExport') , 'iconClass':'fa fa-file-excel-o', 'func': function() {dee.exportWorksheetsTo('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')} }
		],
		'NETWORK': [
		        	{'description' : sbiModule_translate.load('sbi.execution.PdfExport') , 'iconClass': 'fa fa-file-pdf-o', 'func': function(){dee.exportNetworkTo('pdf')} }
					,{'description' : sbiModule_translate.load('sbi.execution.PngExport') , 'iconClass':'fa fa-file-image-o', 'func': function() {dee.exportNetworkTo('png')} }
					,{'description' : sbiModule_translate.load('sbi.execution.GraphmlExport') , 'iconClass':'fa fa-file-image-o', 'func': function() {dee.exportNetworkTo('graphml')} }
		]	
	};
});
	
	
	
	
	
	
	
	
	documentExecutionModule.service('docExecute_urlViewPointService', function(execProperties,
			sbiModule_restServices, $mdDialog, sbiModule_translate,sbiModule_config,docExecute_exportService
			,$mdSidenav,docExecute_paramRolePanelService,documentExecuteServices,documentExecuteFactories,$q,$filter,$timeout
			,docExecute_dependencyService) {
		
		var serviceScope = this;	
		serviceScope.documentUrl = '';
		serviceScope.frameLoaded = true;
		serviceScope.exportation=[];
	
		
		
		serviceScope.executionProcesRestV1 = function(role, params) {			
			 params= typeof params === 'undefined' ? {} : params;
			
			var dataPost = {
					label: execProperties.executionInstance.OBJECT_LABEL,
					role:role,
					SBI_EXECUTION_ID:execProperties.executionInstance.SBI_EXECUTION_ID,
					parameters: params,
			}; 		
			sbiModule_restServices.alterContextPath( sbiModule_config.contextName);
			sbiModule_restServices.promisePost("1.0/documentexecution", 'url',dataPost)
			.then(
				function(response, status, headers, config) {					
					 var data=response.data;
					serviceScope.documentUrl = data.url+'&timereloadurl=' + new Date().getTime();
					//SETTING EXPORT BUTTON
					serviceScope.exportation = docExecute_exportService.exportationHandlers[data['typeCode']];
					execProperties.executionInstance.ENGINE_LABEL=data['engineLabel'];
					//SETTING URL SBI EXECUTION ID
					if(data['sbiExecutionId'] && data['sbiExecutionId'].length>0){
						execProperties.executionInstance.SBI_EXECUTION_ID=data['sbiExecutionId']; 
					}
					//execProperties.currentView.status = 'DOCUMENT';
				},
				function(response, status, headers, config) {
//					sbiModule_restServices.errorHandler(response.data,"Error while attempt to load filters")
//					.then(function(){
//						if(response.data.errors[0].type=="missingRole" || response.data.errors[0].category=="VALIDATION_ERROR"){
//							docExecute_paramRolePanelService.toggleParametersPanel(true);
//						}
//					}); 
					if(response.data.errors[0].type=="missingRole" || response.data.errors[0].category=="VALIDATION_ERROR"){
						docExecute_paramRolePanelService.toggleParametersPanel(true);
					}else{
						sbiModule_restServices.errorHandler(response.data,response.data.errors[0].message);
					}
					
					
					
					serviceScope.frameLoaded = true; 
				});
		};
		
		serviceScope.getViewpoints = function() {
			execProperties.currentView.status = 'PARAMETERS';
			execProperties.parameterView.status='FILTER_SAVED';
			execProperties.isParameterRolePanelDisabled.status = true;
			
			sbiModule_restServices.get(
					"1.0/documentviewpoint", 
					"getViewpoints",
					"label=" + execProperties.executionInstance.OBJECT_LABEL + "&role="+ execProperties.selectedRole.name)
			.success(function(data, status, headers, config) {	
				console.log('data viewpoints '  ,  data.viewpoints);
				serviceScope.gvpCtrlViewpoints = data.viewpoints;
//				execProperties.showParametersPanel.status = false;
				if($mdSidenav('parametersPanelSideNav').isOpen()) {
					docExecute_paramRolePanelService.toggleParametersPanel(false);
				}
			})
			.error(function(data, status, headers, config) {});																	
		};
		
		
		
		
		serviceScope.getSchedulers = function() {
			execProperties.currentView.status = 'PARAMETERS';
			execProperties.parameterView.status='SCHEDULER';
			sbiModule_restServices.get(
					"1.0/documentsnapshot", 
					"getSnapshots",
					"id=" + execProperties.executionInstance.OBJECT_ID)
			.success(function(data, status, headers, config) {	
				console.log('data scheduler '  ,  data.schedulers);
				serviceScope.gvpCtrlSchedulers = data.schedulers;
				console.log('url path ' + data.urlPath);
				serviceScope.snapshotUrlPath=data.urlPath;
				
				if($mdSidenav('parametersPanelSideNav').isOpen()) {
					docExecute_paramRolePanelService.toggleParametersPanel(false);
				}
			})
			.error(function(data, status, headers, config) {});																	
		};
		
		/*
		 * Fill Parameters Panel 
		 */
		serviceScope.fillParametersPanel = function(params){
				
			console.log('Load filter params : ' , params);
			if(execProperties.parametersData.documentParameters.length > 0){
				
				//var readyParams //-> su questi parametri è stato settato il valore (o non ho nessun valore da settarvi) 
				//var dependingOnParameters //-> lista dei parametri dai quali dipendono altri parametri
				//var savedParamtersToSet //-> lista che scorro finchè non vuota, ogni volta che riesco a settarne uno, lo levo dalla lista
				//riesco a settare una valore quando tutti i paramteri (se ce ne sono) da cui dipende sono presenti in readyParams
				
				for(var i = 0; i < execProperties.parametersData.documentParameters.length; i++){
					var parameter = execProperties.parametersData.documentParameters[i];
					
					if(!params[parameter.urlName]) {
						documentExecuteServices.resetParameter(parameter);
					} else {
						//console.log('parametro ' , parameter);
//						if(parameter.valueSelection=='lov' 
////							&& parameter.multivalue==true
//							&& parameter.selectionType.toLowerCase() == "tree"
//						) {
						if(parameter.valueSelection=='lov') {
							if(parameter.selectionType.toLowerCase() == "tree") {
								//TREE DESC FOR LABEL
								var ArrValue = JSON.parse(params[parameter.urlName]);
								var ArrDesc = params[parameter.urlName+'_field_visible_description'].split(';');
								if (typeof parameter.parameterDescription === 'undefined'){
									parameter.parameterDescription = {};
								}
								for(var w=0; w<ArrValue.length; w++){
									parameter.parameterDescription[ArrValue[w]] =ArrDesc[w];
								}
								parameter.parameterValue = ArrValue;
							} else {
								//console.log('setting param ' + params[parameter.urlName]);
								//console.log('setting param in ' , parameter);
								parameter.parameterValue = parameter.multivalue ? JSON.parse(params[parameter.urlName])	: params[parameter.urlName];
								//lookup
								if(parameter.selectionType.toLowerCase() == "lookup"){
									parameter.parameterValueToShow = [];
									parameter.parameterValueToShow = parameter.parameterValue;
								}
							
								
								
								
								
							}
							
						} else if(parameter.valueSelection.toLowerCase() == 'map_in') {
							var valueToBeCleanedByQuotes = params[parameter.urlName].replace(/^'(.*)'$/g, '$1');
							var valueToBeSplitted = valueToBeCleanedByQuotes.split("','");
							
							parameter.parameterValue = (parameter.multivalue)? valueToBeSplitted : valueToBeCleanedByQuotes;
						} else {
							if(parameter.type=='NUM'){
								parameter.parameterValue = parseFloat(params[parameter.urlName],10);
							}else if(parameter.type=='DATE'){
								//console.log('Date to fill ' , params[parameter.urlName]);
								parameter.parameterValue= new Date(params[parameter.urlName]);
							}else if(parameter.type=='STRING'){
								parameter.parameterValue = params[parameter.urlName];
								if(parameter.defaultValues && parameter.defaultValues.length > 0) {
									var parameterValues = parameter.parameterValue;
									var parArr = parameterValues.split(';');
									for(var j = 0; j < parameter.defaultValues.length; j++) {
										var defaultValue = parameter.defaultValues[j];
										for(var k = 0; k < parArr.length; k++) {
											if(defaultValue.value == parArr[k]) {
												defaultValue.isSelected = true;
												break;
											} else {
												defaultValue.isSelected = false;
											}											
										}
									}
								}
							}							
						}												
					}
					docExecute_dependencyService.visualCorrelationWatch(parameter);
					docExecute_dependencyService.dataDependenciesCorrelationWatch(parameter);
				}
			}
			
		};
		
	
		serviceScope.getParametersForExecution = function(role, buildCorrelation,crossParameters) {
			var params = {
					label:execProperties.executionInstance.OBJECT_LABEL,
					role:role,
					parameters:crossParameters
				};
			sbiModule_restServices.promisePost("1.0/documentexecution", "filters", params)
			.then(function(response, status, headers, config) {
				console.log('getParametersForExecution response OK -> ', response);
				//check if document has parameters 
				if(response && response.data.filterStatus && response.data.filterStatus.length>0) {					
								
					//build documentParameters
					angular.copy(response.data.filterStatus, execProperties.parametersData.documentParameters);
					
					//correlation
					buildCorrelation(execProperties.parametersData.documentParameters);
					
					//setting default value				
					serviceScope.buildObjForFillParameterPanel(response.data.filterStatus);
					// Enable visualcorrelation
					execProperties.initResetFunctionDependency.status=true;
					
					execProperties.isParameterRolePanelDisabled.status = docExecute_paramRolePanelService.checkParameterRolePanelDisabled();
				}else{
//					execProperties.showParametersPanel.status = false;
//						$mdSidenav('parametersPanelSideNav').close();
						docExecute_paramRolePanelService.toggleParametersPanel(false);
					
				} 
					
			},function(response, status, headers, config) {
				sbiModule_restServices.errorHandler(response.data,"error while attempt to load filters")   
			}); 
		};
		
		
		
		serviceScope.buildObjForFillParameterPanel = function(filterStatus){
			var fillObj = {};
			var hasDefVal = false;
			for(var i=0; i<filterStatus.length; i++){
				if(filterStatus[i].parameterValue && filterStatus[i].parameterValue.length>0){
					var arrDefToFill = [];
					var arrDefToFillDescription = []; //TREE
					//var fillObj = {};
					//MULTIVALUE
					hasDefVal= true;
					if(filterStatus[i].multivalue || filterStatus[i].selectionType=='TREE'){
						//if(filterStatus[i].defaultValues && filterStatus[i].defaultValues.length>0){
							//arrDefToFill=filterStatus[i].defaultValues;
						//}
						for(var k=0;k<filterStatus[i].parameterValue.length;k++){
							arrDefToFill.push(filterStatus[i].parameterValue[k].value);
							arrDefToFillDescription.push(filterStatus[i].parameterValue[k].description);
						}	
						fillObj[filterStatus[i].urlName] = JSON.stringify(arrDefToFill);
						//TREE
						if(filterStatus[i].selectionType=='TREE'){
							var strDefToFillDescription ='';
							for(var z=0; z<arrDefToFillDescription.length; z++){
								strDefToFillDescription=strDefToFillDescription+arrDefToFillDescription[z];
								if(z<arrDefToFill.length-1){
									strDefToFillDescription=strDefToFillDescription+';';
								}
							}
							fillObj[filterStatus[i].urlName+'_field_visible_description'] = strDefToFillDescription;
						}else{
							fillObj[filterStatus[i].urlName+'_field_visible_description'] = JSON.stringify(arrDefToFill);
						}
						
					}else{
						//SINGLE VALUE
						fillObj[filterStatus[i].urlName] = filterStatus[i].parameterValue[0].value;
						fillObj[filterStatus[i].urlName+'_field_visible_description'] =filterStatus[i].parameterValue[0].value;	
					}
					//serviceScope.fillParametersPanel(fillObj);
				}
			}
			if(hasDefVal){serviceScope.fillParametersPanel(fillObj);}
		}
		
		
		
		
		
		serviceScope.createNewViewpoint = function() {
			$mdDialog.show({
				//scope : serviceScope,
				preserveScope : true,				
				templateUrl : sbiModule_config.contextName + '/js/src/angular_1.4/tools/glossary/commons/templates/dialog-new-parameters-document-execution.html',
				controllerAs : 'vpCtrl',
				controller : function($mdDialog) {
					var vpctl = this;
					vpctl.headerTitle = sbiModule_translate.load("sbi.execution.executionpage.toolbar.saveas");
					vpctl.name = sbiModule_translate.load("sbi.execution.viewpoints.name");
					vpctl.description = sbiModule_translate.load("sbi.execution.viewpoints.description");
					vpctl.visibility = sbiModule_translate.load("sbi.execution.subobjects.visibility");
					vpctl.publicOpt = sbiModule_translate.load("sbi.execution.subobjects.visibility.public");
					vpctl.privateOpt = sbiModule_translate.load("sbi.execution.subobjects.visibility.private");
					vpctl.cancelOpt = sbiModule_translate.load("sbi.ds.wizard.cancel");
					vpctl.submitOpt = sbiModule_translate.load("sbi.generic.update");					
					vpctl.submit = function() {
						vpctl.newViewpoint.OBJECT_LABEL = execProperties.executionInstance.OBJECT_LABEL;
						vpctl.newViewpoint.ROLE = execProperties.selectedRole.name;
						//vpctl.newViewpoint.VIEWPOINT = documentExecuteServices.buildStringParametersForSave(execProperties.parametersData.documentParameters);
						vpctl.newViewpoint.VIEWPOINT = documentExecuteServices.buildStringParameters(execProperties.parametersData.documentParameters);
						sbiModule_restServices.post(
								"1.0/documentviewpoint",
								"addViewpoint", vpctl.newViewpoint)
						   .success(function(data, status, headers, config) {
							if(data.errors && data.errors.length > 0 ) {
								documentExecuteServices.showToast(data.errors[0].message);
							}else{
								$mdDialog.hide();
								documentExecuteServices.showToast(sbiModule_translate.load("sbi.execution.viewpoints.msg.saved"), 3000);
							}							
						})
						.error(function(data, status, headers, config) {
							documentExecuteServices.showToast(sbiModule_translate.load("sbi.execution.viewpoints.msg.error.save"),3000);	
						});
					};
					
					vpctl.annulla = function($event) {
						$mdDialog.hide();
						serviceScope.newViewpoint = JSON.parse(JSON.stringify(documentExecuteFactories.EmptyViewpoint));
					};
				},

				templateUrl : sbiModule_config.contextName 
					+ '/js/src/angular_1.4/tools/documentexecution/templates/dialog-new-parameters-document-execution.html'
			});
		};
		
		this.buildParameterForFirstExecution=function(navParam,menuParam){
			return angular.extend({},navParam,menuParam);
		}
	});
	
	documentExecutionModule.service('docExecute_paramRolePanelService', function(execProperties,$mdSidenav,$timeout) {
				
		this.checkParameterRolePanelDisabled = function() {
			return ((!execProperties.parametersData.documentParameters || execProperties.parametersData.documentParameters.length == 0)
					&& (execProperties.roles.length==1));		
		};
		
		this.returnToDocument = function() {
			execProperties.currentView.status = 'DOCUMENT';
			execProperties.parameterView.status='';
			execProperties.isParameterRolePanelDisabled.status = this.checkParameterRolePanelDisabled();
			execProperties.returnFromViewpoint.status = true;
		};
		
		this.isExecuteParameterDisabled = function() {
			if(execProperties.parametersData.documentParameters.length > 0) {
				for(var i = 0; i < execProperties.parametersData.documentParameters.length; i++ ) {
					if(execProperties.parametersData.documentParameters[i].mandatory 
							&& (!execProperties.parametersData.documentParameters[i].parameterValue
									|| execProperties.parametersData.documentParameters[i].parameterValue == '' )) {
						return true;
					}
				}
			}
			return false
		};
		
		this.toggleParametersPanel = function(open) {
			$timeout(function(){ 
				if(open==undefined){
					execProperties.showParametersPanel.status=!execProperties.showParametersPanel.status;
//					$mdSidenav('parametersPanelSideNav').toggle();
				}else if(open){
					execProperties.showParametersPanel.status=true;
//					$mdSidenav('parametersPanelSideNav').open();
				}else if(!open){
					execProperties.showParametersPanel.status=false;
//					$mdSidenav('parametersPanelSideNav').close();
				}
			},0);
			};
	});
	
	//DEPENDENCIES
	
	angular.module('documentExecutionModule')
	.service('docExecute_dependencyService', function(execProperties, documentExecuteServices,sbiModule_restServices) {
	
		var serviceScope = this;
	
	
		/*
		 * BUILD DATA DEPENDENCIES 
		 */
			this.buildDataDependenciesMap = function(parameters){
			console.log('parameters ' , parameters);
			for(var i=0; i<parameters.length ; i++){
				if(parameters[i].dataDependencies && parameters[i].dataDependencies.length>0){						
					for(var k=0; k<parameters[i].dataDependencies.length; k++){ 
						var dependency = parameters[i].dataDependencies[k];						
						dependency.parameterToChangeUrlName = parameters[i].urlName;
						dependency.parameterToChangeId = this.getRowIdfromUrlName(parameters[i].urlName); 
						dependency.lovParameterMode = parameters[i].selectionType;
						//build visualCorrelationMap : Key is fatherUrlName 
						var keyMap = dependency.objParFatherUrlName;
						if (keyMap in serviceScope.dataDependenciesMap) {
							var dependenciesArr =  serviceScope.dataDependenciesMap[keyMap];
							dependenciesArr.push(dependency);
							serviceScope.dataDependenciesMap[keyMap] = dependenciesArr;
							} else {
								var dependenciesArr = new Array
								dependenciesArr.push(dependency);
								serviceScope.dataDependenciesMap[keyMap] = dependenciesArr;
							}						
					}
				}
			}
			for (var key in serviceScope.dataDependenciesMap) {
				//Fill Array DATA DEPENDENCIES
				var documentParamDependence = execProperties.parametersData.documentParameters[this.getRowIdfromUrlName(key)];
				serviceScope.observableDataDependenciesArray.push(documentParamDependence);	
			}
			
			console.log('observableDataDependenciesArray ' , serviceScope.observableDataDependenciesArray);
		}
		
		
		
			 this.dataDependenciesCorrelationWatch = function(value){
				 
				console.log('modify dependency : ' , value);
				console.log('element key '+ value.urlName , serviceScope.dataDependenciesMap[value.urlName]);
				if(serviceScope.dataDependenciesMap[value.urlName]){
				for(var k=0; k<serviceScope.dataDependenciesMap[value.urlName].length; k++){
					var dataDependenciesElementMap = serviceScope.dataDependenciesMap[value.urlName][k];
					//objPost.MODE= (dataDependenciesElementMap.lovParameterMode!='TREE' ) ? 'simple' : 'complete';
					if(dataDependenciesElementMap.lovParameterMode!='TREE'){
						var objPost = {};
						objPost.OBJECT_LABEL = execProperties.executionInstance.OBJECT_LABEL;
						objPost.ROLE=execProperties.selectedRole.name;
						objPost.PARAMETER_ID=dataDependenciesElementMap.parameterToChangeUrlName;
						console.log('mode parameter type ' + dataDependenciesElementMap.lovParameterMode);
						objPost.MODE='simple';
						objPost.PARAMETERS=documentExecuteServices.buildStringParameters(execProperties.parametersData.documentParameters);
						//objPost.PARAMETERS=JSON.parse('{"param1":"","param1_field_visible_description":"","param2":["South West"],"param2_field_visible_description":"South West"}');
						sbiModule_restServices.post(
								"1.0/documentExeParameters",
								"getParameters", objPost)
						   .success(function(data, status, headers, config) {  
							  
							   
							   
							   if(data.status=="OK"){
								   //from root only visibled element !!! 
								   //set to disabled all default value parameter 
								   for(var z=0; z<execProperties.parametersData.documentParameters.length;z++){
										  if(execProperties.parametersData.documentParameters[z].urlName==data.idParam){
											  if(execProperties.parametersData.documentParameters[z].defaultValues &&
													  execProperties.parametersData.documentParameters[z].defaultValues.length>0){
												  for(var y=0;y<execProperties.parametersData.documentParameters[z].defaultValues.length;y++){
													  //execProperties.parametersData.documentParameters[z].parameterValue = [];
													  execProperties.parametersData.documentParameters[z].defaultValues[y].isEnabled=false; 
												  } 
											  }
											  break;
										  }
										  
									  }
								   //set to enabled the correct default value 
								   if(data.result.root && data.result.root.length>0){
									   for(var p=0; p<data.result.root.length;p++){   
										   //console.log("parameter ID : " + data.idParam + " set value " + data.result.root[p].value);
										   for(var z=0; z<execProperties.parametersData.documentParameters.length;z++){
											   if(execProperties.parametersData.documentParameters[z].urlName==data.idParam){
												   if(execProperties.parametersData.documentParameters[z].defaultValues &&
															  execProperties.parametersData.documentParameters[z].defaultValues.length>0){
													   for(var y=0;y<execProperties.parametersData.documentParameters[z].defaultValues.length;y++){
															if( execProperties.parametersData.documentParameters[z].defaultValues[y].value==data.result.root[p].value){
																//console.log("enabled for : " ,  execProperties.parametersData.documentParameters[z].defaultValues[y]);
																execProperties.parametersData.documentParameters[z].defaultValues[y].isEnabled=true;
															}	  
														   } 
												   }
												   break;
											   }
										   }	   
									   }
								   }else{
									   //if no element in root hide the row component
								   }  
								   // set parameter ready
								   
								   
							   }
						})
						.error(function(data, status, headers, config) {});
						//END REST CALL
					
					}else{
						console.log('IS TREE .... CLEAR PARAM ID ' + dataDependenciesElementMap.parameterToChangeUrlName);
						for(var z=0; z<execProperties.parametersData.documentParameters.length;z++){
							if(execProperties.parametersData.documentParameters[z].urlName==dataDependenciesElementMap.parameterToChangeUrlName){
								console.log('reset ... ' + execProperties.parametersData.documentParameters[z].urlName);
								execProperties.parametersData.documentParameters[z].children = [];
								documentExecuteServices.resetParameter(execProperties.parametersData.documentParameters[z]);
															
								break;
							}
						}
					}
				}	
			 }//check undefined	
		  }
		
		
		
		
		/*
		 * BUILD VISUAL DEPENDENCIES
		 */
		this.buildVisualCorrelationMap = function(parameters){
			
			for(var i=0; i<parameters.length ; i++){
				if(parameters[i].visualDependencies && parameters[i].visualDependencies.length>0){						
					for(var k=0; k<parameters[i].visualDependencies.length; k++){
						var dependency = parameters[i].visualDependencies[k];
						dependency.parameterToChangeUrlName = parameters[i].urlName;
						dependency.parameterToChangeId = this.getRowIdfromUrlName(parameters[i].urlName); 
						//build visualCorrelationMap : Key is fatherUrlName 
						var keyMap = dependency.objParFatherUrlName;
						if (keyMap in serviceScope.visualCorrelationMap) {
							var dependenciesArr =  serviceScope.visualCorrelationMap[keyMap];
							dependenciesArr.push(dependency);
							serviceScope.visualCorrelationMap[keyMap] = dependenciesArr;
							} else {
								var dependenciesArr = new Array
								dependenciesArr.push(dependency);
								serviceScope.visualCorrelationMap[keyMap] = dependenciesArr;
							}						
					}
				}
			}
			for (var key in serviceScope.visualCorrelationMap) {
				//Fill Array VISUAL DEPENDENCIES
				var documentParamVisualDependency = execProperties.parametersData.documentParameters[this.getRowIdfromUrlName(key)];
				serviceScope.observableVisualParameterArray.push(documentParamVisualDependency);	
			}
		}
	
		this.visualCorrelationWatch = function(value){
			//console.log('visual correlation : ' , value);
			if(serviceScope.visualCorrelationMap[value.urlName]){
				var forceExit=false;
				for(var k=0; k<serviceScope.visualCorrelationMap[value.urlName].length; k++){
					if(forceExit){
						break;
					}
					var visualDependency=serviceScope.visualCorrelationMap[value.urlName][k];
				    //id document Parameter to control 
					var idDocumentParameter = visualDependency.parameterToChangeId;
					//value to compare
					var compareValueArr = visualDependency.compareValue.split(",");
					for(var z=0; z<compareValueArr.length; z++){
						var newValueStr = value.parameterValue;
						var compareValueStr=compareValueArr[z].trim();
						//conditions
						var condition = false;
						if( Object.prototype.toString.call( newValueStr ) === '[object Array]' ) {
						   if(visualDependency.operation=='contains') {
							   for(var l=0; l<newValueStr.length; l++){
								   if(compareValueStr==newValueStr[l]){
									   condition=true;
									   break;
								   }
							   }
						   }
						   else { //not contains
							   condition=true; 
							   for(var l=0; l<newValueStr.length; l++){
								   if(compareValueStr==newValueStr[l]){
									   condition=false;
									   break;
								   }
							   }
						   }
						}else{
							condition = (visualDependency.operation=='contains') ? 
									(compareValueStr==newValueStr) : condition=(compareValueStr!=newValueStr);
						}
												
//						var condition = (visualDependency.operation=='contains') 
//							? (compareValueStr==newValueStr) : condition=(compareValueStr!=newValueStr); 
							
						if(condition){
							execProperties.parametersData.documentParameters[idDocumentParameter].label=visualDependency.viewLabel;
							execProperties.parametersData.documentParameters[idDocumentParameter].visible=true;
							//Exit if one conditions is verify
							/* BUG FIX LOAD DEFAULT AND VIEWPOIN PARAMS
							 No resetParameter for DEFAULT and Viewpoin  
							 */
							//console.log('reset for ' , execProperties.parametersData.documentParameters[idDocumentParameter]);
							if(execProperties.initResetFunctionDependency.status){
								documentExecuteServices.resetParameter(execProperties.parametersData.documentParameters[idDocumentParameter]);
							}
						
							forceExit = true;
							break;
						}else{
							execProperties.parametersData.documentParameters[idDocumentParameter].visible=false;
						}								
					}
				}
			}  
			
			//if return to viewpoin enable visual correlation 
			if(execProperties.returnFromViewpoint.status){
				execProperties.initResetFunctionDependency.status=true;
				execProperties.returnFromViewpoint.status = false;
			}
			
			
			
			
		  }
	
		//GET ROW ID FROM URL NAME
		this.getRowIdfromUrlName = function(urlName){
			var row=0;
			for(var i=0; i<execProperties.parametersData.documentParameters.length; i++ ){
				if(execProperties.parametersData.documentParameters[i].urlName == urlName){
					row = i;
					break;
				}
			}
			return row;
		}
	
	});
	
	
})();