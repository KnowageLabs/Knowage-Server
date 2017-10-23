/*
Copyright 2011 OCAD University
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

    /***********************************
     * Full Preview Preferences Editor *
     ***********************************/

    fluid.defaults("fluid.prefs.fullPreview", {
        gradeNames: ["fluid.prefs.prefsEditorLoader"],
        outerUiEnhancerOptions: "{originalEnhancerOptions}.options.originalUserOptions",
        outerUiEnhancerGrades: "{originalEnhancerOptions}.uiEnhancer.options.userGrades",
        components: {
            prefsEditor: {
                container: "{that}.container",
                options: {
                    components: {
                        preview: {
                            type: "fluid.prefs.preview",
                            createOnEvent: "onReady",
                            container: "{prefsEditor}.dom.previewFrame",
                            options: {
                                listeners: {
                                    "onReady.boilOnPreviewReady": "{fullPreview}.events.onPreviewReady"
                                }
                            }
                        }
                    },
                    listeners: {
                        "onReady.boilOnPrefsEditorReady": "{fullPreview}.events.onPrefsEditorReady"
                    },
                    distributeOptions: {
                        source: "{that}.options.preview",
                        removeSource: true,
                        target: "{that > preview}.options"
                    }
                }
            }
        },
        events: {
            onPrefsEditorReady: null,
            onPreviewReady: null,
            onReady: {
                events: {
                    onPrefsEditorReady: "onPrefsEditorReady",
                    onPreviewReady: "onPreviewReady"
                },
                args: "{that}"
            }
        },
        distributeOptions: [{
            source: "{that}.options.outerUiEnhancerOptions",
            target: "{that enhancer}.options"
        }, {
            source: "{that}.options.preview",
            target: "{that preview}.options"
        }, {
            source: "{that}.options.previewEnhancer",
            target: "{that enhancer}.options"
        }, {
            source: "{that}.options.outerUiEnhancerGrades",
            target: "{that enhancer}.options.gradeNames"
        }]
    });

})(jQuery, fluid_2_0_0);
