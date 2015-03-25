
Sbi = this.Sbi || {};
Sbi.sdk = {version: '2.2'};


/**
 * @class Sbi.sdk
 * Sbi.sdk core utilities and functions.
 * @singleton
 */

Sbi.sdk.apply = function(o, c, defaults){
    if(defaults){
        // no "this" reference for friendly out of scope calls
        Sbi.sdk.apply(o, defaults);
    }
    if(o && c && typeof c == 'object'){
        for(var p in c){
            o[p] = c[p];
        }
    }
    return o;
};

/**
 * Creates namespaces to be used for scoping variables and classes so that they are not global.  Usage:
 * <pre><code>
 *  Ext.namespace('Company', 'Company.data');
 *  Company.Widget = function() { ... }
 *  Company.data.CustomStore = function(config) { ... }
 * </code></pre>
 *
 * @param {String} namespace1
 * @param {String} namespace2
 * @param {String} etc
 * @method namespace
 */
Sbi.sdk.namespace =  function() {
    var a=arguments, o=null, i, j, d, rt;
    for (i=0; i<a.length; ++i) {
        d=a[i].split(".");
        rt = d[0];
        eval('if (typeof ' + rt + ' == "undefined"){' + rt + ' = {};} o = ' + rt + ';');
        for (j=1; j<d.length; ++j) {
            o[d[j]]=o[d[j]] || {};
            o=o[d[j]];
        }
    }
};

/**
 * Takes an object and converts it to an encoded URL. e.g. Ext.urlEncode({foo: 1, bar: 2}); would return "foo=1&bar=2".  
 * Optionally, property values can be arrays, instead of keys and the resulting string that's returned will contain a name/value pair for each array value.
 *
 * @param {Object} o
 * @return {String}
*/
Sbi.sdk.urlEncode = function(o){
    if(!o){
        return "";
    }
    var buf = [];
    for(var key in o){
        var ov = o[key], k = encodeURIComponent(key);
        var type = typeof ov;
        if(type == 'undefined'){
            buf.push(k, "=&");
        }else if(type != "function" && type != "object"){
            buf.push(k, "=", encodeURIComponent(ov), "&");
        }else if(ov instanceof Array){
            if (ov.length) {
                for(var i = 0, len = ov.length; i < len; i++) {
                    buf.push(k, "=", encodeURIComponent(ov[i] === undefined ? '' : ov[i]), "&");
                }
            } else {
                buf.push(k, "=&");
            }
        }
    }
    buf.pop();
    return buf.join("");
},

/**
  * Takes an encoded URL and and converts it to an object. e.g. Ext.urlDecode("foo=1&bar=2"); would return {foo: 1, bar: 2} or Ext.urlDecode("foo=1&bar=2&bar=3&bar=4", true); would return {foo: 1, bar: [2, 3, 4]}.
  * @param {String} string
  * @param {Boolean} overwrite (optional) Items of the same name will overwrite previous values instead of creating an an array (Defaults to false).
  * @return {Object} A literal with members
  */
Sbi.sdk.urlDecode = function(string, overwrite){
    if(!string || !string.length){
        return {};
    }
    var obj = {};
    var pairs = string.split('&');
    var pair, name, value;
    for(var i = 0, len = pairs.length; i < len; i++){
        pair = pairs[i].split('=');
        name = decodeURIComponent(pair[0]);
        value = decodeURIComponent(pair[1]);
        if(overwrite !== true){
            if(typeof obj[name] == "undefined"){
                obj[name] = value;
            }else if(typeof obj[name] == "string"){
                obj[name] = [obj[name]];
                obj[name].push(value);
            }else{
                obj[name].push(value);
            }
        }else{
            obj[name] = value;
        }
    }
    return obj;
},

Sbi.sdk.apply(Function.prototype, {
    
	/**
     * Creates a delegate (callback) that sets the scope to obj.
     * Call directly on any function. Example: <code>this.myFunction.createDelegate(this)</code>
     * Will create a function that is automatically scoped to this.
     * @param {Object} obj (optional) The object for which the scope is set
     * @param {Array} args (optional) Overrides arguments for the call. (Defaults to the arguments passed by the caller)
     * @param {Boolean/Number} appendArgs (optional) if True args are appended to call args instead of overriding,
     *                                             if a number the args are inserted at the specified position
     * @return {Function} The new function
     */
    createDelegate : function(obj, args, appendArgs){
        var method = this;
        return function() {
            var callArgs = args || arguments;
            if(appendArgs === true){
                callArgs = Array.prototype.slice.call(arguments, 0);
                callArgs = callArgs.concat(args);
            }else if(typeof appendArgs == "number"){
                callArgs = Array.prototype.slice.call(arguments, 0); // copy arguments first
                var applyArgs = [appendArgs, 0].concat(args); // create method call params
                Array.prototype.splice.apply(callArgs, applyArgs); // splice them in
            }
            return method.apply(obj || window, callArgs);
        };
    },

    
   /**
    * Calls this function after the number of millseconds specified.
    * @param {Number} millis The number of milliseconds for the setTimeout call (if 0 the function is executed immediately)
    * @param {Object} obj (optional) The object for which the scope is set
    * @param {Array} args (optional) Overrides arguments for the call. (Defaults to the arguments passed by the caller)
    * @param {Boolean/Number} appendArgs (optional) if True args are appended to call args instead of overriding,
    *                                             if a number the args are inserted at the specified position
    * @return {Number} The timeout id that can be used with clearTimeout
    */
   defer : function(millis, obj, args, appendArgs){
       var fn = this.createDelegate(obj, args, appendArgs);
       if(millis){
           return setTimeout(fn, millis);
       }
       fn();
       return 0;
   }
})
Sbi.sdk.namespace('Sbi.sdk.ajax');


