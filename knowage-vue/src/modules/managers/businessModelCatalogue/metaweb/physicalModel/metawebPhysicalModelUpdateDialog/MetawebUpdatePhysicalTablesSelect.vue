<template>
    <div v-if="data">
        <div class="p-m-2 p-my-4">
            <label class="kn-material-input-label">{{ data.missingTables.length > 0 ? $t('metaweb.updatePhysicalModel.newTables') : $t('metaweb.updatePhysicalModel.noNewTables') }}</label>

            <DataTable
                :value="rows"
                class="p-datatable-sm kn-table"
                v-model:selection="selectedTables"
                dataKey="value"
                v-model:filters="filters"
                :globalFilterFields="metawebPhysicalModelUpdateDialogDescriptor.globalFilterFields"
                @row-select="onTablesSelect"
                @row-unselect="onTablesSelect"
                @row-select-all="onTablesSelect"
                @row-unselect-all="onTablesSelect"
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
                <Column selectionMode="multiple" :style="metawebPhysicalModelUpdateDialogDescriptor.selectColumnStyle" />
                <Column field="value" :header="data.missingTables.length > 0 ? $t('metaweb.updatePhysicalModel.newTables') : $t('metaweb.updatePhysicalModel.noNewTables')" />
            </DataTable>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iChangedData } from '../../Metaweb'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import metawebPhysicalModelUpdateDialogDescriptor from './MetawebPhysicalModelUpdateDialogDescriptor.json'

export default defineComponent({
    name: 'metaweb-update-changed-lists',
    components: { Column, DataTable },
    props: { changedItem: { type: Object as PropType<iChangedData | null> } },
    emits: ['selected'],
    data() {
        return {
            metawebPhysicalModelUpdateDialogDescriptor,
            data: null as iChangedData | null,
            rows: [] as { value: string }[],
            selectedTables: [] as string[],
            filters: {
                global: [filterDefault]
            } as Object
        }
    },
    watch: {
        changedItem() {
            this.loadData()
        }
    },
    async created() {
        this.loadData()
    },
    methods: {
        loadData() {
            this.data = this.changedItem as iChangedData
            this.loadRows()
        },
        loadRows() {
            this.rows = []
            this.data?.missingTables?.forEach((el: any) => this.rows.push({ value: el }))
        },
        onTablesSelect() {
            this.$emit('selected', this.selectedTables)
        }
    }
})
</script>

<style lang="scss">
.metaweb-missing-tables-list {
    border: none;
}

.metaweb-missing-tables-list .p-listbox-item {
    border-bottom: 1px solid #c2c2c2 !important;
}
</style>
