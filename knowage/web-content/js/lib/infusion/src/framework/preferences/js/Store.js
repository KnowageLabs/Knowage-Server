/*
Copyright 2009 University of Toronto
Copyright 2011-2013 OCAD University

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://github.com/fluid-project/infusion/raw/master/Infusion-LICENSE.txt
*/

var fluid_2_0_0 = fluid_2_0_0 || {};

(function ($, fluid) {
    "use strict";

    /**
     * A Generic data source grade that defines an API for getting and setting
     * data.
     */
     // TODO: unify with Kettle's and ultimately Infusion's dataSource
    fluid.defaults("fluid.prefs.dataSource", {
        gradeNames: ["fluid.component"],
        invokers: {
            get: "fluid.notImplemented",
            set: "fluid.notImplemented"
        }
    });

    fluid.defaults("fluid.prefs.store", {
        gradeNames: ["fluid.prefs.dataSource", "fluid.contextAware"],
        contextAwareness: {
            strategy: {
                defaultGradeNames: "fluid.prefs.cookieStore"
            }
        }
    });

    /****************
     * Cookie Store *
     ****************/

    /**
     * SettingsStore Subcomponent that uses a cookie for persistence.
     * @param {Object} options
     */
    fluid.defaults("fluid.prefs.cookieStore", {
        gradeNames: ["fluid.prefs.store"],
        cookie: {
            name: "fluid-ui-settings",
            path: "/",
            expires: ""
        },
        invokers: {
            get: {
                funcName: "fluid.prefs.cookieStore.get",
                args: "{that}.options.cookie.name"
            },
            set: {
                funcName: "fluid.prefs.cookieStore.set",
                args: ["{arguments}.0", "{that}.options.cookie"]
            }
        }
    });

    /**
     * Retrieve and return the value of the cookie
     */
    fluid.prefs.cookieStore.get = function (cookieName) {
        var cookie = document.cookie;
        if (cookie.length <= 0) {
            return;
        }

        var cookiePrefix = cookieName + "=";
        var startIndex = cookie.indexOf(cookiePrefix);
        if (startIndex < 0) {
            return;
        }

        startIndex = startIndex + cookiePrefix.length;
        var endIndex = cookie.indexOf(";", startIndex);
        if (endIndex < startIndex) {
            endIndex = cookie.length;
        }
        var cookieSection = cookie.substring(startIndex, endIndex);
        var togo;
        try {
            togo = JSON.parse(decodeURIComponent(cookieSection));
        } catch (e) {
            fluid.log("Error parsing cookie " + cookieSection + " as JSON - clearing");
            document.cookie = "";
        }
        return togo;
    };

    /**
     * Assembles the cookie string
     * @param {Object} cookie settings
     */
    fluid.prefs.cookieStore.assembleCookie = function (cookieOptions) {
        var cookieStr = cookieOptions.name + "=" + cookieOptions.data;

        if (cookieOptions.expires) {
            cookieStr += "; expires=" + cookieOptions.expires;
        }

        if (cookieOptions.path) {
            cookieStr += "; path=" + cookieOptions.path;
        }

        return cookieStr;
    };

    /**
     * Saves the settings into a cookie
     * @param {Object} settings
     * @param {Object} cookieOptions
     */
    fluid.prefs.cookieStore.set = function (settings, cookieOptions) {
        cookieOptions.data = encodeURIComponent(JSON.stringify(settings));
        document.cookie = fluid.prefs.cookieStore.assembleCookie(cookieOptions);
    };


    /**************
     * Temp Store *
     **************/

    /**
     * SettingsStore mock that doesn't do persistence.
     * @param {Object} options
     */
    fluid.defaults("fluid.prefs.tempStore", {
        gradeNames: ["fluid.prefs.store", "fluid.modelComponent"],
        invokers: {
            get: {
                funcName: "fluid.identity",
                args: "{that}.model"
            },
            set: {
                funcName: "fluid.prefs.tempStore.set",
                args: ["{arguments}.0", "{that}.applier"]
            }
        }
    });

    fluid.prefs.tempStore.set = function (settings, applier) {
        applier.fireChangeRequest({path: "", type: "DELETE"});
        applier.change("", settings);
    };

    fluid.defaults("fluid.prefs.globalSettingsStore", {
        gradeNames: ["fluid.component"],
        components: {
            settingsStore: {
                type: "fluid.prefs.store",
                options: {
                    gradeNames: ["fluid.resolveRootSingle"],
                    singleRootType: "fluid.prefs.store"
                }
            }
        }
    });

})(jQuery, fluid_2_0_0);
