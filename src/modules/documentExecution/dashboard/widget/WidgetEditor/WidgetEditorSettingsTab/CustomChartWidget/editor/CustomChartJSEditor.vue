<template>
    <div class="cssMirrorContainer" style="height: 500px; width: 100%">
        <VCodeMirror ref="codeMirrorJsEditor" v-model:value="code" :options="scriptOptions" @keyup="onKeyUp" @keyDown="onKeyUp" @change="onKeyUp" @blur="onKeyUp" />
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import VCodeMirror, { CodeMirror } from 'codemirror-editor-vue3'
import descriptor from './CustomChartWidgetAutocomplete.json'

export default defineComponent({
    name: 'custom-chart-js-editor',
    components: { VCodeMirror },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, activeIndex: { type: Number, required: true } },
    data() {
        return {
            descriptor,
            model: {} as IWidget,
            codeMirrorJsEditor: null as any,
            scriptOptions: {
                cursor: true,
                line: false,
                lineNumbers: true,
                mode: 'javascript',
                tabSize: 4,
                theme: 'eclipse',
                matchBrackets: true,
                extraKeys: {
                    'Ctrl-Space': this.keyAssistFunc
                } as any
            },
            code: ''
        }
    },
    watch: {
        widgetModel() {
            this.loadModel()
        },
        activeIndex(value: number) {
            if (value === 2 && this.codeMirrorJsEditor) setTimeout(() => this.codeMirrorJsEditor.refresh(), 100)
        }
    },
    created() {
        this.loadModel()
        this.setupCodeMirror()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel
        },
        setupCodeMirror() {
            const interval = setInterval(() => {
                if (!this.$refs.codeMirrorJsEditor) return
                this.code = this.model.settings.editor.js
                this.codeMirrorJsEditor = (this.$refs.codeMirrorJsEditor as any).cminstance as any
                setTimeout(() => {
                    this.codeMirrorJsEditor.refresh()
                }, 0)
                clearInterval(interval)
            }, 200)

            CodeMirror.registerHelper('hint', 'placeholder', () => {
                const cur = this.codeMirrorJsEditor.getCursor()
                const tok = this.codeMirrorJsEditor.getTokenAt(cur)
                //const start = tok.string.trim() == '' ? tok.start + 1 : tok.start
                const end = tok.end
                const hintList = descriptor.cmAutocomplete as any

                return { list: hintList, from: CodeMirror.Pos(cur.line, end), to: CodeMirror.Pos(cur.line, end) }
            })
        },
        onKeyUp() {
            this.model.settings.editor.js = this.code
        },
        keyAssistFunc() {
            if (this.isDatastore()) {
                CodeMirror.showHint(this.codeMirrorJsEditor, CodeMirror.hint.placeholder)
            }
        },
        isDatastore() {
            const cursor = this.codeMirrorJsEditor.getCursor()
            const token = this.codeMirrorJsEditor.getTokenAt(cursor)
            if (token.string == 'datastore') {
                return true
            }
        }
    }
})
</script>

<style lang="scss">
.CodeMirror-hints {
    z-index: 99999;
}
</style>
