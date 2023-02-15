<template>
    <form class="p-fluid p-formgrid p-grid p-mt-3">
        <div class="p-field p-col-6">
            <span class="p-float-label p-mb-2">
                <InputText
                    id="name"
                    v-model.trim="v$.selectedKpi.name.$model"
                    class="kn-material-input"
                    type="text"
                    max-length="25"
                    :class="{
                        'p-invalid': v$.selectedKpi.name.$invalid && v$.selectedKpi.name.$dirty
                    }"
                    @blur="v$.selectedKpi.name.$touch()"
                />
                <label for="label" class="kn-material-input-label">{{ $t('common.name') }} * </label>
            </span>
            <KnValidationMessages
                :v-comp="v$.selectedKpi.name"
                :additional-translate-params="{
                    fieldName: $t('common.name')
                }"
            >
            </KnValidationMessages>
        </div>
        <div class="p-field p-col-6">
            <span class="p-float-label p-mb-2">
                <InputText id="name" v-model.trim="selectedKpi.author" class="kn-material-input" type="text" :disabled="true" />
                <label for="name" class="kn-material-input-label"> {{ $t('common.author') }}</label>
            </span>
        </div>
    </form>
    <VCodeMirror v-if="!loading" ref="codeMirror" v-model:value="selectedKpi.definition.formula" class="CodeMirrorMathematica" :auto-height="true" :options="codeMirrorOptions" @keyup="onKeyUp" @mousedown="onMouseDown" />
    <Dialog class="kn-dialog--toolbar--primary importExportDialog" footer="footer" :visible="functionDialogVisible" :closable="false" modal>
        <template #header>
            <h4>{{ $t('kpi.kpiDefinition.formulaDialogHeader') }} {{ dialogHeaderInfo.functionName }}</h4>
        </template>

        <div class="p-mt-4 p-ml-4">
            <div class="p-field-radiobutton">
                <RadioButton id="SUM" v-model="selectedFunctionalities" name="city" value="SUM" />
                <label for="SUM">SUM</label>
            </div>
            <div class="p-field-radiobutton">
                <RadioButton id="MAX" v-model="selectedFunctionalities" name="city" value="MAX" />
                <label for="MAX">MAX</label>
            </div>
            <div class="p-field-radiobutton">
                <RadioButton id="MIN" v-model="selectedFunctionalities" name="city" value="MIN" />
                <label for="MIN">MIN</label>
            </div>
            <div class="p-field-radiobutton">
                <RadioButton id="COUNT" v-model="selectedFunctionalities" name="city" value="COUNT" />
                <label for="COUNT">COUNT</label>
            </div>
        </div>
        <template #footer>
            <div>
                <Button class="kn-button kn-button--secondary" :label="$t('common.apply')" @click="openFunctionPicker" />
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import VCodeMirror, { CodeMirror } from 'codemirror-editor-vue3'
import { setMathematicaModified } from '@/helpers/commons/codeMirrorMathematicaModifiedHelper'
import { createValidations } from '@/helpers/commons/validationHelper'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import useValidate from '@vuelidate/core'
import tabViewDescriptor from '../KpiDefinitionDetailDescriptor.json'
import Dialog from 'primevue/dialog'
import RadioButton from 'primevue/radiobutton'
import mainStore from '../../../../../App.store'

