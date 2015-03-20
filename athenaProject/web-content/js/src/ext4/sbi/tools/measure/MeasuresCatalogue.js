/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.tools.measure.MeasuresCatalogue', {
	extend: 'Sbi.widgets.grid.GroupedGrid'

		, constructor: function(config) {

			var columns = this.buildColumns();
			thisPanel = this;
			var joinMeasuresButton = Ext.create('Ext.Button', {
				text    : LN('sbi.tools.catalogue.measures.join.btn'),
				tooltip : LN('sbi.tools.catalogue.measures.join.tooltip'),
				hidden	:true,
				//scope: thisPanel,
				handler : function() {
					this.hide();
					selectMeasuresButton.show();
					thisPanel.columns[thisPanel.columns.length-1].hide();
					thisPanel.executeJoin();
				}
			});

			var selectMeasuresButton = Ext.create('Ext.Button', {
				text    : LN('sbi.tools.catalogue.measures.select.btn'),
				tooltip : LN('sbi.tools.catalogue.measures.select.tooltip'),
				handler : function() {
					this.hide();
					joinMeasuresButton.show();
					thisPanel.columns[thisPanel.columns.length-1].show();
				}
			});

			var myconfig = {
					store: this.buildDataStore(), 
					columns: columns,
					dockedItems : [{
						xtype: 'toolbar',
						items: [selectMeasuresButton, joinMeasuresButton]
					}],
					selModel: Ext.create('Ext.selection.CheckboxModel', {
						injectCheckbox: columns.length+1,
						
					    getHeaderConfig: function() {
					        var me = this,
					            showCheck = me.showHeaderCheckbox !== false;

					        return {
					            isCheckerHd: showCheck,
					            text : '&#160;',
					            width: me.headerWidth,
					            sortable: false,
					            draggable: false,
					            resizable: false,
					            hideable: false,
					            menuDisabled: true,
					            hidden: true,//TO HIDE THE COLUMN AT THE BIGINNING
					            dataIndex: '',
					            cls: showCheck ? Ext.baseCSSPrefix + 'column-header-checkbox ' : '',
					            renderer: Ext.Function.bind(me.renderer, me),
					            editRenderer: me.editRenderer || me.renderEmpty,
					            locked: me.hasLockedHeader()
					        };
					    }
					}),
					plugins: [{
						ptype: 'rowexpander',
						rowBodyTpl : [
						              '<div class="htmltable">',
'<div class="measure-detail-container"><div class="group-view"><h2><div class="group-header" style="background-image: none!important">'+LN('sbi.tools.catalogue.measures.measure.properties')+'</div></h2></div>',
'<table>',
'		<tr style="height: 90px">',
'			<td class="measure-detail-measure">',
'			</td>',
'			<td><table>',
'					<tr><td style="width: 100px"><p><b>Name:</b></td><td><p>{alias}</p></td></tr>',
'					<tr><td><p><b>Type:</b></td><td><p>{classType}</p></td>	</tr>',
'					<tr><td><p><b>Column Name:</b></td><td><p>{columnName}</p></td>	</tr>',
'			</table></td>',			
'		</tr>',
'</table></div>',
'<div class="dataset-detail-container"><div class="group-view"><h2><div class="group-header" style="background-image: none!important">'+LN('sbi.tools.catalogue.measures.dataset.properties')+'</div></h2></div>',
'<table>',
'		<tr style="height: 100px">',
'			<td class="measure-detail-dataset">',
'			</td>',
'			<td><table>',
'					<tr><td style="width: 100px"><p><b>Label:</b></td><td><p>{dsLabel}</p></td></tr>',
'					<tr><td><p><b>Name:</b></td><td><p>{dsName}</p></td></tr>',
'					<tr><td><p><b>Category:</b></td><td><p>{dsCategory}</p></td></tr>',
'					<tr><td><p><b>Type:</b></td><td><p>{dsType}</p></td></tr>',
'			</table></td>',
'		</tr>',
'</table></div>',
'</div>'
						              ]
					}]
			};


			this.callParent([myconfig]);

		},

		buildColumns: function(){
			var columns = [{
				text: 'Alias',
				flex: 1,
				dataIndex: 'alias'
			},
			{
				text: 'DS Name',
				flex: 1,
				dataIndex: 'dsName'
			},
			{
				text: 'DS Label',
				flex: 1,
				dataIndex: 'dsLabel'
			},
			{
				text: 'DS Category',
				flex: 1,
				dataIndex: 'dsCategory'
			},
			{
				text: 'DS Type',
				flex: 1,
				dataIndex: 'dsType'
			}];

			return columns;
		},

		buildDataStore: function(){
			var store = Ext.create('Ext.data.Store', {
			    model: 'Sbi.tools.measure.MeasureModel',
			    autoLoad: true
			});

			return store;
		},
		
		executeJoin: function(){
			var measuresLabels = new Array();
			var selected = this.getSelectionModel().getSelection();
			if(selected!=null && selected!=undefined && selected.length>0){
				for(var i=0; i<selected.length; i++){
					measuresLabels.push(selected[i].data.label);
				}
				//var encoded = Ext.JSON.encode(measuresIds);
				Ext.Ajax.request({
					url: Sbi.config.serviceRegistry.getRestServiceUrl({serviceName: 'measures/join'}),
					params: {labels: measuresLabels},
					success : function(response, options) {
						if(response !== undefined && response.responseText !== undefined && response.statusText=="OK") {
							if(response.responseText!=null && response.responseText!=undefined){
								if(response.responseText.indexOf("error.mesage.description")>=0){
									Sbi.exception.ExceptionHandler.handleFailure(response);
								}else{
									alert("Join ok.. look at the responce");
								}
							}
						} else {
							Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
						}
					},
					scope: this,
					failure: Sbi.exception.ExceptionHandler.handleFailure,  
					scope: this
				});
			}
		}

});
