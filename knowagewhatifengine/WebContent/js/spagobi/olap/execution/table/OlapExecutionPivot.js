/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * Container for the pivot table. Contains:
 * <ul>
 * <li>Filters definition</li>
 * <li>Columns definition</li>
 * <li>Rows definition</li>
 * <li>Table</li>
 * </ul>
 *
 * The tree of the panel layout is:
 * <ul>
 *	<li>this (vbox)
 *		<ul>
 *			<li>filtersPanel</li>
 *			<li>pivotPanel(vbox)
 *				<ul>
 *					<li>columnsPanel (vbox)</li>
 *					<li>tableAndRowsPanel (hbox)
 *						<ul>
 *							<li>rowsPanel</li>
 *							<li>tablePanel</li>
 *						</ul>
 *					</li>
 *				</ul>
 *			</li>
 *		</ul>
 *	</li>
 *  </ul>
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.execution.table.OlapExecutionPivot', {
	extend: 'Ext.panel.Panel',

	layout: {
	    type: 'vbox',
	    align : 'stretch'
	},

	config:{
		border: false,
    	/**
    	 * @cfg {Number} filtersHeight
    	 * Height of the filters definition panel. Default 75
    	 */
		filtersHeight: 52,
    	/**
    	 * @cfg {Number} columnsHeight
    	 * Height of the columns definition panel. Default 50
    	 */
		columnsHeight: 40,
    	/**
    	 * @cfg {Number} rowsWidth
    	 * Width of the rows panel. Default 50
    	 */
		rowsWidth: 30,
    	/**
    	 * @cfg {Number} leftMargin
    	 * Margin left of the pivot table (filters, row, column , table)
    	 */
		leftMargin: 8,
    	/**
    	 * @cfg {Number} rightMargin
    	 * Margin right of the pivot table (filters, row, column , table)
    	 */
		rightMargin: 8,
    	/**
    	 * @cfg {Number} topMargin
    	 * Margin top of the pivot table (filters, row, column , table)
    	 */
		topMargin: 8,
    	/**
    	 * @cfg {Number} bottomMargin
    	 * Margin bottom of the pivot table (filters, row, column , table)
    	 */
		bottomMargin: 8,
    	/**
    	 * @cfg {Number} betweenColumnRowTableMargin
    	 * Margin between columns/rows/table
    	 */
		betweenColumnRowTableMargin: 5
	},

	/**
     * @property {Sbi.olap.execution.table.OlapExecutionFilters} olapExecutionFilters
     *  The table with the data
     */
	olapExecutionFilters: null,

	/**
     * @property {Sbi.olap.execution.table.OlapExecutionColumns} olapExecutionColumns
     *  The table with the data
     */
	olapExecutionColumns: null,

	/**
     * @property {Sbi.olap.execution.table.OlapExecutionRows} olapExecutionRows
     *  The table with the data
     */
	olapExecutionRows: null,

	/**
     * @property {Sbi.olap.execution.table.OlapExecutionTable} olapExecutionTable
     *  The table with the data
     */
	olapExecutionTable: null,

	/**
     * @property {Sbi.olap.PivotModel} model
     *  The model with the pivot configuration
     */
	model: null,


	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.execution && Sbi.settings.olap.execution.table && Sbi.settings.olap.execution.table.OlapExecutionPivot) {
			Ext.apply(this, Sbi.settings.olap.execution.table.OlapExecutionPivot);
		}
		if(Ext.isIE && Ext.ieVersion <10){
			this.filtersHeight = 60;
		}

		this.callParent(arguments);
	},

	initComponent: function() {


		// set margins top, right, bottom, and left
		columnsMargin =  this.topMargin+" "+this.rightMargin+" 0 0";
		filtersMargin = this.topMargin+" "+this.rightMargin+" 0 "+this.leftMargin;
		rowsTableMargin = this.betweenColumnRowTableMargin+" "+this.rightMargin+" "+this.bottomMargin+" "+this.leftMargin;
		rowsMargin = "0 "+this.betweenColumnRowTableMargin+" 0 0";
		crossMargin = this.topMargin+" "+this.betweenColumnRowTableMargin+" 0 "+this.leftMargin;


		//defining the components
		this.olapExecutionFilters   = Ext.create('Sbi.olap.execution.table.OlapExecutionFilters',  {height: this.filtersHeight, margin: filtersMargin , pivotContainer: this });//store: model.get('filters')
		this.olapExecutionColumns   = Ext.create('Sbi.olap.execution.table.OlapExecutionColumns',  {flex: 1, margin: columnsMargin,  pivotContainer: this});
		this.olapExecutionRows   = Ext.create('Sbi.olap.execution.table.OlapExecutionRows',  {width: this.rowsWidth, margin: rowsMargin,  pivotContainer: this});
		this.olapExecutionTable   = Ext.create('Sbi.olap.execution.table.OlapExecutionTable',  {flex: 1});


		this.olapExecutionFilters.on("filterValueChanged", function(){alert("true");},this);

		//defining the structure of the layout
		Ext.apply(this, {
			items: [this.olapExecutionFilters,
			        {
						border: false,
						flex:1,
						layout: {
							type: 'vbox',
							align : 'stretch'
						},
						items:[	{
									border: false,
									layout: {
										type: 'hbox',
										align : 'stretch'
									},
									height: this.columnsHeight,
									items:[	{
										margin: crossMargin,
										border: false,
										frame: false,
										width: this.rowsWidth,
										html: " ",
										bodyCls: "swapaxes",
										listeners: {
											el: {
												click: {
													fn: function (event, html, eOpts) {
														Sbi.olap.eventManager.swapAxis();
													},
													scope: this
												}
											}
										}
									},this.olapExecutionColumns]
								},
						        {
									margin: rowsTableMargin,
									border: false,
									flex:1,
									layout: {
										type: 'hbox',
										align : 'stretch'
									},
									items:[this.olapExecutionRows ,this.olapExecutionTable]
						        }
						]
			        }
			]

		});

		this.callParent();
	},

	/**
	 * Updates the visualization after the execution of a a mdx query
	 * @param pivotModel {Sbi.olap.PivotModel} the model instance with the execution query result set
	 */
	updateAfterMDXExecution: function(pivotModel){

		this.olapExecutionTable.updateAfterMDXExecution(pivotModel.get('table'));
		this.olapExecutionRows.updateAfterMDXExecution(pivotModel.get('rows'), pivotModel.get('rowsAxisOrdinal'));
		this.olapExecutionColumns.updateAfterMDXExecution(pivotModel.get('columns'), pivotModel.get('columnsAxisOrdinal') );
		this.olapExecutionFilters.updateAfterMDXExecution(pivotModel.get('filters'), -1);
	}
});