Sbi.sdk.apply(Sbi.sdk.ajax, {
        
		request : function(method, uri, cb, data, options) {
            if(options){
                var hs = options.headers;
                if(hs){
                    for(var h in hs){
                        if(hs.hasOwnProperty(h)){
                            this.initHeader(h, hs[h], false);
                        }
                    }
                }
                if(options.xmlData){
                    this.initHeader('Content-Type', 'text/xml', false);
                    method = 'POST';
                    data = options.xmlData;
                }else if(options.jsonData){
                    this.initHeader('Content-Type', 'text/javascript', false);
                    method = 'POST';
                    data = typeof options.jsonData == 'object' ? Ext.encode(options.jsonData) : options.jsonData;
                }
            }

            return this.asyncRequest(method, uri, cb, data);
        },

        serializeForm : function(form) {
            if(typeof form == 'string') {
                form = (document.getElementById(form) || document.forms[form]);
            }

            var el, name, val, disabled, data = '', hasSubmit = false;
            for (var i = 0; i < form.elements.length; i++) {
                el = form.elements[i];
                disabled = form.elements[i].disabled;
                name = form.elements[i].name;
                val = form.elements[i].value;

                if (!disabled && name){
                    switch (el.type)
                            {
                        case 'select-one':
                        case 'select-multiple':
                            for (var j = 0; j < el.options.length; j++) {
                                if (el.options[j].selected) {
                                    if (Ext.isIE) {
                                        data += encodeURIComponent(name) + '=' + encodeURIComponent(el.options[j].attributes['value'].specified ? el.options[j].value : el.options[j].text) + '&';
                                    }
                                    else {
                                        data += encodeURIComponent(name) + '=' + encodeURIComponent(el.options[j].hasAttribute('value') ? el.options[j].value : el.options[j].text) + '&';
                                    }
                                }
                            }
                            break;
                        case 'radio':
                        case 'checkbox':
                            if (el.checked) {
                                data += encodeURIComponent(name) + '=' + encodeURIComponent(val) + '&';
                            }
                            break;
                        case 'file':

                        case undefined:

                        case 'reset':

                        case 'button':

                            break;
                        case 'submit':
                            if(hasSubmit == false) {
                                data += encodeURIComponent(name) + '=' + encodeURIComponent(val) + '&';
                                hasSubmit = true;
                            }
                            break;
                        default:
                            data += encodeURIComponent(name) + '=' + encodeURIComponent(val) + '&';
                            break;
                    }
                }
            }
            data = data.substr(0, data.length - 1);
            return data;
        },

        headers:{},

        hasHeaders:false,

        useDefaultHeader:true,

        defaultPostHeader:'application/x-www-form-urlencoded',

        useDefaultXhrHeader:true,

        defaultXhrHeader:'XMLHttpRequest',

        hasDefaultHeaders:true,

        defaultHeaders:{},

        poll:{},

        timeout:{},

        pollInterval:50,

        transactionId:0,

        setProgId:function(id)
        {
            this.activeX.unshift(id);
        },

        setDefaultPostHeader:function(b)
        {
            this.useDefaultHeader = b;
        },

        setDefaultXhrHeader:function(b)
        {
            this.useDefaultXhrHeader = b;
        },

        setPollingInterval:function(i)
        {
            if (typeof i == 'number' && isFinite(i)) {
                this.pollInterval = i;
            }
        },

        createXhrObject:function(transactionId)
        {
            var obj,http;
            try
            {

                http = new XMLHttpRequest();

                obj = { conn:http, tId:transactionId };
            }
            catch(e)
            {
                for (var i = 0; i < this.activeX.length; ++i) {
                    try
                    {

                        http = new ActiveXObject(this.activeX[i]);

                        obj = { conn:http, tId:transactionId };
                        break;
                    }
                    catch(e) {
                    }
                }
            }
            finally
            {
                return obj;
            }
        },

        getConnectionObject:function()
        {
            var o;
            var tId = this.transactionId;

            try
            {
                o = this.createXhrObject(tId);
                if (o) {
                    this.transactionId++;
                }
            }
            catch(e) {
            }
            finally
            {
                return o;
            }
        },

        asyncRequest:function(method, uri, callback, postData)
        {
            var o = this.getConnectionObject();

            if (!o) {
                return null;
            }
            else {
                o.conn.open(method, uri, true);

                if (this.useDefaultXhrHeader) {
                    if (!this.defaultHeaders['X-Requested-With']) {
                        this.initHeader('X-Requested-With', this.defaultXhrHeader, true);
                    }
                }

                if(postData && this.useDefaultHeader){
                    this.initHeader('Content-Type', this.defaultPostHeader);
                }

                 if (this.hasDefaultHeaders || this.hasHeaders) {
                    this.setHeader(o);
                }

                this.handleReadyState(o, callback);
                o.conn.send(postData || null);

                return o;
            }
        },

        handleReadyState:function(o, callback)
        {
            var oConn = this;

            if (callback && callback.timeout) {
                this.timeout[o.tId] = window.setTimeout(function() {
                    oConn.abort(o, callback, true);
                }, callback.timeout);
            }

            this.poll[o.tId] = window.setInterval(
                    function() {
                        if (o.conn && o.conn.readyState == 4) {
                            window.clearInterval(oConn.poll[o.tId]);
                            delete oConn.poll[o.tId];

                            if (callback && callback.timeout) {
                                window.clearTimeout(oConn.timeout[o.tId]);
                                delete oConn.timeout[o.tId];
                            }

                            oConn.handleTransactionResponse(o, callback);
                        }
                    }
                    , this.pollInterval);
        },

        handleTransactionResponse:function(o, callback, isAbort)
        {

            if (!callback) {
                this.releaseObject(o);
                return;
            }

            var httpStatus, responseObject;

            try
            {
                if (o.conn.status !== undefined && o.conn.status != 0) {
                    httpStatus = o.conn.status;
                }
                else {
                    httpStatus = 13030;
                }
            }
            catch(e) {


                httpStatus = 13030;
            }

            if (httpStatus >= 200 && httpStatus < 300) {
                responseObject = this.createResponseObject(o, callback.argument);
                if (callback.success) {
                    if (!callback.scope) {
                        callback.success(responseObject);
                    }
                    else {


                        callback.success.apply(callback.scope, [responseObject]);
                    }
                }
            }
            else {
                switch (httpStatus) {

                    case 12002:
                    case 12029:
                    case 12030:
                    case 12031:
                    case 12152:
                    case 13030:
                        responseObject = this.createExceptionObject(o.tId, callback.argument, (isAbort ? isAbort : false));
                        if (callback.failure) {
                            if (!callback.scope) {
                                callback.failure(responseObject);
                            }
                            else {
                                callback.failure.apply(callback.scope, [responseObject]);
                            }
                        }
                        break;
                    default:
                        responseObject = this.createResponseObject(o, callback.argument);
                        if (callback.failure) {
                            if (!callback.scope) {
                                callback.failure(responseObject);
                            }
                            else {
                                callback.failure.apply(callback.scope, [responseObject]);
                            }
                        }
                }
            }

            this.releaseObject(o);
            responseObject = null;
        },

        createResponseObject:function(o, callbackArg)
        {
            var obj = {};
            var headerObj = {};

            try
            {
                var headerStr = o.conn.getAllResponseHeaders();
                var header = headerStr.split('\n');
                for (var i = 0; i < header.length; i++) {
                    var delimitPos = header[i].indexOf(':');
                    if (delimitPos != -1) {
                        headerObj[header[i].substring(0, delimitPos)] = header[i].substring(delimitPos + 2);
                    }
                }
            }
            catch(e) {
            }

            obj.tId = o.tId;
            obj.status = o.conn.status;
            obj.statusText = o.conn.statusText;
            obj.getResponseHeader = headerObj;
            obj.getAllResponseHeaders = headerStr;
            obj.responseText = o.conn.responseText;
            obj.responseXML = o.conn.responseXML;

            if (typeof callbackArg !== undefined) {
                obj.argument = callbackArg;
            }

            return obj;
        },

        createExceptionObject:function(tId, callbackArg, isAbort)
        {
            var COMM_CODE = 0;
            var COMM_ERROR = 'communication failure';
            var ABORT_CODE = -1;
            var ABORT_ERROR = 'transaction aborted';

            var obj = {};

            obj.tId = tId;
            if (isAbort) {
                obj.status = ABORT_CODE;
                obj.statusText = ABORT_ERROR;
            }
            else {
                obj.status = COMM_CODE;
                obj.statusText = COMM_ERROR;
            }

            if (callbackArg) {
                obj.argument = callbackArg;
            }

            return obj;
        },

        initHeader:function(label, value, isDefault)
        {
            var headerObj = (isDefault) ? this.defaultHeaders : this.headers;

            if (headerObj[label] === undefined) {
                headerObj[label] = value;
            }
            else {


                headerObj[label] = value + "," + headerObj[label];
            }

            if (isDefault) {
                this.hasDefaultHeaders = true;
            }
            else {
                this.hasHeaders = true;
            }
        },


        setHeader:function(o)
        {
            if (this.hasDefaultHeaders) {
                for (var prop in this.defaultHeaders) {
                    if (this.defaultHeaders.hasOwnProperty(prop)) {
                        o.conn.setRequestHeader(prop, this.defaultHeaders[prop]);
                    }
                }
            }

            if (this.hasHeaders) {
                for (var prop in this.headers) {
                    if (this.headers.hasOwnProperty(prop)) {
                        o.conn.setRequestHeader(prop, this.headers[prop]);
                    }
                }
                this.headers = {};
                this.hasHeaders = false;
            }
        },

        resetDefaultHeaders:function() {
            delete this.defaultHeaders;
            this.defaultHeaders = {};
            this.hasDefaultHeaders = false;
        },

        abort:function(o, callback, isTimeout)
        {
            if (this.isCallInProgress(o)) {
                o.conn.abort();
                window.clearInterval(this.poll[o.tId]);
                delete this.poll[o.tId];
                if (isTimeout) {
                    delete this.timeout[o.tId];
                }

                this.handleTransactionResponse(o, callback, true);

                return true;
            }
            else {
                return false;
            }
        },


        isCallInProgress:function(o)
        {


            if (o.conn) {
                return o.conn.readyState != 4 && o.conn.readyState != 0;
            }
            else {

                return false;
            }
        },


        releaseObject:function(o)
        {

            o.conn = null;

            o = null;
        },

        activeX:[
        'MSXML2.XMLHTTP.3.0',
        'MSXML2.XMLHTTP',
        'Microsoft.XMLHTTP'
        ]


    });Sbi.sdk.namespace('Sbi.sdk.jsonp');


