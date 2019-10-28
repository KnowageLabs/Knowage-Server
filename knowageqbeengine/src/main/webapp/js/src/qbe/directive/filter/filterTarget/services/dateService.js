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
	angular.module('targetApp').service('dateService',function(){


		this.getDate = function(dateString){
			return this.getFullDate(dateString)[0]
		}

		this.getMonth = function(dateString){
			return this.getFullDate(dateString)[1]-1
		}

		this.getYear = function(dateString){
			return this.getFullDate(dateString)[2]
		}

		this.getFullDateHour = function(dateString){
			return this.getFullDate(dateString)[3]
		}

		this.getFullDateMinutes = function(dateString){
			return this.getFullDate(dateString)[4]
		}

		this.getFullDate = function(str){
			if(str){
				var regString = "[0-9]{1,4}"
					var regex = new RegExp(regString,"g");
					return str.match(regex, str);
			}

		}

		this.getHour = function(timeString){
			return this.getTime(timeString)[0]
		}



		this.getMinutes = function(timeString){
			return this.getTime(timeString)[1]
		}

		this.getTime = function(str){
			if(str){
				var regString = "[0-9]{1,2}"
					var regex = new RegExp(regString,"g");
					return str.match(regex, str);
			}

		}





	})
})()
