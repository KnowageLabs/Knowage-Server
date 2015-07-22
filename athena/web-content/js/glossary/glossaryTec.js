var app = angular.module('AIDA_GLOSSARY_TECNICAL_USER', [ 'ngMaterial','ui.tree', 'angular_rest','angularUtils.directives.dirPagination' ]);

app.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey').accentPalette(
			'blue-grey');
});


app.service('translate', function() {
	this.load = function(key) {
		return messageResource.get(key, 'messages');
	};
});


app.controller('Controller_tec', [ "restServices","$mdToast", funzione_tec ]);
app.controller('Controller', [ "translate", "restServices", "$q", "$scope", "$mdDialog", "$filter", "$timeout", "$mdToast", funzione_associazione ]);
app.controller('Controller_navigation', [ "translate", "restServices", "$q", "$scope", "$mdDialog", "$filter", "$timeout", "$mdToast", funzione_navigazione ]);

var listDocument = [{
	DOCUMENT_ID :0,
	DOCUMENT_NM : "DOC1",
},{
	DOCUMENT_ID :1,
	DOCUMENT_NM : "DOC2",
}
				]

var global;
var navi;

function funzione_tec(restServices,$mdToast) {
	global=this;
	global.glossary;
	global.listDoc;
	getAllGloss();
	getAllDoc();
	
	global.showToast=function(text, time) {
		var timer = time == undefined ? 6000 : time;

		console.log(text)
		$mdToast.show($mdToast.simple().content(text).position('top').action(
				'OK').highlightAction(false).hideDelay(timer));
	}
	
	
	function getAllGloss() {
		console.log("getAllGloss")
	
		restServices.get("1.0/glossary", "listGlossary").success(
				function(data, status, headers, config) {
					console.log("Glossary Ottenuti " + status)
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						console.log(data.errors[0].message);
						

					} else {
						global.glossary= data;
					}

				}).error(function(data, status, headers, config) {
			console.log("Glossary non Ottenuti " + status);
			
	
		})

	}
	
	function getAllDoc() {
		console.log("getAllDoc")
		
		restServices.get("1.0/glossary", "listDocument").success(
				function(data, status, headers, config) {
					console.log("doc Ottenuti " + status)
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						console.log(data.errors[0].message);
					} else {
						global.listDoc = data;
					
					}

				}).error(function(data, status, headers, config) {
			console.log("Glossary non Ottenuti " + status);
			
		})

	}
	
}


