/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  

/**
 * Authors - Monica Franceschini
 */
Ext.ns("Sbi.kpi");

Sbi.kpi.KpiGUIComments =  function(config) {
		
		var defaultSettings = { //autoScroll: true, 
								height: 400,
								layout: 'fit',
								border:false,		
								padding: 5
							};
		
		var c = Ext.apply(defaultSettings, config || {});
		Ext.apply(this, c);

		this.init(c);
   
		Sbi.kpi.KpiGUIComments.superclass.constructor.call(this, c);

};

/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.kpi.KpiGUIComments , Ext.form.FormPanel, {
	
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	
	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
	services: null,
	
	editorPanel: null,
	listPanel: null,
		
	kpiInstId: null,
	saveBtn: null,
	selectedField: null,
	commentId: null,
	owner: null,
	
	// =================================================================================================================
	// METHODS
	// =================================================================================================================
	
	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
	
	init: function(c){
		this.initServices(c);
		this.initStore(c);
		this.initCommentsGrid(c);
		this.initCommentEditor(c);
//		this.setAutoScroll(true);
		this.items =[{
			layout:'border'
			, autoScroll: true
			, items: [this.listPanel,  this.editorPanel]
		}];
	},
	
	/**
	 * @method 
	 * 
	 * Initialize the following services exploited by this component:
	 * 
	 *    - commentsList
	 *    - commentSave
	 *    - commentDelete
	 */
	initServices: function(c) {

		var execId = c.SBI_EXECUTION_ID;
		var paramsList = {SBI_EXECUTION_ID: execId, LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "COMMENTS_LIST"};
		var paramsSave = {SBI_EXECUTION_ID: execId, LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "SAVE_COMMENT"};
		var paramsDel = {SBI_EXECUTION_ID: execId, LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "DELETE_COMMENT"};		
		
		this.services = this.services || new Array();
		
		this.services['commentsList'] = this.services['commentsList'] || Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'MANAGE_COMMENTS'
			, baseParams: paramsList
		});
		this.services['commentSave'] = this.services['commentSave'] || Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'MANAGE_COMMENTS'
			, baseParams: paramsSave
		});
		this.services['commentDelete'] = this.services['commentDelete'] || Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'MANAGE_COMMENTS'
			, baseParams: paramsDel
		});
	},
	
	initStore: function(c) {
		this.store = new Ext.data.JsonStore({
	    	autoLoad: false    	  
	    	, id : 'id'		
	        , fields: ['owner'
         	          , 'creationDate'
          	          , 'comment'
          	          , 'id'
          	          , 'binId'
          	          ]
	    	, root: 'comments'
			, url: this.services['commentsList']		
		});
	},
	
	/**
	 * @method 
	 * 
	 * Initialize the GUI
	 */
	initCommentsGrid: function(c) {
		
		this.rowselModel = new Ext.grid.RowSelectionModel({
	          singleSelect: true
	          
	    });
		
		this.initDeleteButton(c);
        
		var columns = [
	        {
	            header: 'Owner',       
	            dataIndex: 'owner',
	            tooltip : 'Ownner'
	        }, {
	            header: 'Creation Date',
	            dataIndex: 'creationDate',
	            tpl: '{lastmod:date("m-d h:i a")}',
	            tooltip : 'Creation Date'
	        }
	//	    ,{
	//            header: 'Last Modified',
	//            dataIndex: 'lastModificationDate',
	//            tpl: '{lastmod:date("m-d h:i a")}'
	//        }
	        , {
	            header: 'Comment',
	            dataIndex: 'comment',
	            align: 'right',
	            tooltip : 'Text Comment'
	        },
	        this.deleteColumn
        ];
       
		this.saveButton = new Ext.Button({
			text: 'Save comment'
			, disabled: (!c.canEditPersonal && !c.canEditAll)
			, tooltip:   (!c.canEditPersonal && !c.canEditAll)?'You have no the authorization to save a comment': 'Click to save comment'
		    , handler: this.saveComment
		    , scope: this
		    
		});
		
		var fbarConf = [this.saveButton];
		
		
		
		this.listPanel = new Ext.grid.GridPanel({
	        store: this.store,
	        minHeight: 100,
	        region: 'center',
	        //height: 200,
	        //maxHeight: 200,
	        //autoScroll: true,
	        viewConfig:{forceFit:true},
	        selModel: this.rowselModel,
	     	hideHeaders : true,
	     	//layout: 'fit',
	        columns: columns,
	        fbar: fbarConf
	    });
		
		this.listPanel.on('cellclick', this.onSelectComment, this);
	},
	
	initDeleteButton: function(c) {
		var deleteButtonBaseConf = {
			header:  ' '
			, iconCls: 'icon-remove'
			, width: 25
			, scope: this
			, loggedUser: this.loggedUser
		};
			
		Sbi.debug("[KpiGUIComments.initComponents]: user [" + this.loggedUser + "] can delete all notes [" + c.canDelete + "]");
		Sbi.debug("[KpiGUIComments.initComponents]: user [" + this.loggedUser + "] can edit personal notes [" + c.canEditPersonal + "]");
		Sbi.debug("[KpiGUIComments.initComponents]: user [" + this.loggedUser + "] can edit all notes [" + c.canEditAll + "]");
			
		if(c.canDelete === true) {
			deleteButtonBaseConf.renderer = function(v, p, record){
				return '<center><img class="x-mybutton-'+this.id+' grid-button ' +this.iconCls+'" width="16px" height="16px" src="'+Ext.BLANK_IMAGE_URL+'"/></center>';
		    };
		    Sbi.debug("[KpiGUIComments.initComponents]: added delete button to all records");
		} else if(c.canEditPersonal === true || c.canEditAll === true) {
			deleteButtonBaseConf.renderer = function(v, p, record){
				Sbi.debug("[KpiGUIComments.initComponents]: [" + record.get('owner') +"] === [" + this.loggedUser + "] ? " + (record.get('owner') === this.loggedUser));
				if(record.get('owner') === this.loggedUser) {
					Sbi.debug("[KpiGUIComments.initComponents]: delete button added during rendering to record [" + record.get('comment') + "]");
					return '<center><img class="x-mybutton-'+this.id+' grid-button ' +this.iconCls+'" width="16px" height="16px" src="'+Ext.BLANK_IMAGE_URL+'"/></center>';
				} else {
					Sbi.debug("[KpiGUIComments.initComponents]: delete button not added during rendering to record [" + record.get('comment') + "]");
					 return '&nbsp;';
				}
		    };
		    Sbi.debug("[KpiGUIComments.initComponents]: added delete button only to personal record");
		} else {
			deleteButtonBaseConf.renderer = function(v, p, record){
	   	        return '&nbsp;';
	   	    };
			Sbi.debug("[KpiGUIComments.initComponents]: delete button not added");
		}
	       
		this.deleteColumn = new Ext.grid.ButtonColumn(deleteButtonBaseConf);
	},
	
	onSelectComment: function(grid, rowIndex, columnIndex, e) {
	    var record = grid.getStore().getAt(rowIndex);  // Get the Record
	    var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
	    var data = record.get(fieldName);
	    if(fieldName != undefined){
			this.editorPanel.setValue(record.data.comment);
			this.commentId=record.data.id;	
			this.owner= record.data.owner;
	    }else{
	    	//delete button
	    	this.deleteItem( record.data.id, columnIndex);
	    }
	}, 
	
	initCommentEditor: function(c) {
		 this.editorPanel = new Ext.form.HtmlEditor({
			  	enableSourceEdit: false
			  	, region: 'south'
//			  	, width: 500
			  	, height: 200
			  	, autoScroll: true
			  	, split: true
			  	, margins: '10 0 0 0'
			  	, style:'align: center;'
//			  	, layout: 'fit'
		  }); 
		 
		 this.editorPanel.setReadOnly((!c.canEditPersonal && !c.canEditAll));
		 
	},
	

	
	loadComments: function (field) {
		if(field.attributes.kpiInstId != null && field.attributes.kpiInstId !== undefined){
			Ext.Ajax.request({
		        url: this.services['commentsList'],
		        params: {kpiInstId: field.attributes.kpiInstId},
		        success : function(response, options) {
		      		if(response !== undefined && response.responseText !== undefined) {
		      			var content = Ext.util.JSON.decode( response.responseText );
		      			if (content !== undefined) {
		      				this.listPanel.store.loadData(content);	      				
		      			} 
		      		} else {
		      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
		      		}
		        },
		        scope: this,
				failure: Sbi.exception.ExceptionHandler.handleFailure      
			});
		}
	}
	
	, saveComment: function () {
		var commId= this.commentId;
		Ext.Ajax.request({
	        url: this.services['commentSave'],
	        params: {'comment': this.editorPanel.getValue(), 'kpiInstId': this.kpiInstId, 'commentId': this.commentId, 'owner': this.owner},
			success: function(response, options) {
	      		if(response !== undefined && response.responseText !== undefined) {
	      			var content = Ext.util.JSON.decode( response.responseText);
	      			if (content !== undefined) {

	      				if(content.text == 'Forbidden'){
			      			Ext.MessageBox.show({
			      				title: 'Status',
			      				msg: 'Operation forbidden',
			      				modal: false,
			      				buttons: Ext.MessageBox.OK,
			      				width:300,
			      				icon: Ext.MessageBox.INFO  			
			      			});
	      				}else{
			      			Ext.MessageBox.show({
			      				title: 'Status',
			      				msg: 'Success',
			      				modal: false,
			      				buttons: Ext.MessageBox.OK,
			      				width:300,
			      				icon: Ext.MessageBox.INFO  			
			      			});
				      		this.update(this.selectedField);
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
	
	, update:  function(field){	
		if(field !== undefined && field != null){
			this.kpiInstId = field.attributes.kpiInstId;
			this.selectedField= field;
			this.loadComments(field);
			
		}else{

			this.kpiInstId = null;
		}
		this.editorPanel.setValue('');
		this.editorPanel.show();

	}
	
	, deleteItem: function(id, index) {
		
		Ext.MessageBox.confirm(
			LN('sbi.generic.pleaseConfirm'),
			LN('sbi.generic.confirmDelete'),            
            function(btn, text) {
                if (btn=='yes') {
                	if (id != null) {
                		
						Ext.Ajax.request({
				            url: this.services['commentDelete'],
				            params: {'commentId': id},
				            method: 'GET',
				            success: function(response, options) {
				            	if(response !== undefined && response.responseText != undefined ){
				            		var res = Ext.decode(response.responseText);
				      				if(res.text == 'Forbidden'){
						      			Ext.MessageBox.show({
						      				title: 'Status',
						      				msg: 'Operation forbidden',
						      				modal: false,
						      				buttons: Ext.MessageBox.OK,
						      				width:300,
						      				icon: Ext.MessageBox.INFO  			
						      			});
				      				}else{
						      			Ext.MessageBox.show({
						      				title: 'Status',
						      				msg: 'Success',
						      				modal: false,
						      				buttons: Ext.MessageBox.OK,
						      				width:300,
						      				icon: Ext.MessageBox.INFO  			
						      			});
										var deleteRow = this.rowselModel.getSelected();
										this.store.remove(deleteRow);
										this.store.commitChanges();
										if(this.store.getCount()>0){
											this.rowselModel.selectRow(0);
										}
				      				}
				            	}
				            }
				            , failure: this.onDeleteItemFailure
				            , scope: this
			
						});
					} 
                }
            },
            this
		);
	}
});