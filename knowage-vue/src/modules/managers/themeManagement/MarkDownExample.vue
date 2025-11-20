<template>
    <vue-markdown-it :source="markdown" class="markdownContent"> </vue-markdown-it>
</template>

<script setup lang="ts">
import { VueMarkdownIt } from '@f3ve/vue-markdown-it'
import examples from './ThemeManagementDescriptor.json'

const markdown = examples.examples.markdown
</script>
<style lang="scss" scoped>
.markdownContent {
    position: relative;
    width: 100%;
    top: 0;
    background: var(--kn-documentation-content-background);
    color: var(--kn-documentation-content-color);
    font-family: var(--kn-documentation-content-font-family);
    font-size: var(--kn-documentation-content-font-size);
    margin-top: 0;
    padding-left: 20%;
    padding-right: 20%;
    &:deep(h1),
    &:deep(h2),
    &:deep(h3) {
        color: var(--kn-documentation-content-heading-color);
    }
    &:deep(a) {
        color: var(--kn-documentation-content-link-color);
    }
    &:deep(pre) {
        border-radius: var(--kn-documentation-content-code-border-radius);
        border: 1px solid #ccc;
        padding: 4px;
        background-color: var(--kn-documentation-content-code-background);
        color: var(--kn-documentation-content-code-color);
        overflow-x: auto;
    }
    &:deep(table) {
        border-collapse: collapse;
        td,
        th {
            border: 1px solid #ccc;
            padding: 6px 8px;
        }
    }
    &:deep(h1:first-of-type) {
        padding-top: 10px;
        margin-top: 0;
    }
}
</style>
