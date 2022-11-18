<template>
    <div class="cssMirrorContainer" style="height: 500px; width: 100%">
        <VCodeMirror ref="codeMirrorCssEditor" v-model:value="code" :options="scriptOptions" @keyup="onKeyUp" @keyDown="onKeyUp" @change="onKeyUp" @blur="onKeyUp" />
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import VCodeMirror from 'codemirror-editor-vue3'

export default defineComponent({
    name: 'widget-css-editor',
    components: { VCodeMirror },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, activeIndex: { type: Number, required: true } },
    data() {
        return {
            codeMirrorCssEditor: null as any,
            scriptOptions: {
                cursor: true,
                line: false,
                lineNumbers: true,
                mode: 'css',
                tabSize: 4,
                theme: 'eclipse'
            },
            code: ''
        }
    },
    watch: {
        activeIndex(value: number) {
            if (value === 0 && this.codeMirrorCssEditor) setTimeout(() => this.codeMirrorCssEditor.refresh(), 100)
        }
    },
    created() {
        this.setupCodeMirror()
    },
    methods: {
        setupCodeMirror() {
            const interval = setInterval(() => {
                if (!this.$refs.codeMirrorCssEditor) return
                this.code = this.widgetModel.settings.editor.css
                this.codeMirrorCssEditor = (this.$refs.codeMirrorCssEditor as any).cminstance as any
                setTimeout(() => {
                    this.codeMirrorCssEditor.refresh()
                }, 0)
                clearInterval(interval)
            }, 200)
        },
        onKeyUp() {
            this.widgetModel.settings.editor.css = this.code
        },
        logModel() {
            console.log(this.widgetModel)
        }
    }
})
</script>
