
function resizePage(){
  //$(window.top.document).find('body').height('0px');
  var customBottomMargin = 100;
  var win = $(window).height()
  var iframe = $(window.top.document).find("#iframeDoc");
  var scroll = iframe[0].contentDocument.body.scrollHeight;
  var levels = $('#contentLevels').height()
  var footer = $('#footerContent').outerHeight();
  var toolbar = $('.header-toolbar').height();
  if(win < levels){
          var totHeight = 2*scroll - levels + toolbar + customBottomMargin+footer;
          $('body').height(totHeight+'px')
  }else{
    $('body').height('100%')
  }
  $("#contentLevels").css('margin-bottom','80px');
}

  /**
     * You must include the dependency on 'ngMaterial'
     */

var app = angular.module('knowageIntro', ['ngMaterial', 'ngMessages', 'ngAnimate']);

    app.controller('MainController', ['$scope', function MainController($scope) {

        $scope.level1 = true;
        $scope.level2A = $scope.level2B = $scope.level3A = $scope.level3B = $scope.level4B = false;

        $scope.showLevel1 = function() {
          $scope.level1 = true;
          $scope.level2A = $scope.level2B = $scope.level3A = $scope.level3B = $scope.level4B = false;
          setTimeout(resizePage, 50);
        }

        $scope.showLevel2A = function() {
          $scope.level2A = true;
          $scope.level1 = $scope.level2B = $scope.level3A = $scope.level3B = $scope.level4B = false;
          setTimeout(resizePage, 50);
        }

        $scope.showLevel2B = function() {
          $scope.level2B = true;
          $scope.level1 = $scope.level2A = $scope.level3A = $scope.level3B = $scope.level4B = false;
          setTimeout(resizePage, 50);
        }

        $scope.showLevel3A = function() {
          $scope.level3A = true;
          $scope.level1 = $scope.level2A = $scope.level2B = $scope.level3B = $scope.level4B = false;
          setTimeout(resizePage, 50);
        }

        $scope.showLevel3B = function() {
          $scope.level3B = true;
          $scope.level1 = $scope.level2A = $scope.level3A = $scope.level2B = $scope.level4B = false;
          setTimeout(resizePage, 50);
        }

        $scope.showLevel4B = function() {
            $scope.level4B = true;
            $scope.level1 = $scope.level2A = $scope.level3A = $scope.level2B = $scope.level3B = false;
            setTimeout(resizePage, 50);
        }

        $scope.openMenu = function($mdMenu, ev) {
          originatorEv = ev;
          $mdMenu.open(ev);
        };
      }
    ]);

    // Theme Definition
    app.config(function($mdThemingProvider) {

        $mdThemingProvider.definePalette('s4c-green', {
            '50': 'D3D7DC',
            '100': 'A5B3BA',
            '200': '778F98',
            '300': '496B76',
            '400': '325965',
            '500': '1B4754',
            '600': '153944',
            '700': '12323C',
            '800': '0F2B34',
            '900': '0C242C',
            'A100': 'B1C5C7',
            'A200': '89A9AB',
            'A400': '618D8F',
            'A700': '397173',
            'contrastDefaultColor': 'light',
            'contrastDarkColors': ['50', '100', '200', '300', '400'], //hues which contrast should be 'dark' by default
            'contrastLightColors': undefined    // could also specify this if default was 'dark'
          });
        $mdThemingProvider.definePalette('s4c-orange', {
            '50': 'FFF9F1',
            '100': 'FFEEDE',
            '200': 'FDD8B8',
            '300': 'FBC292',
            '400': 'F9AC6C',
            '500': 'F79646',
            '600': 'DE873F',
            '700': 'C57838',
            '800': 'AC6931',
            '900': '935A2A',
            'A100': 'FFCDB0',
            'A200': 'FFB588',
            'A400': 'FF9D60',
            'A700': 'FF8538',
            'contrastDefaultColor': 'dark',
            'contrastDarkColors': ['50', '100', '200', '300', '400'], //hues which contrast should be 'dark' by default
            'contrastLightColors': undefined    // could also specify this if default was 'dark'
          });

        // Extend the Grey color palette with the white color at code 'A100'        
        var whiteMap = $mdThemingProvider.extendPalette('grey', {
          'A100': '#ffffff',
          'contrastDefaultColor': 'dark'
        });

        // Register the new color palette map with the name <code>neonWhite</code>
        $mdThemingProvider.definePalette('neonWhite', whiteMap);

        // Select For Cities Theme
        $mdThemingProvider.theme('S4C')
                .primaryPalette('s4c-green', {
                  'default': '500', // by default use shade 400 from the pink palette for primary intentions
                  'hue-1': '400', // use shade 100 for the <code>md-hue-1</code> class
                  'hue-2': '300', // use shade 600 for the <code>md-hue-2</code> class
                  'hue-3': '200' // use shade A100 for the <code>md-hue-3</code> class
                })
                // If you specify less than all of the keys, it will inherit from the
                // default shades
                .accentPalette('s4c-orange', {
                  'default': '500' // use shade 500 for default, and keep all other shades the same
                })
                .warnPalette('red')
                .backgroundPalette('neonWhite');


        //Activate Theme
        $mdThemingProvider.setDefaultTheme('S4C');

    });// END Theme Definition
