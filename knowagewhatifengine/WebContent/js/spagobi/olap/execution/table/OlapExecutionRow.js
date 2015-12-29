/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * The row dimension
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.execution.table.OlapExecutionRow', {
	extend: 'Sbi.olap.execution.table.OlapExecutionAxisDimension',

	config:{
		style: "margin-bottom: 3px;",
		cls: "x-column-header",
		bodyStyle: "background-color: transparent",
		roundText: true
	},

	/**
	 * the type of the axis
	 * @props {String} axisType
	 */
	axisType:  "row",

	subPanelLayout: "auto",

	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.execution && Sbi.settings.olap.execution.table && Sbi.settings.olap.execution.table.OlapExecutionRow) {
			Ext.apply(this, Sbi.settings.olap.execution.OlapExecutionRow);
		}
		this.roundText = this.roundText && (Ext.isChrome);

		if(!this.firstDimension){
			this.cls = this.cls+" internal-row-header";
		}

		this.callParent(arguments);
	},


	/**
	 * Builds the central panel with the name of the dimension
	 */
	buildDimensionPanel: function(){
		var thisPanel = this;
		var dimensionConf =  {
				xtype: "panel",
				border: false,

				bodyStyle: "background-color: transparent; white-space: nowrap",
				style: "background-color: transparent",
				html: this.getText(),
				listeners: {
					el: {
						click: {
							fn: function(){
								thisPanel.fireEvent("dimensionClick", thisPanel.dimension);
							}
						}
					}
				}
		};
		if(this.roundText){
			dimensionConf.height = this.getDimensionName().length*6.3+4;
			dimensionConf.cls= "rotate";
		}

		this.dimensionPanel = Ext.create("Ext.Panel",dimensionConf);
	},

	/**
	 * Gets the text to show in the panel
	 * If the browser is IE the text is from left to right and a <br> follow every chararct.
	 * If the browser is not IE the text is rounded via CSS
	 */
	getText: function(){
		var startItalic= 0;
		if(!this.roundText){
			var text ="";
			var n = this.getDimensionName();
			startItalic = n.indexOf('<i>');
			if(startItalic>0){
				n = n.replace('<i>','');
				n= n.replace('</i>','');
			}else{
				startItalic = 100;
			}
			if(n){
				for(var i=0; i<n.length; i++){
					var chararct =n.charAt(i);
					if(i>=startItalic){
						chararct = '<i>'+chararct+'</i>';
					}
					text = text + chararct +'<br>';
					if(i>10){
						text=text+"..";
						return text;
					}
				}
				if(text.length>0){
					text = text.substring(0,text.length-4);
				}
			}
			return text;
		}else{
			return this.getDimensionName();
		}
	}

});