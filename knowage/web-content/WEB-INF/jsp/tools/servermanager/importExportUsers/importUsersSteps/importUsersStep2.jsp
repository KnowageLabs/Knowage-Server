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
	
	<div layout="row" layout-wrap  >
		<div flex >
			<md-radio-group layout="row" ng-model="IEDConf.typeSaveUser">
		      <md-radio-button value="Override" >{{translate.load("sbi.importusers.override");}}</md-radio-button>
		      <md-radio-button value="Missing">{{translate.load("sbi.importusers.addmissing");}} </md-radio-button>
		    </md-radio-group>
		</div>
		<md-checkbox flex   style="line-height: 61px;"  ng-model="IEDConf.importPersonalFolder" aria-label="Checkbox 1">{{translate.load("sbi.impexpusers.importPersonalFolder")}}</md-checkbox>
					
		<md-input-container class="small counter"> 
			<md-button ng-click="save($event)" aria-label="upload Users" >{{translate.load("sbi.importusers.startimport");}}</md-button>
		</md-input-container>
		
	</div>
	
	<md-whiteframe  layout="row" layout-wrap class="sourceTargetToolbar md-whiteframe-1dp" >
		<p flex="45">{{translate.load('sbi.hierarchies.source');}}</p>
		<span flex></span>
		<p flex="45">{{translate.load('sbi.modelinstances.target');}}</p>
	</md-whiteframe >
	 
	<div layout="row" flex>
			<div flex style="position: relative;" >
				 <angular-table ng-show="IEDConf.roles.exportedRoles.length!=0" id='layerlist' 
					ng-model=IEDConf.roles.exportedRoles
					columns='[{"label":"","name":"name"}]'
					columnsSearch='["name"]' 
					show-search-bar=true
					highlights-selected-item=true 
					hide-table-head=true
					menu-option=menuLayer 
					multi-select=true
					selected-item=IEDConf.roles.selectedRoles
					no-pagination=true
					scope-functions=tableFunction>
					</angular-table> 
					
			</div>
			<div layout="column" layout-wrap  >
			<div flex></div>
				 	<md-button  class="md-fab md-mini"  ng-click="addUser()"><md-icon
						md-font-icon="fa fa-angle-right fa-2x"  >
					</md-icon></md-button>
				 	<md-button  class="md-fab md-mini"  ng-click="removeUser()" ><md-icon
						md-font-icon="fa fa-angle-left fa-2x" >
					</md-icon></md-button>
				<div flex></div>
				 	<md-button  class="md-fab md-mini"  ng-click="addAllUser()" ><md-icon
						md-font-icon="fa fa-angle-double-right fa-2x"  >
					</md-icon></md-button>
				 	<md-button class="md-fab md-mini"  ng-click="removeAllUser()"><md-icon
						md-font-icon="fa fa-angle-double-left fa-2x"  >
					</md-icon></md-button>
				<div flex></div>
			 </div>
			<div flex style="position: relative;" >
	
				<angular-table ng-show="IEDConf.roles.currentRoles.length!=0" id='layerlist2' 
				ng-model=IEDConf.roles.currentRoles
				columns='[{"label":"","name":"name"}]'
				columnsSearch='["name"]' 
				show-search-bar=true
				highlights-selected-item=true 
				menu-option=menuLayer 
				multi-select=true
				selected-item=IEDConf.roles.selectedRoles
				no-pagination=true
				scope-functions=tableFunction
				hide-table-head=true>
			
				
			</div>
	</div>

</md-content>
