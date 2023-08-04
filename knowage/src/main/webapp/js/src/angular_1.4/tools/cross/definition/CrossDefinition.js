


angular.module('crossDefinition', ['angular_table','ng-context-menu','ngMaterial','sbiModule','angular-list-detail','angular_list'])
.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}])
.controller('navigationController'
		,['$scope','sbiModule_restServices','sbiModule_translate','$angularListDetail','$mdDialog','$mdToast', 'sbiModule_i18n', 'sbiModule_messaging',function($scope, sbiModule_restServices, sbiModule_translate,$angularListDetail, $mdDialog, $mdToast, sbiModule_i18n, sbiModule_messaging){
			var ctr = this;
			var s = $scope;

			var newRecord = function(){
				ctr.tmpfixedValue = '';
				return {'newRecord':true};
			};




			s.translate = sbiModule_translate;
			s.i18n = sbiModule_i18n;

			$scope.crossModes = [{label:s.translate.load("sbi.crossnavigation.modality.normal"),value:0},
								 {label:s.translate.load("sbi.crossnavigation.modality.popup"),value:1},
								 {label:s.translate.load("sbi.crossnavigation.modality.popupwindow"),value:2},
			                    ];


			ctr.list = [];
			ctr.detail = newRecord();
			ctr.crossmodality = $scope.crossModes[0];
			ctr.dragging = false;

			ctr.addFixedParam = function(){
				if(ctr.tmpfixedValue != 'undefined' && ctr.tmpfixedValue != '' && ctr.tmpfixedValue != null){
					if(!ctr.detail.fromPars)ctr.detail.fromPars=[];
					ctr.detail.fromPars.push({'id':ctr.detail.simpleNavigation.fromDocId,'name':ctr.tmpfixedValue,'type':2,'fixedValue':ctr.tmpfixedValue});
					ctr.tmpfixedValue = '';
				}
			};

			ctr.showHints = function(obj){
				var msg = "";
				var hintTitle = "";
				if (obj == 'Description'){
					msg = sbiModule_translate.load("sbi.crossnavigation.description.hint");
					hintTitle = sbiModule_translate.load("sbi.crossnavigation.description.hintTitle");
				}else{
					msg = sbiModule_translate.load("sbi.crossnavigation.breadcrumb.hint");
					hintTitle = sbiModule_translate.load("sbi.crossnavigation.breadcrumb.hintTitle");
				}

				$mdDialog.show(
						  $mdDialog
						    .alert({
						    	 locals:{},
						    	 clickOutsideToClose:true,
						    	 template:
						             '<md-dialog aria-label="Hint dialog">' +
						             '  <md-dialog-content>'+
						             '		<md-toolbar class="primaryToolbar">'+
						             '			<div class="md-toolbar-tools">'+
						             ' 				<h2>'+
						             '   				<span>'+hintTitle+'</span>'+
						             ' 				</h2>'+
						             '			</div>'+
						             '		</md-toolbar>'+
						             '    	<p>'+ msg+ '</p>'+
						             '  </md-dialog-content>' +
						             '  <md-dialog-actions>' +
						             '    <md-button ng-click="closeDialog()" class="md-raised">' +
						             '      Close' +
						             '    </md-button>' +
						             '  </md-dialog-actions>' +
						             '</md-dialog>',
						             controller: hintDialogController
						      })
						);

				function hintDialogController ($scope,$mdDialog) {
			        $scope.closeDialog = function() {
			          $mdDialog.hide();
			        }
				}
			}

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

						$scope.i18n.loadI18nMap().then(function() {

							for (var i = 0 ; i < ctr.list.length; i ++ ){
								ctr.list[i].name = s.i18n.getI18n(ctr.list[i].name);
							}

						}); // end of load I 18n


					},function(response){
						ctr.navigationList.loadingSpinner = false;
					});
				},
				loadSelectedNavigation : function(item){
					ctr.detailLoadingSpinner = true;
					sbiModule_restServices.promiseGet('1.0/crossNavigation/'+item.id+'/load', "", null)
					.then(function(response) {
						$angularListDetail.goToDetail();
						var data = response.data;
						ctr.detailLoadingSpinner = false;
						ctr.detail = data;
						ctr.crossmodality = $scope.crossModes[data.simpleNavigation.type];
						if(ctr.detail.simpleNavigation.popupOptions){
							ctr.popupOptions = JSON.parse(ctr.detail.simpleNavigation.popupOptions);
						}else ctr.popupOptions = {};


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

                	 var confirm = $mdDialog.confirm()
	                     .title(sbiModule_translate.load('kn.crossnavigation.delete'))
	                     .textContent(sbiModule_translate.load('kn.crossnavigation.confirm'))
	                     .targetEvent(event)
	                     .ok(sbiModule_translate.load('kn.generic.yes'))
	                     .cancel(sbiModule_translate.load('kn.generic.cancel'));

	               $mdDialog.show(confirm).then(function() {
	            	   sbiModule_restServices.promisePost('1.0/crossNavigation/remove', "", "{'id':"+item.id+"}")
						.then(function(response) {
							ctr.navigationList.loadNavigationList();
							$scope.showActionOK("sbi.crossnavigation.remove.ok");
						},function(response){
							$scope.showActionOK("sbi.crossnavigation.remove.ko");
						});
	               }, function() {});
				}
			};

			ctr.navigationList.loadNavigationList();

			ctr.newNavigation = function(){
				ctr.detail = newRecord();
				$angularListDetail.goToDetail();

			};



			ctr.saveFunc = function(){

				ctr.detail.simpleNavigation.type = ctr.crossmodality.value;
				if(ctr.detail.simpleNavigation.type == 2){
					ctr.detail.simpleNavigation.popupOptions = JSON.stringify(ctr.popupOptions);
				}
				sbiModule_restServices.promisePost('1.0/crossNavigation/save', "", ctr.detail)
				.then(function(response){
					$scope.showActionOK("sbi.crossnavigation.save.ok");
					ctr.navigationList.loadNavigationList();
					ctr.detail = newRecord();
					ctr.crossmodality = $scope.crossModes[0];
					$angularListDetail.goToList();
				},function(response){
					$scope.showActionOK(response.data.errors);
				});
			};
			ctr.cancelFunc = function(){
				ctr.detail = newRecord();
				ctr.crossmodality = $scope.crossModes[0];
				$angularListDetail.goToList();
			};

			function loadInputParameters(documentLabel,callbackFunction){
				sbiModule_restServices.promiseGet('1.0/documents/'+documentLabel+'/parameters', "", null)
				.then(function(response) {
					var data = response.data;
					var parameters = [];
					for(var i=0;i<data.results.length;i++){
						parameters.push({'id':data.results[i].id,'name':data.results[i].label,'type':1, 'parType':data.results[i].parType});
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
						parameters.push({'id':data[i].id,'name':data[i].name,'type':0, 'parType':data[i].type.valueCd});
					}
					callbackFunction(parameters);
				},function(response){});
			}

			ctr.listLeftDocuments = function(){
				ctr.listDocuments(function(item, listId, closeDialog){
					if(!ctr.detail.simpleNavigation)ctr.detail.simpleNavigation = {};
					ctr.detail.simpleNavigation.fromDocId = item.DOCUMENT_ID;
					ctr.detail.simpleNavigation.fromDoc = item.DOCUMENT_NAME;
					loadInputParameters(item.DOCUMENT_LABEL,function(data){
						ctr.detail.fromPars = data;
						loadOutputParameters(item.DOCUMENT_ID,function(data){
							ctr.detail.fromPars = ctr.detail.fromPars.concat(data);
							for (var idx in ctr.detail.toPars) {
								ctr.removeLink(ctr.detail.toPars[idx].id);
							}
							closeDialog();
						});
					});
				});
			};

			ctr.listRightDocuments = function(){
				ctr.listDocuments(function(item, listId, closeDialog){
					if(!ctr.detail.simpleNavigation)ctr.detail.simpleNavigation = {};
					ctr.detail.simpleNavigation.toDocId = item.DOCUMENT_ID;
					ctr.detail.simpleNavigation.toDoc = item.DOCUMENT_NAME;
					loadInputParameters(item.DOCUMENT_LABEL,function(data){
						ctr.detail.toPars = data;
						for (var idx in ctr.detail.toPars) {
							ctr.removeLink(ctr.detail.toPars[idx].id);
						}
						closeDialog();
					});
				});
			};

			ctr.listDocuments = function(clickOnSelectedDocFunction,item){
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
					scope.changeDocPage=function(searchValue, itemsPerPage, currentPageNumber , columnsSearch,columnOrdering, reverseOrdering){
						if(searchValue==undefined || searchValue.trim().lenght==0 ){
							searchValue='';
						}
						var item="Page="+currentPageNumber+"&ItemPerPage="+itemsPerPage+"&label=" + searchValue;
						scope.loadListDocuments(item);
					};
					scope.clickOnSelectedDoc = clickOnSelectedDoc;
					scope.translate = translate;
					scope.loading = true;
					scope.totalCount = 0
					scope.loadListDocuments = function(item){
						sbiModule_restServices.promiseGet("2.0/documents", "listDocument",item).then(
								function(response){
									scope.loading = false;
									scope.listDoc = response.data.item;
									scope.totalCount = response.data.itemCount;
								},

								function(response){
										sbiModule_restServices.errorHandler(response.data,"")
									}
								)
					}

				}
			};

			ctr.treeOptions = {
					beforeDrop: function(event) {
						if(ctr.selectedItem >= 0){
							//if(ctr.selectedItem != ""){
							var fromType =  event.source.cloneModel.parType;
							var fromName = event.source.cloneModel.name;
							var toType = ctr.detail.toPars[ctr.selectedItem].parType;
							var toName = ctr.detail.toPars[ctr.selectedItem].name;

							if (fromType && toType && fromType != toType) {
								sbiModule_messaging.showErrorMessage(fromName +' '+ sbiModule_translate.load("sbi.crossnavigation.crossparameters.typeProblem") + ' ' +toName, "Incompatible types");
							}
							else{
								ctr.detail.toPars[ctr.selectedItem].links = [event.source.cloneModel];
							}
							//}
						}
						return false;
					},
					dragStop: function(){
						ctr.unselectAll();
					}
			};

			ctr.treeOptions2 = {
				accept: function(sourceNodeScope, destNodesScope, destIndex){
					//alert(destNodesScope.depth());
					if(
							//destNodesScope.depth()==0
							//&&
							!ctr.hasLink(destIndex)){
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
				ctr.selectedItem = -1;
			};

			ctr.removeLink = function(id){
				for(var i=0;i<ctr.detail.toPars.length;i++){
					if(ctr.detail.toPars[i].id==id){
						ctr.detail.toPars[i].links=[];
					}
				}
			};

//			ctr.deletFixedValue = function(par){
//				var found = false;
//				var indexToDelete = undefined;
//				for(var i=0;i<ctr.detail.fromPars.length && !found;i++){
//					var forPar = ctr.detail.fromPars[i];
//					if(forPar.name == par.name){
//						indexToDelete = i;
//						found = true;
//					}
//				}
//				if(indexToDelete != undefined){
//					ctr.detail.fromPars.splice(indexToDelete,1);
//				}
//			}

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