<template>
    <!-- <Button icon="fas fa-terminal" class="p-button-text p-button-rounded p-button-plain" @click="logModel" /> -->
    <div class="htmlMirrorContainer" style="height: 500px; width: 100%">
        <VCodeMirror ref="codeMirrorHtmlEditor" v-model:value="widgetModel.settings.configuration.htmlToRender" :options="scriptOptions" />
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
            codeMirrorHtmlEditor: null as any,
            scriptOptions: {
                cursor: true,
                line: false,
                lineNumbers: true,
                indentWithTabs: true,
                smartIndent: true,
                lineWrapping: true,
                matchBrackets: true,
                mode: 'html',
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
                if (!this.$refs.codeMirrorHtmlEditor) return
                this.codeMirrorHtmlEditor = (this.$refs.codeMirrorHtmlEditor as any).cminstance as any
                setTimeout(() => {
                    this.codeMirrorHtmlEditor.refresh()
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
