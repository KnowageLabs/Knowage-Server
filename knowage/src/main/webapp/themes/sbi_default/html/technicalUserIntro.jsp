<%@ page language="java"
         extends="it.eng.spago.dispatching.httpchannel.AbstractHttpJspPagePortlet"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         session="true" 
         import="it.eng.spago.base.*,
                 it.eng.spagobi.commons.constants.SpagoBIConstants"
%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.IUrlBuilder"%>
<%@page import="it.eng.spago.base.SourceBean"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="it.eng.spagobi.utilities.themes.ThemesManager"%>
<%@page import="it.eng.spagobi.commons.constants.ObjectsTreeConstants"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="java.util.Enumeration"%>

<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<html>
<head>

<!--S4C-->

<!--<link rel="stylesheet" href="${pageContext.request.contextPath}/themes/sbi_default/s4c/css/angular-material.min.css">-->
<link rel="stylesheet" href="${pageContext.request.contextPath}/themes/sbi_default/s4c/css/angular-material.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/themes/sbi_default/s4c/material-icons/material-icons.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/themes/sbi_default/s4c/css/style.css">
<!--S4C fine -->

<link id="spagobi-angular" rel="styleSheet"	href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>" type="text/css" />  
</head>
<body class="landingPageAdmin" data-ng-controller="MainController" ng-app="knowageIntro">

    <md-toolbar class="header-toolbar" md-colors="::{background: 'grey-A100', color: 'default-blue-grey-900'}" md-scroll-shrink="true" md-whiteframe="1">
        <div class="md-toolbar-tools">
            <div layout="row" layout-align="start center">
                <!--<img class="navbar-brand" src="${pageContext.request.contextPath}/themes/sbi_default/s4c/images/s4c_logo.png">-->
                <div class="navbar-brand"></div>
            </div>
            <span flex></span>
            <div layout="row" layout-align="end center">
                <!--<img class="navbar-brand" src="${pageContext.request.contextPath}/themes/sbi_default/s4c/images/s4c_logo.png">-->
                <div class="navbar-project"></div>
            </div>
        </div>
    </md-toolbar>

<!--
	<div class="layer">
		<img src="../img/adminLogo.png" class="logo"/>
		<div class="text">Open menu here to begin</div>
    </div>
