/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  

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
 * Authors - Chiara Chiarelli
 */
Ext.ns("Sbi.kpi");

Sbi.kpi.ManagePeriodicities = function(config) { 
	
	var paramsList = {MESSAGE_DET: "PERIODICTIES_LIST"};
	var paramsSave = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "PERIODICITY_INSERT"};
	var paramsDel = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "PERIODICITY_DELETE"};
	
	this.services = new Array();
	
	this.services['managePrListService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_PERIODICITIES_ACTION'
		, baseParams: paramsList
	});
	
	this.services['savePrService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_PERIODICITIES_ACTION'
		, baseParams: paramsSave
	});
	
	this.services['deletePrService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_PERIODICITIES_ACTION'
		, baseParams: paramsDel
	});
	
	this.store = new Ext.data.JsonStore({
    	autoLoad: false    	  
    	, id : 'id'		
        , fields: ['idPr'
         	          , 'name'
          	          , 'months'
          	          , 'days'
          	          , 'hours'
          	          , 'mins'
          	          ]
    	, root: 'rows'
		, url: this.services['managePrListService']		
	});
	
	var monthsStore = new Ext.data.SimpleStore({
	    fields: ['months'],
  	    autoLoad: false,
	    data:[[0],[1],[2],[3],[4],[5],[6],[7],[8],[9],[10],[11],[12]]
	});
	
	var daysStore = new Ext.data.SimpleStore({
	    fields: ['days'],
  	    autoLoad: false,
  	    data:[[0],[1],[2],[3],[4],[5],[6],[7],[8],[9],[10],
  	          [11],[12],[13],[14],[15],[16],[17],[18],[19],[20],
  	          [21],[22],[23],[24],[25],[26],[27],[28],[29],[30],[31]]
	});
	
	var hoursStore = new Ext.data.SimpleStore({
	    fields: ['hours'],
  	    autoLoad: false,
  	    data:[[0],[1],[2],[3],[4],[5],[6],[7],[8],[9],[10],
  	          [11],[12],[13],[14],[15],[16],[17],[18],[19],[20],
  	          [21],[22],[23],[24]]
	});
	
	var minsStore = new Ext.data.SimpleStore({
	    fields: ['mins'],
  	    autoLoad: false,
  	    data:[[0],[1],[2],[3],[4],[5],[6],[7],[8],[9],[10],
  	          [11],[12],[13],[14],[15],[16],[17],[18],[19],[20],
  	          [21],[22],[23],[24],[25],[26],[27],[28],[29],[30],
  	          [31],[32],[33],[34],[35],[36],[37],[38],[39],[40],
  	          [41],[42],[43],[44],[45],[46],[47],[48],[49],[50],
  	          [51],[52],[53],[54],[55],[56],[57],[58],[59],[60]]
	});
	
	// Let's pretend we rendered our grid-columns with meta-data from our ORM framework.
	this.userColumns =  [
	    {
	        name: 'idPr',
	        hidden: true
	    },{
	    	header:  LN('sbi.generic.name'), 
	    	id:'name',
	    	width: 80, 
	    	sortable: true, 
	    	dataIndex: 'name', 
	    	editor: new Ext.form.TextField({
	    		  allowBlank: false,
	    		  maxLength:400,
	         	  minLength:1,
	         	  validationEvent:true
	    	})	
	    },{
	    	header: LN('sbi.kpis.months'), 
	    	width: 60, 
			id:'months',
			sortable: true, 
			dataIndex: 'months',  
			editor: new Ext.form.ComboBox({
	        	  name: 'months',
	              store: monthsStore,
	              displayField: 'months',   // what the user sees in the popup
	              valueField: 'months',        // what is passed to the 'change' event
	              typeAhead: true,
	              forceSelection: true,
	              mode: 'local',
	              triggerAction: 'all',
	              selectOnFocus: true,
	              editable: false,
	              allowBlank: false,
	              validationEvent:true
	          })
	    },{
	    	header: LN('sbi.kpis.days'), 
	    	width: 60, 
			id:'days',
			sortable: true, 
			dataIndex: 'days',  
			editor: new Ext.form.ComboBox({
	        	  name: 'days',
	              store: daysStore,
	              displayField: 'days',   // what the user sees in the popup
	              valueField: 'days',        // what is passed to the 'change' event
	              typeAhead: true,
	              forceSelection: true,
	              mode: 'local',
	              triggerAction: 'all',
	              selectOnFocus: true,
	              editable: false,
	              allowBlank: false,
	              validationEvent:true
	          })				
		},{
			header: LN('sbi.kpis.hours'), 
			width: 60, 
			sortable: true, 
			dataIndex: 'hours',
			editor: new Ext.form.ComboBox({
	        	  name: 'hours',
	              store: hoursStore,
	              displayField: 'hours',   // what the user sees in the popup
	              valueField: 'hours',        // what is passed to the 'change' event
	              typeAhead: true,
	              forceSelection: true,
	              mode: 'local',
	              triggerAction: 'all',
	              selectOnFocus: true,
	              editable: false,
	              allowBlank: false,
	              validationEvent:true
	          })
		},{
			header: LN('sbi.kpis.mins'), 
			width: 60, 
			id:'mins',
			sortable: true, 
			dataIndex: 'mins',  		
			editor: new Ext.form.ComboBox({
	        	  name: 'mins',
	              store: minsStore,
	              displayField: 'mins',   // what the user sees in the popup
	              valueField: 'mins',        // what is passed to the 'change' event
	              typeAhead: true,
	              forceSelection: true,
	              mode: 'local',
	              triggerAction: 'all',
	              selectOnFocus: true,
	              editable: false,
	              allowBlank: false,
	              validationEvent:true
	          })
		}		
	];
    
	 var cm = new Ext.grid.ColumnModel({
	        // specify any defaults for each column
	        defaults: {
	            sortable: true // columns are not sortable by default           
	        },
	        columns: this.userColumns
	    });
	 
	 var tb = new Ext.Toolbar({
	    	buttonAlign : 'left',
	    	items:[new Ext.Toolbar.Button({
	            text: LN('sbi.attributes.add'),
	            iconCls: 'icon-add',
	            handler: this.onAdd,
	            width: 30,
	            scope: this
	        }), '-', new Ext.Toolbar.Button({
	            text: LN('sbi.attributes.delete'),
	            iconCls: 'icon-remove',
	            handler: this.onDelete,
	            width: 30,
	            scope: this
	        }), '-', new Ext.Toolbar.Button({
	            text: Sbi.locale.ln['sbi.generic.update'],
	            iconCls: 'icon-save',
	            handler: this.onSave,
	            width: 30,
	            scope: this
	        })
	    	]
	    });
	 
		 var sm = new Ext.grid.RowSelectionModel({
	         singleSelect: true
	     });

	    // create the editor grid
	    var grid = {
	    	xtype: 'grid',
	        store: this.store,
	        layout: 'fit',
	        cm: cm,
	        sm: sm,
	        width: 400,
	        height: 250,
	        //autoExpandColumn: 'label', // column with this id will be expanded
	        frame: true,
	        clicksToEdit: 2,
	        tbar: tb
	    };

    var c = Ext.apply( {}, config, grid);

    // constructor
    Sbi.kpi.ManagePeriodicities.superclass.constructor.call(this, c);
	
    this.store.load();

};

