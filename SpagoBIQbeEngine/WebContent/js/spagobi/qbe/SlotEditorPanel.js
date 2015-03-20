/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  
 
/**
  * SlotEditorPanel - short description
  * 
  * Object documentation ...
  * 
  * by Monica Franceschini
  */

Ext.ns("Sbi.qbe");

Sbi.qbe.SlotEditorPanel = function(config) {	
	
	var c = Ext.apply({}, config || {}, {
		layout: 'fit'
	});

	Ext.apply(this, c);
	
	var params = c.datamart !== undefined ? {'datamartName': c.datamart} : {};
	this.services = this.services || new Array();	
	
	this.services['addCalculatedField'] = this.services['addCalculatedField'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'ADD_CALCULATED_FIELD_ACTION'
		, baseParams: params
		});
	
	this.services['getValuesForQbeFilterLookupService'] = Sbi.config.serviceRegistry.getServiceUrl({
		  serviceName: 'GET_VALUES_FOR_QBE_FILTER_LOOKUP_ACTION'
		, baseParams: params
	});
	
	this.fieldId = c.fieldId;//an be null if click on entity node
	this.firstPage = c.firstPage;
	this.slotWizard = c.slotWizard;
	
	this.initStore(c);
	this.initToolbar(c);
	this.initGrid(c);
	
	Ext.apply(c, {
		tbar: this.panelToolbar,
		items:  [this.gridPanel]
	});	
	
	// constructor
	Sbi.qbe.SlotEditorPanel.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.qbe.SlotEditorPanel, Ext.Panel, {
   
    gridPanel: null 
    , panelToolbar: null
    , valuesItemTemplate: null
    , punctualWindow : null
    , rangeWindow : null
    , rangeToSave : null
    , store: null
    , hasDefault: false
    , fieldId: null
    , firstPage : null
    , expression: null
    , slotWizard: null
    
	, initToolbar: function(c){
	
		this.panelToolbar = new Ext.Toolbar({
			scope: this,
			items: [{
                xtype:'button',
                text: LN('sbi.qbe.bands.addband.btn'),
                iconCls: 'add'
            },{
                xtype:'button',
                text: LN('sbi.qbe.bands.adddefault.btn'),
                iconCls: 'add'
            },{
                xtype:'button',
                text: LN('sbi.qbe.bands.delete.btn'),
                iconCls: 'remove'
            }]
		});
	}
	, initStore: function(c){
	    // create the data store
	    if(c.editStore !== null && c.editStore !== undefined ){
	    	this.store = c.editStore;

	    }else{
		    this.store = new Ext.data.JsonStore({

		        data: {slots:[{ name: "slot 1", valueset: [{type: "range", from: 0, includeFrom: true, to: 100, includeTo: false}, { type: "punctual", values: [100, 101, 201]}]}]},
		        root: 'slots',
		        fields: ['name', 'valueset']
		    });
	    }
	    // looks for default range configuration
	    var defaultRec = null;
	    if(this.store.data != null && this.store.data !== undefined){
			for (var i = 0; i < this.store.data.length; i++) { 
				var record = this.store.getAt(i); 
				var slot = record.data; 
				if(slot.valueset !== null && slot.valueset !== undefined){
					for (var j = 0; j < slot.valueset.length; j++) {
						var val = slot.valueset[j];
						if(val.type == 'default'){
							defaultRec = slot;
							this.hasDefault = true;
							break;
						}
					}
				}
			}
	    }

	}
	, initGrid: function(c) {

        var template = new Ext.XTemplate(
        		'<tpl if="valueset !== null">'+
        		'<tpl for="valueset">'+
        		'<tpl if="type == \'range\'">'+
	        		'<div class="icon-close" id="tpl-slot-val-{[xindex]}">' + 
		        		'<div class="box-to-edit" id="tpl-edit-range-{[xindex]}">' + 
			        		'<tpl if="includeFrom == true">'+
			        			'&gt;=' + 
			        		'</tpl>'+
			        		'<tpl if="includeFrom == false">'+
			        			'&gt;' + 
			        		'</tpl>'+
			        		'{from} '+ 
			        		'<tpl if="includeTo == true">'+
			        			' &lt;=' + 
			        		'</tpl>'+
			        		'<tpl if="includeTo == false">'+
			        			' &lt;' + 
			        		'</tpl>'+
			        		'{to}'+
		        		'</div>'+
	                '</div>'+
                '</tpl>'+
                '<tpl if="type == \'punctual\'">'+
	        		'<div class="icon-close green" id="tpl-slot-val-{[xindex]}">' + 
		        		'<div class="box-to-edit" id="tpl-edit-punct-{[xindex]}">' + 
		                '{values}' + 
		                '</div>'+
      	            '</div>'+
                '</tpl>'+
                '</tpl></tpl>'
             );  
             
        template.compile();
             
        var valuesColumn = new Ext.grid.TemplateColumn({
            header   : LN('sbi.qbe.bands.col.values'), 
            dataIndex: 'valueset',
            xtype: 'templatecolumn',
            tpl : template
        });


	    // button-columns
	    var rangeButtonColumn = new Ext.grid.ButtonColumn(
		    Ext.apply({
		       dataIndex: 'range'
		       , imgSrc: '../img/actions/range.gif'
		       , clickHandler:function(e, t){
		          var index = this.scope.gridPanel.getView().findRowIndex(t);
		          if(index !== 0){
			          var record = this.scope.gridPanel.store.getAt(index);
			          this.scope.openiInsertRangeWindow(record);
			       }else{
		        	  if(this.scope.hasDefault == true){
		        		  alert(LN('sbi.qbe.bands.alert.default'));
		        	  }else{
				          var record = this.scope.gridPanel.store.getAt(index);
				          this.scope.openiInsertRangeWindow(record);
		        	  }
		          }
		       }
		       , width: 20
		       , header: LN('sbi.qbe.bands.col.limits')
		       , renderer : function(v, p, record){
		           return '<center><img class="x-mybutton-'+this.id+'" width="29px" height="16px" src="' + this.imgSrc + '"/></center>';
		       }
		       , scope: this
		    })
	    );
	    var punctualButtonColumn = new Ext.grid.ButtonColumn(
			    Ext.apply({
			       dataIndex: 'dots'
			       , imgSrc: '../img/actions/dots.gif'
			       , clickHandler:function(e, t){
			          var index = this.scope.gridPanel.getView().findRowIndex(t);
			          if(index !== 0 ){
				          var record = this.scope.gridPanel.store.getAt(index);
				          this.scope.openiInsertPunctualWindow(record);
			          }else{
			        	  if(this.scope.hasDefault == true){
			        		  alert(LN('sbi.qbe.bands.alert.default'));
			        	  }else{
					          var record = this.scope.gridPanel.store.getAt(index);
					          this.scope.openiInsertPunctualWindow(record); 
			        	  }
			        	  
			          }
			       }
			       , width: 20
			       , header:  LN('sbi.qbe.bands.col.vallist')
				   , renderer : function(v, p, record){
			           return '<center><img class="x-mybutton-'+this.id+'" width="21px" height="13px" src="' + this.imgSrc + '"/></center>';
			       }
			       , scope: this
			    })
		    );
	    var nameEditor = new Ext.form.TextField();
	    var gridViewStyled = new Ext.grid.GridView({
            forceFit: true,
            enableRowBody:true,
            showPreview:true,
            getRowClass: function(record, index, rowParams,ds) {
				var slot = record.data; 
				var att = rowParams.tstyle;
				if(slot.valueset !== null && slot.valueset !== undefined){
					for (var j = 0; j < slot.valueset.length; j++) {
						var val = slot.valueset[j];
						if(val.type === 'default' ){
							rowParams.tstyle= att+" background-color: #B0CEEE; ";
						}else if(val.type === 'range' || val.type === 'punctual'){
							rowParams.tstyle= att+" background-color: white; ";
						}else{
							rowParams.tstyle= att+" background-color: white; ";
						}
					}
				}
            }
	    });
		this.gridPanel = new Ext.grid.EditorGridPanel({
			id: 'slot-panel',
			store: this.store,
			columns: [
               {
                   id       :'name',
                   header   :  LN('sbi.qbe.bands.col.name'), 
                   sortable : true, 
                   editor: nameEditor,
                   dataIndex: 'name'
               },
               	   valuesColumn
               	,  rangeButtonColumn
               	,  punctualButtonColumn
               ],
	        tbar: this.toolbar,
	        clicksToEdit:2,
	        frame: true,
	        border:true,  
	        style:'padding:0px',
	        iconCls:'icon-grid',
	        collapsible:false,
	        layout: 'fit',
	        view: gridViewStyled,
	        plugins :[rangeButtonColumn,  punctualButtonColumn],
	        enableDragDrop:false,
	        scope: this,
	        listeners:{
	        	 scope: this,
	        	 //afterrender: this.setRowStyle,
	        	 cellclick: function(grid, rowIndex, columnIndex, e) {
	        			// Get the Record for the row
	        	        var record = grid.getStore().getAt(rowIndex);
	        	        // Get field name for the column
	        	        var fieldName = grid.getColumnModel().getDataIndex(columnIndex);
	        	    	var slotItem = e.getTarget();
	        	    	var id = slotItem.id;
	        	    	if(id !== undefined && id != null && id !== ''){
		        	    	var istodelete = id.indexOf('tpl-slot-val-');
		        	    	var istoedit = id.indexOf('tpl-edit-range-');
		        	    	var istoeditP = id.indexOf('tpl-edit-punct-');
		        	    	if(istodelete !== -1){
			        	    	var itemIdx = id.substring(istodelete + ('tpl-slot-val-'.length));
			        	    	var valuesSets = record.data.valueset;
			        	    	try{
	
			        	    		Ext.MessageBox.show({
			        	    			title : LN('sbi.qbe.bands.delete.alert.title'),
			        	    			msg : LN('sbi.qbe.bands.delete.alert.msg'),
			        	    		   	buttons: Ext.Msg.YESNO,
			        	    		   	fn: function(btn) {
			        	    				if(btn === 'yes') {
			    			        	    	var idx = parseInt(itemIdx) ;
							        	    	var toremove = record.data.valueset[idx-1];
							        	    	record.data.valueset.remove(toremove);
							        	    	record.commit();
			        	    				}
			        	    			},
			        	    			scope: this
			        	    		});
			        	    	}catch(err){
			        	    		
			        	    	}
		        	    	}else if(istoedit !== -1){
		        	    		var itemIdx = id.substring(istoedit + ('tpl-edit-range-'.length));
			        	    	var valuesSets = record.data.valueset;
			        	    	var idx = parseInt(itemIdx) ;
			        	    	var valpos = idx-1;
			        	    	var toedit = record.data.valueset[valpos];
			        	    	this.openiInsertRangeWindow(record, toedit, valpos);
		        	    	}else if(istoeditP !== -1){
		        	    		var itemIdx = id.substring(istoeditP + ('tpl-edit-punct-'.length));
			        	    	var valuesSets = record.data.valueset;
			        	    	var idx = parseInt(itemIdx) ;
			        	    	var valpos = idx-1;
			        	    	var toedit = record.data.valueset[valpos];
			        	    	var values = toedit.values;
		        	    		this.openiInsertPunctualWindow(record, toedit.values, valpos);
		        	    	}
	        	    	}
	        	 }
	        }

	    });

		var btnAdd = this.panelToolbar.items.items[0];
		var btnDefault = this.panelToolbar.items.items[1];
		var btnDelete = this.panelToolbar.items.items[2];
		btnAdd.on('click', this.createSlotRowToDisplay, this);
		btnDelete.on('click', this.removeSlot, this);
		btnDefault.on('click', this.createDefault, this);


	}
	, openiInsertRangeWindow: function(rec, toedit, idx){

		this.expression = this.slotWizard.getExpression();
		this.rangeWindow = new Sbi.qbe.RangeDefinitionWindow({slotPanel: this, 
																record: rec, 
																id: this.fieldId, 
																expression: this.expression, 
																toedit: toedit, 
																idx: idx});
		
		this.rangeWindow.mainPanel.doLayout();
		this.rangeWindow.show();
	}
	, openiInsertPunctualWindow: function(rec, vals, idx){
		this.expression = this.slotWizard.getExpression();
		var lookupStore = this.createLookupStore();		
		lookupStore.load();
		var baseConfig = {
	       store: lookupStore
	     , singleSelect: false
	     , valuesSeparator: Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator
		};
		this.punctualWindow = new Sbi.widgets.FilterLookupPopupWindow(baseConfig);
		this.punctualWindow.on('selectionmade', function(xselection) {
			this.addPunctualVals(xselection.xselection.Values, rec, idx);	
			this.punctualWindow.close();
		}, this);
		if(vals !== undefined && vals !== null && vals.length !== 0){
			this.punctualWindow.setSelection(vals);
		}
		this.punctualWindow.show();
	}
	, createSlotRowToDisplay: function(p){
        // access the Record constructor through the grid's store
		var Slot = this.gridPanel.getStore().recordType;
        var p = new Slot({
            name: LN('sbi.qbe.bands.new.name'),
            valueset: null
        });
        this.gridPanel.stopEditing();
        this.gridPanel.store.add(p);
        var idx = this.gridPanel.store.indexOf(p);
        this.gridPanel.startEditing(idx, 0);
	}
	, createDefault: function(){
        // access the Record constructor through the grid's store

        if(this.hasDefault == false){
			var Slot = this.gridPanel.getStore().recordType;
	        var p = new Slot({
	            name: LN('sbi.qbe.bands.default.name'),
	            valueset: [{type: "default", value: ''}]
	        });
	        this.gridPanel.stopEditing();
	        this.store.insert(0, p);
	        this.gridPanel.startEditing(0, 0);
	        this.hasDefault = true;
	        this.gridPanel.getView().refresh();
	        
        }else{
        	alert(LN('sbi.qbe.bands.default.alert'));
        }

	}

	, removeSlot: function(){
        // access the Record constructor through the grid's store
		var slot = this.gridPanel.selModel.selection.record;
        if(slot !== null && slot !== undefined){
        	var idx = this.gridPanel.store.indexOf(slot);
        	if(idx == 0){
        		this.hasDefault = false;
        	}
            this.gridPanel.store.remove(slot);
            this.gridPanel.store.commitChanges();
        }

	}
	, addRange: function(rowIndex, rec, idx){
		var opFrom = rowIndex.from.operand ;
		var includeFrom = false;
		if(opFrom == 2){
			includeFrom = true;
		}
		var opTo = rowIndex.to.operand;
		var includeTo = false;
		if(opTo == 4){
			includeTo = true;
		}
		var item ={type: 'range', from: rowIndex.from.value, includeFrom: includeFrom, to: rowIndex.to.value, includeTo: includeTo};

		if(rec.data.valueset == null){
			rec.data.valueset = new Array();
		}
		if(idx !== undefined){
			// edit mode
			rec.data.valueset[idx] = item;
		}else{
			rec.data.valueset.push(item);
		}

		rec.commit();
    }
	, addPunctualVals: function(vals, rec, idx){
		var item ={type: 'punctual', values: vals};
		if(rec.data.valueset == null){
			rec.data.valueset = new Array();
		}
		if(idx !== undefined){
			// edit mode
			rec.data.valueset[idx] = item;
		}else{
			rec.data.valueset.push(item);
		}
		rec.commit();
    }

	
	, createLookupStore: function() {
		var createStoreUrl = this.services['getValuesForQbeFilterLookupService'];
		
		var params = {};
		
		if (this.fieldId !== null) createStoreUrl += '&ENTITY_ID=' + this.fieldId;
		if (this.expression !== null) {
			params.fieldDescriptor = Ext.util.JSON.encode({expression: this.expression});
		}
		
		var store = new Ext.data.JsonStore({
			url: createStoreUrl
		});
		
		store.on('beforeload', function(store, options) {
			options =  Ext.apply(options.params, params);
		});
		
		store.on('loadexception', function(store, options, response, e) {
			var msg = '';
			var content = Ext.util.JSON.decode( response.responseText );
  			if(content !== undefined) {
  				msg += content.serviceName + ' : ' + content.message;
  			} else {
  				msg += 'Server response is empty';
  			}
	
			Sbi.exception.ExceptionHandler.showErrorMessage(msg, response.statusText);
		});
		return store;	
	}
});

