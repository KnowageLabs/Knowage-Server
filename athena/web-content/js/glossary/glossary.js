var app = angular.module('AIDA_GESTIONE-VOCABOLI', [ 'ngMaterial', 'ui.tree',
		'angularUtils.directives.dirPagination', 'ng-context-menu' ]);

app.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey').accentPalette(
			'blue-grey');
});

app.constant('ENDPOINT_URI', 'http://' + hostName + ':' + serverPort
		+ '/athena/restful-services/1.0/');

app.service('translate', function() {
	this.load = function(key) {
		return messageResource.get(key, 'messages');
	};
});

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

	service.remove = function(endP_path, req_Path, item) {
		item == undefined ? item = "" : item = "?" + item;
		return $http.post(getBaseUrl(endP_path) + "" + req_Path + "" + item);
	};

	service.post = function(endP_path, req_Path, item) {
		return $http.post(getBaseUrl(endP_path) + "" + req_Path, item);
	};

	// prendo i nodi di un glossario

	service.getGlossNode = function(glossID, nodeID) {
		console.log(getBaseUrl() + "listContents?GLOSSARY_ID=" + glossID
				+ "&PARENT_ID=" + nodeID)
		return $http.get(getBaseUrl() + "listContents?GLOSSARY_ID=" + glossID
				+ "&PARENT_ID=" + nodeID);
	};

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
	CHILD : [],
	NEWCONT : true
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

// var SBI_GL_ATTRIBUTES = [ {
// ATTRIBUTE_ID : 1,
// ATTRIBUTE_CD : 1,
// ATTRIBUTE_NM : "PROP1",
// ATTRIBUTE_DS : "DS PROP1",
// MANDATORY_FL : 1,
// TYPE : 1,
// DOMAIN : 1,
// FORMAT : 1,
// DISPLAT_TP : 1,
// ORDER : 1
// }, {
// ATTRIBUTE_ID : 2,
// ATTRIBUTE_CD : 2,
// ATTRIBUTE_NM : "PROP2",
// ATTRIBUTE_DS : "DS PROP2",
// MANDATORY_FL : 2,
// TYPE : 2,
// DOMAIN : 2,
// FORMAT : 2,
// DISPLAT_TP : 2,
// ORDER : 2
// }, {
// ATTRIBUTE_ID : 3,
// ATTRIBUTE_CD : 3,
// ATTRIBUTE_NM : "PROP3",
// ATTRIBUTE_DS : "DS PROP3",
// MANDATORY_FL : 3,
// TYPE : 3,
// DOMAIN : 3,
// FORMAT : 3,
// DISPLAT_TP : 3,
// ORDER : 3
// } ];
//
// var wor = [ {
// WORD_ID : 1,
// WORD : "CLIENTE",
// DESCR : "IL CLIENTE è ....",
// FORMULA : "",
// STATE : "OK",
// CATEGORY : "PERSONE",
// LINK : [],
// SBI_GL_WORD_ATTR : []
// }, {
// WORD_ID : 2,
// WORD : "PROGETTO",
// DESCR : "UN PROGETTO è....",
// FORMULA : "",
// STATE : "OK",
// CATEGORY : "ALTRO",
// SBI_GL_WORD_ATTR : [],
// LINK : []
// }, {
// WORD_ID : 3,
// WORD : "CASA",
// DESCR : "UNA CASA è ...",
// FORMULA : "",
// STATE : "OK",
// CATEGORY : "ABITAZIONI",
// SBI_GL_WORD_ATTR : [],
// LINK : []
// }, ];

app.controller('Controller', [ "translate", "restServices", "$q", "$scope",
		"$mdDialog", "$filter", "$timeout", "$mdToast", funzione ]);

