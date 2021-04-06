angular.module('cross_navigation', ['ngMaterial','bread_crumb','angular_table'])
.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}])
.service('$crossNavigationHelper',
		function($crossNavigationSteps,$rootScope, $window, sbiModule_restServices,sbiModule_config,$mdDialog,sbiModule_translate,sbiModule_i18n,sbiModule_dateServices,$filter,sbiModule_logger){
		var cns=this;
		var selectedRole={};
		this.crossNavigationSteps=$crossNavigationSteps;

		this.changeNavigationRole=function(newRole){
			selectedRole=newRole;
		};

		//chartType,documentName, documentParameters, categoryName, categoryValue, serieName, serieValue, groupingCategoryName, groupingCategoryValue, stringParameters
		this.navigateTo=function(outputParameter,inputParameter,targetDocument,docLabel,otherOutputParameters){

			sbiModule_restServices.promiseGet("1.0/crossNavigation",this.crossNavigationSteps.currentDocument.label+"/loadCrossNavigationByDocument")
			.then(function(response){
				var navObj=response.data;
				var targetUrl="";
				if(navObj.length==0){

					sbiModule_logger.log("No cross navigations available from this document for the current user");
					$mdDialog.show(
							  $mdDialog
							    .alert({
							        title: sbiModule_translate.load("sbi.generic.warning"),
							        textContent: sbiModule_translate.load("sbi.crossnavigation.runtime.noTargetsAvailable"),
							        ok: sbiModule_translate.load("sbi.general.close")
							      })
							);
					return;
				}

				var targetDocumentJSON = null;
				try{
					targetDocumentJSON = JSON.parse(targetDocument);
					if (targetDocumentJSON.length == 0) {
						//the starter engine doesn't set specific cross navigation: all must be considered
						targetDocumentJSON = undefined;
						targetDocument = undefined;
					}
				}catch (e){
					//isn't a JSON object but it's a simple string with the label of the cross to use
					targetDocumentJSON = undefined;
				}

				if (targetDocumentJSON && Array.isArray(targetDocumentJSON)){
					//makes a sublist of objects if its required from the starter engine
					var navObjTemp = [];
					for (var n in navObj){
						if (objectIsRequired(targetDocumentJSON, navObj[n].crossName))
							navObjTemp.push(navObj[n]);  //add only the required cross
					}
					navObj = navObjTemp; //update final object
				}else{
					if (targetDocument && !isCrossNavigationAvailable(targetDocument, navObj)){
						//show dialog with error for wrong configuration
						$mdDialog.show(
								  $mdDialog
								    .alert({
								        title: sbiModule_translate.load("sbi.generic.warning"),
								        textContent: sbiModule_translate.load("sbi.crossnavigation.runtime.targetIsNotAvailable"),
								        ok: sbiModule_translate.load("sbi.general.close")
								      })
								);
						return;
					}
				}
				console.log("outputParameter: ", outputParameter);
				console.log("inputParameter: ", inputParameter);
				console.log("otherOutputParameters: ", otherOutputParameters);

				for (var n = 0; n < navObj.length; n++){
					if (navObj[n].crossText){
						//replace parameters placeholders if exist in navigation popup selection
						var paramsRegex = /\$P\{([a-zA-Z0-9\_\-]*)}/g;
						navObj[n].crossText = navObj[n].crossText.replace(paramsRegex,function(match,p1){
							if (Array.isArray(outputParameter)){
								for (o in outputParameter){
									for(var name in outputParameter[o]){
										if (name == p1)
											return outputParameter[o][name] || '';
									}
								}
							}else if(outputParameter[p1]) return outputParameter[p1];

							if (Array.isArray(inputParameter)){
								for (o in inputParameter){
									for(var name in inputParameter[o]){
										if (inputParameter[o].label == p1)
											return inputParameter[o].parameterValue || '';
									}
								}
							}else if(inputParameter[p1]) return inputParameter[p1];

							if (Array.isArray(otherOutputParameters)){
								for (o in otherOutputParameters){
									for(var name in otherOutputParameters[o]){
										if (name == p1)
											return otherOutputParameters[o][name] || '';
									}
								}
								return ""; //clean placeholder
							}else if(otherOutputParameters[p1]) return otherOutputParameters[p1];
							return match;
						});
					}else{
						navObj[n].crossText = navObj[n].crossName; //default: force the crossName
					}
					navObj[n].crossText = sbiModule_i18n.getI18n(navObj[n].crossText);

					if (navObj[n].crossBreadcrumb){
						//replace parameters placeholders if exist in breadcrumbs
						var paramsRegex = /\$P\{([a-zA-Z0-9\_\-]*)}/g;
						navObj[n].crossBreadcrumb = navObj[n].crossBreadcrumb.replace(paramsRegex,function(match,p1){
							if (Array.isArray(outputParameter)){
								for (o in outputParameter){
									for(var name in outputParameter[o]){
										if (name == p1)
											return outputParameter[o][name] || '';
									}
								}
							}else if(outputParameter[p1]) return outputParameter[p1];

							if (Array.isArray(inputParameter)){
								for (o in inputParameter){
									for(var name in inputParameter[o]){
										if (inputParameter[o].label == p1)
											return inputParameter[o].parameterValue || '';
									}
								}
							}else if(inputParameter[p1]) return inputParameter[p1];

							if (Array.isArray(otherOutputParameters)){
								for (o in otherOutputParameters){
									for(var name in otherOutputParameters[o]){
										if (name == p1)
											return otherOutputParameters[o][name] || '';
									}
								}
								return ""; //clean placeholder
							}else if(otherOutputParameters[p1]) return otherOutputParameters[p1];
							return match;
						});
					}else{
						navObj[n].crossBreadcrumb = navObj[n].document.name; //default: force the docName as old management
					}
				}



				if(navObj.length==1){
					execCross(navObj[0],outputParameter,inputParameter,true, otherOutputParameters);
				}
				else if(navObj.length>1){
					if(targetDocument!=undefined){
						for(var i=0;i<navObj.length;i++){
							if(angular.equals(navObj[i].crossName,targetDocument)){
								execCross(navObj[i],outputParameter,inputParameter,true,otherOutputParameters);
								return;
							}
						}
					}

					$mdDialog.show({
					      controller: function($scope,documents,translate,$mdDialog){
					    	  $scope.translate=translate;
					    	  $scope.documents=documents;
					    	  $scope.cancel=function(){
					    		  $mdDialog.cancel();
					    	  };
					    	  $scope.selectDocument=function(item){
					    		  $mdDialog.hide(item);
					    	  }

					    	  },
					      template: '<md-dialog aria-label="Select document" layout="column" ng-cloak style="max-width: 800px;">'
					    	  +'<md-toolbar>'
					    	  +'	<div class="md-toolbar-tools">'
					    	  +'		<h2>{{translate.load("Seleziona il documento di destinazione")}}</h2>'
					    	  +'	</div>'
					    	  +'</md-toolbar>'
					    	  +'<md-dialog-content>'
//					    	  +'	<angular-table flex id="selectDoctableCross" no-pagination="true" ng-model=documents columns="[{label:\'Label\',name:\'document.label\'},{label:\'Name\',name:\'document.name\'},{label:\'Description\',name:\'document.description\'}]" click-function="selectDocument(item);"></angular-table>'
					    	  +'	<md-list class="md-dense" flex id="selectDoctableCross"  ng-model=documents >'
				              +'	  <md-list-item class="md-2-line" ng-repeat="item in documents" ng-click="selectDocument(item);">'
			                  +'  		<div class="md-list-item-text">{{(item.crossText) ? item.crossText : item.document.name}}</div>'
			                  +'	  </md-list-item>'
			                  +'	</md-list>'
					    	  +'</md-dialog-content>'
					    	  +'<md-dialog-actions layout="row">'
					    	  +'	<span flex></span>'
					    	  +'	<md-button ng-click="cancel()">Cancel</md-button>'
					    	  +'</md-dialog-actions>'
					    	  +'</md-dialog>',
					      clickOutsideToClose:false,
					      locals:{documents:navObj,
					    	  	  translate:sbiModule_translate}
					    })
					    .then(function(doc) {
					    	execCross(doc,outputParameter,inputParameter,true,otherOutputParameters);
					    }, function() {
					     return;
					    });
				}
			},function(response){
				sbiModule_restServices.errorHandler(response.data, "Errors while attempt to open target document")
			})

		};


		function isCrossNavigationAvailable(crossNavigationName, availableCrossNavigationsArray) {
			for (var t in availableCrossNavigationsArray){
				if (availableCrossNavigationsArray[t].crossName.trim() == crossNavigationName.trim())
					return true;
			}
			return false;
		}

		function objectIsRequired(targetDocs, crossName){
			for (var t in targetDocs){
				if (targetDocs[t].trim() == crossName.trim())
					return true;
			}
			return false;
		}

		function execCross(doc,outputParameter,inputParameter,externalCross, otherOutputParameters){
			var parameterStr="";
			if(externalCross){
				parameterStr=cns.responseToStringParameter(doc,outputParameter,inputParameter,otherOutputParameters);
			}else{
				parameterStr=jsonToURI(outputParameter);
			}
			targetUrl= sbiModule_config.contextName
			+ '/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/documentexecution/documentExecutionNg.jsp'
			+ '&OBJECT_ID=' + doc.document.id
			+ '&OBJECT_LABEL=' + encodeURIComponent(doc.document.label)
			+ '&SELECTED_ROLE=' + encodeURIComponent(selectedRole.name)
			+ '&SBI_EXECUTION_ID=null'
			+ '&OBJECT_NAME=' + encodeURIComponent(doc.document.name)
			+"&CROSS_PARAMETER="+parameterStr;
			if (doc.crossType == 1){
				//open cross navigation inside dialog (a.k.a pop-up)
				targetUrl = targetUrl + "&TOOLBAR_VISIBLE=false";
				documentName = doc.document.name;
				$mdDialog.show({
				      controller: function($scope,translate,$mdDialog){
				    	  $scope.translate=translate;
				    	  $scope.cancel=function(){
				    		  $mdDialog.cancel();
				    	  };

				      },
				      template: '<md-dialog aria-label="'+documentName+'" layout="column" ng-cloak flex="85">'
				    	  +'<md-toolbar>'
				    	  +'	<div class="md-toolbar-tools">'
				    	  +'		<h2>'+ documentName +'</h2>'
				          +'<span flex></span>'
				          +'		<md-button class="md-icon-button" ng-click="cancel()">'
				          +'			<md-icon md-font-icon="fa fa-close"></md-icon>'
				          +'		</md-button>'
				    	  +'	</div>'
				    	  +'</md-toolbar>'
				    	  +'<md-dialog-content>'
				    	  +'	<iframe frameBorder="0" class="crossNavigationDialogIframe" src='+ targetUrl +'></iframe>'
				    	  +'</md-dialog-content>'
				    	  +'</md-dialog>',
				      clickOutsideToClose:true,
				      locals:{ translate:sbiModule_translate, targetUrl:targetUrl, documentName: documentName },
				      fullscreen: false
				    })
			} else if(doc.crossType == 2) {
				var jsonPopupOptions = JSON.parse(doc.popupOptions);
				$window.open(targetUrl, '_blank', 'toolbar=0,status=0,menubar=0,width='+ (jsonPopupOptions.width || 800) +',height='+ (jsonPopupOptions.height || 600));
			} else {
				// normal cross navigation
//				cns.crossNavigationSteps.stepControl.insertBread({name:doc.document.name,label:doc.document.label,id:doc.document.id,url:targetUrl});
				cns.crossNavigationSteps.stepControl.insertBread({name:doc.crossBreadcrumb,label:doc.document.label,id:doc.document.id,url:targetUrl});
			}
		};

		this.responseToStringParameter=function(navObj,outputParameter,inputParameter, otherOutputParameters){
			var respStr={};

			//check for output parameters
			if(angular.isArray(outputParameter)){
				respStr={};
				for(var dataKey in outputParameter){
					for(var key in navObj.navigationParams){
						var parVal=navObj.navigationParams[key];
						if(parVal.value.isInput==false && outputParameter[dataKey].hasOwnProperty(parVal.value.label) && outputParameter[dataKey][parVal.value.label]!=undefined && outputParameter[dataKey][parVal.value.label]!=null){
							if(!respStr.hasOwnProperty(key)){
								respStr[key]=[];
							}

							respStr[key].push(parseParameterValue(parVal.value,outputParameter[dataKey][parVal.value.label]));
						}
					}
				}


			}else{
				for(var key in navObj.navigationParams){
					var parVal=navObj.navigationParams[key];
					if(parVal.value.isInput==false &&  outputParameter.hasOwnProperty(parVal.value.label) && outputParameter[parVal.value.label]!=undefined && outputParameter[parVal.value.label]!=null){
						respStr[key]=parseParameterValue(parVal.value,outputParameter[parVal.value.label]);
					}
				}
			}

			//check for input parameters
			if(inputParameter!=undefined && navObj.navigationParams!=undefined){
				for(var parin=0;parin<inputParameter.length;parin++){
					var urlName=inputParameter[parin].urlName;
					for(var key in navObj.navigationParams){
						if(navObj.navigationParams[key].value.isInput==true && angular.equals(navObj.navigationParams[key].value.label,urlName)){
							//respStr[key]=inputParameter[parin].parameterValue;
							respStr[key]=parseInputParameterValue(inputParameter[parin]);
							respStr[key+'_field_visible_description']=inputParameter[parin].parameterDescription;


						}
					}
				}
			}

			//check for otherOutputParameters
			if(otherOutputParameters!=undefined && otherOutputParameters!='' && navObj.navigationParams!=undefined){
				for(var parin=0;parin<otherOutputParameters.length;parin++){
					var staticParName = null;
					var staticParValue = null;
					var staticPar = otherOutputParameters[parin];
					for(var name in staticPar){
						staticParName = name;
						staticParValue = staticPar[name];
					}
					for(var key in navObj.navigationParams){
						if(navObj.navigationParams[key].fixed==false && angular.equals(navObj.navigationParams[key].value.label,staticParName)){
							respStr[key]=staticParValue;
							respStr[key+'_field_visible_description']=staticParValue;


						}
					}
				}
			}

			//load fixed value --- replace all
			for(var key in navObj.navigationParams){
				if(navObj.navigationParams[key].fixed==true){
					respStr[key]=navObj.navigationParams[key].value;
				}
			}


			respStr = jsonToURI(respStr);

			return respStr;
		};


		function parseInputParameterValue(param){
			if(param.type=="DATE" ){
				//back date server format
				if(param.parameterValue!=''){
					return sbiModule_dateServices.formatDate(param.parameterValue, sbiModule_config.serverDateFormat );
				}else{
					return '';
				}
				//return sbiModule_dateServices.getDateFromFormat(value, param.dateFormat)

			}else{
				return param.parameterValue;
			}

		}


		function parseParameterValue(param,value){
			//TO-DO verificare i tuipi numerici se sono interi o double
			//mettere i try catch

			if(param.type==undefined && param.inputParameterType==undefined){
				return value;
			}

			if(param.inputParameterType=="STRING" || (param.type!=undefined && param.type.valueCd=="STRING")){
				return value;
			}

			if(param.inputParameterType=="DATE" || (param.type!=undefined && param.type.valueCd=="DATE")){
				//back date server format
//				return sbiModule_dateServices.formatDate(sbiModule_dateServices.getDateFromFormat(value, param.dateFormat),sbiModule_config.serverDateFormat );
				var parsedValue;
				if (Array.isArray(value)){
					parsedValue = [];
					for (v in value){
						var res = sbiModule_dateServices.formatDate(sbiModule_dateServices.getDateFromFormat(value[v], param.dateFormat),sbiModule_config.serverDateFormat );
						if (res) parsedValue.push(res);
					}
				}else{
					parsedValue = sbiModule_dateServices.formatDate(sbiModule_dateServices.getDateFromFormat(value, param.dateFormat),sbiModule_config.serverDateFormat );
				}
				return parsedValue;
			}
			if(param.inputParameterType=="NUM" || (param.type!=undefined && param.type.valueCd=="NUM")){
				var parsedValue;
				if (Array.isArray(value)){
					parsedValue = [];
					for (v in value){
						var res = parseFloat(value[v]);
						if (!isNaN(res)) parsedValue.push(res);
					}
				}else{
					var res=parseFloat(value);
					parsedValue =  isNaN(res) ? undefined : res;
				}
//				var res=parseFloat(value);
//				return isNaN(res) ? undefined : res;
				return parsedValue;
			}
		};

		function jsonToURI(jsonObj){
			return encodeURIComponent(JSON.stringify(jsonObj))
			.replace(/'/g,"%27")
			.replace(/"/g,"%22");
//			.replace(/'/g,"%27")
//			.replace(/"/g,"%22")
//			.replace(/%3D/g,"=")
//			.replace(/%26/g,"&");
		}

		this.internalNavigateTo=function(params,targetDocLabel){
			 sbiModule_restServices.promiseGet("1.0/documents",targetDocLabel)
			.then(function(response){
				execCross({document:response.data},params,undefined,false);
			},function(response){
				sbiModule_restServices.errorHandler(response.data,"Cross navigation error")
			});
		}
})

.factory('$crossNavigationSteps',function(){
	return {stepControl:{},stepItem:[],value:{}}
})


.directive('crossNavigation', function() {
	return {
		template:'',
		replace:false,
		transclude : true,
		scope:{
			crossNavigationHelper:"=?"
		},
		controller: crossNavControllerFunct,
		link: function(scope, element, attrs, ctrl, transclude) {
			transclude(scope,function(clone,scope) {
			 	element.append(clone) ;
			});
		}
	}
})

.directive('crossNavigationBreadCrumb', function($timeout) {
	return {
		template:'<bread-crumb id="id" '
			+'ng-show=" crossNavigationHelper.crossNavigationSteps.stepItem.length>1" '
			+'ng-model=crossNavigationHelper.crossNavigationSteps.stepItem item-name="name" '
			+'selected-index="crossNavigationHelper.crossNavigationSteps.value" '
			+'control="crossNavigationHelper.crossNavigationSteps.stepControl" '
			+'move-to-callback=callbackFunct() '
			+'selected-item="crossNavigationHelper.crossNavigationSteps.currentDocument" > '
			+'</bread-crumb>',
		link: function(scope, element, attrs, ctrl, transclude) {
		  scope.callbackFunct=function(){
			   $timeout(function(){
				  scope.crossNavigationHelper.crossNavigationSteps.stepControl.refresh();
				  },0)
		  }
		}
	}
});

function crossNavControllerFunct($scope,$timeout,$crossNavigationHelper){

	if($scope.crossNavigationHelper==undefined){
		$scope.crossNavigationHelper=$crossNavigationHelper;
	}
}