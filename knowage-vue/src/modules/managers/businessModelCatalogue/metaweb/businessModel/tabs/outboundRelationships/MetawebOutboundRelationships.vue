<template>
    <DataTable :value="inboundRelationships" class="p-datatable-sm kn-table p-ml-2" responsiveLayout="stack" breakpoint="960px" v-model:filters="filters" :globalFilterFields="orDescriptor.globalFilterFields">
        <template #empty>
            {{ $t('common.info.noDataFound') }}
        </template>
        <template #header>
            <div class="table-header p-d-flex">
                <span class="p-input-icon-left p-mr-3 p-col-12">
                    <i class="pi pi-search" />
                    <InputText class="kn-material-input" v-model="filters['global'].value" :placeholder="$t('common.search')" />
                </span>
            </div>
        </template>
        <Column field="name" :header="$t('common.name')" :sortable="true" />
        <Column field="sourceTableName" :header="$t('metaweb.businessModel.sourceTable')" :sortable="true" />
        <Column class="kn-truncated" :header="$t('metaweb.physicalModel.sourceColumns')" :sortable="true">
            <template #body="slotProps">
                <span v-tooltip.top="createColumnString(slotProps.data.sourceColumns)">{{ createColumnString(slotProps.data.sourceColumns) }}</span>
            </template>
        </Column>
        <Column field="destinationTableName" :header="$t('metaweb.businessModel.targetTable')" :sortable="true" />
        <Column :header="$t('metaweb.physicalModel.targetColumns')" :sortable="true">
            <template #body="slotProps">
                <span v-tooltip.top="createColumnString(slotProps.data.destinationColumns)">{{ createColumnString(slotProps.data.destinationColumns) }}</span>
            </template>
        </Column>
        <Column :style="orDescriptor.style.iconColumnStyle" class="p-text-right">
            <template #header>
                <Button :label="$t('common.add')" class="p-button-link p-text-right" @click="inboundDialogVisible = true" />
            </template>
            <template #body="slotProps">
                <Button icon="pi pi-trash" class="p-button-link" @click="deleteOutbound(slotProps.data)" />
            </template>
        </Column>
    </DataTable>

    <Dialog class="bsdialog" :style="bsDescriptor.style.bsDialog" :visible="inboundDialogVisible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                <template #left>
                    {{ $t('metaweb.businessModel.tabView.outbound') }}
                </template>
            </Toolbar>
        </template>
        <form id="inbound-form" class="p-fluid p-formgrid p-grid p-mt-4 p-mx-2 kn-flex-0">
            <div class="p-field p-col-12 p-md-6">
                <span class="p-float-label">
                    <InputText
                        id="name"
                        class="kn-material-input"
                        v-model.trim="v$.dataSend.name.$model"
                        :class="{
                            'p-invalid': v$.dataSend.name.$invalid && v$.dataSend.name.$dirty
                        }"
                        @blur="v$.dataSend.name.$touch()"
                        @change="$emit('touched')"
                    />
                    <label for="name" class="kn-material-input-label"> {{ $t('metaweb.businessModel.inbound.name') }} *</label>
                </span>
                <KnValidationMessages class="p-mt-1" :vComp="v$.dataSend.name" :additionalTranslateParams="{ fieldName: $t('metaweb.businessModel.inbound.name') }" />
            </div>
            <div class="p-field p-col-12 p-md-6">
                <span class="p-float-label ">
                    <Dropdown
                        id="source"
                        class="kn-material-input"
                        v-model="v$.dataSend.cardinality.$model"
                        optionLabel="name"
                        optionValue="value"
                        :options="orDescriptor.cardinality"
                        :class="{ 'p-invalid': v$.dataSend.cardinality.$invalid && v$.dataSend.cardinality.$dirty }"
                        @blur="v$.dataSend.cardinality.$touch()"
                    />
                    <label for="source" class="kn-material-input-label"> {{ $t('kpi.kpiDefinition.cardinalityTtitle') }} *</label>
                </span>
                <KnValidationMessages class="p-mt-1" :vComp="v$.dataSend.cardinality" :additionalTranslateParams="{ fieldName: $t('kpi.kpiDefinition.cardinalityTtitle') }" />
            </div>
            <div class="p-field p-col-12 p-md-6">
                <span class="p-float-label">
                    <InputText id="target" class="kn-material-input" v-model="businessModel.name" :disabled="true" />
                    <label for="target" class="kn-material-input-label"> {{ $t('metaweb.businessModel.inbound.targetBc') }} </label>
                </span>
            </div>
            <div class="p-field p-col-12 p-md-6">
                <span class="p-float-label ">
                    <Dropdown id="source" class="kn-material-input" v-model="rightElement" :options="sourceBusinessClassOptions" optionLabel="name" @change="alterTableToSimpleBound($event.value)" />
                    <label for="source" class="kn-material-input-label"> {{ $t('metaweb.businessModel.inbound.sourceBc') }} </label>
                </span>
            </div>
        </form>

        <TableAssociator class="kn-flex" :sourceArray="simpleLeft" :targetArray="simpleRight" :useMultipleTablesFromSameSource="false" @drop="onDrop" @relationshipDeleted="onDelete" />

        <template #footer>
            <Button class="p-button-text kn-button" :label="$t('common.cancel')" @click="closeDialog" />
            <Button class="kn-button kn-button--primary" :label="$t('common.create')" :disabled="buttonDisabled" @click="createOutbound" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import { defineComponent, PropType } from 'vue'
