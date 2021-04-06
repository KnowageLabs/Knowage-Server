var uri = process.env.VUE_APP_WEBSOCKET_URL + '/true'
const WS = new WebSocket(uri)
export default WS
