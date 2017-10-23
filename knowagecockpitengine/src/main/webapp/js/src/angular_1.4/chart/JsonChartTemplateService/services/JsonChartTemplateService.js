/**
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


angular.module('JsonChartTemplateServiceModule')

.factory('jsonChartTemplate',function(sbiModule_restServices,$q,$httpParamSerializer){
	
	
	var config = {
			headers:{'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8'},
	
			transformResponse: function(obj) {
	
				obj = obj.replace(new RegExp("&#39;",'g'),"\\'");
				return obj;
			}
	
	}
	return{
		
		readChartTemplateForCockpit:function(jsonTemplate,exportWebData,datasetLabel,jsonData){
			
			var params = {};
			params.jsonTemplate = jsonTemplate;
			params.exportWebData = exportWebData;
			params.datasetLabel = datasetLabel;
			params.jsonData = jsonData;
			
			
			if(jsonTemplate && jsonTemplate.CHART){
				jsonTemplate.CHART.outcomingEventsEnabled = true;
			}
			
			 var deferred = $q.defer();
			sbiModule_restServices
    		.promisePost('1.0/chart/jsonChartTemplate/readChartTemplateForCockpit', '',$httpParamSerializer(params),config)
    		
        	.then
        	(
        			function(response) { 
        				deferred.resolve(response.data);
        			},
	        		
        			function(response) { 
	    				console.log('Error!!!')
    				}
    			
        	);
			
			 return deferred.promise
		},
			
		readChartTemplate:function(jsonTemplate,exportWebData,datasetLabel,jsonData){
			
			var params = {};
			if( (jsonTemplate.CHART.groupCategories || jsonTemplate.CHART.groupSeries || jsonTemplate.CHART.groupSeriesCateg) && jsonTemplate.CHART.VALUES.CATEGORY.groupby!=""){
				var arrayOfCateg = [];
				arrayOfCateg.push(jsonTemplate.CHART.VALUES.CATEGORY)
				 if (jsonTemplate.CHART.VALUES.CATEGORY.groupby.indexOf(',') == -1) { 
						subs = jsonTemplate.CHART.VALUES.CATEGORY.groupby ;
					}
					
					else {
						subs = angular.copy(jsonTemplate.CHART.VALUES.CATEGORY.groupby.substring(0, jsonTemplate.CHART.VALUES.CATEGORY.groupby.indexOf(',')));
					}
				var groupby = {};
				groupby['column'] = subs;
				groupby['groupby'] = "";
				groupby['name'] = subs;
				groupby['groupbyNames'] = "";
				groupby['orderColumn'] = jsonTemplate.CHART.VALUES.CATEGORY.orderColumn;
				groupby['orderType'] =jsonTemplate.CHART.VALUES.CATEGORY.orderType;;
				groupby['stackedType'] = jsonTemplate.CHART.VALUES.CATEGORY.stackedType;
				groupby['stacked'] =  jsonTemplate.CHART.VALUES.CATEGORY.stacked;
				arrayOfCateg.push(groupby);
				delete jsonTemplate.CHART.VALUES.CATEGORY;
				 jsonTemplate.CHART.VALUES.CATEGORY = arrayOfCateg;
			}
		
			params.jsonTemplate = jsonTemplate;
			params.exportWebData = exportWebData;
			params.datasetLabel = datasetLabel;
			params.jsonData = jsonData;
			
			
			if(jsonTemplate && jsonTemplate.CHART){
				jsonTemplate.CHART.outcomingEventsEnabled = true;
			}
			
			 var deferred = $q.defer();
			sbiModule_restServices
    		.promisePost('1.0/chart/jsonChartTemplate/readChartTemplate', '?SBI_EXECUTION_ID='+sbiExecutionId,$httpParamSerializer(params),config)
    		
        	.then
        	(
        			function(response) { 
        				deferred.resolve(response.data);
        			},
	        		
        			function(response) { 
	    				console.log('Error!!!')
    				}
    			
        	);
			
			 return deferred.promise
		},
		
		drilldownHighchart:function(breadcrumb){
			
			var params = {};
			
			params.breadcrumb = breadcrumb;
			
			
			
			
			 var deferred = $q.defer();
			sbiModule_restServices
    		.promisePost('1.0/chart/jsonChartTemplate/drilldownHighchart', '?SBI_EXECUTION_ID='+sbiExecutionId, $httpParamSerializer(params),config)
    		
        	.then
        	(
        			function(response) { 
        				deferred.resolve(eval("(" + response.data + ")"));
        			},
	        		
        			function(response) { 
	    				console.log('Error!!!')
    				}
    			
        	);
			
			 return deferred.promise
		}
	}
	
})