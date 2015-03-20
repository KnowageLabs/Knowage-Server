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
Ext.ns("Sbi.profiling");

Sbi.profiling.ManageAttributes = function(config) { 


	var paramsGetListNew = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', MESSAGE_DET: 'ATTR_LIST', IS_NEW_ATTR : 'true'};
	this.services = new Array();
	this.services['manageAttributesNew'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_ATTRIBUTES_ACTION'
		, baseParams: paramsGetListNew
	});
	
	var paramsGetList = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', MESSAGE_DET: 'ATTR_LIST'};
	this.services['manageAttributes'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_ATTRIBUTES_ACTION'
		, baseParams: paramsGetList
	});
		
	var paramsDelete = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', MESSAGE_DET: 'ATTR_DELETE'};
	this.services['manageAttributesDelete'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'MANAGE_ATTRIBUTES_ACTION'
			, baseParams: paramsDelete
		});
	
		// Typical JsonReader.  Notice additional meta-data params for defining the core attributes of your json-response
	this.reader = new Ext.data.JsonReader({
		    totalProperty: 'total',
		    successProperty: 'success',
		    idProperty: 'id',
		    root: 'samples',
		    messageProperty: 'message'  // <-- New "messageProperty" meta-data
		}, [
		    {name: 'id'},
		    {name: 'name'},
		    {name: 'description'}
	]);

	// The new DataWriter component.
	this.writer = new Ext.data.JsonWriter({
	    encode: false   // <-- don't return encoded JSON -- causes Ext.Ajax#request to send data using jsonData config rather than HTTP params
	});
	this.httpproxy = new Ext.data.HttpProxy({
		url: this.services['manageAttributes'],
        scope: this,
		success: function(response, o){

			var content = Ext.util.JSON.decode( response.responseText );
			if(content.newAttr !== undefined && content.newAttr){

				var respObj = o.reader.read(response);

				var attrID = content.id;

				if(attrID != null && attrID !==''){

					if(content.message !==''){
						//store.commitChanges();
		                Ext.MessageBox.show({
		                    title: LN('sbi.attributes.ok'),
		                    msg: LN('sbi.attributes.ok.msg'),
		                    width: 200,
		                    buttons: Ext.MessageBox.OK
		               });
					}
				}
			}
		}
		,listeners: {
			'exception': function(proxy, type, action, options, response, arg){	
				try{
					var content = Ext.util.JSON.decode( response.responseText );

					if(content !== undefined){
    					if(content.success){
        					var attrID = content.id;

        					if(attrID != null && attrID !==''){
        						rec.set('id', attrID);
        						this.store.commitChanges();
	    						if(content.message !==''){
	    							this.store.commitChanges();
					                Ext.MessageBox.show({
					                    title: LN('sbi.attributes.ok'),
					                    msg: LN('sbi.attributes.ok.msg'),
					                    width: 200,
					                    buttons: Ext.MessageBox.OK
					               });
	    						}
        					}
    					}else if(!content.success){
			                Ext.MessageBox.show({
			                    title: LN('sbi.attributes.error'),
			                    msg: content.message,
			                    width: 400,
			                    buttons: Ext.MessageBox.OK
			               });

    					}
					}else{
		                Ext.MessageBox.show({
		                    title: LN('sbi.attributes.error'),
		                    msg: LN('sbi.attributes.error.msg'),
		                    width: 400,
		                    buttons: Ext.MessageBox.OK
		               }); 
					}
				}catch(exception){
					return;
				}
			}

        }
	});
	// Typical Store collecting the Proxy, Reader and Writer together.
	this.store = new Ext.data.Store({
	    id: 'user',
		storeId:'storeattr1',
	    scope:this,
	    restful: true,     // <-- This Store is RESTful
	    proxy: this.httpproxy,
	    reader: this.reader,
	    writer: this.writer    // <-- plug a DataWriter into the store just as you would a Reader
	});
	

	// load the store immeditately
	this.store.load();

	
	// Let's pretend we rendered our grid-columns with meta-data from our ORM framework.
	this.userColumns =  [
	    {header: LN('sbi.attributes.headerName'), 
	    	id:'name',
	    	width: 250, 
	    	sortable: true, dataIndex: 'name', editor: 
	    		new Ext.form.TextField({
	    			id:'aa'
	    				})},
	    {header: LN('sbi.attributes.headerDescr'), width: 250, 
				id:'description',
				sortable: true, dataIndex: 'description',  
				editor: new Ext.form.TextField({
					id:'bb'})}
	];

	 // use RowEditor for editing
    this.editor = new Ext.ux.grid.RowEditor({
        saveText: LN('sbi.attributes.update')
    });

    
    
    this.editor.on({
  			scope: this,
  			afteredit: function() {
 				this.store.commitChanges();
 				//this.store.save();
		    },
		    exceptionOnValidate: function(){
		    	this.validationErrors();
		    }
		});


    // Create a typical GridPanel with RowEditor plugin
    
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
        }), '-'
    	]
    });
    
    var c = Ext.apply( {}, config, {
        frame: true,
        title: LN('sbi.attributes.title'),
        autoScroll: true,
        height: 400,
        width: 600,
        store: this.store,
        plugins: [this.editor],
        columns : this.userColumns,
        tbar: tb,
        viewConfig: {
            forceFit: true
        }
        ,renderTo: Ext.getBody()
    });


    // constructor
    Sbi.profiling.ManageAttributes.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.profiling.ManageAttributes, Ext.grid.GridPanel, {
  
  	reader:null
  	,services:null
  	,writer:null
  	,store:null
  	,userColumns:null
  	,editor:null
  	,userGrid:null
	/**
     * onAdd
     */
    ,onAdd: function (btn, ev) {
        var u = new this.store.recordType({
           name: '',
           description : ''
        });
        this.store.proxy.setUrl(this.services['manageAttributesNew']);

        this.editor.stopEditing();
        this.store.insert(0, u);

        this.editor.startEditing(0);
        //this.store.commitChanges();
        

    }
    /**
     * onDelete
     */
    ,onDelete: function() {
        var rec = this.getSelectionModel().getSelected();

        var remove = true;

        this.store.proxy = new Ext.data.HttpProxy({
				url: this.services['manageAttributesDelete']
				, listeners: {
					'exception': function(proxy, type, action, options, response, arg){	    	
        				var content = Ext.util.JSON.decode( response.responseText );
        				if(content == undefined || !content.success){
			                Ext.MessageBox.show({
			                    title: LN('sbi.attributes.error'),
			                    msg: LN('sbi.attributes.error.msg'),
			                    width: 400,
			                    buttons: Ext.MessageBox.OK
			                });

			               remove = false;
        				}else{
			                Ext.MessageBox.show({
			                    title: LN('sbi.attributes.ok'),
			                    msg: LN('sbi.attributes.ok.msg'),
			                    width: 400,
			                    buttons: Ext.MessageBox.OK
			                });
        				}
					}
    				
		        }
		        ,scope: this
			});
        this.store.remove(rec);
        this.store.commitChanges();

        if(!remove){
        	//readd record
            this.store.add(rec);
            this.store.commitChanges();
        }


        this.store.proxy = new Ext.data.HttpProxy({
			url: this.services['manageAttributes']
        });
     }


});

Ext.reg('manageattr', Sbi.profiling.ManageAttributes);
