<%--
	Knowage, Open Source Business Intelligence suite
	Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.
	
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
<div layout="row" layout-align="end center">
	<md-button class="md-raised" ng-click="save($event)" aria-label="upload Menu" >{{translate.load("sbi.importusers.startimport");}}</md-button>
</div>
<md-content ng-controller="importController">
	<md-whiteframe  layout="row" class="sourceTargetToolbar md-whiteframe-1dp" >
		<p flex="50">{{translate.load('sbi.hierarchies.source');}}</p>
		<p flex="50">{{translate.load('sbi.modelinstances.target');}}</p>
	</md-whiteframe >
	<md-subheader class="md-primary">{{translate.load('SBISet.impexp.exportedRoles','component_impexp_messages');}}</md-subheader>
	<section>
		<md-list class="centerText" ng-cloak >
			<md-list-item ng-repeat="expRol in IEDConf.roles.exportedRoles" class="secondary-button-padding">
				<p>{{expRol.name}}</p>
				<p ng-if="IEDConf.roles.associatedRoles[expRol.id].fixed==true">{{IEDConf.roles.associatedRoles[expRol.id].name}}</p>
				<md-input-container ng-if="IEDConf.roles.associatedRoles[expRol.id].fixed!=true"  flex="50">
					<label>Role</label>
					<md-select ng-model="IEDConf.roles.associatedRoles[expRol.id]">
						<md-option ng-value="{id:''}"></md-option>
						<md-option ng-repeat="currRol in IEDConf.roles.currentRoles" ng-value="currRol.id" ng-if="currentRoleIsSelectable(currRol,expRol)">
		            		{{currRol.name}}
		            	</md-option>
					</md-select>
				</md-input-container>
				<md-divider></md-divider>
			</md-list-item>
		</md-list>
	</section>
</md-content>