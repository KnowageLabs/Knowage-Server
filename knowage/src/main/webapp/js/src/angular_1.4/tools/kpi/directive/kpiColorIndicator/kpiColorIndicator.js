/* Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>. */

(function() {

var scripts = document.getElementsByTagName("script");
var currentScriptPath = scripts[scripts.length - 1].src;
currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);

angular.module('kpi_color_indicator', ['ngMaterial','sbiModule'])
.directive('kpiColorIndicator', ['$timeout','$mdDialog','sbiModule_translate','sbiModule_restServices','sbiModule_config','$filter','$mdSidenav','$mdToast', function($timeout,$mdDialog,sbiModule_translate,sbiModule_restServices,sbiModule_config,$filter,$mdSidenav,$mdToast){
	return {
		templateUrl: currentScriptPath+'/kpiColorIndicatorTemplate.html',
		restrict: 'E',
		replace:true,
		scope: {
			perspectives:"=",
			criterion:"=",
			definition:"="
		},
		link: function(scope, element, attrs, ctrl, transclude) {
			scope.criterion;
			scope.translate=sbiModule_translate;
			scope.cp = 0;
			scope.ct = 0;
			
			scope.getDefaultCriterion = function(criterion){
				criterion = criterion?criterion:"MAJORITY"
				if(!scope.defaultCriterion){
					for(var dc in scope.criterion){
						if(scope.criterion[dc].valueCd == criterion){
							scope.defaultCriterion = scope.criterion[dc];
							return scope.defaultCriterion;
						}
					}
				}else{
					return scope.defaultCriterion;
				}
			}
								
			scope.kpiList = [];
			scope.getClass=function(color,isIcon){
				
				switch(color) {
				    case "RED":
				        return isIcon ? 'fa-arrow-down' : 'redKpi'
				        break;
				    case "YELLOW":
				    	  return isIcon ? 'fa-minus' : 'yellowKpi'
				        break;
				    case "GREEN":
				    	  return isIcon ? 'fa-arrow-up' : 'greenKpi'
				    	break;
				    default:
				    	  return isIcon ? 'fa-minus' : 'greyKpi'
				    	break;
				   
				}
			}	 
			
			
			scope.renaming = false;
			
			scope.addPerspective=function(){ 
				
				
				scope.newPerspective = {
					'name':'New Perspective',
					'status':'GRAY',
					'criterion':scope.getDefaultCriterion(),
					'options':{"criterionPriority":[]},
					'targets':[],
					'groupedKpis':[]
				};
				scope.perspectives.push($scope.newPerspective);

			};
			
			scope.addTarget=function(perspective){ 
				
				scope.newTarget = {
	                "name": "new target",
	                'criterion':scope.getDefaultCriterion(),
	                "options": {"criterionPriority":[]},
	                "status": "GRAY",
	                "kpis": []
	               };

				perspective.targets.push(scope.newTarget);
			}
			
			scope.parseDate = function(date){
				result = "";
				if(date == "d/m/Y"){
					result = "dd/MM/yyyy";
				}
				if(date =="m/d/Y"){
					result = "MM/dd/yyyy"
				}
				return result;
			};
			
			scope.openTarget=function(p,t){
				for(i=0;i<scope.perspectives.length;i++){
					if(scope.perspectives[i].$$hashKey==p.$$hashKey){
						for(j=0;j<scope.perspectives[i].targets.length;j++){
							if(scope.perspectives[i].targets[j].$$hashKey==t.$$hashKey){
								scope.ct= j;
							}
						
						}
						scope.cp= i;
					}
				}
				
				$mdSidenav('right').toggle();
			}
			
			scope.deleteTarget=function(p,t){
				for(i=0;i<scope.perspectives.length;i++){
					if(scope.perspectives[i].$$hashKey==p.$$hashKey){
						for(j=0;j<scope.perspectives[i].targets.length;j++){
							if(scope.perspectives[i].targets[j].$$hashKey==t.$$hashKey){
								scope.perspectives[i].targets.splice(j,1);
							}
						}
					}
				}
			}
			
			scope.getListKPI = function(){
			
				if(sbiModule_config.contextName != "/knowage") {
					sbiModule_restServices.restToRootProject();
				}
			
				sbiModule_restServices.promiseGet("1.0/kpi","listKpiWithResult")
				.then(function(response){ 
					for(var i=0;i<response.data.length;i++){
						var obj = angular.extend({},response.data[i]);
						var dateFormat = scope.parseDate(sbiModule_config.localizedDateFormat);
						//parse date based on language selected
						obj["datacreation"]=$filter('date')(response.data[i].dateCreation, dateFormat);
						obj.kpiSemaphore="<kpi-semaphore-indicator indicator-color=\"'"+obj.status+"'\"></kpi-semaphore-indicator>";
						if(obj.category == null){
							obj.category = {"translatedValueName" : " "};
						}
						scope.kpiList.push(obj);
					}
				},function(response){
				});
			};
			scope.getListKPI();
			
			scope.addKpiToTarget=function(){ 
				var tmpTargetKpis=[];
				if(scope.perspectives[scope.cp].targets[scope.ct].kpis==undefined){
					scope.perspectives[scope.cp].targets[scope.ct].kpis = [];
				} 
				
				angular.copy(scope.perspectives[scope.cp].targets[scope.ct].kpis,tmpTargetKpis); 
				$mdDialog.show({
					controller: DialogControllerKPI,
					templateUrl: currentScriptPath+'/kpiAddTemplate.html',
					clickOutsideToClose:false,
					preserveScope:true,
					locals: {
						kpiList: scope.kpiList,
						tmpTargetKpis: tmpTargetKpis}
				})
				.then(function(data) {
					scope.perspectives[scope.cp].targets[scope.ct].groupedKpis = [];
					var tempStatus 	= [];
					var tempCount 	= [];
					for(var i=0;i<data.length;i++){
						if(tempStatus.indexOf(data[i].status)==-1){
							tempStatus.push(data[i].status);
							tempCount.push(1);
						}else{
							tempCount[tempStatus.indexOf(data[i].status)]++
						}
						
					}
					for(var k=0;k<tempStatus.length;k++){
						scope.perspectives[scope.cp].targets[scope.ct].groupedKpis.push({"status":tempStatus[k],"count":tempCount[k]});
					}
				angular.copy(data,scope.perspectives[scope.cp].targets[scope.ct].kpis);
				});
				
			};
			scope.removeKpiFromTarget = function(cp,ct,i){
				scope.perspectives[cp].targets[ct].kpis.splice(i,1);
				// deleting the kpi from the kpi status aggregation
				for(k=0;k<scope.perspectives[cp].targets[ct].groupedKpis.length;k++){
					if(scope.perspectives[cp].targets[ct].groupedKpis[k].status == scope.perspectives[cp].targets[ct].kpis[i].status){
						if(scope.perspectives[cp].targets[ct].groupedKpis[k].count==1){scope.perspectives[cp].targets[ct].groupedKpis[k].splice(i,1);}
						else{scope.perspectives[cp].targets[ct].groupedKpis[k].count--;}
					}
				}
			}
			
			var DialogControllerKPI= function(scope,kpiList,tmpTargetKpis){
				scope.kpiAllList=kpiList;
				for(var i=0;i<scope.kpiAllList.length;i++){
					if(scope.kpiAllList[i].category == null){
						scope.kpiAllList[i].category = {"translatedValueName" : " "};
					}
				}
				scope.kpiSelected=tmpTargetKpis;
				
				scope.saveKpiToTarget=function(){
					var stat 	= [];
					var occ 	= [];
					
					for(j=0;j<scope.kpiSelected.length;j++){
						if(stat.indexOf(scope.kpiSelected[j].status)==-1){
							stat.push(scope.kpiSelected[j].status);
							occ.push(1);
						}else{
							occ[stat.indexOf(scope.kpiSelected[j].status)]++;
						}
					}
					  $mdDialog.hide(scope.kpiSelected);
				}
				scope.close=function(){
					
					$mdDialog.cancel();
				}
			}
			
			scope.showKpis = function(pId,tId,definition){
				if(!definition){
				
					$mdDialog.show({
						controller: kpiListControllerKPI,
						templateUrl: currentScriptPath+'/kpiShowKpi.tpl.html',
						clickOutsideToClose:true,
						preserveScope:true,
						locals: {
							perspectives: scope.perspectives,
							pId : pId,
							tId: tId
							
							}
					})
					.then(function(data) {
						
					});
									
									
								
					}
				}
				
				var kpiListControllerKPI= function(scope,pId,tId,perspectives){
				scope.translate=sbiModule_translate;
				scope.getClass=function(color,isIcon){
					
					switch(color) {
					    case "RED":
					        return isIcon ? 'fa-arrow-down' : 'redKpi'
					        break;
					    case "YELLOW":
					    	  return isIcon ? 'fa-minus' : 'yellowKpi'
					        break;
					    case "GREEN":
					    	  return isIcon ? 'fa-arrow-up' : 'greenKpi'
					    	break;
					    default:
					    	  return isIcon ? 'fa-minus' : 'greyKpi'
					    	break;
					   
					}
				}	 
				
				
				
				for(i=0;i<perspectives.length;i++){
					if(perspectives[i].id==pId){
						scope.cp = i;
						for(j=0;j<perspectives[i].targets.length;j++){
							if(perspectives[i].targets[j].id==tId){
								scope.ct = j;
								scope.target = {};
								scope.target = perspectives[scope.cp].targets[scope.ct];
							}
						}
					}
				}
				scope.close=function(){
					$mdDialog.cancel();
				}
			} 
							
			
			scope.deletePerspective = function(pId){
				var confirm = $mdDialog.confirm()
			    .title(sbiModule_translate.load("sbi.kpi.delete.progress"))
			    .content(sbiModule_translate.load("sbi.layer.delete.progress.message.delete"))
			    .ariaLabel('cancel perspective') 
			    .ok(sbiModule_translate.load("sbi.general.yes"))
			    .cancel(sbiModule_translate.load("sbi.general.No"));
			      $mdDialog.show(confirm).then(function() {
			    	  for(i=0;i<scope.perspectives.length;i++){
							if(scope.perspectives[i].id==pId){
								scope.perspectives.splice(i,1);
								return;
							}
						}
			      });
			}
		}
	} 
}])

.directive('inputRename', ['sbiModule_translate', function(sbiModule_translate){
	return {
		template: 	'<div class="inputRename"><span class="renameLabel" ng-if="!renaming" ng-click="toggleInput()">{{item.name}}</span>'+
					'<input type="text" class="renameInput" ng-model="item.name" ng-if="renaming" ng-blur="toggleInput()" ng-keypress="($event.which === 13)?toggleInput():0"/></div>',
		restrict: 'E',
		replace:true,
		scope: {
			item:"="
		},
		link: function(scope, element, attrs, ctrl) {
			scope.renaming = false;
			scope.toggleInput=function(){
				scope.renaming = !scope.renaming;
			} 
			
	                
		}
	}
}]);
})();