var uri = process.env.VUE_APP_WEBSOCKET_URL + '/true'
const WEB_SOCKET = new WebSocket(uri)
WEB_SOCKET.update = function(event) {
	console.log(event)
	/* TO BE OVERRIDDEN BY SPECIFIC USAGE */
}
export default WEB_SOCKET