Sbi.sdk.apply(Sbi.sdk.jsonp, {
	
	timeout : 30000,
    callbackParam : "callback",
    nocache : true,
    trans_id: 0,
    
    asyncRequest : function(uri, callback, scope, arg){
        
		if(this.head === undefined) {
			this.head = document.getElementsByTagName("head")[0];
		}
	
        if(this.nocache){
        	uri += (uri.indexOf("?") != -1 ? "&" : "?") + "_dc=" + (new Date().getTime());
        }
        
        var transId = ++this.trans_id;
        
        var trans = {
            id : transId,
            cb : "stcCallback"+transId,
            scriptId : "stcScript"+transId,
            arg : arg,
            url : uri,
            callback : callback,
            scope : scope
        };
        
        var conn = this;

        window[trans.cb] = function(o){
            conn.handleResponse(o, trans);
        };

        uri += (uri.indexOf("?") != -1 ? "&" : "?") + this.callbackParam + "=" + trans.cb;
        
        if(this.autoAbort !== false){
            this.abort();
        }

        trans.timeoutId = this.handleFailure.defer(this.timeout, this, [trans]);

        var script = document.createElement("script");
        script.setAttribute("src", uri);
        script.setAttribute("type", "text/javascript");
        script.setAttribute("id", trans.scriptId);
        this.head.appendChild(script);

        this.trans = trans;    
	},
	
	 // private
    isLoading : function(){
        return this.trans ? true : false;
    },

    /**
     * Abort the current server request.
     */
    abort : function(){
        if(this.isLoading()){
            this.destroyTrans(this.trans);
        }
    },

    // private
    destroyTrans : function(trans, isLoaded){
        this.head.removeChild(document.getElementById(trans.scriptId));
        clearTimeout(trans.timeoutId);
        if(isLoaded){
            window[trans.cb] = undefined;
            try{
                delete window[trans.cb];
            }catch(e){}
        }else{
            // if hasn't been loaded, wait for load to remove it to prevent script error
            window[trans.cb] = function(){
                window[trans.cb] = undefined;
                try{
                    delete window[trans.cb];
                }catch(e){}
            };
        }
    },

    // private
    handleResponse : function(o, trans){
        this.trans = false;
        this.destroyTrans(trans, true);
        var result = o;
        trans.callback.call(trans.scope||window, result, trans.arg, true);
        
    },

    // private
    handleFailure : function(trans){
        this.trans = false;
        this.destroyTrans(trans, false);
        trans.callback.call(trans.scope||window, null, trans.arg, false);
    }

});
Sbi.sdk.namespace('Sbi.sdk.services');


