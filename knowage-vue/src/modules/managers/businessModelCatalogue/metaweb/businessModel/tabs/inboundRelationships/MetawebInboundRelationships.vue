<template>
    <DataTable :value="inboundRelationships" class="p-datatable-sm kn-table p-ml-2" responsiveLayout="stack" breakpoint="960px" v-model:filters="filters" :globalFilterFields="irDescriptor.globalFilterFields">
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
        <Column :header="$t('metaweb.physicalModel.sourceColumns')" :sortable="true">
            <template #body="slotProps">
                <span v-for="(entry, index) in slotProps.data.sourceColumns" :key="index"> {{ entry.name }} </span>
            </template>
        </Column>
        <Column field="destinationTableName" :header="$t('metaweb.businessModel.targetTable')" :sortable="true" />
        <Column :header="$t('metaweb.physicalModel.targetColumns')" :sortable="true">
            <template #body="slotProps">
                <span v-for="(entry, index) in slotProps.data.destinationColumns" :key="index"> {{ entry.name }} </span>
            </template>
        </Column>
        <Column :style="irDescriptor.style.iconColumnStyle" class="p-text-right">
            <template #header>
                <Button :label="$t('common.add')" class="p-button-link p-text-right" @click="inboundDialogVisible = true" />
            </template>
            <template #body>
                <Button icon="pi pi-trash" class="p-button-text p-button-rounded p-button-plain" />
            </template>
        </Column>
    </DataTable>

    <Dialog class="bsdialog" :style="bsDescriptor.style.bsDialog" :visible="inboundDialogVisible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                <template #left>
                    {{ $t('metaweb.businessModel.tabView.inbound') }}
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
                        :options="irDescriptor.cardinality"
                        :class="{ 'p-invalid': v$.dataSend.cardinality.$invalid && v$.dataSend.cardinality.$dirty }"
                        @blur="v$.dataSend.cardinality.$touch()"
                    />
                    <label for="source" class="kn-material-input-label"> {{ $t('kpi.kpiDefinition.cardinalityTtitle') }} *</label>
                </span>
                <KnValidationMessages class="p-mt-1" :vComp="v$.dataSend.cardinality" :additionalTranslateParams="{ fieldName: $t('kpi.kpiDefinition.cardinalityTtitle') }" />
            </div>
            <div class="p-field p-col-12 p-md-6">
                <span class="p-float-label ">
                    <Dropdown id="source" class="kn-material-input" v-model="rightElement" :options="sourceBusinessClassOptions" optionLabel="name" @change="alterTableToSimpleBound($event.value)" />
                    <label for="source" class="kn-material-input-label"> {{ $t('metaweb.businessModel.inbound.sourceBc') }} </label>
                </span>
            </div>
            <div class="p-field p-col-12 p-md-6">
                <span class="p-float-label">
                    <InputText id="target" class="kn-material-input" v-model="businessModel.name" :disabled="true" />
                    <label for="target" class="kn-material-input-label"> {{ $t('metaweb.businessModel.inbound.targetBc') }} </label>
                </span>
            </div>
        </form>

        <TableAssociator class="kn-flex" :sourceArray="simpleRight" :targetArray="simpleLeft" :useMultipleTablesFromSameSource="false" @drop="onDrop" @relationshipDeleted="onDelete" />

        <template #footer>
            <Button class="p-button-text kn-button" :label="$t('common.cancel')" @click="onCancel" />
            <Button class="kn-button kn-button--primary" :label="$t('common.create')" :disabled="buttonDisabled" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import { defineComponent, PropType } from 'vue'
import { iBusinessModel } from '@/modules/managers/businessModelCatalogue/metaweb/Metaweb'
import { filterDefault } from '@/helpers/commons/filterHelper'
import useValidate from '@vuelidate/core'
import irDescriptor from './MetawebInboundRelationshipsDescriptor.json'
import bsDescriptor from '@/modules/managers/businessModelCatalogue/metaweb/businessModel/MetawebBusinessModelDescriptor.json'
import TableAssociator from '@/modules/managers/businessModelCatalogue/metaweb/businessModel/tableAssociator/MetawebTableAssociator.vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'

export default defineComponent({
    name: 'metaweb-attributes-tab',
    components: { TableAssociator, DataTable, Column, Dialog, Dropdown, KnValidationMessages },
    props: { selectedBusinessModel: { type: Object as PropType<iBusinessModel | null>, required: true }, businessModels: { type: Array, required: true }, businessViews: { type: Array, required: true } },
    emits: ['loading'],
    computed: {
        leftHasLinks(): boolean {
            var x = 0
            this.simpleLeft.forEach((item) => {
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
            inboundRelationships: [] as any,
            irDescriptor,
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
        selectedBusinessModel() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
    },
    validations() {
        const inboundRequired = (value) => {
            return !this.inboundDialogVisible || value
        }
        const customValidators: ICustomValidatorMap = {
            'inbound-dialog-required': inboundRequired
        }
        const validationObject = {
            dataSend: createValidations('dataSend', irDescriptor.validations.dataSend, customValidators)
        }
        return validationObject
    },
    methods: {
        loadData() {
            this.businessModel = this.selectedBusinessModel as iBusinessModel
            this.simpleLeft = this.tableToSimpleBound(this.businessModel)
            this.populateInboundRelationships()
            this.populateSourceBusinessClassOptions()
        },
        populateInboundRelationships() {
            this.inboundRelationships = this.selectedBusinessModel?.relationships.filter((relationship) => this.selectedBusinessModel?.uniqueName != relationship.sourceTableName)
        },
        populateSourceBusinessClassOptions() {
            this.businessModels.forEach((el) => this.sourceBusinessClassOptions.push(el))
            this.businessViews.forEach((el) => this.sourceBusinessClassOptions.push(el))
            console.log('populating...', this.sourceBusinessClassOptions)
        },
        onCancel() {
            this.inboundDialogVisible = false
            this.dataSend = {}
            this.simpleLeft = this.tableToSimpleBound(this.businessModel)
        },
        alterTableToSimpleBound(item) {
            this.simpleRight = this.tableToSimpleBound(item)
        },
        tableToSimpleBound(model) {
            var a = [] as any
            if (model) {
                if (model.columns)
                    model.columns.forEach(function(item) {
                        //add only the column ( not calculated field)
                        // eslint-disable-next-line no-prototype-builtins
                        if (!item.hasOwnProperty('referencedColumns')) {
                            a.push({ name: item.name, uname: item.uniqueName, links: [] })
                        }
                    })
            }
            return a
        },
        createInbound() {
            this.dataSend.sourceColumns = []
            this.dataSend.destinationColumns = []
            this.dataSend.sourceTableName = this.rightElement.uniqueName
            this.dataSend.destinationTableName = this.businessModel?.uniqueName
            this.simpleLeft.forEach((entry) => {
                if (entry.links.length > 0) {
                    this.dataSend.destinationColumns.push(entry.uname)
                    this.dataSend.sourceColumns.push(entry.links[0].uname)
                }
            })

            //dalje ide servis logika
        }
    }
})
</script>
