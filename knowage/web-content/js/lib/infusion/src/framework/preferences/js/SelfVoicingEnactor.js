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

    /*******************************************************************************
     * speak
     *
     * An enactor that is capable of speaking text.
     * Typically this will be used as a base grade to an enactor that supplies
     * the text to be spoken.
     *******************************************************************************/

    fluid.defaults("fluid.prefs.enactor.speak", {
        gradeNames: "fluid.prefs.enactor",
        preferenceMap: {
            "fluid.prefs.speak": {
                "model.enabled": "default"
            }
        },
        components: {
            tts: {
                type: "fluid.textToSpeech",
                options: {
                    model: "{speak}.model",
                    invokers: {
                        queueSpeech: {
                            funcName: "fluid.prefs.enactor.speak.queueSpeech",
                            args: ["{that}", "fluid.textToSpeech.queueSpeech", "{arguments}.0", "{arguments}.1", "{arguments}.2"]
                        }
                    }
                }
            }
        }
    });

    // Accepts a speechFn (either a function or function name), which will be used to perform the
    // underlying queuing of the speech. This allows the SpeechSynthesis to be replaced (e.g. for testing)
    fluid.prefs.enactor.speak.queueSpeech = function (that, speechFn, text, interrupt, options) {
        // force a string value
        var str = text.toString();

        // remove extra whitespace
        str = str.trim();
        str.replace(/\s{2,}/gi, " ");

        if (that.model.enabled && str) {
            if (typeof(speechFn) === "string") {
                fluid.invokeGlobalFunction(speechFn, [that, str, interrupt, options]);
            } else {
                speechFn(that, str, interrupt, options);
            }
        }
    };

    /*******************************************************************************
     * selfVoicing
     *
     * The enactor that enables self voicing of an entire page
     *******************************************************************************/

    fluid.defaults("fluid.prefs.enactor.selfVoicing", {
        gradeNames: ["fluid.prefs.enactor.speak", "fluid.viewComponent"],
        modelListeners: {
            "enabled": {
                listener: "{that}.handleSelfVoicing",
                args: ["{change}.value"]
            }
        },
        invokers: {
            handleSelfVoicing: {
                funcName: "fluid.prefs.enactor.selfVoicing.handleSelfVoicing",
                // Pass in invokers to force them to be resolved
                args: ["{that}.options.strings.welcomeMsg", "{tts}.queueSpeech", "{that}.readFromDOM", "{tts}.cancel", "{arguments}.0"]
            },
            readFromDOM: {
                funcName: "fluid.prefs.enactor.selfVoicing.readFromDOM",
                args: ["{that}", "{that}.container"]
            }
        },
        strings: {
            welcomeMsg: "text to speech enabled"
        }
    });

    fluid.prefs.enactor.selfVoicing.handleSelfVoicing = function (welcomeMsg, queueSpeech, readFromDOM, cancel, enabled) {
        if (enabled) {
            queueSpeech(welcomeMsg, true);
            readFromDOM();
        } else {
            cancel();
        }
    };

    // Constants representing DOM node types.
    fluid.prefs.enactor.selfVoicing.nodeType = {
        ELEMENT_NODE: 1,
        TEXT_NODE: 3
    };

    // TODO: Currently only reads text nodes and alt text.
    // This should be expanded to read other text descriptors as well.
    fluid.prefs.enactor.selfVoicing.readFromDOM = function (that, elm) {
        elm = $(elm);
        var nodes = elm.contents();
        fluid.each(nodes, function (node) {
            if (node.nodeType === fluid.prefs.enactor.selfVoicing.nodeType.TEXT_NODE && node.nodeValue) {
                that.tts.queueSpeech(node.nodeValue);
            }

            if (node.nodeType === fluid.prefs.enactor.selfVoicing.nodeType.ELEMENT_NODE && window.getComputedStyle(node).display !== "none") {
                if (node.nodeName === "IMG") {
                    var altText = node.getAttribute("alt");
                    if (altText) {
                        that.tts.queueSpeech(altText);
                    }
                } else {
                    fluid.prefs.enactor.selfVoicing.readFromDOM(that, node);
                }
            }
        });
    };

})(jQuery, fluid_2_0_0);
