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

app.controller('Controller', [ "translate", "restServices", "$q", "$scope",
                       		"$mdDialog", "$filter", "$timeout", "$mdToast", funzione ]);


var listDocument = [{
	DOCUMENT_ID :0,
	DOCUMENT_NM : "DOC1",
},{
	DOCUMENT_ID :1,
	DOCUMENT_NM : "DOC2",
},{
	DOCUMENT_ID :0,
	DOCUMENT_NM : "DOC1",
},{
	DOCUMENT_ID :1,
	DOCUMENT_NM : "DOC2",
},{
	DOCUMENT_ID :0,
	DOCUMENT_NM : "DOC1",
},{
	DOCUMENT_ID :1,
	DOCUMENT_NM : "DOC2",
},{
	DOCUMENT_ID :0,
	DOCUMENT_NM : "DOC1",
},{
	DOCUMENT_ID :1,
	DOCUMENT_NM : "DOC2",
},{
	DOCUMENT_ID :0,
	DOCUMENT_NM : "DOC1",
},{
	DOCUMENT_ID :1,
	DOCUMENT_NM : "DOC2",
},{
	DOCUMENT_ID :0,
	DOCUMENT_NM : "DOC1",
},{
	DOCUMENT_ID :1,
	DOCUMENT_NM : "DOC2",
},{
	DOCUMENT_ID :0,
	DOCUMENT_NM : "DOC1",
},{
	DOCUMENT_ID :1,
	DOCUMENT_NM : "DOC2",
},{
	DOCUMENT_ID :0,
	DOCUMENT_NM : "DOC1",
},{
	DOCUMENT_ID :1,
	DOCUMENT_NM : "DOC2",
},{
					DOCUMENT_ID :2,
					DOCUMENT_NM : "DOC3",
				} 
				]

function funzione(translate, restServices, $q, $scope, $mdDialog, $filter,
		$timeout, $mdToast) {
	ctrl=this;
	ctrl.listDoc=listDocument;
	ctrl.glossary;
	ctrl.showPreloader = false;
	ctrl.selectedGloss;
	getAllGloss();
	
	
	ctrl.showSelectedGlossary = function(gloss) {
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
								showToast(translate.load("sbi.glossary.load.error"), 3000);

							} else {
								
								if(togg==undefined || togg.collapsed){
								//check if parent is node or glossary
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
							}
						}).error(function(data, status, headers, config) {
					console.log("nodi non ottenuti " + status);
					showToast(translate.load("sbi.glossary.load.error"), 3000);
					if (togg != undefined) {
						togg.expand();
						node.preloader = false;
					}
				})
	}
	
	ctrl.toggle = function(scope, item, gloss) {
		console.log("toggle")
		console.log(scope)
		item.preloader = true;
		if (scope.collapsed) {
			ctrl.getGlossaryNode(gloss, item, scope)
		} else {
			scope.toggle();
			item.preloader = false;
		}
	};
	
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
						ctrl.glossary = data;
					}

					hidePreloader();
				}).error(function(data, status, headers, config) {
			console.log("Glossary non Ottenuti " + status);
			showErrorToast('Ci sono errori! \n status ' + status);
			showToast(translate.load("sbi.glossary.load.error"), 3000);

			hidePreloader();
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
		ctrl.showPreloader = true;

	}
	function hidePreloader(pre) {
		ctrl.showPreloader = false;
	}
}