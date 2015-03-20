/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.core");

/**
 * @class Sbi.cockpit.core.WidgetExtensionPointManager
 *
 * <p>This object provides a registry of available Widget's extensions indexed by a mnemonic code known as the Widget's <code>wtype</code>.
 * The widegt extension point is composed by two interfaces, one for the runtime part and one for the editing part.
 * In order to extend this extension point creating a new widget these interfaces must be properly implemented.
 * The following abstract class are available to be extended in order to simplify the definition of a new widget:
 *
 * <ul>
 * <li>{@link Sbi.cockpit.core.WidgetRuntime Sbi.cockpit.core.WidgetRuntime}</li> This abstarct class manages the runtime facet of the widget
 * <li>{@link Sbi.cockpit.core.WidgetDesigner Sbi.cockpit.core.WidgetDesigner}</li> This abstarct class manages the design facet of the widget
 * </ul>
 *
 * Once the two abstract classes has been implemented the new extension can be registered as shown in the following example:
 *
 * <pre><code>
Sbi.registerWidget('table', {
	name: 'Table'
	, icon: 'js/src/ext/sbi/cockpit/widgets/table/table_64x64_ico.png'
	, runtimeClass: 'Sbi.cockpit.widgets.table.TableWidget'
	, designerClass: 'Sbi.cockpit.widgets.table.TableWidgetDesigner'
});
</code></pre>
 *
 * <code>Sbi.registerWidget</code> is a shortcut to {@link Sbi.cockpit.core.WidgetExtensionPointManager#registerWidget registerWidget}. The list
 * of all available <b>shortcusts</b> is:
 *
 * <ul>
 * <li><code>Sbi.registerWidget</code> for {@link Sbi.cockpit.core.WidgetExtensionPointManager#registerWidget registerWidget}</li>
 * <li><code>Sbi.unregisterWidget</code> for {@link Sbi.cockpit.core.WidgetExtensionPointManager#unregisterWidget unregisterWidget}</li>
 * </ul>
 * @singleton
 */
