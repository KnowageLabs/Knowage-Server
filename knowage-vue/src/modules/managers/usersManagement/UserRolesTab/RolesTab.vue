<template>
    <div class="p-grid   p-fluid p-jc-center kn-height-full">
        <div class="p-col-12">
            <Card>
                <template #header>
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #left>
                            {{ $t('managers.usersManagement.roles').toUpperCase() }}
                        </template>
                    </Toolbar>
                </template>
                <template #content>
                    <div v-if="selectedRoles.length > 1">
                        <div class="p-inputgroup">
                            <span class="p-float-label">
                                <Dropdown v-model="defaultRole" :options="selectedRoles" @change="onSelectDefaultRole($event)" optionLabel="name" optionValue="id" class="p-inputtext p-component kn-material-input" />
                                <label for="defaultRole"> {{ $t('managers.usersManagement.form.defaultRole') }}</label>
                            </span>
                        </div>
                    </div>
                    <p>
                        {{ $t('managers.usersManagement.defaultRoleInfo') }}
                    </p>
                    <DataTable :value="rolesList" v-model:selection="selectedRoles" class="p-datatable-sm kn-table" dataKey="id" :paginator="true" :rows="20" responsiveLayout="stack" breakpoint="960px" @rowSelect="onRowSelect" @rowUnselect="onRowUnselect">
                        <template #empty>
                            {{ $t('common.info.noDataFound') }}
                        </template>
                        <Column selectionMode="multiple" dataKey="id"></Column>
                        <Column field="name" :header="$t('common.name')"></Column>
                    </DataTable>
                </template>
            </Card>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import rolesTabDescriptor from './RolesTabDescriptor.json'
import { iRole } from '../UsersManagement'

export default defineComponent({
    name: 'roles-tab',
    components: {
        Card,
        Column,
        DataTable,
        Dropdown
    },
    props: {
        defRole: Number,
        rolesList: Array,
        selected: Array
    },
    emits: ['changed', 'setDefaultRole'],
    data() {
        return {
            defaultRole: null,
            rolesTabDescriptor,
            selectedRoles: [] as iRole[]
        }
    },
    watch: {
        selected: {
            handler: function(selected: iRole[]) {
                this.selectedRoles = selected
            }
        },
        defRole: {
            handler: function(defRole) {
                this.defaultRole = defRole
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
        onSelectDefaultRole() {
            this.$emit('setDefaultRole', this.defaultRole)
        }
    }
})
</script>
