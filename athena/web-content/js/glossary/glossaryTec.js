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
	
		restServices.get("glossary", "listGlossary").success(
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
		
		restServices.get("glossary", "listDocument").success(
				function(data, status, headers, config) {
					console.log("doc Ottenuti " + status)
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						console.log(data.errors[0].message);
					} else {
						global.listDoc = data;
						navi.pagination.document.item=data;
						navi.pagination.document.total=data.length;
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
	navi.pagination.word = {item:[],selected:[],selGlo:"---",current: 1,total:10,item_number:3,id:"word_pagination",search:"",prev_search:"",prev_glo:"---",tmp_search:"",rest:"listWords",};
	navi.pagination.document = {item:global.listDoc,selected:[],current: 1,total:10,item_number:7,id:"document_pagination",search:"",prev_search:"",tmp_search:"",rest:"listDocument",};
	changeItemPP();
	
	
	
	
	
	
	navi.toggleFilter=function(item){
		console.log(item)
		var myArray=navi.pagination[categid(item)].selected;
		var index=arrayObjectIndexOf(myArray,item);
		console.log(index)
		index!=-1?myArray.splice(index,1):myArray.push(item);
		
		//load the filtered results
		var elem={};
		for (var key in navi.pagination)
		{elem[key]={};
			elem[key].selected=navi.pagination[key].selected;
			elem[key].search=navi.pagination[key].search;
			if(navi.pagination[key].hasOwnProperty("selGlo") &&  navi.pagination[key].selGlo.trim() != "---"){
				elem[key].GLOSSARY_ID=navi.pagination[key].selGlo;
			}
		}
		restServices
		.post("glossary", "loadNavigationItem", elem)
		.success(
				function(data) {

					if (data.hasOwnProperty("errors")) {
						global.showToast(translate.load("sbi.glossary.error.save"),3000);
					} else if (data.Status == "NON OK") {
						global.showToast(translate.load(data.Message),3000);
					} else {
						navi.pagination.document.item=data.document;
						
					}
				
				})
		.error(
				function(data, status, headers,
						config) {
					hidePreloader();
					global.showToast(translate.load("sbi.glossary.error.save"),3000);
					ctr.newWord = JSON.parse(JSON.stringify(EmptyWord));
					
				});
		
		
		
	};
	
	navi.isSelected=function(item){
		if(item==undefined)return false;
		return arrayObjectIndexOf(navi.pagination[categid(item)].selected,item)!=-1?  true: false;
	};
	
	navi.SearchLike = function(arr) {
		console.log("SearchLike")
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
			if (arr.tmp_search != undefined && arr.tmp_search.trim() != "") {
				item += "&WORD=" + ele;
			}

			if (addGlo || arr.hasOwnProperty("selGlo")) {
				if (arr.selGlo != undefined && arr.selGlo.trim() != "---") {
					item += "&GLOSSARY_ID=" + arr.selGlo;
				}
			}
			
					
// ctr.showSearchPreloader = true;
			
	    	
			restServices.get("glossary", arr.rest, item).success(
					function(data, status, headers, config) {

						if (data.hasOwnProperty("errors")) {
							global.showToast(
									translate.load("sbi.glossary.load.error"),
									3000);

						} else {
							
							arr.item =angular.fromJson(data.item);
							arr.total=data.itemCount;
							
							for(var i =0;i<arr.selected.length;i++){
							
								if(arrayObjectIndexOf(arr.item,arr.selected[i])==-1){
									arr.item.unshift(arr.selected[i]);
									arr.total++;
								}
							}
							
//							ctr.showSearchPreloader = false;
						}

					}).error(function(data, status, headers, config) {
				global.showToast(translate.load("sbi.glossary.load.error"), 3000);

//				ctr.showSearchPreloader = false;
			})

		}, 1000);

	}
	
	navi.deleteSelect=function(){
		for (var key in navi.pagination)
		{
			navi.pagination[key].selected=[];
		}
	}
	
	navi.pageChanged = function(newPage,pagin_item) {
	        navi.getResultsPage(newPage,pagin_item);
	    };
	
	navi.getResultsPage = function(pageNumber,pagin_item) {
	    	 console.log("getResultsPage")
	    	 var item="Page=" + pageNumber+"&ItemPerPage="+pagin_item.item_number;
	    	 
	    	 
	    	 if(pagin_item.search!=undefined && pagin_item.search.trim()!=""){
	    		 item+="&WORD=" +pagin_item.search;
	    	 }
	    	 
	    	 if ( pagin_item.hasOwnProperty("selGlo")) {
					if (pagin_item.selGlo != undefined && pagin_item.selGlo.trim() != "---") {
						item += "&GLOSSARY_ID=" + pagin_item.selGlo;
					}
				}
	    	
	        restServices.get("glossary", pagin_item.rest, item)
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.log("non Ottenuti");
				} else {
					pagin_item.item=data.item;
					pagin_item.total=data.itemCount;
					
				}
				

			}).error(function(data, status, headers, config) {
				console.log(" non Ottenuti " + status);
			
			})

	        
	    }

	navi.getResultsPage(1,navi.pagination.word);
	     
	     
	     
	   
	 	function changeItemPP() {
	 		console.log("changeItemPP-nav")
	 		var boxsize = angular.element(document.querySelector('.navig-tab-content'))[0].offsetHeight;
	 		var tbw = angular.element(document.querySelector('.xs-head'))[0].offsetHeight;
	 		var bpw = angular.element(document.querySelector('.box_pagination'))[0].offsetHeight;
	 		var search = angular.element(document.querySelector('.search-input'))[0].offsetHeight;

	 		 bpw == 0 ? bpw = 19 : bpw = bpw;
	 	
	 		var WordItemPerPage=parseInt((boxsize - tbw - search- bpw -32 -3 ) / 16);
	 		var elemItemPerPage=parseInt((boxsize - tbw*2 - search- bpw -3  ) / 16);
	 		
	 		navi.pagination.word.item_number= WordItemPerPage;
	 		
	 			
	 	}
	 	
	 	$scope
	 	.$watch(
	 			function() {
	 				return angular.element(document.querySelector('.glossaryTec'))[0].offsetHeight;
	 			}, function(newValue, oldValue) {
	 				console.log("watch")
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
		
		function propid(item){
			if(item.hasOwnProperty("WORD_ID")){
				return 'WORD_ID'
				}else if(item.hasOwnProperty("DOCUMENT_ID")){
					return 'DOCUMENT_ID'}
		}
		
		function categid(item){
			if(item.hasOwnProperty("WORD_ID")){
				return 'word'
				}else if(item.hasOwnProperty("DOCUMENT_ID")){
					return 'document'}
		}
}



function funzione_associazione(translate, restServices, $q, $scope, $mdDialog, $filter,$timeout, $mdToast) {
	ctrl=this;
	ctrl.listDoc;
	ctrl.showPreloader = false;
	ctrl.preloaderTree= false;
	ctrl.selectedGloss;
	ctrl.selectedDocument;
	getAllDoc();
	ctrl.words = [];
	
	

	ctrl.DocumentLike= function(ele){
		console.log("DocumentLike "+ele);
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
			restServices.get("glossary", "glosstreeLike", "WORD=" + ele+"&GLOSSARY_ID="+ctrl.selectedGloss.GLOSSARY_ID).success(
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
							restServices.remove("glossary", "deleteDocWlist",
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
						"glossary","getDocumentInfo","DOCUMENT_ID=" + DOCUMENT_ID )
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
						"glossary",
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
		restServices.get("glossary", "listDocument").success(
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
		ctrl.DocItemPerPage=parseInt((boxItemGlo - tbw - bpw -5  ) / 16);
	
	}
	
	$scope
	.$watch(
			function() {
				return angular.element(document.querySelector('.glossaryTec'))[0].offsetHeight;
			}, function(newValue, oldValue) {
				console.log("watch")
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
				.post("glossary", "addDocWlist", elem)
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

