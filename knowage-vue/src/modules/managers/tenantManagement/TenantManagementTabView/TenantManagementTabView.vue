<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #left>{{ tenant.MULTITENANT_NAME }}</template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="handleSubmit" :disabled="buttonDisabled" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="card">
        <TabView class="tabview-custom" data-test="tab-view">
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.tenantManagement.detail.title') }}</span>
                </template>
                {{ tenant }}
                <TenantDetail :selectedTenant="tenant" @fieldChanged="onFieldChange" />
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.tenantManagement.productTypes.title') }}</span>
                </template>
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.tenantManagement.dataSource.title') }}</span>
                </template>
            </TabPanel>
        </TabView>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iMultitenant } from '../TenantManagement'
// import axios from 'axios'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import tabViewDescriptor from './TenantManagementTabViewDescriptor.json'
import TenantDetail from './DetailTab/TenantDetail.vue'
import useValidate from '@vuelidate/core'

export default defineComponent({
    components: {
        TabView,
        TabPanel,
        TenantDetail
    },
    props: {
        selectedTenant: {
            type: Object,
            required: false
        }
    },
    emits: ['touched', 'closed', 'inserted'],
    data() {
        return {
            tabViewDescriptor,
            loading: false,
            operation: 'insert',
            v$: useValidate() as any,
            tenant: {} as iMultitenant
        }
    },
    computed: {
        buttonDisabled(): any {
            return this.v$.$invalid
        }
    },
    mounted() {
        if (this.selectedTenant) {
            this.tenant = { ...this.selectedTenant } as iMultitenant
        }
    },
    watch: {
        selectedTenant() {
            this.tenant = { ...this.selectedTenant } as iMultitenant
        }
    },
    methods: {
        closeTemplate() {
            this.$router.push('/tenants')
            this.$emit('closed')
        },
        onFieldChange(event) {
            this.tenant[event.fieldName] = event.value
            this.$emit('touched')
        }
    }
})
</script>

<style lang="scss" scoped>
::v-deep(.p-toolbar-group-right) {
    height: 100%;
}
</style>
