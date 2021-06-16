<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #left>{{ tenant.MULTITENANT_NAME }}</template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="handleSubmit" :disabled="buttonDisabled" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplateConfirm" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="card">
        <TabView class="tabview-custom" data-test="tab-view">
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.tenantManagement.detail.title') }}</span>
                </template>

                <TenantDetail :selectedTenant="tenant" :listOfThemes="listOfThemes" @fieldChanged="onFieldChange" />
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.tenantManagement.productTypes.title') }}</span>
                </template>

                <ProductTypes :title="$t('managers.tenantManagement.productTypes.title')" :dataList="listOfProductTypes" :selectedData="listOfSelectedProducts" @changed="setSelectedProducts($event)" />
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.tenantManagement.dataSource.title') }}</span>
                </template>

                <ProductTypes :title="$t('managers.tenantManagement.dataSource.title')" :dataList="listOfDataSources" :selectedData="listOfSelectedDataSources" @changed="setSelectedDataSources($event)" />
            </TabPanel>
        </TabView>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iMultitenant, iTenantToSave } from '../TenantManagement'
import axios from 'axios'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import tabViewDescriptor from './TenantManagementTabViewDescriptor.json'
import TenantDetail from './DetailTab/TenantDetail.vue'
import ProductTypes from './SelectionTableTab/SelectionTable.vue'
import useValidate from '@vuelidate/core'

export default defineComponent({
    components: {
        TabView,
        TabPanel,
        TenantDetail,
        ProductTypes
    },
    props: {
        selectedTenant: {
            type: Object,
            required: false
        },
        licenses: Array
    },
    emits: ['touched', 'closed', 'inserted'],
    data() {
        return {
            tabViewDescriptor,
            loading: false,
            touched: false,
            operation: 'insert',
            v$: useValidate() as any,
            tenant: {} as iMultitenant,
            listOfThemes: [] as any,
            availableLicenses: [] as any,
            listOfProductTypes: [] as any,
            listOfSelectedProducts: [] as any,
            listOfDataSources: [] as any,
            listOfSelectedDataSources: [] as any
        }
    },
    computed: {
        buttonDisabled(): any {
            if ((this.listOfSelectedProducts && this.listOfSelectedProducts.length === 0) || (this.listOfSelectedDataSources && this.listOfSelectedDataSources.length === 0) || this.v$.$invalid) {
                return true
            }
            return false
        }
    },
    mounted() {
        if (this.selectedTenant) {
            this.tenant = { ...this.selectedTenant } as iMultitenant
        }
        this.availableLicenses = this.licenses
        this.loadAllData()
        this.getTenantData()
    },
    watch: {
        selectedTenant() {
            this.tenant = { ...this.selectedTenant } as iMultitenant
            this.getTenantData()
        },
        licenses() {
            this.availableLicenses = this.licenses
        }
    },
    methods: {
        loadData(dataType: string) {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `multitenant${dataType}`)
        },

        async loadAllData() {
            this.loading = true
            await this.loadData('/themes').then((response) => {
                this.listOfThemes = response.data.root
            })
            await this.loadData('/datasources').then((response) => {
                this.listOfDataSources = response.data.root
            })
            await this.loadData('/producttypes').then((response) => {
                this.listOfProductTypes = response.data.root
                this.filterArrayByTargetArr(this.listOfProductTypes, this.availableLicenses)
            })
            this.loading = false
        },

        filterArrayByTargetArr(sourceArr, targetArr) {
            var newArr = sourceArr.filter((elem) => targetArr.find((target) => elem.LABEL == target.product))
            this.listOfProductTypes = newArr
        },

        async getTenantData() {
            this.loading = true
            this.listOfSelectedProducts = null
            this.listOfSelectedDataSources = null
            this.touched = false

            await this.loadData(`/producttypes?TENANT=${this.tenant.MULTITENANT_NAME}`).then((response) => {
                var productTypes = response.data.root

                this.listOfSelectedProducts = []
                this.copySelectedElement(productTypes, this.listOfSelectedProducts)
            })
            await this.loadData(`/datasources?TENANT=${this.tenant.MULTITENANT_NAME}`).then((response) => {
                var dataSources = response.data.root

                this.listOfSelectedDataSources = []
                this.copySelectedElement(dataSources, this.listOfSelectedDataSources)
            })
            this.loading = false
        },
        copySelectedElement(source, selected) {
            for (var i = 0; i < source.length; i++) {
                if (source[i].CHECKED == true) {
                    selected.push(source[i])
                }
            }
        },

        async handleSubmit() {
            if (this.v$.$invalid) {
                return
            }
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + 'multitenant/save'

            await axios.post(url, this.createTenantToSave()).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t(this.tabViewDescriptor.operation[this.operation].toastTitle),
                    msg: this.$t(this.tabViewDescriptor.operation.success)
                })
                this.$emit('inserted')
                this.$router.replace('/tenants')
            })
        },

        createTenantToSave() {
            let tenantToSave = {} as iTenantToSave
            tenantToSave.MULTITENANT_ID = this.tenant.MULTITENANT_ID ? '' + this.tenant.MULTITENANT_ID : ''
            tenantToSave.MULTITENANT_NAME = this.tenant.MULTITENANT_NAME
            tenantToSave.MULTITENANT_THEME = this.tenant.MULTITENANT_THEME
            tenantToSave.DS_LIST = this.listOfSelectedDataSources.map((dataSource) => {
                delete dataSource.CHECKED
                return dataSource
            })
            tenantToSave.PRODUCT_TYPE_LIST = this.listOfSelectedProducts.map((productType) => {
                delete productType.CHECKED
                return productType
            })
            return tenantToSave
        },

        closeTemplate() {
            this.$router.push('/tenants')
            this.$emit('closed')
        },

        onFieldChange(event) {
            this.tenant[event.fieldName] = event.value
            this.touched = true
            this.$emit('touched')
        },

        setSelectedProducts(categories: any[]) {
            this.listOfSelectedProducts = categories
            this.touched = true
            this.$emit('touched')
        },

        setSelectedDataSources(categories: any[]) {
            this.listOfSelectedDataSources = categories
            this.touched = true
            this.$emit('touched')
        },

        closeTemplateConfirm() {
            if (!this.touched) {
                this.closeTemplate()
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.closeTemplate()
                    }
                })
            }
        }
    }
})
</script>

<style lang="scss" scoped>
::v-deep(.p-toolbar-group-right) {
    height: 100%;
}
</style>
