<!-- <md-button ng-click="nextStep()">nextStep 3</md-button> -->

<md-content layout-padding layout-column layout-wrap>
<div layout="row" layout-align="end center">
	<md-button ng-click="saveEngineAssociation()" class="md-raised">next</md-button> 
</div>

<md-list  ng-cloak ng-init="manageFirstAssociationsEngines()">
	<md-list-item ng-repeat="expEngines in IEDConf.engines.exportedEngines" class="secondary-button-padding">
	<div layout="column" layout-wrap flex="50">
   		<span><b>{{expEngines.name}}</b></span>
   		<span>{{expEngines.description}}</span>
   		<span>{{expEngines.url}}</span>
   		<span>{{expEngines.driverName}}</span>
	</div>
   		<p ng-if="IEDConf.engines.associatedEngines[expEngines.id].fixed==true">{{IEDConf.engines.associatedEngines[expEngines.id].name}}</p>
   		<md-input-container ng-if="IEDConf.engines.associatedEngines[expEngines.id].fixed!=true"  flex="50">
	        <label>Engines</label>
	        <md-select ng-model="IEDConf.engines.associatedEngines[expEngines.id]">
	        <md-option ng-value="{id:''}"></md-option>
	          <md-option ng-repeat="currEngines in IEDConf.engines.currentEngines" ng-value="currEngines" ng-if="currentEnginesIsSelectable(currEngines,expEngines)">
	            {{currEngines.name}}
	          </md-option>
	        </md-select>
      </md-input-container>
    <md-divider></md-divider>
  </md-list-item>
</md-list>


</md-content>
