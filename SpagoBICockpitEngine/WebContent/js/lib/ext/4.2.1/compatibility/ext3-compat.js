/*========================================================================
 * 
 * Ext JS 3 -> 4 Compatibility Layer
 * Copyright (c) 2011 Sencha Inc.
 * 
 * Requires ext3-core-compat.js
 * 
 * Author: Brian Moeskau
 * Sencha Inc.
 * 
 * Contributors:
 * - Zach Garder, Nicholas Howard, and Bryce Minter of
 *   AllofE Solutions, Inc. (AllofE.com)
 * 
 * Revision History:
 * - 2011-04-26: Initial release
 * - 2011-05-27: Bug fixes for Ext 4.0.1 release
 * - 2011-08-31: Incorporated changes from AllofE.com contributors
 *
 *========================================================================*/

// This should be working as the alternateClassName but doesn't for some reason
if (Ext.toolbar && Ext.toolbar.Toolbar) {
	Ext.toolbar.Toolbar.SplitButton = Ext.button.Split;
}
// DomHelper does not currently go through the ClassManager so there is no alternateClassName
if (!Ext.DomHelper) {
    Ext.DomHelper = Ext.core.DomHelper;
}

Ext.apply(Ext.panel.Panel.prototype, {
    getToolbars : function(dock){
        Ext.Compat.deprecate({pkg:"Ext.panel.Panel", member:"getToolbars",
            msg:"This function only exists in the compatibility layer. Use panel.dockedItems.findBy(function(inItem) { return inItem.alias == 'widget.toolbar' && inItem.dock == '" + dock + "'; })"});
    
        return this.dockedItems.findBy(function(inItem) {
            return inItem.alias == "widget.toolbar" && inItem.dock == dock;
        });
    }
});

// Not sure if these are intended to be deprecated or they just haven't been moved over
Ext.apply(Ext.menu.Menu.prototype, {
    addSeparator : function() {
        Ext.Compat.deprecate({pkg:"Ext.menu.Menu", member:"addSeparator",
            msg:"Use this.add(Ext.create('Ext.menu.Separator')) instead."});
    
        return this.add(Ext.create('Ext.menu.Separator'));
    },
    addElement : function(el) {
        Ext.Compat.deprecate({pkg:"Ext.menu.Menu", member:"addElement",
            msg:"Use this.add(Ext.create('Ext.menu.Item', {el:el})) instead."});
    
        return this.add(Ext.create('Ext.menu.Item', {
            el: el
        }));
    },
    addItem : function(item) {
        Ext.Compat.deprecate({pkg:"Ext.menu.Menu", member:"addItem",
            msg:"Use this.add(item) instead."});
    
        return this.add(item);
    },
    addMenuItem : function(config) {
        Ext.Compat.deprecate({pkg:"Ext.menu.Menu", member:"addMenuItem",
            msg:"Use this.add(this.lookupComponent(config)) instead."});
    
        return this.add(this.lookupComponent(config));
    },
    addText : function(text){
        Ext.Compat.deprecate({pkg:"Ext.menu.Menu", member:"addText",
            msg:"Use this.add(Ext.create('Ext.menu.Item', {plain:true, text:text})) instead."});
    
        return this.add(Ext.create('Ext.menu.Item', {
            plain: true,
            text: text
        }));
    }
});


/*========================================================================
 * 
 * This section contains true compatibility overrides and should ship
 * with Ext 4 as an optional compatibility layer for Ext 3 code.
 * Ext.Compat is defined in ext3-core-compat.js.
 *
 *========================================================================*/

