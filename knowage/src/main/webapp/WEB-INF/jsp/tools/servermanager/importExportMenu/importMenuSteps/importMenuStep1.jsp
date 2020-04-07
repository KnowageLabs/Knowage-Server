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
<div layout="row" layout-wrap >
   <span flex></span>
   <md-button class="md-raised" ng-if="fileTree.length > 0 || treeInTheDB.length > 0" ng-click="goAhead()" aria-label="upload Menu" >{{translate.load('Sbi.next','component_impexp_messages');}}</md-button>
</div>
<md-content ng-controller="importController">
	<md-whiteframe  layout="row" class="sourceTargetToolbar md-whiteframe-1dp" >
		<p flex="50">{{translate.load('sbi.hierarchies.source');}}</p>
		<p flex="50">{{translate.load('sbi.modelinstances.target');}}</p>
	</md-whiteframe >
	<!-- <md-subheader class="md-primary">{{translate.load('SBISet.impexp.exportedMenu','component_impexp_messages');}}</md-subheader> -->
	<md-subheader class="md-primary"></md-subheader>
	<section>
		<div layout="row"  layout-wrap>
		   <span flex></span>
		   <div flex=50  >
		      <treecontrol 
		         class="tree-classic knowage-theme" 
		         tree-model="fileTree"
		         expanded-nodes="fileTreeExpandedNodes"
		         disabled-nodes="fileTreeDisabledNodes"
		         options="treeOptions"
				>
		         {{node.name}}
		      </treecontrol>
		   </div>
		   <div flex=50>
		      <treecontrol 
		         class="tree-classic knowage-theme" 
		         tree-model="treeInTheDB"
		         expanded-nodes="treeInTheDBExpandedNodes"
		         disabled-nodes="treeInTheDBDisabledNodes"
		         options="treeOptions"
		         >
		         {{node.name}}
		      </treecontrol>
		   </div>
		   <span flex></span>
		</div>
	</section>
</md-content>