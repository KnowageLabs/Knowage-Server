
export default {
    logout(){
        window.location.href = process.env.VUE_APP_API_URL + '/knowage/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE&NEW_SESSION=TRUE'
    }
}
