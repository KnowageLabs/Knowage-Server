/* Knowage, Open Source Business Intelligence suite
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
*/
(function () {
angular.module('sbiModule')
	.factory('knModule_aggridLabels',
		function(sbiModule_translate) {
			return {
				noRowsToShow: sbiModule_translate.load('kn.table.norows'),
				// Number Filter & Text Filter
			    filterOoo: sbiModule_translate.load('kn.aggrid.filterOoo'),
			    equals: sbiModule_translate.load('kn.aggrid.equals'),
			    notEqual: sbiModule_translate.load('kn.aggrid.notEqual'),
			    empty: sbiModule_translate.load('kn.aggrid.empty'),

			    // Number Filter
			    lessThan: sbiModule_translate.load('kn.aggrid.lessThan'),
			    greaterThan: sbiModule_translate.load('kn.aggrid.greaterThan'),
			    lessThanOrEqual: sbiModule_translate.load('kn.aggrid.lessThanOrEqual'),
			    greaterThanOrEqual: sbiModule_translate.load('kn.aggrid.greaterThanOrEqual'),
			    inRange: sbiModule_translate.load('kn.aggrid.inRange'),
			    inRangeStart: sbiModule_translate.load('kn.aggrid.inRangeStart'),
			    inRangeEnd: sbiModule_translate.load('kn.aggrid.inRangeEnd'),

			    // Text Filter
			    contains: sbiModule_translate.load('sbi.lookup.Contains'),
			    notContains: sbiModule_translate.load('sbi.lookup.NotContains'),
			    startsWith: sbiModule_translate.load('sbi.lookup.StartsWith'),
			    endsWith: sbiModule_translate.load('sbi.lookup.EndsWith')
			}
	});
})();