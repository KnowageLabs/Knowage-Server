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
(function(){
	angular.module('formatModule').service('formatDate',function($mdDateLocale){

		 var dateFormats = ['LLLL','llll','LLL','lll','DD/MM/YYYY HH:mm:SS','DD/MM/YYYY HH:mm','LL','ll','L','l']

		 var formatMapping = {
				 "dd/MM/yyyy" : "DD/MM/YYYY",
				 "dd/MM/yyyy HH:mm:ss.SSS" : "DD/MM/YYYY HH:mm:ss.SSS",
				 "HH:mm:ss.SSS" : "HH:mm:ss"
		 }

		 var contains = function(array,item){
			 for(var i in array){
				 if(array[i] === item){
					 return true
				 }
			 }

			 return false;
		 }

		 var format = function(date,format,dateFormatJava){

			 if(formatMapping[dateFormatJava] && contains(dateFormats,format)){
				 return $mdDateLocale.formatDate($mdDateLocale.parseDate(date,formatMapping[dateFormatJava]),format)
			 }

			 return date;



		 }

		 return {
			 dateFormats : dateFormats,
			 format : format,
		 }

	})
})()
