<template>
    <Dialog class="remove-padding" :visible="showBusinessClassDialog" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                <template #left>
                    {{ $t('metaweb.businessModel.newBusiness') }}
                </template>
            </Toolbar>
        </template>
        <form ref="bcForm" class="p-fluid p-formgrid p-grid p-m-2">
            <div class="p-field p-col-12 p-md-6">
                <span class="p-float-label">
                    <InputText id="name" class="kn-material-input" v-model="tmpBusinessModel.name" />
                    <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} *</label>
                </span>
            </div>
            <div class="p-field p-col-12 p-md-6">
                <span class="p-float-label">
                    <InputText id="desc" class="kn-material-input" v-model="tmpBusinessModel.description" />
                    <label for="desc" class="kn-material-input-label"> {{ $t('common.description') }} *</label>
                </span>
            </div>
            <div class="p-field p-col-12">
                <span class="p-float-label ">
                    <Dropdown id="driver" class="kn-material-input" v-model="tmpBusinessModel.physicalModel" :options="physicalModels" optionLabel="name" />
                    <label for="driver" class="kn-material-input-label"> {{ $t('metaweb.businessModel.physTable') }} *</label>
                </span>
            </div>
        </form>

        <DataTable
            v-if="tmpBusinessModel.physicalModel"
            :value="tmpBusinessModel.physicalModel.columns"
            class="p-datatable-sm kn-table p-ml-1"
            style="75%"
            v-model:selection="tmpBusinessModel.selectedColumns"
            :scrollable="true"
            scrollHeight="100%"
            dataKey="position"
            v-model:filters="filters"
            :globalFilterFields="bsDescriptor.globalFilterFields"
        >
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
            <Column selectionMode="multiple" headerStyle="width: 3em" />
            <Column field="name" :header="$t('common.name')" />
        </DataTable>

        <template #footer>
            <Button class="p-button-text kn-button" :label="$t('common.cancel')" @click="onCancel" />
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import useValidate from '@vuelidate/core'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import bsDescriptor from '../MetawebBusinessModelDescriptor.json'

export default defineComponent({
    name: 'document-drivers',
    components: { Dialog, Dropdown, DataTable, Column },
    emits: ['closeDialog'],
    props: { physicalModels: Array, showBusinessClassDialog: Boolean },
    data() {
        return {
            bsDescriptor,
            v$: useValidate() as any,
            tmpBusinessModel: { physicalModel: null, selectedColumns: [], name: '', description: '' } as any,
            filters: {
                global: [filterDefault]
            } as Object
        }
    },
    watch: {
        selectedDriver() {}
    },
    created() {},
    validations() {},
    methods: {
        resetPhModel() {
            this.tmpBusinessModel.selectedColumns = []
        },
        onCancel() {
            this.$emit('closeDialog')
            this.tmpBusinessModel = { physicalModel: { columns: [] }, selectedColumns: [], name: '', description: '' } as any
        }
    }
})
</script>