-->
 <md-content flex layout-padding>
    <div class="knowage-intro" layout="row" layout-xs="column" layout-wrap layout-align="center center" >

        <div id="level1" ng-show="level1" flex-gt-md="99" flex-lg="66">
          <div class="footer-button" layout="row" layout-sm="column" layout-align="center center" layout-wrap>
           <!-- <md-button class="md-raised md-primary">Advanced configuration</md-button> -->
           &nbsp;
          </div>
          <div layout="row" layout-xs="column" >

            <md-card flex flex-gt-sm="33" md-whiteframe="{{height_a}}" ng-init="height_a = 3" ng-mouseenter="height_a = 10" ng-mouseleave="height_a = 3">
              <img ng-src="${pageContext.request.contextPath}/themes/sbi_default/s4c/images/dashboard_management.png" class="md-card-image" alt="Dashboard Management">
              <md-card-title>
                <md-card-title-text >
                  <span class="md-headline">Dashboard Management</span>
                </md-card-title-text>
              </md-card-title>
              <md-card-content>
                  <span class="md-subhead">Create interactive visualizations and manage your dashboard</span>
              </md-card-content>
              <md-card-actions layout="row" layout-align="end center">
                  <md-button class="md-primary" aria-label="New" ng-click="showLevel2A()">
                    Manage
                  </md-button>
              </md-card-actions>
            </md-card>

            <md-card flex flex-gt-sm="33"  md-whiteframe="{{height_b}}" ng-init="height_b = 3" ng-mouseenter="height_b = 10" ng-mouseleave="height_b = 3">
              <img ng-src="${pageContext.request.contextPath}/themes/sbi_default/s4c/images/kpi_management.png" class="md-card-image" alt="KPI Management">
              <md-card-title>
                <md-card-title-text >
                  <span class="md-headline">KPI Management</span>
                </md-card-title-text>
              </md-card-title>
              <md-card-content>
                  <span class="md-subhead">Develop metrics witch allow city manager to take a snapshot on key aspects of their city</span>
              </md-card-content>
              <md-card-actions layout="row" layout-align="end center">
                  <md-button class="md-primary" aria-label="New" ng-click="showLevel2B()">
                    Manage
                  </md-button>
              </md-card-actions>
            </md-card>

            <md-card flex flex-gt-sm="33"  md-whiteframe="{{height_c}}" ng-init="height_c = 3" ng-mouseenter="height_c = 10" ng-mouseleave="height_c = 3">
              <img ng-src="${pageContext.request.contextPath}/themes/sbi_default/s4c/images/dataset_management.png" class="md-card-image" alt="Dataset Management">
              <md-card-title>
                <md-card-title-text >
                  <span class="md-headline">Dataset Management</span>
                </md-card-title-text>
              </md-card-title>
              <md-card-content>
                  <span class="md-subhead">Define and manage datasets among a wide range of types</span>
              </md-card-content>
              <md-card-actions layout="row" layout-align="end center">
                  <md-button class="md-primary" aria-label="New" ng-href="/knowage/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/catalogue/datasetManagement.jsp?LIGHT_NAVIGATOR_RESET_INSERT=TRUE">
                    Manage
                  </md-button>
              </md-card-actions>
            </md-card>

          </div>
          <!--<div class="footer-button" layout="row" layout-sm="column" layout-align="center center" layout-wrap>
            <md-button class="md-raised md-primary">Advanced configuration</md-button>
          </div>-->
        </div>

        <div id="level2A" ng-show="level2A" flex-gt-md="99" flex-lg="66">
          <div class="footer-button" layout="row" layout-sm="column" layout-align="center center" layout-wrap>
            <md-button class="md-raised md-accent" ng-click="showLevel1()">Back</md-button>
            <!--<md-button class="md-raised md-primary">Advanced configuration</md-button>-->
          </div>
          <div layout="row" layout-xs="column">

            <md-card flex flex-gt-sm="33" flex-gt-xs="33" flex-offset-gt-sm="17" flex-offset-gt-xs="17" md-whiteframe="{{height_a}}" ng-init="height_a = 3" ng-mouseenter="height_a = 10" ng-mouseleave="height_a = 3">
              <img ng-src="${pageContext.request.contextPath}/themes/sbi_default/s4c/images/new.png" class="md-card-image" alt="Dashboard projects">
              <md-card-title>
                <md-card-title-text >
                  <span class="md-headline">Create Dashboard</span>
                </md-card-title-text>
              </md-card-title>
              <md-card-content>
                  <span class="md-subhead">Create nice visualizations and understand the data of your city</span>
              </md-card-content>
              <md-card-actions layout="row" layout-align="end center">
                  <md-button class="md-primary" aria-label="New" ng-click="showLevel3A()">
                    Create
                  </md-button>
              </md-card-actions>
            </md-card>

            <md-card flex flex-gt-sm="33" flex-gt-xs="33" md-whiteframe="{{height_b}}" ng-init="height_b = 3" ng-mouseenter="height_b = 10" ng-mouseleave="height_b = 3">
              <img ng-src="${pageContext.request.contextPath}/themes/sbi_default/s4c/images/view.png" class="md-card-image" alt="My Dashbords">
              <md-card-title>
                <md-card-title-text >
                  <span class="md-headline">My Dashbords</span>
                </md-card-title-text>
              </md-card-title>
              <md-card-content>
                  <span class="md-subhead">Visualize and edit your dashboard</span>
              </md-card-content>
              <md-card-actions layout="row" layout-align="end center">
                  <md-button class="md-primary" aria-label="New" ng-href="/knowage/servlet/AdapterHTTP?ACTION_NAME=DOCUMENT_USER_BROWSER_START_ANGULAR_ACTION">
                    View
                  </md-button>
              </md-card-actions>
            </md-card>

          </div>
          <!--<div class="footer-button" layout="row" layout-sm="column" layout-align="center center" layout-wrap>
            <md-button class="md-raised md-primary" ng-click="showLevel1()">Back</md-button>
            <md-button class="md-raised md-primary">Advanced configuration</md-button>
          </div>-->
        </div>

        <div id="level3A" ng-show="level3A" flex-gt-md="99" flex-lg="66">
          <div class="footer-button" layout="row" layout-sm="column" layout-align="center center" layout-wrap>
            <md-button class="md-raised md-accent" ng-click="showLevel2A()">Back</md-button>
          </div>
          <div layout="row" layout-xs="column">

            <md-card flex flex-gt-sm="33" flex-gt-xs="33" flex-offset-gt-sm="17" flex-offset-gt-xs="17" md-whiteframe="{{height_a}}" ng-init="height_a = 3" ng-mouseenter="height_a = 10" ng-mouseleave="height_a = 3">
              <img ng-src="${pageContext.request.contextPath}/themes/sbi_default/s4c/images/empty.png" class="md-card-image" alt="image caption">
              <md-card-title>
                <md-card-title-text>
                  <span class="md-headline">Empty dashboard</span>
                  <!--<span class="md-subhead">Create nice visualizations and understand the data of your city</span>-->
                </md-card-title-text>
              </md-card-title>
              <md-card-content>
                Create a new empty dashboard
              </md-card-content>
              <md-card-actions layout="row" layout-align="end center">
                  <md-button class="md-primary" aria-label="Create" ng-href="/knowage/servlet/AdapterHTTP?ACTION_NAME=DOCUMENT_USER_BROWSER_WORKSPACE">
                    create
                  </md-button>
              </md-card-actions>
            </md-card>

            <md-card flex flex-gt-sm="33" flex-gt-xs="33" class="disabled"  md-whiteframe="{{height_b}}" ng-init="height_b = 3" ng-mouseenter="height_b = 10" ng-mouseleave="height_b = 3">
              <img ng-src="${pageContext.request.contextPath}/themes/sbi_default/s4c/images/template-1.png" class="md-card-image" alt="image caption">
              <md-card-title>
                <md-card-title-text>
                  <span class="md-headline">Mobility dashboard template</span>
                  <!--<span class="md-subhead">Visualize and edit your dashboard</span>-->
                </md-card-title-text>
              </md-card-title>
              <md-card-content>
                Create a simple Mobility dashboard template
              </md-card-content>
              <md-card-actions layout="row" layout-align="end center">
                <md-button class="md-primary" disabled>Use</md-button>
              </md-card-actions>
            </md-card>

          </div>
      <!--<div class="footer-button" layout="row" layout-sm="column" layout-align="center center" layout-wrap>
        <md-button class="md-raised md-primary" ng-click="showLevel2A()">Back</md-button>
      </div>-->
        </div>

        <div id="level2B" ng-show="level2B" flex-gt-md="99" flex-lg="66">
          <div class="footer-button" layout="row" layout-sm="column" layout-align="center center" layout-wrap>
            <md-button class="md-raised md-accent" ng-click="showLevel1()">Back</md-button>
          </div>
              <div layout="row" layout-xs="column">

                <md-card flex flex-gt-sm="33" md-whiteframe="{{height_a}}" ng-init="height_a = 3" ng-mouseenter="height_a = 10" ng-mouseleave="height_a = 3">
                  <img ng-src="${pageContext.request.contextPath}/themes/sbi_default/s4c/images/kpi_definition.svg" class="md-card-image" alt="KPI Definition">
                  <md-card-title>
                    <md-card-title-text layout-align="center center"> 
                      <span class="md-headline">KPI Definition</span>
                    </md-card-title-text>
                  </md-card-title>
                  <md-card-content>
                  </md-card-content>
                  <md-card-actions layout="row" layout-align="end center">
                    <md-button class="md-primary" aria-label="KPI Definition" ng-href="/knowage/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/kpi/kpiDefinition.jsp">
                        Open
                    </md-button>
                  </md-card-actions>
                </md-card>

                <md-card flex flex-gt-sm="33" md-whiteframe="{{height_b}}" ng-init="height_b = 3" ng-mouseenter="height_b = 10" ng-mouseleave="height_b = 3">
                  <img ng-src="${pageContext.request.contextPath}/themes/sbi_default/s4c/images/kpi_measure.svg" class="md-card-image" alt="Measure/Rule Definition">
                  <md-card-title>
                    <md-card-title-text layout-align="center center">
                      <p class="md-headline">Measure/Rule Definition</p>
                    </md-card-title-text>
                  </md-card-title>
                  <md-card-content>
                  </md-card-content>
                  <md-card-actions layout="row" layout-align="end center">
                    <md-button class="md-primary" aria-label="Measure/Rule Definition" ng-href="/knowage/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/kpi/measureRuleDefinition.jsp" >
                        Open
                    </md-button>
                  </md-card-actions>
                </md-card>

                <md-card flex flex-gt-sm="33" md-whiteframe="{{height_c}}" ng-init="height_c = 3" ng-mouseenter="height_c = 10" ng-mouseleave="height_c = 3">
                  <img ng-src="${pageContext.request.contextPath}/themes/sbi_default/s4c/images/kpi_target.svg" class="md-card-image" alt="Target Definition">

                  <md-card-title>
                    <md-card-title-text layout-align="center center">
                      <span class="md-headline">Target Definition</span>
                    </md-card-title-text>
                  </md-card-title>
                  <md-card-content>
                  </md-card-content>
                  <md-card-actions layout="row" layout-align="end center">
                    <md-button class="md-primary" aria-label="Target definition" ng-href="/knowage/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/kpi/targetDefinition.jsp">
                    Open
                    </md-button>
                  </md-card-actions>
                </md-card>

              </div>
              <div layout="row" layout-xs="column">

                <md-card flex flex-gt-sm="33" flex-gt-xs="33" flex-offset-gt-sm="17" flex-offset-gt-xs="17" md-whiteframe="{{height_d}}" ng-init="height_d = 3" ng-mouseenter="height_d = 10" ng-mouseleave="height_d = 3">
                  <img ng-src="${pageContext.request.contextPath}/themes/sbi_default/s4c/images/kpi_scheduler.svg" class="md-card-image" alt="KPI Scheduler">
                  <md-card-title>
                    <md-card-title-text layout-align="center center">
                      <span class="md-headline">KPI Scheduler</span>
                    </md-card-title-text>
                  </md-card-title>
                  <md-card-content>
                  </md-card-content>
                  <md-card-actions layout="row" layout-align="end center">
                    <md-button class="md-primary" aria-label="KPI Scheduler" ng-href="/knowage/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/kpi/schedulerKpi.jsp">
                    Open
                    </md-button>
                  </md-card-actions>
                </md-card>

                <md-card flex flex-gt-sm="33" flex-gt-xs="33" md-whiteframe="{{height_e}}" ng-init="height_e = 3" ng-mouseenter="height_e = 10" ng-mouseleave="height_e = 3">
                  <img ng-src="${pageContext.request.contextPath}/themes/sbi_default/s4c/images/kpi_scorecard.svg" class="md-card-image" alt="Scorecard">
                  <md-card-title>
                    <md-card-title-text layout-align="center center">
                      <span class="md-headline">Scorecard</span>
                    </md-card-title-text>
                  </md-card-title>
                  <md-card-content>
                  </md-card-content>
                  <md-card-actions layout="row" layout-align="end center">
                    <md-button class="md-primary" aria-label="Scorecard" ng-href="/knowage/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/kpi/scorecardKpi.jsp">
                    Open
                    </md-button>
                  </md-card-actions>
                </md-card>

              </div>
          </div>

    </div>
  </md-content>

  <md-content layout="row" flex="noshrink" layout-align="center center">
    <div id="license-footer" flex="" >
      
    </div>
  </md-content>

  <!-- Angular Material requires Angular.js Libraries -->
  <script src="${pageContext.request.contextPath}/themes/sbi_default/s4c/js/angular.min.js"></script>
  <script src="${pageContext.request.contextPath}/themes/sbi_default/s4c/js/angular-animate.min.js"></script>
  <script src="${pageContext.request.contextPath}/themes/sbi_default/s4c/js/angular-aria.min.js"></script>
  <script src="${pageContext.request.contextPath}/themes/sbi_default/s4c/js/angular-messages.min.js"></script>
  <!-- Angular Material Library -->
  <script src="${pageContext.request.contextPath}/themes/sbi_default/s4c/js/angular-material.min.js"></script>


  <script type="text/javascript">
    /**
     * You must include the dependency on 'ngMaterial'
     */

    var app = angular.module('knowageIntro', ['ngMaterial', 'ngMessages', 'ngAnimate']);

    app.controller('MainController', ['$scope',
      function MainController($scope) {
        $scope.level1 = true;
        $scope.level2A = $scope.level2B = $scope.level3A = false;

        $scope.showLevel1 = function() {
          $scope.level1 = true;
          $scope.level2A = $scope.level2B = $scope.level3A = false;
        }

        $scope.showLevel2A = function() {
          $scope.level2A = true;
          $scope.level1 = $scope.level2B = $scope.level3A = false;
        }

        $scope.showLevel2B = function() {
          $scope.level2B = true;
          $scope.level1 = $scope.level2A = $scope.level3A = false;
        }

        $scope.showLevel3A = function() {
          $scope.level3A = true;
          $scope.level1 = $scope.level2A = $scope.level2B = false;
        }

      }
    ]);

    app.config(function($mdThemingProvider) {

        $mdThemingProvider.definePalette('s4c-green', {
            '50': 'D3D7DC',
            '100': 'A5B3BA',
            '200': '778F98',
            '300': '496B76',
            '400': '325965',
            '500': '1B4754',
            '600': '153944',
            '700': '12323C',
            '800': '0F2B34',
            '900': '0C242C',
            'A100': 'B1C5C7',
            'A200': '89A9AB',
            'A400': '618D8F',
            'A700': '397173',
            'contrastDefaultColor': 'light',
            'contrastDarkColors': ['50', '100', '200', '300', '400'], //hues which contrast should be 'dark' by default
            'contrastLightColors': undefined    // could also specify this if default was 'dark'
          });
        $mdThemingProvider.definePalette('s4c-orange', {
            '50': 'FFF9F1',
            '100': 'FFEEDE',
            '200': 'FDD8B8',
            '300': 'FBC292',
            '400': 'F9AC6C',
            '500': 'F79646',
            '600': 'DE873F',
            '700': 'C57838',
            '800': 'AC6931',
            '900': '935A2A',
            'A100': 'FFCDB0',
            'A200': 'FFB588',
            'A400': 'FF9D60',
            'A700': 'FF8538',
            'contrastDefaultColor': 'dark',
            'contrastDarkColors': ['50', '100', '200', '300', '400'], //hues which contrast should be 'dark' by default
            'contrastLightColors': undefined    // could also specify this if default was 'dark'
          });

        $mdThemingProvider.theme('S4C')
                .primaryPalette('s4c-green', {
                  'default': '500', // by default use shade 400 from the pink palette for primary intentions
                  'hue-1': '400', // use shade 100 for the <code>md-hue-1</code> class
                  'hue-2': '300', // use shade 600 for the <code>md-hue-2</code> class
                  'hue-3': '200' // use shade A100 for the <code>md-hue-3</code> class
                })
                // If you specify less than all of the keys, it will inherit from the
                // default shades
                .accentPalette('s4c-orange', {
                  'default': '500' // use shade 200 for default, and keep all other shades the same
                })
                .backgroundPalette('neonWhite')
        
        var whiteMap = $mdThemingProvider.extendPalette('grey', {
            'A100': '#ffffff',
            'contrastDefaultColor': 'dark'
          });

        // Register the new color palette map with the name <code>neonWhite</code>
        $mdThemingProvider.definePalette('neonWhite', whiteMap);


        //Activate Theme
    //      $mdThemingProvider.setDefaultTheme('default');
          $mdThemingProvider.setDefaultTheme('S4C');

    });

  </script>

</body>
</html>