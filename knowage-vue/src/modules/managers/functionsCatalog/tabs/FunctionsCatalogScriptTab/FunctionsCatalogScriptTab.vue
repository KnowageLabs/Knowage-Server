<template>
    <div class="p-field kn-flex p-m-2">
        <span>
            <label class="kn-material-input-label">{{ $t('common.language') }}</label>
            <Dropdown v-model="selectedFunction.language" class="kn-material-input" :options="functionsCatalogScriptTabDescriptor.languages" option-label="value" option-value="value" :disabled="readonly" />
        </span>
    </div>
    <div v-if="selectedFunction.language" class="p-mt-4">
        <div>
            <label class="kn-material-input-label">{{ $t('common.script') }}</label>
            <VCodeMirror ref="codeMirror" v-model:value="code" class="p-mt-2" :auto-height="true" :options="options" @keyup="onKeyUp" />
        </div>
    </div>
    <div v-if="selectedFunction.language && selectedFunction.family === 'offline'">
        <div class="p-mt-4">
            <label class="kn-material-input-label">{{ $t('managers.functionsCatalog.trainModel') }}</label>
            <VCodeMirror ref="codeMirror" v-model:value="trainModelCode" class="p-mt-2" :auto-height="true" :options="options" @keyup="onKeyUpTrainModel" />
        </div>
        <div class="p-mt-4">
            <label class="kn-material-input-label">{{ $t('managers.functionsCatalog.useModel') }}</label>
            <VCodeMirror ref="codeMirror" v-model:value="useModelCode" class="p-mt-2" :auto-height="true" :options="options" @keyup="onKeyUpUseModel" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunction } from '../../FunctionsCatalog'
// eslint-disable-next-line
import VCodeMirror, { CodeMirror } from 'codemirror-editor-vue3'
import Dropdown from 'primevue/dropdown'
import functionsCatalogScriptTabDescriptor from './FunctionsCatalogScriptTabDescriptor.json'

export default defineComponent({
    name: 'function-catalog-script-tab',
    components: { Dropdown, VCodeMirror },
    props: { propFunction: { type: Object }, readonly: { type: Boolean }, activeTab: { type: Number } },
    data() {
        return {
            functionsCatalogScriptTabDescriptor,
            selectedFunction: {} as iFunction,
            code: '',
            trainModelCode: '',
            useModelCode: '',
            codeMirror: {} as any,
            options: {
                mode: '',
                lineWrapping: true,
                theme: 'eclipse',
                lineNumbers: true,
                autoRefresh: true
            }
        }
    },
    watch: {
        activeTab(value: number) {
            if (value === 2 && this.codeMirror) setTimeout(() => this.codeMirror.refresh(), 100)
        }
    },
    created() {
        this.loadFunction()
        this.setupCodeMirror()
    },
    methods: {
        loadFunction() {
            this.selectedFunction = this.propFunction as iFunction

            if (this.selectedFunction) {
                this.code = this.selectedFunction.onlineScript ?? ''
                this.trainModelCode = this.selectedFunction.trainModelCode ?? ''
                this.useModelCode = this.selectedFunction.useModelCode ?? ''
                this.options.mode = this.selectedFunction.language.toLowerCase()
            }
        },
        setupCodeMirror() {
            const interval = setInterval(() => {
                if (!this.$refs.codeMirror) return
                this.codeMirror = (this.$refs.codeMirror as any).cminstance as any
                this.codeMirror.setOption('readOnly', this.readonly)
                setTimeout(() => {
                    this.codeMirror.refresh()
                }, 0)
                clearInterval(interval)
            }, 200)
        },
        onKeyUp() {
            this.selectedFunction.onlineScript = this.code
        },
        onKeyUpTrainModel() {
            this.selectedFunction.trainModelCode = this.trainModelCode
        },
        onKeyUpUseModel() {
            this.selectedFunction.useModelCode = this.useModelCode
        }
    }
})
</script>
