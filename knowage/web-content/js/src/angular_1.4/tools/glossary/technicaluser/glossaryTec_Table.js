angular.module('glossaryTecnicalFunctionality').controller("GTTableController",["$scope","sbiModule_translate","sbiModule_restServices",'$timeout','$mdDialog',TControllerFunction]);

function TControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$timeout,$mdDialog){
	$scope.listTable=[];
	$scope.showSearchTablePreloader=false;
	$scope.sizeTable=0;
	$scope.selectedTable;
	$scope.selectedTableInfo={};
	$scope.tableWords=[];
	$scope.sbiGlTableWlist=[];
	
	$scope.loadTableInfo=function(item){
		sbiModule_restServices.promiseGet("1.0/glossary","getMetaTableInfo?META_TABLE_ID="+item.tableId).then(
				function(response){
					$scope.selectedTableInfo=response.data.metaTable;
					$scope.tableWords=response.data.words;
					$scope.sbiGlTableWlist=response.data.sbiGlTableWlist;
					$timeout(function(){
						$scope.expandAllTree("TreeColumn-Word-Table")
					},500)
				},
				function(response){
					sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.glossary.load.error"))
				});
	}
	
	$scope.tableLike=function(searchValue,itemsPerPage){
		var item="Page=1&ItemPerPage="+itemsPerPage+"&label=" + searchValue;
		$scope.loadTableList(item);
	}
	
	$scope.changeTablePage=function(newPageNumber,itemsPerPage,searchValue){
		if(searchValue==undefined || searchValue.trim().lenght==0 ){
			searchValue='';
		}
		var item="Page="+newPageNumber+"&ItemPerPage="+itemsPerPage+"&label=" + searchValue;
		$scope.loadTableList(item);
	}
	
	$scope.loadTableList=function(item){
		sbiModule_restServices.promiseGet("1.0/metaTable", "listMetaTable", item).then(
				function(response) {
					$scope.listTable = response.data.item;
					$scope.sizeTable=response.data.itemCount;
//						$scope.showSearchDatasetPreloader = false;
					
				}
				,function(response) {
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
				if(event.dest.nodesScope.$parent.$element[0].id=="wordTableTree"){
					elem.COLUMN_NAME=".SELF";
				}else{
					event.dest.nodesScope.$parent.expand();
					elem.COLUMN_NAME=event.dest.nodesScope.$parent.$modelValue.name;
				}
				 
				elem.WORD_ID = event.source.nodeScope.$modelValue.WORD_ID;
				elem.META_TABLE_ID=$scope.selectedTable.tableId;
				
//				elem.ORGANIZATION=datasetAss.infoSelectedDataSet.id.organization;
				 
//				showPreloader();
				sbiModule_restServices.promisePost("1.0/glossary", "addMetaTableWlist", elem).then(
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
	
	$scope.treeOptionsTable_Word = {

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
	
	$scope.tableColumnTreeOptionsWord = {
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
						req="WORD_ID=" +word.WORD_ID+"&TABLE_ID="+$scope.selectedTableInfo.tableId+"&COLUMN=.SELF";
					}else{
						req="WORD_ID=" +word.WORD_ID+"&TABLE_ID="+$scope.selectedTableInfo.tableId+"&COLUMN="+item.name;
					}
					sbiModule_restServices.promiseDelete("1.0/glossary", "deleteMetaTableWlist",req).then(
							function(response){
								if(item==null){
									$scope.tableWords.splice($scope.tableWords.indexOf(word), 1);
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
	
	$scope.tableWordSpeedMenuOpt = [ 
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