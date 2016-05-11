var app = angular.module('glossaryTecnicalFunctionality', [ 'ngMaterial','ui.tree', 'sbiModule','angularUtils.directives.dirPagination','glossary_tree','angular_list','document_viewer']);

app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]);




app.controller('ControllerDataSet', [ "sbiModule_translate", "sbiModule_restServices", "$q", "$scope", "$mdDialog", "$filter", "$timeout", "$mdToast", funzione_associazione_dataset ]);
app.controller('Controller', [ "sbiModule_translate", "sbiModule_restServices", "$q", "$scope", "$mdDialog", "$filter", "$timeout", "$mdToast", funzione_associazione_documenti ]);
app.controller('Controller_navigation', [ "sbiModule_translate", "sbiModule_restServices", "$q", "$scope", "$mdDialog", "$filter", "$timeout", "$mdToast","$window","sbiModule_config",'$documentViewer', funzione_navigazione ]);
app.controller('Controller_tec', [ "$scope","sbiModule_translate","sbiModule_restServices","$mdToast","$timeout", funzione_tec ]);


var global;
var navi;
var docAss;
var dsAss;

//--------------------------------------------------------------------------globale------------------------------------------------------------
function funzione_tec($scope,sbiModule_translate,sbiModule_restServices,$mdToast,$timeout) {
	global=this;
	$scope.translate=sbiModule_translate;
	global.glossary;
	global.selectedTab="";

	global.initializer={};
//	global.initializer.navigation={state:false,scope:navi};
//	global.initializer.docAssoc={state:false,scope:docAss};
//	global.initializer.datasetAssoc={state:false,scope:dsAss};

	global.init=function(component){
		global.selectedTab=component;
		if(component=='glossTreePage')return;
		
		if(component=="navigation"){
			console.log("refresh navi")
			global.initializer[component].scope.loadNavItem();
		}else if(global.initializer[component].state==false){
			console.log("Initialize "+component)
			global.initializer[component].state=true;
			global.initializer[component].scope.init();
		}
//		else{
//			if(component=="navigation"){
//				console.log("refresh navi")
//				global.initializer[component].scope.loadNavItem();
//			}
//		}
		
	}


	global.showToast=function(text, time) {
		var timer = time == undefined ? 6000 : time;

		console.log(text)
		$mdToast.show($mdToast.simple().content(text).position('top').action(
		'OK').highlightAction(false).hideDelay(timer));
	}

	global.getAllGloss=function() {
		sbiModule_restServices.get("1.0/glossary", "listGlossary").success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						console.log(data.errors[0].message);
					} else {
						global.glossary= data;
					}

				}).error(function(data, status, headers, config) {
					console.log("Glossary non Ottenuti " + status);


				})

	}
	global.getAllGloss();
	
	global.expandAllTree= function(tree){
		console.log("expand")
		if(angular.element(document.getElementById(tree)).scope()!=undefined)
		angular.element(document.getElementById(tree)).scope().expandAll();
	}


}


