/*
Copyright 2009 University of Toronto
Copyright 2010-2015 OCAD University
Copyright 2011 Lucendo Development Ltd.

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://github.com/fluid-project/infusion/raw/master/Infusion-LICENSE.txt
*/

var fluid_2_0_0 = fluid_2_0_0 || {};

(function ($, fluid) {
    "use strict";

    /*****************************
     * Preferences Editor Loader *
     *****************************/

    /**
     * An Preferences Editor top-level component that reflects the collaboration between prefsEditor, templateLoader and messageLoader.
     * This component is the only Preferences Editor component that is intended to be called by the outside world.
     *
     * @param {Object} options
     */
    fluid.defaults("fluid.prefs.prefsEditorLoader", {
        gradeNames: ["fluid.prefs.settingsGetter", "fluid.prefs.initialModel", "fluid.viewComponent"],
        defaultLocale: "en",
        members: {
            settings: {
                expander: {
                    funcName: "fluid.prefs.prefsEditorLoader.getCompleteSettings",
                    args: ["{that}.initialModel", "{that}.getSettings"]
                }
            }
        },
        components: {
            prefsEditor: {
                priority: "last",
                type: "fluid.prefs.prefsEditor",
                createOnEvent: "onCreatePrefsEditorReady",
                options: {
                    members: {
                        initialModel: "{prefsEditorLoader}.initialModel"
                    },
                    invokers: {
                        getSettings: "{prefsEditorLoader}.getSettings"
                    }
                }
            },
            templateLoader: {
                type: "fluid.resourceLoader",
                options: {
                    events: {
                        onResourcesLoaded: "{prefsEditorLoader}.events.onPrefsEditorTemplatesLoaded"
                    }
                }
            },
            messageLoader: {
                type: "fluid.resourceLoader",
                options: {
                    defaultLocale: "{prefsEditorLoader}.options.defaultLocale",
                    locale: "{prefsEditorLoader}.settings.locale",
                    resourceOptions: {
                        dataType: "json"
                    },
                    events: {
                        onResourcesLoaded: "{prefsEditorLoader}.events.onPrefsEditorMessagesLoaded"
                    }
                }
            }
        },
        events: {
            onPrefsEditorTemplatesLoaded: null,
            onPrefsEditorMessagesLoaded: null,
            onCreatePrefsEditorReady: {
                events: {
                    templateLoaded: "onPrefsEditorTemplatesLoaded",
                    prefsEditorMessagesLoaded: "onPrefsEditorMessagesLoaded"
                }
            }
        },
        distributeOptions: [{
            source: "{that}.options.templateLoader",
            removeSource: true,
            target: "{that > templateLoader}.options"
        }, {
            source: "{that}.options.messageLoader",
            removeSource: true,
            target: "{that > messageLoader}.options"
        }, {
            source: "{that}.options.terms",
            target: "{that > templateLoader}.options.terms"
        }, {
            source: "{that}.options.terms",
            target: "{that > messageLoader}.options.terms"
        }, {
            source: "{that}.options.prefsEditor",
            removeSource: true,
            target: "{that > prefsEditor}.options"
        }]
    });

    fluid.prefs.prefsEditorLoader.getCompleteSettings = function (initialModel, getSettingsFunc) {
        var savedSettings = getSettingsFunc();
        return $.extend(true, {}, initialModel, savedSettings);
    };

    // TODO: This mixin grade appears to be supplied manually by various test cases but no longer appears in
    // the main configuration. We should remove the need for users to supply this - also the use of "defaultPanels" in fact
    // refers to "starter panels"
    fluid.defaults("fluid.prefs.transformDefaultPanelsOptions", {
        // Do not supply "fluid.prefs.inline" here, since when this is used as a mixin for separatedPanel, it ends up displacing the
        // more refined type of the prefsEditorLoader
        gradeNames: ["fluid.viewComponent"],
        distributeOptions: [{
            source: "{that}.options.textSize",
            removeSource: true,
            target: "{that textSize}.options"
        }, {
            source: "{that}.options.lineSpace",
            removeSource: true,
            target: "{that lineSpace}.options"
        }, {
            source: "{that}.options.textFont",
            removeSource: true,
            target: "{that textFont}.options"
        }, {
            source: "{that}.options.contrast",
            removeSource: true,
            target: "{that contrast}.options"
        }, {
            source: "{that}.options.layoutControls",
            removeSource: true,
            target: "{that layoutControls}.options"
        }, {
            source: "{that}.options.linksControls",
            removeSource: true,
            target: "{that linksControls}.options"
        }]
    });

    /**********************
     * Preferences Editor *
     **********************/

    fluid.defaults("fluid.prefs.settingsGetter", {
        gradeNames: ["fluid.component"],
        members: {
            getSettings: "{fluid.prefs.store}.get"
        }
    });

    fluid.defaults("fluid.prefs.settingsSetter", {
        gradeNames: ["fluid.component"],
        invokers: {
            setSettings: {
                funcName: "fluid.prefs.settingsSetter.setSettings",
                args: ["{arguments}.0", "{fluid.prefs.store}.set"]
            }
        }
    });

    fluid.prefs.settingsSetter.setSettings = function (model, set) {
        var userSettings = fluid.copy(model);
        set(userSettings);
    };

    fluid.defaults("fluid.prefs.uiEnhancerRelay", {
        gradeNames: ["fluid.modelComponent"],
        listeners: {
            "onCreate.addListener": "{that}.addListener",
            "onDestroy.removeListener": "{that}.removeListener"
        },
        events: {
            updateEnhancerModel: "{fluid.prefs.prefsEditor}.events.onUpdateEnhancerModel"
        },
        invokers: {
            addListener: {
                funcName: "fluid.prefs.uiEnhancerRelay.addListener",
                args: ["{that}.events.updateEnhancerModel", "{that}.updateEnhancerModel"]
            },
            removeListener: {
                funcName: "fluid.prefs.uiEnhancerRelay.removeListener",
                args: ["{that}.events.updateEnhancerModel", "{that}.updateEnhancerModel"]
            },
            updateEnhancerModel: {
                funcName: "fluid.prefs.uiEnhancerRelay.updateEnhancerModel",
                args: ["{uiEnhancer}", "{fluid.prefs.prefsEditor}.model.preferences"]
            }
        }
    });

    fluid.prefs.uiEnhancerRelay.addListener = function (modelChanged, listener) {
        modelChanged.addListener(listener);
    };

    fluid.prefs.uiEnhancerRelay.removeListener = function (modelChanged, listener) {
        modelChanged.removeListener(listener);
    };

    fluid.prefs.uiEnhancerRelay.updateEnhancerModel = function (uiEnhancer, newModel) {
        uiEnhancer.updateModel(newModel);
    };

    /**
     * A component that works in conjunction with the UI Enhancer component
     * to allow users to set personal user interface preferences. The Preferences Editor component provides a user
     * interface for setting and saving personal preferences, and the UI Enhancer component carries out the
     * work of applying those preferences to the user interface.
     *
     * @param {Object} container
     * @param {Object} options
     */
    fluid.defaults("fluid.prefs.prefsEditor", {
        gradeNames: ["fluid.prefs.settingsGetter", "fluid.prefs.settingsSetter", "fluid.prefs.initialModel", "fluid.viewComponent"],
        invokers: {
            /**
             * Updates the change applier and fires modelChanged on subcomponent fluid.prefs.controls
             *
             * @param {Object} newModel
             * @param {Object} source
             */
            fetch: {
                funcName: "fluid.prefs.prefsEditor.fetch",
                args: ["{that}", "{arguments}.0"]
            },
            applyChanges: {
                funcName: "fluid.prefs.prefsEditor.applyChanges",
                args: ["{that}"]
            },
            save: {
                funcName: "fluid.prefs.prefsEditor.save",
                args: ["{that}"]
            },
            saveAndApply: {
                funcName: "fluid.prefs.prefsEditor.saveAndApply",
                args: ["{that}"]
            },
            reset: {
                funcName: "fluid.prefs.prefsEditor.reset",
                args: ["{that}"]
            },
            cancel: {
                funcName: "fluid.prefs.prefsEditor.cancel",
                args: ["{that}"]
            }
        },
        selectors: {
            cancel: ".flc-prefsEditor-cancel",
            reset: ".flc-prefsEditor-reset",
            save: ".flc-prefsEditor-save",
            previewFrame : ".flc-prefsEditor-preview-frame"
        },
        events: {
            onSave: null,
            onCancel: null,
            beforeReset: null,
            afterReset: null,
            onAutoSave: null,
            modelChanged: null,
            onPrefsEditorRefresh: null,
            onUpdateEnhancerModel: null,
            onPrefsEditorMarkupReady: null,
            onReady: null
        },
        listeners: {
            "onCreate.init": "fluid.prefs.prefsEditor.init",
            "onAutoSave.save": "{that}.save"
        },
        modelListeners: {
            "": [{
                listener: "fluid.prefs.prefsEditor.handleAutoSave",
                args: ["{that}"]
            }, {
                listener: "{that}.events.modelChanged.fire",
                args: ["{change}.value"]
            }]
        },
        resources: {
            template: "{templateLoader}.resources.prefsEditor"
        },
        autoSave: false
    });

    /**
     * Refresh PrefsEditor
     */
    fluid.prefs.prefsEditor.applyChanges = function (that) {
        that.events.onUpdateEnhancerModel.fire();
    };

    fluid.prefs.prefsEditor.fetch = function (that, eventName) {
        var completeModel = that.getSettings();
        completeModel = $.extend(true, {}, that.initialModel, completeModel);
        // TODO: This may not be completely effective if the root model is smaller than
        // the current one. Given our previous discoveries re "model shrinkage"
        // (http://issues.fluidproject.org/browse/FLUID-5585 ), the proper thing to do here
        // is to apply a DELETE to the root before putting in the new model. And this should
        // be done within a transaction in order to avoid notifying the tree more than necessary.
        // However, the transactional model of the changeApplier is going to change radically
        // soon (http://wiki.fluidproject.org/display/fluid/New+New+Notes+on+the+ChangeApplier)
        // and this implementation doesn't seem to be causing a problem at present so we had
        // just better leave it the way it is for now.
        that.applier.change("", completeModel);
        if (eventName) {
            that.events[eventName].fire(that);
        }
        that.events.onPrefsEditorRefresh.fire();
        that.applyChanges();
    };

    /**
     * Sends the prefsEditor.model to the store and fires onSave
     * @param that: A fluid.prefs.prefsEditor instance
     * @return the saved model
     */
    fluid.prefs.prefsEditor.save = function (that) {
        if (!that.model) {  // Don't save a reset model
            return;
        }

        var modelToSave = fluid.copy(that.model),
            initialModel = that.initialModel,
            stats = {changes: 0, unchanged: 0, changeMap: {}},
            changedPrefs = {};

        // To address https://issues.fluidproject.org/browse/FLUID-4686
        fluid.model.diff(modelToSave.preferences, fluid.get(initialModel, ["preferences"]), stats);

        if (stats.changes === 0) {
            delete modelToSave.preferences;
        } else {
            fluid.each(stats.changeMap, function (state, pref) {
                fluid.set(changedPrefs, pref, modelToSave.preferences[pref]);
            });
            modelToSave.preferences = changedPrefs;
        }

        that.events.onSave.fire(modelToSave);
        that.setSettings(modelToSave);
        return modelToSave;
    };

    fluid.prefs.prefsEditor.saveAndApply = function (that) {
        var prevSettings = that.getSettings(),
            changedSelections = that.save();

        // Only when preferences are changed, re-render panels and trigger enactors to apply changes
        if (!fluid.model.diff(fluid.get(changedSelections, "preferences"), fluid.get(prevSettings, "preferences"))) {
            that.events.onPrefsEditorRefresh.fire();
            that.applyChanges();
        }
    };

    /**
     * Resets the selections to the integrator's defaults and fires afterReset
     */
    fluid.prefs.prefsEditor.reset = function (that) {
        that.events.beforeReset.fire(that);
        that.applier.fireChangeRequest({path: "", type: "DELETE"});
        that.applier.change("", fluid.copy(that.initialModel));
        that.events.onPrefsEditorRefresh.fire();
        that.events.afterReset.fire(that);
    };

    /**
     * Resets the selections to the last saved selections and fires onCancel
     */
    fluid.prefs.prefsEditor.cancel = function (that) {
        that.events.onCancel.fire();
        that.fetch();
    };

    // called once markup is applied to the document containing tab component roots
    fluid.prefs.prefsEditor.finishInit = function (that) {
        var bindHandlers = function (that) {
            var saveButton = that.locate("save");
            if (saveButton.length > 0) {
                saveButton.click(that.saveAndApply);
                var form = fluid.findForm(saveButton);
                $(form).submit(function () {
                    that.saveAndApply();
                });
            }
            that.locate("reset").click(that.reset);
            that.locate("cancel").click(that.cancel);
        };

        that.container.append(that.options.resources.template.resourceText);
        bindHandlers(that);

        that.fetch("onPrefsEditorMarkupReady");
        that.events.onReady.fire(that);
    };

    fluid.prefs.prefsEditor.handleAutoSave = function (that) {
        if (that.options.autoSave) {
            that.events.onAutoSave.fire();
        }
    };

    fluid.prefs.prefsEditor.init = function (that) {
        // This setTimeout is to ensure that fetching of resources is asynchronous,
        // and so that component construction does not run ahead of subcomponents for SeparatedPanel
        // (FLUID-4453 - this may be a replacement for a branch removed for a FLUID-2248 fix)
        setTimeout(function () {
            if (!fluid.isDestroyed(that)) {
                fluid.prefs.prefsEditor.finishInit(that);
            }
        }, 1);
    };

    /******************************
     * Preferences Editor Preview *
     ******************************/

    fluid.defaults("fluid.prefs.preview", {
        gradeNames: ["fluid.viewComponent"],
        components: {
            enhancer: {
                type: "fluid.uiEnhancer",
                container: "{preview}.enhancerContainer",
                createOnEvent: "onReady"
            },
            templateLoader: "{templateLoader}"
        },
        invokers: {
            updateModel: {
                funcName: "fluid.prefs.preview.updateModel",
                args: [
                    "{preview}",
                    "{prefsEditor}.model.preferences"
                ]
            }
        },
        events: {
            onReady: null
        },
        listeners: {
            "onCreate.startLoadingContainer": "fluid.prefs.preview.startLoadingContainer",
            "{prefsEditor}.events.modelChanged": "{that}.updateModel",
            "onReady.updateModel": "{that}.updateModel"
        },
        templateUrl: "%prefix/PrefsEditorPreview.html"
    });

    fluid.prefs.preview.updateModel = function (that, preferences) {
        /**
         * SetTimeout is temp fix for http://issues.fluidproject.org/browse/FLUID-2248
         */
        setTimeout(function () {
            if (that.enhancer) {
                that.enhancer.updateModel(preferences);
            }
        }, 0);
    };

    fluid.prefs.preview.startLoadingContainer = function (that) {
        var templateUrl = that.templateLoader.transformURL(that.options.templateUrl);
        that.container.on("load", function () {
            that.enhancerContainer = $("body", that.container.contents());
            that.events.onReady.fire();
        });
        that.container.attr("src", templateUrl);
    };

})(jQuery, fluid_2_0_0);
