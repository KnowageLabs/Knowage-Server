<template>
    <DataTable :value="businessModel.calculatedBusinessColumns" class="p-datatable-sm kn-table p-ml-2" responsiveLayout="stack" breakpoint="960px">
        <template #empty>
            {{ $t('common.info.noDataFound') }}
        </template>
        <Column field="name" :header="$t('common.name')" :sortable="true" />
        <Column :style="descriptor.style.iconColumnStyle" class="p-text-right">
            <template #header>
                <Button :label="$t('common.add')" class="p-button-link p-text-right" @click="onCalcFieldSave" />
            </template>
            <template #body="slotProps">
                <Button icon="far fa-edit" class="p-button-link" @click="editCalcField(slotProps.data)" />
                <Button icon="pi pi-trash" class="p-button-link" @click="deleteCalcField(slotProps.data)" />
            </template>
        </Column>
    </DataTable>

    <KnCalculatedField v-model:visibility="calcFieldDialogVisible" @save="onCalcFieldSave" @cancel="closeCalcField" :fields="calcFieldColumns" :descriptor="calcFieldDescriptor" :readOnly="readOnly" @update:readOnly="updateReadOnly" v-model:template="selectedTransformation" />
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent, PropType } from 'vue'
import { iBusinessModel } from '../../../Metaweb'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import descriptor from './MetawebCalculatedFieldDescriptor.json'
import calcFieldDescriptor from './MetawebCalcFieldDescriptor.json'
import KnCalculatedField from '@/components/functionalities/KnCalculatedField/KnCalculatedField.vue'

const { generate, applyPatch } = require('fast-json-patch')

export default defineComponent({
    name: 'metaweb-filter-tab',
    components: { DataTable, Column, KnCalculatedField },
    props: { selectedBusinessModel: { type: Object as PropType<iBusinessModel | null> }, propMeta: { type: Object }, observer: { type: Object, required: true } },
    emits: ['metaUpdated'],
    data() {
        return {
            descriptor,
            calcFieldDescriptor,
            meta: null as any,
            businessModel: null as iBusinessModel | null,
            calcFieldDialogVisible: false,
            readOnly: false,
            selectedTransformation: {},
            calcFieldColumns: [] as any
        }
    },
    watch: {
        selectedBusinessModel() {
            this.loadMeta()
            this.loadBusinessModel()
        }
    },
    created() {
        this.loadMeta()
        this.loadBusinessModel()
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
        editCalcField(event) {
            console.log(event)
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
        showCalcField() {
            this.createCalcFieldColumns()
            this.calcFieldDialogVisible = true
        },
        createCalcFieldColumns() {
            this.calcFieldColumns = []
            this.businessModel?.simpleBusinessColumns.forEach((field) => {
                this.calcFieldColumns.push({ fieldAlias: field.name })
            })
        },
        closeCalcField() {
            this.calcFieldDialogVisible = false
        },
        onCalcFieldSave() {
            let calcFieldOutput = {
                alias: 'Calc FIELD', //iz input fielda
                expression: 'Employee id + 99 + 3 +2 +1', //codemirror formula
                format: undefined, // koristi se samo ako je izabran datum, datum formata iz dropdowna
                nature: 'MEASURE', //nature dropdown
                type: 'STRING' // type dropdown
            }

            let calculatedField = {
                expression: calcFieldOutput.expression,
                dataType: calcFieldOutput.type,
                columnType: calcFieldOutput.nature.toLowerCase(),
                name: calcFieldOutput.alias,
                sourceTableName: this.businessModel?.uniqueName,
                editMode: false
            }

            // let calculatedField = buildCalculatedField(calcFieldOutput, this.selectedQuery.fields)
            console.log(calcFieldOutput, calculatedField)
            this.createCalcField(calculatedField)
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
        }
    }
})
</script>
