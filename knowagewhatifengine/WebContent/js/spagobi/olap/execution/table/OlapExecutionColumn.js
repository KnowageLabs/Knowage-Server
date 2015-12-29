/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * The column Dimension..
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.execution.table.OlapExecutionColumn', {
	extend: 'Sbi.olap.execution.table.OlapExecutionAxisDimension',

	config:{
		cls: "x-column-header",
		bodyStyle: "background-color: transparent",
		style: "margin-right: 3px;"
	},

	subPanelLayout: "hbox",

	/**
	 * the type of the axis
	 * @props {String} axisType
	 */
	axisType:  "column",

	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.execution && Sbi.settings.olap.execution.table && Sbi.settings.olap.execution.table.OlapExecutionColumn) {
			Ext.apply(this, Sbi.settings.olap.execution.OlapExecutionColumn);
		}

		if(!this.firstDimension){
			this.cls = this.cls+" internal-column-header";
		}

		this.callParent(arguments);
	},

	/**
	 * Builds the central panel with the name of the Dimension
	 */
	buildDimensionPanel: function(){
		var thisPanel = this;
		this.dimensionPanel = Ext.create("Ext.Panel",{
			border: false,
			html: this.getDimensionName()+"&nbsp;",//to avoid line breaks
			style: "background-color: transparent !important;",
			bodyStyle: "background-color: transparent !important",
			listeners: {
				el: {
					click: {
						fn: function(){
							thisPanel.fireEvent("dimensionClick", thisPanel.dimension);
						}
					}
				}
			}

		});
	}

});