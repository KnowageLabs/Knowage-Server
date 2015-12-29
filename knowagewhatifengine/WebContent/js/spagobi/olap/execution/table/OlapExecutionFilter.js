/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * The filter member..
 * The panel contains 3 subpanels:
 * <ul>
 * <li>a panel with the name of the dimension: with the text of the filter</li>
 * <li>selectedValuePanel: with the text of the filter</li>
 * <li>a panel with the funnel iconr</li>
 * </ul>
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.execution.table.OlapExecutionFilter', {
	extend: 'Sbi.olap.execution.table.OlapExecutionDimension',
	layout: "border",

	config:{
		/**
		 * @cfg {Sbi.olap.MemberModel} selectedMember
		 * The value of the filter
		 */
		selectedMember: null,
		/**
		 * @cfg {int} dimensionMaxtextLength
		 * The max length of the text.. If the text is longer we cut it and add 2 dots
		 */
		dimensionMaxtextLength: 20,
		/**
		 * @cfg {int} memberMaxtextLength
		 * The max length of the text.. If the text is longer we cut it and add 2 dots
		 */
		memberMaxtextLength: 17,
		/**
		 * @cfg {int} width
		 * The width of the filter
		 */
		width: 130,
		/**
		 * @cfg {boolean} multiSelection
		 * true to allow the multi selection of member, false otherwise
		 */
		multiSelection: false,
		cls: "x-column-header",
		bodyStyle: "background-color: transparent;",
		style: "margin-right: 3px; padding: 0px;"
	},

	/**
	 * @property {Ext.Panel} selectedValuePanel
	 *  Panel with the selected value of the filter
	 */
	selectedValuePanel: null,

	/**
	 * @property {Ext.Panel} titlePanel
	 *  Panel with the dimension name
	 */
	titlePanel: null,

	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.execution && Sbi.settings.olap.execution.table && Sbi.settings.olap.execution.table.OlapExecutionFilter) {
			Ext.apply(this, Sbi.settings.olap.execution.OlapExecutionFilter);
		}


		this.callParent(arguments);


		this.addEvents(
				/**
				 * @event dimensionClick
				 * Fired when the user clicks on the panel
				 * @param {Sbi.model.DimensionModel} dimensionClick
				 */
				'dimensionClick'
		);

	},



	buildItems: function(){
		var thisPanel = this;

		var selectedValuePanelConfig = {
				flex: 1,
				html:"...",
				border: false,
				bodyCls: "filter-value"};


		var titlePanelConfig = {
				flex: 1,
				region: 'north',
				html: "",
				border: true,
				bodyCls: "filter-title "
		};


		this.initTitlePanel(titlePanelConfig);
		this.initValuePanel(selectedValuePanelConfig);

		this.selectedValuePanel = Ext.create("Ext.Panel",	selectedValuePanelConfig);



		if(this.dimension.raw.hierarchies && this.dimension.raw.hierarchies.length>1){

			titlePanelConfig.border= false;
			titlePanelConfig.style = " margin-left: -5px!important";

			this.titlePanel = Ext.create("Ext.Panel", {
				region: 'north',
				layout: 'hbox',
				border: true,
				bodyCls: "filter-title ",
				style: " padding: 0px!important",
				bodyStyle: " padding: 0px!important",
				items: [this.buildMultiHierarchiesButton({style:"margin-left: 3px"}), titlePanelConfig]
			});

		}else{
			this.titlePanel = Ext.create("Ext.Panel", titlePanelConfig);
		}







		return  [this.titlePanel , {
			region: 'center',
			style: 'height: "100%"; background-color: transparent; margin: 4px',
			bodyStyle: 'background-color: transparent;',

			layout: {
				type: 'hbox',
				align:'stretch'
			},
			defaults: {
				style: "background-color: transparent;"
			},
			border: false,
			items:[
			       this.selectedValuePanel,
			       {
			    	   width:20,
			    	   html:" ",
			    	   cls:"filter-funnel-image",
			    	   border: true,
			    	   bodyCls: "filter-funnel-body",

			    	   listeners: {
			    		   el: {
			    			   click: {
			    				   fn: function (event, html, eOpts) {
			    					   var win =   Ext.create("Sbi.olap.execution.table.OlapExecutionFilterTree",{
			    						   title: LN('sbi.olap.execution.table.filter.filter.title'),
			    						   dimension: thisPanel.dimension,
			    						   selectedMember: this.selectedMember
			    					   });
			    					   win.show();
			    					   win.on("select", function(member){
			    						   this.setFilterValue(member);
			    					   },this);
			    				   },
			    				   scope: this
			    			   }
			    		   }
			    	   }
			       }
			       ]
		}];
	},

	/**
	 * @private
	 * Initializes the panel with the name of the dimension
	 */
	initTitlePanel: function(config){

		//get the name of the dimension
		var dimensionName = this.getDimensionName();
		var dimensionNameTooltip = this.getDimensionName();
		var thisPanel = this;

		//add the ellipses if the text is to long
		if(dimensionName.length>this.dimensionMaxtextLength){
			dimensionName = dimensionName.substring(0,this.dimensionMaxtextLength-2)+"..";
		}

		config.html= dimensionName;

		config.listeners = {
				el: {
					click: {
						fn: function(){
							thisPanel.fireEvent("dimensionClick", thisPanel.dimension);
						}
					}
				},
				render: {
					fn: function(){
						Ext.create('Ext.tip.ToolTip', {
							target: this.titlePanel.el,
							html: dimensionNameTooltip
						});
					},
					scope: thisPanel
				}
		};


	},

	/**
	 * @private
	 * Initializes the panel with the value of the slicer
	 */
	initValuePanel: function(config){

		var selectedHierarchy = null;
		var selectedHierarchyPosition = this.dimension.get("selectedHierarchyPosition");
		if(selectedHierarchyPosition!=null && selectedHierarchyPosition!=undefined){
			var hierarchies = this.dimension.get("hierarchies");
			if(hierarchies!=null && hierarchies!=undefined){
				selectedHierarchy = hierarchies[selectedHierarchyPosition];
			}
		}

		if(selectedHierarchy!=null){
			//get the slicers
			var slicers = selectedHierarchy.slicers;
			if(slicers && slicers.length>0){

				//creates the tooltip
				var dimensionValueTooltip="";
				for(var i=0; i<slicers.length; i++){
					dimensionValueTooltip = dimensionValueTooltip+", "+slicers[i].name;
				}
				dimensionValueTooltip = dimensionValueTooltip.substring(2);

				//creates the value
				var slicersValue = dimensionValueTooltip;

				//add the ellipses if the text is to long
				if(slicersValue.length>this.dimensionMaxtextLength){
					slicersValue = slicersValue.substring(0,this.dimensionMaxtextLength-2)+"..";
				}

				config.html=slicersValue;

				//add the tooltip
				config.listeners={
						"render":{
							fn: function(){
								Ext.create('Ext.tip.ToolTip', {
									target: this.selectedValuePanel.el,
									html: dimensionValueTooltip
								});
								},
							scope: this
						}

				};

				//if there is a slicer initialize the local variable this.selectedMember
				var selected =  Ext.create(Ext.ModelMgr.getModel('Sbi.olap.MemberModel'),slicers[0] );
				this.selectedMember = selected;

			}

		}
	},

	/**
	 * Sets the value of the filter
	 * @param {Sbi.olap.MemberModel} member the value of the filter
	 */
	setFilterValue: function(members){

		if(members && members.length){
			var member = members[0];

			var isChanged = false;
			if(member){
				if(this.selectedMember){
					isChanged = (this.selectedMember.uniqueName != member.uniqueName);
				}else{
					isChanged=true;
				}

				this.selectedMember = member;
				//updates the text
				var name =  this.selectedMember.name;
				if(name.length>this.memberMaxtextLength){
					name = name.substring(0,this.memberMaxtextLength-2)+"..";
				}
				this.selectedValuePanel.update(name);
			}
			if(isChanged){
				Sbi.olap.eventManager.addSlicer(this.dimension, this.selectedMember, this.multiSelection);
			}
		}
	}



});