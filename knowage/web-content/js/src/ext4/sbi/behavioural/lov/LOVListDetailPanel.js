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
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */
 
   
Ext.define
(
	"Sbi.behavioural.lov.LOVListDetailPanel", 
	
	{
		extend: 'Sbi.widgets.compositepannel.ListDetailPanel',

		config: 
		{
			stripeRows: true,
			modelName: "Sbi.behavioural.lov.LOVModel"
		},

		constructor: function(config) 
		{		
			var isSuperadmin = config.isSuperadmin;
			
			this.services =[];
			this.initServices();
			
			this.detailPanel =  Ext.create
			(
				"Sbi.behavioural.lov.LOVDetailPanel",
				
				{
					services: this.services, 
					isSuperadmin: isSuperadmin 
				}
			);
						
			this.columns = 
			[
			 	{ dataIndex:"LOV_NAME", 		header:LN('sbi.generic.label') }, 
			 	{ dataIndex:"LOV_DESCRIPTION", 	header:LN('sbi.generic.descr') }
		 	];
			
			this.fields = ["LOV_ID", "LOV_NAME", "LOV_DESCRIPTION", "INPUT_TYPE_COMBOBOX"];
			
			this.detailPanel.on("save", this.checkCanSave, this);
			
			this.filteredProperties = ["LOV_NAME"];
			
			this.buttonToolbarConfig = 
			{
				newButton: true
			};
			
			this.buttonColumnsConfig =
			{
				deletebutton: true
			};
	
			this.callParent(arguments);
		},
		
		initServices: function(baseParams)
		{
//			this.services["test"]= Sbi.config.serviceRegistry.getRestServiceUrl({
//				serviceName: 'datasources/test'
//					, baseParams: baseParams
//			});
			
			this.services["getDomains"]= Sbi.config.serviceRegistry.getRestServiceUrl
			(
				{
					serviceName: 'domains/listValueDescriptionByType', 
					baseParams: baseParams
				}
			);
			
			this.services["getDataSources"] = Sbi.config.serviceRegistry.getRestServiceUrl
			(
				{
					serviceName: 'datasources', 
					baseParams: baseParams
				}
			);	
		
			this.services["LOV"] = Sbi.config.serviceRegistry.getRestServiceUrl
			(
				{
					serviceName: 'LOV', 
					baseParams: baseParams
				}
			);	
		},
		
		checkCanSave: function(record)
		{
			var currentPanel = this;
			
			if (record.LOV_LABEL == "")
			{
				Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.behavioural.lov.details.labelMissing'));
			}
			else if (record.LOV_NAME == "")
			{
				Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.behavioural.lov.details.nameMissing'));
			}
			else if (record.I_TYPE_CD == "")
			{
				Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.behavioural.lov.details.inputTypeMissing'));
			}
			else if (record.DATASOURCE_ID == "" || record.DATASOURCE_ID == null)
			{
				Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.behavioural.lov.details.dataSourceMissing'));
			}
			else
			{
				currentPanel.onFormSave(record);
			}
			
		},	
		
		onFormSave: function(record)
		{			
			this.detailPanel.getFormState().save
			(
				{
					success: function(object, response, options)
							{																
								if(response !== undefined && response.response !== undefined && response.response.responseText !== undefined && response.response.statusText=="OK") 
								{									
									response = response.response ;
									
									if(response.responseText!=null && response.responseText!=undefined)
									{										
										if(response.responseText.indexOf("error.mesage.description")>=0)
										{
											Sbi.exception.ExceptionHandler.handleFailure(response);
										}
										else
										{
											// When there is no error in message - successfully saved record
											Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.behavioural.lov.saved'));
											var selectedRow = this.grid.getSelectionModel().getSelection();
											
											
											
											// Ext.apply() - Copies all the properties of config to the specified object
											selectedRow[0].data = Ext.apply(selectedRow[0].data, record);	
											selectedRow[0].raw = Ext.apply(selectedRow[0].raw, record);											
											
											selectedRow[0].data.LOV_ID = response.responseText;
											
											selectedRow[0].commit();

											this.grid.store.sync();
											this.grid.store.commitChanges() ;
											this.grid.store.loadData(selectedRow[0], true) ;
											
											this.grid.getView().refresh();
											
											this.detailPanel.lovId.setValue(response.responseText);
										}
									}
								}
								else
								{
									Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
								}
							},
					
					failure: Sbi.exception.ExceptionHandler.handleFailure,
							
					scope: this					
				}
			);			
		},
		
		onDeleteRow: function(record)
		{			
			var selectedRecord = this.grid.getSelectionModel().getSelection();	
			
			var recordToDelete = Ext.create("Sbi.behavioural.lov.LOVModel",record.data);
			
			if (selectedRecord[0] != undefined && recordToDelete.data.LOV_ID == selectedRecord[0].data.LOV_ID)
			{
				this.detailPanel.hide();
			}
			
			if (record.data.LOV_ID == 0)
			{
				this.grid.store.remove(record);
				this.grid.store.commitChanges(); 
			}
			else	
			{			
				recordToDelete.destroy
				(
					{
						appendId:false,
						
						success : function(object, response, options) 
						{
							if(response !== undefined && response.response !== undefined && response.response.responseText !== undefined && response.response.statusText=="OK") 
							{
								var aaa = response;
								
								response = response.response ;
								
								if(response.responseText!=null && response.responseText!=undefined)
								{
									if(response.responseText.indexOf("error.mesage.description")>=0 || 
											response.responseText.indexOf("Error while deleting LOV") >= 0)
									{
										Sbi.exception.ExceptionHandler.handleFailure(aaa);
									}
									else
									{
										Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.datasource.deleted'));
										this.grid.store.remove(record);
										this.grid.store.commitChanges(); 
									}
								}
							} 
							else 
							{
								Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
							}
						},
						
						scope: this,
						
						failure: Sbi.exception.ExceptionHandler.handleFailure      
					}
				);
			}
		},
		
		
		onGridSelect: function(selectionrowmodel, record, index, eOpts)
		{
			this.detailPanel.show();
			
			if (record.data.LOV_ID == null || record.data.LOV_ID == "")
			{	
				this.detailPanel.panel2.hide();
			}
			
			this.detailPanel.setFormState(record.data);
		}
	}
);