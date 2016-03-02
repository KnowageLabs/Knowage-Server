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


<md-content layout-fill ng-controller="kpiDefinitionCardinalityController">
	<table flex class="cardinalityTable" >
		<tr>
			<th></th>
		    <th ng-repeat="measure in measureList" >{{measure.measureName}}</th>
	    </tr>
	  
		<tr class="attributeRow" ng-repeat="attr in attributesList">
		<td>{{attr}}</td>
			<td ng-repeat="measure in measureList">
			<div class="measureCell" ng-if="!measureHaveAttribute(attr,measure)">
<!-- 			<i class="fa fa-times invalidCell "></i> -->
			</div>
			<div class="measureCell" ng-if="measureHaveAttribute(attr,measure)"
			 ng-click="toggleCell(attr,measure)"  ">
			<i ng-if="!isEnabled(attr,measure)" class="fa fa-ban invalidCell"></i>
			<i ng-if="measure.attributs[attr]" class="fa fa-check selectedCell"></i>
			<i ng-if="(measure.attributs[attr] && !canDisable(attr,measure))" class="fa fa-lock selectedCell"></i>
			<i ng-if="!measure.attributs[attr] && isEnabled(attr,measure)" class="fa fa-check selectableCell"></i>
			</div>
			</td>
		</tr>
	</table>


</md-content>
