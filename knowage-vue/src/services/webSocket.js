var url = new URL(window.location.origin)
url.protocol = url.protocol.replace('http', 'ws')

var uri = url + import.meta.env.VUE_APP_WEBSOCKET_URL
const WEB_SOCKET = new WebSocket(uri)

export default WEB_SOCKET
