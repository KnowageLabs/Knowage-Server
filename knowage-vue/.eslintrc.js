module.exports = {
    env: {
        node: true,
        jest: true
    },
    globals: {
        process: true
    },
    extends: ['plugin:vue/vue3-essential', 'eslint:recommended', '@vue/typescript'],
    rules: {
        '@typescript-eslint/no-unused-vars': ['error', { ignoreRestSiblings: true }]
    }
}
