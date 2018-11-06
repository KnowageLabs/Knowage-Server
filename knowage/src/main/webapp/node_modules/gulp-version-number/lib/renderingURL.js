module.exports = function(config){
	return (config['scheme'] ? config.scheme + ':' + config.slash : '') + (config['host'] ? config.host : '') + (config['port'] ? ':' + config.port : '') + (config['path'] ? '/' + config.path : '') + ( config['query'] ? '?' + config.query : '') + (config['hash'] ? '#' + config.hash : '');;
};
