<md-toolbar class="secondaryToolbar" layout="row">
    <div class="md-toolbar-tools" flex layout-align="center center">
      	<h2 class="md-flex" >{{translate.load("sbi.execution.viewpoints.title")}}</h2>
     	<span flex></span>
      	<md-button title="Close" aria-label="Close" class="toolbar-button-custom" ng-click="returnToDocument()">
			{{translate.load("sbi.general.close")}}
	 	</md-button>
	</div>
</md-toolbar>
<div ag-grid="savedParametersGrid" class="ag-theme-balham ag-theme-knowage-advanced ag-noBorders"></div>