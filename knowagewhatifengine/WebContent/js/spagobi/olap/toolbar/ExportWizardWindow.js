/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * Window that allows the management of versions
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */



Ext.define('Sbi.olap.toolbar.ExportWizardWindow', {
	extend: 'Ext.window.Window',

	config:{
		height: 200,
		width: 400,
		autoScroll: false,
		title: LN("sbi.olap.toolbar.export.wizard.title"),
		defCsvFieldDelimiter: "|",
		defCsvRowDelimiter: "\\r\\n",
		defTableName: "WHATIFOUTPUTTABLE",
		defType: "table",
		actualVersion: null

	},

	layout:'card',

	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.toolbar && Sbi.settings.olap.toolbar.ExportWizardWindow) {
			Ext.apply(this, Sbi.settings.olap.toolbar.ExportWizardWindow);
		}

		var thisPanel = this;
		this.selectExportTypePanel = this.buildSelectExportType();
		this.selectExportCsvPanel = this.buildSelectExportCsv();
		this.selectExportTablePanel = this.buildSelectExportTable();



		Ext.apply(this,{
			activeItem: 0, // index or id
			bbar: [
			       '->',
			       {
			    	   id: 'out-move-prev',
			    	   text: '&laquo; '+LN('sbi.common.prev'),
			    	   handler: function(btn) {
			    		   thisPanel.navigate(btn.up("panel"), "prev");
			    	   },
			    	   disabled: true
			       },
			       {
			    	   id: 'out-move-next',
			    	   text: LN('sbi.common.next')+' &raquo;',
			    	   handler: function(btn) {
			    		   thisPanel.navigate(btn.up("panel"), "next");
			    	   }
			       },
			       {
			    	   id: 'out-move-ok',
			    	   text: LN('sbi.common.ok'),
			    	   handler: function(btn) {
			    		   thisPanel.navigate(btn.up("panel"), "ok");
			    	   },
			    	   disabled: true
			       },
			       {
			    	   id: 'out-move-cancel',
			    	   text: LN('sbi.common.cancel'),
			    	   handler: function(btn) {
			    		   thisPanel.destroy();
			    	   }
			       }

			       ],

			       items: [this.selectExportTypePanel, this.selectExportTablePanel, this.selectExportCsvPanel]
		});

		this.addEvents(
				/**
				 * @event exportOutput
				 * Thrown when the user wants to export the analysis
				 * @param {Object} configuration
				 */
				'exportOutput'
		);

		this.callParent(arguments);
	},


	navigate: function(panel, direction){
		//send the event and close
		if(direction =="ok"){
			this.fireEvent('exportOutput',this.getFormValues());
			this.destroy();
		}else{
			//update the view
			var layout = panel.getLayout();
			layout[direction]();

			if(this.isCsvExport()){
				layout[direction]();
			}

			if(direction  == "prev"){
				Ext.getCmp('out-move-prev').setDisabled(true);
				Ext.getCmp('out-move-next').setDisabled(false);
				Ext.getCmp('out-move-ok').setDisabled(true);
			}else{
				Ext.getCmp('out-move-prev').setDisabled(false);
				Ext.getCmp('out-move-next').setDisabled(true);
				Ext.getCmp('out-move-ok').setDisabled(false);
			}
		}

	},

	buildSelectExportType: function(){

		var service = Ext.create("Sbi.service.RestService",{
			url: "version"
		});

		var store = Ext.create('Ext.data.Store', {
			model: 'Sbi.olap.VersionModel',
			proxy: {
				type: 'rest',
				url: service.getRestUrlWithParameters(),
				extraParams: service.getRequestParams()
			},
			autoLoad: true,
			sorters: [{
		        sorterFn: function(o1, o2){
		            var rank1 = o1.getId();
		            var rank2 = o2.getId();

		            if (rank1  == rank2) {
		                return 0;
		            }

		            return rank1 < rank2 ? -1 : 1;
		        }
		    }]
		});



		var formPanel = Ext.create('Ext.form.Panel', {
			bodyPadding: 5,

			fieldDefaults: {
				labelAlign: 'left',
				labelWidth: 120
			},

			items: [
			        {
			        	xtype: 'displayfield',
			        	hideEmptyLabel: true,
			        	value:  LN("sbi.olap.toolbar.export.wizard.type.description")
			        },{
			        	xtype: 'radiofield',
			        	name: 'exportType',
			        	inputValue: 'csv',
			        	checked: this.defType =="csv",
			        	fieldLabel: LN("sbi.olap.toolbar.export.wizard.type"),
			        	boxLabel: LN("sbi.olap.toolbar.export.wizard.type.csv")
			        }, {
			        	xtype: 'radiofield',
			        	name: 'exportType',
			        	inputValue: 'table',
			        	fieldLabel: '',
			        	labelSeparator: '',
			        	hideEmptyLabel: false,
			        	checked: this.defType =="table",
			        	boxLabel: LN("sbi.olap.toolbar.export.wizard.type.table")
			        }, {
			        	xtype: 'combo',
			        	name: 'version',
			        	fieldLabel: LN("sbi.olap.toolbar.export.wizard.version"),
			        	store: store,
			        	displayField: 'name',
			        	valueField: 'id',
			        	value: this.actualVersion
			        }]
		});
		return formPanel;
	},

	buildSelectExportCsv: function(){
		var formPanel = Ext.create('Ext.form.Panel', {
			bodyPadding: 5,

			fieldDefaults: {
				labelAlign: 'left',
				labelWidth: 120
			},

			items: [
			        {
			        	xtype: 'displayfield',
			        	hideEmptyLabel: true,
			        	value:  LN("sbi.olap.toolbar.export.wizard.type.csv.description")
			        },{
			        	xtype: 'textfield',
			        	name: 'csvFieldDelimiter',
			        	fieldLabel: LN("sbi.olap.toolbar.export.wizard.type.csv.filter.delimiter"),
			        	value: this.defCsvFieldDelimiter
			        }

			        ]
		});
		return formPanel;
	},

	buildSelectExportTable: function(){
		var formPanel = Ext.create('Ext.form.Panel', {
			bodyPadding: 5,

			fieldDefaults: {
				labelAlign: 'left',
				labelWidth: 120
			},

			items: [
			        {
			        	xtype: 'displayfield',
			        	hideEmptyLabel: true,
			        	value:  LN("sbi.olap.toolbar.export.wizard.type.table.description")
			        },{
			        	xtype: 'textfield',
			        	name: 'tableName',
			        	fieldLabel: LN("sbi.olap.toolbar.export.wizard.type.table.name"),
			        	value: this.defTableName
			        }]
		});
		return formPanel;
	},

	isCsvExport: function(){
		return (this.selectExportTypePanel && this.selectExportTypePanel.getForm().getValues()["exportType"] =='csv');
	},

	getFormValues: function(){
		var values = {
				exportType: this.defType
		};
		if(this.selectExportTypePanel && this.selectExportTypePanel.getForm()){
			values = this.selectExportTypePanel.getForm().getValues();
		}
		if(values.exportType =="csv"){
			Ext.apply(values, this.selectExportCsvPanel.getValues());
		}else{
			Ext.apply(values, this.selectExportTablePanel.getValues());
		}

		return values;
	}




});
