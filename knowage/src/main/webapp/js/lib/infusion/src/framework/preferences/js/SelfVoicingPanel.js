/*
Copyright 2014-2015 OCAD University

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://github.com/fluid-project/infusion/raw/master/Infusion-LICENSE.txt
*/

var fluid_2_0_0 = fluid_2_0_0 || {};

(function ($, fluid) {
    "use strict";

    /**********************************************************************************
    * speakPanel
    **********************************************************************************/
    fluid.defaults("fluid.prefs.panel.speak", {
        gradeNames: ["fluid.prefs.panel"],
        preferenceMap: {
            "fluid.prefs.speak": {
                "model.speak": "default"
            }
        },
        selectors: {
            speak: ".flc-prefsEditor-speak",
            label: ".flc-prefsEditor-speak-label",
            speakDescr: ".flc-prefsEditor-speak-descr"
        },
        protoTree: {
            label: {messagekey: "speakLabel"},
            speakDescr: {messagekey: "speakDescr"},
            speak: "${speak}"
        }
    });

})(jQuery, fluid_2_0_0);
