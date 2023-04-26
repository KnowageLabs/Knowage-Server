<template>
    <div class="p-d-flex p-flex-column">
        <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12 kn-flex-0">
            <template #start>{{ $t('documentExecution.olap.customView.title') }} </template>

            <template #end>
                <Button id="document-execution-schedulations-close-button" class="kn-button kn-button--primary" @click="close"> {{ $t('common.close') }}</Button>
            </template>
        </Toolbar>

        <DataTable
            id="olap-custom-views-table"
            v-model:filters="filters"
            :value="customViews"
            class="p-datatable-sm kn-table"
            data-key="id"
            :global-filter-fields="olapCustomViewTableDescriptor.globalFilterFields"
            :paginator="customViews.length > 20"
            :rows="20"
            :loading="loading"
            responsive-layout="stack"
            breakpoint="600px"
        >
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <template #header>
                <div class="table-header p-d-flex p-ai-center">
                    <span id="search-container" class="p-input-icon-left p-mr-3">
                        <i class="pi pi-search" />
                        <InputText v-model="filters['global'].value" class="kn-material-input" :placeholder="$t('common.search')" />
                    </span>
                </div>
            </template>

            <Column v-for="(column, index) in olapCustomViewTableDescriptor.columns" :key="index" class="kn-truncated" :field="column.field" :header="$t(column.label)" :sortable="true"></Column>
            <Column :style="olapCustomViewTableDescriptor.iconColumn.style">
                <template #body="slotProps">
                    <Button v-tooltip.top="$t('common.apply')" icon="pi pi-file" class="p-button-link" @click="applyCustomView(slotProps.data)" />
                    <Button v-tooltip.top="$t('common.delete')" icon="pi pi-trash" class="p-button-link" @click="deleteCustomViewsConfirm(slotProps.data)" />
                </template>
            </Column>
        </DataTable>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iOlapCustomView } from '../Olap'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import olapCustomViewTableDescriptor from './OlapCustomViewTableDescriptor.json'
import mainStore from '../../../../App.store'

export default defineComponent({
    name: 'olap-custom-view-table',
    components: { Column, DataTable },
    props: { olapCustomViews: { type: Object as PropType<iOlapCustomView[]> } },
    emits: ['close', 'applyCustomView'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            olapCustomViewTableDescriptor,
            customViews: [] as iOlapCustomView[],
            filters: { global: [filterDefault] } as Object,
            loading: false
        }
    },
    watch: {
        olapCustomViews() {
            this.loadCustomViews()
        }
    },
    created() {
        this.loadCustomViews()
    },
    methods: {
        loadCustomViews() {
            this.customViews = this.olapCustomViews as iOlapCustomView[]
        },
        applyCustomView(customView: iOlapCustomView) {
            this.$emit('applyCustomView', { subobj_id: customView.id, subobj_name: customView.name, subobj_description: customView.description, subobj_visibility: customView.isPublic })
        },
        deleteCustomViewsConfirm(customView: iOlapCustomView) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: async () => this.deleteCustomView(customView)
            })
        },
        async deleteCustomView(customView: iOlapCustomView) {
            this.loading = true
            await this.$http
                .delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `/1.0/olapsubobjects/removeOlapSubObject?idObj=${customView.id}`)
                .then(() => {
                    this.removeCustomView(customView)
                    this.store.setInfo({
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                })
                .catch(() => {})
            this.loading = false
        },
        removeCustomView(customView: iOlapCustomView) {
            const index = this.customViews.findIndex((el: iOlapCustomView) => el.id === customView.id)
            if (index !== -1) this.customViews.splice(index, 1)
        },
        close() {
            this.$emit('close')
            this.customViews = []
        }
    }
})
</script>