//--------------------------------------------------------------------------assoc_doc----------------------------------------------------------
function funzione_associazione_documenti(sbiModule_translate, sbiModule_restServices, $q, $scope, $mdDialog, $filter,$timeout, $mdToast) {
	docAss=this;
	docAss.listDoc;
	docAss.sizeDoc=0;
	docAss.searchDoc="";
	docAss.showPreloader = false;
	docAss.preloaderTree= false;
	docAss.selectedGloss;
	docAss.selectedDocument;
	docAss.words = [];
	docAss.tmpDocSearch = "";
	docAss.prevDocSearch = "-1";
	global.initializer.docAssoc={state:false,scope:docAss};
	docAss.init=function(){
//		changeItemPP();
//		docAss.ChangeDocPage(1);
	}

	docAss.loadDocList=function(item){
		sbiModule_restServices.get("2.0/documents", "listDocument", item+"&scope=GLOSSARY").success(
				function(data, status, headers, config) {
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						global.showToast(sbiModule_translate.load("sbi.glossary.load.error"),3000);

					} else {
						console.log("list doc ottenute")
						docAss.listDoc = data.item;
						docAss.sizeDoc=data.itemCount;
						docAss.showSearchDocPreloader = false;
					}

				}).error(function(data, status, headers, config) {
					global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
					docAss.showSearchDocPreloader = false;
				})
	}

	docAss.ChangeDocPage=function(newPageNumber,itemsPerPage,searchValue){
		if(searchValue==undefined || searchValue.trim().lenght==0 ){
			searchValue='';
		}
		var item="Page="+newPageNumber+"&ItemPerPage="+itemsPerPage+"&label=" + searchValue;
		docAss.loadDocList(item)
	}


	docAss.DocumentLike= function(ele,itemsPerPage){
		console.log("DocumentLike "+ele);
		
			docAss.showSearchDocPreloader = true;
			var item="Page=1&ItemPerPage="+itemsPerPage+"&label=" + ele;
			docAss.loadDocList(item)
		}


	docAss.prevSWSG = "";
	docAss.tmpSWSG = "";
	docAss.SearchWordInSelectedGloss= function(ele){
		console.log("SearchWordInSelectedGloss  "+ele);
		docAss.tmpSWSG = ele;
		$timeout(function() {
			if (docAss.tmpSWSG != ele || docAss.prevSWSG == ele) {
				console.log("interrompo la ricerca  di ele " + ele)
				return;
			}

			docAss.prevSWSG = ele;

			console.log("cerco "+ele)
			showPreloader("preloaderTree");
			sbiModule_restServices.get("1.0/glossary", "glosstreeLike", "WORD=" + ele+"&GLOSSARY_ID="+docAss.selectedGloss.GLOSSARY_ID).success(
					function(data, status, headers, config) {
						console.log("glosstreeLike Ottenuti " + status)
						console.log(data)

						if (data.hasOwnProperty("errors")) {
							showErrorToast(data.errors[0].message);
							global.showToast(
									sbiModule_translate.load("sbi.glossary.load.error"),
									3000);

						} else {
							docAss.selectedGloss=data.GlossSearch;

							if(ele!=""){
								$timeout(function() {
									global.expandAllTree("GlossTree");
								},500);
							}
						}
						hidePreloader("preloaderTree");

					}).error(function(data, status, headers, config) {
						console.log("glosstreeLike non Ottenuti " + status);
						global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

						hidePreloader("preloaderTree");
					})

		}, 1000);
	}

	docAss.removeWord= function(event,word){
		console.log("remove word");
		console.log(word);

		var confirm = $mdDialog.confirm().title(
				sbiModule_translate.load("sbi.glossary.word.delete")).content(
						sbiModule_translate.load("sbi.glossary.word.delete.message")).ariaLabel(
						'Lucky day').ok(sbiModule_translate.load("sbi.generic.delete")).cancel(
								sbiModule_translate.load("sbi.myanalysis.delete.cancel"));

		$mdDialog.show(confirm).then(
				function() {

					showPreloader();
					sbiModule_restServices.remove("1.0/glossary", "deleteDocWlist",
							"WORD_ID=" + word.WORD_ID+"&DOCUMENT_ID="+docAss.selectedDocument.DOCUMENT_ID)
							.success(
									function(data, status, headers,
											config) {
										console.log("Word eliminato")
										console.log(data)
										if (data.hasOwnProperty("errors")) {
											showErrorToast(data.errors[0].message)
											global.showToast(sbiModule_translate.load("sbi.glossary.word.delete.error"),3000);

										} else {
											docAss.words.splice(docAss.words.indexOf(word), 1);
											global.showToast(sbiModule_translate.load("sbi.glossary.word.delete.success"),3000);


										}
										hidePreloader();

									})
									.error(
											function(data, status, headers,
													config) {
												console.log("WORD NON ELMINIATO "+ status);
												showErrorToast("word non eliminato "+ status);
												global.showToast(sbiModule_translate.load("sbi.glossary.word.delete.error"),3000);
												hidePreloader();
											})


				}, function() {
					console.log('Annullo.');
				});




	}

	docAss.loadDocumentInfo= function(item){
		console.log("loadDocumentInfo");
		docAss.words=[];
		docAss.searchDoc="";
		showPreloader("preloader");
		sbiModule_restServices
		.get(
				"1.0/glossary","getDocumentInfo","DOCUMENT_ID=" + item.DOCUMENT_ID )
				.success(
						function(data, status, headers, config) {
							console.log("loadDocumentInfo ottnuti")
							console.log(data)

							if (data.hasOwnProperty("errors")) {
								showErrorToast(data.errors[0].message);
								global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

							} else {
								docAss.words=data.word;



							}

							hidePreloader("preloader");
						}).error(function(data, status, headers, config) {
							console.log("nodi non ottenuti " + status);
							global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
							if (togg != undefined) {
								togg.expand();
								hidePreloader("preloader");
							}
						})
	}

	docAss.showSelectedGlossary = function(gloss) {
		if(docAss.selectedGloss!=undefined && docAss.selectedGloss.GLOSSARY_ID==gloss.GLOSSARY_ID){return;}


		docAss.selectedGloss=gloss;
		docAss.getGlossaryNode(docAss.selectedGloss, null);
	}

	docAss.getGlossaryNode = function(gloss, node, togg) {
		console.log("getGlossaryNode")
		console.log(node)
		console.log(gloss)
		console.log(togg);
		var PARENT_ID = (node == null ? null : node.CONTENT_ID);
		var GLOSSARY_ID = (gloss == null ? null : gloss.GLOSSARY_ID);

		showPreloader("preloaderTree");
		sbiModule_restServices
		.get(
				"1.0/glossary",
				"listContents",
				"GLOSSARY_ID=" + GLOSSARY_ID + "&PARENT_ID="
				+ PARENT_ID)
				.success(
						function(data, status, headers, config) {
							console.log("nodi ottnuti")
							console.log(data)

							if (data.hasOwnProperty("errors")) {
								showErrorToast(data.errors[0].message);
								global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

							} else {

								if(togg==undefined || togg.collapsed){
									// check if parent is node or glossary
									node==null ? gloss.SBI_GL_CONTENTS = data : node.CHILD = data;
								}else{
									console.log("riordino manualmente");
									if(node!=null){
										node.CHILD.sort(function(a,b) {return (a.CONTENT_NM > b.CONTENT_NM) ? 1 : ((b.CONTENT_NM > a.CONTENT_NM) ? -1 : 0);} ); 
									}
								}

								if (togg != undefined) {
									togg.expand();
								}
								if(node!=null && node.hasOwnProperty("preloader")){
									node.preloader = false;
								}
								hidePreloader("preloaderTree");
							}
						}).error(function(data, status, headers, config) {
							console.log("nodi non ottenuti " + status);
							global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
							if (togg != undefined) {
								togg.expand();
								hidePreloader("preloaderTree");
							}
						})
	}

	docAss.toggle = function(scope, item, gloss) {

		console.log("toggle")

		if(docAss.searchWD!="" && docAss.searchWD!=undefined ){
			scope.toggle();
			return;	
		}

		console.log(scope)
		item.preloader = true;
		if (scope.collapsed) {
			docAss.getGlossaryNode(gloss, item, scope)
		} else {
			scope.toggle();
			item.preloader = false;
		}
	};

	function getAllDoc() {
		console.log("getAllDoc")
		showPreloader();
		sbiModule_restServices.get("1.0/glossary", "listDocument").success(
				function(data, status, headers, config) {
					console.log("doc Ottenuti " + status)
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						showErrorToast(data.errors[0].message);
						global.showToast(sbiModule_translate.load("sbi.glossary.load.error"),
								3000);

					} else {
						docAss.listDoc = data;
					}

					hidePreloader();
				}).error(function(data, status, headers, config) {
					console.log("Glossary non Ottenuti " + status);
					showErrorToast('Ci sono errori! \n status ' + status);
					global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

					hidePreloader();
				})

	}





	docAss.docWordSpeedMenuOpt = [ 
			               	{
			               		label : sbiModule_translate.load('sbi.generic.delete'),
			               		icon	:'fa fa-times'	,
			               		backgroundColor:'transparent',
			               		color:'black',
			               		action : function(item,event) {
			               			docAss.removeWord(event,item);
			               			}
			               	}
			             
			             ];
	
	docAss.TreeOptionsWord = {

			accept : function(sourceNodeScope, destNodesScope, destIndex) {
				var present=false;
				for(var i=0;i<destNodesScope.$modelValue.length;i++){
					if(destNodesScope.$modelValue[i].WORD_ID==sourceNodeScope.$modelValue.WORD_ID){
						console.log("word present")	;
						present= true;
						break;
					}
				}
				if(present){console.log("present"); return false;}
				else{console.log("accepted");
				return true;}



			},
			beforeDrop : function(event) {
				console.log("dropped")
			},
			dragStart : function(event) {
			},
			dragStop : function(event) {
			}
	};

	docAss.TreeOptions = {

			accept : function(sourceNodeScope, destNodesScope, destIndex) {
				console.log("accept")
				return false;
			},
			beforeDrop : function(event) {
				console.log("beforeDrop TreeOptions")
				console.log(event)

				if(event.source.nodesScope.$id==event.dest.nodesScope.$id){
					console.log("no drop ")
					return false;
				}

				var elem = {};

				elem.WORD_ID = event.source.nodeScope.$modelValue.WORD_ID;
				elem.DOCUMENT_ID=docAss.selectedDocument.DOCUMENT_ID;
				console.log(elem)

				showPreloader();
				sbiModule_restServices
				.post("1.0/glossary", "addDocWlist", elem)
				.success(
						function(data, status, headers,
								config) {

							if (data
									.hasOwnProperty("errors")) {
								showErrorToast(data.errors[0].message)
								global.showToast(
										sbiModule_translate
										.load("sbi.glossary.error.save"),
										3000);
							} else if (data.Status == "NON OK") {
								global.showToast(
										sbiModule_translate
										.load(data.Message),
										3000);
							} else {
//								global.showToast(
//								sbiModule_translate
//								.load("sbi.glossary.success.save"),
//								3000);


//								event.dest.nodesScope.insertNode(event.dest.index,elem);
							}



							hidePreloader();
						})
						.error(
								function(data, status, headers,
										config) {
									hidePreloader();
									global.showToast(
											sbiModule_translate
											.load("sbi.glossary.error.save"),
											3000);
								});



			},
			dragStart : function(event) {
			},
			dragStop : function(event) {
			}
	};


	function showErrorToast(err, time) {
		var timer = time == undefined ? 6000 : time
				console.log("ci sono errori")
				console.log(err)
				hidePreloader();
		// $mdToast.show($mdToast.simple().content('Ci sono errori! \n ' + err)
		// .position('top').action('OK').highlightAction(false).hideDelay(
		// timer));
	}



	function showPreloader(pre) {
		switch(pre){
		case 'preloaderTree':docAss.preloaderTree=true;
		break;
		default:docAss.showPreloader = true;
		break;
		}
	}

	function hidePreloader(pre) {
		switch(pre){
		case 'preloaderTree':docAss.preloaderTree=false;
		break;
		default:docAss.showPreloader = false;
		break;
		}
	}

}