Sbi.sdk.apply(Sbi.sdk.services, {

    services: null
    
    , baseUrl:  {
		protocol: 'http'     
		, host: 'localhost'
	    , port: '8080'
	    , contextPath: 'SpagoBI'
	    , controllerPath: 'servlet/AdapterHTTP'    
	}
    
    , initServices: function() {
        this.services = {};
        this.services.authenticate = {
            type: 'ACTION', 
            name: 'LOGIN_ACTION_WEB', 
            baseParams: {NEW_SESSION: 'TRUE'}
        };
        this.services.execute = {
            type: 'PAGE', 
            name: 'ExecuteBIObjectPage', 
            baseParams: {NEW_SESSION: 'TRUE', MODALITY: 'SINGLE_OBJECT_EXECUTION_MODALITY', IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS: 'true'}
        };
        
        this.services.executewithext = {
            type: 'ACTION', 
            name: 'EXECUTE_DOCUMENT_ACTION', 
            baseParams: {NEW_SESSION: 'TRUE', IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS: 'true'}
        };
    }
    
    , setBaseUrl: function(u) {
        Sbi.sdk.apply(this.baseUrl, u || {});
    }
    
    , getServiceUrl: function(serviceName, p) {
        var urlStr = null;
        
        if(this.services === null) {
            this.initServices();
        }
        
        if(this.services[serviceName] === undefined) {
            alert('ERROR: Service [' + + '] does not exist');
        } else {
            urlStr = '';
            urlStr = this.baseUrl.protocol + '://' + this.baseUrl.host + ":" + this.baseUrl.port + '/' + this.baseUrl.contextPath + '/' + this.baseUrl.controllerPath;
            var params;
            if(this.services[serviceName].type === 'PAGE'){
            	params = {PAGE: this.services[serviceName].name};
            } else {
            	params = {ACTION_NAME: this.services[serviceName].name};            	
            }
            
            Sbi.sdk.apply(params, p || {}, this.services[serviceName].baseParams || {});
            var paramsStr = Sbi.sdk.urlEncode(params);
            urlStr += '?' + paramsStr;
        }
        
        return urlStr;
    }
});

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
		
		if(config.useExtUI === true) {
			documentUrl = Sbi.sdk.services.getServiceUrl('executewithext', params);
		} else {
			documentUrl = Sbi.sdk.services.getServiceUrl('execute', params);
		}
		
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
Sbi = this.Sbi || {};
Sbi.sdk = {version: '2.2'};


