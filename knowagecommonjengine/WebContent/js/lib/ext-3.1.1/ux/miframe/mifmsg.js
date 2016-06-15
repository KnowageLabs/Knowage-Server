/* global Ext */
/* Messaging Driver for ux.ManagedIFrame 
 *******************************************************************************
 * This file is distributed on an AS IS BASIS WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * ***********************************************************************************
 * @version 2.1.2
 *
 * License: ux.ManagedIFrame, ux.ManagedIFramePanel, ux.ManagedIFrameWindow  
 * are licensed under the terms of the Open Source GPL 3.0 license:
 * http://www.gnu.org/licenses/gpl.html
 *
 * Commercial use is prohibited without a Commercial Developement License. See
 * http://licensing.theactivegroup.com.
 *
 * Donations are welcomed: http://donate.theactivegroup.com
 *
 */ 

 
(function(){
      
     /**
     * @private, frame messaging interface (for same-domain-policy frames
     *           only)
     */
     var _XFrameMessaging = function() {
        // each tag gets a hash queue ($ = no tag ).
        var tagStack = { '$' : [] };
        var isEmpty = function(v, allowBlank) {
            return v === null || v === undefined
                    || (!allowBlank ? v === '' : false);
        };
        var apply = function(o, c, defaults) {
	        if (defaults) { apply(o, defaults);}
	        if (o && c && typeof c == 'object') {
	            for (var p in c) {
	                o[p] = c[p];
	            }
	        }
	        return o;
	    };

        window.sendMessage = function(message, tag, domain) {
            var MIF;
            if (MIF = arguments.callee.manager) {
                if (message._fromHost) {
                    var fn, result;
                    // only raise matching-tag handlers
                    var compTag = message.tag || tag || null;
                    var mstack = !isEmpty(compTag) ? tagStack[String(compTag).toLowerCase()] || [] : tagStack["$"];

                    for (var i = 0, l = mstack.length; i < l; i++) {
                        if (fn = mstack[i]) {
                            result = fn.apply(fn.__scope, arguments) === false? false: result;
                            if (fn.__single) {
                                mstack[i] = null;
                            }
                            if (result === false) {
                                break;
                            }
                        }
                    }

                    return result;
                } else {

                    message = {
                        type : 'message',
                        data : message,
                        domain : domain || document.domain,
                        origin : location.protocol + '//' + location.hostname,
                        uri : document.documentURI,
                        source : window,
                        tag : tag ? String(tag).toLowerCase() : null
                    };

                    try {
                        return MIF.disableMessaging !== true
                                ? MIF._observable ? MIF._observable.fireEvent.call(MIF._observable, message.type,MIF, message) : null
                                : null;
                    } catch (ex) {
                    } // trap for message:tag handlers not yet defined

                    return null;
                }

            }
        };
        window.onhostmessage = function(fn, scope, single, tag) {

            if (typeof fn == 'function') {
                if (!isEmpty(fn.__index)) {
                    throw "onhostmessage: duplicate handler definition"
                            + (tag ? " for tag:" + tag : '');
                }

                var k = isEmpty(tag) ? "$" : tag.toLowerCase();
                tagStack[k] || (tagStack[k] = []);
                apply(fn, {
                            __tag : k,
                            __single : single || false,
                            __scope : scope || window,
                            __index : tagStack[k].length
                        });
                tagStack[k].push(fn);

            } else {
                throw "onhostmessage: function required";
            }

        };
        window.unhostmessage = function(fn) {
            if (typeof fn == 'function' && typeof fn.__index != 'undefined') {
                var k = fn.__tag || "$";
                tagStack[k][fn.__index] = null;
            }
        };

    }; 
   
    var MIFEl = Ext.ux.ManagedIFrame.Element;
    
    Ext.override( MIFEl ,{
        
        disableMessaging :  true,
        
        _renderHook : MIFEl.prototype._renderHook.createSequence(function(){
            
            if(this.disableMessaging){return;}
            
            //assert a default 'message' event
            var O=this._observable;
            O && (O.events.message || (O.addEvents('message')));
            
            if (this.domWritable()) {
                this.loadFunction({
                            name : 'XMessage',
                            fn : _XFrameMessaging
                        }, false, true);
                        
                var sm, w = this.getWindow();
                w && (sm = w.sendMessage) && (sm.manager = this);
                
            }
            
        }),
        
        /**
         * dispatch a message to the embedded frame-window context (same-origin frames only)
         * @name sendMessage
         * @memberOf Ext.ux.ManagedIFrame.Element
         * @param {Mixed} message The message payload.  The payload can be any supported JS type. 
         * @param {String} tag Optional reference tag 
         * @param {String} origin Optional domain designation of the sender (defaults
         * to document.domain).
         */
        sendMessage : function(message, tag, domain) {
            var win, L = location;
            if (this.domWritable() && (win = this.getWindow())) {
                // support MIF frame-to-frame messaging relay
                tag || (tag = message.tag || '');
                tag = tag.toLowerCase();
                domain = domain || document.domain;
                message = Ext.applyIf(message.data ? message : {
                            data : message
                        }, {
                            type : 'message',
                            domain : domain,
                            origin : L.protocol + '\/\/' + L.hostname,
                            uri : document.documentURI,
                            source : window,
                            tag : tag || null,
                            _fromHost : this
                        });
                return win.sendMessage ? win.sendMessage.call(null, message, tag, domain) : undefined;
            }
            return;
        },
        
        /**
         * Dispatch a cross-document message (per HTML5 specification) if the browser supports it natively.
         * @name postMessage
         * @memberOf Ext.ux.ManagedIFrame.Element
         * @param {String} message Required message payload (String only)
         * @param {String} origin Optional domain designation of the sender (defaults
         * to document.domain). 
         * <p>Notes:  on IE8, this action is synchronous.
         */
        postMessage : function(message, origin, target ){
            var d, w = target || this.getWindow();
            if(w && !this.disableMessaging){
	            origin = origin ||  location.protocol + '\/\/' + location.hostname;
                message = Ext.isObject(message) || Ext.isArray(message) ? Ext.encode(message) : message;
	            w.postMessage && w.postMessage(message, origin);
            }
        }
        
    });
    var MIF = Ext.ux.ManagedIFrame;
    Ext.each(['Component', 'Panel', 'Window'], 
	      function(K){
           MIF[K] && Ext.override(MIF[K] ,{
        	   
        	   disableMessaging : true,
                /**
                 * @memberOf Ext.ux.ManagedIFrame.ComponentAdapter
                 * @param {Mixed} message The message payload.  The payload can be any supported JS type. 
                 * @param {String} tag Optional reference tag 
                 * @param {String} origin Optional domain designation of the sender (defaults
                 * to document.domain).
                 */
	           sendMessage : function(){
                  this.getFrame() && this.frameEl.sendMessage.apply(this.frameEl, arguments);
               }
	       });
    });
    
    var MM = MIF.Manager;
    MM && Ext.apply(MM,{
    
        /**
         * @private
         * @memberOf Ext.ux.ManagedIFrame.ComponentAdapter
         */
        onMessage : function(e){
            
            var be = e.browserEvent;
            //Copy the relevant message members to the Ext.event
            try {
                var mif ;
                if (mif = (be && be.source && be.source.frameElement) ? be.source.frameElement.ownerCt : null){
                    if(mif){
                        e.stopEvent();
                        be && Ext.apply(e,{
			               origin      : be.origin,
			               data        : be.data,
			               lastEventId : be.lastEventId,
			               source      : be.source
			            });
                        mif && mif._observable.fireEvent('message', mif, e);
                    }
                }
            } catch (rhEx) {} 
        },
        /**
         * @private
         */
        destroy : MM.destroy.createSequence(function(){
             (window.postMessage || document.postMessage) &&
                Ext.EventManager.un(window, 'message', this.onMessage, this);  
        })
        
    });
    
     //Add support for postMessage message events if the browser supports it
    MM && (window.postMessage || document.postMessage) &&
        Ext.EventManager.on(window, 'message', MM.onMessage, MM);       
      
})();   

  /** @sourceURL=<mifmsg.js> */
Ext.provide && Ext.provide('mifmsg');
  