function funzione(translate, restServices, $q, $scope, $mdDialog, $filter,
		$timeout, $mdToast) {
	ctr = this;
	ctr.showPreloader = false;
	ctr.showSearchPreloader = false;
	ctr.activeTab = 'Glossari';
	ctr.filterSelected = true;
	ctr.words = [];
	getAllWords();

	ctr.newWord = JSON.parse(JSON.stringify(EmptyWord));
	ctr.glossary;
	getAllGloss();
	ctr.newGloss = JSON.parse(JSON.stringify(EmptyGloss));
	ctr.propWord;
	ctr.querySearchProp = "";
	self.searchTextProp = null;
	ctr.selectedGloss = {};

	ctr.modifyWord = function(word) {
		if (JSON.stringify(EmptyWord) != JSON.stringify(ctr.newWord)) {
			var confirm = $mdDialog
					.confirm()
					.title(translate.load("sbi.glossary.word.modify.progress"))
					.content(
							translate
									.load("sbi.glossary.word.modify.progress.message.modify"))
					.ariaLabel('Lucky day').ok(
							translate.load("sbi.general.continue")).cancel(
							translate.load("sbi.general.cancel"));

			console.log
			$mdDialog.show(confirm).then(function() {

				resetForm(word)

			}, function() {
				console.log('Annulla');
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
			text.title = translate.load("sbi.glossary.word.modify.progress");
			text.content = translate
					.load("sbi.glossary.word.modify.progress.message.abort");
			text.ok = translate.load("sbi.general.yes");
			text.cancel = translate.load("sbi.general.No");

		} else {
			text = {};
			text.title = translate.load("sbi.glossary.word.modify.progress");
			text.content = translate
					.load("sbi.glossary.word.modify.progress.message.new");
			text.ok = translate.load("sbi.general.yes");
			text.cancel = translate.load("sbi.general.No");
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

			console.log("(ctr.tmpPropSearch != query)"
					+ (ctr.tmpPropSearch != query))

			console.log("(ctr.prevPropSearch == query)"
					+ (ctr.prevPropSearch == query))

			console.log("interrompo la ricerca prop di ele " + query)
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

			restServices.get("glossary", "listAttribute", "ATTR=" + query)
					.success(function(data, status, headers, config) {
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

			restServices.get("glossary", "listWords", "WORD=" + chip).success(
					function(data, status, headers, config) {
						console.log("chipsword Ottenuto")
						console.log(data)
						if (data.hasOwnProperty("errors")) {

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
			console.log("salvo")
			ctr.newWord.SaveOrUpdate = "Save";
		} else {
			console.log("modificato")
			ctr.newWord.SaveOrUpdate = "Update";
			// ctr.newWord = JSON.parse(JSON.stringify(EmptyWord));
		}

		for (var i = 0; i < ctr.newWord.SBI_GL_WORD_ATTR.length; i++) {
			ctr.newWord.SBI_GL_WORD_ATTR[i].ORDER = i;
		}

		for (var i = 0; i < ctr.newWord.LINK.length; i++) {
			ctr.newWord.LINK[i].ORDER = i;
		}

		showPreloader();
		restServices.post("glossary", "addWord", ctr.newWord).success(
				function(data, status, headers, config) {
					console.log("word salvato  Ottenuto")
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						showErrorToast(data.errors[0].message)
						showToast(translate
								.load("sbi.glossary.word.save.error"), 3000);
					} else {

						if (ctr.newWord.SaveOrUpdate == "Save") {

							ctr.newWord.WORD_ID = data.id;
							delete ctr.newWord.NEWWORD;
							product.push(ctr.newWord);
						} else {
							ctr.newWord.oldWord.WORD = ctr.newWord.WORD;
						}

						ctr.newWord = JSON.parse(JSON.stringify(EmptyWord));

						showToast(translate
								.load("sbi.glossary.word.save.success"), 3000);
						ctr.activeTab = 'Glossari';
					}
					hidePreloader();
				}).error(function(data, status, headers, config) {
			showErrorToast("word non salvato " + status)
			console.log("Words non salvato " + status);
			showToast(translate.load("sbi.glossary.word.save.error"), 3000);
			hidePreloader();
		})

	};

	ctr.deleteWord = function(ev) {
		// Appending dialog to document.body to cover
		// sidenav in docs app
		console.log("deleteWord")
		var confirm = $mdDialog.confirm().title(
				translate.load("sbi.glossary.word.delete")).content(
				translate.load("sbi.glossary.word.delete.message")).ariaLabel(
				'Lucky day').ok(translate.load("sbi.generic.delete")).cancel(
				translate.load("sbi.myanalysis.delete.cancel")).targetEvent(ev);
		console.log(confirm)

		var wds = ctr.words;
		var nw = ctr.newWord;
		$mdDialog
				.show(confirm)
				.then(
						function() {
							showPreloader();
							restServices
									.remove("glossary", "deleteWord",
											"WORD_ID=" + ev.WORD_ID)
									.success(
											function(data, status, headers,
													config) {
												console.log("Word eliminato")
												console.log(data)
												if (data
														.hasOwnProperty("errors")) {
													showErrorToast(data.errors[0].message)
													showToast(
															translate
																	.load("sbi.glossary.word.delete.error"),
															3000);

												} else {
													var index = wds.indexOf(ev);
													wds.splice(index, 1);
													showToast(
															translate
																	.load("sbi.glossary.word.delete.success"),
															3000);
												}
												hidePreloader();

											})
									.error(
											function(data, status, headers,
													config) {
												console
														.log("WORD NON ELMINIATO "
																+ status);
												showErrorToast("word non eliminato "
														+ status);
												showToast(
														translate
																.load("sbi.glossary.word.delete.error"),
														3000);

											})

							// controllo se l'elemento
							// che si vule eliminare è
							// in fase di modifica
							if (nw.WORD_ID === ev.WORD_ID) {
								// console.log("Sto modificando l'elemento che
								// sto eliminando")
								nw.NEWWORD = true;
							}

						}, function() {
							console.log('You decided to keep your debt.');
						});
	};

	// glossary

	ctr.CloneGloss = function(ev, gl) {

		$mdDialog
				.show({
					controllerAs : 'gloCtrl',
					controller : function($mdDialog) {
						var gctl = this;

						if (gl != undefined) {
							// load glossary data
							gctl.headerTitle = translate
									.load("sbi.glossary.clone");

							showPreloader();
							restServices
									.get("glossary", "getGlossary",
											"GLOSSARY_ID=" + gl.GLOSSARY_ID)
									.success(
											function(data, status, headers,
													config) {
												console
														.log("glossary Ottenuto "
																+ status)
												console.log(data)
												if (data
														.hasOwnProperty("errors")) {
													showErrorToast(data.errors[0].message);
													$mdDialog.hide();
													showToast(
															translate
																	.load("sbi.glossary.load.error"),
															3000);
												} else {
													gctl.newGloss = data;
												}

												hidePreloader();
											})
									.error(
											function(data, status, headers,
													config) {
												console
														.log("glossary non Ottenuto "
																+ status);
												showErrorToast('Ci sono errori! \n status '
														+ status);
												hidePreloader();
												translate
														.load("sbi.glossary.load.error");
											})

						} else {
							showErrorToast("Errore! glossario non puo essere nullo")
							$mdDialog.hide();
							showToast(
									translate.load("sbi.glossary.load.error"),
									3000);
							return false;
						}

						gctl.annulla = function($event) {
							$mdDialog.hide();

						};

						gctl.submit = function() {

							console.log(gl)
							showPreloader();
							restServices
									.post("glossary", "cloneGlossary",
											gctl.newGloss)
									.success(
											function(data, status, headers,
													config) {
												console.log("Gloss clonato")
												console.log(data)
												if (data
														.hasOwnProperty("errors")) {
													showErrorToast(data.errors[0].message);

													showToast(
															translate
																	.load("sbi.glossary.clone.error"),
															3000);

												} else {
													showToast(
															translate
																	.load("sbi.glossary.clone.success"),
															3000);
													gctl.newGloss.GLOSSARY_ID = data.id;
													ctr.glossary
															.push(gctl.newGloss)
													$mdDialog.hide();

												}
												hidePreloader();

											})
									.error(
											function(data, status, headers,
													config) {
												hidePreloader();
												$mdDialog.hide();
												showToast(
														translate
																.load("sbi.glossary.clone.error"),
														3000);
											});

						}

					},
					templateUrl : '/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/glossary/dialog-new-glossary.html',
					targetEvent : ev,
				})

	}

	ctr.deleteGlossary = function(ev) {
		// Appending dialog to document.body to cover
		// sidenav in docs app
		console.log("deleteGloss")
		var confirm = $mdDialog.confirm().title(
				translate.load("sbi.glossary.delete")).content(
				translate.load("sbi.glossary.delete.message")).ariaLabel(
				'Lucky day').ok(translate.load("sbi.generic.delete")).cancel(
				translate.load("sbi.ds.wizard.cancel")).targetEvent(ev);
		console.log(confirm)

		var wds = ctr.glossary;
		$mdDialog
				.show(confirm)
				.then(
						function() {
							showPreloader();
							restServices
									.remove("glossary", "deleteGlossary",
											"GLOSSARY_ID=" + ev.GLOSSARY_ID)
									.success(
											function(data, status, headers,
													config) {
												console
														.log("GLOSSARIO eliminato")
												console.log(data)
												if (data
														.hasOwnProperty("errors")) {
													showErrorToast(data.errors[0].message)
													showToast(
															translate
																	.load("sbi.glossary.delete.error"),
															3000);

												} else {
													var index = wds.indexOf(ev);
													wds.splice(index, 1);
													showToast(
															translate
																	.load("sbi.glossary.delete.success"),
															3000);
													if (ev.GLOSSARY_ID == ctr.selectedGloss.GLOSSARY_ID) {
														ctr.selectedGloss = {};
													}
												}
												hidePreloader();

											})
									.error(
											function(data, status, headers,
													config) {
												console
														.log("WORD NON ELMINIATO "
																+ status);
												showErrorToast("word non eliminato "
														+ status);
												showToast(
														translate
																.load("sbi.glossary.delete.error"),
														3000);

												hidePreloader();
											})

						}, function() {
							console.log('You decided to keep your debt.');
						});
	};

	ctr.createNewGlossary = function(ev, gl) {

		$mdDialog
				.show({
					controllerAs : 'gloCtrl',
					controller : function($mdDialog) {
						var gctl = this;

						if (gl != undefined) {
							// load glossary data
							gctl.headerTitle = translate
									.load("sbi.glossary.modify");
							showPreloader();
							restServices
									.get("glossary", "getGlossary",
											"GLOSSARY_ID=" + gl.GLOSSARY_ID)
									.success(
											function(data, status, headers,
													config) {
												console.log("Words Ottenuti "
														+ status)
												console.log(data)
												if (data
														.hasOwnProperty("errors")) {
													showErrorToast(data.errors[0].message)
													$mdDialog.hide();
													showToast(
															translate
																	.load("sbi.glossary.load.error"),
															3000);
													return false;
												} else {
													gctl.newGloss = data;
												}

												hidePreloader();
											})
									.error(
											function(data, status, headers,
													config) {
												console
														.log("Words non Ottenuti "
																+ status);
												showErrorToast('Ci sono errori! \n status '
														+ status)

												$mdDialog.hide();
												showToast(
														translate
																.load("sbi.glossary.load.error"),
														3000);
												hidePreloader();
											})

						} else {
							gctl.headerTitle = translate
									.load("sbi.glossary.save");
							gctl.newGloss = ctr.newGloss;
						}

						gctl.submit = function() {
							console.log(gctl.newGloss)

							console.log("salvo o modifico")
							if (gctl.newGloss.NEWGLOSS != undefined) {
								console.log("salvo")
								gctl.newGloss.SaveOrUpdate = "Save";
							} else {
								console.log("modificato")
								gctl.newGloss.SaveOrUpdate = "Update";
							}
							showPreloader();
							restServices
									.post("glossary", "addGlossary",
											gctl.newGloss)
									.success(
											function(data, status, headers,
													config) {
												console
														.log("glossary salvato  Ottenuto")
												console.log(data)
												if (data
														.hasOwnProperty("errors")) {

													showErrorToast(data.errors[0].message)
													showToast(
															translate
																	.load("sbi.glossary.save.error"),
															3000);
												} else {

													if (gctl.newGloss.SaveOrUpdate == "Save") {
														// salvataggio riuscito
														delete gctl.newGloss.NEWGLOSS;
														gctl.newGloss.GLOSSARY_ID = data.id;
														console
																.log(gctl.newGloss)
														ctr.glossary
																.push(gctl.newGloss);
														ctr.newGloss = JSON
																.parse(JSON
																		.stringify(EmptyGloss));
														$mdDialog.hide();
														showToast(
																translate
																		.load("sbi.glossary.save.success"),
																3000);
													} else {
														// modifica riuscita
														gl.GLOSSARY_NM = gctl.newGloss.GLOSSARY_NM;
														ctr.newGloss = JSON
																.parse(JSON
																		.stringify(EmptyGloss));
														$mdDialog.hide();
														showToast(
																translate
																		.load("sbi.glossary.modify.success"),
																3000);
													}

												}
												hidePreloader();
											})
									.error(
											function(data, status, headers,
													config) {
												$mdDialog.hide();
												showErrorToast("glossary non salvato "
														+ status);
												hidePreloader();
												showToast(
														translate
																.load("sbi.glossary.save.error"),
														3000);
											})

						};
						gctl.annulla = function($event) {
							$mdDialog.hide();

						};

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
		},
		beforeDrop : function(event) {
			console.log("beforeDropWord")
			console.log(event)

			// if is D&D to newWord chisp link return true
			if (event.dest.nodesScope.$parent.$element[0].id == "chipsTree"
					|| event.dest.nodesScope.$parent.$element[0].id == "wordTree") {
				return true;
			}

			var confirm = $mdDialog.confirm().parent(
					angular.element(document.body)).title(
					translate.load("sbi.glossary.add.word")).content(
					translate.load("sbi.glossary.message.save.database"))
					.ariaLabel('Muovi')
					.ok(translate.load("sbi.attributes.add")).cancel(
							translate.load("sbi.ds.wizard.cancel"))

			$mdDialog
					.show(confirm)
					.then(
							function() {
								console.log("accettato");

								var elem = {};

								elem.PARENT_ID = event.dest.nodesScope.$nodeScope == null ? null
										: event.dest.nodesScope.$nodeScope.$modelValue.CONTENT_ID

								elem.WORD_ID = event.source.nodeScope.$modelValue.WORD_ID;

								showPreloader();
								restServices
										.post("glossary", "addContents", elem)
										.success(
												function(data, status, headers,
														config) {

													if (data
															.hasOwnProperty("errors")) {
														showErrorToast(data.errors[0].message)
														showToast(
																translate
																		.load("sbi.glossary.error.save"),
																3000);
													} else {
														showToast(
																translate
																		.load("sbi.glossary.success.save"),
																3000);

														elem.WORD = event.source.nodeScope.$modelValue.WORD;
														if (elem.PARENT_ID != null) {
															event.dest.nodesScope.$nodeScope.$modelValue.HAVE_WORD_CHILD = true;
														}
														event.dest.nodesScope
																.insertNode(
																		event.dest.index,
																		elem);
													}
													if (elem.PARENT_ID != null) {
														event.dest.nodesScope
																.expand();
													}
													hidePreloader();
												})
										.error(
												function(data, status, headers,
														config) {
													hidePreloader();
													showToast(
															translate
																	.load("sbi.glossary.error.save"),
															3000);
												});

							}, function() {
								console.log("rifiutato");
								hidePreloader();
							});

			// non faccio spostare l'elemento automaticamente
			event.source.nodeScope.$$apply = false;

		},
		dragStart : function(event) {
			ctr.showAddFiglioBox();
		},
		dragStop : function(event) {
			ctr.hideAddFiglioBox();
		}
	};

	ctr.showAddFiglioBox = function() {
		angular.element(document.getElementsByClassName('addFiglioBox')).css(
				"display", "block");
		angular.element(
				document.querySelector('.chipsTree .angular-ui-tree-empty'))
				.css("display", "block");

	}
	ctr.hideAddFiglioBox = function() {
		angular.element(document.getElementsByClassName('addFiglioBox')).css(
				"display", "none");
		var x = angular.element(
				document.querySelector('.chipsTree .angular-ui-tree-empty'))
				.css("display", "none");

	}

	ctr.TreeOptions = {

		accept : function(sourceNodeScope, destNodesScope, destIndex) {
			console.log("accept TreeOptions")
			console.log(destNodesScope)

			// check if is a D&D of word
			if (sourceNodeScope.$modelValue.hasOwnProperty("WORD")) {
				// check if is glossary root
				if (destNodesScope.$parent.$modelValue
						.hasOwnProperty("SBI_GL_CONTENTS")) {
					// if (destNodesScope.$parent.$modelValue.PARENT_ID == null)
					// {
					console.log("D&D not avaible on the glossary root");
					return false;
					// }

				}
				// check if have logical node child
				if (destNodesScope.$parent.$modelValue
						.hasOwnProperty("HAVE_CONTENTS_CHILD")) {
					if (destNodesScope.$parent.$modelValue.HAVE_CONTENTS_CHILD == true) {
						console.log("figli logici presenti, non faccio il d&d")
						return false;
					}
				}
			} else {
				// d&d of logical node
				if (destNodesScope.$parent.$modelValue
						.hasOwnProperty("HAVE_WORD_CHILD")) {
					if (destNodesScope.$parent.$modelValue.HAVE_WORD_CHILD == true) {
						console.log("figli word presenti")
						return false;
					}
				}
			}

			// if (destNodesScope.hasChild()) {
			// var child = destNodesScope.childNodes()
			// for (ch in child) {
			// if (child[ch].$modelValue.CONTENT_CD != undefined) {
			// return false;
			// }
			// }
			// }
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
					translate.load("sbi.glossary.modify.answare")).content(
					translate.load("sbi.glossary.message.save.database"))
					.ariaLabel('Muovi').ok(translate.load("sbi.glossary.move"))
					.cancel(translate.load("sbi.general.cancel"))

			$mdDialog
					.show(confirm)
					.then(
							function() {
								console.log("accettato");
								console.log(event.source.nodeScope.item)

								var elem = event.source.nodeScope.item;

								elem.PARENT_ID = event.dest.nodesScope.$nodeScope == null ? null
										: event.dest.nodesScope.$nodeScope.item.CONTENT_ID

								elem.GLOSSARY_ID = ctr.selectedGloss.GLOSSARY_ID;

								elem.OLD_PARENT_ID = event.source.nodesScope.$nodeScope == null ? null
										: event.source.nodesScope.$nodeScope.$parent.$modelValue[0].CONTENT_ID;

								showPreloader();
								restServices
										.post("glossary",
												"ModifyContentsGlossary", elem)
										.success(
												function(data, status, headers,
														config) {

													if (data
															.hasOwnProperty("errors")) {
														showErrorToast(data.errors[0].message);
														showToast(
																translate
																		.load("sbi.glossary.error.save"),
																3000);
													} else {
														showToast(translate.load("sbi.glossary.success.save"),3000);
														
														if(elem.hasOwnProperty("WORD_ID")){
															//confirm that there is a word and check if destination have other word
															event.dest.nodesScope.$parent.$modelValue.HAVE_WORD_CHILD=true;
//															for(var i=0 i<event.source.nodesScope.$parent.$modelValue.CHILD.length;i++){
//																if
//																
//															}
															
															}else{
																//confirm that there is a cont entand check if destination have other contents
																event.dest.nodeScope.$parent.$modelValue.HAVE_CONTENTS_CHILD=true;	
															}
														event.dest.nodesScope.insertNode(event.dest.index,elem);
														event.source.nodesScope.$modelValue.splice(event.source.index,1);

														
													}
													if (elem.PARENT_ID != null) {
														event.dest.nodesScope.expand();
													}
													hidePreloader();
												})
										.error(
												function(data, status, headers,
														config) {
													showToast(
															translate
																	.load("sbi.glossary.error.save"),
															3000);
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
			ctr.showAddFiglioBox();
		},
		dragStop : function(event) {
			ctr.hideAddFiglioBox();
		}
	};

	ctr.TreeOptionsChips = {
		accept : function(sourceNodeScope, destNodesScope, destIndex) {
			console.log("accept TreeOptionsChips")
			console.log(sourceNodeScope)
			console.log(angular.element(document
					.querySelector('.linkChips .md-chips')))

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
			ctr.showAddFiglioBox();
		},
		dragStop : function(event) {
			ctr.hideAddFiglioBox();
		}

	};

	ctr.remove = function(scope) {
		console.log("remove")
		scope.remove();
	};

	ctr.removeContents = function(ev) {
		console.log("removeContents")
		console.log(ev)
		// ev.remove();

		var confirm = $mdDialog.confirm().title(
				translate.load("sbi.glossary.content.delete")).content(
				translate.load("sbi.glossary.content.delete.message"))
				.ariaLabel('Lucky day')
				.ok(translate.load("sbi.generic.delete")).cancel(
						translate.load("sbi.general.cancel")).targetEvent(ev);
		console.log(confirm)

		var req = "";
		if (ev.$modelValue.hasOwnProperty("CONTENT_ID")) {
			// delete logical node
			req = "CONTENTS_ID=" + ev.$modelValue.CONTENT_ID;
		} else {
			// delete word of content
			req = "PARENT_ID=" + ev.$parentNodeScope.$modelValue.CONTENT_ID
					+ "&WORD_ID=" + ev.$modelValue.WORD_ID;
		}

		$mdDialog
				.show(confirm)
				.then(
						function() {

							restServices
									.remove("glossary", "deleteContents", req)
									.success(
											function(data, status, headers,
													config) {
												console
														.log("Contents eliminato")
												console.log(data)
												if (data
														.hasOwnProperty("errors")) {
													showErrorToast(data.errors[0].message)
													showToast(
															translate
																	.load("sbi.glossary.content.delete.error"),
															3000);
												} else {
													ev.remove();
													showToast(
															translate
																	.load("sbi.glossary.content.delete.success"),
															3000);
												}
												hidePreloader();

											})
									.error(
											function(data, status, headers,
													config) {

												showErrorToast("nodo non eliminato "
														+ status);
												showToast(
														translate
																.load("sbi.glossary.content.delete.error"),
														3000);
												hidePreloader();
											})

						}, function() {
							console.log('You decided to keep your debt.');
						});

	};

	ctr.toggle = function(scope, item, gloss) {
		console.log("toggle")
		scope.preloader = true;
		if (scope.collapsed) {
			ctr.getGlossaryNode(gloss, item, scope)
		} else {
			scope.toggle();
			scope.preloader = false;
		}
	};

	ctr.showClickedGlossary = function(gloss) {

		if (JSON.stringify(EmptyWord) != JSON.stringify(ctr.newWord)) {
			var confirm = $mdDialog
					.confirm()
					.title(translate.load("sbi.glossary.word.modify.progress"))
					.content(
							translate
									.load("sbi.glossary.word.modify.progress.message.showGloss"))
					.ariaLabel('Lucky day').ok(
							translate.load("sbi.general.continue")).cancel(
							translate.load("sbi.general.cancel"));

			console.log
			$mdDialog.show(confirm).then(function() {
				resetForm();
				ctr.selectedGloss = gloss;
				ctr.activeTab = 'Glossari';
				ctr.getGlossaryNode(gloss, null)

			}, function() {
				console.log('You decided to keep your debt.');
			});

		} else {
			ctr.selectedGloss = gloss;
			ctr.activeTab = 'Glossari';
			ctr.getGlossaryNode(gloss, null)
		}

	}

	ctr.newSubItem = function(scope, parent, modCont) {
		$mdDialog
				.show({
					controllerAs : "renCtrl",
					controller : function($mdDialog) {
						var rn = this;

						if (modCont == true) {
							// load content data
							rn.headerTitle = translate
									.load("sbi.glossary.content.modify");
							showPreloader();
							restServices
									.get("glossary", "getContent",
											"CONTENT_ID=" + parent.CONTENT_ID)
									.success(
											function(data, status, headers,
													config) {
												console.log("Content Ottenuti "
														+ status)
												console.log(data)
												if (data
														.hasOwnProperty("errors")) {
													showErrorToast(data.errors[0].message)
													$mdDialog.hide();
													showToast(
															translate
																	.load("sbi.glossary.content.load.error"),
															3000);
												} else {
													rn.tmpNW = data;
												}

												hidePreloader();
											})
									.error(
											function(data, status, headers,
													config) {
												console
														.log("Contents non Ottenuto "
																+ status);
												showErrorToast('Ci sono errori! \n status '
														+ status)
												showToast(
														translate
																.load("sbi.glossary.content.load.error"),
														3000);
												hidePreloader();
											})

						} else {
							rn.headerTitle = translate
									.load("sbi.glossary.content.new");
							rn.tmpNW = JSON.parse(JSON
									.stringify(EmptyLogicalNode));
						}

						rn.salva = function() {

							console.log("salvo o modifico")
							if (rn.tmpNW.NEWCONT != undefined) {
								console.log("salvo")
								rn.tmpNW.SaveOrUpdate = "Save";
								rn.tmpNW.PARENT_ID = parent.CONTENT_ID;
								rn.tmpNW.GLOSSARY_ID = ctr.selectedGloss.GLOSSARY_ID;
							} else {
								console.log("modificato")
								rn.tmpNW.SaveOrUpdate = "Update";
								rn.tmpNW.CONTENT_ID = parent.CONTENT_ID;
							}

							showPreloader();
							restServices
									.post("glossary", "addContents", rn.tmpNW)
									.success(
											function(data, status, headers,
													config) {
												console.log("ok")
												if (data
														.hasOwnProperty("errors")) {
													showErrorToast(data.errors[0].message)
													showToast(
															translate
																	.load("sbi.glossary.error.save"),
															3000);
												} else {

													if (rn.tmpNW.SaveOrUpdate == "Save") {
														showToast(
																translate
																		.load("sbi.glossary.success.save"),
																3000);

														rn.tmpNW.CONTENT_ID = data.id;
														rn.tmpNW.HAVE_CONTENTS_CHILD = false;
														rn.tmpNW.HAVE_WORD_CHILD = false;

														if (parent
																.hasOwnProperty("CHILD")) {
															parent.CHILD
																	.push(rn.tmpNW);
															parent.HAVE_CONTENTS_CHILD = true;
															scope.expand();
														} else {
															parent.SBI_GL_CONTENTS
																	.push(rn.tmpNW);
														}

														rn.tmpNW = JSON
																.parse(JSON
																		.stringify(EmptyLogicalNode));

													} else {
														$mdDialog.hide();
														parent.CONTENT_NM = rn.tmpNW.CONTENT_NM;
														rn.tmpNW = JSON
																.parse(JSON
																		.stringify(EmptyLogicalNode));
														showToast(
																translate
																		.load("sbi.glossary.error.modify"),
																3000);

													}

												}
												hidePreloader();
											})
									.error(
											function(data, status, headers,
													config) {
												console.log("nonok")
												showToast("Errore nel salvataggio del nuovo nodo logico "
														+ status);
												showToast(
														translate
																.load("sbi.glossary.error.save"),
														3000);
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
		var lbw = angular.element(document.querySelector('.wordListBox'))[0].offsetHeight;
		var tbw = angular.element(document.querySelector('.md-toolbar-tools'))[0].offsetHeight;
		var bpw = angular.element(document.querySelector('.box_pagination'))[0].offsetHeight;

		// bpw == 0 ? bpw = 30 : bpw = bpw;
		var nit = parseInt((lbw - tbw - bpw - 23) / 31);
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
						showErrorToast(data.errors[0].message);
						showToast(translate.load("sbi.glossary.load.error"),
								3000);

					} else {
						ctr.words = data;
					}

					hidePreloader();
				}).error(function(data, status, headers, config) {
			console.log("Words non Ottenuti " + status);
			showErrorToast('Ci sono errori! \n status ' + status);
			showToast(translate.load("sbi.glossary.load.error"), 3000);

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
						showErrorToast(data.errors[0].message);
						showToast(translate.load("sbi.glossary.load.error"),
								3000);

					} else {
						ctr.glossary = data;
					}

					hidePreloader();
				}).error(function(data, status, headers, config) {
			console.log("Glossary non Ottenuti " + status);
			showErrorToast('Ci sono errori! \n status ' + status);
			showToast(translate.load("sbi.glossary.load.error"), 3000);

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
			restServices.get("glossary", "listWords", "WORD=" + ele).success(
					function(data, status, headers, config) {
						console.log("WordLike Ottenuti " + status)
						console.log(data)

						if (data.hasOwnProperty("errors")) {
							showErrorToast(data.errors[0].message);
							showToast(
									translate.load("sbi.glossary.load.error"),
									3000);

						} else {
							ctr.words = data;
							ctr.showSearchPreloader = false;
						}

					}).error(function(data, status, headers, config) {
				console.log("WordLike non Ottenuti " + status);
				showToast(translate.load("sbi.glossary.load.error"), 3000);

				ctr.showSearchPreloader = false;
			})

		}, 1000);

	}

	function getWord(ele) {
		console.log("getWord")
		showPreloader();
		restServices
				.get("glossary", "getWord", "WORD_ID=" + ele.WORD_ID)
				.success(
						function(data, status, headers, config) {
							console.log("Word Ottenuto")
							console.log(data)
							if (data.hasOwnProperty("errors")) {
								showErrorToast(data.errors[0].message);
								showToast(translate
										.load("sbi.glossary.load.error"), 3000);
								ctr.newWord = JSON.parse(JSON
										.stringify(EmptyWord));
							} else {
								ctr.newWord = data;
								ctr.newWord.oldWord = ele;

								ctr.activeTab = 'Vocabolo';
							}

							hidePreloader();

						}).error(function(data, status, headers, config) {
					console.log("Words non Ottenuti " + status);
					showErrorToast('Ci sono errori! \n status ' + status);
					showToast(translate.load("sbi.glossary.load.error"), 3000);
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
								showToast(translate
										.load("sbi.glossary.load.error"), 3000);

							} else {

								if (node == null) {
									// add to glossary
									gloss.SBI_GL_CONTENTS = data
								} else {
									// add to child
									node.CHILD = data;
								}

								if (togg != undefined) {
									togg.toggle();
									togg.preloader = false;
								}
							}
						}).error(function(data, status, headers, config) {
					console.log("nodi non ottenuti " + status);
					showToast(translate.load("sbi.glossary.load.error"), 3000);
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
		hidePreloader();
		// $mdToast.show($mdToast.simple().content('Ci sono errori! \n ' + err)
		// .position('top').action('OK').highlightAction(false).hideDelay(
		// timer));
	}

	function showToast(text, time) {
		var timer = time == undefined ? 6000 : time;

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
		console.log(translate.load("sbi.generic.genericError"));
	}
}