function funzione_navigazione(translate, restServices, $q, $scope, $mdDialog, $filter,$timeout, $mdToast) {
	navi=this;
	
	// pagination
	navi.WordItemPerPage=5;
	navi.pagination={};
	navi.pagination.word = {item_type:"word",item:[],selected:[],selGlo:"---",current: 1,total:10,item_number:3,id:"word_pagination",search:"",prev_search:"",prev_glo:"---",tmp_search:"",preloader:false};
	navi.pagination.document = {item_type:"document",item:[],selected:[],current: 1,total:10,item_number:7,id:"document_pagination",search:"",prev_search:"",tmp_search:"",preloader:false};
	navi.pagination.dataset = {item_type:"dataset",item:[],selected:[],current: 1,total:10,item_number:7,id:"dataset_pagination",search:"",prev_search:"",tmp_search:"",preloader:false};
	navi.bigPreloader=false;
	changeItemPP();
	navi.showPreloader=function(item,type){
		console.log(type)
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
	navi.loadNavItem=function(type,item){
		console.log("loadNavItem")
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
		restServices
		.post("1.0/glossary", "loadNavigationItem", elem)
		.success(
				function(data) {

					if (data.hasOwnProperty("errors")) {
						global.showToast(translate.load("sbi.glossary.load.error"),3000);
					} else if (data.Status == "NON OK") {
						global.showToast(translate.load(data.Message),3000);
					} else {
						console.log("loadNavItem ottenuti")
						console.log(data)
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
						
						
					}
					navi.hidePreloader(item,type);
				})
		.error(
				function(data, status, headers,
						config) {
					global.showToast(translate.load("sbi.glossary.load.error"),3000);
					navi.hidePreloader(item,type);
				});
	}
	navi.loadNavItem();     
	
	navi.toggleFilter=function(item){
		console.log(item)
		var myArray=navi.pagination[categid(item)].selected;
		var index=arrayObjectIndexOf(myArray,item);
		console.log(index)
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
		
		if(item==undefined){
			navi.pagination.word.current=1;
			navi.pagination.document.current=1;
			navi.pagination.dataset.current=1;
			navi.loadNavItem();
		}else{
			navi.loadNavItem("click",item);
		}
	}
	
	navi.pageChanged = function(newPage,pagin_item) {
		console.log("PAgeChange")
		navi.loadNavItem("pagination",pagin_item.item_type);
	    };
	
	    

		navi.showInfoWORD=function(ev,wordid){
			console.log("showInfo");
			console.log(event)
			console.log(wordid)
			$mdDialog
				.show({  
						controllerAs : 'infCtrl',
					controller : function($mdDialog) {
						var iwctrl = this;
						iwctrl.translate=translate;
				restServices.get("1.0/glossary", "getWord", "WORD_ID=" + wordid)
						.success(
								function(data, status, headers, config) {
									if (data.hasOwnProperty("errors")) {
										showToast(translate.load("sbi.glossary.load.error"), 3000);
									} else {
										iwctrl.info = data;
									}
								}).error(function(data, status, headers, config) {
							showToast(translate.load("sbi.glossary.load.error"), 3000);
							
						})
						
				
					
					},
					templateUrl : 'info_word.html',
					targetEvent : ev,
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
					controller : function($mdDialog) {
						var idctrl = this;
						idctrl.translate=translate;
				restServices.get("1.0/documents", docLB)
						.success(
								function(data, status, headers, config) {
									if (data.hasOwnProperty("errors")) {
										global.showToast(translate.load("sbi.glossary.load.error"), 3000);
									} else {
										idctrl.info = data;
										
										
										
										restServices.get("2.0/documents", docID+"/roles")
										.success(
												function(data, status, headers, config) {
													if (data.hasOwnProperty("errors")) {
														global.showToast(translate.load("sbi.glossary.load.error"), 3000);
													} else {
														idctrl.info.access = data;
													}
											}).error(function(data, status, headers, config) {
													global.showToast(translate.load("sbi.glossary.load.error"), 3000);
											})
										
										
										
										
										
										
										
										
									}
								}).error(function(data, status, headers, config) {
									global.showToast(translate.load("sbi.glossary.load.error"), 3000);
							
						})
						
					},
					templateUrl : 'info_document.html',
					targetEvent : ev,
					clickOutsideToClose :true
				})
		}
		
		navi.showInfoDS=function(ev,dslab){
			console.log("showInfo");
			console.log(event)
			console.log(dslab)
			$mdDialog
				.show({
					controllerAs : 'infCtrl',
					controller : function($mdDialog) {
						var idsctrl = this;
						idsctrl.translate=translate;
						restServices.get("1.0/datasets", dslab)
						.success(
								function(data, status, headers, config) {
									if (data.hasOwnProperty("errors")) {
										showToast(translate.load("sbi.glossary.load.error"), 3000);
									} else {
										idsctrl.info = data;
									}
								}).error(function(data, status, headers, config) {
							showToast(translate.load("sbi.glossary.load.error"), 3000);
							
						})
						
					},
					templateUrl : 'info_dataset.html',
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
		}
	}
		

			function categid(item) {
		if (item.hasOwnProperty("WORD_ID")) {
			return 'word'
		} else if (item.hasOwnProperty("DOCUMENT_ID")) {
			return 'document'
		} else if (item.hasOwnProperty("DATASET_ID")) {
			return 'dataset'
		}
	}
			
		
}



function funzione_associazione(translate, restServices, $q, $scope, $mdDialog, $filter,$timeout, $mdToast) {
	ctrl=this;
	ctrl.listDoc;
	ctrl.sizeDoc=0;
	ctrl.searchDoc="";
	ctrl.showPreloader = false;
	ctrl.preloaderTree= false;
	ctrl.selectedGloss;
	ctrl.selectedDocument;
	
//	getAllDoc();
	
	ctrl.words = [];
	
	

	ctrl.tmpDocSearch = "";
	ctrl.prevDocSearch = "-1";
	
	ctrl.loadDocList=function(item){
		restServices.get("2.0/documents", "listDocument", item).success(
				function(data, status, headers, config) {
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						global.showToast(translate.load("sbi.glossary.load.error"),3000);

					} else {
						console.log("list doc ottenute")
						ctrl.listDoc = data.item;
						ctrl.sizeDoc=data.itemCount;
						ctrl.showSearchDocPreloader = false;
					}

				}).error(function(data, status, headers, config) {
					global.showToast(translate.load("sbi.glossary.load.error"), 3000);
			ctrl.showSearchDocPreloader = false;
		})
	}
	
	ctrl.ChangeDocPage=function(page){
		var item="Page="+page+"&ItemPerPage="+ctrl.DocItemPerPage+"&label=" + ctrl.searchDoc;
		ctrl.loadDocList(item)
    	}
	ctrl.ChangeDocPage(1);
	
	ctrl.DocumentLike= function(ele,page){
		console.log("DocumentLike "+ele);
		ctrl.tmpDocSearch = ele;
		$timeout(function() {

			if (ctrl.tmpDocSearch != ele || ctrl.prevDocSearch == ele) {
				return;
			}

			ctrl.prevDocSearch = ele;
			ctrl.showSearchDocPreloader = true;
			page==undefined? page=1:page=page;
			var item="Page=1&ItemPerPage="+ctrl.DocItemPerPage;
	    	if(ctrl.tmpDocSearch!=undefined && ctrl.tmpDocSearch.trim()!=""){
	    		 item+="&label=" + ele;
	    	 }
	    	
	    	ctrl.loadDocList(item)
		

		}, 1000);
		}
	
	
	ctrl.prevSWSG = "";
	ctrl.tmpSWSG = "";
	ctrl.SearchWordInSelectedGloss= function(ele){
		console.log("SearchWordInSelectedGloss  "+ele);
		ctrl.tmpSWSG = ele;
		$timeout(function() {
			if (ctrl.tmpSWSG != ele || ctrl.prevSWSG == ele) {
				console.log("interrompo la ricerca  di ele " + ele)
				return;
			}

			ctrl.prevSWSG = ele;
			
			console.log("cerco "+ele)
			showPreloader("preloaderTree");
			restServices.get("1.0/glossary", "glosstreeLike", "WORD=" + ele+"&GLOSSARY_ID="+ctrl.selectedGloss.GLOSSARY_ID).success(
					function(data, status, headers, config) {
						console.log("glosstreeLike Ottenuti " + status)
						console.log(data)

						if (data.hasOwnProperty("errors")) {
							showErrorToast(data.errors[0].message);
							global.showToast(
									translate.load("sbi.glossary.load.error"),
									3000);

						} else {
							ctrl.selectedGloss=data.GlossSearch;
							
							if(ele!=""){
							$timeout(function() {
								ctrl.expandAllTree("GlossTree");
							},500);
							}
						}
						hidePreloader("preloaderTree");

					}).error(function(data, status, headers, config) {
				console.log("glosstreeLike non Ottenuti " + status);
				global.showToast(translate.load("sbi.glossary.load.error"), 3000);

				hidePreloader("preloaderTree");
			})

		}, 1000);
	}
	
	ctrl.removeWord= function(word){
		console.log("remove word");
		console.log(word);
		
		var confirm = $mdDialog.confirm().title(
				translate.load("sbi.glossary.word.delete")).content(
				translate.load("sbi.glossary.word.delete.message")).ariaLabel(
				'Lucky day').ok(translate.load("sbi.generic.delete")).cancel(
				translate.load("sbi.myanalysis.delete.cancel")).targetEvent(word);
		
			$mdDialog.show(confirm).then(
						function() {
						
							showPreloader();
							restServices.remove("1.0/glossary", "deleteDocWlist",
											"WORD_ID=" + word.WORD_ID+"&DOCUMENT_ID="+ctrl.selectedDocument)
									.success(
											function(data, status, headers,
													config) {
												console.log("Word eliminato")
												console.log(data)
												if (data.hasOwnProperty("errors")) {
													showErrorToast(data.errors[0].message)
													global.showToast(translate.load("sbi.glossary.word.delete.error"),3000);

												} else {
													ctrl.words.splice(ctrl.words.indexOf(word), 1);
													global.showToast(translate.load("sbi.glossary.word.delete.success"),3000);
													
													
												}
												hidePreloader();

											})
									.error(
											function(data, status, headers,
													config) {
												console.log("WORD NON ELMINIATO "+ status);
												showErrorToast("word non eliminato "+ status);
												global.showToast(translate.load("sbi.glossary.word.delete.error"),3000);
												hidePreloader();
											})


						}, function() {
							console.log('Annullo.');
						});
		
		
		
		
	}
	
	ctrl.loadDocumentInfo= function(DOCUMENT_ID){
		console.log("loadDocumentInfo");
		ctrl.words=[];
		ctrl.searchDoc="";
		showPreloader("preloader");
		restServices
				.get(
						"1.0/glossary","getDocumentInfo","DOCUMENT_ID=" + DOCUMENT_ID )
				.success(
						function(data, status, headers, config) {
							console.log("loadDocumentInfo ottnuti")
							console.log(data)

							if (data.hasOwnProperty("errors")) {
								showErrorToast(data.errors[0].message);
								global.showToast(translate.load("sbi.glossary.load.error"), 3000);

							} else {
								ctrl.words=data.word;
								
								
								
							}
							
							hidePreloader("preloader");
						}).error(function(data, status, headers, config) {
					console.log("nodi non ottenuti " + status);
					global.showToast(translate.load("sbi.glossary.load.error"), 3000);
					if (togg != undefined) {
						togg.expand();
						hidePreloader("preloader");
					}
				})
	}
	
	ctrl.showSelectedGlossary = function(gloss) {
		if(ctrl.selectedGloss!=undefined && ctrl.selectedGloss.GLOSSARY_ID==gloss.GLOSSARY_ID){return;}
		
		
		ctrl.selectedGloss=gloss;
		ctrl.getGlossaryNode(ctrl.selectedGloss, null);
	}
	
	ctrl.getGlossaryNode = function(gloss, node, togg) {
		console.log("getGlossaryNode")
		console.log(node)
		console.log(gloss)
		console.log(togg);
		var PARENT_ID = (node == null ? null : node.CONTENT_ID);
		var GLOSSARY_ID = (gloss == null ? null : gloss.GLOSSARY_ID);
	
		showPreloader("preloaderTree");
		restServices
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
								global.showToast(translate.load("sbi.glossary.load.error"), 3000);

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
					global.showToast(translate.load("sbi.glossary.load.error"), 3000);
					if (togg != undefined) {
						togg.expand();
						hidePreloader("preloaderTree");
					}
				})
	}
	
	ctrl.expandAllTree= function(tree){
		console.log("expand")
		angular.element(document.getElementById(tree)).scope().expandAll();
	}
	
	ctrl.toggle = function(scope, item, gloss) {
		
		console.log("toggle")
		
		if(ctrl.searchDoc!="" && ctrl.searchDoc!=undefined ){
			scope.toggle();
			return;	
		}
		
		console.log(scope)
		item.preloader = true;
		if (scope.collapsed) {
			ctrl.getGlossaryNode(gloss, item, scope)
		} else {
			scope.toggle();
			item.preloader = false;
		}
	};
	
	function getAllDoc() {
		console.log("getAllDoc")
		showPreloader();
		restServices.get("1.0/glossary", "listDocument").success(
				function(data, status, headers, config) {
					console.log("doc Ottenuti " + status)
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						showErrorToast(data.errors[0].message);
						global.showToast(translate.load("sbi.glossary.load.error"),
								3000);

					} else {
						ctrl.listDoc = data;
					}

					hidePreloader();
				}).error(function(data, status, headers, config) {
			console.log("Glossary non Ottenuti " + status);
			showErrorToast('Ci sono errori! \n status ' + status);
			global.showToast(translate.load("sbi.glossary.load.error"), 3000);

			hidePreloader();
		})

	}
	
	
	ctrl.WordItemPerPage=10;
	ctrl.DocItemPerPage=10;
	changeItemPP();
	function changeItemPP() {
		var boxItemGlo = angular.element(document.querySelector('.boxItemGlo'))[0].offsetHeight;
		var tbw = angular.element(document.querySelector('.xs-head'))[0].offsetHeight;
		var bpw = angular.element(document.querySelector('.box_pagination'))[0].offsetHeight;

		 bpw == 0 ? bpw = 19 : bpw = bpw;
		
	
		 
		ctrl.WordItemPerPage=parseInt((boxItemGlo - tbw - bpw -5 ) / 28);
		ctrl.DocItemPerPage=parseInt((boxItemGlo - tbw - bpw -22 -5  ) / 16);
	
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
	
	
	ctrl.TreeOptionsWord = {

			accept : function(sourceNodeScope, destNodesScope, destIndex) {
				
// if(destNodesScope.hasChild(sourceNodeScope.$modelValue)){
// console.log(destNodesScope.hasChild(sourceNodeScope.$modelValue))
// return false;
// }
// console.log(destNodesScope.$modelValue.toString());
				var present=false;
				for(var i=0;i<destNodesScope.$modelValue.length;i++){
					if(destNodesScope.$modelValue[i].WORD_ID==sourceNodeScope.$modelValue.WORD_ID){
					console.log("word present")	;
					present= true;
					break;
					}
				}
				if(present){ return false;}
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
	
	ctrl.TreeOptions = {

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
				elem.DOCUMENT_ID=ctrl.selectedDocument;
				console.log(elem)
			
				showPreloader();
				restServices
				.post("1.0/glossary", "addDocWlist", elem)
				.success(
						function(data, status, headers,
								config) {

							if (data
									.hasOwnProperty("errors")) {
								showErrorToast(data.errors[0].message)
								global.showToast(
										translate
												.load("sbi.glossary.error.save"),
										3000);
							} else if (data.Status == "NON OK") {
								global.showToast(
										translate
												.load(data.Message),
										3000);
							} else {
// global.showToast(
// translate
// .load("sbi.glossary.success.save"),
// 3000);

							
// event.dest.nodesScope.insertNode(event.dest.index,elem);
							}
							
							
							
							hidePreloader();
						})
				.error(
						function(data, status, headers,
								config) {
							hidePreloader();
							global.showToast(
									translate
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
		case 'preloaderTree':ctrl.preloaderTree=true;
							break;
		default:ctrl.showPreloader = true;
				break;
		}
	}
	
	function hidePreloader(pre) {
		switch(pre){
		case 'preloaderTree':ctrl.preloaderTree=false;
							break;
		default:ctrl.showPreloader = false;
				break;
		}
	}
	
}