/**
 * @class Sbi.sdk
 * Sbi.sdk core utilities and functions.
 * @singleton
 */

Sbi.sdk.apply = function(o, c, defaults){
    if(defaults){
        // no "this" reference for friendly out of scope calls
        Sbi.sdk.apply(o, defaults);
    }
    if(o && c && typeof c == 'object'){
        for(var p in c){
            o[p] = c[p];
        }
    }
    return o;
};

/**
 * Creates namespaces to be used for scoping variables and classes so that they are not global.  Usage:
 * <pre><code>
 *  Ext.namespace('Company', 'Company.data');
 *  Company.Widget = function() { ... }
 *  Company.data.CustomStore = function(config) { ... }
 * </code></pre>
 *
 * @param {String} namespace1
 * @param {String} namespace2
 * @param {String} etc
 * @method namespace
 */
Sbi.sdk.namespace =  function() {
    var a=arguments, o=null, i, j, d, rt;
    for (i=0; i<a.length; ++i) {
        d=a[i].split(".");
        rt = d[0];
        eval('if (typeof ' + rt + ' == "undefined"){' + rt + ' = {};} o = ' + rt + ';');
        for (j=1; j<d.length; ++j) {
            o[d[j]]=o[d[j]] || {};
            o=o[d[j]];
        }
    }
};

/**
 * Takes an object and converts it to an encoded URL. e.g. Ext.urlEncode({foo: 1, bar: 2}); would return "foo=1&bar=2".  
 * Optionally, property values can be arrays, instead of keys and the resulting string that's returned will contain a name/value pair for each array value.
 *
 * @param {Object} o
 * @return {String}
*/
Sbi.sdk.urlEncode = function(o){
    if(!o){
        return "";
    }
    var buf = [];
    for(var key in o){
        var ov = o[key], k = encodeURIComponent(key);
        var type = typeof ov;
        if(type == 'undefined'){
            buf.push(k, "=&");
        }else if(type != "function" && type != "object"){
            buf.push(k, "=", encodeURIComponent(ov), "&");
        }else if(ov instanceof Array){
            if (ov.length) {
                for(var i = 0, len = ov.length; i < len; i++) {
                    buf.push(k, "=", encodeURIComponent(ov[i] === undefined ? '' : ov[i]), "&");
                }
            } else {
                buf.push(k, "=&");
            }
        }
    }
    buf.pop();
    return buf.join("");
},

/**
  * Takes an encoded URL and and converts it to an object. e.g. Ext.urlDecode("foo=1&bar=2"); would return {foo: 1, bar: 2} or Ext.urlDecode("foo=1&bar=2&bar=3&bar=4", true); would return {foo: 1, bar: [2, 3, 4]}.
  * @param {String} string
  * @param {Boolean} overwrite (optional) Items of the same name will overwrite previous values instead of creating an an array (Defaults to false).
  * @return {Object} A literal with members
  */
Sbi.sdk.urlDecode = function(string, overwrite){
    if(!string || !string.length){
        return {};
    }
    var obj = {};
    var pairs = string.split('&');
    var pair, name, value;
    for(var i = 0, len = pairs.length; i < len; i++){
        pair = pairs[i].split('=');
        name = decodeURIComponent(pair[0]);
        value = decodeURIComponent(pair[1]);
        if(overwrite !== true){
            if(typeof obj[name] == "undefined"){
                obj[name] = value;
            }else if(typeof obj[name] == "string"){
                obj[name] = [obj[name]];
                obj[name].push(value);
            }else{
                obj[name].push(value);
            }
        }else{
            obj[name] = value;
        }
    }
    return obj;
},

Sbi.sdk.apply(Function.prototype, {
    
	/**
     * Creates a delegate (callback) that sets the scope to obj.
     * Call directly on any function. Example: <code>this.myFunction.createDelegate(this)</code>
     * Will create a function that is automatically scoped to this.
     * @param {Object} obj (optional) The object for which the scope is set
     * @param {Array} args (optional) Overrides arguments for the call. (Defaults to the arguments passed by the caller)
     * @param {Boolean/Number} appendArgs (optional) if True args are appended to call args instead of overriding,
     *                                             if a number the args are inserted at the specified position
     * @return {Function} The new function
     */
    createDelegate : function(obj, args, appendArgs){
        var method = this;
        return function() {
            var callArgs = args || arguments;
            if(appendArgs === true){
                callArgs = Array.prototype.slice.call(arguments, 0);
                callArgs = callArgs.concat(args);
            }else if(typeof appendArgs == "number"){
                callArgs = Array.prototype.slice.call(arguments, 0); // copy arguments first
                var applyArgs = [appendArgs, 0].concat(args); // create method call params
                Array.prototype.splice.apply(callArgs, applyArgs); // splice them in
            }
            return method.apply(obj || window, callArgs);
        };
    },

    
   /**
    * Calls this function after the number of millseconds specified.
    * @param {Number} millis The number of milliseconds for the setTimeout call (if 0 the function is executed immediately)
    * @param {Object} obj (optional) The object for which the scope is set
    * @param {Array} args (optional) Overrides arguments for the call. (Defaults to the arguments passed by the caller)
    * @param {Boolean/Number} appendArgs (optional) if True args are appended to call args instead of overriding,
    *                                             if a number the args are inserted at the specified position
    * @return {Number} The timeout id that can be used with clearTimeout
    */
   defer : function(millis, obj, args, appendArgs){
       var fn = this.createDelegate(obj, args, appendArgs);
       if(millis){
           return setTimeout(fn, millis);
       }
       fn();
       return 0;
   }
})
Sbi.sdk.namespace('Sbi.sdk.ajax');


