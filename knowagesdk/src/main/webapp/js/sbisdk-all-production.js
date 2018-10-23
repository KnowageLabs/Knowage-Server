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


    });
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

 Sbi.sdk.namespace('Sbi.sdk.jsonp');


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

Sbi.sdk.namespace('Sbi.sdk.cors');

Sbi.sdk.apply(Sbi.sdk.cors, {
	/*
	 * The default callback function used when the server returns an error.
	 *
	 * @param obj the object contained in the response body
	 * @param status the status code of the response
	 * @param statusText the status text of the response
	 * */
	defaultCallbackError:  function (obj, status, statusText){
		var message = 'Error: ';
		if (status !== undefined) message += status + ' - ' + statusText;

		if (obj.errors !== undefined)
			message += '\n\n' + obj.errors[0].localizedMessage;

		alert(message);
	}

	/*
	 * It performs an async request.
	 *
	 * @param config it must contain the method (GET, POST etc..), the url, callbackOk that is the callback to be called if the server returns
	 * the aspected response. Optionally it can contains body, headers and callbackError (the callback function used when the server returns an error).
	 *
	 * To be used if the response is a Json string
	 * */
	, asyncRequest: function(config){
		var xhr = new XMLHttpRequest();

		if ("withCredentials" in xhr) { //The browser support XMLHttpRequest. Chrome, Firefox, Safari and new versions of Opera and Internet Explorer support it
			xhr.open(config.method, config.url, true);
		}
		else if (typeof XDomainRequest != "undefined") { //The browser is Internet Explorer 8 or 9 (they use XDomainRequest instead of XMLHttpRequest)
			xhr = new XDomainRequest();
			xhr.open(config.method, config.url);
		}
		else { //The browser doesn't support CORS
			alert('Your browser does not support CORS.');
			return;
		}

		xhr.onerror = function() {
			alert('Error while trying to contact server');
		}

		var callbackError;
		if (config.callbackError) callbackError = config.callbackError;
		else callbackError = this.defaultCallbackError;

		xhr.onload = function() {
			var obj = null;

			if (xhr.response !== "") obj = JSON.parse(xhr.response);

			if (xhr.status == '200' || xhr.status == '201'){

				/* A REST service could return a status code 200 even if there are errors
				 * (in such a case errors will be inside the Json object in the response body) */
				if (obj.errors !== undefined){
					callbackError(obj);
				}

				config.callbackOk(obj);
			}
			else {
				callbackError(obj, xhr.status, xhr.statusText);
			}

		}

		for (var index in config.headers){
			var header = config.headers[index];
			xhr.setRequestHeader(header.name, header.value);
		}

		//It enables the use of cookies
		xhr.withCredentials = 'true';

		if (config.body !== "undefined"){
			xhr.send(config.body);
		}
		else xhr.send();
	}
});
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


Sbi.sdk.namespace('Sbi.sdk.services');


