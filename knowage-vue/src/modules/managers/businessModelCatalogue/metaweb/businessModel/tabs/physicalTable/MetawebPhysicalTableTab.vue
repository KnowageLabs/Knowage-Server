<template>
    <div>
        <DataTable v-if="businessModel" class="p-datatable-sm kn-table p-m-2" :value="physicalTables" v-model:filters="filters" :globalFilterFields="metawebPhysicalTableTabDescriptor.globalFilterFields" :loading="loading" editMode="cell" responsiveLayout="stack" breakpoint="960px">
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
                    <Button class="kn-button kn-button--primary p-button-link p-jc-center" @click="openAddPhysicalTableDialog"> {{ $t('common.add') }}</Button>
                </template>

                <template #body="slotProps">
                    <div class="p-d-flex p-flex-row p-jc-end">
                        <Button icon="pi pi-trash" class="p-button-link" v-tooltip.top="$t('common.delete')" @click="deletePhysicalTableConfirm(slotProps.data)" />
                    </div>
                </template>
            </Column>
        </DataTable>

        <MetawebAddPhysicalTableDialog :visible="addTableDialogVisible" :physicalTables="availablePhysicalTables" :propLoading="loading" @close="addTableDialogVisible = false" @save="addNewPhysicalTables"></MetawebAddPhysicalTableDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { iBusinessModel } from '../../../Metaweb'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import metawebPhysicalTableTabDescriptor from './MetawebPhysicalTableTabDescriptor.json'
import MetawebAddPhysicalTableDialog from './metawebAddPhysicalTableDialog/MetawebAddPhysicalTableDialog.vue'

const { generate, applyPatch } = require('fast-json-patch')

export default defineComponent({
    name: 'metaweb-physical-table-tab',
    props: { selectedBusinessModel: { type: Object as PropType<iBusinessModel | null> }, propMeta: { type: Object }, observer: { type: Object } },
    components: { Column, DataTable, MetawebAddPhysicalTableDialog },
    data() {
        return {
            metawebPhysicalTableTabDescriptor,
            meta: null as any,
            businessModel: null as iBusinessModel | null,
            physicalTables: [] as any,
            filters: {
                global: [filterDefault]
            } as Object,
            availablePhysicalTables: [] as any[],
            addTableDialogVisible: false,
            loading: false
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
    methods: {
        loadData() {
            this.loadMeta()
            this.loadBusinessModel()
        },
        loadMeta() {
            this.meta = this.propMeta as any
        },
        loadBusinessModel() {
            this.businessModel = this.selectedBusinessModel as iBusinessModel
            this.loadPhysicalTables()
        },
        loadPhysicalTables() {
            this.physicalTables = []
            if (this.businessModel) {
                this.businessModel.physicalTables?.forEach((el: any) => this.physicalTables.push(this.meta.physicalModels[el.physicalTableIndex]))
            }
        },
        openAddPhysicalTableDialog() {
            this.availablePhysicalTables = [...this.meta.physicalModels]
            const indexesToRemove = this.businessModel?.physicalTables?.map((el: any) => el.physicalTableIndex).sort()

            if (indexesToRemove) {
                for (let i = indexesToRemove.length - 1; i >= 0; i--) {
                    this.availablePhysicalTables.splice(indexesToRemove[i], 1)
                }
            }

            this.addTableDialogVisible = true
        },
        deletePhysicalTableConfirm(physicalTable: any) {
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
                .post(import.meta.env.VUE_APP_META_API_URL + `/1.0/metaWeb/deletePhysicalColumnfromBusinessView`, postData)
                .then((response: AxiosResponse<any>) => {
                    this.meta = applyPatch(this.meta, response.data).newDocument
                    this.loadData()

                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    generate(this.observer)
                })
                .catch(() => {})
            this.loading = false
        },
        async addNewPhysicalTables(selectedTables: any[]) {
            this.loading = true
            const postData = { data: { viewUniqueName: this.businessModel?.uniqueName, physicalTables: selectedTables.map((el: any) => el.name) }, diff: generate(this.observer) }
            await this.$http
                .post(import.meta.env.VUE_APP_META_API_URL + `/1.0/metaWeb/addPhysicalColumnToBusinessView`, postData)
                .then((response: AxiosResponse<any>) => {
                    this.meta = applyPatch(this.meta, response.data).newDocument
                    this.loadData()

                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.toast.createSuccess')
                    })
                    this.addTableDialogVisible = false
                    generate(this.observer)
                })
                .catch(() => {})
            this.loading = false
        }
    }
})
</script>
