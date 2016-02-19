<div layout="column" layout-wrap layout-fill ng-controller="measureRoleQueryController">
    <md-input-container>
        <label>{{translate.load("sbi.ds.dataSource")}}</label>
        <md-select ng-model="selectedDatasource">
          <md-option ng-repeat="ds in datasourcesList" value="{{ds.DATASOURCE_LABEL}}" ng-click="alterDatasource(ds.DATASOURCE_ID)">
            {{ds.DATASOURCE_LABEL}}
          </md-option>
        </md-select>
     </md-input-container> 
    <md-whiteframe ng-if="dataSourcesIsSelected" class="md-whiteframe-2dp relative" layout-margin flex  >
		<ui-codemirror class="absolute" layout-fill ui-codemirror-opts="codemirrorOptions" ng-model=currentMeasure.query></ui-codemirror> 
     </md-whiteframe>
</div>