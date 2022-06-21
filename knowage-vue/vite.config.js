import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import builtins from 'rollup-plugin-node-builtins';

const path = require('path')

const builtinsPlugin = { ...builtins({ crypto: true }), name: 'rollup-plugin-node-builtins' };

export default defineConfig((command, mode) => {
    const env = loadEnv(mode, process.cwd())
    return {
        plugins: [vue(), builtinsPlugin],
        resolve: {
            extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue'],
            alias: {
                '@': path.resolve(__dirname, './src')
            }
        },
        define: {
            global: {}
        },
        css: {
            preprocessorOptions: {
                scss: {
                    additionalData: '@import "@/assets/scss/main.scss";'
                }
            }
        },
        server: {
            proxy: {
                '^/knowagedossierengine/api': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowageqbeengine/': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowagemeta/restful-services/1.0/': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowage/restful-services/[0-9].0': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowage/restful-services/': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowage-api/api/': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowage/webSocket': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowage/servlet': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowagemeta': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowagecockpitengine/': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowagewhatifengine/': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                },
                '^/knowage-data-preparation/': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true,
                    ws: true
                },
                '^/knowagekpiengine/': {
                    target: env.VITE_HOST_URL,
                    changeOrigin: true
                }
            }
        }
    }
})