Sbi.sdk.apply(Sbi.sdk.services, {

    services: null

    , baseUrl:  {
		protocol: 'http'
		, host: 'localhost'
	    , port: '8080'
	    , contextPath: 'knowage'
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
            type: 'ACTION',
            name: 'EXECUTE_DOCUMENT_ANGULAR_ACTION',
            baseParams: {NEW_SESSION: 'TRUE'}
        };

        this.services.adHocReporting = {
    		type: 'ACTION',
            name: 'AD_HOC_REPORTING_START_ACTION',
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
            alert('ERROR: Service [' + serviceName + '] does not exist');
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

Sbi.sdk.namespace('Sbi.sdk.api');

Sbi.sdk.apply(Sbi.sdk.api, {
	authenticate:  function ( config ) {
		var serviceUrl = Sbi.sdk.services.getServiceUrl('authenticate', config.params);
		Sbi.sdk.jsonp.asyncRequest(serviceUrl, config.callback.fn, config.callback.scope, config.callback.args);
	}

	/**
	 * This callback is called a response is returned by the server.
	 * @callback ResponseCallback
	 * @param {String} json - json of response
	 * @param {Array} args
	 * @param {Boolean} success - true if it's a success response otherwise false
	 */

	/**
	 * It returns the list of datasets
	 * @example
	 * execTest6 = function() {
	 *	    Sbi.sdk.api.getDataSetList({
	 *	    	callback: function( json, args, success ) {
	 *	    		if (success){
	 *	    			var str = "";
	 *
	 *	    			for (var key in json){
	 *		    			str += "<tr><td>" + json[key].label + "</td><td>" + json[key].name + "</td><td>" + json[key].description + "</td></tr>";
	 *	    			}
	 *
	 *	    			document.getElementById('datasets').innerHTML = str;
	 *	    		}
	 *			}});
	 *	};
	 * @method Sbi.sdk.api.getDataSetList
	 * @param {Object} config - the configuration
	 * @param {ResponseCallback} config.callback - function to be called after the response is returned by the server
	 */
	, getDataSetList: function( config ) {

		Sbi.sdk.jsonp.timeout = 10000;

		var baseUrl = Sbi.sdk.services.baseUrl;
		var serviceUrl = baseUrl.protocol + '://' + baseUrl.host + ":" + baseUrl.port + '/' + baseUrl.contextPath + '/restful-services/2.0/datasets';

		Sbi.sdk.jsonp.asyncRequest(serviceUrl, config.callback, this);
	}
	/**
	 * It returns the list of documents
	 * @example
	 * execTest8 = function() {
	 *	    Sbi.sdk.api.getDocuments({
	 *	    	callback: function( json, args, success ) {
	 *	    		if (success){
	 *	    			var str = "";
	 *
	 *	    			for (var key in json){
	 *		    			str += "<tr><td>" + json[key].label + "</td><td>" + json[key].name + "</td><td>" + json[key].description + "</td></tr>";
	 *	    			}
	 *
	 *	    			document.getElementById('documents').innerHTML = str;
	 *	    		}
	 *			}});
	 *	};
	 * @method Sbi.sdk.api.getDocuments
	 * @param {Object} config - the configuration
	 * @param {ResponseCallback} config.callback - function to be called after the response is returned by the server
	 */
	, getDocuments: function( config ) {

		Sbi.sdk.jsonp.timeout = 10000;

		var baseUrl = Sbi.sdk.services.baseUrl;
		var serviceUrl = baseUrl.protocol + '://' + baseUrl.host + ":" + baseUrl.port + '/' + baseUrl.contextPath + '/restful-services/2.0/documents';


		Sbi.sdk.jsonp.asyncRequest(serviceUrl, config.callback, this);
	}

	/**
	 * It executes a dataset
	 * @example
	 * execTest7 = function() {
	 *    Sbi.sdk.api.executeDataSet({
	 *    	datasetLabel: 'DS_DEMO_EXTCHART'
	 *    	, parameters: {
	 *    		par_year: 2011,
	 *    		par_family: 'Food'
	 *    	}
	 *    	, callback: function( json, args, success ) {
	 *    		if (success){
	 *    			var str = "<th>Id</th>";
	 *
	 *    			var fields = json.metaData.fields;
	 *    			for(var fieldIndex in fields) {
	 *    				if (fields[fieldIndex].hasOwnProperty('header'))
	 *    					str += '<th>' + fields[fieldIndex]['header'] + '</th>';
	 *    			}
	 *
	 *    			str += '<tbody>';
	 *
	 *    			var rows = json.rows;
	 *    			for (var rowIndex in rows){
	 *    				str += '<tr>';
	 *    				for (var colIndex in rows[rowIndex]) {
	 *    					str += '<td>' + rows[rowIndex][colIndex] + '</td>';
	 *    				}
	 *    				str += '</tr>';
	 *    			}
	 *
	 *    			str += '</tbody>';
	 *
	 *    			document.getElementById('results').innerHTML = str;
	 *    		}
	 *		}});
	 * };
	 * @method Sbi.sdk.api.executeDataSet
	 * @param {Object} config - the configuration
	 * @param {String} config.documentLabel - the document label
	 * @param {Object} [config.parameters] - the values of dataset parameters
	 * @param {ResponseCallback} config.callback - function to be called after the response is returned by the server
	 */
	, executeDataSet: function( config ) {

		Sbi.sdk.jsonp.timeout = 20000;

		var baseUrl = Sbi.sdk.services.baseUrl;
		var serviceUrl = baseUrl.protocol + '://' + baseUrl.host + ":" + baseUrl.port + '/' + baseUrl.contextPath + '/restful-services/2.0/datasets/';
		serviceUrl += config.datasetLabel + '/content';

		if (config.parameters !== undefined) {
			var first = true;

			for(var parameter in config.parameters) {
				if (first) {
					serviceUrl += '?';
					first = false;
				}
				else serviceUrl += '&';

				serviceUrl += parameter + '=' + config.parameters[parameter];
			}
		}

		Sbi.sdk.jsonp.asyncRequest(serviceUrl, config.callback, this);
	}
});
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

Sbi.sdk.namespace('Sbi.sdk.cors.api');


/**
* @namespace Sbi.sdk.cors
*/

/**
 * There are three main advantages on using CORS over jsonp:
 * <ul>
 *  <li>all the methods are available while in jsonp only GET request can be done;</li>
 *  <li>if an error occurs it is possible to manage it with CORS, while in jsonp it is only possible to set a timeout;</li>
 *  <li>jsonp has security problems (see later for an example).</li>
 * </ul>
 * @see {@link Sbi.sdk.api}
 * @namespace Sbi.sdk.cors.api
 **/
Sbi.sdk.apply(Sbi.sdk.cors.api, {

	elId: 0
	, dataSetList: {}

	, authenticate: function ( config ) {
		var serviceUrl = Sbi.sdk.services.getServiceUrl('authenticate', config.params);
		Sbi.sdk.cors.asyncRequest({
			method: 'POST',
			url: serviceUrl,
			headers: config.headers,
			callbackOk: config.callbackOk,
			callbackError: config.callbackError,
			body: config.credentials
		});
	}

	/**
	 * It returns the list of datasets. This time config contains two callback functions: callbackOk and callbackError. This is because with CORS it is possible to know when the server returns an error, so we can define the behavior to have if there is an error. However, it is not mandatory to specify the callbackError function: there is a default one that create a pop up with status code and text describing the error.
	 * @example
 	 * execTest6 = function() {
	 *    Sbi.sdk.cors.api.getDataSetList({
	 *    	callbackOk: function(obj) {
	 *    		str = '';
	 *
	 *    		for (var key in obj){
	 *    			str += "<tr><td>" + obj[key].label + "</td><td>" + obj[key].name + "</td><td>" + obj[key].description + "</td></tr>";
	 *  			}
	 *
	 *  			document.getElementById('datasets').innerHTML = str;
	 *		}
	 *    });
	 *	};
	 * @method Sbi.sdk.cors.api.getDataSetList
	 * @param {Object} config - the configuration
	 * @param {ResponseCallback} config.callbackOk - function to be called after the ok response is returned by the server
	 * @param {ResponseCallback} [config.callbackError] - function to be called after the error response is returned by the server
	 */
	, getDataSetList: function ( config ) {
		var baseUrl = Sbi.sdk.services.baseUrl;
		var serviceUrl = baseUrl.protocol + '://' + baseUrl.host + ":" + baseUrl.port + '/' + baseUrl.contextPath + '/restful-services/2.0/datasets';

		var headers = [];
		if (config.basicAuthentication !== undefined) {
			var ba = config.basicAuthentication;
			var encoded = btoa(ba.userName + ':' + ba.password);

			headers[0] = {
				name: 'Authorization',
				value: 'Basic ' + encoded
			}
		}

		Sbi.sdk.cors.asyncRequest({
			method: 'GET',
			url: serviceUrl,
			headers: headers,
			callbackOk: config.callbackOk,
			callbackError: config.callbackError
		});
	}
	/**
	 * It returns the list of Documents. This time config contains two callback functions: callbackOk and callbackError. This is because with CORS it is possible to know when the server returns an error, so we can define the behavior to have if there is an error. However, it is not mandatory to specify the callbackError function: there is a default one that create a pop up with status code and text describing the error.
	 * @example
 	 * execTest6 = function() {
	 *    Sbi.sdk.cors.api.getDocuments({
	 *    	callbackOk: function(obj) {
	 *    		str = '';
	 *
	 *    		for (var key in obj){
	 *    			str += "<tr><td>" + obj[key].label + "</td><td>" + obj[key].name + "</td><td>" + obj[key].description + "</td></tr>";
	 *  			}
	 *
	 *  			document.getElementById('documentss').innerHTML = str;
	 *		}
	 *    });
	 *	};
	 * @method Sbi.sdk.cors.api.getDocuments
	 * @param {Object} config - the configuration
	 * @param {ResponseCallback} config.callbackOk - function to be called after the ok response is returned by the server
	 * @param {ResponseCallback} [config.callbackError] - function to be called after the error response is returned by the server
	 */
	, getDocuments: function ( config ) {
		var baseUrl = Sbi.sdk.services.baseUrl;
		var serviceUrl = baseUrl.protocol + '://' + baseUrl.host + ":" + baseUrl.port + '/' + baseUrl.contextPath + '/restful-services/2.0/documents';

		var headers = [];
		if (config.basicAuthentication !== undefined) {
			var ba = config.basicAuthentication;
			var encoded = btoa(ba.userName + ':' + ba.password);

			headers[0] = {
				name: 'Authorization',
				value: 'Basic ' + encoded
			}
		}

		Sbi.sdk.cors.asyncRequest({
			method: 'GET',
			url: serviceUrl,
			headers: headers,
			callbackOk: config.callbackOk,
			callbackError: config.callbackError
		});
	}
	/**
	 * It executes a dataset. This time config contains two callback functions: callbackOk and callbackError. This is because with CORS it is possible to know when the server returns an error, so we can define the behavior to have if there is an error. However, it is not mandatory to specify the callbackError function: there is a default one that create a pop up with status code and text describing the error.
	 * @example
	 * execTest7 = function() {
	 *    Sbi.sdk.cors.api.executeDataSet({
	 *    	datasetLabel: 'DS_DEMO_EXTCHART'
	 *    	, parameters: {
	 *    		par_year: 1998,
	 *    		par_family: 'Food'
	 *    	}
	 *    	, callbackOk: function(obj) {
	 *    		var str = "<th>Id</th>";
	 *
	 *  			var fields = obj.metaData.fields;
	 *  			for(var fieldIndex in fields) {
	 *  				if (fields[fieldIndex].hasOwnProperty('header'))
	 *  					str += '<th>' + fields[fieldIndex]['header'] + '</th>';
	 *  			}
	 *
	 *  			str += '<tbody>';
	 *
	 *  			var rows = obj.rows;
	 *  			for (var rowIndex in rows){
	 *  				str += '<tr>';
	 *  				for (var colIndex in rows[rowIndex]) {
	 *  					str += '<td>' + rows[rowIndex][colIndex] + '</td>';
	 *  				}
	 *  				str += '</tr>';
	 *  			}
	 *
	 *  			str += '</tbody>';
	 *
	 *  			document.getElementById('results').innerHTML = str;
	 *		}});
	 *};
	 * @method Sbi.sdk.cors.api.executeDataSet
	 * @param {Object} config - the configuration
	 * @param {String} config.documentLabel - the document label
	 * @param {Object} [config.parameters] - the values of dataset parameters
	 * @param {ResponseCallback} config.callbackOk - function to be called after the ok response is returned by the server
	 * @param {ResponseCallback} [config.callbackError] - function to be called after the error response is returned by the server
	 */
	, executeDataSet: function ( config ) {
		var baseUrl = Sbi.sdk.services.baseUrl;
		var serviceUrl = baseUrl.protocol + '://' + baseUrl.host + ":" + baseUrl.port + '/' + baseUrl.contextPath + '/restful-services/2.0/datasets/';
		serviceUrl += config.datasetLabel + '/content';

		if (config.parameters !== undefined) {
			var first = true;

			for(var parameter in config.parameters) {
				if (first) {
					serviceUrl += '?';
					first = false;
				}
				else serviceUrl += '&';

				serviceUrl += parameter + '=' + config.parameters[parameter];
			}
		}

		var headers = [];
		if (config.basicAuthentication !== undefined) {
			var ba = config.basicAuthentication;
			var encoded = btoa(ba.userName + ':' + ba.password);

			headers[0] = {
				name: 'Authorization',
				value: 'Basic ' + encoded
			}
		}

		Sbi.sdk.cors.asyncRequest({
			method: 'GET',
			url: serviceUrl,
			headers: headers,
			callbackOk: config.callbackOk,
			callbackError: config.callbackError
		});
	}
});
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

Sbi.sdk.namespace('Sbi.sdk.api');

/**
* @namespace Sbi
*/

/**
* @namespace Sbi.sdk
*/

/**
 * Note that Sbi.sdk.api definition is defined in both api.js and api_jsonp.js.
 * In api_jsonp.js there are functions that uses jsonp to avoid the same-origin policy.
 * The same functions were also developed with CORS and they are defined in api_cors.js.
 *
 * jsonp is deprecated, it is highly recommended to use CORS instead of it.
 *
 * NB: CORS functions are inside Sbi.sdk.cors.api namespace and have same names as jsonp counterpart.
 * @namespace Sbi.sdk.api
 */
Sbi.sdk.apply(Sbi.sdk.api, {

	elId: 0
	, dataSetList: {}

	, getIFrameHtml: function( serviceUrl, config ) {

		var html;
		config.iframe = config.iframe || {};

		if(config.iframe.id === undefined) {
			config.iframe.id = 'sbi-docexec-iframe-' + this.elId;
			this.elId = this.elId +1;
		}

		html = '';
		html += '<iframe';
		html += ' id = "' + config.iframe.id + '" ';
		html += ' src = "' + serviceUrl + '" ';
		if(config.iframe.style !== undefined) html += ' style = "' + config.iframe.style + '" ';
		if(config.iframe.width !== undefined) html += ' width = "' + config.iframe.width + '" ';
		if(config.iframe.height !== undefined) html += ' height = "' + config.iframe.height + '" ';
		html += '></iframe>';

		return html;
	}

	, injectIFrame: function( serviceUrl, config ) {

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


		targetEl.innerHTML = this.getIFrameHtml(serviceUrl, config);
	}

	, getDocumentUrl: function( config ) {
		var documentUrl = null;

		if(config.documentId === undefined && config.documentLabel === undefined) {
			alert('ERROR: at least one beetween documentId and documentLabel attributes must be specified');
			return null;
		}

		//var params = Sbi.sdk.apply({}, config.parameters || {});
		var params = {};
		params.PARAMETERS = Sbi.sdk.urlEncode(config.parameters);

		if(config.documentId !== undefined) params.OBJECT_ID = config.documentId;
		if(config.documentLabel !== undefined) params.OBJECT_LABEL = config.documentLabel;
		if(config.documentName !== undefined) params.OBJECT_NAME = config.documentName;
		if (config.executionRole !== undefined) params.ROLE = config.executionRole;
		if (config.displayToolbar !== undefined) params.TOOLBAR_VISIBLE = config.displayToolbar;
		if (config.canResetParameters !== undefined) params.CAN_RESET_PARAMETERS = config.canResetParameters;
		if (config.theme !== undefined)	params.theme = config.theme;

		documentUrl = Sbi.sdk.services.getServiceUrl('execute', params);

		return documentUrl;
	}

	 /**
	 * It returns the HTML code of an iFrame containing document visualization. In particular config is an object that must contain at least one between documentId and documentLabel.
	 * It can also have (optional) parameters (an object containing values of document parameters), executionRole, displayToolbar and iframe, an object containing the style, height and width of the iframe where the document will be rendered (height and width can also be put outside the iframe object).
	 * @example
	 * var html = Sbi.sdk.api.getDocumentHtml({
	 * 		documentLabel: 'RPT_WAREHOUSE_PROF'
	 * 		, executionRole: '/spagobi/user'
	 * 		, parameters: {warehouse_id: 19}
	 * 		, displayToolbar: false
	 * 		, displaySliders: false
	 * 		, iframe: {
	 *     		height: '500px'
	 *     		, width: '100%'
	 * 			, style: 'border: 0px;'
	 * 		}
	 * 	});
	 *
	 * @method Sbi.sdk.api.getDocumentHtml
	 * @param {Object} config - the configuration
	 * @param {String} config.documentId - the document id, must contain at least one between documentId and documentLabel
	 * @param {String} config.documentLabel - the document label,  must contain at least one between documentId and documentLabel
	 * @param {Object} [config.parameters] - the values of document parameters
	 * @param {String} [config.executionRole] - the role of execution
	 * @param {Boolean} [config.displayToolbar] - display or not the toolbar
	 * @param {Boolean} [config.displaySliders] - display or not the sliders
	 * @param {Object} [config.iframe] - the style object of iframe
	 * @param {String} [config.iframe.height] - the height of iframe
	 * @param {String} [config.iframe.width] - the width of iframe
	 * @param {String} [config.iframe.style] - the style of iframe
	 */
	, getDocumentHtml: function( config ) {

		var serviceUrl = this.getDocumentUrl( config );
		return this.getIFrameHtml(serviceUrl, config);
	}

	 /**
	 * It calls {@link Sbi.sdk.api.getDocumentHtml} and inject the generated iFrame inside a specified tag. If the target tag is not specified in config variable, it chooses the <body> tag as default.
	 * It can also have (optional) parameters (an object containing values of document parameters), executionRole, displayToolbar and iframe, an object containing the style, height and width of the iframe where the document will be rendered (height and width can also be put outside the iframe object).
	 * @see {@link Sbi.sdk.api.getDocumentHtml}
	 * @example
	 * execTest8 = function() {
 	 *	Sbi.sdk.api.injectWorksheet({
	 *		datasetLabel: 'DS_DEMO_51_COCKPIT'
 	 *		, target: 'worksheet'
	 *		, height: '600px'
	 *		, width: '1100px'
	 *		, iframe: {
	 *			style: 'border: 0px;'
	 *		}
	 *	});
	 * };
	 *@method Sbi.sdk.api.injectDocument
	 * @param {Object} config - the configuration
	 * @param {String} config.documentId - the document id, must contain at least one between documentId and documentLabel
	 * @param {String} config.documentLabel - the document label,  must contain at least one between documentId and documentLabel
	 * @param {Object} [config.parameters] - the values of document parameters
	 * @param {String} [config.executionRole] - the role of execution
	 * @param {Boolean} [config.displayToolbar] - display or not the toolbar
	 * @param {Boolean} [config.displaySliders] - display or not the sliders
	 * @param {Object} [config.iframe] - the style object of iframe
	 * @param {String} [config.iframe.height] - the height of iframe
	 * @param {String} [config.iframe.width] - the width of iframe
	 * @param {String} [config.iframe.style] - the style of iframe
	 */
	, injectDocument: function( config ) {

		var serviceUrl = this.getDocumentUrl( config );
		return this.injectIFrame(serviceUrl, config);
	}

	, getAdHocReportingUrl: function( config ) {
		var url = null;

		if(config.datasetLabel === undefined) {
			alert('ERROR: datasetLabel attribute must be specified');
			return null;
		}

		var params = {};
		params.dataset_label = config.datasetLabel;
		params.TYPE_DOC = config.type;

		if (config.parameters !== undefined){
			for(var parameter in config.parameters)
				params[parameter] = config.parameters[parameter];
		}

		return Sbi.sdk.services.getServiceUrl('adHocReporting', params);
	}

	, getWorksheetUrl: function( config ) {
		config.type = 'WORKSHEET';
		return this.getAdHocReportingUrl(config);
	}

	/**
	 * It returns the HTML code of an iFrame containing worksheet visualization.
	 * config is an object that must contain datasetLabel and iframe, an object containing the style, height and width of the iframe where the worksheet and qbe will be rendered (height and width can also be put outside the iframe object).
	 *
	 * @method Sbi.sdk.api.getWorksheetHtml
	 * @param {Object} config - the configuration
	 * @param {String} config.target - the target
	 * @param {String} config.documentLabel - the document label,  must contain at least one between documentId and documentLabel
	 * @param {String} [config.height] - the height of iframe, can be put inside iframe object
	 * @param {String} [config.width] - the width of iframe, can be put inside iframe object
	 * @param {Object} config.iframe - the style object of iframe
	 * @param {String} [config.iframe.height] - the height of iframe, can be put outside
	 * @param {String} [config.iframe.width] - the width of iframe
	 * @param {String} [config.iframe.style] - the style of iframe
	 */
	, getWorksheetHtml: function( config ) {

		var serviceUrl = this.getWorksheetUrl( config );
		return this.getIFrameHtml(serviceUrl, config);
	}

	/**
	 * It calls {@link Sbi.sdk.api.getWorksheetHtml} and inject the generated iFrame inside a specified tag. If the target tag is not specified in config variable, it chooses the <body> tag as default
	 * config is an object that must contain datasetLabel and iframe, an object containing the style, height and width of the iframe where the worksheet and qbe will be rendered (height and width can also be put outside the iframe object).
	 *
	 * @method Sbi.sdk.api.injectWorksheet
	 * @param {Object} config - the configuration
	 * @param {String} [config.target="<body>"] - the target
	 * @param {String} config.documentLabel - the document label,  must contain at least one between documentId and documentLabel
	 * @param {String} [config.height] - the height of iframe, can be put inside iframe object
	 * @param {String} [config.width] - the width of iframe, can be put inside iframe object
	 * @param {Object} config.iframe - the style object of iframe
	 * @param {String} [config.iframe.height] - the height of iframe, can be put outside
	 * @param {String} [config.iframe.width] - the width of iframe
	 * @param {String} [config.iframe.style] - the style of iframe
	 */
	, injectWorksheet: function( config ) {

		var serviceUrl = this.getWorksheetUrl( config );
		return this.injectIFrame(serviceUrl, config);
	}

	, getQbeUrl: function( config ) {
		config.type = 'QBE';
		return this.getAdHocReportingUrl(config);
	}

	/**
	 * It returns the HTML code of an iFrame containing qbe visualization.
	 * config is an object that must contain datasetLabel and iframe, an object containing the style, height and width of the iframe where the worksheet and qbe will be rendered (height and width can also be put outside the iframe object).
	 *
	 * @method Sbi.sdk.api.getQbeHtml
	 * @param {Object} config - the configuration
	 * @param {String} config.target - the target
	 * @param {String} config.documentLabel - the document label,  must contain at least one between documentId and documentLabel
	 * @param {String} [config.height] - the height of iframe, can be put inside iframe object
	 * @param {String} [config.width] - the width of iframe, can be put inside iframe object
	 * @param {Object} config.iframe - the style object of iframe
	 * @param {String} [config.iframe.height] - the height of iframe, can be put outside
	 * @param {String} [config.iframe.width] - the width of iframe
	 * @param {String} [config.iframe.style] - the style of iframe
	 */
	, getQbeHtml: function( config ) {

		var serviceUrl = this.getQbeUrl( config );
		return this.getIFrameHtml(serviceUrl, config);
	}

	/**
	 * It calls {@link Sbi.sdk.api.getQbeHtml} and inject the generated iFrame inside a specified tag. If the target tag is not specified in config variable, it chooses the <body> tag as default.
	 * config is an object that must contain datasetLabel and iframe, an object containing the style, height and width of the iframe where the worksheet and qbe will be rendered (height and width can also be put outside the iframe object).
	 * @example
	 *	execTest9 = function() {
	 *		Sbi.sdk.api.injectQbe({
	 *			datasetLabel: 'DS_DEMO_51_COCKPIT'
	 *			, target: 'qbe'
	 *			, height: '600px'
	 *			, width: '1100px'
	 *			, iframe: {
	 *			style: 'border: 0px;'
	 *		  }
	 *		});
	 *	};
	 * @method Sbi.sdk.api.injectQbe
	 * @param {Object} config - the configuration
	 * @param {String} [config.target="<body>"] - the target
	 * @param {String} config.documentLabel - the document label,  must contain at least one between documentId and documentLabel
	 * @param {String} [config.height] - the height of iframe, can be put inside iframe object
	 * @param {String} [config.width] - the width of iframe, can be put inside iframe object
	 * @param {Object} config.iframe - the style object of iframe
	 * @param {String} [config.iframe.height] - the height of iframe, can be put outside
	 * @param {String} [config.iframe.width] - the width of iframe
	 * @param {String} [config.iframe.style] - the style of iframe
	 */
	, injectQbe: function( config ) {

		var serviceUrl = this.getQbeUrl( config );
		return this.injectIFrame(serviceUrl, config);
	}


	, authenticateToken: function ( token ){
		console.log(token);
		var baseUrl = Sbi.sdk.services.baseUrl;
		var serviceUrl = baseUrl.protocol + '://' + baseUrl.host + ":" + baseUrl.port + '/' + baseUrl.contextPath + '/restful-services/2.0/autenticateUser';

		var headers = [];
		headers[0] = {
				name: 'X-Auth-Token',
				value: token
			}

		Sbi.sdk.cors.asyncRequest({
			method: 'GET',
			url: serviceUrl,
			headers: headers ,
			callbackOk : function (){

			}
		});	}

});