//--------------------------------------------------------------------------assoc_dataset----------------------------------------------------------
function funzione_associazione_dataset(sbiModule_translate, sbiModule_restServices, $q, $scope, $mdDialog, $filter,$timeout, $mdToast) {
	datasetAss=this;
	datasetAss.listDataset;
	datasetAss.sizeDataset=0;
	datasetAss.searchDataset=""; 
	datasetAss.showPreloader = false;
	datasetAss.preloaderTreeDS= false;
	datasetAss.selectedGloss;
	datasetAss.selectedDataset;
	datasetAss.words = [];
	datasetAss.tmpDatasetSearch = "";
	datasetAss.prevDatasetSearch = "-1";
	global.initializer.datasetAssoc={state:false,scope:datasetAss};
	datasetAss.init=function(){
//		changeItemPP();
//		datasetAss.ChangeDatasetPage(1);
	}


	datasetAss.loadDatasetList=function(item){
		sbiModule_restServices.get("2.0/datasets", "listDataset", item).success(
				function(data, status, headers, config) {
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						global.showToast(sbiModule_translate.load("sbi.glossary.load.error"),3000);

					} else {
						console.log("list Dataset ottenute")
						datasetAss.listDataset = data.item;
						datasetAss.sizeDataset=data.itemCount;
						datasetAss.showSearchDatasetPreloader = false;
					}

				}).error(function(data, status, headers, config) {
					global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
					datasetAss.showSearchDatasetPreloader = false;
				})
	}


	datasetAss.ChangeDatasetPage=function(newPageNumber,itemsPerPage,searchValue){
		if(searchValue==undefined || searchValue.trim().lenght==0 ){
			searchValue='';
		}
		var item="Page="+newPageNumber+"&ItemPerPage="+itemsPerPage+"&label=" + searchValue;
		datasetAss.loadDatasetList(item);
	}

	

	datasetAss.DatasetLike= function(ele,itemsPerPage){
		console.log("DatasetLike "+ele);
		
		datasetAss.showSearchDatasetPreloader = true;
			var item="Page=1&ItemPerPage="+itemsPerPage+"&label=" + ele;
			datasetAss.loadDatasetList(item);
		}
	
	

	datasetAss.prevSWSG = "";
	datasetAss.tmpSWSG = "";
	datasetAss.SearchWordInSelectedGloss= function(ele){
		console.log("SearchWordInSelectedGloss  "+ele);
		datasetAss.tmpSWSG = ele;
		$timeout(function() {
			if (datasetAss.tmpSWSG != ele || datasetAss.prevSWSG == ele) {
				console.log("interrompo la ricerca  di ele " + ele)
				return;
			}

			datasetAss.prevSWSG = ele;

			console.log("cerco "+ele)
			showPreloader("preloaderTreeDS");
			sbiModule_restServices.get("1.0/glossary", "glosstreeLike", "WORD=" + ele+"&GLOSSARY_ID="+datasetAss.selectedGloss.GLOSSARY_ID).success(
					function(data, status, headers, config) {
						console.log("glosstreeLike Ottenuti " + status)
						console.log(data)

						if (data.hasOwnProperty("errors")) {
							showErrorToast(data.errors[0].message);
							global.showToast(
									sbiModule_translate.load("sbi.glossary.load.error"),
									3000);

						} else {
							datasetAss.selectedGloss=data.GlossSearch;

							if(ele!=""){
								$timeout(function() {
									global.expandAllTree("GlossTreeDS");
								},500);
							}
						}
						hidePreloader("preloaderTreeDS");

					}).error(function(data, status, headers, config) {
						console.log("glosstreeLike non Ottenuti " + status);
						global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

						hidePreloader("preloaderTreeDS");
					})

		}, 1000);
	}

	datasetAss.removeWord= function(item,word){
		console.log("remove word");
		console.log(item);

		var confirm = $mdDialog.confirm().title(
				sbiModule_translate.load("sbi.glossary.word.delete")).content(
						sbiModule_translate.load("sbi.glossary.word.delete.message")).ariaLabel(
						'Lucky day').ok(sbiModule_translate.load("sbi.generic.delete")).cancel(
								sbiModule_translate.load("sbi.myanalysis.delete.cancel"));

		$mdDialog.show(confirm).then(
				function() {
					var req="";
					if(item==null){
						req="WORD_ID=" +word.WORD_ID+"&DATASET_ID="+datasetAss.infoSelectedDataSet.id.dsId+"&ORGANIZATION="+datasetAss.infoSelectedDataSet.id.organization+"&COLUMN=.SELF";
					}else{
						req="WORD_ID=" +word.WORD_ID+"&DATASET_ID="+item.datasetId+"&ORGANIZATION="+item.organization+"&COLUMN="+item.alias;
					}
					showPreloader();
					sbiModule_restServices.remove("1.0/glossary", "deleteDatasetWlist",req)
							.success(
									function(data, status, headers,
											config) {
										console.log("Word eliminato")
										console.log(data)
										if (data.hasOwnProperty("errors")) {
											showErrorToast(data.errors[0].message)
											global.showToast(sbiModule_translate.load("sbi.glossary.word.delete.error"),3000);

										} else {
											if(item==null){
												datasetAss.SelfWords.splice(datasetAss.SelfWords.indexOf(word), 1);
												req="WORD_ID=" +word.WORD_ID+"&DATASET_ID="+datasetAss.infoSelectedDataSet.id.dsId+"&ORGANIZATION="+datasetAss.infoSelectedDataSet.id.organization+"&COLUMN=.SELF";
											}else{
												item.word.splice(item.word.indexOf(word), 1);
												}
											
											global.showToast(sbiModule_translate.load("sbi.glossary.word.delete.success"),3000);
										}
										hidePreloader();
									})
									.error(
											function(data, status, headers,
													config) {
												console.log("WORD NON ELMINIATO "+ status);
												showErrorToast("word non eliminato "+ status);
												global.showToast(sbiModule_translate.load("sbi.glossary.word.delete.error"),3000);
												hidePreloader();
											})
				}, function() {
					console.log('Annullo.');
				});
	}

	datasetAss.loadDatasetInfo= function(item){
		console.log("loadDatasetInfo");
		datasetAss.words=[];
		datasetAss.searchDataset="";

		showPreloader("preloader");
		sbiModule_restServices
		.get("1.0/glossary","getDataSetInfo?DATASET_ID="+item.id.dsId+"&ORGANIZATION="+item.id.organization)
				.success(
						function(data, status, headers, config) {
							console.log("loadDatasetInfo ottnuti")
							console.log(data)

							if (data.hasOwnProperty("errors")) {
								showErrorToast(data.errors[0].message);
								global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

							} else {
									datasetAss.infoSelectedDataSet=data.DataSet;
									datasetAss.words=data.SbiGlDataSetWlist;
									datasetAss.SelfWords=data.Word;
									console.log('datasetAss.SelfWords',datasetAss.SelfWords)
									$timeout(function() {
										global.expandAllTree("Tree-Word-Dataset");
									},500);
								
						}

						hidePreloader("preloader");
	}).error(function(data, status, headers, config) {
		console.log("nodi non ottenuti " + status);
		global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
		if (togg != undefined) {
			togg.expand();
			hidePreloader("preloader");
		}
	})
}

