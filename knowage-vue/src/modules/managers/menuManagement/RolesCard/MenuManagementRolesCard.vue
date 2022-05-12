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
            <DataTable
                :value="rolesListFiltered"
                v-model:filters="filters"
                v-model:selection="selectedRoles"
                class="p-datatable-sm kn-table"
                dataKey="id"
                responsiveLayout="stack"
                breakpoint="960px"
                @rowSelect="onRowSelect"
                @rowUnselect="onRowUnselect"
                @rowSelectAll="onAllRowSelectionChange"
                @rowUnselectAll="onAllRowSelectionChange"
            >
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
import { defineComponent, PropType } from 'vue'
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
        rolesList: {
            type: Array as PropType<iRole[]>
        },
        selected: Array as PropType<iRole[]>,
        parentNodeRoles: {
            type: Array as PropType<iRole[]>
        }
    },
    emits: ['changed'],
    data() {
        return {
            selectedRoles: [] as iRole[] | null,
            filters: {
                global: [filterDefault]
            }
        }
    },
    created() {
        if (this.selected) {
            this.selectedRoles = this.selected
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
        },
        onAllRowSelectionChange() {
            setTimeout(() => this.$emit('changed', this.selectedRoles), 0)
        }
    },
    computed: {
        rolesListFiltered(): iRole[] {
            if (!this.rolesList) return []
            if (this.parentNodeRoles) {
                return this.rolesList.filter((role) => this.parentNodeRoles && this.parentNodeRoles.findIndex((parentNodeRole) => parentNodeRole.id === role.id) >= 0)
            } else {
                return this.rolesList
            }
        }
    }
})
</script>
