/*
Copyright 2015 OCAD University

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://github.com/fluid-project/infusion/raw/master/Infusion-LICENSE.txt

Includes code from Underscore.js 1.8.3
http://underscorejs.org
(c) 2009-2016 Jeremy Ashkenas, DocumentCloud and Investigative Reporters & Editors
Underscore may be freely distributed under the MIT license.

*/

/* global speechSynthesis, SpeechSynthesisUtterance*/

var fluid_2_0_0 = fluid_2_0_0 || {};

(function ($, fluid) {
    "use strict";

    fluid.registerNamespace("fluid.textToSpeech");

    /******************************************************************************************* *
     * fluid.textToSpeech provides a wrapper around the SpeechSynthesis Interface                *
     * from the Web Speech API ( https://dvcs.w3.org/hg/speech-api/raw-file/tip/speechapi.html ) *
     *********************************************************************************************/

    fluid.textToSpeech.isSupported = function () {
        return !!(window && window.speechSynthesis);
    };

    /**
     * Ensures that TTS is supported in the browser, including cases where the
     * feature is detected, but where the underlying audio engine is missing.
     * For example in VMs on SauceLabs, the behaviour for browsers which report that the speechSynthesis
     * API is implemented is for the `onstart` event of an utterance to never fire. If we don't receive this
     * event within a timeout, this API's behaviour is to return a promise which rejects.
     *
     * @param delay {Number} A time in milliseconds to wait for the speechSynthesis to fire its onStart event
     * by default it is 5000ms (5s). This is crux of the test, as it needs time to attempt to run the speechSynthesis.
     * @return {fluid.promise} A promise which will resolve if the TTS is supported (the onstart event is fired within the delay period)
     * or be rejected otherwise.
     */
    fluid.textToSpeech.checkTTSSupport = function (delay) {
        var promise = fluid.promise();
        if (fluid.textToSpeech.isSupported()) {
            // MS Edge speech synthesizer won't speak if the text string is blank,
            // so this must contain actual text
            var toSpeak = new SpeechSynthesisUtterance("short"); // short text to attempt to speak
            toSpeak.volume = 0; // mutes the Speech Synthesizer
            // Same timeout as the timeout in the IoC testing framework
            var timeout = setTimeout(function () {
                fluid.textToSpeech.deferredSpeechSynthesisControl("cancel");
                promise.reject();
            }, delay || 5000);
            toSpeak.onend = function () {
                clearTimeout(timeout);
                fluid.textToSpeech.deferredSpeechSynthesisControl("cancel");
                promise.resolve();
            };
            fluid.textToSpeech.deferredSpeechSynthesisControl("speak", toSpeak);
        } else {
            setTimeout(promise.reject, 0);
        }
        return promise;
    };


    fluid.defaults("fluid.textToSpeech", {
        gradeNames: ["fluid.modelComponent"],
        events: {
            onStart: null,
            onStop: null,
            onError: null,
            onSpeechQueued: null
        },
        members: {
            queue: []
        },
        // Model paths: speaking, pending, paused, utteranceOpts, pauseRequested, resumeRequested
        model: {
            // Changes to the utteranceOpts will only text that is queued after the change.
            // All of these options can be overriden in the queueSpeech method by passing in
            // options directly there. It is useful in cases where a single instance needs to be
            // spoken with different options (e.g. single text in a different language.)
            utteranceOpts: {
                // text: "", // text to synthesize. avoid as it will override any other text passed in
                // lang: "", // the language of the synthesized text
                // voice: {} // a WebSpeechSynthesis object; if not set, will use the default one provided by the browser
                // volume: 1, // a value between 0 and 1
                // rate: 1, // a value from 0.1 to 10 although different synthesizers may have a smaller range
                // pitch: 1, // a value from 0 to 2
            }
        },
        modelListeners: {
            "speaking": {
                listener: "fluid.textToSpeech.speak",
                args: ["{that}", "{change}.value"]
            },
            "pauseRequested": {
                listener: "fluid.textToSpeech.requestControl",
                args: ["{that}", "pause", "{change}"]
            },
            "resumeRequested": {
                listener: "fluid.textToSpeech.requestControl",
                args: ["{that}", "resume", "{change}"]
            }
        },
        invokers: {
            queueSpeech: {
                funcName: "fluid.textToSpeech.queueSpeech",
                args: ["{that}", "{arguments}.0", "{arguments}.1", "{arguments}.2"]
            },
            cancel: {
                funcName: "fluid.textToSpeech.cancel",
                args: ["{that}"]
            },
            pause: {
                changePath: "pauseRequested",
                value: true
            },
            resume: {
                changePath: "resumeRequested",
                value: true
            },
            getVoices: {
                "this": "speechSynthesis",
                "method": "getVoices"
            },
            handleStart: {
                changePath: "speaking",
                value: true
            },
            // The handleEnd method is assumed to be triggered asynchronously
            // as it is processed/triggered by the mechanism voicing the utterance.
            handleEnd: {
                funcName: "fluid.textToSpeech.handleEnd",
                args: ["{that}"]
            },
            handleError: "{that}.events.onError.fire",
            handlePause: {
                changePath: "paused",
                value: true
            },
            handleResume: {
                changePath: "paused",
                value: false
            }
        }
    });

    // Issue commands to the speechSynthesis interface with deferral (1 ms timeout);
    // this makes the wrapper behave better when issuing commands, especially
    // play and pause
    fluid.textToSpeech.deferredSpeechSynthesisControl = function (control, args) {
        setTimeout(function () {
            speechSynthesis[control](args);
        }, 1);
    };

    // Throttle implementation adapted from underscore.js 1.8.3; see
    // file header for license details
    // Returns a version of a function that will only be called max once
    // every "wait" MS
    fluid.textToSpeech.throttle = function (func, wait, options) {
        var timeout, context, args, result;
        var previous = 0;
        if (!options) {
            options = {};
        }

        var later = function () {
            previous = options.leading === false ? 0 : new Date().getTime();
            timeout = null;
            result = func.apply(context, args);
            if (!timeout) {
                context = args = null;
            }
        };

        var throttled = function () {
            var now = new Date().getTime();
            if (!previous && options.leading === false) {
                previous = now;
            }
            var remaining = wait - (now - previous);
            context = this;
            args = arguments;
            if (remaining <= 0 || remaining > wait) {
                if (timeout) {
                    clearTimeout(timeout);
                    timeout = null;
                }
                previous = now;

                result = func.apply(context, args);
                if (!timeout) {
                    context = args = null;
                }
            } else if (!timeout && options.trailing !== false) {
                timeout = setTimeout(later, remaining);
            }
            return result;
        };

        throttled.cancel = function () {
            clearTimeout(timeout);
            previous = 0;
            timeout = context = args = null;
        };
        return throttled;
    };

    // Throttled version of deferred speech synthesis control
    fluid.textToSpeech.throttleControl = fluid.textToSpeech.throttle(fluid.textToSpeech.deferredSpeechSynthesisControl, 100, {leading: false});

    fluid.textToSpeech.speak = function (that, speaking) {
        that.events[speaking ? "onStart" : "onStop"].fire();
    };

    fluid.textToSpeech.requestControl = function (that, control, change) {
        // If there's a control request (value change to true), clear and
        // execute it
        if (change.value) {
            that.applier.change(change.path, false);
            fluid.textToSpeech.throttleControl(control);
        }
    };

    fluid.textToSpeech.handleEnd = function (that) {

        that.queue.shift();

        var resetValues = {
            speaking: false,
            pending: false,
            paused: false
        };

        if (that.queue.length) {
            that.applier.change("pending", true);
        } else if (!that.queue.length) {
            var newModel = $.extend({}, that.model, resetValues);
            that.applier.change("", newModel);
        }
    };

    fluid.textToSpeech.queueSpeech = function (that, text, interrupt, options) {
        if (interrupt) {
            that.cancel();
        }

        var errorFn = function () {
            that.handleError(text);
        };

        var toSpeak = new SpeechSynthesisUtterance(text);


        var eventBinding = {
            onstart: that.handleStart,
            onend: that.handleEnd,
            onerror: errorFn,
            onpause: that.handlePause,
            onresume: that.handleResume
        };
        $.extend(toSpeak, that.model.utteranceOpts, options, eventBinding);

        // Store toSpeak additionally on the queue to help deal
        // with premature garbage collection described at https://bugs.chromium.org/p/chromium/issues/detail?id=509488#c11
        // this makes the speech synthesis behave much better in Safari in
        // particular
        that.queue.push({text: text, utterance: toSpeak});

        that.events.onSpeechQueued.fire(text);
        fluid.textToSpeech.deferredSpeechSynthesisControl("speak", toSpeak);
    };

    fluid.textToSpeech.cancel = function (that) {
        that.queue = [];
        fluid.textToSpeech.deferredSpeechSynthesisControl("cancel");
    };

})(jQuery, fluid_2_0_0);
