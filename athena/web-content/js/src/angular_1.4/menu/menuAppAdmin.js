var myApp = angular.module('menuAppAdmin', ['ngMaterial']);

myApp.controller('menuCtrl', ['$scope','$mdDialog',
	function ($scope,$mdDialog ) {
		$scope.languages = [];
		$scope.openAside = false;
		$scope.toggleMenu = function(){
			$scope.openAside = !$scope.openAside;
		}
	}
]);

myApp.directive('menuAside', ['$http','$mdDialog', function($http,$mdDialog) {
    return {


    	


        restrict: 'E',
        templateUrl: Sbi.config.contextName+"/js/src/angular_1.4/menu/templates/menuBarAdmin.html",
        replace: true,
        link:function($scope, elem, attrs) {
        	$scope.jsonFile = {
		    "fixedMenu": [
		        {
		            "iconAlign": "top",
		            "scale": "large",
		            "tooltip": "My roles",
		            "iconCls": "assignment_ind",
		            "hrefTarget": "_self",
		            "itemLabel": "ROLE",
		            "href": "javascript:roleSelection()",
		            "linkType": "roleSelection",
		            "firstUrl": "/athena"
		        },
		        {
		            "iconAlign": "top",
		            "scale": "large",
		            "tooltip": "Languages",
		            "iconCls": "flag",
		            "hrefTarget": "_self",
		            "itemLabel": "LANG",
		            "linkType": "languageSelection",
		            "firstUrl": "/athena"
		        },
		        {
		            "iconAlign": "top",
		            "scale": "large",
		            "tooltip": "Help - SpagoBI Wiki",
		            "iconCls": "help",
		            "itemLabel": "HELP",
		            "href": "http://wiki.spagobi.org/xwiki/bin/view/Main/",
		            "firstUrl": "http://wiki.spagobi.org/xwiki/bin/view/Main/",
		            "hrefTarget": "_blank",
		            "linkType": "externalUrl"
		        },
		        {
		            "iconAlign": "top",
		            "scale": "large",
		            "tooltip": "Info",
		            "iconCls": "info",
		            "hrefTarget": "_self",
		            "itemLabel": "INFO",
		            "href": "javascript:info()",
		            "linkType": "info",
		            "firstUrl": "/athena"
		        },
		        {
		            "iconAlign": "top",
		            "scale": "large",
		            "tooltip": "Logout",
		            "iconCls": "power_settings_new",
		            "hrefTarget": "_self",
		            "href": "javascript:execUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE&NEW_SESSION=TRUE')",
		            "linkType": "execUrl",
		            "firstUrl": "/athena/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE&NEW_SESSION=TRUE"
		        }
		    ],
		    "userMenu": [],
		    "customMenu": [
		        {
		            "iconCls": "home",
		            "tooltip": "Home",
		            "iconAlign": "top",
		            "scale": "large",
		            "path": "Home",
		            "itemLabel": "HOME",
		            "hrefTarget": "_self",
		            "href": "javascript:goHome(null, 'spagobi');",
		            "linkType": "goHome"
		        },
		        {
		            "iconCls": "spagobi",
		            "tooltip": "User menu",
		            "iconAlign": "top",
		            "scale": "large",
		            "path": "User menu",
		            "hrefTarget": "_self",
		            "menu": [
		                {
		                    "text": "Mio Report",
		                    "style": "text-align: left;",
		                    "hrefTarget": "_self",
		                    "iconCls": "bullet",
		                    "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MENU_BEFORE_EXEC&MENU_ID=1', 'Mio Report' )",
		                    "linkType": "execDirectUrl",
		                    "menu": [
		                        {
		                            "text": "DocBrowser",
		                            "style": "text-align: left;",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=DOCUMENT_USER_BROWSER_START_ACTION', 'Mio Report > DocBrowser')",
		                            "linkType": "execDirectUrl"
		                        }
		                    ]
		                }
		            ]
		        },
		        {
		            "iconCls": "cogwheels",
		            "tooltip": "Resources",
		            "iconAlign": "top",
		            "scale": "large",
		            "path": "Resources",
		            "hrefTarget": "_self",
		            "menu": [
		                {
		                    "title": "Data Providers",
		                    "titleAlign": "left",
		                    "columns": 1,
		                    "xtype": "buttongroup",
		                    "items": [
		                        {
		                            "text": "Tenants management",
		                            "style": "text-align: left;",
		                            "src": "/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/multitenant/multitenantManagement.jsp",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/multitenant/multitenantManagement.jsp', 'Tenants management')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Engines management",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?PAGE=ListEnginesPage&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?PAGE=ListEnginesPage&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Engines management')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Data source",
		                            "style": "text-align: left;",
		                            "src": "/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/datasource/listDataSource.jsp;LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/datasource/listDataSource.jsp;LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Data source')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Data set",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_DATASETS_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_DATASETS_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Data set')",
		                            "linkType": "execDirectUrl"
		                        }
		                    ]
		                },
		                {
		                    "title": "Catalogs",
		                    "titleAlign": "left",
		                    "columns": 1,
		                    "xtype": "buttongroup",
		                    "items": [
		                        {
		                            "text": "Business Models catalog",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_META_MODELS_CATALOGUE_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_META_MODELS_CATALOGUE_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Business Models catalog')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Mondrian schemas catalog",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_ARTIFACTS_CATALOGUE_ACTION&type=MONDRIAN_SCHEMA&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_ARTIFACTS_CATALOGUE_ACTION&type=MONDRIAN_SCHEMA&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Mondrian schemas catalog')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Maps",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?PAGE=ListMapsPage&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?PAGE=ListMapsPage&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Maps')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Features",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?PAGE=ListFeaturesPage&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?PAGE=ListFeaturesPage&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Features')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Measures catalog",
		                            "style": "text-align: left;",
		                            "src": "/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/measure/measuresCatalogue.jsp",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/measure/measuresCatalogue.jsp', 'Measures catalog')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Layers catalog",
		                            "style": "text-align: left;",
		                            "src": "/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/layer/layerCatalogue.jsp",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/layer/layerCatalogue.jsp', 'Layers catalog')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Glossary",
		                            "style": "text-align: left;",
		                            "src": "/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/glossary/businessuser/glossaryBusiness.jsp",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/glossary/businessuser/glossaryBusiness.jsp', 'Glossary')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Glossary",
		                            "style": "text-align: left;",
		                            "src": "/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/glossary/technicaluser/glossaryTechnical.jsp",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/glossary/technicaluser/glossaryTechnical.jsp', 'Glossary')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Dataset federation",
		                            "style": "text-align: left;",
		                            "src": "/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp', 'Dataset federation')",
		                            "linkType": "execDirectUrl"
		                        }
		                    ]
		                },
		                {
		                    "title": "Server Settings",
		                    "titleAlign": "left",
		                    "columns": 1,
		                    "xtype": "buttongroup",
		                    "items": [
		                        {
		                            "text": "User Data Properties",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_UDP_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_UDP_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'User Data Properties')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Configuration management",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=CONFIG_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=CONFIG_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Configuration management')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Domain management",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=DOMAIN_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=DOMAIN_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Domain management')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Metadata",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?PAGE=ListObjMetadataPage&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?PAGE=ListObjMetadataPage&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Metadata')",
		                            "linkType": "execDirectUrl"
		                        }
		                    ]
		                }
		            ]
		        },
		        {
		            "iconCls": "group",
		            "tooltip": "Profiling",
		            "iconAlign": "top",
		            "scale": "large",
		            "path": "Profiling",
		            "hrefTarget": "_self",
		            "menu": [
		                {
		                    "title": "Profile Management",
		                    "titleAlign": "left",
		                    "columns": 1,
		                    "xtype": "buttongroup",
		                    "items": [
		                        {
		                            "text": "Profile Attributes Management ",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_ATTRIBUTES_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_ATTRIBUTES_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Profile Attributes Management ')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Roles Management",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_ROLES_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_ROLES_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Roles Management')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Users Management",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_USER_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_USER_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Users Management')",
		                            "linkType": "execDirectUrl"
		                        }
		                    ]
		                },
		                {
		                    "title": "Behavioural model",
		                    "titleAlign": "left",
		                    "columns": 1,
		                    "xtype": "buttongroup",
		                    "items": [
		                        {
		                            "text": "Lovs Management",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?PAGE=ListLovsPage&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?PAGE=ListLovsPage&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Lovs Management')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Analytical drivers management",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?PAGE=ListParametersPage&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?PAGE=ListParametersPage&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Analytical drivers management')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Constraints management",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?PAGE=LISTMODALITIESCHECKSPAGE&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?PAGE=LISTMODALITIESCHECKSPAGE&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Constraints management')",
		                            "linkType": "execDirectUrl"
		                        }
		                    ]
		                },
		                {
		                    "title": "Menu Configuration",
		                    "titleAlign": "left",
		                    "columns": 1,
		                    "xtype": "buttongroup",
		                    "items": [
		                        {
		                            "text": "Menu configuration",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?PAGE=MenuConfigurationPage",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?PAGE=MenuConfigurationPage', 'Menu configuration')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Community management",
		                            "style": "text-align: left;",
		                            "src": "/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/community/ManageCommunity.jsp",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/restful-services/publish?PUBLISHER=/WEB-INF/jsp/community/ManageCommunity.jsp', 'Community management')",
		                            "linkType": "execDirectUrl"
		                        }
		                    ]
		                },
		                {
		                    "title": "Tree Management",
		                    "titleAlign": "left",
		                    "columns": 1,
		                    "xtype": "buttongroup",
		                    "items": [
		                        {
		                            "text": "Functionalities management",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?PAGE=BIObjectsPage&OPERATION=FUNCTIONALITIES_OPERATION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?PAGE=BIObjectsPage&OPERATION=FUNCTIONALITIES_OPERATION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Functionalities management')",
		                            "linkType": "execDirectUrl"
		                        }
		                    ]
		                }
		            ]
		        },
		        {
		            "iconCls": "folder_open",
		            "tooltip": "Documents development",
		            "iconAlign": "top",
		            "scale": "large",
		            "path": "Documents development",
		            "hrefTarget": "_self",
		            "href": "javascript:javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=DOCUMENT_USER_BROWSER_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Documents development')",
                    "firstUrl":"/athena/servlet/AdapterHTTP?ACTION_NAME=DOCUMENT_USER_BROWSER_START_ACTION", 
		            "linkType": "execDirectUrl"
		        },
		        {
		            "iconCls": "my_data",
		            "tooltip": "My data",
		            "iconAlign": "top",
		            "scale": "large",
		            "path": "My data",
		            "hrefTarget": "_self",
		            "href": "javascript:javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=SELF_SERVICE_DATASET_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE&MYDATA=false', 'My data')",
		            "firstUrl": "/athena/servlet/AdapterHTTP?ACTION_NAME=SELF_SERVICE_DATASET_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE&MYDATA=false",
		            "linkType": "execDirectUrl"
		        },
		        {
		            "iconCls": "charts",
		            "tooltip": "Analytical model",
		            "iconAlign": "top",
		            "scale": "large",
		            "path": "Analytical model",
		            "hrefTarget": "_self",
		            "menu": [
		                {
		                    "title": "Kpi Model",
		                    "titleAlign": "left",
		                    "columns": 1,
		                    "xtype": "buttongroup",
		                    "items": [
		                        {
		                            "text": "KPI Definition",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_KPIS_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_KPIS_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'KPI Definition')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Threshold Definition",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_THRESHOLDS_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_THRESHOLDS_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Threshold Definition')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Model Definition",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_MODELS_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_MODELS_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Model Definition')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Model Instance",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_MODEL_INSTANCES_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_MODEL_INSTANCES_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Model Instance')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Grants Definition",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_OU_EMPTY_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_OU_EMPTY_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Grants Definition')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Goals Definition",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_GOALS_EMPTY_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_GOALS_EMPTY_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Goals Definition')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Resources Definition",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_RESOURCES_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_RESOURCES_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Resources Definition')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Alarms Management",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_ALARMS_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_ALARMS_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Alarms Management')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Contacts Management",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_CONTACTS_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_CONTACTS_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Contacts Management')",
		                            "linkType": "execDirectUrl"
		                        }
		                    ]
		                }
		            ]
		        },
		        {
		            "iconCls": "repo",
		            "tooltip": "Repository Management",
		            "iconAlign": "top",
		            "scale": "large",
		            "path": "Repository Management",
		            "hrefTarget": "_self",
		            "menu": [
		                {
		                    "title": "Tools",
		                    "titleAlign": "left",
		                    "columns": 1,
		                    "xtype": "buttongroup",
		                    "items": [
		                        {
		                            "text": "Import/export",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?PAGE=BIObjectsPage&OPERATION=IMPORTEXPORT_OPERATION&OBJECTS_VIEW=VIEW_OBJECTS_AS_TREE&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?PAGE=BIObjectsPage&OPERATION=IMPORTEXPORT_OPERATION&OBJECTS_VIEW=VIEW_OBJECTS_AS_TREE&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Import/export')",
		                            "linkType": "execDirectUrl"
		                        },
		                        {
		                            "text": "Scheduler",
		                            "style": "text-align: left;",
		                            "src": "/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_SCHEDULER_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE",
		                            "hrefTarget": "_self",
		                            "iconCls": "bullet",
		                            "href": "javascript:execDirectUrl('/athena/servlet/AdapterHTTP?ACTION_NAME=MANAGE_SCHEDULER_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE', 'Scheduler')",
		                            "linkType": "execDirectUrl"
		                        }
		                    ]
		                }
		            ]
		        }
		    ],
		    "userName": "SpagoBI Administrator"
		};

        		$scope.fixed = $scope.jsonFile.fixedMenu;
        		$scope.userName = $scope.jsonFile.userName;
        		$scope.groups = $scope.jsonFile.customMenu;
        		
        		
        		
        		var newJson= new Array();
				angular.forEach($scope.groups, function(value, key) {
					if (typeof(value.menu) != 'undefined')	{
						for (var i=0; i<value.menu.length;i++){
							var newGroups = {};
								newGroups.title = value.menu[i].title ? value.menu[i].title : value.menu[i].text;
								newGroups.items = value.menu[i].items ? value.menu[i].items : value.menu[i].menu;
								newJson.push(newGroups);
								
						}
					} else {
					      $scope.fixed.push(value);
				     }		
				});
        		
        		$scope.groups = newJson;

        	$scope.showAlert = function(title,messageText){
                var alert = $mdDialog.alert()
                .title(title)
                .content('<br/><div>'+messageText+'</div>')
                .ok('Close');
                  $mdDialog
                    .show( alert )
                    .finally(function() {
                      alert = undefined;
                    });
        	}
        	
        	$scope.showDialog = function showDialog() {
        	       var parentEl = angular.element(document.body);
        	       $mdDialog.show({
        	         parent: parentEl,
        	         templateUrl: Sbi.config.contextName+"/js/src/angular_1.4/menu/templates/languageDialog.html",
        	         locals: {
        	           languages: $scope.languages
        	         }
        	         ,controller: DialogController
        	      });
        	      function DialogController(scope, $mdDialog, languages) {
        	        scope.languages = languages;
        	        scope.closeDialog = function() {
        	          $mdDialog.hide();
        	        }
        	        scope.menuCall=$scope.menuCall;
        	      }
        	    }

			$scope.redirectIframe = function(url){
				document.getElementById("iframeDoc").contentWindow.location.href = url;
				$scope.openAside = false;
			}
			
			$scope.execUrl = function(url){
				document.location.href = url;
				return;
			}
			
			$scope.roleSelection = function roleSelection(){				
				if(Sbi.user.roles && Sbi.user.roles.length > 1){
					this.win_roles = new Sbi.home.DefaultRoleWindow({'SBI_EXECUTION_ID': ''});
					this.win_roles.show();
				} else {
					$scope.openAside = false;
					$scope.showAlert('Role Selection','You currently have only one role');
				}
			}
			
			$scope.externalUrl = function externalUrl(url){
				window.open(url, "_blank")

			}
			
			$scope.info = function info(){
				if(!win_info_1){

					win_info_1= new Ext.Window({
						frame: false,
						style:"background-color: white",
						id:'win_info_1',
						autoLoad: {url: Sbi.config.contextName+'/themes/'+Sbi.config.currTheme+'/html/infos.jsp'},             				
						layout:'fit',
						width:210,
						height:180,
						closeAction:'hide',
						//closeAction:'close',
						buttonAlign : 'left',
						plain: true,
						title: LN('sbi.home.Info')
					});
				}		
				win_info_1.show();
			}
			
			$scope.callExternalApp = function callExternalApp(url){
				if (!Sbi.config.isSSOEnabled) {
					if (url.indexOf("?") == -1) {
						url += '?<%= SsoServiceInterface.USER_ID %>=' + Sbi.user.userUniqueIdentifier;
					} else {
						url += '&<%= SsoServiceInterface.USER_ID %>=' + Sbi.user.userUniqueIdentifier;
					}
				}
				
				$scope.redirectIframe(url);
			}
			
			$scope.goHome = function goHome(html){
				var url;
				if(!html){
					url = firstUrlTocallvar;
				}else{
					url = Sbi.config.contextName+'/themes/'+Sbi.config.currTheme+html;
				}
				if(url){
					$scope.redirectIframe(url);
				}
			}
			
			$scope.languageSelection = function languageSelection(){
        		$scope.toggleMenu();
        		
        		debugger;
	 			var languages = [];

    	 		for (var j = 0; j < Sbi.config.supportedLocales.length ; j++) {
    	 			var aLocale = Sbi.config.supportedLocales[j];
     				var languageItem = {
    					text: aLocale.language,
    					iconCls:'icon-' + aLocale.language,
    					href: $scope.getLanguageUrl(aLocale),
    					linkType: 'execUrl'
    				};
     				languages.push(languageItem);
    	 		}
    	 		
    	 		var languageTemplate;
    	 		for (var i = 0; i < languages.length; i++){
    	 			if (languageTemplate != undefined){
    	 				languageTemplate = languageTemplate + languages[i].text +"<br/>";
    	 			} else {
    	 				languageTemplate = languages[i].text +"<br/>";
    	 			}
    	 		}
        		
    	 		$scope.languages = languages;
        		
    	 		$scope.showDialog();
				//$scope.showAlert("Select Language",languageTemplate)
			}
			
			$scope.getLanguageUrl = function getLanguageUrl(config){
				var languageUrl = Sbi.config.contextName+"/servlet/AdapterHTTP?ACTION_NAME=CHANGE_LANGUAGE&LANGUAGE_ID="+config.language+"&COUNTRY_ID="+config.country+"&THEME_NAME="+Sbi.config.currTheme;
				return languageUrl;
			}
			
			
			$scope.menuCall = function menuCall(url,type){
				if (type == 'execDirectUrl'){
					$scope.redirectIframe(url);
				} else if (type == 'roleSelection'){
					$scope.roleSelection();
				} else if (type =="execUrl"){
					$scope.execUrl(url)
				} else if (type == "externalUrl"){
					$scope.externalUrl(url)
				} else if (type == "info"){
					$scope.info();
				} else if (type == "callExternalApp"){
					$scope.callExternalApp(url)
				} else if (type == "goHome"){
					$scope.goHome(url);
				} else if (type == "languageSelection"){
					$scope.languageSelection();
				}
			}
        }
    };
}]);