import { iBusinessModel } from '@/modules/managers/businessModelCatalogue/metaweb/Metaweb'
import { filterDefault } from '@/helpers/commons/filterHelper'
import useValidate from '@vuelidate/core'
import orDescriptor from './MetawebOutboundRelationshipsDescriptor.json'
import bsDescriptor from '@/modules/managers/businessModelCatalogue/metaweb/businessModel/MetawebBusinessModelDescriptor.json'
import TableAssociator from '@/modules/managers/businessModelCatalogue/metaweb/businessModel/tableAssociator/MetawebTableAssociator.vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'

const { generate, applyPatch } = require('fast-json-patch')

export default defineComponent({
    name: 'metaweb-attributes-tab',
    components: { TableAssociator, DataTable, Column, Dialog, Dropdown, KnValidationMessages },
    props: { selectedBusinessModel: { type: Object as PropType<iBusinessModel | null>, required: true }, propMeta: { type: Object, required: true }, observer: { type: Object, required: true } },
    emits: ['loading'],
    computed: {
        leftHasLinks(): boolean {
            var x = 0
            this.simpleRight.forEach((item) => {
                if (item.links.length > 0) x += 1
            })
            return x > 0 ? false : true
        },
        buttonDisabled(): boolean {
            if (this.v$.$invalid || this.leftHasLinks) {
                return true
            } else return false
        }
    },
    data() {
        return {
            v$: useValidate() as any,
            businessModel: null as iBusinessModel | null,
            meta: null as any,
            inboundRelationships: [] as any,
            orDescriptor,
            bsDescriptor,
            inboundDialogVisible: false,
            sourceBusinessClassOptions: [] as any,
            dataSend: {} as any,
            simpleLeft: [] as any,
            simpleRight: [] as any,
            rightElement: null as any,
            filters: {
                global: [filterDefault]
            } as Object
        }
    },
    watch: {
        selectedBusinessModel: {
            handler() {
                this.loadData()
            },
            deep: true
        }
    },
    created() {
        this.loadData()
    },
    validations() {
        const outboundRequired = (value) => {
            return !this.inboundDialogVisible || value
        }
        const customValidators: ICustomValidatorMap = {
            'outbound-dialog-required': outboundRequired
        }
        const validationObject = {
            dataSend: createValidations('dataSend', orDescriptor.validations.dataSend, customValidators)
        }
        return validationObject
    },
    methods: {
        loadData() {
            this.meta = this.propMeta as any
            this.businessModel = this.selectedBusinessModel as iBusinessModel
            this.simpleLeft = this.tableToSimpleBound(this.businessModel)
            this.populateInboundRelationships()
            this.populateSourceBusinessClassOptions()
        },
        populateInboundRelationships() {
            this.inboundRelationships = this.selectedBusinessModel?.relationships.filter((relationship) => this.selectedBusinessModel?.uniqueName === relationship.sourceTableName)
        },
        populateSourceBusinessClassOptions() {
            this.sourceBusinessClassOptions = []
            this.propMeta.businessModels.forEach((el) => this.sourceBusinessClassOptions.push(el))
            this.propMeta.businessViews.forEach((el) => this.sourceBusinessClassOptions.push(el))
        },
        createColumnString(data) {
            var ret = [] as any
            data.forEach((entry) => {
                ret.push(entry.name)
            }, this)
            return ret.join(', ')
        },
        closeDialog() {
            this.dataSend = {}
            this.rightElement = null
            this.simpleRight = []
            this.simpleLeft = this.tableToSimpleBound(this.businessModel)
            this.inboundDialogVisible = false
        },
        alterTableToSimpleBound(item) {
            this.simpleRight = this.tableToSimpleBound(item)
        },
        tableToSimpleBound(model) {
            var a = [] as any
            if (model) {
                if (model.columns)
                    model.columns.forEach(function(item) {
                        // eslint-disable-next-line no-prototype-builtins
                        if (!item.hasOwnProperty('referencedColumns')) {
                            a.push({ name: item.name, uname: item.uniqueName, links: [] })
                        }
                    })
            }
            return a
        },
        async createOutbound() {
            this.dataSend.sourceColumns = []
            this.dataSend.destinationColumns = []
            this.dataSend.sourceTableName = this.businessModel?.uniqueName
            this.dataSend.destinationTableName = this.rightElement.uniqueName
            this.simpleRight.forEach((entry) => {
                if (entry.links.length > 0) {
                    this.dataSend.destinationColumns.push(entry.uname)
                    this.dataSend.sourceColumns.push(entry.links[0].uname)
                }
            })
            const postData = { data: this.dataSend, diff: generate(this.observer) }
            await this.$http
                .post(process.env.VUE_APP_META_API_URL + `/1.0/metaWeb/addBusinessRelation`, postData)
                .then((response: AxiosResponse<any>) => {
                    this.meta = applyPatch(this.meta, response.data)
                    this.closeDialog()
                    this.populateInboundRelationships()
                })
                .catch(() => {})
                .finally(() => generate(this.observer))
        },
        async deleteOutbound(item) {
            const postData = { data: item, diff: generate(this.observer) }
            await this.$http
                .post(process.env.VUE_APP_META_API_URL + `/1.0/metaWeb/deleteBusinessRelation`, postData)
                .then((response: AxiosResponse<any>) => {
                    this.meta = applyPatch(this.meta, response.data)
                    this.populateInboundRelationships()
                })
                .catch(() => {})
                .finally(() => generate(this.observer))
        }
    }
})
</script>
