<template>
    <DataTable
        v-if="businessModel && physicalTable"
        class="p-datatable-sm kn-table p-m-2"
        :value="physicalTable.columns"
        v-model:filters="filters"
        :globalFilterFields="metawebPhysicalTableTabDescriptor.globalFilterFields"
        :loading="loading"
        editMode="cell"
        responsiveLayout="stack"
        breakpoint="960px"
    >
        <template #header>
            <div class="table-header">
                <span class="p-input-icon-left">
                    <i class="pi pi-search" />
                    <InputText class="kn-material-input" type="text" v-model="filters['global'].value" :placeholder="$t('common.search')" badge="0" data-test="search-input" />
                </span>
            </div>
        </template>
        <template #empty>
            {{ $t('common.info.noDataFound') }}
        </template>
        <Column class="kn-truncated" :header="$t('common.name')">
            <template #body="slotProps">
                <span v-tooltip.top="slotProps.data.name">{{ slotProps.data.name }}</span>
            </template>
        </Column>
        <Column :style="metawebPhysicalTableTabDescriptor.iconColumnStyle">
            <template #header>
                <Button class="kn-button kn-button--primary p-button-link" @click="openAddPhysicalTableDialog"> {{ $t('common.add') }}</Button>
            </template>

            <template #body="slotProps">
                <div class="p-d-flex p-flex-row p-jc-end">
                    <Button icon="pi pi-trash" class="p-button-link" v-tooltip.top="$t('common.delete')" @click="deletePhysicalTableConfirm(slotProps.data)" />
                </div>
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iBusinessModel } from '../../../Metaweb'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import metawebPhysicalTableTabDescriptor from './MetawebPhysicalTableTabDescriptor.json'
import { AxiosResponse } from 'axios'

const { generate, applyPatch } = require('fast-json-patch')

export default defineComponent({
    name: 'metaweb-physical-table-tab',
    props: { selectedBusinessModel: { type: Object as PropType<iBusinessModel | null> }, propMeta: { type: Object }, observer: { type: Object } },
    components: { Column, DataTable },
    data() {
        return {
            metawebPhysicalTableTabDescriptor,
            meta: null as any,
            businessModel: null as iBusinessModel | null,
            physicalTable: null as any,
            filters: {
                global: [filterDefault]
            } as Object,
            loading: false
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
            // console.log('LOADED META IN METAWEB ATTRIBUTES TAB: ', this.meta)
        },
        loadBusinessModel() {
            this.businessModel = this.selectedBusinessModel as iBusinessModel

            console.log('BUSINESS MODEL LOADED PHYSICAL TABLE: ', this.businessModel)
            console.log('BUSINESS MODEL LOADED META: ', this.meta)
            this.physicalTable = this.meta.physicalModels[this.businessModel.physicalTable.physicalTableIndex]
            console.log('LOADED PHYSICAL TABLE', this.physicalTable)
        },
        openAddPhysicalTableDialog() {
            console.log('ADD CLICKED!')
        },
        deletePhysicalTableConfirm(physicalTable: any) {
            console.log('DELETE FOR PHYSICAL TABLE: ', physicalTable)
            this.$confirm.require({
                message: this.$t('documentExecution.dossier.deleteConfirm'),
                header: this.$t('documentExecution.dossier.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: async () => await this.deletePhysicalTable(physicalTable)
            })
        },
        async deletePhysicalTable(physicalTable: any) {
            this.loading = true
            const postData = { data: { viewUniqueName: this.businessModel?.uniqueName, physicalTable: physicalTable.name } }
            await this.$http
                .post(process.env.VUE_APP_META_API_URL + `/1.0/metaWeb/deletePhysicalColumnfromBusinessView`, postData)
                .then((response: AxiosResponse<any>) => {
                    this.meta = applyPatch(this.meta, response.data)
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    generate(this.observer)
                })
                .catch(() => {})
            this.loading = false
        }
    }
})
</script>
