/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  

/**
 * 
 * @author Marco Cortella (marco.cortella@eng.it)
 */

Ext.define('Sbi.adhocreporting.MyAnalysisView', {
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
			
			sorters : new Array(),
			
			autoScroll : false,
			
			PUBLIC_USER: 'public_user'
						
		  //  ,DETAIL_DOCUMENT: 'DocumentDetailManagement',
		  //  CREATE_DOCUMENT: 'CreateDocument'

		}	

		/**
		 * In this constructor you must pass configuration
		 */
		,
		constructor : function(config) {

			this.initServices();
			this.initConfig(config);
			this.initTemplate();
		
			Ext.apply(this, config || {});
		
			this.itemSelector = 'dd';  
			this.trackOver = true;
			this.overItemCls = 'over';
			this.frame = true;
			this.emptyText = LN('No Documents');
			this.inline = {
					wrap : false
			};
			this.scrollable = 'horizontal';
		
			this.callParent(arguments);
		
			
			this.addListener('itemclick', this.onClick, this);
			this.addListener('itemmouseenter', this.onMouseOverX, this);
			this.addListener('itemmouseleave', this.onMouseOutX, this);
			
			this.addEvents('detail');
			this.addEvents('delete');		
			this.addEvents('clone');		
			this.addEvents('showMetadata');		
			this.addEvents('share');		
		}
		
		,initServices : function(){
			var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE'};
			params.operation = 'DOWNLOAD';
			this.services = this.services || new Array();

			this.services['getImageContent'] = this.services['getImageContent'] || Sbi.config.serviceRegistry.getServiceUrl({
				serviceName: 'MANAGE_PREVIEW_FILE_ACTION'
				, baseParams: params
			});
		}
		
		//Build the TPL
		,initTemplate : function() {
			Sbi.debug('MyAnalysisView building the tpl...');
			//var noItem = LN('No documents');
			var changed = LN('sbi.ds.changedon');
			var title = LN('sbi.ds.listTitle');
		
	
			var documentTpl = this.getDocumentTemplate();
			
			var currentUser = this.config.user;

			
			/*
			var noMsg = '';
			
			noMsg += '<tpl if="root.length == 0">'+
			'<div id="empty-group-message">'+
				noItem+
			'</div>'+
			'</tpl>';
			*/
			
			this.tpl = new Ext.XTemplate(
					 '<div id="list-container" class="main-datasets-list">',
					 	'<dl>',
			            '<tpl for=".">',
			            	//'<dl>',
			            		//noMsg,		            		
				                //'<tpl for="samples">',   
				                	'{[engine=""]}',
				                	'{[summary=""]}',
				                	'{[views=""]}',
				                	'{[previewFile=""]}',				                	
				                        // -- DOCUMENT -----------------------------------------------
				                        '<tpl if="this.exists(engine) == true">',
					                       	'<dd id="{label}" class="box">', //document
					                        	documentTpl,
												'<tpl if="creationUser == \''+currentUser+'\' && \''+currentUser+'\' != \''+this.PUBLIC_USER+'\'">'+
													'<div class="fav-container" >',
										            '<tpl if="isPublic == false">'+
													'	<div class="share"  title="'+LN('sbi.myanalysis.sharedocument')+'">',
													'    <a href="#"><span class="icon"></span></a> '+
													'	</div>',
										            '</tpl>'+
										            '<tpl if="isPublic == true">'+
													'	<div class="share"  title="'+LN('sbi.myanalysis.unsharedocument')+'">',
													'    <a href="#"><span class="iconActive"></span></a> '+
													'	</div>',
										            '</tpl>'+					            
													'</div>',		
												'</tpl>'+						                        	
				                        '</tpl>',
				                        '</dd>',
				                // '</tpl>', // '<tpl for="samples">',   
				            //'</dl>',
				                        
			            '</tpl>',
		                '<div style="clear:left"></div>',
					 	'</dl>',
			        '</div>', {
				        	exists: function(o){
				        		return typeof o != 'undefined' && o != null && o!='';
				        	}
				        	, isSearchResult: function(o) {
				        		if((typeof o != undefined) && o != null && o!=''){
				        			return true;
				        		}else{
				        			return false;
				        		}
				        		
				        	}
					 }
			);
			
			Sbi.debug('MyAnalysisView tpl built.');

			
			return this.tpl;


		}
		
		  // private methods 
		
		,getDocumentTemplate : function(){
			var img = Ext.BLANK_IMAGE_URL ;

			var classImg = ' class="{typeCode}-icon" ';

			
			var author = LN('sbi.generic.author');
			var changed = LN('sbi.ds.changedon');

			var currentUser = this.config.user;
			
			
			var documentTpl = ''+
			'<div class="box-container-myanalysis">'+
				'<div id="document-item-icon" class="box-figure">'+
					'<tpl if="this.isSearchResult(summary) == true">'+
						'<tpl if="this.exists(previewFile) == true">'+
							'<img align="center" class="preview-icon" src="'+this.services['getImageContent']+'&fileName={previewFile}" + ext:qtip="<b>{views}</b><br/>{summary}"></img>' +
						'</tpl>' +
						'<tpl if="this.exists(previewFile) == false">'+
							'<img  align="center" src="' + img + '" '+ classImg+'" + ext:qtip="<b>{views}</b><br/>{summary}"></img>' +
						'</tpl>' +
					'</tpl>'+
					'<tpl if="this.isSearchResult(summary) == false">'+ 
						'<tpl if="this.exists(previewFile) == true">'+
							'<img align="center" class="preview-icon" src="'+this.services['getImageContent']+'&fileName={previewFile}"></img>' +
						'</tpl>' +
						'<tpl if="this.exists(previewFile) == false">'+
							'<img align="center" src="' + img + '" '+ classImg+'" ></img>' +
						'</tpl>' +
					'</tpl>'+	
					'<span class="shadow"></span>'+
					'<div class="hover">'+
//			        	'<div class="box-actions-container">'+
//			            '    <ul class="box-actions">'+	    
//			            '		<tpl for="actions">'+  
//			            ' 			<tpl if="name != \'delete\' && name != \'clone\' ">'+
//				        ' 	       		<li class="{name}"><a href="#" title="{description}"></a></li>'+
//				        '			</tpl>'+
//				        '		</tpl>'+
//			            '    </ul>'+
//			            '</div>'+
			            '<tpl for="actions">'+   //TO OPTIMIZE WITHOUT CICLE ON ACTIONS!!!!
			            '	<tpl if="name != \'delete\' && name != \'clone\'">'+
			            '		<a href="#" class="{name}" title="{description}">{name}</a>'+
			            '	</tpl>' +
			            '	<tpl if="name == \'delete\'">'+
			            '		<a href="#" class="delete" title="{description}">Cancella</a>'+
			            '	</tpl>' +
			            '	<tpl if="name == \'clone\'">'+
			            '		<a href="#" class="clone" title="{description}">Clone</a>'+
			            '	</tpl>' +
			            '</tpl>' +
			        '</div>'+ //hover
				'</div>'+ //box-figure
				'<div title="{name}" class="box-text">'+
					'<h2>{name}</h2>'+
//					'<p>{[Ext.String.ellipsis(values.description, 100, false)]}</p>'+
					'<p>{description}</p>'+
					'<p><b>'+author+':</b> {creationUser}</p>'+
					'<p class="modified">'+changed+' {creationDate}</p>'+
				'</div>'+
			'</div>';
			
			return documentTpl;
		}
		
		
		,onRender : function(obj, opt) {
			Ext.DataView.superclass.onRender.call(this, opt);
		}
		
		, 
		onClick : function(obj, record, item, index, e, eOpts) {
			var scope = this;
			
			var actionDetail = e.getTarget('a[class=detail]', 10, true);
	    	var actionMetaData = e.getTarget('a[class=showmetadata]', 10, true);
	        var actionDelete = e.getTarget('a[class=delete]', 10, true);
	        var actionClone = e.getTarget('a[class=clone]', 10, true);
	        var actionShare = e.getTarget('div[class=share]',10,true)
	        var actionExecution = e.getTarget('div[class=box-container-myanalysis]',10,true)

	        //var actionFavourite = e.getTarget('span.icon', 10, true); //TBD

	        delete record.data.actions; 

	    	if (actionDetail != null){
		     	Sbi.debug('MyAnalysisView view detail ');        
	        	scope.fireEvent('detail', record.data);   
			 }else if (actionDelete != null){
		     	Sbi.debug('MyAnalysisView delete');   
	        	scope.fireEvent('delete', record.data);
			 }else if (actionClone != null){
		     	Sbi.debug('MyAnalysisView clone');       
	        	scope.fireEvent('clone', record.data);
			 }else if (actionMetaData != null){
		     	Sbi.debug('MyAnalysisView showMetadata');        
	        	scope.fireEvent('showMetadata', record.data);
			 }else if (actionShare != null){
			     Sbi.debug('MyAnalysisView share');        
		         scope.fireEvent('share', record.data);
			 } else if (actionExecution != null) {
				 //Execution of document
				 if (record.data.engine){
				     Sbi.debug('MyAnalysisView raise Document execution event');        			     
				     if ((record.data.typeCode == 'MAP') && (record.data.engine == 'Gis Engine')){
					   	scope.fireEvent('executeDocument','GEOREPORT','DOCUMENT',record);
				     } else if (record.data.typeCode == 'WORKSHEET'){
				   		scope.fireEvent('executeDocument','WORKSHEET','DOCUMENT',record);
				     } else if (record.data.typeCode == 'DOCUMENT_COMPOSITE'){
				    	 scope.fireEvent('executeDocument','COCKPIT','DOCUMENT',record);
				     }

				 }

			 }
	    	/*
			 else if (actionFavourite != null){
		     	//temporary until favourites aren't managed:
		     	return;
			 }
			 */
	    	
	    	return true;

		}
		
		,
		onMouseOverX : function( obj, record, item, index, e, eOpts ) {
			
		}
		
		,
		onMouseOutX : function( obj, record, item, index, e, eOpts ) {
			
		}
		
});