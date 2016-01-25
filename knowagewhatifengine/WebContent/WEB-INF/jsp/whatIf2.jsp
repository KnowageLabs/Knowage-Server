
<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS               --%>
<%-- ---------------------------------------------------------------------- --%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="olapManager">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/olap/olapController.js"></script>
<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/css/olap.css">
<title>OLAP</title>

</head>
<body ng-controller="olapController">

<div layout="row" >
		<div flex=20>

			<div>
				<md-toolbar class='md-toolbar-tools knowage-blue'>Cubes</md-toolbar>
				<md-content layout="row" layout-wrap style="height:15%">
					<div flex=100>
						<md-input-container> 
							<label>Cubes</label>
								<md-select ng-model="cubes"> 
									<md-option ng-repeat="c in cubes" > 
										{{c}} 
									</md-option>
								</md-select> 
						</md-input-container>
					</div>
				</md-content>
			</div>

			<div>
				<md-toolbar class='md-toolbar-tools knowage-blue'>Dimensions</md-toolbar>
				<md-content layout="row" layout-wrap style="height:40%">
					<div flex=100>
						<md-list ng-repeat="d in dimensions">
							<md-list-item md-ink-ripple class="md-clickable">
								{{d}}
							</md-list-item>
						</md-list>
					</div>
				</md-content>
			</div>

			<div>
				<md-toolbar class='md-toolbar-tools knowage-blue'>Mesuers</md-toolbar>
				<md-content layout="row" layout-wrap style="height:40%">
					<div flex=100 >
						<md-list ng-repeat="d in dimensions">
							<md-list-item md-ink-ripple class="md-clickable">
								{{d}}
							</md-list-item>
						</md-list>
					</div>
				</md-content>
			</div>
			
		</div>
		<!--<div flex=20></div> -->
		<div style="width:2px"></div>
		<div flex=60>
			<!-- <div style="height:5%; background-color: #E91E63;"> -->
			<md-toolbar class='md-toolbar-tools knowage-blue'>
				 <section layout="row" layout-sm="column" layout-align="center center" layout-wrap>
				      <md-button class="groupX left">Position</md-button>
				      <md-button class="groupX middle">Member</md-button>
				      <md-button class="groupX right">Replace</md-button>
				      <md-button class="fa fa-table icon">
				      	<md-tooltip md-direction="bottom">
				      		Show parent
				      	</md-tooltip>
				  	  </md-button>
				      <md-button class="fa fa-th-large icon">
				      	<md-tooltip md-direction="bottom">
				      		Hide
				      	</md-tooltip>
				  	  </md-button>
				      <md-button class="fa fa-th-list icon">
				      	<md-tooltip md-direction="bottom">
				      		Show properties
				      	</md-tooltip>
				  	  </md-button>
				      <md-button class="fa fa-cubes icon">
				      	<md-tooltip md-direction="bottom">
				      		Suppress empty rows
				      	</md-tooltip>
				  	  </md-button>
				      <md-button class="fa fa-refresh icon">
				      	<md-tooltip md-direction="bottom">
				      		Reload
				      	</md-tooltip>
				  	  </md-button>
   				 </section>
   			</md-toolbar>
			<!-- </div> -->
			<!-- FILTERS -->
			<div layout="row">

				<md-card flex="20"class="filter-card">
					<md-card-title class="knowage-blue filter-toolbar">
          				<md-card-title-text >
            				<span>Region</span>
          				</md-card-title-text>
        			</md-card-title>
			        <md-card-actions layout="row" layout-align="end">
			          <md-button class="md-icon-button" style="bottom:0;" aria-label="Favorite" ng-click="openFiltersDialog(e)">
		              		<md-icon md-font-icon="fa fa-filter"></md-icon>
		               </md-button>
			        </md-card-actions>        			
				</md-card>

				<md-card flex=20 class="filter-card ">
					<md-card-title class="knowage-blue filter-toolbar">
          				<md-card-title-text>
            				<span>Customers</span>
          				</md-card-title-text>
        			</md-card-title>
			        <md-card-actions layout="row" layout-align="end">
			          <md-button class="md-icon-button" style="bottom:0;" aria-label="Favorite" ng-click="openFiltersDialog(e)">
		              		<md-icon md-font-icon="fa fa-filter"></md-icon>
		               </md-button>
			        </md-card-actions>        			
				</md-card>

				<md-card flex=20 class="filter-card">
					<md-card-title class="knowage-blue filter-toolbar">
          				<md-card-title-text>
            				<span> Product</span>
          				</md-card-title-text>
        			</md-card-title>
			        <md-card-actions layout="row" layout-align="end">
			          <md-button class="md-icon-button" style="bottom:0;" aria-label="Favorite" ng-click="openFiltersDialog(e)">
		              		<md-icon md-font-icon="fa fa-filter"></md-icon>
		               </md-button>
			        </md-card-actions>        			
				</md-card>
			</div>

			<div layout="column">
				<div layout="row">
					<md-toolbar flex=5 class='md-toolbar-tools knowage-blue'>
						&nbsp;
					</md-toolbar>
					<md-toolbar class='md-toolbar-tools knowage-blue dimension-top-toolbar'>
						<md-button class="dimension-top">Measure <icon class="fa fa-filter"/></md-button>
					</md-toolbar>
				</div>
				<div class="central-panel" layout="row" >
					<div style="width:32px;" class='knowage-blue dimension-left-toolbar'>
						<div layout="column">
							<md-button class="dimension-left" >Product <icon class="fa fa-filter"/></md-button>

							<md-button class="dimension-left" >Product1 <icon class="fa fa-filter"/></md-button>

							<md-button class="dimension-left" >Product2 <icon class="fa fa-filter"/></md-button>
						</div>	
					</div>
					<div>
						<div ng-iclude="listTest.html"></div>
						<table style=" margin-top: 10px; margin-left: 10px; width:100%">
						  <tr>
						    <td>Jill</td>
						    <td>Smith</td> 
						    <td>50</td>
						  </tr>
						  <tr>
						    <td>Eve</td>
						    <td>Jackson</td> 
						    <td>94</td>
						  </tr>
						  <tr>
						    <td>Eve</td>
						    <td>Jackson</td> 
						    <td>94</td>
						  </tr>
						  <tr>
						    <td>Eve</td>
						    <td>Jackson</td> 
						    <td>94</td>
						  </tr>
						  <tr>
						    <td>Eve</td>
						    <td>Jackson</td> 
						    <td>94</td>
						  </tr>
						</table>
					</div>
				</div>
			</div>

		</div>

		<div style="width:2px"></div>
		<div flex=20>
			<md-toolbar class='md-toolbar-tools knowage-blue'>Customization</md-toolbar>
			<md-tabs >
				  <md-tab label="Table">
				  	<md-content >
				  		Here goes the customization for table
				  	</md-content>
				  </md-tab>
				  <md-tab label="Chart">
				  	
				  </md-tab>
				  <md-tab label="Table & Chart">
				  		Table and chart
				  </md-tab>
			</md-tabs>			
		</div>
	</div>

</body>

</html>