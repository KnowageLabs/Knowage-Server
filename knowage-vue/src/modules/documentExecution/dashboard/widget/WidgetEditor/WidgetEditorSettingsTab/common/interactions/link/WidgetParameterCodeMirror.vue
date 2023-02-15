<template>
    <div class="kn-width-full">
        <VCodeMirror ref="codeMirror" v-model:value="code" class="p-mt-2" :auto-height="true" :height="200" :options="options" @keyup="onKeyUp" />
    </div>
</template>

<script lang="ts">
import { IWidgetInteractionParameter } from '@/modules/documentExecution/dashboard/Dashboard'
import { defineComponent, PropType } from 'vue'
import VCodeMirror from 'codemirror-editor-vue3'

export default defineComponent({
    name: 'table-widget-link-parameters-list',
    components: { VCodeMirror },
    props: { propParameter: { type: Object as PropType<IWidgetInteractionParameter>, required: true }, visible: { type: Boolean } },
    emits: ['change'],
    data() {
        return {
            parameter: null as IWidgetInteractionParameter | null,
            codeMirror: {} as any,
            code: '',
            options: {
                theme: 'eclipse',
                lineWrapping: true,
                lineNumbers: true,
                autoRefresh: true,
                mode: 'javascript'
            }
        }
    },
    watch: {
        visible() {
            this.loadParameter()
            setTimeout(() => this.codeMirror.refresh(), 1000)
        },
        propParameter() {
            this.loadParameter()
            setTimeout(() => this.codeMirror.refresh(), 1000)
        }
    },
    created() {
        this.setupCodeMirror()
        this.loadParameter()
        setTimeout(() => this.codeMirror.refresh(), 1000)
    },
    methods: {
        loadParameter() {
            this.parameter = this.propParameter
            this.code = this.parameter?.json ?? ''
            this.setupCodeMirror()
            setTimeout(() => this.codeMirror.refresh(), 1000)
        },
        setupCodeMirror() {
            const interval = setInterval(() => {
                if (!this.$refs.codeMirror) return
                this.codeMirror = (this.$refs.codeMirror as any).cminstance as any
                setTimeout(() => {
                    this.codeMirror.refresh()
                }, 1000)
                clearInterval(interval)
            }, 200)
        },
        onKeyUp() {
            if (!this.parameter) return
            this.parameter.json = this.code
        }
    }
})
</script>
