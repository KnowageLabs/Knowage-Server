/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.tools.scheduler.SchedulerDetailPanel', {
    extend: 'Ext.form.Panel'
    	
        , config: {
        	//frame: true,
        	bodyPadding: '5 5 0',
        	defaults: {
                width: 400
            },        
            fieldDefaults: {
                labelAlign: 'right',
                msgTarget: 'side'
            },
            border: false,
            services:[]
        }

		, constructor: function(config) {
			
			this.initConfig(config);
			this.addEvents('addSchedulation');
			
			thisPanel = this;
			
			Ext.tip.QuickTipManager.init();

			
			this.triggersData = null;

			this.initFields();
			this.items=[this.activityLabel , this.documentsGrid, this.schedulationsGrid];
			this.tbar = Sbi.widget.toolbar.StaticToolbarBuilder.buildToolbar({items:[]},this);
			this.callParent(arguments);
			this.on("render",function(){this.hide()},this);

		}
		

		
		//initialize fields of the gui
		, initFields: function(){
			
			this.activityLabel = Ext.create("Ext.form.Label",{
				text: LN('sbi.scheduler.overview'),
			});
			
			this.documentsGridStore = Ext.create('Ext.data.Store', {
				fields:['name', 'condensedParameters'],
				data:{},
				proxy: {
					type: 'memory',
					reader: {
						type: 'json',
						root: 'documents'
					}
				}
			});
			
			this.documentsGrid = Ext.create('Ext.grid.Panel', {
			    title: LN('sbi.scheduler.documents'),
			    store: this.documentsGridStore,
			    columns: [
			        { text: LN('sbi.scheduler.name'),  dataIndex: 'name' },
			        { text: LN('sbi.scheduler.parameters'), dataIndex: 'condensedParameters', flex: 1,
				          renderer : 
				        		function(value, metadata, record, rowIndex, colIndex) {
				        			metadata.tdAttr = 'data-qtip="' + value +'"';

				        		return value;
				        	}
			        }

			    ],
			    height: 300,
			    width: '100%',
			    margin: '5 0 0 0'

			});
			
			this.schedulationsGridStore = Ext.create('Ext.data.Store', {
				fields:['jobName','jobGroup','triggerName','triggerGroup','triggerDescription' ,'triggerChronString','triggerStartDate','triggerStartTime','triggerEndDate','triggerEndTime','triggerIsPaused'],
				data:{},
				proxy: {
					type: 'memory',
					reader: {
						type: 'json',
						root: 'triggers'
					}
				}
			});
			
			this.schedulationsGrid = Ext.create('Ext.grid.Panel', {
			    title: LN('sbi.scheduler.schedulations'),
			    store: this.schedulationsGridStore,
			    columns: [
			        { text: LN('sbi.scheduler.name'),  dataIndex: 'triggerName', flex: 1,
			          renderer : 
			        		function(value, metadata, record, rowIndex, colIndex) {
			        			var triggerDescription = record.get('triggerDescription');
			        			metadata.tdAttr = 'data-qtip="<b>Name:</b> ' + value +'</br> <b>Description:</b> '+triggerDescription+'"';

			        		return value;
			        	} 
			        },
			      //  { text: 'Generation', dataIndex: 'generation' },
			        { text: LN('sbi.scheduler.type'), dataIndex: 'triggerChronString' },
			        { text: LN('sbi.scheduler.startdate'), dataIndex: 'triggerStartDate' },
			        { text: LN('sbi.scheduler.starttime'), dataIndex: 'triggerStartTime' },
			        { text: LN('sbi.scheduler.enddate'), dataIndex: 'triggerEndDate' },
			        { text: LN('sbi.scheduler.endtime'), dataIndex: 'triggerEndTime' },
					{
						//SCHEDULE INFO POPUP BUTTON
			        	menuDisabled: true,
						sortable: false,
						xtype: 'actioncolumn',
						width: 20,
						columnType: "decorated",
						items: [{
							tooltip: LN('sbi.scheduler.schedulation.info'),
							iconCls   : 'button-info',  
							handler: function(grid, rowIndex, colIndex) {
								var selectedRecord =  grid.store.getAt(rowIndex);
								thisPanel.onInfoSchedulation(selectedRecord);
							}
						}]
					},	
					{
						//DETAIL BUTTON
			        	menuDisabled: true,
						sortable: false,
						xtype: 'actioncolumn',
						width: 20,
						columnType: "decorated",
						items: [{
							tooltip: LN('sbi.scheduler.schedulation.detail'),
							iconCls   : 'button-detail',  
							handler: function(grid, rowIndex, colIndex) {
								var selectedRecord =  grid.store.getAt(rowIndex);
								thisPanel.onDetailSchedulation(selectedRecord);
							}
						}]
					},		
					{
						//EXECUTE NOW BUTTON
			        	menuDisabled: true,
						sortable: false,
						xtype: 'actioncolumn',
						width: 20,
						columnType: "decorated",
						items: [{
							iconCls   : 'button-execute', 
							tooltip: LN('sbi.scheduler.schedulation.execute'),
							handler: function(grid, rowIndex, colIndex) {
								var selectedRecord =  grid.store.getAt(rowIndex);
								thisPanel.onExecuteSchedulation(selectedRecord);
							}
						}]
					},
					{
						//STATE RESUME/PAUSE BUTTON
			        	menuDisabled: true,
						sortable: false,
						xtype: 'actioncolumn',
						width: 20,
						columnType: "decorated",
						renderer : 
			        		function(value, metadata, record, rowIndex, colIndex) {
							if (record.get('triggerIsPaused') == true) {
			        			metadata.tdAttr = 'data-qtip="'+LN('sbi.scheduler.schedulation.resume')+'"';
							} else {
			        			metadata.tdAttr = 'data-qtip="'+LN('sbi.scheduler.schedulation.pause')+'"';
							}

			        		return value;
			        	} ,
						items: [{
							//iconCls   : 'button-select',  
							handler: function(grid, rowIndex, colIndex) {
								var selectedRecord =  grid.store.getAt(rowIndex);
								if (selectedRecord.get('triggerIsPaused') == true){
									thisPanel.onResumeSchedulation(selectedRecord);
								} else {
									thisPanel.onPauseSchedulation(selectedRecord);
								}
							},
							getClass: function(v, meta, rec) {
								if (rec.get('triggerIsPaused') == true) {
									return 'button-resume-trigger';
								} else {
									return 'button-pause-trigger';
								}
							}

						}]
					},
					{
						//DELETE BUTTON
			        	menuDisabled: true,
						sortable: false,
						xtype: 'actioncolumn',
						width: 20,
						columnType: "decorated",
						items: [{
							tooltip: LN('sbi.scheduler.schedulation.delete'),
							iconCls   : 'button-remove',  
							handler: function(grid, rowIndex, colIndex) {								
								var selectedRecord =  grid.store.getAt(rowIndex);
								thisPanel.onDeleteSchedulation(selectedRecord);
							}
						}]
					}


			    ],
			    height: 300,
			    width: '100%',
			    margin: '5 0 0 0',
			    tbar: [{
			    	text: LN('sbi.generic.add'),
			        iconCls: 'icon-add',
			    	scope: this,
			    	tooltip: LN('sbi.scheduler.addschedulation'),
			    	handler: this.onAddClick
			    }]
			});
			
			
		}
		, onInfoSchedulation: function(record){
			var values = {}
			values.jobName = record.data.jobName;
			values.jobGroup = record.data.jobGroup;
			values.triggerName = record.data.triggerName;
			values.triggerGroup = record.data.triggerGroup;
			
			//perform Ajax Request

			Ext.Ajax.request({
				url: this.services["getTriggerSaveOptions"],
				params: values,
				success : function(response, options) {
					if(response !== undefined  && response.responseText !== undefined && response.statusText=="OK") {
						if(response.responseText!=null && response.responseText!=undefined){
							if(response.responseText.indexOf("error.mesage.description")>=0){
								Sbi.exception.ExceptionHandler.handleFailure(response);
							}else{						
								var config =  {};
								config.title = LN('sbi.scheduler.schedulation.info')+': '+values.triggerName;
								this.windowPopup = Ext.create('Sbi.tools.scheduler.TriggerInfoWindow',config, response.responseText);
								this.windowPopup.show();

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
		
		, onPauseSchedulation: function(record){
			var values = {}
			values.jobName = record.data.jobName;
			values.jobGroup = record.data.jobGroup;
			values.triggerName = record.data.triggerName;
			values.triggerGroup = record.data.triggerGroup;
			
			Ext.MessageBox.confirm(
					LN('sbi.generic.pleaseConfirm'),
					LN('sbi.schedulation.pauseConfirm'),
					function(btn, text){

						if (btn=='yes') {
							//perform Ajax Request

							Ext.Ajax.request({
								url: this.services["pauseTrigger"],
								params: values,
								success : function(response, options) {
									if(response !== undefined  && response.responseText !== undefined && response.statusText=="OK") {
										if(response.responseText!=null && response.responseText!=undefined){
											if(response.responseText.indexOf("error.mesage.description")>=0){
												Sbi.exception.ExceptionHandler.handleFailure(response);
											}else{						
												Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.scheduler.schedulation.paused'));
												record.set('triggerIsPaused',true);
												thisPanel.schedulationsGrid.store.commitChanges();
												thisPanel.schedulationsGrid.reconfigure(thisPanel.schedulationsGridStore) 
												
												if ((thisPanel.triggersData != null) && (thisPanel.triggersData !== undefined)){
													//remove the trigger also from the original json data
													var index = -1;
													for (var i = 0; i < thisPanel.triggersData.length; i++) {
														var element = thisPanel.triggersData[i];
														if ( (element.jobName == record.get('jobName')) &&
														     (element.jobGroup == record.get('jobGroup')) &&
														     (element.triggerGroup == record.get('triggerGroup')) &&
														     (element.triggerName == record.get('triggerName')) ){		
															
															element.triggerIsPaused = true;
															break;
														}
 
													}
													
												}

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
		
		, onResumeSchedulation: function(record){
			var values = {}
			values.jobName = record.data.jobName;
			values.jobGroup = record.data.jobGroup;
			values.triggerName = record.data.triggerName;
			values.triggerGroup = record.data.triggerGroup;
			
			Ext.MessageBox.confirm(
					LN('sbi.generic.pleaseConfirm'),
					LN('sbi.schedulation.resumeConfirm'),
					function(btn, text){

						if (btn=='yes') {
							//perform Ajax Request

							Ext.Ajax.request({
								url: this.services["resumeTrigger"],
								params: values,
								success : function(response, options) {
									if(response !== undefined  && response.responseText !== undefined && response.statusText=="OK") {
										if(response.responseText!=null && response.responseText!=undefined){
											if(response.responseText.indexOf("error.mesage.description")>=0){
												Sbi.exception.ExceptionHandler.handleFailure(response);
											}else{						
												Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.scheduler.schedulation.resumed'));
												record.set('triggerIsPaused',false);
												thisPanel.schedulationsGrid.store.commitChanges();
												thisPanel.schedulationsGrid.reconfigure(thisPanel.schedulationsGridStore) 
												
												if ((thisPanel.triggersData != null) && (thisPanel.triggersData !== undefined)){
													//remove the trigger also from the original json data
													var index = -1;
													for (var i = 0; i < thisPanel.triggersData.length; i++) {
														var element = thisPanel.triggersData[i];
														if ( (element.jobName == record.get('jobName')) &&
														     (element.jobGroup == record.get('jobGroup')) &&
														     (element.triggerGroup == record.get('triggerGroup')) &&
														     (element.triggerName == record.get('triggerName')) ){		
															
															element.triggerIsPaused = false;
															break;
														}
 
													}
													
												}

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
		
		, onExecuteSchedulation: function(record){
			var values = {}
			values.jobName = record.data.jobName;
			values.jobGroup = record.data.jobGroup;
			values.triggerName = record.data.triggerName;
			values.triggerGroup = record.data.triggerGroup;
			
			//perform Ajax Request

			Ext.Ajax.request({
				url: this.services["executeTrigger"],
				params: values,
				success : function(response, options) {
					if(response !== undefined  && response.responseText !== undefined && response.statusText=="OK") {
						if(response.responseText!=null && response.responseText!=undefined){
							if(response.responseText.indexOf("error.mesage.description")>=0){
								Sbi.exception.ExceptionHandler.handleFailure(response);
							}else{						
								Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.scheduler.schedulation.executed'));
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
		
		, onDetailSchedulation: function(record){
			var jobName = record.get('jobName');
			var jobGroup = record.get('jobGroup');
			var triggerName = record.get('triggerName');
			var triggerGroup = record.get('triggerGroup');
			
			window.location.assign(this.contextName + '/servlet/AdapterHTTP?JOBGROUPNAME='+jobGroup+'&PAGE=TriggerManagementPage&TYPE_LIST=TYPE_LIST&MESSAGEDET=MESSAGE_GET_SCHEDULE_DETAIL&TRIGGERGROUP='+triggerGroup+'&JOBNAME='+jobName+'&TRIGGERNAME='+triggerName);
		}
		
		, onDeleteSchedulation: function(record){
			
			var values = {}
			values.jobName = record.data.jobName;
			values.jobGroup = record.data.jobGroup;
			values.triggerName = record.data.triggerName;
			values.triggerGroup = record.data.triggerGroup;

			
			Ext.MessageBox.confirm(
					LN('sbi.generic.pleaseConfirm'),
					LN('sbi.generic.confirmDelete'),
					function(btn, text){

						if (btn=='yes') {
							//perform Ajax Request

							Ext.Ajax.request({
								url: this.services["deleteTrigger"],
								params: values,
								success : function(response, options) {
									if(response !== undefined  && response.responseText !== undefined && response.statusText=="OK") {
										if(response.responseText!=null && response.responseText!=undefined){
											if(response.responseText.indexOf("error.mesage.description")>=0){
												Sbi.exception.ExceptionHandler.handleFailure(response);
											}else{						
												Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.scheduler.schedulation.deleted'));
												thisPanel.schedulationsGrid.store.remove(record);
												thisPanel.schedulationsGrid.store.commitChanges();
												
												if ((thisPanel.triggersData != null) && (thisPanel.triggersData !== undefined)){
													//remove the trigger also from the original json data
													var index = -1;
													for (var i = 0; i < thisPanel.triggersData.length; i++) {
														var element = thisPanel.triggersData[i];
														if ( (element.jobName == record.get('jobName')) &&
														     (element.jobGroup == record.get('jobGroup')) &&
														     (element.triggerGroup == record.get('triggerGroup')) &&
														     (element.triggerName == record.get('triggerName')) ){		
															
															index = i;
															break;
														}
 
													}
													if (index != -1){
														thisPanel.triggersData.splice(index, 1);
													}
												}

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
		
		, onAddClick: function(){
			this.fireEvent('addSchedulation');
		}
		
		, setFormState: function(values){
			this.activityLabel.setText( LN('sbi.scheduler.overview')+values.jobName );
			this.documentsGridStore.loadData(values.documents,false);
			
			if ((values != undefined) && (values.triggers != undefined)){
				//iterate store to modify CronExpression (get only the type part).
				var typeValue;
				for (var i = 0; i < values.triggers.length; i++) {
					var element = values.triggers[i];
					var indFirstBra = element.triggerChronString.indexOf("{");
					if (indFirstBra !== -1){
						element.triggerChronString = element.triggerChronString.substring(0, indFirstBra);
					}
				}
			}
			this.schedulationsGridStore.loadData(values.triggers,false);
			//original data for permanent delete (not only on local store)
			this.triggersData = values.triggers

		}
    	
});    	