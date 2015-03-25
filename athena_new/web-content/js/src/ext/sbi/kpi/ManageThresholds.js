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
 * Authors - Chiara Chiarelli (chiara.chiarelli@eng.it)
 */
Ext.ns("Sbi.kpi");

Sbi.kpi.ManageThresholds = function(config) {
	 
	var paramsList = {MESSAGE_DET: "THRESHOLDS_LIST"};
	var paramsSave = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "THRESHOLD_INSERT"};
	var paramsDel = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "THRESHOLD_DELETE"};
	
	this.configurationObject = {};
	
	this.nodeTypesCd = config.nodeTypesCd;
	this.thrSeverityTypesCd = config.thrSeverityTypesCd;
	this.drawSelectColumn = config.drawSelectColumn;
	
	this.configurationObject.manageListService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_THRESHOLDS_ACTION'
		, baseParams: paramsList
	});
	this.configurationObject.saveItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_THRESHOLDS_ACTION'
		, baseParams: paramsSave
	});
	this.configurationObject.deleteItemService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_THRESHOLDS_ACTION'
		, baseParams: paramsDel
	});
	
	this.initConfigObject();
	
	config.configurationObject = this.configurationObject;	
	config.singleSelection = true;
	
	var c = Ext.apply({}, config || {}, {});

	Sbi.kpi.ManageThresholds.superclass.constructor.call(this, c);	 	
	this.detailThrColor.focus(false,60);
	
	this.rowselModel.addListener('rowselect',function(sm, row, rec) { 
		this.activateThrValuesForm(null, rec, row); 
		this.getForm().loadRecord(rec);  
     }, this);
	
	this.addListener('selected',this.selectThr,this);
	this.addEvents('selectEvent');
	
	this.tabs.setSize(500,490);
};

