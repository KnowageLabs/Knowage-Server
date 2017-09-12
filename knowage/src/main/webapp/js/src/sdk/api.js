Sbi.sdk.namespace('Sbi.sdk.api');

Sbi.sdk.apply(Sbi.sdk.api, {
	
	elId: 0
	
	/*	
	config = { 
		params: {
			user: 'biuser'
			, password: 'biuser'
		}
		
		, callback: {
			fn: doThis
			, scope: this
			, args: {arg1: 'A', arg2: 'B', ...}
		}
	}
	*/
	
	, authenticate:  function (config) {	    
		var serviceUrl = Sbi.sdk.services.getServiceUrl('authenticate', config.params);
		Sbi.sdk.jsonp.asyncRequest(serviceUrl, config.callback.fn, config.callback.scope, config.callback.args);
    }

	, executeDataSet: function (config) {	    
		var serviceUrl = Sbi.sdk.services.getServiceUrl('executedataset', config.params);
		Sbi.sdk.jsonp.asyncRequest(serviceUrl, config.callback.fn, config.callback.scope, config.callback.args);
    }

	, getDocumentUrl: function( config ) {
		var documentUrl = null;
		
		if(config.documentId === undefined && config.documentLabel === undefined) {
			alert('ERRORE: at least one beetween documentId and documentLabel attributes must be specifyed');
			return null;
		}
		
		var params = Sbi.sdk.apply({}, config.parameters || {});
		
		if(config.documentId !== undefined) params.OBJECT_ID = config.documentId;
		if(config.documentLabel !== undefined) params.OBJECT_LABEL = config.documentLabel;
		
		if (config.executionRole !== undefined) params.ROLE = config.executionRole;
		if (config.displayToolbar !== undefined) params.TOOLBAR_VISIBLE = config.displayToolbar;
		if (config.displaySliders !== undefined) params.SLIDERS_VISIBLE = config.displaySliders;
		if (config.theme !== undefined)	params.theme = config.theme;
		
		//if(config.useExtUI === true) {
		// no more modality different from ext
			documentUrl = Sbi.sdk.services.getServiceUrl('executewithext', params);
//		} else {
//			documentUrl = Sbi.sdk.services.getServiceUrl('execute', params);
//		}
		
		return documentUrl;
	}

	, getDocumentHtml: function( config ) {
		
		var documentHtml;
		var serviceUrl = this.getDocumentUrl( config );
		
		config.iframe = config.iframe || {};
		
		if(config.iframe.id === undefined) {
			config.iframe.id = 'sbi-docexec-iframe-' + this.elId;
			this.elId = this.elId +1;
		}
		
		documentHtml = '';
		documentHtml += '<iframe';
		documentHtml += ' id = "' + config.iframe.id + '" ';
		documentHtml += ' name = "' + config.iframe.id + '" ';
		documentHtml += ' src = "' + serviceUrl + '" ';
		if(config.iframe.style !== undefined) documentHtml += ' style = "' + config.iframe.style + '" ';
		if(config.iframe.width !== undefined) documentHtml += ' width = "' + config.iframe.width + '" ';
		if(config.iframe.height !== undefined) documentHtml += ' height = "' + config.iframe.height + '" ';
		documentHtml += '></iframe>';
		
		return documentHtml;
	}
	
	, injectDocument: function( config ) {
		var targetEl = config.target || document.body;
		
		
		if(typeof targetEl === 'string') {
			var elId = targetEl;
			targetEl = document.getElementById(targetEl);
			
			if(targetEl === null) {
				targetEl = document.createElement('div');
				targetEl.setAttribute('id', elId);
				if(config.width !== undefined) {
					targetEl.setAttribute('width', config.width);
				}				
				if(config.height !== undefined) {
					targetEl.setAttribute('height', config.height);
				}
				document.body.appendChild( targetEl );
			} 
		}
		
		config.iframe = config.iframe || {};
		config.iframe.width = targetEl.getAttribute('width');
		config.iframe.height = targetEl.getAttribute('height');
		
		
		targetEl.innerHTML = this.getDocumentHtml( config );
	}


});