datasetAss.showSelectedGlossary = function(gloss) {
	if(datasetAss.selectedGloss!=undefined && datasetAss.selectedGloss.GLOSSARY_ID==gloss.GLOSSARY_ID){return;}
	datasetAss.selectedGloss=gloss;
	datasetAss.getGlossaryNode(datasetAss.selectedGloss, null);
}

datasetAss.getGlossaryNode = function(gloss, node, togg) {
	console.log("getGlossaryNode")
	console.log(node)
	console.log(gloss)
	console.log(togg);
	var PARENT_ID = (node == null ? null : node.CONTENT_ID);
	var GLOSSARY_ID = (gloss == null ? null : gloss.GLOSSARY_ID);

	showPreloader("preloaderTreeDS");
	sbiModule_restServices
	.get(
			"1.0/glossary",
			"listContents",
			"GLOSSARY_ID=" + GLOSSARY_ID + "&PARENT_ID="
			+ PARENT_ID)
			.success(
					function(data, status, headers, config) {
						console.log("nodi ottnuti")
						console.log(data)

						if (data.hasOwnProperty("errors")) {
							showErrorToast(data.errors[0].message);
							global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

						} else {

							if(togg==undefined || togg.collapsed){
								// check if parent is node or glossary
								node==null ? gloss.SBI_GL_CONTENTS = data : node.CHILD = data;
							}else{
								console.log("riordino manualmente");
								if(node!=null){
									node.CHILD.sort(function(a,b) {return (a.CONTENT_NM > b.CONTENT_NM) ? 1 : ((b.CONTENT_NM > a.CONTENT_NM) ? -1 : 0);} ); 
								}
							}

							if (togg != undefined) {
								togg.expand();
							}
							if(node!=null && node.hasOwnProperty("preloader")){
								node.preloader = false;
							}
							hidePreloader("preloaderTreeDS");
						}
					}).error(function(data, status, headers, config) {
						console.log("nodi non ottenuti " + status);
						global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
						if (togg != undefined) {
							togg.expand();
							hidePreloader("preloaderTreeDS");
						}
					})
}



