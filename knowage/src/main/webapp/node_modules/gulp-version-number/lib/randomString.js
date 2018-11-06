module.exports = function(len) {
	var x = "0123456789POIUYTREWQLKJHGFDSAMNBVCXZpoiuytrewqlkjhgfdsamnbvcxz";
	var tmp = [];
	for (var i = 0; i < len; i++) {
		tmp.push(x.charAt(Math.ceil(Math.random() * 100000000) % x.length));
	}
	return tmp.join('');
};