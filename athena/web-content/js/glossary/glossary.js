var app = angular.module('AIDA_GESTIONE-VOCABOLI', [ 'ngMaterial', 'ui.tree',
		'angularUtils.directives.dirPagination', 'ng-context-menu' ]);

app.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey').accentPalette(
			'blue-grey');
});

app.constant('ENDPOINT_URI', 'http://' + hostName + ':' + serverPort
		+ '/athena/restful-services/1.0/');

app.service('restServices', function($http, ENDPOINT_URI) {

	var service = this;
	var path = "glossary";

	function getBaseUrl(endP_path) {
		endP_path == undefined ? endP_path = path : true;
		return ENDPOINT_URI + endP_path + "/";
	}
	;

	service.get = function(endP_path, req_Path, item) {
		item == undefined ? item = "" : item = "?" + item;
		console.log("service.get");
		console.log("endP_path= " + endP_path)
		console.log("req_Path=" + req_Path)
		console.log("item=" + item)
		return $http.get(getBaseUrl(endP_path) + "" + req_Path + "" + item);
	};

	// prendo i nodi di un glossario

	service.getGlossNode = function(glossID, nodeID) {
		console.log(getBaseUrl() + "listContents?GLOSSARY_ID=" + glossID
				+ "&PARENT_ID=" + nodeID)
		return $http.get(getBaseUrl() + "listContents?GLOSSARY_ID=" + glossID
				+ "&PARENT_ID=" + nodeID);
	};

	// prendo tutte le informazioni di uno specifico word
	service.getWord = function(item) {
		return $http.get(getBaseUrl() + "getWord?WORD_ID=" + item);
	};

	// salvo un word
	service.addWord = function(item) {
		return $http.post(getBaseUrl() + "addWord", item);
	};

	// elimino un word
	service.deleteWord = function(item) {
		return $http.post(getBaseUrl() + "deleteWord", item);
	};

	service.WordLike = function(item) {
		return $http.get(getBaseUrl() + "listWords?WORD=" + item);
	};

	service.AttributeLike = function(item) {
		return $http.get(getBaseUrl() + "listAttribute?ATTR=" + item);
	};

	service.AlterContentGloss = function(item) {
		console.log("AlterContentGloss")
		return $http.post(getBaseUrl() + "ModifyContentsGlossary", item);
	}

});

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

var EmptyLogicalNode = {
	CONTENT_ID : '',
	GLOSSARY_ID : '',
	PARENT_ID : '',
	CONTENT_CD : '',
	CONTENT_NM : '',
	CONTENT_DS : '',
	DEPTH : '',
	CHILD : []
}

var EmptyGloss = {
	// GLOSSARY_ID : 1,
	GLOSSARY_CD : "",
	GLOSSARY_NM : "",
	GLOSSARY_DS : "",
	SBI_GL_CONTENTS : [], // DOVREBBERO ESSERE I NODI
	NEWGLOSS : true
// MI SERVE SOLO PER LA CREAZIONE DI UN NODO E POI LO RIMUOVO
};

var SBI_GL_ATTRIBUTES = [ {
	ATTRIBUTE_ID : 1,
	ATTRIBUTE_CD : 1,
	ATTRIBUTE_NM : "PROP1",
	ATTRIBUTE_DS : "DS PROP1",
	MANDATORY_FL : 1,
	TYPE : 1,
	DOMAIN : 1,
	FORMAT : 1,
	DISPLAT_TP : 1,
	ORDER : 1
}, {
	ATTRIBUTE_ID : 2,
	ATTRIBUTE_CD : 2,
	ATTRIBUTE_NM : "PROP2",
	ATTRIBUTE_DS : "DS PROP2",
	MANDATORY_FL : 2,
	TYPE : 2,
	DOMAIN : 2,
	FORMAT : 2,
	DISPLAT_TP : 2,
	ORDER : 2
}, {
	ATTRIBUTE_ID : 3,
	ATTRIBUTE_CD : 3,
	ATTRIBUTE_NM : "PROP3",
	ATTRIBUTE_DS : "DS PROP3",
	MANDATORY_FL : 3,
	TYPE : 3,
	DOMAIN : 3,
	FORMAT : 3,
	DISPLAT_TP : 3,
	ORDER : 3
} ];

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

