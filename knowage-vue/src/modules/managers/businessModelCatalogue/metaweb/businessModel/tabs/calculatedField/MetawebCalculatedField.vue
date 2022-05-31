<template>
    <DataTable :value="businessModel.calculatedBusinessColumns" class="p-datatable-sm kn-table p-ml-2" responsiveLayout="stack" breakpoint="960px">
        <template #empty>
            {{ $t('common.info.noDataFound') }}
        </template>
        <Column field="name" :header="$t('common.name')" :sortable="true" />
        <Column :style="descriptor.style.iconColumnStyle" class="p-text-right">
            <template #header>
                <Button :label="$t('common.add')" class="p-button-link p-text-right" @click="addCalcField" />
            </template>
            <template #body="slotProps">
                <Button icon="far fa-edit" class="p-button-link" @click="editCalcField(slotProps.data)" />
                <Button icon="pi pi-trash" class="p-button-link" @click="deleteCalcField(slotProps.data)" />
            </template>
        </Column>
    </DataTable>

    <KnCalculatedField
        v-if="calcFieldDialogVisible"
        v-model:template="selectedCalcField"
        v-model:visibility="calcFieldDialogVisible"
        :fields="calcFieldColumns"
        :descriptor="calcFieldDescriptor"
        :propCalcFieldFunctions="calcFieldFunctions"
        :source="'QBE'"
        :readOnly="false"
        :valid="true"
        @save="onCalcFieldSave"
        @cancel="calcFieldDialogVisible = false"
    >
        <template #additionalInputs>
            <div class="p-field" :class="[selectedCalcField.type === 'DATE' ? 'p-col-3' : 'p-col-4']">
                <span class="p-float-label ">
                    <Dropdown id="type" class="kn-material-input" v-model="selectedCalcField.type" :options="descriptor.types" optionLabel="label" optionValue="name" />
                    <label for="type" class="kn-material-input-label"> {{ $t('components.knCalculatedField.type') }} </label>
                </span>
            </div>
            <div v-if="selectedCalcField.type === 'DATE'" class="p-field p-col-3">
                <span class="p-float-label ">
                    <Dropdown id="type" class="kn-material-input" v-model="selectedCalcField.format" :options="descriptor.admissibleDateFormats">
                        <template #value>
                            <span>{{ selectedCalcField.format ? moment().format(selectedCalcField.format) : '' }}</span>
                        </template>
                        <template #option="slotProps">
                            <span>{{ moment().format(slotProps.option) }}</span>
                        </template>
                    </Dropdown>
                    <label for="type" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanDateFormat') }} </label>
                </span>
            </div>
            <div class="p-field" :class="[selectedCalcField.type === 'DATE' ? 'p-col-3' : 'p-col-4']">
                <span class="p-float-label ">
                    <Dropdown id="columnType" class="kn-material-input" v-model="selectedCalcField.nature" :options="descriptor.columnTypes" optionLabel="label" optionValue="name" />
                    <label for="columnType" class="kn-material-input-label"> {{ $t('managers.functionsCatalog.columnType') }} </label>
                </span>
            </div>
        </template>
    </KnCalculatedField>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent, PropType } from 'vue'
import { iBusinessModel } from '../../../Metaweb'
import { IKnCalculatedFieldFunction } from '@/components/functionalities/KnCalculatedField/KnCalculatedField'
import moment from 'moment'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import descriptor from './MetawebCalculatedFieldDescriptor.json'
import calcFieldDescriptor from './MetawebCalcFieldDescriptor.json'
import KnCalculatedField from '@/components/functionalities/KnCalculatedField/KnCalculatedField.vue'
import Dropdown from 'primevue/dropdown'

const { generate, applyPatch } = require('fast-json-patch')
const deepcopy = require('deepcopy')

