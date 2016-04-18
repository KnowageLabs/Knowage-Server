


angular.module('crossOutPars', ['angular_table','ng-context-menu','ngMaterial','sbiModule','angular-list-detail','angular_list'])
.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}])
.controller('outputParametersController',['$scope','sbiModule_restServices','sbiModule_translate','$mdDialog','$mdToast','$location',outputParametersControllerFunction]);
		
		
		function outputParametersControllerFunction($scope, sbiModule_restServices, sbiModule_translate, $mdDialog, $mdToast, $location){
			var ctr = this;
			var s = $scope;
			s.translate = sbiModule_translate;
			var qs = $location.search();
			s.config = {
				list : {
					columns : [{"label":s.translate.load("sbi.crossnavigation.parname.lbl"),"name":"name"}
					          ,{"label":s.translate.load("sbi.crossnavigation.type.lbl"),"name":"type.translatedValueName"}],
		            dsSpeedMenu :  [{
	                	label : s.translate.load('sbi.crossnavigation.action.delete'),
	                	icon :'fa fa-trash',
	                	action : function(item,event) {
	                		ctr.removeItem(item,event);
                        }
	                }]
				},
				detail : {
					
				}
			};
			
			var newRecord = function(){
				ctr.detail = {'id':null, 'biObjectId': objectId};
			};
			
			var loadParametersList = function(){
				ctr.list = [];
				ctr.listloadingSpinner = true;
				sbiModule_restServices.promiseGet('2.0/documents/'+objectId+'/listOutParams', "", null).then(
					function(response) {
						ctr.list = response.data;
						ctr.listloadingSpinner = false;
					},function(response) {
						
					});
			};
			var loadTypeList = function(){
				ctr.typeList = [];
				sbiModule_restServices.promiseGet("2.0/domains","listByCode/PAR_TYPE").then(
					function(response) {
						ctr.typeList = response.data;
					},function(response) {
						console.log(s.translate.load("sbi.glossary.load.error"));

					});
			};
			var loadDateTypes = function(){
				ctr.dateFormats = [];
				sbiModule_restServices.promiseGet("2.0/domains","listByCode/DATE_FORMAT").then(
					function(response) {
						ctr.dateFormats = response.data;
					},function(response) {
						console.log(s.translate.load("sbi.glossary.load.error"));
					});
			}
			
			newRecord();
			
			loadTypeList();
			loadParametersList();
			loadDateTypes();
			
			ctr.newFunc = function(){
				newRecord();
			};
			
			ctr.cancelFunc = function(){
				newRecord();
			};
			ctr.loadSelected = function(item){
				ctr.detail = angular.copy(item);
				ctr.detail.formatObj = {'valueCd':item.formatCode,'valueName':item.formatValue};
			};
			ctr.removeItem = function(item, event){
				sbiModule_restServices.promiseDelete('2.0/documents/'+item.id+'/deleteOutParam', "", null)
				.then(function(data) {
					$scope.showActionOK("sbi.crossnavigation.remove.ok");
					loadParametersList();
				}
				,function(data){
					$scope.showActionOK("sbi.crossnavigation.remove.ko");
				});
			}
			ctr.saveFunc = function(){
				if(ctr.detail.formatObj){
					ctr.detail.formatCode = ctr.detail.formatObj.valueCd;
					if(ctr.detail.formatCode!='CUSTOM'){
						ctr.detail.formatValue = ctr.detail.formatObj.valueName;
					}
					delete ctr.detail.formatObj;
				}
				sbiModule_restServices.promisePost('2.0/documents/saveOutParam', "", ctr.detail).then(
					function(response){
						$scope.showActionOK("sbi.crossnavigation.save.ok");
						loadParametersList();
						newRecord();
					},
					function(response){
						$scope.showActionOK(response.data.errors);
					}
				);
			};
			
			
			$scope.showActionOK = function(msg) {
				var delay = 3000;
				var content = '';
				if(Object.prototype.toString.call(msg) === '[object Array]'){
					for(var i=0;i<msg.length;i++){
						if(i!=0){
							content += ' - ';
							delay += 1000;
						}
						content += s.translate.load(msg[i]);
					}
				}else{
					content = s.translate.load(msg);
				}
				var toast = $mdToast.simple()
				.content(content)
				.action(s.translate.load('sbi.general.ok'))
				.highlightAction(false)
				.hideDelay(delay)
				.position('top');

				$mdToast.show(toast).then(function(response) {
					if ( response == 'ok' ) {
					}
				});
			};
		};