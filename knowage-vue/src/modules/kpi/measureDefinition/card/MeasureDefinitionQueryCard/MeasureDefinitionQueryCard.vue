<template>
    <Card>
        <template #content>
            <div class="p-m-3">
                <span class="p-float-label">
                    <Dropdown id="dataSource" class="kn-material-input" v-model="selectedRule.dataSource" :options="datasourcesList" optionLabel="DATASOURCE_LABEL" @change="loadDataSourceStructure"> </Dropdown>
                    <label for="dataSourceLabel" class="kn-material-input-label">{{ $t('kpi.measureDefinition.dataSource') }}</label>
                </span>
            </div>
            <div v-if="selectedRule.dataSource">
                <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
                    <template #end>
                        <Button class="kn-button p-button-text p-button-rounded" @click="showPreview" :disabled="previewDisabled">{{ $t('kpi.measureDefinition.preview') }}</Button>
                    </template>
                </Toolbar>
                <VCodeMirror ref="codeMirror" v-model:value="code" :autoHeight="true" :options="options" @keyup="onKeyUp" />
            </div>
        </template>
    </Card>
    {{'TODO'}}
    {{options}}
    <MeasureDefinitionPreviewDialog v-if="preview" :currentRule="selectedRule" :placeholders="placeholders" :columns="columns" :propRows="rows" @close="$emit('closePreview')" @loadPreview="$emit('loadPreview')"></MeasureDefinitionPreviewDialog>
</template>

<script lang="ts">
import { defineComponent  } from 'vue'
import { iRule } from '../../MeasureDefinition'
import { AxiosResponse } from 'axios'
import VCodeMirror, { CodeMirror  } from 'codemirror-editor-vue3'
import queryCardDescriptor from './MeasureDefinitionQueryCardDescriptor.json'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import MeasureDefinitionPreviewDialog from './MeasureDefinitionPreviewDialog.vue'

import "codemirror/theme/dracula.css";

import 'codemirror/lib/codemirror.css'
import 'codemirror/theme/monokai.css'
import 'codemirror/theme/eclipse.css'
import 'codemirror/addon/hint/show-hint.css'
import 'codemirror/addon/hint/show-hint.js'
import 'codemirror/addon/hint/sql-hint.js'
import 'codemirror/addon/lint/lint.js'
import 'codemirror/addon/selection/mark-selection.js'
import 'codemirror/mode/htmlmixed/htmlmixed.js'
import 'codemirror/mode/javascript/javascript.js'
import 'codemirror/mode/python/python.js'
import 'codemirror/mode/xml/xml.js'
import 'codemirror/mode/sql/sql.js'
import 'codemirror/mode/groovy/groovy.js'
import 'codemirror/mode/clike/clike.js'
import 'codemirror/mode/mathematica/mathematica.js'


