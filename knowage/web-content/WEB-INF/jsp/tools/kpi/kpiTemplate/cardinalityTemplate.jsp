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


<md-content layout-fill layout="column" ng-controller="kpiDefinitionCardinalityController">
<md-card >
<div class="toolbarFormula">
	<div class="formula " id="formulaId"></div>
</div>
</md-card>

<div flex class="overflow" >	
	<table  class="cardinalityTable  " >
	<thead>
		<tr>
			<th></th>
		    <th ng-mouseover="blinkMeasure($event,'',$index)" ng-mouseleave="removeblinkMeasure()" ng-repeat="measure in cardinality.measureList" ><div>{{measure.measureName}}</div></th>
	    </tr>
	  </thead>
	  <tbody>
	  <tr id="trFirst"></tr>
		<tr class="attributeRow" ng-repeat="attr in attributesList">
		<td ng-class ="{classBold:currentCell.row==attr}">{{attr}}</td>
			<td ng-mouseover="blinkMeasure($event,attr,$index)" ng-mouseleave="removeblinkMeasure()" 
			ng-repeat="measure in cardinality.measureList" >
			<div class="measureCell" ng-if="measureHaveAttribute(attr,measure)"
			 ng-click="toggleCell(attr,measure)"  ">
			<i ng-if="!isEnabled(attr,measure)" class="fa fa-ban invalidCell"></i>
			<i ng-if="measure.attributes[attr]" class="fa fa-check selectedCell"></i>
			<i ng-if="(measure.attributes[attr] && !canDisable(attr,measure))" class="fa fa-lock selectedCell"></i>
			<i ng-if="!measure.attributes[attr] && isEnabled(attr,measure)" class="fa fa-check selectableCell"></i>
			</div>
			</td>
		</tr>
		</tbody>
	</table>
</div>
</md-content>
