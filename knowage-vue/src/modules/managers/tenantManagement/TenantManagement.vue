<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.tenantManagement.title') }}
                    </template>
                    <template #end>
                        <FabButton icon="fas fa-plus" @click="showForm" data-test="open-form-button" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <Listbox
                    v-if="!loading"
                    class="kn-list--column"
                    :options="multitenants"
                    :filter="true"
                    :filterPlaceholder="$t('common.search')"
                    optionLabel="name"
                    filterMatchMode="contains"
                    :filterFields="tenantsDescriptor.filterFields"
                    :emptyFilterMessage="$t('common.info.noDataFound')"
                    @change="showForm"
                    data-test="tenants-list"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" data-test="list-item">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.MULTITENANT_NAME }}</span>
                                <span class="kn-list-item-text-secondary">{{ slotProps.option.MULTITENANT_THEME }}</span>
                            </div>
                            <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteTenantConfirm(slotProps.option)" data-test="delete-button" />
                        </div>
                    </template>
                </Listbox>
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <router-view :selectedTenant="selTenant" :licenses="listOfavailableLicenses" @touched="touched = true" @closed="onFormClose" @inserted="pageReload" />
                <KnHint :title="'managers.tenantManagement.hintTitle'" :hint="'managers.tenantManagement.hint'" v-if="hintVisible" />
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

    export default defineComponent({
        name: 'tenant-management',
        components: {
            FabButton,
            Listbox,
            KnHint
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
                return this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `multitenant${dataType}`).finally(() => (this.loading = false))
            },
            async getLicences() {
                return this.$http
                    .get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/license`)
                    .then((response: AxiosResponse<any>) => {
                        var host = response.data.hosts[0].hostName
                        var licenses = response.data.licenses[host]
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
                let url = import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + 'multitenant'
                await this.$http.delete(url, { data: selectedTenant }).then(() => {
                    this.$store.commit('setInfo', {
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