export default defineComponent({
    name: 'measure-definition-query-card',
    components: { Card, Dropdown, 
    VCodeMirror, 
    MeasureDefinitionPreviewDialog },
    props: { rule: { type: Object, required: true }, datasourcesList: { type: Array, required: true }, aliases: { type: Array }, placeholders: { type: Array }, columns: { type: Array }, rows: { type: Array }, codeInput: { type: String }, preview: { type: Boolean } },
    emits: ['touched', 'queryChanged', 'loadPreview', 'closePreview'],
    data() {
        return {
            queryCardDescriptor,
            selectedRule: {} as iRule,
            code: '',
            datasourceStructure: {},
            codeMirror: {} as any,
            hintList: [] as any,
            options: {
                mode: 'text/x-mysql',
                indentWithTabs: true,
                smartIndent: true,
                lineWrapping: true,
                matchBrackets: true,
                autofocus: true,
                theme: 'dracula',
                lineNumbers: true,
                extraKeys: {
                    'Ctrl-Space': this.keyAssistFunc
                } as any,
                hintOptions: { tables: this.datasourceStructure }
            },
            cursorPosition: null
        }
    },
    computed: {
        previewDisabled(): Boolean {
            return !this.code
        }
    },
    watch: {
        codeInput() {
            this.cursorPosition = this.codeMirror.getCursor()
            this.codeMirror.replaceRange(this.codeInput, this.cursorPosition)
            this.selectedRule.definition = this.code
            this.$emit('queryChanged')
        }
    },
    async mounted() {
        this.loadRule()
        await this.loadDataSourceStructure()
    },
    methods: {
        loadRule() {
            this.selectedRule = this.rule as iRule
            this.code = this.rule.definition ?? ''
        },
        async loadDataSourceStructure() {
            if (this.selectedRule.dataSource) {
                await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasources/structure/${this.selectedRule.dataSource.DATASOURCE_ID}`).then((response: AxiosResponse<any>) => (this.datasourceStructure = response.data))
            }
            this.$emit('touched')

            this.setupCodeMirror()

            if (this.codeMirror && this.codeMirror.options) {
                this.codeMirror.options.hintOptions = { tables: this.datasourceStructure }
            }
        },
        setupCodeMirror() {
            const interval = setInterval(() => {
                if (!this.$refs.codeMirror) return
                console.log("this.$refs.codeMirror: ", this.$refs.codeMirror)
               //  this.codeMirror =  (this.$refs.codeMirror as any).cminstance as any
                this.codeMirror = (this.$refs.codeMirror as any).cminstance as any
                console.log("CODE MIRROR: ", this.codeMirror)
                setTimeout(() => {
                    this.codeMirror.refresh()
                }, 0)
                clearInterval(interval)
            }, 200)

            CodeMirror.registerHelper('hint', 'alias', () => {
                const cur = this.codeMirror.getCursor()
                const tok = this.codeMirror.getTokenAt(cur)
                const start = tok.string.trim() == '' ? tok.start + 1 : tok.start
                const end = tok.end
                const hintList = [] as any
                for (const key in this.aliases) {
                    if (tok.string.trim() == '' || this.aliases[key].name.startsWith(tok.string)) {
                        hintList.push(this.aliases[key].name)
                    }
                }
                return { list: hintList, from: CodeMirror.Pos(cur.line, start), to: CodeMirror.Pos(cur.line, end) }
            })

            CodeMirror.registerHelper('hint', 'placeholder', () => {
                const cur = this.codeMirror.getCursor()
                const tok = this.codeMirror.getTokenAt(cur)
                const start = tok.start + 1
                const end = tok.end
                const str = tok.string.substring(1, tok.string.length)
                const hintList = [] as any
                for (const key in this.placeholders) {
                    if (str == '' || this.placeholders[key].name.toUpperCase().startsWith(str.toUpperCase())) {
                        hintList.push(this.placeholders[key].name)
                    }
                }
                if (this.placeholders?.length == 1) {
                    hintList.push(str)
                }

                return { list: hintList, from: CodeMirror.Pos(cur.line, start), to: CodeMirror.Pos(cur.line, end) }
            })
        },
        keyAssistFunc() {
            console.log("CODE MIRROR STATIC: ", CodeMirror)
            if (this.isAlias()) {
                CodeMirror.showHint(this.codeMirror, CodeMirror.hint.alias)
            } else if (this.isPlaceholder()) {
                CodeMirror.showHint(this.codeMirror, CodeMirror.hint.placeholder)
            } else {
                CodeMirror.showHint(this.codeMirror, CodeMirror.hint.autocomplete)
            }
        },
        isAlias() {
            const cursor = this.codeMirror.getCursor()
            let token = this.codeMirror.getTokenAt(cursor)

            if (token.string.trim() != '') {
                const tmpCursor = CodeMirror.Pos(cursor.line, token.start)
                tmpCursor.ch = token.start
                token = this.codeMirror.getTokenAt(tmpCursor)
            }

            const beforeCursor = CodeMirror.Pos(cursor.line, token.start)
            beforeCursor.ch = token.start
            const beforeToken = this.codeMirror.getTokenAt(beforeCursor)

            if (beforeToken.string.toLowerCase() == 'as') {
                const text = this.codeMirror.getDoc().getRange(CodeMirror.Pos(0, 0), beforeCursor)

                const patt = new RegExp(/^((.*\)\s*select)|(\s*select)) ((?!FROM).)* AS$/gi)
                if (!patt.test(text.replace(/\n/g, ' '))) {
                    return false
                } else {
                    return true
                }
            }
            return false
        },
        isPlaceholder() {
            const cursor = this.codeMirror.getCursor()
            const token = this.codeMirror.getTokenAt(cursor)
            if (token.string.startsWith('@')) {
                return true
            }
        },
        onKeyUp() {
            const cur = this.codeMirror.getCursor()
            const tok = this.codeMirror.getTokenAt(cur)
            if (tok.string == '@') {
                CodeMirror.showHint(this.codeMirror, CodeMirror.hint.placeholder)
            }
            this.selectedRule.definition = this.code
            this.$emit('queryChanged')
        },
        showPreview() {
            this.$emit('loadPreview')
        }
    }
})
</script>

<style lang="scss" scoped>
::v-deep(.p-toolbar-group-right) {
    height: 100%;
}

#dataSource {
    width: 100%;
}

.error-dialog {
    width: 60vw;
}
</style>
