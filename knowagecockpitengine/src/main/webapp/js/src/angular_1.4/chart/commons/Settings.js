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

/**
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */

angular
	.module("chartengine.settings", [])
	.service
	(
		"chartEngineSettings",
		
		function() {
			
			// All ChartEngine common setting properties are going to be placed here. (danristo)
			var settings = {
				
				// Not all properties are transfered to the this (new) Setting.js file from the old one. (danristo)
					
				parallel:
				{
					tooltip:
					{
						/**
						 * This parameter is used for the threshold that is used for
						 * determining if the text on the PARALLEL's tooltip is going
						 * to be black (when the tooltip's background is lighter color)
						 * or white (when the background is too dark).
						 */
						darknessThreshold: 0.7
					}
				},
			
			tree_D_Options:
			{
				 alpha: 25,
	             beta: 15,
	             depth: 50,
	             viewDistance: 25
			}
					
			};
			
			return settings;	
			
		}
	);