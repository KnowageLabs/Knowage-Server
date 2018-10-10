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


<md-content  layout="column" layout-wrap>
	
	<md-radio-group layout="row" ng-model="IEDConf.conflictsAction">
		<md-radio-button value="overwrite">Overwrite</md-radio-button>
		<md-radio-button value="keep">Keep</md-radio-button>
		<md-radio-button value="abort">Abort</md-radio-button>
	</md-radio-group>
	
	<div layout="row" layout-wrap  layout-align="end center">
		<md-input-container class="small counter"> 
			<md-button ng-click="save($event)" aria-label="Start import" >{{translate.load("sbi.importkpis.startimport");}}</md-button>
		</md-input-container>
	</div>
	<!--md-checkbox ng-model="IEDConf.overwriteMode" aria-label="Overwrite">Overwrite existing METADATAs and rules</md-checkbox>
	<md-checkbox ng-model="IEDConf.targetsAndRelatedKpis" aria-label="Import targets and related METADATAs">Import targets and related METADATAs</md-checkbox>
	<md-checkbox ng-model="IEDConf.scorecardsAndRelatedKpis" aria-label="Import scorecards and related METADATAs">Import scorecards and related METADATAs</md-checkbox>
	<md-checkbox ng-model="IEDConf.schedulersAndRelatedKpis" aria-label="Import METADATA schedulers and related METADATAs">Import METADATA schedulers and related METADATAs</md-checkbox-->
	<!--div layout="row" flex>
		<div flex style="position: relative;" >
			<angular-table id="layerlist"
				ng-show="IEDConf.exportedKpis.length!=0"
				ng-model="IEDConf.exportedKpis"
				columns="[{'label':'ID','name':'id'},{'label':'Name','name':'name'},{'label':'Formula','name':'formula'},{'label':'Threshold','name':'thresholdName'},{'label':'Targets','name':'targetsNames'}]"
				columnsSearch="['userId']" 
				show-search-bar="false"
				highlights-selected-item="true"
				hide-table-head="false"
				menu-option="menuLayer"
				multi-select="true"
				selected-item="IEDConf.roles.selectedKpis"
				no-pagination="true"
				scope-functions="tableFunction">
			</angular-table>
		</div-->
	</div>
</md-content>
