/*
Copyright 2015 OCAD University

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://github.com/fluid-project/infusion/raw/master/Infusion-LICENSE.txt
*/

var fluid_2_0_0 = fluid_2_0_0 || {};

(function (fluid) {
    "use strict";

    /*******************************************************************************
    * Starter auxiliary schema grade
    *
    * Contains the settings for 7 preferences: text size, line space, text font,
    * contrast, table of contents, inputs larger and emphasize links
    *******************************************************************************/

    // Fine-tune the starter aux schema and add speak panel
    fluid.defaults("fluid.prefs.auxSchema.speak", {
        gradeNames: ["fluid.prefs.auxSchema"],
        auxiliarySchema: {
            "namespace": "fluid.prefs.constructed",
            "terms": {
                "templatePrefix": "../../framework/preferences/html/",
                "messagePrefix": "../../framework/preferences/messages/"
            },
            "template": "%templatePrefix/SeparatedPanelPrefsEditor.html",
            "message": "%messagePrefix/prefsEditor.json",

            speak: {
                type: "fluid.prefs.speak",
                enactor: {
                    type: "fluid.prefs.enactor.selfVoicing",
                    container: "body"
                },
                panel: {
                    type: "fluid.prefs.panel.speak",
                    container: ".flc-prefsEditor-speak",
                    template: "%templatePrefix/PrefsEditorTemplate-speak.html",
                    message: "%messagePrefix/speak.json"
                }
            }
        }
    });


    /*******************************************************************************
    * Primary Schema
    *******************************************************************************/

    // add extra prefs to the starter primary schemas

    fluid.defaults("fluid.prefs.schemas.speak", {
        gradeNames: ["fluid.prefs.schemas"],
        schema: {
            "fluid.prefs.speak": {
                "type": "boolean",
                "default": false
            }
        }
    });

})(fluid_2_0_0);
