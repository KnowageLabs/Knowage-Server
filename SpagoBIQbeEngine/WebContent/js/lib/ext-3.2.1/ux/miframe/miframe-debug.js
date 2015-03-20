/*!
 * ux.ManagedIFrame for ExtJS Library 3.1+
 * Copyright(c) 2008-2009 Active Group, Inc.
 * licensing@theactivegroup.com
 * http://licensing.theactivegroup.com
 */
 /**
  * @class Ext.ux.plugin.VisibilityMode
  * @version 1.3.1
  * @author Doug Hendricks. doug[always-At]theactivegroup.com
  * @copyright 2007-2010, Active Group, Inc.  All rights reserved.
  * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a>
  * Commercial Developer License (CDL) is available at http://licensing.theactivegroup.com.
  * @singleton
  * @static
  * @desc This plugin provides an alternate mechanism for hiding Ext.Elements and a new hideMode for Ext.Components.<br />
  * <p>It is generally designed for use with all browsers <b>except</b> Internet Explorer, but may used on that Browser as well.
  * <p>If included in a Component as a plugin, it sets it's hideMode to 'nosize' and provides a new supported
  * CSS rule that sets the height and width of an element and all child elements to 0px (rather than
  * 'display:none', which causes DOM reflow to occur and re-initializes nested OBJECT, EMBED, and IFRAMES elements)
  * @example
   var div = Ext.get('container');
   new Ext.ux.plugin.VisibilityMode().extend(div);
   //You can override the Element (instance) visibilityCls to any className you wish at any time
   div.visibilityCls = 'my-hide-class';
   div.hide() //or div.setDisplayed(false);

   // In Ext Layouts:
   someContainer.add({
     xtype:'flashpanel',
     plugins: [new Ext.ux.plugin.VisibilityMode() ],
     ...
    });

   // or, Fix a specific Container only and all of it's child items:
   // Note: An upstream Container may still cause Reflow issues when hidden/collapsed

    var V = new Ext.ux.plugin.VisibilityMode({ bubble : false }) ;
    new Ext.TabPanel({
     plugins     : V,
     defaults    :{ plugins: V },
     items       :[....]
    });
  */

 Ext.namespace('Ext.ux.plugin');
 Ext.onReady(function(){

   /* This important rule solves many of the <object/iframe>.reInit issues encountered
    * when setting display:none on an upstream(parent) element (on all Browsers except IE).
    * This default rule enables the new Panel:hideMode 'nosize'. The rule is designed to
    * set height/width to 0 cia CSS if hidden or collapsed.
    * Additional selectors also hide 'x-panel-body's within layouts to prevent
    * container and <object, img, iframe> bleed-thru.
    */
    var CSS = Ext.util.CSS;
    if(CSS){
        CSS.getRule('.x-hide-nosize') || //already defined?
            CSS.createStyleSheet('.x-hide-nosize{height:0px!important;width:0px!important;border:none!important;zoom:1;}.x-hide-nosize * {height:0px!important;width:0px!important;border:none!important;zoom:1;}');
        CSS.refreshCache();
    }

});

