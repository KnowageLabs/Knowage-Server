<template>
    <div class="p-fluid p-jc-center kn-height-full">
        <div class="p-col-12">
            <Card>
                <template #header>
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #left>
                            {{ $t('managers.usersManagement.roles') }}
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
                        <Message severity="info">{{ $t('managers.usersManagement.defaultRoleInfo') }}</Message>
                    </p>
                    <DataTable
                        :value="rolesList"
                        @row-select-all="onRowSelect"
                        @row-unselect-all="onRowUnselect"
                        v-model:selection="selectedRoles"
                        class="p-datatable-sm kn-table"
                        dataKey="id"
                        :paginator="true"
                        :rows="20"
                        responsiveLayout="stack"
                        breakpoint="960px"
                        @rowSelect="onRowSelect"
                        @rowUnselect="onRowUnselect"
                    >
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
import Message from 'primevue/message'
import rolesTabDescriptor from './RolesTabDescriptor.json'
import { iRole } from '../UsersManagement'

export default defineComponent({
    name: 'roles-tab',
    components: {
        Card,
        Column,
        DataTable,
        Dropdown,
        Message
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
            console.log('selectttttttttt')
            this.$emit('changed', this.selectedRoles)
        },
        onRowUnselect() {
            console.log('unselectt')
            this.$emit('changed', this.selectedRoles)
            if (this.selectedRoles?.length <= 1) {
                this.defaultRole = null
                this.onSelectDefaultRole()
            }
        },
        onSelectDefaultRole() {
            this.$emit('setDefaultRole', this.defaultRole)
        },
        onSelectAll() {
            this.$emit('changed', this.selectedRoles)
            if (this.selectedRoles?.length <= 1) {
                this.defaultRole = null
                this.onSelectDefaultRole()
            }
        }
    }
})
</script>
