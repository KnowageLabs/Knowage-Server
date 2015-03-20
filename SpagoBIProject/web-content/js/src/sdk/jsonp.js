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