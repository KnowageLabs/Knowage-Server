<template>
    <div class="kn-width-full">
        <VCodeMirror ref="codeMirror" class="p-mt-2" v-model:value="code" :autoHeight="true" :height="200" :options="options" @keyup="onKeyUp" />
    </div>
</template>

<script lang="ts">
import { ITableWidgetParameter } from '@/modules/documentExecution/dashboard/Dashboard'
import { defineComponent, PropType } from 'vue'
import VCodeMirror, { CodeMirror } from 'codemirror-editor-vue3'

export default defineComponent({
    name: 'table-widget-link-parameters-list',
    components: { VCodeMirror },
    props: { propParameter: { type: Object as PropType<ITableWidgetParameter>, required: true }, visible: { type: Boolean } },
    emits: ['change'],
    data() {
        return {
            parameter: null as ITableWidgetParameter | null,
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
