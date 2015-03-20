/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.tools.scheduler.SchedulerListDetailPanel', {
	extend: 'Sbi.widgets.compositepannel.ListDetailPanel'

		, config: {
			stripeRows: true,
			modelName: "Sbi.tools.scheduler.SchedulerModel",
	        contextName: ''
		}

		, constructor: function(config) {
		
			this.initConfig(config);

			var isSuperadmin= config.isSuperadmin;
						
			var thisPanel = this;
		
			this.services =[];
			this.initServices();
			this.detailPanel =  Ext.create('Sbi.tools.scheduler.SchedulerDetailPanel',{services: this.services, isSuperadmin: isSuperadmin, contextName: this.contextName });
			this.detailPanel.on("addSchedulation",this.addSchedulation,this);

			this.columns = [{dataIndex:"jobName", header:LN('sbi.generic.label')}, {dataIndex:"jobDescription", header:LN('sbi.generic.descr')}];
			this.fields = [
			               "jobName",
			               "jobDescription",
			               "jobClass",
			               "jobDurability",
			               "jobRequestRecovery",
			               "useVolatility",
			               "jobParameters",
			               "documents",
			               "triggers"
			               ];
	
			
			this.filteredProperties = ["jobName"];
			
			//for filtering inside objects of fields (in this case triggers object)
			this.filteredObjects = [
			              {
			            	 objectName : "triggers",
			            	 filteredProperties: ["triggerStartTime","triggerEndTime","triggerChronString"]
			              }          
			];
			
			this.buttonToolbarConfig = {
					newButton: true
			};
			this.buttonColumnsConfig ={
					deletebutton:true
			};
			
			this.customComboToolbarConfig = {
					data: [{
							"name": LN('sbi.scheduler.starttime'),
							"value": "triggerStartTime",		        
						}, {
							"name": LN('sbi.scheduler.endtime'),
							"value": "triggerEndTime",	
						}, {
							"name": LN('sbi.scheduler.schedulationtype'),
							"value": "triggerChronString",	
						},
						{
							"name": LN('sbi.generic.label'),
							"value": "jobName",	
						}
					],
					fields: ["name","value"],
					displayField: "name",
					valueField: "value"
			
			}
			
			
			 Ext.tip.QuickTipManager.init();
			
			//custom buttons for scheduler operations
			Sbi.widget.grid.StaticGridDecorator.addCustomBottonColumn(this.columns, 'button-detail', LN('sbi.scheduler.activity.detailactivity') ,function(grid, rowIndex, colIndex) {
				var record = grid.getStore().getAt(rowIndex);
				var jobName = record.get('jobName');
				var jobGroup = record.get('jobGroup');
				window.location.assign(thisPanel.contextName + '/servlet/AdapterHTTP?JOBGROUPNAME='+jobGroup+'&PAGE=JobManagementPage&TYPE_LIST=TYPE_LIST&MESSAGEDET=MESSAGE_GET_JOB_DETAIL&JOBNAME='+jobName);

			})
			//Schedulation List button
			/*
			Sbi.widget.grid.StaticGridDecorator.addCustomBottonColumn(this.columns, 'button-schedule', LN('sbi.scheduler.activity.schedulationlist'),function(grid, rowIndex, colIndex) {
				var record = grid.getStore().getAt(rowIndex);
				var jobName = record.get('jobName');
				var jobGroup = record.get('jobGroup');
				window.location.assign(thisPanel.contextName + '/servlet/AdapterHTTP?JOBGROUPNAME='+jobGroup+'&PAGE=TriggerManagementPage&TYPE_LIST=TYPE_LIST&MESSAGEDET=MESSAGE_GET_JOB_SCHEDULES&JOBNAME='+jobName);

			})
			*/
		
			this.callParent(arguments);
		}
		
		
		, initServices: function(baseParams){
			
			this.services["delete"]= Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName: 'scheduler/deleteJob'
					, baseParams: baseParams
			});
			
			this.services["deleteTrigger"]= Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName: 'scheduler/deleteTrigger'
					, baseParams: baseParams
			});
			
			this.services["executeTrigger"]= Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName: 'scheduler/executeTrigger'
					, baseParams: baseParams
			});
			
			this.services["pauseTrigger"]= Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName: 'scheduler/pauseTrigger'
					, baseParams: baseParams
			});
			
			this.services["resumeTrigger"]= Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName: 'scheduler/resumeTrigger'
					, baseParams: baseParams
			});
			
			this.services["getTriggerSaveOptions"]= Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName: 'scheduler/getTriggerSaveOptions'
					, baseParams: baseParams
			});

		}
		
		, onDeleteRow: function(record){			
			var recordToDelete = Ext.create("Sbi.tools.scheduler.SchedulerModel",record.data);
			var values = {};
			values.jobGroup = record.data.jobGroup ;
			values.jobName = record.data.jobName ;
			
			Ext.MessageBox.confirm(
					LN('sbi.generic.pleaseConfirm'),
					LN('sbi.generic.confirmDelete'),
					function(btn, text){
						if (btn=='yes') {
							//perform Ajax Request
							Ext.Ajax.request({
								url: this.services["delete"],
								params: values,
								success : function(response, options) {
									if(response !== undefined  && response.responseText !== undefined && response.statusText=="OK") {
										if(response.responseText!=null && response.responseText!=undefined){
											if(response.responseText.indexOf("error.mesage.description")>=0){
												Sbi.exception.ExceptionHandler.handleFailure(response);
											}else{						
												Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.scheduler.activity.deleted'));
												this.grid.store.remove(record);
												this.grid.store.commitChanges();
												this.detailPanel.hide();
											}
										}
									} else {
										Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
									}
								},
								scope: this,
								failure: Sbi.exception.ExceptionHandler.handleFailure      
							})
						}
					},
					this
				);
		}
		
		, addSchedulation: function(){
			if (this.grid.getSelectionModel().hasSelection()) {
				   var row = this.grid.getSelectionModel().getSelection()[0];
				   var jobGroup = row.get('jobGroup');
				   var jobName = row.get('jobName');
				   window.location.assign(this.contextName + '/servlet/AdapterHTTP?JOBGROUPNAME='+jobGroup+'&PAGE=TriggerManagementPage&TYPE_LIST=TYPE_LIST&MESSAGEDET=MESSAGE_NEW_SCHEDULE&JOBNAME='+jobName);

				}
		}
		
		//overwrite parent method
		, onAddNewRow: function(){
			window.location.assign(this.contextName + '/servlet/AdapterHTTP?PAGE=JobManagementPage&TYPE_LIST=TYPE_LIST&MESSAGEDET=MESSAGE_NEW_JOB');
		}
		
		//when selecting a row in the grid list
		, onGridSelect: function(selectionrowmodel, record, index, eOpts){
			this.detailPanel.show();
			this.detailPanel.setFormState(record.data);
		}
		
});		