datasetAss.toggle = function(scope, item, gloss) {

	console.log("toggle")

	if(datasetAss.searchWDS!="" && datasetAss.searchWDS!=undefined ){
		scope.toggle();
		return;	
	}

	console.log(scope)
	item.preloader = true;
	if (scope.collapsed) {
		datasetAss.getGlossaryNode(gloss, item, scope)
	} else {
		scope.toggle();
		item.preloader = false;
	}
};

function getAllDataset() {
	console.log("getAllDataset")
	showPreloader();
	sbiModule_restServices.get("1.0/glossary", "listDataset").success(
			function(data, status, headers, config) {
				console.log("Dataset Ottenuti " + status)
				console.log(data)
				if (data.hasOwnProperty("errors")) {
					showErrorToast(data.errors[0].message);
					global.showToast(sbiModule_translate.load("sbi.glossary.load.error"),
							3000);

				} else {
					datasetAss.listDataset = data;
				}

				hidePreloader();
			}).error(function(data, status, headers, config) {
				console.log("Glossary non Ottenuti " + status);
				showErrorToast('Ci sono errori! \n status ' + status);
				global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

				hidePreloader();
			})

}




datasetAss.datasetWordSpeedMenuOpt = [ 
  			               	{
  			               		label : sbiModule_translate.load('sbi.generic.delete'),
  			               		icon	:'fa fa-times'	,
  			               		backgroundColor:'transparent',
  			               		color:'black',
  			               		action : function(item,event) {
  			               		datasetAss.removeWord(null,item);
  			               			}
  			               	}
  			             
  			             ];

datasetAss.TreeOptionsWord = {

		accept : function(sourceNodeScope, destNodesScope, destIndex) {
	console.log(destNodesScope)

	if(destNodesScope.depth()==0){
		return false;
	}
	
			for(var i=0;i<destNodesScope.$modelValue.length;i++){
				if(destNodesScope.$modelValue[i].WORD_ID==sourceNodeScope.$modelValue.WORD_ID){
					console.log("word present")	;
					return false;
				}
			}
			

return  true;

		},
		beforeDrop : function(event) {
			console.log("dropped")
		},
		dragStart : function(event) {
		},
		dragStop : function(event) {
		}
};



datasetAss.TreeOptionsDataset_Word = {

		accept : function(sourceNodeScope, destNodesScope, destIndex) {
	console.log(destNodesScope)

	
	
			for(var i=0;i<destNodesScope.$modelValue.length;i++){
				if(destNodesScope.$modelValue[i].WORD_ID==sourceNodeScope.$modelValue.WORD_ID){
					console.log("word present")	;
					return false;
				}
			}
			

return  true;

		},
		beforeDrop : function(event) {
			console.log("dropped")
		},
		dragStart : function(event) {
		},
		dragStop : function(event) {
		}
};



datasetAss.TreeOptions = {

		accept : function(sourceNodeScope, destNodesScope, destIndex) {
			console.log("accept")
			return false;
		},
		beforeDrop : function(event) {
			
			console.log("beforeDrop TreeOptions")
			console.log(event)
			
			if(event.source.nodesScope.$id==event.dest.nodesScope.$id){
				console.log("no drop ")
				return false;
			}

			var elem = {};

			if(event.dest.nodesScope.$parent.$element[0].id=="wordDSTree"){
				elem.COLUMN_NAME=".SELF";
				}else{
					event.dest.nodesScope.$parent.expand();
					elem.COLUMN_NAME=event.dest.nodesScope.$parent.$modelValue.alias;
				}
			
			
			elem.WORD_ID = event.source.nodeScope.$modelValue.WORD_ID;
			elem.DATASET_ID=datasetAss.selectedDataset.id.dsId;
			
			elem.ORGANIZATION=datasetAss.infoSelectedDataSet.id.organization;
			console.log(elem)
			
			
			showPreloader();
			sbiModule_restServices
			.post("1.0/glossary", "addDataSetWlist", elem)
			.success(
					function(data, status, headers,
							config) {

						if (data
								.hasOwnProperty("errors")) {
							showErrorToast(data.errors[0].message)
							global.showToast(
									sbiModule_translate
									.load("sbi.glossary.error.save"),
									3000);
						} else if (data.Status == "NON OK") {
							global.showToast(
									sbiModule_translate
									.load(data.Message),
									3000);
						} else {
//							global.showToast(
//							sbiModule_translate
//							.load("sbi.glossary.success.save"),
//							3000);


//							event.dest.nodesScope.insertNode(event.dest.index,elem);
						}



						hidePreloader();
					})
					.error(
							function(data, status, headers,
									config) {
								hidePreloader();
								global.showToast(
										sbiModule_translate
										.load("sbi.glossary.error.save"),
										3000);
							});



		},
		dragStart : function(event) {
		},
		dragStop : function(event) {
		}
};


