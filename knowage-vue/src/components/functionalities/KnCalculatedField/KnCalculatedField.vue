<template>
    <Dialog class="kn-dialog--toolbar--primary calculatedFieldDialogClass" v-bind:visible="visibility" :header="$t('components.KnCalculatedField.title')" :closable="false" modal :breakpoints="{ '960px': '75vw', '640px': '100vw' }">
        <Message severity="info" :closable="false"> {{ $t('components.KnCalculatedField.description') }} </Message>

        <div class="p-fluid p-grid">
            <div class="p-col-8">
                <span class="p-float-label p-field kn-flex">
                    <InputText
                        ref="colName"
                        type="text"
                        class="kn-material-input"
                        id="colName"
                        v-model="v$.cf.colName.$model"
                        :class="{
                            'p-invalid': v$.cf.colName.$invalid
                        }"
                        @blur="v$.cf.colName.$touch()"
                    />
                    <label class="kn-material-input-label"> {{ $t('components.KnCalculatedField.columnName') }} </label>
                </span>
            </div>
            <div class="p-col-4">
                <span v-if="descriptor.availableOutputTypes" class="p-float-label p-field p-ml-2 kn-flex">
                    <Dropdown v-model="cf.outputType" :options="descriptor.availableOutputTypes" class="kn-material-input" optionLabel="label" optionValue="code" />
                    <label class="kn-material-input-label"> {{ $t('components.KnCalculatedField.type') }} </label>
                </span>
            </div>
        </div>

        <Card class="card-0-padding">
            <template #content>
                <div class="p-fluid p-grid p-m-0 p-p-0">
                    <div class="p-col-4">
                        <h5 class="p-float-label p-text-uppercase p-m-2">{{ $t('components.KnCalculatedField.fields') }}</h5>

                        <Listbox class="kn-list kn-flex kn-list-no-border-right" :options="fields" optionLabel="name" listStyle="max-height:200px" style="width:15em"
                            ><template #option="slotProps">
                                <div class="p-text-uppercase kn-list-item" draggable="true" @dragstart="dragElement($event, slotProps.option, 'field')">
                                    <div><i class="fa fa-solid fa-bars"></i></div>
                                    <div class="p-ml-2">{{ slotProps.option.fieldAlias }}</div>
                                </div>
                            </template></Listbox
                        >
                    </div>
                    <div class="p-col-4">
                        <span class="p-float-label">
                            <Dropdown id="category" v-model="selectedCategory" :options="handleOptions()" class="kn-material-input" optionLabel="name" optionValue="code" @change="filterFunctions" />
                            <label for="category" class="kn-material-input-label"> {{ $t(descriptor.category.label) }} </label>
                        </span>
                        <Divider />
                        <Listbox class="kn-list kn-flex kn-list-no-border-right" v-model="selectedFunction" :options="availableFunctions" optionLabel="name" listStyle="max-height:160px"
                            ><template #option="slotProps">
                                <div class="p-text-uppercase kn-list-item p-d-flex p-ai-center" draggable="true" @dragstart="dragElement($event, slotProps.option, 'function')">
                                    <div><i class="fa fa-solid fa-bars"></i></div>
                                    <div class="p-ml-2">{{ slotProps.option.label }}</div>
                                </div>
                            </template></Listbox
                        >
                    </div>
                    <div class="p-col-4">
                        <span v-if="selectedFunction && Object.keys(selectedFunction).length > 0" class="kn-flex p-d-flex p-flex-column p-jc-between helpCol p-m-2">
                            <div class="kn-flex">
                                <h5 class="p-float-label p-text-uppercase p-m-2">
                                    {{ selectedFunction.label }}
                                </h5>
                                <ScrollPanel class="helpScrollPanel custombar1">
                                    {{ selectedFunction.help }}
                                </ScrollPanel>
                            </div>
                            <div>
                                <Message icon="pi-percentage" :closable="false"> {{ selectedFunction.formula }}</Message>
                            </div>
                        </span>
                        <span class="p-m-2" v-else>
                            {{ $t(descriptor.hint) }}
                        </span>
                    </div>
                </div>
            </template>
        </Card>

        <VCodeMirror
            class="p-mt-4 codeMirrorClass"
            ref="formula"
            v-model:value="cf.formula"
            :options="scriptOptions"
            @drop="drop($event)"
            @dragover="handleDragover($event)"
            v-model="v$.cf.formula.$model"
            :class="{
                'p-invalid': v$.cf.formula.$invalid
            }"
        />

        <template #footer>
            <Button class="p-button-text kn-button thirdButton" :label="$t('common.cancel')" @click="cancel" />
            <Button class="kn-button kn-button--primary" v-t="'common.apply'" @click="apply" :disabled="saveButtonDisabled" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { createValidations } from '@/helpers/commons/validationHelper'
import { defineComponent } from 'vue'
import { IKnCalculatedField } from '@/components/functionalities/KnCalculatedField/KnCalculatedField'
import { VCodeMirror } from 'vue3-code-mirror'

import Dropdown from 'primevue/dropdown'
import Dialog from 'primevue/dialog'
import Divider from 'primevue/divider'
import Listbox from 'primevue/listbox'
import Message from 'primevue/message'
import ScrollPanel from 'primevue/scrollpanel'
import useValidate from '@vuelidate/core'