Sbi.sdk.apply(Sbi.sdk.ajax, {
        
		request : function(method, uri, cb, data, options) {
            if(options){
                var hs = options.headers;
                if(hs){
                    for(var h in hs){
                        if(hs.hasOwnProperty(h)){
                            this.initHeader(h, hs[h], false);
                        }
                    }
                }
                if(options.xmlData){
                    this.initHeader('Content-Type', 'text/xml', false);
                    method = 'POST';
                    data = options.xmlData;
                }else if(options.jsonData){
                    this.initHeader('Content-Type', 'text/javascript', false);
                    method = 'POST';
                    data = typeof options.jsonData == 'object' ? Ext.encode(options.jsonData) : options.jsonData;
                }
            }

            return this.asyncRequest(method, uri, cb, data);
        },

        serializeForm : function(form) {
            if(typeof form == 'string') {
                form = (document.getElementById(form) || document.forms[form]);
            }

            var el, name, val, disabled, data = '', hasSubmit = false;
            for (var i = 0; i < form.elements.length; i++) {
                el = form.elements[i];
                disabled = form.elements[i].disabled;
                name = form.elements[i].name;
                val = form.elements[i].value;

                if (!disabled && name){
                    switch (el.type)
                            {
                        case 'select-one':
                        case 'select-multiple':
                            for (var j = 0; j < el.options.length; j++) {
                                if (el.options[j].selected) {
                                    if (Ext.isIE) {
                                        data += encodeURIComponent(name) + '=' + encodeURIComponent(el.options[j].attributes['value'].specified ? el.options[j].value : el.options[j].text) + '&';
                                    }
                                    else {
                                        data += encodeURIComponent(name) + '=' + encodeURIComponent(el.options[j].hasAttribute('value') ? el.options[j].value : el.options[j].text) + '&';
                                    }
                                }
                            }
                            break;
                        case 'radio':
                        case 'checkbox':
                            if (el.checked) {
                                data += encodeURIComponent(name) + '=' + encodeURIComponent(val) + '&';
                            }
                            break;
                        case 'file':

                        case undefined:

                        case 'reset':

                        case 'button':

                            break;
                        case 'submit':
                            if(hasSubmit == false) {
                                data += encodeURIComponent(name) + '=' + encodeURIComponent(val) + '&';
                                hasSubmit = true;
                            }
                            break;
                        default:
                            data += encodeURIComponent(name) + '=' + encodeURIComponent(val) + '&';
                            break;
                    }
                }
            }
            data = data.substr(0, data.length - 1);
            return data;
        },

        headers:{},

        hasHeaders:false,

        useDefaultHeader:true,

        defaultPostHeader:'application/x-www-form-urlencoded',

        useDefaultXhrHeader:true,

        defaultXhrHeader:'XMLHttpRequest',

        hasDefaultHeaders:true,

        defaultHeaders:{},

        poll:{},

        timeout:{},

        pollInterval:50,

        transactionId:0,

        setProgId:function(id)
        {
            this.activeX.unshift(id);
        },

        setDefaultPostHeader:function(b)
        {
            this.useDefaultHeader = b;
        },

        setDefaultXhrHeader:function(b)
        {
            this.useDefaultXhrHeader = b;
        },

        setPollingInterval:function(i)
        {
            if (typeof i == 'number' && isFinite(i)) {
                this.pollInterval = i;
            }
        },

        createXhrObject:function(transactionId)
        {
            var obj,http;
            try
            {

                http = new XMLHttpRequest();

                obj = { conn:http, tId:transactionId };
            }
            catch(e)
            {
                for (var i = 0; i < this.activeX.length; ++i) {
                    try
                    {

                        http = new ActiveXObject(this.activeX[i]);

                        obj = { conn:http, tId:transactionId };
                        break;
                    }
                    catch(e) {
                    }
                }
            }
            finally
            {
                return obj;
            }
        },

        getConnectionObject:function()
        {
            var o;
            var tId = this.transactionId;

            try
            {
                o = this.createXhrObject(tId);
                if (o) {
                    this.transactionId++;
                }
            }
            catch(e) {
            }
            finally
            {
                return o;
            }
        },

        asyncRequest:function(method, uri, callback, postData)
        {
            var o = this.getConnectionObject();

            if (!o) {
                return null;
            }
            else {
                o.conn.open(method, uri, true);

                if (this.useDefaultXhrHeader) {
                    if (!this.defaultHeaders['X-Requested-With']) {
                        this.initHeader('X-Requested-With', this.defaultXhrHeader, true);
                    }
                }

                if(postData && this.useDefaultHeader){
                    this.initHeader('Content-Type', this.defaultPostHeader);
                }

                 if (this.hasDefaultHeaders || this.hasHeaders) {
                    this.setHeader(o);
                }

                this.handleReadyState(o, callback);
                o.conn.send(postData || null);

                return o;
            }
        },

        handleReadyState:function(o, callback)
        {
            var oConn = this;

            if (callback && callback.timeout) {
                this.timeout[o.tId] = window.setTimeout(function() {
                    oConn.abort(o, callback, true);
                }, callback.timeout);
            }

            this.poll[o.tId] = window.setInterval(
                    function() {
                        if (o.conn && o.conn.readyState == 4) {
                            window.clearInterval(oConn.poll[o.tId]);
                            delete oConn.poll[o.tId];

                            if (callback && callback.timeout) {
                                window.clearTimeout(oConn.timeout[o.tId]);
                                delete oConn.timeout[o.tId];
                            }

                            oConn.handleTransactionResponse(o, callback);
                        }
                    }
                    , this.pollInterval);
        },

        handleTransactionResponse:function(o, callback, isAbort)
        {

            if (!callback) {
                this.releaseObject(o);
                return;
            }

            var httpStatus, responseObject;

            try
            {
                if (o.conn.status !== undefined && o.conn.status != 0) {
                    httpStatus = o.conn.status;
                }
                else {
                    httpStatus = 13030;
                }
            }
            catch(e) {


                httpStatus = 13030;
            }

            if (httpStatus >= 200 && httpStatus < 300) {
                responseObject = this.createResponseObject(o, callback.argument);
                if (callback.success) {
                    if (!callback.scope) {
                        callback.success(responseObject);
                    }
                    else {


                        callback.success.apply(callback.scope, [responseObject]);
                    }
                }
            }
            else {
                switch (httpStatus) {

                    case 12002:
                    case 12029:
                    case 12030:
                    case 12031:
                    case 12152:
                    case 13030:
                        responseObject = this.createExceptionObject(o.tId, callback.argument, (isAbort ? isAbort : false));
                        if (callback.failure) {
                            if (!callback.scope) {
                                callback.failure(responseObject);
                            }
                            else {
                                callback.failure.apply(callback.scope, [responseObject]);
                            }
                        }
                        break;
                    default:
                        responseObject = this.createResponseObject(o, callback.argument);
                        if (callback.failure) {
                            if (!callback.scope) {
                                callback.failure(responseObject);
                            }
                            else {
                                callback.failure.apply(callback.scope, [responseObject]);
                            }
                        }
                }
            }

            this.releaseObject(o);
            responseObject = null;
        },

        createResponseObject:function(o, callbackArg)
        {
            var obj = {};
            var headerObj = {};

            try
            {
                var headerStr = o.conn.getAllResponseHeaders();
                var header = headerStr.split('\n');
                for (var i = 0; i < header.length; i++) {
                    var delimitPos = header[i].indexOf(':');
                    if (delimitPos != -1) {
                        headerObj[header[i].substring(0, delimitPos)] = header[i].substring(delimitPos + 2);
                    }
                }
            }
            catch(e) {
            }

            obj.tId = o.tId;
            obj.status = o.conn.status;
            obj.statusText = o.conn.statusText;
            obj.getResponseHeader = headerObj;
            obj.getAllResponseHeaders = headerStr;
            obj.responseText = o.conn.responseText;
            obj.responseXML = o.conn.responseXML;

            if (typeof callbackArg !== undefined) {
                obj.argument = callbackArg;
            }

            return obj;
        },

        createExceptionObject:function(tId, callbackArg, isAbort)
        {
            var COMM_CODE = 0;
            var COMM_ERROR = 'communication failure';
            var ABORT_CODE = -1;
            var ABORT_ERROR = 'transaction aborted';

            var obj = {};

            obj.tId = tId;
            if (isAbort) {
                obj.status = ABORT_CODE;
                obj.statusText = ABORT_ERROR;
            }
            else {
                obj.status = COMM_CODE;
                obj.statusText = COMM_ERROR;
            }

            if (callbackArg) {
                obj.argument = callbackArg;
            }

            return obj;
        },

        initHeader:function(label, value, isDefault)
        {
            var headerObj = (isDefault) ? this.defaultHeaders : this.headers;

            if (headerObj[label] === undefined) {
                headerObj[label] = value;
            }
            else {


                headerObj[label] = value + "," + headerObj[label];
            }

            if (isDefault) {
                this.hasDefaultHeaders = true;
            }
            else {
                this.hasHeaders = true;
            }
        },


        setHeader:function(o)
        {
            if (this.hasDefaultHeaders) {
                for (var prop in this.defaultHeaders) {
                    if (this.defaultHeaders.hasOwnProperty(prop)) {
                        o.conn.setRequestHeader(prop, this.defaultHeaders[prop]);
                    }
                }
            }

            if (this.hasHeaders) {
                for (var prop in this.headers) {
                    if (this.headers.hasOwnProperty(prop)) {
                        o.conn.setRequestHeader(prop, this.headers[prop]);
                    }
                }
                this.headers = {};
                this.hasHeaders = false;
            }
        },

        resetDefaultHeaders:function() {
            delete this.defaultHeaders;
            this.defaultHeaders = {};
            this.hasDefaultHeaders = false;
        },

        abort:function(o, callback, isTimeout)
        {
            if (this.isCallInProgress(o)) {
                o.conn.abort();
                window.clearInterval(this.poll[o.tId]);
                delete this.poll[o.tId];
                if (isTimeout) {
                    delete this.timeout[o.tId];
                }

                this.handleTransactionResponse(o, callback, true);

                return true;
            }
            else {
                return false;
            }
        },


        isCallInProgress:function(o)
        {


            if (o.conn) {
                return o.conn.readyState != 4 && o.conn.readyState != 0;
            }
            else {

                return false;
            }
        },


        releaseObject:function(o)
        {

            o.conn = null;

            o = null;
        },

        activeX:[
        'MSXML2.XMLHTTP.3.0',
        'MSXML2.XMLHTTP',
        'Microsoft.XMLHTTP'
        ]


    });Sbi.sdk.namespace('Sbi.sdk.jsonp');


