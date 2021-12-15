<template>
    <div class="card" style="height: calc(100vh - 165px)">
        <DataTable
            :value="foreignKeys"
            class="p-datatable-sm kn-table"
            dataKey="id"
            v-model:filters="filters"
            :globalFilterFields="metawebForeignKeyTabDescriptor.globalFilterFields"
            responsiveLayout="stack"
            breakpoint="600px"
            :scrollable="true"
            :scrollHeight="metawebForeignKeyTabDescriptor.scrollHeight"
        >
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <template #header>
                <div class="table-header p-d-flex p-ai-center">
                    <span id="search-container" class="p-input-icon-left p-mr-3">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" v-model="filters['global'].value" :placeholder="$t('common.search')" />
                    </span>
                </div>
            </template>

            <Column class="kn-truncated" field="name" :header="$t('common.name')" key="name" :sortable="true">
                <template #body="slotProps">
                    <span v-tooltip.top="slotProps.data.name">{{ slotProps.data.name }}</span>
                </template>
            </Column>
            <Column class="kn-truncated" :header="$t('metaweb.physicalModel.sourceColumns')" :sortable="true">
                <template #body="slotProps">
                    <span v-tooltip.top="getColumns(slotProps.data, 'sourceColumns')">{{ getColumns(slotProps.data, 'sourceColumns') }}</span>
                </template>
            </Column>
            <Column class="kn-truncated" :header="$t('metaweb.physicalModel.targetColumns')" :sortable="true">
                <template #body="slotProps">
                    <span v-tooltip.top="getColumns(slotProps.data, 'destinationColumns')">{{ getColumns(slotProps.data, 'destinationColumns') }}</span>
                </template>
            </Column>
        </DataTable>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { iForeignKey } from '../../Metaweb'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import metawebForeignKeyTabDescriptor from './MetawebForeignKeyTabDescriptor.json'

export default defineComponent({
    name: 'metaweb-foreign-key-tab',
    components: { Column, DataTable },
    props: { propForeignKeys: { type: Array } },
    data() {
        return {
            metawebForeignKeyTabDescriptor,
            foreignKeys: [] as iForeignKey[],
            filters: { global: [filterDefault] } as Object
        }
    },
    watch: {
        propForeignKeys() {
            this.loadForeignKeys()
        }
    },
    created() {
        this.loadForeignKeys()
    },
    methods: {
        loadForeignKeys() {
            this.foreignKeys = this.propForeignKeys as iForeignKey[]
        },
        getColumns(foreignKey: iForeignKey, columnType: string) {
            const columns = [] as string[]
            for (let i = 0; i < foreignKey[columnType].length; i++) {
                columns.push(foreignKey[columnType][i].tableName + '.' + foreignKey[columnType][i].name)
            }

            return columns.join(',')
        }
    }
})
</script>
