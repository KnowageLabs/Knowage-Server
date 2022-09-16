/**
 * Forwards routes in the given list to a route with a trailing slash in the dev server
 * Useful for multi page vite apps where all rollup inputs are known.
 * 
 * Vite fix is upcoming, which will make this plugin unnecessary
 * https://github.com/vitejs/vite/issues/6596
 */
export default routes => ({
    name: 'forward-to-trailing-slash',
    configureServer(server) {
        server.middlewares.use((req, _res, next) => {
            const requestURLwithoutLeadingSlash = req.url.substring(1)

            if (routes.includes(requestURLwithoutLeadingSlash)) {
                req.url = `${req.url}/`
            }
            next()
        })
    }
})