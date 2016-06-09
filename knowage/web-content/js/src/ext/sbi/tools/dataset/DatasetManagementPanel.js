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
 * Authors - Chiara Chiarelli (chiara.chiarelli@eng.it) Monica Franceschini
 * (monica.franceshini@eng.it) 
 *
 * Fabrizio Lovato - fabrizio.lovato@gmail.com
 */
// var thisPanel;
Ext.ns("Sbi.tools.dataset");
var ns=Sbi.tools.dataset;
   
Sbi.tools.dataset.DatasetManagementPanel = function(config) {

	// Need this to allow to hide labels
	Ext.layout.FormLayout.prototype.trackLabels = true;

	var paramsList = {
		MESSAGE_DET : "DATASETS_LIST"
	};
	var paramsSave = {
		LIGHT_NAVIGATOR_DISABLED : 'TRUE',
		MESSAGE_DET : "DATASET_INSERT"
	};
	var paramsDel = {
		LIGHT_NAVIGATOR_DISABLED : 'TRUE',
		MESSAGE_DET : "DATASET_DELETE"
	};
	
	this.isFromSaveNoMetadata = false;
	
	this.isFromSaveWithMetadata = false;

	this.configurationObject = {};

	this.configurationObject.manageListService = Sbi.config.serviceRegistry
			.getServiceUrl({
				serviceName : 'MANAGE_DATASETS_ACTION',
				baseParams : paramsList
			});
	this.configurationObject.saveItemService = Sbi.config.serviceRegistry
			.getServiceUrl({
				serviceName : 'MANAGE_DATASETS_ACTION',
				baseParams : paramsSave
			});
	this.configurationObject.deleteItemService = Sbi.config.serviceRegistry
			.getServiceUrl({
				serviceName : 'MANAGE_DATASETS_ACTION',
				baseParams : paramsDel
			});

	this.configurationObject.uploadFileService = Sbi.config.serviceRegistry
			.getServiceUrl({
				serviceName : 'UPLOAD_DATASET_FILE_ACTION',
				baseParams : {
					LIGHT_NAVIGATOR_DISABLED : 'TRUE'
				}
			});

	this.configurationObject.getDatamartsService = Sbi.config.serviceRegistry
			.getServiceUrl({
				serviceName : 'GET_META_MODELS_ACTION',
				baseParams : {
					LIGHT_NAVIGATOR_DISABLED : 'TRUE'
				}
			});
	// this.configurationObject.getDatamartsService =
	// Sbi.config.qbeGetDatamartsUrl;

	this.initConfigObject();

	this.configurationObject.filter = true;
	this.configurationObject.columnName = [
			[ 'label', LN('sbi.generic.label') ],
			[ 'name', LN('sbi.generic.name') ],
			[ 'category.valueNm', LN('sbi.ds.catType') ] ];
	this.configurationObject.setCloneButton = true;
	config.configurationObject = this.configurationObject;
	config.singleSelection = true;

	// added
	config.id = 'datasetForm';
	config.fileUpload = true; // this is a multipart form!!
	config.isUpload = true;
	config.method = 'POST';
	config.enctype = 'multipart/form-data';

	config.tabPanelWidth = '70%'; // 520;
	config.gridWidth = '30%'; // 470;

	var c = Ext.apply({}, config || {}, {});

	Sbi.tools.dataset.DatasetManagementPanel.superclass.constructor.call(this,
			c);

	this.hasDocumentsAssociated = '';
	this.hasFederationsAssociated = '';
	
	this.fileUploaded = false; // used to warn that a new Dataset File is uploaded
	this.initSchedulingCronLine = true;

	this.rowselModel.addListener('rowselect', function(sm, row, rec) {
		this.activateDsTypeForm(null, rec, row);
		this.activateTransfForm(null, rec, row);
		this.activatePersistForm(null, rec.get('isPersisted'), null, null);
		this.activateScheduleForm(null, rec, null, null);
		this.activateDsVersionsGrid(null, rec, row);
		this.activateDsTestTab(this.datasetTestTab);
		this.manageDatasetFieldMetadataGrid.loadItems(rec.get("meta"), rec);
		this.setValues(rec);
		// destroy the qbe query builder, if existing
		if (this.qbeDataSetBuilder != null) {
			this.qbeDataSetBuilder.destroy();
			this.qbeDataSetBuilder = null;
		}
		this.manageFormsEnabling();
		this.fileUploaded = false; // reset to default
		this.hasDocumentsAssociated = rec.get("hasDocumentsAssociated");
		this.hasFederationsAssociated = rec.get("hasFederationsAssociated");
	}, this);

	this.tabs.addListener('tabchange', this.modifyToolbar, this);

	thisPanel = this;

	// invokes before each ajax request
	Ext.Ajax.on('beforerequest', this.showMask, this);
	// invokes after request completed
	Ext.Ajax.on('requestcomplete', this.hideMask, this);
	// invokes if exception occured
	Ext.Ajax.on('requestexception', this.hideMask, this);

	this.manageFormsDisabling();

};

ns.infoButton=function(htmlFile) {
	new Ext.Window({
		autoLoad : {
			url : Sbi.config.contextName + '/themes/'
					+ Sbi.config.currTheme
					+ '/html/'+htmlFile
		},
		layout : 'fit',
		width : 620,
		height : 410,
		autoScroll : true,
		closeAction : 'close',
		buttonAlign : 'left',
		plain : true,
		title : LN('sbi.ds.help')
	}).show();
};

ns.getTextField= function(isRequired,htmlFile,name) {
	var res= {
		maxLength : 250,
		minLength : 1,
		width : 350,
		regexText : LN('sbi.roles.alfanumericString'),
	};

	if (htmlFile!==null) {
		ns.applyHelp(res,htmlFile);
	}

	res.allowBlank = isRequired===true?false:true;
	if (isRequired===true) {
		res.validationEvent=true;
	}
	res.fieldLabel=LN('sbi.ds.'+name);
	res.name=name;
	return new Ext.form.TextField(res);
};

ns.getTextArea = function(isRequired,name) {
	var res= {
		maxLength : 30000,
		xtype : 'textarea',
		height : 200,
		width : 720,
		regexText : LN('sbi.roles.alfanumericString'),
	};

	res.allowBlank = isRequired===true?false:true;
	if (isRequired===true) {
		res.validationEvent=true;
	}
	res.fieldLabel=LN('sbi.ds.'+name);
	res.name=name;
	return new Ext.form.TextArea(res);
};

//appli a help button to a form field label
ns.applyHelp=function(config,htmlFile) {
	if (typeof config.listeners == 'undefined') {
		config.listeners={};
	}
	config.listeners.afterrender = function( field ) {
		var labelDom=field.label.dom;
		var label=field.fieldLabel;
		labelDom.innerHTML='<button class="x-btn-text icon-info">&nbsp;&nbsp;&nbsp;</button> '+label+":";
		labelDom.getElementsByTagName("button")[0].addEventListener('click', function() {
		   ns.infoButton(htmlFile);
		});
	}
};

ns.getComboBox = function(isRequired,storeData,name) {
	var store = new Ext.data.SimpleStore({
		fields : [ name, 'name' ],
		data : storeData,
		autoLoad : false
	});

	var label=LN('sbi.ds.'+name);
	var res = {
		name : name,
		store : store,
		width : 350, // 160,
		fieldLabel : label,
		displayField : 'name', 
		valueField : name, 
		typeAhead : true,
		forceSelection : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		editable : false,
		xtype : 'combo'
	};

	res.allowBlank = isRequired===true?false:true;
	if (isRequired===true) {
		res.validationEvent=true;
	}

	return new Ext.form.ComboBox(res);
};


ns.getCheckBox = function(htmlFile,name) {
	var conf={
		xtype : 'checkbox',
		itemId : name,
		name : name,
		fieldLabel : LN('sbi.ds.'+name)
	};


	if (htmlFile!==null) {
		ns.applyHelp(conf,htmlFile);
	}
	return new Ext.form.Checkbox(conf);
} 


ns.getEditorGrid = function(height,userColumns,htmlFile,name) {
	var config= {
		id:name,
		name:name,
		isFormField:true, 
		fieldLabel : LN('sbi.ds.'+name),
		width: 720,
		height: height
	};

	if (htmlFile!==null) {
		ns.applyHelp(config,htmlFile);
	}

	//set the width of columns
	for (var i=0;i<userColumns.length;i++) {
		userColumns[i].width=config.width/userColumns.length;
	}
	var cm = new Ext.grid.ColumnModel({
	    columns: userColumns
	});
	config.cm=cm;

	var store = new Ext.data.JsonStore({
	    fields: ['name','value'],
	    data:{}
	});
	config.store=store;

	var res= new Sbi.tools.ManageDatasetParameters(config);

	//simulate a Form Field setValue
	//@value the string JSON value
	res.setValue = function(value) {
		store.removeAll();
		if (value==null || value.trim().length==0) {
			return;  
		}
		var jsonValue=JSON.parse(value);

		var records=[];

		if (userColumns.length==2) {
			var record = Ext.data.Record.create([
			    {name: 'name'},
			    {name: 'value'},
			]);

			for (var key in jsonValue) {
				if (!jsonValue.hasOwnProperty(key)) {
					continue;
				}
				records.push(new record({name:key,value:jsonValue[key]}));
			}
		} else {
			//more than 2
			var recordFields=[];
			for (var i=0;i<userColumns.length;i++) {
				recordFields.push({name:userColumns[i].id});
			}
			var record = Ext.data.Record.create(recordFields);
			for (var i=0;i<jsonValue.length;i++) {
				var recordData=jsonValue[i];
				records.push(new record(recordData));
			}			
		}
		store.add(records);
	};

	//simulate a Form Field getValue
	res.getValue = function() {
		
		var result;
		if (userColumns.length==2) {
			result={};
			for(var i = 0;i< store.getCount();i++){
				var item = store.getAt(i); //Record
				result[item.get('name')]=item.get('value')
			}
		} else {
			//more than 2
			result=[];
			for(var i = 0;i< store.getCount();i++){
				var item = store.getAt(i); //Record
				var rec={};
				for (var j=0;j<userColumns.length;j++) {
					rec[userColumns[j].id]=item.get(userColumns[j].id);
				}
				result.push(rec);
			}
		}
		return JSON.stringify(result);
	};
	return res
}

ns.getColumn=function(header,id,allowBlank,data) {
	var res= {
		header: header, 
		id:id,
		sortable: true, 
		dataIndex: id,  
		
	};

	var editor;
	if (data!==undefined) {
		var store = new Ext.data.SimpleStore({
	        fields: [id],
	        data: data,
	        autoLoad: false
	    });

		editor = new Ext.form.ComboBox({
			name:id,
			valueField : id,
			displayField: id,   
			store: store,
			typeAhead: true,
			forceSelection: false,
			mode: 'local',
			triggerAction: 'all',
			selectOnFocus: true,
			editable: true,
			allowBlank: allowBlank,
			validationEvent:true
		});
	} else {
		editor = new Ext.form.TextField({
			 allowBlank: allowBlank,
	         validationEvent:true
		})
	}
	res.editor=editor;

	
	return res;
};



//REST data set fields with form fields

ns.restHeaderNames = [
	['Content-Type'],['Accept']
];

ns.restHeaderValues = [
	['application/json'],['text/plain']
];

ns.restRequestHeadersColumns=[ 
	ns.getColumn(LN('sbi.generic.name'),'name',false,ns.restHeaderNames),
	ns.getColumn(LN('sbi.generic.value'),'value',true,ns.restHeaderValues),
];

ns.restJSONPathTypes = [
	["string"],
	["int"],
	["double"],
	["date yyyy-MM-dd"],
	["timestamp yyyy-MM-dd HH:mm:ss"],
	["time HH:mm:ss"],
	["boolean"]
];

ns.restJsonPathAttributesColumns=[
	ns.getColumn(LN('sbi.generic.name'),'name',false),
	ns.getColumn(LN('sbi.ds.jsonPathValue'),'jsonPathValue',false),
	ns.getColumn(LN('sbi.ds.jsonPathType'),'jsonPathType',true,ns.restJSONPathTypes)
];

ns.restFields=['restAddress','restRequestBody','restHttpMethod', 
	'restRequestHeaders','restJsonPathItems','restDirectlyJSONAttributes','restNGSI','restJsonPathAttributes',
	'restOffset','restFetchSize','restMaxResults'];
ns.INDEX_REQUEST_HEADERS=3;
ns.INDEX_REQUEST_JSON_PATH_ATTRIBUTES=7;
//[function,[arguments before name argument]]
ns.restFormFields=[
	[ns.getTextField,[true,null]],
	[ns.getTextArea,[false]],
	[ns.getComboBox,[true,[ //http method
		['Post','Post'],['Get','Get'],['Put','Put'],['Delete','Delete']
	]]], 
	[ns.getEditorGrid,[150,ns.restRequestHeadersColumns,null]], //headers 
	[ns.getTextField,[false,"restdataset-jsonpath-items.html"]],   //JSON Path Items
	[ns.getCheckBox,["restdataset-attributes-directly.html"]], //directly json attributes
	[ns.getCheckBox,["restdataset-ngsi.html"]], //NGSI
	[ns.getEditorGrid,[200,ns.restJsonPathAttributesColumns,"restdataset-json-path-attributes.html"]], //attributes
	[ns.getTextField,[false,null]],
	[ns.getTextField,[false,null]],
	[ns.getTextField,[false,null]]
];

//check if parameters are correctly present
ns.checkRESTParams = function (params) {
	//check if NGSI is set or JSON Path items is present
	if (params[ns.restFields[4]]==='' && params[ns.restFields[6]]===false) {
		Sbi.exception.ExceptionHandler.showErrorMessage( LN('sbi.ds.restJsonPathItemsError'), LN('sbi.generic.serviceError'));
		return false;
	}
	
	//check if NGSI is set or JSON Path Attributes is not empty or NGSI is set
	if (params[ns.restFields[6]]===false && params[ns.restFields[5]]===false && params[ns.restFields[ns.INDEX_REQUEST_JSON_PATH_ATTRIBUTES]].length<=2) {
		Sbi.exception.ExceptionHandler.showErrorMessage( LN('sbi.ds.restJsonPathAttributesError'), LN('sbi.generic.serviceError'));
		return false;
	}
	return true;
};

//add the rest fields argument to form fields
(function(){
	for (var i=0;i<ns.restFields.length;i++) {
		ns.restFormFields[i][1].push(ns.restFields[i]);
	}
})();
//end REST fields


