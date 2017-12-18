/*
Copyright 2011 OCAD University
Copyright 2011 Lucendo Development Ltd.
Copyright 2015 Raising the Floor (International)

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
     * Tabs *
     *********************/

    fluid.defaults("fluid.tabs", {
        gradeNames: ["fluid.viewComponent"],
        tabOptions: {},
        events: {
            // These events are forwarded out of the jQueryUI Tabs' equivalents
            // with signature (that, event, ui)
            tabsselect: "preventable",
            tabsload: null,
            tabsshow: null
        },
        listeners: {
            "onCreate.initTabs": {
                "this": "{that}.container",
                "method": "tabs",
                "args": "{that}.options.tabOptions"
            },
            "onCreate.bindEvents": {
                priority: "after:initTabs",
                funcName: "fluid.tabs.bindEvents"
            }
        }
    });

    fluid.tabs.bindEvents = function (that) { // TODO: The test cases for this component do not exercise any of these events
        fluid.each(that.options.events, function (value, eventName) {
            that.container.on(eventName, function (event, ui) {
                return that.events[eventName].fire(that, event, ui);
            });
        });
    };

})(jQuery, fluid_2_0_0);
