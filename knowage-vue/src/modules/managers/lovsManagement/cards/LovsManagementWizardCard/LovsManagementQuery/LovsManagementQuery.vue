<template>
    <div class="p-fluid p-m-4">
        <div>
            <span>
                <label for="dataSource" class="kn-material-input-label" aria-label="dropdown">{{ $t('managers.lovsManagement.dataSource') }} * </label>
                <Dropdown
                    id="dataSource"
                    v-model="query.datasource"
                    class="kn-material-input"
                    :class="{
                        'p-invalid': !query.datasource && dirty
                    }"
                    :options="datasources"
                    option-label="label"
                    option-value="label"
                    :placeholder="$t('managers.lovsManagement.placeholderDatasource')"
                    aria-label="dropdown"
                    @before-show="dirty = true"
                    @change="$emit('touched')"
                />
            </span>
            <div v-if="!query.datasource && dirty" class="p-error p-grid">
                <small class="p-col-12">
                    {{
                        $t('common.validation.required', {
                            fieldName: $t('managers.lovsManagement.dataSource')
                        })
                    }}
                </small>
            </div>
        </div>
        <div v-if="query.datasource" class="p-mt-4">
            <label class="kn-material-input-label">{{ $t('managers.lovsManagement.queryDefinition') }}</label>
            <VCodeMirror ref="codeMirror" v-model:value="code" class="p-mt-2" :auto-height="true" :options="options" @keyup="onKeyUp" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iLov } from '../../../LovsManagement'
// eslint-disable-next-line
import VCodeMirror, { CodeMirror } from 'codemirror-editor-vue3'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'lovs-management-query',
    components: { Dropdown, VCodeMirror },
    props: {
        selectedLov: { type: Object, required: true },
        selectedQuery: { type: Object },
        datasources: { type: Array },
        codeInput: { type: Object }
    },
    emits: ['touched'],
    data() {
        return {
            lov: {} as iLov,
            query: {} as any,
            code: '',
            dirty: false,
            codeMirror: {} as any,
            options: {
                mode: 'text/x-mysql',
                indentWithTabs: true,
                smartIndent: true,
                lineWrapping: true,
                matchBrackets: true,
                autofocus: true,
                theme: 'eclipse',
                lineNumbers: true
            },
            cursorPosition: null
        }
    },
    computed: {
        lovType(): string {
            return this.selectedLov.itypeCd
        }
    },
    watch: {
        selectedLov() {
            this.loadLov()
            this.loadSelectedQuery()
            this.setupCodeMirror()
        },
        codeInput() {
            this.setupCodeMirror()
            this.cursorPosition = this.codeMirror.getCursor()
            this.codeMirror.replaceRange('${' + this.codeInput?.code + '}', this.cursorPosition)
        }
    },
    async created() {
        this.loadLov()
        this.loadSelectedQuery()
        this.setupCodeMirror()
    },
    methods: {
        loadLov() {
            this.lov = this.selectedLov as iLov
        },
        loadSelectedQuery() {
            this.query = this.selectedQuery as any
            if (this.query) {
                this.code = this.query.query ?? ''
            }
        },
        setupCodeMirror() {
            if (this.$refs.codeMirror) {
                this.codeMirror = (this.$refs.codeMirror as any).cminstance as any
            }
        },
        onKeyUp() {
            this.query.query = this.code
            this.$emit('touched')
        }
    }
})
</script>
