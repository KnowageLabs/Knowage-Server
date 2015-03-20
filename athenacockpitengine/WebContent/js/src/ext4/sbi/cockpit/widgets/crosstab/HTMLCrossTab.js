/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/







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