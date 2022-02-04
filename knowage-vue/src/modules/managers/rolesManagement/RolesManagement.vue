<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.rolesManagement.title') }}
                    </template>
                    <template #end>
                        <FabButton icon="fas fa-plus" @click="showForm" data-test="open-form-button" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
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
                            <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteRoleConfirm(slotProps.option.id)" data-test="delete-button" />
                        </div>
                    </template>
                </Listbox>
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-router-view">
                <router-view @touched="touched = true" @closed="touched = false" @inserted="pageReload" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iRole } from './RolesManagement'
import { AxiosResponse } from 'axios'
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
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/roles')
                .then((response: AxiosResponse<any>) => {
                    this.roles = response.data
                })
                .finally(() => (this.loading = false))
        },
        showForm(event: any) {
            const path = event.value ? `/roles-management/${event.value.id}` : '/roles-management/new-role'

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
            await this.$http
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/roles/' + roleId, { headers: { 'X-Disable-Errors': 'true' } })
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    this.$router.push('/roles-management')
                    this.loadAllRoles()
                })
                .catch((error) => {
                    if (error) {
                        this.$store.commit('setError', {
                            title: this.$t('common.toast.deleteTitle'),
                            msg: this.$t('common.error.deleting')
                        })
                    }
                })
        },
        pageReload() {
            this.touched = false
            this.loadAllRoles()
        }
    }
})
</script>
