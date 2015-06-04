var app = angular.module('AIDA_GESTIONE-VOCABOLI', [ 'ngMaterial', 'ui.tree',
		'ng-context-menu' ]);

var EmptyWord = {
	LINK : [],
	SBI_GL_WORD_ATTR : [],
	DESCR : "",
	WORD : "",
	STATE : "",
	CATEGORY : "",
	FORMULA : "",
	NEWWORD : true
};


var EmptyGloss = {
	// GLOSSARY_ID : 1,
	GLOSSARY_CD : "",
	GLOSSARY_NM : "",
	GLOSSARY_DS : "",
	SBI_GL_CONTENTS : [], // DOVREBBERO ESSERE I NODI
	NEWGLOSS : true
// MI SERVE SOLO PER LA CREAZIONE DI UN NODO E POI LO RIMUOVO
};


var SBI_GL_ATTRIBUTES=[
                       {
                    	   ATTRIBUTE_ID:1,
                    	   ATTRIBUTE_CD:1,
                    	   ATTRIBUTE_NM:"PROP1",
                    	   ATTRIBUTE_DS:"DS PROP1",
                    	   MANDATORY_FL:1,
                    	   TYPE:1,
                    	   DOMAIN:1,
                    	   FORMAT:1,
                    	   DISPLAT_TP:1,
                    	   ORDER:1
                       },{
                    	   ATTRIBUTE_ID:2,
                    	   ATTRIBUTE_CD:2,
                    	   ATTRIBUTE_NM:"PROP2",
                    	   ATTRIBUTE_DS:"DS PROP2",
                    	   MANDATORY_FL:2,
                    	   TYPE:2,
                    	   DOMAIN:2,
                    	   FORMAT:2,
                    	   DISPLAT_TP:2,
                    	   ORDER:2
                       },{
                    	   ATTRIBUTE_ID:3,
                    	   ATTRIBUTE_CD:3,
                    	   ATTRIBUTE_NM:"PROP3",
                    	   ATTRIBUTE_DS:"DS PROP3",
                    	   MANDATORY_FL:3,
                    	   TYPE:3,
                    	   DOMAIN:3,
                    	   FORMAT:3,
                    	   DISPLAT_TP:3,
                    	   ORDER:3
                       }];



var wor = [ {
	WORD_ID : 1,
	WORD : "CLIENTE",
	DESCR : "IL CLIENTE è ....",
	FORMULA : "",
	STATE : "OK",
	CATEGORY : "PERSONE",
	LINK : [],
	SBI_GL_WORD_ATTR : []
}, {
	WORD_ID : 2,
	WORD : "PROGETTO",
	DESCR : "UN PROGETTO è....",
	FORMULA : "",
	STATE : "OK",
	CATEGORY : "ALTRO",
	SBI_GL_WORD_ATTR : [],
	LINK : []
}, {
	WORD_ID : 3,
	WORD : "CASA",
	DESCR : "UNA CASA è ...",
	FORMULA : "",
	STATE : "OK",
	CATEGORY : "ABITAZIONI",
	SBI_GL_WORD_ATTR : [],
	LINK : []
}, ];

var glos = [ {
	GLOSSARY_ID : 1,
	GLOSSARY_CD : "C1",
	GLOSSARY_NM : "GLOSSARIO 1",
	GLOSSARY_DS : "IL GLOSSARIO 1 è",
	SBI_GL_CONTENTS : [ {
		CONTENT_ID : 'P1',
		GLOSSARY_ID : 1,
		PARENT_ID : "null",
		CONTENT_CD : 'c1',
		CONTENT_NM : 'cont1',
		CONTENT_DS : 'descr1',
		DEPTH : 0,
		CHILD : [ {
			CONTENT_ID : 'P11',
			GLOSSARY_ID : 1,
			PARENT_ID : 'P1',
			CONTENT_CD : 'c2',
			CONTENT_NM : 'cont2',
			CONTENT_DS : 'descr2',
			DEPTH : 1,
			CHILD : []
		}, {
			CONTENT_ID : 'P13',
			GLOSSARY_ID : 1,
			PARENT_ID : 'P1',
			CONTENT_CD : 'c3',
			CONTENT_NM : 'cont3',
			CONTENT_DS : 'descr3',
			DEPTH : 0,
			CHILD : []
		} ]
	}, ], // DOVREBBERO ESSERE I NODI
}, {
	GLOSSARY_ID : 2,
	GLOSSARY_CD : "C2",
	GLOSSARY_NM : "GLOSSARIO 2",
	GLOSSARY_DS : "IL GLOSSARIO 2 è",
	SBI_GL_CONTENTS : [], // DOVREBBERO ESSERE I NODI
}, {
	GLOSSARY_ID : 3,
	GLOSSARY_CD : "C3",
	GLOSSARY_NM : "GLOSSARIO 3",
	GLOSSARY_DS : "IL GLOSSARIO 3 è",
	SBI_GL_CONTENTS : [], // DOVREBBERO ESSERE I NODI
} ];

