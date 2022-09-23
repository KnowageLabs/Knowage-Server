<template>
    <Dialog class="kn-dialog--toolbar--primary calculatedFieldDialogClass" v-bind:visible="visibility" :header="$t('components.knCalculatedField.title')" :closable="false" modal :breakpoints="{ '960px': '75vw', '640px': '100vw' }">
        <Message severity="info" :closable="false">{{ $t('components.knCalculatedField.description') }}</Message>

        <div class="p-fluid p-grid">
            <div class="p-col">
                <span class="p-float-label p-field kn-flex">
                    <InputText
                        ref="colName"
                        type="text"
                        :disabled="readOnly"
                        class="kn-material-input"
                        id="colName"
                        v-model="v$.cf.colName.$model"
                        :class="{
                            'p-invalid': v$.cf.colName.$invalid
                        }"
                        @blur="v$.cf.colName.$touch()"
                    />
                    <label class="kn-material-input-label"> {{ $t('components.knCalculatedField.columnName') }} </label>
                </span>
            </div>
            <slot name="additionalInputs"> </slot>
        </div>

        <Card class="card-0-padding">
            <template #content>
                <div class="p-fluid p-grid">
                    <div class="p-col-4">
                        <h5 class="p-float-label p-text-uppercase p-m-2">{{ $t('components.knCalculatedField.fields') }}</h5>

                        <ScrollPanel class="kn-list knListBox kn-flex kn-list-no-border-right" style="height: 200px !important; border: 1px">
                            <div v-for="(field, index) in fields" v-bind:key="index" class="kn-list-item p-d-flex p-ai-center fieldType kn-truncated p-ml-2" draggable="true" @dragstart="dragElement($event, field, 'field')" v-tooltip.bottom="source === 'QBE' ? field.fieldLabel : field.fieldAlias">
                                <div><i class="fa fa-solid fa-bars"></i></div>
                                <div v-if="source === 'QBE'" class="p-ml-2">{{ field.fieldLabel }}</div>
                                <div v-else class="p-ml-2">{{ field.fieldAlias }}</div>
                            </div>
                        </ScrollPanel>
                    </div>
                    <div class="p-col-4">
                        <span class="p-float-label p-m-2">
                            <Dropdown id="category" v-model="selectedCategory" :options="availableCategories" class="kn-material-input" optionLabel="name" optionValue="code" @change="filterFunctions" />
                            <label for="category" class="kn-material-input-label"> {{ $t(descriptor.category.label) }} </label>
                        </span>

                        <h5 class="p-float-label p-text-uppercase p-m-2">{{ $t('components.knCalculatedField.functions') }}</h5>
                        <ScrollPanel class="kn-list knListBox kn-flex kn-list-no-border-right" style="height: 150px !important; border: 1px">
                            <div
                                v-for="(af, index) in availableFunctions"
                                v-bind:key="index"
                                class="kn-list-item p-d-flex p-ai-center formulaType kn-truncated p-ml-2"
                                :class="{ selected: af.formula === selectedFunction.formula }"
                                draggable="true"
                                @dragstart="dragElement($event, af, 'function')"
                                v-tooltip.bottom="af.formula"
                                @click="handleClick(af)"
                            >
                                <div><i class="fa fa-solid fa-bars"></i></div>
                                <div class="p-ml-2">{{ af.formula }}</div>
                            </div>
                        </ScrollPanel>
                    </div>
                    <div class="p-col-4">
                        <span v-if="showHelpPanel" class="kn-flex p-d-flex p-flex-column p-jc-between helpCol p-m-2">
                            <h5 class="p-float-label p-text-uppercase p-m-2">
                                {{ selectedFunction.label }}
                            </h5>

                            <ScrollPanel class="helpScrollPanel custombar1"> <div v-html="$t(selectedFunction.help)"></div></ScrollPanel>

                            <div v-if="selectedFunction.officialDocumentationLink" class="helpClass">
                                <a :href="selectedFunction.officialDocumentationLink" target="_blank"> {{ $t('components.knCalculatedField.officialDocumentation', { function: selectedFunction.label }) }}</a>
                            </div>
                        </span>
                        <span class="p-m-2" v-else>
                            <KnHint class="kn-hint-sm" :title="'components.knCalculatedField.title'" :hint="$t(descriptor.hint)" data-test="hint"></KnHint>
                        </span>
                    </div>
                </div>
            </template>
        </Card>

        <VCodeMirror :class="['p-mt-2 codeMirrorClass', this.readOnly ? 'readOnly' : '', v$.cf.formula.$invalid ? 'p-invalid' : '']" ref="codeMirror" v-model:value="cf.formula" :options="scriptOptions" @drop="drop" v-model="v$.cf.formula.$model" />

        <template #footer>
            <Button :class="readOnly ? 'kn-button kn-button--primary' : 'kn-button kn-button--secondary'" :label="$t('common.cancel')" @click="cancel" />
            <Button v-if="!readOnly" class="kn-button kn-button--primary" v-t="'common.apply'" @click="apply" :disabled="saveButtonDisabled" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { createValidations } from '@/helpers/commons/validationHelper'