(function(){
    var compat = Ext.Compat,
        deprecate = Ext.bind(compat.deprecate, compat),
        notify = Ext.bind(compat.notify, compat),
        breaking = Ext.bind(compat.breaking, compat);
    
    /*-------------------------------------------------------------
     * Date (from the Ext.util folder in 3.x)
     *-------------------------------------------------------------*/
    (function(){
        var nativeDate = window.Date,
            utilDate = Ext.Date,
            staticMappings = ['useStrict', 'formatCodeToRegex', 'parseFunctions', 'parseRegexes', 'formatFunctions', 'y2kYear', 
                'MILLI', 'SECOND', 'MINUTE', 'HOUR', 'DAY', 'MONTH', 'YEAR', 'defaults', 'dayNames', 'monthNames', 
                'monthNumbers', 'formatCodes', 'getFormatCode', 'createFormat', 'createParser', 'parseCodes'],
            staticOverrides = ['getShortMonthName', 'getShortDayName', 'getMonthNumber', 'isValid', 'parseDate'],
            proto = ['dateFormat', 'format', 'getTimezone', 'getGMTOffset', 'getDayOfYear', 'getWeekOfYear', 'isLeapYear', 
                'getFirstDayOfMonth', 'getLastDayOfMonth', 'getFirstDateOfMonth', 'getLastDateOfMonth', 'getDaysInMonth', 
                'getSuffix', 'clone', 'isDST', 'clearTime', 'add', 'between'];
            
        // Static direct mappings. These are either private methods or any members
        // that cannot be aliased as methods to include a warning. These are documented
        // in the migration guide as breaking changes to be fixed.
        Ext.Array.forEach(staticMappings, function(member) {
            nativeDate[member] = utilDate[member];
        });
        
        // Static overrides. These are public static methods that can be overridden
        // as methods to include console warnings.
        Ext.Array.forEach(staticOverrides, function(member) {
            nativeDate[member] = function(){
                deprecate({pkg:'Date', member:member, alt:'Ext.Date.'+member});
                return utilDate[member].apply(utilDate, arguments);
            };
        });
        
        // Prototype (instance) overrides
        Ext.Array.forEach(proto, function(member) {
            nativeDate.prototype[member] = function() {
                if(member !== 'dateFormat'){
                    // dateFormat is actually private, and it is aliased publicly as "format". It needs
                    // to be included, but we can skip the warning as it would be a duplicate of the format
                    // warning and would actually be misleading since it won't have been called directly.
                    // All other methods in this array are public and should give a warning.
                    deprecate({pkg:'Date', member:'<date>.'+member, alt:'Ext.Date.'+member,
                        msg:'Note that this is now a static method, so the date instance will be the first argument to the new version.'});
                }
                return utilDate[member].apply(utilDate, [this].concat(Array.prototype.slice.call(arguments)));
            };
        });
        
        if (Ext.KeyMap) {
            Ext.apply(Ext.KeyMap.prototype, {
                constructor:Ext.Function.createInterceptor(Ext.KeyMap.prototype.constructor, function() {
                    if(arguments.callee.caller.caller.caller.toString().indexOf("Ext.KeyMap") != -1 || arguments.callee.caller.caller.caller.caller.caller.arguments[0] != "Ext.util.KeyMap")   {
                        deprecate({pkg:"KeyMap", msg:"Ext.KeyMap has been deprecated, use Ext.util.KeyMap."});
                    }
                })
            });
        }
        
        // Inject debugger into console.warn if debugErrors flag is set
        if(Ext.global.console && typeof(Ext.global.console.warn) == "function") {
            Ext.apply(Ext.global.console, {
                warn: Ext.Function.createInterceptor(Ext.global.console.warn, function() {
                    if(Ext.Compat.debugErrors && (!arguments.callee.caller.caller || arguments.callee.caller.caller.toString().indexOf("debugErrors") == -1))   {
                        debugger;
                    }
                })
            });
        }
    })();
    
    
    /*-------------------------------------------------------------
     * Ext
     *-------------------------------------------------------------*/
    Ext.apply(Ext, {
        extend: Ext.Function.createInterceptor(Ext.extend, function(inSuperclass, inConfig)  {
            deprecate({pkg:"Ext", member:"extend",
                msg:"Use Ext.define instead. e.g. Ext.my.cool.Class = Ext.extend(Ext.Component, {...}) is now Ext.define('Ext.my.cool.Class', {extend:'Ext.Component', ...})"});
        })
    });
    

    /*-------------------------------------------------------------
     * XTypes
     *-------------------------------------------------------------*/
    if (Ext.ClassManager) {
        Ext.apply(Ext.ClassManager, {
            instantiateByAlias: Ext.Function.createInterceptor(Ext.ClassManager.instantiateByAlias, function() {
                var args = Ext.Array.toArray(arguments),
                    alias = args.shift();
                //
                // These are xtypes that are not currently aliased in the framework code.
                // Not sure if these should really be deprecated or not...
                //
                if(alias == 'widget.tbsplit'){
                    deprecate({pkg:'Core', member:'tbsplit', alt:'splitbutton', type:'xtype'});
                    Ext.ClassManager.setAlias('Ext.button.Split', 'widget.tbsplit');
                }
                if(alias == 'widget.progress'){
                    deprecate({pkg:'Core', member:'progress', alt:'progressbar', type:'xtype'});
                    Ext.ClassManager.setAlias('Ext.ProgressBar', 'widget.progress');
                }
            })
        });
    }
    
    /*-------------------------------------------------------------
     * MixedCollection
     *-------------------------------------------------------------*/
    if (Ext.util.AbstractMixedCollection) {
        Ext.apply(Ext.util.AbstractMixedCollection.prototype, {
            removeKey : function() {
                deprecate({pkg:'Ext.util.MixedCollection', member:'removeKey', alt:'removeAtKey'});
                return this.removeAtKey.apply(this, arguments);
            },
            item : function() {
                deprecate({pkg:'Ext.util.MixedCollection', member:'item', alt:'get'});
                return this.get.apply(this, arguments);
            },
            itemAt : function() {
                deprecate({pkg:'Ext.util.MixedCollection', member:'itemAt', alt:'getAt'});
                return this.getAt.apply(this, arguments);
            },
            key : function() {
                deprecate({pkg:'Ext.util.MixedCollection', member:'key', alt:'getByKey'});
                return this.getByKey.apply(this, arguments);
            }
        });
    }
    if (Ext.util.MixedCollection) {
        Ext.apply(Ext.util.MixedCollection.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.util.MixedCollection.prototype.constructor, function() {
                this._origSort = this.sort;
                this.sort = function(dir, fn) {
                    if (Ext.isFunction(fn)) {
                        deprecate({pkg:'Ext.util.MixedCollection', member:'sort (with a comparator fn)', alt:'sortBy', 
                            msg:'Calling MixedCollection.sort and passing a comparator function as the second parameter '+
                                'is no longer supported. See the docs for MixedCollection.sort to see the current '+
                                'valid parameter list. When passing a comparator function, use sortBy instead.'});
                        
                        return this.sortBy(fn);
                    }
                    return this._origSort(arguments);
                }
            }),
            keySort : function() {
                deprecate({pkg:'Ext.util.MixedCollection', member:'keySort', alt:'sortByKey'});
                return this.sortByKey.apply(this, arguments);
            }
        });
    }
    
    /*-------------------------------------------------------------
     * AbstractComponent
     *-------------------------------------------------------------*/
    if (Ext.AbstractComponent) {
        Ext.apply(Ext.AbstractComponent.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.AbstractComponent.prototype.constructor, function(inConfig) {
				inConfig = inConfig || {};
				
                if(inConfig.autoWidth || inConfig.autoHeight)   {
                    notify("autoWidth and autoHeight have been deprecated. Configure the container's layout rather than using those properties.");
                }
                if(inConfig.bbar && inConfig.bbar.xtype == "paging")    {
                    deprecate({pkg:"Ext.toolbar.Paging", member:"paging", type:"xtype", alt:"pagingtoolbar"});
                    inConfig.bbar.xtype = "pagingtoolbar";
                }
            }),
            addClass : function() {
                deprecate({pkg:'Ext.Component', member:'addClass', alt:'addCls'});
                return this.addCls.apply(this, arguments);
            },
            removeClass : function() {
                deprecate({pkg:'Ext.Component', member:'removeClass', alt:'removeCls'});
                return this.removeCls.apply(this, arguments);
            }
        });
    }
    
    /*-------------------------------------------------------------
     * Component
     *-------------------------------------------------------------*/
    if (Ext.Component) {
        Ext.apply(Ext.Component.prototype, {
            initComponent: Ext.Function.createInterceptor(Ext.Component.prototype.initComponent, function() {
                if(Ext.isDefined(this.applyTo)){
                    deprecate({pkg:'Ext.Component', member:'applyTo', type:'config', alt:'renderTo',
                        msg:'Applying components to existing markup via "applyTo" is no longer supported.'});
                    
                    var replaced = false;
                    try{
                        var target = Ext.get(this.applyTo);
                        if (target) {
                            var parent = target.parent();
                            if (parent) {
                                this.renderTo = parent;
                                target.remove();
                                replaced = true;
                                notify({pkg:'Ext.Component', member:'applyTo', msg:'This component will attempt to render to '+
                                    'the applyTo target\'s parent node ("' + parent.id + '"). If this did not work as expected then '+
                                    'you will have to replace the applyTo config manually before this component will render properly.'})
                            }
                        }
                    } catch(ex) {}
                    
                    if (!replaced) {
                        breaking({pkg:'Ext.Component', member:'applyTo', msg:'Attempted to render the component to the applyTo target\'s '+
                            'parent node, but this failed. You must resolve this manually before the component can render.'})
                    }
                }
            })
        });

        Ext.define('Ext.BoxComponent', {
            extend: 'Ext.Component',
            constructor: function() {
                deprecate({member:'Ext.BoxComponent', alt:'Ext.Component', 
                    msg:'All functionality from BoxComponent is now directly in Component. '+
                        'Replace "BoxComponent" with "Component" and/or xtype "box" with "component".'});
                
                this.callParent(arguments);
            }
        });
    }
    
    /*-------------------------------------------------------------
     * ComponentManager
     *-------------------------------------------------------------*/
    if (Ext.ComponentManager) {
        var regMsg = 'Calling a separate function to register custom types is no longer necessary. '+
                     'Switch your class definition to use Ext.define with "{0}" as the alias config.';
        
        var checkClassRef = function(pkg, cls, member) {
            if (!Ext.isString(cls)) {
                breaking({pkg:pkg, member:member, msg:'You must either convert the passed class reference to a string -- e.g. ' + 
                    pkg + '.' + member + '("myclass", "Ext.ux.MyClass") -- or change your class definition to use Ext.define. '+
                    'See the section in the Migration Guide on registering xtypes for more info.'});
            }
        };
        
        Ext.ComponentManager.registerType = function(xtype, cls){
            deprecate({pkg:'ComponentManager', member:'registerType', msg:Ext.String.format(regMsg, 'widget.'+xtype)});
            checkClassRef('ComponentManager', cls, 'registerType');
            Ext.ClassManager.setAlias(cls, 'widget.'+xtype);
        };
        Ext.reg = function(xtype, cls){
            deprecate({pkg:'Ext', member:'reg', msg:Ext.String.format(regMsg, 'widget.'+xtype)});
            checkClassRef('Ext', cls, 'reg');
            Ext.ClassManager.setAlias(cls, 'widget.'+xtype);
        };
        Ext.ComponentManager.registerPlugin = function(ptype, cls){
            deprecate({pkg:'ComponentManager', member:'registerPlugin', msg:Ext.String.format(regMsg, 'plugin.'+ptype)});
            checkClassRef('ComponentManager', cls, 'registerPlugin');
            Ext.ClassManager.setAlias(cls, 'plugin.'+ptype);
        };
        Ext.preg = function(ptype, cls){
            deprecate({pkg:'Ext', member:'preg', msg:Ext.String.format(regMsg, 'plugin.'+ptype)});
            checkClassRef('Ext', cls, 'preg');
            Ext.ClassManager.setAlias(cls, 'plugin.'+ptype);
        };
        
        Ext.ComponentManager.create = function(component, defaultType)  {
            if (component instanceof Ext.AbstractComponent) {
                return component;
            }
            else if (Ext.isString(component)) {
                return Ext.createByAlias('widget.' + component);
            }
            else {
                var type = component.xtype || defaultType,
                    config = component;
                
                if(!Ext.ClassManager.maps.aliasToName["widget." + type])    {
                    if(Ext.ClassManager.maps.aliasToName[type]) {
                        deprecate({pkg:"Ext.ClassManager", member:"alias",
                            msg:"Using the xtype of '" + type + "' assumes the alias was created as 'widget." + type + 
                            "'. Please fix your class definition to include \"alias: 'widget." + type + "'\". Automatically fixing."});
                        
                        Ext.ClassManager.setAlias(Ext.ClassManager.getByAlias(type), "widget." + type);
                    }
                    else    {
                        breaking({pkg:"Ext.ClassManager", member:"alias",
                            msg:"The xtype of '" + type + "' does not exist and has no alias. "});
                    }
                }
                return Ext.createByAlias('widget.' + type, config);
            }
        };
    }
    
    /*-------------------------------------------------------------
     * Container
     *-------------------------------------------------------------*/
    if (Ext.container.AbstractContainer) {
        Ext.apply(Ext.container.AbstractContainer.prototype, {
            initComponent: Ext.Function.createInterceptor(Ext.container.AbstractContainer.prototype.initComponent, function(inConfig) {
				inConfig = inConfig || {};
				
                if (this.layout && (this.layout === 'form' || this.layout.type == "form")) {
                    deprecate({pkg:'FormPanel', member:'form', type:'layout', 
                        msg:'Form layout no longer exists, use a different container layout and allow each field\'s '+
                            'Field layout to apply labels. Falling back to anchor layout.'});
                    
                    if(this.layout.type == "form")  {
                        this.layout.type = 'anchor';
                    }
                    else    {
                        this.layout = "anchor";
                    }
                }
            }),
            get: function(comp) {
                deprecate({pkg:'Ext.Container', member:'get', alt:'getComponent'});
                return this.getComponent(comp);
            },
            findById: function(id) {
                deprecate({pkg:'Ext.Container', member:'findById', alt:'query', msg:'Use the query method with the # id syntax, e.g. comp.query("#"+id).'});
                return this.query('#'+id);
            }
        });
    }

    /*-------------------------------------------------------------
     * Toolbar
     *-------------------------------------------------------------*/
    if (Ext.toolbar.Toolbar) {
        Ext.apply(Ext.toolbar.Toolbar.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.toolbar.Toolbar.prototype.constructor, function(inConfig) {
				inConfig = inConfig || {};
				
                if (inConfig.items) {
                    var foundRightFill = false;
                    
                    Ext.Array.forEach(inConfig.items, function(inItem) {
                        if(inItem == "->") {
                            if(foundRightFill) {
                                deprecate({pkg:"Ext.toolbar.Toolbar", member:"->",
                                    msg:"You can't have multiple '->' right fill items on a single toolbar."});
                            }
                            foundRightFill = true;
                        }
                    });
                }
            }),
            addField : function(field){
                deprecate({pkg:'Toolbar', member:'addField', alt:'add'});
                return this.add(field);
            },
            addButton : function(btn)    {
                deprecate({pkg:"Toolbar", member:"addButton", msg:"Use items.add() instead."});
                return this.items.add(btn);
            }
        });
    }
    
    /*-------------------------------------------------------------
     * Menu
     *-------------------------------------------------------------*/
    if(Ext.menu.Menu)   {
        Ext.apply(Ext.menu.Menu.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.menu.Menu.prototype.constructor, function(inConfig) {
                var me = this;
            
				inConfig = inConfig || {};
				
                if(inConfig.defaults && inConfig.items) {
                    for(var i = 0; i < inConfig.items.length; i++)  {
                        var item = inConfig.items[i];
                        if(Ext.isString(item))  {
                            notify({pkg:"Ext.menu.Menu", member:"items",
                                msg:"Ext 4.0.2 has a bug where a string item is getting looked up using the wrong function. See code below for what to use instead. Automatically fixing."});
                            inConfig.items[i] = me.lookupItemFromString(item);
                        }
                    }
                }
                if(inConfig.listeners && inConfig.listeners.mouseout){
                    deprecate({pkg:"Ext.menu.Menu", member:"listeners", msg: "The event mouseout is now mouseleave. Auto-fixing"});
                    this.listeners.mouseleave = this.listeners.mouseout;
                    delete this.listeners.mouseout;
                }
                if(inConfig.listeners && inConfig.listeners.mousein){
                    deprecate({pkg:"Ext.menu.Menu", member:"listeners", msg: "The event mousein is now mouseenter. Auto-fixing"});
                    this.listeners.mouseenter = this.listeners.mousein;
                    delete this.listeners.mousein;
                }
            }),
            find : function(inProperty, inValue)    {
                deprecate({pkg:"Ext.menu.Menu", member:"find", msg:"Use menu.items.find(function(inMenuItem) { return (inMenuItem." + inProperty + " == '" + inValue + "'); }) instead of menu.find(property, value)."});
            }
        });
    }
    
    if(Ext.menu.CheckItem)  {
        Ext.apply(Ext.menu.CheckItem.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.menu.CheckItem.prototype.constructor, function(inConfig) {
				inConfig = inConfig || {};
				
                if(inConfig.iconCls)    {
                    deprecate({pkg:"Ext.menu.CheckItem", member:"iconCls",
                        msg:"When providing an iconCls with a check item, the checkbox will be displayed even if this has a group. You probably don't want to specify an iconCls with a checkItem."});
                }
            })
        });
    }
    
    /*-------------------------------------------------------------
     * PagingToolbar
     *-------------------------------------------------------------*/
    if (Ext.toolbar.Paging) {
        Ext.apply(Ext.toolbar.Paging.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.toolbar.Paging.prototype.constructor, function(config) {
                config = config || {};
                
                if (config.paramNames) {
                    var msg = 'Instead of params specific to this toolbar you should set any needed options on the associated store/proxy. '+
                        'See the header docs for Ext.data.Store for details. The defaults for PagingToolbar {start: \'start\', limit: \'limit\'} '+
                        'would map to the store\'s proxy as {startParam: \'start\', limitParam: \'limit\'}.';
                    
                    if (config.store && config.store.proxy) {
                        config.store.proxy.startParam = config.paramNames[start];
                        config.store.proxy.limitParam = config.paramNames[limit];
                        deprecate({pkg:'PagingToolbar', member:'paramNames', msg:msg});
                    }
                    else {
                        breaking({pkg:'PagingToolbar', member:'paramNames', msg:msg + ' No proxy is available in the current PagingToolbar '+
                            'configuration so this cannot be aliased automatically.'});
                    }
                    delete config.paramNames;
                }
                if (config.pageSize) {
                    config.store.pageSize = config.pageSize;
                    deprecate({pkg:'PagingToolbar', member:'pageSize', alt:'store.pageSize'});
                }
            }),
            changePage : function(page){
                deprecate({pkg:'PagingToolbar', member:'changePage', alt:'store.loadPage'});
                this.store.loadPage(page);
            }
        });
    }
    
    /*-------------------------------------------------------------
     * Views
     *-------------------------------------------------------------*/
    if (Ext.view.AbstractView) {
        Ext.apply(Ext.view.AbstractView.prototype, {
            initComponent : Ext.Function.createInterceptor(Ext.view.AbstractView.prototype.initComponent, function(){
                var isDef = Ext.isDefined;
                if (!isDef(this.tpl) || !isDef(this.store) || !isDef(this.itemSelector)) {
                    breaking({pkg:'DataView', msg:"DataView requires tpl, store and itemSelector configurations to be defined."});
                }
                if(Ext.isString(this.tpl) || Ext.isArray(this.tpl)){
                    notify({pkg:"DataView", member:"tpl", msg:"Automatically creating an XTemplate from 'this.tpl'"});
                    this.tpl = new Ext.XTemplate(this.tpl);
                }
                
                if (isDef(this.overClass)){
                    deprecate({pkg:'DataView', member:'overClass', alt:'overItemCls', type:'config'});
                    this.overItemCls = this.overClass;
                    delete this.overClass;
                }
                if (isDef(this.overCls)){
                    deprecate({pkg:'DataView', member:'overCls', alt:'overItemCls', type:'config'});
                    this.overItemCls = this.overCls;
                    delete this.overCls;
                }
                if (isDef(this.selectedClass)){
                    deprecate({pkg:'DataView', member:'selectedClass', alt:'selectedItemCls', type:'config'});
                    this.selectedItemCls = this.selectedClass;
                    delete this.selectedClass;
                }
                if (isDef(this.selectedCls)){
                    deprecate({pkg:'DataView', member:'selectedCls', alt:'selectedItemCls', type:'config'});
                    this.selectedItemCls = this.selectedCls;
                    delete this.selectedCls;
                }
                if(this.emptyText && this.deferEmptyText)   {
                    deprecate({pkg:"DataView", member:"deferEmptyText",
                        msg:"deferEmptyText by default is true. emptyText won't show if that's true, even though the docs says it will. Automatically setting to false."});
                    this.deferEmptyText = false;
                }
            })
        });
    }
    
    if (Ext.view.View)  {
        Ext.apply(Ext.view.View.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.view.View.prototype.constructor, function(inConfig) {
				inConfig = inConfig || {};
				
                if(inConfig.overItemCls && !inConfig.trackOver) {
                    inConfig.trackOver = true;
                    deprecate({pkg:"Ext.view.View", member:"overItemCls", type:"config",
                        msg:"This Ext.view.View has an overItemCls but trackOver isn't set to true. Please set trackOver to true since it defaults to false. Automatically correcting."});
                }
                if(inConfig.listeners && inConfig.listeners.dblclick)   {
                    deprecate({pkg:"Ext.view.View", member:"listeners.dblclick", type:"config",
                        msg:"dblclick:function(dataview, index, node, event) has been deprecated in favor of itemdblclick:function(dataview, record, node, index, event)." +
                            "Note that the parameters have changed. Automatically fixing."});
                            
                    inConfig.listeners.itemdblclick = function(inDataView, inRecord, inNode, inIndex, inEvent)  {
                        inConfig.listeners.dblclick.apply(this, [inDataView, inIndex, inNode, inEvent]);
                    };
                }
            }),
            getSelectionCount : function() {
                deprecate({pkg:"Ext.view.View", member:"getSelectionCount", type:"method",
                           msg: "getSelectionCount is no longer supported for the Ext.view.View. Instead use view.selModel.getSelection().length"});
                           
                return this.selModel.getSelection().length;
            },
            getSelectedRecords : function() {
                deprecate({pkg:"Ext.view.View", member:"getSelectionedRecords", type:"method",
                           msg: "getSelectdRecords is no longer supported for the Ext.view.View. Instead use view.selModel.getSelection()"});
                           
                return this.selModel.getSelection();
            }
            
        });
    }
    
    /*-------------------------------------------------------------
     * Panel
     *-------------------------------------------------------------*/
    function notifyToolHandlerArguments(inMessage)  {
        notify((inMessage ? inMessage + " " : "") + "The arguments passed to a tool's handler have changed. Originally it was (event, toolEl, " +
                "panel and tool). Now it's (event, tool, *panelHeader* and tool). To access the panel, use 'panelHeader.ownerCt'.");
    }
    
    if (Ext.panel.Panel) {
        Ext.apply(Ext.panel.AbstractPanel.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.panel.AbstractPanel.prototype.constructor, function(inConfig) {
				inConfig = inConfig || {};
				
                if(typeof(this.layout) != "undefined")  {
                    this.layoutDefined = true;
                }
                if(inConfig.tools)  {
                    notifyToolHandlerArguments();
                }
                // I believe this is just a doc issue, it should be supported:
//                if(typeof(inConfig.unstyled) != "undefined")    {
//                    notify("Though it still works, Ext.panel.Panel.unstyled is no longer a public configuration as of ExtJS 4.0.2.");
//                }
            }),
            initComponent: Ext.Function.createInterceptor(Ext.panel.AbstractPanel.prototype.initComponent, function() {
                if (this.bodyCssClass) {
                    var me = this,
                        msg = '',
                        bodyCssClass = me.bodyCssClass;
                        
                    if (Ext.isFunction(bodyCssClass)) {
                        me.bodyCls = bodyCssClass();
                        msg = 'Note that passing a function reference as the config value is no longer supported.'
                    }
                    else {
                        me.bodyCls = bodyCssClass;
                    }
                    delete me.bodyCssClass;
                    deprecate({pkg:'Ext.panel.Panel', member:'bodyCssClass', type:'config', alt:'bodyCls', msg:msg});
                }
                if(this.layout && this.layoutConfig && !this.layoutDefined) {
                    notify({pkg:"Ext.panel.Panel", member:"layoutConfig", type:"config",
                        msg:"Both a layout and a layoutConfig are specified. Consider merging layoutConfig into layout." +
                            "(e.g. layout:'hbox', layoutConfig:{align:'stretch', pack:'start'} => layout:{type:'hbox', align:'stretch', pack:'start'})"});
                            
                    this.layout = Ext.apply((typeof(this.layout) == "string" ? {type:this.layout} : this.layout), this.layoutConfig);
                }
                this.addListener("afterrender", function()  {
                    this.body.getUpdater = function()   {
                        deprecate({pkg:"Ext.panel.Panel", member:"body.getUpdater",
                            msg:"The panel's body no longer contains the updater. Use panel.getLoader() instead"});
                            
                        return {
                            reload: function()   {
                                deprecate({pkg:"Ext.panel.Panel.body", member:"reload",
                                    msg:"This pseudo-updater is no longer supported. Use panel.getLoader().load() instead of panel.body.getUpdater().reload()."});
                            }
                        };
                    };
                });
                if(this.autoLoad)   {
                    deprecate({pkg:"Ext.panel.Panel", member:"autoLoad",
                        msg:"Use panel.loader:{...} instead of panel.autoLoad:{...}. Automatically switching to loader."});
                        
                    this.loader = this.autoLoad;
                    delete this.autoLoad;
                }
                if(this.buttonAlign)    {
                    notify({pkg:"Ext.panel.Panel", member:"buttonAlign",
                        msg:"Rather than {buttonAlign:'X', buttons:[]}, the format {fbar:{layout:{pack:'X'}, items:[]}} is preferred."});
                }
            })
        });
        
        Ext.apply(Ext.panel.Panel.prototype, {
            getTopToolbar: function(){
                notify('Panel now supports an arbitrary number of toolbars, so getTopToolbar() will return the top toolbar at index 0 if multiple are found');
                var items = this.getToolbars('top');
                return items.length > 0 ? items[0] : null;
            },
            getBottomToolbar: function(){
                notify('Panel now supports an arbitrary number of toolbars, so getBottomToolbar() will return the bottom toolbar at index 0 if multiple are found');
                var items = this.getToolbars('bottom');
                return items.length > 0 ? items[0] : null;
            },
            find:function(inProperty, inValue)  {
                deprecate({pkg:"Ext.panel.Panel", member:"find",
                    msg:"Use items.findBy(function(inItem) { return inItem." + inProperty + " = '" + inValue + "'; }) instead."});
                    
                return this.items.findBy(function(inItem) {
                    return inItem[inProperty] == inValue;
                });
            },
            findByType:function(inType) {
                deprecate({pkg:"Ext.panel.Panel", member:"findByType",
                    msg:"Use var found = false; this.cascade(function(inItem) { if(inItem.getXType() == '" + inType + 
                    "') { found = inItem; return false; }}); instead. Automatically fixing."});
            
                var found = false;
                
                this.cascase(function(inItem)   {
                    if(inItem.getXType() == inType) {
                        found = inItem;
                        return false;
                    }
                });
                
                return found;
            },
            load : function(inOptions){
                deprecate({pkg:"Ext.panel.Panel", member:"load",
                    msg:"Instead of panel.load({...}), do: panel.loader = {...}; panel.getLoader().load();"});
                    
                this.loader = inOptions;
                return this.getLoader().load();
            }
        });
    }
    if (Ext.panel.Header) {
        Ext.apply(Ext.panel.Header.prototype, {
            find: function(inProperty, inValue)  {
                var returnValue = Ext.panel.Panel.prototype.find.apply(this, arguments),
                    callbackArguments = arguments.callee.caller.arguments;

                if(callbackArguments.length == 4 && callbackArguments[2].getXType() == "header") { // Coming from a tool's handler
                    notifyToolHandlerArguments("Your code relies on the third argument to be the panel rather than the panel header.");
                }
                return returnValue;
            }
        });
    }
    
    /*-------------------------------------------------------------
     * TabPanel
     *-------------------------------------------------------------*/
    if (Ext.tab.Panel) {
        Ext.apply(Ext.tab.Panel.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.tab.Panel.prototype.constructor, function(inConfig) {
				inConfig = inConfig || {};
				
                if(typeof(inConfig.autoTabs) != "undefined") {
                    delete inConfig.autoTabs;
                    deprecate({pkg:"Ext.tab.Panel", member:"autoTabs",
                        msg:"autoTabs functionality is no longer supported in ExtJS 4. There is no equivalent."});
                }
            })
        });
    }

    /*-------------------------------------------------------------
     * Layouts
     *-------------------------------------------------------------*/
    if (Ext.layout.container.Accordion) {
        Ext.apply(Ext.layout.container.Accordion.prototype, {
            constructor: Ext.Function.createSequence(Ext.layout.container.Accordion.prototype.constructor, function() {
                notify('AccordionLayout now defaults to animating expand/collapse. To revert to the 3.x default behavior set animate:false on the layout.')
            })
        });
    }
    
    /*-------------------------------------------------------------
     * TablePanel
     *-------------------------------------------------------------*/
    if (Ext.panel.Table) {
        Ext.apply(Ext.panel.Table.prototype, {
            initComponent: Ext.Function.createInterceptor(Ext.panel.Table.prototype.initComponent, function() {
                if (Ext.isDefined(this.preventHeaders)) {
                    deprecate({pkg:'Ext.grid.Panel', member:'preventHeaders', type:'config', alt:'hideHeaders'});
                    this.hideHeaders = this.preventHeaders;
                    delete this.preventHeaders;
                }
            })
        });
    }
    
    /*-------------------------------------------------------------
     * Grid components
     *-------------------------------------------------------------*/
    if (Ext.grid.Panel) {
        var oldGridPanelAddListener = Ext.grid.Panel.prototype.addListener;
    
        Ext.apply(Ext.grid.Panel.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.grid.Panel.prototype.constructor, function(config) {
                config = config || {};
                
                if (config.trackMouseOver !== undefined) {
                    deprecate({pkg:'Ext.GridPanel', member:'trackMouseOver', alt:'trackOver', type:'config',
                        msg:'Specify this as an attribute of the "viewConfig" config (e.g. viewConfig: {trackOver: false}).'});
                    
                    config.viewConfig = config.viewConfig || {};
                    config.viewConfig.trackOver = config.viewConfig.trackOver || config.trackMouseOver;
                    delete config.trackMouseOver; 
                }
                if (config.stripeRows !== undefined) {
                    deprecate({pkg:'Ext.GridPanel', member:'stripeRows', type:'config',
                        msg:'The stripeRows option should now be passed as an attribute of the "viewConfig" config (e.g. viewConfig: {stripeRows: true}).'});
                    
                    config.viewConfig = config.viewConfig || {};
                    config.viewConfig.stripeRows = config.viewConfig.stripeRows || config.stripeRows;
                    delete config.stripeRows; 
                }
                if (config.cm || config.colModel) {
                    deprecate({pkg:'Ext.GridPanel', member:'colModel/cm', type:'config',
                        msg:'Grids no longer use a ColumnModel class. Just specify the columns array config instead.'});
                    
                    // the ColumnModel mock constructor below just returns the columns array
                    config.columns = config.cm || config.colModel;
                    delete config.cm;
                    delete config.colModel;
                }
                if(config.columns && typeof(config.columns.$className) != "undefined")  {
                    notify({pkg:"Ext.grid.Panel", member:"columns", type:"config",
                        msg:"When specifying columns, use either a simple array or an object containing items/defaults. " +
                            "e.g. columns:Ext.create('Ext.grid.property.HeaderContainer', {columns:[]}) -> columns:{items:[]}"});
                }
                var cols = config.columns || this.columns;
                if (cols && Ext.isArray(cols)) {
                    Ext.each(cols, function(col) {
                        if (col.id) {
                            notify('Grid column "' + col.id + '" is defined with an id. In Ext 4 the id is used to reference the '+
                                'columns as components, and dataIndex is used to map back to the data id. Please add dataIndex to all columns, and remove IDs.');
                                
                            if(!col.dataIndex){
                                col.dataIndex = col.id;
                            }
                            delete col.id;
                        }
                    });
                }
                if (config.store && config.store instanceof Ext.data.GroupingStore) {
                    notify({pkg:'Ext.GridPanel', msg:'Attempting to convert a GroupingStore store config into a Grouping feature. See the '+
                        'GroupingStore constructor warning for additional details.'});
                        
                    config.features = config.features || [];
					if (!Ext.isArray(config.features)) {
						config.features = [config.features];
					}
                    config.features.push(Ext.create('Ext.grid.feature.Grouping'));
                }
                if (config.listeners && config.listeners.rowclick)  {
                    deprecate({pkg:"Ext.grid.Panel", member:"listeners.rowclick", type:"config",
                        msg:"The grid's rowclick listener has been deprecated. Use the selection model's select listener. " +
                            "The parameters of the function have changed in the selection model's select listener. Originally " +
                            "it was rowclick:function(grid, index, event), now it's select:function(selectionModel, record, index). " +
                            "If your listener code requires the grid, use 'var grid = selectionModel.view.panel'."});
                }
                if(config.view) {
                    deprecate({pkg:"Ext.grid.Panel", member:"view", type:"config",
                        msg:"Defining 'view' in the config will produce incorrect handling of the grid. Use 'viewConfig' instead. Automatically fixing."});
                        
                    config.viewConfig = config.view;
                    delete config.view;
                }
            }),
            
            initComponent: Ext.Function.createInterceptor(Ext.grid.Panel.prototype.initComponent, function() {
                if (this.autoExpandColumn !== undefined) {
                    deprecate({pkg:'Ext.grid.Panel', member:'autoExpandColumn', alt:'flex (header config)', type:'config',
                        msg:'You can set "flex: 1" in a specific header config for behavior equivalent to autoExpandColumn.'});
                        
                    var id;
                    Ext.Array.each(this.headers, function(header){
                        id = header.id || header.dataIndex;
                        if(id === this.autoExpandColumn){
                            header.flex = 1;
                            return false;
                        }
                    }, this);
                }
                this.addListener("groupchange", function() {
                    this.store.fireGroupChange();
                    deprecate({pkg:'Ext.grid.Panel', member:'groupchange', type:'config',
                        msg:'grid.groupchange is not the correct listener. Use grid.store.fireGroupChange() instead'});
                });
                if (this.sm) {
                    deprecate({pkg:"Ext.grid.Panel", member:"sm", type:"config", msg:"grid.sm is no longer used. Use grid.selModel instead"});
                    this.selModel = this.sm;
                    delete this.sm;
                }
                var columns = this.columns.items || this.columns;
                if(columns && this.store) {
                    var model = (typeof(this.store) == "string" ? Ext.data.StoreManager.get(this.store) : this.store).model,
                        fields = model ? ((typeof(model) == "string" ? Ext.ModelManager.getModel(model) : model).prototype.fields) : null;
                	
					if (fields) {
	                    Ext.Array.forEach(columns.items || columns, function(inColumn)   {
	                        if(inColumn.summaryType)    {
	                            var field = fields.findBy(function(inField) {
	                                    return inField.name == inColumn.dataIndex;
	                                }),
	                                testString = "ABC";
                                
	                            if(field.sortType(testString) === field.type.convert(testString))   {
	                                deprecate({pkg:"Ext.data.Field", type:"summaryType",
	                                    msg:"You have specified a '" + inColumn.summaryType + "' and the field '" + field.name +
	                                        "' does not convert input to a number. This can be a problem if the value returned by the " +
	                                        "provider is the empty string. Please add a type to the definition of the " + field.name + 
	                                        " field, e.g. fields:['" + field.name + "'] -> fields:[{name:'" + field.name + "', type:'int'}]."});
	                            }
	                        }
	                    });
					}
                }
            }),
            
            addListener: function(inEventName)   {
                if(inEventName == "rowclick" || inEventName == "rowdblclick")   {
                    var altEventName = inEventName.replace("row", "item");
                    deprecate({pkg:"Ext.grid.Panel", member:"listeners." + inEventName, type:"listener", alt:"listeners." + altEventName,
                        msg:"Automatically fixing."});
                        
                    inEventName = altEventName;
                }
                oldGridPanelAddListener.apply(this, arguments);
            }
        });
        
        Ext.apply(Ext.grid.GridPanel.prototype, {
            getColumnModel: function(){
                deprecate({pkg:'Ext.grid.Panel', member:'getColumnModel', type:'config',
                    msg:'grid.getColumnModel() has been deprecated. Use grid.headerCt instead.'});
            
                if (!this.colModel && !this.cm) {
                    this.cm = this.colModel = new Ext.grid.ColumnModel({
                        columns: this.columns
                    });
                }
                return this.cm;
            },
            groupBy:function()  {
                deprecate({pkg:'Ext.grid.Panel', member:'groupBy', type:'config',
                    msg:'grid.groupBy() has been deprecated. Use grid.group() instead.'});
                    
                return this.group.apply(this, arguments);
            }
        });
        
        Ext.grid.EditorGridPanel = function(config) {
            deprecate({pkg:'Ext.grid.EditorGridPanel', msg:'EditorGridPanel no longer exists as a separate class. Instead just '+
                'create a standard GridPanel and include the CellEditing plugin, e.g. "plugins: Ext.create("Ext.grid.plugin.CellEditing", {...})'});
            
            return Ext.createWidget('grid', Ext.apply(config || {}, {
                plugins: Ext.create('Ext.grid.plugin.CellEditing', {
                    clicksToEdit: 1
                })
            }));
        }
    }
    
    if (Ext.grid.View) {
        Ext.apply(Ext.grid.View.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.grid.View.prototype.constructor, function(config) {
                config = config || {};
                
                if(Ext.isFunction(config.getRowClass)){
                    var getRowClass = config.getRowClass;
                    this.__getRowClass = Ext.bind(getRowClass, this);
                    delete config.getRowClass;
                    
                    this.getRowClass = function(rec, rowIndex, rowParams, store){
                        var result = this.__getRowClass(rec, rowIndex, rowParams, store);
                        if (rowParams.body) {
                            delete rowParams.body;
                            breaking({pkg:'Ext.grid.View', member:'getRowClass.rowParams.body', single:true,
                                msg:'To implement a custom row body you must add the RowBody feature (ftype:"rowbody") '+
                                    'to the grid\'s viewConfig and override the "getAdditionalData" template method '+
                                    '(or use the Ext.grid.RowBodyPlugin helper class). Unfortunately this cannot be '+
                                    'inferred at design time so it must be fixed manually.'});
                        }
                        if (rowParams.bodyStyle) {
                            delete rowParams.bodyStyle;
                            deprecate({pkg:'Ext.grid.View', member:'getRowClass.rowParams.bodyStyle', single:true,
                                msg:'To implement custom row styles you must add the RowBody feature (ftype:"rowbody") '+
                                    'to the grid\'s viewConfig and override the "getAdditionalData" template method '+
                                    '(or use the Ext.grid.RowBodyPlugin helper class). Note that in 3.x this property '+
                                    'was a CSS style spec, whereas now you specify "rowBodyCls" as a CSS classname instead. Ignoring for now.'});
                        }
                        if (rowParams.tstyle) {
                            delete rowParams.tstyle;
                            deprecate({pkg:'Ext.grid.View', member:'getRowClass.rowParams.tstyle', single:true,
                                msg:'Grid row bodies no longer use a wrapping TABLE element, so the "tstyle" property '+
                                    'no longer directly applies. If you have CSS styles that still need to be applied, you '+
                                    'should add the RowBody feature (ftype:"rowbody") to the grid\'s viewConfig and override '+
                                    'the "getAdditionalData" template method (or use the Ext.grid.RowBodyPlugin helper class). '+
                                    'Note that in 3.x this property was a CSS style spec, whereas now you would use the "rowBodyCls" '+
                                    'CSS classname instead (and adjust for the fact that there is no TABLE if needed). Ignoring for now.'});
                        }
                        return result;
                    };
                }
            }),
            getEditorParent: function() {
                deprecate({pkg: 'Ext.grid.View', msg: 'getEditorParent() no longer exists.'});
            }
        });
        
        Ext.grid.GroupingView = function(config) {
            breaking({pkg:'Ext.grid.GroupingView', msg:'GroupingView no longer exists as a separate class, and grid views should '+
                'not need to be instantiated directly. Instead just create a standard GridPanel and include the Grouping feature, '+
                'e.g. "features: Ext.create("Ext.grid.feature.Grouping", {...}). Unfortunately there is no way to alias a call to this '+
                'constructor properly, so you\'ll need to adjust your GridPanel constructor as noted to resolve this.'});
        }
    }
    
    if (Ext.grid.header.Container) {
        Ext.apply(Ext.grid.header.Container.prototype, {
            constructor: Ext.Function.createSequence(Ext.grid.header.Container.prototype.constructor, function() {
                this.__prepareData = this.prepareData;
                this.prepareData = function() {
                    var obj = this.__prepareData.apply(this, arguments);
                    if (obj.cssWarning) {
                        delete obj.cssWarning;
                        deprecate({pkg:'Ext.grid.Panel', single:true, msg:'Your grid column renderer is including the legacy "css" attribute '+
                            'in the returned metaData object. This has been renamed to "tdCls" so you should change the attribute name in your renderer.'});
                    }
                    return obj;
                }
            })
        });
    }
    
    if (Ext.grid.Header) {
        Ext.apply(Ext.grid.Header.prototype, {
            initComponent: Ext.Function.createInterceptor(Ext.grid.Header.prototype.initComponent, function() {
                if (Ext.isDefined(this.header)) {
                    deprecate({pkg:'Ext.grid.Panel', member:'header', alt:'text', type:'config', single: true,
                        msg:'In 3.x the grid had a "columns" array containing "header" configs for the title of each column. '+
                            'In 4.0 the grid has a "headers" array and you should specify the "text" config for each header.'});
                            
                    this.text = this.header;
                    delete this.header;
                }
            })
        });
    }
    
    Ext.grid.ColumnModel = function(config) {
        return Ext.applyIf(config.columns ? config.columns : config, {
            on: Ext.emptyFn,
            addListener: Ext.emptyFn,
            getColumnId: Ext.emptyFn,
            getColumnAt: Ext.emptyFn,
            setConfig: Ext.emptyFn,
            getColumnById: Ext.emptyFn,
            getIndexById: Ext.emptyFn,
            moveColumn: Ext.emptyFn,
            getColumnCount: Ext.emptyFn,
            getColumnsBy: Ext.emptyFn,
            isSortable: Ext.emptyFn,
            isMenuDisabled: Ext.emptyFn,
            getRenderer: Ext.emptyFn,
            getRendererScope: Ext.emptyFn,
            setRenderer: Ext.emptyFn,
            getColumnWidth: Ext.emptyFn,
            setColumnWidth: Ext.emptyFn,
            getTotalWidth: Ext.emptyFn,
            getColumnHeader: Ext.emptyFn,
            setColumnHeader: Ext.emptyFn,
            getColumnTooltip: Ext.emptyFn,
            setColumnTooltip: Ext.emptyFn,
            getDataIndex: Ext.emptyFn,
            setDataIndex: Ext.emptyFn,
            findColumnIndex: Ext.emptyFn,
            isCellEditable: Ext.emptyFn,
            getCellEditor: Ext.emptyFn,
            setEditable: Ext.emptyFn,
            isHidden: Ext.emptyFn,
            isFixed: Ext.emptyFn,
            isResizable: Ext.emptyFn,
            setHidden: function()   {
                deprecate({pkg:"Ext.grid.ColumnModel", msg:"Ext.grid.ColumnModel.setHidden(x, true|false) has been deprecated. Use grid.headerCt.getComponent(x).show|hide()."});
            },
            setEditor: Ext.emptyFn,
            destroy: Ext.emptyFn,
            setState: Ext.emptyFn
        });
    };
    
    if (Ext.grid.Column) {
        Ext.Compat.bindProperty({owner:Ext.grid.Column, name:'types', defaultValue:{},
            getterMsg: function(){
                deprecate({pkg:'Ext.grid.Column', member:'types', type:'property', alt:'alias (config)',
                    msg:'The new approach to creating a custom column type is to specify the alias config '+
                    'within your column\'s class definition (e.g., alias: ["widget.mycolumn"]). You could also '+
                    'call the setAlias method after the class is defined.'});
            }
        });
    }
    
    // temp aliases -- these will be added into Ext 4
    Ext.apply(Ext.grid.Panel.prototype, {
        getStore: function() {
            return this.store;
        }
    });
    
    if (Ext.selection.Model) {
        Ext.apply(Ext.selection.Model.prototype, {
            selectRow: function(index){
                deprecate({pkg:'Ext.grid.RowSelectionModel', member:'selectRow', alt:'Ext.selection.RowModel.select|selectRange', 
                    msg:'Note that selectRange requires both start and end rows as its first two arguments, defaulting both to the index ('+index+').'});
                
                return this.select(index);
            },
            getSelections: function(){
                deprecate({pkg:'Ext.grid.RowSelectionModel', member:'getSelections', alt:'Ext.selection.RowModel.getSelection'});
                return this.getSelection();
            }
        });
    }
    
    if (Ext.grid.feature.Grouping) {
        var oldInitFeatures = Ext.view.Table.prototype.initFeatures;
        
        Ext.apply(Ext.view.Table.prototype, {
            initFeatures: function() {
                var returnValue = oldInitFeatures.apply(this, arguments),
                    columns = this.headerCt.items;
                
                this.featuresMC.each(function(inItem) {
                    var groupField = inItem.property || (inItem.getGroupField && inItem.getGroupField());
                    if(!groupField){
                        return true;
                    }
                    var column = columns.findBy(function(inColumn) { return inColumn.dataIndex == groupField; });
                        
                    if(!column) {
                        return true;
                    }
                        
                    if(!column.hidden) {
                        deprecate({pkg:"Ext.grid.column.Column", member:"hidden",
                            msg:"The column associated with the field you are grouping by is not hidden. This was hidden by default in ExtJS 3 but not in 4. Auto hiding."});
                            
                        if(column.rendered) {
                            column.hide();
                        }
                        else {
                            column.addListener("afterrender", function() {
                                column.hide();
                            });
                        }
                    }
                });
                
                return returnValue;
            }
        });
        
        Ext.apply(Ext.grid.feature.Grouping.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.grid.feature.Grouping.prototype.constructor, function(inConfig) {
				inConfig = inConfig || {};
				
                if (typeof(inConfig.hideGroupedColumn) !== "undefined") {
                    deprecate({pkg:"Ext.grid.feature.Grouping", member:"hideGroupedColumn",
                        msg:"hideGroupedColumn is no longer supported. The column has to be hidden."});
                }
                if (typeof(inConfig.showGroupName) !== "undefined") {
                    deprecate({pkg:"Ext.grid.feature.Grouping", member:"showGroupName",
                        msg:"No longer used. Change the groupHeaderTpl instead."});
                }
            })
        });
        
        Ext.apply(Ext.grid.column.Column.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.grid.column.Column.prototype.constructor, function(inConfig) {
				inConfig = inConfig || {};
				
                if(inConfig.renderer && Ext.isFunction(inConfig.renderer)) {
                    var oldRenderer = inConfig.renderer;
                    inConfig.renderer = function(inValue, inMeta) {
                        var returnValue = oldRenderer.apply(this, arguments);
                        
                        if(inMeta.cls)  {
                            deprecate({pkg:"Ext.grid.column.Column", member:"metaData.cls",
                                msg:"metaData.cls has been moved to metaData.tdCls. Automatically updating."});
                            inMeta.tdCls = inMeta.cls;
                            delete inMeta.cls;
                        }
                        if(inMeta.attr) {
                            deprecate({pkg:"Ext.grid.column.Column", member:"metaData.attr",
                                msg:"metaData.attr has been moved to metaData.tdAttr. Automatically updating."});
                            inMeta.tdAttr = inMeta.attr;
                            delete inMeta.attr;
                        }
                        if(inMeta.tdAttr && inMeta.tdAttr.indexOf("ext:q") !== -1)   {
                            deprecate({pkg:"Ext.XTemplate", member:"ext:qtip",
                                msg:"Use data-qtip instead of ext:qtip, data-qwidth instead of ext:qwidth, etc. Replacing in DEP2321."});
                        }
                        return returnValue;
                    };
                }
            })
        });
    }
    
    /*-------------------------------------------------------------
     * Tree (removed classes)
     *-------------------------------------------------------------*/
    Ext.tree.TreeNode = function(config){
        deprecate({pkg:'Ext.tree.TreeNode', msg:'This class is no longer needed. The Tree now uses standard Records that get decorated '+
            'by the NodeInterface class in Ext 4.'});
        Ext.apply(this, config);
    };
    Ext.tree.AsyncTreeNode = function(config){
        deprecate({pkg:'Ext.tree.AsyncTreeNode', msg:'This class is no longer needed. Specify a TreeStore with an AjaxProxy instead.'});
        Ext.apply(this, config);
    };
    Ext.tree.AsyncTreeNode.prototype = {
        expand: function(){
            if (this.store) {
                deprecate({pkg:"Ext.tree.AsyncTreeNode", member:"expand", msg:"This method has been deprecated."});
                this.store.load({
                    url: this.url || this.dataUrl
                });
            }
        }
    };
    
    Ext.tree.TreeSorter = function(tree, config){
        deprecate({pkg:'Ext.tree.TreeSorter', msg:'This class is no longer needed. Specify a TreeStore with standard "sorter" config options instead.'});
        Ext.apply(this, config);
    };
    
    Ext.tree.TreeLoader = function(config){
        deprecate({pkg:'Ext.tree.TreeLoader', msg:'This class is no longer needed. Specify a TreeStore with standard store options to load the tree.'});
        Ext.apply(this, config);
    };
    
    /*-------------------------------------------------------------
     * TreePanel
     *-------------------------------------------------------------*/
    if (Ext.tree.Panel) {
        Ext.apply(Ext.tree.Panel.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.tree.Panel.prototype.constructor, function(config) {
                if (config.hlDrop) {
                    delete config.hlDrop;
                    deprecate({pkg:'Ext.tree.TreePanel', member:'hlDrop', type:'config', 
                        msg:'Highlighting tree nodes on drop is no longer supported. You can simply remove this config.'});
                }
                if (config.hlColor) {
                    delete config.hlDrop;
                    deprecate({pkg:'Ext.tree.TreePanel', member:'hlColor', type:'config',
                        msg:'Highlighting tree nodes on drop is no longer supported. You can simply remove this config.'});
                }
                
                var ddConfig = config.ddConfig || {};
                if (Ext.isDefined(config.enableDrag)) {
                    deprecate({pkg:'Ext.tree.TreePanel', member:'enableDrag', type:'config', alt:'ddConfig.enableDrag'});
                    ddConfig.enableDrag = config.enableDrag;
                    delete config.enableDrag;
                }
                if (Ext.isDefined(config.enableDrop)) {
                    deprecate({pkg:'Ext.tree.TreePanel', member:'enableDrop', type:'config', alt:'ddConfig.enableDrop'});
                    ddConfig.enableDrop = config.enableDrop;
                    delete config.enableDrop;
                }
                if (Ext.isDefined(config.enableDD)) {
                    var msg = config.enableDD ? 'Note that ddConfig defaults to enabling both drag and drop by default in Ext 4. Since you are '+
                        'currently passing "enableDD: true", in this case the config can simply be ommitted entirely.' : '';
                    
                    ddConfig = {
                        enableDrag: config.enableDD,
                        enableDrop: config.enableDD
                    };
                    delete config.enableDD;
                    deprecate({pkg:'Ext.tree.TreePanel', member:'enableDD', type:'config', alt:'ddConfig', msg:msg});
                }
                config.ddConfig = ddConfig;
                
                var url = config.dataUrl || this.dataUrl;
                if (url) {
                    deprecate({pkg:'Ext.tree.TreePanel', member:'dataUrl', type:'config', alt:'TreeStore',
                        msg:'The TreePanel no longer supports loading data directly. Creating an implicit TreeStore using the url: '+url});
                    
                    this.loader = { dataUrl: url };
                    delete config.dataUrl;
                    delete this.dataUrl;
                }
                else if (config.loader) {
                    this.loader = config.loader;
                    delete config.loader;
                    deprecate({pkg:'Ext.tree.TreePanel', member:'loader', type:'config', alt:'TreeStore',
                        msg:'The TreeLoader class and TreePanel.loader config have been removed. Trees now use the TreeStore '+
                            'which provides all standard Ext.data.Store loading capabilities.'});
                }
                
                if (config.root && (config.root instanceof Ext.tree.AsyncTreeNode || config.root.nodeType == 'async')) {
                    config.loader = this.loader;
                }
                this.applyCompatOptions();
            }),
            
            initComponent: Ext.Function.createSequence(Ext.tree.Panel.prototype.initComponent, function() {
                this.on('itemclick', function(view, model, el, idx, e){
                    if (this.events['click']) {
                        model.attributes = model.attributes || model.data;
                        this.fireEvent('click', model, e);
                        deprecate({pkg:'Ext.tree.TreePanel', member:'click', type:'event', alt:'itemclick', 
                            msg:'Note that the argument order has changed, and that the data argument was a node in 3.x and is now '+
                                'the selected model. Instead of node.attributes you can access the data via model.data.'})
                    }
                });
            }),
            
            applyCompatOptions: function(){
                var loader = this.loader;
                if (loader && (loader.url || loader.dataUrl || loader.proxy)) {
                    var urlProp = loader.url ? 'url' : 'dataUrl',
                        proxy = loader.proxy || {
                            type: 'ajax',
                            url: loader[urlProp]
                        },
                        storeConfig = {
                            proxy: proxy
                        }
                    
                    if (this.root) {
                        storeConfig.root = this.root;
                        delete this.root;
                    }
                    this.store = new Ext.data.TreeStore(storeConfig);
                    this.loader.store = this.store;
                    
                    notify({pkg:'Ext.tree.Panel', msg:'Using the TreeLoader.' + urlProp + 
                        ' config to generate a default TreeStore + Proxy with the url: '+loader[urlProp]});
                }
            },
            
            // Aliased in TreePanel
//            setRootNode: function(root){
//                deprecate({pkg:'Ext.tree.Panel', member:'setRootNode', alt:'TreeStore.setRootNode', 
//                    msg:'Alternately you could add a "root" option to your TreeStore config.'});
//                
//                if (this.store) {
//                    this.store.setRootNode(root);
//                }
//                else {
//                    this.root = root;
//                    this.applyCompatOptions();
//                }
//            },
            
            // Aliased in TreePanel
//            getRootNode : function(){
//                deprecate({pkg:'Ext.tree.Panel', member:'getRootNode', alt:'TreeStore.getRootNode'});
//                return this.store.getRootNode.apply(this.store, arguments);
//            },
            
            getNodeById : function(){
                deprecate({pkg:'Ext.tree.Panel', member:'getNodeById', alt:'TreeStore.getNodeById',
                    msg:'If you have a TreePanel reference you can call treePanel.getStore().getNodeById("id").'});
                
                return this.store.getNodeById.apply(this.store, arguments);
            },
            
            getChecked : function(){
                deprecate({pkg:'Ext.tree.Panel', member:'getChecked', alt:'Ext.tree.View.getChecked',
                    msg:'Note that in 3.x this method returned objects of type TreeNode. In 4.0 it returns standard Records, '+
                        'so the code that processes the checked items will have to be adjusted accordingly. For compatibility '+
                        'the record\'s data objects are being returned, as each record\'s data is now decorated with the node '+
                        'interface so they should match the 3.x API. However your 4.0 code should expect full Record objects '+
                        'instead and will access the node attributes via Record.get(\'attrName\') or Record.data.attrName.'});
                
                var recs = this.getView().getChecked(),
                    nodes = [];
                
                Ext.each(recs, function(rec){
                    nodes.push(rec.data);
                });
                
                return nodes;
            }
        });
    }
    
    // Not sure if this is still needed?
