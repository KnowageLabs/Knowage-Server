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
 
  
Ext.define('Sbi.tools.layer.LayerListDetailPanel', {
	extend: 'Sbi.widgets.compositepannel.ListDetailPanel'

	,config: {
		stripeRows: true,
		modelName: "Sbi.tools.layer.LayerModel"
	}

	, constructor: function(config) {
		this.services =[];
		this.initServices();
		this.detailPanel =  Ext.create('Sbi.tools.layer.LayerDetailPanel',{services: this.services});
		this.columns = [{dataIndex:"label", header:LN('sbi.generic.label')}, {dataIndex:"descr", header:LN('sbi.generic.descr')}, {dataIndex:"type", header:LN('sbi.generic.type')}];
		this.fields =  [
		   	         "type",
			         "id",
			         "name",
			         "descr",
			         "label", 
			         "baseLayer",
			         "propsFile",
			         "propsUrl",
			         "propsName",
			         "propsLabel",
			         "propsZoom",
			         "propsId",
			         "propsCentralPoint",
			         "propsParams",
			         "propsOptions"
			         ];
		this.detailPanel.on("save",this.onFormSave,this);
		this.filteredProperties = ["label","type","descr"];
		this.buttonToolbarConfig = {
				newButton: true
		};
		this.buttonColumnsConfig ={
				deletebutton:true
		};
	

		this.callParent(arguments);
	}
	
	, initServices: function(baseParams){
		this.services["getTypes"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'domains/listValueDescriptionByType'
				, baseParams: baseParams
		});
	}
	
	, onDeleteRow: function(record){
		var recordToDelete = Ext.create("Sbi.tools.layer.LayerModel",record.data);
		recordToDelete.destroy({
			success : function(object, response, options) {
				if(response !== undefined && response.response !== undefined && response.response.responseText !== undefined && response.response.statusText=="OK") {
					response = response.response ;
					if(response.responseText!=null && response.responseText!=undefined){
						if(response.responseText.indexOf("error.mesage.description")>=0){
							Sbi.exception.ExceptionHandler.handleFailure(response);
						}else{
							Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.layer.deleted'));
							this.grid.store.remove(record);
							this.grid.store.commitChanges();
						}
					}
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
				}
			},
			scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure      
		});
	}
	
	, onFormSave: function(record){
	
		var recordToSave = Ext.create("Sbi.tools.layer.LayerModel",record);
		recordToSave.save({
			success : function(object, response, options) {
	
				if(response !== undefined && response.response !== undefined && response.response.responseText !== undefined && response.response.statusText=="OK") {
					response = response.response ;
					if(response.responseText!=null && response.responseText!=undefined){
						if(response.responseText.indexOf("error.mesage.description")>=0){
							Sbi.exception.ExceptionHandler.handleFailure(response);
						}else{
							var respoceJSON = Ext.decode(response.responseText);
							if(respoceJSON.id){
								record.id = respoceJSON.id;
							}
							Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.layer.saved'));
							var selectedRow = this.grid.getSelectionModel().getSelection();
							
							
							//unused.. Its a workaround because it doesn't update the values in the grids...
							selectedRow[0].set("descr",selectedRow.DESCRIPTION);
							
							
							selectedRow[0].data = Ext.apply(selectedRow[0].data,record);
							this.grid.store.commitChanges();	
							this.detailPanel.setFormState(record);
						}
					}
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
				}
			},
			scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure  
		});
	}
	
	, onGridSelect: function(selectionrowmodel, record, index, eOpts){
		this.detailPanel.show();
		this.detailPanel.setFormState(record.data);
	}
});