(function(){

      var El = Ext.Element, A = Ext.lib.Anim, supr = El.prototype;
      var VISIBILITY = "visibility",
        DISPLAY = "display",
        HIDDEN = "hidden",
        NONE = "none";

      var fx = {};

      fx.El = {

            /**
             * Sets the CSS display property. Uses originalDisplay if the specified value is a boolean true.
             * @param {Mixed} value Boolean value to display the element using its default display, or a string to set the display directly.
             * @return {Ext.Element} this
             */
           setDisplayed : function(value) {
                var me=this;
                me.visibilityCls ? (me[value !== false ?'removeClass':'addClass'](me.visibilityCls)) :
                    supr.setDisplayed.call(me, value);
                return me;
            },

            /**
             * Returns true if display is not "none" or the visibilityCls has not been applied
             * @return {Boolean}
             */
            isDisplayed : function() {
                return !(this.hasClass(this.visibilityCls) || this.isStyle(DISPLAY, NONE));
            },
            // private
            fixDisplay : function(){
                var me = this;
                supr.fixDisplay.call(me);
                me.visibilityCls && me.removeClass(me.visibilityCls);
            },

            /**
             * Checks whether the element is currently visible using both visibility, display, and nosize class properties.
             * @param {Boolean} deep (optional) True to walk the dom and see if parent elements are hidden (defaults to false)
             * @return {Boolean} True if the element is currently visible, else false
             */
            isVisible : function(deep) {
                var vis = this.visible ||
                    (!this.isStyle(VISIBILITY, HIDDEN) &&
                        (this.visibilityCls ?
                            !this.hasClass(this.visibilityCls) :
                                !this.isStyle(DISPLAY, NONE))
                      );

                  if (deep !== true || !vis) {
                    return vis;
                  }

                  var p = this.dom.parentNode,
                      bodyRE = /^body/i;

                  while (p && !bodyRE.test(p.tagName)) {
                    if (!Ext.fly(p, '_isVisible').isVisible()) {
                      return false;
                    }
                    p = p.parentNode;
                  }
                  return true;

            },
            //Assert isStyle method for Ext 2.x
            isStyle: supr.isStyle || function(style, val) {
                return this.getStyle(style) == val;
            }

        };

 //Add basic capabilities to the Ext.Element.Flyweight class
 Ext.override(El.Flyweight, fx.El);

 Ext.ux.plugin.VisibilityMode = function(opt) {

    Ext.apply(this, opt||{});

    var CSS = Ext.util.CSS;

    if(CSS && !Ext.isIE && this.fixMaximizedWindow !== false && !Ext.ux.plugin.VisibilityMode.MaxWinFixed){
        //Prevent overflow:hidden (reflow) transitions when an Ext.Window is maximize.
        CSS.updateRule ( '.x-window-maximized-ct', 'overflow', '');
        Ext.ux.plugin.VisibilityMode.MaxWinFixed = true;  //only updates the CSS Rule once.
    }

   };


  Ext.extend(Ext.ux.plugin.VisibilityMode , Object, {

       /**
        * @cfg {Boolean} bubble If true, the VisibilityMode fixes are also applied to parent Containers which may also impact DOM reflow.
        * @default true
        */
      bubble              :  true,

      /**
      * @cfg {Boolean} fixMaximizedWindow If not false, the ext-all.css style rule 'x-window-maximized-ct' is disabled to <b>prevent</b> reflow
      * after overflow:hidden is applied to the document.body.
      * @default true
      */
      fixMaximizedWindow  :  true,

      /**
       *
       * @cfg {array} elements (optional) A list of additional named component members to also adjust visibility for.
       * <br />By default, the plugin handles most scenarios automatically.
       * @default null
       * @example ['bwrap','toptoolbar']
       */

      elements       :  null,

      /**
       * @cfg {String} visibilityCls A specific CSS classname to apply to Component element when hidden/made visible.
       * @default 'x-hide-nosize'
       */

      visibilityCls   : 'x-hide-nosize',

      /**
       * @cfg {String} hideMode A specific hideMode value to assign to affected Components.
       * @default 'nosize'
       */
      hideMode  :   'nosize' ,

      ptype     :  'uxvismode',
      /**
      * Component plugin initialization method.
      * @param {Ext.Component} c The Ext.Component (or subclass) for which to apply visibilityMode treatment
      */
      init : function(c) {

        var hideMode = this.hideMode || c.hideMode,
            plugin = this,
            bubble = Ext.Container.prototype.bubble,
            changeVis = function(){

                var els = [this.collapseEl, this.actionMode].concat(plugin.elements||[]);

                Ext.each(els, function(el){
                    plugin.extend( this[el] || el );
                },this);

                var cfg = {
                    visFixed  : true,
                    animCollapse : false,
                    animFloat   : false,
                    hideMode  : hideMode,
                    defaults  : this.defaults || {}
                };

                cfg.defaults.hideMode = hideMode;

                Ext.apply(this, cfg);
                Ext.apply(this.initialConfig || {}, cfg);

            };

         c.on('render', function(){

            // Bubble up the layout and set the new
            // visibility mode on parent containers
            // which might also cause DOM reflow when
            // hidden or collapsed.
            if(plugin.bubble !== false && this.ownerCt){

               bubble.call(this.ownerCt, function(){
                  this.visFixed || this.on('afterlayout', changeVis, this, {single:true} );
               });
             }

             changeVis.call(this);

          }, c, {single:true});

     },
     /**
      * @param {Element/Array} el The Ext.Element (or Array of Elements) to extend visibilityCls handling to.
      * @param {String} visibilityCls The className to apply to the Element when hidden.
      * @return this
      */
     extend : function(el, visibilityCls){
        el && Ext.each([].concat(el), function(e){

            if(e && e.dom){
                 if('visibilityCls' in e)return;  //already applied or defined?
                 Ext.apply(e, fx.El);
                 e.visibilityCls = visibilityCls || this.visibilityCls;
            }
        },this);
        return this;
     }

  });

  Ext.preg && Ext.preg('uxvismode', Ext.ux.plugin.VisibilityMode );
  /** @sourceURL=<uxvismode.js> */
  Ext.provide && Ext.provide('uxvismode');
})();/* global Ext El ElFrame ELD*/
/*
 * ******************************************************************************
 * This file is distributed on an AS IS BASIS WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * ***********************************************************************************
 * License: multidom.js is offered under an MIT License.
 * Donations are welcomed: http://donate.theactivegroup.com
 */

 /**
  * @class multidom
  * @version 2.11
  * @license MIT
  * @author Doug Hendricks. Forum ID: <a href="http://extjs.com/forum/member.php?u=8730">hendricd</a>
  * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
  * @copyright 2007-2010, Active Group, Inc. All rights reserved.
  * @description [Designed For Ext Core and ExtJs Frameworks (using ext-base adapter only) 3.0 or higher ONLY]
  * The multidom library extends (overloads) Ext Core DOM methods and functions to
  * provide document-targeted access to the documents loaded in external (FRAME/IFRAME)
  * documents.
  * <p>It maintains seperate DOM Element caches (and more) for each document instance encountered by the
  * framework, permitting safe access to DOM Elements across document instances that may share
  * the same Element id or name.  In essence, multidom extends the functionality provided by Ext Core
  * into any child document without having to load the Core library into the frame's global context.
  * <h3>Custom Element classes.</h3>
  * The Ext.get method is enhanced to support resolution of the custom Ext.Element implementations.
  * (The ux.ManagedIFrame 2 Element class is an example of such a class.)
  * <p>For example: If you were retrieving the Ext.Element instance for an IFRAME and the class
  * Ext.Element.IFRAME were defined:
  * <pre><code>Ext.get('myFrame')</pre></code>
  * would return an instance of Ext.Element.IFRAME for 'myFrame' if it were found.
  * @example
   // return the Ext.Element with an id 'someDiv' located in external document hosted by 'iframe'
   var iframe = Ext.get('myFrame');
   var div = Ext.get('someDiv', iframe.getFrameDocument()); //Firefox example
   if(div){
     div.center();
    }
   Note: ux.ManagedIFrame provides an equivalent 'get' method of it's own to access embedded DOM Elements
   for the document it manages.
   <pre><code>iframe.get('someDiv').center();</pre></code>

   Likewise, you can retrieve the raw Element of another document with:
   var el = Ext.getDom('myDiv', iframe.getFrameDocument());
 */

 (function(){

    /*
     * Ext.Element and Ext.lib.DOM enhancements.
     * Primarily provides the ability to interact with any document context
     * (not just the one Ext was loaded into).
     */
   var El = Ext.Element,
       ElFrame,
       ELD = Ext.lib.Dom,
       A = Ext.lib.Anim,
       Evm = Ext.EventManager,
       E = Ext.lib.Event,
       DOC = document,
       emptyFn = function(){},
       OP = Object.prototype,
       OPString = OP.toString,
       HTMLDoc = '[object HTMLDocument]';
       
   if(!Ext.elCache || parseInt( Ext.version.replace(/\./g,''),10) < 311 ) {
    alert ('Ext Release '+Ext.version+' is not supported');
   }

   /**
    * @private
    */
   Ext._documents= {}; 
   Ext._documents[Ext.id(document,'_doc')]=Ext.elCache;

    /**
    * @private
    * Resolve the Element cache for a given element/window/document context.
    */
    var resolveCache = ELD.resolveDocumentCache = function(el, cacheId){
        var doc = GETDOC(el),
            c = Ext.isDocument(doc) ? Ext.id(doc) : cacheId,
            cache = Ext._documents[c] || null, d, win;

         //see if the document instance is managed by FRAME
         if(!cache && doc && (win = doc.parentWindow || doc.defaultView)){  //Is it a frame document
              if(d = win.frameElement){
                   c = d.id || d.name;  //the id of the frame is the cacheKey
                }
         }
         return cache ||
            Ext._documents[c] ||
            (c ? Ext._documents[c] = {}: null);
     },
     clearCache = ELD.clearDocumentCache = function(cacheId){
       delete  Ext._documents[cacheId];
     };

   El.addMethods || ( El.addMethods = function(ov){ Ext.apply(El.prototype, ov||{}); });
   
   Ext.removeNode =  function(n){
         var dom = n ? n.dom || n : null;
         if(dom && dom.tagName != 'BODY'){
            var el, elc, elCache = resolveCache(dom), parent;

            //clear out any references if found in the El.cache(s)
            if((elc = elCache[dom.id]) && (el = elc.el) ){
                if(el.dom){
                    Ext.enableNestedListenerRemoval ? Evm.purgeElement(el.dom, true) : Evm.removeAll(el.dom);
                }
                delete elCache[dom.id];
                delete el.dom;
                delete el._context;
                el = null;
            }
            (parent = dom.parentElement || dom.parentNode) && parent.removeChild(dom);
            dom = null;
         }
    };

     var overload = function(pfn, fn ){
           var f = typeof pfn === 'function' ? pfn : function t(){};
           var ov = f._ovl; //call signature hash
           if(!ov){
               ov = { base: f};
               ov[f.length|| 0] = f;
               f= function t(){  //the proxy stub
                  var o = arguments.callee._ovl;
                  var fn = o[arguments.length] || o.base;
                  //recursion safety
                  return fn && fn != arguments.callee ? fn.apply(this,arguments): undefined;
               };
           }
           var fnA = [].concat(fn);
           for(var i=0,l=fnA.length; i<l; ++i){
             //ensures no duplicate call signatures, but last in rules!
             ov[fnA[i].length] = fnA[i];
           }
           f._ovl= ov;
           var t = null;
           return f;
       };

    Ext.applyIf( Ext, {
        overload : overload( overload,
           [
             function(fn){ return overload(null, fn);},
             function(obj, mname, fn){
                 return obj[mname] = overload(obj[mname],fn);}
          ]),

        isArray : function(v){
           return !!v && OPString.apply(v) == '[object Array]';
        },

        isObject:function(obj){
            return !!obj && typeof obj == 'object';
        },

        /**
         * HTMLDocument assertion with optional accessibility testing
         * @param {HTMLELement} el The DOM Element to test
         * @param {Boolean} testOrigin (optional) True to test "same-origin" access
         *
         */
        isDocument : function(el, testOrigin){
            var elm = el ? el.dom || el : null;
            var test = elm && ((OPString.apply(elm) == HTMLDoc) || (elm && elm.nodeType == 9));
            if(test && testOrigin){
                try{
                    test = !!elm.location;
                }
                catch(e){return false;}
            }
            return test;
        },

        isWindow : function(el){
          var elm = el ? el.dom || el : null;
          return elm ? !!elm.navigator || OPString.apply(elm) == "[object Window]" : false;
        },

        isIterable : function(v){
            //check for array or arguments
            if(Ext.isArray(v) || v.callee){
                return true;
            }
            //check for node list type
            if(/NodeList|HTMLCollection/.test(OPString.call(v))){
                return true;
            }
            //NodeList has an item and length property
            //IXMLDOMNodeList has nextNode method, needs to be checked first.
            return ((typeof v.nextNode != 'undefined' || v.item) && Ext.isNumber(v.length));
  
        },
        isElement : function(obj){
            return obj && Ext.type(obj)== 'element';
        },

        isEvent : function(obj){
            return OPString.apply(obj) == '[object Event]' || (Ext.isObject(obj) && !Ext.type(o.constructor) && (window.event && obj.clientX && obj.clientX == window.event.clientX));
        },

        isFunction: function(obj){
            return !!obj && typeof obj == 'function';
        },

        /**
         * Determine whether a specified DOMEvent is supported by a given HTMLElement or Object.
         * @param {String} type The eventName (without the 'on' prefix)
         * @param {HTMLElement/Object/String} testEl (optional) A specific HTMLElement/Object to test against, otherwise a tagName to test against.
         * based on the passed eventName is used, or DIV as default.
         * @return {Boolean} True if the passed object supports the named event.
         */
        isEventSupported : function(evName, testEl){
             var TAGNAMES = {
                  'select':'input',
                  'change':'input',
                  'submit':'form',
                  'reset':'form',
                  'load':'img',
                  'error':'img',
                  'abort':'img'
                },
                //Cached results
                cache = {},
                onPrefix = /^on/i,
                //Get a tokenized string of the form nodeName:type
                getKey = function(type, el){
                    var tEl = Ext.getDom(el);
                    return (tEl ?
                           (Ext.isElement(tEl) || Ext.isDocument(tEl) ?
                                tEl.nodeName.toLowerCase() :
                                    el.self ? '#window' : el || '#object')
                       : el || 'div') + ':' + type;
                };

            return function (evName, testEl) {
              evName = (evName || '').replace(onPrefix,'');
              var el, isSupported = false;
              var eventName = 'on' + evName;
              var tag = (testEl ? testEl : TAGNAMES[evName]) || 'div';
              var key = getKey(evName, tag);

              if(key in cache){
                //Use a previously cached result if available
                return cache[key];
              }

              el = Ext.isString(tag) ? DOC.createElement(tag): testEl;
              isSupported = (!!el && (eventName in el));

              isSupported || (isSupported = window.Event && !!(String(evName).toUpperCase() in window.Event));

              if (!isSupported && el) {
                el.setAttribute && el.setAttribute(eventName, 'return;');
                isSupported = Ext.isFunction(el[eventName]);
              }
              //save the cached result for future tests
              cache[key] = isSupported;
              el = null;
              return isSupported;
            };

        }()
    });


    /**
     * @private
     * Determine Ext.Element[tagName] or Ext.Element (default)
     */
    var assertClass = function(el){
    	
    	return El;
        return El[(el.tagName || '-').toUpperCase()] || El;

      };

    var libFlyweight;
    function fly(el, doc) {
        if (!libFlyweight) {
            libFlyweight = new Ext.Element.Flyweight();
        }
        libFlyweight.dom = Ext.getDom(el, null, doc);
        return libFlyweight;
    }


    Ext.apply(Ext, {
    /*
     * Overload Ext.get to permit Ext.Element access to other document objects
     * This implementation maintains safe element caches for each document queried.
     *
     */

      get : El.get = function(el, doc){         //document targeted
            if(!el ){ return null; }
            var isDoc = Ext.isDocument(el); 
            
            Ext.isDocument(doc) || (doc = DOC);
            
            var ex, elm, id, cache = resolveCache(doc);
            if(typeof el == "string"){ // element id
                elm = Ext.getDom(el, null, doc);
                if(!elm) return null;
                if(cache[el] && cache[el].el){
                    ex = cache[el].el;
                    ex.dom = elm;
                }else{
                    ex = El.addToCache(new (assertClass(elm))(elm, null, doc));
                }
                return ex;
            
            }else if(isDoc){

                if(!Ext.isDocument(el, true)){ return false; }  //is it accessible
                cache = resolveCache(el);

                if(cache[Ext.id(el)] && cache[el.id].el){
                    return cache[el.id].el;
                }
                // create a bogus element object representing the document object
                var f = function(){};
                f.prototype = El.prototype;
                var docEl = new f();
                docEl.dom = el;
                docEl.id = Ext.id(el,'_doc');
                docEl._isDoc = true;

                El.addToCache( docEl, null, cache);
                cache[docEl.id].skipGC = true;
                return docEl;
                        
             }else if( el instanceof El ){ 
                
                // refresh dom element in case no longer valid,
                // catch case where it hasn't been appended
                 
                if(el.dom){
                    el.id = Ext.id(el.dom);
                }else{
                    el.dom = el.id ? Ext.getDom(el.id, true) : null;
                }
                if(el.dom){
	                cache = resolveCache(el);
	                (cache[el.id] || 
	                       (cache[el.id] = {data : {}, events : {}}
	                       )).el = el; // in case it was created directly with Element(), let's cache it
                }
                return el;
                
            }else if(el.tagName || Ext.isWindow(el)){ // dom element
                cache = resolveCache(el);
                id = Ext.id(el);
                if(cache[id] && (ex = cache[id].el)){
                    ex.dom = el;
                }else{
                    ex = El.addToCache(new (assertClass(el))(el, null, doc), null, cache); 
                    el.navigator && (cache[id].skipGC = true);
                }
                return ex;

            }else if(el.isComposite){
                return el;

            }else if(Ext.isArray(el)){
                return Ext.get(doc,doc).select(el);
            }
           return null;

    },

     /**
      * Ext.getDom to support targeted document contexts
      */
     getDom : function(el, strict, doc){
        var D = doc || DOC;
        if(!el || !D){
            return null;
        }
        if (el.dom){
            return el.dom;
        } else {
            if (Ext.isString(el)) {
                var e = D.getElementById(el);
                // IE returns elements with the 'name' and 'id' attribute.
                // we do a strict check to return the element with only the id attribute
                if (e && Ext.isIE && strict) {
                    if (el == e.getAttribute('id')) {
                        return e;
                    } else {
                        return null;
                    }
                }
                return e;
            } else {
                return el;
            }
        }
            
     },
     /**
     * Returns the current/specified document body as an {@link Ext.Element}.
     * @param {HTMLDocument} doc (optional)
     * @return Ext.Element The document's body
     */
     getBody : function(doc){
            var D = ELD.getDocument(doc) || DOC;
            return Ext.get(D.body || D.documentElement);
       },

     getDoc :Ext.overload([
       Ext.getDoc,
       function(doc){ return Ext.get(doc,doc); }
       ])
   });

   // private method for getting and setting element data
    El.data = function(el, key, value){
        el = El.get(el);
        if (!el) {
            return null;
        }
        var c = resolveCache(el)[el.id].data;
        if(arguments.length == 2){
            return c[key];
        }else{
            return (c[key] = value);
        }
    };
    
    El.addToCache = function(el, id, cache ){
	    id = id || el.id;    
        var C = cache || resolveCache(el);
	    C[id] = {
	        el:  el,
	        data: {},
	        events: {}
	    };
	    return el;
	};
    
    /*
     * Add new Visibility Mode to element (sets height and width to 0px instead of display:none )
     */
    El.NOSIZE = 3;

    var propCache = {},
        camelRe = /(-[a-z])/gi,
        camelFn = function(m, a){ return a.charAt(1).toUpperCase(); },
        opacityRe = /alpha\(opacity=(.*)\)/i,
        trimRe = /^\s+|\s+$/g,
        marginRightRe = /marginRight/,
        propFloat = Ext.isIE ? 'styleFloat' : 'cssFloat',
        view = DOC.defaultView,
        VISMODE = 'visibilityMode',
        ELDISPLAY = El.DISPLAY,
        ELVISIBILITY = El.VISIBILITY,
        ELNOSIZE = El.NOSIZE,
        ORIGINALDISPLAY = 'originalDisplay',
        PADDING = "padding",
        MARGIN = "margin",
        BORDER = "border",
        LEFT = "-left",
        RIGHT = "-right",
        TOP = "-top",
        BOTTOM = "-bottom",
        WIDTH = "-width",
        MATH = Math,
        OPACITY = "opacity",
        VISIBILITY = "visibility",
        DISPLAY = "display",
        OFFSETS = "offsets",
        NOSIZE  = "nosize",
        HIDDEN = "hidden",
        NONE = "none", 
        ISVISIBLE = 'isVisible',
        ISCLIPPED = 'isClipped',
        OVERFLOW = 'overflow',
        OVERFLOWX = 'overflow-x',
        OVERFLOWY = 'overflow-y',
        ORIGINALCLIP = 'originalClip',
        XMASKED = "x-masked",
        XMASKEDRELATIVE = "x-masked-relative",
        // special markup used throughout Ext when box wrapping elements
        borders = {l: BORDER + LEFT + WIDTH, r: BORDER + RIGHT + WIDTH, t: BORDER + TOP + WIDTH, b: BORDER + BOTTOM + WIDTH},
        paddings = {l: PADDING + LEFT, r: PADDING + RIGHT, t: PADDING + TOP, b: PADDING + BOTTOM},
        margins = {l: MARGIN + LEFT, r: MARGIN + RIGHT, t: MARGIN + TOP, b: MARGIN + BOTTOM},
        data = El.data,
        GETDOM = Ext.getDom,
        GET = Ext.get,
        DH = Ext.DomHelper,
        propRe = /^(?:scope|delay|buffer|single|stopEvent|preventDefault|stopPropagation|normalized|args|delegate)$/,
        CSS = Ext.util.CSS,  //Not available in Ext Core.
        getDisplay = function(dom){
            var d = data(dom, ORIGINALDISPLAY);
            if(d === undefined){
                data(dom, ORIGINALDISPLAY, d = '');
            }
            return d;
        },
        getVisMode = function(dom){
            var m = data(dom, VISMODE);
            if(m === undefined){
                data(dom, VISMODE, m = 1)
            }
            return m;
        };

    function chkCache(prop) {
        return propCache[prop] || (propCache[prop] = prop == 'float' ? propFloat : prop.replace(camelRe, camelFn));
    };


    El.addMethods({
        /**
         * Resolves the current document context of this Element
         */
        getDocument : function(){
           return this._context || (this._context = GETDOC(this));
        },

        /**
      * Removes this element from the DOM and deletes it from the cache
      * @param {Boolean} cleanse (optional) Perform a cleanse of immediate childNodes as well.
      * @param {Boolean} deep (optional) Perform a deep cleanse of all nested childNodes as well.
      */

        remove : function(cleanse, deep){
            
          var dom = this.dom;
          this.isMasked() && this.unmask();
          if(dom){
            
            Ext.removeNode(dom);
            delete this._context;
            delete this.dom;
          }
        },

         /**
         * Appends the passed element(s) to this element
         * @param {String/HTMLElement/Array/Element/CompositeElement} el
         * @param {Document} doc (optional) specific document context for the Element search
         * @return {Ext.Element} this
         */
        appendChild: function(el, doc){
            return GET(el, doc || this.getDocument()).appendTo(this);
        },

        /**
         * Appends this element to the passed element
         * @param {Mixed} el The new parent element
         * @param {Document} doc (optional) specific document context for the Element search
         * @return {Ext.Element} this
         */
        appendTo: function(el, doc){
            GETDOM(el, false, doc || this.getDocument()).appendChild(this.dom);
            return this;
        },

        /**
         * Inserts this element before the passed element in the DOM
         * @param {Mixed} el The element before which this element will be inserted
         * @param {Document} doc (optional) specific document context for the Element search
         * @return {Ext.Element} this
         */
        insertBefore: function(el, doc){
            (el = GETDOM(el, false, doc || this.getDocument())).parentNode.insertBefore(this.dom, el);
            return this;
        },

        /**
         * Inserts this element after the passed element in the DOM
         * @param {Mixed} el The element to insert after
         * @param {Document} doc (optional) specific document context for the Element search
         * @return {Ext.Element} this
         */
        insertAfter: function(el, doc){
            (el = GETDOM(el, false, doc || this.getDocument())).parentNode.insertBefore(this.dom, el.nextSibling);
            return this;
        },

        /**
         * Inserts (or creates) an element (or DomHelper config) as the first child of this element
         * @param {Mixed/Object} el The id or element to insert or a DomHelper config to create and insert
         * @param {Document} doc (optional) specific document context for the Element search
         * @return {Ext.Element} The new child
         */
        insertFirst: function(el, returnDom){
            el = el || {};
            if(el.nodeType || el.dom || typeof el == 'string'){ // element
                el = GETDOM(el);
                this.dom.insertBefore(el, this.dom.firstChild);
                return !returnDom ? GET(el) : el;
            }else{ // dh config
                return this.createChild(el, this.dom.firstChild, returnDom);
            }
        },

        /**
         * Replaces the passed element with this element
         * @param {Mixed} el The element to replace
         * @param {Document} doc (optional) specific document context for the Element search
         * @return {Ext.Element} this
         */
        replace: function(el, doc){
            el = GET(el, doc || this.getDocument());
            this.insertBefore(el);
            el.remove();
            return this;
        },

        /**
         * Replaces this element with the passed element
         * @param {Mixed/Object} el The new element or a DomHelper config of an element to create
         * @param {Document} doc (optional) specific document context for the Element search
         * @return {Ext.Element} this
         */
        replaceWith: function(el, doc){
            var me = this;
            if(el.nodeType || el.dom || typeof el == 'string'){
                el = GETDOM(el, false, doc || me.getDocument());
                me.dom.parentNode.insertBefore(el, me.dom);
            }else{
                el = DH.insertBefore(me.dom, el);
            }
            var C = resolveCache(me);
            Ext.removeNode(me.dom);
            me.id = Ext.id(me.dom = el);

            El.addToCache(me.isFlyweight ? new (assertClass(me.dom))(me.dom, null, C) : me);     
            return me;
        },


        /**
         * Inserts an html fragment into this element
         * @param {String} where Where to insert the html in relation to this element - beforeBegin, afterBegin, beforeEnd, afterEnd.
         * @param {String} html The HTML fragment
         * @param {Boolean} returnEl (optional) True to return an Ext.Element (defaults to false)
         * @return {HTMLElement/Ext.Element} The inserted node (or nearest related if more than 1 inserted)
         */
        insertHtml : function(where, html, returnEl){
            var el = DH.insertHtml(where, this.dom, html);
            return returnEl ? Ext.get(el, GETDOC(el)) : el;
        },
        
        /**
         * Sets the element's visibility mode. When setVisible() is called it
         * will use this to determine whether to set the visibility or the display property.
         * @param {Number} visMode Ext.Element.VISIBILITY or Ext.Element.DISPLAY
         * @return {Ext.Element} this
         */
        setVisibilityMode : function(visMode){  
            data(this.dom, VISMODE, visMode);
            return this;
        },
        
        
        /**
         * Checks whether the element is currently visible using both visibility and display properties.
         * @return {Boolean} True if the element is currently visible, else false
         */
        isVisible : function() {
            return this.visible || Ext.value( data(this.dom, ISVISIBLE ), 
               !this.isStyle(VISIBILITY, HIDDEN) && !this.isStyle(DISPLAY, NONE));
        },
        
        /**
         * Sets the visibility of the element (see details). If the visibilityMode is set to Element.DISPLAY, it will use
         * the display property to hide the element, otherwise it uses visibility. The default is to hide and show using the visibility property.
         * @param {Boolean} visible Whether the element is visible
         * @param {Boolean/Object} animate (optional) True for the default animation, or a standard Element animation config object, or one of four
         *         possible hideMode strings: 'display, visibility, offsets, nosize'
         * @return {Ext.Element} this
         */
        setVisible : function(visible, animate){
            var me = this,
                dom = me.dom,
                isDisplay, isVisibility, isOffsets, isNosize;
            
            // hideMode string override
            if (typeof animate == 'string'){
                isDisplay = animate == DISPLAY;
                isVisibility = animate == VISIBILITY;
                isOffsets = animate == OFFSETS;
                isNosize  = animate == NOSIZE;
                animate = false;
            } else {
                var visMode = getVisMode(dom);
                isDisplay = visMode == ELDISPLAY;
                isVisibility = visMode == ELVISIBILITY;
                isNosize = visMode == ELNOSIZE;
            }
            
            if (!animate || !me.anim) {
                
                if (isNosize){
                    if (!visible){
                        me.hideModeStyles = {
                            width: me.getWidth(),
                            height: me.getHeight()
                        };

                        me.applyStyles({width: '0px', height: '0px'});
                    } else {
                        me.applyStyles(me.hideModeStyles || {width: 'auto', height: 'auto'});
                    }
                   
                } else if (isDisplay){
                    me.setDisplayed(visible);
                    
                } else if (isOffsets){
                    if (!visible){
                        me.hideModeStyles = {
                            position: me.getStyle('position'),
                            top: me.getStyle('top'),
                            left: me.getStyle('left')
                        };
                        me.applyStyles({position: 'absolute', top: '-10000px', left: '-10000px'});
                    } else {
                        me.applyStyles(me.hideModeStyles || {position: '', top: '', left: ''});
                    }
                
                }else{
                    me.fixDisplay();
                    dom.style.visibility = visible ? "visible" : HIDDEN;
                }
            }else{
                // closure for composites            
                if(visible){
                    me.setOpacity(.01);
                    me.setVisible(true);
                }
                me.anim({opacity: { to: (visible?1:0) }},
                        me.preanim(arguments, 1),
                        null,
                        .35,
                        'easeIn',
                        function(){
                             if(!visible){
                                 dom.style[isDisplay ? DISPLAY : VISIBILITY] = (isDisplay) ? NONE : HIDDEN;                     
                                 Ext.fly(dom).setOpacity(1);
                             }
                        });
            }
            data(dom, ISVISIBLE, visible);
            return me;
        },
        /**
         * Sets the CSS display property. Uses originalDisplay if the specified value is a boolean true.
         * @param {Mixed} value Boolean value to display the element using its default display, or a string to set the display directly.
         * @return {Ext.Element} this
         */
        setDisplayed : function(value) {            
            if(typeof value == "boolean"){
               data(this.dom, ISVISIBLE, value);
               value = value ? getDisplay(this.dom) : NONE;
            }
            this.setStyle(DISPLAY, value);
            return this;
        },
        
        // private
        fixDisplay : function(){
            var me = this;
           
            if(me.isStyle(DISPLAY, NONE)){
                me.setStyle(VISIBILITY, HIDDEN);
                me.setStyle(DISPLAY, getDisplay(me.dom)); // first try reverting to default
                if(me.isStyle(DISPLAY, NONE)){ // if that fails, default to block
                    me.setStyle(DISPLAY, "block");
                }
            }
            
        },
        
        /**
         * Convenience method for setVisibilityMode(Element.DISPLAY)
         * @param {String} display (optional) What to set display to when visible
         * @return {Ext.Element} this
         */
        enableDisplayMode : function(display){      
            this.setVisibilityMode(El.DISPLAY);
            if(!Ext.isEmpty(display)){
                data(this.dom, ORIGINALDISPLAY, display);
            }
            return this;
        },
        
        scrollIntoView : function(container, hscroll){
                var d = this.getDocument();
                var c = Ext.getDom(container, null, d) || Ext.getBody(d).dom;
                var el = this.dom;
                var o = this.getOffsetsTo(c),
                    s = this.getScroll(),
                    l = o[0] + s.left,
                    t = o[1] + s.top,
                    b = t + el.offsetHeight,
                    r = l + el.offsetWidth;
                var ch = c.clientHeight;
                var ct = parseInt(c.scrollTop, 10);
                var cl = parseInt(c.scrollLeft, 10);
                var cb = ct + ch;
                var cr = cl + c.clientWidth;
                if(el.offsetHeight > ch || t < ct){
                    c.scrollTop = t;
                }else if(b > cb){
                    c.scrollTop = b-ch;
                }
                c.scrollTop = c.scrollTop; // corrects IE, other browsers will ignore
                if(hscroll !== false){
                    if(el.offsetWidth > c.clientWidth || l < cl){
                        c.scrollLeft = l;
                    }else if(r > cr){
                        c.scrollLeft = r-c.clientWidth;
                    }
                    c.scrollLeft = c.scrollLeft;
                }
                return this;
        },

        contains : function(el){
            try {
                return !el ? false : ELD.isAncestor(this.dom, el.dom ? el.dom : el);
            } catch(e) {
                return false;
            }
        },

        /**
         * Returns the current scroll position of the element.
         * @return {Object} An object containing the scroll position in the format {left: (scrollLeft), top: (scrollTop)}
         */
        getScroll : function(){
            var d = this.dom,
            doc = this.getDocument(),
            body = doc.body,
            docElement = doc.documentElement,
            l,
            t,
            ret;

            if(Ext.isDocument(d) || d == body){
                if(Ext.isIE && ELD.docIsStrict(doc)){
                    l = docElement.scrollLeft;
                    t = docElement.scrollTop;
                }else{
                    l = window.pageXOffset;
                    t = window.pageYOffset;
                }
                ret = {left: l || (body ? body.scrollLeft : 0), top: t || (body ? body.scrollTop : 0)};
            }else{
                ret = {left: d.scrollLeft, top: d.scrollTop};
            }
            return ret;
        },
        /**
         * Normalizes currentStyle and computedStyle.
         * @param {String} property The style property whose value is returned.
         * @return {String} The current value of the style property for this element.
         */
        getStyle : function(){
            var getStyle =
             view && view.getComputedStyle ?
                function GS(prop){
                    var el = !this._isDoc ? this.dom : null,
                        v,
                        cs,
                        out,
                        display,
                        wk = Ext.isWebKit,
                        display;

                    if(!el || !el.style) return null;
                    prop = chkCache(prop);
                    
                    out =  (v = el.style[prop]) ? v :
                           (cs = view.getComputedStyle(el, '')) ? cs[prop] : null;
                           
                    // Fix bug caused by this: https://bugs.webkit.org/show_bug.cgi?id=13343
                    if(wk && marginRightRe.test(prop) && out != '0px'){
                        display = this.getStyle('display');
                        el.style.display = 'inline-block';
                        out = view.getComputedStyle(el, '');
                        el.style.display = display;
                    }
                    // Webkit returns rgb values for transparent.
                    if(wk && out == 'rgba(0, 0, 0, 0)'){
                        out = 'transparent';
                    }
                    return out;
                } :
                function GS(prop){ //IE
                   var el = !this._isDoc ? this.dom : null,
                        m,
                        cs;
                    if(!el || !el.style) return null;
                    if (prop == OPACITY) {
                        if (el.style.filter.match) {
                            if(m = el.style.filter.match(opacityRe)){
                                var fv = parseFloat(m[1]);
                                if(!isNaN(fv)){
                                    return fv ? fv / 100 : 0;
                                }
                            }
                        }
                        return 1;
                    }
                    prop = chkCache(prop);
                    return el.style[prop] || ((cs = el.currentStyle) ? cs[prop] : null);
                };
                var GS = null;
                return getStyle;
        }(),
        /**
         * Wrapper for setting style properties, also takes single object parameter of multiple styles.
         * @param {String/Object} property The style property to be set, or an object of multiple styles.
         * @param {String} value (optional) The value to apply to the given property, or null if an object was passed.
         * @return {Ext.Element} this
         */
        setStyle : function(prop, value){
            if(this._isDoc || Ext.isDocument(this.dom)) return this;
            var tmp,
                style,
                camel;
            if (!Ext.isObject(prop)) {
                tmp = {};
                tmp[prop] = value;
                prop = tmp;
            }
            for (style in prop) {
                value = prop[style];
                style == OPACITY ?
                    this.setOpacity(value) :
                    this.dom.style[chkCache(style)] = value;
            }
            return this;
        },
        /**
        * Centers the Element in either the viewport, or another Element.
        * @param {Mixed} centerIn (optional) The element in which to center the element.
        */
        center : function(centerIn){
            return this.alignTo(centerIn || this.getDocument(), 'c-c');
        },
        
        /**
         * Puts a mask over this element to disable user interaction. Requires core.css.
         * This method can only be applied to elements which accept child nodes.
         * @param {String} msg (optional) A message to display in the mask
         * @param {String} msgCls (optional) A css class to apply to the msg element
         * @return {Element} The mask element
         */
        mask : function(msg, msgCls){
            var me = this,
                dom = me.dom,
                dh = Ext.DomHelper,
                EXTELMASKMSG = "ext-el-mask-msg",
                el, 
                mask;
                
            if(me.getStyle("position") == "static"){
                me.addClass(XMASKEDRELATIVE);
            }
            if((el = data(dom, 'maskMsg'))){
                el.remove();
            }
            if((el = data(dom, 'mask'))){
                el.remove();
            }
    
            mask = dh.append(dom, {cls : "ext-el-mask"}, true);
            data(dom, 'mask', mask);
    
            me.addClass(XMASKED);
            mask.setDisplayed(true);
            if(typeof msg == 'string'){
                var mm = dh.append(dom, {cls : EXTELMASKMSG, cn:{tag:'div'}}, true);
                data(dom, 'maskMsg', mm);
                mm.dom.className = msgCls ? EXTELMASKMSG + " " + msgCls : EXTELMASKMSG;
                mm.dom.firstChild.innerHTML = msg;
                mm.setDisplayed(true);
                mm.center(me);
            }
            if(Ext.isIE && !(Ext.isIE7 && Ext.isStrict) && me.getStyle('height') == 'auto'){ // ie will not expand full height automatically
                mask.setSize(undefined, me.getHeight());
            }
            return mask;
        },
    
        /**
         * Removes a previously applied mask.
         */
        unmask : function(){
            var me = this,
                dom = me.dom,
                mask = data(dom, 'mask'),
                maskMsg = data(dom, 'maskMsg');
            if(mask){
                if(maskMsg){
                    maskMsg.remove();
                    data(dom, 'maskMsg', undefined);
                }
                mask.remove();
                data(dom, 'mask', undefined);
            }
            me.removeClass([XMASKED, XMASKEDRELATIVE]);
        },
        
        /**
         * Returns true if this element is masked
         * @return {Boolean}
         */
        isMasked : function(){
            var m = data(this.dom, 'mask');
            return m && m.isVisible();
        },

        /**
        * Calculates the x, y to center this element on the screen
        * @return {Array} The x, y values [x, y]
        */
        getCenterXY : function(){
            return this.getAlignToXY(this.getDocument(), 'c-c');
        },
        /**
         * Gets the x,y coordinates specified by the anchor position on the element.
         * @param {String} anchor (optional) The specified anchor position (defaults to "c").  See {@link #alignTo}
         * for details on supported anchor positions.
         * @param {Boolean} local (optional) True to get the local (element top/left-relative) anchor position instead
         * of page coordinates
         * @param {Object} size (optional) An object containing the size to use for calculating anchor position
         * {width: (target width), height: (target height)} (defaults to the element's current size)
         * @return {Array} [x, y] An array containing the element's x and y coordinates
         */
        getAnchorXY : function(anchor, local, s){
            //Passing a different size is useful for pre-calculating anchors,
            //especially for anchored animations that change the el size.
            anchor = (anchor || "tl").toLowerCase();
            s = s || {};

            var me = this,  doc = this.getDocument(),
                vp = me.dom == doc.body || me.dom == doc,
                w = s.width || vp ? ELD.getViewWidth(false,doc) : me.getWidth(),
                h = s.height || vp ? ELD.getViewHeight(false,doc) : me.getHeight(),
                xy,
                r = Math.round,
                o = me.getXY(),
                scroll = me.getScroll(),
                extraX = vp ? scroll.left : !local ? o[0] : 0,
                extraY = vp ? scroll.top : !local ? o[1] : 0,
                hash = {
                    c  : [r(w * .5), r(h * .5)],
                    t  : [r(w * .5), 0],
                    l  : [0, r(h * .5)],
                    r  : [w, r(h * .5)],
                    b  : [r(w * .5), h],
                    tl : [0, 0],
                    bl : [0, h],
                    br : [w, h],
                    tr : [w, 0]
                };

            xy = hash[anchor];
            return [xy[0] + extraX, xy[1] + extraY];
        },

        /**
         * Anchors an element to another element and realigns it when the window is resized.
         * @param {Mixed} element The element to align to.
         * @param {String} position The position to align to.
         * @param {Array} offsets (optional) Offset the positioning by [x, y]
         * @param {Boolean/Object} animate (optional) True for the default animation or a standard Element animation config object
         * @param {Boolean/Number} monitorScroll (optional) True to monitor body scroll and reposition. If this parameter
         * is a number, it is used as the buffer delay (defaults to 50ms).
         * @param {Function} callback The function to call after the animation finishes
         * @return {Ext.Element} this
         */
        anchorTo : function(el, alignment, offsets, animate, monitorScroll, callback){
            var me = this,
                dom = me.dom;

            function action(){
                fly(dom).alignTo(el, alignment, offsets, animate);
                Ext.callback(callback, fly(dom));
            };

            Ext.EventManager.onWindowResize(action, me);

            if(!Ext.isEmpty(monitorScroll)){
                Ext.EventManager.on(window, 'scroll', action, me,
                    {buffer: !isNaN(monitorScroll) ? monitorScroll : 50});
            }
            action.call(me); // align immediately
            return me;
        },

        /**
         * Returns the current scroll position of the element.
         * @return {Object} An object containing the scroll position in the format {left: (scrollLeft), top: (scrollTop)}
         */
        getScroll : function(){
            var d = this.dom,
                doc = this.getDocument(),
                body = doc.body,
                docElement = doc.documentElement,
                l,
                t,
                ret;

            if(d == doc || d == body){
                if(Ext.isIE && ELD.docIsStrict(doc)){
                    l = docElement.scrollLeft;
                    t = docElement.scrollTop;
                }else{
                    l = window.pageXOffset;
                    t = window.pageYOffset;
                }
                ret = {left: l || (body ? body.scrollLeft : 0), top: t || (body ? body.scrollTop : 0)};
            }else{
                ret = {left: d.scrollLeft, top: d.scrollTop};
            }
            return ret;
        },

        /**
         * Gets the x,y coordinates to align this element with another element. See {@link #alignTo} for more info on the
         * supported position values.
         * @param {Mixed} element The element to align to.
         * @param {String} position The position to align to.
         * @param {Array} offsets (optional) Offset the positioning by [x, y]
         * @return {Array} [x, y]
         */
        getAlignToXY : function(el, p, o){
            var doc;
            el = Ext.get(el, doc = this.getDocument());

            if(!el || !el.dom){
                throw "Element.getAlignToXY with an element that doesn't exist";
            }

            o = o || [0,0];
            p = (p == "?" ? "tl-bl?" : (!/-/.test(p) && p != "" ? "tl-" + p : p || "tl-bl")).toLowerCase();

            var me = this,
                d = me.dom,
                a1,
                a2,
                x,
                y,
                //constrain the aligned el to viewport if necessary
                w,
                h,
                r,
                dw = ELD.getViewWidth(false,doc) -10, // 10px of margin for ie
                dh = ELD.getViewHeight(false,doc)-10, // 10px of margin for ie
                p1y,
                p1x,
                p2y,
                p2x,
                swapY,
                swapX,
                docElement = doc.documentElement,
                docBody = doc.body,
                scrollX = (docElement.scrollLeft || docBody.scrollLeft || 0)+5,
                scrollY = (docElement.scrollTop || docBody.scrollTop || 0)+5,
                c = false, //constrain to viewport
                p1 = "",
                p2 = "",
                m = p.match(/^([a-z]+)-([a-z]+)(\?)?$/);

            if(!m){
               throw "Element.getAlignToXY with an invalid alignment " + p;
            }

            p1 = m[1];
            p2 = m[2];
            c = !!m[3];

            //Subtract the aligned el's internal xy from the target's offset xy
            //plus custom offset to get the aligned el's new offset xy
            a1 = me.getAnchorXY(p1, true);
            a2 = el.getAnchorXY(p2, false);

            x = a2[0] - a1[0] + o[0];
            y = a2[1] - a1[1] + o[1];

            if(c){
               w = me.getWidth();
               h = me.getHeight();
               r = el.getRegion();
               //If we are at a viewport boundary and the aligned el is anchored on a target border that is
               //perpendicular to the vp border, allow the aligned el to slide on that border,
               //otherwise swap the aligned el to the opposite border of the target.
               p1y = p1.charAt(0);
               p1x = p1.charAt(p1.length-1);
               p2y = p2.charAt(0);
               p2x = p2.charAt(p2.length-1);
               swapY = ((p1y=="t" && p2y=="b") || (p1y=="b" && p2y=="t"));
               swapX = ((p1x=="r" && p2x=="l") || (p1x=="l" && p2x=="r"));


               if (x + w > dw + scrollX) {
                    x = swapX ? r.left-w : dw+scrollX-w;
               }
               if (x < scrollX) {
                   x = swapX ? r.right : scrollX;
               }
               if (y + h > dh + scrollY) {
                    y = swapY ? r.top-h : dh+scrollY-h;
                }
               if (y < scrollY){
                   y = swapY ? r.bottom : scrollY;
               }
            }

            return [x,y];
        },
            // private ==>  used outside of core
        adjustForConstraints : function(xy, parent, offsets){
            return this.getConstrainToXY(parent || this.getDocument(), false, offsets, xy) ||  xy;
        },

        // private ==>  used outside of core
        getConstrainToXY : function(el, local, offsets, proposedXY){
            var os = {top:0, left:0, bottom:0, right: 0};

            return function(el, local, offsets, proposedXY){
                var doc = this.getDocument();
                el = Ext.get(el, doc);
                offsets = offsets ? Ext.applyIf(offsets, os) : os;

                var vw, vh, vx = 0, vy = 0;
                if(el.dom == doc.body || el.dom == doc){
                    vw = ELD.getViewWidth(false,doc);
                    vh = ELD.getViewHeight(false,doc);
                }else{
                    vw = el.dom.clientWidth;
                    vh = el.dom.clientHeight;
                    if(!local){
                        var vxy = el.getXY();
                        vx = vxy[0];
                        vy = vxy[1];
                    }
                }

                var s = el.getScroll();

                vx += offsets.left + s.left;
                vy += offsets.top + s.top;

                vw -= offsets.right;
                vh -= offsets.bottom;

                var vr = vx+vw;
                var vb = vy+vh;

                var xy = proposedXY || (!local ? this.getXY() : [this.getLeft(true), this.getTop(true)]);
                var x = xy[0], y = xy[1];
                var w = this.dom.offsetWidth, h = this.dom.offsetHeight;

                // only move it if it needs it
                var moved = false;

                // first validate right/bottom
                if((x + w) > vr){
                    x = vr - w;
                    moved = true;
                }
                if((y + h) > vb){
                    y = vb - h;
                    moved = true;
                }
                // then make sure top/left isn't negative
                if(x < vx){
                    x = vx;
                    moved = true;
                }
                if(y < vy){
                    y = vy;
                    moved = true;
                }
                return moved ? [x, y] : false;
            };
        }(),
        /**
        * Calculates the x, y to center this element on the screen
        * @return {Array} The x, y values [x, y]
        */
        getCenterXY : function(){
            return this.getAlignToXY(Ext.getBody(this.getDocument()), 'c-c');
        },
       
        /**
        * Centers the Element in either the viewport, or another Element.
        * @param {Mixed} centerIn (optional) The element in which to center the element.
        */
        center : function(centerIn){
            return this.alignTo(centerIn || Ext.getBody(this.getDocument()), 'c-c');
        } ,

        /**
         * Looks at this node and then at parent nodes for a match of the passed simple selector (e.g. div.some-class or span:first-child)
         * @param {String} selector The simple selector to test
         * @param {Number/Mixed} maxDepth (optional) The max depth to search as a number or element (defaults to 50 || document.body)
         * @param {Boolean} returnEl (optional) True to return a Ext.Element object instead of DOM node
         * @return {HTMLElement} The matching DOM node (or null if no match was found)
         */
        findParent : function(simpleSelector, maxDepth, returnEl){
            var p = this.dom,
                D = this.getDocument(),
                b = D.body,
                depth = 0,
                stopEl;
            if(Ext.isGecko && OPString.call(p) == '[object XULElement]') {
                return null;
            }
            maxDepth = maxDepth || 50;
            if (isNaN(maxDepth)) {
                stopEl = Ext.getDom(maxDepth, null, D);
                maxDepth = Number.MAX_VALUE;
            }
            while(p && p.nodeType == 1 && depth < maxDepth && p != b && p != stopEl){
                if(Ext.DomQuery.is(p, simpleSelector)){
                    return returnEl ? Ext.get(p, D) : p;
                }
                depth++;
                p = p.parentNode;
            }
            return null;
        },
        /**
         *  Store the current overflow setting and clip overflow on the element - use <tt>{@link #unclip}</tt> to remove
         * @return {Ext.Element} this
         */
        clip : function(){
            var me = this,
                dom = me.dom;
                
            if(!data(dom, ISCLIPPED)){
                data(dom, ISCLIPPED, true);
                data(dom, ORIGINALCLIP, {
                    o: me.getStyle(OVERFLOW),
                    x: me.getStyle(OVERFLOWX),
                    y: me.getStyle(OVERFLOWY)
                });
                me.setStyle(OVERFLOW, HIDDEN);
                me.setStyle(OVERFLOWX, HIDDEN);
                me.setStyle(OVERFLOWY, HIDDEN);
            }
            return me;
        },
    
        /**
         *  Return clipping (overflow) to original clipping before <tt>{@link #clip}</tt> was called
         * @return {Ext.Element} this
         */
        unclip : function(){
            var me = this,
                dom = me.dom;
                
            if(data(dom, ISCLIPPED)){
                data(dom, ISCLIPPED, false);
                var o = data(dom, ORIGINALCLIP);
                if(o.o){
                    me.setStyle(OVERFLOW, o.o);
                }
                if(o.x){
                    me.setStyle(OVERFLOWX, o.x);
                }
                if(o.y){
                    me.setStyle(OVERFLOWY, o.y);
                }
            }
            return me;
        },
        
        getViewSize : function(){
            var doc = this.getDocument(),
                d = this.dom,
                isDoc = (d == doc || d == doc.body);

            // If the body, use Ext.lib.Dom
            if (isDoc) {
                var extdom = Ext.lib.Dom;
                return {
                    width : extdom.getViewWidth(),
                    height : extdom.getViewHeight()
                }

            // Else use clientHeight/clientWidth
            } else {
                return {
                    width : d.clientWidth,
                    height : d.clientHeight
                }
            }
        },
        /**
        * <p>Returns the dimensions of the element available to lay content out in.<p>
        *
        * getStyleSize utilizes prefers style sizing if present, otherwise it chooses the larger of offsetHeight/clientHeight and offsetWidth/clientWidth.
        * To obtain the size excluding scrollbars, use getViewSize
        *
        * Sizing of the document body is handled at the adapter level which handles special cases for IE and strict modes, etc.
        */

        getStyleSize : function(){
            var me = this,
                w, h,
                doc = this.getDocument(),
                d = this.dom,
                isDoc = (d == doc || d == doc.body),
                s = d.style;

            // If the body, use Ext.lib.Dom
            if (isDoc) {
                var extdom = Ext.lib.Dom;
                return {
                    width : extdom.getViewWidth(),
                    height : extdom.getViewHeight()
                }
            }
            // Use Styles if they are set
            if(s.width && s.width != 'auto'){
                w = parseFloat(s.width);
                if(me.isBorderBox()){
                   w -= me.getFrameWidth('lr');
                }
            }
            // Use Styles if they are set
            if(s.height && s.height != 'auto'){
                h = parseFloat(s.height);
                if(me.isBorderBox()){
                   h -= me.getFrameWidth('tb');
                }
            }
            // Use getWidth/getHeight if style not set.
            return {width: w || me.getWidth(true), height: h || me.getHeight(true)};
        }
    });
    
    //Stop the existing collectorThread
    Ext.isDefined(El.collectorThreadId) && clearInterval(El.collectorThreadId);
    // private
	// Garbage collection - uncache elements/purge listeners on orphaned elements
	// so we don't hold a reference and cause the browser to retain them
	function garbageCollect(){
	    if(!Ext.enableGarbageCollector){
	        clearInterval(El.collectorThreadId);
	    } else {
	        var eid,
	            el,
	            d,
                o,
                EC = Ext.elCache;
	
	        for(eid in EC){
                o = EC[eid];
                if(o.skipGC){
	                continue;
	            }
	            el = o.el;
	            d = el.dom;
	            // -------------------------------------------------------
	            // Determining what is garbage:
	            // -------------------------------------------------------
	            // !d
	            // dom node is null, definitely garbage
	            // -------------------------------------------------------
	            // !d.parentNode
	            // no parentNode == direct orphan, definitely garbage
	            // -------------------------------------------------------
	            // !d.offsetParent && !document.getElementById(eid)
	            // display none elements have no offsetParent so we will
	            // also try to look it up by it's id. However, check
	            // offsetParent first so we don't do unneeded lookups.
	            // This enables collection of elements that are not orphans
	            // directly, but somewhere up the line they have an orphan
	            // parent.
	            // -------------------------------------------------------
	            
	            if(!d || !d.parentNode || (!d.offsetParent && !DOC.getElementById(eid))){
	                if(Ext.enableListenerCollection){
	                    Ext.EventManager.removeAll(d);
	                }
	                delete EC[eid];
	            }
	        
            }
	        // Cleanup IE COM Object Hash reference leaks 
	        if (Ext.isIE) {
	            var t = {};
	            for (eid in EC) {
	                t[eid] = EC[eid];
	            }
	            Ext.elCache = Ext._documents[Ext.id(document)] = t;
                t = null;
	        }
	    }
	}
    //Restart if enabled
    if(Ext.enableGarbageCollector){
	   El.collectorThreadId = setInterval(garbageCollect, 30000);
    }

    Ext.apply(ELD , {
        /**
         * Resolve the current document context of the passed Element
         */
        getDocument : function(el, accessTest){
          var dom= null;
          try{
            dom = Ext.getDom(el, null, null); //will fail if El.dom is non "same-origin" document
          }catch(ex){}

          var isDoc = Ext.isDocument(dom);
          if(isDoc){
            if(accessTest){
                return Ext.isDocument(dom, accessTest) ? dom : null;
            }
            return dom;
          }
          return dom ?
                dom.ownerDocument ||  //Element
                dom.document //Window
                : null;
        },

        /**
         * Return the Compatability Mode of the passed document or Element
         */
        docIsStrict : function(doc){
            return (Ext.isDocument(doc) ? doc : this.getDocument(doc)).compatMode == "CSS1Compat";
        },

        getViewWidth : Ext.overload ([
           ELD.getViewWidth || function(full){},
            function() { return this.getViewWidth(false);},
            function(full, doc) {
                return full ? this.getDocumentWidth(doc) : this.getViewportWidth(doc);
            }]
         ),

        getViewHeight : Ext.overload ([
            ELD.getViewHeight || function(full){},
            function() { return this.getViewHeight(false);},
            function(full, doc) {
                return full ? this.getDocumentHeight(doc) : this.getViewportHeight(doc);
            }]),

        getDocumentHeight: Ext.overload([
           ELD.getDocumentHeight || emptyFn,
           function(doc) {
            if(doc=this.getDocument(doc)){
              return Math.max(
                 !this.docIsStrict(doc) ? doc.body.scrollHeight : doc.documentElement.scrollHeight
                 , this.getViewportHeight(doc)
                 );
            }
            return undefined;
           }
         ]),

        getDocumentWidth: Ext.overload([
           ELD.getDocumentWidth || emptyFn,
           function(doc) {
              if(doc=this.getDocument(doc)){
                return Math.max(
                 !this.docIsStrict(doc) ? doc.body.scrollWidth : doc.documentElement.scrollWidth
                 , this.getViewportWidth(doc)
                 );
              }
              return undefined;
            }
        ]),

        getViewportHeight: Ext.overload([
           ELD.getViewportHeight || emptyFn,
           function(doc){
             if(doc=this.getDocument(doc)){
                if(Ext.isIE){
                    return this.docIsStrict(doc) ? doc.documentElement.clientHeight : doc.body.clientHeight;
                }else{
                    return doc.defaultView.innerHeight;
                }
             }
             return undefined;
           }
        ]),

        getViewportWidth: Ext.overload([
           ELD.getViewportWidth || emptyFn,
           function(doc) {
              if(doc=this.getDocument(doc)){
                return !this.docIsStrict(doc) && !Ext.isOpera ? doc.body.clientWidth :
                   Ext.isIE ? doc.documentElement.clientWidth : doc.defaultView.innerWidth;
              }
              return undefined;
            }
        ]),

        getXY : Ext.overload([
            ELD.getXY || emptyFn,
            function(el, doc) {

                el = Ext.getDom(el, null, doc);
                var D= this.getDocument(el),
                    bd = D ? (D.body || D.documentElement): null;

                if(!el || !bd || el == bd){ return [0, 0]; }
                return this.getXY(el);
            }
          ])
                
                
    });

    var GETDOC = ELD.getDocument,
        flies = El._flyweights;

    /**
     * @private
     * Add Ext.fly support for targeted document contexts
     */

    Ext.fly = El.fly = function(el, named, doc){
        var ret = null;
        named = named || '_global';

        if (el = Ext.getDom(el, null, doc)) {
            (ret = flies[named] = (flies[named] || new El.Flyweight())).dom = el;
            Ext.isDocument(el) && (ret._isDoc = true);
        }
        return ret;
    };

    var flyFn = function(){};
    flyFn.prototype = El.prototype;

    // dom is optional
    El.Flyweight = function(dom){
       this.dom = dom;
    };

    El.Flyweight.prototype = new flyFn();
    El.Flyweight.prototype.isFlyweight = true;
    
    function addListener(el, ename, fn, task, wrap, scope){
        el = Ext.getDom(el);
        if(!el){ return; }

        var id = Ext.id(el),
            es = (resolveCache(el)[id]||{}).events || {},
            wfn;

        wfn = E.on(el, ename, wrap);
        es[ename] = es[ename] || [];
        es[ename].push([fn, wrap, scope, wfn, task]);

        // this is a workaround for jQuery and should somehow be removed from Ext Core in the future
        // without breaking ExtJS.
        if(el.addEventListener && ename == "mousewheel" ){ 
            var args = ["DOMMouseScroll", wrap, false];
            el.addEventListener.apply(el, args);
            Ext.EventManager.addListener(window, 'beforeunload', function(){
                el.removeEventListener.apply(el, args);
            });
        }
        if(ename == "mousedown" && Ext.isDocument(el)){ // fix stopped mousedowns on the document
            Ext.EventManager.stoppedMouseDownEvent.addListener(wrap);
        }
    };

    function createTargeted(h, o){
        return function(){
            var args = Ext.toArray(arguments);
            if(o.target == Ext.EventObject.setEvent(args[0]).target){
                h.apply(this, args);
            }
        };
    };

    function createBuffered(h, o, task){
        return function(e){
            // create new event object impl so new events don't wipe out properties
            task.delay(o.buffer, h, null, [new Ext.EventObjectImpl(e)]);
        };
    };

    function createSingle(h, el, ename, fn, scope){
        return function(e){
            Ext.EventManager.removeListener(el, ename, fn, scope);
            h(e);
        };
    };

    function createDelayed(h, o, fn){
        return function(e){
            var task = new Ext.util.DelayedTask(h);
            (fn.tasks || (fn.tasks = [])).push(task);
            task.delay(o.delay || 10, h, null, [new Ext.EventObjectImpl(e)]);
        };
    };

    function listen(element, ename, opt, fn, scope){
        var o = !Ext.isObject(opt) ? {} : opt,
            el = Ext.getDom(element), task;

        fn = fn || o.fn;
        scope = scope || o.scope;

        if(!el){
            throw "Error listening for \"" + ename + '\". Element "' + element + '" doesn\'t exist.';
        }
        function h(e){
            // prevent errors while unload occurring
            if(!window.Ext){ return; }
            e = Ext.EventObject.setEvent(e);
            var t;
            if (o.delegate) {
                if(!(t = e.getTarget(o.delegate, el))){
                    return;
                }
            } else {
                t = e.target;
            }
            if (o.stopEvent) {
                e.stopEvent();
            }
            if (o.preventDefault) {
               e.preventDefault();
            }
            if (o.stopPropagation) {
                e.stopPropagation();
            }
            if (o.normalized) {
                e = e.browserEvent;
            }

            fn.call(scope || el, e, t, o);
        };
        if(o.target){
            h = createTargeted(h, o);
        }
        if(o.delay){
            h = createDelayed(h, o, fn);
        }
        if(o.single){
            h = createSingle(h, el, ename, fn, scope);
        }
        if(o.buffer){
            task = new Ext.util.DelayedTask(h);
            h = createBuffered(h, o, task);
        }

        addListener(el, ename, fn, task, h, scope);
        return h;
    };

    Ext.apply(Evm ,{
         addListener : Evm.on = function(element, eventName, fn, scope, options){
            if(Ext.isObject(eventName)){
                var o = eventName, e, val;
                for(e in o){
                    val = o[e];
                    if(!propRe.test(e)){
                        if(Ext.isFunction(val)){
                            // shared options
                            listen(element, e, o, val, o.scope);
                        }else{
                            // individual options
                            listen(element, e, val);
                        }
                    }
                }
            } else {
                listen(element, eventName, options, fn, scope);
            }
        },

        /**
         * Removes an event handler from an element.  The shorthand version {@link #un} is equivalent.  Typically
         * you will use {@link Ext.Element#removeListener} directly on an Element in favor of calling this version.
         * @param {String/HTMLElement} el The id or html element from which to remove the listener.
         * @param {String} eventName The name of the event.
         * @param {Function} fn The handler function to remove. <b>This must be a reference to the function passed into the {@link #addListener} call.</b>
         * @param {Object} scope If a scope (<b><code>this</code></b> reference) was specified when the listener was added,
         * then this must refer to the same object.
         */
        removeListener : Evm.un = function(element, eventName, fn, scope){
            var el = Ext.getDom(element);
            el && Ext.get(el);
            var elCache = el ? resolveCache(el) : {},
                f = el && ((elCache[el.id]||{events:{}}).events)[eventName] || [],
                wrap, i, l, k, len, fnc;

            for (i = 0, len = f.length; i < len; i++) {
                /* 0 = Original Function,
                   1 = Event Manager Wrapped Function,
                   2 = Scope,
                   3 = Adapter Wrapped Function,
                   4 = Buffered Task
                */
                if (Ext.isArray(fnc = f[i]) && fnc[0] == fn && (!scope || fnc[2] == scope)) {
                    fnc[4] && fnc[4].cancel();
                    k = fn.tasks && fn.tasks.length;
                    if(k) {
                        while(k--) {
                            fn.tasks[k].cancel();
                        }
                        delete fn.tasks;
                    }
                    wrap = fnc[1];
                    E.un(el, eventName, E.extAdapter ? fnc[3] : wrap);
                    
                    // jQuery workaround that should be removed from Ext Core
                    if(wrap && eventName == "mousewheel" && el.addEventListener ){
                        el.removeEventListener("DOMMouseScroll", wrap, false);
                    }
        
                    if(wrap && eventName == "mousedown" && Ext.isDocument(el)){ // fix stopped mousedowns on the document
                        Ext.EventManager.stoppedMouseDownEvent.removeListener(wrap);
                    }
                    
                    f.splice(i,1);
                    if (f.length === 0) {
                        delete elCache[el.id].events[eventName];
                    }
                    
                    for (k in elCache[el.id].events) {
                        return false;
                    }
                    elCache[el.id].events = {};
                    return false;
                }
            }

            
        },

        /**
         * Removes all event handers from an element.  Typically you will use {@link Ext.Element#removeAllListeners}
         * directly on an Element in favor of calling this version.
         * @param {String/HTMLElement} el The id or html element from which to remove all event handlers.
         */
        removeAll : function(el){
            if (!(el = Ext.getDom(el))) {
                return;
            }
            var id = el.id,
                elCache = resolveCache(el)||{},
                es = elCache[id] || {},
                ev = es.events || {},
                f, i, len, ename, fn, k, wrap;

            for(ename in ev){
                if(ev.hasOwnProperty(ename)){
                    f = ev[ename];
                    /* 0 = Original Function,
                       1 = Event Manager Wrapped Function,
                       2 = Scope,
                       3 = Adapter Wrapped Function,
                       4 = Buffered Task
                    */
                    for (i = 0, len = f.length; i < len; i++) {
                        fn = f[i];
                        fn[4] && fn[4].cancel();
                        if(fn[0].tasks && (k = fn[0].tasks.length)) {
                            while(k--) {
                                fn[0].tasks[k].cancel();
                            }
                            delete fn.tasks;
                        }
                        
                        wrap =  fn[1];
                        E.un(el, ename, E.extAdapter ? fn[3] : wrap);

                        // jQuery workaround that should be removed from Ext Core
                        if(wrap && el.addEventListener && ename == "mousewheel"){
                            el.removeEventListener("DOMMouseScroll", wrap, false);
                        }

                        // fix stopped mousedowns on the document
                        if(wrap && Ext.isDocument(el) &&  ename == "mousedown"){
                            Ext.EventManager.stoppedMouseDownEvent.removeListener(wrap);
                        }
                    }
                }
            }
            elCache[id] && (elCache[id].events = {});
        },

        getListeners : function(el, eventName) {
            el = Ext.getDom(el);
            if (!el) {
                return;
            }
            var id = (Ext.get(el)||{}).id,
                elCache = resolveCache(el),
                es = ( elCache[id] || {} ).events || {};

            return es[eventName] || null;
        },

        purgeElement : function(el, recurse, eventName) {
            el = Ext.getDom(el);
            var id = el.id,
                elCache = resolveCache(el),
                es = (elCache[id] || {}).events || {},
                i, f, len;
            if (eventName) {
                if (es.hasOwnProperty(eventName)) {
                    f = es[eventName];
                    for (i = 0, len = f.length; i < len; i++) {
                        Evm.removeListener(el, eventName, f[i][0]);
                    }
                }
            } else {
                Evm.removeAll(el);
            }
            if (recurse && el && el.childNodes) {
                for (i = 0, len = el.childNodes.length; i < len; i++) {
                    Evm.purgeElement(el.childNodes[i], recurse, eventName);
                }
            }
        }
    });
    
    // deprecated, call from EventManager
    E.getListeners = function(el, eventName) {
       return Ext.EventManager.getListeners(el, eventName);
    };

    /** @sourceURL=<multidom.js> */
    Ext.provide && Ext.provide('multidom');
 })();/* global Ext */
