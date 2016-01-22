<!-- <md-button ng-click="nextStep()">nextStep 3</md-button> -->

<md-content layout-padding layout-column layout-wrap>
<div layout="row" layout-align="end center">
	<md-button ng-click="saveDatasourceAssociation()" class="md-raised">next</md-button> 
</div>

<md-list  ng-cloak ng-init="manageFirstAssociationsDatasources()">
	<md-list-item ng-repeat="expDatasources in IEDConf.datasources.exportedDatasources" class="secondary-button-padding">
	<div layout="column" layout-wrap flex="50">
   		<span><b>{{expDatasources.label}}</b></span>
   		<span>{{expDatasources.descr}}</span>
   		<span>{{expDatasources.jndi}}</span>
	</div>
   		<p ng-if="IEDConf.datasources.associatedDatasources[expDatasources.dsId].fixed==true">{{IEDConf.datasources.associatedDatasources[expDatasources.dsId].label}}</p>
   		<md-input-container ng-if="IEDConf.datasources.associatedDatasources[expDatasources.dsId].fixed!=true"  flex="50">
	        <label>Datasources</label>
	        <md-select ng-model="IEDConf.datasources.associatedDatasources[expDatasources.dsId]">
	        <md-option ng-value="{dsId:''}"></md-option>
	          <md-option ng-repeat="currDatasources in IEDConf.datasources.currentDatasources" ng-value="currDatasources" ng-if="currentDatasourcesIsSelectable(currDatasources,expDatasources)">
	            {{currDatasources.label}}
	          </md-option>
	        </md-select>
      </md-input-container>
    <md-divider></md-divider>
  </md-list-item>
</md-list>


</md-content>