app.controller('Controller', [ "$mdDialog", "$filter", "$timeout", funzione ]);

function funzione($mdDialog, $filter, $timeout) {

	ctr = this;
	ctr.activeTab = 'Vocabolo';
	ctr.filterSelected = true;
	ctr.words = wor;
	ctr.newWord = JSON.parse(JSON.stringify(EmptyWord));
	ctr.glossary = glos;
	ctr.newGloss = JSON.parse(JSON.stringify(EmptyGloss));
	ctr.propWord=SBI_GL_ATTRIBUTES;
	ctr.querySearchProp   = "";	
	 self.searchTextProp    = null;
	ctr.selectedGloss = JSON.parse(JSON.stringify(EmptyGloss));

	ctr.modifyWord = function(word) {

		if (JSON.stringify(EmptyWord) != JSON.stringify(ctr.newWord)) {
			var confirm = $mdDialog
					.confirm()
					.title('Modifica word in corso')
					.content(
							"E' in corso la modifica di un word. Vuoi annullare e modificare quello selezionato?")
					.ariaLabel('Lucky day').ok('Modifica selezionato').cancel(
							'Annulla');

			console.log
			$mdDialog.show(confirm).then(function() {

				resetForm(word)

			}, function() {
				console.log('You decided to keep your debt.');
			});

		} else {
			resetForm(word)
		}

	};

	ctr.isEmpty = function() {
		return (JSON.stringify(EmptyWord) != JSON.stringify(ctr.newWord));
	}

	ctr.createNewWord = function(reset) {
		var text;
		if (reset != undefined) {
			text = {};
			text.title = 'Modifica word in corso';
			text.content = "E' in corso la modifica di un word. Vuoi annullare ?";
			text.ok = "Si";
			text.cancel = "No";

		} else {
			text = {};
			text.title = 'Modifica word in corso';
			text.content = "E' in corso la modifica di un word. Vuoi annullare e crearne uno nuovo?";
			text.ok = "Crea Nuovo";
			text.cancel = "Annulla";
		}

		if (JSON.stringify(EmptyWord) != JSON.stringify(ctr.newWord)) {
			var confirm = $mdDialog.confirm().title(text.title).content(
					text.content).ariaLabel('Lucky day').ok(text.ok).cancel(
					text.cancel);

			console.log
			$mdDialog.show(confirm).then(function() {

				resetForm();
				ctr.activeTab = 'Vocabolo';
			}, function() {
				console.log('You decided to keep your debt.');
			});

		} else {
			resetForm();
			ctr.activeTab = 'Vocabolo';
		}
	}

	function resetForm(ele) {
		console.log("resetForm")

		if (ele == undefined) {
			ctr.newWord = JSON.parse(JSON.stringify(EmptyWord));
		} else {
			ctr.newWord = ele;
		}

	}

	
	//querysearch della select autocompleate durante l'inserimento delle proprietà in new word
	 ctr.querySearchProp =function(query) {
		console.log("querySearchProp")
		console.log(query)
		
		var results = $filter('filter')(ctr.propWord, {
			ATTRIBUTE_NM : query.toUpperCase()
		}, compareForNestedFiltering);
		
		
	      return results;
	    }
	
	 
	 ctr.addProp= function(prop){
		 ctr.newWord.SBI_GL_WORD_ATTR.push(prop)
		 ctr.tmpAttr={};
	 };
	 
	 ctr.propPresent =function(query) {
		 
			console.log("propPresent")
		
			console.log(query)
			if(query==null )return false;
			
			var results ;
			if (typeof query !== 'object'){
				console.log("non obj")
				 results = $filter('filter')(ctr.propWord, {
						ATTRIBUTE_NM : query.toUpperCase()
					}, true);
			}else{
				console.log(" obj")
				 results = $filter('filter')(ctr.propWord, {
						ATTRIBUTE_NM : query.ATTRIBUTE_NM.toUpperCase()
					}, true);
			}
			
			
			console.log(results)
			
			if(results.length!=0)return true;
			
		      return false ;
		    }
	
	
	
	// chips
	compareForNestedFiltering = function(actual, expected) {
		console.log("compareForNestedFiltering")
		function contains(actualVal, expectedVal) {
			return actualVal.toString().toLowerCase().indexOf(
					expectedVal.toString().trim().toLowerCase()) !== -1;
		}

		if (typeof expected !== 'object')
			return contains(actual, expected);

		var result = Object.keys(expected).every(function(key) {
			return contains(eval('actual.' + key), eval('expected.' + key));
		});
		return result;
	};

	ctr.querySearch = function(chip) {
		console.log("querySearch")
		var found = $filter('filter')(ctr.words, {
			WORD : chip.toUpperCase()
		}, compareForNestedFiltering);
		return found;
	}

	ctr.newLink = function(chip) {

		var found = $filter('filter')(ctr.words, {
			WORD : chip.toUpperCase()
		}, true);

		if (found.length) {
			console.log("found");
			console.log(found);
			return found[0];
		} else {
			console.log("not found ds");

		}

	};

	ctr.addWord = function(product) {
		console.log(ctr.newWord.NEWWORD)

		if (ctr.newWord.NEWWORD != undefined) {
			console.log("salvato")
			ctr.newWord.WORD_ID = (new Date()).getTime();
			delete ctr.newWord.NEWWORD;
			product.push(ctr.newWord);
		} else {
			console.log("modificato")
		}

		ctr.newWord = JSON.parse(JSON.stringify(EmptyWord));
	};

	ctr.deleteWord = function(ev) {
		// Appending dialog to document.body to cover
		// sidenav in docs app
		console.log("deleteWord")
		console.log(ctr.words)
		var confirm = $mdDialog.confirm().title('Elimina word').content(
				'Sei sicuro di voler eliminare questo word?').ariaLabel(
				'Lucky day').ok('Elimina!').cancel('Annulla').targetEvent(ev);
		console.log(confirm)

		var wds = ctr.words;
		var nw = ctr.newWord;
		$mdDialog.show(confirm).then(function() {

			var index = wds.indexOf(ev);
			wds.splice(index, 1);

			// controllo se l'elemento
			// che si vule eliminare è
			// in fase di modifica
			if (nw.WORD_ID === ev.WORD_ID) {
				console.log("Sto modificando l'elemento che sto eliminando")
				nw.NEWWORD = true;
			}

		}, function() {
			console.log('You decided to keep your debt.');
		});
	};

	// glossary

	ctr.createNewGlossary = function(ev) {

		$mdDialog.show({
			controllerAs : 'gloCtrl',
			controller : function($mdDialog) {
				var gctl = this;
				gctl.newGloss = ctr.newGloss;

				gctl.submit = function() {
					console.log(gctl.newGloss)
					if (gctl.newGloss.NEWGLOSS != undefined) {
						console.log("salvo")
						gctl.newGloss.GLOSSARY_ID = (new Date()).getTime();
						delete gctl.newGloss.NEWGLOSS;
						ctr.glossary.push(gctl.newGloss);

					} else {
						console.log("modifico")
					}

					gctl.newGloss = JSON.parse(JSON.stringify(EmptyGloss));

					$mdDialog.hide();

				};
				gctl.annulla = function($event) {
					$mdDialog.hide();
					console.log("esco ");

				};

				gctl.isCompletedGlossary = function() {

					console.log("isCompletedGlossary")
					console.log(gctl.newGloss)

					for ( var cod in gctl.newGloss) {
						console.log(cod)
						if (gctl.newGloss[cod] == ""
								&& cod != "SBI_GL_CONTENTS") {
							return false;
						}
					}
					return true;
					//											
					// return
					// (JSON.stringify(EmptyGloss)
					// !=
					// JSON
					// .stringify(gctl.newGloss));
				}
			},
			 
			
//			"web-content/WEB-INF/jsp/tools/glossary/dialog-new-glossary.html"
			templateUrl : '/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/glossary/dialog-new-glossary.html',
			targetEvent : ev,
		})

	};

	// <!-- tree -->
	ctr.selectedItem = {};

	ctr.TreeOptionsWord = {

		accept : function(sourceNodeScope, destNodesScope, destIndex) {
			console.log("accept TreeOptionsWord")
			return false;
		}
	};

	ctr.TreeOptions = {
		accept : function(sourceNodeScope, destNodesScope, destIndex) {
			console.log("accept TreeOptions")

			if (destNodesScope.hasChild()) {
				var child = destNodesScope.childNodes()
				for (ch in child) {
					if (child[ch].$modelValue.CONTENT_CD != undefined) {
						return false;
					}
				}
			}
			return true;
		},

		beforeDrop : function(event) {
		},

		dragStart : function(event) {
		},

		dragStop : function(event) {
		}
	};

	ctr.TreeOptionsChips = {
		accept : function(sourceNodeScope, destNodesScope, destIndex) {
			console.log("accept TreeOptionsChips")
			console.log(sourceNodeScope)
			console.log(angular.element(document
					.querySelector('.linkChips .md-chips')))

			console.log(angular.element(
					document.querySelector('.linkChips .md-chips')).css(
					"box-shadow"))

			var found = $filter('filter')(ctr.newWord.LINK, {
				WORD_ID : sourceNodeScope.$modelValue.WORD_ID
			}, true);

			if (found != 0) {
				console.log("WORD GIa PRESENTE")

				

				angular.element(document.querySelector('.linkChips .md-chips'))
						.css("box-shadow", "0 2px rgb(255, 0, 0)");
				$timeout(function() {
					angular.element(document.querySelector('.linkChips .md-chips'))
					.css("box-shadow", "");
				}, 500);

				return false;
			}
			console.log("WORD non PRESENTE")
			return true;
		},

		beforeDrop : function(event) {
		},

		dragStart : function(event) {
		},

		dragStop : function(event) {
		}

	};

	ctr.remove = function(scope) {
		console.log("remove")
		scope.remove();
	};

	ctr.toggle = function(scope) {
		scope.toggle();
	};

	ctr.hasVocabolaryChild = function(scope) {
		for (ch in scope.CHILD) {
			if (scope.CHILD[ch].CONTENT_CD == undefined) {
				return false;
			}
		}
		return true;
	};

	ctr.newSubItemRootGloss = function(parent) {
		console.log("add childrto p")

		parent.SBI_GL_CONTENTS.push({
			CONTENT_ID : 'Phhh',
			GLOSSARY_ID : 1,
			PARENT_ID : 'P1',
			CONTENT_CD : 'c2',
			CONTENT_NM : parent.GLOSSARY_NM + ' ch'
					+ (parent.SBI_GL_CONTENTS.length + 1),
			CONTENT_DS : 'descr2',
			DEPTH : 1,
			CHILD : []
		});
	};

	ctr.newSubItem = function(scope, parent) {
		console.log("add childr")
		var nodeData = scope.$modelValue;
		console.log(parent)

		parent.CHILD.push({
			CONTENT_ID : 'Phhh',
			GLOSSARY_ID : 1,
			PARENT_ID : 'P1',
			CONTENT_CD : 'c2',
			CONTENT_NM : parent.CONTENT_NM + ' ch' + (parent.CHILD.length + 1),
			CONTENT_DS : 'descr2',
			DEPTH : 1,
			CHILD : []
		});
	};

	// <!-- fine tree -->

}
