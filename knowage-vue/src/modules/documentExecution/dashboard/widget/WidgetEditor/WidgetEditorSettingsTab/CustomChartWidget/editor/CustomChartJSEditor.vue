<template>
    <div class="cssMirrorContainer" style="height: 500px; width: 100%">
        <VCodeMirror ref="codeMirrorJsEditor" v-model:value="code" :options="scriptOptions" @keyup="onKeyUp" @keyDown="onKeyUp" @change="onKeyUp" @blur="onKeyUp" />
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import VCodeMirror from 'codemirror-editor-vue3'

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
                theme: 'eclipse'
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
        },
        onKeyUp() {
            this.widgetModel.settings.editor.js = this.code
        }
    }
})
</script>
