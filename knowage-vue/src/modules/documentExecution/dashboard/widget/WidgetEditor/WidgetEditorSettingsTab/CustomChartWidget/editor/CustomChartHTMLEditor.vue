<template>
    <div id="htmlMirrorContainer">
        <VCodeMirror ref="codeMirrorHtmlEditor" v-model:value="code" :options="scriptOptions" @keyup="onKeyUp" @keyDown="onKeyUp" @change="onKeyUp" @blur="onKeyUp" />
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import VCodeMirror from 'codemirror-editor-vue3'

export default defineComponent({
    name: 'custom-chart-html-editor',
    components: { VCodeMirror },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        activeIndex: { type: Number, required: true }
    },
    data() {
        return {
            codeMirrorHtmlEditor: null as any,
            scriptOptions: {
                cursor: true,
                line: false,
                lineNumbers: true,
                indentWithTabs: true,
                smartIndent: true,
                lineWrapping: true,
                matchBrackets: true,
                mode: 'xml',
                tabSize: 4,
                theme: 'eclipse'
            },
            cursorPosition: null,
            code: ''
        }
    },
    watch: {
        activeIndex(value: number) {
            if (value === 1 && this.codeMirrorHtmlEditor) setTimeout(() => this.codeMirrorHtmlEditor.refresh(), 100)
        }
    },
    created() {
        this.setupCodeMirror()
    },
    methods: {
        setupCodeMirror() {
            const interval = setInterval(() => {
                if (!this.$refs.codeMirrorHtmlEditor) return
                this.code = this.widgetModel.settings.editor.html
                this.codeMirrorHtmlEditor = (this.$refs.codeMirrorHtmlEditor as any).cminstance as any
                setTimeout(() => {
                    this.codeMirrorHtmlEditor.refresh()
                }, 0)
                clearInterval(interval)
            }, 200)
        },
        onKeyUp() {
            this.widgetModel.settings.editor.html = this.code
        }
    }
})
</script>

<style lang="scss" scoped>
#htmlMirrorContainer {
    height: 500px;
    width: 100%;
}
</style>
