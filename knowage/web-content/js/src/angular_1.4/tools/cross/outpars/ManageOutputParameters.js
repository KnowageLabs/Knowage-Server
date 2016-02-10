


angular.module('crossOutPars', ['angular_table','ng-context-menu','ngMaterial','sbiModule','angular-list-detail','angular_list'])
.controller('outputParametersController'
		,['$scope','sbiModule_restServices','sbiModule_translate','$mdDialog','$mdToast','$location',function($scope, sbiModule_restServices, sbiModule_translate, $mdDialog, $mdToast, $location){
			var ctr = this;
			var s = $scope;
			s.translate = sbiModule_translate;
			var qs = $location.search();
			s.config = {
				list : {
					columns : [{"label":s.translate.load("sbi.crossnavigation.parname.lbl"),"name":"name"}
					          ,{"label":s.translate.load("sbi.crossnavigation.type.lbl"),"name":"type"}],
		            dsSpeedMenu :  [{
	                	label : s.translate.load('sbi.crossnavigation.action.delete'),
	                	icon :'fa fa-trash',
	                	action : ctr.removeItem
	                }]
				},
				detail : {
					
				}
			};
			
			s.translate = sbiModule_translate;
			
			var newRecord = function(){
				ctr.detail = {'newRecord':true, 'biObjectId': objectId};
			};
			
			var loadParametersList = function(){
				ctr.list = [];
				ctr.listloadingSpinner = true;
				sbiModule_restServices.get('2.0/documents/'+objectId+'/listOutParams', "", null).success(function(data) {
					var parameters = [];
					for(var i=0;i<data.length;i++){
						ctr.list.push({'id':data[i].id,'name':data[i].name,'type':data[i].typeLbl,'typeId':data[i].typeId});
					}
					ctr.listloadingSpinner = false;
				});
			};
			var loadTypeList = function(){
				ctr.typeList = []; //[{value:'string',descr:'String'},{value:'number',descr:'Number'}];
				sbiModule_restServices.get("domains", "listValueDescriptionByType","DOMAIN_TYPE=PAR_TYPE").success(
					function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
							console.log(sbiModule_translate.load("sbi.glossary.load.error"));
						} else {
							for(var i=0;i<data.length;i++){
								ctr.typeList.push({'id':data[i].VALUE_ID,'descr':data[i].VALUE_NM});
							}
							//ctr.typeList = data;
						}
					}).error(function(data, status, headers, config) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));

					});
			};
			
			newRecord();
			loadParametersList();
			loadTypeList();
			
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
					function(data,status,headers,config){
						$scope.showActionOK("sbi.crossnavigation.save.ok");
						loadParametersList();
						newRecord();
					},
					function(data,status,headers,config){
						$scope.showActionOK(data.errors);
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
						content += sbiModule_translate.load(msg[i]);
					}
				}else{
					content = sbiModule_translate.load(msg);
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
		}]);