//    if (Ext.data.TreeStore) {
//        Ext.override(Ext.data.TreeStore, {
//            fillNode: function(node, records) {
//                var me = this,
//                    ln = records ? records.length : 0,
//                    i = 0, sortCollection;
//        
////                if (ln && me.sortOnLoad && !me.remoteSort && me.sorters && me.sorters.items) {
////                    sortCollection = Ext.create('Ext.util.MixedCollection');
////                    sortCollection.addAll(records);
////                    sortCollection.sort(me.sorters.items);
////                    records = sortCollection.items;
////                }
//                
//                node.set('loaded', true);
//                for (; i < ln; i++) {
//                    node.appendChild(records[i], undefined, true);
//                }
//
//                return records;
//            }
//        });
//    }
    
    /*-------------------------------------------------------------
     * SelectionModel
     *-------------------------------------------------------------*/
    if (Ext.selection.RowModel) {
        Ext.apply(Ext.selection.RowModel.prototype, {
            constructor: Ext.Function.createSequence(Ext.selection.RowModel.prototype.constructor, function(inConfig) {
                inConfig = inConfig || {};
                
                this.on('select', function(sm, rec, idx){
                    if (this.events['rowselect']) {
                        this.fireEvent('rowselect', sm, idx, rec);
                        deprecate({pkg:'Ext.grid.RowSelectionModel', member:'rowselect', type:'event', alt:'select', 
                            msg:'Note that the argument order has changed (the index and record/model args have been switched).'})
                    }
                });
                this.on('deselect', function(sm, rec, idx){
                    if (this.events['rowdeselect']) {
                        this.fireEvent('rowdeselect', sm, idx, rec);
                        deprecate({pkg:'Ext.grid.RowSelectionModel', member:'rowdeselect', type:'event', alt:'deselect', 
                            msg:'Note that the argument order has changed (the index and record/model args have been switched).'})
                    }
                });
                
                if (typeof(inConfig.singleSelect) !== "undefined") {
                    deprecate({pkg:"Ext.selection.RowModel", member:"singleSelect", alt:"mode",
                        msg:"Use mode instead of singleSelect. e.g. singleSelect:true -> mode:'SINGLE', singleSelect:false -> mode:'MULTI'."});
                
                    if(inConfig.singleSelect)   {
                        this.mode = "SINGLE";
                    }
                    else    {
                        this.mode = "MULTI";
                    }
                }
            }),
            
            getSelections: function() {
                deprecate({pkg:"Ext.selection.RowModel", msg:"getSelections() has been deprecated. Use getSelection()."});
                return this.getSelection();
            },
            getSelected:function() {
                deprecate({pkg:"Ext.selection.RowModel", msg:"getSelected() has been deprecated. Use getSelection()[0]."});
                return this.getSelection()[0];
            },
            selectFirstRow:function() {
                deprecate({pkg:"Ext.selection.RowModel", msg:"selectFirstRow() has been deprecated. Use select(0)."});
                return this.select(0);
            }
        });
    }
    
    if (Ext.selection.CheckboxModel) {
        Ext.define("Ext.grid.CheckboxSelectionModel", {
            extend:"Ext.selection.CheckboxModel",
            
            constructor:function()  {
                deprecate({pkg:"Ext.grid.CheckboxSelectionModel",
                    msg:"This class has been moved to Ext.selection.CheckboxModel."});
                    
                this.callParent(arguments);
            }
        });
    }
    
    /*-------------------------------------------------------------
     * Window
     *-------------------------------------------------------------*/
    if (Ext.window.Window) {
        Ext.apply(Ext.window.Window.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.window.Window.prototype.constructor, function(config) {
                config = config || {};
                
                if (config.closeAction === 'close') {
                    deprecate({pkg:'Ext.Window', member:'closeAction', type:'config', 
                        msg:'The default value of "close" is no longer valid. Use "destroy" instead.'});
                        
                    delete config.closeAction;
                    this.closeAction = 'destroy';
                }
                if (config.maximized) {
                    notify({pkg:"Ext.window.Window", member:"maximized", type:"config",
                        msg:"maximized isn't working correctly in 4.0.2; there is still some space to the left of the window. " +
                            "The only way to correctly maximize is to win.show().maximize()."});
                }
            })
        });
    }
    
    /*-------------------------------------------------------------
     * Forms
     *-------------------------------------------------------------*/
    if (Ext.form.Basic) {
        Ext.apply(Ext.form.Basic.prototype, {
            add: function() {
                deprecate({pkg:'Ext.form.Basic', member:'add'});
                return this;
            },
            
            remove: function(field) {
                deprecate({pkg:'Ext.form.Basic', member:'remove'});
                return this;
            },
            
            cleanDestroyed: function() {
                deprecate({pkg:'Ext.form.Basic', member:'cleanDestroyed'});
            },
            
            render: function() {
                deprecate({pkg:'Ext.form.Basic', member:'render'});
                return this;
            },
            
            // It looks like this is still supported with a slightly different use
            // case than getValues according to the current API docs:
//            getFieldValues: function(dirtyOnly) {
//                deprecate({pkg:'Ext.form.Basic', member:'getFieldValues', alt:'getValues'});
//                return this.getValues(false, dirtyOnly);
//            },
    
            callFieldMethod: function(fnName, args) {
                deprecate({pkg:'Ext.form.Basic', member:'callFieldMethod'});
    
                args = args || [];
                this.getFields().each(function(f) {
                    if (Ext.isFunction(f[fnName])) {
                        f[fnName].apply(f, args);
                    }
                });
                return this;
            }
        });
    }

    if (Ext.form.Panel) {
        Ext.apply(Ext.form.Panel.prototype, {
            monitorValid: false,
            monitorPoll: 200,
            
            initComponent: Ext.Function.createInterceptor(Ext.form.Panel.prototype.initComponent, function() {
                var me = this,
                    fieldDefaultsProps = {
                        hideLabels: 'hideLabel',
                        labelAlign: 'labelAlign',
                        labelPad: 'labelPad',
                        labelSeparator: 'labelSeparator',
                        labelWidth: 'labelWidth'
                    },
                    fieldDefaults = me.fieldDefaults || (me.fieldDefaults = {});
    
                Ext.iterate(fieldDefaultsProps, function(from, to) {
                    if (from in me) {
                        deprecate({pkg:'Ext.form.Panel', member:from, type:'config', 
                            msg:'Use the fieldDefaults config object with a "' + to + '" property instead.'});
                            
                        fieldDefaults[to] = me[from];
                    }
                });
    
                if (me.hasOwnProperty('monitorValid')) {
                    deprecate({pkg:'Ext.form.Panel', member:'monitorValid', alt:'pollForChanges'});
                }
                if (me.hasOwnProperty('monitorPoll')) {
                    deprecate({pkg:'Ext.form.Panel', member:'monitorPoll', alt:'pollInterval'});
                }
            }),
            
            startMonitoring: function() {
                deprecate({pkg:'Ext.form.Panel', member:'startMonitoring', alt:'startPolling'});
                this.startPolling(this.monitorPoll);
            },
            
            stopMonitoring: function() {
                deprecate({pkg:'Ext.form.Panel', member:'stopMonitoring', alt:'stopPolling'});
                this.stopPolling();
            }
        });
    }

    if (Ext.form.field.Base) {
        Ext.apply(Ext.form.field.Base.prototype, {
            initComponent: Ext.Function.createInterceptor(Ext.form.field.Base.prototype.initComponent, function() {
                // Many legacy examples modify the default msgTarget on the Ext.form.Field class's prototype; this doesn't
                // work anymore since Field is a mixin. Copy to Ext.form.field.Base and inform about change and the new
                // recommended FormPanel.fieldDefaults. Only do this once rather than for every field.
                var msgTarget = Ext.form.Field.prototype.msgTarget;
                if (msgTarget && msgTarget !== 'qtip') {
                    deprecate({pkg:'Ext.form.Field', member:'msgTarget', type:'config', single: true,
                        msg:'Found an overridden value for Ext.form.Field.prototype.msgTarget -- Ext.form.Field is ' +
                            'now Ext.form.field.Base; either override msgTarget on Ext.form.field.Base\'s prototype ' +
                            'or use the new recommended Ext.form.Panel#fieldDefaults object instead.'});
                    
                    Ext.form.field.Base.prototype.msgTarget = Ext.form.Field.prototype.msgTarget;
                }
            }),
            constructor: Ext.Function.createInterceptor(Ext.form.field.Base.prototype.constructor, function(config) {
                if (config.fieldCls && config.fieldCls.indexOf(" ") !== -1) {
                    deprecate({pkg: "Ext.form.field.Base", member: "fieldCls", msg: "Ext4.0.2 does not support fieldCls with spaces. Autofixing"});
                    var temp = config.fieldCls;
                    delete config.fieldCls;
                    
                    if(!config.listeners){
                        config.listeners = {};
                    }
                    if(!config.listeners.afterrender){
                        config.listeners.afterrender = function(){this.inputEl.addCls(temp)}
                    }
                    else{
                        notify({pkg: "Ext.form.field.Base", member: "fieldCls", msg: "afterrender listener exists so replacement could not be done"});
                    }
                }
            })
        });
    }

    if (Ext.form.field.Checkbox) {
        Ext.apply(Ext.form.field.Checkbox.prototype, {
            initComponent: Ext.Function.createInterceptor(Ext.form.field.Checkbox.prototype.initComponent, function() {
                this.addEvents(
                    /**
                     * @event check
                     * Fires when the checkbox is checked or unchecked.
                     * @deprecated Use the 'change' event instead.
                     * @param {Ext.form.field.Checkbox} this This checkbox
                     * @param {Boolean} checked The new checked value
                     */
                    'check'
                );
                // TODO is there a clean way to throw a deprecation warning when the user listens for the check event?
            }),
            
            onChange: Ext.Function.createInterceptor(Ext.form.field.Checkbox.prototype.onChange, function(newVal, oldVal) {
                this.fireEvent('check', this, this.checked);
            })
        });
    }

    if (Ext.form.CheckboxGroup) {
        var cbgSetValue = Ext.form.CheckboxGroup.prototype.setValue;
    
        Ext.apply(Ext.form.CheckboxGroup.prototype, {
    
            initComponent: Ext.Function.createInterceptor(Ext.form.CheckboxGroup.prototype.initComponent, function() {
                var me = this,
                    items = me.items;
    
                // Handle the old structure where the 'items' could be a set of column configs
                if (items && items[0] && 'columnWidth' in items[0] && me.layout !== 'column') {
                    deprecate({pkg:'Ext.form.CheckboxGroup', type:'config',
                        msg:'CheckboxGroup and RadioGroup no longer accept implicit column containers in the "items" ' +
                            'config. If you wish to use a custom column arrangement, set layout:"column" and create ' +
                            'a standard items structure with container xtypes.'});
                    me.layout = 'column';
                    Ext.Array.forEach(items, function(column) {
                        column.xtype = 'container';
                        column.defaultType = me.defaultType;
                    });
                }
            }),
            
            setValue: function(id, value) {
                var me = this,
                    f;
                if (arguments.length === 1) {
                    value = id;
                    if (Ext.isObject(value)) {
                        cbgSetValue.call(me, value);
                    }
                    if (Ext.isString(value)) {
                        deprecate({pkg:'Ext.form.CheckboxGroup', member:'setValue', 
                            msg:'The setValue method no longer accepts a String argument. Use the new Object form instead.'});
                            
                        me.setValueForItem(value);
                    }
                    else if (Ext.isArray(value)) {
                        deprecate({pkg:'Ext.form.CheckboxGroup', member:'setValue', 
                            msg:'The setValue method no longer accepts an Array argument. Use the new Object form instead.'});
                            
                        me.batchChanges(function() {
                            Ext.each(value, function(val, idx){
                                if (Ext.isObject(val) && val.setValue) { // array of checkbox components to be checked
                                    val.setValue(true);
                                }
                                else if (Ext.isString(val)) {
                                    f = me.getBox(val);
                                    if (f) {
                                        f.setValue(true);
                                    }
                                }
                                else { // an array of boolean values
                                    var item = me.getBoxes()[idx];
                                    if (item) {
                                        item.setValue(val);
                                    }
                                }
                            });
                        });
                    }
                }
                else {
                    deprecate({pkg:'Ext.form.CheckboxGroup', member:'setValue', 
                        msg:'The setValue method no longer accepts a two-argument form. Use the new single Object form instead.'});
                        
                    f = me.getBox(id);
                    if (f) {
                        f.setValue(value);
                    }
                }
    
                return me;
            },
    
            // private
            setValueForItem : function(val){
                deprecate({pkg:'Ext.form.CheckboxGroup', member:'setValueForItem'});
                var me = this;
                val = String(val).split(',');
                me.batchChanges(function() {
                    me.eachBox(function(item) {
                        if (val.indexOf(item.inputValue) > -1) {
                            item.setValue(true);
                        }
                    });
                });
            },
    
            // private
            getBox : function(id){
                deprecate({pkg:'Ext.form.CheckboxGroup', member:'getBox'});
                var box = null;
                this.eachBox(function(f) {
                    if (id == f || f.dataIndex == id || f.id == id || f.getName() == id) {
                        box = f;
                        return false;
                    }
                });
                return box;
            }
        });
    }


    /*-------------------------------------------------------------
     * CompositeField
     *-------------------------------------------------------------*/
    if (Ext.form.FieldContainer) {
        Ext.define('Ext.form.CompositeField', {
            extend: 'Ext.form.FieldContainer',
            alias: 'widget.compositefield',
            uses: ['Ext.layout.container.HBox'],
    
            isComposite: true,
            combineErrors: true,
    
            layout: {
                type: 'hbox',
                defaultMargins: {top: 0, right: 5, bottom: 0, left: 0}
            },
            baseDefaults: {
                hideLabel: true
            },
    
            initComponent: function() {
                deprecate({member:'Ext.form.CompositeField', alt:'Ext.form.FieldContainer',
                    msg:'What used to be CompositeField has been replaced by the more flexible FieldContainer. '+
                        'We will reintroduce a true Composite field in a future release.'});
    
                this.defaults = Ext.apply({}, this.defaults, this.baseDefaults);
    
                this.callParent(arguments);
            }
        });
    }
    
    
    /*-------------------------------------------------------------
     * ComboBox
     *-------------------------------------------------------------*/
    if (Ext.form.field.ComboBox) {
        Ext.apply(Ext.form.field.ComboBox.prototype, {
            initComponent: Ext.Function.createInterceptor(Ext.form.field.ComboBox.prototype.initComponent, function() {
                var me = this,
                    isDef = Ext.isDefined;
                
                // shortcut for configs that just changed names:
                var remap = function(cfg, alt){
                    if(isDef(me[cfg])){
                        deprecate({pkg:'Ext.form.field.ComboBox', member:cfg, type:'config', alt:alt});
                        me[alt] = me[cfg];
                        delete me[cfg];
                    }
                };
                remap('listAlign', 'pickerAlign');
                remap('mode', 'queryMode');
                remap('triggerClass', 'triggerCls');
    
                // shortcut for configs that were moved into the listConfig object:
                var listConfig = me.listConfig || (me.listConfig = {}),
                remapToListConfig = function(cfg, alt) {
                    if(isDef(me[cfg])){
                        // the defaultListConfig has been applied at this point, so check that this 
                        // option was not simply the default value applied by the superclass
                        if(!isDef(me.defaultListConfig[cfg]) || me.defaultListConfig[cfg] !== me[cfg]) {
                            deprecate({pkg:'Ext.form.field.ComboBox', member:cfg, type:'config', alt:'listConfig.' + alt});
                            listConfig[alt] = me[cfg];
                            delete me[cfg];
                        }
                    }
                };
                remapToListConfig('itemSelector', 'itemSelector');
                remapToListConfig('listClass', 'cls');
                remapToListConfig('listWidth', 'width');
                remapToListConfig('loadingText', 'loadingText');
                remapToListConfig('minHeight', 'minHeight');
                remapToListConfig('minListWidth', 'minWidth');
                remapToListConfig('maxHeight', 'maxHeight');
                remapToListConfig('resizable', 'resizable');
                remapToListConfig('selectedClass', 'selectedItemCls');
                remapToListConfig('shadow', 'shadow');
    
                // shortcut for configs that were completely removed with no replacement:
                var remove = function(cfg){
                    if(isDef(me[cfg])){
                        notify({pkg:'Ext.form.field.ComboBox', member:cfg,
                            msg:'This config is no longer needed and has no replacement -- just remove it from your code.'});
                        delete me[cfg];
                    }
                };
                remove('autoCreate');
                remove('clearFilterOnReset');
                remove('handleHeight');
                remove('hiddenId');
                remove('hiddenName');
                remove('lazyInit');
                remove('lazyRender');
                remove('title');
                
                // non-standard mappings:
                if(isDef(me.tpl)){
                    deprecate({pkg:'Ext.form.field.ComboBox', member:'tpl', type:'config', alt:'getInnerTpl (method)',
                        msg:'There is no config for providing the combo\'s item template now. Instead, you should override '+
                            'listConfig.getInnerTpl = function(){ return '+(Ext.isString(me.tpl) ? 'XTemplate': 'XTemplate.html') +'}'});
                    
                    // make sure we are returning a template string and not an XTemplate instance:
                    var tpl = me.tpl.html ? me.tpl.html : me.tpl;
                    if(!me.listConfig){
                        me.listConfig = {};
                    }
                    me.listConfig.getInnerTpl = function(){
                        return tpl;
                    };
                    delete me.tpl;
                }
            })
        });
    }
    
    /*-------------------------------------------------------------
     * Slider
     *-------------------------------------------------------------*/
    if (Ext.slider.Multi) {
        Ext.apply(Ext.slider.Multi.prototype, {
            initComponent: Ext.Function.createInterceptor(Ext.slider.Multi.prototype.initComponent, function() {
                if (this.plugins) {
                    Ext.each(this.plugins, function(p){
                        if (p.getText) {
                            deprecate({pkg:'Ext.Slider', msg:'In 3.x the Ext.slider.Tip plugin was required to provide custom slider tip text. '+
                                'In 4.0 you should instead supply the tipText config directly.'});
                                
                            this.tipText = p.getText;
                            Ext.Array.remove(this.plugins, p);
                            return;
                        }
                    }, this); 
                }
            })
        });
    }

    /*-------------------------------------------------------------
     * Store
     *-------------------------------------------------------------*/
    if (Ext.data.Store) {
        var oldLoadData = Ext.data.Store.prototype.loadData;
        
        Ext.apply(Ext.data.AbstractStore.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.data.AbstractStore.prototype.constructor, function(config) {
                config = config || {};
                
                if (this.$className == 'Ext.data.NodeStore') {
                    return;
                }
                if (config.url) {
                    deprecate({pkg:'Ext.data.Store', member:'url', type:'config', alt:'proxy.url',
                        msg:'The store\'s "url" config should now be passed as a config to a valid remote-style proxy.'});
                            
                    if (!config.proxy) {
                        deprecate({pkg:'Ext.data.Store', msg:'A store url was specified with no proxy config. Implcitily creating an AjaxProxy with that url. '+
                            'Please see the header docs for Ext.data.Store for details on properly setting up your data components.'});
                        
                        config.proxy = {
                            type: 'ajax',
                            url: config.url
                        };
                        delete config.url;
                        
                        if (config.reader) {
                            config.proxy.reader = config.reader;
                            delete config.reader;
                            deprecate({pkg:'Ext.data.Store', member:'reader', type:'config', msg:'As part of creating an implicit AjaxProxy for compatibility, '+
                                'the store\'s existing reader config has also been moved to the proxy. Note that the reader config should no longer be passed '+
                                'directly as a store config, but should be specified on the proxy instead.'});
                        }
                    }
                }
                else if (!this.model && !config.model){
                    // there is no model set, so we need to try the various possible configurations supported by 3.x
                    // and hopefully find something we can convert into an implicit model
                    var fields;
                    if (config.fields) {
                        // shorthand store classes like ArrayStore and XmlStore support fields directly on the store config
                        fields = config.fields;
                        delete config.fields;
                        // this is required to be done, but skip the warning. In some cases TreeStore internally adds this. The bigger picture
                        // issue of configuring the store correctly will already be covered by other warnings.
//                        deprecate({pkg:'Ext.data.Store', msg:'Passing a "fields" config directly on the store\'s config is no longer supported. '+
//                            'Instead you should configure a model and pass it as the store\'s "model" config. ' +
//                            'Please see the header docs for Ext.data.Store for details on properly setting up your data components.'});
                    }
                    else if (config.reader) {
                        if (config.reader.model) {
                            // the compat warning for this case is displayed below in the Ext.data.Reader override where
                            // reader.model is set. This code is just here to make it work properly.
                            config.model = config.reader.model;
                            delete this.fields;
                            this.implicitModel = true;
                            return true;
                        }
                        else if (config.reader.fields) {
                            // standard stores typically get fields from the reader config
                            fields = config.reader.fields;
                            delete config.reader.fields;
                            deprecate({pkg:'Ext.data.Store', msg:'Passing a "fields" config via the store\'s reader config is no longer supported. '+
                                'Instead you should configure a model and pass it as the store\'s "model" config. ' +
                                'Please see the header docs for Ext.data.Store for details on properly setting up your data components.'});
                        }
                        else {
                            breaking({pkg:'Ext.data.Store', msg:'No valid model or field configuration could be found '+
                                'so this store could not be constructed. Please see the header docs for Ext.data.Store for '+
                                'details on properly setting up your data components.'});
                            
                            return false;
                        }
                        if (config.proxy) {
                            config.proxy.reader = config.reader;
                            delete config.reader;
                            deprecate({pkg:'Ext.data.Store', member:'reader', type:'config', msg:'The reader config should now be specified on the '+
                                'configured proxy rather than directly on the store.'});
                        }
                    }
                    else {
                        // we should never get here, but just in case
                        breaking({pkg:'Ext.data.Store', msg: 'A store was specified with no model, url, or fields configured. '+
                            'Please see the header docs for Ext.data.Store for details on properly setting up your data components.'});
                        
                        return false;
                    }
    
                    var pn = config.paramNames;                
                    if (config.proxy && pn) {
                        Ext.apply(config.proxy, {
                            startParam: pn.start || 'start',
                            limitParam: pn.limit || 'limit',
                            sortParam : pn.sort || 'sort',
                            directionParam  : pn.dir || 'dir'
                        });
                        deprecate({pkg:'Ext.data.Store', member:'paramNames', msg:'This is now split out into individual configs at the proxy '+
                            'level (e.g., paramNames.start == proxy.startParam). Set each config directly on the proxy as needed.'})
                    }
                    
                    var id = 'Ext.data.Store.ImplicitModel-' + (config.storeId || config.id || Ext.id());
                    notify({pkg:'Ext.data.Store', msg:'Registering implicit model ' + id +
                        '. This is OK if it\'s intentional, but this could also be due to your Ext 3.x record definition being ' +
                        'defaulted in an unexpected fashion. If you were using a specific record defintion previously and are ' +
                        'getting this warning you should double-check that you have a valid Ext 4 model defintion (otherwise you ' +
                        'can ignore this warning.'});
                    
                    config.model = Ext.define(id, {
                        extend: 'Ext.data.Model',
                        fields: fields,
                        proxy: config.proxy
                    });
                    this.implicitModel = true;
                }
                if (config.baseParams) {
                    config.proxy.extraParams = config.baseParams;
                    delete config.baseParams;
                    deprecate({pkg:'Ext.data.Store', member:'extraParams', type:'config', msg:'Use store.proxy.extraParams instead of store.baseParams.'});
                }
                if (config.extraParams) {
                    config.proxy.extraParams = config.extraParams;
                    delete config.extraParams;
                    deprecate({pkg:'Ext.data.Store', member:'extraParams', type:'config', msg:'The extraParams needs to be defined in the store\'s proxy.'});
                }
                if (config.groupField) {
                    var groupField = Ext.ModelManager.getModel(config.model).prototype.fields.get(config.groupField),
                        sortTypeInput = "ZACH";
                        
                    if (groupField && groupField.sortType && groupField.sortType(sortTypeInput) !== sortTypeInput) {
                        deprecate({pkg:"Ext.data.Store", member:"groupField", type:"config", msg:"The sortType for a field should not be used in conjunction with groupField." +
                            "Move the sortType and groupField to a grouper. (e.g. {groupField:'XYZ', fields:[{name:'XYZ', sortType:function(){}}]} -> {groupers:[{property:'XYZ', sorterFn:function(){}}]})"});
                    }
                }
                if (config.proxy && !config.proxy.type) {
                    var msg = 'You have configured your proxy without a type. ';
                    
                    if(config.proxy.url)    {
                        config.proxy.type = "ajax";
                        msg += 'Automatically aliasing to "type: \'ajax\' based on url: ' + config.proxy.url;
                    }
                    else {
                        config.proxy.type = "localstorage";
                        config.proxy.id = (config.storeId || "LocalStorageProxy-" + Ext.id());
                        msg += 'No url found, defaulting to "type: \'localstorage\'".';
                    }
                    notify({pkg:"Ext.data.Store", member:"proxy.type", msg:msg});
                }
                if (config.idProperty) {
                    deprecate({pkg:"Ext.data.Store", member:"idProperty",
                        msg:"idProperty should be in either the store's model or its reader. Moving to store's reader."});
                        
                    if(config.proxy && !config.proxy.reader){
                        config.proxy.reader = {};
                    }
                    var temp = config.idProperty;
                    config.proxy.reader.idProperty = temp;
                    delete config.idProperty;
                }
            }),
            reload: function() {
                deprecate({pkg:"Ext.data.Store", member:"reload", msg:"Use store.load() instead of store.reload()."});
                return this.load();
            },
            filter: Ext.Function.createInterceptor(Ext.data.AbstractStore.prototype.filter, function() {
                notify({pkg:"Ext.data.AbstractStore", member:"filter",
                    msg:"Right now filter isn't correctly replacing the old filter with the new one. A workaround is to call " +
                        "clearFilter() before filter(), but that may not work in all cases."});
            })
        });
        
        Ext.apply(Ext.data.Store.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.data.Store.prototype.constructor, function(config) {
                config = config || {};
                if (config.data && Ext.isObject(config.data)) {
                // Seems to be still supported officially for now
//                    deprecate({pkg:'Ext.data.Store', member:'data<Object>', type:'config', alt:'data<Array>',
//                        msg:'Passing inline data to store\'s constructor as an object is no longer supported. Pass a '+
//                            'plain array of record data or use one of the standard proxy configurations for loading data.'});
                    
                    if(config.root){
                        this.inlineData = config.data[config.root];
                        delete config.data;
                    }
//                    else {
//                        breaking({pkg:'Ext.data.Store', 
//                            msg:'Passing inline data as an object to the Store constructor without specifying a root property is not supported.'});
//                    }
                }
                if (config.sortInfo) {
                    deprecate({pkg:'Ext.data.Store', member:'sortInfo', type:'config', alt:'sorters'});
                    config.sorters = [{
                        property: config.sortInfo.field,
                        direction: config.sortInfo.direction
                    }];
                }
                if (config.autoSave) {
                    deprecate({pkg:'Ext.data.Store', member:'autoSave', type:'config', alt:'autoSync'});
                    this.autoSync = config.autoSave;
                    delete config.autoSave;
                }
            }),
            
            setDefaultSort : function(field, dir) {
                deprecate({pkg:'Ext.data.Store', member:'setDefaultSort', alt:'sorters (config)',
                    msg:'Either add the default sort via the "sorters" config or by adding it to the "sorters" property after the store is created. '+
                        'See the Ext.data.Store header docs for details on configuring sorters.'});
                
                this.sorters = new Ext.util.MixedCollection();
                this.sorters.add(new Ext.util.Sorter({
                    property: field,
                    direction: dir ? dir.toUpperCase() : 'ASC'
                }));
            },
            
            save: function() {
                deprecate({pkg:'Ext.data.Store', member:'save', alt:'sync'});
                return this.sync.apply(this, arguments);
            },
            
            loadData:function (records) {
                if (Ext.isObject(records)) {
                    deprecate({pkg:"Ext.data.AbstractStore", member:"loadData", msg:"Passing loadData an object is no longer supported.  Now just pass the array"});
                    records = records[this.proxy.root||this.root||"rows"];
                }
                oldLoadData.apply(this, arguments);
            },
            
            commitChanges:function () {
                deprecate({pkg:"Ext.data.Store", member:"commitChanges", msg:"Instead of calling commitChanges on the store loop through the data and "+
                "call commit on each record. e.g store.data.each(function(record,index,data){record.commit();})"});
                this.data.each(function(record,index,data){record.commit();});
            
            }
        });
        
        Ext.Compat.bindProperty({owner:Ext.data.Store, name:'recordType',
            getter: function(){
                return this.model;
            },
            getterMsg: function(){
                deprecate({pkg:'Ext.data.Store', member:'recordType', type:'property', alt:'model'});
            }
        });
    }
    
    if (Ext.data.JsonStore) {
        // TODO: Move this override into the lib?
        Ext.apply(Ext.data.JsonStore.prototype, {
            constructor: function(config) {
                config = config || {};
                config.proxy = config.proxy || {};
                
                if(!config.proxy.type)  {
                    deprecate({pkg:"Ext.data.proxy.Proxy", member:"type",
                        msg:"You have defined a proxy without a type. More than likely the type should be 'ajax' and will be autocorrected to it."});
                }
                
                Ext.applyIf(config.proxy, {
                    url   : config.url,
                    type  : 'ajax',
                    writer: 'json',
                    reader: new Ext.data.JsonReader(config)
                });
                Ext.data.JsonStore.superclass.constructor.call(this, config);
            }
        });
    }
    
    Ext.define("Ext.data.GroupingStore", {
        extend:"Ext.data.Store",
        constructor:function(config) {
            deprecate({pkg:'Ext.data.GroupingStore', msg:'GroupingStore no longer exists as a separate class. Instead just '+
                'create a standard GridPanel and include the Grouping feature, e.g. "features: Ext.create("Ext.grid.feature.Grouping", {...})'});
        }
    });
    
    /*-------------------------------------------------------------
     * Record
     *-------------------------------------------------------------*/
    if (Ext.data.Record) {
        Ext.data.Record.create = function(o){
            deprecate({pkg:'Ext.data.Record', member:'create', msg:'There is no longer any need to statically define records. '+
                'You can simply define a new Model configured with the necessary fields via Ext.define, extending Ext.data.Model.'});
                
            var f = Ext.extend(Ext.data.Record, {});
            var p = f.prototype;
            p.fields = new Ext.util.MixedCollection(false, function(field){
                return field.name;
            });
            for(var i = 0, len = o.length; i < len; i++){
                p.fields.add(new Ext.data.Field(o[i]));
            }
            f.getField = function(name){
                return p.fields.get(name);
            };
            return f;
        };
    }
    
    /*-------------------------------------------------------------
     * Readers
     *-------------------------------------------------------------*/
    if (Ext.data.JsonReader) {
        Ext.data.JsonReader.override({
            //TODO: seems to be a bug in the class system that this is required for the Reader override 
            constructor: function(){
                this.callParent(arguments);
            }
        });
    }
    
    if (Ext.data.Reader) {
        Ext.apply(Ext.data.Reader.prototype, {
            constructor: function(config, recordType) {
                Ext.apply(this, config || {});
        
                if (config.fields) {
                    // this will get converted to an implicit model in the store constructor
                    deprecate({pkg:'Ext.data.Reader', member:'fields', type:'config',
                        msg:'The fields config is no longer supported. Please refer to the '+
                            'Ext.data.Store header docs for the proper way to set up your data components.'});
                }
                if (recordType) {
                    // this will get converted to an implicit model in the store constructor
                    config.fields = recordType;
                    deprecate({pkg:'Ext.data.Reader', member:'recordType', type:'arg',
                        msg:'The recordType argument to the Reader constructor is no longer supported. Please refer to the '+
                            'Ext.data.Store header docs for the proper way to set up your data components.'});
                }
                
                if (config.model) {
                    this.model = Ext.ModelManager.getModel(config.model);
                }
                else if (config.fields) {
                    this.model = Ext.define('Ext.data.Store.ImplicitModel-' + Ext.id(), {
                        extend: 'Ext.data.Model',
                        fields: config.fields
                    });
                }
                // This is not always true, e.g. with inline array data:
//                else {
//                    breaking({pkg:'Ext.data.Reader', 
//                        msg:'No valid model or field configuration could be found so this reader could not be constructed.'});
//                }
                
                if (this.model) {
                    this.buildExtractors();
                }
            }
        });
    }
    
    if (Ext.data.XmlReader) {
        Ext.apply(Ext.data.XmlReader.prototype, {
            // FYI, this entire constructor is now deprecated because all behavior is now in the superclass constructor
            constructor: function(config, recordType) {
                config = config || {};
                if (config.idPath) {
                    config.idProperty = config.idPath;
                    deprecate({pkg:'Ext.data.XmlReader', member:'idPath', type:'config', alt:'idProperty'});
                }
                if (config.id) {
                    config.idProperty = config.id;
                    deprecate({pkg:'Ext.data.XmlReader', member:'id', type:'config', alt:'idProperty'});
                }
                if (config.success) {
                    config.successProperty = config.success;
                    deprecate({pkg:'Ext.data.XmlReader', member:'success', type:'config', alt:'successProperty'});
                }
                // make sure we pass arguments in case the deprecated recordType arg is included
                Ext.data.XmlReader.superclass.constructor.apply(this, arguments);
            }
        });
    }
    
    /*-------------------------------------------------------------
     * Proxies
     *-------------------------------------------------------------*/
    if (Ext.data.ServerProxy) {
        Ext.apply(Ext.data.ServerProxy.prototype, {
            constructor:Ext.Function.createInterceptor(Ext.data.ServerProxy.prototype.constructor, function(inConfig) {
				inConfig = inConfig || {};
				
                if(typeof(inConfig.simpleSortMode) != "undefined")  {
                    this.simpleSortModeDefined = true;
                }
            }),
            getParams: Ext.Function.createInterceptor(Ext.data.ServerProxy.prototype.getParams, function(params, operation) {
                // In 4.0.4 this method was changed to accept an operation as the only argument
                if (params && params.getResultSet) {
                    // this is actually an operation, so it must be a 4.0.4+ call
                    operation = params;
                }
                
                if (this.sortParam && operation.sorters && operation.sorters.length > 0) {
                    if (!this.simpleSortMode && !this.simpleSortModeDefined) {
                        Ext.Compat.warn('ServerProxy now supports multiple sort, so if any sort options are specified '+
                            'the sort params get JSON-encoded by default. Unless you have specifically coded for this on '+
                            'the server it will not work and you should set "simpleSortMode = true" on the proxy. Since '+
                            'this was not supported in Ext 3 and you are passing a sort param, simple sorting is assumed '+
                            'and has been set automatically, but you should reexamine this code as you migrate to Ext 4. '+
                            'For now just set "simpleSortMode: true" on your proxy to dismiss this warning.');
                    }
                }
            })
        });
    }
    
    if (Ext.data.MemoryProxy) {
        Ext.apply(Ext.data.MemoryProxy.prototype, {
            read: Ext.Function.createInterceptor(Ext.data.MemoryProxy.prototype.read, function(op, cb, scope) {
                if (this.doRequest) {
                    deprecate({pkg:'Ext.data.MemoryProxy', member:'doRequest', alt: 'read',
                        msg:'ClientProxy subclasses no longer implement doRequest.'});
                        
                    var params = {
                        start: op.start, 
                        limit: op.limit
                    };
                    if (op.sorters && op.sorters.length > 0) {
                        var idx = op.sorters.length-1; // take the last sort if multiple
                        params[this.sortParam || 'sort'] = op.sorters[idx].property;
                        params[this.directionParam || 'dir'] = op.sorters[idx].direction;
                    }
                    if (op.filters && op.filters.length > 0) {
                        // not sure if we can compat this
                        //params[this.filterParam || 'filter'] = ??;
                    }
                    this.doRequest(op.action, op.getRecords(), params, this.getReader(), function(result, options){
                        Ext.apply(op, {
                            resultSet: result
                        });
                        op.setCompleted();
                        op.setSuccessful();
                        Ext.callback(cb, scope || this, [op]);
                    }, scope);
                    
                    return false; // skip original read logic
                }
            })
        });
    }
    
    /*-------------------------------------------------------------
     * Model
     *-------------------------------------------------------------*/
    if (Ext.data.Model) {
        Ext.apply(Ext.data.Model.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.data.Model.prototype.constructor, function(data, id) {
                var newData = {};
                if (Ext.isArray(data)){
                    // Support for loading an array, needed for calling loadData on an ArrayStore
                    var fields = this.fields.items,
                        length = fields.length,
                        field, name, 
                        i = 0, 
                        newData = {};
                        
                    for (; i < length; i++) {
                        field = fields[i];
                        name  = field.name;
                        newData[name] = data[i];
                    }
                    data = newData;
                }
            }),
            
            initComponent: Ext.Function.createInterceptor(Ext.data.Model.prototype.initComponent, function() {
                // Needed to bootstrap 3.x stores that use id. Once converted to a model this will
                // not be needed, so there's no need for a separate warning, just a temp shim.
                this.id = this.internalId;
            })
        });
    }
    
    /*-------------------------------------------------------------
     * Other Ext.data.* stuff
     *-------------------------------------------------------------*/
    if (Ext.data.Operation) {
        Ext.apply(Ext.data.Operation.prototype, {
            markStarted: function() {
                deprecate({pkg:'Ext.data.Operation', member:'markStarted', alt:'setStarted'});
                return this.setStarted();
            },
            markCompleted: function() {
                deprecate({pkg:'Ext.data.Operation', member:'markCompleted', alt:'setCompleted'});
                return this.setCompleted();
            },
            markSuccessful: function() {
                deprecate({pkg:'Ext.data.Operation', member:'markSuccessful', alt:'setSuccessful'});
                return this.setSuccessful();
            },
            markException: function() {
                deprecate({pkg:'Ext.data.Operation', member:'markException', alt:'setException'});
                return this.setException();
            }
        });
    }
    
    /*-------------------------------------------------------------
     * Tooltip
     *-------------------------------------------------------------*/
    if (Ext.tip.ToolTip) {
        Ext.apply(Ext.tip.ToolTip.prototype, {
            initTarget: function(target) {
                deprecate({pkg:'Ext.ToolTip', member:'initTarget', alt:'setTarget'});
                return this.setTarget(target);
            }
        });
    }
    
    if (Ext.TaskManager) {
        Ext.TaskMgr = function() {
            deprecate({pkg:'Ext.TaskMgr', alt:'Ext.TaskManager'});
            return Ext.TaskManager;
        }
        Ext.TaskMgr.start = Ext.TaskManager.start;
        Ext.TaskMgr.stop = Ext.TaskManager.stop;
        Ext.TaskMgr.stopAll = Ext.TaskManager.stopAll;
    }
    
    /*-------------------------------------------------------------
     * Chart
     *-------------------------------------------------------------*/
    if (Ext.chart.Chart) {
        Ext.apply(Ext.chart.Chart.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.chart.Chart.prototype.constructor, function(inConfig) {
                var invalidTitles = [];

				inConfig = inConfig || {};
                
                if(inConfig.southTitle) {
                    invalidTitles.push("south");
                }
                if(inConfig.northTitle) {
                    invalidTitles.push("north");
                }
                if(inConfig.westTitle)  {
                    invalidTitles.push("west");
                }
                if(inConfig.eastTitle)  {
                    invalidTitles.push("east");
                }
                
                if(invalidTitles.length)    {
                    invalidTitles = invalidTitles.join("Title") + "Title";
                    deprecate({pkg:"Ext.chart.Chart", member:invalidTitles, type:"config",
                        msg:"The properties " + invalidTitles + " must now be specified in the axes config rather than directly through the config."});
                }
                
                if(inConfig.xField) {
                    deprecate({pkg:"Ext.chart.Chart", member:"xField", type:"config",
                        msg:"The xField property must now be contained in an axis rather than in the chart config."});
                }
                
                if(inConfig.yField) {
                    deprecate({pkg:"Ext.chart.Chart", member:"yField", type:"config",
                        msg:"The yField property must now be contained in an axis rather than in the chart config."});
                }
                
                if(inConfig.defaultSeriesType)  {
                    deprecate({pkg:"Ext.chart.Chart", member:"defaultSeriesType", type:"config",
                        msg:"The defaultSeriesType has been deprecated. No analogy exists yet."});
                }
            })
        });
    }
    
    /*-------------------------------------------------------------
     * Message Box
     *-------------------------------------------------------------*/
    if (Ext.MessageBox) {
        Ext.apply(Ext.MessageBox, {
            show: Ext.Function.createInterceptor(Ext.MessageBox.show, function(inConfig) {
				inConfig = inConfig || {};
				
                if(inConfig.buttons && typeof(inConfig.buttons) == "object")    {
                    deprecate({pkg:"Ext.MessageBox", member:"show.buttons", type:"function",
                        msg:"Ext.MessageBox no longer supports custom button text by default. This will get resolved in a future version of Ext " +
                            "but for now the compat layer will attempt to fix this."});
                        
                    var originalText = {},
                        newButtonsInt = 0;
                        restoreOriginalText = function()    {
                            for(var key in originalText)    {
                                Ext.MessageBox.msgButtons[key].setText(originalText[key]);
                            }
                        };
                    
                    for(var key in inConfig.buttons)    {
                        var value = inConfig.buttons[key],
                            idIndex = Ext.Array.indexOf(Ext.MessageBox.buttonIds, key),
                            button = Ext.MessageBox.msgButtons[idIndex];
                        key = key.toUpperCase();
                        originalText[idIndex] = button.text;
                        button.setText(value);
                        newButtonsInt += Ext.MessageBox[key];
                    }
                    
                    inConfig.fn = (inConfig.fn ? Ext.Function.createInterceptor(inConfig.fn, restoreOriginalText) : restoreOriginalText);
                    inConfig.buttons = newButtonsInt;
                }
            })
        });
    }
    
    /*-------------------------------------------------------------
     * Button
     *-------------------------------------------------------------*/
    if (Ext.button.Button) {
        Ext.apply(Ext.button.Button.prototype, {
            constructor: Ext.Function.createInterceptor(Ext.button.Button.prototype.constructor, function(inConfig) {
				inConfig = inConfig || {};
				
                if (inConfig.url && inConfig.handler)    {
                    notify({pkg:"Ext.button.Button", member:"url", type:"config",
                        msg:"This button has been configured with a URL, which will render it as a link, and a handler, which will also be called on click. " +
                            "Please change the button's config to be button.href instead and use this.href inside the handler. This will probably produce unexpected results."});
                }
            })
        });
    }
    
    /*-------------------------------------------------------------
     * XTemplate
     *-------------------------------------------------------------*/
    if (Ext.XTemplate) {
        var oldApplyTemplate = Ext.XTemplate.prototype.applyTemplate;
    
        Ext.apply(Ext.XTemplate.prototype, {
            applyTemplate: function(inValues) {
                var returnValue = oldApplyTemplate.apply(this, arguments);
                
                if (returnValue.indexOf("ext:q") !== -1) {
                    returnValue = returnValue.replace(/ext:q/g, "data-q");
                    deprecate({pkg:"Ext.XTemplate", member:"ext:qtip",
                        msg:"Use data-qtip instead of ext:qtip, data-qwidth instead of ext:qwidth, etc. This error is often hard to trace back, " +
                            "so search for 'ext:q' in your code. This may be being set by a column's renderer."});
                }
                return returnValue;
            }
        });
    }
    
    /*-------------------------------------------------------------
     * util.JSON
     *-------------------------------------------------------------*/
    if (Ext.JSON) {
        Ext.util.JSON = {};
        for(var key in Ext.JSON)    {
            var value = Ext.JSON[key];
            
            if(typeof(value) == "function") {
                Ext.util.JSON[key] = function() {
                    deprecate({pkg:"Ext.util.JSON", member:key,
                        msg:"Use Ext.JSON." + key + " instead. Automatically fixing."});
                        
                    return value.apply(Ext.JSON, arguments);
                };
            }
        }
    }
    
    /*-------------------------------------------------------------
     * util.Function
     *-------------------------------------------------------------*/
    if (!Ext.util.Functions) {
        Ext.util.Functions = {
            createDelegate:function()   {
                deprecate({pkg:"Ext.util.Functions", member:"createDelegate",
                    msg:"Use Ext.Function.bind instead. Automatically fixing."});
                return Ext.Function.bind.apply(Ext.Function, arguments);
            },
            createInterceptor:function()    {
                deprecate({pkg:"Ext.util.Functions", member:"createInterceptor",
                    msg:"Use Ext.Function.createInterceptor instead. Automatically fixing."});
                return Ext.Function.createInterceptor.apply(Ext.Function, arguments);
            },
            createSequence:function()   {
                deprecate({pkg:"Ext.util.Functions", member:"createSequence",
                    msg:"Use Ext.Function.createSequence instead. Automatically fixing."});
                return Ext.Function.createSequence.apply(Ext.Function, arguments);
            },
            defer:function()    {
                deprecate({pkg:"Ext.util.Functions", member:"defer",
                    msg:"Use Ext.Function.defer instead. Automatically fixing."});
                return Ext.Function.defer.apply(Ext.Function, arguments);
            }
        };
    }
    
    /*-------------------------------------------------------------
     * Unsupported CSS Rules
     *-------------------------------------------------------------*/
    Ext.onReady(function() {
        var cssRules = Ext.util.CSS.getRules(),
            getCssText = function(inStyle) {
                if(inStyle.cssText) {
                    return inStyle.cssText;
                }
                return inStyle.selectorText + " {" + inStyle.style.cssText + "}";
            },
            updatedRules = "";
        
        for (var key in cssRules) {
            if (key.indexOf(".ext-") != -1) {
                var matches = key.match(/(ie6|ie7|ie8|ie|gecko2|gecko3|gecko|opera|webkit)/g);
                
                if (matches) {
                    notify("Browser version classes on the body have different prefixes now, e.g. '.ext-ie' is now '.x-ie'. Automatically fixing.");
                    
                    var cssText = getCssText(cssRules[key]);
                    for (var i = 0; i < matches.length; i++) {
                        var match = matches[i];
                        cssText = cssText.replace(new RegExp("\\.ext-" + match, "g"), ".x-" + match);
                    }
                    updatedRules += cssText;
                }
            }
        
            if (key.indexOf("x-grid3") != -1) {
                var notifyMessage = "The grid panel's class has changed from x-grid3 to just x-grid. Please update the " + key + " rule in your stylesheet. Automatically fixing.",
                    cssText = getCssText(cssRules[key]).replace(/x-grid3/g, "x-grid");
                    
                if (key.indexOf("x-grid-summary-row") != -1) { // x-grid3-summary-row -> x-grid-row-summary
                    cssText = cssText.replace(/x-grid-summary-row/g, "x-grid-row-summary");
                    notifyMessage += " (Note: x-grid3-summary-row -> x-grid-row-summary)";
                }
                
                notify(notifyMessage);
                
                updatedRules += cssText;
            }
        }
        
        if (updatedRules) {
            if (document.location.href.indexOf("COMPAT_STYLE_REPLACE=1") !== -1) {
                Ext.util.CSS.createStyleSheet(updatedRules, "compatLayerUnsupportedRules");
            }
        }
    });
})();


