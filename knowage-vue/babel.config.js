module.exports = {
  presets: ['@vue/app'],
  env: {
    test: {
      presets: [['@babel/preset-env']],
      plugins: ['@babel/plugin-transform-runtime']
    }
  }
}