Sbi.cockpit.core.WidgetExtensionPointManager = {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	registry: {}

	// =================================================================================================================
	// METHODS
	// =================================================================================================================
	/**
	 * @method
	 * Registers a widget extension.
	 *
	 * @param {String} wtype The mnemonic code of the registered widget extension.
	 * @param {Object} descriptor The object that describe the registered widget extension.
	 * @param {String} descriptor.name The descriptive name of the widget extension.
	 * @param {String} descriptor.icon The icon associate to the widget extension.
	 * @param {String} descriptor.runtimeClass The name of the class used as widget's runtime. It must extend Sbi.cockpit.core.WidgetRuntime
	 * @param {String} descriptor.designerClass The name of the class used as widget's designer. It must extend Sbi.cockpit.core.WidgetDesigner
	 */
	, registerWidget: function(wtype, descriptor) {
		Sbi.trace("[WidgetExtensionPoint.registerWidget]: IN");
		Sbi.debug("[WidgetExtensionPoint.registerWidget]: registered widget extension type [" + wtype + "]");
		Sbi.cockpit.core.WidgetExtensionPointManager.registry[wtype] = descriptor;
		Sbi.trace("[WidgetExtensionPoint.registerWidget]: OUT");
	}

	/**
	 * @method
	 * Unregisters the widget extension whose <code>wtype</code> is equal to the one passed in as argument.
	 *
	 * @param {String} wtype The wtype of the widget extension to unregister
	 *
	 * @return {Object} The descriptor of the unregistered widget extension (see #registerWidget to have more info about the structure of the descriptor object)
	 */
	, unregisterWidget: function(wtype) {
		Sbi.debug("[WidgetExtensionPoint.registerWidget]: unregistered widget extension type [" + wtype + "]");
		var wdescriptor = Sbi.cockpit.core.WidgetExtensionPointManager.registry[wtype];
		delete Sbi.cockpit.core.WidgetExtensionPointManager.registry[wtype];
		return wdescriptor;
	}

	/**
	 * @method
	 *
	 * Returns true if a widget extension of the passed in <code>wtype</code> is registered, false otherwise.
	 *
	 * @param {String} wtype The widget type
	 *
	 * @return {boolean} true if a widget of the specified type is registered
	 */
	, isWidgetRegistered: function(wtype) {
		var widgetDescriptor = this.getWidgetDescriptor(wtype);
		return  Sbi.isValorized(widgetDescriptor);
	}

	/**
	 * @method
	 *
	 * Returns the list of wtypes of registered widget extensions
	 *
	 * @return {String[]} the list of wtypes
	 */
	, getWidgetTypes: function() {
		var types = new Array();
		var registry = Sbi.cockpit.core.WidgetExtensionPointManager.registry;
		for(wtype in registry) {
			types.push(wtype);
		}
		return types;
	}

	/**
	 * @method
	 *
	 * Returns the list of all descriptors associated to the registered widget extensions. To see the inner structure of a descriptor object
	 * see #registerWidget method. The returned descriptors are anyway just a  copy of the ones internally used by <code>WidgetExtensionPointManager</code>
	 * so any modification made to them have no impact on the related extensions. To modify a descriptor associated to an extension type is necessary
	 * to unregister it and the register the modified version.
	 *
	 * @return {Object[]} the list of registered widgets descriptors
	 */
	, getWidgetDescriptors: function() {
		var descriptors = new Array();
		var registry = Sbi.cockpit.core.WidgetExtensionPointManager.registry;
		for(wtype in registry) {
			descriptors.push( Ext.apply({}, registry[wtype]) );
		}
		return descriptors;
	}

	/**
	 * @method
	 *
	 * Returns the descriptor associated to the registered widget extension associated to the <code>wtype</code> passed in as argument.
	 * To see the inner structure of a descriptor object see #registerWidget method. The returned descriptor is anyway just a
	 * copy of the one internally used by <code>WidgetExtensionPointManager</code> so any modification made to it have no impact
	 * on the related extensions. To modify a descriptor associated to an extension type is necessary
	 * to unregister it and the register the modified version.
	 *
	 * @return {Object} The descriptor associated to the <code>wtype</code> passed in as argument.
	 */
	, getWidgetDescriptor: function(wtype) {
		return  Sbi.cockpit.core.WidgetExtensionPointManager.registry[wtype];
	}

	/**
	 * @method
	 *
     * Executes the specified function once for every registered widget extension, passing the following arguments:
     * <div class="mdetail-params"><ul>
     * <li><b>wtype</b> : String<p class="sub-desc">The widget extension type</p></li>
     * <li><b>index</b> : Object<p class="sub-desc">The widget extension descriptor. It's not the original one, just a copy.
     * Any modification applied to it so have no impact on on the related extension</p></li>
     * <li><b>length</b> : Number<p class="sub-desc">The total number of items in the collection</p></li>
     * </ul></div>
     * The function should return a boolean value. Returning false from the function will stop the iteration.
     *
     * @param {Function} fn The function to execute for each widget extension.
     * @param {Object} scope (optional) The scope (<code>this</code> reference) in which the function is executed. Defaults to the current item in the iteration.
     */
	, forEachWidget : function(fn, scope){

		var registry = Sbi.cockpit.core.WidgetExtensionPointManager.registry;
	    for(var wtype in registry){
	    	if(fn.call(scope || window, wtype, Ext.apply({}, registry[wtype])) === false){
	                break;
	        }
	    }
	}

	/**
	 * @method
	 *
	 * Returns a widget runtime If widget parameter is already a valid widget runtime return it. Otherwise try to create it using
	 * the #widget parameter as a configuration object passed to method #createWidgetRuntime as shown in the following example
	 *
 * <pre><code>
Sbi.cockpit.core.WidgetExtensionPointManager.getWidget(widget);
</code></pre>
	 */
	, getWidgetRuntime: function(widget) {
		var w = null;

		Sbi.trace("[WidgetExtensionPoint.getWidgetRuntime]: IN");
		if(Sbi.isNotValorized(widget)) {
    		Sbi.warn("[WidgetExtensionPoint.getWidgetRuntime]: Input parameter [widget] is not valorized.");
    	} else if( (widget instanceof Sbi.cockpit.core.WidgetRuntime) === true) {
    		var wtype = widget.getWType();
    		Sbi.warn("[WidgetExtensionPoint.getWidgetRuntime]: Input parameter [widget] is a widget object of type [" + wtype + "]");
    		if( this.isWidgetRegistered(wtype) ) {
    			w = widget;
    		} else {
    			Sbi.warn("[WidgetExtensionPoint.getWidgetRuntime]: Input parameter [widget] is of an unregistered type");
    		}
    	} else {
    		if(typeof widget === 'object' && (widget instanceof Ext.util.Observable) === false) {
    			Sbi.trace("[WidgetExtensionPoint.getWidgetRuntime]: Input parameter [widget] is a widget configuration object equlas to [" + Sbi.toSource(widget, true) + "]");
    			w = Sbi.cockpit.core.WidgetExtensionPointManager.createWidgetRuntime(widget);
    		} else {
    			Sbi.error("[WidgetExtensionPoint.getWidgetRuntime]: Input parameter [widget] of type [" + (typeof widget) + "] is not valid");
    		}
    	}
		Sbi.trace("[WidgetExtensionPoint.getWidgetRuntime]: OUT");

		return w;
	}

	/**
	 * @method
	 *
	 * Returns a brandnew widget of type #conf.wtype. The parameter #conf is passed to the constructor of the new widget.
	 *
	 * @param {Object} conf The object to pass to the constructor of the new widget
	 * @param {String} conf.wtype The type of the new widget
	 * @param {String} conf.storeId (optional) The label of the dataset that feed the new widget
	 * @param {Object} conf.wconf (optional) The custom configuration of the new widget
	 * @param {Object} conf.wlayout (optional) The layout configuration of the new widget
	 * @param {Object} conf.wstyle (optional) The style configuration of the new widget
	 *
	 * @return {Sbi.cockpit.core.WidgetRuntime} The new widget runtime
	 */
	, createWidgetRuntime: function(conf) {
		Sbi.trace("[WidgetExtensionPoint.createWidgetRuntime]: IN");

		var wdescriptor = Sbi.cockpit.core.WidgetExtensionPointManager.registry[conf.wtype];

		if(wdescriptor !== undefined) {
			Sbi.trace("[WidgetExtensionPoint.createWidgetRuntime]: runtime class for widget of type [" + conf.wtype + "] is equal to [" + wdescriptor.runtimeClass + "]");
			var widget = Sbi.createObjectByClassName(wdescriptor.runtimeClass, conf);
			return widget;
		} else {
			alert("Widget of type [" + conf.wtype +"] not supprted. Supported types are [" + Sbi.cockpit.core.WidgetExtensionPointManager.getWidgetTypes().join() + "]");
		}
		Sbi.trace("[WidgetExtensionPoint.createWidgetRuntime]: OUT");
	}

	/**
	 * @method
	 *
	 * Returns a widget designer. If designer parameter is already a valid widget designer return it. Otherwise try to create it using
	 * the #designer parameter as a configuration object passed to method #createWidgetDesigner as shown in the following example
	 *
 * <pre><code>
Sbi.cockpit.core.WidgetExtensionPointManager.createWidgetDesigner(designer);
</code></pre>
	 */
	, getWidgetDesigner: function(designer) {
		var d = null;

		Sbi.trace("[WidgetExtensionPoint.getWidgetDesigner]: IN");
		if(Sbi.isNotValorized(designer)) {
    		Sbi.warn("[WidgetExtensionPoint.getWidgetDesigner]: Input parameter [designer] is not valorized.");
    	} else if( (designer instanceof Sbi.cockpit.core.WidgetDesigner) === true) {
    		var wtype = designer.getWType();
    		Sbi.warn("[WidgetExtensionPoint.getWidgetDesigner]: Input parameter [designer] is a widget designer object of type [" + wtype + "]");
    		if( this.isWidgetRegistered(wtype) ) {
    			d = designer;
    		} else {
    			Sbi.warn("[WidgetExtensionPoint.getWidgetDesigner]: Input parameter [designer] is of an unregistered type");
    		}
    	} else {
    		if(typeof designer === 'object' && (designer instanceof Ext.util.Observable) === false) {
    			Sbi.trace("[WidgetExtensionPoint.getWidgetDesigner]: Input parameter [designer] is a widget designer configuration object equlas to [" + Sbi.toSource(designer, true) + "]");
    			d = Sbi.cockpit.core.WidgetExtensionPointManager.createWidgetDesigner(designer);
    		} else {
    			Sbi.error("[WidgetExtensionPoint.getWidgetDesigner]: Input parameter [designer] of type [" + (typeof designer) + "] is not valid");
    		}
    	}
		Sbi.trace("[WidgetExtensionPoint.getWidgetDesigner]: OUT");

		return d;
	}

	/**
	 * @method
	 *
	 * Returns a brandnew widget designer of type #conf.wtype. The parameter #conf is passed to the constructor of the new widget designer.
	 *
	 * @param {Object} The widget designer configuration oject
	 *
	 * @return {Sbi.cockpit.core.WidgetDesigner} The new widget designer
	 */
	, createWidgetDesigner: function(conf) {
		Sbi.trace("[WidgetExtensionPoint.createWidgetDesigner]: IN");

		var wdescriptor = Sbi.cockpit.core.WidgetExtensionPointManager.registry[conf.wtype];

		if(wdescriptor !== undefined) {
			var widgetDesigner = Sbi.createObjectByClassName(wdescriptor.designerClass, conf);
			if(Sbi.isNotValorized( widgetDesigner.getDesignerType() ) ) { // TODO remove this
				widgetDesigner.wtype = wtype;
			}
			return widgetDesigner;
		} else {
			alert("Widget of type [" + wtype +"] not supported. Supported types are [" + Sbi.cockpit.core.WidgetExtensionPointManager.getWidgetTypes().join() + "]");
		}
		Sbi.trace("[WidgetExtensionPoint.createWidgetDesigner]: OUT");
	}
};

// shortcuts
Sbi.registerWidget = Sbi.cockpit.core.WidgetExtensionPointManager.registerWidget;
Sbi.unregisterWidget = Sbi.cockpit.core.WidgetExtensionPointManager.unregisterWidget;