export default defineComponent({
    components: { VCodeMirror, Dialog, RadioButton, KnValidationMessages },
    props: { propKpi: Object as any, measures: { type: Array as any }, aliasToInput: { type: String }, checkFormula: { type: Boolean }, activeTab: { type: Number }, loading: Boolean, reloadKpi: Boolean },
    emits: ['touched', 'errorInFormula', 'updateFormulaToSave', 'onGuideClose'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            codeMirrorOptions: {
                mode: 'text/x-mathematica',
                indentWithTabs: true,
                smartIndent: true,
                lineWrapping: true,
                matchBrackets: true,
                autofocus: true,
                theme: 'eclipse',
                lineNumbers: true,
                gutters: ['CodeMirror-lint-markers'],
                lint: true,
                extraKeys: {
                    'Ctrl-Space': this.keyAssistFunc
                } as any
            },
            v$: useValidate() as any,
            tabViewDescriptor,
            selectedKpi: {} as any,
            codeMirror: {} as any,
            previousTabIndex: 0 as any,
            dialogHeaderInfo: {} as any,
            measuresToJSON: [] as any,
            functionsTOJSON: [] as any,
            formula: '',
            formulaDecoded: '',
            formulaSimple: '',
            token: '',
            selectedFunctionalities: 'SUM',
            functionDialogVisible: false,
            cursorPosition: null
        }
    },
    validations() {
        return {
            selectedKpi: createValidations('selectedKpi', tabViewDescriptor.validations.selectedKpi)
        }
    },
    watch: {
        propKpi() {
            this.selectedKpi = this.propKpi as any
            if (this.selectedKpi.definition != '') {
                this.selectedKpi.definition = JSON.parse(this.selectedKpi.definition)
            }
            this.loadKPI()
        },
        aliasToInput() {
            this.cursorPosition = this.codeMirror.getCursor()
            this.codeMirror.replaceRange(' ' + this.aliasToInput, this.cursorPosition)
            this.$emit('touched')
        },
        activeTab() {
            setTimeout(() => {
                this.codeMirror.refresh()
            }, 0)
            if (this.previousTabIndex === 0 && this.activeTab != 0) {
                this.checkFormulaForErrors()
            }
            this.previousTabIndex = this.activeTab
        },
        reloadKpi() {
            if (this.reloadKpi === true) {
                this.loadKPI()
            }
        }
    },
    created() {
        setMathematicaModified()
    },
    mounted() {
        if (this.propKpi) {
            this.selectedKpi = this.propKpi as any
        }
        this.registerCodeMirrorHelper()
        this.loadKPI()
    },

    methods: {
        openFunctionPicker() {
            const cur = this.codeMirror.getCursor()
            let token = this.codeMirror.getTokenAt(cur)

            while (token.string.trim() == '') {
                cur.ch = cur.ch + 1
                token = this.codeMirror.getTokenAt(cur)
            }

            while (token.type == 'operator' || token.type == 'bracket' || token.type == 'number') {
                cur.ch = cur.ch + 1
                token = this.codeMirror.getTokenAt(cur)
            }

            while (token.string.trim() == '') {
                cur.ch = cur.ch + 1
                token = this.codeMirror.getTokenAt(cur)
            }

            if (this.selectedFunctionalities != '') {
                const arr = this.codeMirror.findMarksAt({ line: this.codeMirror.getCursor().line, ch: token.end })
                for (let i = 0; i < arr.length; i++) {
                    arr[i].clear()
                }
            }
            if (this.selectedFunctionalities == 'MAX') {
                this.codeMirror.markText({ line: this.codeMirror.getCursor().line, ch: token.start }, { line: this.codeMirror.getCursor().line, ch: token.end }, { className: 'cm-m-max', atomic: true })
            } else if (this.selectedFunctionalities == 'MIN') {
                this.codeMirror.markText({ line: this.codeMirror.getCursor().line, ch: token.start }, { line: this.codeMirror.getCursor().line, ch: token.end }, { className: 'cm-m-min', atomic: true })
            } else if (this.selectedFunctionalities == 'COUNT') {
                this.codeMirror.markText({ line: this.codeMirror.getCursor().line, ch: token.start }, { line: this.codeMirror.getCursor().line, ch: token.end }, { className: 'cm-m-count', atomic: true })
            } else if (this.selectedFunctionalities == 'SUM') {
                this.codeMirror.markText({ line: this.codeMirror.getCursor().line, ch: token.start }, { line: this.codeMirror.getCursor().line, ch: token.end }, { className: 'cm-m-sum', atomic: true })
            }
            this.functionDialogVisible = false
            this.checkError(this.codeMirror, token)
        },

        checkError(cm, token) {
            let flag = false

            if (this.measureInList(token.string, this.measures) == -1) {
                flag = true
            }
            if (flag) cm.markText({ line: cm.getCursor().line, ch: token.start }, { line: cm.getCursor().line, ch: token.end }, { className: 'error_word' })

            document.querySelectorAll('.CodeMirrorMathematica .CodeMirror-code span.error_word ').forEach((element) => element.setAttribute('target', 'Measure Missing'))
        },

        registerCodeMirrorHelper() {
            CodeMirror.registerHelper('hint', 'measures', () => {
                const cur = this.codeMirror.getCursor()
                const tok = this.codeMirror.getTokenAt(cur)
                const start = tok.string.trim() == '' ? tok.start + 1 : tok.start
                const end = tok.end

                const hint = [] as any

                for (let i = 0; i < this.measures.length; i++) {
                    if (tok.string.trim() == '' || this.measures[i].alias.startsWith(tok.string)) {
                        hint.push(this.measures[i].alias)
                    }
                }
                return { list: hint, from: CodeMirror.Pos(cur.line, start), to: CodeMirror.Pos(cur.line, end) }
            })
        },

        onKeyUp(event) {
            const cm = this.codeMirror
            this.$emit('touched')

            if ((event.keyIdentifier != undefined && event.keyIdentifier != 'U+0008' && event.keyIdentifier != 'Left' && event.keyIdentifier != 'Right') || (event.key != undefined && event.key != 'Backspace' && event.key != 'Left' && event.key != 'Right')) {
                const cur = cm.getCursor()
                const token = cm.getTokenAt(cur)

                if (token.string == '{' || token.string == '}' || token.string == '[' || token.string == ']') {
                    cm.replaceRange('', { line: cm.getCursor().line, ch: token.start }, { line: cm.getCursor().line, ch: token.end + 1 })
                } else if ((token.type == 'operator' || token.type == 'bracket') && token.string != '_') {
                    token.string = ' '
                    cm.replaceRange(token.string, { line: cm.getCursor().line, ch: token.end })
                    cm.replaceRange(' ', { line: cm.getCursor().line, ch: token.start })
                }
            }
        },

        onMouseDown(event) {
            if ('srcElement' in event) {
                for (let i = 0; i < event.srcElement.classList.length; i++) {
                    this.token = event.srcElement.innerHTML
                    if (event.srcElement.classList[i] == 'cm-m-max') {
                        this.selectedFunctionalities = 'MAX'
                        break
                    } else if (event.srcElement.classList[i] == 'cm-m-min') {
                        this.selectedFunctionalities = 'MIN'
                        break
                    } else if (event.srcElement.classList[i] == 'cm-m-count') {
                        this.selectedFunctionalities = 'COUNT'
                        break
                    } else if (event.srcElement.classList[i] == 'cm-m-sum') {
                        this.selectedFunctionalities = 'SUM'
                        break
                    }
                }
                const className = event.srcElement.className
                if (className.startsWith('cm-keyword') || className.startsWith('cm-variable-2')) {
                    this.dialogHeaderInfo.functionName = event.srcElement.innerHTML
                    this.functionDialogVisible = true
                }
            }
        },

        keyAssistFunc() {
            CodeMirror.showHint(this.codeMirror, CodeMirror.hint.measures)
        },

        loadKPI() {
            const interval = setInterval(() => {
                if (!this.$refs.codeMirror) return
                this.codeMirror = (this.$refs.codeMirror as any).cminstance as any
                setTimeout(() => {
                    this.codeMirror.refresh()
                }, 0)
                this.codeMirror.setValue('')
                this.codeMirror.clearHistory()
                this.codeMirror.setValue(this.selectedKpi.definition.formulaSimple)

                this.changeIndexWithMeasures(this.selectedKpi.definition.functions, this.codeMirror)
                clearInterval(interval)
            }, 200)
        },
        changeIndexWithMeasures(functions, codeMirror) {
            let counter = 0
            for (let i = 0; i < codeMirror.lineCount(); i++) {
                const arrayOfLines = this.removeSpace(codeMirror.getLineTokens(i))
                for (let j = 0; j < arrayOfLines.length; j++) {
                    const token = arrayOfLines[j]
                    if (token.type == 'keyword' || token.type == 'variable-2') {
                        const className = functions[counter]
                        counter++
                        if (className == 'MAX') {
                            codeMirror.markText({ line: i, ch: token.start }, { line: i, ch: token.end }, { className: 'cm-m-max' })
                        } else if (className == 'MIN') {
                            codeMirror.markText({ line: i, ch: token.start }, { line: i, ch: token.end }, { className: 'cm-m-min' })
                        } else if (className == 'SUM') {
                            codeMirror.markText({ line: i, ch: token.start }, { line: i, ch: token.end }, { className: 'cm-m-sum' })
                        } else if (className == 'COUNT') {
                            codeMirror.markText({ line: i, ch: token.start }, { line: i, ch: token.end }, { className: 'cm-m-count' })
                        }
                    }
                }
            }
        },

        removeSpace(tokenList) {
            for (let i = 0; i < tokenList.length; i++) {
                if (tokenList[i].type == null) {
                    tokenList.splice(i, 1)
                }
            }
            return tokenList
        },
        reset() {
            this.measuresToJSON = []
            this.functionsTOJSON = []
            this.formula = ''
            this.formulaDecoded = ''
            this.formulaSimple = ''
        },
        measureInList(item, list) {
            for (let i = 0; i < list.length; i++) {
                const object = list[i]
                if (object.alias == item) {
                    return i
                }
            }

            return -1
        },

        checkFormulaForErrors() {
            this.reset()
            let countOpenBracket = 0
            let countCloseBracket = 0
            const codeMirror = (this.$refs.codeMirror as any).cminstance as any
            let flag = true
            let numMeasures = 0

            FORFirst: for (let i = 0; i < codeMirror.lineCount(); i++) {
                const line = i + 1
                const array = this.removeSpace(codeMirror.getLineTokens(i))
                for (let j = 0; j < array.length; j++) {
                    const token = array[j]
                    const arr = codeMirror.findMarksAt({ line: i, ch: token.end })
                    if (token.string.trim() != '') {
                        if (arr.length == 0) {
                            if (j - 1 >= 0) {
                                const token_before = array[j - 1]
                                if (token_before.type == 'keyword' || token_before.type == 'variable-2') {
                                    if (token.type == 'keyword' || token.type == 'number' || token.type == 'variable-2' || token.string == '(') {
                                        this.store.setError({ msg: this.$t('kpi.kpiDefinition.errorformula.missingoperator') + line })
                                        this.$emit('errorInFormula', true)
                                        this.reset()
                                        flag = false
                                        break FORFirst
                                    }
                                }
                                if (token_before.type == 'operator') {
                                    if (token.type == 'operator' || token.string == ')') {
                                        this.store.setError({ msg: this.$t('kpi.kpiDefinition.errorformula.malformed') + line })
                                        this.$emit('errorInFormula', true)
                                        this.reset()
                                        flag = false
                                        break FORFirst
                                    }
                                }
                                if (token_before.type == 'number') {
                                    if (token.type == 'number' || token.string == '(' || token.type == 'keyword' || token.type == 'variable-2') {
                                        this.store.setError({ msg: this.$t('kpi.kpiDefinition.errorformula.malformed') + line })
                                        this.$emit('errorInFormula', true)
                                        this.reset()
                                        flag = false
                                        break FORFirst
                                    }
                                }
                                if (token_before.type == 'bracket') {
                                    if ((token.string == ')' && token_before.string == '(') || (token.string == '(' && token_before.string == ')')) {
                                        this.store.setError({ msg: this.$t('kpi.kpiDefinition.errorformula.malformed') + line })
                                        this.$emit('errorInFormula', true)
                                        flag = false
                                        break FORFirst
                                    }
                                    if (token_before.string == ')') {
                                        if (token.type == 'keyword' || token.type == 'number' || token.type == 'variable-2') {
                                            this.store.setError({ msg: this.$t('kpi.kpiDefinition.errorformula.missingoperator') })
                                            this.$emit('errorInFormula', true)
                                            this.reset()
                                            flag = false
                                            break FORFirst
                                        }
                                    }
                                }
                                if (token_before.string == '(') {
                                    if (token.type == 'operator') {
                                        this.store.setError({ msg: this.$t('kpi.kpiDefinition.errorformula.malformed') + line })
                                        this.$emit('errorInFormula', true)
                                        this.reset()
                                        flag = false
                                        break FORFirst
                                    }
                                }
                            }
                            if (j == array.length - 1) {
                                if (token.type == 'operator') {
                                    this.store.setError({ msg: this.$t('kpi.kpiDefinition.errorformula.malformed') + line })
                                    this.$emit('errorInFormula', true)
                                    this.reset()
                                    flag = false
                                    break FORFirst
                                }
                            }
                            if (token.type == 'operator') {
                                //operator
                                if (j == 0) {
                                    this.store.setError({ msg: this.$t('kpi.kpiDefinition.errorformula.malformed') + line })
                                    this.$emit('errorInFormula', true)
                                    this.reset()
                                    flag = false
                                    break FORFirst
                                } else {
                                    this.formula = this.formula + token.string
                                    this.formulaDecoded = this.formulaDecoded + token.string
                                    this.formulaSimple = this.formulaSimple + ' ' + token.string + ' '
                                }
                            } else if (token.type == 'bracket') {
                                //bracket
                                if (token.string == '(') {
                                    countOpenBracket++
                                } else {
                                    countCloseBracket++
                                }
                                this.formula = this.formula + token.string
                                this.formulaDecoded = this.formulaDecoded + token.string
                                this.formulaSimple = this.formulaSimple + ' ' + token.string + ' '
                            } else if (token.type == 'number') {
                                this.formula = this.formula + token.string
                                this.formulaDecoded = this.formulaDecoded + token.string
                                this.formulaSimple = this.formulaSimple + token.string
                            } else {
                                //error no function associated
                                this.store.setError({ msg: this.$t('kpi.kpiDefinition.errorformula.missingfunctions') })
                                this.$emit('errorInFormula', true)
                                this.reset()
                                flag = false
                                break FORFirst
                            }
                        } else {
                            if (j - 1 >= 0) {
                                const token_before = array[j - 1]
                                if (token_before.type == 'number' || token_before.type == 'keyword' || token_before.type == 'variable-2') {
                                    this.store.setError({ msg: this.$t('kpi.kpiDefinition.errorformula.missingoperator') })
                                    this.$emit('errorInFormula', true)
                                    this.reset()
                                    flag = false
                                    break FORFirst
                                }
                            }
                            //parse classes token
                            for (let k = 0; k < arr.length; k++) {
                                const className = arr[k]['className']
                                if (this.measureInList(token.string, this.measures) == -1) {
                                    this.store.setError({ msg: this.$t('kpi.kpiDefinition.errorformula.generic') })
                                    this.$emit('errorInFormula', true)
                                    this.reset()
                                    flag = false
                                }
                                if (className == 'cm-m-max') {
                                    numMeasures++
                                    this.measuresToJSON.push(token.string)
                                    this.functionsTOJSON.push('MAX')
                                    const index = this.measuresToJSON.length - 1
                                    const string = 'M' + index
                                    this.formula = this.formula + string
                                    this.formulaDecoded = this.formulaDecoded + 'MAX(' + token.string + ')'
                                    this.formulaSimple = this.formulaSimple + token.string
                                } else if (className == 'cm-m-min') {
                                    numMeasures++
                                    this.measuresToJSON.push(token.string)
                                    this.functionsTOJSON.push('MIN')
                                    const index = this.measuresToJSON.length - 1
                                    const string = 'M' + index
                                    this.formula = this.formula + string
                                    this.formulaDecoded = this.formulaDecoded + 'MIN(' + token.string + ')'
                                    this.formulaSimple = this.formulaSimple + token.string
                                } else if (className == 'cm-m-count') {
                                    numMeasures++
                                    this.measuresToJSON.push(token.string)
                                    this.functionsTOJSON.push('COUNT')
                                    const index = this.measuresToJSON.length - 1
                                    const string = 'M' + index
                                    this.formula = this.formula + string
                                    this.formulaDecoded = this.formulaDecoded + 'COUNT(' + token.string + ')'
                                    this.formulaSimple = this.formulaSimple + token.string
                                } else if (className == 'cm-m-sum') {
                                    numMeasures++
                                    this.measuresToJSON.push(token.string)
                                    this.functionsTOJSON.push('SUM')
                                    const index = this.measuresToJSON.length - 1
                                    const string = 'M' + index
                                    this.formula = this.formula + string
                                    this.formulaDecoded = this.formulaDecoded + 'SUM(' + token.string + ')'
                                    this.formulaSimple = this.formulaSimple + token.string
                                } else if (className == 'error_word') {
                                    this.store.setError({ msg: this.$t('kpi.kpiDefinition.errorformula.generic') })
                                    this.$emit('errorInFormula', true)
                                    this.reset()
                                    flag = false
                                    break FORFirst
                                }
                            }
                        }
                    }
                }
            }
            if (flag) this.$emit('errorInFormula', false)
            if (countOpenBracket != countCloseBracket && flag) {
                this.store.setError({ msg: this.$t('kpi.kpiDefinition.errorformula.missingbracket') })
                this.$emit('errorInFormula', true)
                this.reset()
            } else {
                if (numMeasures == 0 && flag) {
                    this.store.setError({ msg: this.$t('kpi.kpiDefinition.errorformula.missingmeasure') })
                    this.$emit('errorInFormula', true)
                    this.reset()
                }
                if (this.formula != '' && flag) {
                    this.selectedKpi.definition['formula'] = this.formula
                    this.selectedKpi.definition['measures'] = this.measuresToJSON
                    this.selectedKpi.definition['functions'] = this.functionsTOJSON
                    this.selectedKpi.definition['formulaDecoded'] = this.formulaDecoded
                    this.selectedKpi.definition['formulaSimple'] = this.formulaSimple
                    this.$emit('updateFormulaToSave', this.formula)
                    this.loadKPI()
                    return this.selectedKpi.definition
                }
            }
            return {}
        }
    }
})
</script>
