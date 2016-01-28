


angular.module('crossDefinition', ['angular_table','ng-context-menu','ngMaterial','sbiModule','angular-list-detail','angular_list'])
.controller('navigationController'
		,['$scope','sbiModule_restServices','sbiModule_translate','$mdDialog','$mdToast',function($scope, sbiModule_restServices, sbiModule_translate, $mdDialog, $mdToast){
			var ctr = this;
			var s = $scope;
			
			var newRecord = function(){
				return {'newRecord':true};
			};
			
			s.translate = sbiModule_translate;
			
			ctr.list = [];
			ctr.listDoc = [];
			ctr.detail = newRecord();
			ctr.dragging = false;
			ctr.selectedItem = '';
			
			ctr.navigationList = {
				columns : [{"label":s.translate.load("sbi.crossnavigation.lst.name"),"name":"name"}
						  ,{"label":s.translate.load("sbi.crossnavigation.lst.doc.a"),"name":"fromDoc"}
						  ,{"label":s.translate.load("sbi.crossnavigation.lst.doc.b"),"name":"toDoc"}],
				searchColumns : ["name","fromDoc","toDoc"],
				loadingSpinner : false,
				loadNavigationList : function(){
					ctr.navigationList.loadingSpinner = true;
					sbiModule_restServices.get('1.0/crossNavigation/listNavigation', "", null).success(function(data) {
						ctr.navigationList.loadingSpinner = false;
						ctr.list = data;
					}).error(function(){ctr.navigationList.loadingSpinner = false;});
				},
				loadSelectedNavigation : function(item){
					ctr.detailLoadingSpinner = true;
					sbiModule_restServices.get('1.0/crossNavigation/'+item.id+'/load', "", null).success(function(data) {
						ctr.detailLoadingSpinner = false;
						ctr.detail = data;
					});
				},
				dsSpeedMenu :  [{
                	label : s.translate.load('sbi.crossnavigation.action.delete'),
                	icon :'fa fa-trash',
                	action : function(item, event){ctr.navigationList.removeItem(item, event);}
                }],
                removeItem : function(item, event){
					sbiModule_restServices.post('1.0/crossNavigation/remove', "", "{'id':"+item.id+"}").success(function(data) {
						if(data.hasOwnProperty("errors")){
							$scope.showActionOK("sbi.crossnavigation.remove.ko");
						}else{
							$scope.showActionOK("sbi.crossnavigation.remove.ok");
							ctr.navigationList.loadNavigationList();
						}
					});
				}
			};
			
			ctr.navigationList.loadNavigationList();
			
			ctr.newNavigation = function(){
				ctr.detail = newRecord();
			};
			ctr.saveFunc = function(){
				sbiModule_restServices.post('1.0/crossNavigation/save', "", ctr.detail).success(
					function(data,status,headers,config){
						if(data.hasOwnProperty("errors")){
							$scope.showActionOK(data.errors);
						}else{
							$scope.showActionOK("sbi.crossnavigation.save.ok");
							ctr.navigationList.loadNavigationList();
						}
					}
				);
			};
			ctr.cancelFunc = function(){
				ctr.detail = newRecord();
			};
			
			

			ctr.listDocuments = function(source){
				ctr.clickOnSelectedDoc = function(item, listId, closeDialog){
					if(!ctr.detail.simpleNavigation)ctr.detail.simpleNavigation = {};
					if(source=='A'){
						ctr.detail.simpleNavigation.fromDoc = item.DOCUMENT_NAME;
					}else{
						ctr.detail.simpleNavigation.toDoc = item.DOCUMENT_NAME;
					}
					sbiModule_restServices.get('2.0/documents/'+item.DOCUMENT_NAME+'/parameters', "", null).success(function(data) {
						var parameters = [];
						for(var i=0;i<data.length;i++){
							parameters.push({'id':data[i].id,'name':data[i].label,'type':'input'});
						}
						if(source=='A'){
							ctr.detail.fromPars = parameters;
						}else{
							ctr.detail.toPars = parameters;
						}
						closeDialog();
					});
				};
				$mdDialog.show({
					controller: DialogController,
					templateUrl: 'dialog1.tmpl.html',
					parent: angular.element(document.body),
					clickOutsideToClose:false,
					locals: {
						clickOnSelectedDoc: ctr.clickOnSelectedDoc
					}
				});
				
				function DialogController(scope, $mdDialog, /*listDoc,*/ clickOnSelectedDoc) {
					scope.closeDialog = function() {
						$mdDialog.hide();
					};
					scope.clickOnSelectedDoc = clickOnSelectedDoc;
					scope.loading = true;
					sbiModule_restServices.get('2.0/documents/listDocument/', "", null).success(function(data) {
						scope.loading = false;
						scope.listDoc = data.item;
					});
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