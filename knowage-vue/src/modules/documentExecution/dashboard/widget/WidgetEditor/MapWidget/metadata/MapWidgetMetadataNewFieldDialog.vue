<template>
    <Dialog class="kn-dialog--toolbar--primary" :visible="visible" :header="$t('dashboard.widgetEditor.map.metadata.addField')" :style="descriptor.style.dialog" :closable="false" modal :breakpoints="{ '960px': '75vw', '640px': '100vw' }">
        <DataTable
            id="fields-datatable"
            v-model:selection="selectedFields"
            v-model:filters="filters"
            class="p-datatable-sm kn-table kn-page-content"
            :global-filter-fields="descriptor.globalFilterFields"
            data-key="name"
            :value="availableFields"
            :rows="20"
            :paginator="true"
            breakpoint="960px"
            responsive-layout="stack"
        >
            <template #header>
                <div class="table-header p-d-flex p-ai-center">
                    <span id="search-container" class="p-input-icon-left p-mr-3">
                        <i class="pi pi-search" />
                        <InputText v-model="filters['global'].value" class="kn-material-input" :placeholder="$t('common.search')" />
                    </span>
                </div>
            </template>
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <Column selection-mode="multiple" />
            <Column v-for="col of descriptor.columns" :key="col.field" :field="col.field" :header="$t(col.header)" style="col.style" :sortable="true" class="kn-truncated">
                <template #body="slotProps">
                    <span :title="slotProps.data[col.field]">{{ slotProps.data[col.field] }}</span>
                </template>
            </Column>
        </DataTable>
        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="addSelectedFields">{{ $t('common.add') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidgetMapLayerColumn } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import descriptor from './MapWidgetMetadataDescriptor.json'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'map-widget-metadata-new-field-dialog',
    components: { Column, DataTable, Dialog },
    props: { visible: { required: true, type: Boolean }, propFields: { required: true, type: Array as PropType<IWidgetMapLayerColumn[]> } },
    emits: ['close', 'addSelectedFields'],
    data() {
        return {
            descriptor,
            availableFields: [] as IWidgetMapLayerColumn[],
            selectedFields: [] as IWidgetMapLayerColumn[],
            filters: { global: [filterDefault] } as any
        }
    },
    watch: {
        propFields() {
            this.loadAvailableFields()
        }
    },
    created() {
        this.loadAvailableFields()
    },

    methods: {
        loadAvailableFields() {
            this.availableFields = this.propFields ? this.propFields.filter((field: IWidgetMapLayerColumn) => field.deleted) : []
        },
        addSelectedFields() {
            this.$emit('addSelectedFields', deepcopy(this.selectedFields))
            this.selectedFields = []
        },
        closeDialog() {
            this.selectedFields = []
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss">
#fields-datatable .p-datatable-wrapper {
    height: auto;
}
</style>
