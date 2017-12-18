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
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */


Ext.ns("Sbi.browser");

Sbi.browser.FolderViewTemplate = function(config) { 

	var documentAttributes = '';
	var attributeNameView = '';
	var img = Ext.BLANK_IMAGE_URL ;
	var classImg = ' class="{typeCode}-icon" ';
	
	this.services = this.services || new Array();
	
	var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE'};
	params.operation = 'DOWNLOAD';
	this.services['getImageContent'] = this.services['getImageContent'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_PREVIEW_FILE_ACTION'
		, baseParams: params
	});

	var changed = LN('sbi.ds.changedon');
	var author = LN('sbi.generic.author');
	var viewDetail = LN('sbi.browser.folderdetailpanel.viewDetail');
	var hideDetail = LN('sbi.browser.folderdetailpanel.hideDetail');
	
	var documentTpl = ''+
	'<div class="box-container-browser-synthesis">'+
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
		
	// Synthesis case
		'<div title="{name} - {description}" class="box-text synthesis-box-visible">'+
		// if name is longer than 60 use short text	    
			'<p class="box-text-name">{shortName}</p>'+
			'<a class="viewMore" title="View Detail" style="font-size: 13px;float: right;">'+viewDetail+' >> </a>'+
		'</div>'+

	// Detail case
		'<div title="{name} - {description}" class="box-text detail-box-hidden">'+
		'<p class="box-text-name">{name}</p>'+
			'<p>{description}</p>'+
			'<p><b>'+author+':</b> {creationUser}</p>'+
			'<p class="modifiedDate">'+changed+' {creationDate}</p>'+			
			'<a class="viewLess" title="Hide Detail" style="font-size: 13px;float: right;"> << '+hideDetail+' </a>'+
			'</div>'+
	'</div>';

	
	var folderAttributes = '';	
	var folderTpl = '' + 
	'<div class="box-container">'+
		'<div id="document-item-icon"  class="box-figure">'+
			'<tpl if="this.isHomeFolder(codType) == true">' +
				'<div id="icon" class="folder_home" ></div>' +
		    '</tpl>' +
		    '<tpl if="this.isHomeFolder(codType) == false">' + 
		    	'<div id="icon" class="folder"></div>' + 
			'</tpl>' +
			'<span class="shadow"></span>'+
			'<div class="hover">'+
	            '		<tpl for="actions">'+  
	            '			<a href="#" class="{name}" title="{description}">{description}</a>'+
		        '		</tpl>'+
	        '</div>'+
		'</div>'+ 	
//        folderAttributes +
		'<div title="{name}" class="box-text">'+
			'<h2>{name}</h2>'+
			'<p>{description}</p>'+
	    '</div>'+
	 '</div>';

	var noItem = LN('sbi.browser.folderdetailpanel.emptytext');
	var noMsg = '';
	var groups = '';

	if (!Sbi.config.flatViewModality){
		groups += '<h2><div class="group-header">{titleLabel} ({[values.samples.length]})</div></h2>';
		noMsg += '<tpl if="samples.length == 0">'+
        			'<div id="empty-group-message">'+
        				noItem+
        			'</div>'+
        		 '</tpl>';
	}

	Sbi.browser.FolderViewTemplate.superclass.constructor.call(this, 
			 '<div id="sample-ct" class="main-datasets-list">',
	            '<tpl for=".">',
	            	groups,
	            	'<dl>',
	            		noMsg,
		                '<tpl for="samples">',   
		                	'{[engine=""]}',
		                	'{[summary=""]}',
		                	'{[views=""]}',
		                	'{[previewFile=""]}',
		                        // -- DOCUMENT -----------------------------------------------
		                        '<tpl if="this.exists(engine) == true">',
		                        	'<dd id="{label}" class="box">', //document
		                        	documentTpl,
		                        '</tpl>',
		                        // -- FOLDER -----------------------------------------------
		                        '<tpl if="this.exists(engine) == false">',
		                        	'<dd class="box-folder">', //Folder	
		                        	folderTpl,
		                        '</tpl>',
		                        '</dd>',
		                 '</tpl>', // '<tpl for="samples">',   
		                '<div style="clear:left"></div>',
		            '</dl>',
	            '</tpl>',
	        '</div>', {
	        	exists: function(o){
	        		return typeof o != 'undefined' && o != null && o!='';
	        	}			
	        	, isHomeFolder: function(s) {
	        		return s == 'USER_FUNCT';
	        	}
	        	, isSearchResult: function(o) {
	        		if((typeof o != undefined) && o != null && o!=''){
	        			return true;
	        		}else{
	        			return false;
	        		}
	        		
	        	}
	        	, isAction: function(o) {
	        		if(typeof o != undefined  && o != null && o!='delete'){
	        			return true;
	        		}else{
	        			return false;
	        		}
	        		
	        	}
	        	, getTitle: function(n) {
	        		if(typeof n != undefined  && n != null ){
	        			//normalization for ln function
	        			if (n == 'detail') n = 'details';
	        			else if (n == 'showmetadata') 
	        			return LN('sbi.generic.'+n);
	        		}else{
	        			return '';
	        		}
	        		
	        	}
	        }
	);
}; 
   
    
Ext.extend(Sbi.browser.FolderViewTemplate, Ext.XTemplate, {
    services : null
  
});

