import axios from 'axios'
import store from './App.store.js'
//import router from './app.routes.js'

  axios.interceptors.request.use(
    config => {
      return config;
    },
    error => {
        Promise.reject(error)
    });

  axios.interceptors.response.use(
    res => {
      store.commit('setUser',{name:'Davide'})
      return res
    },
    (err, status) => {
      //if(err.status === 401){
        console.log(status)
        //if(router.history.current.name !== 'login') router.push('/login')
      //}
      console.log('axios',err)
      return Promise.reject(err);
    }
  )

export default axios
