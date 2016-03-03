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