/*
 * Copyright 2007-2010, Active Group, Inc.  All rights reserved.
 * ******************************************************************************
 * This file is distributed on an AS IS BASIS WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * ***********************************************************************************
 * @version 2.1.2
 * [For Ext 3.1.1 or higher only]
 *
 * License: ux.ManagedIFrame, ux.ManagedIFrame.Panel, ux.ManagedIFrame.Portlet, ux.ManagedIFrame.Window  
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
    
    var El = Ext.Element, 
        ElFrame, 
        ELD = Ext.lib.Dom,
        EMPTYFN = function(){},
        OP = Object.prototype,
        addListener = function () {
            var handler;
            if (window.addEventListener) {
                handler = function F(el, eventName, fn, capture) {
                    el.addEventListener(eventName, fn, !!capture);
                };
            } else if (window.attachEvent) {
                handler = function F(el, eventName, fn, capture) {
                    el.attachEvent("on" + eventName, fn);
                };
            } else {
                handler = function F(){};
            }
            var F = null; //Gbg collect
            return handler;
        }(),
       removeListener = function() {
            var handler;
            if (window.removeEventListener) {
                handler = function F(el, eventName, fn, capture) {
                    el.removeEventListener(eventName, fn, (capture));
                };
            } else if (window.detachEvent) {
                handler = function F(el, eventName, fn) {
                    el.detachEvent("on" + eventName, fn);
                };
            } else {
                handler = function F(){};
            }
            var F = null; //Gbg collect
            return handler;
        }();
 
  //assert multidom support: REQUIRED for Ext 3 or higher!
  if(typeof ELD.getDocument != 'function'){
     alert("MIF 2.1.1 requires multidom support" );
  }
  //assert Ext 3.1.1+ 
  if(!Ext.elCache || parseInt( Ext.version.replace(/\./g,''),10) < 311 ) {
    alert ('Ext Release '+Ext.version+' is not supported');
   }
  
  Ext.ns('Ext.ux.ManagedIFrame', 'Ext.ux.plugin');
  
  var MIM, MIF = Ext.ux.ManagedIFrame, MIFC;
  var frameEvents = ['documentloaded',
                     'domready',
                     'focus',
                     'blur',
                     'resize',
                     'scroll',
                     'unload',
                     'scroll',
                     'exception', 
                     'message',
                     'reset'];
                     
    var reSynthEvents = new RegExp('^('+frameEvents.join('|')+ ')', 'i');

    /**
     * @class Ext.ux.ManagedIFrame.Element
     * @extends Ext.Element
     * @version 2.1.2 
     * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a> 
     * @author Doug Hendricks. Forum ID: <a href="http://extjs.com/forum/member.php?u=8730">hendricd</a> 
     * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
     * @copyright 2007-2010, Active Group, Inc. All rights reserved.
     * @constructor Create a new Ext.ux.ManagedIFrame.Element directly. 
     * @param {String/HTMLElement} element
     * @param {Boolean} forceNew (optional) By default the constructor checks to see if there is already an instance of this element in the cache and if there is it returns the same instance. This will skip that check (useful for extending this class).
     * @param {DocumentElement} (optional) Document context uses to resolve an Element search by its id.
     */
     
    Ext.ux.ManagedIFrame.Element = Ext.extend(Ext.Element, {
                         
            constructor : function(element, forceNew, doc ){
                var d = doc || document,
                	elCache  = ELD.resolveDocumentCache(d),
                    dom = Ext.getDom(element, false, d);
                
                if(!dom || !(/^(iframe|frame)/i).test(dom.tagName)) { // invalid id/element
                    return null;
                }
                var id = Ext.id(dom);
                
                /**
                 * The DOM element
                 * @type HTMLElement
                 */
                this.dom = dom;
                
                /**
                 * The DOM element ID
                 * @type String
                 */
                this.id = id ;
                
                (elCache[id] || 
                   (elCache[id] = {
                     el: this,
                     events : {},
                     data : {}
                    })
                ).el = this;
                
                this.dom.name || (this.dom.name = this.id);
                 
                if(Ext.isIE){
                     document.frames && (document.frames[this.dom.name] || (document.frames[this.dom.name] = this.dom));
                 }
                 
                this.dom.ownerCt = this;
                MIM.register(this);

                if(!this._observable){
	                    (this._observable = new Ext.util.Observable()).addEvents(
	                    
	                    /**
	                     * Fires when the iFrame has reached a loaded/complete state.
	                     * @event documentloaded
	                     * @param {Ext.ux.MIF.Element} this
	                     */
	                    'documentloaded',
	                    
	                    /**
	                     * Fires ONLY when an iFrame's Document(DOM) has reach a
	                     * state where the DOM may be manipulated ('same origin' policy)
	                     * Note: This event is only available when overwriting the iframe
	                     * document using the update or load methods and "same-origin"
	                     * documents. Returning false from the eventHandler stops further event
	                     * (documentloaded) processing.
	                     * @event domready 
	                     * @param {Ext.ux.MIF.Element} this
	                     */
	
	                    'domready',
	                    
	                    /**
	                     * Fires when the frame actions raise an error
	                     * @event exception
	                     * @param {Ext.ux.MIF.Element} this.iframe
	                     * @param {Error/string} exception
	                     */
	                     'exception',
	                     
	                    /**
	                     * Fires when the frame's window is resized.  This event, when raised from a "same-origin" frame,
	                     * will send current height/width reports with the event.
	                     * @event resize
	                     * @param {Ext.ux.MIF.Element} this.iframe
	                     * @param {Object} documentSize A height/width object signifying the new document size
	                     * @param {Object} viewPortSize A height/width object signifying the size of the frame's viewport
	                     * @param {Object} viewSize A height/width object signifying the size of the frame's view
	                     */
	                     'resize',
	                     
	                    /**
	                     * Fires upon receipt of a message generated by window.sendMessage
	                     * method of the embedded Iframe.window object
	                     * @event message
	                     * @param {Ext.ux.MIF} this.iframe
	                     * @param {object}
	                     *            message (members: type: {string} literal "message", data
	                     *            {Mixed} [the message payload], domain [the document domain
	                     *            from which the message originated ], uri {string} the
	                     *            document URI of the message sender source (Object) the
	                     *            window context of the message sender tag {string} optional
	                     *            reference tag sent by the message sender
	                     * <p>Alternate event handler syntax for message:tag filtering Fires upon
	                     * receipt of a message generated by window.sendMessage method which
	                     * includes a specific tag value of the embedded Iframe.window object
	                     */
	                    'message',
	
	                    /**
	                     * Fires when the frame is blurred (loses focus).
	                     * @event blur
	                     * @param {Ext.ux.MIF} this
	                     * @param {Ext.Event}
	                     *            Note: This event is only available when overwriting the
	                     *            iframe document using the update method and to pages
	                     *            retrieved from a "same domain". Returning false from the
	                     *            eventHandler [MAY] NOT cancel the event, as this event is
	                     *            NOT ALWAYS cancellable in all browsers.
	                     */
	                     'blur',
	
	                    /**
	                     * Fires when the frame gets focus. Note: This event is only available
	                     * when overwriting the iframe document using the update method and to
	                     * pages retrieved from a "same domain". Returning false from the
	                     * eventHandler [MAY] NOT cancel the event, as this event is NOT ALWAYS
	                     * cancellable in all browsers.
	                     * @event focus
	                     * @param {Ext.ux.MIF.Element} this
	                     * @param {Ext.Event}
	                     *
	                    */
	                    'focus',
	
	                    /**
	                     * Note: This event is only available when overwriting the iframe
	                     * document using the update method and to pages retrieved from a "same-origin"
	                     * domain. Note: Opera does not raise this event.
	                     * @event unload * Fires when(if) the frames window object raises the unload event
	                     * @param {Ext.ux.MIF.Element} this.
	                     * @param {Ext.Event}
	                     */
	                     'unload',
	                     
	                     /**
	                     * Note: This event is only available when overwriting the iframe
	                     * document using the update method and to pages retrieved from a "same-origin"
	                     * domain.  To prevent numerous scroll events from being raised use the buffer listener 
	                     * option to limit the number of times the event is raised.
	                     * @event scroll 
	                     * @param {Ext.ux.MIF.Element} this.
	                     * @param {Ext.Event}
	                     */
	                     'scroll',
	                     
	                    /**
	                     * Fires when the iFrame has been reset to a neutral domain state (blank document).
	                     * @event reset
	                     * @param {Ext.ux.MIF.Element} this
	                     */
	                    'reset'
	                 );
	                    //  Private internal document state events.
	                 this._observable.addEvents('_docready','_docload');
                 } 
                 var H = Ext.isIE?'onreadystatechange':'onload';
                 // Hook the Iframes loaded and error state handlers
                 this.dom[H] = this.loadHandler.createDelegate(this);
                 this.dom['onerror'] = this.loadHandler.createDelegate(this);
                
            },

            /** @private
             * Removes the MIFElement interface from the FRAME Element.
             * It does NOT remove the managed FRAME from the DOM.  Use the {@link Ext.#ux.ManagedIFrame.Element-remove} method to perfom both functions.
             */
            destructor   :  function () {
                this.dom[Ext.isIE?'onreadystatechange':'onload'] = this.dom['onerror'] = EMPTYFN;
                MIM.deRegister(this);
                this.removeAllListeners();
                Ext.destroy(this.frameShim, this.DDM);
                this.hideMask(true);
                delete this.loadMask;
                this.reset(); 
                this.manager = null;
                this.dom.ownerCt = null;
            },
            
            /**
             * Deep cleansing childNode Removal
             * @param {Boolean} forceReclean (optional) By default the element
             * keeps track if it has been cleansed already so
             * you can call this over and over. However, if you update the element and
             * need to force a reclean, you can pass true.
             * @param {Boolean} deep (optional) Perform a deep cleanse of all childNodes as well.
             */
            cleanse : function(forceReclean, deep){
                if(this.isCleansed && forceReclean !== true){
                    return this;
                }
                var d = this.dom, n = d.firstChild, nx;
                while(d && n){
                     nx = n.nextSibling;
                     deep && Ext.fly(n).cleanse(forceReclean, deep);
                     Ext.removeNode(n);
                     n = nx;
                }
                this.isCleansed = true;
                return this;
            },

            /** (read-only) The last known URI set programmatically by the Component
             * @property  
             * @type {String|Function}
             */
            src     : null,

            /** (read-only) For "same-origin" frames only.  Provides a reference to
             * the Ext.util.CSS singleton to manipulate the style sheets of the frame's
             * embedded document.
             *
             * @property
             * @type Ext.util.CSS
             */
            CSS     : null,

            /** Provides a reference to the managing Ext.ux.MIF.Manager instance.
             *
             * @property
             * @type Ext.ux.MIF.Manager
             */
            manager : null,

            /**
              * Enables/disables internal cross-frame messaging interface
              * @cfg {Boolean} disableMessaging False to enable cross-frame messaging API
              * Default = true
              *
              */
            disableMessaging  :  true,

             /**
              * Maximum number of domready event detection retries for IE.  IE does not provide
              * a native DOM event to signal when the frames DOM may be manipulated, so a polling process
              * is used to determine when the documents BODY is available. <p> Certain documents may not contain
              * a BODY tag:  eg. MHT(rfc/822), XML, or other non-HTML content. Detection polling will stop after this number of 2ms retries 
              * or when the documentloaded event is raised.</p>
              * @cfg {Integer} domReadyRetries 
              * @default 7500 (* 2 = 15 seconds) 
              */
            domReadyRetries   :  7500,
            
            /**
             * True to set focus on the frame Window as soon as its document
             * reports loaded.  <p>(Many external sites use IE's document.createRange to create 
             * DOM elements, but to be successful, IE requires that the FRAME have focus before
             * such methods are called)</p>
             * @cfg focusOnLoad
             * @default true if IE
             */
            focusOnLoad   : Ext.isIE,
            
            /**
              * Enables/disables internal cross-frame messaging interface
              * @cfg {Boolean} disableMessaging False to enable cross-frame messaging API
              * Default = true
              *
              */
            eventsFollowFrameLinks   : true,
           

            /**
             * Removes the FRAME from the DOM and deletes it from the cache
             */
            remove  : function(){
                this.destructor.apply(this, arguments);
                ElFrame.superclass.remove.apply(this,arguments);
            },
            
            /**
             * Return the ownerDocument property of the IFRAME Element.
             * (Note: This is not the document context of the FRAME's loaded document. 
             * See the getFrameDocument method for that.)
             */
            getDocument :  
                function(){ return this.dom ? this.dom.ownerDocument : document;},
            
            /**
	         * Loads the frame Element with the response from a form submit to the 
	         * specified URL with the ManagedIframe.Element as it's submit target.
	         *
	         * @param {Object} submitCfg A config object containing any of the following options:
	         * <pre><code>
	         *      myIframe.submitAsTarget({
	         *         form : formPanel.form,  //optional Ext.FormPanel, Ext form element, or HTMLFormElement
	         *         url: &quot;your-url.php&quot;,
             *         action : (see url) ,
	         *         params: {param1: &quot;foo&quot;, param2: &quot;bar&quot;}, // or URL encoded string or function that returns either
	         *         callback: yourFunction,  //optional, called with the signature (frame)
	         *         scope: yourObject, // optional scope for the callback
	         *         method: 'POST', //optional form.method 
             *         encoding : "multipart/form-data" //optional, default = HTMLForm default  
	         *      });
	         *
	         * </code></pre>
             * @return {Ext.ux.ManagedIFrame.Element} this
	         *
	         */
            submitAsTarget : function(submitCfg){
                var opt = submitCfg || {}, 
                D = this.getDocument(),
  	            form = Ext.getDom(
                       opt.form ? opt.form.form || opt.form: null, false, D) || 
                  Ext.DomHelper.append(D.body, { 
                    tag: 'form', 
                    cls : 'x-hidden x-mif-form',
                    encoding : 'multipart/form-data'
                  }),
                formFly = Ext.fly(form, '_dynaForm'),
                formState = {
                    target: form.target || '',
                    method: form.method || '',
                    encoding: form.encoding || '',
                    enctype: form.enctype || '',
                    action: form.action || '' 
                 },
                encoding = opt.encoding || form.encoding,
                method = opt.method || form.method || 'POST';
        
                formFly.set({
                   target  : this.dom.name,
                   method  : method,
                   encoding: encoding,
                   action  : opt.url || opt.action || form.action
                });
                
                if(method == 'POST' || !!opt.enctype){
                    formFly.set({enctype : opt.enctype || form.enctype || encoding});
                }
                
		        var hiddens, hd, ps;
                // add any additional dynamic params
		        if(opt.params && (ps = Ext.isFunction(opt.params) ? opt.params() : opt.params)){ 
		            hiddens = [];
                     
		            Ext.iterate(ps = typeof ps == 'string'? Ext.urlDecode(ps, false): ps, 
                        function(n, v){
		                    Ext.fly(hd = D.createElement('input')).set({
		                     type : 'hidden',
		                     name : n,
		                     value: v
                            });
		                    form.appendChild(hd);
		                    hiddens.push(hd);
		                });
		        }
		
		        opt.callback && 
                    this._observable.addListener('_docready',opt.callback, opt.scope,{single:true});
                     
                this._frameAction = true;
                this._targetURI = location.href;
		        this.showMask();
		        
		        //slight delay for masking
		        (function(){
                    
		            form.submit();
                    // remove dynamic inputs
		            hiddens && Ext.each(hiddens, Ext.removeNode, Ext);

                    //Remove if dynamically generated, restore state otherwise
		            if(formFly.hasClass('x-mif-form')){
                        formFly.remove();
                    }else{
                        formFly.set(formState);
                    }
                    delete El._flyweights['_dynaForm'];
                    formFly = null;
		            this.hideMask(true);
		        }).defer(100, this);
                
                return this;
		    },

            /**
             * @cfg {String} resetUrl Frame document reset string for use with the {@link #Ext.ux.ManagedIFrame.Element-reset} method.
             * Defaults:<p> For IE on SSL domains - the current value of Ext.SSL_SECURE_URL<p> "about:blank" for all others.
             */
            resetUrl : (function(){
                return Ext.isIE && Ext.isSecure ? Ext.SSL_SECURE_URL : 'about:blank';
            })(),

            /**
             * Sets the embedded Iframe src property. Note: invoke the function with
             * no arguments to refresh the iframe based on the current src value.
             *
             * @param {String/Function} url (Optional) A string or reference to a Function that
             *            returns a URI string when called
             * @param {Boolean} discardUrl (Optional) If not passed as <tt>false</tt>
             *            the URL of this action becomes the default SRC attribute
             *            for this iframe, and will be subsequently used in future
             *            setSrc calls (emulates autoRefresh by calling setSrc
             *            without params).
             * @param {Function} callback (Optional) A callback function invoked when the
             *            frame document has been fully loaded.
             * @param {Object} scope (Optional) scope by which the callback function is
             *            invoked.
             */
            setSrc : function(url, discardUrl, callback, scope) {
                var src = url || this.src || this.resetUrl;
                
                var O = this._observable;
                this._unHook();
                Ext.isFunction(callback) && O.addListener('_docload', callback, scope||this, {single:true});
                this.showMask();
                (discardUrl !== true) && (this.src = src);
                var s = this._targetURI = (Ext.isFunction(src) ? src() || '' : src);
                try {
                    this._frameAction = true; // signal listening now
                    this.dom.src = s;
                    this.checkDOM();
                } catch (ex) {
                    O.fireEvent.call(O, 'exception', this, ex);
                }
                return this;
            },

            /**
             * Sets the embedded Iframe location using its replace method (precluding a history update). 
             * Note: invoke the function with no arguments to refresh the iframe based on the current src value.
             *
             * @param {String/Function} url (Optional) A string or reference to a Function that
             *            returns a URI string when called
             * @param {Boolean} discardUrl (Optional) If not passed as <tt>false</tt>
             *            the URL of this action becomes the default SRC attribute
             *            for this iframe, and will be subsequently used in future
             *            setSrc calls (emulates autoRefresh by calling setSrc
             *            without params).
             * @param {Function} callback (Optional) A callback function invoked when the
             *            frame document has been fully loaded.
             * @param {Object} scope (Optional) scope by which the callback function is
             *            invoked.
             *
             */
            setLocation : function(url, discardUrl, callback, scope) {

                var src = url || this.src || this.resetUrl;
                var O = this._observable;
                this._unHook();
                Ext.isFunction(callback) && O.addListener('_docload', callback, scope||this, {single:true});
                this.showMask();
                var s = this._targetURI = (Ext.isFunction(src) ? src() || '' : src);
                if (discardUrl !== true) {
                    this.src = src;
                }
                try {
                    this._frameAction = true; // signal listening now
                    this.getWindow().location.replace(s);
                    this.checkDOM();
                } catch (ex) {
                    O.fireEvent.call(O,'exception', this, ex);
                }
                return this;
            },

            /**
             * Resets the frame to a neutral (blank document) state without
             * loadMasking.
             *
             * @param {String}
             *            src (Optional) A specific reset string (eg. 'about:blank')
             *            to use for resetting the frame.
             * @param {Function}
             *            callback (Optional) A callback function invoked when the
             *            frame reset is complete.
             * @param {Object}
             *            scope (Optional) scope by which the callback function is
             *            invoked.
             */
            reset : function(src, callback, scope) {
                
                this._unHook();
                var loadMaskOff = false,
                    s = src, 
                    win = this.getWindow(),
                    O = this._observable;
                    
                if(this.loadMask){
                    loadMaskOff = this.loadMask.disabled;
                    this.loadMask.disabled = false;
                 }
                this.hideMask(true);
                
                if(win){
                    this.isReset= true;
                    var cb = callback;
	                O.addListener('_docload',
	                  function(frame) {
	                    if(this.loadMask){
	                        this.loadMask.disabled = loadMaskOff;
	                    };
	                    Ext.isFunction(cb) &&  (cb = cb.apply(scope || this, arguments));
                        O.fireEvent("reset", this);
	                }, this, {single:true});
	            
                    Ext.isFunction(s) && ( s = src());
                    s = this._targetURI = Ext.isEmpty(s, true)? this.resetUrl: s;
                    win.location ? (win.location.href = s) : O.fireEvent('_docload', this);
                }
                
                return this;
            },

           /**
            * @private
            * Regular Expression filter pattern for script tag removal.
            * @cfg {regexp} scriptRE script removal RegeXp
            * Default: "/(?:<script.*?>)((\n|\r|.)*?)(?:<\/script>)/gi"
            */
            scriptRE : /(?:<script.*?>)((\n|\r|.)*?)(?:<\/script>)/gi,

            /**
             * Write(replacing) string content into the IFrames document structure
             * @param {String} content The new content
             * @param {Boolean} loadScripts
             * (optional) true to also render and process embedded scripts
             * @param {Function} callback (Optional) A callback function invoked when the
             * frame document has been written and fully loaded. @param {Object}
             * scope (Optional) scope by which the callback function is invoked.
             */
            update : function(content, loadScripts, callback, scope) {
                loadScripts = loadScripts || this.getUpdater().loadScripts || false;
                content = Ext.DomHelper.markup(content || '');
                content = loadScripts === true ? content : content.replace(this.scriptRE, "");
                var doc;
                if ((doc = this.getFrameDocument()) && !!content.length) {
                    this._unHook();
                    this.src = null;
                    this.showMask();
                    Ext.isFunction(callback) &&
                        this._observable.addListener('_docload', callback, scope||this, {single:true});
                    this._targetURI = location.href;
                    doc.open();
                    this._frameAction = true;
                    doc.write(content);
                    doc.close();
                    this.checkDOM();

                } else {
                    this.hideMask(true);
                    Ext.isFunction(callback) && callback.call(scope, this);
                }
                
                return this;
            },
            
            /**
             * Executes a Midas command on the current document, current selection, or the given range.
             * @param {String} command The command string to execute in the frame's document context.
             * @param {Booloean} userInterface (optional) True to enable user interface (if supported by the command)
             * @param {Mixed} value (optional)
             * @param {Boolean} validate If true, the command is validated to ensure it's invocation is permitted.
             * @return {Boolean} indication whether command execution succeeded
             */
            execCommand : function(command, userInterface, value, validate){
               var doc, assert;
               if ((doc = this.getFrameDocument()) && !!command) {
                  try{
                      Ext.isIE && this.getWindow().focus();
	                  assert = validate && Ext.isFunction(doc.queryCommandEnabled) ? 
	                    doc.queryCommandEnabled(command) : true;
                  
                      return assert && doc.execCommand(command, !!userInterface, value);
                  }catch(eex){return false;}
               }
               return false;
                
            },

            /**
             * Sets the current DesignMode attribute of the Frame's document
             * @param {Boolean/String} active True (or "on"), to enable designMode
             * 
             */
            setDesignMode : function(active){
               var doc;
               (doc = this.getFrameDocument()) && 
                 (doc.designMode = (/on|true/i).test(String(active))?'on':'off');
            },
            
            /**
            * Gets this element's Updater
            * 
            * @return {Ext.ux.ManagedIFrame.Updater} The Updater
            */
            getUpdater : function(){
               return this.updateManager || 
                    (this.updateManager = new MIF.Updater(this));
                
            },

            /**
             * Method to retrieve frame's history object.
             * @return {object} or null if permission was denied
             */
            getHistory  : function(){
                var h=null;
                try{ h=this.getWindow().history; }catch(eh){}
                return h;
            },
            
            /**
             * Method to retrieve embedded frame Element objects. Uses simple
             * caching (per frame) to consistently return the same object.
             * Automatically fixes if an object was recreated with the same id via
             * AJAX or DOM.
             *
             * @param {Mixed}
             *            el The id of the node, a DOM Node or an existing Element.
             * @return {Element} The Element object (or null if no matching element
             *         was found)
             */
            get : function(el) {
                var doc = this.getFrameDocument();
                return doc? Ext.get(el, doc) : doc=null;
            },

            /**
             * Gets the globally shared flyweight Element for the frame, with the
             * passed node as the active element. Do not store a reference to this
             * element - the dom node can be overwritten by other code.
             *
             * @param {String/HTMLElement}
             *            el The dom node or id
             * @param {String}
             *            named (optional) Allows for creation of named reusable
             *            flyweights to prevent conflicts (e.g. internally Ext uses
             *            "_internal")
             * @return {Element} The shared Element object (or null if no matching
             *         element was found)
             */
            fly : function(el, named) {
                var doc = this.getFrameDocument();
                return doc ? Ext.fly(el, named, doc) : null;
            },

            /**
             * Return the dom node for the passed string (id), dom node, or
             * Ext.Element relative to the embedded frame document context.
             *
             * @param {Mixed} el
             * @return HTMLElement
             */
            getDom : function(el) {
                var d;
                if (!el || !(d = this.getFrameDocument())) {
                    return (d=null);
                }
                return Ext.getDom(el, d);
            },
            
            /**
             * Creates a {@link Ext.CompositeElement} for child nodes based on the
             * passed CSS selector (the selector should not contain an id).
             *
             * @param {String} selector The CSS selector
             * @param {Boolean} unique (optional) True to create a unique Ext.Element for
             *            each child (defaults to false, which creates a single
             *            shared flyweight object)
             * @return {Ext.CompositeElement/Ext.CompositeElementLite} The composite element
             */
            select : function(selector, unique) {
                var d; return (d = this.getFrameDocument()) ? Ext.Element.select(selector,unique, d) : d=null;
            },

            /**
             * Selects frame document child nodes based on the passed CSS selector
             * (the selector should not contain an id).
             *
             * @param {String} selector The CSS selector
             * @return {Array} An array of the matched nodes
             */
            query : function(selector) {
                var d; return (d = this.getFrameDocument()) ? Ext.DomQuery.select(selector, d): null;
            },
            
            /**
             * Removes a DOM Element from the embedded document
             * @param {Element/String} node The node id or node Element to remove
             */
            removeNode : Ext.removeNode,
            
            /**
             * @private execScript sandbox and messaging interface
             */ 
            _renderHook : function() {
                this._windowContext = null;
                this.CSS = this.CSS ? this.CSS.destroy() : null;
                this._hooked = false;
                try {
                    if (this.writeScript('(function(){(window.hostMIF = parent.document.getElementById("'
                                    + this.id
                                    + '").ownerCt)._windowContext='
                                    + (Ext.isIE
                                            ? 'window'
                                            : '{eval:function(s){return new Function("return ("+s+")")();}}')
                                    + ';})()')) {
                        var w, p = this._frameProxy, D = this.getFrameDocument();
                        if(w = this.getWindow()){
                            p || (p = this._frameProxy = this._eventProxy.createDelegate(this));    
                            addListener(w, 'focus', p);
                            addListener(w, 'blur', p);
                            addListener(w, 'resize', p);
                            addListener(w, 'unload', p);
                            D && addListener(Ext.isIE ? w : D, 'scroll', p);
                        }
                        
                        D && (this.CSS = new Ext.ux.ManagedIFrame.CSS(D));
                       
                    }
                } catch (ex) {}
                return this.domWritable();
            },
            
             /** @private : clear all event listeners and Element cache */
            _unHook : function() {
                if (this._hooked) {
                    
                    this._windowContext && (this._windowContext.hostMIF = null);
                    this._windowContext = null;
                
                    var w, p = this._frameProxy;
                    if(p && this.domWritable() && (w = this.getWindow())){
                        removeListener(w, 'focus', p);
                        removeListener(w, 'blur', p);
                        removeListener(w, 'resize', p);
                        removeListener(w, 'unload', p);
                        removeListener(Ext.isIE ? w : this.getFrameDocument(), 'scroll', p);
                    }
                }
                
                ELD.clearDocumentCache && ELD.clearDocumentCache(this.id);
                this.CSS = this.CSS ? this.CSS.destroy() : null;
                this.domFired = this._frameAction = this.domReady = this._hooked = false;
            },
            
            /** @private */
            _windowContext : null,

            /**
             * If sufficient privilege exists, returns the frame's current document
             * as an HTMLElement.
             *
             * @return {HTMLElement} The frame document or false if access to document object was denied.
             */
            getFrameDocument : function() {
                var win = this.getWindow(), doc = null;
                try {
                    doc = (Ext.isIE && win ? win.document : null)
                            || this.dom.contentDocument
                            || window.frames[this.dom.name].document || null;
                } catch (gdEx) {
                    
                    ELD.clearDocumentCache && ELD.clearDocumentCache(this.id);
                    return false; // signifies probable access restriction
                }
                doc = (doc && Ext.isFunction(ELD.getDocument)) ? ELD.getDocument(doc,true) : doc;
                
                return doc;
            },

            /**
             * Returns the frame's current HTML document object as an
             * {@link Ext.Element}.
             * @return {Ext.Element} The document
             */
            getDoc : function() {
                var D = this.getFrameDocument();
                return Ext.get(D,D); 
            },
            
            /**
             * If sufficient privilege exists, returns the frame's current document
             * body as an HTMLElement.
             *
             * @return {HTMLElement} The frame document body or Null if access to
             *         document object was denied.
             */
            getBody : function() {
                var d;
                return (d = this.getFrameDocument()) ? this.get(d.body || d.documentElement) : null;
            },

            /**
             * Attempt to retrieve the frames current URI via frame's document object
             * @return {string} The frame document's current URI or the last know URI if permission was denied.
             */
            getDocumentURI : function() {
                var URI, d;
                try {
                    URI = this.src && (d = this.getFrameDocument()) ? d.location.href: null;
                } catch (ex) { // will fail on NON-same-origin domains
                }
                return URI || (Ext.isFunction(this.src) ? this.src() : this.src);
                // fallback to last known
            },

           /**
            * Attempt to retrieve the frames current URI via frame's Window object
            * @return {string} The frame document's current URI or the last know URI if permission was denied.
            */
            getWindowURI : function() {
                var URI, w;
                try {
                    URI = (w = this.getWindow()) ? w.location.href : null;
                } catch (ex) {
                } // will fail on NON-same-origin domains
                return URI || (Ext.isFunction(this.src) ? this.src() : this.src);
                // fallback to last known
            },

            /**
             * Returns the frame's current window object.
             *
             * @return {Window} The frame Window object.
             */
            getWindow : function() {
                var dom = this.dom, win = null;
                try {
                    win = dom.contentWindow || window.frames[dom.name] || null;
                } catch (gwEx) {}
                return win;
            },
            
            /**
             * Scrolls a frame document's child element into view within the passed container.
             * @param {String} child The id of the element to scroll into view. 
             * @param {Mixed} container (optional) The container element to scroll (defaults to the frame's document.body).  Should be a 
             * string (id), dom node, or Ext.Element.
             * @param {Boolean} hscroll (optional) False to disable horizontal scroll (defaults to true)
             * @return {Ext.ux.ManagedIFrame.Element} this 
             */ 
            scrollChildIntoView : function(child, container, hscroll){
                this.fly(child, '_scrollChildIntoView').scrollIntoView(this.getDom(container) || this.getBody().dom, hscroll);
                return this;
            },

            /**
             * Print the contents of the Iframes (if we own the document)
             * @return {Ext.ux.ManagedIFrame.Element} this 
             */
            print : function() {
                try {
                    var win;
                    if( win = this.getWindow()){
                        Ext.isIE && win.focus();
                        win.print();
                    }
                } catch (ex) {
                    throw new MIF.Error('printexception' , ex.description || ex.message || ex);
                }
                return this;
            },

            /**
             * Returns the general DOM modification capability (same-origin status) of the frame. 
             * @return {Boolean} accessible If True, the frame's inner DOM can be manipulated, queried, and
             * Event Listeners set.
             */
            domWritable : function() {
                return !!Ext.isDocument(this.getFrameDocument(),true) //test access
                    && !!this._windowContext;
            },

            /**
             * eval a javascript code block(string) within the context of the
             * Iframes' window object.
             * @param {String} block A valid ('eval'able) script source block.
             * @param {Boolean} useDOM  if true, inserts the function
             * into a dynamic script tag, false does a simple eval on the function
             * definition. (useful for debugging) <p> Note: will only work after a
             * successful iframe.(Updater) update or after same-domain document has
             * been hooked, otherwise an exception is raised.
             * @return {Mixed}  
             */
            execScript : function(block, useDOM) {
                try {
                    if (this.domWritable()) {
                        if (useDOM) {
                            this.writeScript(block);
                        } else {
                            return this._windowContext.eval(block);
                        }
                    } else {
                        throw new MIF.Error('execscript-secure-context');
                    }
                } catch (ex) {
                    this._observable.fireEvent.call(this._observable,'exception', this, ex);
                    return false;
                }
                return true;
            },

            /**
             * Write a script block into the iframe's document
             * @param {String} block A valid (executable) script source block.
             * @param {object} attributes Additional Script tag attributes to apply to the script
             * Element (for other language specs [vbscript, Javascript] etc.) <p>
             * Note: writeScript will only work after a successful iframe.(Updater)
             * update or after same-domain document has been hooked, otherwise an
             * exception is raised.
             */
            writeScript : function(block, attributes) {
                attributes = Ext.apply({}, attributes || {}, {
                            type : "text/javascript",
                            text : block
                        });
                try {
                    var head, script, doc = this.getFrameDocument();
                    if (doc && typeof doc.getElementsByTagName != 'undefined') {
                        if (!(head = doc.getElementsByTagName("head")[0])) {
                            // some browsers (Webkit, Safari) do not auto-create
                            // head elements during document.write
                            head = doc.createElement("head");
                            doc.getElementsByTagName("html")[0].appendChild(head);
                        }
                        if (head && (script = doc.createElement("script"))) {
                            for (var attrib in attributes) {
                                if (attributes.hasOwnProperty(attrib)
                                        && attrib in script) {
                                    script[attrib] = attributes[attrib];
                                }
                            }
                            return !!head.appendChild(script);
                        }
                    }
                } catch (ex) {
                    this._observable.fireEvent.call(this._observable, 'exception', this, ex);

                }finally{
                    script = head = null;
                }
                return false;
            },

            /**
             * Eval a function definition into the iframe window context.
             * @param {String/Object} fn Name of the function or function map
             * object: {name:'encodeHTML',fn:Ext.util.Format.htmlEncode}
             * @param {Boolean} useDOM  if true, inserts the fn into a dynamic script tag,
             * false does a simple eval on the function definition
             * @param {Boolean} invokeIt if true, the function specified is also executed in the
             * Window context of the frame. Function arguments are not supported.
             * @example <pre><code> var trim = function(s){ return s.replace(/^\s+|\s+$/g,''); }; 
             * iframe.loadFunction('trim');
             * iframe.loadFunction({name:'myTrim',fn:String.prototype.trim || trim});</code></pre>
             */
            loadFunction : function(fn, useDOM, invokeIt) {
                var name = fn.name || fn;
                var fnSrc = fn.fn || window[fn];
                name && fnSrc && this.execScript(name + '=' + fnSrc, useDOM); // fn.toString coercion
                invokeIt && this.execScript(name + '()'); // no args only
            },

            /**
             * @private
             * Evaluate the Iframes readyState/load event to determine its
             * 'load' state, and raise the 'domready/documentloaded' event when
             * applicable.
             */
            loadHandler : function(e, target) {
                
                var rstatus = (this.dom||{}).readyState || (e || {}).type ;
                
                if (this.eventsFollowFrameLinks || this._frameAction || this.isReset ) {
                                       
	                switch (rstatus) {
	                    case 'domready' : // MIF
                        case 'DOMFrameContentLoaded' :
	                    case 'domfail' : // MIF
	                        this._onDocReady (rstatus);
	                        break;
	                    case 'load' : // Gecko, Opera, IE
	                    case 'complete' :
	                        this._onDocLoaded(rstatus);
	                        break;
	                    case 'error':
	                        this._observable.fireEvent.apply(this._observable,['exception', this].concat(arguments));
	                        break;
	                    default :
	                }
                    this.frameState = rstatus;
                }
                
            },

            /**
             * @private
             * @param {String} eventName
             */
            _onDocReady  : function(eventName ){
                var w, obv = this._observable, D;
                if(!this.isReset && this.focusOnLoad && (w = this.getWindow())){
                    w.focus();
                }
                //raise internal event regardless of state.
                obv.fireEvent("_docready", this);
                
                (D = this.getDoc()) && (D.isReady = true);
               
                if ( !this.domFired && 
                     (this._hooked = this._renderHook())) {
                        // Only raise if sandBox injection succeeded (same origin)
                        this.domFired = true;
                        this.isReset || obv.fireEvent.call(obv, 'domready', this);
                }
                
                this.domReady = true;
                this.hideMask();
            },

            /**
             * @private
             * @param {String} eventName
             */
            _onDocLoaded  : function(eventName ){
                var obv = this._observable, w;
                this.domReady || this._onDocReady('domready');
                
                obv.fireEvent("_docload", this);  //invoke any callbacks
                this.isReset || obv.fireEvent("documentloaded", this);
                this.hideMask(true);
                this._frameAction = this.isReset = false;
            },

            /**
             * @private
             * Poll the Iframes document structure to determine DOM ready
             * state, and raise the 'domready' event when applicable.
             */
            checkDOM : function( win) {
                if ( Ext.isGecko ) { return; } 
                // initialise the counter
                var n = 0, frame = this, domReady = false,
                    b, l, d, 
                    max = this.domReadyRetries || 2500, //default max 5 seconds 
                    polling = false,
                    startLocation = (this.getFrameDocument() || {location : {}}).location.href;
                (function() { // DOM polling for IE and others
                    d = frame.getFrameDocument() || {location : {}};
                    // wait for location.href transition
                    polling = (d.location.href !== startLocation || d.location.href === frame._targetURI);
                    if ( frame.domReady) { return;}
                    domReady = polling && ((b = frame.getBody()) && !!(b.dom.innerHTML || '').length) || false;
                    // null href is a 'same-origin' document access violation,
                    // so we assume the DOM is built when the browser updates it
                    if (d.location.href && !domReady && (++n < max)) {
                        setTimeout(arguments.callee, 2); // try again
                        return;
                    }
                    frame.loadHandler({ type : domReady ? 'domready' : 'domfail'});
                })();
            },
            
            /**
            * @private 
            */
            filterEventOptionsRe: /^(?:scope|delay|buffer|single|stopEvent|preventDefault|stopPropagation|normalized|args|delegate)$/,

           /**
            * @private override to handle synthetic events vs DOM events
            */
            addListener : function(eventName, fn, scope, options){

                if(typeof eventName == "object"){
                    var o = eventName;
                    for(var e in o){
                        if(this.filterEventOptionsRe.test(e)){
                            continue;
                        }
                        if(typeof o[e] == "function"){
                            // shared options
                            this.addListener(e, o[e], o.scope,  o);
                        }else{
                            // individual options
                            this.addListener(e, o[e].fn, o[e].scope, o[e]);
                        }
                    }
                    return;
                }

                if(reSynthEvents.test(eventName)){
                    var O = this._observable; 
                    if(O){
                        O.events[eventName] || (O.addEvents(eventName)); 
                        O.addListener.call(O, eventName, fn, scope || this, options) ;}
                }else {
                    ElFrame.superclass.addListener.call(this, eventName,
                            fn, scope || this, options);
                }
                return this;
            },

            /**
             * @private override
             * Removes an event handler from this element.
             */
            removeListener : function(eventName, fn, scope){
                var O = this._observable;
                if(reSynthEvents.test(eventName)){
                    O && O.removeListener.call(O, eventName, fn, scope || this, options);
                }else {
                  ElFrame.superclass.removeListener.call(this, eventName, fn, scope || this);
              }
              return this;
            },

            /**
             * Removes all previous added listeners from this element
             * @private override
             */
            removeAllListeners : function(){
                Ext.EventManager.removeAll(this.dom);
                var O = this._observable;
                O && O.purgeListeners.call(this._observable);
                return this;
            },
            
            /**
             * Forcefully show the defined loadMask
             * @param {String} msg Mask text to display during the mask operation, defaults to previous defined
             * loadMask config value.
             * @param {String} msgCls The CSS class to apply to the loading message element (defaults to "x-mask-loading")
             * @param {String} maskCls The CSS class to apply to the mask element
             */
            showMask : function(msg, msgCls, maskCls) {
                var lmask = this.loadMask;
                if (lmask && !lmask.disabled ){
                    this.mask(msg || lmask.msg, msgCls || lmask.msgCls, maskCls || lmask.maskCls, lmask.maskEl);
                }
            },
            
            /**
             * Hide the defined loadMask 
             * @param {Boolean} forced True to hide the mask regardless of document ready/loaded state.
             */
            hideMask : function(forced) {
                var tlm = this.loadMask || {};
                if (forced || (tlm.hideOnReady && this.domReady)) {
                     this.unmask();
                }
            },
            
            /**
             * Puts a mask over the FRAME to disable user interaction. Requires core.css.
             * @param {String} msg (optional) A message to display in the mask
             * @param {String} msgCls (optional) A css class to apply to the msg element
             * @param {String} maskCls (optional) A css class to apply to the mask element
             * @param {String/Element} maskEl (optional) A targeted Element (parent of the IFRAME) to use the masking agent
             * @return {Element} The mask element
             */
            mask : function(msg, msgCls, maskCls, maskEl){
                this._mask && this.unmask();
                var p = Ext.get(maskEl) || this.parent('.ux-mif-mask-target') || this.parent();
                if(p.getStyle("position") == "static" && 
                    !p.select('iframe,frame,object,embed').elements.length){
                        p.addClass("x-masked-relative");
                }
                
                p.addClass("x-masked");
                
                this._mask = Ext.DomHelper.append(p, {cls: maskCls || "ux-mif-el-mask"} , true);
                this._mask.setDisplayed(true);
                this._mask._agent = p;
                
                if(typeof msg == 'string'){
                     this._maskMsg = Ext.DomHelper.append(p, {cls: msgCls || "ux-mif-el-mask-msg" , style: {visibility:'hidden'}, cn:{tag:'div', html:msg}}, true);
                     this._maskMsg
                        .setVisibilityMode(Ext.Element.VISIBILITY)
                        .center(p).setVisible(true);
                }
                if(Ext.isIE && !(Ext.isIE7 && Ext.isStrict) && this.getStyle('height') == 'auto'){ // ie will not expand full height automatically
                    this._mask.setSize(undefined, this._mask.getHeight());
                }
                return this._mask;
            },

            /**
             * Removes a previously applied mask.
             */
            unmask : function(){
                
                var a;
                if(this._mask){
                    (a = this._mask._agent) && a.removeClass(["x-masked-relative","x-masked"]);
                    if(this._maskMsg){
                        this._maskMsg.remove();
                        delete this._maskMsg;
                    }
                    this._mask.remove();
                    delete this._mask;
                }
             },

             /**
              * Creates an (frontal) transparent shim agent for the frame.  Used primarily for masking the frame during drag operations.
              * @return {Ext.Element} The new shim element.
              * @param {String} imgUrl Optional Url of image source to use during shimming (defaults to Ext.BLANK_IMAGE_URL).
              * @param {String} shimCls Optional CSS style selector for the shimming agent. (defaults to 'ux-mif-shim' ).
              * @return (HTMLElement} the shim element
              */
             createFrameShim : function(imgUrl, shimCls ){
                 this.shimCls = shimCls || this.shimCls || 'ux-mif-shim';
                 this.frameShim || (this.frameShim = this.next('.'+this.shimCls) ||  //already there ?
                  Ext.DomHelper.append(
                     this.dom.parentNode,{
                         tag : 'img',
                         src : imgUrl|| Ext.BLANK_IMAGE_URL,
                         cls : this.shimCls ,
                         galleryimg : "no"
                    }, true)) ;
                 this.frameShim && (this.frameShim.autoBoxAdjust = false); 
                 return this.frameShim;
             },
             
             /**
              * Toggles visibility of the (frontal) transparent shim agent for the frame.  Used primarily for masking the frame during drag operations.
              * @param {Boolean} show Optional True to activate the shim, false to hide the shim agent.
              */
             toggleShim : function(show){
                var shim = this.frameShim || this.createFrameShim();
                var cls = this.shimCls + '-on';
                !show && shim.removeClass(cls);
                show && !shim.hasClass(cls) && shim.addClass(cls);
             },

            /**
             * Loads this panel's iframe immediately with content returned from an XHR call.
             * @param {Object/String/Function} config A config object containing any of the following options:
             * <pre><code>
             *      frame.load({
             *         url: &quot;your-url.php&quot;,
             *         params: {param1: &quot;foo&quot;, param2: &quot;bar&quot;}, // or encoded string
             *         callback: yourFunction,
             *         scope: yourObject, // optional scope for the callback
             *         discardUrl: false,
             *         nocache: false,
             *         text: &quot;Loading...&quot;,
             *         timeout: 30,
             *         scripts: false,
             *         //optional custom renderer
             *         renderer:{render:function(el, response, updater, callback){....}}  
             *      });
             * </code></pre>
             * The only required property is url. The optional properties
             *            nocache, text and scripts are shorthand for
             *            disableCaching, indicatorText and loadScripts and are used
             *            to set their associated property on this panel Updater
             *            instance.
             * @return {Ext.ManagedIFrame.Element} this
             */
            load : function(loadCfg) {
                var um;
                if (um = this.getUpdater()) {
                    if (loadCfg && loadCfg.renderer) {
                        um.setRenderer(loadCfg.renderer);
                        delete loadCfg.renderer;
                    }
                    um.update.apply(um, arguments);
                }
                return this;
            },

             /** @private
              * Frame document event proxy
              */
             _eventProxy : function(e) {
                 if (!e) return;
                 e = Ext.EventObject.setEvent(e);
                 var be = e.browserEvent || e, er, args = [e.type, this];
                 
                 if (!be['eventPhase']
                         || (be['eventPhase'] == (be['AT_TARGET'] || 2))) {
                            
                     if(e.type == 'resize'){
	                    var doc = this.getFrameDocument();
	                    doc && (args.push(
	                        { height: ELD.getDocumentHeight(doc), width : ELD.getDocumentWidth(doc) },
	                        { height: ELD.getViewportHeight(doc), width : ELD.getViewportWidth(doc) },
	                        { height: ELD.getViewHeight(false, doc), width : ELD.getViewWidth(false, doc) }
	                      ));  
	                 }
                     
                     er =  this._observable ? 
                           this._observable.fireEvent.apply(this._observable, args.concat(
                              Array.prototype.slice.call(arguments,0))) 
                           : null;
                 
	                 // same-domain unloads should clear ElCache for use with the
	                 // next document rendering
	                 (e.type == 'unload') && this._unHook();
                     
                 }
                 return er;
            },
            
            /**
	         * dispatch a message to the embedded frame-window context (same-origin frames only)
	         * @name sendMessage
	         * @param {Mixed} message The message payload.  The payload can be any supported JS type. 
	         * @param {String} tag Optional reference tag 
	         * @param {String} origin Optional domain designation of the sender (defaults
	         * to document.domain).
	         */
	        sendMessage : function(message, tag, origin) {
	          //(implemented by mifmsg.js )
	        },
            
            /**
	         * Dispatch a cross-document message (per HTML5 specification) if the browser supports it natively.
	         * @name postMessage
	         * @param {String} message Required message payload (String only)
	         * @param {Array} ports Optional array of ports/channels. 
	         * @param {String} origin Optional domain designation of the sender (defaults
	         * to document.domain). 
	         * <p>Notes:  on IE8, this action is synchronous.
	         */
	        postMessage : function(message ,ports ,origin ){
	            //(implemented by mifmsg.js )
	        }

    });
   
    ElFrame = Ext.Element.IFRAME = Ext.Element.FRAME = Ext.ux.ManagedIFrame.Element;
    
      
    var fp = ElFrame.prototype;
    /**
     * @ignore
     */
    Ext.override ( ElFrame , {
          
    /**
     * Appends an event handler (shorthand for {@link #addListener}).
     * @param {String} eventName The type of event to handle
     * @param {Function} fn The handler function the event invokes
     * @param {Object} scope (optional) The scope (this element) of the handler function
     * @param {Object} options (optional) An object containing standard {@link #addListener} options
     * @member Ext.Element
     * @method on
     */
        on :  fp.addListener,
        
    /**
     * Removes an event handler from this element (shorthand for {@link #removeListener}).
     * @param {String} eventName the type of event to remove
     * @param {Function} fn the method the event invokes
     * @return {MIF.Element} this
     * @member Ext.Element
     * @method un
     */
        un : fp.removeListener,
        
        getUpdateManager : fp.getUpdater
    });

  /**
   * @class Ext.ux.ManagedIFrame.ComponentAdapter
   * @version 2.1.2 
   * @author Doug Hendricks. doug[always-At]theactivegroup.com
   * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
   * @copyright 2007-2010, Active Group, Inc.  All rights reserved.
   * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a>
   * @constructor
   * @desc
   * Abstract class.  This class should not be instantiated.
   */
  
   Ext.ux.ManagedIFrame.ComponentAdapter = function(){}; 
   Ext.ux.ManagedIFrame.ComponentAdapter.prototype = {
       
        /** @property */
        version : 2.12,
        
        /**
         * @cfg {String} defaultSrc the default src property assigned to the Managed Frame when the component is rendered.
         * @default null
         */
        defaultSrc : null,
        
        /**
         * @cfg {String} unsupportedText Text to display when the IFRAMES/FRAMESETS are disabled by the browser.
         *
         */
        unsupportedText : 'Inline frames are NOT enabled\/supported by your browser.',
        
        hideMode   : !Ext.isIE && !!Ext.ux.plugin.VisibilityMode ? 'nosize' : 'display',
        
        animCollapse  : Ext.isIE ,

        animFloat  : Ext.isIE ,
        
        /**
         * @cfg {object} frameConfig Frames DOM configuration options
         * This optional configuration permits override of the IFRAME's DOM attributes
         * @example
          frameConfig : {
              name : 'framePreview',
              frameborder : 1,
              allowtransparency : true
             }
         */
        frameConfig  : null,
        
        /**
         * @cfg focusOnLoad True to set focus on the frame Window as soon as its document
         * reports loaded.  (Many external sites use IE's document.createRange to create 
         * DOM elements, but to be successfull IE requires that the FRAME have focus before
         * the method is called)
         * @default false
         */
        focusOnLoad   : false,
        
        /**
         * @property {Object} frameEl An {@link #Ext.ux.ManagedIFrame.Element} reference to rendered frame Element.
         */
        frameEl : null, 
  
        /**
         * @cfg {Boolean} useShim
         * True to use to create a transparent shimming agent for use in masking the frame during
         * drag operations.
         * @default false
         */
        useShim   : false,

        /**
         * @cfg {Boolean} autoScroll
         * True to use overflow:'auto' on the frame element and show scroll bars automatically when necessary,
         * false to clip any overflowing content (defaults to true).
         * @default true
         */
        autoScroll: true,
        
         /**
         * @cfg {String/Object} autoLoad
         * Loads this Components frame after the Component is rendered with content returned from an
         * XHR call or optionally from a form submission.  See {@link #Ext.ux.ManagedIFrame.ComponentAdapter-load} and {@link #Ext.ux.ManagedIFrame.ComponentAdapter-submitAsTarget} methods for
         * available configuration options.
         * @default null
         */
        autoLoad: null,
        
        /** @private */
        getId : function(){
             return this.id   || (this.id = "mif-comp-" + (++Ext.Component.AUTO_ID));
        },
        
        stateEvents : ['documentloaded'],
        
        stateful    : false,
        
        /**
         * Sets the autoScroll state for the frame.
         * @param {Boolean} auto True to set overflow:auto on the frame, false for overflow:hidden
         * @return {Ext.ux.ManagedIFrame.Component} this
         */
        setAutoScroll : function(auto){
            var scroll = Ext.value(auto, this.autoScroll === true);
            this.rendered && this.getFrame() &&  
                this.frameEl.setOverflow( (this.autoScroll = scroll) ? 'auto':'hidden');
            return this;
        },
        
        getContentTarget : function(){
            return this.getFrame();
        },
        
        /**
         * Returns the Ext.ux.ManagedIFrame.Element of the frame.
         * @return {Ext.ux.ManagedIFrame.Element} this.frameEl 
         */
        getFrame : function(){
             if(this.rendered){
                if(this.frameEl){ return this.frameEl;}
                var f = this.items && this.items.first ? this.items.first() : null;
                f && (this.frameEl = f.frameEl);
                return this.frameEl;
             }
             return null;
            },
        
        /**
         * Returns the frame's current window object.
         *
         * @return {Window} The frame Window object.
         */
        getFrameWindow : function() {
            return this.getFrame() ? this.frameEl.getWindow() : null;
        },

        /**
         * If sufficient privilege exists, returns the frame's current document
         * as an HTMLElement.
         *
         * @return {HTMLElement} The frame document or false if access to
         *         document object was denied.
         */
        getFrameDocument : function() {
            return this.getFrame() ? this.frameEl.getFrameDocument() : null;
        },

        /**
         * Get the embedded iframe's document as an Ext.Element.
         *
         * @return {Ext.Element object} or null if unavailable
         */
        getFrameDoc : function() {
            return this.getFrame() ? this.frameEl.getDoc() : null;
        },

        /**
         * If sufficient privilege exists, returns the frame's current document
         * body as an HTMLElement.
         *
         * @return {Ext.Element} The frame document body or Null if access to
         *         document object was denied.
         */
        getFrameBody : function() {
            return this.getFrame() ? this.frameEl.getBody() : null;
        },
        
        /**
         * Reset the embedded frame to a neutral domain state and clear its contents
          * @param {String}src (Optional) A specific reset string (eg. 'about:blank')
         *            to use for resetting the frame.
         * @param {Function} callback (Optional) A callback function invoked when the
         *            frame reset is complete.
         * @param {Object} scope (Optional) scope by which the callback function is
         *            invoked.
         * @return {Ext.ux.ManagedIFrame.Component} this
         */
        resetFrame : function() {
            this.getFrame() && this.frameEl.reset.apply(this.frameEl, arguments);
            return this;
        },
        
        /**
         * Loads the Components frame with the response from a form submit to the 
         * specified URL with the ManagedIframe.Element as it's submit target.
         * @param {Object} submitCfg A config object containing any of the following options:
         * <pre><code>
         *      mifPanel.submitAsTarget({
         *         form : formPanel.form,  //optional Ext.FormPanel, Ext form element, or HTMLFormElement
         *         url: &quot;your-url.php&quot;,
         *         params: {param1: &quot;foo&quot;, param2: &quot;bar&quot;}, // or a URL encoded string
         *         callback: yourFunction,  //optional
         *         scope: yourObject, // optional scope for the callback
         *         method: 'POST', //optional form.action (default:'POST')
         *         encoding : "multipart/form-data" //optional, default HTMLForm default
         *      });
         *
         * </code></pre>
         *
         * @return {Ext.ux.ManagedIFrame.Component} this
         */
        submitAsTarget  : function(submitCfg){
            this.getFrame() && this.frameEl.submitAsTarget.apply(this.frameEl, arguments);
            return this;
        },
        
        /**
         * Loads this Components's frame immediately with content returned from an
         * XHR call.
         *
         * @param {Object/String/Function} loadCfg A config object containing any of the following
         *            options:
         *
         * <pre><code>
         *      mifPanel.load({
         *         url: &quot;your-url.php&quot;,
         *         params: {param1: &quot;foo&quot;, param2: &quot;bar&quot;}, // or a URL encoded string
         *         callback: yourFunction,
         *         scope: yourObject, // optional scope for the callback
         *         discardUrl: false,
         *         nocache: false,
         *         text: &quot;Loading...&quot;,
         *         timeout: 30,
         *         scripts: false,
         *         submitAsTarget : false,  //optional true, to use Form submit to load the frame (see submitAsTarget method)
         *         renderer:{render:function(el, response, updater, callback){....}}  //optional custom renderer
         *      });
         *
         * </code></pre>
         *
         * The only required property is url. The optional properties
         *            nocache, text and scripts are shorthand for
         *            disableCaching, indicatorText and loadScripts and are used
         *            to set their associated property on this panel Updater
         *            instance.
         * @return {Ext.ux.ManagedIFrame.Component} this
         */
        load : function(loadCfg) {
            if(loadCfg && this.getFrame()){
                var args = arguments;
                this.resetFrame(null, function(){ 
                    loadCfg.submitAsTarget ?
                    this.submitAsTarget.apply(this,args):
                    this.frameEl.load.apply(this.frameEl,args);
                },this);
            }
            this.autoLoad = loadCfg;
            return this;
        },

        /** @private */
        doAutoLoad : function() {
            this.autoLoad && this.load(typeof this.autoLoad == 'object' ? 
                this.autoLoad : { url : this.autoLoad });
        },

        /**
         * Get the {@link #Ext.ux.ManagedIFrame.Updater} for this panel's iframe. Enables
         * Ajax-based document replacement of this panel's iframe document.
         *
         * @return {Ext.ux.ManagedIFrame.Updater} The Updater
         */
        getUpdater : function() {
            return this.getFrame() ? this.frameEl.getUpdater() : null;
        },
        
        /**
         * Sets the embedded Iframe src property. Note: invoke the function with
         * no arguments to refresh the iframe based on the current src value.
         *
         * @param {String/Function} url (Optional) A string or reference to a Function that
         *            returns a URI string when called
         * @param {Boolean} discardUrl (Optional) If not passed as <tt>false</tt>
         *            the URL of this action becomes the default SRC attribute
         *            for this iframe, and will be subsequently used in future
         *            setSrc calls (emulates autoRefresh by calling setSrc
         *            without params).
         * @param {Function} callback (Optional) A callback function invoked when the
         *            frame document has been fully loaded.
         * @param {Object} scope (Optional) scope by which the callback function is
         *            invoked.
         * @return {Ext.ux.ManagedIFrame.Component} this
         */
        setSrc : function(url, discardUrl, callback, scope) {
            this.getFrame() && this.frameEl.setSrc.apply(this.frameEl, arguments);
            return this;
        },

        /**
         * Sets the embedded Iframe location using its replace method. Note: invoke the function with
         * no arguments to refresh the iframe based on the current src value.
         *
         * @param {String/Function} url (Optional) A string or reference to a Function that
         *            returns a URI string when called
         * @param {Boolean} discardUrl (Optional) If not passed as <tt>false</tt>
         *            the URL of this action becomes the default SRC attribute
         *            for this iframe, and will be subsequently used in future
         *            setSrc calls (emulates autoRefresh by calling setSrc
         *            without params).
         * @param {Function} callback (Optional) A callback function invoked when the
         *            frame document has been fully loaded.
         * @param {Object} scope (Optional) scope by which the callback function is
         *            invoked.
         * @return {Ext.ux.ManagedIFrame.Component} this
         */
        setLocation : function(url, discardUrl, callback, scope) {
           this.getFrame() && this.frameEl.setLocation.apply(this.frameEl, arguments);
           return this;
        },

        /**
         * @private //Make it state-aware
         */
        getState : function() {
            var URI = this.getFrame() ? this.frameEl.getDocumentURI() || null : null;
            var state = this.supr().getState.call(this);
            state = Ext.apply(state || {}, 
                {defaultSrc : Ext.isFunction(URI) ? URI() : URI,
                 autoLoad   : this.autoLoad
                });
            return state;
        },
        
        /**
         * @private
         */
        setMIFEvents : function(){
            
            this.addEvents(

                    /**
                     * Fires when the iFrame has reached a loaded/complete state.
                     * @event documentloaded
                     * @memberOf Ext.ux.ManagedIFrame.ComponentAdapter
                     * @param {Ext.ux.ManagedIFrame.Element} frameEl
                     */
                    'documentloaded',  
                      
                    /**
                     * Fires ONLY when an iFrame's Document(DOM) has reach a
                     * state where the DOM may be manipulated (ie same domain policy)
                     * Note: This event is only available when overwriting the iframe
                     * document using the update method and to pages retrieved from a "same
                     * domain". Returning false from the eventHandler stops further event
                     * (documentloaded) processing.
                     * @event domready 
                     * @memberOf Ext.ux.ManagedIFrame.ComponentAdapter
                     * @param {Ext.ux.ManagedIFrame.Element} this.frameEl
                     */
                    'domready',
                    /**
                     * Fires when the frame actions raise an error
                     * @event exception
                     * @memberOf Ext.ux.ManagedIFrame.ComponentAdapter
                     * @param {Ext.ux.MIF.Element} frameEl
                     * @param {Error/string} exception
                     */
                    'exception',

                    /**
                     * Fires upon receipt of a message generated by window.sendMessage
                     * method of the embedded Iframe.window object
                     * @event message
                     * @memberOf Ext.ux.ManagedIFrame.ComponentAdapter
                     * @param {Ext.ux.ManagedIFrame.Element} this.frameEl
                     * @param {object}
                     *            message (members: type: {string} literal "message", data
                     *            {Mixed} [the message payload], domain [the document domain
                     *            from which the message originated ], uri {string} the
                     *            document URI of the message sender source (Object) the
                     *            window context of the message sender tag {string} optional
                     *            reference tag sent by the message sender
                     * <p>Alternate event handler syntax for message:tag filtering Fires upon
                     * receipt of a message generated by window.sendMessage method which
                     * includes a specific tag value of the embedded Iframe.window object
                     *
                     */
                    'message',

                    /**
                     * Fires when the frame is blurred (loses focus).
                     * @event blur
                     * @memberOf Ext.ux.ManagedIFrame.ComponentAdapter
                     * @param {Ext.ux.ManagedIFrame.Element} frameEl
                     * @param {Ext.Event} e Note: This event is only available when overwriting the
                     *            iframe document using the update method and to pages
                     *            retrieved from a "same domain". Returning false from the
                     *            eventHandler [MAY] NOT cancel the event, as this event is
                     *            NOT ALWAYS cancellable in all browsers.
                     */
                    'blur',

                    /**
                     * Fires when the frame gets focus. Note: This event is only available
                     * when overwriting the iframe document using the update method and to
                     * pages retrieved from a "same domain". Returning false from the
                     * eventHandler [MAY] NOT cancel the event, as this event is NOT ALWAYS
                     * cancellable in all browsers.
                     * @event focus
                     * @memberOf Ext.ux.ManagedIFrame.ComponentAdapter
                     * @param {Ext.ux.ManagedIFrame.Element} frameEl
                     * @param {Ext.Event} e
                     *
                    */
                    'focus',
                    
                     /**
                     * Note: This event is only available when overwriting the iframe
                     * document using the update method and to pages retrieved from a "same-origin"
                     * domain.  To prevent numerous scroll events from being raised use the <i>buffer</i> listener 
                     * option to limit the number of times the event is raised.
                     * @event scroll 
                     * @param {Ext.ux.MIF.Element} this.
                     * @param {Ext.Event}
                     */
                    'scroll',
                    
                    /**
                     * Fires when the frames window is resized. Note: This event is only available
                     * when overwriting the iframe document using the update method and to
                     * pages retrieved from a "same domain". 
                     * @event resize
                     * @memberOf Ext.ux.ManagedIFrame.ComponentAdapter
                     * @param {Ext.ux.ManagedIFrame.Element} frameEl
                     * @param {Ext.Event} e
                     * @param {Object} documentSize A height/width object signifying the new document size
                     * @param {Object} viewPortSize A height/width object signifying the size of the frame's viewport
                     * @param {Object} viewSize A height/width object signifying the size of the frame's view
                     *
                    */
                    'resize',
                    
                    /**
                     * Fires when(if) the frames window object raises the unload event
                     * Note: This event is only available when overwriting the iframe
                     * document using the update method and to pages retrieved from a "same-origin"
                     * domain. Note: Opera does not raise this event.
                     * @event unload 
                     * @memberOf Ext.ux.ManagedIFrame.ComponentAdapter
                     * @param {Ext.ux.ManagedIFrame.Element} frameEl
                     * @param {Ext.Event}
                     */
                    'unload',
                    
                    /**
                     * Fires when the iFrame has been reset to a neutral domain state (blank document).
                     * @event reset
                     * @param {Ext.ux.ManagedIFrame.Element} frameEl
                     */
                    'reset'
                );
        },
        
        /**
         * dispatch a message to the embedded frame-window context (same-origin frames only)
         * @name sendMessage
         * @memberOf Ext.ux.ManagedIFrame.Element
         * @param {Mixed} message The message payload.  The payload can be any supported JS type. 
         * @param {String} tag Optional reference tag 
         * @param {String} origin Optional domain designation of the sender (defaults
         * to document.domain).
         */
        sendMessage : function(message, tag, origin) {
       
          //(implemented by mifmsg.js )
        },
        //Suspend (and queue) host container events until the child MIF.Component is rendered.
        onAdd : function(C){
             C.relayTarget && this.suspendEvents(true); 
        },
        
        initRef: function() {
      
	        if(this.ref){
	            var t = this,
	                levels = this.ref.split('/'),
	                l = levels.length,
	                i;
	            for (i = 0; i < l; i++) {
	                if(t.ownerCt){
	                    t = t.ownerCt;
	                }
	            }
	            this.refName = levels[--i];
	            t[this.refName] || (t[this.refName] = this);
	            
	            this.refOwner = t;
	        }
	    }
      
   };
   
   /*
    * end Adapter
    */
   
  /**
   * @class Ext.ux.ManagedIFrame.Component
   * @extends Ext.BoxComponent
   * @version 2.1.2 
   * @author Doug Hendricks. doug[always-At]theactivegroup.com
   * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
   * @copyright 2007-2010, Active Group, Inc.  All rights reserved.
   * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a>
   * @constructor
   * @base Ext.ux.ManagedIFrame.ComponentAdapter
   * @param {Object} config The config object
   */
  Ext.ux.ManagedIFrame.Component = Ext.extend(Ext.BoxComponent , { 
            
            ctype     : "Ext.ux.ManagedIFrame.Component",
            
            /** @private */
            initComponent : function() {
               
                var C = {
	                monitorResize : this.monitorResize || (this.monitorResize = !!this.fitToParent),
	                plugins : (this.plugins ||[]).concat(
	                    this.hideMode === 'nosize' && Ext.ux.plugin.VisibilityMode ? 
		                    [new Ext.ux.plugin.VisibilityMode(
		                        {hideMode :'nosize',
		                         elements : ['bwrap']
		                        })] : [] )
                  };
                  
                MIF.Component.superclass.initComponent.call(
                  Ext.apply(this,
                    Ext.apply(this.initialConfig, C)
                    ));
                    
                this.setMIFEvents();
            },   

            /** @private */
            onRender : function(ct, position){
                
                //default child frame's name to that of MIF-parent id (if not specified on frameCfg).
                var frCfg = this.frameCfg || this.frameConfig || (this.relayTarget ? {name : this.relayTarget.id}: {}) || {};
                
                //backward compatability with MIF 1.x
                var frDOM = frCfg.autoCreate || frCfg;
                frDOM = Ext.apply({tag  : 'iframe', id: Ext.id()}, frDOM);
                
                var el = Ext.getDom(this.el);

                (el && el.tagName == 'iframe') || 
                  (this.autoEl = Ext.apply({
                                    name : frDOM.id,
                                    frameborder : 0
                                   }, frDOM ));
                 
                MIF.Component.superclass.onRender.apply(this, arguments);
               
                if(this.unsupportedText){
                    ct.child('noframes') || ct.createChild({tag: 'noframes', html : this.unsupportedText || null});  
                }   
                var frame = this.el ;
                
                var F;
                if( F = this.frameEl = (this.el ? new MIF.Element(this.el.dom, true): null)){
                    (F.ownerCt = (this.relayTarget || this)).frameEl = F;
                    F.addClass('ux-mif'); 
                    if (this.loadMask) {
                        //resolve possible maskEl by Element name eg. 'body', 'bwrap', 'actionEl'
                        var mEl = this.loadMask.maskEl;
                        F.loadMask = Ext.apply({
                                    disabled    : false,
                                    hideOnReady : false,
                                    msgCls      : 'ext-el-mask-msg x-mask-loading',  
                                    maskCls     : 'ext-el-mask'
                                },
                                {
                                  maskEl : F.ownerCt[String(mEl)] || F.parent('.' + String(mEl)) || F.parent('.ux-mif-mask-target') || mEl 
                                },
                                Ext.isString(this.loadMask) ? {msg:this.loadMask} : this.loadMask
                              );
                        Ext.get(F.loadMask.maskEl) && Ext.get(F.loadMask.maskEl).addClass('ux-mif-mask-target');
                    }
                    
                    Ext.apply(F,{
                        disableMessaging : Ext.value(this.disableMessaging, true),
                        focusOnLoad      : Ext.value(this.focusOnLoad, Ext.isIE)
                    });
                    
                    F._observable && 
                        (this.relayTarget || this).relayEvents(F._observable, frameEvents.concat(this._msgTagHandlers || []));
                    delete this.contentEl;
                 }
                 
            },
            
            /** @private */
            afterRender  : function(container) {
                MIF.Component.superclass.afterRender.apply(this,arguments);
                
                // only resize (to Parent) if the panel is NOT in a layout.
                // parentNode should have {style:overflow:hidden;} applied.
                if (this.fitToParent && !this.ownerCt) {
                    var pos = this.getPosition(), size = (Ext.get(this.fitToParent)
                            || this.getEl().parent()).getViewSize();
                    this.setSize(size.width - pos[0], size.height - pos[1]);
                }

                this.getEl().setOverflow('hidden'); //disable competing scrollers
                this.setAutoScroll();
                var F;
               /* Enable auto-Shims if the Component participates in (nested?)
                * border layout.
                * Setup event handlers on the SplitBars and region panels to enable the frame
                * shims when needed
                */
                if(F = this.frameEl){
                    var ownerCt = this.ownerCt;
                    while (ownerCt) {
                        ownerCt.on('afterlayout', function(container, layout) {
                            Ext.each(['north', 'south', 'east', 'west'],
                                    function(region) {
                                        var reg;
                                        if ((reg = layout[region]) && 
                                             reg.split && reg.split.dd &&
                                             !reg._splitTrapped) {
                                               reg.split.dd.endDrag = reg.split.dd.endDrag.createSequence(MIM.hideShims, MIM );
                                               reg.split.on('beforeresize',MIM.showShims,MIM);
                                               reg._splitTrapped = MIM._splitTrapped = true;
                                        }
                            }, this);
                        }, this, { single : true}); // and discard
                        ownerCt = ownerCt.ownerCt; // nested layouts?
                    }
                    /*
                     * Create an img shim if the component participates in a layout or forced
                     */
                    if(!!this.ownerCt || this.useShim ){ this.frameShim = F.createFrameShim(); }
                    this.getUpdater().showLoadIndicator = this.showLoadIndicator || false;
                    
                    //Resume Parent containers' events 
                    var resumeEvents = this.relayTarget && this.ownerCt ?                         
                       this.ownerCt.resumeEvents.createDelegate(this.ownerCt) : null;
                       
                    if(this.autoload){
                       this.doAutoLoad();
                    } else if(this.frameMarkup || this.html) {
                       F.update(this.frameMarkup || this.html, true, resumeEvents);
                       delete this.html;
                       delete this.frameMarkup;
                       return;
                    }else{
                       if(this.defaultSrc){
                            F.setSrc(this.defaultSrc, false);
                       }else{
                            /* If this is a no-action frame, reset it first, then resume parent events
                             * allowing access to a fully reset frame by upstream afterrender/layout events
                             */ 
                            F.reset(null, resumeEvents);
                            return;
                       }
                    }
                    resumeEvents && resumeEvents();
                }
            },
            
            /** @private */
            beforeDestroy : function() {
                var F;
                if(F = this.getFrame()){
                    F.remove();
                    this.frameEl = this.frameShim = null;
                }
                this.relayTarget && (this.relayTarget.frameEl = null);
                MIF.Component.superclass.beforeDestroy.call(this);
            }
    });

    Ext.override(MIF.Component, MIF.ComponentAdapter.prototype);
    Ext.reg('mif', MIF.Component);
   
    /*
    * end Component
    */
    
  /**
   * @private
   * this function renders a child MIF.Component to MIF.Panel and MIF.Window
   * designed to be called by the constructor of higher-level MIF.Components only.
   */
  function embed_MIF(config){
    
    config || (config={});
    config.layout = 'fit';
    config.items = {
             xtype    : 'mif',
               ref    : 'mifChild',
            useShim   : true,
           autoScroll : Ext.value(config.autoScroll , this.autoScroll),
          defaultSrc  : Ext.value(config.defaultSrc , this.defaultSrc),
         frameMarkup  : Ext.value(config.html , this.html),
            loadMask  : Ext.value(config.loadMask , this.loadMask),
    disableMessaging  : Ext.value(config.disableMessaging, this.disableMessaging),
         focusOnLoad  : Ext.value(config.focusOnLoad, this.focusOnLoad),
          frameConfig : Ext.value(config.frameConfig || config.frameCfg , this.frameConfig),
          relayTarget : this  //direct relay of events to the parent component
        };
    delete config.html; 
    this.setMIFEvents();
    return config; 
    
  };
    
  /**
   * @class Ext.ux.ManagedIFrame.Panel
   * @extends Ext.Panel
   * @version 2.1.2 
   * @author Doug Hendricks. doug[always-At]theactivegroup.com
   * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
   * @copyright 2007-2010, Active Group, Inc.  All rights reserved.
   * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a>
   * @constructor
   * @base Ext.ux.ManagedIFrame.ComponentAdapter
   * @param {Object} config The config object
   */

  Ext.ux.ManagedIFrame.Panel = Ext.extend( Ext.Panel , {
        ctype       : 'Ext.ux.ManagedIFrame.Panel',
        bodyCssClass: 'ux-mif-mask-target',
        constructor : function(config){
            MIF.Panel.superclass.constructor.call(this, embed_MIF.call(this, config));
         }
  });
  
  Ext.override(MIF.Panel, MIF.ComponentAdapter.prototype);
  Ext.reg('iframepanel', MIF.Panel);
    /*
    * end Panel
    */

    /**
     * @class Ext.ux.ManagedIFrame.Portlet
     * @extends Ext.ux.ManagedIFrame.Panel
     * @version 2.1.2 
     * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
     * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a> 
     * @author Doug Hendricks. Forum ID: <a href="http://extjs.com/forum/member.php?u=8730">hendricd</a> 
     * @copyright 2007-2010, Active Group, Inc. All rights reserved.
     * @constructor Create a new Ext.ux.ManagedIFramePortlet 
     * @param {Object} config The config object
     */

    Ext.ux.ManagedIFrame.Portlet = Ext.extend(Ext.ux.ManagedIFrame.Panel, {
                ctype      : "Ext.ux.ManagedIFrame.Portlet",
                anchor     : '100%',
                frame      : true,
                collapseEl : 'bwrap',
                collapsible: true,
                draggable  : true,
                cls        : 'x-portlet'
                
            });
            
    Ext.reg('iframeportlet', MIF.Portlet);
   /*
    * end Portlet
    */
    
  /**
   * @class Ext.ux.ManagedIFrame.Window
   * @extends Ext.Window
   * @version 2.1.2 
   * @author Doug Hendricks. 
   * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
   * @copyright 2007-2010, Active Group, Inc.  All rights reserved.
   * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a>
   * @constructor
   * @base Ext.ux.ManagedIFrame.ComponentAdapter
   * @param {Object} config The config object
   */
    
  Ext.ux.ManagedIFrame.Window = Ext.extend( Ext.Window , 
       {
            ctype       : "Ext.ux.ManagedIFrame.Window",
            bodyCssClass: 'ux-mif-mask-target',
            constructor : function(config){
			    MIF.Window.superclass.constructor.call(this, embed_MIF.call(this, config));
            }
    });
    Ext.override(MIF.Window, MIF.ComponentAdapter.prototype);
    Ext.reg('iframewindow', MIF.Window);
    
    /*
    * end Window
    */
    
    /**
     * @class Ext.ux.ManagedIFrame.Updater
     * @extends Ext.Updater
     * @version 2.1.2 
     * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
     * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a> 
     * @author Doug Hendricks. Forum ID: <a href="http://extjs.com/forum/member.php?u=8730">hendricd</a> 
     * @copyright 2007-2010, Active Group, Inc. All rights reserved.
     * @constructor Creates a new Ext.ux.ManagedIFrame.Updater instance.
     * @param {String/Object} el The element to bind the Updater instance to.
     */
    Ext.ux.ManagedIFrame.Updater = Ext.extend(Ext.Updater, {
    
       /**
         * Display the element's "loading" state. By default, the element is updated with {@link #indicatorText}. This
         * method may be overridden to perform a custom action while this Updater is actively updating its contents.
         */
        showLoading : function(){
            this.showLoadIndicator && this.el && this.el.mask(this.indicatorText);
            
        },
        
        /**
         * Hide the Frames masking agent.
         */
        hideLoading : function(){
            this.showLoadIndicator && this.el && this.el.unmask();
        },
        
        // private
        updateComplete : function(response){
            MIF.Updater.superclass.updateComplete.apply(this,arguments);
            this.hideLoading();
        },
    
        // private
        processFailure : function(response){
            MIF.Updater.superclass.processFailure.apply(this,arguments);
            this.hideLoading();
        }
        
    }); 
    
    
    var styleCamelRe = /(-[a-z])/gi;
    var styleCamelFn = function(m, a) {
        return a.charAt(1).toUpperCase();
    };
    
    /**
     * @class Ext.ux.ManagedIFrame.CSS
     * Stylesheet interface object
     * @version 2.1.2 
     * @author Doug Hendricks. doug[always-At]theactivegroup.com
     * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
     * @copyright 2007-2010, Active Group, Inc.  All rights reserved.
     * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a>
     */
    Ext.ux.ManagedIFrame.CSS = function(hostDocument) {
        var doc;
        if (hostDocument) {
            doc = hostDocument;
            return {
                rules : null,
                /** @private */
                destroy  :  function(){  return doc = null; },

                /**
                 * Creates a stylesheet from a text blob of rules. These rules
                 * will be wrapped in a STYLE tag and appended to the HEAD of
                 * the document.
                 *
                 * @param {String}
                 *            cssText The text containing the css rules
                 * @param {String} id An (optional) id to add to the stylesheet for later removal
                 * @return {StyleSheet}
                 */
                createStyleSheet : function(cssText, id) {
                    var ss;
                    if (!doc)return;
                    var head = doc.getElementsByTagName("head")[0];
                    var rules = doc.createElement("style");
                    rules.setAttribute("type", "text/css");
                    Ext.isString(id) && rules.setAttribute("id", id);

                    if (Ext.isIE) {
                        head.appendChild(rules);
                        ss = rules.styleSheet;
                        ss.cssText = cssText;
                    } else {
                        try {
                            rules.appendChild(doc.createTextNode(cssText));
                        } catch (e) {
                            rules.cssText = cssText;
                        }
                        head.appendChild(rules);
                        ss = rules.styleSheet
                                ? rules.styleSheet
                                : (rules.sheet || doc.styleSheets[doc.styleSheets.length - 1]);
                    }
                    this.cacheStyleSheet(ss);
                    return ss;
                },

                /**
                 * Removes a style or link tag by id
                 *
                 * @param {String}
                 *            id The id of the tag
                 */
                removeStyleSheet : function(id) {

                    if (!doc || !id)return;
                    var existing = doc.getElementById(id);
                    if (existing) {
                        existing.parentNode.removeChild(existing);
                    }
                },

                /**
                 * Dynamically swaps an existing stylesheet reference for a new
                 * one
                 *
                 * @param {String}
                 *            id The id of an existing link tag to remove
                 * @param {String}
                 *            url The href of the new stylesheet to include
                 */
                swapStyleSheet : function(id, url) {
                    if (!doc)return;
                    this.removeStyleSheet(id);
                    var ss = doc.createElement("link");
                    ss.setAttribute("rel", "stylesheet");
                    ss.setAttribute("type", "text/css");
                    Ext.isString(id) && ss.setAttribute("id", id);
                    ss.setAttribute("href", url);
                    doc.getElementsByTagName("head")[0].appendChild(ss);
                },

                /**
                 * Refresh the rule cache if you have dynamically added stylesheets
                 * @return {Object} An object (hash) of rules indexed by selector
                 */
                refreshCache : function() {
                    return this.getRules(true);
                },

                // private
                cacheStyleSheet : function(ss, media) {
                    this.rules || (this.rules = {});
                    
                     try{// try catch for cross domain access issue
			          
				          Ext.each(ss.cssRules || ss.rules || [], 
				            function(rule){ 
				              this.hashRule(rule, ss, media);
				          }, this);  
				          
				          //IE @imports
				          Ext.each(ss.imports || [], 
				           function(sheet){
				              sheet && this.cacheStyleSheet(sheet,this.resolveMedia([sheet, sheet.parentStyleSheet]));
				           }
				          ,this);
			          
			        }catch(e){}
                },
                 // @private
			   hashRule  :  function(rule, sheet, mediaOverride){
			      
			      var mediaSelector = mediaOverride || this.resolveMedia(rule);
			      
			      //W3C @media
			      if( rule.cssRules || rule.rules){
			          this.cacheStyleSheet(rule, this.resolveMedia([rule, rule.parentRule ]));
			      } 
			      
			       //W3C @imports
			      if(rule.styleSheet){ 
			         this.cacheStyleSheet(rule.styleSheet, this.resolveMedia([rule, rule.ownerRule, rule.parentStyleSheet]));
			      }
			      
			      rule.selectorText && 
			        Ext.each((mediaSelector || '').split(','), 
			           function(media){
			            this.rules[((media ? media.trim() + ':' : '') + rule.selectorText).toLowerCase()] = rule;
			        }, this);
			      
			   },
			
			   /**
			    * @private
			    * @param {Object/Array} rule CSS Rule (or array of Rules/sheets) to evaluate media types.
			    * @return a comma-delimited string of media types. 
			    */
			   resolveMedia  : function(rule){
			        var media;
			        Ext.each([].concat(rule),function(r){
			            if(r && r.media && r.media.length){
			                media = r.media;
			                return false;
			            }
			        });
			        return media ? (Ext.isIE ? String(media) : media.mediaText ) : '';
			     },

                /**
                 * Gets all css rules for the document
                 *
                 * @param {Boolean}
                 *            refreshCache true to refresh the internal cache
                 * @return {Object} An object (hash) of rules indexed by
                 *         selector
                 */
                getRules : function(refreshCache) {
                    if (!this.rules || refreshCache) {
                        this.rules = {};
                        if (doc) {
                            var ds = doc.styleSheets;
                            for (var i = 0, len = ds.length; i < len; i++) {
                                try {
                                    this.cacheStyleSheet(ds[i]);
                                } catch (e) {}
                            }
                        }
                    }
                    return this.rules;
                },

               /**
			    * Gets an an individual CSS rule by selector(s)
			    * @param {String/Array} selector The CSS selector or an array of selectors to try. The first selector that is found is returned.
			    * @param {Boolean} refreshCache true to refresh the internal cache if you have recently updated any rules or added styles dynamically
			    * @param {String} mediaSelector Name of optional CSS media context (eg. print, screen)
			    * @return {CSSRule} The CSS rule or null if one is not found
			    */
                getRule : function(selector, refreshCache, mediaSelector) {
                    var rs = this.getRules(refreshCache);

			        if(Ext.type(mediaSelector) == 'string'){
			            mediaSelector = mediaSelector.trim() + ':';
			        }else{
			            mediaSelector = '';
			        }
			
			        if(!Ext.isArray(selector)){
			            return rs[(mediaSelector + selector).toLowerCase()];
			        }
			        var select;
			        for(var i = 0; i < selector.length; i++){
			            select = (mediaSelector + selector[i]).toLowerCase();
			            if(rs[select]){
			                return rs[select];
			            }
			        }
			        return null;
                },

               /**
			    * Updates a rule property
			    * @param {String/Array} selector If it's an array it tries each selector until it finds one. Stops immediately once one is found.
			    * @param {String} property The css property
			    * @param {String} value The new value for the property
			    * @param {String} mediaSelector Name(s) of optional media contexts. Multiple may be specified, delimited by commas (eg. print,screen)
			    * @return {Boolean} true If a rule was found and updated
			    */
                updateRule : function(selector, property, value, mediaSelector){
    
			         Ext.each((mediaSelector || '').split(','), function(mediaSelect){    
			            if(!Ext.isArray(selector)){
			                var rule = this.getRule(selector, false, mediaSelect);
			                if(rule){
			                    rule.style[property.replace(camelRe, camelFn)] = value;
			                    return true;
			                }
			            }else{
			                for(var i = 0; i < selector.length; i++){
			                    if(this.updateRule(selector[i], property, value, mediaSelect)){
			                        return true;
			                    }
			                }
			            }
			            return false;
			         }, this);
                }
            };
        }
    };

    /**
     * @class Ext.ux.ManagedIFrame.Manager
     * @version 2.1.2 
	 * @author Doug Hendricks. doug[always-At]theactivegroup.com
	 * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
	 * @copyright 2007-2010, Active Group, Inc.  All rights reserved.
	 * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a>
	 * @singleton
     */
    Ext.ux.ManagedIFrame.Manager = function() {
        var frames = {};
        var implementation = {
            // private DOMFrameContentLoaded handler for browsers (Gecko, Webkit, Opera) that support it.
            _DOMFrameReadyHandler : function(e) {
                try {
                    var $frame ;
                    if ($frame = e.target.ownerCt){
                        $frame.loadHandler.call($frame,e);
                    }
                } catch (rhEx) {} //nested iframes will throw when accessing target.id
            },
            /**
             * @cfg {String} shimCls
             * @default "ux-mif-shim"
             * The default CSS rule applied to MIF image shims to toggle their visibility.
             */
            shimCls : 'ux-mif-shim',

            /** @private */
            register : function(frame) {
                frame.manager = this;
                frames[frame.id] = frames[frame.name] = {ref : frame };
                return frame;
            },
            /** @private */
            deRegister : function(frame) {
                delete frames[frame.id];
                delete frames[frame.name];
                
            },
            /**
             * Toggles the built-in MIF shim off on all visible MIFs
             * @methodOf Ext.ux.MIF.Manager
             *
             */
            hideShims : function() {
                var mm = MIF.Manager;
                mm.shimsApplied && Ext.select('.' + mm.shimCls, true).removeClass(mm.shimCls+ '-on');
                mm.shimsApplied = false;
            },

            /**
             * Shim ALL MIFs (eg. when a region-layout.splitter is on the move or before start of a drag operation)
             * @methodOf Ext.ux.MIF.Manager
             */
            showShims : function() {
                var mm = MIF.Manager;
                !mm.shimsApplied && Ext.select('.' + mm.shimCls, true).addClass(mm.shimCls+ '-on');
                mm.shimsApplied = true;
            },

            /**
             * Retrieve a MIF instance by its DOM ID
             * @methodOf Ext.ux.MIF.Manager
             * @param {Ext.ux.MIF/string} id
             */
            getFrameById : function(id) {
                return typeof id == 'string' ? (frames[id] ? frames[id].ref
                        || null : null) : null;
            },

            /**
             * Retrieve a MIF instance by its DOM name
             * @methodOf Ext.ux.MIF.Manager
             * @param {Ext.ux.MIF/string} name
             */
            getFrameByName : function(name) {
                return this.getFrameById(name);
            },

            /** @private */
            // retrieve the internal frameCache object
            getFrameHash : function(frame) {
                return frames[frame.id] || frames[frame.id] || null;
            },

            /** @private */
            destroy : function() {
                if (document.addEventListener && !Ext.isOpera) {
                      window.removeEventListener("DOMFrameContentLoaded", this._DOMFrameReadyHandler , false);
                }
            }
        };
        // for Gecko and any who might support it later 
        document.addEventListener && !Ext.isOpera &&
            window.addEventListener("DOMFrameContentLoaded", implementation._DOMFrameReadyHandler , false);

        Ext.EventManager.on(window, 'beforeunload', implementation.destroy, implementation);
        return implementation;
    }();
    
    MIM = MIF.Manager;
    MIM.showDragMask = MIM.showShims;
    MIM.hideDragMask = MIM.hideShims;
    
    /**
     * Shim all MIF's during a Window drag operation.
     */
    var winDD = Ext.Window.DD;
    Ext.override(winDD, {
       startDrag : winDD.prototype.startDrag.createInterceptor(MIM.showShims),
       endDrag   : winDD.prototype.endDrag.createInterceptor(MIM.hideShims)
    });

    //Previous release compatibility
    Ext.ux.ManagedIFramePanel = MIF.Panel;
    Ext.ux.ManagedIFramePortlet = MIF.Portlet;
    Ext.ux.ManagedIframe = function(el,opt){
        
        var args = Array.prototype.slice.call(arguments, 0),
            el = Ext.get(args[0]),
            config = args[0];

        if (el && el.dom && el.dom.tagName == 'IFRAME') {
            config = args[1] || {};
        } else {
            config = args[0] || args[1] || {};

            el = config.autoCreate ? Ext.get(Ext.DomHelper.append(
                    config.autoCreate.parent || Ext.getBody(), Ext.apply({
                        tag : 'iframe',
                        frameborder : 0,
                        cls : 'x-mif',
                        src : (Ext.isIE && Ext.isSecure)? Ext.SSL_SECURE_URL: 'about:blank'
                    }, config.autoCreate)))
                    : null;

            if(el && config.unsupportedText){
                Ext.DomHelper.append(el.dom.parentNode, {tag:'noframes',html: config.unsupportedText } );
            }
        }
        
        var mif = new MIF.Element(el,true);
        if(mif){
            Ext.apply(mif, {
                disableMessaging : Ext.value(config.disableMessaging , true),
                loadMask : !!config.loadMask ? Ext.apply({
                            msg : 'Loading..',
                            msgCls : 'x-mask-loading',
                            maskEl : null,
                            hideOnReady : false,
                            disabled : false
                        }, config.loadMask) : false,
                _windowContext : null,
                eventsFollowFrameLinks : Ext.value(config.eventsFollowFrameLinks ,true)
            });
            
            config.listeners && mif.on(config.listeners);
            
            if(!!config.html){
                mif.update(config.html);
            } else {
                !!config.src && mif.setSrc(config.src);
            }
        }
        
        return mif;   
    };

    /**
     * Internal Error class for ManagedIFrame Components
	 * @class Ext.ux.ManagedIFrame.Error
     * @extends Ext.Error
     * @version 2.1.2 
     * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
     * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a> 
     * @author Doug Hendricks. Forum ID: <a href="http://extjs.com/forum/member.php?u=8730">hendricd</a> 
     * @copyright 2007-2010, Active Group, Inc. All rights reserved.
	 * @constructor 
     * @param {String} message
     * @param {Mixed} arg optional argument to include in Error object.
	 */
	Ext.ux.ManagedIFrame.Error = Ext.extend(Ext.Error, {
	    constructor : function(message, arg) {
	        this.arg = arg;
	        Ext.Error.call(this, message);
	    },
	    name : 'Ext.ux.ManagedIFrame'
	});
    
	Ext.apply(Ext.ux.ManagedIFrame.Error.prototype, {
	    lang: {
	        'documentcontext-remove': 'An attempt was made to remove an Element from the wrong document context.',
	        'execscript-secure-context': 'An attempt was made at script execution within a document context with limited access permissions.',
	        'printexception': 'An Error was encountered attempting the print the frame contents (document access is likely restricted).'
	    }
	});
    
    /** @private */
    Ext.onReady(function() {
            // Generate CSS Rules but allow for overrides.
            var CSS = new Ext.ux.ManagedIFrame.CSS(document), rules = [];

            CSS.getRule('.ux-mif-fill')|| (rules.push('.ux-mif-fill{height:100%;width:100%;}'));
            CSS.getRule('.ux-mif-mask-target')|| (rules.push('.ux-mif-mask-target{position:relative;zoom:1;}'));
            CSS.getRule('.ux-mif-el-mask')|| (rules.push(
              '.ux-mif-el-mask {z-index: 100;position: absolute;top:0;left:0;-moz-opacity: 0.5;opacity: .50;*filter: alpha(opacity=50);width: 100%;height: 100%;zoom: 1;} ',
              '.ux-mif-el-mask-msg {z-index: 1;position: absolute;top: 0;left: 0;border:1px solid;background:repeat-x 0 -16px;padding:2px;} ',
              '.ux-mif-el-mask-msg div {padding:5px 10px 5px 10px;border:1px solid;cursor:wait;} '
              ));


            if (!CSS.getRule('.ux-mif-shim')) {
                rules.push('.ux-mif-shim {z-index:8500;position:absolute;top:0px;left:0px;background:transparent!important;overflow:hidden;display:none;}');
                rules.push('.ux-mif-shim-on{width:100%;height:100%;display:block;zoom:1;}');
                rules.push('.ext-ie6 .ux-mif-shim{margin-left:5px;margin-top:3px;}');
            }

            !!rules.length && CSS.createStyleSheet(rules.join(' '), 'mifCSS');
            
        });

    /** @sourceURL=<mif.js> */
    Ext.provide && Ext.provide('mif');
})();