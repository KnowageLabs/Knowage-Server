import store from '@/App.store'

export default {
    logout(isPublicUser?:string):void {
        localStorage.clear()
        store.commit('setUser', {})
        let url = window.location.origin
        window.location.href = `${url}/knowage/${isPublicUser?'public/':''}servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE&NEW_SESSION=TRUE`
    },
    handleUnauthorized():void {
        localStorage.clear()
        store.commit('setUser', {})
        let url = window.location.origin
        window.location.href = url + '/knowage/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE'
    }
}
