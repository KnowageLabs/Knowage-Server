<!-- <md-button ng-click="nextStep()">nextStep 2</md-button> -->

<md-content  layout="column" layout-wrap>
	<div layout="row" layout-align="end center">
		<md-button ng-click="saveRoleAssociation()" ng-if="IEDConf.importPersonalFolder" class="md-raised">{{translate.load('Sbi.next','component_impexp_messages');}}</md-button> 
		<md-button class="md-raised" ng-if="!IEDConf.importPersonalFolder" ng-click="saveRoleAssociation()" aria-label="upload Users" >{{translate.load("sbi.importusers.startimport");}}</md-button>
		
	</div>
	<md-whiteframe  layout="row" layout-wrap class="sourceTargetToolbar md-whiteframe-1dp" >
		<p flex="50">{{translate.load('sbi.hierarchies.source');}}</p>
		<p flex="50">{{translate.load('sbi.modelinstances.target');}}</p>
	</md-whiteframe >
	<md-content flex layout="column"  >
	<md-subheader class="md-primary">{{translate.load('SBISet.impexp.exportedRoles','component_impexp_messages');}}</md-subheader>
	<md-list class="centerText" ng-cloak >
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
</md-content>

