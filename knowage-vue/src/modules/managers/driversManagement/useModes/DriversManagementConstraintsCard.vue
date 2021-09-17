<template>
    <Card style="width:100%" class="p-m-2">
        <template #content>
            <DataTable v-model:selection="selectedMode.associatedChecks" :value="constraints" class="p-datatable-sm kn-table" dataKey="checkId" responsiveLayout="stack" v-model:filters="filters" filterDisplay="menu" data-test="values-list">
                <template #header>
                    <div class="table-header">
                        <span class="p-input-icon-left">
                            <i class="pi pi-search" />
                            <InputText class="kn-material-input" type="text" v-model="filters['global'].value" :placeholder="$t('common.search')" badge="0" data-test="filter-input" />
                        </span>
                    </div>
                </template>
                <template #empty>
                    {{ $t('common.info.noDataFound') }}
                </template>
                <template #loading>
                    {{ $t('common.info.dataLoading') }}
                </template>

                <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
                <Column field="name" header="Constraints" class="kn-truncated"></Column>
            </DataTable>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { FilterOperator } from 'primevue/api'
export default defineComponent({
    name: 'constraints-card',
    components: { Column, DataTable },
    props: {
        constraints: {
            type: Array,
            required: false
        },
        selectedModeProp: {
            type: Array,
            required: false
        }
    },
    data() {
        return {
            selectedMode: [] as any,
            filters: {
                global: [filterDefault],
                name: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                }
            } as Object
        }
    },
    watch: {
        selectedModeProp() {
            this.selectedMode = this.selectedModeProp as any[]
        }
    },
    mounted() {
        if (this.selectedModeProp) {
            this.selectedMode = this.selectedModeProp as any[]
        }
    }
})
</script>
