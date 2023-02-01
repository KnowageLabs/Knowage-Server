<template>
    <div class="cssMirrorContainer" style="height: 500px; width: 100%">
        <VCodeMirror ref="codeMirrorJsEditor" v-model:value="code" :options="scriptOptions" @keyup="onKeyUp" @keyDown="onKeyUp" @change="onKeyUp" @blur="onKeyUp" />
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import VCodeMirror, { CodeMirror } from 'codemirror-editor-vue3'

export default defineComponent({
    name: 'custom-chart-js-editor',
    components: { VCodeMirror },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, activeIndex: { type: Number, required: true } },
    data() {
        return {
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
                } as any,
                hintOptions: { test1: 'test1', test2: 'test2' }
            },
            code: ''
        }
    },
    watch: {
        activeIndex(value: number) {
            if (value === 2 && this.codeMirrorJsEditor) setTimeout(() => this.codeMirrorJsEditor.refresh(), 100)
        }
    },
    created() {
        this.setupCodeMirror()
    },
    methods: {
        setupCodeMirror() {
            const interval = setInterval(() => {
                if (!this.$refs.codeMirrorJsEditor) return
                this.code = this.widgetModel.settings.editor.js
                this.codeMirrorJsEditor = (this.$refs.codeMirrorJsEditor as any).cminstance as any
                setTimeout(() => {
                    this.codeMirrorJsEditor.refresh()
                }, 0)
                clearInterval(interval)
            }, 200)

            CodeMirror.registerHelper('hint', 'placeholder', () => {
                const cur = this.codeMirrorJsEditor.getCursor()
                const tok = this.codeMirrorJsEditor.getTokenAt(cur)
                const start = tok.string.trim() == '' ? tok.start + 1 : tok.start
                const end = tok.end
                const hintList = [] as any
                // for (const key in this.aliases) {
                //     if (tok.string.trim() == '' || this.aliases[key].name.startsWith(tok.string)) {
                hintList.push('test1')
                hintList.push('test2')
                // }
                // }
                return { list: hintList, from: CodeMirror.Pos(cur.line, start), to: CodeMirror.Pos(cur.line, end) }
            })
        },
        onKeyUp() {
            this.widgetModel.settings.editor.js = this.code

            const cur = this.codeMirrorJsEditor.getCursor()
            const tok = this.codeMirrorJsEditor.getTokenAt(cur)
            if (tok.string == '@') {
                CodeMirror.showHint(this.codeMirrorJsEditor, CodeMirror.hint.placeholder)
            }
        },
        keyAssistFunc() {
            if (this.isDatastore()) {
                console.log('IS DATASTORE', CodeMirror.hint.placeholder)
                console.log(' this.codeMirror.options.hintOptions ', this.codeMirrorJsEditor.options.hintOptions)
                console.log(' this.codeMirrorJsEditor', this.codeMirrorJsEditor)
                console.log('  CodeMirror.hint.placeholder', CodeMirror.hint.placeholder())
                CodeMirror.showHint(this.codeMirrorJsEditor, CodeMirror.hint.placeholder)
            }
        },
        isDatastore() {
            const cursor = this.codeMirrorJsEditor.getCursor()
            const token = this.codeMirrorJsEditor.getTokenAt(cursor)
            console.log('TOKEEEN', token)
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
