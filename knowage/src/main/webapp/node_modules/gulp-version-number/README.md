# gulp-version-number #
Add version number to js/css/image in HTML

[![NPM version](https://img.shields.io/npm/v/fecs.svg?style=flat)](https://www.npmjs.com/package/gulp-version-number)

## usage ##

    var version = require('gulp-version-number');

    gulp.src('src/*.html')
        .pipe(version({
    		... configuration ...
        }))
        .pipe(gulp.dest('build'));

## configuration ##

**config**

    {
    
        /**
         * Global version value
         * default: %MDS%
         */
        'value' : '%MDS%',
    
        /**
         * MODE: REPLACE
         * eg:
         *    'keyword'
         *    /regexp/ig
         *    ['keyword']
         *    [/regexp/ig, '%MD5%']]
         */
        'replaces' : [
        
            /**
             * {String|Regexp} Replace Keyword/Rules to global value (config.value)
             */
            '#{VERSION_REPlACE}#',
            
            /**
             * {Array}
             * Replace keyword to custom value
             * if just have keyword, the value will use the global value (config.value).
             */    
            [/#{VERSION_REPlACE}#/g, '%TS%']
        ],
        
        
        /**
         * MODE: APPEND
         * Can coexist and replace, after execution to replace
         */
        'append' : {
        
            /**
             * Parameter
             */
            'key' : '_v',
            
            /**
             * Whether to overwrite the existing parameters
             * default: 0 (don't overwrite)
             * If the parameter already exists, as a "custom", covering not executed.
             * If you need to cover, please set to 1
             */
            'cover' : 0,
            
            /**
             * Appended to the position (specify type)
             * {String|Array|Object}
             * If you set to 'all', will apply to all type, rules will use the global setting.
             * If an array or object, will use your custom rules.
             * others will passing.
             * 
             * eg:
             *     'js'
             *     ['js']
             *     {type:'js'}
             *     ['css', '%DATE%']
             */
            'to' : [
            
                /**
                 * {String} Specify type, the value is the global value
                 */
                'css',
                
                /**
                 * {Array}
                 * Specify type, keyword and cover rules will use the global 
                 * setting, If you need more details, please use the object 
                 * configure.
                 *
                 * argument 0 necessary, otherwise passing.
                 * argument 1 optional, the value will use the global value
                 */
                  ['image', '%TS%'],
                  
                /**
                 * {Object}
                 * Use detailed custom rules to replace, missing items will 
                 * be taken in setting the global completion
                 
                 * type is necessary, otherwise passing.
                 */
                {
                    'type' : 'js',
                    'key' : '_v',
                    'value' : '%DATE%',
                    'cover' : 1
                }
            ]
        },
     
        /**
         * Output to config file
         */
        'output' : {
            'file' : 'version.json'
        }
    }

---

**Priority - Covering relations**

- {Object}config.append.to[x].type == {Array}config.append.to[x][0] == {String}config.append.to[x]
- config.append.to[x].key > config.append.key
- config.append.to[x].cover > config.append.cover
- config.append.to[x].value == config.append.to[x][1] [ (IF cover is TRUE) > (ELSE) == config.replace[x][1] ] > config.value

## Options ##

**Version types**

- %DATE% date [**YYYYMMDD**]
- %DT% date + time [**YYYYMMDDHHIISS**]
- %TS% timestamp [**INT**10]
- %TSM% timestamp(millisecond) [**INT**13]
- %MD5% MD5(timestamp) [**STRING**32]
- %MDS% MD5(MD5(timestamp) + salt) [**STRING**32]
- {STRING} In addition to the above keywords, considered custom


## Change log ##

##### = 0.1.4 = #####
- Detailed description comment and readme.
- Change salt to length 8

##### = 0.1.3 = #####
- BUGFIX: css rules

##### = 0.1.2 = #####
- Output function mounting, the version output to a file.


##### = 0.1.1 = #####
- BUGFIX: regexp

##### = 0.1.0 = #####
**I was born**