Sbi.sdk.apply(Sbi.sdk.jsonp, {
	
	timeout : 30000,
    callbackParam : "callback",
    nocache : true,
    trans_id: 0,
    
    asyncRequest : function(uri, callback, scope, arg){
        
		if(this.head === undefined) {
			this.head = document.getElementsByTagName("head")[0];
		}
	
        if(this.nocache){
        	uri += (uri.indexOf("?") != -1 ? "&" : "?") + "_dc=" + (new Date().getTime());
        }
        
        var transId = ++this.trans_id;
        
        var trans = {
            id : transId,
            cb : "stcCallback"+transId,
            scriptId : "stcScript"+transId,
            arg : arg,
            url : uri,
            callback : callback,
            scope : scope
        };
        
        var conn = this;

        window[trans.cb] = function(o){
            conn.handleResponse(o, trans);
        };

        uri += (uri.indexOf("?") != -1 ? "&" : "?") + this.callbackParam + "=" + trans.cb;
        
        if(this.autoAbort !== false){
            this.abort();
        }

        trans.timeoutId = this.handleFailure.defer(this.timeout, this, [trans]);

        var script = document.createElement("script");
        script.setAttribute("src", uri);
        script.setAttribute("type", "text/javascript");
        script.setAttribute("id", trans.scriptId);
        this.head.appendChild(script);

        this.trans = trans;    
	},
	
	 // private
    isLoading : function(){
        return this.trans ? true : false;
    },

    /**
     * Abort the current server request.
     */
    abort : function(){
        if(this.isLoading()){
            this.destroyTrans(this.trans);
        }
    },

    // private
    destroyTrans : function(trans, isLoaded){
        this.head.removeChild(document.getElementById(trans.scriptId));
        clearTimeout(trans.timeoutId);
        if(isLoaded){
            window[trans.cb] = undefined;
            try{
                delete window[trans.cb];
            }catch(e){}
        }else{
            // if hasn't been loaded, wait for load to remove it to prevent script error
            window[trans.cb] = function(){
                window[trans.cb] = undefined;
                try{
                    delete window[trans.cb];
                }catch(e){}
            };
        }
    },

    // private
    handleResponse : function(o, trans){
        this.trans = false;
        this.destroyTrans(trans, true);
        var result = o;
        trans.callback.call(trans.scope||window, result, trans.arg, true);
        
    },

    // private
    handleFailure : function(trans){
        this.trans = false;
        this.destroyTrans(trans, false);
        trans.callback.call(trans.scope||window, null, trans.arg, false);
    }

});
Sbi.sdk.namespace('Sbi.sdk.services');