import { defineComponent } from 'vue'
import { IKnCalculatedField, IKnCalculatedFieldFunction } from '@/components/functionalities/KnCalculatedField/KnCalculatedField'
import VCodeMirror, { CodeMirror } from 'codemirror-editor-vue3'

import Dropdown from 'primevue/dropdown'
import Dialog from 'primevue/dialog'
import KnHint from '@/components/UI/KnHint.vue'
import Message from 'primevue/message'
import ScrollPanel from 'primevue/scrollpanel'
import useValidate from '@vuelidate/core'

export default defineComponent({
    name: 'calculated-field',
    components: { Dialog, Dropdown, KnHint, Message, ScrollPanel, VCodeMirror },
    props: {
        fields: Array,
        visibility: Boolean,
        readOnly: Boolean,
        descriptor: Object,
        template: {} as any,
        valid: Boolean,
        source: String,
        propCalcFieldFunctions: { type: Array as PropType<IKnCalculatedFieldFunction[]>, required: true }
    },
    data() {
        return {
            cf: { formula: '' } as IKnCalculatedField,
            allCategories: { name: 'ALL', code: 'ALL' },
            selectedFunction: {},
            selectedCategory: '',
            availableCategories: [] as any,
            codeMirror: null as any,
            availableFunctions: [] as any,
            scriptOptions: {
                mode: 'text/x-mathematica',
                indentWithTabs: true,
                smartIndent: true,
                lineWrapping: true,
                matchBrackets: true,
                autofocus: true,
                theme: 'eclipse',
                lineNumbers: true,
                readOnly: this.readOnly
            },
            v$: useValidate() as any,
            formulaValidationInterval: {} as any,
            isValidFormula: false,
            calcFieldFunctions: [] as IKnCalculatedFieldFunction[],
            showHelpPanel: false
        }
    },
    emits: ['save', 'cancel', 'update:readOnly'],
    created() {
        this.calcFieldFunctions = [...this.propCalcFieldFunctions]
        this.availableFunctions = [...this.calcFieldFunctions].sort((a, b) => {
            return a.name.localeCompare(b.name)
        })
        this.availableFunctions.forEach((x) => {
            x.category = x.category.toUpperCase()
        })

        this.cf = { formula: '' } as IKnCalculatedField

        if (!this.readOnly && this.template && !this.template.parameters && this.source === 'QBE') {
            this.cf = { colName: this.template.alias, formula: this.template.expression } as IKnCalculatedField
        }
        this.handleCategories()
    },

    updated() {
        this.setupCodeMirror()
        if (!this.cf.formula) this.cf.formula = ''

        if (this.readOnly && this.template && this.template.parameters) {
            this.cf = {} as IKnCalculatedField
            for (var i = 0; i < this.template.parameters.length; i++) {
                if (this.template.parameters[i]['name'] == 'formula') this.cf.formula = this.template.parameters[i]['value']
                else if (this.template.parameters[i]['name'] == 'colName') this.cf.colName = this.template.parameters[i]['value']
            }
        }

        if (!this.readOnly && this.template && !this.template.parameters && this.source === 'QBE') {
            this.cf = { colName: this.template.alias, formula: this.template.expression } as IKnCalculatedField
        }
    },

    validations() {
        if (this.descriptor) {
            return { cf: createValidations('cf', this.descriptor.validations) }
        }
        return {}
    },

    methods: {
        handleClick(af) {
            if (JSON.stringify(this.selectedFunction) === JSON.stringify(af)) {
                this.selectedFunction = {}
            } else {
                this.selectedFunction = af
            }
        },
        setupCodeMirror() {
            CodeMirror.Pos(0, 0)
            const interval = setInterval(() => {
                if (!this.$refs.codeMirror) return
                this.codeMirror = (this.$refs.codeMirror as any).cminstance as any
                setTimeout(() => {
                    this.codeMirror.refresh()
                }, 0)
                clearInterval(interval)
            }, 200)
        },
        apply(): void {
            this.$emit('save', this.cf)
            this.clearForm()
        },
        cancel(): void {
            this.$emit('update:readOnly', false)
            this.$emit('cancel', this.cf)
            this.clearForm()
        },
        clearForm(): void {
            this.cf = { formula: '' } as IKnCalculatedField
            this.selectedFunction = {}
            this.selectedCategory = ''
        },
        filterFunctions() {
            let tmp = [...this.calcFieldFunctions].sort((a, b) => {
                return a.name.localeCompare(b.name)
            })
            tmp.forEach((x) => {
                x.category = x.category.toUpperCase()
            })
            this.availableFunctions = tmp
            if (this.selectedCategory && this.selectedCategory !== this.allCategories.name) {
                let cat = this.selectedCategory as any
                this.availableFunctions = tmp.filter((x) => x.category.toUpperCase() === cat.toUpperCase())
            }
        },
        handleCategories() {
            let tmp = [] as any

            this.calcFieldFunctions
                .sort((a, b) => {
                    return a.name.localeCompare(b.name)
                })
                .map((x) => ({ name: x.category, code: x.category.toUpperCase() }))
                .forEach((element) => {
                    if (tmp.filter((y) => y.code === element.code).length == 0) tmp.push({ name: element.name, code: element.code })
                })

            if (tmp.filter((x) => x.name === this.allCategories.name).length == 0) tmp = [this.allCategories, ...tmp]

            this.availableCategories = tmp
        },
        allowDrop(ev) {
            ev.preventDefault()
        },
        clearCodemirror(cursor, data) {
            if (this.codeMirror.somethingSelected()) {
                let selections = this.codeMirror.getSelections()
                for (var sel of selections) {
                    this.codeMirror.replaceRange('', { line: cursor.line, ch: cursor.ch - JSON.stringify(data).length }, { line: cursor.line, ch: cursor.ch - JSON.stringify(data).length + sel.length })
                }
            }
        },

        dragElement(ev, item, elementType: String) {
            if (this.readOnly) return
            if (elementType === 'function') {
                ev.dataTransfer.setData('myItem', JSON.stringify({ item: item.formula, elementType: elementType }))
            } else if (elementType === 'field') {
                ev.dataTransfer.setData('myItem', JSON.stringify({ item: item, elementType: elementType }))
            }
            ev.dataTransfer.effectAllowed = 'copy'
        },

        drop(cm, ev) {
            if (this.readOnly) return
            ev.stopPropagation()
            ev.preventDefault()

            var data = JSON.parse(ev.dataTransfer.getData('myItem'))

            var cursor = cm.coordsChar({
                left: ev.x,
                top: ev.y
            })

            this.clearCodemirror(cursor, data)

            this.codeMirror.clearHistory()

            let start = -1
            let end = -1
            if (cm.getLine(cursor.line).length == cursor.ch) {
                start = cursor.ch
                end = cursor.ch
            } else {
                start = this.codeMirror.findWordAt(cursor).anchor.ch
                end = this.codeMirror.findWordAt(cursor).head.ch
            }

            let from = { line: cursor.line, ch: start }
            let to = { line: cursor.line, ch: end }

            let range = this.codeMirror.getDoc().getRange(from, to)
            let fieldAlias = this.source !== 'QBE' ? '$F{' + data.item.fieldAlias + '}' : data.item.fieldAlias
            let spContent = data.elementType === 'function' ? data.item : fieldAlias

            if (range.match(/\(|\)|,|\./g)) {
                this.codeMirror.getDoc().replaceSelection(spContent, cursor)
            } else {
                this.codeMirror.getDoc().replaceRange(spContent, from, to)
            }

            let lines = document.querySelector('.CodeMirror-line')
            if (lines) {
                let textEl = lines.querySelector('div span') as any

                if (textEl) this.cf.formula = textEl.innerText
            }

            this.codeMirror.refresh()
        },
        applyValidationResultsToFormula() {
            let from = { line: this.codeMirror.getDoc().firstLine(), ch: 0 }
            let to = { line: this.codeMirror.getDoc().lastLine(), ch: this.codeMirror.getDoc().getLine(this.codeMirror.getDoc().lastLine()).length }

            if (!this.isValidFormula) {
                this.codeMirror.getDoc().markText(from, to, { className: 'syntax-error' })
            } else {
                this.codeMirror.getDoc().markText(from, to, { className: 'no-syntax-error' })
            }
        }
    },
    watch: {
        selectedFunction(newValue, oldValue) {
            if (newValue && oldValue !== newValue && newValue.label) {
                this.showHelpPanel = true
            } else {
                this.showHelpPanel = false
            }
        },
        readOnly(value) {
            this.scriptOptions.readOnly = value
        },
        visibility(newV, oldV) {
            if (newV && newV !== oldV) {
                if (!this.selectedCategory) {
                    if (this.descriptor?.defaultSelectedCategory) this.selectedCategory = this.descriptor?.defaultSelectedCategory
                    else this.selectedCategory = this.allCategories.name
                }
            }
        },
        cf: {
            handler() {
                if (this.cf.formula) {
                    if (this.descriptor?.validationServiceUrl) {
                        this.formulaValidationInterval = setInterval(() => {
                            this.$http.get(this.descriptor?.validationServiceUrl).then((response: AxiosResponse<any>) => {
                                this.isValidFormula = response.data[0]
                                this.applyValidationResultsToFormula()
                            })
                            clearInterval(this.formulaValidationInterval)
                            this.formulaValidationInterval = null
                        }, 2500)
                    } else {
                        this.isValidFormula = true
                    }
                }
            },
            deep: true
        }
    },
    computed: {
        saveButtonDisabled(): any {
            if (typeof this.valid === 'undefined') return this.v$.$invalid || !this.isValidFormula
            else return this.v$.$invalid || !this.isValidFormula || !this.valid
        }
    }
})
</script>
<style lang="scss">
.calculatedFieldDialogClass {
    min-width: 600px;
    width: 60%;
    max-width: 1200px;
}

