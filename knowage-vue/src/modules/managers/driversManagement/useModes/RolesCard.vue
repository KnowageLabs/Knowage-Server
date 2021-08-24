<template>
    <Card style="width:100%" class="p-m-2">
        <template #content>
            <DataTable :paginator="true" :rows="10" v-model:selection="selectedMode.associatedRoles" :value="roles" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack">
                <!-- <template #header>
                    <div class="table-header">
                        <span class="p-input-icon-left">
                            <i class="pi pi-search" />
                            <InputText class="kn-material-input" type="text" v-model="filters['global'].value" :placeholder="$t('common.search')" badge="0" data-test="search-input" />
                        </span>
                    </div>
                </template> -->
                <template #empty>
                    {{ $t('common.info.noDataFound') }}
                </template>
                <template #loading>
                    {{ $t('common.info.dataLoading') }}
                </template>

                <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
                <Column field="name" header="Roles" class="kn-truncated"></Column>
            </DataTable>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
export default defineComponent({
    name: 'roles-card',
    components: { Column, DataTable },
    props: {
        roles: {
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
            selectedMode: [] as any
        }
    },
    watch: {
        selectedModeProp() {
            //this.v$.$reset()
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
