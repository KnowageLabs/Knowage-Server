<!-- <md-button ng-click="nextStep()">nextStep 3</md-button> -->

<md-content  layout-column layout-wrap>
<div layout="row" layout-align="end center">
	<md-button ng-click="saveEngineAssociation()" class="md-raised">{{translate.load('Sbi.next','component_impexp_messages');}} </md-button> 
</div>

	<md-whiteframe  layout="row" layout-wrap class="sourceTargetToolbar md-whiteframe-1dp" >
		<p flex="50">{{translate.load('sbi.hierarchies.source');}}</p>
		<p flex="50">{{translate.load('sbi.modelinstances.target');}}</p>
	</md-whiteframe >
	<md-content flex layout="column"  >
	<md-subheader class="md-primary">{{translate.load('SBISet.impexp.exportedEngines','component_impexp_messages');}}</md-subheader>
	
	<md-list   class="centerText" ng-cloak >
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
</md-content>