function showErrorToast(err, time) {
	var timer = time == undefined ? 6000 : time
			console.log("ci sono errori")
			console.log(err)
			hidePreloader();
	// $mdToast.show($mdToast.simple().content('Ci sono errori! \n ' + err)
	// .position('top').action('OK').highlightAction(false).hideDelay(
	// timer));
}



function showPreloader(pre) {
	switch(pre){
	case 'preloaderTreeDS':datasetAss.preloaderTreeDS=true;
	break;
	default:datasetAss.showPreloader = true;
	break;
	}
}

function hidePreloader(pre) {
	switch(pre){
	case 'preloaderTreeDS':datasetAss.preloaderTreeDS=false;
	break;
	default:datasetAss.showPreloader = false;
	break;
	}
}

}


//--------------------------------------------------------------------------navigazione--------------------------------------------------------

function funzione_navigazione(sbiModule_translate, sbiModule_restServices, $q, $scope, $mdDialog, $filter,$timeout, $mdToast,$window,sbiModule_config,$documentViewer) {
	navi=this;
	global.initializer.navigation={state:false,scope:navi};
	navi.pagination={};
	navi.bigPreloader=false;

	// pagination



	navi.showPreloader=function(item,type){
		if(item!=undefined && (type!="all" && type!="click")){
			navi.pagination[item].preloader=true;
		}else{
			for (var key in navi.pagination) {
				navi.pagination[key].preloader=true;
			}
		}
	}

	navi.hidePreloader=function(item,type){
		if(item!=undefined && (type!="all" && type!="click")){
			navi.pagination[item].preloader=false;
		}else{
			for (var key in navi.pagination) {
				navi.pagination[key].preloader=false;
			}
		}
	}

	navi.lastReq;


	navi.loadNavItem=function(type,item){
		var d=(new Date).getTime();
		navi.lastReq={type:type,item:item,time:d};

		$timeout(function() {
			if((navi.lastReq.type==type && navi.lastReq.item==item && navi.lastReq.time==d )|| navi.lastReq==undefined){
				navi.loadNI(type,item);
				console.log("load nI")
			}else{
				console.log("annullo LoadNi")
			}
		}, 1000);
	}

	navi.loadNI = function(type,item){
		var elem={};
		type!=undefined ? elem.type=type:elem.type="all";
		if(item!=undefined)elem.item=item
		for (var key in navi.pagination)
		{elem[key]={};
		elem[key].selected=navi.pagination[key].selected;
		elem[key].search=navi.pagination[key].search;
		elem[key].item_number=navi.pagination[key].item_number;
		elem[key].page=	navi.pagination[key].current;
		if(navi.pagination[key].hasOwnProperty("selGlo") &&  navi.pagination[key].selGlo.trim() != "---"){
			elem[key].GLOSSARY_ID=navi.pagination[key].selGlo;
		}
		}

		navi.showPreloader(item,type);
		sbiModule_restServices
		.post("1.0/glossary", "loadNavigationItem", elem)
		.success(
				function(data) {

					if (data.hasOwnProperty("errors")) {
						global.showToast(sbiModule_translate.load("sbi.glossary.load.error"),3000);
					} else if (data.Status == "NON OK") {
						global.showToast(sbiModule_translate.load(data.Message),3000);
					} else {
//						console.log("loadNavItem ottenuti")
//						console.log(data)
						if(data.hasOwnProperty("document")){
							navi.pagination.document.item=data.document;
							navi.pagination.document.total=data.document_size;
							if(type!="pagination" && item=="document"){
								navi.pagination.document.current=1;
							}

						}

						if(data.hasOwnProperty("word")){
							navi.pagination.word.item=data.word;
							navi.pagination.word.total=data.word_size;
							if(type!="pagination"  && item=="word"){
								navi.pagination.word.current=1;
							}

						}

						if(data.hasOwnProperty("dataset")){
							navi.pagination.dataset.item=data.dataset;
							navi.pagination.dataset.total=data.dataset_size;
							if(type!="pagination"  && item=="dataset"){
								navi.pagination.dataset.current=1;
							}
						}

						if(data.hasOwnProperty("bness_cls")){
							navi.pagination.bness_cls.item=data.bness_cls;
							navi.pagination.bness_cls.total=data.bness_cls_size;
							if(type!="pagination"  && item=="bness_cls"){
								navi.pagination.bness_cls.current=1;
							}
						}

						if(data.hasOwnProperty("table")){
							navi.pagination.table.item=data.table;
							navi.pagination.table.total=data.table_size;
							if(type!="pagination"  && item=="table"){
								navi.pagination.table.current=1;
							}
						}


					}
					navi.hidePreloader(item,type);
				})
				.error(
						function(data, status, headers,
								config) {
							global.showToast(sbiModule_translate.load("sbi.glossary.load.error"),3000);
							navi.hidePreloader(item,type);
						});
	}

	navi.toggleFilter=function(item){

		var myArray=navi.pagination[categid(item)].selected;
		var index=arrayObjectIndexOf(myArray,item);

		index!=-1?myArray.splice(index,1):myArray.push(item);

		navi.loadNavItem("click",categid(item));
	};

	navi.isSelected=function(item){
		if(item==undefined)return false;
		return arrayObjectIndexOf(navi.pagination[categid(item)].selected,item)!=-1?  true: false;
	};

	navi.selectPresent= function() {
		for ( var key in navi.pagination) {
			if (navi.pagination[key].selected.length != 0)
				return true
		}
		return false;
	} 

	navi.deleteSelect=function(item){

		if(item=="word" || item==undefined){
			navi.pagination.word.selected=[];
			navi.pagination.word.search="";
			navi.pagination.word.selGlo="---";
		}

		if(item=="document" || item==undefined){
			navi.pagination.document.selected=[];
			navi.pagination.document.search="";
		}

		if(item=="dataset" || item==undefined){
			navi.pagination.dataset.selected=[];
			navi.pagination.dataset.search="";			
		}

		if(item=="bness_cls" || item==undefined){
			navi.pagination.bness_cls.selected=[];
			navi.pagination.bness_cls.search="";			
		}

		if(item=="table" || item==undefined){
			navi.pagination.table.selected=[];
			navi.pagination.table.search="";			
		}

		if(item==undefined){
			navi.pagination.word.current=1;
			navi.pagination.document.current=1;
			navi.pagination.dataset.current=1;
			navi.pagination.bness_cls.current=1;
			navi.pagination.table.current=1;
			navi.loadNavItem();
		}else{
			navi.loadNavItem("reset",item);
		}
	}

	navi.pageChanged = function(newPage,pagin_item) {
		console.log("PAgeChange")
		navi.loadNavItem("pagination",pagin_item.item_type);
	};

	navi.showInfoWORD=function(wordid){
		console.log("showInfo");
		console.log(event)
		console.log(wordid)
		$mdDialog
		.show({  
			controllerAs : 'infCtrl',
			 scope: $scope,preserveScope: true,
			controller : function($mdDialog) {
				var iwctrl = this;
				sbiModule_restServices.get("1.0/glossary", "getWord", "WORD_ID=" + wordid)
				.success(
						function(data, status, headers, config) {
							if (data.hasOwnProperty("errors")) {
								showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
							} else {
								iwctrl.info = data;
							}
						}).error(function(data, status, headers, config) {
							showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

						})
			},
			templateUrl : '/knowage/js/src/angular_1.4/tools/glossary/commons/templates/info_word.html',
			clickOutsideToClose :true
		})
	}

	navi.showInfoDOC=function(ev,docLB,docID){
		console.log("showInfo");
		console.log(event)
		console.log(docLB)
		$mdDialog
		.show({
			controllerAs : 'infCtrl',
			 scope: $scope,preserveScope: true,
			controller : function($mdDialog) {
				var idctrl = this;
				
				idctrl.runDocument=function(){ 
					$documentViewer.openDocument(idctrl.info.id,idctrl.info.label,idctrl.info.name);
//					var pathUrl="";
//					pathUrl+="&OBJECT_ID="+idctrl.info.id;
//					pathUrl+="&OBJECT_LABEL="+idctrl.info.label;
//					pathUrl+="&OBJECT_NAME="+idctrl.info.name;
//					
//					$window.location.href=sbiModule_config.adapterPath+'?ACTION_NAME=EXECUTE_DOCUMENT_ANGULAR_ACTION&SBI_ENVIRONMENT=DOCBROWSER&IS_SOURCE_DOCUMENT=true&SBI_EXECUTION_ID=null'+pathUrl
//						$window.location.href='/knowage/servlet/AdapterHTTP?ACTION_NAME=EXECUTE_DOCUMENT_ANGULAR_ACTION&SBI_ENVIRONMENT=DOCBROWSER&OBJECT_ID=35&OBJECT_LABEL=start%20doc&IS_SOURCE_DOCUMENT=true&LIGHT_NAVIGATOR_DISABLED=TRUE&SBI_EXECUTION_ID=null&OBJECT_NAME=start%20doc'
				}
				
				sbiModule_restServices.get("1.0/documents", docLB)
				.success(
						function(data, status, headers, config) {
							if (data.hasOwnProperty("errors")) {
								global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
							} else {
								idctrl.info = data;
								sbiModule_restServices.get("2.0/documents", docID+"/roles")
								.success(
										function(data, status, headers, config) {
											if (data.hasOwnProperty("errors")) {
												global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
											} else {
												idctrl.info.access = data;
											}
										}).error(function(data, status, headers, config) {
											global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
										})
							}
						}).error(function(data, status, headers, config) {
							global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
						})
			},
			templateUrl : '/knowage/js/src/angular_1.4/tools/glossary/commons/templates/info_document.html',
			targetEvent : ev,
			clickOutsideToClose :true
		})
	}

	navi.showInfoDS=function(ev,dsId,dsOrg){
		console.log("showInfo");
		$mdDialog
		.show({
			controllerAs : 'infCtrl',
			 scope: $scope,preserveScope: true,
			controller : function($mdDialog) {
				var idsctrl = this;
				sbiModule_restServices.get("1.0/glossary", "getDataSetInfo?DATASET_ID="+dsId+"&ORGANIZATION="+dsOrg)
				.success(
						function(data, status, headers, config) {
							if (data.hasOwnProperty("errors")) {
								showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
							} else {
								idsctrl.info = data;
							}
						}).error(function(data, status, headers, config) {
							showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

						})

			},
			templateUrl : '/knowage/js/src/angular_1.4/tools/glossary/commons/templates/info_dataset.html',
			targetEvent : ev,
			clickOutsideToClose :true
		})
	}

	navi.showInfoBC=function(ev,bclab){
		console.log("showInfo");
		console.log(event)
		console.log(bclab)
		$mdDialog
		.show({
			controllerAs : 'infCtrl',
			 scope: $scope,preserveScope: true,
			controller : function($mdDialog) {
				var idsctrl = this;
				sbiModule_restServices.get("1.0/glossary","getBnessCls", "BC_ID=" +bclab)
				.success(
						function(data, status, headers, config) {
							if (data.hasOwnProperty("errors")) {
								showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
							} else {
								idsctrl.info = data;
							}
						}).error(function(data, status, headers, config) {
							showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

						})

			},
			templateUrl : '/knowage/js/src/angular_1.4/tools/glossary/commons/templates/info_bness_cls.html',
			targetEvent : ev,
			clickOutsideToClose :true
		})
	}

	navi.showInfoTB=function(ev,tblab){

		$mdDialog
		.show({
			controllerAs : 'infCtrl',
			 scope: $scope,preserveScope: true,
			controller : function($mdDialog) {
				var idsctrl = this;
				sbiModule_restServices.get("1.0/glossary","getTable", "TABLE_ID=" +tblab)
				.success(
						function(data, status, headers, config) {
							if (data.hasOwnProperty("errors")) {
								showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
							} else {
								idsctrl.info = data;
							}
						}).error(function(data, status, headers, config) {
							showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

						})

			},
			templateUrl : '/knowage/js/src/angular_1.4/tools/glossary/commons/templates/info_table.html',
			targetEvent : ev,
			clickOutsideToClose :true
		})
	}

	navi.SearchLike = function(arr) {
		console.log("SearchLike");
		ele=arr.search;
		arr.tmp_search = ele;
		$timeout(function() {
			var item="Page=1&ItemPerPage="+arr.item_number;


			var addGlo = false;
			if (arr.hasOwnProperty("selGlo") && arr.selGlo != arr.prev_glo) {
				// cambio glossario
				arr.prev_glo = arr.selGlo;
				addGlo = true;
			}

			if ((arr.tmp_search != ele || arr.prev_search == ele) && !addGlo) {
				return;
			}

			arr.prev_search = ele;
			navi.loadNavItem("search",arr.item_type);

		}, 1000);

	}    


	function changeItemPP() {
		var boxsize = angular.element(document.querySelector('.navig-tab-content'))[0].offsetHeight;
		var tbw = angular.element(document.querySelector('.xs-head'))[0].offsetHeight;
		var bpw = angular.element(document.querySelector('.box_pagination'))[0].offsetHeight;
		var search = angular.element(document.querySelector('.searchBar'))[0].offsetHeight;

		bpw == 0 ? bpw = 19 : bpw = bpw;

		var WordItemPerPage=parseInt((boxsize - tbw - search- bpw -32 -5) / 16);
		var elemItemPerPage=parseInt((boxsize - tbw*2 - search- bpw -5  ) / 16);

		navi.pagination.word.item_number= WordItemPerPage;
		navi.pagination.document.item_number= elemItemPerPage;
		navi.pagination.dataset.item_number= elemItemPerPage;
		navi.pagination.bness_cls.item_number= elemItemPerPage;
		navi.pagination.table.item_number= elemItemPerPage;


	}

	$scope
	.$watch(
			function() {
				return angular.element(document.querySelector('.glossaryTec'))[0].offsetHeight;
			}, function(newValue, oldValue) {
				if (newValue != oldValue) {
					changeItemPP();
				}
			}, true);



	function arrayObjectIndexOf(myArray,searchTerm) {
		if(searchTerm==undefined)return -1;
		var prop=propid(searchTerm)
		for(var i = 0, len = myArray.length; i < len; i++) {
			if (myArray[i][prop] == searchTerm[prop]) return i;
		}
		return -1;
	}


	function propid(item) {
		if (item.hasOwnProperty("WORD_ID")) {
			return 'WORD_ID'
		} else if (item.hasOwnProperty("DOCUMENT_ID")) {
			return 'DOCUMENT_ID'
		} else if (item.hasOwnProperty("DATASET_ID")) {
			return 'DATASET_ID'
		}else if (item.hasOwnProperty("BC_ID")) {
			return 'BC_ID'
		}else if (item.hasOwnProperty("TABLE_ID")) {
			return 'TABLE_ID'
		}
	}


	function categid(item) {
		if (item.hasOwnProperty("WORD_ID")) {
			return 'word'
		} else if (item.hasOwnProperty("DOCUMENT_ID")) {
			return 'document'
		} else if (item.hasOwnProperty("DATASET_ID")) {
			return 'dataset'
		} else if (item.hasOwnProperty("BC_ID")) {
			return 'bness_cls'
		} else if (item.hasOwnProperty("TABLE_ID")) {
			return 'table'
		}
	}

	navi.init=function(){
		console.log("init Navi")
		navi.pagination.word = {item_type:"word",item:[],selected:[],selGlo:"---",current: 1,total:10,item_number:3,id:"word_pagination",search:"",prev_search:"",prev_glo:"---",tmp_search:"",preloader:false};
		navi.pagination.document = {item_type:"document",item:[],selected:[],current: 1,total:10,item_number:7,id:"document_pagination",search:"",prev_search:"",tmp_search:"",preloader:false};
		navi.pagination.dataset = {item_type:"dataset",item:[],selected:[],current: 1,total:10,item_number:7,id:"dataset_pagination",search:"",prev_search:"",tmp_search:"",preloader:false};
		navi.pagination.bness_cls = {item_type:"bness_cls",item:[],selected:[],current: 1,total:10,item_number:7,id:"bness_cls_pagination",search:"",prev_search:"",tmp_search:"",preloader:false};
		navi.pagination.table = {item_type:"table",item:[],selected:[],current: 1,total:10,item_number:7,id:"table_pagination",search:"",prev_search:"",tmp_search:"",preloader:false};
		changeItemPP();
//		navi.loadNavItem();     
	}

	navi.init();


}