export default defineComponent({
    name: 'metaweb-filter-tab',
    components: { DataTable, Column, KnCalculatedField, Dropdown },
    props: { selectedBusinessModel: { type: Object as PropType<iBusinessModel | null> }, propMeta: { type: Object }, propCustomFunctions: { type: Array }, observer: { type: Object, required: true } },
    emits: ['metaUpdated'],
    data() {
        return {
            moment,
            descriptor,
            calcFieldDescriptor,
            meta: null as any,
            businessModel: null as iBusinessModel | null,
            calcFieldDialogVisible: false,
            readOnly: false,
            selectedCalcField: {} as any,
            calcFieldColumns: [] as any,
            calcFieldFunctions: [] as IKnCalculatedFieldFunction[]
        }
    },
    computed: {
        isGeographicBm(): boolean {
            let hideFields = false
            this.businessModel?.properties?.forEach((el: any) => {
                const key = Object.keys(el)[0]
                if (key === 'structural.tabletype' && el[key].value === 'geographic dimension') {
                    hideFields = true
                }
            })
            return hideFields
        }
    },
    watch: {
        selectedBusinessModel: {
            handler() {
                this.loadMeta()
                this.loadBusinessModel()
                this.calcFieldFunctions = this.createCalcFieldFunctions(calcFieldDescriptor.availableFunctions, this.propCustomFunctions)
            },
            deep: true
        }
    },
    created() {
        this.loadMeta()
        this.loadBusinessModel()
        this.calcFieldFunctions = this.createCalcFieldFunctions(calcFieldDescriptor.availableFunctions, this.propCustomFunctions)
    },
    methods: {
        loadMeta() {
            this.meta = this.propMeta as any
        },
        loadBusinessModel() {
            this.businessModel = this.selectedBusinessModel as iBusinessModel
        },
        updateMeta() {
            setTimeout(() => {
                this.$emit('metaUpdated')
            }, 250)
        },

        createCalcFieldColumns() {
            this.calcFieldColumns = []
            this.businessModel?.simpleBusinessColumns.forEach((field) => {
                this.calcFieldColumns.push({ fieldAlias: field.name, fieldLabel: field.name })
            })
        },

        editCalcField(calcField) {
            this.createCalcFieldColumns()
            this.selectedCalcField = this.formatCalcFieldForComponent(calcField)
            this.calcFieldDialogVisible = true
        },

        formatCalcFieldForComponent(calcField) {
            let formatField = {} as any

            formatField.alias = calcField.name
            formatField.uniqueName = calcField.uniqueName

            for (var i = 0; i < calcField.properties.length; i++) {
                var key = Object.keys(calcField.properties[i])[0]
                if (key === 'structural.datatype') {
                    formatField.type = calcField.properties[i][key].value.toUpperCase()
                }
                if (key === 'structural.expression') {
                    formatField.expression = calcField.properties[i][key].value
                }
                if (key === 'structural.columntype') {
                    formatField.nature = calcField.properties[i][key].value.toUpperCase()
                }
            }

            return formatField
        },

        addCalcField() {
            this.createCalcFieldColumns()
            this.selectedCalcField = { alias: '', expression: '', format: undefined, nature: 'ATTRIBUTE', type: 'STRING' } as any
            this.calcFieldDialogVisible = true
        },

        onCalcFieldSave(event) {
            let calculatedField = {
                expression: event.formula,
                dataType: this.selectedCalcField.type,
                columnType: this.selectedCalcField.nature.toLowerCase(),
                name: event.colName,
                sourceTableName: this.businessModel?.uniqueName,
                editMode: false
            } as any

            console.log(calculatedField.dataType, event)
            calculatedField.dataType == 'DATE' ? (calculatedField.format = this.selectedCalcField.format) : ''

            if (this.selectedCalcField.uniqueName) {
                calculatedField.uniquename = this.selectedCalcField.uniqueName
                calculatedField.editMode = true
            }

            this.createCalcField(calculatedField)
            this.calcFieldDialogVisible = false
        },

        async createCalcField(calculatedField) {
            const postData = { data: calculatedField, diff: generate(this.observer) }
            await this.$http
                .post(process.env.VUE_APP_META_API_URL + `/1.0/metaWeb/setCalculatedField`, postData)
                .then((response: AxiosResponse<any>) => {
                    this.meta = applyPatch(this.meta, response.data).newDocument
                })
                .catch(() => {})
                .finally(() => generate(this.observer))
        },

        async deleteCalcField(calcField) {
            let dataToSend = { name: calcField.name, sourceTableName: this.businessModel?.uniqueName }
            const postData = { data: dataToSend, diff: generate(this.observer) }
            await this.$http
                .post(process.env.VUE_APP_META_API_URL + `/1.0/metaWeb/deleteCalculatedField`, postData)
                .then((response: AxiosResponse<any>) => {
                    this.meta = applyPatch(this.meta, response.data).newDocument
                })
                .catch(() => {})
                .finally(() => generate(this.observer))
        },
        createCalcFieldFunctions(providedFunctions, customFunctions?) {
            let functions = deepcopy(providedFunctions)

            if (customFunctions) {
                customFunctions.forEach((funct) => {
                    functions.push(funct)
                })
            }
            if (!this.isGeographicBm) {
                let tempFunctions = deepcopy(functions)
                functions = tempFunctions.filter((funct) => {
                    return funct.category !== 'SPATIAL'
                })
            }

            return functions
        }
    }
})
</script>
