angular.module('cross_navigation', ['ngMaterial','bread_crumb','angular_table'])
.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}])
.service('$crossNavigationHelper',
		function($crossNavigationSteps,sbiModule_restServices,sbiModule_config,$mdDialog,sbiModule_translate,sbiModule_dateServices,$filter){
		var cns=this;
		var selectedRole={};
		this.crossNavigationSteps=$crossNavigationSteps;

		this.changeNavigationRole=function(newRole){
			selectedRole=newRole;
		};

		//chartType,documentName, documentParameters, categoryName, categoryValue, serieName, serieValue, groupingCategoryName, groupingCategoryValue, stringParameters
		this.navigateTo=function(outputParameter,inputParameter,targetDocument,docLabel){

			sbiModule_restServices.promiseGet("1.0/crossNavigation",this.crossNavigationSteps.currentDocument.label+"/loadCrossNavigationByDocument")
			.then(function(response){

				var navObj=response.data;
				var targetUrl="";
				if(navObj.length==0){
					alert("No cross navigation defined for the object");
					return;
				}else if(navObj.length==1){
					execCross(navObj[0],outputParameter,inputParameter,true);
				}
				else if(navObj.length>=1){
					if(targetDocument!=undefined){
						for(var i=0;i<navObj.length;i++){
							if(angular.equals(navObj[i].crossName,targetDocument)){
								execCross(navObj[i],outputParameter,inputParameter,true);
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
					      template: '<md-dialog aria-label="Select document" layout="column" ng-cloak style="max-width: 400px;">'
					    	  +'<md-toolbar>'
					    	  +'	<div class="md-toolbar-tools">'
					    	  +'		<h2>{{translate.load("Seleziona il documento di destinazione")}}</h2>'
					    	  +'	</div>'
					    	  +'</md-toolbar>'
					    	  +'<div layout-margin flex>'
					    	  +'	<angular-table flex id="selectDoctableCross" ng-model=documents columns="[{label:\'\',name:\'document.name\'}]" hide-table-head="true" click-function="selectDocument(item);"></angular-table>'
					    	  +'</div>'
					    	  +'<div layout="row">'
					    	  +'	<span flex></span>'
					    	  +'	<md-button ng-click="cancel()">Cancel</md-button>'
					    	  +'</div>'
					    	  +'</md-dialog>',
					      clickOutsideToClose:false,
					      locals:{documents:navObj,
					    	  translate:sbiModule_translate}
					    })
					    .then(function(doc) {
					    	execCross(doc,outputParameter,inputParameter,true);
					    }, function() {
					     return;
					    });


				}





			},function(response){
				sbiModule_restServices.errorHandler(response.data, "Errors while attempt to open target document")
			})

		};

		function execCross(doc,outputParameter,inputParameter,externalCross){
			var parameterStr="";
			if(externalCross){
				parameterStr=cns.responseToStringParameter(doc,outputParameter,inputParameter);
			}else{
				parameterStr=jsonToURI(outputParameter);
			}
			targetUrl= sbiModule_config.contextName
			+ '/restful-services/publish?PUBLISHER=documentExecutionNg'
			+ '&OBJECT_ID=' + doc.document.id
			+ '&OBJECT_LABEL=' + doc.document.label
			+ '&SELECTED_ROLE=' + selectedRole.name
			+ '&SBI_EXECUTION_ID=null'
			+ '&OBJECT_NAME=' + doc.document.name
			+"&CROSS_PARAMETER="+parameterStr
			;
			cns.crossNavigationSteps.stepControl.insertBread({name:doc.document.name,label:doc.document.label,id:doc.document.id,url:targetUrl});
		};

		this.responseToStringParameter=function(navObj,outputParameter,inputParameter){
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
				return sbiModule_dateServices.formatDate(sbiModule_dateServices.getDateFromFormat(value, param.dateFormat),sbiModule_config.serverDateFormat );
				//return sbiModule_dateServices.getDateFromFormat(value, param.dateFormat)

			}
			if(param.inputParameterType=="NUM" || (param.type!=undefined && param.type.valueCd=="NUM")){
				var res=parseFloat(value);
				return isNaN(res) ? undefined : res;
			}
		};

		function jsonToURI(jsonObj){
			return encodeURIComponent(JSON.stringify(jsonObj))
			.replace(/'/g,"%27")
			.replace(/"/g,"%22")
			.replace(/%3D/g,"=")
			.replace(/%26/g,"&");
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