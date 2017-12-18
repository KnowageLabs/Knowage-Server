angular.module('glossaryTecnicalFunctionality').controller("GTBusinessClassController",["$scope","sbiModule_translate","sbiModule_restServices","$timeout","$mdDialog",BCControllerFunction]);

function BCControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$timeout,$mdDialog ){
	$scope.listBusinessClass=[];
	$scope.showSearchBusinessClassPreloader=false;
	$scope.sizeBusinessClass=0;
	$scope.selectedBusinessClass;
	$scope.selectedBcInfo={};
	$scope.BcWords=[];
	$scope.sbiGlBcWlist=[];
		
	$scope.loadBusinessClassInfo=function(item){
		sbiModule_restServices.promiseGet("1.0/glossary","getMetaBcInfo?META_BC_ID="+item.id).then(
				function(response){
					$scope.selectedBcInfo=response.data.metaBc;
					$scope.BcWords=response.data.words;
					$scope.sbiGlBcWlist=response.data.sbiGlBnessClsWlist;
					$timeout(function(){
						$scope.expandAllTree("TreeColumn-Word-Bc")
					},500)
				},
				function(response){
					sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.glossary.load.error"))
				});
	}

	$scope.businessClassLike=function(searchValue,itemsPerPage){
		var item="Page=1&ItemPerPage="+itemsPerPage+"&label=" + searchValue;
		$scope.loadBusinessClassList(item);
	}
	 
	$scope.changeBusinessClassPage=function(newPageNumber,itemsPerPage,searchValue){
		if(searchValue==undefined || searchValue.trim().lenght==0 ){
			searchValue='';
		}
		var item="Page="+newPageNumber+"&ItemPerPage="+itemsPerPage+"&label=" + searchValue;
		$scope.loadBusinessClassList(item);
	}

	$scope.loadBusinessClassList=function(item){
		sbiModule_restServices.promiseGet("1.0/metaBC", "listMetaBC", item).then(
				function(response) {
					
					$scope.listBusinessClass = response.data.item;
					$scope.sizeBusinessClass=response.data.itemCount;
//						$scope.showSearchDatasetPreloader = false;
					
				}
				,function(response) {
//					global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
					sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.glossary.load.error"))
//					$scope.showSearchDatasetPreloader = false;
				})
	}
	
	$scope.TreeOptions = {

			accept : function(sourceNodeScope, destNodesScope, destIndex) {
				return false;
			},
			dropped : function(event){
				if(event.source.nodesScope.$id==event.dest.nodesScope.$id){
					return false;
				}
				var elem = {};
				if(event.dest.nodesScope.$parent.$element[0].id=="wordBCTree"){
					elem.COLUMN_NAME=".SELF";
				}else{
					event.dest.nodesScope.$parent.expand();
					elem.COLUMN_NAME=event.dest.nodesScope.$parent.$modelValue.name;
				}
				 
				elem.WORD_ID = event.source.nodeScope.$modelValue.WORD_ID;
				elem.META_BC_ID=$scope.selectedBusinessClass.id;
				
//				elem.ORGANIZATION=datasetAss.infoSelectedDataSet.id.organization;
				 
//				showPreloader();
				sbiModule_restServices.promisePost("1.0/glossary", "addMetaBcWlist", elem).then(
				 function(data, status, headers,config) {
//						hidePreloader();
						},
				function(response, status, headers, config) {
						sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.glossary.error.save"));
//						hidePreloader();
						}); 
			},
			 
			dragStart : function(event) {
			},
			dragStop : function(event) {
			}
	};
	 
	$scope.treeOptionsBC_Word = {

			accept : function(sourceNodeScope, destNodesScope, destIndex) {
				for(var i=0;i<destNodesScope.$modelValue.length;i++){
					if(destNodesScope.$modelValue[i].WORD_ID==sourceNodeScope.$modelValue.WORD_ID){
						return false;
					}
				}
				return  true;
			},
			beforeDrop : function(event) {
			},
			dragStart : function(event) {
			},
			dragStop : function(event) {
			}
	};
	
	$scope.BCColumnTreeOptionsWord = {
			accept : function(sourceNodeScope, destNodesScope, destIndex) {
				if(destNodesScope.depth()==0){
					return false;
				}
				for(var i=0;i<destNodesScope.$modelValue.length;i++){
					if(destNodesScope.$modelValue[i].WORD_ID==sourceNodeScope.$modelValue.WORD_ID){
						return false;
					}
				}
				return  true;

			},
			beforeDrop : function(event) {
			},
			dragStart : function(event) {
			},
			dragStop : function(event) {
			}
	};
	
$scope.removeWordFromData=function(item,word){
		
		var confirm = $mdDialog.confirm().title(
				sbiModule_translate.load("sbi.glossary.word.delete")).content(
						sbiModule_translate.load("sbi.glossary.word.delete.message")).ariaLabel(
						'Lucky day').ok(sbiModule_translate.load("sbi.generic.delete")).cancel(
								sbiModule_translate.load("sbi.myanalysis.delete.cancel"));

		$mdDialog.show(confirm).then(
				function() {
					var req="";
					if(item==null){
						req="WORD_ID=" +word.WORD_ID+"&BC_ID="+$scope.selectedBusinessClass.id+"&COLUMN=.SELF";
					}else{
						req="WORD_ID=" +word.WORD_ID+"&BC_ID="+$scope.selectedBusinessClass.id+"&COLUMN="+item.name;
					}
					sbiModule_restServices.promiseDelete("1.0/glossary", "deleteMetaBcWlist",req).then(
							function(response){
								if(item==null){
									$scope.BcWords.splice($scope.BcWords.indexOf(word), 1);
								 }else{
									 item.word.splice(item.word.indexOf(word), 1);
								}
								global.showToast(sbiModule_translate.load("sbi.glossary.word.delete.success"),3000);
							},
							function(response){
								sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.glossary.word.delete.error"))
								});
 
				} );
	}
	
	$scope.BcWordSpeedMenuOpt = [ 
           			               	{
          			               		label : sbiModule_translate.load('sbi.generic.delete'),
          			               		icon	:'fa fa-times'	,
          			               		backgroundColor:'transparent',
          			               		color:'black',
          			               		action : function(item,event) {
          			               		$scope.removeWordFromData(null,item);
          			               			}
          			               	}
          			             
          			             ];
}