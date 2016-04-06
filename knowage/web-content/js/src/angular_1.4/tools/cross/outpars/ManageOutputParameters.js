


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
					          ,{"label":s.translate.load("sbi.crossnavigation.type.lbl"),"name":"typeLbl"}],
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
				ctr.detail = {'newRecord':true, 'biObjectId': objectId};
			};
			
			var loadParametersList = function(){
				ctr.list = [];
				ctr.listloadingSpinner = true;
				sbiModule_restServices.promiseGet('2.0/documents/'+objectId+'/listOutParams', "", null).then(
					function(response) {
						var data = response.data;
						for(var i=0;i<data.length;i++){
							ctr.list.push(
								{'id':data[i].id
								,'name':data[i].name
								,'typeLbl':ctr.typeMap[data[i].typeId]
								,'typeId':data[i].typeId
								,'biObjectId':data[i].biObjectId});
						}
						ctr.listloadingSpinner = false;
					},function(response) {
						
					});
			};
			var loadTypeList = function(){
				ctr.typeList = []; //[{value:'string',descr:'String'},{value:'number',descr:'Number'}];
				ctr.typeMap = {};
				sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType","DOMAIN_TYPE=PAR_TYPE").then(
					function(response) {
						var data = response.data;
						for(var i=0;i<data.length;i++){
							ctr.typeList.push({'id':data[i].VALUE_ID,'descr':data[i].VALUE_NM});
							ctr.typeMap[data[i].VALUE_ID] = data[i].VALUE_NM;
						}
					},function(response) {
						console.log(s.translate.load("sbi.glossary.load.error"));

					});
			};
			
			newRecord();
			//list of types must be loaded before list of parameters
			loadTypeList();
			loadParametersList();
			
			ctr.newFunc = function(){
				newRecord();
			};
			
			ctr.cancelFunc = function(){
				newRecord();
			};
			ctr.loadSelected = function(item){
				ctr.detail = angular.copy(item);
				ctr.selected = item;
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