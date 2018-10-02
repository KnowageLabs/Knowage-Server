
/*
 * Knowage, Open Source Business Intelligence suite
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
    angular.module('RegistryDocument')
    .factory('registryPaginationService', function() {


    			var pagination = {};
    			pagination.getTotalPages = function(resultNumber,itemsPerPage) {
    	            return new Array(Math.ceil(resultNumber / itemsPerPage));
    	        };

    	        pagination.hasNext = function(page,itemsPerPage,resultNumber) {
    	            return page * itemsPerPage < resultNumber;
    	        };

    	        pagination.hasPrevious = function(page) {
    	            return page > 1;
    	        };

    	        pagination.min = function(resultsNumber,page,itemsPerPage) {
    	            return resultsNumber > 0 ? (page - 1) * itemsPerPage + 1 : 0;
    	        };

    	        pagination.max = function(page,itemsPerPage,resultNumber) {
    	            return pagination.hasNext(page,itemsPerPage,resultNumber) ? (page * itemsPerPage) : resultNumber;
    	        };


    	        pagination.next = function(page,formParams,itemsPerPage,filters) {

    	        	formParams.start = (page - 1) * itemsPerPage;
    	        	formParams.limit = itemsPerPage;
    	        	var filterFields = Object.keys(filters);
    	        	var filterValues = Object.values(filters);

    	        	for(var i = 0; i < filterFields.length; i++) {
    	        		formParams[filterFields[i]] = filterValues[i];
    	        	}
    	        	return formParams;
    	        };

    	        pagination.previous = function(page,formParams,itemsPerPage,filters) {

    	        	formParams.start = (page - 1) * itemsPerPage;
    	        	formParams.limit = itemsPerPage;
    	        	var filterFields = Object.keys(filters);
    	        	var filterValues = Object.values(filters);

    	        	for(var i = 0; i < filterFields.length; i++) {
    	        		formParams[filterFields[i]] = filterValues[i];
    	        	}
    	        	return formParams;
    	        };

    	        pagination.goToPage = function(page,formParams,itemsPerPage,filters) {
    	        	formParams.start = (page - 1) * itemsPerPage;
    	        	formParams.limit = itemsPerPage;
    	        	var filterFields = Object.keys(filters);
    	        	var filterValues = Object.values(filters);

    	        	for(var i = 0; i < filterFields.length; i++) {
    	        		formParams[filterFields[i]] = filterValues[i];
    	        	}
    	        	return formParams;
    	        };

    	        return pagination;

    	   	   });
})();