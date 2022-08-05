<template>
    <div class="p-fluid p-m-4">
        <div>
            <span>
                <label for="language" class="kn-material-input-label" aria-label="dropdown">{{ $t('managers.lovsManagement.language') }} * </label>
                <Dropdown
                    id="language"
                    class="kn-material-input"
                    :class="{
                        'p-invalid': !script.language && dirty
                    }"
                    v-model="script.language"
                    :options="listOfScriptTypes"
                    optionLabel="VALUE_NM"
                    optionValue="VALUE_CD"
                    :placeholder="$t('managers.lovsManagement.placeholderScript')"
                    aria-label="dropdown"
                    @before-show="dirty = true"
                    @change="onLanguageChanged($event.value)"
                />
            </span>
            <div v-if="!script.language && dirty" class="p-error p-grid">
                <small class="p-col-12">
                    {{
                        $t('common.validation.required', {
                            fieldName: $t('managers.lovsManagement.language')
                        })
                    }}
                </small>
            </div>
        </div>
        <div v-if="script.language" class="p-mt-4">
            <label class="kn-material-input-label">{{ $t('managers.lovsManagement.script') }}</label>
            <VCodeMirror ref="codeMirror" class="p-mt-2" v-model:value="code" :autoHeight="true" :options="options" @keyup="onKeyUp" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iLov } from '../../../LovsManagement'
import VCodeMirror, { CodeMirror  } from 'codemirror-editor-vue3'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'lovs-management-script',
    components: { Dropdown, VCodeMirror },
    props: {
        selectedLov: { type: Object, required: true },
        selectedScript: { type: Object, required: true },
        listOfScriptTypes: { type: Array }
    },
    emits: ['touched'],
    data() {
        return {
            lov: {} as iLov,
            script: {} as any,
            code: '',
            dirty: false,
            codeMirror: {} as any,
            options: {
                mode: '',
                indentWithTabs: true,
                smartIndent: true,
                lineWrapping: true,
                matchBrackets: true,
                autofocus: true,
                theme: 'eclipse',
                lineNumbers: true
            }
        }
    },
    watch: {
        selectedLov() {
            this.loadLov()
            this.loadSelectedScript()
        }
    },
    computed: {
        lovType(): string {
            return this.selectedLov.itypeCd
        }
    },
    created() {
        this.loadLov()
        this.loadSelectedScript()
        this.setupCodeMirror()
    },
    methods: {
        loadLov() {
            this.lov = this.selectedLov as iLov
        },
        loadSelectedScript() {
            this.script = this.selectedScript as any

            if (this.script) {
                this.code = this.script.text ?? ''
            }
            this.options.mode = this.script.type === 'ECMAScript' ? 'text/javascript' : 'text/x-groovy'
        },
        onKeyUp() {
            this.$emit('touched')
            this.script.text = this.code
        },
        onLanguageChanged(value: string) {
            const mode = value === 'ECMAScript' ? 'text/javascript' : 'text/x-groovy'
            setTimeout(() => {
                this.setupCodeMirror()
                this.codeMirror.setOption('mode', mode)
            }, 250)
            this.$emit('touched')
        },
        setupCodeMirror() {
            if (this.$refs.codeMirror) {
                this.codeMirror = (this.$refs.codeMirror as any).cminstance as any
            }
        }
    }
})
</script>
