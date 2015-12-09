/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 * Container of the federated dataset catalogue.
 * 
 * 
 *  @author
 * Giulio Gavardi(giulio.gavardi@eng.it)
 *  
 */
 
  
Ext.define('Sbi.tools.dataset.FederatedDatasetView', {
	extend : 'Ext.DataView'

		,
		config : {
			/**
			 * The Ext.data.Store to bind this DataView to.
			 */
			store : null,

			/**
			 * A simple CSS selector that will be used to determine what nodes this
			 * DataView will be working with.
			 */
			itemSelector : null,

			/**
			 * The definition of the columns of the grid.
			 * {@link Sbi.widgets.store.InMemoryFilteredStore#InMemoryFilteredStore}
			 */
			columns : [],
			/**
			 * The list of the properties that should be filtered
			 */
			filteredProperties : new Array(),
			
			autoScroll : true


		}

		/**
		 * In this constructor you must pass configuration
		 */
		,
		constructor : function(config) {
					
			this.initConfig(config);
			this.initTemplate();
			
			Ext.apply(this, config || {});
			
			this.itemSelector = 'dd';
			//this.overClass = 'over';
			this.trackOver = true;
			//this.overItemCls = 'x-item-over';
			this.overItemCls = 'over';
			this.frame = true;
			this.emptyText = LN('sbi.ds.noDataset');
			this.inline = {
				wrap : false
			};
			this.scrollable = 'horizontal';

			this.callParent(arguments);
						
			this.addListener('itemclick', this.onClick, this);

			this.addEvents(
			        /**
			         * @event event1
			         * Execute the qbe clicking in the model
					 * @param {Object} docType 'QBE'
					 * @param {Object} inputType 'MODEL'
					 * @param {Object} record the record that contains all the information of the metamodel
			         */
			        'executeDocument'
					);
		}
		
				
		
		,initTemplate : function() {
			
			createFederationDefinition = function () {
				var urlToCall =  Sbi.config.contextName+"/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp";
				window.location.href = urlToCall;
			}
			
			// BUILD THE TPL
			Sbi.debug('DataViewPanel bulding the tpl...');

			var noItem = LN('sbi.browser.folderdetailpanel.emptytext');
			var title = LN('sbi.ds.listTitle');

			var buttonShowQbe ='<li class="qbe"><a id="showQbe" href="#" title="Show Qbe"></a></li>';
			var buttonEditFederated = '<li class="editFederated"><a id="editFederated" href="#" title="Edit federated"></a></li>';
			var buttonDeleteFederated = '<li class="deleteFederated"><a id="deleteFederated" href="#" title="Delete federated"></a></li>';
			var createFederation = '<button class="btn btn-default" onclick="createFederationDefinition()"><a id="createFederated" href="#" title="Create federated"></a>CREATE FEDERATION</button>';
			var buttonHelpOnLine= Sbi.user.functionalities.indexOf("Glossary")!=-1 ? '<li class="MyDataHelpOnLine"><a id="MHOL" href="#" title="Show Help OnLine"></a></li>' : "";
			
			this.tpl = new Ext.XTemplate(
					'<script src="'+Sbi.config.contextName+'/js/src/ext4/sbi/tools/dataset/FederatedDatasetView.js">',
					'</script>',
					'<div class="list-actions-container createfederationbtn">',
					createFederation,
					'</div>',
					'<div id="list-container" class="main-datasets-list">', 	            
		 	            	'<tpl if="root.length == 0">',
		 	            		'<div id="empty-group-message">',
		 	            		noItem,
		 	            		'</div>',
		 	            	'</tpl>', 
		 	            	'<tpl for=".">',
								'<dd class="box">',
									'<div class="box-container">',
										'<div class="box-figure">',
													'<img  align="center" src="'+Sbi.config.contextName+'/themes/sbi_default/img/metamodel/federation.png" alt=" " />',
											'<span class="shadow"></span>',
											'<div class="hover">',
												'<div class="box-actions-container">',
									            '    <ul class="box-actions">',	    
									    		buttonShowQbe,
									    		buttonEditFederated,
									    		buttonDeleteFederated,
									    		'    </ul>',
												'</div>',
											'</div>',										
										'</div>',
										'<div title="{name}" class="box-text">',
											'<h2>{name}</h2>',
											'<p>{[Ext.String.ellipsis(values.description, 100, false)]}</p>',
										'</div>',
									'</div>',
								'</dd>',
							 '</tpl>',	 
							 '<div style="clear:left"></div>',

				'</div>'
			);			
			Sbi.debug('DataViewPanel tpl built.');

			return this.tpl;
		}
	
		,onClick : function(obj, record, item, index, e, eOpts) {
			
				if(e.target.id == 'createFederated')
				{
					var urlToCall = Sbi.config.contextName+"/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp";
					window.location.href = urlToCall;
					}
					else {
						this.fireEvent('executeDocument','QBE','FEDERATED_DATASET',record);
				}
				
				if(e.target.id == 'editFederated')
					{
					var id = record.data.id;
					var label = record.data.label;
					var urlToCall = Sbi.config.contextName+"/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp&id="+id+"&label="+label;
					window.location.href = urlToCall;
					}
				else {
						this.fireEvent('executeDocument','QBE','FEDERATED_DATASET',record);
				}
				
				if(e.target.id == 'deleteFederated')
				{
					var id = record.data.id;
					
					Ext.Ajax.request
					(
						{ 
							url: Sbi.config.contextName+"/restful-services/2.0/federateddataset/"+id, 
							method: 'DELETE', 
							headers: { 'Content-Type': 'application/json' }, 
							
						}
					);
										
					var urlToCall =  Sbi.config.contextName+"/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/dataset/manageSelfService.jsp";
					window.location.href = urlToCall;
				}
				else {
					this.fireEvent('executeDocument','QBE','FEDERATED_DATASET',record);
			}
	    }
	});