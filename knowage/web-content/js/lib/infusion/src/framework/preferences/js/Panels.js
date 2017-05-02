/*
Copyright 2013-2015 OCAD University

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://github.com/fluid-project/infusion/raw/master/Infusion-LICENSE.txt
*/

var fluid_2_0_0 = fluid_2_0_0 || {};


(function ($, fluid) {
    "use strict";

    /**********************
     * msgLookup grade *
     **********************/

    fluid.defaults("fluid.prefs.msgLookup", {
        gradeNames: ["fluid.component"],
        members: {
            msgLookup: {
                expander: {
                    funcName: "fluid.prefs.stringLookup",
                    args: ["{msgResolver}", "{that}.options.stringArrayIndex"]
                }
            }
        },
        stringArrayIndex: {}
    });

    fluid.prefs.stringLookup = function (messageResolver, stringArrayIndex) {
        var that = {id: fluid.allocateGuid()};
        that.singleLookup = function (value) {
            var looked = messageResolver.lookup([value]);
            return fluid.get(looked, "template");
        };
        that.multiLookup = function (values) {
            return fluid.transform(values, function (value) {
                return that.singleLookup(value);
            });
        };
        that.lookup = function (value) {
            var values = fluid.get(stringArrayIndex, value) || value;
            var lookupFn = fluid.isArrayable(values) ? "multiLookup" : "singleLookup";
            return that[lookupFn](values);
        };
        that.resolvePathSegment = that.lookup;
        return that;
    };

    /***********************************************
     * Base grade panel
     ***********************************************/

    fluid.defaults("fluid.prefs.panel", {
        gradeNames: ["fluid.prefs.msgLookup", "fluid.rendererComponent"],
        events: {
            onDomBind: null
        },
        // Any listener that requires a DOM element, should be registered
        // to the onDomBind listener. By default it is fired by onCreate, but
        // when used as a subpanel, it will be triggered by the resetDomBinder invoker.
        listeners: {
            "onCreate.onDomBind": "{that}.events.onDomBind"
        },
        components: {
            msgResolver: {
                type: "fluid.messageResolver"
            }
        },
        rendererOptions: {
            messageLocator: "{msgResolver}.resolve"
        },
        distributeOptions: {
            source: "{that}.options.messageBase",
            target: "{that > msgResolver}.options.messageBase"
        }
    });

    /***************************
     * Base grade for subpanel *
     ***************************/

    fluid.defaults("fluid.prefs.subPanel", {
        gradeNames: ["fluid.prefs.panel", "{that}.getDomBindGrade"],
        listeners: {
            "{compositePanel}.events.afterRender": {
                listener: "{that}.events.afterRender",
                args: ["{that}"]
            },
            // Changing the firing of onDomBind from the onCreate.
            // This is due to the fact that the rendering process, controlled by the
            // composite panel, will set/replace the DOM elements.
            "onCreate.onDomBind": null, // remove listener
            "afterRender.onDomBind": "{that}.resetDomBinder"
        },
        rules: {
            expander: {
                func: "fluid.prefs.subPanel.generateRules",
                args: ["{that}.options.preferenceMap"]
            }
        },
        invokers: {
            refreshView: "{compositePanel}.refreshView",
            // resetDomBinder must fire the onDomBind event
            resetDomBinder: {
                funcName: "fluid.prefs.subPanel.resetDomBinder",
                args: ["{that}"]
            },
            getDomBindGrade: {
                funcName: "fluid.prefs.subPanel.getDomBindGrade",
                args: ["{prefsEditor}"]
            }
        },
        strings: {},
        parentBundle: "{compositePanel}.messageResolver",
        renderOnInit: false
    });

    fluid.defaults("fluid.prefs.subPanel.domBind", {
        gradeNames: ["fluid.component"],
        listeners: {
            "onDomBind.domChange": {
                listener: "{prefsEditor}.events.onSignificantDOMChange"
            }
        }
    });

    fluid.prefs.subPanel.getDomBindGrade = function (prefsEditor) {
        var hasListener = fluid.get(prefsEditor, "options.events.onSignificantDOMChange") !== undefined;
        if (hasListener) {
            return "fluid.prefs.subPanel.domBind";
        }
    };

    /*
     * Since the composite panel manages the rendering of the subpanels
     * the markup used by subpanels needs to be completely replaced.
     * The subpanel's container is refereshed to point at the newly
     * rendered markup, and the domBinder is re-initialized. Once
     * this is all done, the onDomBind event is fired.
     */
    fluid.prefs.subPanel.resetDomBinder = function (that) {
        // TODO: The line below to find the container jQuery instance was copied from the framework code -
        // https://github.com/fluid-project/infusion/blob/master/src/framework/core/js/FluidView.js#L145
        // in order to reset the dom binder when panels are in an iframe.
        // It can be be eliminated once we have the new renderer.
        var userJQuery = that.container.constructor;
        var context = that.container[0].ownerDocument;
        var selector = that.container.selector;
        that.container = userJQuery(selector, context);
        // To address FLUID-5966, manually adding back the selector and context properties that were removed from jQuery v3.0.
        // ( see: https://jquery.com/upgrade-guide/3.0/#breaking-change-deprecated-context-and-selector-properties-removed )
        // In most cases the "selector" property will already be restored through the DOM binder or fluid.container.
        // However, in this case we are manually recreating the container to ensure that it is referencing an element currently added
        // to the correct Document ( e.g. iframe ) (also see: FLUID-4536). This manual recreation of the container requires us to
        // manually add back the selector and context from the original container. This code and fix parallels that in
        // FluidView.js fluid.container line 129
        that.container.selector = selector;
        that.container.context = context;
        if (that.container.length === 0) {
            fluid.fail("resetDomBinder got no elements in DOM for container searching for selector " + that.container.selector);
        }
        fluid.initDomBinder(that, that.options.selectors);
        that.events.onDomBind.fire(that);
    };

    fluid.prefs.subPanel.safePrefKey = function (prefKey) {
        return prefKey.replace(/[.]/g, "_");
    };

    /*
     * Generates the model relay rules for a subpanel.
     * Takes advantage of the fact that compositePanel
     * uses the preference key (with "." replaced by "_"),
     * as its model path.
     */
    fluid.prefs.subPanel.generateRules = function (preferenceMap) {
        var rules = {};
        fluid.each(preferenceMap, function (prefObj, prefKey) {
            fluid.each(prefObj, function (value, prefRule) {
                if (prefRule.indexOf("model.") === 0) {
                    rules[fluid.prefs.subPanel.safePrefKey(prefKey)] = prefRule.slice("model.".length);
                }
            });
        });
        return rules;
    };

    /**********************************
     * Base grade for composite panel *
     **********************************/

    fluid.registerNamespace("fluid.prefs.compositePanel");

    fluid.prefs.compositePanel.arrayMergePolicy = function (target, source) {
        target = fluid.makeArray(target);
        source = fluid.makeArray(source);
        fluid.each(source, function (selector) {
            if (target.indexOf(selector) < 0) {
                target.push(selector);
            }
        });
        return target;
    };

    fluid.defaults("fluid.prefs.compositePanel", {
        gradeNames: ["fluid.prefs.panel", "{that}.getDistributeOptionsGrade", "{that}.getSubPanelLifecycleBindings"],
        mergePolicy: {
            subPanelOverrides: "noexpand",
            selectorsToIgnore: fluid.prefs.compositePanel.arrayMergePolicy
        },
        selectors: {}, // requires selectors into the template which will act as the containers for the subpanels
        selectorsToIgnore: [], // should match the selectors that are used to identify the containers for the subpanels
        repeatingSelectors: [],
        events: {
            initSubPanels: null
        },
        listeners: {
            "onCreate.combineResources": "{that}.combineResources",
            "onCreate.appendTemplate": {
                "this": "{that}.container",
                "method": "append",
                "args": ["{that}.options.resources.template.resourceText"]
            },
            "onCreate.initSubPanels": "{that}.events.initSubPanels",
            "onCreate.hideInactive": "{that}.hideInactive",
            "onCreate.surfaceSubpanelRendererSelectors": "{that}.surfaceSubpanelRendererSelectors",
            "afterRender.hideInactive": "{that}.hideInactive"
        },
        invokers: {
            getDistributeOptionsGrade: {
                funcName: "fluid.prefs.compositePanel.assembleDistributeOptions",
                args: ["{that}.options.components"]
            },
            getSubPanelLifecycleBindings: {
                funcName: "fluid.prefs.compositePanel.subPanelLifecycleBindings",
                args: ["{that}.options.components"]
            },
            combineResources: {
                funcName: "fluid.prefs.compositePanel.combineTemplates",
                args: ["{that}.options.resources", "{that}.options.selectors"]
            },
            surfaceSubpanelRendererSelectors: {
                funcName: "fluid.prefs.compositePanel.surfaceSubpanelRendererSelectors",
                args: ["{that}", "{that}.options.components", "{that}.options.selectors"]
            },
            produceSubPanelTrees: {
                funcName: "fluid.prefs.compositePanel.produceSubPanelTrees",
                args: ["{that}"]
            },
            expandProtoTree: {
                funcName: "fluid.prefs.compositePanel.expandProtoTree",
                args: ["{that}"]
            },
            produceTree: {
                funcName: "fluid.prefs.compositePanel.produceTree",
                args: ["{that}"]
            },
            hideInactive: {
                funcName: "fluid.prefs.compositePanel.hideInactive",
                args: ["{that}"]
            },
            handleRenderOnPreference: {
                funcName: "fluid.prefs.compositePanel.handleRenderOnPreference",
                args: ["{that}", "{that}.refreshView", "{that}.conditionalCreateEvent", "{arguments}.0", "{arguments}.1", "{arguments}.2"]
            },
            conditionalCreateEvent: {
                funcName: "fluid.prefs.compositePanel.conditionalCreateEvent"
            }
        },
        subPanelOverrides: {
            gradeNames: ["fluid.prefs.subPanel"]
        },
        rendererFnOptions: {
            noexpand: true,
            cutpointGenerator: "fluid.prefs.compositePanel.cutpointGenerator",
            subPanelRepeatingSelectors: {
                expander: {
                    funcName: "fluid.prefs.compositePanel.surfaceRepeatingSelectors",
                    args: ["{that}.options.components"]
                }
            }
        },
        components: {},
        resources: {} // template is reserved for the compositePanel's template, the subpanel template should have same key as the selector for its container.
    });

    /*
     * Attempts to prefetch a components options before it is instantiated.
     * Only use in cases where the instatiated component cannot be used.
     */
    fluid.prefs.compositePanel.prefetchComponentOptions = function (type, options) {
        var baseOptions = fluid.getMergedDefaults(type, fluid.get(options, "gradeNames"));
        // TODO: awkwardly, fluid.merge is destructive on each argument!
        return fluid.merge(baseOptions.mergePolicy, fluid.copy(baseOptions), options);
    };
    /*
     * Should only be used when fluid.prefs.compositePanel.isActivatePanel cannot.
     * While this implementation doesn't require an instantiated component, it may in
     * the process miss some configuration provided by distribute options and demands.
     */
    fluid.prefs.compositePanel.isPanel = function (type, options) {
        var opts = fluid.prefs.compositePanel.prefetchComponentOptions(type, options);
        return fluid.hasGrade(opts, "fluid.prefs.panel");
    };

    fluid.prefs.compositePanel.isActivePanel = function (comp) {
        return comp && fluid.hasGrade(comp.options, "fluid.prefs.panel");
    };

    /*
     * Creates a grade containing the distributeOptions rules needed for the subcomponents
     */
    fluid.prefs.compositePanel.assembleDistributeOptions = function (components) {
        var gradeName = "fluid.prefs.compositePanel.distributeOptions_" + fluid.allocateGuid();
        var distributeOptions = [];
        var relayOption = {};
        fluid.each(components, function (componentOptions, componentName) {
            if (fluid.prefs.compositePanel.isPanel(componentOptions.type, componentOptions.options)) {
                distributeOptions.push({
                    source: "{that}.options.subPanelOverrides",
                    target: "{that > " + componentName + "}.options"
                });
            }

            // Construct the model relay btw the composite panel and its subpanels
            var componentRelayRules = {};
            var definedOptions = fluid.prefs.compositePanel.prefetchComponentOptions(componentOptions.type, componentOptions.options);
            var preferenceMap = fluid.get(definedOptions, ["preferenceMap"]);
            fluid.each(preferenceMap, function (prefObj, prefKey) {
                fluid.each(prefObj, function (value, prefRule) {
                    if (prefRule.indexOf("model.") === 0) {
                        fluid.set(componentRelayRules, prefRule.slice("model.".length), "{compositePanel}.model." + fluid.prefs.subPanel.safePrefKey(prefKey));
                    }
                });
            });
            relayOption[componentName] = componentRelayRules;
            distributeOptions.push({
                source: "{that}.options.relayOption." + componentName,
                target: "{that > " + componentName + "}.options.model"
            });
        });
        fluid.defaults(gradeName, {
            gradeNames: ["fluid.component"],
            relayOption: relayOption,
            distributeOptions: distributeOptions
        });
        return gradeName;
    };

    fluid.prefs.compositePanel.conditionalCreateEvent = function (value, createEvent) {
        if (value) {
            createEvent();
        }
    };


    fluid.prefs.compositePanel.handleRenderOnPreference = function (that, refreshViewFunc, conditionalCreateEventFunc, value, createEvent, componentNames) {
        componentNames = fluid.makeArray(componentNames);
        conditionalCreateEventFunc(value, createEvent);
        fluid.each(componentNames, function (componentName) {
            var comp = that[componentName];
            if (!value && comp) {
                comp.destroy();
            }
        });
        refreshViewFunc();
    };

    fluid.prefs.compositePanel.creationEventName = function (pref) {
        return "initOn_" + pref;
    };

    fluid.prefs.compositePanel.generateModelListeners = function (conditionals) {
        return fluid.transform(conditionals, function (componentNames, pref) {
            var eventName = fluid.prefs.compositePanel.creationEventName(pref);
            return {
                func: "{that}.handleRenderOnPreference",
                args: ["{change}.value", "{that}.events." + eventName + ".fire", componentNames]
            };
        });
    };

    /*
     * Creates a grade containing all of the lifecycle binding configuration needed for the subpanels.
     * This includes the following:
     * - adding events used to trigger the initialization of the subpanels
     * - adding the createOnEvent configuration for the subpanels
     * - binding handlers to model changed events
     * - binding handlers to afterRender and onCreate
     */
    fluid.prefs.compositePanel.subPanelLifecycleBindings = function (components) {
        var gradeName = "fluid.prefs.compositePanel.subPanelCreationTimingDistibution_" + fluid.allocateGuid();
        var distributeOptions = [];
        var subPanelCreationOpts = {
            "default": "initSubPanels"
        };
        var conditionals = {};
        var listeners = {};
        var events = {};
        fluid.each(components, function (componentOptions, componentName) {
            if (fluid.prefs.compositePanel.isPanel(componentOptions.type, componentOptions.options)) {
                var creationEventOpt = "default";
                // would have had renderOnPreference directly sourced from the componentOptions
                // however, the set of configuration specified there is restricted.
                var renderOnPreference = fluid.get(componentOptions, "options.renderOnPreference");
                if (renderOnPreference) {
                    var pref = fluid.prefs.subPanel.safePrefKey(renderOnPreference);
                    var onCreateListener = "onCreate." + pref;
                    creationEventOpt = fluid.prefs.compositePanel.creationEventName(pref);
                    subPanelCreationOpts[creationEventOpt] = creationEventOpt;
                    events[creationEventOpt] = null;
                    conditionals[pref] = conditionals[pref] || [];
                    conditionals[pref].push(componentName);
                    listeners[onCreateListener] = {
                        listener: "{that}.conditionalCreateEvent",
                        args: ["{that}.model." + pref, "{that}.events." + creationEventOpt + ".fire"]
                    };
                }
                distributeOptions.push({
                    source: "{that}.options.subPanelCreationOpts." + creationEventOpt,
                    target: "{that}.options.components." + componentName + ".createOnEvent"
                });
            }
        });

        fluid.defaults(gradeName, {
            gradeNames: ["fluid.component"],
            events: events,
            listeners: listeners,
            modelListeners: fluid.prefs.compositePanel.generateModelListeners(conditionals),
            subPanelCreationOpts: subPanelCreationOpts,
            distributeOptions: distributeOptions
        });
        return gradeName;
    };

    /*
     * Used to hide the containers of inactive sub panels.
     * This is necessary as the composite panel's template is the one that has their containers and
     * it would be undesirable to have them visible when their associated panel has not been created.
     * Also, hiding them allows for the subpanel to initialize, as it requires their container to be present.
     * The subpanels need to be initialized before rendering, for the produce function to source the rendering
     * information from it.
     */
    fluid.prefs.compositePanel.hideInactive = function (that) {
        fluid.each(that.options.components, function (componentOpts, componentName) {
            if (fluid.prefs.compositePanel.isPanel(componentOpts.type, componentOpts.options) && !fluid.prefs.compositePanel.isActivePanel(that[componentName])) {
                that.locate(componentName).hide();
            }
        });
    };

    /*
     * Use the renderer directly to combine the templates into a single
     * template to be used by the components actual rendering.
     */
    fluid.prefs.compositePanel.combineTemplates = function (resources, selectors) {
        var cutpoints = [];
        var tree = {children: []};

        fluid.each(resources, function (resource, resourceName) {
            if (resourceName !== "template") {
                tree.children.push({
                    ID: resourceName,
                    markup: resource.resourceText
                });
                cutpoints.push({
                    id: resourceName,
                    selector: selectors[resourceName]
                });
            }
        });

        var resourceSpec = {
            base: {
                resourceText: resources.template.resourceText,
                href: ".",
                resourceKey: ".",
                cutpoints: cutpoints
            }
        };

        var templates = fluid.parseTemplates(resourceSpec, ["base"]);
        var renderer = fluid.renderer(templates, tree, {cutpoints: cutpoints, debugMode: true});
        resources.template.resourceText = renderer.renderTemplates();
    };

    fluid.prefs.compositePanel.rebaseSelectorName = function (memberName, selectorName) {
        return memberName + "_" + selectorName;
    };

    /*
     * Surfaces the rendering selectors from the subpanels to the compositePanel,
     * and scopes them to the subpanel's container.
     * Since this is used by the cutpoint generator, which only gets run once, we need to
     * surface all possible subpanel selectors, and not just the active ones.
     */
    fluid.prefs.compositePanel.surfaceSubpanelRendererSelectors = function (that, components, selectors) {
        fluid.each(components, function (compOpts, compName) {
            if (fluid.prefs.compositePanel.isPanel(compOpts.type, compOpts.options)) {
                var opts = fluid.prefs.compositePanel.prefetchComponentOptions(compOpts.type, compOpts.options);
                fluid.each(opts.selectors, function (selector, selName) {
                    if (!opts.selectorsToIgnore || opts.selectorsToIgnore.indexOf(selName) < 0) {
                        fluid.set(selectors,  fluid.prefs.compositePanel.rebaseSelectorName(compName, selName), selectors[compName] + " " + selector);
                    }
                });
            }
        });
    };

    fluid.prefs.compositePanel.surfaceRepeatingSelectors = function (components) {
        var repeatingSelectors = [];
        fluid.each(components, function (compOpts, compName) {
            if (fluid.prefs.compositePanel.isPanel(compOpts.type, compOpts.options)) {
                var opts = fluid.prefs.compositePanel.prefetchComponentOptions(compOpts.type, compOpts.options);
                var rebasedRepeatingSelectors = fluid.transform(opts.repeatingSelectors, function (selector) {
                    return fluid.prefs.compositePanel.rebaseSelectorName(compName, selector);
                });
                repeatingSelectors = repeatingSelectors.concat(rebasedRepeatingSelectors);
            }
        });
        return repeatingSelectors;
    };

    fluid.prefs.compositePanel.cutpointGenerator = function (selectors, options) {
        var opts = {
            selectorsToIgnore: options.selectorsToIgnore,
            repeatingSelectors: options.repeatingSelectors.concat(options.subPanelRepeatingSelectors)
        };
        return fluid.renderer.selectorsToCutpoints(selectors, opts);
    };

    fluid.prefs.compositePanel.rebaseID = function (value, memberName) {
        return memberName + "_" + value;
    };

    fluid.prefs.compositePanel.rebaseParentRelativeID = function (val, memberName) {
        var slicePos = "..::".length; // ..:: refers to the parentRelativeID prefix used in the renderer
        return val.slice(0, slicePos) + fluid.prefs.compositePanel.rebaseID(val.slice(slicePos), memberName);
    };

    fluid.prefs.compositePanel.rebaseValueBinding = function (value, modelRelayRules) {
        return fluid.find(modelRelayRules, function (oldModelPath, newModelPath) {
            if (value === oldModelPath) {
                return newModelPath;
            } else if (value.indexOf(oldModelPath) === 0) {
                return value.replace(oldModelPath, newModelPath);
            }
        }) || value;
    };

    fluid.prefs.compositePanel.rebaseTreeComp = function (msgResolver, model, treeComp, memberName, modelRelayRules) {
        var rebased = fluid.copy(treeComp);

        if (rebased.ID) {
            rebased.ID = fluid.prefs.compositePanel.rebaseID(rebased.ID, memberName);
        }

        if (rebased.children) {
            rebased.children = fluid.prefs.compositePanel.rebaseTree(msgResolver, model, rebased.children, memberName, modelRelayRules);
        } else if (rebased.selection) {
            rebased.selection = fluid.prefs.compositePanel.rebaseTreeComp(msgResolver, model, rebased.selection, memberName, modelRelayRules);
        } else if (rebased.messagekey) {
            // converts the "UIMessage" renderer component into a "UIBound"
            // and passes in the resolved message as the value.
            rebased.componentType = "UIBound";
            rebased.value = msgResolver.resolve(rebased.messagekey.value, rebased.messagekey.args);
            delete rebased.messagekey;
        } else if (rebased.parentRelativeID) {
            rebased.parentRelativeID = fluid.prefs.compositePanel.rebaseParentRelativeID(rebased.parentRelativeID, memberName);
        } else if (rebased.valuebinding) {
            rebased.valuebinding = fluid.prefs.compositePanel.rebaseValueBinding(rebased.valuebinding, modelRelayRules);

            if (rebased.value) {
                var modelValue = fluid.get(model, rebased.valuebinding);
                rebased.value = modelValue !== undefined ? modelValue : rebased.value;
            }
        }

        return rebased;
    };

    fluid.prefs.compositePanel.rebaseTree = function (msgResolver, model, tree, memberName, modelRelayRules) {
        var rebased;

        if (fluid.isArrayable(tree)) {
            rebased = fluid.transform(tree, function (treeComp) {
                return fluid.prefs.compositePanel.rebaseTreeComp(msgResolver, model, treeComp, memberName, modelRelayRules);
            });
        } else {
            rebased = fluid.prefs.compositePanel.rebaseTreeComp(msgResolver, model, tree, memberName, modelRelayRules);
        }

        return rebased;
    };

    fluid.prefs.compositePanel.produceTree = function (that) {
        var produceTreeOption = that.options.produceTree;
        var ownTree = produceTreeOption ?
            (typeof (produceTreeOption) === "string" ? fluid.getGlobalValue(produceTreeOption) : produceTreeOption)(that) :
            that.expandProtoTree();
        var subPanelTree = that.produceSubPanelTrees();
        var tree = {
            children: ownTree.children.concat(subPanelTree.children)
        };
        return tree;
    };

    fluid.prefs.compositePanel.expandProtoTree = function (that) {
        var expanderOptions = fluid.renderer.modeliseOptions(that.options.expanderOptions, {ELstyle: "${}"}, that);
        var expander = fluid.renderer.makeProtoExpander(expanderOptions, that);
        return expander(that.options.protoTree || {});
    };

    fluid.prefs.compositePanel.produceSubPanelTrees = function (that) {
        var tree = {children: []};
        fluid.each(that.options.components, function (options, componentName) {
            var subPanel = that[componentName];
            if (fluid.prefs.compositePanel.isActivePanel(subPanel)) {
                var expanderOptions = fluid.renderer.modeliseOptions(subPanel.options.expanderOptions, {ELstyle: "${}"}, subPanel);
                var expander = fluid.renderer.makeProtoExpander(expanderOptions, subPanel);
                var subTree = subPanel.produceTree();
                subTree = fluid.get(subPanel.options, "rendererFnOptions.noexpand") ? subTree : expander(subTree);
                var rebasedTree = fluid.prefs.compositePanel.rebaseTree(subPanel.msgResolver, that.model, subTree, componentName, subPanel.options.rules);
                tree.children = tree.children.concat(rebasedTree.children);
            }
        });
        return tree;
    };

    /********************************************************************************
     * The grade that contains the connections between a panel and the prefs editor *
     ********************************************************************************/

    fluid.defaults("fluid.prefs.prefsEditorConnections", {
        gradeNames: ["fluid.component"],
        listeners: {
            "{fluid.prefs.prefsEditor}.events.onPrefsEditorRefresh": "{fluid.prefs.panel}.refreshView"
        },
        strings: {},
        parentBundle: "{fluid.prefs.prefsEditorLoader}.msgResolver"
    });

    /********************************
     * Preferences Editor Text Size *
     ********************************/

    /**
     * A sub-component of fluid.prefs that renders the "text size" panel of the user preferences interface.
     */
    fluid.defaults("fluid.prefs.panel.textSize", {
        gradeNames: ["fluid.prefs.panel"],
        preferenceMap: {
            "fluid.prefs.textSize": {
                "model.textSize": "default",
                "range.min": "minimum",
                "range.max": "maximum"
            }
        },
        // The default model values represent both the expected format as well as the setting to be applied in the absence of values passed down to the component.
        // i.e. from the settings store, or specific defaults derived from schema.
        // Note: Except for being passed down to its subcomponent, these default values are not contributed and shared out
        range: {
            min: 1,
            max: 2
        },
        selectors: {
            textSize: ".flc-prefsEditor-min-text-size",
            label: ".flc-prefsEditor-min-text-size-label",
            multiplier: ".flc-prefsEditor-multiplier",
            textSizeDescr: ".flc-prefsEditor-text-size-descr"
        },
        selectorsToIgnore: ["textSize"],
        components: {
            textfieldSlider: {
                type: "fluid.textfieldSlider",
                container: "{that}.dom.textSize",
                createOnEvent: "afterRender",
                options: {
                    model: {
                        value: "{fluid.prefs.panel.textSize}.model.textSize"
                    },
                    range: "{fluid.prefs.panel.textSize}.options.range",
                    sliderOptions: "{fluid.prefs.panel.textSize}.options.sliderOptions"
                }
            }
        },
        protoTree: {
            label: {messagekey: "textSizeLabel"},
            multiplier: {messagekey: "multiplier"},
            textSizeDescr: {messagekey: "textSizeDescr"}
        },
        sliderOptions: {
            orientation: "horizontal",
            step: 0.1,
            range: "min"
        }
    });

    /********************************
     * Preferences Editor Text Font *
     ********************************/

    /**
     * A sub-component of fluid.prefs that renders the "text font" panel of the user preferences interface.
     */
    fluid.defaults("fluid.prefs.panel.textFont", {
        gradeNames: ["fluid.prefs.panel"],
        preferenceMap: {
            "fluid.prefs.textFont": {
                "model.value": "default",
                "controlValues.textFont": "enum"
            }
        },
        selectors: {
            textFont: ".flc-prefsEditor-text-font",
            label: ".flc-prefsEditor-text-font-label",
            textFontDescr: ".flc-prefsEditor-text-font-descr"
        },
        stringArrayIndex: {
            textFont: ["textFont-default", "textFont-times", "textFont-comic", "textFont-arial", "textFont-verdana"]
        },
        protoTree: {
            label: {messagekey: "textFontLabel"},
            textFontDescr: {messagekey: "textFontDescr"},
            textFont: {
                optionnames: "${{that}.msgLookup.textFont}",
                optionlist: "${{that}.options.controlValues.textFont}",
                selection: "${value}",
                decorators: {
                    type: "fluid",
                    func: "fluid.prefs.selectDecorator",
                    options: {
                        styles: "{that}.options.classnameMap.textFont"
                    }
                }
            }
        },
        classnameMap: null, // must be supplied by implementors
        controlValues: {
            textFont: ["default", "times", "comic", "arial", "verdana"]
        }
    });

    /*********************************
     * Preferences Editor Line Space *
     *********************************/

    /**
     * A sub-component of fluid.prefs that renders the "line space" panel of the user preferences interface.
     */
    fluid.defaults("fluid.prefs.panel.lineSpace", {
        gradeNames: ["fluid.prefs.panel"],
        preferenceMap: {
            "fluid.prefs.lineSpace": {
                "model.lineSpace": "default",
                "range.min": "minimum",
                "range.max": "maximum"
            }
        },
        // The default model values represent both the expected format as well as the setting to be applied in the absence of values passed down to the component.
        // i.e. from the settings store, or specific defaults derived from schema.
        // Note: Except for being passed down to its subcomponent, these default values are not contributed and shared out
        range: {
            min: 1,
            max: 2
        },
        selectors: {
            lineSpace: ".flc-prefsEditor-line-space",
            label: ".flc-prefsEditor-line-space-label",
            multiplier: ".flc-prefsEditor-multiplier",
            lineSpaceDescr: ".flc-prefsEditor-line-space-descr"
        },
        selectorsToIgnore: ["lineSpace"],
        components: {
            textfieldSlider: {
                type: "fluid.textfieldSlider",
                container: "{that}.dom.lineSpace",
                createOnEvent: "afterRender",
                options: {
                    model: {
                        value: "{fluid.prefs.panel.lineSpace}.model.lineSpace"
                    },
                    range: "{fluid.prefs.panel.lineSpace}.options.range",
                    sliderOptions: "{fluid.prefs.panel.lineSpace}.options.sliderOptions"
                }
            }
        },
        protoTree: {
            label: {messagekey: "lineSpaceLabel"},
            multiplier: {messagekey: "multiplier"},
            lineSpaceDescr: {messagekey: "lineSpaceDescr"}
        },
        sliderOptions: {
            orientation: "horizontal",
            step: 0.1,
            range: "min"
        }
    });

    /*******************************
     * Preferences Editor Contrast *
     *******************************/

    /**
     * A sub-component of fluid.prefs that renders the "contrast" panel of the user preferences interface.
     */
    fluid.defaults("fluid.prefs.panel.contrast", {
        gradeNames: ["fluid.prefs.panel"],
        preferenceMap: {
            "fluid.prefs.contrast": {
                "model.value": "default",
                "controlValues.theme": "enum"
            }
        },
        listeners: {
            "afterRender.style": "{that}.style"
        },
        selectors: {
            themeRow: ".flc-prefsEditor-themeRow",
            themeLabel: ".flc-prefsEditor-theme-label",
            themeInput: ".flc-prefsEditor-themeInput",
            label: ".flc-prefsEditor-contrast-label",
            contrastDescr: ".flc-prefsEditor-contrast-descr"
        },
        styles: {
            defaultThemeLabel: "fl-prefsEditor-contrast-defaultThemeLabel"
        },
        stringArrayIndex: {
            theme: ["contrast-default", "contrast-bw", "contrast-wb", "contrast-by", "contrast-yb", "contrast-lgdg"]
        },
        repeatingSelectors: ["themeRow"],
        protoTree: {
            label: {messagekey: "contrastLabel"},
            contrastDescr: {messagekey: "contrastDescr"},
            expander: {
                type: "fluid.renderer.selection.inputs",
                rowID: "themeRow",
                labelID: "themeLabel",
                inputID: "themeInput",
                selectID: "theme-radio",
                tree: {
                    optionnames: "${{that}.msgLookup.theme}",
                    optionlist: "${{that}.options.controlValues.theme}",
                    selection: "${value}"
                }
            }
        },
        controlValues: {
            theme: ["default", "bw", "wb", "by", "yb", "lgdg"]
        },
        markup: {
            // Aria-hidden needed on fl-preview-A and Display 'a' created as pseudo-content in css to prevent AT from reading out display 'a' on IE, Chrome, and Safari
            // Aria-hidden needed on fl-crossout to prevent AT from trying to read crossout symbol in Safari
            label: "<span class=\"fl-preview-A\" aria-hidden=\"true\"></span><span class=\"fl-hidden-accessible\">%theme</span><div class=\"fl-crossout\" aria-hidden=\"true\"></div>"
        },
        invokers: {
            style: {
                funcName: "fluid.prefs.panel.contrast.style",
                args: [
                    "{that}.dom.themeLabel",
                    "{that}.msgLookup.theme",
                    "{that}.options.markup.label",
                    "{that}.options.controlValues.theme",
                    "default",
                    "{that}.options.classnameMap.theme",
                    "{that}.options.styles.defaultThemeLabel"
                ]
            }
        }
    });

    fluid.prefs.panel.contrast.style = function (labels, strings, markup, theme, defaultThemeName, style, defaultLabelStyle) {
        fluid.each(labels, function (label, index) {
            label = $(label);

            var themeValue = strings[index];
            label.html(fluid.stringTemplate(markup, {
                theme: themeValue
            }));

            // Aria-label set to prevent Firefox from reading out the display 'a'
            label.attr("aria-label", themeValue);

            var labelTheme = theme[index];
            if (labelTheme === defaultThemeName) {
                label.addClass(defaultLabelStyle);
            }
            label.addClass(style[labelTheme]);
        });
    };

    /**************************************
     * Preferences Editor Layout Controls *
     **************************************/

    /**
     * A sub-component of fluid.prefs that renders the "layout and navigation" panel of the user preferences interface.
     */
    fluid.defaults("fluid.prefs.panel.layoutControls", {
        gradeNames: ["fluid.prefs.panel"],
        preferenceMap: {
            "fluid.prefs.tableOfContents": {
                "model.toc": "default"
            }
        },
        selectors: {
            toc: ".flc-prefsEditor-toc",
            label: ".flc-prefsEditor-toc-label",
            tocDescr: ".flc-prefsEditor-toc-descr"
        },
        protoTree: {
            label: {messagekey: "tocLabel"},
            tocDescr: {messagekey: "tocDescr"},
            toc: "${toc}"
        }
    });

    /**************************************
     * Preferences Editor Emphasize Links *
     **************************************/
    /**
     * A sub-component of fluid.prefs that renders the "links and buttons" panel of the user preferences interface.
     */
    fluid.defaults("fluid.prefs.panel.emphasizeLinks", {
        gradeNames: ["fluid.prefs.panel"],
        preferenceMap: {
            "fluid.prefs.emphasizeLinks": {
                "model.links": "default"
            }
        },
        selectors: {
            links: ".flc-prefsEditor-links",
            linksChoiceLabel: ".flc-prefsEditor-links-choice-label"
        },
        protoTree: {
            linksChoiceLabel: {messagekey: "linksChoiceLabel"},
            links: "${links}"
        }
    });

    /************************************
     * Preferences Editor Inputs Larger *
     ************************************/
    /**
     * A sub-component of fluid.prefs that renders the "links and buttons" panel of the user preferences interface.
     */
    fluid.defaults("fluid.prefs.panel.inputsLarger", {
        gradeNames: ["fluid.prefs.panel"],
        preferenceMap: {
            "fluid.prefs.inputsLarger": {
                "model.inputsLarger": "default"
            }
        },
        selectors: {
            inputsLarger: ".flc-prefsEditor-inputs-larger",
            inputsChoiceLabel: ".flc-prefsEditor-links-inputs-choice-label"
        },
        protoTree: {
            inputsChoiceLabel: {messagekey: "inputsChoiceLabel"},
            inputsLarger: "${inputsLarger}"
        }
    });

    /*************************************
     * Preferences Editor Links Controls *
     *************************************/
    /**
     * A sub-component of fluid.prefs that renders the "links and buttons" panel of the user preferences interface.
     */
    fluid.defaults("fluid.prefs.panel.linksControls", {
        gradeNames: ["fluid.prefs.compositePanel"],
        selectors: {
            label: ".flc-prefsEditor-linksControls-label"
        },
        protoTree: {
            label: {messagekey: "linksControlsLabel"}
        }
    });

    /********************************************************
     * Preferences Editor Select Dropdown Options Decorator *
     ********************************************************/

    /**
     * A sub-component that decorates the options on the select dropdown list box with the css style
     */
    fluid.defaults("fluid.prefs.selectDecorator", {
        gradeNames: ["fluid.viewComponent"],
        listeners: {
            "onCreate.decorateOptions": "fluid.prefs.selectDecorator.decorateOptions"
        },
        styles: {
            preview: "fl-preview-theme"
        }
    });

    fluid.prefs.selectDecorator.decorateOptions = function (that) {
        fluid.each($("option", that.container), function (option) {
            var styles = that.options.styles;
            $(option).addClass(styles.preview + " " + styles[fluid.value(option)]);
        });
    };

})(jQuery, fluid_2_0_0);
