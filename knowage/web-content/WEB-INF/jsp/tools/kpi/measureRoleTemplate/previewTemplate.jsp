<md-content ng-controller="measureRolePreviewController" layout-fill  layout="row">

	<angular-table class="relative fullWidth" flex id='previewtable' ng-model="detailProperty.previewData.rows"
				columns="detailProperty.previewData.columns" no-pagination="true">
	</angular-table>
				 
	<md-sidenav class="md-sidenav-rigth md-whiteframe-z2" md-component-id="placeholderFormTab" md-is-locked-open="::havePlaceholder()">
	      <md-toolbar>
	      <div class="md-toolbar-tools" latoyt="row">
	        <h1 flex >{{translate.load("sbi.kpi.filters")}}</h1>
	         <md-button ng-click="loadPreview()">Run</md-button>
	      </div>
	      </md-toolbar>
	      <md-content layout-margin >   
	         <md-input-container class="md-block" ng-repeat=" plcH in currentRule.placeholders">
	            <label>{{plcH.name}}</label>
	            <input ng-model="plcH.value">
	         </md-input-container>
          
	      </md-content>
	    </md-sidenav>

</md-content>
<!-- SELECT * FROM employee as cur where cur.full_name=@firstPlaceh -->