Ext.define('Ext.ux.PreviewPlugin', {
    extend: 'Ext.AbstractPlugin',
    alias: 'plugin.preview',
    requires: ['Ext.grid.feature.RowBody', 'Ext.grid.feature.RowWrap'],
    
    // private, css class to use to hide the body
    hideBodyCls: 'x-grid-row-body-hidden',
    
    /**
     * @cfg {String} bodyField
     * Field to display in the preview. Must me a field within the Model definition
     * that the store is using.
     */
    bodyField: '',
    
    /**
     * @cfg {Boolean} previewExpanded
     */
    previewExpanded: true,
    
    constructor: function(config) {
        this.callParent(arguments);
        var bodyField   = this.bodyField,
            hideBodyCls = this.hideBodyCls,
            section     = this.getCmp();
        
        section.previewExpanded = this.previewExpanded;
        section.features = [{
            ftype: 'rowbody',
            getAdditionalData: function(data, idx, record, orig, view) {
                var o = Ext.grid.feature.RowBody.prototype.getAdditionalData.apply(this, arguments);
                Ext.apply(o, {
                    rowBody: data[bodyField],
                    rowBodyCls: section.previewExpanded ? '' : hideBodyCls
                });
                return o;
            }
        },{
            ftype: 'rowwrap'
        }];
    },
    
    /**
     * Toggle between the preview being expanded/hidden
     * @param {Boolean} expanded Pass true to expand the record and false to not show the preview.
     */
    toggleExpanded: function(expanded) {
        var view = this.getCmp();
        this.previewExpanded = view.previewExpanded = expanded;
        view.refresh();
    }
});

Ext.chart.Chart.prototype.getFunctionRef = function(val){
    if(Ext.isFunction(val)){
        return {
            fn: val,
            scope: this
        };
    }else{
        return {
            fn: val.fn,
            scope: val.scope || this
        };
    }
};

Ext.chart.Chart.prototype.setTipRenderer = Ext.emptyFn;

Ext.Compat.debugErrors = (document.location.href.split("COMPAT_DEBUG=1").length > 1);
