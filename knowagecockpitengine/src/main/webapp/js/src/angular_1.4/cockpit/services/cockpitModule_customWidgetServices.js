/*
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
 */

/**
 * @authors Radmila Selakovic (radmila.selakovic@eng.it)
 *
 */
angular.module("cockpitModule").factory("datastore",function($filter,cockpitModule_datasetServices,sbiModule_util, datastoreService){
	
	var transformDataStore = function (datastore){
		var newDataStore = {};
		newDataStore.metaData = datastore.metaData;
		newDataStore.results = datastore.results;
		newDataStore.rows = [];

		for(var i=0; i<datastore.rows.length; i++){
			var obj = {};
			for(var j=1; j<datastore.metaData.fields.length; j++){
				if(datastore.rows[i][datastore.metaData.fields[j].name]!=undefined){
					obj[datastore.metaData.fields[j].header] = datastore.rows[i][datastore.metaData.fields[j].name];
				}
			}
			newDataStore.rows.push(obj);
		}
		return newDataStore;
	}
		
	function datastore(data) {
		this.data = data || transformDataStore(datastoreService.datastore6);
	}
	
	datastore.prototype.getDataArray = function (getDataArrayFn){
		var dataArray = [];
		for(var i=0; i<this.data.rows.length; i++){
			var dataObj = getDataArrayFn(this.data.rows[i]);
			dataArray.push(dataObj)
		}
		return dataArray;
	}
	
	datastore.prototype.getColumn = function (column){
		var categArray = [];
		for(var i=0; i<this.data.rows.length; i++){
			var dataObj = this.data.rows[i][column];
			categArray.push(dataObj)
		}
		return categArray;
	}
	
	datastore.prototype.getSeriesAndData = function (getDataArrayFn,column){
		var seriesMap = {};
		for(var i=0; i<this.data.rows.length; i++){
			if(seriesMap[this.data.rows[i][column]]==undefined){
				seriesMap[this.data.rows[i][column]] = []
			}
			seriesMap[this.data.rows[i][column]].push(getDataArrayFn(this.data.rows[i]))
		}
		var series = []
		for (var property in seriesMap) {
			var serieObj = {};
			serieObj.name = property;
			serieObj.id = property;
			serieObj.data = seriesMap[property];
			series.push(serieObj);
		}
		return series;
	}
	
	datastore.prototype.sort = function(sortingObject){
		var newData = angular.copy(this.data);
		newData.rows = $filter('orderBy')(newData.rows, sortingObject);
		return new datastore(newData);
	},
	
	datastore.prototype.filter = function(filterObject){
		var newData = angular.copy(this.data);
		newData.rows = $filter('filter')(newData.rows, filterObject);
		return new datastore(newData);
	}
	
	return new datastore;

//	return function datastore(){
//		// TODO insert correct datastore linkage here
//		var data = transformDataStore(datastoreService.datastore6);
//	  
//		return {
//			getDataArray: function (getDataArrayFn){
//				var dataArray = [];
//				for(var i=0; i<data.rows.length; i++){
//					var dataObj = getDataArrayFn(data.rows[i]);
//					dataArray.push(dataObj)
//				}
//				return dataArray;
//			},
//			
//			getColumn: function (column){
//				var categArray = [];
//				for(var i=0; i<data.rows.length; i++){
//					var dataObj = data.rows[i][column];
//					categArray.push(dataObj)
//				}
//				return categArray;
//			},
//			
//			getSeriesAndData: function (getDataArrayFn,column){
//				var seriesMap = {};
//				for(var i=0; i<data.rows.length; i++){
//					if(seriesMap[data.rows[i][column]]==undefined){
//						seriesMap[data.rows[i][column]] = []
//					}
//					seriesMap[data.rows[i][column]].push(getDataArrayFn(data.rows[i]))
//				}
//				var series = []
//				for (var property in seriesMap) {
//					var serieObj = {};
//					serieObj.name = property;
//					serieObj.id = property;
//					serieObj.data = seriesMap[property];
//					series.push(serieObj);
//				}
//				return series;
//			},
//			
//			sort: function(sortingObject){
//				// {name:"M", phone:"1"}
//				data.rows = $filter('orderBy')(data.rows, sortingObject);
//				return this
//			},
//			
//			filter: function(filterObject){
//				// {name:"M", phone:"1"}
//				data.rows = $filter('filter')(data.rows, filterObject);
//				return this
//			}
//		}
//	}
});