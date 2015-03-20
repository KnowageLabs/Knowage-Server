Ext.define('Sbi.widget.grid.StaticGridDecorator', {
	statics: {
		
		/**
		 * Adds to the grid the button columns according to the configuration conf. This configuration object is a map 
		 * like this one: {"selectbutton": true, "deletebutton":true, "copybutton":true}
		 * @param {Object} configuration object
		 * @param {Array} list of columns of the grid
		 * @param {grid} the grid that fire the events handled from the buttons
		 */
		addButtonColumns: function(conf, columns, grid){
			if(conf){
				if(conf.selectbutton){
					Sbi.widget.grid.StaticGridDecorator.addSelectColumn(columns, grid);
				}
				if(conf.deletebutton){
					Sbi.widget.grid.StaticGridDecorator.addDeleteColumn(columns, grid);			
				}
			}

		},

		/**
		 * Adds to the grid the button column to select the row. When the user clicks on the button an event will be thrown
		 * @param {Array} list of columns of the grid
		 * @param {grid} the grid that fire the events handled from the buttons
		 */
		addSelectColumn: function(columns, grid){
			var selectbutton =
			{
					menuDisabled: true,
					sortable: false,
					xtype: 'actioncolumn',
					width: 20,
					columnType: "decorated",
					items: [{
						iconCls   : 'button-select',  // Use a URL in the icon config
						handler: function(grid, rowIndex, colIndex) {
							var selectedRecord =  grid.store.getAt(rowIndex);
				 	        grid.fireEvent("selectrow",selectedRecord,rowIndex,colIndex)
						}
					}]
			};
			columns.push(selectbutton);
		},

		addDeleteColumn: function(columns, grid ){
			var deletebutton =
			{
					menuDisabled: true,
					sortable: false,
					xtype: 'actioncolumn',
					width: 20,
					columnType: "decorated",
					items: [{
						iconCls   : 'button-remove',  // Use a URL in the icon config
						handler: function(grid, rowIndex, colIndex) {
							var selectedRecord =  grid.store.getAt(rowIndex);
				 	        grid.fireEvent("deleterow",selectedRecord,rowIndex,colIndex)
						}
					}]
			};
			columns.push(deletebutton);
		}
		

		,addCustomBottonColumn: function(columns, iconclass, tooltip,handlerFunction){
			var button =
			{
					menuDisabled: true,
					sortable: false,
					xtype: 'actioncolumn',
					width: 20,
					columnType: "decorated",
					items: [{
						iconCls   : iconclass,  // Use a URL in the icon config
						tooltip: tooltip,
						handler: handlerFunction
					}]
			};
			columns.push(button);
		}
		
		,getAdditionalToolbarButtons: function(conf, grid){
			var buttons = new Array();
			if(conf){
				if(conf.newButton){
					buttons.push(Sbi.widget.grid.StaticGridDecorator.addNewItemToolbarButton(grid));
				}
				if(conf.cloneButton){
					buttons.push(Sbi.widget.grid.StaticGridDecorator.addCloneItemToolbarButton(grid));
				}
			}
			return buttons;
		}
		
		,addNewItemToolbarButton: function(grid){
			return({
				text: LN('sbi.generic.add'),
 	            iconCls: 'icon-add',
	            handler: function(button, event) {
	            	grid.fireEvent("addnewrow");
				},
	            scope: this
	        });
		}
		
		,addCloneItemToolbarButton: function(grid){
			return({
				text: LN('sbi.generic.clone'),
 	            iconCls: 'icon-clone',
	            handler: function(button, event) {
	            	var selecterRecords = grid.getSelectionModel().getSelection();
	            	if(!selecterRecords || selecterRecords.length==0){
	            		Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.select.toclone'), LN('sbi.generic.serviceError'));
	            	}else{
	            		grid.fireEvent("clonerow",selecterRecords[0].data);
	            	}
				},
	            scope: this
	        });
		}
	}
	

});