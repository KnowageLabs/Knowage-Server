


angular.module('crossDefinition', ['angular_table','ng-context-menu','ngMaterial','sbiModule','angular-list-detail','angular_list'])
.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}])
.controller('navigationController'
		,['$scope','sbiModule_restServices','sbiModule_translate','$mdDialog','$mdToast',function($scope, sbiModule_restServices, sbiModule_translate, $mdDialog, $mdToast){
			var ctr = this;
			var s = $scope;
			
			var newRecord = function(){
				ctr.tmpfixedValue = '';
				return {'newRecord':true};
			};
			
			s.translate = sbiModule_translate;
			
			ctr.list = [];
			ctr.detail = newRecord();
			ctr.dragging = false;
			ctr.selectedItem = '';
			
			ctr.addFixedParam = function(){
				if(ctr.tmpfixedValue != 'undefined' && ctr.tmpfixedValue != '' && ctr.tmpfixedValue != null){
					if(!ctr.detail.fromPars)ctr.detail.fromPars=[];
					ctr.detail.fromPars.push({'id':ctr.detail.simpleNavigation.fromDocId,'name':ctr.tmpfixedValue,'type':2,'fixedValue':ctr.tmpfixedValue});
					ctr.tmpfixedValue = '';
				}
			};
			
			ctr.navigationList = {
				columns : [{"label":s.translate.load("sbi.crossnavigation.lst.name"),"name":"name"}
						  ,{"label":s.translate.load("sbi.crossnavigation.lst.doc.a"),"name":"fromDoc"}
						  ,{"label":s.translate.load("sbi.crossnavigation.lst.doc.b"),"name":"toDoc"}],
				searchColumns : ["name","fromDoc","toDoc"],
				loadingSpinner : false,
				loadNavigationList : function(){
					ctr.navigationList.loadingSpinner = true;
					sbiModule_restServices.promiseGet('1.0/crossNavigation/listNavigation', "", null)
					.then(function(response) {
						ctr.navigationList.loadingSpinner = false;
						ctr.list = response.data;
					},function(response){
						ctr.navigationList.loadingSpinner = false;
					});
				},
				loadSelectedNavigation : function(item){
					ctr.detailLoadingSpinner = true;
					sbiModule_restServices.promiseGet('1.0/crossNavigation/'+item.id+'/load', "", null)
					.then(function(response) {
						var data = response.data;
						ctr.detailLoadingSpinner = false;
						ctr.detail = data;
					},function(response){
						console.log(response);
					});
				},
				dsSpeedMenu :  [{
                	label : s.translate.load('sbi.crossnavigation.action.delete'),
                	icon :'fa fa-trash',
                	action : function(item, event){ctr.navigationList.removeItem(item, event);}
                }],
                removeItem : function(item, event){
					sbiModule_restServices.promisePost('1.0/crossNavigation/remove', "", "{'id':"+item.id+"}")
					.then(function(response) {
						ctr.navigationList.loadNavigationList();
						$scope.showActionOK("sbi.crossnavigation.remove.ok");
					},function(response){
						$scope.showActionOK("sbi.crossnavigation.remove.ko");
					});
				}
			};
			
			ctr.navigationList.loadNavigationList();
			
			ctr.newNavigation = function(){
				ctr.detail = newRecord();
			};
			ctr.saveFunc = function(){
				sbiModule_restServices.promisePost('1.0/crossNavigation/save', "", ctr.detail)
				.then(function(response){
					$scope.showActionOK("sbi.crossnavigation.save.ok");
					ctr.navigationList.loadNavigationList();
					ctr.detail = newRecord();
				},function(response){
					$scope.showActionOK(response.data.errors);
				});
			};
			ctr.cancelFunc = function(){
				ctr.detail = newRecord();
			};
			
			function loadInputParameters(documentLabel,callbackFunction){
				sbiModule_restServices.promiseGet('2.0/documents/'+documentLabel+'/parameters', "", null)
				.then(function(response) {
					var data = response.data;
					var parameters = [];
					for(var i=0;i<data.length;i++){
						parameters.push({'id':data[i].id,'name':data[i].label,'type':1});
					}
					callbackFunction(parameters);
				},function(response){});
			}
			
			function loadOutputParameters(documentId,callbackFunction){
				sbiModule_restServices.promiseGet('2.0/documents/'+documentId+'/listOutParams', "", null)
				.then(function(response) {
					var data = response.data;
					var parameters = [];
					for(var i=0;i<data.length;i++){
						parameters.push({'id':data[i].id,'name':data[i].name,'type':0});
					}
					callbackFunction(parameters);
				},function(response){});
			}
			
			ctr.listLeftDocuments = function(){
				ctr.listDocuments(function(item, listId, closeDialog){
					if(!ctr.detail.simpleNavigation)ctr.detail.simpleNavigation = {};
					ctr.detail.simpleNavigation.fromDocId = item.DOCUMENT_ID;
					ctr.detail.simpleNavigation.fromDoc = item.DOCUMENT_NAME;
					loadInputParameters(item.DOCUMENT_NM,function(data){
						ctr.detail.fromPars = data;
						loadOutputParameters(item.DOCUMENT_ID,function(data){
							ctr.detail.fromPars = ctr.detail.fromPars.concat(data);
							closeDialog();
						});
					});
				});
			};
			
			ctr.listRightDocuments = function(){
				ctr.listDocuments(function(item, listId, closeDialog){
					if(!ctr.detail.simpleNavigation)ctr.detail.simpleNavigation = {};
					ctr.detail.simpleNavigation.toDoc = item.DOCUMENT_NAME;
					loadInputParameters(item.DOCUMENT_NM,function(data){
						ctr.detail.toPars = data;
						closeDialog();
					});
				});
			};
			
			ctr.listDocuments = function(clickOnSelectedDocFunction){
				$mdDialog.show({
					controller: DialogController,
					templateUrl: 'dialog1.tmpl.html',
					parent: angular.element(document.body),
					clickOutsideToClose:false,
					locals: {
						clickOnSelectedDoc: clickOnSelectedDocFunction
						,translate: s.translate
					}
				});
				
				function DialogController(scope, $mdDialog, clickOnSelectedDoc, translate) {
					scope.closeDialog = function() {
						$mdDialog.hide();
					};
					scope.clickOnSelectedDoc = clickOnSelectedDoc;
					scope.translate = translate;
					scope.loading = true;
					sbiModule_restServices.promiseGet("2.0/documents", "listDocument").then(
					function(response){
						scope.loading = false;
						scope.listDoc = response.data.item;},
					function(response){
							sbiModule_restServices.errorHandler(response.data,"")
						}
					) 
				}
			};
			
			ctr.treeOptions = {
				beforeDrop: function(event) {
					if(ctr.selectedItem >= 0){
						ctr.detail.toPars[ctr.selectedItem].links = [event.source.cloneModel];
					}
					return false;
				},
				dragStop: function(){
					ctr.unselectAll();
				}
			};
			
			ctr.treeOptions2 = {
				accept: function(sourceNodeScope, destNodesScope, destIndex){
					if(destNodesScope.depth()==0 && !ctr.hasLink(destIndex)){
						ctr.selectItem(destIndex);
					}else{
						ctr.unselectAll();
					}
					return false;
				}
			};
			
			ctr.hasLink = function(i){
				var item = ctr.detail.toPars[i];
				return item && item.links && item.links.length > 0;
			};
			
			ctr.selectItem = function(index){
				ctr.selectedItem = index;
			};
			
			ctr.unselectAll = function(){
				ctr.selectedItem = '';
			};
			
			ctr.removeLink = function(id){
				for(var i=0;i<ctr.detail.toPars.length;i++){
					if(ctr.detail.toPars[i].id==id){
						ctr.detail.toPars[i].links=[];
					}
				}
			};
			
			ctr.getTypeLabel = function(type){
				if(type==0){
					return s.translate.load('sbi.crossnavigation.output');
				}else if(type==1){
					return s.translate.load('sbi.crossnavigation.input');
				}else{
					return s.translate.load('sbi.crossnavigation.fixed');
				}
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
						if(Object.prototype.toString.call(msg[i])==='[object Object]'){
							content += sbiModule_translate.load(msg[i].message);
						}else{
							content += sbiModule_translate.load(msg[i]);
						}
					}
				}else{
					content = sbiModule_translate.load(msg);
				}
				var toast = $mdToast.simple()
				.content(content)
				.action('OK')
				.highlightAction(false)
				.hideDelay(delay)
				.position('top');

				$mdToast.show(toast).then(function(response) {
					if ( response == 'ok' ) {
					}
				});
			};
		}]);