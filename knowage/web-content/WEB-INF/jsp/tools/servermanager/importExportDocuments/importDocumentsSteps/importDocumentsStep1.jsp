<!-- <md-button ng-click="nextStep()">nextStep 2</md-button> -->

<md-content layout-padding layout-column layout-wrap>
<div layout="row" layout-align="end center">
	<md-button ng-click="saveRoleAssociation()" class="md-raised">next</md-button> 
</div>

<md-list  ng-cloak ng-init="manageFirstAssociationsRole()">
	<md-list-item ng-repeat="expRol in IEDConf.roles.exportedRoles" class="secondary-button-padding">
   		<p>{{expRol.name}}</p>
   		<p ng-if="IEDConf.roles.associatedRoles[expRol.id].fixed==true">{{IEDConf.roles.associatedRoles[expRol.id].name}}</p>
   		<md-input-container ng-if="IEDConf.roles.associatedRoles[expRol.id].fixed!=true"  flex="50">
	        <label>State</label>
	        <md-select ng-model="IEDConf.roles.associatedRoles[expRol.id]">
	        <md-option ng-value="{id:''}"></md-option>
	          <md-option ng-repeat="currRol in IEDConf.roles.currentRoles" ng-value="currRol" ng-if="currentRoleIsSelectable(currRol,expRol)">
	            {{currRol.name}}
	          </md-option>
	        </md-select>
      </md-input-container>
    <md-divider></md-divider>
  </md-list-item>
</md-list>


</md-content>