Sbi.sdk.apply(Sbi.sdk.services, {

    services: null
    
    , baseUrl:  {
		protocol: 'http'     
		, host: 'localhost'
	    , port: '8080'
	    , contextPath: 'SpagoBI'
	    , controllerPath: 'servlet/AdapterHTTP'    
	}
    
    , initServices: function() {
        this.services = {};
        this.services.authenticate = {
            type: 'ACTION', 
            name: 'LOGIN_ACTION_WEB', 
            baseParams: {NEW_SESSION: 'TRUE'}
        };
        this.services.execute = {
            type: 'PAGE', 
            name: 'ExecuteBIObjectPage', 
            baseParams: {NEW_SESSION: 'TRUE', MODALITY: 'SINGLE_OBJECT_EXECUTION_MODALITY', IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS: 'true'}
        };
        
        this.services.executewithext = {
            type: 'ACTION', 
            name: 'EXECUTE_DOCUMENT_ACTION', 
            baseParams: {NEW_SESSION: 'TRUE', IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS: 'true'}
        };
        
        this.services.executedataset = {
            type: 'ACTION', 
            name: 'EXECUTE_DATASET_ACTION', 
            baseParams: {NEW_SESSION: 'TRUE'}
        };
    }
    
    , setBaseUrl: function(u) {
        Sbi.sdk.apply(this.baseUrl, u || {});
    }
    
    , getServiceUrl: function(serviceName, p) {
        var urlStr = null;
        
        if(this.services === null) {
            this.initServices();
        }
        
        if(this.services[serviceName] === undefined) {
            alert('ERROR: Service [' + + '] does not exist');
        } else {
            urlStr = '';
            urlStr = this.baseUrl.protocol + '://' + this.baseUrl.host + ":" + this.baseUrl.port + '/' + this.baseUrl.contextPath + '/' + this.baseUrl.controllerPath;
            var params;
            if(this.services[serviceName].type === 'PAGE'){
            	params = {PAGE: this.services[serviceName].name};
            } else {
            	params = {ACTION_NAME: this.services[serviceName].name};            	
            }
            
            Sbi.sdk.apply(params, p || {}, this.services[serviceName].baseParams || {});
            var paramsStr = Sbi.sdk.urlEncode(params);
            urlStr += '?' + paramsStr;
        }
        
        return urlStr;
    }
});

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
		
		if(config.useExtUI === true) {
			documentUrl = Sbi.sdk.services.getServiceUrl('executewithext', params);
		} else {
			documentUrl = Sbi.sdk.services.getServiceUrl('execute', params);
		}
		
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