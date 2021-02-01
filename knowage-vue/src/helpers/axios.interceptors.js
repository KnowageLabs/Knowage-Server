import axios from 'axios'

export default function execute() {

  axios.interceptors.response.use(
    err => {
      console.log(err)
      throw err;
    }
  )
}
