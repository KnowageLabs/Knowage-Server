Ext.define('Sbi.community.CommunityListDetailPanel', {
	extend: 'Sbi.widgets.compositepannel.ListDetailPanel'

	,config: {
		stripeRows: true,
		modelName: "Sbi.community.CommunityModel"
	}

	, constructor: function(config) {
		this.services =[];
		this.initServices();
		this.detailPanel =  Ext.create('Sbi.community.CommunityDetailPanel',{services: this.services});
		this.columns = [{dataIndex:"communityId", header:LN('sbi.community.id')},  {dataIndex:"name", header:LN('sbi.community.name')}, {dataIndex:"owner", header:LN('sbi.community.owner')}];
		this.fields = ["communityId","name","description","owner","functCode"];
		
		this.detailPanel.on("save",this.onFormSave,this);
		this.filteredProperties = ["name", "owner"];
		this.buttonToolbarConfig = {
				newButton: true
		};
		this.buttonColumnsConfig ={
				deletebutton:true
		};
	

		this.callParent(arguments);
	}
	
	, initServices: function(baseParams){

	}
	
	, onDeleteRow: function(record){
		var recordToDelete = Ext.create("Sbi.community.CommunityModel",record.data);
		recordToDelete.destroy({
			success : function(object, response, options) {
				if(response !== undefined && response.response !== undefined && response.response.responseText !== undefined && response.response.statusText=="OK") {
					response = response.response ;
					if(response.responseText!=null && response.responseText!=undefined){
						if(response.responseText.indexOf("error.mesage.description")>=0){
							Sbi.exception.ExceptionHandler.handleFailure(response);
						}else{
							Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.community.deleted'));
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
	
		var recordToSave = Ext.create("Sbi.community.CommunityModel",record);
		recordToSave.save({
			success : function(object, response, options) {
	
				if(response !== undefined && response.response !== undefined && response.response.responseText !== undefined && response.response.statusText=="OK") {
					response = response.response ;
					if(response.responseText!=null && response.responseText!=undefined){
						if(response.responseText.indexOf("error.mesage.description")>=0){
							Sbi.exception.ExceptionHandler.handleFailure(response);
						}else{
							var respoceJSON = Ext.decode(response.responseText);
							if(respoceJSON.communityId){
								record.communityId = respoceJSON.communityId;
							}
							Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.community.saved'));
							var selectedRow = this.grid.getSelectionModel().getSelection();
							
							
							//unused.. Its a workaround because it doesn't update the values in the grids...
							selectedRow[0].set("name",selectedRow.name);
							
							
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
