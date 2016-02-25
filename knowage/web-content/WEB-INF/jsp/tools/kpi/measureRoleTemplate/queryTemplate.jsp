<div layout="column" layout-wrap layout-fill ng-controller="measureRoleQueryController">
    <md-input-container>
        <label>{{translate.load("sbi.ds.dataSource")}}</label>
        <md-select ng-model="currentRole.dataSourceId">
          <md-option ng-repeat="ds in datasourcesList" value={{ds.DATASOURCE_ID}} ng-click="alterDatasource(ds.DATASOURCE_ID)">
            {{ds.DATASOURCE_LABEL}}
          </md-option>
        </md-select>
     </md-input-container> 
    <md-whiteframe ng-if="detailProperty.dataSourcesIsSelected" class="md-whiteframe-2dp relative" layout-margin flex  >
		<div ui-codemirror="{ onLoad : codemirrorLoaded }" class="absolute" layout-fill ui-codemirror-opts="codemirrorOptions" ng-model=currentRole.definition></div> 
     </md-whiteframe>
</div>