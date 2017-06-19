<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<md-content  layout-column layout-wrap>
<div layout="row" layout-align="end center">
	<md-button ng-click="associateddatasource($event)" class="md-raised" ng-disabled="checkRolesAssociated()">{{translate.load('sbi.generic.next');}}</md-button> 
</div>
<md-whiteframe ng-if="showRoles"  layout="row" layout-wrap class="sourceTargetToolbar md-whiteframe-1dp" >
		<p flex="50">{{translate.load('sbi.hierarchies.source');}}</p>
		<p flex="50">{{translate.load('sbi.modelinstances.target');}}</p>
</md-whiteframe >
<md-content flex layout="column"  >

<md-subheader ng-if="showRoles"  class="md-primary">{{translate.load('sbi.impexp.exportedRole');}}</md-subheader>
<md-subheader ng-if="!showRoles"  class="md-primary">{{translate.load('sbi.impexp.noExportedRole');}}</md-subheader>
	
<md-list ng-cloak > 
<!-- <md-list class="centerText" ng-cloak >  -->   
	<md-list-item ng-repeat="expRoles in IEDConf.roles.exportedRoles" class="secondary-button-padding">
	<div layout="column" layout-wrap flex="50">
   		<span><b>{{expRoles.name}}</b>
	</div>

  	<p ng-if="IEDConf.roles.associatedRoles[expRoles.id].fixed==true">{{IEDConf.roles.associatedRoles[expRoles.id].name}}</p>
  	<md-input-container ng-if="IEDConf.roles.associatedRoles[expRoles.id].fixed!=true"  flex="50">  
        <md-select ng-model="IEDConf.roles.associatedRoles[expRoles.id]">
        <md-option ng-value="{id:''}"></md-option>
          <md-option ng-repeat="currRoles in IEDConf.roles.currentRoles" ng-value="currRoles" ng-selected="IEDConf.roles.currentRoles.length==1" >
            {{currRoles.name}}
          </md-option>
        </md-select>
     </md-input-container>

    <md-divider></md-divider>
  </md-list-item>
</md-list>
</md-content>
</md-content>
