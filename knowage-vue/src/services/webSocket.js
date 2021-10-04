var url = new URL(window.location.origin)
url.protocol = url.protocol.replace('http', 'ws')

var uri = url + process.env.VUE_APP_WEBSOCKET_URL + '/true'
const WEB_SOCKET = new WebSocket(uri)
WEB_SOCKET.update = function(event) {
    console.log(event)
    /* TO BE OVERRIDDEN BY SPECIFIC USAGE */
}
export default WEB_SOCKET
