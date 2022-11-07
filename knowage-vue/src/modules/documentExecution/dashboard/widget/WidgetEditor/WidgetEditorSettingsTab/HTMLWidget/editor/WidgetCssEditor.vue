<template>
    <!-- <Button icon="fas fa-terminal" class="p-button-text p-button-rounded p-button-plain" @click="logModel" /> -->
    <div class="cssMirrorContainer" style="height: 500px; width: 100%">
        <VCodeMirror ref="codeMirrorCssEditor" v-model:value="widgetModel.settings.editor.css" :options="scriptOptions" />
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import VCodeMirror from 'codemirror-editor-vue3'

export default defineComponent({
    name: 'widget-responsive',
    components: { VCodeMirror },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            codeMirrorCssEditor: null as any,
            scriptOptions: {
                cursor: true,
                line: false,
                lineNumbers: true,
                indentWithTabs: true,
                smartIndent: true,
                lineWrapping: true,
                matchBrackets: true,
                mode: 'css',
                tabSize: 4,
                theme: 'eclipse'
            }
        }
    },
    created() {
        this.setupCodeMirror()
    },
    methods: {
        setupCodeMirror() {
            const interval = setInterval(() => {
                if (!this.$refs.codeMirrorCssEditor) return
                this.codeMirrorCssEditor = (this.$refs.codeMirrorCssEditor as any).cminstance as any
                setTimeout(() => {
                    this.codeMirrorCssEditor.refresh()
                }, 0)
                clearInterval(interval)
            }, 200)
        },
        logModel() {
            console.log(this.widgetModel)
        }
    }
})
</script>