Ext.extend(Sbi.kpi.ManageThresholds, Sbi.widgets.ListDetailForm, {
	
	configurationObject: null
	, gridForm:null
	, mainElementsStore:null
	, severityStore: null
	, nodeTypesCd: null
	, thrSeverityTypesCd: null
	, drawSelectColumn: null
	, detailThrColor: null
	
	,selectThr:function(itemId,index){
		var record = this.mainElementsStore.getAt(index);
		var code = record.get('code');
		this.fireEvent('selectEvent',itemId,index,code);
	}

	,activateThrValuesForm:function(combo,record,index){
		var thrTypeSelected = record.get('typeCd');
		if(thrTypeSelected != null && thrTypeSelected=='MINIMUM'){
			
			this.tempThrV.setVisible(false);
			this.thrMinOrMaxDetail.setVisible(true);
			this.detailThrMin.enable();
            this.detailThrMinClosed.enable();
            this.detailThrMax.setRawValue( null );
            this.detailThrMax.disable();
            this.detailThrMaxClosed.setValue( false );
            this.detailThrMaxClosed.disable();
            this.detailThrColor.setColorBackG(record.get('color'));
		}else if (thrTypeSelected != null && thrTypeSelected=='MAXIMUM'){
			
			this.tempThrV.setVisible(false);
			this.thrMinOrMaxDetail.setVisible(true);
			this.detailThrMin.disable();
			this.detailThrMin.setRawValue( null );
            this.detailThrMinClosed.disable();
			this.detailThrMinClosed.setValue( false);
            this.detailThrMax.enable(true);
            this.detailThrMaxClosed.enable();
            this.detailThrColor.setColorBackG(record.get('color'));
		}else if (thrTypeSelected != null && thrTypeSelected=='RANGE'){
			
			this.tempThrV.setVisible(true);
			this.thrMinOrMaxDetail.setVisible(false);	
			var myData = record.get('thrValues');
			
			if(myData!=null && myData!=undefined){
				this.tempThrV.loadItems(myData);
			}else{
				this.tempThrV.loadItems([]);
			}
		}
	}
	
	,initConfigObject:function(){
	    this.configurationObject.fields = ['id'
		                     	          , 'name'
		                    	          , 'code'
		                    	          , 'description'   
		                    	          , 'typeCd'
		                    	          , 'thrValues'
		                    	          , 'idThrVal'
		                    	          , 'label'
		                    	          , 'position'
		                    	          , 'min'
		                    	          , 'minIncluded'
		                    	          , 'max'
		                    	          , 'maxIncluded'
		                    	          , 'val'
		                    	          , 'color'
		                    	          , 'severityCd'
		                    	          ];
		
		this.configurationObject.emptyRecToAdd = new Ext.data.Record({
										  id: 0,
										  name:'', 
										  code:'', 
										  description:'',
										  typeCd: '',
										  thrValues: [],
										  idThrVal: 0,
										  label: '',
					                      position: '',
					                      min: '',
					                      minIncluded: false,
					                      max: '',
					                      maxIncluded: false,
					                      val: '',
					                      color: '',
					                      severityCd: ''
										 });   
		
		this.configurationObject.gridColItems = [
		                                         {id:'name',header: LN('sbi.generic.name'), width: 155, sortable: true, locked:false, dataIndex: 'name'},
		                                         {header: LN('sbi.generic.code'), width: 155, sortable: true, dataIndex: 'code'},
		                                         {header: LN('sbi.generic.type'), width: 90, sortable: true, dataIndex: 'typeCd'}
		                                        ];
		
		if(this.drawSelectColumn){
			this.configurationObject.drawSelectColumn = true;
		}

		this.configurationObject.panelTitle = LN('sbi.thresholds.panelTitle');
		this.configurationObject.listTitle = LN('sbi.thresholds.listTitle');
		
		this.configurationObject.filter = true;
		
		this.initTabItems();
    }

	,initTabItems: function(){

		//Store of the combobox
 	    this.typesStore = new Ext.data.SimpleStore({
 	        fields: ['typeCd'],
 	        data: this.nodeTypesCd,
 	        autoLoad: false
 	    });
 	    
 	   this.severityStore = new Ext.data.SimpleStore({
	        fields: ['severityCd'],
	        data: this.thrSeverityTypesCd,
	        autoLoad: false
	    });
 	    
 	   //START list of detail fields
 	   var detailFieldId = {
               name: 'id',
               hidden: true
           };
 		   
 	   var detailFieldName = new Ext.form.TextField({
          	 maxLength:400,
          	 width : 250,
        	 //regex : new RegExp("^([a-zA-Z1-9_\x2F])+$", "g"),
        	 regexText : LN('sbi.roles.alfanumericString'),
             fieldLabel: LN('sbi.generic.name'),
             validationEvent:true,
             name: 'name'
         });
 			  
 	   var detailFieldCode = new Ext.form.TextField({
          	 maxLength:45,
        	 minLength:1,
        	 width : 250,
        	 //regex : new RegExp("^([A-Za-z0-9_])+$", "g"),
        	 regexText : LN('sbi.roles.alfanumericString2'),
             fieldLabel:LN('sbi.generic.code'),
             allowBlank: false,
             validationEvent:true,
             name: 'code'
         });	  
 		   
 	   var detailFieldDescr = new Ext.form.TextArea({
          	 maxLength:1000,
        	 width : 310,
             height : 150,
        	 //regex : new RegExp("^([a-zA-Z1-9_\x2F])+$", "g"),
        	 regexText : LN('sbi.roles.alfanumericString'),
             fieldLabel: LN('sbi.generic.descr'),
             validationEvent:true,
             name: 'description'
         });	 		   
 		   
 	   var detailFieldNodeType =  new Ext.form.ComboBox({
        	  name: 'typeCd',
              store: this.typesStore,
              width : 250,
              fieldLabel: LN('sbi.generic.type'),
              displayField: 'typeCd',   // what the user sees in the popup
              valueField: 'typeCd',        // what is passed to the 'change' event
              typeAhead: true,
              forceSelection: true,
              mode: 'local',
              triggerAction: 'all',
              selectOnFocus: true,
              editable: false,
              allowBlank: false,
              validationEvent:true
          });  
 	  detailFieldNodeType.addListener('select',this.activateThrValuesForm,this);
 	  //END list of detail fields
 	  
 	  var c = {};
 	  c.severityStore = this.severityStore;
 	   
 	  this.tempThrV = new Sbi.kpi.ManageThresholdValues(c);
 	  
 	  var detailThrValFieldId = {
             name: 'idThrVal',
             hidden: true
         };
 	  
 	  var detailThrPosition = new Ext.form.NumberField({		
             fieldLabel: LN('sbi.thresholds.position'),
             width : 250,
             xtype: 'numeric',
             validationEvent:true,
             name: 'position'
         });	 
 	  
 	  var detailThrLabel = new Ext.form.TextField({
 			 maxLength:20,
 			 minLength:1,
 			 width : 250,
             fieldLabel: LN('sbi.generic.label'),
             validationEvent:true,
             allowBlank: false,
             name: 'label'
         });	
 	  
 	 this.detailThrMin = new Ext.form.NumberField({
             fieldLabel: LN('sbi.thresholds.minVal'),
             width : 250,
             xtype: 'numeric',
             validationEvent:true,
             name: 'min'
         });
 	 
 	this.detailThrMinClosed = new Ext.form.Checkbox({
            fieldLabel: LN('sbi.thresholds.include'),
            validationEvent:true,
            name: 'minIncluded'
        });
 	
 	 this.detailThrMax = new Ext.form.NumberField({
             fieldLabel: LN('sbi.thresholds.maxVal'),
             width : 250,
             xtype: 'numeric',
             validationEvent:true,
             name: 'max'
         });
 	 
 	this.detailThrMaxClosed = new Ext.form.Checkbox({
			 xtype: 'checkbox',
             fieldLabel: LN('sbi.thresholds.include'),
             validationEvent:true,
             //margins: {top:20, right:0, bottom:0, left:0},
             name: 'maxIncluded'
 		});
 	
 	var detailThrValue = new Ext.form.NumberField({
             fieldLabel: LN('sbi.thresholds.value'),
             width : 250,
             xtype: 'numeric',
             validationEvent:true,
             name: 'val'
        });
 	
 	this.detailThrColor = new Ext.ux.ColorField({
 			fieldLabel: LN('sbi.thresholds.color'), 
 			width : 250,
 			value: '#FFFFFF', 
 			msgTarget: 'qtip', 
 			name: 'color',
 			fallback: true
 			});
 	
 	
 	var detailThrSeverity = new Ext.form.ComboBox({
      	  name: 'severityCd',
          store: this.severityStore,
          width : 250,
          fieldLabel: LN('sbi.thresholds.severity'),
          displayField: 'severityCd',   // what the user sees in the popup
          valueField: 'severityCd',        // what is passed to the 'change' event
          typeAhead: true,
          forceSelection: true,
          mode: 'local',
          triggerAction: 'all',
          selectOnFocus: true,
          editable: false,
          allowBlank: true,
          validationEvent:true,
          xtype: 'combo'
      });  
 	  
 	  this.thrMinOrMaxDetail = new Ext.form.FieldSet({  	
             labelWidth: 110,
             //defaults: {width: 200, border:false},    
             defaultType: 'textfield',
             autoHeight: true,
             autoScroll  : true,
            // bodyStyle: Ext.isIE ? 'padding:0 0 5px 15px;' : 'padding:10px 15px;',
             border: true,
             style: {
                 "margin-left": "30px", 
                 "margin-top": "20px", 
                 "margin-right": Ext.isIE6 ? (Ext.isStrict ? "-30px" : "-33px") : "30px"  
             },
             items: [detailThrValFieldId, detailThrPosition, detailThrLabel, this.detailThrMin, 
                     this.detailThrMinClosed, this.detailThrMax, this.detailThrMaxClosed, 
                     detailThrValue, this.detailThrColor, detailThrSeverity]
    	});

 	  this.detailItem = new Ext.form.FieldSet({ 
		   		 id: 'items-detail',   	
	 		   	 itemId: 'items-detail',   	              
	 		   	// columnWidth: 0.4,
	             xtype: 'fieldset',
	             labelWidth: 110,
	            // defaults: {width: 200, border:false},    
	             defaultType: 'textfield',
	             autoHeight: true,
	             autoScroll  : true,
	             bodyStyle: Ext.isIE ? 'padding:0 0 10px 15px;' : 'padding:10px 15px;',
	             border: false,
	             style: {
	                 "margin-left": "10px",  
	                 "margin-right": Ext.isIE6 ? (Ext.isStrict ? "-10px" : "-13px") : "0"  
	             },
	             items: [detailFieldId, detailFieldName, detailFieldCode, 
	                     detailFieldDescr, detailFieldNodeType]
	    	});
 	   
 	   this.thrValuesItem = new Ext.Panel({
		        title: LN('sbi.thresholds.values')
			        , id : 'thr-values'
			        , layout: 'fit'
			        , autoScroll: false
			        , items: [this.tempThrV,
			                  this.thrMinOrMaxDetail]
			        , itemId: 'thrValues'
			        , scope: this
			    });
 		
 	   this.configurationObject.tabItems = [{
		        title: LN('sbi.generic.details')
		        , itemId: 'detail'
		        , items: [this.detailItem]
		    },this.thrValuesItem];

 	   this.tempThrV.on('DeletedThrVal', function(itemId) { 		   
	    	var rec = this.rowselModel.getSelected();
 	    	var thrval = rec.get('thrValues');
 			Ext.each(thrval, function(item, index) {
 	    		if(item.idThrVal == itemId){
 	    			thrval.remove(item);
			    }		
 			});
 	    	rec.set('thrValues', thrval);
	    	rec.commit();
	    }, this);
 	   
	}
	
	//OVERRIDING METHOD
	, addNewItem : function(){
		
		var emptyRecToAdd = this.emptyRecord;
		this.getForm().loadRecord(emptyRecToAdd);
		this.tempThrV.loadItems([]);
	
	    this.tabs.items.each(function(item)
		    {		
		    	item.doLayout();
		    });   
	    
	    this.tabs.setActiveTab(0);
	}
	
    //OVERRIDING save method
	,save : function() {

		var values = this.getForm().getFieldValues();
		var idRec = values['id'];
		var newRec;
		var thrVal = new Array();
	
		if(idRec == 0 || idRec == null || idRec === ''){

			newRec =new Ext.data.Record({
					name: values['name'],
					code: values['code'],
			        description: values['description'],		
			        typeCd: values['typeCd']
			});	  
			
			if(values['typeCd']!=null && values['typeCd']!=undefined){
				if(values['typeCd']=='MINIMUM' || values['typeCd']=='MAXIMUM'){
					
					newRec.set('label',values['label']);
					newRec.set('position',values['position']);
					newRec.set('val',values['val']);
					newRec.set('color',values['color']);
					newRec.set('severityCd',values['severityCd']);

					if(values['typeCd']=='MINIMUM'){
						newRec.set('min',values['min']);
						newRec.set('minIncluded',values['minIncluded']);						
					}else if(values['typeCd']=='MAXIMUM'){
						newRec.set('max',values['max']);
						newRec.set('maxIncluded',values['maxIncluded']);
					}					
				}else if(values['typeCd']=='RANGE'){
					var tempStore = this.tempThrV.getStore();
					
	      	        var storeL = tempStore.getCount();
					for(var i=0;i<storeL;i++){
      		   	        var thrValRecord = tempStore.getAt(i);

      		   	        var tempRec ={
      		   	             idThrVal: thrValRecord.get('idThrVal'),
      		   	             label: thrValRecord.get('label'),
      		   	             position: thrValRecord.get('position'),
      		   	             min: thrValRecord.get('min'),
      		   	             minIncluded: thrValRecord.get('minIncluded'),
      		   	             max: thrValRecord.get('max'),
      		   	             maxIncluded: thrValRecord.get('maxIncluded'),
      		   	             val: thrValRecord.get('val'),
      		   	             color: thrValRecord.get('color'),
      		   	             severityCd: thrValRecord.get('severityCd')
      			      	          };	
      		   	        thrVal.push(tempRec);
      		   	    }
					newRec.set('thrValues',thrVal);
				}
			}
			
		}else{

			var record;
			var length = this.mainElementsStore.getCount();
			var toSelAfterSave;
			for(var i=0;i<length;i++){
	   	        var tempRecord = this.mainElementsStore.getAt(i);
	   	        if(tempRecord.data.id==idRec){
	   	        	record = tempRecord;
	   	        	toSelAfterSave = i;
				}			   
	   	    }	

			record.set('name',values['name']);
			record.set('code',values['code']);
			record.set('description',values['description']);
			record.set('typeCd',values['typeCd']);		
			
			if(values['typeCd']!=null && values['typeCd']!=undefined)
				if(values['typeCd']=='MINIMUM' || values['typeCd']=='MAXIMUM'){
					
					record.set('label',values['label']);
					record.set('position',values['position']);
					record.set('val',values['val']);
					record.set('color',values['color']);
					record.set('severityCd',values['severityCd']);

					if(values['typeCd']=='MINIMUM'){
						record.set('min',values['min']);
						record.set('minIncluded',values['minIncluded']);						
					}else if(values['typeCd']=='MAXIMUM'){
						record.set('max',values['max']);
						record.set('maxIncluded',values['maxIncluded']);
					}					
				}else if(values['typeCd']=='RANGE'){
					var tempStore = this.tempThrV.getStore();
					
	      	        var storeL = tempStore.getCount();
					for(var i=0;i<storeL;i++){
      		   	        var thrValRecord = tempStore.getAt(i);
      		   	        var tempRec ={
      		   	             idThrVal: thrValRecord.get('idThrVal'),
      		   	             label: thrValRecord.get('label'),
      		   	             position: thrValRecord.get('position'),
      		   	             min: thrValRecord.get('min'),
      		   	             minIncluded: thrValRecord.get('minIncluded'),
      		   	             max: thrValRecord.get('max'),
      		   	             maxIncluded: thrValRecord.get('maxIncluded'),
      		   	             val: thrValRecord.get('val'),
      		   	             color: thrValRecord.get('color'),
      		   	             severityCd: thrValRecord.get('severityCd')
      			      	};	
      		   	        thrVal.push(tempRec);
      		   	    }
					record.set('thrValues',thrVal);

				}
		}
		

        var params = {
        	name : values['name'],
        	code : values['code'],
        	description : values['description'],
        	typeCd : values['typeCd'],
			label: values['label'],
            position: values['position'],
            min: values['min'],
            minIncluded: values['minIncluded'],
            max: values['max'],
            maxIncluded: values['maxIncluded'],
            val: values['val'],
            color: values['color'],
            severityCd: values['severityCd']
        };
        params.thrValues = Ext.util.JSON.encode(thrVal);    
        
        if(idRec){
        	params.id = idRec;
        }
        
        var idThrValRec = values['idThrVal'];

        if(idThrValRec){
        	params.idThrVal = idThrValRec;
        }
        
        Ext.Ajax.request({
            url: this.services['saveItemService'],
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
			      			var idThrVal = content.idThrVal;
			      			var thrValues = content.thrValues;
			      			var record;
			      			
			      			if(newRec != null && newRec != undefined && itemId != null && itemId !==''){
			      				newRec.set('id', itemId);
			      				if(idThrVal!=null && idThrVal!==''){
			      					newRec.set('idThrVal', idThrVal);
			      				}

			      				if(thrValues!=null && thrValues!=undefined){
			      					var thrVal = new Array();
			      					var length = thrValues.length;		      					
				      				for(var i=0;i<length;i++){
				      		   	        var tempThrVal = thrValues[i];
				      		   	        if(tempThrVal!=null && tempThrVal!=undefined){
					      		   	       var tempRecord = {
				      	      		   	             idThrVal: tempThrVal.idThrVal,
				      	      		   	             label: tempThrVal.label,
				      	      		   	             position: tempThrVal.position,
				      	      		   	             min: tempThrVal.min,
				      	      		   	             minIncluded: tempThrVal.minIncluded,
				      	      		   	             max: tempThrVal.max,
				      	      		   	             maxIncluded: tempThrVal.maxIncluded,
				      	      		   	             val: tempThrVal.val,
				      	      		   	             color: tempThrVal.color,
				      	      		   	             severityCd: tempThrVal.severityCd
				      	      			      	};	
				      		   	        	thrVal.push(tempRecord);     				
				      		   	        }				      		   	        		   
				      		   	    }
				      				newRec.set('thrValues',thrVal);
			      				}
			      				
			      				this.mainElementsStore.add(newRec);
			      				
			      			}else {
			      				var length = this.mainElementsStore.getCount();
			      				for(var i=0;i<length;i++){
			      		   	        var tempRecord = this.mainElementsStore.getAt(i);
			      		   	        if(tempRecord.data.id==itemId){
			      		   	        	record = tempRecord;
			      					}			   
			      		   	    }
			      				if(idThrVal!=null && idThrVal!==''){
			      					record.set('idThrVal', idThrVal);
			      				}else if (thrValues!=null && thrValues!=undefined){
			      					var thrVal = new Array();
			      					var length = thrValues.length;		      					
				      				for(var i=0;i<length;i++){
				      		   	        var tempThrVal = thrValues[i];
				      		   	        if(tempThrVal!=null && tempThrVal!=undefined){
					      		   	       var tempRecord = {
				      	      		   	             idThrVal: tempThrVal.idThrVal,
				      	      		   	             label: tempThrVal.label,
				      	      		   	             position: tempThrVal.position,
				      	      		   	             min: tempThrVal.min,
				      	      		   	             minIncluded: tempThrVal.minIncluded,
				      	      		   	             max: tempThrVal.max,
				      	      		   	             maxIncluded: tempThrVal.maxIncluded,
				      	      		   	             val: tempThrVal.val,
				      	      		   	             color: tempThrVal.color,
				      	      		   	             severityCd: tempThrVal.severityCd
				      	      			      	};	
				      		   	        	thrVal.push(tempRecord);     				
				      		   	        }				      		   	        		   
				      		   	    }
				      				record.set('thrValues',thrVal);
			      				}
			      			}	
			      				
			      			this.mainElementsStore.commitChanges();
			      			
			      			if(newRec != null && newRec != undefined && itemId != null && itemId !==''){
					            this.rowselModel.selectLastRow(true);
				            }else if(record != null && record != undefined ){				            
								if(toSelAfterSave !== undefined){
									this.rowselModel.selectRow(toSelAfterSave);
								}
			      			}
			      			
			      			Ext.MessageBox.show({
			                        title: LN('sbi.generic.result'),
			                        msg: LN('sbi.generic.resultMsg'),
			                        width: 200,
			                        buttons: Ext.MessageBox.OK
			                });
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

});