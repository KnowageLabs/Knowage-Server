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
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors - Alberto Ghedin
 */
Ext.ns("Sbi.kpi");

Sbi.kpi.ManageGoalsViewPort = function(config) { 
	
	this.manageGoalsDetailsPanelConfig = {};
	var paramsList = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "GRANT_LIST"};
	this.manageGoalsDetailsPanelConfig.manageGrantListService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_OUS_ACTION'
			, baseParams: paramsList
	});
	
	this.manageGoalsDetailsPanelConfig.manageGrantDefinitionService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_OUS_ACTION'
			, baseParams: {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "OU_HIERARCHY_AND_ROOT"}
	});
	
	

	//DRAW center element
	this.manageGoals = new Sbi.kpi.ManageGoals(config, this);

	this.manageGoalsGrid = new Sbi.kpi.ManageGoalsGrid(config, this);
	//this.ManageOUGrants.on('saved',function(){this.manageGoalsGrid.mainElementsStore.reload(); },this);

	this.initPanels();

	var c = Ext.apply({}, config || {}, this.viewport);

	Sbi.kpi.ManageGoalsViewPort.superclass.constructor.call(this, c);	 		

};

Ext.extend(Sbi.kpi.ManageGoalsViewPort, Ext.Viewport, {
	manageGoals: null
	, viewport: null
	, manageGoalsGrid: null
	, manageGoalsDetailsPanel: null
	, manageGoalsDetailsPanelConfig: null

	, initPanels : function(config) {
		var thisPanel = this;

		this.manageGoalsDetailsPanel = new Sbi.kpi.ManageGoalsDetailsPanel(this.manageGoalsDetailsPanelConfig);
		this.tabs = new Ext.TabPanel({
			title: 'Manage Goals'
				, hideLabel: true
			, activeTab: 0
			, items: [this.manageGoalsDetailsPanel, this.manageGoals]
		});
		//reload after save
		this.manageGoalsDetailsPanel.on('saved',function(){
			this.manageGoalsGrid.mainElementsStore.reload();
			this.manageGoalsGrid.getView().refresh();
			this.manageGoalsGrid.getView().on('refresh', function(){	
				var n = this.manageGoalsGrid.getStore().getCount();
				this.manageGoalsGrid.getView().focusRow(n - 1);
				this.manageGoalsGrid.rowselModel.selectLastRow();
				this.manageGoalsGrid.fireEvent('newItem');
				}, this);

		},this);
		
		this.tabs.on('beforetabchange', 
			function(thisPanel, newTab, currentTab){
				if(newTab.id=='goalPanel'){
				
					if(this.manageGoalsDetailsPanel.goalId==null || this.manageGoalsDetailsPanel.goalId==''){
//						if(this.newGoalGridLine){
//							this.manageGoalsGrid.rowselModel.selectLastRow();
//							this.sendSelectedItem();
//						}else{
							Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.goals.nogoal'), LN('sbi.generic.warning'));
							return false;
//						}
					}
					
					if(this.manageGoalsDetailsPanel.detailFieldGrant.getValue()==null || this.manageGoalsDetailsPanel.detailFieldGrant.getValue()==undefined || this.manageGoalsDetailsPanel.detailFieldGrant.getValue()=='' || this.manageGoalsDetailsPanel.detailFieldGrant.getValue()=='undefined'){
						Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.goals.nogrant'), LN('sbi.generic.warning'));
						return false;
					}else{
						this.getGrant(this.manageGoalsDetailsPanel.detailFieldGrant.getValue());
					}
				}
			}
			, this);
		
		this.manageGoalsGrid.addListener('rowclick', function(a,b,c){
				this.newGoalGridLine = false;
				this.sendSelectedItem(a,b,c);
		}, this);	
		this.manageGoalsGrid.addListener('newItem', function(a,b,c){
				this.newGoalGridLine = true;
				this.sendSelectedItem(a,b,c);
		}, this);	
		
		
		this.viewport = {
				layout: 'border'
					, height:560
					, autoScroll: true
					, items: [
					          {
					        	  id: 'modelInstancesList00',
					        	  region: 'west',
					        	  width: 275,
					        	  height:560,
					        	  collapseMode:'mini',
					        	  autoScroll: true,
					        	  split: true,
					        	  layout: 'fit',
					        	  items: [this.manageGoalsGrid]
					          },
					          {
					        	  id: 'main00',	  
					        	  region: 'center',
					        	  width: 300,
					        	  height:560,
					        	  split: true,
					        	  collapseMode:'mini',
					        	  autoScroll: true,
					        	  layout: 'fit',
					        	  items: [this.tabs]
					          }
					          ]
		};
	}

	,sendSelectedItem: function(grid, rowIndex, e){	
		this.tabs.setActiveTab(0);//when a user select a grant we show the details tab
		this.manageGoalsDetailsPanel.setDisabled(false);
		var rec = this.manageGoalsGrid.rowselModel.getSelected();
		this.manageGoalsDetailsPanel.detailFieldLabel.setValue(rec.data.label);
		this.manageGoalsDetailsPanel.detailFieldName.setValue(rec.data.name);
		this.manageGoalsDetailsPanel.detailFieldDescr.setValue(rec.data.description);
		this.manageGoalsDetailsPanel.detailFieldFrom.setRawValue(rec.data.startdate);
		this.manageGoalsDetailsPanel.detailFieldTo.setRawValue(rec.data.enddate);
		this.manageGoalsDetailsPanel.detailFieldGrant.setValue(rec.data.grantid);
		this.manageGoalsDetailsPanel.detailFieldGrant.setRawValue(rec.data.grantname);
		this.manageGoalsDetailsPanel.goalId=rec.data.id;
		this.manageGoals.goalId = rec.data.id;
	}
	
	
	, getGrant: function(grantId){
		Ext.Ajax.request({
			url: this.manageGoalsDetailsPanelConfig.manageGrantDefinitionService,
			params: {'grantId': grantId},
			method: 'GET',
			success: function(response, options) {
				if (response !== undefined && response.responseText!== undefined) {
					var grantNode = Ext.util.JSON.decode(response.responseText);
					var ouRootId = grantNode.ouRootId;
					var ouRootName = grantNode.ouRootName;
					this.manageGoals.updatePanel(grantId,ouRootName,ouRootId);
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.savingItemError'), LN('sbi.generic.serviceError'));
				}
			}
			,scope: this

		});
	}
});
