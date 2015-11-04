var app = angular.module('glossaryWordManager', [ 'ngMaterial', 'ui.tree',
		'angularUtils.directives.dirPagination', 'ng-context-menu',
		'sbiModule', 'glossary_tree', 'angular_list' ]);

app.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey').accentPalette(
			'blue-grey');
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

app.controller('Controller', [ "sbiModule_translate", "sbiModule_restServices", "$q", "$scope",
		"$mdDialog", "$filter", "$timeout", "$mdToast", funzione ]);

function funzione(sbiModule_translate, sbiModule_restServices, $q, $scope, $mdDialog, $filter,
		$timeout, $mdToast) {
	ctr = this;
	$scope.translate = sbiModule_translate;
	ctr.showPreloader = false;
	ctr.showSearchPreloader = false;
	ctr.activeTab = 'Glossari';
	ctr.filterSelected = true;
	ctr.translate = sbiModule_translate;
	ctr.propertyList = [];
	ctr.expanderNode; // when create vocable from contex-menu in tree i use
						// this variable to store node object and then expand it
	ctr.newWord = JSON.parse(JSON.stringify(EmptyWord));
	ctr.glossary;
	getAllGloss();
	ctr.newGloss = JSON.parse(JSON.stringify(EmptyGloss));
	ctr.propWord;
	self.searchTextProp = null;
	ctr.selectedGloss = {};
	ctr.state = [];
	ctr.category = [];
	ctr.safeMode = true;
	ctr.pr = function() {
		console.log(",lsdfkdslfkdslòfdsfdsfdsfdsfdsfdsf")
	}
	ctr.words = [];
	// ctr.WordItemPerPage = 5;
	// changeWordItemPP();
	ctr.totalWord = 0;

	// getAllWords();
	ctr.pagination = {
		current : 1
	};
	ctr.pageChanged = function(newPageNumber, itemsPerPage, searchValue) {
		ctr.getResultsPage(newPageNumber, itemsPerPage, searchValue);
	};

	ctr.getResultsPage = function(pageNumber, itemsPerPage, searchValue) {
		var childElem = angular.element((document.querySelector('#word')));
		var childScope = childElem.isolateScope();

		var item = "Page=" + pageNumber + "&ItemPerPage=" + itemsPerPage;
		if (searchValue != undefined && searchValue.trim() != "") {
			item += "&WORD=" + searchValue;
		}
		ctr.showSearchPreloader = true;
		sbiModule_restServices.get("1.0/glossary", "listWords", item).success(
				function(data, status, headers, config) {
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						console.log("word non Ottenuti");
					} else {
						ctr.words = data.item;
						ctr.totalWord = data.itemCount;

					}
					ctr.showSearchPreloader = false;

				}).error(function(data, status, headers, config) {
			console.log("word non Ottenuti " + status);
			ctr.showSearchPreloader = false;
		})

	}

	// ctr.getResultsPage(1);

	ctr.modifyWord = function(word) {
		if (ctr.isEmptyNewWord()) {
			var confirm = $mdDialog
					.confirm()
					.title(sbiModule_translate.load("sbi.glossary.word.modify.progress"))
					.content(
							sbiModule_translate
									.load("sbi.glossary.word.modify.progress.message.modify"))
					.ariaLabel('Lucky day').ok(
							sbiModule_translate.load("sbi.general.continue")).cancel(
							sbiModule_translate.load("sbi.general.cancel"));

			$mdDialog.show(confirm).then(function() {

				resetForm(word)

			}, function() {
				console.log('Annulla');
			});

		} else {
			resetForm(word)
		}

	};

	ctr.isEmptyNewWord = function() {
		var nw = JSON.parse(JSON.stringify(ctr.newWord));
		if (nw.hasOwnProperty("PARENT")) {
			delete nw.PARENT;
		}

		return (JSON.stringify(EmptyWord) != JSON.stringify(nw));
	}

	ctr.createNewWord = function(reset, parent) {
		console.log(window.parent)
		var text;
		if (reset == true) {
			text = {};
			text.title = sbiModule_translate.load("sbi.glossary.word.modify.progress");
			text.content = sbiModule_translate.load("sbi.glossary.word.modify.progress.message.abort");
			text.ok = sbiModule_translate.load("sbi.general.yes");
			text.cancel = sbiModule_translate.load("sbi.general.No");

		} else {
			text = {};
			text.title = sbiModule_translate.load("sbi.glossary.word.modify.progress");
			text.content = sbiModule_translate.load("sbi.glossary.word.modify.progress.message.new");
			text.ok = sbiModule_translate.load("sbi.general.yes");
			text.cancel = sbiModule_translate.load("sbi.general.No");
		}

		if (ctr.isEmptyNewWord()) {
			var confirm = $mdDialog.confirm().title(text.title).content(
					text.content).ariaLabel('Lucky day').ok(text.ok).cancel(
					text.cancel);

			$mdDialog.show(confirm).then(function() {

				resetForm();
				if (reset == true) {
					ctr.activeTab = 'Glossari';
				} else {
					ctr.activeTab = 'Vocabolo';
				}

			}, function() {
				console.log('Annulla');
			});

		} else {
			resetForm();
			if (reset == true) {
				ctr.activeTab = 'Glossari';
			} else {
				ctr.activeTab = 'Vocabolo';
			}
		}

		if (parent != undefined) {
			ctr.newWord.PARENT = parent.$parent.$modelValue;
			ctr.expanderNode = parent.$parent;
		}

	}

	function resetForm(ele) {
		angular
				.element(
						document
								.querySelector('.md-chip-input-container .md-whiteframe-z1 input'))
				.val("");
		angular
				.element(
						document
								.querySelector('.wordForm .textareaInputBox textarea#formulaText'))
				.val("");
		angular
				.element(
						document
								.querySelector('.wordForm .textareaInputBox textarea#descrText'))
				.val("");

		ctr.tmpAttr = {};

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

	ctr.loadProperty = function() {

		sbiModule_restServices.get("1.0/udp", "loadUdp", "FAMILY=Glossary").success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						console.log("attributeLike non Ottenuti");
					} else {
						
						for (var i = 0; i < data.length; i++) {
							if (JSON.stringify(ctr.newWord.SBI_GL_WORD_ATTR)
									.toString().toLowerCase().indexOf(
											data[i].ATTRIBUTE_NM.toString()
													.toLowerCase()) != -1) {
								data.splice(i, 1);
								i--;
							}
						}
							ctr.propertyList = data;
						
					}
					hidePreloader();

				}).error(function(data, status, headers, config) {
			console.log("AttributeLike non Ottenuti " + status);
		})

	}

	ctr.prevPropSearch = "";
	ctr.querySearchProp = function(query) {

		if (ctr.prevPropSearch == query) {
			ctr.prevPropSearch = "";
			return false;
		}
		ctr.prevPropSearch = query;

		var def = $q.defer();

		sbiModule_restServices.get("1.0/udp", "loadUdp",
				"LABEL=" + query + "&FAMILY=Glossary").success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						console.log("attributeLike non Ottenuti");
					} else {
						def.resolve(data);
					}
					hidePreloader();

				}).error(function(data, status, headers, config) {
			console.log("AttributeLike non Ottenuti " + status);
		})

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
					console.log("promise non ok")

				})

	}

	ctr.addProp = function(prop) {
		var np = {};
		np.ATTRIBUTE_NM = prop.Prop.ATTRIBUTE_NM;
		np.ATTRIBUTE_ID = prop.Prop.ATTRIBUTE_ID;
		np.VALUE = prop.Val;

		ctr.newWord.SBI_GL_WORD_ATTR.push(np)
		ctr.tmpAttr = {};
		ctr.prevPropSearch = "";
	};

	ctr.removeProp = function(prop) {
		ctr.newWord.SBI_GL_WORD_ATTR.splice(ctr.newWord.SBI_GL_WORD_ATTR
				.indexOf(prop), 1)
	}

	ctr.propPresent = function(query) {
		if (query == null || query == undefined || angular.equals({}, query)) {
			return false;
		}

		var results;
		if (typeof query !== 'object') {
			results = $filter('filter')(ctr.propWord, {
				ATTRIBUTE_NM : query.toUpperCase()
			}, true);
		} else {
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
				return;
			}

			ctr.prevChipsSearch = chip;

			sbiModule_restServices.get("1.0/glossary", "listWords", "WORD=" + chip)
					.success(function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
							console.log("Words non Ottenuti ");
						} else {
							def.resolve(data);
						}
						hidePreloader();
					}).error(function(data, status, headers, config) {
						console.log("Words non Ottenuti " + status);
					})
		}, 1000);

		ctr.prevChipsSearch = "-1-";

		return def.promise
				.then(
						function(val) {

							for (var i = 0; i < val.length; i++) {
								if (JSON.stringify(ctr.newWord.LINK).toString()
										.toLowerCase().indexOf(
												val[i].WORD.toString()
														.toLowerCase()) != -1
										|| val[i].WORD.toString().toLowerCase() == ctr.newWord.WORD
												.toString().toLowerCase()) {
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
			return found[0];
		} else {
			console.log("not found ds");
		}

	};

	ctr.addWord = function(product) {
		if (ctr.newWord.NEWWORD != undefined) {
			ctr.newWord.SaveOrUpdate = "Save";
		} else {
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
		sbiModule_restServices
				.post("1.0/glossary/business", "addWord", ctr.newWord)
				.success(
						function(data, status, headers, config) {
							if (data.hasOwnProperty("errors")) {
								showToast(data.errors[0].message)
							} else if (data.Status == "NON OK") {
								showToast(sbiModule_translate.load(data.Message), 3000);
								if (data.hasOwnProperty("Error_text")) {
									console.log(data.Error_text);
								}
							} else {

								if (ctr.newWord.SaveOrUpdate == "Save") {
									ctr.newWord.WORD_ID = data.id;
									delete ctr.newWord.NEWWORD;
									product.push(ctr.newWord);
								} else {
									ctr.modyfyWordInSelectedGloss(
											ctr.selectedGloss.SBI_GL_CONTENTS,
											"" + ctr.newWord.oldWord.WORD, ""
													+ ctr.newWord.WORD,
											"modify")
									ctr.newWord.oldWord.WORD = ctr.newWord.WORD;
								}

								if (ctr.newWord.hasOwnProperty("PARENT")) {
									var par = ctr.newWord.PARENT;
									delete ctr.newWord.PARENT;
									var elem = {};
									elem.PARENT_ID = par.CONTENT_ID;
									elem.WORD_ID = ctr.newWord.WORD_ID;
									elem.GLOSSARY_ID = ctr.selectedGloss.GLOSSARY_ID;

									showPreloader();
									sbiModule_restServices
											.post("1.0/glossary/business",
													"addContents", elem)
											.success(
													function(data) {

														if (data
																.hasOwnProperty("errors")) {
															showErrorToast(data.errors[0].message)
															showToast(
																	sbiModule_translate
																			.load("sbi.glossary.error.save"),
																	3000);
														} else if (data.Status == "NON OK") {
															showToast(
																	sbiModule_translate
																			.load(data.Message),
																	3000);
														} else {
															// showToast(sbiModule_translate.load("sbi.glossary.success.save"),3000);

															par.HAVE_WORD_CHILD = true;

															par.CHILD
																	.push(ctr.newWord);
															// ctr.newWord.PARENT.HAVE_WORD_CHILD
															// = true;
															// ctr.newWord.PARENT.CHILD.push(ctr.newWord);

														}

														if (ctr.expanderNode != undefined) {
															ctr
																	.getGlossaryNode(
																			ctr.selectedGloss,
																			par,
																			ctr.expanderNode);
															ctr.expanderNode = undefined;
														}

														ctr.newWord = JSON
																.parse(JSON
																		.stringify(EmptyWord));
														showToast(
																sbiModule_translate
																		.load("sbi.glossary.word.save.success"),
																3000);

														hidePreloader();

													})
											.error(
													function(data, status,
															headers, config) {
														hidePreloader();
														showToast(
																sbiModule_translate
																		.load("sbi.glossary.error.save"),
																3000);
														ctr.newWord = JSON
																.parse(JSON
																		.stringify(EmptyWord));

													});

									ctr.activeTab = 'Glossari';

								} else {
									ctr.newWord = JSON.parse(JSON
											.stringify(EmptyWord));
									showToast(
											sbiModule_translate
													.load("sbi.glossary.word.save.success"),
											3000);
									ctr.activeTab = 'Glossari';
								}

							}
							hidePreloader();
						})
				.error(
						function(data, status, headers, config) {
							showErrorToast("word non salvato " + status)
							showToast(sbiModule_translate
									.load("sbi.glossary.word.save.error"), 3000);
							hidePreloader();
						})

	};

	ctr.modyfyWordInSelectedGloss = function(arrList, oldName, newName, type) {
		if (arrList == undefined) {
			return;
		}
		for (var i = 0; i < arrList.length; i++) {
			if (arrList[i].hasOwnProperty("WORD") && arrList[i].WORD == oldName) {
				if (type == "modify") {
					arrList[i].WORD = newName;
					break;
					// i break because if is a word , is unique in the list e
					// dont'have child.
				} else if (type == "delete") {
					arrList.splice(i, 1);
					break;
					// i break because if is a word , is unique in the list e
					// dont'have child.
				}
			}
			if (arrList[i].hasOwnProperty("CHILD")) {
				ctr.modyfyWordInSelectedGloss(arrList[i].CHILD, oldName,
						newName, type);
			}
		}
	};

	// ctr.findWordInSelectedGloss=function(arrList,wordId){
	// if(arrList==undefined){
	// return false;
	// }
	// for(var i=0;i<arrList.length;i++){
	// if(arrList[i].hasOwnProperty("WORD_ID") && arrList[i].WORD_ID==wordId ){
	// return true;
	// break;
	// }
	//			
	// if(arrList[i].hasOwnProperty("CHILD") ){
	// if(ctr.findWordInSelectedGloss(arrList[i].CHILD,wordId)){
	// return true;
	// }
	// }
	// }
	// return false;
	//			
	// };
	//	
	ctr.deleteWord = function(ev) {
		// Appending dialog to document.body to cover
		// sidenav in docs app
		var confirm = $mdDialog.confirm().title(
				sbiModule_translate.load("sbi.glossary.word.delete")).content(
				sbiModule_translate.load("sbi.glossary.word.delete.message")).ariaLabel(
				'Lucky day').ok(sbiModule_translate.load("sbi.generic.delete")).cancel(
				sbiModule_translate.load("sbi.myanalysis.delete.cancel")).targetEvent(ev);

		var wds = ctr.words;
		var nw = ctr.newWord;
		$mdDialog
				.show(confirm)
				.then(
						function() {
							showPreloader();
							sbiModule_restServices
									.remove("1.0/glossary/business",
											"deleteWord",
											"WORD_ID=" + ev.WORD_ID)
									.success(
											function(data, status, headers,
													config) {

												if (data
														.hasOwnProperty("errors")) {
													showErrorToast(data.errors[0].message)
													showToast(
															sbiModule_translate
																	.load("sbi.glossary.word.delete.error"),
															3000);

												} else {

													ctr
															.modyfyWordInSelectedGloss(
																	ctr.selectedGloss.SBI_GL_CONTENTS,
																	""
																			+ ev.WORD,
																	"",
																	"delete");
													var index = wds.indexOf(ev);
													wds.splice(index, 1);
													showToast(
															sbiModule_translate
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
														sbiModule_translate
																.load("sbi.glossary.word.delete.error"),
														3000);
												hidePreloader();
											})

							// controllo se l'elemento
							// che si vule eliminare è
							// in fase di modifica
							if (nw.WORD_ID === ev.WORD_ID) {
								nw.NEWWORD = true;
							}

						}, function() {
							console.log('annulla');
						});
	};

	// glossary

	ctr.CloneGloss = function(ev, gl) {

		$mdDialog
				.show({
					controllerAs : 'gloCtrl',
					scope : $scope,
					preserveScope : true,
					controller : function($mdDialog) {
						var gctl = this;

						if (gl != undefined) {
							// load glossary data
							gctl.headerTitle = sbiModule_translate
									.load("sbi.glossary.clone");

							showPreloader();
							sbiModule_restServices
									.get("1.0/glossary", "getGlossary",
											"GLOSSARY_ID=" + gl.GLOSSARY_ID)
									.success(
											function(data, status, headers,
													config) {

												if (data
														.hasOwnProperty("errors")) {
													showErrorToast(data.errors[0].message);
													$mdDialog.hide();
													showToast(
															sbiModule_translate
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

												showErrorToast('Ci sono errori! \n status '
														+ status);
												hidePreloader();
												sbiModule_translate
														.load("sbi.glossary.load.error");
											})

						} else {
							showErrorToast("Errore! glossario non puo essere nullo")
							$mdDialog.hide();
							showToast(
									sbiModule_translate.load("sbi.glossary.load.error"),
									3000);
							return false;
						}

						gctl.annulla = function($event) {
							$mdDialog.hide();

						};

						gctl.submit = function() {

							showPreloader();
							sbiModule_restServices
									.post("1.0/glossary/business",
											"cloneGlossary", gctl.newGloss)
									.success(
											function(data, status, headers,
													config) {

												if (data
														.hasOwnProperty("errors")) {
													showErrorToast(data.errors[0].message);

													showToast(
															sbiModule_translate
																	.load("sbi.glossary.clone.error"),
															3000);

												} else {
													showToast(
															sbiModule_translate
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
														sbiModule_translate
																.load("sbi.glossary.clone.error"),
														3000);
											});

						}

					},
					templateUrl : '/athena/js/src/angular_1.4/tools/glossary/commons/templates/dialog-new-glossary.html',
					targetEvent : ev,
				})

	}

	ctr.deleteGlossary = function(ev) {
		// Appending dialog to document.body to cover
		// sidenav in docs app
		var confirm = $mdDialog.confirm().title(
				sbiModule_translate.load("sbi.glossary.delete")).content(
				sbiModule_translate.load("sbi.glossary.delete.message")).ariaLabel(
				'Lucky day').ok(sbiModule_translate.load("sbi.generic.delete")).cancel(
				sbiModule_translate.load("sbi.ds.wizard.cancel")).targetEvent(ev);

		var wds = ctr.glossary;
		$mdDialog
				.show(confirm)
				.then(
						function() {
							showPreloader();
							sbiModule_restServices
									.remove("1.0/glossary/business",
											"deleteGlossary",
											"GLOSSARY_ID=" + ev.GLOSSARY_ID)
									.success(
											function(data, status, headers,
													config) {

												if (data
														.hasOwnProperty("errors")) {
													showErrorToast(data.errors[0].message)
													showToast(
															sbiModule_translate
																	.load("sbi.glossary.delete.error"),
															3000);

												} else {
													var index = wds.indexOf(ev);
													wds.splice(index, 1);
													showToast(
															sbiModule_translate
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
														sbiModule_translate
																.load("sbi.glossary.delete.error"),
														3000);

												hidePreloader();
											})

						}, function() {
							console.log('Annulla');
						});
	};

	ctr.createNewGlossary = function(ev, gl) {
		$mdDialog
				.show({
					scope : $scope,
					preserveScope : true,
					controllerAs : 'gloCtrl',
					controller : function($mdDialog) {
						var gctl = this;
						if (gl != undefined) {
							// load glossary data
							gctl.headerTitle = sbiModule_translate
									.load("sbi.glossary.modify");
							showPreloader();
							sbiModule_restServices
									.get("1.0/glossary", "getGlossary",
											"GLOSSARY_ID=" + gl.GLOSSARY_ID)
									.success(
											function(data, status, headers,
													config) {

												if (data
														.hasOwnProperty("errors")) {
													showErrorToast(data.errors[0].message)
													$mdDialog.hide();
													showToast(
															sbiModule_translate
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
														sbiModule_translate
																.load("sbi.glossary.load.error"),
														3000);
												hidePreloader();
											})

						} else {
							gctl.headerTitle = sbiModule_translate
									.load("sbi.glossary.save");
							gctl.newGloss = ctr.newGloss;
						}

						gctl.submit = function() {

							if (gctl.newGloss.NEWGLOSS != undefined) {

								gctl.newGloss.SaveOrUpdate = "Save";
							} else {

								gctl.newGloss.SaveOrUpdate = "Update";
							}
							showPreloader();
							sbiModule_restServices
									.post("1.0/glossary/business",
											"addGlossary", gctl.newGloss)
									.success(
											function(data, status, headers,
													config) {

												if (data
														.hasOwnProperty("errors")) {

													showErrorToast(data.errors[0].message)
													showToast(
															sbiModule_translate
																	.load("sbi.glossary.save.error"),
															3000);
												} else if (data.Status == "NON OK") {
													showToast(
															sbiModule_translate
																	.load(data.Message),
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
																sbiModule_translate
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
																sbiModule_translate
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
														sbiModule_translate
																.load("sbi.glossary.save.error"),
														3000);
											})

						};
						gctl.annulla = function($event) {
							$mdDialog.hide();
							ctr.newGloss = JSON.parse(JSON
									.stringify(EmptyGloss));

						};

					},

					// "/athena/js/dialog-new-glossary.html"
					templateUrl : '/athena/js/src/angular_1.4/tools/glossary/commons/templates/dialog-new-glossary.html',
					targetEvent : ev,
				})

	};
	// <!-- tree -->


	ctr.TreeOptionsWord = {

		accept : function(sourceNodeScope, destNodesScope, destIndex) {
			return false;
		},
		beforeDrop : function(event) {
			// if is D&D to newWord chisp link return true
			if (event.dest.nodesScope.$parent.$element[0].id == "chipsTree"
					|| event.dest.nodesScope.$parent.$element[0].id == "wordTree") {
				return true;
			}

			var confirm = $mdDialog.confirm().parent(
					angular.element(document.body)).title(
					sbiModule_translate.load("sbi.glossary.add.word")).content(
					sbiModule_translate.load("sbi.glossary.message.save.database"))
					.ariaLabel('Muovi')
					.ok(sbiModule_translate.load("sbi.attributes.add")).cancel(
							sbiModule_translate.load("sbi.ds.wizard.cancel"));

			var addContentFunc = function() {

				var elem = {};
				elem.PARENT_ID = event.dest.nodesScope.$nodeScope == null ? null
						: event.dest.nodesScope.$nodeScope.$modelValue.CONTENT_ID

				elem.WORD_ID = event.source.nodeScope.$modelValue.WORD_ID;
				elem.GLOSSARY_ID = ctr.selectedGloss.GLOSSARY_ID;

				showPreloader();
				sbiModule_restServices
						.post("1.0/glossary/business", "addContents", elem)
						.success(
								function(data, status, headers, config) {

									if (data.hasOwnProperty("errors")) {
										showErrorToast(data.errors[0].message)
										showToast(
												sbiModule_translate
														.load("sbi.glossary.error.save"),
												3000);
									} else if (data.Status == "NON OK") {
										showToast(sbiModule_translate.load(data.Message),
												3000);
									} else {
										// showToast(sbiModule_translate.load("sbi.glossary.success.save"),3000);

										elem.WORD = event.source.nodeScope.$modelValue.WORD;
										if (elem.PARENT_ID != null) {
											event.dest.nodesScope.$nodeScope.$modelValue.HAVE_WORD_CHILD = true;
										}
										event.dest.nodesScope.insertNode(
												event.dest.index, elem);
									}

									if (elem.PARENT_ID != null) {

										event.dest.nodesScope.expand();

										ctr
												.getGlossaryNode(
														ctr.selectedGloss,
														event.dest.nodesScope.$parent.$modelValue);

									}
									hidePreloader();
								}).error(
								function(data, status, headers, config) {
									hidePreloader();
									showToast(sbiModule_translate
											.load("sbi.glossary.error.save"),
											3000);
								});

			}

			if (!ctr.safeMode) {
				addContentFunc();
			} else {
				$mdDialog.show(confirm).then(function() {
					addContentFunc();
				}, function() {
					console.log("rifiutato");
					hidePreloader();
				});
			}

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
			var isRootD = false;
			if (destNodesScope.$parent.$type == "uiTree") {
				isRootD = true;
			}

			// check if is a D&D of word
			if (sourceNodeScope.$modelValue.hasOwnProperty("WORD")) {

				if (isRootD) {
					// root
					console.log("D&D not avaible on the glossary root");
					return false;
				}
				// check if have logical node child
				if (destNodesScope.$parent.$modelValue
						.hasOwnProperty("HAVE_CONTENTS_CHILD")) {
					if (destNodesScope.$parent.$modelValue.HAVE_CONTENTS_CHILD == true) {
						console.log("figli logici presenti, non faccio il d&d")
						return false;
					}
				}

				// //controllo che il glossario non contenghi gia lo stesso
				// vocabolo
				// console.log(ctr.selectedGloss)
				// if(ctr.findWordInSelectedGloss(ctr.selectedGloss.SBI_GL_CONTENTS,sourceNodeScope.$modelValue.WORD_ID)){
				// console.log("Word presente neol glossario")
				// return false;
				//					
				// }

				// controllo che già non ci sia uno stesso elemento come figlio
				// del padre
				for (var i = 0; i < destNodesScope.$parent.$modelValue.CHILD.length; i++) {

					if (sourceNodeScope.$parentNodeScope != null
							&& sourceNodeScope.$parentNodeScope.$modelValue
									.hasOwnProperty("$$hashKey")) {
						// D&D from tree
						if (destNodesScope.$parent.$modelValue.CHILD[i].WORD_ID == sourceNodeScope.$modelValue.WORD_ID
								&& sourceNodeScope.$parentNodeScope.$modelValue.$$hashKey != destNodesScope.$parent.$modelValue.$$hashKey) {
							console.log("word from tree già presente")
							return false;
						}
					} else {
						// D&D from word list
						if (destNodesScope.$parent.$modelValue.CHILD[i].WORD_ID == sourceNodeScope.$modelValue.WORD_ID) {
							console.log("word from list già presente")
							return false;
						}
					}

				}

			} else {

				// if (destNodesScope.$parent.$modelValue
				// .hasOwnProperty("SBI_GL_CONTENTS")) {
				// // root
				// return true;
				// }
				if (isRootD) {
					// root
					return true;
				}

				// d&d of logical node
				if (destNodesScope.$parent.$modelValue
						.hasOwnProperty("HAVE_WORD_CHILD")) {
					if (destNodesScope.$parent.$modelValue.HAVE_WORD_CHILD == true) {
						console.log("figli word presenti")
						return false;
					}
				}

				for (var i = 0; i < destNodesScope.$parent.$modelValue.CHILD.length; i++) {
					if (destNodesScope.$parent.$modelValue.CHILD[i].CONTENT_NM == sourceNodeScope.$modelValue.CONTENT_NM
							&& sourceNodeScope.$parentNodeScope != null
							&& sourceNodeScope.$parentNodeScope.$modelValue.$$hashKey != destNodesScope.$parent.$modelValue.$$hashKey) {
						console.log("content già presente")
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
			var n1 = event.dest.nodesScope.$nodeScope == null ? null
					: event.dest.nodesScope.$nodeScope.$id;
			var n2 = event.source.nodesScope.$nodeScope == null ? null
					: event.source.nodesScope.$nodeScope.$id;

			if (n1 == n2) {
				event.source.nodeScope.$$apply = false;
				return;
			}

			var confirm = $mdDialog.confirm().parent(
					angular.element(document.body)).title(
					sbiModule_translate.load("sbi.glossary.modify.answare")).content(
					sbiModule_translate.load("sbi.glossary.message.save.database"))
					.ariaLabel('Muovi').ok(sbiModule_translate.load("sbi.glossary.move"))
					.cancel(sbiModule_translate.load("sbi.general.cancel"));

			var moveItem = function() {
				var isRootS = false;
				var isRootD = false;
				if (event.source.nodesScope.$parent.$type == "uiTree") {
					isRootS = true;
				}

				if (event.dest.nodesScope.$parent.$type == "uiTree") {
					isRootD = true;
				}

				var elem = event.source.nodeScope.item;

				elem.PARENT_ID = isRootD ? null
						: event.dest.nodesScope.$nodeScope.item.CONTENT_ID
				elem.OLD_PARENT_ID = isRootS ? null
						: event.source.nodesScope.$parent.$modelValue.CONTENT_ID;
				elem.GLOSSARY_ID = ctr.selectedGloss.GLOSSARY_ID;

				showPreloader();
				sbiModule_restServices
						.post("1.0/glossary/business",
								"modifyContentsGlossary", elem)
						.success(
								function(data, status, headers, config) {
									if (data.hasOwnProperty("errors")) {
										showErrorToast(data.errors[0].message);
										showToast(
												sbiModule_translate
														.load("sbi.glossary.error.save"),
												3000);
										event.source.nodesScope.insertNode(
												event.dest.index,
												event.source.nodeScope.item);
										event.dest.nodesScope.$modelValue
												.splice(event.dest.index, 1);
									} else if (data.Status == "NON OK") {
										showToast(sbiModule_translate.load(data.Message),
												3000);
										event.source.nodesScope.insertNode(
												event.dest.index,
												event.source.nodeScope.item);
										event.dest.nodesScope.$modelValue
												.splice(event.dest.index, 1);
									} else {

										// showToast(sbiModule_translate.load("sbi.glossary.success.save"),3000);

										if (elem.hasOwnProperty("WORD_ID")) {
											// confirm that there is a word and
											// check if destination have other
											// word
											if (!isRootD) {
												event.dest.nodesScope.$parent.$modelValue.HAVE_WORD_CHILD = true;
											}
											// equal to one because the element
											// actual is no dragged

											if (!isRootS) {
												if (!event.source.nodesScope.$parent.$modelValue
														.hasOwnProperty("CHILD")) {
													event.source.nodesScope.$parent.$modelValue.CHILD = {};
												}
												if (event.source.nodesScope.$parent.$modelValue.CHILD.length == 1) {
													event.source.nodesScope.$parent.$modelValue.HAVE_WORD_CHILD = false;
												}
											}
										} else {
											// confirm that there is a cont
											// entand check if destination have
											// other contents
											if (!isRootD) {
												event.dest.nodesScope.$parent.$modelValue.HAVE_CONTENTS_CHILD = true;
											}
											if (!isRootS) {
												if (!event.source.nodesScope.$parent.$modelValue
														.hasOwnProperty("CHILD")) {
													event.source.nodesScope.$parent.$modelValue.CHILD = {};
												}
												if (event.source.nodesScope.$parent.$modelValue.CHILD.length == 1) {
													event.source.nodesScope.$parent.$modelValue.HAVE_CONTENTS_CHILD = false;
												}
											}
										}

										// event.dest.nodesScope.insertNode(event.dest.index,elem);
										// event.source.nodesScope.$modelValue.splice(event.source.index,1);

										if (!isRootD) {
											event.dest.nodesScope.expand();
											ctr
													.getGlossaryNode(
															ctr.selectedGloss,
															event.dest.nodesScope.$parent.$modelValue,
															event.dest.nodesScope.$parent);
										} else {
											ctr.selectedGloss.SBI_GL_CONTENTS
													.sort(function(a, b) {
														return (a.CONTENT_NM > b.CONTENT_NM) ? 1
																: ((b.CONTENT_NM > a.CONTENT_NM) ? -1
																		: 0);
													});

										}

									}

									hidePreloader();
								}).error(
								function(data, status, headers, config) {
									showToast(sbiModule_translate
											.load("sbi.glossary.error.save"),
											3000);
									event.source.nodesScope.insertNode(
											event.dest.index,
											event.source.nodeScope.item);
									event.dest.nodesScope.$modelValue.splice(
											event.dest.index, 1);
									hidePreloader();
								});

			};
			if (!ctr.safeMode) {
				moveItem();
			} else {
				$mdDialog.show(confirm).then(
						function() {
							moveItem();
						},
						function() {
							console.log("rifiutato");

							event.source.nodesScope.insertNode(
									event.dest.index,
									event.source.nodeScope.item);
							event.dest.nodesScope.$modelValue.splice(
									event.dest.index, 1);

							hidePreloader();
						});

			}

			// non faccio spostare l'elemento automaticamente
			// event.source.nodeScope.$$apply = false;

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
			if (sourceNodeScope.$modelValue.WORD_ID == ctr.newWord.WORD_ID) {
				angular.element(document.querySelector('.linkChips .md-chips'))
						.css("box-shadow", "0 2px rgb(255, 0, 0)");
				$timeout(function() {
					angular.element(
							document.querySelector('.linkChips .md-chips'))
							.css("box-shadow", "");
				}, 500);
				return false;
			}

			var found = $filter('filter')(ctr.newWord.LINK, {
				WORD_ID : sourceNodeScope.$modelValue.WORD_ID
			}, true);

			if (found != 0) {
				angular.element(document.querySelector('.linkChips .md-chips'))
						.css("box-shadow", "0 2px rgb(255, 0, 0)");
				$timeout(function() {
					angular.element(
							document.querySelector('.linkChips .md-chips'))
							.css("box-shadow", "");
				}, 500);

				return false;
			}
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
		scope.remove();
	};

	ctr.removeContents = function(ev) {

		console.log(ev)

		var confirm = $mdDialog.confirm().title(
				sbiModule_translate.load("sbi.glossary.content.delete")).content(
				sbiModule_translate.load("sbi.glossary.content.delete.message"))
				.ariaLabel('Lucky day')
				.ok(sbiModule_translate.load("sbi.generic.delete")).cancel(
						sbiModule_translate.load("sbi.general.cancel")).targetEvent(ev);

		var req = "";
		if (ev.$modelValue.hasOwnProperty("CONTENT_ID")) {
			// delete logical node
			req = "CONTENTS_ID=" + ev.$modelValue.CONTENT_ID;
		} else {
			// delete word of content
			req = "PARENT_ID=" + ev.$parentNodeScope.$modelValue.CONTENT_ID
					+ "&WORD_ID=" + ev.$modelValue.WORD_ID;
		}

		var deleteAction = function() {
			sbiModule_restServices
					.remove("1.0/glossary/business", "deleteContents", req)
					.success(
							function(data, status, headers, config) {

								if (data.hasOwnProperty("errors")) {
									showErrorToast(data.errors[0].message)
									showToast(
											sbiModule_translate
													.load("sbi.glossary.content.delete.error"),
											3000);
								} else {
									if (ev.$parentNodeScope.$modelValue.CHILD.length == 1) {
										console.log("ci sono figli")
										if (ev.$modelValue
												.hasOwnProperty("WORD_ID")) {
											ev.$parentNodeScope.$modelValue.HAVE_WORD_CHILD = false;
										} else {
											ev.$parentNodeScope.$modelValue.HAVE_CONTENTS_CHILD = false;
										}
									}
									console
											.log(ev.$parentNodeScope.$modelValue)
									ev.remove();
									showToast(
											sbiModule_translate
													.load("sbi.glossary.content.delete.success"),
											3000);
								}
								hidePreloader();

							})
					.error(
							function(data, status, headers, config) {

								showErrorToast("nodo non eliminato " + status);
								showToast(
										sbiModule_translate
												.load("sbi.glossary.content.delete.error"),
										3000);
								hidePreloader();
							})
		}

		if (!ctr.safeMode) {
			deleteAction();
		} else {
			$mdDialog.show(confirm).then(function() {
				deleteAction();
			}, function() {
				console.log('annulla');
			});
		}

	};

	ctr.toggle = function(scope, item, gloss) {
		item.preloader = true;
		if (scope.collapsed) {
			ctr.getGlossaryNode(gloss, item, scope)
		} else {
			scope.toggle();
			item.preloader = false;
		}
	};

	ctr.showClickedGlossary = function(gloss) {

		if (ctr.isEmptyNewWord()) {
			var confirm = $mdDialog
					.confirm()
					.title(sbiModule_translate.load("sbi.glossary.word.modify.progress"))
					.content(
							sbiModule_translate
									.load("sbi.glossary.word.modify.progress.message.showGloss"))
					.ariaLabel('Lucky day').ok(
							sbiModule_translate.load("sbi.general.continue")).cancel(
							sbiModule_translate.load("sbi.general.cancel"));

			$mdDialog.show(confirm).then(function() {
				resetForm();
				ctr.selectedGloss = gloss;
				ctr.activeTab = 'Glossari';
				ctr.getGlossaryNode(gloss, null)

			}, function() {
				console.log('Annulla');
			});

		} else {
			ctr.selectedGloss = gloss;
			ctr.activeTab = 'Glossari';
			ctr.getGlossaryNode(gloss, null);
		}

	}

	ctr.newSubItem = function(scope, parent, modCont) {
		console.log(parent)
		$mdDialog
				.show({
					controllerAs : "renCtrl",
					scope : $scope,
					preserveScope : true,
					controller : function($mdDialog) {
						var rn = this;

						if (modCont == true) {
							// load content data
							rn.headerTitle = sbiModule_translate
									.load("sbi.glossary.content.modify");
							showPreloader();
							sbiModule_restServices
									.get("1.0/glossary", "getContent",
											"CONTENT_ID=" + parent.CONTENT_ID)
									.success(
											function(data, status, headers,
													config) {

												if (data
														.hasOwnProperty("errors")) {
													showErrorToast(data.errors[0].message)
													$mdDialog.hide();
													showToast(
															sbiModule_translate
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
														sbiModule_translate
																.load("sbi.glossary.content.load.error"),
														3000);
												hidePreloader();
											})

						} else {
							rn.headerTitle = sbiModule_translate
									.load("sbi.glossary.content.new");
							rn.tmpNW = JSON.parse(JSON
									.stringify(EmptyLogicalNode));
						}

						rn.salva = function() {

							if (rn.tmpNW.NEWCONT != undefined) {
								rn.tmpNW.SaveOrUpdate = "Save";
								rn.tmpNW.PARENT_ID = parent.CONTENT_ID;
								rn.tmpNW.GLOSSARY_ID = ctr.selectedGloss.GLOSSARY_ID;
							} else {
								rn.tmpNW.SaveOrUpdate = "Update";
								rn.tmpNW.CONTENT_ID = parent.CONTENT_ID;
							}

							showPreloader();
							sbiModule_restServices
									.post("1.0/glossary/business",
											"addContents", rn.tmpNW)
									.success(
											function(data, status, headers,
													config) {
												if (data
														.hasOwnProperty("errors")) {
													showErrorToast(data.errors[0].message)
													showToast(
															sbiModule_translate
																	.load("sbi.glossary.error.save"),
															3000);
												} else if (data.Status == "NON OK") {
													showToast(
															sbiModule_translate
																	.load(data.Message),
															3000);
												} else {

													if (rn.tmpNW.SaveOrUpdate == "Save") {
														showToast(
																sbiModule_translate
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
															parent.preloader = true;
															ctr
																	.getGlossaryNode(
																			ctr.selectedGloss,
																			parent,
																			scope);
															// scope.expand();

														} else {
															parent.SBI_GL_CONTENTS
																	.push(rn.tmpNW);
														}

														rn.tmpNW = JSON
																.parse(JSON
																		.stringify(EmptyLogicalNode));
														$mdDialog.hide();
													} else {
														$mdDialog.hide();
														parent.CONTENT_NM = rn.tmpNW.CONTENT_NM;
														rn.tmpNW = JSON
																.parse(JSON
																		.stringify(EmptyLogicalNode));
														// showToast(sbiModule_translate.load("sbi.glossary.success.modify"),3000);

													}

												}
												hidePreloader();
											})
									.error(
											function(data, status, headers,
													config) {
												showToast("Errore nel salvataggio del nuovo nodo logico "
														+ status);
												showToast(
														sbiModule_translate
																.load("sbi.glossary.error.save"),
														3000);
												hidePreloader();
											});

							// $mdDialog.hide();
						};
						rn.annulla = function() {
							console.log("annulla");
							$mdDialog.hide();
						}

					},
					templateUrl : '/athena/js/src/angular_1.4/tools/glossary/commons/templates/new.logical.node.dialog.html',
					parent : angular.element(document.body),

				})
	};

	// <!-- fine tree -->

	ctr.showInfoWORD = function(ev, wordid) {
		console.log("showInfo");
		console.log(event)
		console.log(wordid)
		$mdDialog
				.show({
					controllerAs : 'infCtrl',
					scope : $scope,
					preserveScope : true,
					controller : function($mdDialog) {
						var iwctrl = this;
						sbiModule_restServices
								.get("1.0/glossary", "getWord",
										"WORD_ID=" + wordid)
								.success(
										function(data, status, headers, config) {
											if (data.hasOwnProperty("errors")) {
												showToast(
														sbiModule_translate
																.load("sbi.glossary.load.error"),
														3000);
											} else {
												iwctrl.info = data;
											}
										})
								.error(
										function(data, status, headers, config) {
											showToast(
													sbiModule_translate
															.load("sbi.glossary.load.error"),
													3000);

										})
					},
					templateUrl : '/athena/js/src/angular_1.4/tools/glossary/commons/templates/info_word.html',
					targetEvent : ev,
					clickOutsideToClose : true
				})
	}

	// rest call function

	function getAllWords() {
		showPreloader();
		sbiModule_restServices.get("1.0/glossary", "listWords").success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						showErrorToast(data.errors[0].message);
						showToast(sbiModule_translate.load("sbi.glossary.load.error"),
								3000);

					} else {
						ctr.words = data;
					}

					hidePreloader();
				}).error(function(data, status, headers, config) {
			showErrorToast('Ci sono errori! \n status ' + status);
			showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

			hidePreloader();
		})

	}

	function getAllGloss() {
		showPreloader();
		sbiModule_restServices.get("1.0/glossary", "listGlossary").success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						showErrorToast(data.errors[0].message);
						showToast(sbiModule_translate.load("sbi.glossary.load.error"),
								3000);

					} else {
						ctr.glossary = data;
					}

					hidePreloader();
				}).error(function(data, status, headers, config) {
			showErrorToast('Ci sono errori! \n status ' + status);
			showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

			hidePreloader();
		})

	}

	ctr.WordLike = function(ele, itemsPerPage) {
		var item = "Page=1&ItemPerPage=" + itemsPerPage;
		item += "&WORD=" + ele;
		ctr.showSearchPreloader = true;
		sbiModule_restServices.get("1.0/glossary", "listWords", item).success(
				function(data, status, headers, config) {

					if (data.hasOwnProperty("errors")) {
						showErrorToast(data.errors[0].message);
						showToast(sbiModule_translate.load("sbi.glossary.load.error"),
								3000);

					} else {
						ctr.words = data.item;
						ctr.totalWord = data.itemCount;
						ctr.showSearchPreloader = false;
					}

				}).error(function(data, status, headers, config) {
			showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
			ctr.showSearchPreloader = false;
		})
	}

	function getWord(ele) {
		showPreloader();
		sbiModule_restServices
				.get("1.0/glossary", "getWord", "WORD_ID=" + ele.WORD_ID)
				.success(
						function(data, status, headers, config) {
							if (data.hasOwnProperty("errors")) {
								showErrorToast(data.errors[0].message);
								showToast(sbiModule_translate
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
					showErrorToast('Ci sono errori! \n status ' + status);
					showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
					hidePreloader();
				})

	}

	ctr.getGlossaryNode = function(gloss, node, togg) {
		var PARENT_ID = (node == null ? null : node.CONTENT_ID);
		var GLOSSARY_ID = (gloss == null ? null : gloss.GLOSSARY_ID);

		sbiModule_restServices
				.get(
						"1.0/glossary",
						"listContents",
						"GLOSSARY_ID=" + GLOSSARY_ID + "&PARENT_ID="
								+ PARENT_ID)
				.success(
						function(data, status, headers, config) {

							if (data.hasOwnProperty("errors")) {
								showErrorToast(data.errors[0].message);
								showToast(sbiModule_translate
										.load("sbi.glossary.load.error"), 3000);

							} else {
								if (togg == undefined || togg.collapsed) {
									// check if parent is node or glossary
									node == null ? gloss.SBI_GL_CONTENTS = data
											: node.CHILD = data;
								} else {
									if (node != null) {
										node.CHILD
												.sort(function(a, b) {
													return (a.CONTENT_NM > b.CONTENT_NM) ? 1
															: ((b.CONTENT_NM > a.CONTENT_NM) ? -1
																	: 0);
												});
									}
								}

								if (togg != undefined) {
									togg.expand();
								}
								if (node != null
										&& node.hasOwnProperty("preloader")) {
									node.preloader = false;
								}
							}
						}).error(function(data, status, headers, config) {
					showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
					if (togg != undefined) {
						togg.expand();
						node.preloader = false;
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

	ctr.loadState = function() {
		sbiModule_restServices.get("domains", "listValueDescriptionByType",
				"DOMAIN_TYPE=GLS_STATE").success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						showToast(sbiModule_translate.load("sbi.glossary.load.error"),
								3000);
					} else {
						ctr.state = data;
					}
				}).error(function(data, status, headers, config) {
			showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

		})
	}
	ctr.loadState();

	ctr.loadCategory = function() {
		sbiModule_restServices.get("domains", "listValueDescriptionByType",
				"DOMAIN_TYPE=GLS_CATEGORY").success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						showToast(sbiModule_translate.load("sbi.glossary.load.error"),
								3000);
					} else {
						ctr.category = data;
					}
				}).error(function(data, status, headers, config) {
			showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

		})
	}
	ctr.loadCategory();

	// context menu options for angular list
	ctr.menuOpt = [ {
		label : sbiModule_translate.load('sbi.generic.modify'),
		action : function(item, event) {
			ctr.modifyWord(item);
		}
	},

	{
		label : sbiModule_translate.load('sbi.generic.delete'),
		action : function(item, event) {
			ctr.deleteWord(item);
		}
	},

	{
		label : sbiModule_translate.load('sbi.generic.details'),
		action : function(item, event) {
			ctr.showInfoWORD(event, item.WORD_ID);
		}
	}

	];

	ctr.glossMenuOpt = [ {
		label : sbiModule_translate.load('sbi.generic.modify'),
		action : function(item, event) {
			ctr.createNewGlossary(event, item);
		}
	},

	{
		label : sbiModule_translate.load('sbi.generic.clone'),
		action : function(item, event) {
			ctr.CloneGloss(event, item);
		}
	},

	{
		label : sbiModule_translate.load('sbi.generic.delete'),
		action : function(item, event) {
			ctr.deleteGlossary(item);
		}
	}

	];

	// ctr.glossSpeedMenuOpt = [
	// {
	// label : sbiModule_translate.load('sbi.generic.modify'),
	// icon :'fa fa-pencil' ,
	// backgroundColor:'red',
	// action : function(item,event) {
	// ctr.createNewGlossary(event,item);
	// }
	// },
	//		               	
	// {
	// label : sbiModule_translate.load('sbi.generic.clone'),
	// icon:"fa fa-files-o",
	// backgroundColor:'green',
	// action : function(item,event) {
	// ctr.CloneGloss(event,item);
	// }
	// } ,
	//		               	
	// {
	// label : sbiModule_translate.load('sbi.generic.delete'),
	// icon:"fa fa-trash-o",
	// backgroundColor:'blue',
	// action : function(item,event) {
	// ctr.deleteGlossary(item);
	// }
	// }
	//		             
	// ];

}