.codeMirrorClass {
    height: 80px !important;
    max-height: 80px !important;
    border: 1px solid var(--kn-color-borders);

    .CodeMirror-scroll {
        overflow-x: hidden !important;
        overflow-y: auto !important;
    }
}

.readOnly {
    .CodeMirror-scroll {
        background-color: var(--kn-color-disabled);
        cursor: default;
        .CodeMirror-lines {
            cursor: default !important;
        }
    }
}

.field-header {
    font-weight: bold;
}

.kn-remove-card-padding .data-condition-list {
    border: 1px solid var(--kn-color-borders);
    border-top: none;
}

.p-listbox-item {
    height: 24px;
    .kn-list-item {
        height: 24px;
    }
}

.card-0-padding .p-card-body,
.card-0-padding .p-card-content {
    padding: 0.25rem;
}

.helpCol {
    height: 100%;
    width: 100%;

    .helpScrollPanel {
        font-size: 0.75em !important;
        height: 140px !important;
    }
}

::v-deep(.p-scrollpanel) {
    p {
        padding: 0.5rem;
        line-height: 1.5;
        margin: 0;
    }
    &.custombar1 {
        .p-scrollpanel-wrapper {
            border-right: 9px solid var(--surface-ground);
        }
        .p-scrollpanel-bar {
            background-color: var(--primary-color);
            opacity: 1;
            transition: background-color 0.2s;
            &:hover {
                background-color: #007ad9;
            }
        }
    }
}

.syntax-error {
    text-decoration: underline;
    text-decoration-style: wavy;
    text-decoration-color: red;
}

.no-syntax-error {
    text-decoration: none;
}

.helpClass {
    font-size: 0.75em;
}

.fieldType,
.formulaType {
    font-size: 0.75em;
    height: 25px !important;
    border-bottom: 1px solid var(--kn-list-border-color);
    cursor: -webkit-grab;
    cursor: grab;

    &.selected {
        background-color: var(--kn-list-item-selected-background-color) !important;
    }

    &:hover {
        background-color: var(--kn-list-item-hover-background-color);
    }
}
</style>
