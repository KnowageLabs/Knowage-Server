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
 * 
 * Container of the meta models catalogue.
 * If the user clicks on a metamodel an execution of the associated engine will be started
 * 
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 *  
 */
 
  
Ext.define('Sbi.tools.model.MetaModelsView', {
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
		,
		initTemplate : function() {
			// BUILD THE TPL
			Sbi.debug('DataViewPanel bulding the tpl...');

			var noItem = LN('sbi.browser.folderdetailpanel.emptytext');
			var title = LN('sbi.ds.listTitle');
			
			/*
			this.tpl = new Ext.XTemplate(
					'<div id="sample-ct">', 	            
		 	           '<div class="group-view-small">',
		 	            '<ul>',
		 	            	'<tpl if="root.length == 0">',
		 	            		'<div id="empty-group-message">',
		 	            		noItem,
		 	            		'</div>',
		 	            	'</tpl>',        
		 	            	'<tpl for=".">',
			 	            	'<dd class="box-no-border">',
									'<a href="#" class="box-link">',
										'<div class="box-map">',
											'<img src="'+Sbi.config.contextName+'/themes/sbi_default/img/metamodel/metamodel.png" alt=" " />',
//											'<span class="shadow"></span>',
										'</div>',
										'<div class="box-text">',
											'<br><h2>{name}</h2>',
											'<p>{description}</p>',
										'</div>',
									'</a>',
								'</dd>',
//			                    '<dd class="group-item">',
//			                    	'<div class="button">',
//			                        	'<div class="meta-models-view">  &nbsp ',
//			                    	    	'<span class="shadow"></span>',
//			                    	    '</div>',
//										'<p><b>{name}</b></p>',
//										'<p>{description}</p>',
//									'</div>',
//			                    '</dd>',
			                '</tpl>',	              
			          '</ul>',
		 	          '</div>',
		 	        '</div>');
			
*/
			this.tpl = new Ext.XTemplate(
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
										' 			<tpl if="type == \'MODEL\'">',										
													'<img  align="center" src="'+Sbi.config.contextName+'/themes/sbi_default/img/metamodel/metamodel.png" alt=" " />',
													'</tpl>',
											'<span class="shadow"></span>',
											'<div class="hover">',
												'<div class="box-actions-container">',
									            '    <ul class="box-actions">',	    
									    		'<li class="qbe"><a href="#" title="Show Qbe"></a></li>',
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

						this.fireEvent('executeDocument','QBE','MODEL',record);
						
	    }
	});