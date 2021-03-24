module.exports = {
  preset: '@vue/cli-plugin-unit-jest/presets/typescript-and-babel',
  transformIgnorePatterns: ['/node_modules/(?!vue-awesome)'],
  testMatch: ['**/**/**/w*.spec.[jt]s?(x)'],
  moduleNameMapper: {
    vue$: 'vue/dist/vue'
  },

  transform: {
    '^.+\\.vue$': 'vue-jest',
    '.+\\.(css|styl|less|sass|scss|png|jpg|ttf|woff|woff2)$': 'jest-transform-stub',
    '^.+\\.(js|jsx)?$': 'babel-jest'
  }
}
