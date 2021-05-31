<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('managers.rolesManagement.title') }}
                    </template>
                    <template #right>
                        <FabButton icon="fas fa-plus" @click="showForm" data-test="open-form-button" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <div class="p-col">
                    <Listbox
                        v-if="!loading"
                        class="kn-list--column"
                        :options="roles"
                        :filter="true"
                        :filterPlaceholder="$t('common.search')"
                        optionLabel="name"
                        filterMatchMode="contains"
                        :filterFields="rolesDecriptor.filterFields"
                        :emptyFilterMessage="$t('managers.rolesManagement.noResults')"
                        @change="showForm"
                        data-test="roles-list"
                    >
                        <template #empty>{{ $t('common.info.noDataFound') }}</template>
                        <template #option="slotProps">
                            <div class="kn-list-item" data-test="list-item">
                                <div class="kn-list-item-text">
                                    <span>{{ slotProps.option.name }}</span>
                                    <span class="kn-list-item-text-secondary">{{ slotProps.option.roleTypeCD }}</span>
                                </div>
                                <Button icon="far fa-trash-alt" class="p-button-link p-button-sm" @click.stop="deleteRoleConfirm(slotProps.option.id)" data-test="delete-button" />
                            </div>
                        </template>
                    </Listbox>
                </div>
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <router-view @touched="touched = true" @closed="touched = false" @inserted="pageReload" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iRole } from './RolesManagement'
import axios from 'axios'
import rolesDecriptor from './RolesManagementDescriptor.json'
import FabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'roles-management',
    components: {
        FabButton,
        Listbox
    },
    data() {
        return {
            roles: [] as iRole[],
            loading: false,
            touched: false,
            rolesDecriptor: rolesDecriptor,
            hiddenForm: false,
            dirty: false
        }
    },
    async created() {
        await this.loadAllRoles()
    },
    methods: {
        async loadAllRoles() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/roles')
                .then((response) => {
                    this.roles = response.data
                })
                .finally(() => (this.loading = false))
        },
        showForm(event: any) {
            const path = event.value ? `/roles/${event.value.id}` : '/roles/new-role'

            if (!this.touched) {
                this.$router.push(path)
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.$router.push(path)
                    }
                })
            }
        },
        deleteRoleConfirm(roleId: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteRole(roleId)
            })
        },
        async deleteRole(roleId: number) {
            await axios.delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/roles/' + roleId).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.$router.push('/roles')
                this.loadAllRoles()
            })
        },
        pageReload() {
            this.touched = false
            this.loadAllRoles()
        }
    }
})
</script>
