


angular.module('crossDefinition', ['angular_table','ng-context-menu','ngMaterial','sbiModule'])
.controller('navigationList'
	,['$scope','sbiModule_restServices','sbiModule_translate',function($scope, sbiModule_restServices, sbiModule_translate){
		var ctr = this;
		var s = $scope;
		
		s.translate = sbiModule_translate;
		
		ctr.list = [];
		ctr.listDoc = [];
		ctr.detail = {};
		
		sbiModule_restServices.get('1.0/crossNavigation/listNavigation', "", null).success(function(data) {
			ctr.list = data;
		});
		
		ctr.loadSelectedNavigation = function(item){
			console.log(item);
			
			sbiModule_restServices.get('1.0/crossNavigation/'+item.id+'/load', "", null).success(function(data) {
				ctr.detail = data;
			});
		}
		
		ctr.listDocuments = function(){
			sbiModule_restServices.get('2.0/documents/listDocument/', "", null).success(function(data) {
				ctr.listDoc = data;
			});
		}
		
		
		ctr.treeOptions = {
//			accept: function() {return false;},
//			beforeDrop: function(event) {
//				var from = event.source.cloneModel;
//				var to = event.dest.nodesScope;
//				if(ctr.selectedItem){
//					from.toName = ctr.selectedItem.$modelValue.name;
//					from.isLink = true;
//					from.emptyList = [ctr.selectedItem];
//					ctr.detail.toPars.push(cloneModel);
//				}
//				return false;
//			}
		};
		
		ctr.selectItem = function(e){
			ctr.selectedItem = e.target;
		};
		
		ctr.unselectAll = function(){
			ctr.selectedItem = '';
		};
	}]);