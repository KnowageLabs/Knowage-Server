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



Ext.define('Sbi.olap.toolbar.SaveAsWindow', {
	extend: 'Ext.window.Window',

	config:{
		height: 200,
		width: 400,
		autoScroll: false,
		title: LN("sbi.olap.toolbar.export.wizard.title")
	},

	layout:'fit',

	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.toolbar && Sbi.settings.olap.toolbar.ExportWizardWindow) {
			Ext.apply(this, Sbi.settings.olap.toolbar.ExportWizardWindow);
		}

		var thisPanel = this;

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
			        	value:  LN("sbi.olap.toolbar.save.as.description")
			        },{
			        	xtype: 'textfield',
			        	name: 'versionName',
			        	fieldLabel: LN("sbi.olap.toolbar.save.as.version.name"),
			        	value: ""
			        },{
			        	xtype: 'textarea',
			        	name: 'versionDescription',
			        	fieldLabel: LN("sbi.olap.toolbar.save.as.version.description"),
			        	value: ""
			        }
			        ]
		});



		Ext.apply(this,{
			activeItem: 0, // index or id
			bbar: [
			       '->',
			       {
			    	   id: 'out-move-ok',
			    	   text: LN('sbi.common.ok'),
			    	   handler: function(btn) {
			    		   thisPanel.fireEvent('saveAs',formPanel.getValues());
			    		   thisPanel.destroy();
			    	   }
			       },
			       {
			    	   id: 'out-move-cencel',
			    	   text: LN('sbi.common.cancel'),
			    	   handler: function(btn) {
			    		   thisPanel.destroy();
			    	   }
			       }
			       ],

			       items: [formPanel]
		});

		this.addEvents(
				/**
				 * @event exportOutput
				 * Thrown when the user wants to export the analysis
				 * @param {Object} configuration
				 */
				'saveAs'
		);

		this.callParent(arguments);
	}






});
