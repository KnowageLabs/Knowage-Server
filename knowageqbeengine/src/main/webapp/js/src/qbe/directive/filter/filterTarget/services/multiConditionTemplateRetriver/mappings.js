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
	angular.module('targetApp').service('mappings',function(parsingHelper){

		var mappings = {};
		var parsingHelper = parsingHelper;
		var init = function(){
			register(['manual'],'manual',mappings)
			register(['parameter'],'parameter',mappings)
			register(['valueoffield'],'valueoffield',mappings)
			register(['anotherentity'],'anotherentity',mappings)
			register(['subquery'],'subqueries',mappings)
			register(['java.sql.date','manual'],'date',mappings)
			register(['java.sql.time','manual'],'time',mappings)
			register(['java.sql.timestamp','manual'],'timestamp',mappings)
			register(['between','java.sql.date','manual'],'multidate',mappings)
			register(['notbetween','java.sql.date','manual'],'multidate',mappings)
			register(['between','java.sql.time','manual'],'multitime',mappings)
			register(['notbetween','java.sql.time','manual'],'multitime',mappings)
			register(['between','java.sql.timestamp','manual'],'multitimestamp',mappings)
			register(['notbetween','java.sql.timestamp','manual'],'multitimestamp',mappings)

			register(['default'],'manual',mappings)

			register(['in','manual'],'multimanual',mappings)
			register(['notin','manual'],'multimanual',mappings)

			register(['between','manual'],'betweenmanual',mappings)
			register(['notbetween','manual'],'betweenmanual',mappings)

		}

		this.get = function(){
			return mappings;
		}

		var register = function(properties,value,obj){
			parsingHelper.set(properties,obj,value)

		}

		init()
	})

})()
