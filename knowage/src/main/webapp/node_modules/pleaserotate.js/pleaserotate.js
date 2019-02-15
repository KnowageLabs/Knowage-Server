(function(){
    var PleaseRotate = {},
        currentOrientation = null,
        isMobile = /Android|iPhone|iPad|iPod|IEMobile|Opera Mini/i.test(navigator.userAgent),
        init = false;

    var options = {
        startOnPageLoad: true,
        onHide: function(){},
        onShow: function(){},
        forcePortrait: false,
        message: "Please Rotate Your Device",
        subMessage: "(or click to continue)",
        allowClickBypass: true,
        onlyMobile: true,
        zIndex: 1000,
        iconNode: null
    };

    var cssRules = [
       "#pleaserotate-graphic { margin-left: 50px; width: 200px; animation: pleaserotateframes ease 2s; animation-iteration-count: infinite; transform-origin: 50% 50%; -webkit-animation: pleaserotateframes ease 2s; -webkit-animation-iteration-count: infinite; -webkit-transform-origin: 50% 50%; -moz-animation: pleaserotateframes ease 2s; -moz-animation-iteration-count: infinite; -moz-transform-origin: 50% 50%; -ms-animation: pleaserotateframes ease 2s; -ms-animation-iteration-count: infinite; -ms-transform-origin: 50% 50%; }",
        "#pleaserotate-backdrop { background-color: white; top: 0; left: 0; position: fixed; width: 100%; height: 100%; }",
        "#pleaserotate-container { width: 300px; position: absolute; top: 50%; left: 50%; margin-right: -50%; transform: translate(-50%, -50%); -webkit-transform: translate(-50%, -50%); }",
        "#pleaserotate-message { margin-top: 20px; font-size: 1.3em; text-align: center; font-family: Verdana, Geneva, sans-serif; text-transform: uppercase }",
        "#pleaserotate-message small { opacity: .5; display: block; font-size: .6em}"
    ];
    
    var cssKeyframeRules = [
        "pleaserotateframes{ 0% { transform:  rotate(0deg) ; -moz-transform:  rotate(0deg) ;-webkit-transform:  rotate(0deg) ;-ms-transform:  rotate(0deg) ;} 49% { transform:  rotate(90deg) ;-moz-transform:  rotate(90deg) ;-webkit-transform:  rotate(90deg) ; -ms-transform:  rotate(90deg) ;  } 100% { transform:  rotate(90deg) ;-moz-transform:  rotate(90deg) ;-webkit-transform:  rotate(90deg) ; -ms-transform:  rotate(90deg) ;  } }",
    ];

    /* private functions */
   
    function overrideOptions(updates){
        var prop;
        for (var prop in updates){
            options[prop] = updates[prop];
        }
    }

    function setBodyClass(state){
        if(document.documentElement){
            document.documentElement.className = document.documentElement.className.replace( /(?:^|\s)pleaserotate-\S*/g , '' );
            document.documentElement.className += " pleaserotate-" + state;
        }
    }

    function addRules(sheet){

        var i;

        for(i = 0; i< cssRules.length; i++){
            sheet.insertRule(cssRules[i], 0);
        }

        sheet.insertRule("#pleaserotate-backdrop { z-index: " + options.zIndex + "}", 0);

        if(options.allowClickBypass){
            sheet.insertRule("#pleaserotate-backdrop { cursor: pointer }", 0);
        }

        if(options.forcePortrait){
            sheet.insertRule("#pleaserotate-backdrop { -webkit-transform-origin: 50% }", 0);

        }

        for(i = 0; i< cssKeyframeRules.length; i++){
            if (CSSRule.WEBKIT_KEYFRAMES_RULE) { // WebKit
                sheet.insertRule("@-webkit-keyframes " + cssKeyframeRules[i], 0);
            }
            else if (CSSRule.MOZ_KEYFRAMES_RULE) { // Mozilla
                sheet.insertRule("@-moz-keyframes " + cssKeyframeRules[i], 0);
            }
            else if (CSSRule.KEYFRAMES_RULE) { // W3C
                sheet.insertRule("@keyframes " + cssKeyframeRules[i], 0);
            }
        }

    }

    function createStyleSheet(){
        var style = document.createElement("style");
        style.appendChild(document.createTextNode("")); // I'm told we need this hack... something to do with safari but I don't feel like checking for sure
        document.head.insertBefore(style, document.head.firstChild);
        addRules(style.sheet);
    }

    function createElements(){
        var backdrop = document.createElement("div"),
            container = document.createElement("div"),
            message = document.createElement("div"),
            subMessage = document.createElement("small");

        backdrop.setAttribute("id", "pleaserotate-backdrop");
        container.setAttribute("id", "pleaserotate-container");
        message.setAttribute("id", "pleaserotate-message");

        backdrop.appendChild(container);

        if(options.iconNode !== null){
            container.appendChild(options.iconNode);
        } else {
            container.appendChild(createPhoneSVG());
        }

        container.appendChild(message);
        message.appendChild(document.createTextNode(options.message));
        subMessage.appendChild(document.createTextNode(options.subMessage));

        message.appendChild(subMessage);

        document.body.appendChild(backdrop);

    }

    function createPhoneSVG(){
        var svg = document.createElementNS('http://www.w3.org/2000/svg','svg');
        svg.setAttributeNS('http://www.w3.org/2000/xmlns/','xmlns:xlink','http://www.w3.org/1999/xlink');
        svg.setAttribute('id','pleaserotate-graphic');
        svg.setAttribute('viewBox','0 0 250 250');

        var group = document.createElementNS('http://www.w3.org/2000/svg','g');
        group.setAttribute('id','pleaserotate-graphic-path');

        if(options.forcePortrait){
            group.setAttribute('transform','rotate(-90 125 125)');
        }

        var path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        path.setAttribute('d', 'M190.5,221.3c0,8.3-6.8,15-15,15H80.2c-8.3,0-15-6.8-15-15V28.7c0-8.3,6.8-15,15-15h95.3c8.3,0,15,6.8,15,15V221.3z' +
            'M74.4,33.5l-0.1,139.2c0,8.3,0,17.9,0,21.5c0,3.6,0,6.9,0,7.3c0,0.5,0.2,0.8,0.4,0.8s7.2,0,15.4,0h75.6c8.3,0,15.1,0,15.2,0' + 
            's0.2-6.8,0.2-15V33.5c0-2.6-1-5-2.6-6.5c-1.3-1.3-3-2.1-4.9-2.1H81.9c-2.7,0-5,1.6-6.3,4C74.9,30.2,74.4,31.8,74.4,33.5z' + 
            'M127.7,207c-5.4,0-9.8,5.1-9.8,11.3s4.4,11.3,9.8,11.3s9.8-5.1,9.8-11.3S133.2,207,127.7,207z');
        svg.appendChild(group);
        group.appendChild(path);

        return svg;
    }

    function setVisibility(visible){
        var backdropElement = document.getElementById("pleaserotate-backdrop");

        if(visible){
            if(backdropElement){
                backdropElement.style["display"] = "block";
            }
        } else {
            if(backdropElement){
                backdropElement.style["display"] = "none";

            }
        }
    }

    function orientationChanged(){
        var triggerOn = currentOrientation && !options.forcePortrait || !currentOrientation && options.forcePortrait,
            propogate;
            
        if(triggerOn){
            propogate = options.onShow();
            setBodyClass("showing");
        } else {
            propogate = options.onHide();
            setBodyClass("hiding");
        }


        if(propogate !== undefined && !propogate){
            return;
        }

        PleaseRotate.Showing = triggerOn;

        setVisibility(triggerOn);

    }

    function isPortrait(){
        return ( window.innerWidth < window.innerHeight);
    }

    function checkOrientationChange(){
        if(!isMobile && options.onlyMobile){
            if(!init){
                init = true;
                setVisibility(false);
                setBodyClass("hiding");
                options.onHide(); // run this exactly once if not mobile
            }
            return;
        }

        if(currentOrientation !== isPortrait()){
            currentOrientation = isPortrait();
            orientationChanged();
        }
    }

    /* public functions */

    PleaseRotate.start = function(opts){
        if(!document.body){
            window.addEventListener('load', PleaseRotate.start.bind(null, opts), false);
            return;
        }

        if(opts){
            overrideOptions(opts);
        }
        
        createStyleSheet();
        createElements();
        checkOrientationChange();
        window.addEventListener( 'resize', checkOrientationChange, false );

        if(options.allowClickBypass){
            document.getElementById("pleaserotate-backdrop").addEventListener("click", function(){
                var propogate = options.onHide();
                setBodyClass("hiding");
                PleaseRotate.Showing = false;
                
                if(propogate === undefined || propogate){
                    setVisibility(false);
                }
            });
        }
    }

    PleaseRotate.stop = function(){
        window.removeEventListener('resize', onWindowResize, false);
    }

    PleaseRotate.onShow = function(fn){
        options.onShow = fn;

        if(init){
            // if we have already been initialized, force a check
            init = false;
            currentOrientation = null;
            checkOrientationChange();
        }
    };

    PleaseRotate.onHide = function(fn){
        options.onHide = fn;

        if(init){
            // if we have already been initialized, force a check so that onHide gets called
            currentOrientation = null;
            init = false;
            checkOrientationChange();
        }
    };

    PleaseRotate.Showing = false;

    /* plumbing to support AMD, CommonJS, or Globals */

    if (typeof define === 'function' && define.amd) {
        setBodyClass("initialized");
        define(['PleaseRotate'], function() {
            return PleaseRotate;
        });
    } else if (typeof exports === 'object') {
        setBodyClass("initialized");
        module.exports = PleaseRotate;
    } else {
        setBodyClass("initialized");
        window.PleaseRotate = PleaseRotate;
        overrideOptions(window.PleaseRotateOptions);

        if (options.startOnPageLoad) {
            PleaseRotate.start();
        }
    }

})();