app.controller('Controller', [ "restServices", "$q", "$scope", "$mdDialog",
		"$filter", "$timeout", "$mdToast", funzione ]);

function funzione(restServices, $q, $scope, $mdDialog, $filter, $timeout,
		$mdToast) {
	ctr = this;
	ctr.showPreloader = false;
	ctr.showSearchPreloader = false;
	ctr.activeTab = 'Glossario';
	ctr.filterSelected = true;
	ctr.words = [];
	getAllWords();

	// var c = 0;
	//
	// for (x in wor) {
	// for (var i = 0; i < 20; i++) {
	// var tmp = JSON.parse(JSON.stringify(wor[x]))
	// tmp.WORD = tmp.WORD + "-" + i
	// tmp.WORD_ID = tmp.WORD_ID * i * 100
	// ctr.words.push(tmp)
	//
	// }
	// }

	ctr.newWord = JSON.parse(JSON.stringify(EmptyWord));
	ctr.glossary = glos;
	getAllGloss();
	ctr.newGloss = JSON.parse(JSON.stringify(EmptyGloss));
	ctr.propWord = SBI_GL_ATTRIBUTES;
	ctr.querySearchProp = "";
	self.searchTextProp = null;
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

			// scarico tutte le informazioni del word
			getWord(ele);

			// ctr.newWord = ele;
		}

	}

	// querysearch della select autocompleate durante l'inserimento delle
	// proprietà in new word
	ctr.tmpPropSearch = "";
	ctr.prevPropSearch = "";

	ctr.querySearchProp = function(query) {
		console.log("querySearchProp " + query)

		ctr.tmpPropSearch = query;

		if (ctr.tmpPropSearch != query || ctr.prevPropSearch == query) {
			
			console.log("(ctr.tmpPropSearch != query)"+(ctr.tmpPropSearch != query))
					
					console.log("(ctr.prevPropSearch == query)"+ (ctr.prevPropSearch == query))
					
					console.log("interrompo la ricerca prop di ele "+query)
			return;
		}

		if (ctr.selectedItem != null) {
			console.log("ctr.selectedItem.ATTRIBUTE_NM="
					+ ctr.selectedItem.ATTRIBUTE_NM)
			console.log("query=" + query)
			if (ctr.selectedItem.ATTRIBUTE_NM === query) {
				console.log("xxxxxinterrompo la ricerca prop di ele " + query)

				return;
			}
		}

		var def = $q.defer();
		$timeout(function() {

			console.log(query)
			if (ctr.tmpPropSearch != query || ctr.prevPropSearch == query) {
				console.log((ctr.tmpPropSearch != query)
						+ "interrompo la ricerca prop di ele "
						+ (ctr.prevPropSearch == query))
				return;
			}

			ctr.prevPropSearch = query;

			restServices.AttributeLike(query).success(
					function(data, status, headers, config) {
						console.log("AttributeLike Ottenuto")
						console.log(data)
						if (data.hasOwnProperty("errors")) {
							return;
						} else {
							def.resolve(data);
						}
						hidePreloader();
						
					}).error(function(data, status, headers, config) {
				console.log("AttributeLike non Ottenuti " + status);
			
			})
		}, 1000);

		ctr.prevPropSearch = "-1-";

		return def.promise.then(
				function(val) {

					for (var i = 0; i < val.length; i++) {
						if (JSON.stringify(ctr.newWord.SBI_GL_WORD_ATTR)
								.toString().toLowerCase().indexOf(
										val[i].ATTRIBUTE_NM.toString()
												.toLowerCase()) != -1) {
							val.splice(i, 1);
							i--;
						}
					}
					return val;
				}, function(val) {
					console.log("promisenonok")

				})

		// var results = $filter('filter')(ctr.propWord, {
		// ATTRIBUTE_NM : query.toUpperCase()
		// }, compareForNestedFiltering);
		//
		// console.log(results)
		// return results;
	}

	ctr.addProp = function(prop) {
		console.log("aggiungo attributo")
		console.log(prop)

		var np = {};
		np.ATTRIBUTE_NM = prop.Prop.ATTRIBUTE_NM;
		np.ATTRIBUTE_ID = prop.Prop.ATTRIBUTE_ID;
		np.VALUE = prop.Val;

		ctr.newWord.SBI_GL_WORD_ATTR.push(np)
		ctr.tmpAttr = {};
	};

	ctr.removeProp = function(prop) {
		console.log("removeprop")
		ctr.newWord.SBI_GL_WORD_ATTR.splice(ctr.newWord.SBI_GL_WORD_ATTR
				.indexOf(prop), 1)
	}

	ctr.propPresent = function(query) {
		if (query == null || query == undefined || angular.equals({}, query)) {
			return false;
		}

		console.log(query)

		var results;
		if (typeof query !== 'object') {
			console.log("non obj")
			results = $filter('filter')(ctr.propWord, {
				ATTRIBUTE_NM : query.toUpperCase()
			}, true);
		} else {
			console.log(" obj")
			results = $filter('filter')(ctr.propWord, {
				ATTRIBUTE_NM : query.ATTRIBUTE_NM.toUpperCase()
			}, true);
		}

		if (results.length != 0)
			return true;

		return false;
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

	// non sincronizzato( sulla base dei word presenti)
	ctr.querySearchold = function(chip) {
		console.log("querySearch")
		var found = $filter('filter')(ctr.words, {
			WORD : chip.toUpperCase()
		}, compareForNestedFiltering);
		return found;
	}

	ctr.tmpWordChipsSearch = "";
	ctr.prevChipsSearch = "";

	ctr.querySearch = function(chip) {
		var def = $q.defer();

		ctr.tmpWordChipsSearch = chip;
		$timeout(function() {

			if (ctr.tmpWordChipsSearch != chip || ctr.prevChipsSearch == chip) {
				console.log("interrompo la ricerca chips di ele " + chip)
				return;
			}

			ctr.prevChipsSearch = chip;

			restServices.WordLike(chip).success(
					function(data, status, headers, config) {
						console.log("chipsword Ottenuto")
						console.log(data)
						if (data.hasOwnProperty("errors")) {
							return;
						} else {
							def.resolve(data);
						}
						hidePreloader();
					}).error(function(data, status, headers, config) {
				console.log("Words non Ottenuti " + status);
			})
		}, 1000);

		ctr.prevChipsSearch = "-1-";

		return def.promise.then(function(val) {

			for (var i = 0; i < val.length; i++) {
				if (JSON.stringify(ctr.newWord.LINK).toString().toLowerCase()
						.indexOf(val[i].WORD.toString().toLowerCase()) != -1) {
					val.splice(i, 1);
					i--;
				}
			}
			return val;
		}, function(val) {
			console.log("promisenonok")

		})

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

			restServices.addWord(ctr.newWord)
					.success(
							function(data, status, headers, config) {
								console.log("word salvato ottenuto Ottenuto")
								console.log(data)
								if (data.hasOwnProperty("errors")) {
									return;
								} else {

									ctr.newWord.WORD_ID = data.id;
									delete ctr.newWord.NEWWORD;
									product.push(ctr.newWord);
									ctr.newWord = JSON.parse(JSON
											.stringify(EmptyWord));
								}
								hidePreloader();
							}).error(function(data, status, headers, config) {
						console.log("Words non Ottenuti " + status);
					})

		} else {
			console.log("modificato")
			ctr.newWord = JSON.parse(JSON.stringify(EmptyWord));
		}

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

		$mdDialog
				.show({
					controllerAs : 'gloCtrl',
					controller : function($mdDialog) {
						var gctl = this;
						gctl.newGloss = ctr.newGloss;

						gctl.submit = function() {
							console.log(gctl.newGloss)
							if (gctl.newGloss.NEWGLOSS != undefined) {
								console.log("salvo")
								gctl.newGloss.GLOSSARY_ID = (new Date())
										.getTime();
								delete gctl.newGloss.NEWGLOSS;
								ctr.glossary.push(gctl.newGloss);

							} else {
								console.log("modifico")
							}

							gctl.newGloss = JSON.parse(JSON
									.stringify(EmptyGloss));

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

					// "web-content/WEB-INF/jsp/tools/glossary/dialog-new-glossary.html"
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
			console.log("beforeDrop")
			console.log(event)

			var n1 = event.dest.nodesScope.$nodeScope == null ? null
					: event.dest.nodesScope.$nodeScope.$id;
			var n2 = event.source.nodesScope.$nodeScope == null ? null
					: event.source.nodesScope.$nodeScope.$id;

			if (n1 == n2) {
				console.log("nessun movimento")
				return;
			}

			var confirm = $mdDialog.confirm().parent(
					angular.element(document.body)).title(
					'Sei sicuro di voler effettuare questa modifica?').content(
					'I dati saranno immediatamente salvati sul database.')
					.ariaLabel('Muovi').ok('Muovi').cancel('Annulla')

			$mdDialog
					.show(confirm)
					.then(
							function() {
								console.log("accettato");
								console.log(event.source.nodeScope.item)

								var elem = event.source.nodeScope.item;

								elem.PARENT_ID = event.dest.nodesScope.$nodeScope == null ? null
										: event.dest.nodesScope.$nodeScope.item.CONTENT_ID

								showPreloader();
								restServices
										.AlterContentGloss(elem)
										.success(
												function(data, status, headers,
														config) {

													if (data
															.hasOwnProperty("errors")) {
														showErrorToast(data.errors[0].message)
													} else {
														showToast(
																"Salvato con successo",
																3000);
														event.dest.nodesScope
																.insertNode(
																		event.dest.index,
																		elem);
														event.source.nodesScope.$modelValue
																.splice(
																		event.source.index,
																		1);
													}

													event.dest.nodesScope
															.expand();
													hidePreloader();
												}).error(
												function(data, status, headers,
														config) {
													hidePreloader();
												});

							}, function() {
								console.log("rifiutato");
								hidePreloader();
							});

			// non faccio spostare l'elemento automaticamente
			event.source.nodeScope.$$apply = false;

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
					angular.element(
							document.querySelector('.linkChips .md-chips'))
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

	ctr.rename = function(node) {
		console.log("rename")
		console.log(node)
		console.log(node.$modelValue)

		$mdDialog
				.show(
						{
							controllerAs : "renCtrl",
							controller : function($mdDialog) {
								var rn = this;
								rn.oldValue = node.$modelValue.CONTENT_NM;
								rn.salva = function(val) {
									console.log("salvo " + val)

									showPreloader();
									restServices
											.AlterContentGloss(node.$modelValue)
											.success(
													function(data, status,
															headers, config) {
														console.log("ok")
														if (data
																.hasOwnProperty("errors")) {
															showErrorToast(data.errors[0].message)
														} else {
															showToast(
																	"Salvato con successo",
																	3000);

															node.$modelValue.CONTENT_NM = val;
														}
														hidePreloader();
													})
											.error(
													function(data, status,
															headers, config) {
														console.log("nonok")
														showToast("Errore nel salvataggio "
																+ status);
														hidePreloader();
													});

									$mdDialog.hide();
								};
								rn.annulla = function() {
									console.log("annulla");
									$mdDialog.hide();
								}

							},
							templateUrl : 'rename.dialog.html',
							parent : angular.element(document.body),

						}).then(
						function(answer) {
							console.log('You said the information was "'
									+ answer + '".');
						}, function() {
							console.log('You cancelled the dialog.');
						});

	};

	ctr.toggle = function(scope, item, gloss) {
		console.log("toggle")
		scope.preloader = true;
		if (scope.collapsed) {
			ctr.getGlossaryNode(null, item, scope)
		} else {
			scope.toggle();
			scope.preloader = false;
		}
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

		// var tmpNW = {
		// CONTENT_ID : 'Phhh',
		// GLOSSARY_ID : 1,
		// PARENT_ID : 'P1',
		// CONTENT_CD : 'c2',
		// CONTENT_NM : parent.CONTENT_NM + ' ch' + (parent.CHILD.length + 1),
		// CONTENT_DS : 'descr2',
		// DEPTH : 1,
		// CHILD : []
		// }

		$mdDialog
				.show({
					controllerAs : "renCtrl",
					controller : function($mdDialog) {
						var rn = this;
						rn.tmpNW = JSON.parse(JSON.stringify(EmptyLogicalNode));

						rn.salva = function() {
							console.log("salvo nuovo nodo logico")
							rn.tmpNW.PARENT_ID = parent.CONTENT_ID;
							rn.tmpNW.GLOSSARY_ID = ctr.selectedGloss.GLOSSARY_ID;

							showPreloader();
							restServices
									.addWord(rn.tmpNW)
									.success(
											function(data, status, headers,
													config) {
												console.log("ok")
												if (data
														.hasOwnProperty("errors")) {
													showErrorToast(data.errors[0].message)
												} else {
													showToast(
															"Salvato con successo",
															3000);

													parent.CHILD.push(tmpNW);

												}
												hidePreloader();
											})
									.error(
											function(data, status, headers,
													config) {
												console.log("nonok")
												showToast("Errore nel salvataggio del nuovo nodo logico "
														+ status);
												hidePreloader();
											});

							$mdDialog.hide();
						};
						rn.annulla = function() {
							console.log("annulla");
							$mdDialog.hide();
						}

					},
					templateUrl : 'new.logical.node.dialog.html',
					parent : angular.element(document.body),

				})

	};

	// <!-- fine tree -->

	// pagination word
	function changeWordItemPP() {
		var lbw = angular.element(document.querySelector('.leftBox_word'))[0].offsetHeight;
		var tbw = angular.element(document.querySelector('.md-toolbar-tools'))[0].offsetHeight;
		var bpw = angular.element(document.querySelector('.box_pagination'))[0].offsetHeight;
		bpw == 0 ? bpw = 40 : bpw = bpw;
		var nit = parseInt((lbw - tbw - bpw) / 31)
		ctr.WordItemPerPage = nit;
	}

	$scope
			.$watch(
					function() {
						return angular.element(document
								.querySelector('.leftBox_word'))[0].offsetHeight;
					}, function(newValue, oldValue) {
						if (newValue != oldValue) {
							changeWordItemPP()
						}
					}, true);

	ctr.WordItemPerPage = 5;
	changeWordItemPP();

	// rest call function

	function getAllWords() {
		console.log("getAllWords")
		showPreloader();
		restServices.get("glossary", "listWords").success(
				function(data, status, headers, config) {
					console.log("Words Ottenuti " + status)
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						showErrorToast(data.errors[0].message)
					} else {
						ctr.words = data;
					}

					hidePreloader();
				}).error(function(data, status, headers, config) {
			console.log("Words non Ottenuti " + status);
			showErrorToast('Ci sono errori! \n status ' + status)
			hidePreloader();
		})

	}

	function getAllGloss() {
		console.log("getAllGloss")
		showPreloader();
		restServices.get("glossary", "listGlossary").success(
				function(data, status, headers, config) {
					console.log("Glossary Ottenuti " + status)
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						showErrorToast(data.errors[0].message)
					} else {
						ctr.glossary = data;
					}

					hidePreloader();
				}).error(function(data, status, headers, config) {
			console.log("Glossary non Ottenuti " + status);
			showErrorToast('Ci sono errori! \n status ' + status)
			hidePreloader();
		})

	}

	ctr.tmpWordSearch = "";
	ctr.prevSearch = "";

	ctr.WordLike = function(ele) {
		console.log("WordLike" + ele)
		ctr.tmpWordSearch = ele;
		$timeout(function() {

			if (ctr.tmpWordSearch != ele || ctr.prevSearch == ele) {
				console.log("interrompo la ricerca  di ele " + ele)
				return;
			}

			ctr.prevSearch = ele;
			ctr.showSearchPreloader = true;
			restServices.WordLike(ele).success(
					function(data, status, headers, config) {
						console.log("WordLike Ottenuti " + status)
						console.log(data)
						ctr.words = data;
						ctr.showSearchPreloader = false;
					}).error(function(data, status, headers, config) {
				console.log("WordLike non Ottenuti " + status);
				ctr.showSearchPreloader = false;
			})

		}, 1000);

	}

	function getWord(ele) {
		console.log("getWord")
		showPreloader();
		restServices.getWord(ele.WORD_ID).success(
				function(data, status, headers, config) {
					console.log("Word Ottenuto")
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						showErrorToast(data.errors[0].message)
						ctr.newWord = JSON.parse(JSON.stringify(EmptyWord));
					} else {
						ctr.newWord = data;
						ctr.activeTab = 'Vocabolo';
					}

					hidePreloader();

				}).error(function(data, status, headers, config) {
			console.log("Words non Ottenuti " + status);
			showErrorToast('Ci sono errori! \n status ' + status)
			hidePreloader();
		})

	}

	ctr.getGlossaryNode = function(gloss, node, togg) {
		console.log("getGlossaryNode")
		console.log(node)
		console.log(gloss)
		var PARENT_ID = (node == null ? null : node.CONTENT_ID);
		var GLOSSARY_ID = (gloss == null ? null : gloss.GLOSSARY_ID);
		console.log(PARENT_ID)
		console.log(GLOSSARY_ID)

		restServices.get("glossary", "listContents",
				"GLOSSARY_ID=" + GLOSSARY_ID + "&PARENT_ID=" + PARENT_ID)
				.success(function(data, status, headers, config) {
					console.log("nodi ottnuti")
					console.log(data)
					console.log(node)

					if (node == null) {
						gloss.SBI_GL_CONTENTS = data
					} else {
						node.CHILD = data;
					}

					if (togg != undefined) {
						togg.toggle();
						togg.preloader = false;
					}
				}).error(function(data, status, headers, config) {
					console.log("nodi non ottenuti " + status)
					if (togg != undefined) {
						togg.toggle();
						togg.preloader = false;
					}
				})
	}

	function showErrorToast(err, time) {
		var timer = time == undefined ? 6000 : time
		console.log("ci sono errori")
		console.log(err)
		$mdToast.show($mdToast.simple().content('Ci sono errori! \n ' + err)
				.position('top').action('OK').highlightAction(false).hideDelay(
						timer));
	}

	function showToast(text, time) {
		var timer = time == undefined ? 6000 : time
		console.log("ci sono errori")
		console.log(text)
		$mdToast.show($mdToast.simple().content(text).position('top').action(
				'OK').highlightAction(false).hideDelay(timer));
	}

	function showPreloader(pre) {
		ctr.showPreloader = true;

	}
	function hidePreloader(pre) {
		ctr.showPreloader = false;
	}

	ctr.prova = function() {
		console.log("prova")
	}
}

// app.directive('resize', function ($window) {
// return function (scope, element) {
// var ww = angular.element($window)
// console.log(angular.element($window))
// var changeHeight = function() {
// console.log("resize");
//            
// var
// lbw=angular.element(document.querySelector('.leftBox_word'))[0].offsetHeight;
// var
// tbw=angular.element(document.querySelector('.md-toolbar-tools'))[0].offsetHeight;
// var
// bpw=angular.element(document.querySelector('.box_pagination'))[0].offsetHeight;
// bpw==0 ? bpw=40:bpw=bpw;
// console.log(lbw);
// console.log(tbw);
// console.log(bpw);
//    		
// var nit=parseInt((lbw-tbw-bpw)/35)
// console.log(nit)
// WIPR=nit;
//            
//            
// };
// ww.bind('resize', function () {
// changeHeight(); // when window size gets changed
// });
// changeHeight(); // when page loads
// }
// })

