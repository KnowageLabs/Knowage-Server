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
                    <template #right>
                        <Button class="kn-button p-button-text p-button-rounded" @click="previewVisible = true">{{ $t('kpi.measureDefinition.preview') }}</Button>
                    </template>
                </Toolbar>
                <VCodeMirror ref="codeMirror" v-model:value="code" :autoHeight="true" :options="options" @keyup="onKeyUp" />
            </div>
        </template>
    </Card>
    <PreviewDialog v-if="previewVisible" :currentRule="selectedRule" :placeholders="placeholders" @close="previewVisible = false"></PreviewDialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iRule } from '../../MeasureDefinition'
import { VCodeMirror } from 'vue3-code-mirror'
import CodeMirror from 'codemirror'
import axios from 'axios'
import queryCardDescriptor from './QueryCardDescriptor.json'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import PreviewDialog from './PreviewDialog.vue'

export default defineComponent({
    name: 'query-card',
    components: { Card, Dropdown, VCodeMirror, PreviewDialog },
    props: { rule: { type: Object, required: true }, datasourcesList: { type: Array, required: true }, aliases: { type: Array }, placeholders: { type: Array } },
    emits: ['touched'],
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
                theme: 'eclipse',
                lineNumbers: true,
                extraKeys: {
                    'Ctrl-Space': this.keyAssistFunc
                } as any,
                hintOptions: { tables: this.datasourceStructure }
            },
            previewVisible: false
        }
    },
    async mounted() {
        this.loadRule()
        console.log('QueryCard Selected Rule: ', this.selectedRule)
        console.log('QueryCard Datsources: ', this.datasourcesList)
        console.log('QueryCard Options: ', this.options)
        console.log('MOUNTED', this.$refs.codeMirror)
        await this.loadDataSourceStructure()
    },
    methods: {
        loadRule() {
            this.selectedRule = this.rule as iRule
            this.code = this.rule.definition ?? ''
        },
        async loadDataSourceStructure() {
            // console.log('SELECTED DATASOURCE', this.selectedRule.dataSource)
            if (this.selectedRule.dataSource) {
                await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/datasources/structure/${this.selectedRule.dataSource.DATASOURCE_ID}`).then((response) => (this.datasourceStructure = response.data))
            }
            this.$emit('touched')
            console.log('DATASOURCE STRUCTURE: ', this.datasourceStructure)
            // console.log('ALISASES: ', this.aliases)
            console.log('PLACEHOLDERS: ', this.placeholders)

            this.setupCodeMirror()

            console.log('HINT LIST: ', this.hintList)
            this.codeMirror.options.hintOptions = { tables: this.datasourceStructure }
            // this.codeMirror.options.hintOptions = { test: { radi: null } }
            console.log('CODE MIRROR OPTIONS', this.codeMirror.options.hintOptions)
            console.log('TEEEEEEEEEEEST', this.codeMirror)
        },
        setupCodeMirror() {
            this.codeMirror = (this.$refs.codeMirror as any).editor as any
            //  console.log('QueryCard EDITOR: ', CodeMirror)
            CodeMirror.registerHelper('hint', 'alias', () => {
                var cur = this.codeMirror.getCursor()
                var tok = this.codeMirror.getTokenAt(cur)
                var start = tok.string.trim() == '' ? tok.start + 1 : tok.start
                var end = tok.end
                var hintList = [] as any
                for (var key in this.aliases) {
                    if (tok.string.trim() == '' || this.aliases[key].name.startsWith(tok.string)) {
                        hintList.push(this.aliases[key].name)
                    }
                }
                return { list: hintList, from: CodeMirror.Pos(cur.line, start), to: CodeMirror.Pos(cur.line, end) }
            })

            CodeMirror.registerHelper('hint', 'placeholder', () => {
                var cur = this.codeMirror.getCursor()
                var tok = this.codeMirror.getTokenAt(cur)
                var start = tok.start + 1
                var end = tok.end
                var str = tok.string.substring(1, tok.string.length)
                var hintList = [] as any
                for (var key in this.placeholders) {
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
            console.log('RADI')
            if (this.isAlias()) {
                console.log('isAlias called!!!!!!!!!!')
                CodeMirror.showHint(this.codeMirror, CodeMirror.hint.alias)
            } else if (this.isPlaceholder()) {
                console.log('isPlaceholder called!!!!!!!!!!')
                CodeMirror.showHint(this.codeMirror, CodeMirror.hint.placeholder)
            } else {
                CodeMirror.showHint(this.codeMirror, CodeMirror.hint.autocomplete)
            }
        },
        isAlias() {
            console.log('CODE MIRROR', this.codeMirror)
            var cursor = this.codeMirror.getCursor()
            var token = this.codeMirror.getTokenAt(cursor)

            if (token.string.trim() != '') {
                var tmpCursor = CodeMirror.Pos(cursor.line, token.start)
                tmpCursor.ch = token.start
                token = this.codeMirror.getTokenAt(tmpCursor)
            }

            var beforeCursor = CodeMirror.Pos(cursor.line, token.start)
            beforeCursor.ch = token.start
            var beforeToken = this.codeMirror.getTokenAt(beforeCursor)

            if (beforeToken.string.toLowerCase() == 'as') {
                var text = this.codeMirror.getDoc().getRange(CodeMirror.Pos(0, 0), beforeCursor)

                var patt = new RegExp(/^((.*\)\s*select)|(\s*select)) ((?!FROM).)* AS$/gi)
                if (!patt.test(text.replace(/\n/g, ' '))) {
                    return false
                } else {
                    return true
                }
            }
            return false
        },
        isPlaceholder() {
            var cursor = this.codeMirror.getCursor()
            var token = this.codeMirror.getTokenAt(cursor)
            if (token.string.startsWith('@')) {
                return true
            }
        },
        onKeyUp() {
            var cur = this.codeMirror.getCursor()
            var tok = this.codeMirror.getTokenAt(cur)
            if (tok.string == '@') {
                CodeMirror.showHint(this.codeMirror, CodeMirror.hint.placeholder)
            }
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
</style>
