/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * Container of the tools for the view definition.<br>
 * It contains:
 * <ul>
 *		<li>Cube Selector</li>
 *		<li>Dimensions Selector</li>
 *		<li>Measures Selector</li>
 *	</ul>
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */

Ext.define('Sbi.olap.tools.OlapViewDefinitionTools', {
	//class to extends
	extend: 'Ext.panel.Panel',

	config:{
		collapsible: true,
		split: true,
		collapseMode: "mini",
		title: "View Editor",
		width: "100%"
	},

	/**
     * @property {Sbi.view.tools.OlapViewCubeSelector} cubeSelector
     *  Selector of the cube
     */
	cubeSelector: null,
	/**
     * @property {Sbi.view.tools.OlapViewDimensionsSelector} dimensionsSelector
     *  Selector of the dimensions
     */
	dimensionsSelector: null,
	/**
     * @property {Sbi.view.tools.OlapViewMeasuresSelector} measuresSelector
     *  Selector of the measures
     */
	measuresSelector: null,


	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.tools && Sbi.settings.olap.tools.OlapViewDefinitionTools) {
			Ext.apply(this, Sbi.settings.olap.tools.OlapViewDefinitionTools);
		}
		this.cubeSelector = Ext.create('Sbi.olap.tools.OlapViewCubeSelector', {});
		this.dimensionsSelector = Ext.create('Sbi.olap.tools.OlapViewDimensionsSelector', {});
		this.measuresSelector = Ext.create('Sbi.olap.tools.OlapViewMeasuresSelector', {});

		this.callParent(arguments);
	},

	initComponent: function() {


		Ext.apply(this, {
			items: [this.cubeSelector,this.dimensionsSelector,this.measuresSelector]
		});
		this.callParent();
	}
});