Ext.extend(Sbi.kpi.ManagePeriodicities, Ext.grid.EditorGridPanel, {
  
	
  	reader:null
  	,currentRowRecordEdited:null
  	,services:null
  	,writer:null
  	,store:null
  	,userColumns:null
  	,editor:null
  	,userGrid:null
  	,severityStore:null

    ,onAdd: function (btn, ev) {

        var emptyRecToAdd = new Ext.data.Record({
        	  idPr: 0,
        	  name: '',
        	  months: 0,
        	  days: 0,
        	  hours: 0,
        	  mins: 0 
			 });   
        this.store.insert(0,emptyRecToAdd);
    }

    ,onDelete: function() {
        var rec = this.getSelectionModel().getSelected();
        var itemId = rec.get('idPr');
        if (itemId != null && itemId!=undefined && itemId!=0){
        Ext.MessageBox.confirm(
    			LN('sbi.generic.pleaseConfirm'),
    			LN('sbi.generic.confirmDelete'),            
                function(btn, text) {
                    if (btn=='yes') {
                    	if (itemId != null) {	

    						Ext.Ajax.request({
    				            url: this.services['deletePrService'],
    				            params: {'idPr': itemId},
    				            method: 'GET',
    				            success: function(response, options) {
    								if (response !== undefined) {
    									var deleteRow = this.getSelectionModel().getSelected();
    									this.store.remove(deleteRow);
    									this.store.commitChanges();
    								} else {
    									Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.deletingItemError'), LN('sbi.generic.serviceError'));
    								}
    				            },
    				            failure: function() {
    				                Ext.MessageBox.show({
    				                    title: LN('sbi.generic.error'),
    				                    msg: LN('sbi.generic.deletingItemError'),
    				                    width: 150,
    				                    buttons: Ext.MessageBox.OK
    				               });
    				            }
    				            ,scope: this
    			
    						});
    					} else {
    						Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.generic.error.msg'),LN('sbi.generic.warning'));
    					}
                    }
                },
                this
    		);
        }else{
        	this.store.remove(rec);
            this.store.commitChanges();
        }

     }
    
    , saveElement: function(rec,params){
    	Ext.Ajax.request({
            url: this.services['savePrService'],
            params: params,
            method: 'GET',
            success: function(response, options) {
				if (response !== undefined) {			
		      		if(response.responseText !== undefined) {

		      			var content = Ext.util.JSON.decode( response.responseText );
		      			if(content.responseText !== 'Operation succeded') {
			                    Ext.MessageBox.show({
			                        title: LN('sbi.generic.error'),
			                        msg: content,
			                        width: 150,
			                        buttons: Ext.MessageBox.OK
			                   });
			      		}else{
			      			var itemId = content.id;			      			
			      			
			      			if(itemId != null && itemId !==''){
			      				rec.set('idPr', itemId); 
			      			}
			      			rec.commit();

			      		}      				 

		      		} else {
		      			Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.serviceResponseEmpty'), LN('sbi.generic.serviceError'));
		      		}
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.savingItemError'), LN('sbi.generic.serviceError'));
				}
            },
            failure: function(response) {
	      		if(response.responseText !== undefined) {
	      			var content = Ext.util.JSON.decode( response.responseText );
	      			var errMessage ='';
					for (var count = 0; count < content.errors.length; count++) {
						var anError = content.errors[count];
	        			if (anError.localizedMessage !== undefined && anError.localizedMessage !== '') {
	        				errMessage += anError.localizedMessage;
	        			} else if (anError.message !== undefined && anError.message !== '') {
	        				errMessage += anError.message;
	        			}
	        			if (count < content.errors.length - 1) {
	        				errMessage += '<br/>';
	        			}
					}

	                Ext.MessageBox.show({
	                    title: LN('sbi.generic.validationError'),
	                    msg: errMessage,
	                    width: 400,
	                    buttons: Ext.MessageBox.OK
	               });
	      		}else{
	                Ext.MessageBox.show({
	                    title: LN('sbi.generic.error'),
	                    msg: LN('sbi.generic.savingItemError'),
	                    width: 150,
	                    buttons: Ext.MessageBox.OK
	               });
	      		}
            }
            ,scope: this
        });
    }
    
    ,onSave : function() {
    	var modifRecs = this.store.getModifiedRecords();
    	var length = modifRecs.length;
    	
		for(var i=0;i<length;i++){
			
   	        var rec = modifRecs[i];	
	    	var idRec = rec.get('idPr');
	
	        var params = {
	        	name :  rec.get('name'),
	        	months : rec.get('months'),
	        	days : rec.get('days'),
	        	hours : rec.get('hours'),
	        	mins : rec.get('mins')
	        };
	        
	        if(idRec){
	        	params.idPr = idRec;
	        }
	        this.saveElement(rec, params);
	        
		}
	}
    
});