export default defineComponent({
    name: 'calculated-field',
    components: { Dialog, Divider, Dropdown, Listbox, Message, ScrollPanel, VCodeMirror },
    props: {
        fields: Array,
        visibility: Boolean,
        descriptor: Object
    },
    data() {
        return {
            cf: { formula: '' } as IKnCalculatedField,
            allCategories: { name: 'ALL', code: 'ALL' },
            selectedFunction: {},
            selectedCategory: '',

            availableFunctions: [] as any,
            scriptOptions: {
                mode: 'text/x-mathematica',
                indentWithTabs: true,
                smartIndent: true,
                lineWrapping: true,
                matchBrackets: true,
                autofocus: true,
                theme: 'eclipse',
                lineNumbers: true
            },
            v$: useValidate() as any,
            formulaValidationInterval: {} as any,
            isValidFormula: false
        }
    },
    emits: ['save', 'cancel'],
    created() {
        this.availableFunctions = [...this.descriptor?.availableFunctions]
        this.availableFunctions.forEach((x) => {
            x.category = x.category.toUpperCase()
        })

        this.cf = { formula: '' } as IKnCalculatedField
    },

    updated() {
        if (!this.cf.formula) this.cf.formula = ''
    },

    validations() {
        if (this.descriptor) {
            return { cf: createValidations('cf', this.descriptor.validations) }
        }
        return {}
    },

    methods: {
        apply(): void {
            this.$emit('save', this.cf)
            this.clearForm()
        },
        cancel(): void {
            this.$emit('cancel', this.cf)
            this.clearForm()
        },
        clearForm(): void {
            this.cf = {} as IKnCalculatedField
            this.selectedFunction = {}
            this.selectedCategory = ''
        },
        filterFunctions() {
            let tmp = [...this.descriptor?.availableFunctions]
            this.availableFunctions.forEach((x) => {
                x.category = x.category.toUpperCase()
            })
            this.availableFunctions = tmp
            if (this.selectedCategory && this.selectedCategory !== this.allCategories.name) {
                let cat = this.selectedCategory as any
                this.availableFunctions = tmp.filter((x) => x.category.toUpperCase() === cat.toUpperCase())
            }
        },
        handleOptions() {
            let tmp = [] as any

            this.descriptor?.availableFunctions
                .map((x) => ({ name: x.category, code: x.category.toUpperCase() }))
                .forEach((element) => {
                    console.log(element)
                    if (tmp.filter((y) => y.code === element.code).length == 0) tmp.push({ name: element.name, code: element.code })
                })

            if (tmp.filter((x) => x.name === this.allCategories.name).length == 0) tmp = [this.allCategories, ...tmp]

            return tmp
        },
        allowDrop(ev) {
            ev.preventDefault()
        },
        clearCodemirror(editor, cursor, data) {
            if (editor.somethingSelected()) {
                let selections = editor.getSelections()
                for (var sel of selections) {
                    editor.replaceRange('', { line: cursor.line, ch: cursor.ch - JSON.stringify(data).length }, { line: cursor.line, ch: cursor.ch - JSON.stringify(data).length + sel.length })
                }
            }
        },
        handleDragover(ev) {
            const doc = this.$refs.formula as any
            var cursor = doc.editor.getCursor()
            if (ev.target.className.includes('field-')) {
                doc.editor.markText(0, cursor)
            }
        },
        dragElement(ev, item, elementType: String) {
            if (elementType === 'function') {
                ev.dataTransfer.setData('text/plain', JSON.stringify({ item: item.formula, elementType: elementType }))
            } else if (elementType === 'field') {
                ev.dataTransfer.setData('text/plain', JSON.stringify({ item: item, elementType: elementType }))
            }
            ev.dataTransfer.effectAllowed = 'copy'
        },

        drop(ev) {
            ev.stopPropagation()
            ev.preventDefault()

            var data = JSON.parse(ev.dataTransfer.getData('text/plain'))

            const doc = this.$refs.formula as any
            let editor = doc.editor
            var cursor = editor.getCursor()

            this.clearCodemirror(editor, cursor, data)

            editor.clearHistory()

            cursor = editor.getCursor()

            let start = editor.findWordAt(cursor).anchor.ch
            let end = editor.findWordAt(cursor).head.ch

            let from = { line: cursor.line, ch: start }
            let to = { line: cursor.line, ch: end }

            let range = editor.getRange(from, to)
            let spContent = data.elementType === 'function' ? data.item : data.item.fieldAlias

            if (range === '' || range.match(/\(|\)|,|\./g)) {
                editor.replaceSelection(spContent, cursor)
            } else {
                const sp = document.createElement('span')
                sp.textContent = spContent
                editor.doc.markText(from, to, {
                    replacedWith: sp,
                    inclusiveLeft: false,
                    inclusiveRight: false
                })
            }
        },
        applyValidationResultsToFormula() {
            const doc = this.$refs.formula as any
            let editor = doc.editor

            let from = { line: editor.firstLine(), ch: 0 }
            let to = { line: editor.lastLine(), ch: editor.getLine(editor.lastLine()).length }

            if (!this.isValidFormula) {
                editor.markText(from, to, { className: 'syntax-error' })
            } else {
                editor.markText(from, to, { className: 'no-syntax-error' })
            }
        }
    },
    watch: {
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
            return this.v$.$invalid || !this.isValidFormula
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
    height: 100px;
    max-height: 100px;
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
        height: 120px;
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
</style>
