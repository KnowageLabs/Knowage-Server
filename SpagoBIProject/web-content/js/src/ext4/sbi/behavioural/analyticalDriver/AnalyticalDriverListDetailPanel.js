Ext.define('Sbi.behavioural.analyticalDriver.AnalyticalDriverListDetailPanel', {
			extend: 'Sbi.widgets.compositepannel.ListDetailPanel',
		
			config: {
				stripeRows: true,
				modelName: "Sbi.behavioural.analyticalDriver.AnalyticalDriverModel"
			},
			
			constructor: function(config) {
				
				this.services =[];
				this.initServices();
				this.detailPanel = Ext.create('Sbi.behavioural.analyticalDriver.AnalyticalDriverDetailPanel',{services: this.services});
				this.columns = [{dataIndex:"LABEL", header:LN('sbi.generic.label')}, {dataIndex:"DESCRIPTION", header:LN('sbi.generic.descr')}];
				this.fields = ["ID","LABEL","DESCRIPTION","NAME","FUNCTIONALFLAG","TEMPORALFLAG","INPUTTYPECD"];
				this.filteredProperties = ["LABEL","DESCRIPTION"];
				this.buttonToolbarConfig = {
						newButton:true
				};
				this.buttonColumnsConfig ={
						deletebutton:true
				};
				
				this.detailPanel.on("save",this.onSaveRow,this);
				
				this.callParent(arguments);
			
			},
			
			initServices: function(baseParams){
				
				this.services["getTypes"]= Sbi.config.serviceRegistry.getRestServiceUrl({
					serviceName: 'domains/listValueDescriptionByType'
						, baseParams: baseParams
				});
				
				this.services["getUses"]= Sbi.config.serviceRegistry.getRestServiceUrl({
					serviceName: 'analyticalDriverUse'
						, baseParams: baseParams
				});
				
			},
			
			onDeleteRow: function(record){
				
				var selectedRow = this.grid.getSelectionModel().getSelection();
				
				if (selectedRow.length !== 0 && selectedRow[0].data.ID  == record.data.ID){
					
					this.detailPanel.hide();
					
				}
				
				var recordToDelete = Ext.create("Sbi.behavioural.analyticalDriver.AnalyticalDriverModel", record.data);
				recordToDelete.destroy({
					success : function(object, response, options) {
						this.grid.store.remove(record);
						this.grid.store.commitChanges();
						
					},
					scope: this
					
				});
				
				
				
			},
			onGridSelect: function(selectionrowmodel, record, index, eOpts){
					
				this.detailPanel.show();
				this.detailPanel.setFormState(record.data);
				
				},
				
			onSaveRow: function(record){
				
				var recordToSave = Ext.create("Sbi.behavioural.analyticalDriver.AnalyticalDriverModel", record);
				
				recordToSave.save({
					success : function(object, response, options) {
						
						if(response !== undefined && response.response !== undefined && response.response.responseText !== undefined && response.response.statusText=="OK") {
							response = response.response ;
								if(response.responseText.indexOf("error.mesage.description")>=0){
									Sbi.exception.ExceptionHandler.handleFailure(response);
								}else{
						
						
								var selectedRow = this.grid.getSelectionModel().getSelection();
					
								var JSONResponse = Ext.decode(response.responseText);
								
								selectedRow[0].data = Ext.apply(selectedRow[0].data, record);
								selectedRow[0].raw = Ext.apply(selectedRow[0].raw, record);
								selectedRow[0].data.ID = JSONResponse.ID;
					
					
								
								selectedRow[0].commit();
					
								this.grid.store.sync();
								this.grid.store.commitChanges() ;
								this.grid.store.loadData(selectedRow[0], true) ;
					
								this.grid.getView().refresh();
					
								this.detailPanel.adid.setValue(JSONResponse.ID);
								}
						} else {
							Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
						}
					},
					scope:this
							
				});
				
				
				
			}
		
			
		});