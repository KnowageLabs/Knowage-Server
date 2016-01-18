<!-- <md-button ng-click="nextStep()">nextStep 2</md-button> -->

<md-content layout-padding>
<md-list  ng-cloak>
	<md-list-item ng-repeat="expRol in IEDConf.roles.exportedRoles" class="secondary-button-padding">
   		<p>{{expRol.name}}</p>
   		
   		<md-input-container  flex="50">
	        <label>State</label>
	        <md-select ng-model="selaa">
	          <md-option ng-repeat="currRol in IEDConf.roles.currentRoles" value="{{currRol.name}}">
	            {{currRol.name}}
	          </md-option>
	        </md-select>
      </md-input-container>
    <md-divider></md-divider>
  </md-list-item>
  
</md-list>
</md-content>

