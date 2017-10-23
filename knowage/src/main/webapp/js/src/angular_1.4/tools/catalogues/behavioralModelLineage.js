/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

var app= angular.module('BMLModule',['ngMaterial','sbiModule','sbi-containerModule','table-containerModule']);

app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);

app.controller('BMLController', ['$scope', function($scope) {
	
	$scope.tables =[
	                
	                {	name:'Lovs',
	                	dataPath:'2.0/lovs/get/all',
	                	selectedEvent:'lov',
	                	broadcasts: [{event:'refresh',handler:function(event,data){event.currentScope.refresh()}},
	                	             {event:'adriver',handler:function(event,data){event.currentScope.getData('2.0/analyticalDrivers/'+data.id+'/lovs')}},
	                	             {event:'document',handler:function(event,data){event.currentScope.getData('1.0/documents/'+data.label+'/lovs')}}
	                	             ]
	                },
	                {	name:'Analitical Drivers',
						dataPath:'2.0/analyticalDrivers',
						selectedEvent:'adriver',
						broadcasts: [{event:'refresh',handler:function(event,data){event.currentScope.refresh()}},
						             {event:'lov',handler:function(event,data){event.currentScope.getData('2.0/lovs/'+data.id+'/analyticalDrivers')}},
						             {event:'document',handler:function(event,data){event.currentScope.getData('1.0/documents/'+data.label+'/analyticalDrivers')}}
						             ]
						
					},
	               
	                
	                {	name:'Documents',
	                	dataPath:'2.0/documents',
	                	selectedEvent:'document',
	                	broadcasts: [{event:'refresh',handler:function(event,data){event.currentScope.refresh()}},
	                	             {event:'adriver',handler:function(event,data){event.currentScope.getData('2.0/analyticalDrivers/'+data.id+'/documents')}},
	                	             {event:'lov',handler:function(event,data){event.currentScope.getData('2.0/lovs/'+data.id+'/documents')}}
	                	             ]
	                }	
	                //{name:'Datasets',dataPath:'1.0/datasets'}
    
    ];
}]);
