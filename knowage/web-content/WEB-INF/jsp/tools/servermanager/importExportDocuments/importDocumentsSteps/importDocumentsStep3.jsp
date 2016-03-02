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


<!-- <md-button ng-click="nextStep()">nextStep 3</md-button> -->

<md-content  layout-column layout-wrap>
<div layout="row" layout-align="end center">
	<md-button ng-click="saveDatasourceAssociation()" class="md-raised">{{translate.load('Sbi.next','component_impexp_messages');}}</md-button> 
</div>
<md-whiteframe  layout="row" layout-wrap class="sourceTargetToolbar md-whiteframe-1dp" >
		<p flex="50">{{translate.load('sbi.hierarchies.source');}}</p>
		<p flex="50">{{translate.load('sbi.modelinstances.target');}}</p>
	</md-whiteframe >
	<md-content flex layout="column"  >
	<md-subheader class="md-primary">{{translate.load('SBISet.impexp.exportedDS','component_impexp_messages');}}</md-subheader>
	
<md-list class="centerText" ng-cloak >
	<md-list-item ng-repeat="expDatasources in IEDConf.datasources.exportedDatasources" class="secondary-button-padding">
	<div layout="column" layout-wrap flex="50">
   		<span><b>{{expDatasources.label}}</b>
   		<b ng-if="expDatasources.jndi!=undefined && expDatasources.jndi!=''">(jndi)</b>
   		 <b ng-if="!(expDatasources.jndi!=undefined && expDatasources.jndi!='')">(jdbc)</b></span>
   		<span>{{expDatasources.descr}}</span>
   		<span>{{expDatasources.driver}}</span>
   		<span>{{expDatasources.urlConnection}}</span>
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
</md-content>
