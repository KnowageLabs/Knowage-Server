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
 * 
 * Public Properties
 * 
  * MANDATORY PARAMETERS: serviceUrl: the url for the ajax request
  * OPTIONAL:
  * 	pagingConfig:{} Object. If this object is defined the paging toollbar will be displayed
 * 
 * 
 * Public Methods
 * 
 *  [list]
 * 
 * 
 * Public Events
 * 
 *  [list]
 * 
 * Authors
 * 
 * - Alberto Ghedin (alberto.ghedin@eng.it)
 */

Ext.define('Sbi.behavioural.lov.TestLovConfigurationGridPanel', {
    extend: 'Ext.grid.Panel'

    ,config: {
      	stripeRows: true,
      	columns: []
    }

	, constructor: function(config) {
		this.title =  "Fields";
		//this.border = false;
		this.viewConfig = {
			plugins: {
				ddGroup: 'GridLovDD',
				ptype: 'gridviewdragdrop',
				enableDrop: false
			}
		};
			
		
		Sbi.debug('TestLovConfigurationGridPanel costructor IN');
		Ext.apply(this,config);
		
    	//this.width = 200;
		this.flex = 1;
    	this.height = 200;
		
		this.store  = Ext.create('Ext.data.Store', {
		    fields: ['name', 'isValue', 'isDescription', 'isVisible'],
		    //data : [{'name':'a','isValue':false, 'isDescription':true, 'isVisible':false }]
		    data : [{}]
		});

		this.columnsDefinition = [{
            header: LN('sbi.behavioural.lov.name'),
            dataIndex: 'name',
            flex: 1
        }];
		
		if(config.lovType.indexOf("tree")<0){
			this.columnsDefinition.push({
	            xtype: 'radiocolumn',
	            header: LN('sbi.behavioural.lov.value'),
	            dataIndex: 'isValue',
	            width: 90
	        });
			this.columnsDefinition.push({
	            xtype: 'radiocolumn',
	            header: LN('sbi.behavioural.lov.description'),
	            dataIndex: 'isDescription',
	            width: 90
	        });
			this.columnsDefinition.push({
	            xtype: 'checkcolumn',
	            header: LN('sbi.behavioural.lov.visible'),
	            dataIndex: 'isVisible',
	            width: 90,
	            editor: {
	                xtype: 'checkbox',
	                cls: 'x-grid-checkheader-editor'
	            }});
		}
		
		this.columns = this.columnsDefinition.slice(0,this.columnsDefinition.length);
		
		this.store.load();
    	this.callParent(arguments);
    	Sbi.debug('TestLovConfigurationGridPanel costructor OUT');
    	


	}
	
	,onParentStroreLoad: function(){
		var fields = this.parentStore.proxy.reader.jsonData.metaData.fields;
		if(fields!=null && fields!=undefined && fields.length>0){
			var data = [];
			for(var i=0; i<fields.length; i++){
				var aData = {};
				aData.name = fields[i].name;
				data.push(aData);
			}
			this.setValues(data);
			this.store  = Ext.create('Ext.data.Store', {
				 fields: ['name', 'isValue', 'isDescription', 'isVisible'],
			    data : data
			});
			this.store.load();
			this.columns = this.columnsDefinition.slice(0,this.columnsDefinition.length);
			this.reconfigure(this.store);
		}
	}
	
	, getValues: function(){
		var value;
		var descriptions;
		var visible =[]; 
		var data = this.store.data;
		if(data!=null && data!=undefined && data.items!=null && data.items!=undefined ){
			for(var i=0; i<data.items.length; i++){
				var aItem = data.items[i];
				if(aItem.data.isValue){
					value = aItem.data.name;
				}
				if(aItem.data.isDescription){
					description = aItem.data.name;
				}
				if(aItem.data.isVisible){
					visible.push(aItem.data.name);
				}
			}
		}
		
		if(!value){
			 Sbi.exception.ExceptionHandler.showErrorMessage(LN("sbi.behavioural.lov.missingValueColumn"));
			 return null;
		}
		
		var LOVConfiguration = {
				valueColumnName:value,
				descriptionColumnName:description,
				visibleColumnNames:visible,
				lovType: this.lovType,
				column: this.column
		}
		
		
		return LOVConfiguration;
		
	}

	, setValues: function(data){
		this.column = [];
		if(data!=null && data!=undefined && this.lovConfig!=null && this.lovConfig!=undefined){
			for(var i=0; i<data.length; i++){
				var aItem = data[i];
				if(aItem.name == this.lovConfig.valueColumnName){
					aItem.isValue = true;
				}else{
					aItem.isValue = false;
				}
				if(aItem.name == this.lovConfig.descriptionColumnName ){
					aItem.isDescription = true;
				}else{
					aItem.isDescription = false;
				}
				if(this.lovConfig.visibleColumnNames.indexOf(aItem.name)>=0){
					aItem.isVisible = true;
				}else{
					aItem.isVisible = false;
				}
				this.column.push(aItem.name);
			}
		}
	}
	
	
});

