<template>
    <VCodeMirror v-if="!loading" ref="codeMirror" v-model:value="kpi.definition.formula" :autoHeight="true" :options="codeMirrorOptions" @keyup="onKeyUp" @mousedown="onMouseDown" />
    <Button label="print html" class="p-button-link" @click="loadKPI" />
    <Button label="parse formula" class="p-button-link" @click="parseFormula" />
    <!-- <div class="p-mt-6">{{ kpi.definition }}</div> -->
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { VCodeMirror } from 'vue3-code-mirror'
import CodeMirror from 'codemirror'

export default defineComponent({
    components: { VCodeMirror },
    props: {
        selectedKpi: Object as any,
        measures: { type: Array as any },
        loading: Boolean
    },
    computed: {},
    data() {
        return {
            kpi: {} as any,
            text: '',
            codeMirror: {} as any,
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
            measuresToJSON: [] as any,
            functionsTOJSON: [] as any,
            formula: '',
            formulaDecoded: '',
            formulaSimple: '',
            token: '',
            selectedFunctionalities: 'SUM',
            formulaForDB: {} as any
        }
    },

    async created() {},

    mounted() {
        if (this.selectedKpi) {
            this.kpi = { ...this.selectedKpi } as any
        }
        this.registerCodeMirrorHelper()
    },

    watch: {
        selectedKpi() {
            this.kpi = { ...this.selectedKpi } as any
            this.kpi.definition = JSON.parse(this.kpi.definition)
            console.log('watcher: selectedKpi()', this.kpi.definition)
        }
    },

    methods: {
        registerCodeMirrorHelper() {
            CodeMirror.registerHelper('hint', 'measures', (mirror) => {
                var cur = mirror.getCursor()
                var tok = mirror.getTokenAt(cur)
                var start = tok.string.trim() == '' ? tok.start + 1 : tok.start
                var end = tok.end

                var hintList = [] as any

                for (var i = 0; i < this.measures.length; i++) {
                    if (tok.string.trim() == '' || this.measures[i].alias.startsWith(tok.string)) {
                        hintList.push(this.measures[i].alias)
                    }
                }
                return { list: hintList, from: CodeMirror.Pos(cur.line, start), to: CodeMirror.Pos(cur.line, end) }
            })
        },

        //codemirror loaded (setting up events) ================
        onKeyUp(event) {
            console.log(event)

            var cm = this.codeMirror

            if ((event.keyIdentifier != undefined && event.keyIdentifier != 'U+0008' && event.keyIdentifier != 'Left' && event.keyIdentifier != 'Right') || (event.key != undefined && event.key != 'Backspace' && event.key != 'Left' && event.key != 'Right')) {
                var cur = cm.getCursor()
                var token = cm.getTokenAt(cur)

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
            console.log(event)

            event.srcElement = event.target || event.srcElement
            for (var i = 0; i < event.srcElement.classList.length; i++) {
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
            var className = event.srcElement.className
            if (className.startsWith('cm-keyword') || className.startsWith('cm-variable-2')) {
                //prikazi dijalog ovde?
                console.log('Prikazi Dijalog?')
            }
        },

        keyAssistFunc() {
            console.log('keyAssistFunc() {')
            CodeMirror.showHint(this.codeMirror, CodeMirror.hint.measures)
        },

        loadKPI() {
            this.codeMirror = (this.$refs.codeMirror as any).editor as any

            setTimeout(() => {
                this.codeMirror.refresh()
            }, 0)

            this.codeMirror.setValue('')
            this.codeMirror.clearHistory()

            this.codeMirror.setValue(this.kpi.definition.formulaSimple)
            this.changeIndexWithMeasures(this.kpi.definition.functions, this.codeMirror)
        },
        changeIndexWithMeasures(functions, codeMirror) {
            var counter = 0
            for (var i = 0; i < codeMirror.lineCount(); i++) {
                var arrayOfLines = this.removeSpace(codeMirror.getLineTokens(i))
                for (var j = 0; j < arrayOfLines.length; j++) {
                    var token = arrayOfLines[j]
                    console.log('token', token)
                    if (token.type == 'keyword' || token.type == 'variable-2') {
                        console.log('tokentype keywoard or smth')

                        var className = functions[counter]
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
            for (var i = 0; i < tokenList.length; i++) {
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
            for (var i = 0; i < list.length; i++) {
                var object = list[i]
                if (object.alias == item) {
                    return i
                }
            }

            return -1
        },

        //proverava da li je query ok? ako nije vrati korisnika na Formula tab
        parseFormula() {
            this.reset()
            var countOpenBracket = 0
            var countCloseBracket = 0
            var codeMirror = (this.$refs.codeMirror as any).editor as any
            var flag = true
            var numMeasures = 0

            FORFirst: for (var i = 0; i < codeMirror.lineCount(); i++) {
                var line = i + 1
                var array = this.removeSpace(codeMirror.getLineTokens(i))
                for (var j = 0; j < array.length; j++) {
                    var token = array[j]
                    var arr = codeMirror.findMarksAt({ line: i, ch: token.end })
                    if (token.string.trim() != '') {
                        if (arr.length == 0) {
                            if (j - 1 >= 0) {
                                var token_before = array[j - 1]
                                if (token_before.type == 'keyword' || token_before.type == 'variable-2') {
                                    if (token.type == 'keyword' || token.type == 'number' || token.type == 'variable-2' || token.string == '(') {
                                        //	var line = i+1;
                                        flag = false
                                        this.$store.commit('setInfo', { msg: this.$t('kpi.kpiDefinition.errorformula.missingoperator' + line) })
                                        this.reset()
                                        // this.selectedTab.tab = 0
                                        break FORFirst
                                    }
                                }
                                if (token_before.type == 'operator') {
                                    if (token.type == 'operator' || token.string == ')') {
                                        flag = false
                                        this.$store.commit('setInfo', { msg: this.$t('kpi.kpiDefinition.errorformula.malformed' + line) })
                                        this.reset()
                                        //this.selectedTab.tab = 0
                                        break FORFirst
                                    }
                                }
                                if (token_before.type == 'number') {
                                    if (token.type == 'number' || token.string == '(' || token.type == 'keyword' || token.type == 'variable-2') {
                                        //		var line = i+1;
                                        flag = false
                                        this.$store.commit('setInfo', { msg: this.$t('kpi.kpiDefinition.errorformula.malformed' + line) })
                                        this.reset()
                                        //this.selectedTab.tab = 0
                                        break FORFirst
                                    }
                                }
                                if (token_before.type == 'bracket') {
                                    //	var line = i+1;
                                    if ((token.string == ')' && token_before.string == '(') || (token.string == '(' && token_before.string == ')')) {
                                        flag = false
                                        this.$store.commit('setInfo', { msg: this.$t('kpi.kpiDefinition.errorformula.malformed' + line) })
                                        //this.selectedTab.tab = 0
                                        break FORFirst
                                    }
                                    if (token_before.string == ')') {
                                        if (token.type == 'keyword' || token.type == 'number' || token.type == 'variable-2') {
                                            this.$store.commit('setInfo', { msg: this.$t('kpi.kpiDefinition.errorformula.missingoperator' + line) })
                                            this.reset()
                                            flag = false
                                            //this.selectedTab.tab = 0
                                            break FORFirst
                                        }
                                    }
                                }
                                if (token_before.string == '(') {
                                    if (token.type == 'operator') {
                                        flag = false
                                        this.$store.commit('setInfo', { msg: this.$t('kpi.kpiDefinition.errorformula.malformed' + line) })

                                        this.reset()
                                        //this.selectedTab.tab = 0
                                        break FORFirst
                                    }
                                }
                            }
                            if (j == array.length - 1) {
                                //last token
                                if (token.type == 'operator') {
                                    this.$store.commit('setInfo', { msg: this.$t('kpi.kpiDefinition.errorformula.malformed' + line) })
                                    this.reset()
                                    flag = false
                                    //this.selectedTab.tab = 0
                                    break FORFirst
                                }
                            }
                            if (token.type == 'operator') {
                                //operator
                                if (j == 0) {
                                    //	var line = i+1;
                                    this.$store.commit('setInfo', { msg: this.$t('kpi.kpiDefinition.errorformula.malformed' + line) })
                                    this.reset()
                                    flag = false
                                    //this.selectedTab.tab = 0
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
                                this.$store.commit('setInfo', { msg: this.$t('kpi.kpiDefinition.errorformula.missingfunctions') })

                                this.reset()
                                flag = false
                                //this.selectedTab.tab = 0
                                break FORFirst
                            }
                        } else {
                            if (j - 1 >= 0) {
                                token_before = array[j - 1]
                                if (token_before.type == 'number' || token_before.type == 'keyword' || token_before.type == 'variable-2') {
                                    //		var line = i+1;
                                    flag = false
                                    this.$store.commit('setInfo', { msg: this.$t('kpi.kpiDefinition.errorformula.missingoperator') })

                                    this.reset()
                                    //this.selectedTab.tab = 0
                                    break FORFirst
                                }
                            }
                            //parse classes token
                            for (var k = 0; k < arr.length; k++) {
                                var className = arr[k]['className']
                                if (this.measureInList(token.string, this.measures) == -1) {
                                    this.$store.commit('setInfo', { msg: this.$t('kpi.kpiDefinition.errorformula.generic') })

                                    //this.selectedTab.tab = 0
                                    this.reset()
                                    flag = false
                                }
                                if (className == 'codeMirror-m-max') {
                                    numMeasures++
                                    this.measuresToJSON.push(token.string)
                                    this.functionsTOJSON.push('MAX')
                                    var index = this.measuresToJSON.length - 1
                                    var string = 'M' + index
                                    this.formula = this.formula + string
                                    this.formulaDecoded = this.formulaDecoded + 'MAX(' + token.string + ')'
                                    this.formulaSimple = this.formulaSimple + token.string
                                } else if (className == 'codeMirror-m-min') {
                                    numMeasures++
                                    this.measuresToJSON.push(token.string)
                                    this.functionsTOJSON.push('MIN')
                                    index = this.measuresToJSON.length - 1
                                    string = 'M' + index
                                    this.formula = this.formula + string
                                    this.formulaDecoded = this.formulaDecoded + 'MIN(' + token.string + ')'
                                    this.formulaSimple = this.formulaSimple + token.string
                                } else if (className == 'codeMirror-m-count') {
                                    numMeasures++
                                    this.measuresToJSON.push(token.string)
                                    this.functionsTOJSON.push('COUNT')
                                    index = this.measuresToJSON.length - 1
                                    string = 'M' + index
                                    this.formula = this.formula + string
                                    this.formulaDecoded = this.formulaDecoded + 'COUNT(' + token.string + ')'
                                    this.formulaSimple = this.formulaSimple + token.string
                                } else if (className == 'codeMirror-m-sum') {
                                    numMeasures++
                                    this.measuresToJSON.push(token.string)
                                    this.functionsTOJSON.push('SUM')
                                    index = this.measuresToJSON.length - 1
                                    string = 'M' + index
                                    this.formula = this.formula + string
                                    this.formulaDecoded = this.formulaDecoded + 'SUM(' + token.string + ')'
                                    this.formulaSimple = this.formulaSimple + token.string
                                } else if (className == 'error_word') {
                                    this.$store.commit('setInfo', { msg: this.$t('kpi.kpiDefinition.errorformula.generic') })
                                    this.reset()
                                    flag = false
                                    //this.selectedTab.tab = 0
                                    break FORFirst
                                }
                            }
                        }
                    }
                }
            }
            if (countOpenBracket != countCloseBracket && flag) {
                this.reset()
                //this.selectedTab.tab = 0
                this.$store.commit('setInfo', { msg: this.$t('kpi.kpiDefinition.errorformula.missingbracket') })
            } else {
                if (numMeasures == 0 && flag) {
                    this.reset()
                    //this.selectedTab.tab = 0
                    this.$store.commit('setInfo', { msg: this.$t('kpi.kpiDefinition.errorformula.missingmeasure') })
                }
                if (this.formula != '' && flag) {
                    this.formulaForDB['formula'] = this.formula
                    this.formulaForDB['measures'] = this.measuresToJSON
                    this.formulaForDB['functions'] = this.functionsTOJSON
                    this.formulaForDB['formulaDecoded'] = this.formulaDecoded
                    this.formulaForDB['formulaSimple'] = this.formulaSimple
                    return this.formulaForDB
                }
            }
            return {}
            //this.selectedTab.tab = 0
        }
    }
})
</script>

<style lang="scss">
.CodeMirrorMathematica .CodeMirror-code span.cm-keyword,
span.cm-variable-2 {
    color: #7f0055 !important;
    font-weight: bold !important;
}

.CodeMirrorMathematica .CodeMirror-code span.cm-keyword::before,
span.cm-variable-2::before {
    content: 'f(';
    color: green;
}

.CodeMirrorMathematica .CodeMirror-code span.cm-keyword::after,
span.cm-variable-2::after,
.MAX::after,
.MIN::after,
.COUNT::after,
.SUM::after {
    content: ')';
    color: green;
}

.CodeMirrorMathematica .CodeMirror-code span.cm-m-max::before,
.MAX::before {
    content: 'MAX(';
    color: green;
}

.MAX,
.MIN,
.COUNT,
.SUM {
    color: #80004c;
}

.CodeMirrorMathematica .CodeMirror-code span.cm-m-min::before,
.MIN::before {
    content: 'min(';
    color: green;
}

.CodeMirrorMathematica .CodeMirror-code span.cm-m-count::before,
.COUNT::before {
    content: 'COUNT(';
    color: green;
}

.CodeMirrorMathematica .CodeMirror-code span.cm-m-sum::before,
.SUM::before {
    content: 'Σ(';
    color: green;
}
</style>
