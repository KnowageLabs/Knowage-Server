<template>
    <div class="kn-width-full">
        <VCodeMirror ref="codeMirrorEditor" v-model:value="code" :auto-height="true" :height="200" :options="options" :disabled="disabled" @keyup="onKeyUp" @keyDown="onKeyUp" @change="onKeyUp" @blur="$emit('blur')" />
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import VCodeMirror from 'codemirror-editor-vue3'

export default defineComponent({
    name: 'highcharts-formatter-code-mirror',
    components: { VCodeMirror },
    props: {
        propCode: { type: Object as PropType<string | undefined>, required: true },
        disabled: { type: Boolean }
    },
    emits: ['change', 'blur'],
    data() {
        return {
            codeMirrorEditor: null as any,
            code: '' as string,
            options: {
                mode: 'text/javascript',
                indentWithTabs: true,
                smartIndent: true,
                lineWrapping: true,
                matchBrackets: true,
                autofocus: true,
                theme: 'eclipse',
                lineNumbers: true,
                readOnly: false
            }
        }
    },
    watch: {
        propCode() {
            this.setupCodeMirror()
            setTimeout(() => this.codeMirrorEditor.refresh(), 100)
        },
        disabled() {
            this.setupCodeMirror()
            setTimeout(() => this.codeMirrorEditor.refresh(), 100)
        }
    },
    created() {
        this.setupCodeMirror()
    },
    methods: {
        setupCodeMirror() {
            const interval = setInterval(() => {
                if (!this.$refs.codeMirrorEditor) return
                this.code = this.propCode ?? ''
                this.options.readOnly = this.disabled
                this.codeMirrorEditor = (this.$refs.codeMirrorEditor as any).cminstance as any
                setTimeout(() => {
                    this.codeMirrorEditor.refresh()
                }, 0)
                clearInterval(interval)
            }, 200)
        },
        onKeyUp() {
            this.$emit('change', this.code)
        }
    }
})
</script>
