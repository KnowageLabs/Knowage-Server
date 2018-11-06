/**
 * parse URL
 * @id STK.core.str.parseURL
 * @alias STK.core.str.parseURL
 * @param {String} str
 * @return {Object} that
 * @author Robin Young | yonglin@staff.sina.com.cn
 * @example
 * STK.core.str.parseURL('http://t.sina.com.cn/profile?beijing=huanyingni') ===
 * {
 * 	hash : ''
 * 	host : 't.sina.com.cn'
 * 	path : 'profile'
 * 	port : ''
 * 	query : 'beijing=huanyingni'
 * 	scheme : http
 * 	slash : '//'
 * 	url : 'http://t.sina.com.cn/profile?beijing=huanyingni'
 * }
 */
module.exports = function(url) {
    if (url.substr(0, 1) === '.' || url.substr(0, 1) === '/') {
        var _path_prefix = /.*(?=[\/])/.exec(url)[0];
        url = url.substr(_path_prefix.length);
    }
    else if (!/^(?:([A-Za-z]+):(\/{0,3}))/.test(url)) {
        var _real_path = '' + url;
        url = '/' + url;
    }
    var parse_url = /^(?:([A-Za-z]+):(\/{0,3}))?([0-9.\-A-Za-z]+\.[0-9A-Za-z]+)?(?::(\d+))?(?:\/([^?#]*))?(?:\?([^#]*))?(?:#(.*))?$/;
    var names = ['url', 'scheme', 'slash', 'host', 'port', 'path', 'query', 'hash'];
    var results = parse_url.exec(url);
    var that = {};
    for (var i = 0, len = names.length; i < len; i += 1) {
        that[names[i]] = results[i] || '';
    }
    if ( typeof _path_prefix !== 'undefined') {
        that.host = _path_prefix + '/' + that.path;
        that.path = '';
    }
    else if ( typeof _real_path !== 'undefined') {
        that.host = '' + that.path;
        that.path = '';
    }
    return that;
};
