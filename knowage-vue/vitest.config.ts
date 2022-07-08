import { defineConfig } from 'vite'
import Vue from '@vitejs/plugin-vue'

const path = require('path')

export default defineConfig({
  plugins: [
    Vue(),
  ],
  resolve: {
    extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue'],
    alias: {
        '@': path.resolve(__dirname, './src')
    }
},
  test: {
    globals: true,
    environment: 'jsdom',
  },
})