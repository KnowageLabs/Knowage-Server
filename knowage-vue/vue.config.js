import.meta.env.VUE_APP_VERSION = import.meta.env.npm_package_version

module.exports = {
    publicPath: import.meta.env.VUE_APP_PUBLIC_PATH,
    outputDir: './src/main/webapp',
    devServer: {
        https: import.meta.env.VUE_APP_HOST_HTTPS === 'true',
        proxy: {
            '^/knowagedossierengine/api': {
                target: import.meta.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowageqbeengine/': {
                target: import.meta.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowagemeta/restful-services/1.0/': {
                target: import.meta.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowage/restful-services/[0-9].0': {
                target: import.meta.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowage/restful-services/': {
                target: import.meta.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowage-api/api/': {
                target: import.meta.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowage/webSocket': {
                target: import.meta.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowage/servlet': {
                target: import.meta.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowagemeta': {
                target: import.meta.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowagecockpitengine/': {
                target: import.meta.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowagewhatifengine/': {
                target: import.meta.env.VUE_APP_HOST_URL,
                changeOrigin: true
            },
            '^/knowage-data-preparation/': {
                target: import.meta.env.VUE_APP_HOST_URL,
                changeOrigin: true,
                ws: true
            },
            '^/knowagekpiengine/': {
                target: import.meta.env.VUE_APP_HOST_URL,
                changeOrigin: true
            }
        }
    },
    configureWebpack: {
        devtool: 'source-map'
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
