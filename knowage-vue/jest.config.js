module.exports = {
	preset: '@vue/cli-plugin-unit-jest/presets/typescript-and-babel',
	transformIgnorePatterns: ['/node_modules/(?!vue-awesome)'],
	//testMatch: ['**/**/**/**/*.spec.[jt]s?(x)', 'src/components/languageDialog/languageDialog.spec.js'],
	testMatch: ['**/?(*.)+(spec).[jt]s?(x)'],

	transform: {
		'^.+\\.vue$': 'vue-jest',
		'.+\\.(css|styl|less|sass|scss|png|jpg|ttf|woff|woff2)$': 'jest-transform-stub',
		'^.+\\.(js|jsx)?$': 'babel-jest'
	}
}
