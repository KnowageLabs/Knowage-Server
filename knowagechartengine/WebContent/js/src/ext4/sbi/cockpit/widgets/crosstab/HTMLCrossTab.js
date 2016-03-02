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







/**
  * Object name
  *
  * [description]
  *
  *
  * Public Properties
  *
  * [list]
  *
  *
  * Public Methods
  *
  *  [list]
  *
  *
  * Public Events
  *
  *  [list]
  *
  * Authors
  *
  * - Alberto Ghedin (alberto.ghedin@eng.it)
  */

//================================================================
//CrossTab
//================================================================
//
//The cross tab is a grid with headers and for the x and for the y.
//it's look like this:
//       ----------------
//       |     k        |
//       ----------------
//       |  y  |  x     |
//       ----------------
//       |y1|y2|x1|x2|x3|
//-----------------------
//| | |x1|  |  |  |  |  |
//| | |------------------
//| |x|x2|  |  |  |  |  |
//| | |------------------
//|k| |x3|  |  |  |  |  |
//| |--------------------
//| | |y1|  |  |  |  |  |
//| |y|------------------
//| | |y2|  |  |  |  |  |
//-----------------------
//
//The grid is structured in 4 panels:
//         -----------------------------------------
//         |emptypanelTopLeft|    columnHeaderPanel|
// table=  -----------------------------------------
//         |rowHeaderPanel   |    datapanel        |
//         -----------------------------------------

Ext.define('Sbi.cockpit.widgets.crosstab.HTMLCrossTab', {
	extend: 'Ext.panel.Panel',

	statics: {
		sort: function(column, axis, globalId){
			var config = Sbi.cockpit.widgets.crosstab.globalConfigs[globalId].sortOptions;
			var axisConfig;
			if(axis==1){
				if(!config.columnsSortKeys){
					config.columnsSortKeys={};
				}
				axisConfig = config.columnsSortKeys;
			}else{
				if(!config.rowsSortKeys){
					config.rowsSortKeys={};
				}
				axisConfig = config.rowsSortKeys;
			}

			var direction = axisConfig[column];
			if(!direction){
				direction = 1;
			}
			direction = direction*(-1);

			axisConfig[column] = direction;

			Sbi.cockpit.widgets.crosstab.globalConfigs[globalId].sortCrosstab();
		}
	},

	config:{
  		 border: false
  		, autoWidth: true
  		, cls: "widget-crosstab"
  		, widgetContainer: null

	},

	constructor : function(config) {
		this.initConfig(config||{});
		this.layout= 'fit';
		var settings = Sbi.getObjectSettings('Sbi.cockpit.widgets.crosstab.HTMLCrossTab');
		Ext.apply(this, settings);
		this.html = config.htmlData;
		this.callParent(arguments);
	}

});