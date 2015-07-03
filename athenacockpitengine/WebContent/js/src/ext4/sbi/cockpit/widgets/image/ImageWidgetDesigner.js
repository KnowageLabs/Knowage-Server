/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


Ext.ns("Sbi.cockpit.widgets.image");


Sbi.cockpit.widgets.image.ImageWidgetDesigner = function(config) {

	var defaultSettings = {
		name: 'imageWidgetDesigner',
		title: LN('sbi.cockpit.widgets.image.imageWidgetDesigner.title'),
	};

	
	if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.widgets && Sbi.settings.cockpit.widgets.image && Sbi.settings.cockpit.widgets.image.imageWidgetDesigner) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.cockpit.widgets.image.imageWidgetDesigner);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);

	this.initImagePanel();
	
	c = {
		layout: 'fit',
		height: 350,
		items: [this.imagePanel]
	};

	Sbi.cockpit.widgets.image.ImageWidgetDesigner.superclass.constructor.call(this, c);

	this.on(
		'beforerender' ,
		function (thePanel, attribute) {
			var state = {};
			state.itemSelected = thePanel.itemSelected;
			state.wtype = 'image';
			this.setDesignerState(state);
		},
		this
	);
};

Ext.extend(Sbi.cockpit.widgets.image.ImageWidgetDesigner, Sbi.cockpit.core.WidgetDesigner, {
	imagePanel: null

	, getDesignerState: function(running) {
		Sbi.trace("[ImageWidgetDesigner.getDesignerState]: IN");

		var state = Sbi.cockpit.widgets.image.ImageWidgetDesigner.superclass.getDesignerState(this);
		state.designer = 'Image Designer';
		state.wtype = 'image';
		
		state.itemSelected = this.imagePanel.itemSelected;
		
		Sbi.trace("[ImageWidgetDesigner.getDesignerState]: OUT");
		return state;
	}

	, setDesignerState: function(state) {
		Sbi.trace("[ImageWidgetDesigner.setDesignerState]: IN");
		Sbi.cockpit.widgets.image.ImageWidgetDesigner.superclass.setDesignerState(this, state);
		if(state.itemSelected) this.imagePanel.itemSelected = state.itemSelected;
		
		Sbi.trace("[ImageWidgetDesigner.setDesignerState]: OUT");
	}

	, validate: function(validFields){
		Sbi.trace("[ImageWidgetDesigner.validate]");
		//TODO verificare che itemSelected sia valorizzato
		return Sbi.cockpit.widgets.image.ImageWidgetDesigner.superclass.validate(this, validFields);
	}

	, initImagePanel: function (){
		var imageStore = Ext.create('Ext.data.Store', {
		    storeId:'imagesDataStore',
		    proxy: {
		        type: 'ajax',
		        url: Sbi.config.contextName+'/restful-services/1.0/images/listImages',
		        method: 'GET',
		        params: {'SBI_EXECUTION_ID': Sbi.config.SBI_EXECUTION_ID, 'user_id':Sbi.config.userId},
		        reader: {
		            type: 'json',
		            root: 'data'
		        }
		    },
		    model: 'ImageDataModel'
		});
		imageStore.load();
		this.imagePanel = Ext.create('Ext.Panel', {
			id: 'mainImagePanel',
	        itemSelected: null,
//	        frame: true,
	        layout: {type: 'hbox',align: 'stretch'},
	        title: LN('sbi.cockpit.widgets.image.imageWidgetDesigner.selectItem'),
	        refreshPanelTitle: function(){
	    		if(this.itemSelected){
	    			this.setTitle(LN('sbi.cockpit.widgets.image.imageWidgetDesigner.itemSelected')+' '+this.itemSelected.name);
	    		}else{
	    			this.setTitle(LN('sbi.cockpit.widgets.image.imageWidgetDesigner.selectItem'));
	    		}
	    	},
	        items: [
				Ext.create('Ext.view.View', {
					id: 'galleryView',
					flex: 3,
				    store: imageStore,
				    tpl: [
				        '<tpl for=".">',
				            '<div class="thumb-wrap" id="{name}" style="float:left;margin:4px;margin-right:0;padding:5px;">',
				            '<div class="thumb" style="min-height:80px;"><img src="{urlPreview}" title="{name}"></div>',
				            '<span >{shortName}</span></div>',
				        '</tpl>',
				        '<div class="x-clear"></div>'
				    ],
				    multiSelect: false,
				    autoScroll: true,
				    overItemCls: 'x-item-over',
				    selectedItemCls: 'x-item-selected',
				    itemSelector: 'div.thumb-wrap',
				    emptyText: LN('sbi.cockpit.widgets.image.imageWidgetDesigner.noRecords'),
				    plugins: [{ptype:'gridviewdragdrop',enableDrop:false,dragGroup:'imageDragDrop'}],
				    prepareData: function(data) {
				        Ext.apply(data, {
				            shortName: Ext.util.Format.ellipsis(data.name, 15),
				            sizeString: Ext.util.Format.fileSize(data.size),
				            dateString: Ext.util.Format.date(data.lastmod, "d/m/Y g:i a")
				        });
				        return data;
				    },
				    listeners: {
				        selectionchange: function(dv, nodes )
				    	{
				    		var imagePanel = this.up('panel');
				            if(nodes.length > 0){
				            	imagePanel.itemSelected = nodes[0].data;
				            }else{
				            }
				            imagePanel.refreshPanelTitle();
				        }
				    }
				}),
				{xtype: 'panel',
				type: 'vbox',
				frame: true,
				flex:1,
				items:[
					Ext.create('Ext.form.Panel', {
					    frame: true,
					    items: [{
					        xtype: 'filefield',
					        name: 'uploadedImage',
					        msgTarget: 'side',
					        allowBlank: false,
					        buttonText: LN('sbi.cockpit.widgets.image.imageWidgetDesigner.selectImage')
					    }],
					    buttons: [{
					        text: LN('sbi.cockpit.widgets.image.imageWidgetDesigner.buttonUpload'),
					        handler: function() {
					            var form = this.up('form').getForm();
					            if(form.isValid()){
					            	var params = {
					                    url: Sbi.config.contextName+'/restful-services/1.0/images/addImage',
					                    waitMsg: LN('sbi.cockpit.widgets.image.imageWidgetDesigner.uploadingImage'),
					                    success: function(fp, o) {
					                    	Ext.data.StoreManager.lookup('imagesDataStore').load();
					                    	Ext.getCmp('galleryView').refresh();
					                        Ext.Msg.alert('Success', LN(o.result.msg));
					                    },
					                    failure: function(fp, o) {
					                    	Ext.Msg.alert('Failure', LN(o.result.msg));
					                    }
					                };
					                form.submit(params);
					            }
					        }
					    }],
					    listeners: {
					    	actioncomplete: function(){}
					    }
					}),
					Ext.create('Ext.panel.Panel',{
						html: LN('sbi.cockpit.widgets.image.imageWidgetDesigner.dropToDelete'),
//						height: 100,
						frame: true,
						cls: 'x-dd-drop-ok',
						buttons: [{
					        text: LN('sbi.cockpit.widgets.image.imageWidgetDesigner.buttonDelete'),
					        handler: function(panel){panel.up('panel').requestDeleteRecord(Ext.getCmp('mainImagePanel').itemSelected.name);}
					    }],
					    requestDeleteRecord: function(imageName) {
				        	Ext.Msg.show({
				        		msg: LN('sbi.cockpit.widgets.image.imageWidgetDesigner.confirmDelete'),
				        		buttons: Ext.Msg.YESNO,
				        		fn: function(button){
				        			if(button=='yes'){
				        				var panel = Ext.getCmp('mainImagePanel');
				        				var myMask = new Ext.LoadMask(panel,{msg:LN('sbi.cockpit.widgets.image.imageWidgetDesigner.deletingImage')});
				        				var deleteByAjax = Ext.create('Ext.data.Connection');
				        				deleteByAjax.on("beforerequest", function(){
				        		        	myMask.show();
				        		        });
				        				deleteByAjax.on("requestcomplete", function(){
				        		        	myMask.destroy();
				        		        });
				        				deleteByAjax.request({
				        					params: {name: imageName},
				        					method: 'GET',
				        		    		url: Sbi.config.contextName+'/restful-services/1.0/images/deleteImage',
				        					success: function(response) {
				        						Ext.getCmp('mainImagePanel').itemSelected = null;
				        						if(Ext.decode(response.responseText).success){
				        							Ext.data.StoreManager.lookup('imagesDataStore').load();
				        						}
				        						Ext.Msg.alert(LN(Ext.decode(response.responseText).msg));
				        					}
				        				});
				        				
				        			}
				        		}
				        	});
				        },
					    listeners: {
					    	render: function(panel) {
					    		this.dropZone = new Ext.dd.DropZone(this.getEl(), {
					    			ddGroup: 'imageDragDrop',
					    			// If the mouse is over a grid row, return that node. This is
					    	        // provided as the "target" parameter in all "onNodeXXXX" node event handling functions
					    	        getTargetFromEvent: function(e) {
					    	            return e.getTarget();
					    	        },

					    	        // On entry into a target node, highlight that node.
					    	        onNodeEnter : function(target, dd, e, data){
					    	            Ext.fly(target).addCls('my-row-highlight-class');
					    	        },

					    	        // On exit from a target node, unhighlight that node.
					    	        onNodeOut : function(target, dd, e, data){
					    	            Ext.fly(target).removeCls('my-row-highlight-class');
					    	        },

					    	        // While over a target node, return the default drop allowed class which
					    	        // places a "tick" icon into the drag proxy.
					    	        onNodeOver : function(target, dd, e, data){
					    	            return Ext.dd.DropZone.prototype.dropAllowed;
					    	        },

					    	        // On node drop we can interrogate the target to find the underlying
					    	        // application object that is the real target of the dragged data.
					    	        // In this case, it is a Record in the GridPanel's Store.
					    	        // We can use the data set up by the DragZone's getDragData method to read
					    	        // any data we decided to attach in the DragZone's getDragData method.
					    	        onNodeDrop : function(target, dd, e, data){
					    	        	if(data.records[0].data.name){
					    	        		panel.requestDeleteRecord(data.records[0].data.name);
					    	        		return true;
					    	        	}
					    	        	return false;
					    	        }
					    		});
					    	}
					    }
					})
				]}
	        ]
	    });
		this.imagePanel.on('beforerender' ,
			function (thePanel, attribute) {
				thePanel.refreshPanelTitle();
			},
			this
		);
	}
	
});

Ext.define('ImageDataModel', {
    extend: 'Ext.data.Model',
    fields: [
       {name: 'name', type: 'string'},
       {name: 'size', type: 'float'},
       {name:'lastmod', type:'date', dateFormat:'timestamp'},
       {name: 'url', type: 'string', convert:function(o){
    	   return '/athena/restful-services'+o;
       }},
       {name: 'urlPreview', type: 'string', convert:function(o){
    	   return '/athena/restful-services'+o;
       }}
    ]
});