Ext
		.extend(
				Sbi.tools.dataset.DatasetManagementPanel,
				Sbi.widgets.ListDetailForm,
				{

					configurationObject : null,
					tbInfoButton : null,
					tbProfAttrsButton : null,
					tbTransfInfoButton : null,
					tbPersistInfoButton : null,
					gridForm : null,
					mainElementsStore : null,
					profileAttributesStore : null,
					trasfDetail : null,
					persistDetail : null,
					schedulingDetail : null,
					jClassDetail : null,
					customDataDetail : null,
					scriptDetail : null,
					queryDetail : null,
					flatDetail : null,
					WSDetail : null,
					fileDetail : null,
					parsGrid : null,
					datasetTestTab : null,
					manageParsGrid : null,
					manageDsVersionsGrid : null,
					manageDatasetFieldMetadataGrid : null,
					newRecord : null,
					detailFieldId : null,
					detailFieldUserIn : null,
					detailFieldDateIn : null,
					detailFieldVersNum : null,
					detailFieldVersId : null,
					qbeDataSetBuilder : null,
					customDataGrid : null,
					detailFieldScopeId : null,

					// CKAN detail
					ckanDetail : null,
					//REST detail
					restDetail : null,

					manageFormsDisabling : function() {
						this.tabs.disable();
					},
					manageFormsEnabling : function() {
						this.tabs.enable();
					},
					modifyToolbar : function(tabpanel, panel) {
						var itemId = panel.getItemId();
						if (itemId !== undefined && itemId !== null
								&& itemId === 'type') {
							this.tbInfoButton.show();
							this.tbProfAttrsButton.show();
							this.tbTransfInfoButton.hide();
							this.tbPersistInfoButton.hide();
						} else if (itemId !== undefined && itemId !== null
								&& itemId === 'transf') {
							this.tbTransfInfoButton.show();
							this.tbInfoButton.hide();
							this.tbProfAttrsButton.hide();
							this.tbPersistInfoButton.hide();
						} else if (itemId !== undefined && itemId !== null
								&& itemId === 'advanced') {
							this.tbTransfInfoButton.hide();
							this.tbInfoButton.hide();
							this.tbProfAttrsButton.hide();
							this.tbPersistInfoButton.show();
						} else {
							this.tbInfoButton.hide();
							this.tbProfAttrsButton.hide();
							this.tbTransfInfoButton.hide();
							this.tbPersistInfoButton.hide();
						}
					}

					,
					activateTransfForm : function(combo, record, index) {
						var transfSelected = record.get('trasfTypeCd');
						if (transfSelected != null 	&& transfSelected == '&nbsp;'){
							//set null in value
							transfSelected = null;
							combo.setValue(transfSelected);
							//reset all transformation values
							var trasValues = this.trasfDetail.items.items;
							for (t in trasValues){
								var tv = trasValues[t];
								if (tv.reset) tv.reset();
							}
						}
						if (transfSelected != null
								&& transfSelected == 'PIVOT_TRANSFOMER') {
							this.trasfDetail.setVisible(true);
						} else {
							this.trasfDetail.setVisible(false);
						}
					}

					// extjs4: ,activatePersistForm : function(check, newValue,
					// oldValue, opts) {
					,
					activatePersistForm : function(check, checked) {
						// var persistSelected = newValue;
						var persistSelected = checked;
						if (persistSelected != null && persistSelected == true) {
							this.persistDetail.setVisible(true);
							this.isScheduled.setVisible(true);
						} else {
							this.persistDetail.setVisible(false);
							this.isScheduled.setVisible(false);
							this.isScheduled.setValue(false);
						}
					}

					,
					activateScheduleForm : function(check, rec) {
						var scheduleSelected = rec.get('isScheduled');
						if (scheduleSelected != null
								&& scheduleSelected == true) {
							// if(this.initSchedulingCronLine) {
							var cronExpression = rec.get('schedulingCronLine')
									.split(" ");
							// cronExpression[0] represent seconds -> not
							// considered here
							var minutes = cronExpression[1];
							if (minutes != '*') {
								var multiselect = Ext
										.getCmp('minutesMultiselect');
								var radioEvery = Ext.getCmp('minute-every');
								var radioChoose = Ext.getCmp('minute-choose');
								radioEvery.setValue(false);
								radioChoose.setValue(true);
								multiselect.setValue(minutes);
							}
							var hours = cronExpression[2];
							if (hours != '*') {
								var multiselect = Ext
										.getCmp('hoursMultiselect');
								var radioEvery = Ext.getCmp('hour-every');
								var radioChoose = Ext.getCmp('hour-choose');
								radioEvery.setValue(false);
								radioChoose.setValue(true);
								multiselect.setValue(hours);
							}
							var days = cronExpression[3];
							if (days != '*' && days != '?') {
								var multiselect = Ext.getCmp('daysMultiselect');
								var radioEvery = Ext.getCmp('day-every');
								var radioChoose = Ext.getCmp('day-choose');
								radioEvery.setValue(false);
								radioChoose.setValue(true);
								multiselect.setValue(days);
							}
							var months = cronExpression[4];
							if (months != '*') {
								var multiselect = Ext
										.getCmp('monthsMultiselect');
								var radioEvery = Ext.getCmp('month-every');
								var radioChoose = Ext.getCmp('month-choose');
								radioEvery.setValue(false);
								radioChoose.setValue(true);
								multiselect.setValue(months);
							}
							var weekdays = cronExpression[5];
							if (weekdays != '*' && weekdays != '?') {
								var multiselect = Ext
										.getCmp('weekdaysMultiselect');
								var radioEvery = Ext.getCmp('weekday-every');
								var radioChoose = Ext.getCmp('weekday-choose');
								radioEvery.setValue(false);
								radioChoose.setValue(true);
								multiselect.setValue(weekdays);
							}
							this.setSchedulingCronLine();
							// this.initSchedulingCronLine = false;
							// }
						} else {
							var multiselect = Ext.getCmp('minutesMultiselect');
							var radioEvery = Ext.getCmp('minute-every');
							var radioChoose = Ext.getCmp('minute-choose');
							radioEvery.setValue(true);
							radioChoose.setValue(false);
							multiselect.reset();

							multiselect = Ext.getCmp('hoursMultiselect');
							radioEvery = Ext.getCmp('hour-every');
							radioChoose = Ext.getCmp('hour-choose');
							radioEvery.setValue(true);
							radioChoose.setValue(false);
							multiselect.reset();

							multiselect = Ext.getCmp('daysMultiselect');
							radioEvery = Ext.getCmp('day-every');
							radioChoose = Ext.getCmp('day-choose');
							radioEvery.setValue(true);
							radioChoose.setValue(false);
							multiselect.reset();

							multiselect = Ext.getCmp('monthsMultiselect');
							radioEvery = Ext.getCmp('month-every');
							radioChoose = Ext.getCmp('month-choose');
							radioEvery.setValue(true);
							radioChoose.setValue(false);
							multiselect.reset();

							multiselect = Ext.getCmp('weekdaysMultiselect');
							radioEvery = Ext.getCmp('weekday-every');
							radioChoose = Ext.getCmp('weekday-choose');
							radioEvery.setValue(true);
							radioChoose.setValue(false);
							multiselect.reset();

							this.setSchedulingCronLine();
						}
					}

					,
					showScheduleForm : function(check, checked) {
						if (checked) {
							this.schedulingDetail.setVisible(true);
						} else {
							this.schedulingDetail.setVisible(false);
						}
					}

					,
					activateDsTestTab : function(panel) {
						if (panel) {
							var record = this.rowselModel.getSelected();
							if (record) {
								var dsParsList = this.manageParsGrid
										.getParsArray();
								this.parsGrid.fillParameters(dsParsList);
							}
						}
					},
					activateDsVersionsGrid : function(combo, record, index) {
						var dsVersionsList = record.get('dsVersions');
						this.manageDsVersionsGrid.loadItems(dsVersionsList);
					}

					,
					activateDsTypeForm : function(combo, record, index) {
						var details = {
							'File':this.fileDetail,
							'Query':this.queryDetail,
							'Java Class':this.jClassDetail,
							'Script':this.scriptDetail,
							'Custom':this.customDataDetail,
							'Web Service':this.WSDetail,
							'Qbe':this.qbeQueryDetail,
							'Flat':this.flatDetail,
							'Ckan':this.ckanDetail,
							'REST':this.restDetail
						};

						//make details visible based on combo
						var dsTypeSelected = record.get('dsTypeCd');
						if (dsTypeSelected!=null) {
							var detail=details[dsTypeSelected];
							if (detail!=null) {
								detail.setVisible(true);
							}
							for (var key in details) {
								var otherDetail=details[key];
								if (otherDetail!==detail) {
									otherDetail.setVisible(false);
								}
							}
						}

						var dsParsList = record.get('pars');
						if (dsParsList != null && dsParsList != undefined) {
							this.manageParsGrid.loadItems(dsParsList);
						} else {
							this.manageParsGrid.loadItems([]);
						}

						if (record && record.json) {
							var dsCustomList = record.json.customs;
							if (dsCustomList != null
									&& dsCustomList != undefined) {
								this.customDataGrid.loadItems(dsCustomList);
							} else {
								this.customDataGrid.loadItems([]);
							}
						}

					}

					,
					test : function(button, event, service) {
						var values = this.getValues();

						var requestParameters = {
							start : 0,
							limit : 25,
							dsId : values['id'],
							dsTypeCd : values['dsTypeCd'],
							fileName : values['fileName'],
							csvDelimiter : values['csvDelimiter'],
							skipRows : values['skipRows'],
							limitRows : values['limitRows'],
							xslSheetNumber : values['xslSheetNumber'],
							fileType : values['fileType'],
							csvQuote : values['csvQuote'],
							skipRows : values['skipRows'],
							limitRows : values['limitRows'],
							xslSheetNumber : values['xslSheetNumber'],
							query : values['query'],
							queryScript : values['queryScript'],
							queryScriptLanguage : values['queryScriptLanguage'],
							dataSource : values['dataSource'],
							wsAddress : values['wsAddress'],
							wsOperation : values['wsOperation'],
							script : values['script'],
							scriptLanguage : values['scriptLanguage'],
							jclassName : values['jclassName'],
							customData : values['customData'],
							trasfTypeCd : values['trasfTypeCd'],
							pivotColName : values['pivotColName'],
							pivotColValue : values['pivotColValue'],
							pivotRowName : values['pivotRowName'],
							pivotIsNumRows : values['pivotIsNumRows'],
							isPersisted : values['isPersisted'],
							persistTableName : values['persistTableName'],
							isScheduled : values['isScheduled'],
							schedulingCronLine : values['schedulingCronLine'],
							startDate : values['startDate'],
							endDate : [ 'endDate' ],
							flatTableName : values['flatTableName'],
							dataSourceFlat : values['dataSourceFlat'],
							qbeSQLQuery : values['qbeSQLQuery'],
							qbeJSONQuery : values['qbeJSONQuery'],
							qbeDataSource : values['qbeDataSource'],
							qbeDatamarts : values['qbeDatamarts'],
							userIn : values['userIn'],
							dateIn : values['dateIn'],
							versNum : values['versNum'],
							versId : values['versId'],
							meta : values['meta'],
							fileUploaded : thisPanel.fileUploaded,
							scopeCd : values['scopeCd'],
							ckanFileType : values['ckanFileType'],
							ckanCsvDelimiter : values['ckanCsvDelimiter'],
							ckanCsvQuote : values['ckanCsvQuote'],
							ckanCsvEncoding : values['ckanCsvEncoding'],
							ckanSkipRows : values['ckanSkipRows'],
							ckanLimitRows : values['ckanLimitRows'],  
							ckanXslSheetNumber : values['ckanXslSheetNumber'],
							ckanId : values['ckanId'],
							ckanUrl : values['ckanUrl']
						};
						
						//add REST parameters 
						for (var i=0;i<ns.restFields.length;i++) {
							var field=ns.restFields[i];
							requestParameters[field]=values[field]; 
						}
						
						arrayPars = this.parsGrid.getParametersValues();

						//merge only default values of preview params with
						//general managed params
						var mergeParams = function (currParams,defaultValeusParams) {
							for (var i=0;i<currParams.length;i++) {
								for (var j=0;j<defaultValeusParams.length;j++) {
									var itemA=currParams[i];
									var itemM=defaultValeusParams[j];
									if (itemA.name === itemM.name) {
										if (typeof itemM.defaultValue !== 'undefined'){
											itemA.defaultValue = itemM.defaultValue;
										}
										break;
									}
								}
							}
						};

						if (arrayPars) { 
							var manageParsArray=this.manageParsGrid.getParsArray();
							mergeParams(arrayPars,manageParsArray);

							requestParameters.pars = Ext.util.JSON
									.encode(arrayPars);
						}

						customString = this.customDataGrid.getDataString();

						if (customString) {
							requestParameters.customData = Ext.util.JSON
									.encode(customString);
						}

						if (this.previewWindow === undefined) {
							this.previewWindow = new Sbi.tools.dataset.PreviewWindow(
									{
										modal : true,
										width : this.getWidth() - 50,
										height : this.getHeight() - 50
									});
						}
						this.previewWindow.show();
						this.previewWindow.load(requestParameters);
					}

					,
					initConfigObject : function() {
						var confO=this.configurationObject;
						confO.fields = [ 'id', 'name',
								'label', 'description', 'dsTypeCd',
								'catTypeVn', 'isPublic', 'usedByNDocs',
								'fileName', 'fileType', 'csvDelimiter',
								'csvQuote', 'skipRows', 'limitRows',
								'xslSheetNumber', 'query', 'queryScript',
								'queryScriptLanguage', 'dataSource',
								'wsAddress', 'wsOperation', 'script',
								'scriptLanguage', 'jclassName',
								'jclassNameForCustom', 'customData', 'pars',
								'trasfTypeCd', 'pivotColName', 'pivotColValue',
								'pivotRowName', 'pivotIsNumRows', 'dsVersions',
								'isPersisted', 'persistTableName',
								'isScheduled', 'schedulingCronLine',
								'startDate', 'endDate', 'flatTableName',
								'dataSourceFlat', 'qbeSQLQuery',
								'qbeJSONQuery', 'qbeDataSource',
								'qbeDatamarts', 'userIn', 'dateIn', 'versNum',
								'versId', 'meta', 'scopeCd', 'ckanFileType',
								'ckanCsvDelimiter', 'ckanCsvQuote',
								'ckanCsvEncoding', 'ckanSkipRows',
								'ckanLimitRows', 'ckanXslSheetNumber',
								'ckanId', 'ckanUrl',
								'hasDocumentsAssociated', 'hasFederationsAssociated'];

						confO.fields=confO.fields.concat(ns.restFields);

						var recordConf={
									id : null,
									name : '',
									label : '',
									description : '',
									dsTypeCd : '',
									catTypeVn : '',
									isPublic : '',
									usedByNDocs : 0,
									csvDelimiter : '',
									fileType : '',
									csvQuote : '',
									skipRows : '',
									limitRows : '',
									xslSheetNumber : '',
									fileName : '',
									query : '',
									queryScript : '',
									queryScriptLanguage : '',
									dataSource : '',
									wsAddress : '',
									wsOperation : '',
									script : '',
									scriptLanguage : '',
									jclassName : '',
									jclassNameForCustom : '',
									customData : '',
									pars : [],
									trasfTypeCd : '',
									pivotColName : '',
									pivotColValue : '',
									pivotRowName : '',
									pivotIsNumRows : '',
									isPersisted : '',
									persistTableName : '',
									isScheduled : '',
									schedulingCronLine : '',
									startDate : '',
									endDate : '',
									flatTableName : '',
									dataSourceFlat : '',
									qbeSQLQuery : '',
									qbeJSONQuery : '',
									qbeDataSource : '',
									qbeDatamarts : '',
									dsVersions : [],
									userIn : '',
									dateIn : '',
									versNum : '',
									versId : '',
									meta : [],
									scopeCd : '',
									ckanFileType : '',
									ckanCsvDelimiter : '',
									ckanCsvQuote : '',
									ckanCsvEncoding : '',
									ckanSkipRows : '',
									ckanLimitRows : '',
									ckanXslSheetNumber : '',
									ckanId : '',
									ckanUrl : '',
									hasDocumentsAssociated : '',
									hasFederationsAssociated : ''
								};
								
						//add REST fields
						for (var i=0;i<ns.restFields.length;i++) {
							recordConf[ns.restFields[i]]='';
						}

						confO.emptyRecToAdd=new Ext.data.Record(recordConf);

						confO.gridColItems = [ {
							id : 'label',
							header : LN('sbi.generic.label'),
							width : 105,
							sortable : true,
							locked : false,
							dataIndex : 'label'
						}, {
							header : LN('sbi.generic.name'),
							width : 105,
							sortable : true,
							dataIndex : 'name'
						}, {
							header : LN('sbi.generic.type'),
							width : 60,
							sortable : true,
							dataIndex : 'dsTypeCd'
						}, {
							header : LN('sbi.ds.numDocs'),
							width : 55,
							sortable : true,
							dataIndex : 'usedByNDocs'
						} ];

						confO.panelTitle = LN('sbi.ds.panelTitle');
						confO.listTitle = LN('sbi.ds.listTitle');

						var tbButtonsArray = new Array();

						this.tbFieldsMetadataButton = new Ext.Toolbar.Button({
							text : LN('sbi.ds.metadata'),
							iconCls : 'icon-metadata',
							handler : this.fieldsMetadata,
							width : 30,
							scope : this
						});
						tbButtonsArray.push(this.tbFieldsMetadataButton);
						
						this.tbDatasetLinkButton = new Ext.Toolbar.Button({
							text : 'Link Dataset',
							iconCls : 'icon-metadata',
							handler : this.linkDataset,
							width : 30,
							scope : this
						});
						tbButtonsArray.push(this.tbDatasetLinkButton);
						
						this.saveNoMetadataButton = new Ext.Toolbar.Button({
							text : LN('sbi.ds.save.no.metadata'),
							iconCls : 'icon-save',
							handler : this.saveNoMetadata,
							width : 30,
							scope : this
						});
						tbButtonsArray.push(this.saveNoMetadataButton);

						this.tbProfAttrsButton = new Ext.Toolbar.Button({
							text : LN('sbi.ds.pars'),
							iconCls : 'icon-profattr',
							handler : this.profileAttrs,
							width : 30,
							scope : this
						});
						tbButtonsArray.push(this.tbProfAttrsButton);

						this.tbInfoButton = new Ext.Toolbar.Button({
							text : LN('sbi.ds.help'),
							iconCls : 'icon-info',
							handler : this.info,
							width : 30,
							scope : this
						});
						tbButtonsArray.push(this.tbInfoButton);

						this.tbTransfInfoButton = new Ext.Toolbar.Button({
							text : LN('sbi.ds.help'),
							iconCls : 'icon-info',
							handler : this.transfInfo,
							width : 30,
							scope : this
						});
						tbButtonsArray.push(this.tbTransfInfoButton);

						this.tbPersistInfoButton = new Ext.Toolbar.Button({
							text : LN('sbi.ds.help'),
							iconCls : 'icon-info',
							handler : this.persistInfo,
							width : 30,
							scope : this
						});
						tbButtonsArray.push(this.tbPersistInfoButton);
						confO.tbButtonsArray = tbButtonsArray;

						this.initTabItems();
					}

					,
					initTabItems : function() {
						this.initDetailTab();
						this.initTypeTab();
						this.initTrasfTab();
						this.initAdvancedTab();
						this.initTestTab();
					}

					,
					initDetailTab : function() {
						this.profileAttributesStore = new Ext.data.SimpleStore(
								{
									fields : [ 'profAttrs' ],
									data : config.attrs,
									autoLoad : false
								});

						// Store of the combobox
						this.catTypesStore = new Ext.data.SimpleStore({
							fields : [ 'catTypeVn' ],
							data : config.catTypeVn,
							autoLoad : false
						});

						this.scopeStore = new Ext.data.SimpleStore({
							fields : [ 'scopeCd' ],
							data : config.scopeCd,
							autoLoad : false
						});

						this.isPublicStore = new Ext.data.SimpleStore(
								{
									fields : [ 'value', 'field', 'description' ],
									data : [
											[ true, 'Public',
													'Everybody can view this datset' ],
											[ false, 'Private',
													'The saved dataset will be visible only to you' ] ]
								});

						// START list of detail fields
						this.detailFieldId = new Ext.form.TextField({
							name : 'id',
							hidden : true
						});

						this.detailFieldUserIn = new Ext.form.TextField({
							name : 'userIn',
							hidden : true
						});

						this.detailFieldDateIn = new Ext.form.TextField({
							name : 'dateIn',
							hidden : true
						});

						this.detailFieldVersNum = new Ext.form.TextField({
							name : 'versNum',
							hidden : true
						});

						this.detailFieldVersId = new Ext.form.TextField({
							name : 'versId',
							hidden : true
						});

						var detailFieldName = {
							maxLength : 50,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.generic.name'),
							allowBlank : false,
							validationEvent : true,
							name : 'name'
						};

						var detailFieldLabel = {
							maxLength : 50,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString2'),
							fieldLabel : LN('sbi.generic.label'),
							allowBlank : false,
							validationEvent : true,
							name : 'label'
						};

						var detailFieldDescr = {
							xtype : 'textarea',
							width : 350,
							height : 80,
							maxLength : 160,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.generic.descr'),
							validationEvent : true,
							name : 'description'
						};

						var detailFieldCatType = {
							name : 'catTypeVn',
							store : this.catTypesStore,
							width : 350,
							fieldLabel : LN('sbi.ds.catType'),
							displayField : 'catTypeVn',
							valueField : 'catTypeVn',
							typeAhead : true,
							forceSelection : true,
							mode : 'local',
							triggerAction : 'all',
							selectOnFocus : true,
							editable : false,
							allowBlank : true,
							validationEvent : true,
							xtype : 'combo'
						};

						var scopeField = new Ext.form.ComboBox({
							name : 'isPublic',
							store : this.isPublicStore,
							width : 350,
							fieldLabel : LN('sbi.ds.scope'),
							displayField : 'field',
							valueField : 'value',
							typeAhead : true,
							forceSelection : true,
							mode : 'local',
							triggerAction : 'all',
							selectOnFocus : true,
							editable : false,
							allowBlank : true,
							validationEvent : true,
							xtype : 'combo'
						});

						var scopeCdField = new Ext.form.ComboBox({
							name : 'scopeCd',
							store : this.scopeStore,
							width : 350,
							// fieldLabel : LN('sbi.ds.scope'),
							displayField : 'scopeCd',
							valueField : 'scopeCd',
							typeAhead : true,
							forceSelection : true,
							mode : 'local',
							triggerAction : 'all',
							selectOnFocus : true,
							editable : false,
							allowBlank : true,
							validationEvent : true,
							xtype : 'combo'
						});
						// END list of detail fields

						var c = {};
						this.manageDatasetFieldMetadataGrid = new Sbi.tools.ManageDatasetFieldMetadata(
								c);

						this.manageDsVersionsGrid = new Sbi.tools.ManageDatasetVersions(
								c);
						this.manageDsVersionsGrid
								.addListener(
										'verionrestored',
										function(version) {

											var values = this.getValues();
											var newDsVersion = new Ext.data.Record(
													{
														dsId : values['id'],
														dateIn : values['dateIn'],
														userIn : values['userIn'],
														versId : values['versId'],
														type : values['dsTypeCd'],
														versNum : values['versNum']
													});
											this.manageDsVersionsGrid
													.getStore().addSorted(
															newDsVersion);
											this.manageDsVersionsGrid
													.getStore().commitChanges();
											var rec = this
													.buildNewRecordDsVersion(version);
											this.activateDsTypeForm(null, rec,
													null);
											this.activateTransfForm(null, rec,
													null);
											this.activatePersistForm(null, rec
													.get('isPersisted'), null,
													null);
											this.activateScheduleForm(null,
													rec, null, null);
											this
													.activateDsTestTab(this.datasetTestTab);
											this.setValues(rec);
											this.updateMainStore(values['id']);
										}, this);

						this.manageDsVersionsGrid
								.addListener(
										'verionsdeleted',
										function() {
											var values = this.getValues();
											this
													.updateDsVersionsOfMainStore(values['id']);
										}, this);

						this.manageDsVersionsPanel = new Ext.Panel({
							id : 'man-vers',
							title : LN('sbi.ds.versionPanel'),
							layout : 'fit',
							autoScroll : true,
							style : {
								"margin-top" : "20px"
							},
							border : true,
							items : [ this.manageDsVersionsGrid ],
							scope : this
						});

						this.detailTab = new Ext.Panel(
								{
									title : LN('sbi.generic.details'),
									itemId : 'detail',
									width : 430,
									items : {
										id : 'items-detail',
										itemId : 'items-detail',
										columnWidth : 0.4,
										xtype : 'fieldset',
										labelWidth : 90,
										defaultType : 'textfield',
										autoHeight : true,
										autoScroll : true,
										bodyStyle : Ext.isIE ? 'padding:0 0 8px 10px;'
												: 'padding:8px 10px;',
										border : false,

										style : {
											"margin-left" : "8px",
											"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-5px"
													: "-8px")
													: "8"
										},
										items : [ detailFieldLabel,
												detailFieldName,
												detailFieldDescr,
												detailFieldCatType, scopeField,
												scopeCdField,
												this.manageDsVersionsPanel,
												this.detailFieldUserIn,
												this.detailFieldDateIn,
												this.detailFieldVersNum,
												this.detailFieldVersId,
												this.detailFieldId ]
									}
								});
					}

					,
					initTypeTab : function() {

						this.dsTypesStore = new Ext.data.SimpleStore({
							fields : [ 'dsTypeCd' ],
							data : config.dsTypes,
							autoLoad : false
						});

						this.dataSourceStore = new Ext.data.SimpleStore({
							fields : [ 'dataSource', 'name' ],
							data : config.dataSourceLabels,
							autoLoad : false
						});

						this.scriptLanguagesStore = new Ext.data.SimpleStore({
							fields : [ 'scriptLanguage', 'name' ],
							data : config.scriptTypes,
							autoLoad : false
						});

						// START list of Advanced fields
						var detailDsType = new Ext.form.ComboBox({
							name : 'dsTypeCd',
							store : this.dsTypesStore,
							width : 350, // 160,
							fieldLabel : LN('sbi.ds.dsTypeCd'),
							displayField : 'dsTypeCd', // what the user sees in
							// the popup
							valueField : 'dsTypeCd', // what is passed to the
							// 'change' event
							typeAhead : true,
							forceSelection : true,
							mode : 'local',
							triggerAction : 'all',
							selectOnFocus : true,
							editable : false,
							allowBlank : false,
							validationEvent : true
						});
						detailDsType.addListener('select',
								this.activateDsTypeForm, this);

						this.fileUploadFormPanel = new Sbi.tools.dataset.FileDatasetPanel(
								config);
						var uploadButton = this.fileUploadFormPanel
								.getComponent('fileUploadPanel').getComponent(
										'fileUploadButton');

						uploadButton.setHandler(this.uploadFileButtonHandler,
								this);

						this.detailDataSource = new Ext.form.ComboBox({
							name : 'dataSource',
							store : this.dataSourceStore,
							width : 350, // 180,
							fieldLabel : LN('sbi.ds.dataSource'),
							displayField : 'dataSource', // what the user
							// sees in the
							// popup
							valueField : 'dataSource', // what is passed to the
							// 'change' event
							typeAhead : true,
							forceSelection : true,
							mode : 'local',
							triggerAction : 'all',
							selectOnFocus : true,
							editable : false,
							allowBlank : false,
							validationEvent : true
						});

						this.detailQbeDataSource = new Ext.form.ComboBox({
							name : 'qbeDataSource',
							store : this.dataSourceStore,
							width : 350,
							fieldLabel : LN('sbi.ds.dataSource'),
							displayField : 'dataSource', // what the user
							// sees in the popup
							valueField : 'dataSource', // what is passed to the
							// 'change' event
							typeAhead : true,
							forceSelection : true,
							mode : 'local',
							triggerAction : 'all',
							selectOnFocus : true,
							editable : false,
							allowBlank : false,
							validationEvent : true
						});

						this.detailQuery = new Ext.form.TextArea({
							maxLength : 30000,
							xtype : 'textarea',
							width : '100%',// 350,
							height : 400, // 140,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.ds.query'),
							validationEvent : true,
							allowBlank : false,
							name : 'query'
						});

						this.detailQueryScript = new Ext.form.TextArea({
							maxLength : 30000,
							xtype : 'textarea',
							width : '100%', // 350,
							height : 300, // 80,
							// regexText : 'script',
							fieldLabel : 'script',
							// validationEvent : true,
							allowBlank : false,
							name : 'queryScript'
						});

						this.detailWsAddress = new Ext.form.TextField({
							maxLength : 250,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.ds.wsAddress'),
							allowBlank : false,
							validationEvent : true,
							name : 'wsAddress'
						});

						this.detailWsOperation = new Ext.form.TextField({
							maxLength : 50,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.ds.wsOperation'),
							allowBlank : true,
							validationEvent : true,
							name : 'wsOperation'
						});

						this.detailScript = new Ext.form.TextArea({
							maxLength : 30000,
							xtype : 'textarea',
							// width : this.textAreaWidth,
							height : 350, // 195,
							width : '100%', // 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.ds.script'),
							allowBlank : false,
							validationEvent : true,
							name : 'script'
						});

						var openQbeWizardButton = new Ext.Button({
							text : LN('sbi.ds.openQbeQizard'),
							handler : this.jsonTriggerFieldHandler,
							scope : this,
							icon : 'null' // workaround: without this,
						// the button shows other
						// icons (such as Bold, Italic, ...) in
						// background
						});

						this.qbeSQLQuery = new Ext.form.Hidden({
							name : 'qbeSQLQuery'
						});

						this.qbeJSONQuery = new Ext.form.TriggerField({
							name : 'qbeJSONQuery',
							valueField : 'qbeJSONQuery',
							width : 350,
							fieldLabel : 'Qbe Query',
							triggerClass : 'x-form-search-trigger',
							editable : false
						});
						this.qbeJSONQuery.on("render", function(field) {
							field.trigger.on("click", function(e) {
								this.jsonTriggerFieldHandler();
							}, this);
						}, this);

						var datamartsStore = new Ext.data.JsonStore({
							url : this.configurationObject.getDatamartsService,
							root : 'rows',
							fields : [ 'id', 'name', 'description' ]
						});

						this.qbeDatamarts = new Ext.form.ComboBox(
								{
									name : 'qbeDatamarts',
									fieldLabel : LN('sbi.tools.managedatasets.datamartcombo.label'),
									forceSelection : true,
									editable : false,
									store : datamartsStore,
									width : 350,
									displayField : 'name',
									valueField : 'name',
									typeAhead : true,
									triggerAction : 'all',
									selectOnFocus : true,
									allowBlank : false
								});

						this.detailScriptLanguage = new Ext.form.ComboBox({
							name : 'scriptLanguage',
							store : this.scriptLanguagesStore,
							width : 350, // 160,
							fieldLabel : LN('sbi.ds.scriptLanguage'),
							displayField : 'name', // what the user
							// sees in the
							// popup
							valueField : 'scriptLanguage', // what is passed to
							// the
							// 'change' event
							typeAhead : true,
							forceSelection : true,
							mode : 'local',
							triggerAction : 'all',
							selectOnFocus : true,
							editable : false,
							allowBlank : false,
							validationEvent : true,
							xtype : 'combo'
						});

						this.detailJclassName = new Ext.form.TextField({
							maxLength : 100,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.ds.jclassName'),
							allowBlank : false,
							validationEvent : true,
							name : 'jclassName'
						});

						this.detailJclassNameForCustom = new Ext.form.TextField(
								{
									maxLength : 100,
									minLength : 1,
									width : 350,
									regexText : LN('sbi.roles.alfanumericString'),
									fieldLabel : LN('sbi.ds.jclassName'),
									allowBlank : false,
									validationEvent : true,
									name : 'jclassNameForCustom'
								});

						this.customDataGrid = new Sbi.tools.dataset.CustomDataGrid();

						this.dsTypeDetail = new Ext.form.FieldSet(
								{
									labelWidth : 100,
									defaultType : 'textfield',
									// autoHeight : true,
									autoScroll : true,
									border : false,
									style : {
										"margin-left" : "5px",
										"margin-bottom" : "0px",
										"margin-top" : "3px",
										"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "0px"
												: "-3px")
												: "0px"
									},
									items : [ detailDsType ]
								});

						this.queryDetail = new Sbi.tools.dataset.QueryDatasetConfigurationPanel(
								config);

						this.qbeQueryDetail = new Ext.form.FieldSet(
								{
									labelWidth : 120,
									defaults : {
										// width : 280,
										border : true
									},
									defaultType : 'textfield',
									autoHeight : true,
									autoScroll : true,
									border : true,
									style : {
										"margin-left" : "3px",
										"margin-top" : "0px",
										"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-3px"
												: "-5px")
												: "3px"
									},
									items : [ this.detailQbeDataSource,
											this.qbeDatamarts,
											this.qbeSQLQuery, this.qbeJSONQuery ]
								});

						this.jClassDetail = new Ext.form.FieldSet(
								{
									labelWidth : 100,
									defaults : {
										// width : 280,
										border : true
									},
									defaultType : 'textfield',
									autoHeight : true,
									autoScroll : true,
									border : true,
									style : {
										"margin-left" : "3px",
										"margin-top" : "0px",
										"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-3px"
												: "-5px")
												: "3px"
									},
									items : [ this.detailJclassName ]
								});

						this.customDataDetail = new Ext.form.FieldSet(
								{
									labelWidth : 100,
									defaults : {
										// width : 280,
										border : true
									},
									defaultType : 'textfield',
									autoHeight : true,
									autoScroll : true,
									border : true,
									style : {
										"margin-left" : "3px",
										"margin-top" : "0px",
										"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-3px"
												: "-5px")
												: "3px"
									},
									items : [ this.detailJclassNameForCustom,
											this.customDataGrid ]
								});

						this.fileDetail = new Ext.form.FieldSet({
							labelWidth : 80,
							defaults : {
								// width : 280,
								border : true
							},
							defaultType : 'textfield',
							autoHeight : true,
							autoScroll : true,
							border : true,
							items : [ this.fileUploadFormPanel ]
						});

						this.WSDetail = new Ext.form.FieldSet(
								{
									labelWidth : 100,
									defaults : {
										// width : 280,
										border : true
									},
									defaultType : 'textfield',
									autoHeight : true,
									autoScroll : true,
									border : true,
									style : {
										"margin-left" : "3px",
										"margin-top" : "0px",
										"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-3px"
												: "-5px")
												: "3px"
									},
									items : [ this.detailWsAddress,
											this.detailWsOperation ]
								});

						this.scriptDetail = new Ext.form.FieldSet(
								{
									labelWidth : 100,
									defaults : {
										// width : 280,
										border : true
									},
									defaultType : 'textfield',
									autoHeight : true,
									autoScroll : true,
									border : true,
									style : {
										"margin-left" : "3px",
										"margin-top" : "0px",
										"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-3px"
												: "-5px")
												: "3px"
									},
									items : [ this.detailScriptLanguage,
											this.detailScript ]
								});

						var detailFlatTableName = new Ext.form.TextField({
							maxLength : 50,
							minLength : 1,
							width : 350,
							fieldLabel : LN('sbi.ds.flatTableName'),
							allowBlank : false,
							validationEvent : true,
							name : 'flatTableName'
						});

						var dataSourceFlat = new Ext.form.ComboBox({
							id : 'dataSourceFlat',
							name : 'dataSourceFlat',
							store : this.dataSourceStore,
							width : 350, // 180,
							fieldLabel : LN('sbi.ds.dataSource'),
							displayField : 'dataSource', // what the user
															// sees in the popup
							valueField : 'dataSource', // what is passed to the
														// 'change' event
							typeAhead : true,
							forceSelection : true,
							mode : 'local',
							triggerAction : 'all',
							selectOnFocus : true,
							editable : false,
							allowBlank : false,
							validationEvent : true
						});

						this.flatDetail = new Ext.form.FieldSet(
								{
									labelWidth : 100,
									defaults : {
										border : true
									},
									defaultType : 'textfield',
									autoHeight : true,
									autoScroll : true,
									border : true,
									style : {
										"margin-left" : "3px",
										"margin-top" : "0px",
										"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-3px"
												: "-5px")
												: "3px"
									},
									items : [ detailFlatTableName,
											dataSourceFlat ]
								});

						// CKAN Detail Fields - START

						this.detailCkanFileType = new Ext.form.TextField({
							maxLength : 250,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.ds.ckanFileType'),
							allowBlank : false,
							validationEvent : true,
							name : 'ckanFileType'
						});
						Ext.QuickTips.register({
							target:  this.detailCkanFileType,                 
						    text: LN('sbi.ds.ckanFileType.tooltip'),
						    enabled: true
						});

						// this.detailCkanFileName = new Ext.form.TextField({
						// maxLength : 250, minLength : 1, width : 350,
						// regexText : LN('sbi.roles.alfanumericString'),
						// fieldLabel : LN('sbi.ds.ckanFileName'),
						// allowBlank : false, validationEvent : true,
						// name : 'ckanFileName'
						// });

						this.detailCkanCsvDelimiter = new Ext.form.TextField({
							maxLength : 250,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.ds.ckanCsvDelimiter'),
							allowBlank : false,
							validationEvent : true,
							name : 'ckanCsvDelimiter'
						});
						Ext.QuickTips.register({
							target:  this.detailCkanCsvDelimiter,                 
						    text: LN('sbi.ds.ckanCsvDelimiter.tooltip'),
						    enabled: true
						});

						this.detailCkanCsvQuote = new Ext.form.TextField({
							maxLength : 250,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.ds.ckanCsvQuote'),
							allowBlank : false,
							validationEvent : true,
							name : 'ckanCsvQuote'
						});
						Ext.QuickTips.register({
							target:  this.detailCkanCsvQuote,                 
						    text: LN('sbi.ds.ckanCsvQuote.tooltip'),
						    enabled: true
						});

						this.detailCkanCsvEncoding = new Ext.form.TextField({
							maxLength : 250,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.ds.ckanCsvEncoding'),
							allowBlank : true, // validationEvent : true,
							name : 'ckanCsvEncoding'
						});
						Ext.QuickTips.register({
							target:  this.detailCkanCsvEncoding,                 
						    text: LN('sbi.ds.ckanCsvEncoding.tooltip'),
						    enabled: true
						});

						this.detailCkanSkipRows = new Ext.form.TextField({
							maxLength : 250,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.ds.ckanSkipRows'),
							allowBlank : true, // validationEvent : true,
							name : 'ckanSkipRows'
						});
						Ext.QuickTips.register({
							target:  this.detailCkanSkipRows,                 
						    text: LN('sbi.ds.ckanSkipRows.tooltip'),
						    enabled: true
						});

						this.detailCkanLimitRows = new Ext.form.TextField({
							maxLength : 250,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.ds.ckanLimitRows'),
							allowBlank : true, // validationEvent : true,
							name : 'ckanLimitRows'
						});
						Ext.QuickTips.register({
							target:  this.detailCkanLimitRows,                 
						    text: LN('sbi.ds.ckanLimitRows.tooltip'),
						    enabled: true
						});

						this.detailckanXslSheetNumber = new Ext.form.TextField({
									maxLength : 250,
									minLength : 1,
									width : 350,
									regexText : LN('sbi.roles.alfanumericString'),
									fieldLabel : LN('sbi.ds.ckanXslSheetNumber'),
									allowBlank : true, // validationEvent :
														// true,
									name : 'ckanXslSheetNumber'
						});
						Ext.QuickTips.register({
							target:  this.detailckanXslSheetNumber,                 
						    text: LN('sbi.ds.ckanXslSheetNumber.tooltip'),
						    enabled: true
						});

						this.detailCkanId = new Ext.form.TextField({
							maxLength : 250,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.ds.ckanId'),
							allowBlank : false,
							validationEvent : true,
							name : 'ckanId'
						});
						Ext.QuickTips.register({
							target:  this.detailCkanId,                 
						    text: LN('sbi.ds.ckanId.tooltip'),
						    enabled: true
						});

						this.detailCkanUrl = new Ext.form.TextField({
							maxLength : 250,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.ds.ckanUrl'),
							allowBlank : false,
							validationEvent : true,
							name : 'ckanUrl'
						});
						Ext.QuickTips.register({
							target:  this.detailCkanUrl,                 
						    text: LN('sbi.ds.ckanUrl.tooltip'),
						    enabled: true
						});

						var getFieldSet=function(detailItems) {
							var res= {
								labelWidth : 100,
								defaults : {
									border : true
								},
								defaultType : 'textfield',
								autoHeight : true,
								autoScroll : true,
								border : true,
								style : {
									"margin-left" : "3px",
									"margin-top" : "0px",
									"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-3px"
											: "-5px")
											: "3px"
								},
								items : detailItems
							};
							return new Ext.form.FieldSet(res);
						};

					

						this.ckanDetail = getFieldSet([
							this.detailCkanFileType,
							this.detailCkanCsvDelimiter,
							this.detailCkanCsvQuote,
							this.detailCkanCsvEncoding,
							this.detailCkanSkipRows,
							this.detailCkanLimitRows,
							this.detailckanXslSheetNumber,
							this.detailCkanId,
							this.detailCkanUrl
						]);
						// CKAN Detail Fields - END

						//REST details
						var restFieldSetItems=[];
						for (var i=0;i<ns.restFormFields.length;i++) {
							var formFieldData=ns.restFormFields[i];
							var formField=formFieldData[0].apply(this,formFieldData[1]);
							restFieldSetItems.push(formField);
						}
						this.restDetail = getFieldSet(restFieldSetItems);
						this.restRequestHeadersDetail = restFieldSetItems[ns.INDEX_REQUEST_HEADERS];
						this.restJsonPathAttributesDetails= restFieldSetItems[ns.INDEX_REQUEST_JSON_PATH_ATTRIBUTES];

						var c = {};
						this.manageParsGrid = new Sbi.tools.ManageDatasetParameters(
								c);

						this.manageParsPanel = new Ext.Panel({
							id : 'man-pars',
							layout : 'fit',
							autoScroll : false,
							bodyStyle : Ext.isIE ? 'padding:0 0 3px 3px;'
									: 'padding:3px 3px;',
							border : true,
							height : 'auto',
							autoScroll : true,
							boxMaxHeight : 300,
							boxMinHeight : 100,
							items : [ this.manageParsGrid ],
							scope : this
						});

						this.typeTab = new Ext.Panel(
								{
									title : LN('sbi.generic.type'),
									itemId : 'type',
									width : 350,
									//it enables the scrolling on type tab
									autoScroll :true, 
									items : {
										id : 'type-detail',
										itemId : 'type-detail',
										// columnWidth: 0.4,
										xtype : 'fieldset',
										scope : this,
										labelWidth : 80,
										defaultType : 'textfield',
										autoHeight : true,
										autoScroll : true,
										border : false,
										style : {
											"margin-left" : "5px",
											"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-5px"
													: "-7px")
													: "0"
										},
										items : [ this.dsTypeDetail,
												this.jClassDetail,
												this.customDataDetail,
												this.scriptDetail,
												this.queryDetail,
												this.WSDetail, this.fileDetail,
												this.qbeQueryDetail,
												this.flatDetail,
												this.ckanDetail,
												this.restDetail,
												this.manageParsPanel
												 ]
									}
								});
					}

					// handler for the upload file button
					,
					uploadFileButtonHandler : function(btn, e) {

						Sbi
								.debug("[DatasetManagementPanel.uploadFileButtonHandler]: IN");

						var form = Ext.getCmp('datasetForm').getForm();

						Sbi
								.debug("[DatasetManagementPanel.uploadFileButtonHandler]: form is equal to ["
										+ form + "]");

						var completeUrl = thisPanel.configurationObject.uploadFileService;
						var baseUrl = completeUrl.substr(0, completeUrl
								.indexOf("?"));

						Sbi
								.debug("[DatasetManagementPanel.uploadFileButtonHandler]: base url is equal to ["
										+ baseUrl + "]");

						var queryStr = completeUrl.substr(completeUrl
								.indexOf("?") + 1);
						var params = Ext.urlDecode(queryStr);

						Sbi
								.debug("[DatasetManagementPanel.uploadFileButtonHandler]: base url is equal to ["
										+ Sbi.toSource(params) + "]");

						Sbi
								.debug("[DatasetManagementPanel.uploadFileButtonHandler]: form is valid ["
										+ form.isValid() + "]");

						form
								.submit({
									clientValidation : false,
									url : baseUrl // a multipart form cannot
									// contain parameters on its
									// main URL; they must POST
									// parameters
									,
									params : params,
									waitMsg : LN('sbi.generic.wait'),
									success : function(form, action) {
										Ext.MessageBox.alert('Success!',
												'File Uploaded to the Server');
										var fileNameUploaded = Ext.getCmp(
												'fileUploadField').getValue();
										// get only the file name from the path
										// (for IE)
										fileNameUploaded = fileNameUploaded
												.replace(/^.*[\\\/]/, '');
										Ext.getCmp('fileNameField').setValue(
												fileNameUploaded);
										this.fileUploadFormPanel
												.activateFileTypePanel(action.result.fileExtension);
										thisPanel.fileUploaded = true;
									},
									failure : function(form, action) {
										switch (action.failureType) {
										case Ext.form.Action.CLIENT_INVALID:
											Ext.Msg
													.alert('Failure',
															'Form fields may not be submitted with invalid values');
											break;
										case Ext.form.Action.CONNECT_FAILURE:
											Ext.Msg
													.alert('Failure',
															'Ajax communication failed');
											break;
										case Ext.form.Action.SERVER_INVALID:
											if (action.result.msg
													&& action.result.msg
															.indexOf("NonBlockingError:") >= 0) {
												var error = Ext.util.JSON
														.decode(action.result.msg);
												if (error.error == 'USED') {// the
																			// file
																			// is
																			// used
																			// from
																			// more
																			// than
																			// one
																			// dataset
													Sbi.exception.ExceptionHandler
															.showErrorMessage(
																	LN('sbi.ds.'
																			+ error.error)
																			+ error.used
																			+ " datasets",
																	LN("sbi.ds.failedToUpload"));
												} else {
													Sbi.exception.ExceptionHandler
															.showErrorMessage(
																	LN('sbi.ds.'
																			+ error.error),
																	LN("sbi.ds.failedToUpload"));
												}

											} else {
												Sbi.exception.ExceptionHandler
														.showErrorMessage(
																action.result.msg,
																'Failure');
											}
										}
									},
									scope : this
								});

						Sbi
								.debug("[DatasetManagementPanel.uploadFileButtonHandler]: OUT");
					}

					,
					initTrasfTab : function() {
						this.transfTypesStore = new Ext.data.SimpleStore({
							fields : [ 'trasfTypeCd' ],
							data : config.trasfTypes,
							autoLoad : false
						});

						var detailTransfType = new Ext.form.ComboBox({
							name : 'trasfTypeCd',
							store : this.transfTypesStore,
							width : 350,// 120,
							fieldLabel : LN('sbi.ds.trasfTypeCd'),
							displayField : 'trasfTypeCd', // what the user
							// sees in the
							// popup
							valueField : 'trasfTypeCd', // what is passed to the
							// 'change' event
							typeAhead : true,
							forceSelection : true,
							mode : 'local',
							triggerAction : 'all',
							selectOnFocus : true,
							editable : false,
							allowBlank : true,
							validationEvent : true,
							xtype : 'combo'
						});
						detailTransfType.addListener('select',
								this.activateTransfForm, this);

						this.trasfTypeDetail = new Ext.form.FieldSet(
								{									
									labelWidth : 120,
									defaults : {
										width : 350,// 260,
										border : true
									},
									defaultType : 'textfield',
									autoHeight : true,
									autoScroll : true,
									bodyStyle : Ext.isIE ? 'padding:0 0 5px 15px;'
											: 'padding:10px 15px;',
									border : true,
									style : {
										"margin-left" : "10px",
										"margin-top" : "10px",
										"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-10px"
												: "-13px")
												: "10px"
									},
									items : [ detailTransfType ]
								});

						var detailPivotCName = {
							maxLength : 40,
							minLength : 1,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.ds.pivotColName'),
							allowBlank : false,
							validationEvent : true,
							name : 'pivotColName'
						};

						var detailPivotCValue = {
							maxLength : 40,
							minLength : 1,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.ds.pivotColValue'),
							allowBlank : false,
							validationEvent : true,
							name : 'pivotColValue'
						};

						var detailPivotRName = {
							maxLength : 40,
							minLength : 1,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.ds.pivotRowName'),
							allowBlank : true,
							validationEvent : true,
							name : 'pivotRowName'
						};

						var detailIsNumRow = new Ext.form.Checkbox({
							xtype : 'checkbox',
							itemId : 'pivotIsNumRows',
							name : 'pivotIsNumRows',
							fieldLabel : LN('sbi.ds.pivotIsNumRows')
						});

						this.trasfDetail = new Ext.form.FieldSet(
								{
									labelWidth : 150,
									defaults : {
										width : 350,
										border : true
									},
									defaultType : 'textfield',
									autoHeight : true,
									autoScroll : true,
									bodyStyle : Ext.isIE ? 'padding:0 0 5px 15px;'
											: 'padding:10px 15px;',
									border : true,
									style : {
										"margin-left" : "10px",
										"margin-top" : "10px",
										"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-10px"
												: "-13px")
												: "10px"
									},
									items : [ detailPivotCName,
											detailPivotCValue,
											detailPivotRName, detailIsNumRow ]
								});

						this.transfTab = new Ext.Panel(
								{
									title : LN('sbi.ds.transfType'),
									itemId : 'transf',
									width : 350,
									items : {
										id : 'transf-detail',
										itemId : 'transf-detail',
										xtype : 'fieldset',
										scope : this,
										labelWidth : 90,
										defaultType : 'textfield',
										autoHeight : true,
										autoScroll : true,
										bodyStyle : Ext.isIE ? 'padding:0 0 5px 15px;'
												: 'padding:0px 0px;',
										border : false,
										style : {
											"margin-left" : "10px",
											"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-10px"
													: "-13px")
													: "0"
										},
										items : [ this.trasfTypeDetail,
												this.trasfDetail ]
									}
								});
					}

					,
					initPersistTab : function() {
						this.isPersisted = new Ext.form.Checkbox({
							xtype : 'checkbox',
							itemId : 'isPersisted',
							name : 'isPersisted',
							fieldLabel : LN('sbi.ds.isPersisted')
						});
						// extjs4: this.isPersisted.addListener('change',
						// this.activatePersistForm, this);
						this.isPersisted.addListener('check',
								this.activatePersistForm, this);

						var persistTableName = new Ext.form.TextField({
							maxLength : 50,
							minLength : 1,
							width : 200,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.ds.persistTableName'),
							allowBlank : false,
							validationEvent : true,
							name : 'persistTableName'
						});

						var fsPersist = new Ext.form.FieldSet(
								{
									labelWidth : 150,
									defaults : {
										// width : 200,
										border : false
									},
									defaultType : 'textfield',
									autoHeight : true,
									autoScroll : true,
									bodyStyle : Ext.isIE ? 'padding:0 0 5px 15px;'
											: 'padding:10px 15px;',
									border : true,
									style : {
										"margin-left" : "10px",
										"margin-top" : "10px",
										"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-10px"
												: "-13px")
												: "10px"
									},
									items : [ persistTableName ]
								});
						return fsPersist;
					}

					,
					initSchedulingPersistencePanel : function() {

						this.isScheduled = new Ext.form.Checkbox(
								{
									bodyStyle : Ext.isIE ? 'padding:0 0 5px 15px;'
											: 'padding:10px 15px;',
									xtype : 'checkbox',
									hidden : true,
									style : {
										"margin-left" : "10px",
										"margin-top" : "10px",
										"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-10px"
												: "-13px")
												: "10px"
									},
									itemId : 'isScheduled',
									name : 'isScheduled',
									fieldLabel : LN('sbi.ds.isScheduled')
								});
						this.isScheduled.addListener('check',
								this.showScheduleForm, this);

						/* Datepicker */
						var datefield = new Ext.form.FieldSet(
								{
									// renderTo: 'datefield',
									labelWidth : 100, // label settings here
														// cascade unless
														// overridden
									// title: 'Datepicker',
									bodyStyle : 'padding:5px 5px 0',
									width : 360,
									defaults : {
										width : 220
									},
									defaultType : 'datefield',
									items : [
											{
												fieldLabel : LN('sbi.ds.persist.cron.startdate'),
												name : 'startDate',
												format : 'd/m/Y'
											},
											{
												fieldLabel : LN('sbi.ds.persist.cron.enddate'),
												name : 'endDate',
												format : 'd/m/Y'
											} ]
								});

						var minutesDs = new Ext.data.ArrayStore({
							data : [ [ '0', '00' ], [ '1', '01' ],
									[ '2', '02' ], [ '3', '03' ],
									[ '4', '04' ], [ '5', '05' ],
									[ '6', '06' ], [ '7', '07' ],
									[ '8', '08' ], [ '9', '09' ],
									[ '10', '10' ], [ '11', '11' ],
									[ '12', '12' ], [ '13', '13' ],
									[ '14', '14' ], [ '15', '15' ],
									[ '16', '16' ], [ '17', '17' ],
									[ '18', '18' ], [ '19', '19' ],
									[ '20', '20' ], [ '21', '21' ],
									[ '22', '22' ], [ '23', '23' ],
									[ '24', '24' ], [ '25', '25' ],
									[ '26', '26' ], [ '27', '27' ],
									[ '28', '28' ], [ '29', '29' ],
									[ '30', '30' ], [ '31', '31' ],
									[ '32', '32' ], [ '33', '33' ],
									[ '34', '34' ], [ '35', '35' ],
									[ '36', '36' ], [ '37', '37' ],
									[ '38', '38' ], [ '39', '39' ],
									[ '40', '40' ], [ '41', '41' ],
									[ '42', '42' ], [ '43', '43' ],
									[ '44', '44' ], [ '45', '45' ],
									[ '46', '46' ], [ '47', '47' ],
									[ '48', '48' ], [ '49', '49' ],
									[ '50', '50' ], [ '51', '51' ],
									[ '52', '52' ], [ '53', '53' ],
									[ '54', '54' ], [ '55', '55' ],
									[ '56', '56' ], [ '57', '57' ],
									[ '58', '58' ], [ '59', '59' ] ],
							fields : [ 'value', 'text' ]
						});

						var hoursDs = new Ext.data.ArrayStore({
							data : [ [ '0', '0' ], [ '1', '1' ], [ '2', '2' ],
									[ '3', '3' ], [ '4', '4' ], [ '5', '5' ],
									[ '6', '6' ], [ '7', '7' ], [ '8', '8' ],
									[ '9', '9' ], [ '10', '10' ],
									[ '11', '11' ], [ '12', '12' ],
									[ '13', '13' ], [ '14', '14' ],
									[ '15', '15' ], [ '16', '16' ],
									[ '17', '17' ], [ '18', '18' ],
									[ '19', '19' ], [ '20', '20' ],
									[ '21', '21' ], [ '22', '22' ],
									[ '23', '23' ] ],
							fields : [ 'value', 'text' ]
						});

						var daysDs = new Ext.data.ArrayStore({
							data : [ [ '1', '1' ], [ '2', '2' ], [ '3', '3' ],
									[ '4', '4' ], [ '5', '5' ], [ '6', '6' ],
									[ '7', '7' ], [ '8', '8' ], [ '9', '9' ],
									[ '10', '10' ], [ '11', '11' ],
									[ '12', '12' ], [ '13', '13' ],
									[ '14', '14' ], [ '15', '15' ],
									[ '16', '16' ], [ '17', '17' ],
									[ '18', '18' ], [ '19', '19' ],
									[ '20', '20' ], [ '21', '21' ],
									[ '22', '22' ], [ '23', '23' ],
									[ '24', '24' ], [ '25', '25' ],
									[ '26', '26' ], [ '27', '27' ],
									[ '28', '28' ], [ '29', '29' ],
									[ '30', '30' ] ],
							fields : [ 'value', 'text' ]
						});

						var monthsDs = new Ext.data.ArrayStore(
								{
									data : [
											[
													'1',
													LN('sbi.ds.persist.cron.month.january') ],
											[
													'2',
													LN('sbi.ds.persist.cron.month.february') ],
											[
													'3',
													LN('sbi.ds.persist.cron.month.march') ],
											[
													'4',
													LN('sbi.ds.persist.cron.month.april') ],
											[
													'5',
													LN('sbi.ds.persist.cron.month.may') ],
											[
													'6',
													LN('sbi.ds.persist.cron.month.june') ],
											[
													'7',
													LN('sbi.ds.persist.cron.month.july') ],
											[
													'8',
													LN('sbi.ds.persist.cron.month.august') ],
											[
													'9',
													LN('sbi.ds.persist.cron.month.september') ],
											[
													'10',
													LN('sbi.ds.persist.cron.month.october') ],
											[
													'11',
													LN('sbi.ds.persist.cron.month.november') ],
											[
													'12',
													LN('sbi.ds.persist.cron.month.december') ] ],
									fields : [ 'value', 'text' ]
								});

						var weekdaysDs = new Ext.data.ArrayStore(
								{
									data : [
											[
													'1',
													LN('sbi.ds.persist.cron.weekday.monday') ],
											[
													'2',
													LN('sbi.ds.persist.cron.weekday.tuesday') ],
											[
													'3',
													LN('sbi.ds.persist.cron.weekday.wednesday') ],
											[
													'4',
													LN('sbi.ds.persist.cron.weekday.thursday') ],
											[
													'5',
													LN('sbi.ds.persist.cron.weekday.friday') ],
											[
													'6',
													LN('sbi.ds.persist.cron.weekday.saturday') ],
											[
													'7',
													LN('sbi.ds.persist.cron.weekday.sunday') ] ],
									fields : [ 'value', 'text' ]
								});

						var minuteColumn = [ {
							bodyStyle : 'padding-right:5px;',
							// items: {
							xtype : 'fieldset',
							title : 'Minute',
							defaultType : 'radio', // each item will be a radio
													// button
							items : [
									{
										checked : true,
										hideLabel : true,
										boxLabel : LN('sbi.ds.persist.cron.everyminute'),
										name : 'minute-choose',
										id : 'minute-every',
										inputValue : 'every',
										scope : this,
										handler : function(ctl, val) {
											var multiselect = Ext
													.getCmp('minutesMultiselect');
											if (val) {
												multiselect.disable();
												// this.setSchedulingCronLine();
											} else {
												multiselect.enable();
												// this.setSchedulingCronLine();
											}
										}
									},
									{
										hideLabel : true,
										boxLabel : LN('sbi.ds.persist.cron.choose'),
										name : 'minute-choose',
										id : 'minute-choose',
										inputValue : 'choose'
									},
									{
										hideLabel : true,
										xtype : "multiselect",
										id : "minutesMultiselect",
										dataFields : [ "value", "text" ],
										valueField : "value",
										displayField : "text",
										width : 75,
										height : 150,
										disabled : true,
										allowBlank : false,
										bodyStyle : 'overflowY: auto; position:relative;',
										store : minutesDs,
										listeners : {
											// change:
											// this.setSchedulingCronLine,
											scope : this
										}
									} ]
						// }
						} ];
						var hourColumn = [ {
							bodyStyle : 'padding-right:5px;',
							// items: {
							xtype : 'fieldset',
							title : 'Hour',
							defaultType : 'radio', // each item will be a radio
													// button
							items : [
									{
										checked : true,
										hideLabel : true,
										boxLabel : LN('sbi.ds.persist.cron.everyhour'),
										name : 'hour-choose',
										id : 'hour-every',
										inputValue : 'every',
										scope : this,
										handler : function(ctl, val) {
											var multiselect = Ext
													.getCmp('hoursMultiselect');
											if (val) {
												multiselect.disable();
												// this.setSchedulingCronLine();
											} else {
												multiselect.enable();
												// this.setSchedulingCronLine();
											}
										}
									},
									{
										hideLabel : true,
										boxLabel : LN('sbi.ds.persist.cron.choose'),
										name : 'hour-choose',
										id : 'hour-choose',
										inputValue : 'choose'
									}, {
										hideLabel : true,
										xtype : "multiselect",
										id : "hoursMultiselect",
										dataFields : [ "value", "text" ],
										valueField : "value",
										displayField : "text",
										width : 75,
										height : 150,
										disabled : true,
										allowBlank : false,
										store : hoursDs,
										listeners : {
											// change:
											// this.setSchedulingCronLine,
											scope : this
										}
									} ]
						// }
						} ];
						var dayColumn = [ {
							bodyStyle : 'padding-right:5px;',
							// items: {
							xtype : 'fieldset',
							title : 'Day',
							defaultType : 'radio', // each item will be a radio
													// button
							items : [
									{
										checked : true,
										hideLabel : true,
										boxLabel : LN('sbi.ds.persist.cron.everyday'),
										name : 'day-choose',
										id : 'day-every',
										inputValue : 'every',
										scope : this,
										handler : function(ctl, val) {
											var multiselect = Ext
													.getCmp('daysMultiselect');
											if (val) {
												multiselect.disable();
												// this.setSchedulingCronLine();
											} else {
												multiselect.enable();
												// this.setSchedulingCronLine();
											}
										}
									},
									{
										hideLabel : true,
										boxLabel : LN('sbi.ds.persist.cron.choose'),
										name : 'day-choose',
										id : 'day-choose',
										inputValue : 'choose'
									}, {
										hideLabel : true,
										xtype : "multiselect",
										id : "daysMultiselect",
										dataFields : [ "value", "text" ],
										valueField : "value",
										displayField : "text",
										width : 75,
										height : 150,
										disabled : true,
										allowBlank : false,
										store : daysDs,
										listeners : {
											// change:
											// this.setSchedulingCronLine,
											scope : this
										}
									} ]
						// }
						} ];
						var monthColumn = [ {
							bodyStyle : 'padding-right:5px;',
							// items: {
							xtype : 'fieldset',
							title : 'Month',
							defaultType : 'radio', // each item will be a radio
													// button
							items : [
									{
										checked : true,
										hideLabel : true,
										boxLabel : LN('sbi.ds.persist.cron.everymonth'),
										name : 'month-choose',
										id : 'month-every',
										inputValue : 'every',
										scope : this,
										handler : function(ctl, val) {
											var multiselect = Ext
													.getCmp('monthsMultiselect');
											if (val) {
												multiselect.disable();
												// this.setSchedulingCronLine();
											} else {
												multiselect.enable();
												// this.setSchedulingCronLine();
											}
										}
									},
									{
										hideLabel : true,
										boxLabel : LN('sbi.ds.persist.cron.choose'),
										name : 'month-choose',
										id : 'month-choose',
										inputValue : 'choose'
									}, {
										hideLabel : true,
										xtype : "multiselect",
										id : "monthsMultiselect",
										dataFields : [ "value", "text" ],
										valueField : "value",
										displayField : "text",
										width : 75,
										height : 150,
										disabled : true,
										allowBlank : false,
										store : monthsDs,
										listeners : {
											// change:
											// this.setSchedulingCronLine,
											scope : this
										}
									} ]
						// }
						} ];
						var weekdayColumn = [ {
							bodyStyle : 'padding-right:5px;',
							// items: {
							xtype : 'fieldset',
							title : 'Weekday',
							defaultType : 'radio', // each item will be a radio
													// button
							items : [
									{
										checked : true,
										hideLabel : true,
										boxLabel : LN('sbi.ds.persist.cron.everyweekday'),
										name : 'weekday-choose',
										id : 'weekday-every',
										inputValue : 'every',
										scope : this,
										handler : function(ctl, val) {
											var multiselect = Ext
													.getCmp('weekdaysMultiselect');
											if (val) {
												multiselect.disable();
												// this.setSchedulingCronLine();
											} else {
												multiselect.enable();
												// this.setSchedulingCronLine();
											}
										}
									},
									{
										hideLabel : true,
										boxLabel : LN('sbi.ds.persist.cron.choose'),
										name : 'weekday-choose',
										id : 'weekday-choose',
										inputValue : 'choose'
									}, {
										hideLabel : true,
										xtype : "multiselect",
										id : "weekdaysMultiselect",
										dataFields : [ "value", "text" ],
										valueField : "value",
										displayField : "text",
										width : 75,
										height : 150,
										disabled : true,
										allowBlank : false,
										store : weekdaysDs,
										style : {
											"margin-left" : "5px",
											"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-5px"
													: "-7px")
													: "0"
										},
										listeners : {
											// change:
											// this.setSchedulingCronLine,
											scope : this
										}
									} ]
						// }
						} ];

						var schedulingCronLine = new Ext.form.TextField(
								{
									width : 300,
									value : '0 * * * * *',
									fieldLabel : LN('sbi.ds.persist.cron.schedulingline'),
									labelSeparator : ':',
									readOnly : true,
									hidden : true,
									allowBlank : true,
									validationEvent : true,
									id : 'schedulingCronLine'
								});
						
						var cronHumanDescription = new Ext.form.TextField(
								{
									width : 400,
									value : '-',
									fieldLabel : LN('sbi.ds.persist.cron.schedulingline'),
									labelSeparator : ':',
									readOnly : true,
									allowBlank : true,
									validationEvent : true,
									fieldStyle:"border:none 0px black",
									id : 'cronHumanDescription'
								});
						
						var cronNextFire = new Ext.form.TextField(
								{
									width : 400,
									value : '-',
									fieldLabel : LN('sbi.ds.persist.cron.nextfire'),
									labelSeparator : ':',
									readOnly : true,
									allowBlank : true,
									validationEvent : true,
									fieldStyle:"border:none 0px black",
									id : 'cronNextFire'
								});

						var cronPanel = new Ext.form.FieldSet(
								{
									autoHeight : true,
									hidden : true,
									title : 'Update: scheduling detail',
									bodyStyle : Ext.isIE ? 'padding:0 0 5px 15px;'
											: 'padding:10px 15px;',
									border : true,
									style : {
										"margin-left" : "10px",
										"margin-top" : "10px",
										"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-10px"
												: "-13px")
												: "10px"
									},
									items : [
											datefield,
											{
												layout : 'column',
												border : false,
												id : 'cronColumn',
												// defaults are applied to all
												// child items unless otherwise
												// specified by child item
												defaults : {
													columnWidth : '.2',
													border : false
												},
												items : [ minuteColumn,
														hourColumn, dayColumn,
														monthColumn,
														weekdayColumn ]
											}, schedulingCronLine, cronHumanDescription, cronNextFire  ]
								});
						return cronPanel;
					}

					,
					setSchedulingCronLine : function() {
						var second, minute, hour, day, month, weekday;

						second = '0';
						minute = this.getSelection('minute');
						hour = this.getSelection('hour');
						day = this.getSelection('day');
						month = this.getSelection('month');
						weekday = this.getSelection('weekday');
						// Support for specifying both a day-of-week and a
						// day-of-month value is not complete
						// (you must currently use the '?' character in one of
						// these fields).
						if (day == '*' && weekday != '*') {
							day = '?';
						} else {
							weekday = '?';
						}
						
						var cron =  minute + " " + hour + " " + day + " " + month
						+ " " + weekday

						Ext.get('schedulingCronLine').dom.value = second + " " + cron;
						
						Ext.get('cronHumanDescription').dom.value = prettyCron.toString(cron);
						Ext.get('cronNextFire').dom.value = prettyCron.getNext(cron);
					}

					,
					getSelection : function(name) {
						var chosen;
						if (Ext.get(name + "-every").dom.checked) {
							chosen = '*';
						} else {
							chosen = Ext.getCmp(name + 'sMultiselect')
									.getValue();
							if (!chosen.length) {
								chosen = '*';
							}
						}
						return chosen;
					}

					,
					initAdvancedTab : function() {

						this.persistDetail = this.initPersistTab();

						this.schedulingDetail = this
								.initSchedulingPersistencePanel();

						this.persistPanel = new Ext.Panel(
								{
									title : LN('sbi.ds.persist'),
									itemId : 'persistPanel',
									width : '100%', // 500,
									items : {
										id : 'persist-detail',
										itemId : 'persist-detail',
										xtype : 'fieldset',
										scope : this,
										labelWidth : 90,
										defaultType : 'textfield',
										autoHeight : true,
										autoScroll : true,
										bodyStyle : Ext.isIE ? 'padding:0 0 5px 15px;'
												: 'padding:0px 0px;',
										border : false,
										style : {
											"margin-left" : "0px",
											"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-10px"
													: "-13px")
													: "0"
										},
										items : [ this.isPersisted,
												this.persistDetail,
												this.isScheduled,
												this.schedulingDetail ]
									}
								});

						this.advancedTab = new Ext.Panel(
								{
									title : LN('sbi.ds.advancedTab'),
									itemId : 'advanced',
									width : '100%', // 550,
									items : {
										id : 'advanced-detail',
										itemId : 'advanced-detail',
										xtype : 'fieldset',
										scope : this,
										labelWidth : 90,
										defaultType : 'textfield',
										autoHeight : true,
										autoScroll : true,
										bodyStyle : Ext.isIE ? 'padding:0 0 5px 10px;'
												: 'padding:0px 0px;',
										border : true,
										style : {
											"margin-left" : "15px",
											"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-5px"
													: "-8px")
													: "0"
										},
										items : [ this.persistPanel ]
									}
								});
					}

					,
					initTestTab : function() {
						this.tbTestDSButton = new Ext.Toolbar.Button({
							text : LN('sbi.ds.test'),
							width : 30,
							handler : this.test,
							iconCls : 'icon-execute',
							scope : this
						});

						this.tbTestToolbar = new Ext.Toolbar({
							buttonAlign : 'right',
							height : 25,
							scope : this,
							items : [ this.tbTestDSButton ]
						});

						var c = {
							tbar : this.tbTestToolbar
						};
						this.parsGrid = new Sbi.tools.ParametersFillGrid(c);

						this.datasetTestTab = new Ext.Panel({
							title : LN('sbi.ds.test'),
							id : 'test-pars',
							layout : 'fit',
							autoScroll : true,
							bodyStyle : Ext.isIE ? 'padding:0 0 10px 10px;'
									: 'padding:10px 10px;',
							border : true,
							items : [ this.parsGrid ],
							scope : this
						});
						this.datasetTestTab.addListener('activate',
								this.activateDsTestTab, this);

						this.configurationObject.tabItems = [ this.detailTab,
								this.typeTab, this.transfTab, this.advancedTab, // this.persistTab,
																				// this.flatTab,
								this.datasetTestTab ];
					}

					// OVERRIDING METHOD
					,
					addNewItem : function() {
						if (this.tabs.disabled) {
							this.manageFormsEnabling();
						}

						var recordConf={
							id : null,
							name : '',
							label : '',
							description : '',
							dsTypeCd : '',
							catTypeVn : '',
							isPublic : '',
							usedByNDocs : 0,
							csvDelimiter : '',
							fileType : '',
							csvQuote : '',
							skipRows : '',
							limitRows : '',
							xslSheetNumber : '',
							fileName : '',
							query : '',
							queryScript : '',
							queryScriptLanguage : '',
							dataSource : '',
							wsAddress : '',
							wsOperation : '',
							script : '',
							scriptLanguage : '',
							jclassName : '',
							jclassNameForCustom : '',
							customData : '',
							pars : [],
							trasfTypeCd : '',
							pivotColName : '',
							pivotColValue : '',
							pivotRowName : '',
							pivotIsNumRows : '',
							isPersisted : '',
							persistTableName : '',
							isScheduled : '',
							schedulingCronLine : '',
							startDate : '',
							endDate : '',
							flatTableName : '',
							dataSourceFlat : '',
							qbeSQLQuery : '',
							qbeJSONQuery : '',
							qbeDataSource : '',
							qbeDatamarts : '',
							dsVersions : [],
							userIn : '',
							dateIn : '',
							versNum : 2,
							versId : 0,
							meta : [],
							scopeCd : '',
							ckanFileType : '',
							ckanCsvDelimiter : '',
							ckanCsvQuote : '',
							ckanCsvEncoding : '',
							ckanSkipRows : '',
							ckanLimitRows : '',
							ckanXslSheetNumber : '',
							ckanId : '',
							ckanUrl : ''
						};

						//add REST params
						for (var i=0;i<ns.restFields.length;i++) {
							var field=ns.restFields[i];
							recordConf[field]='';
						}

						this.newRecord = new Ext.data.Record(recordConf);

						this.setValues(this.newRecord);
						this.manageParsGrid.loadItems([]);
						this.manageDsVersionsGrid.loadItems([]);
						this.manageDatasetFieldMetadataGrid.loadItems([]);

						this.tabs.items.each(function(item) {
							item.doLayout();
						});
						this.trasfDetail.setVisible(false);
						this.persistDetail.setVisible(false);

						if (this.newRecord != null
								&& this.newRecord != undefined) {
							this.mainElementsStore.add(this.newRecord);
							this.rowselModel.selectLastRow(true);
						}
						this.tabs.setActiveTab(0);
					}

					,
					cloneItem : function() {
						var values = this.getValues();
						var params = this.buildParamsToSendToServer(values);
						var arrayPars = this.manageParsGrid.getParsArray();

						this.newRecord = this.buildNewRecordToSave(values);
						this.newRecord.set('pars', arrayPars);
						this.setValues(this.newRecord);

						if (arrayPars) {
							this.manageParsGrid.loadItems(arrayPars);
						} else {
							this.manageParsGrid.loadItems([]);
						}
						this.manageDsVersionsGrid.loadItems([]);

						this.tabs.items.each(function(item) {
							item.doLayout();
						});
						if (this.newRecord != null
								&& this.newRecord != undefined) {
							this.mainElementsStore.add(this.newRecord);
							this.rowselModel.selectLastRow(true);
						}
						this.tabs.setActiveTab(0);
					},
					buildNewRecordDsVersion : function(values) {
						var actualValues = this.getValues();

						var recordConf={
							id : actualValues['id'],
							name : actualValues['name'],
							label : actualValues['label'],
							usedByNDocs : actualValues['usedByNDocs'],
							dsVersions : [],
							pars : values['pars'],
							description : actualValues['description'],
							dsTypeCd : values['dsTypeCd'],
							catTypeVn : values['catTypeVn'],
							isPublic : values['isPublic'],
							usedByNDocs : values['usedByNDocs'],
							fileName : values['fileName'],
							csvDelimiter : values['csvDelimiter'],
							skipRows : values['skipRows'],
							limitRows : values['limitRows'],
							xslSheetNumber : values['xslSheetNumber'],
							fileType : values['fileType'],
							csvQuote : values['csvQuote'],
							query : values['query'],
							queryScript : values['queryScript'],
							queryScriptLanguage : values['queryScriptLanguage'],
							dataSource : values['dataSource'],
							wsAddress : values['wsAddress'],
							wsOperation : values['wsOperation'],
							script : values['script'],
							scriptLanguage : values['scriptLanguage'],
							jclassName : values['jclassName'],
							customData : values['customData'],
							trasfTypeCd : values['trasfTypeCd'],
							pivotColName : values['pivotColName'],
							pivotColValue : values['pivotColValue'],
							pivotRowName : values['pivotRowName'],
							pivotIsNumRows : values['pivotIsNumRows'],
							isPersisted : values['isPersisted'],
							persistTableName : values['persistTableName'],
							isScheduled : values['isScheduled'],
							schedulingCronLine : values['schedulingCronLine'],
							startDate : values['startDate'],
							endDate : [ 'endDate' ],
							dataSourceFlat : values['dataSourceFlat'],
							flatTableName : values['flatTableName'],
							qbeSQLQuery : values['qbeSQLQuery'],
							qbeJSONQuery : values['qbeJSONQuery'],
							qbeDataSource : values['qbeDataSource'],
							qbeDatamarts : values['qbeDatamarts'],
							userIn : values['userIn'],
							dateIn : values['dateIn'],
							versNum : values['versNum'],
							versId : values['versId'],
							meta : values['meta'],
							scopeCd : values['scopeCd'],
							ckanFileType : values['ckanFileType'],
							// ckanFileName: values['ckanFileName'],
							ckanCsvDelimiter : values['ckanCsvDelimiter'],
							ckanCsvQuote : values['ckanCsvQuote'],
							ckanCsvEncoding : values['ckanCsvEncoding'],
							ckanSkipRows : values['ckanSkipRows'],
							ckanLimitRows : values['ckanLimitRows'],
							ckanXslSheetNumber : values['ckanXslSheetNumber'],
							ckanId : values['ckanId'],
							ckanUrl : values['ckanUrl']
						};

						//add REST params
						for (var i=0;i<ns.restFields.length;i++) {
							var field=ns.restFields[i];
							recordConf[field]=values[field];
						}

						var newRec = new Ext.data.Record(recordConf);
						return newRec;
					}

					,
					buildNewRecordToSave : function(values) {

						var recordConf= {
							id : null,
							name : values['name'],
							label : '...',
							usedByNDocs : 0,
							dsVersions : [],
							description : values['description'],
							dsTypeCd : values['dsTypeCd'],
							catTypeVn : values['catTypeVn'],
							isPublic : values['isPublic'],
							usedByNDocs : values['usedByNDocs'],
							fileName : values['fileName'],
							csvDelimiter : values['csvDelimiter'],
							skipRows : values['skipRows'],
							limitRows : values['limitRows'],
							xslSheetNumber : values['xslSheetNumber'],
							fileType : values['fileType'],
							csvQuote : values['csvQuote'],
							query : values['query'],
							queryScript : values['queryScript'],
							queryScriptLanguage : values['queryScriptLanguage'],
							dataSource : values['dataSource'],
							wsAddress : values['wsAddress'],
							wsOperation : values['wsOperation'],
							script : values['script'],
							scriptLanguage : values['scriptLanguage'],
							customData : values['customData'],
							jclassName : values['jclassName'],
							jclassNameForCustom : values['jclassNameForCustom'],
							trasfTypeCd : values['trasfTypeCd'],
							pivotColName : values['pivotColName'],
							pivotColValue : values['pivotColValue'],
							pivotRowName : values['pivotRowName'],
							pivotIsNumRows : values['pivotIsNumRows'],
							isPersisted : values['isPersisted'],
							persistTableName : values['persistTableName'],
							isScheduled : values['isScheduled'],
							schedulingCronLine : values['schedulingCronLine'],
							startDate : values['startDate'],
							endDate : [ 'endDate' ],
							dataSourceFlat : values['dataSourceFlat'],
							flatTableName : values['flatTableName'],
							qbeSQLQuery : values['qbeSQLQuery'],
							qbeJSONQuery : values['qbeJSONQuery'],
							qbeDataSource : values['qbeDataSource'],
							qbeDatamarts : values['qbeDatamarts'],
							userIn : values['userIn'],
							dateIn : values['dateIn'],
							versNum : values['versNum'],
							versId : values['versId'],
							meta : values['meta'],
							scopeCd : values['scopeCd'],
							ckanFileType : values['ckanFileType'],
							// ckanFileName: values['ckanFileName'],
							ckanCsvDelimiter : values['ckanCsvDelimiter'],
							ckanCsvQuote : values['ckanCsvQuote'],
							ckanCsvEncoding : values['ckanCsvEncoding'],
							ckanSkipRows : values['ckanSkipRows'],
							ckanLimitRows : values['ckanLimitRows'],
							ckanXslSheetNumber : values['ckanXslSheetNumber'],
							ckanId : values['ckanId'],
							ckanUrl : values['ckanUrl']
						};

						//add REST params
						for (var i=0;i<ns.restFields.length;i++) {
							var field=ns.restFields[i];
							recordConf[field]=values[field];
						}
						var newRec = new Ext.data.Record(recordConf);
						return newRec;
					}

					,
					buildParamsToSendToServer : function(values) {
						var params = {
							name : values['name'],
							label : values['label'],
							description : values['description'],
							dsTypeCd : values['dsTypeCd'],
							catTypeVn : values['catTypeVn'],
							isPublic : values['isPublic'],
							usedByNDocs : values['usedByNDocs'],
							fileName : values['fileName'],
							csvDelimiter : values['csvDelimiter'],
							skipRows : values['skipRows'],
							limitRows : values['limitRows'],
							xslSheetNumber : values['xslSheetNumber'],
							fileType : values['fileType'],
							csvQuote : values['csvQuote'],
							query : values['query'],
							queryScript : values['queryScript'],
							queryScriptLanguage : values['queryScriptLanguage'],
							dataSource : values['dataSource'],
							wsAddress : values['wsAddress'],
							wsOperation : values['wsOperation'],
							script : values['script'],
							scriptLanguage : values['scriptLanguage'],
							jclassName : values['jclassName'],
							customData : values['customData'],
							trasfTypeCd : values['trasfTypeCd'],
							pivotColName : values['pivotColName'],
							pivotColValue : values['pivotColValue'],
							pivotRowName : values['pivotRowName'],
							pivotIsNumRows : values['pivotIsNumRows'],
							isFromSaveNoMetadata : values['isFromSaveNoMetadata'],
							isPersisted : values['isPersisted'],
							persistTableName : values['persistTableName'],
							isScheduled : values['isScheduled'],
							schedulingCronLine : values['schedulingCronLine'],
							startDate : values['startDate'],
							endDate : values['endDate'],
							dataSourceFlat : values['dataSourceFlat'],
							flatTableName : values['flatTableName'],
							qbeSQLQuery : values['qbeSQLQuery'],
							qbeJSONQuery : values['qbeJSONQuery'],
							qbeDataSource : values['qbeDataSource'],
							qbeDatamarts : values['qbeDatamarts'],
							userIn : values['userIn'],
							dateIn : values['dateIn'],
							versNum : values['versNum'],
							versId : values['versId'],
							meta : values['meta'],
							fileUploaded : thisPanel.fileUploaded,
							scopeCd : values['scopeCd'],
							ckanFileType : values['ckanFileType'],
							// ckanFileName: values['ckanFileName'],
							ckanCsvDelimiter : values['ckanCsvDelimiter'],
							ckanCsvQuote : values['ckanCsvQuote'],
							ckanCsvEncoding : values['ckanCsvEncoding'],
							ckanSkipRows : values['ckanSkipRows'],
							ckanLimitRows : values['ckanLimitRows'],
							ckanXslSheetNumber : values['ckanXslSheetNumber'],
							ckanId : values['ckanId'],
							ckanUrl : values['ckanUrl']
						};

						//add REST params
						for (var i=0;i<ns.restFields.length;i++) {
							var field=ns.restFields[i];
							params[field]=values[field];
						}
						return params;
					}

					,
					updateNewRecord : function(record, values, arrayPars, meta,
							customString) {
						record.set('label', values['label']);
						record.set('name', values['name']);
						record.set('description', values['description']);
//						record.set('usedByNDocs', 0); ?? why it was forced to 0 ?? It causes issue KNOWAGE-67
						record.set('dsTypeCd', values['dsTypeCd']);
						record.set('catTypeVn', values['catTypeVn']);
						record.set('isPublic', values['isPublic']);
						record.set('fileName', values['fileName']);
						record.set('csvDelimiter', values['csvDelimiter']);
						record.set('skipRows', values['skipRows']);
						record.set('limitRows', values['limitRows']);
						record.set('xslSheetNumber', values['xslSheetNumber']);
						record.set('fileType', values['fileType']);
						record.set('csvQuote', values['csvQuote']);
						record.set('query', values['query']);
						record.set('queryScript', values['queryScript']);
						record.set('queryScriptLanguage',
								values['queryScriptLanguage']);
						record.set('dataSource', values['dataSource']);
						record.set('wsAddress', values['wsAddress']);
						record.set('wsOperation', values['wsOperation']);
						record.set('script', values['script']);
						record.set('scriptLanguage', values['scriptLanguage']);
						record.set('jclassName', values['jclassName']);
						record.set('jclassNameForCustom', values['jclassName']);
						record.set('customData', values['customData']);
						record.set('trasfTypeCd', values['trasfTypeCd']);
						record.set('pivotColName', values['pivotColName']);
						record.set('pivotColValue', values['pivotColValue']);
						record.set('pivotRowName', values['pivotRowName']);
						record.set('pivotIsNumRows', values['pivotIsNumRows']);
						record.set('isPersisted', values['isPersisted']),
								record.set('persistTableName',
										values['persistTableName']), record
										.set('isScheduled',
												values['isScheduled']), record
										.set('schedulingCronLine',
												values['schedulingCronLine']),
								record.set('startDate', values['startDate']),
								record.set('endDate', values['endDate']),
								record.set('dataSourceFlat',
										values['dataSourceFlat']), record.set(
										'flatTableName',
										values['flatTableName']), record.set(
										'qbeSQLQuery', values['qbeSQLQuery']);
						record.set('qbeJSONQuery', values['qbeJSONQuery']);
						record.set('qbeDataSource', values['qbeDataSource']);
						record.set('qbeDatamarts', values['qbeDatamarts']);
						record.set('userIn', values['userIn']);
						record.set('dateIn', values['dateIn']);
						record.set('versNum', values['versNum']);
						record.set('versId', values['versId']);
						record.set('scopeCd', values['scopeCd']);
						record.set('ckanFileType', values['ckanFileType']),
								record.set('ckanCsvDelimiter',
										values['ckanCsvDelimiter']), record
										.set('ckanCsvQuote',
												values['ckanCsvQuote']), record
										.set('ckanCsvEncoding',
												values['ckanCsvEncoding']),
								record.set('ckanSkipRows',
										values['ckanSkipRows']), record.set(
										'ckanLimitRows',
										values['ckanLimitRows']), record.set(
										'ckanXslSheetNumber',
										values['ckanXslSheetNumber']), record
										.set('ckanId', values['ckanId']),
								record.set('ckanUrl', values['ckanUrl']);

						//add REST params
						for (var i=0;i<ns.restFields.length;i++) {
							var field=ns.restFields[i];
							record.set(field,values[field]);
						}

						if (arrayPars) {
							record.set('pars', arrayPars);
						}
						if (arrayPars) {
							record.set('pars', arrayPars);
						}
						if (customString) {
							record.set('customData', customString);
						}
						if (meta) {
							record.set('meta', meta);
						}

					}

					,
					updateMainStore : function(idRec) {
						var values = this.getValues();
						var record;
						var length = this.mainElementsStore.getCount();
						for (var i = 0; i < length; i++) {
							var tempRecord = this.mainElementsStore.getAt(i);
							if (tempRecord.data.id == idRec) {
								record = tempRecord;
							}
						}
						var params = this.buildParamsToSendToServer(values);
						var arrayPars = this.manageParsGrid.getParsArray();
						var meta = this.manageDatasetFieldMetadataGrid
								.getValues()
						this.updateNewRecord(record, values, arrayPars, meta);
						this.mainElementsStore.commitChanges();
					}

					,
					updateDsVersionsOfMainStore : function(idRec) {
						var arrayVersions = this.manageDsVersionsGrid
								.getCurrentDsVersions();
						if (arrayVersions) {
							var record;
							var length = this.mainElementsStore.getCount();
							for (var i = 0; i < length; i++) {
								var tempRecord = this.mainElementsStore
										.getAt(i);
								if (tempRecord.data.id == idRec) {
									record = tempRecord;
								}
							}
							record.set('dsVersions', arrayVersions);
							this.mainElementsStore.commitChanges();
						}
					}

					,
					getValues : function() {
						var values = this.getForm().getFieldValues();
						//workaround for grid value (grid not recognized as aform field)
						values[ns.restFields[ns.INDEX_REQUEST_HEADERS]]=
							this.restRequestHeadersDetail.getValue();
						values[ns.restFields[ns.INDEX_REQUEST_JSON_PATH_ATTRIBUTES]]=
							this.restJsonPathAttributesDetails.getValue();

						// in the refactored architecture all detail parts will
						// be
						// implemented as external widgets. The state of each
						// widget
						// will be read using the method getFormState.
						// QueryDetail is
						// the first refactored part.
						values = Ext.apply(values, this.queryDetail
								.getFormState());
						// ----------------------------------------------------------

						if (this.customDataDetail.isVisible()) {
							values.jclassName = values.jclassNameForCustom;
						}

						values.fileUploaded = thisPanel.fileUploaded;

						return values;

					}

					,
					setValues : function(record) {
						record.data.jclassNameForCustom = record.data.jclassName;
						this.getForm().loadRecord(record);
						//workaround for grid value (grid not recognized as a form field)
						this.restRequestHeadersDetail.setValue(record.data[ns.restFields[ns.INDEX_REQUEST_HEADERS]]);
						this.restJsonPathAttributesDetails.setValue(record.data[ns.restFields[ns.INDEX_REQUEST_JSON_PATH_ATTRIBUTES]]);

						// in the refactored architecture all detail parts will
						// be
						// implemented as external widgets. The state of each
						// widget
						// will be set using the method setFormState.
						// QueryDetail is
						// the first refactored part.
						this.queryDetail.setFormState(record.data);
						// ----------------------------------------------------------
						this.fileUploadFormPanel.setFormState(record.data);
						this.hasDocumentsAssociated = record.hasDocumentsAssociated || false;
						this.hasFederationsAssociated = record.hasFederationsAssociated || false;
						
					}

					// OVERRIDING save method
					,
					save : function() {
						
						this.setSchedulingCronLine();
						var values = this.getValues();
						if (values['dsTypeCd']==='REST') {
							var check=ns.checkRESTParams(values);
							if (check === false) { 
								return;
							}
						}
						
						if (!this.validValues(values)){
							Ext.MessageBox
								.alert(LN('sbi.generic.error'),LN('sbi.generic.validationError') +
											' [' + LN('sbi.ds.transfType') + ' Tab]');
								return;
						}
						var idRec = values['id'];
						if (idRec == 0 || idRec == null || idRec === '') {
							this.doSave("yes");
						} else {
							//checks if the dataset has some documents associated: if yes asks confirm to continue.
							//if it has some federations associated it will be checked by server side and eventually blocked 
							//(it depends by the changes' type)
							if (this.hasDocumentsAssociated !== "" && this.hasFederationsAssociated == ""){
								var msg = String.format(LN('sbi.ds.saveconfirm.msg'), this.hasDocumentsAssociated);
								Ext.MessageBox
								.confirm(
										LN('sbi.ds.saveconfirm.title'),
										msg,
										function(btn, text) {
				    		                if ( btn == 'yes' ) {
				    		                	this.doSave();
				    		                }
				    					}, 
				    					this);
							}else{
								this.doSave();
							}
							
//							Ext.MessageBox
//									.confirm(
//											LN('sbi.ds.recalculatemetadataconfirm.title'),
//											LN('sbi.ds.recalculatemetadataconfirm.msg'),
//											this.doSave, this);
						}
					}
					
					/**
					 * Validation forms' values
					 */
					, validValues: function(values) {
						var toReturn = true;
						//TRANFORMATION VALIDATION
						if (values['trasfTypeCd'] == 'PIVOT_TRANSFOMER'){
							if (values['pivotColName'] === "" || values['pivotColValue'] === ""){
								toReturn = false;
							}
						}
						return toReturn;
					}
					
					/**
					 * Opens the loading mask
					 */
					,
					showMask : function() {
						if (this.loadMask == null) {
							this.loadMask = new Ext.LoadMask(this, {
								msg : "Loading..."
							});
						}
						this.loadMask.show();
					}

					/**
					 * Closes the loading mask
					 */
					,
					hideMask : function() {
						if (this.loadMask != null) {
							this.loadMask.hide();
						}
					},
					doSave : function(recalculateMetadata) {
						
						var values = this.getValues();
						if(this.isFromSaveNoMetadata){
							values.isFromSaveNoMetadata = this.isFromSaveNoMetadata;
							Ext.Ajax.on('beforerequest', this.hideMask, this);
						}
						var idRec = values['id'];
						var newRec;
						var newDsVersion;
						var isNewRec = false;
						var params = this.buildParamsToSendToServer(values);
						params.dsId = idRec;
						//params.recalculateMetadata = recalculateMetadata;
						params.recalculateMetadata = 'true';	// always recalculate metadata
						var arrayPars = this.manageParsGrid.getParsArray();
						var customString = this.customDataGrid.getDataString();
						var meta = this.manageDatasetFieldMetadataGrid
								.getValues()
						if (idRec == 0 || idRec == null || idRec === '') {
							this.updateNewRecord(this.newRecord, values,
									arrayPars, meta, customString);
							isNewRec = true;
						} else {
							var record;
							var oldType;
							var length = this.mainElementsStore.getCount();
							for (var i = 0; i < length; i++) {
								var tempRecord = this.mainElementsStore
										.getAt(i);
								if (tempRecord.data.id == idRec) {
									record = tempRecord;
									oldType = record.get('dsTypeCd');
								}
							}
							this.updateNewRecord(record, values, arrayPars,
									meta, customString);

							newDsVersion = new Ext.data.Record({
								dsId : values['id'],
								dateIn : values['dateIn'],
								userIn : values['userIn'],
								versId : values['versId'],
								type : oldType,
								versNum : values['versNum']
							});
						}

						if (arrayPars) {
							params.pars = Ext.util.JSON.encode(arrayPars);
						}
						if (customString) {
							params.customData = Ext.util.JSON
									.encode(customString);
						}
						if (this.manageDatasetFieldMetadataGrid) {
							params.meta = Ext.util.JSON
									.encode(this.manageDatasetFieldMetadataGrid
											.getValues());
						}

						if (idRec) {
							params.id = idRec;
						}

						Ext.Ajax
								.request({
									url : this.services['saveItemService'],
									params : params,
									method : 'POST',
									success : function(response, options) {
										if (response !== undefined) {
											if (response.responseText !== undefined) {

												var content = Ext.util.JSON
														.decode(response.responseText);
												if (content.responseText !== 'Operation succeded') {
													// if (mask != undefined)
													// mask.hide();
													Ext.MessageBox
															.show({
																title : LN('sbi.generic.error'),
																msg : content,
																width : 150,
																buttons : Ext.MessageBox.OK
															});
												} else {
													var itemId = content.id;
													var dateIn = content.dateIn;
													var userIn = content.userIn;
													var versId = content.versId;
													var versNum = content.versNum;
													var meta = content.meta;

													// update metadata
													var length = this.mainElementsStore
															.getCount();
													for (var i = 0; i < length; i++) {
														var tempRecord = this.mainElementsStore
																.getAt(i);
														if (tempRecord.data.id == itemId) {
															tempRecord.set(
																	'meta',
																	meta);

															var dsType = tempRecord
																	.get('dsTypeCd');
															// update filename
															// if the dataset is
															// a FileDataset
															if ((dsType != undefined)
																	&& (dsType == 'File')) {
																var fileType = tempRecord
																		.get('fileType');
																fileType = fileType
																		.toLowerCase();
																var dsLabel = tempRecord
																		.get('label');
																var updatedFileName = dsLabel
																		+ '.'
																		+ fileType
																tempRecord
																		.set(
																				'fileName',
																				updatedFileName); // update
																									// record
																									// in
																									// JsonStore
																Ext
																		.getCmp(
																				'fileNameField')
																		.setValue(
																				updatedFileName); // update
																									// field
																									// in
																									// GUI

															}

															tempRecord.commit();
															this.manageDatasetFieldMetadataGrid
																	.loadItems(
																			meta,
																			tempRecord);
															break;
														}
													}

													// var newRecord =
													// this.mainElementsStore.getAt(this.mainElementsStore.getCount()-1);
													// newRecord.data.meta =
													// meta;
													// newRecord.commit();
													if (isNewRec
															&& itemId != null
															&& itemId !== '') {

														var record;

														for (var i = 0; i < length; i++) {
															var tempRecord = this.mainElementsStore
																	.getAt(i);

															this.rowselModel
																	.selectLastRow(true);
															if (!tempRecord.data.id
																	|| tempRecord.data.id == 0) {
																tempRecord.set(
																		'id',
																		itemId);
																tempRecord
																		.set(
																				'dateIn',
																				dateIn);
																tempRecord
																		.set(
																				'userIn',
																				userIn);
																tempRecord
																		.set(
																				'versId',
																				versId);
																tempRecord
																		.set(
																				'versNum',
																				versNum);
																tempRecord.set(
																		'meta',
																		meta);
																// update
																// filename if
																// the dataset
																// is a
																// FileDataset
																var dsType = tempRecord
																		.get('dsTypeCd');
																if ((dsType != undefined)
																		&& (dsType == 'File')) {
																	var fileType = tempRecord
																			.get('fileType');
																	fileType = fileType
																			.toLowerCase();
																	var dsLabel = tempRecord
																			.get('label');
																	var updatedFileName = dsLabel
																			+ '.'
																			+ fileType
																	tempRecord
																			.set(
																					'fileName',
																					updatedFileName); // update
																										// record
																										// in
																										// JsonStore
																	Ext
																			.getCmp(
																					'fileNameField')
																			.setValue(
																					updatedFileName); // update
																										// field
																										// in
																										// GUI

																}
																tempRecord
																		.commit();
																this.detailFieldId
																		.setValue(itemId);
																this.detailFieldUserIn
																		.setValue(userIn);
																this.detailFieldDateIn
																		.setValue(dateIn);
																this.detailFieldVersNum
																		.setValue(versNum);
																this.detailFieldVersId
																		.setValue(versId);
															}
														}
													} else {
														if (newDsVersion != null
																&& newDsVersion != undefined) {
															this.manageDsVersionsGrid
																	.getStore()
																	.addSorted(
																			newDsVersion);
															this.manageDsVersionsGrid
																	.getStore()
																	.commitChanges();
															var values = this
																	.getValues();
															this
																	.updateDsVersionsOfMainStore(values['id']);
														}
													}
													this.mainElementsStore
															.commitChanges();
													if (isNewRec) {
														this.rowselModel
																.selectLastRow();
													}
													thisPanel.fileUploaded = false // reset
																					// to
																					// default
													
													if(this.isFromSaveNoMetadata){
														Ext.MessageBox
														.show({
															title : LN('sbi.generic.result'),
															msg : LN('sbi.ds.saved.no.meta'),
															width : 200,
															buttons : Ext.MessageBox.OK
														});
														
														var values = this.getValues();
														this.isFromSaveNoMetadata = false;
														this.doSave();
													}
													else if(this.isFromSaveWithMetadata){
														this.isFromSaveWithMetadata = false;
														Ext.MessageBox
														.show({
															title : LN('sbi.generic.result'),
															msg : LN('sbi.generic.resultMsg'),
															width : 200,
															buttons : Ext.MessageBox.OK
														});
													}
												}

											} else {
												Sbi.exception.ExceptionHandler
														.showErrorMessage(
																LN('sbi.generic.serviceResponseEmpty'),
																LN('sbi.generic.serviceError'));
											}
										} else {
											Sbi.exception.ExceptionHandler
													.showErrorMessage(
															LN('sbi.generic.savingItemError'),
															LN('sbi.generic.serviceError'));
										}
									},
									failure : Sbi.exception.ExceptionHandler.handleFailure,
									scope : this
								});
					}

					,
					manageQbeQuery : function(theQbeDatasetBuilder, qbeQuery) {
						var jsonQuery = qbeQuery.data.jsonQuery;
						this.qbeJSONQuery.setValue(Ext.util.JSON
								.encode(jsonQuery));
						var sqlQuery = qbeQuery.data.sqlQuery.sql;
						this.qbeSQLQuery.setValue(sqlQuery);
						var datamarts = qbeQuery.data.datamarts;
						this.qbeDatamarts.setValue(datamarts);
						var parameters = qbeQuery.data.parameters;
						this.manageParsGrid.loadItems([]);
						this.manageParsGrid.loadItems(parameters);
					}

					,
					jsonTriggerFieldHandler : function() {
						// alert("jsonTriggerFieldHandler");
						var values = this.getValues();
						var datasetId = values['id'];
						var datasourceLabel = this.detailQbeDataSource
								.getValue();
						if (datasourceLabel == '') {
							Ext.MessageBox
									.show({
										title : LN('sbi.generic.error'),
										msg : LN('sbi.tools.managedatasets.errors.missingdatasource'),
										width : 150,
										buttons : Ext.MessageBox.OK
									});
							return;
						}
						if (datamart == '') {
							Ext.MessageBox
									.show({
										title : LN('sbi.generic.error'),
										msg : LN('sbi.tools.managedatasets.errors.missingdatamart'),
										width : 150,
										buttons : Ext.MessageBox.OK
									});
							return;
						}
						var datamart = this.qbeDatamarts.getValue();
						this.initQbeDataSetBuilder(datasourceLabel, datamart,
								datasetId);
						this.qbeDataSetBuilder.show();
					}

					,
					initQbeDataSetBuilder : function(datasourceLabel, datamart,
							datasetId) {
						if (this.qbeDataSetBuilder === null) {
							this.initNewQbeDataSetBuilder(datasourceLabel,
									datamart, datasetId);
							return;
						}
						if (this.mustRefreshQbeView(datasourceLabel, datamart,
								datasetId)) {
							this.qbeDataSetBuilder.destroy();
							this.initNewQbeDataSetBuilder(datasourceLabel,
									datamart, datasetId);
							return;
						}
					}

					,
					initNewQbeDataSetBuilder : function(datasourceLabel,
							datamart, datasetId) {
						this.qbeDataSetBuilder = new Sbi.tools.dataset.QbeDatasetBuilder(
								{
									datasourceLabel : datasourceLabel,
									datamart : datamart,
									datasetId : datasetId,
									jsonQuery : this.qbeJSONQuery.getValue(),
									qbeParameters : this.manageParsGrid
											.getParsArray(),
									modal : true,
									width : this.getWidth() - 50,
									height : this.getHeight() - 50,
									listeners : {
										hide : {
											fn : function(theQbeDatasetBuilder) {
												theQbeDatasetBuilder
														.getQbeQuery(); // asynchronous
											},
											scope : this
										},
										gotqbequery : {
											fn : this.manageQbeQuery,
											scope : this
										}
									}
								});
					}

					,
					mustRefreshQbeView : function(datasourceLabel, datamart,
							datasetId) {
						if (datasourceLabel == this.qbeDataSetBuilder
								.getDatasourceLabel()
								&& datamart == this.qbeDataSetBuilder
										.getDatamart()
								&& datasetId == this.qbeDataSetBuilder
										.getDatasetId()) {
							return false;
						}
						return true;
					}

					// METHOD TO BE OVERRIDDEN IN EXTENDED ELEMENT!!!!!
					,
					info : function() {
						var win_info_2;
						if (!win_info_2) {
							win_info_2 = new Ext.Window({
								id : 'win_info_2',
								autoLoad : {
									url : Sbi.config.contextName + '/themes/'
											+ Sbi.config.currTheme
											+ '/html/dsrules.html'
								},
								layout : 'fit',
								width : 620,
								height : 410,
								autoScroll : true,
								closeAction : 'close',
								buttonAlign : 'left',
								plain : true,
								title : LN('sbi.ds.help')
							});
						}
						;
						win_info_2.show();
					}

					,
					transfInfo : function() {
						var win_info_4;
						if (!win_info_4) {
							win_info_4 = new Ext.Window({
								id : 'win_info_4',
								autoLoad : {
									url : Sbi.config.contextName + '/themes/'
											+ Sbi.config.currTheme
											+ '/html/dsTrasformationHelp.html'
								},
								layout : 'fit',
								width : 760,
								height : 420,
								autoScroll : true,
								closeAction : 'close',
								buttonAlign : 'left',
								plain : true,
								title : LN('sbi.ds.help')
							});
						}
						;
						win_info_4.show();
					}

					,
					persistInfo : function() {
						var win_info_5;
						if (!win_info_5) {
							win_info_5 = new Ext.Window({
								id : 'win_persist_5',
								autoLoad : {
									url : Sbi.config.contextName + '/themes/'
											+ Sbi.config.currTheme
											+ '/html/dsPersistenceHelp.html'
								},
								layout : 'fit',
								width : 760,
								height : 420,
								autoScroll : true,
								closeAction : 'close',
								buttonAlign : 'left',
								plain : true,
								title : LN('sbi.ds.help')
							});
						}
						;
						win_info_5.show();
					}

					,
					fieldsMetadata : function() {
console.log(this.manageDatasetFieldMetadataGrid);
						if (!this.win_info_metadata) {
							this.win_info_metadata = new Ext.Window(
									{
										layout : 'fit',
										width : 370,
										height : 350,
										closeAction : 'hide',
										autoScroll : false,
										title : LN('sbi.ds.metadata'),
										items : [ this.manageDatasetFieldMetadataGrid ],
										buttonAlign : 'right',
										buttons : [ {
											text : LN('sbi.general.ok'),
											handler : function() {
												console.log(this.win_info_metadata);
												this.manageDatasetFieldMetadataGrid
														.updateRecord();
												this.win_info_metadata.hide();
											},
											scope : this
										} ]
									});
						}
						;
						if (this.manageDatasetFieldMetadataGrid.emptyStore) {
							Sbi.exception.ExceptionHandler
									.showInfoMessage(LN("sbi.ds.field.metadata.nosaved"));
						} else {
							this.win_info_metadata.show();
						}

					}

					,
					
					linkDataset : function() {
						
						if (this.manageDatasetFieldMetadataGrid.record.json == undefined) {
							
							Ext.MessageBox
							.show({
								title : LN('sbi.generic.error'),
								msg : "Please create the dataset and save before creating links",
								width : 250,
								buttons : Ext.MessageBox.OK
							});
					return;
							
						} else {
							
							var id = this.manageDatasetFieldMetadataGrid.record.json.id;
							var label = this.manageDatasetFieldMetadataGrid.record.json.label;
							document.location.href = "/knowage/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/dataset/linkDataset.jsp&id="+id+"&label="+label

						}
						
						
						
						
						
					}
					,
					
					saveNoMetadata : function() {
						var values = this.getValues();
						
						var isPersisted = values['isPersisted'];
									
						var params = this.buildParamsToSendToServer(values);
						
						if(isPersisted){
							this.isFromSaveNoMetadata = true;
							this.doSave();
						} else {
							Ext.MessageBox.show({
			                	title: LN('sbi.generic.error'),
			                    msg: LN('sbi.ds.save.no.metadata.warning'),
			                    width: 350,
			                    height: 200,
			                    buttons: Ext.MessageBox.OK
			               });						
						}
					}
					,
					profileAttrs : function() {
						var win_info_3;
						if (!win_info_3) {
							win_info_3 = new Ext.Window({
								id : 'win_info_3',
								layout : 'fit',
								width : 220,
								height : 350,
								closeAction : 'close',
								buttonAlign : 'left',
								autoScroll : true,
								plain : true,
								items : {
									xtype : 'grid',
									border : false,
									columns : [ {
										header : LN('sbi.ds.pars'),
										width : 170
									} ],
									store : this.profileAttributesStore
								}
							});
						}
						;
						win_info_3.show();
					}
					,
					onDeleteItemFailure : function(response, options) {
					
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
			                	title: LN('sbi.generic.error'),
			                    msg: errMessage,
			                    width: 400,
			                    buttons: Ext.MessageBox.OK
			               });
			      		}else{
			                Ext.MessageBox.show({
			                	title: LN('sbi.generic.error'),
			                    msg: LN('sbi.generic.deletingItemError'),
			                    width: 150,
			                    buttons: Ext.MessageBox.OK
			               });
			      		}
					
					
					
					
					
					
					
					
					
					}

					/**
					 * Opens the loading mask
					 */
					,
					showMask : function() {
						this.un('afterlayout', this.showMask, this);
						if (this.loadMask == null) {
							this.loadMask = new Ext.LoadMask(Ext.getBody(), {
								msg : "Loading...  "
							});
						}
						if (this.loadMask) {
							this.loadMask.show();
						}
					}

					/**
					 * Closes the loading mask
					 */
					,
					hideMask : function() {
						if (this.loadMask && this.loadMask != null) {
							this.loadMask.hide();
						}
					}
				});
