(function() {

	var stringStartsWith = function (string, prefix) {
		return string.toLowerCase().slice(0, prefix.length) == prefix.toLowerCase();
	};

	var documentExecutionApp = angular.module('documentExecutionModule');

	documentExecutionApp.config(['$mdThemingProvider', function($mdThemingProvider) {
		$mdThemingProvider.theme('knowage')
		$mdThemingProvider.setDefaultTheme('knowage');
	}]);
	
	documentExecutionApp.controller( 'documentExecutionController', 
			['$scope', '$http', '$mdSidenav', '$mdDialog','$mdToast', 'sbiModule_translate', 'sbiModule_restServices', 
			 'sbiModule_config', 'sbiModule_messaging', 'execProperties', 'documentExecuteFactories', 'sbiModule_helpOnLine',
			 'documentExecuteServices','docExecute_urlViewPointService','docExecute_paramRolePanelService','infoMetadataService','sbiModule_download',
			 documentExecutionControllerFn]);

	function documentExecutionControllerFn(
			$scope, $http, $mdSidenav,$mdDialog,$mdToast, sbiModule_translate, sbiModule_restServices, sbiModule_config,
			sbiModule_messaging, execProperties, documentExecuteFactories, sbiModule_helpOnLine,documentExecuteServices
			,docExecute_urlViewPointService,docExecute_paramRolePanelService,infoMetadataService,sbiModule_download) {

		console.log("documentExecutionControllerFn IN ");
		$scope.executionInstance = execProperties.executionInstance || {};
		$scope.roles = execProperties.roles;
		$scope.selectedRole = execProperties.selectedRole;
		$scope.execContextId = "";
		//$scope.documentUrl="";
		$scope.showSelectRoles=true;
		$scope.translate = sbiModule_translate;
		$scope.documentParameters = execProperties.parametersData.documentParameters;
		$scope.newViewpoint = JSON.parse(JSON.stringify(documentExecuteFactories.EmptyViewpoint));
		$scope.viewpoints = [];
		$scope.documentExecuteFactories = documentExecuteFactories;
		$scope.documentExecuteServices = documentExecuteServices;
		$scope.paramRolePanelService = docExecute_paramRolePanelService;
		$scope.urlViewPointService = docExecute_urlViewPointService;		
		$scope.currentView = execProperties.currentView;
		$scope.parameterView=execProperties.parameterView;
		$scope.isParameterRolePanelDisabled = execProperties.isParameterRolePanelDisabled;
		$scope.showParametersPanel = execProperties.showParametersPanel;
		//rank
		$scope.rankDocumentSaved = 0;
		$scope.requestToRating={};		
		$scope.isClick=false;
		$scope.setRank = false;
		//note
		$scope.noteLoaded = {};
		$scope.typeNote='Private';
		$scope.notesList = [];
		$scope.profile="";
		$scope.selectedTab={'tab':0};
		
		$scope.openInfoMetadata = function(){
			infoMetadataService.openInfoMetadata();
		}
		
		$scope.initSelectedRole = function(){
			console.log("initSelectedRole IN ");
			if(execProperties.roles && execProperties.roles.length > 0) {
				if(execProperties.roles.length==1) {
					execProperties.selectedRole.name = execProperties.roles[0];
					$scope.showSelectRoles=false;
					//loads parameters if role is selected
					docExecute_urlViewPointService.getParametersForExecution(execProperties.selectedRole.name);
					execProperties.isParameterRolePanelDisabled.status = true;
				}
				docExecute_urlViewPointService.executionProcesRestV1(execProperties.selectedRole.name);
			}
			
			console.log("initSelectedRole OUT ");
		};
		

		//ranking document
		$scope.rankDocument = function(){
			var obj = {
					'obj':$scope.executionInstance.OBJECT_ID
					};
			sbiModule_restServices.promisePost("documentrating","getvote",obj).then(function(response){ 
				//angular.copy(response.data,$scope.rankDocumentSaved);
				$scope.rankDocumentSaved = response.data;
			},function(response){
				$mdDialog.cancel();
				$scope.isClick=false;
			});
			
			$mdDialog.show({
				templateUrl:sbiModule_config.contextName+'/js/src/angular_1.4/tools/documentbrowser/template/documentRank.html',
				scope:$scope,
				preserveScope: true,
				clickOutsideToClose:true
			})
			.then(function(answer) {
				$scope.status = 'You said the information was "' + answer + '".';
				$scope.isClick=false;
			}, function() {
				$scope.status = 'You cancelled the dialog.';
				$scope.isClick=false;
			});
		};
		
		$scope.rateScore=function(value){
			$scope.setRank = true;
			$scope.requestToRating = {
					'rating':value,
					'obj':$scope.executionInstance.OBJECT_ID,
			};
			$scope.isClick=true;
		};
		
		$scope.saveRank = function(){
			sbiModule_restServices.promisePost("documentrating", 'vote',$scope.requestToRating)
			.then(function(response) {
					if (response.data.hasOwnProperty("errors")) {
						$scope.showAction(response.data);
					} else {
						$mdDialog.cancel();
						$scope.showAction(sbiModule_translate.load('sbi.execution.executionpage.toolbar.rating.saved'));
						$scope.isClick=false;
					}
	
				},
				function(response) {
					$scope.isClick=false;
					$scope.errorHandler(response.data,"");
				}
			);
		};
		
		$scope.hoverStar = function(value){
			if($scope.setRank){
				for(var i=1;i<=5;i++){
					var string= "star"+i;
					angular.element(document.getElementById(string).firstChild).removeClass('fa-star');
					angular.element(document.getElementById(string).firstChild).addClass('fa-star-o');
				}
			}
			$scope.isClick=false;
			for(var i=1;i<=value;i++){
				var string= "star"+i;
				angular.element(document.getElementById(string).firstChild).removeClass('fa-star-o');
				angular.element(document.getElementById(string).firstChild).addClass('fa-star');
				
			}
		};
		
		$scope.leaveStar = function(value){
			
			if(!$scope.isClick && !$scope.setRank){
				for(var i=1;i<=value;i++){
					var string= "star"+i;
					angular.element(document.getElementById(string).firstChild).removeClass('fa-star');
					angular.element(document.getElementById(string).firstChild).addClass('fa-star-o');
				}
			} else if(!$scope.isClick && $scope.setRank){
				for(var i=1;i<=5;i++){
					var string= "star"+i;
					angular.element(document.getElementById(string).firstChild).removeClass('fa-star');
					angular.element(document.getElementById(string).firstChild).addClass('fa-star-o');
				}
				for(var i=1;i<=$scope.requestToRating.rating;i++){
					var string= "star"+i;
					angular.element(document.getElementById(string).firstChild).removeClass('fa-star-o');
					angular.element(document.getElementById(string).firstChild).addClass('fa-star');
				}
			}
		};
		
		$scope.close=function(){
			$mdDialog.cancel();
			$scope.isClick=false;
		};
		
		$scope.showAction = function(text) {
			$scope.isClick=false;
			var toast = $mdToast.simple()
			.content(text)
			.action('OK')
			.highlightAction(false)
			.hideDelay(3000)
			.position('top');

			$mdToast.show(toast).then(function(response) {
				if ( response == 'ok' ) {

				}
			});
		};
		$scope.note = {'info': 'Info'};
		//note document
		$scope.noteDocument=function(){
			var obj = {'id' : $scope.executionInstance.OBJECT_ID};
			sbiModule_restServices.promisePost("documentnotes", 'getNote',obj).then(
					function(response) {
						if (response.data.hasOwnProperty("errors")) {
							$scope.showAction(response.data);
						} else {
							console.log(response);
							angular.copy(response.data,$scope.noteLoaded);
							$scope.contentNotes = $scope.noteLoaded.nota;
							$scope.profile = response.data.profile;
						}

					},function(response) {
						$scope.errorHandler(response.data,"");
					})
					
			$mdDialog.show({
				controller: DialogControllerKPI,
				templateUrl:sbiModule_config.contextName+'/js/src/angular_1.4/tools/documentbrowser/template/documentNote.html',
				scope:$scope,
				preserveScope: true,
				clickOutsideToClose:true
			})
			.then(function(answer) {
				$scope.status = 'You said the information was "' + answer + '".';
			}, function() {
				$scope.status = 'You cancelled the dialog.';
			});
		}
		
		$scope.saveNote = function(){
			var obj = {
					'nota' : $scope.contentNotes,
					'idObj': $scope.executionInstance.OBJECT_ID,
					'type' : $scope.typeNote
			}
			sbiModule_restServices.promisePost("documentnotes", 'saveNote',obj).then(
					function(response) {
						if (response.data.hasOwnProperty("errors")) {
							$scope.showAction(response.data);
						} else {
							$scope.showAction("Saved");
							$scope.getList();
						}

					},function(response) {
						$scope.errorHandler(response.data,"");
					})
	
		}
		$scope.getNotesList = function(){
			
			if($scope.notesList.length==0){
				$scope.getList();
			}
		}
		$scope.getList = function(){
			var obj = {'id' : $scope.executionInstance.OBJECT_ID};
			sbiModule_restServices.promisePost("documentnotes", 'getListNotes',obj).then(
					function(response) {
						if (response.data.hasOwnProperty("errors")) {
							$scope.showAction(response.data);
						} else {
							angular.copy(response.data,$scope.notesList)
							
						}

					},function(response) {
						$scope.errorHandler(response.data,"");
					})
		}
		$scope.deleteNote = function(nota){
			var obj ={
					"id":nota.biobjId,
					"execReq":nota.execReq,
					"owner":nota.owner
			}
			
			var confirm = $mdDialog.confirm()
					.title("Are you sure?")
					.ariaLabel('cancel metadata') 
					.ok($scope.translate.load("sbi.general.ok"))
					.cancel($scope.translate.load("sbi.general.cancel"));
					$mdDialog.show(confirm).then(function() {
						sbiModule_restServices.promisePost("documentnotes", 'deleteNote',obj).then(
								function(response) {
									if (response.data.hasOwnProperty("errors")) {
										$scope.showAction(response.data);
									} else {
										$scope.showAction("Nota deleted");
										$scope.getList();
									}

								},function(response) {
									$scope.errorHandler(response.data,"");
								})
					}, function() {
						return;
					});
		}
		
		$scope.editNote = function(nota){
			$scope.contentNotes=nota.content;
			$scope.noteLoaded.content = nota.content;
			$scope.noteLoaded.id = nota.id;
			$scope.noteLoaded.lastChangeDate = nota.lastChangeDate;
			$scope.noteLoaded.creationDate = nota.creationDate;
			$scope.noteLoaded.exeqReq = nota.exeqReq;
			$scope.noteLoaded.owner = nota.owner;
			$scope.selectedTab.tab=0;
		
		}
		
		$scope.exportNote = function(typeExport){
			var obj = {
					'idObj': $scope.executionInstance.OBJECT_ID,
					'type' : typeExport	
			}
			sbiModule_restServices.promisePost("documentnotes", 'getDownalNote',obj).then(
					function(response) {
						if (response.data.hasOwnProperty("errors")) {
							$scope.showAction(response.data);
						} else {
							console.log(response);
							
							var arr = response.data.file;
							var byteArray = new Uint8Array(arr);
							sbiModule_download.getBlob(byteArray,$scope.executionInstance.OBJECT_LABEL,"application/"+typeExport,typeExport);

							/*var a = window.document.createElement('a');

							a.href = window.URL.createObjectURL(new Blob([byteArray], { type: 'application/octet-stream' }));
							a.download = $scope.executionInstance.OBJECT_LABEL+'.'+typeExport;
							// Append anchor to body.
							document.body.appendChild(a)
							a.click();


							// Remove anchor from body
							document.body.removeChild(a)*/
							
						}

					},function(response) {
						$scope.errorHandler(response.data,"");
					})
			
		}
		
		$scope.bin2String = function(array) {
			  var result = "";
			  for (var i = 0; i < array.length; i++) {
			    result += String.fromCharCode(parseInt(array[i], 2));
			  }
			  return result;
			}
		$scope.setTab = function(Tab){
			$scope.selectedTab = Tab;
		}
		$scope.openHelpOnLine=function(){	
			sbiModule_helpOnLine.showDocumentHelpOnLine($scope.executionInstance.OBJECT_LABEL);
		};
					
		/*
		 * EXECUTE PARAMS
		 * Submit param form
		 */
		$scope.executeParameter = function() {
			console.log("executeParameter IN ");
			docExecute_urlViewPointService.executionProcesRestV1(execProperties.selectedRole.name, JSON.stringify(documentExecuteServices.buildStringParameters(execProperties.parametersData.documentParameters)));			
			if($mdSidenav('parametersPanelSideNav').isOpen()) {
				$mdSidenav('parametersPanelSideNav').close();
				execProperties.showParametersPanel.status = $mdSidenav('parametersPanelSideNav').isOpen();
			}
			console.log("executeParameter OUT ");
		};
		
		$scope.changeRole = function(role) {
			console.log("changeRole IN ");
			if(role != execProperties.selectedRole.name) {  
				docExecute_urlViewPointService.executionProcesRestV1(role);
				docExecute_urlViewPointService.getParametersForExecution(role);
			}
			console.log("changeRole OUT ");
		};
	
		
		$scope.showRequiredFieldMessage = function(parameter) {
			return (
				parameter.mandatory 
				&& (
						!parameter.parameterValue
						|| (Array.isArray(parameter.parameterValue) && parameter.parameterValue.length == 0) 
						|| parameter.parameterValue == '')
				) == true;
		};		

		$scope.isParameterPanelDisabled = function(){
			return (!execProperties.parametersData.documentParameters || execProperties.parametersData.documentParameters.length == 0);
		};
		
		$scope.executeDocument = function(){
			console.log('Executing document -> ', execProperties);
		};
	
		$scope.editDocument = function(){
			alert('Editing document');
			console.log('Editing document -> ', execProperties);
		};
	
		$scope.deleteDocument = function(){
			alert('Deleting document');
			console.log('Deleting document -> ', execProperties);
		};
		
		$scope.clearListParametersForm = function(){
			if(execProperties.parametersData.documentParameters.length > 0){
				for(var i = 0; i < execProperties.parametersData.documentParameters.length; i++){
					var parameter = execProperties.parametersData.documentParameters[i];
					documentExecuteServices.resetParameter(parameter);
				}
			}
		};
		
		$scope.printDocument = function() {
			var frame = window.frames["documentFrame"];
			if(frame.print){
				frame.print();
			}else if(frame.contentWindow){
				frame.contentWindow.print();
			}
		} ;
		
		console.log("documentExecutionControllerFn OUT ");
	};
	function DialogControllerKPI($scope,$mdDialog){
		$scope.contentNotes = "";
		$scope.close = function(){
			$mdDialog.cancel();

		}
		$scope.apply = function(){
			$mdDialog.cancel();
		}

	}
	documentExecutionApp.directive('iframeSetDimensionsOnload', [function(){
		return {
			restrict: 'A',
			link: function(scope, element, attrs){
				element.on('load', function(){
					var iFrameHeight = element[0].parentElement.scrollHeight + 'px';
					element.css('height', iFrameHeight);				
					element.css('width', '100%');
				});
			}
		};
	}]);

})();	



