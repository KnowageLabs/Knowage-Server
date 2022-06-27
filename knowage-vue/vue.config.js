process.env.VUE_APP_VERSION = process.env.npm_package_version

module.exports = {
    publicPath: process.env.VUE_APP_PUBLIC_PATH,
    outputDir: './src/main/webapp',
    devServer: {
        https: process.env.VUE_APP_HOST_HTTPS === 'true',
        proxy: {
            '^/knowagedossierengine/api': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowageqbeengine/': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowagemeta/restful-services/1.0/': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowage/restful-services/[0-9].0': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowage/restful-services/': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowage-api/api/': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowage/webSocket': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowage/servlet': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowagemeta': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowagecockpitengine/': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowagewhatifengine/': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowage-data-preparation/': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true,
                ws: true
            },
            '^/knowagekpiengine/': {
                target: process.env.VUE_APP_HOST_URL,
                changeOrigin: true
            }
        }
    },
    configureWebpack: {
        devtool: 'source-map',
        devServer: {
            headers: { 'Access-Control-Allow-Origin': '*' }
        }
    },
    chainWebpack: (config) => {
        config.plugin('html').tap((args) => {
            args[0].title = 'Knowage'
            return args
        })
    },
    css: {
        loaderOptions: {
            scss: {
                additionalData: '@import "@/assets/scss/main.scss";'
            }
        }
    }
}
