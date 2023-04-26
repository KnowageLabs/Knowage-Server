<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.tenantManagement.title') }}
                    </template>
                    <template #end>
                        <FabButton icon="fas fa-plus" data-test="open-form-button" @click="showForm" />
                    </template>
                </Toolbar>
                <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />
                <Listbox
                    v-if="!loading"
                    class="kn-list--column"
                    :options="multitenants"
                    :filter="true"
                    :filter-placeholder="$t('common.search')"
                    option-label="name"
                    filter-match-mode="contains"
                    :filter-fields="tenantsDescriptor.filterFields"
                    :empty-filter-message="$t('common.info.noDataFound')"
                    data-test="tenants-list"
                    @change="showForm"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" data-test="list-item">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.MULTITENANT_NAME }}</span>
                                <span class="kn-list-item-text-secondary">{{ slotProps.option.MULTITENANT_THEME }}</span>
                            </div>
                            <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" data-test="delete-button" @click.stop="deleteTenantConfirm(slotProps.option)" />
                        </div>
                    </template>
                </Listbox>
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <router-view :selected-tenant="selTenant" :licenses="listOfavailableLicenses" @touched="touched = true" @closed="onFormClose" @inserted="pageReload" />
                <KnHint v-if="hintVisible" :title="'managers.tenantManagement.hintTitle'" :hint="'managers.tenantManagement.hint'" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iMultitenant } from './TenantManagement'
import { AxiosResponse } from 'axios'
import tenantsDescriptor from './TenantManagementDescriptor.json'
import FabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import KnHint from '@/components/UI/KnHint.vue'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'tenant-management',
    components: {
        FabButton,
        Listbox,
        KnHint
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            multitenants: [] as iMultitenant[],
            selTenant: {} as iMultitenant,
            listOfThemes: [] as any,
            listOfDataSources: [] as any,
            listOfProductTypes: [] as any,
            listOfavailableLicenses: [] as any,
            tenantsDescriptor,
            loading: false,
            touched: false,
            hintVisible: true
        }
    },
    async created() {
        await this.loadTenants()
        await this.getLicences()
    },
    methods: {
        loadData(dataType: string) {
            return this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `multitenant${dataType}`).finally(() => (this.loading = false))
        },
        async getLicences() {
            return this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/license`)
                .then((response: AxiosResponse<any>) => {
                    const host = response.data.hosts[0].hostName
                    const licenses = response.data.licenses[host]
                    this.listOfavailableLicenses = licenses
                })
                .finally(() => (this.loading = false))
        },
        async loadTenants() {
            this.loading = true
            await this.loadData('').then((response: AxiosResponse<any>) => {
                this.multitenants = response.data.root
            })
            this.loading = false
        },
        deleteTenantConfirm(selectedTenant: Object) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteTenant(selectedTenant)
            })
        },
        async deleteTenant(selectedTenant: Object) {
            const url = import.meta.env.VITE_RESTFUL_SERVICES_PATH + 'multitenant'
            await this.$http.delete(url, { data: selectedTenant }).then(() => {
                this.store.setInfo({
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.$router.push('/tenants-management')
                this.pageReload()
            })
        },
        showForm(event: any) {
            const path = event.value ? `/tenants-management/${event.value.MULTITENANT_ID}` : '/tenants-management/new-tenant'
            this.hintVisible = false

            if (!this.touched) {
                this.$router.push(path)
                this.selTenant = event.value
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.$router.push(path)
                        this.selTenant = event.value
                    }
                })
            }
        },
        pageReload() {
            this.touched = false
            this.hintVisible = true
            this.loadTenants()
        },
        onFormClose() {
            this.touched = false
            this.hintVisible = true
        }
    }
})
</script>
