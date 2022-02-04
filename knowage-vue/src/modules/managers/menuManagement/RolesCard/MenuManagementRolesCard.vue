<template>
    <Card class="p-mx-auto">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('managers.menuManagement.roles') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <DataTable :value="rolesList" v-model:filters="filters" v-model:selection="selectedRoles" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack" breakpoint="960px" @rowSelect="onRowSelect" @rowUnselect="onRowUnselect">
                <template #empty>
                    {{ $t('common.info.noDataFound') }}
                </template>
                <template #header>
                    <div class="table-header">
                        <span class="p-input-icon-left">
                            <i class="pi pi-search" />
                            <InputText class="kn-material-input" type="text" v-model="filters['global'].value" :placeholder="$t('common.search')" badge="0" data-test="search-input" />
                        </span>
                    </div>
                </template>
                <Column selectionMode="multiple" dataKey="id" style="width:50px"></Column>
                <Column field="name" :header="$t('common.name')"></Column>
            </DataTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { iRole } from '../MenuManagement'

export default defineComponent({
    name: 'roles-tab',
    components: {
        Card,
        Column,
        DataTable
    },
    props: {
        rolesList: Array,
        selected: Array
    },
    emits: ['changed'],
    data() {
        return {
            selectedRoles: [] as iRole[],
            filters: {
                global: [filterDefault]
            }
        }
    },
    watch: {
        selected: {
            handler: function(selected: iRole[]) {
                this.selectedRoles = selected
            }
        }
    },
    methods: {
        onRowSelect() {
            this.$emit('changed', this.selectedRoles)
        },
        onRowUnselect() {
            this.$emit('changed', this.selectedRoles)
        }
    }
})
</script>
