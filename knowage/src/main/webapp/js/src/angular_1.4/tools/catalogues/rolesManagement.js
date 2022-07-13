var app = angular.module("RolesManagementModule", ["ngMaterial", 'ngMessages', "angular_list", "angular_table", "sbiModule", "angular_2_col","angular-list-detail", 'angularXRegExp']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);

app.controller("RolesManagementController", ["sbiModule_translate", "sbiModule_restServices", "kn_regex", "$scope", "$mdDialog", "$mdToast", "$timeout","sbiModule_messaging","sbiModule_user", RolesManagementFunction]);

function RolesManagementFunction(sbiModule_translate, sbiModule_restServices, kn_regex, $scope, $mdDialog, $mdToast, $timeout,sbiModule_messaging,sbiModule_user) {

	// VARIABLES
	$scope.regex = kn_regex;
	$scope.showme = false; // flag for showing right side
	$scope.dirtyForm = false; // flag to check for modification
	$scope.disable = false; // flag that disable some role options
	$scope.checkName = false;
	$scope.translate = sbiModule_translate;
	$scope.selectedRole = {}; // main item
	$scope.selectedMeta = {};
	$scope.selectedDs = {};
	$scope.rolesList = []; // array that hold list of roles
	$scope.authList = [];
	$scope.listType = []; // list that holds list of domain roles
	$scope.roleMetaModelCategories = [];
	$scope.category = [];
	$scope.roleDataSetCategories = [];
	$scope.ds_category = [];
	$scope.listCategories = [];
	//VARIABLE FOR CATEGORY KPI
	$scope.categoriesSelected = [];
	checkboxList =[{dbname:"SAVE_SUBOBJECTS",label:"saveSubobj",visible:false, category:"SAVE"},  //save,see,send,build,manage,items,export,ENABLE
	               {dbname:"SEE_SUBOBJECTS",label:"seeSubobj",visible:false, category:"SEE"},
	               {dbname:"SEE_VIEWPOINTS",label:"seeViewpoints",visible:false, category:"SEE"},
	               {dbname:"SEND_MAIL",label:"sendMail",visible:false, category:"SEND"},
	               {dbname:"SAVE_INTO_FOLDER",label:"savePersonalFolder",visible:false, category:"SAVE"},
	               {dbname:"SAVE_REMEMBER_ME",label:"saveRemember",visible:false, category:"SAVE"},
	               {dbname:"BUILD_QBE_QUERY",label:"buildQbe",visible:false, category:"BUILD"},
	               {dbname:"MANAGE_USERS",label:"manageUsers",visible:false, category:"MANAGE"},
	               {dbname:"SEE_DOCUMENT_BROWSER",label:"seeDocBrowser",visible:false, category:"SEE"},
	               {dbname:"SEE_FAVOURITES",label:"seeFavourites",visible:false, category:"ITEMS"},
	               {dbname:"SEE_TODO_LIST",label:"seeToDoList",visible:false, category:"ITEMS"},
	               {dbname:"CREATE_DOCUMENTS",label:"createDocument",visible:false, category:"ITEMS"},
	               {dbname:"ENABLE_DATASET_PERSISTENCE",label:"enableDatasetPersistence",visible:false, category:"ENABLE"},
	               {dbname:"EDIT_PYTHON_SCRIPTS",label:"editPythonScripts",visible:false, category:"ENABLE"},
	               {dbname:"CREATE_CUSTOM_CHART",label:"createCustomChart",visible:false, category:"ENABLE"},
	               {dbname:"ENABLE_FEDERATED_DATASET",label:"enableFederatedDataset",visible:false, category:"ENABLE"},
	               {dbname: "ENABLE_TO_RATE", label: "enableToRate", visible: false, category: "ENABLE"},
	               {dbname: "ENABLE_TO_PRINT", label: "enableToPrint", visible: false, category: "ENABLE"},
	               {dbname: "ENABLE_TO_COPY_AND_EMBED", label: "enableToCopyAndEmbed", visible: false, category: "ENABLE"},
	               {dbname:"SEE_MY_DATA",label:"seeMyData",visible:false, category:"ITEMS"},
	               {dbname:"SEE_MY_WORKSPACE",label:"seeMyWorkspace",visible:false, category:"ITEMS"},
	               {dbname:"DO_MASSIVE_EXPORT",label:"doMassiveExport",visible:false, category:"EXPORT"},
	               {dbname:"SEE_SUBSCRIPTIONS",label:"seeSubscriptions",visible:false, category:"ITEMS"},
	               {dbname:"CREATE_SOCIAL_ANALYSIS",label:"createSocialAnalysis",visible:false, category:"ITEMS"},
	               {dbname:"VIEW_SOCIAL_ANALYSIS",label:"viewSocialAnalysis",visible:false, category:"ITEMS"},
	               {dbname:"MANAGE_KPI_VALUE",label:"manageKpiValue",visible:false, category:"MANAGE"},
	               {dbname:"FUNCTIONS_CATALOG_USAGE",label:"functionsCatalogUsage",visible:false, category:"ITEMS"},
	               {dbname:"HIERARCHIES_MANAGEMENT",label:"hierarchiesManagement",visible:false, category:"ITEMS"},
	               {dbname:"MANAGE_INTERNATIONALIZATION",label:"manageInternationalization",visible:false, category:"MANAGE"},
	               {dbname:"CREATE_SELF_SERVICE_COCKPIT",label:"createSelfSelviceCockpit",visible:false, category:"ITEMS"},
	               {dbname:"CREATE_SELF_SERVICE_GEOREPORT",label:"createSelfSelviceGeoreport",visible:false, category:"ITEMS"},
	               {dbname:"CREATE_SELF_SERVICE_KPI",label:"createSelfSelviceKpi",visible:false, category:"ITEMS"},
	               {dbname:"SEE_NEWS",label:"newsVisualization",visible:false, category:"ITEMS"},
	               {dbname:"MANAGE_WIDGET_GALLERY",label:"manageWidgetGallery",visible:false, category:"MANAGE"}
	               ];

	var showEEAuthorizations = sbiModule_user.functionalities.indexOf("EnterpriseAuthorizations")>-1;
	if (showEEAuthorizations){
		checkboxList.push({dbname:"SEE_NOTES",label:"seeNotes",visible:false, category:"SEE"});
		checkboxList.push({dbname:"SEE_METADATA",label:"seeMeta",visible:false, category:"SEE"});
		checkboxList.push({dbname:"SAVE_METADATA",label:"saveMeta",visible:false, category:"SAVE"});
		checkboxList.push({dbname:"GLOSSARY",label:"",visible:false, category:"save"});
		checkboxList.push({dbname:"MANAGE_GLOSSARY_BUSINESS",label:"manageGlossaryBusiness",visible:false, category:"MANAGE"});
		checkboxList.push({dbname:"MANAGE_GLOSSARY_TECHNICAL",label:"manageGlossaryTechnical",visible:false, category:"MANAGE"});
		checkboxList.push({dbname:"MANAGE_CALENDAR",label:"manageCalendar",visible:false, category:"MANAGE"});
		checkboxList.push({dbname:"SEE_SNAPSHOTS",label:"seeSnapshot",visible:false, category:"SEE"});
		checkboxList.push({dbname:"RUN_SNAPSHOTS",label:"runSnapshot",visible:false, category:"SEE"});
	}



	$scope.rmSpeedMenu = [{
		label: sbiModule_translate.load("sbi.generic.delete"),
		icon: 'fa fa-trash',
		//icon: 'fa fa-trash-o fa-lg',
		//color: '#153E7E',
		action: function (item, event) {

			$scope.confirmDelete(item,event);
		}
	}];

	$scope.confirm = $mdDialog
	.confirm()
	.title(sbiModule_translate.load("sbi.catalogues.generic.modify"))
	.content(
			sbiModule_translate
			.load("sbi.catalogues.generic.modify.msg"))
			.ariaLabel('toast').ok(
					sbiModule_translate.load("sbi.general.continue")).cancel(
							sbiModule_translate.load("sbi.general.cancel"));

	$scope.confirmDelete = function(item,ev) {
		var confirm = $mdDialog.confirm()
		.title(sbiModule_translate.load("sbi.catalogues.toast.confirm.title"))
		.content(sbiModule_translate.load("sbi.catalogues.toast.confirm.content"))
		.ariaLabel("confirm_delete")
		.targetEvent(ev)
		.ok(sbiModule_translate.load("sbi.general.continue"))
		.cancel(sbiModule_translate.load("sbi.general.cancel"));
		$mdDialog.show(confirm).then(function() {
			$scope.deleteRole(item);
		}, function() {

		});
	};


	// FUNCTIONS

	angular.element(document).ready(function () { // on page load function
		$scope.getRoles();
		$scope.getAuthorizations();
		$scope.getDomainType();
		$scope.getCategories();
		$scope.getDsCategories();

	});
	$scope.roleInit = function(){ // function the inits role object on creation

		$scope.disable = true;
		$scope.selectedRole.ableToManageGlossaryBusiness= false;
		$scope.selectedRole.ableToManageGlossaryTechnical= false;
		$scope.selectedRole.ableToManageKpiValue= false;
		$scope.selectedRole.ableToManageCalendar = false;
		$scope.selectedRole.ableToManageInternationalization = false;
		$scope.selectedRole.ableToUseFunctionsCatalog = false;
		$scope.selectedRole.ableToManageUsers= false;
		$scope.selectedRole.ableToSaveIntoPersonalFolder= true;
		$scope.selectedRole.ableToEnableDatasetPersistence= true;
		$scope.selectedRole.ableToEnableFederatedDataset= true;
		$scope.selectedRole.ableToCreateSocialAnalysis= true;
		$scope.selectedRole.ableToEditMyKpiComm= true;
		$scope.selectedRole.ableToSeeSubobjects= true;
		$scope.selectedRole.ableToBuildQbeQuery= true;
		$scope.selectedRole.ableToSaveSubobjects= true;
		$scope.selectedRole.ableToEditPythonScripts= false;
		$scope.selectedRole.ableToSaveRememberMe= true;
		$scope.selectedRole.ableToSendMail= true;
		$scope.selectedRole.ableToSeeFavourites= true;
		$scope.selectedRole.ableToSaveMetadata= true;
		$scope.selectedRole.ableToSeeViewpoints= true;
		$scope.selectedRole.ableToSeeNotes= true;
		$scope.selectedRole.ableToSeeSnapshots= false;
		$scope.selectedRole.ableToRunSnapshots= false;
		$scope.selectedRole.ableToDoMassiveExport= true;
		$scope.selectedRole.ableToCreateDocuments= true;
		$scope.selectedRole.ableToHierarchiesManagement= true;
		$scope.selectedRole.ableToEditAllKpiComm= true;
		$scope.selectedRole.ableToSeeDocumentBrowser= true;
		$scope.selectedRole.ableToSeeSubscriptions= true;
		$scope.selectedRole.ableToSeeMyData= true;
		$scope.selectedRole.ableToSeeMyWorkspace= true;
		$scope.selectedRole.ableToSeeMetadata= true;
		$scope.selectedRole.ableToSeeToDoList= true;
		$scope.selectedRole.ableToViewSocialAnalysis= true;
		$scope.selectedRole.ableToDeleteKpiComm= true;
		$scope.selectedRole.ableToEnableRate = false;
		$scope.selectedRole.ableToEnablePrint = false;
		$scope.selectedRole.ableToEnableCopyAndEmbed = false;
		$scope.selectedRole.ableToCreateSelfServiceCockpit = true;
		$scope.selectedRole.ableToCreateSelfServiceGeoreport = true;
		$scope.selectedRole.ableToCreateSelfServiceKpi = true;
		$scope.selectedRole.ableToManageWidgetGallery = true;
	}

	$scope.setDirty = function () {
		$scope.dirtyForm = true;
	}

	/*
	 * 	function that checks if role is "USER" and enables user available choices
	 *	also assigns Domain Type values to main item on change
	 */
	$scope.changeType = function(item) {
		console.log(item);
		for (var i = 0; i < $scope.listType.length; i++) {
			if($scope.listType[i].VALUE_CD == item){
				$scope.selectedRole.roleTypeID=$scope.listType[i].VALUE_ID;
			}
		}
		if ($scope.selectedRole.roleTypeCD == "USER") {
			$scope.disable = false;
		}else{
			$scope.disable = true;
		}
		//The admin has all the DataSets associated
		if ($scope.selectedRole.roleTypeCD == "ADMIN") {
			//Store a backup copy of the selected DataSet and select all the DataSets
			$scope.ds_category_backup = angular.copy($scope.ds_category);
			$scope.ds_category = [].concat($scope.roleDataSetCategories);
		}else{
			//restore previous DataSet selection
			$scope.ds_category = [];
			if ($scope.ds_category_backup != undefined){
				for ( var i = 0 ; i <  $scope.ds_category_backup.length ; i++){
					for (var j = 0 ; j < $scope.roleDataSetCategories.length; j++){
						if ($scope.roleDataSetCategories[j].VALUE_NM == $scope.ds_category_backup[i].VALUE_NM){
							$scope.ds_category.push($scope.roleDataSetCategories[j]);
						}
					}
				}
			}
		}
	}

	/*
	 * 	function that adds VALUE_TR property to each Domain Type
	 *  object because of internalization
	 */
	$scope.addTranslation = function() {

		for ( var l in $scope.listType) {
			switch ($scope.listType[l].VALUE_CD) {
			case "USER":
				$scope.listType[l].VALUE_TR = sbiModule_translate.load("sbidomains.nm.user");
				break;
			case "ADMIN":
				$scope.listType[l].VALUE_TR = sbiModule_translate.load("sbidomains.nm.admin");
				break;
			case "DEV_ROLE":
				$scope.listType[l].VALUE_TR = sbiModule_translate.load("sbidomains.nm.dev_role");
				break;
			case "TEST_ROLE":
				$scope.listType[l].VALUE_TR = sbiModule_translate.load("sbidomains.nm.test_role");
				break;
			case "MODEL_ADMIN":
				$scope.listType[l].VALUE_TR = sbiModule_translate.load("sbidomains.nm.model_admin");
				break;
			default:
				break;
			}
		}

	}

	/*
	 * 	this function is used to properly format
	 *  selected roles meta model categories
	 *  for adding or updating.
	 *
	 */
	$scope.formatCategories = function () {

		if ($scope.category.length > 0) {

			var tmpR = [];
			for (var i = 0; i < $scope.category.length; i++) {
				$scope.selectedMeta={};
				$scope.selectedMeta.categoryId=$scope.category[i].VALUE_ID;
				tmpR.push($scope.selectedMeta);
			}
			$scope.selectedRole.roleMetaModelCategories = tmpR;

		}else{

			delete $scope.selectedRole.roleMetaModelCategories;
		}
	}


	/*
	 * 	this function is used to properly fill meta model
	 *  categories table with meta model categories from
	 *  selected role
	 */
	$scope.setCetegories = function(data) {
		if (data.length>0) {
			$scope.category = [];
			for (var i = 0; i < $scope.roleMetaModelCategories.length; i++) {
				for (var j = 0; j < data.length; j++) {
					if (data[j].categoryId == $scope.roleMetaModelCategories[i].VALUE_ID) {
						$scope.category.push($scope.roleMetaModelCategories[i]);
					}
				}
			}
		}else{
			$scope.category = [];
		}
	}


	/*
	 * 	this function is used to properly format
	 *  selected roles data set categories
	 *  for adding or updating.
	 *
	 */
	$scope.formatDsCategories = function () {

		if ($scope.ds_category.length > 0) {

			var tmpR = [];
			for (var i = 0; i < $scope.ds_category.length; i++) {
				$scope.selectedDs={};
				$scope.selectedDs.categoryId=$scope.ds_category[i].VALUE_ID;
				tmpR.push($scope.selectedDs);
			}
			$scope.selectedRole.roleDataSetCategories = tmpR;

		}else{

			delete $scope.selectedRole.roleDataSetCategories;
		}
	}


	/*
	 * 	this function is used to properly fill data set
	 *  categories table with data set categories from
	 *  selected role
	 */
	$scope.setDsCategories = function(data) {
	/*	if (data.length>0) {
			$scope.ds_category = [];
			for (var i = 0; i < $scope.roleDataSetCategories.length; i++) {
				for (var j = 0; j < data.length; j++) {
					if (data[j].categoryId == $scope.roleDataSetCategories[i].VALUE_ID) {
						$scope.ds_category.push($scope.roleDataSetCategories[i]);
					}
				}
			}
		}else{
			$scope.ds_category = [];
		}	*/
	}


	$scope.indexInList=function(item, list,param) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object[param]==item){
				return i;
			}
		}

		return -1;
	};


	/*
	 * 	this function is called when item
	 *  from table is clicked
	 */
	$scope.loadRole = function (item) {
		$scope.getCategoriesByID(item);

		$scope.categoriesSelected = [];
		$scope.ds_category = [];
		sbiModule_restServices.promiseGet("2.0/roles", "categories/"+item.id)
		.then(function(response) {
			for(var i=0;i<response.data.length;i++){
				var index =$scope.indexInList(response.data[i].categoryId, $scope.listCategories,"VALUE_ID");
				if(index!=-1){
			//		var obj = {};
			//		obj["VALUE_ID"] = response.data[i].categoryId;
					$scope.categoriesSelected.push($scope.listCategories[index])
				}
				var index =$scope.indexInList(response.data[i].categoryId, $scope.roleDataSetCategories,"VALUE_ID");
				if(index!=-1){

					$scope.ds_category.push($scope.roleDataSetCategories[index]);
				}

			}

		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});

		if ($scope.dirtyForm) {
			$mdDialog.show($scope.confirm).then(function () {
				$scope.dirtyForm = false;
				$scope.selectedRole = angular.copy(item);
				$scope.showme = true;
			}, function () {
				$scope.showme = true;
			});

		} else {

			$scope.selectedRole = angular.copy(item);
			$scope.showme = true;
		}
		if ($scope.selectedRole.roleTypeCD == "USER") {
			$scope.disable = false;
		}else{
			$scope.disable = true;
		}
	}

	$scope.cancel = function () { // on cancel button
		$scope.selectedRole = {};
		$scope.category = [];
		$scope.ds_category = [];
		$scope.showme = false;
		$scope.dirtyForm = false;
	}

	/*
	 * 	this function is called when clicking
	 *  on plus button(create)
	 */
	$scope.createRole = function () {
		angular.copy([],$scope.categoriesSelected);
		if ($scope.dirtyForm) {
			$mdDialog.show($scope.confirm).then(function () {
				$scope.selectedRole = {};
				$scope.category = [];
				$scope.ds_category = [];

				$scope.dirtyForm = false;
				$scope.roleInit();
				$scope.showme = true;

			}, function () {

				$scope.showme = true;

			});

		} else {
			$scope.selectedRole = {};
			$scope.category = [];
			$scope.ds_category = [];
			$scope.roleInit();
			$scope.showme = true;
		}
	}

	/*
	 * 	this function is called when clicking
	 *  on save button.
	 *  If item already exists do update @PUT,
	 *  If item doesn't exist insert new one @POST
	 *
	 */
	$scope.saveRole = function () {

		$scope.formatCategories();
		$scope.formatDsCategories();
		console.log($scope.selectedRole.roleMetaModelCategories);

		//assign to selectedRole the categories of kpi checked.
		for(var i =0;i<$scope.categoriesSelected.length;i++){
			var obj = $scope.categoriesSelected[i];
			if($scope.selectedRole.roleMetaModelCategories ==undefined){
				$scope.selectedRole.roleMetaModelCategories = [];
			}
			var objTemp = {};
			objTemp["categoryId"] = obj.VALUE_ID;
			objTemp["roleId"] = $scope.selectedRole.id;
			$scope.selectedRole.roleMetaModelCategories.push(objTemp);
		}
		if($scope.selectedRole.roleDataSetCategories!=undefined){
			if($scope.selectedRole.roleMetaModelCategories==undefined){
				$scope.selectedRole.roleMetaModelCategories = [];
			}
			for(var i =0;i<$scope.selectedRole.roleDataSetCategories.length;i++){
				$scope.selectedRole.roleMetaModelCategories.push($scope.selectedRole.roleDataSetCategories[i]);
			}
			delete $scope.selectedRole.roleDataSetCategories;
		}

		if($scope.selectedRole.hasOwnProperty("id")){

			sbiModule_restServices.promisePost("2.0/roles", $scope.selectedRole.id , $scope.selectedRole)
			.then(function(response) {
				$scope.rolesList = [];
				$timeout(function(){
					$scope.getRoles();
				}, 1000);
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');
				$scope.selectedRole = {};
				$scope.showme=false;
				$scope.dirtyForm=false;
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

			});

		}else{

			for (var i = 0; i < $scope.rolesList.length; i++) {
				if($scope.rolesList[i].name.toUpperCase() === $scope.selectedRole.name.toUpperCase()){
					sbiModule_messaging.showErrorMessage('Role already exists', 'Error');
					$scope.checkName = true;
				}
			}


			if(!$scope.checkName){
			sbiModule_restServices.promisePost("2.0/roles","",angular.toJson($scope.selectedRole, true))
			.then(function(response) {
				$scope.rolesList=[];
				$timeout(function(){
					$scope.getRoles();
				}, 1000);
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
				$scope.selectedRole = {};
				$scope.showme=false;
				$scope.dirtyForm=false;
				$scope.checkName = false;
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

			});
			}
		}
	}

	$scope.getRoles = function () { // service that gets list of roles GET

		sbiModule_restServices.promiseGet("2.0", "roles")
		.then(function(response) {
			$scope.rolesList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	}

	$scope.getAuthorizations = function () { // service that gets list of authorizations GET

		sbiModule_restServices.promiseGet("authorizations","")
		.then(function(response) {
			$scope.authList = response.data;
			initCBList();
			console.log($scope.authList);
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	}

	/*
	 * 	service that gets domain types for
	 *  dropdown @GET
	 */
	$scope.getDomainType = function(){

		sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType","DOMAIN_TYPE=ROLE_TYPE")
		.then(function(response) {
			$scope.listType = response.data;
			$scope.addTranslation();
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	}

	/*
	 * 	service that gets domain types for meta model
	 *  categories @GET
	 */
	$scope.getCategories = function(){

		sbiModule_restServices.promiseGet("domains","listValueDescriptionByType","DOMAIN_TYPE=BM_CATEGORY")
		.then(function(response) {
			$scope.roleMetaModelCategories = response.data;

		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	}



	/*
	 * 	service that gets loaded meta model categories
	 *  for selected role @GET
	 */
	$scope.getCategoriesByID = function(item){

		sbiModule_restServices.promiseGet("2.0/roles/categories", item.id)
		.then(function(response) {
			$scope.setCetegories(response.data);

		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	}

	/*
	 * 	service that gets domain types for data set
	 *  categories @GET
	 */
	$scope.getDsCategories = function(){

		sbiModule_restServices.promiseGet("domains","listValueDescriptionByType","DOMAIN_TYPE=CATEGORY_TYPE")
		.then(function(response) {
			$scope.roleDataSetCategories = response.data;

		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	}

	/*
	 * 	this function is called when
	 *  clicking on delete button @DELETE
	 */
	$scope.deleteRole = function (item) {

		sbiModule_restServices.promiseDelete("2.0/roles", item.id)
		.then(function(response) {
			$scope.rolesList = [];
			$timeout(function () {
				$scope.getRoles();
			}, 1000);
			sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
			$scope.selectedRole = {};
			$scope.showme = false;
			$scope.dirtyForm = false;

		}, function(response) {
			console.log(response);
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.catalogues.error.inuse"), 'Error');

		});
	}

	initCBList = function(){
		for(var i=0; i<$scope.authList.root.length;i++){
			setVisible($scope.authList.root[i].name);
		}
	}

	setVisible = function(dbname){
		for(var i=0; i<checkboxList.length;i++){
			if(checkboxList[i].dbname == dbname){
				checkboxList[i].visible = true;
				break;
			}
		}
	}

	$scope.isVisible = function(label){
		for(var i=0; i<checkboxList.length;i++){
			if(checkboxList[i].label == label)
				return checkboxList[i].visible;
		}
	}

	$scope.isToolbarVisible = function(name){
		name = name.toUpperCase();
		for(var i=0; i<checkboxList.length;i++){
			if(checkboxList[i].category == name && checkboxList[i].visible)
				return true;
		}
		return false;
	};
};
