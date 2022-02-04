<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #start>{{ selectedRole.name }} </template>
        <template #end>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="handleSubmit" :disabled="buttonDisabled" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="rolesDetail">
        <TabView class="tabview-custom kn-tab" data-test="tab-view">
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.rolesManagement.detail.title') }}</span>
                </template>

                <RoleDetailTab :selectedRole="selectedRole" @fieldChanged="onFieldChange" @roleTypeChanged="onRoleTypeChange" />
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.rolesManagement.authorizations.title') }}</span>
                </template>

                <RoleAuthorizationsTab :selectedRole="selectedRole" :authList="authorizationList" :authCBs="authorizationCBs" @authChanged="onFieldChange" />
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.rolesManagement.businessModels') }}</span>
                </template>

                <DomainCategoryTab :title="$t('managers.rolesManagement.businessModels') + ' ' + $t('managers.rolesManagement.categories')" :categoryList="businessModelList" :selected="selectedBusinessModels" @changed="setSelectedBusinessModels($event)"></DomainCategoryTab>
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.rolesManagement.dataSets') }}</span>
                </template>

                <DomainCategoryTab :title="$t('managers.rolesManagement.dataSets') + ' ' + $t('managers.rolesManagement.categories')" :categoryList="dataSetList" :selected="selectedDataSets" @changed="setSelectedDataSets($event)"></DomainCategoryTab>
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.rolesManagement.kpiCategories') }}</span>
                </template>

                <DomainCategoryTab :title="$t('managers.rolesManagement.kpiCategories')" :categoryList="kpiCategoriesList" :selected="selectedKPICategories" @changed="setSelectedKPICategories($event)"></DomainCategoryTab>
            </TabPanel>
        </TabView>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iCategory, iRole } from './RolesManagement'
import { AxiosResponse } from 'axios'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import rolesManagementTabViewDescriptor from './RolesManagementTabViewDescriptor.json'
import useValidate from '@vuelidate/core'
import DomainCategoryTab from './tabs/DomainCategoryTab/DomainCategoryTab.vue'
import RoleDetailTab from './tabs/RoleDetailTab/RoleDetailTab.vue'
import RoleAuthorizationsTab from './tabs/RoleAuthorizationsTab/RoleAuthorizationsTab.vue'

export default defineComponent({
    components: {
        DomainCategoryTab,
        RoleDetailTab,
        TabView,
        TabPanel,
        RoleAuthorizationsTab
    },
    props: {
        id: {
            type: String,
            required: false
        }
    },
    emits: ['touched', 'closed', 'inserted'],
    data() {
        return {
            rolesManagementTabViewDescriptor: rolesManagementTabViewDescriptor,
            selectedBusinessModels: [] as iCategory[],
            selectedDataSets: [] as iCategory[],
            selectedKPICategories: [] as iCategory[],
            selectedRole: {} as iRole,
            roleMetaModelCategories: [] as any[],
            selectedCategories: [] as any[],
            authorizationList: [] as any,
            authorizationCBs: {} as any,
            businessModelList: [] as iCategory[],
            dataSetList: [] as iCategory[],
            kpiCategoriesList: [] as iCategory[],
            loading: false,
            operation: 'insert',
            v$: useValidate() as any
        }
    },
    computed: {
        buttonDisabled(): any {
            return this.v$.$invalid
        }
    },
    watch: {
        id() {
            this.loadSelectedRole()
            this.clearSelectedLists()
        }
    },
    async created() {
        await this.loadAllDomainsData()
        await this.loadSelectedRole()
        await this.loadAuthorizations()
        this.initAuthorizationCB()
    },
    methods: {
        async handleSubmit() {
            if (this.v$.$invalid) {
                return
            }

            this.selectedRole.roleMetaModelCategories = []

            this.mapCategories()

            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/roles/'
            if (this.selectedRole.id) {
                this.operation = 'update'
                url += this.selectedRole.id
            }

            await this.$http.post(url, this.selectedRole).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t(this.rolesManagementTabViewDescriptor.operation[this.operation].toastTitle),
                    msg: this.$t(this.rolesManagementTabViewDescriptor.operation.success)
                })
                this.$emit('inserted')
                this.$router.replace('/roles-management')
            })
        },
        loadCategories(id: string) {
            return this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/roles/categories/${id}`).finally(() => (this.loading = false))
        },
        loadDomains(type: string) {
            return this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=${type}`).finally(() => (this.loading = false))
        },
        async loadAuthorizations() {
            this.loading = true
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + 'authorizations')
                .then((response: AxiosResponse<any>) => {
                    this.authorizationList = response.data.root
                    this.rolesManagementTabViewDescriptor.authorizations.forEach((authorization) => {
                        authorization.visible = this.authorizationList.findIndex((auth) => authorization.dbname === auth.name) >= 0
                    })
                })
                .finally(() => (this.loading = false))
        },
        initAuthorizationCB() {
            this.rolesManagementTabViewDescriptor.categories.forEach((category) => {
                this.authorizationCBs[category.categoryName] = this.rolesManagementTabViewDescriptor.authorizations.filter((authCB) => authCB.category === category.categoryName && authCB.visible)
            })
        },
        async loadAllDomainsData() {
            this.loading = true
            await this.loadDomains('BM_CATEGORY').then((response: AxiosResponse<any>) => {
                response.data.map((category: any) => {
                    this.businessModelList.push({
                        categoryId: category.VALUE_ID,
                        categoryName: category.VALUE_NM
                    } as iCategory)
                })
            })
            await this.loadDomains('CATEGORY_TYPE').then((response: AxiosResponse<any>) => {
                response.data.map((category: any) => {
                    this.dataSetList.push({
                        categoryId: category.VALUE_ID,
                        categoryName: category.VALUE_NM
                    } as iCategory)
                })
            })
            await this.loadDomains('KPI_KPI_CATEGORY').then((response: AxiosResponse<any>) => {
                response.data.map((category: any) => {
                    this.kpiCategoriesList.push({
                        categoryId: category.VALUE_ID,
                        categoryName: category.VALUE_NM
                    } as iCategory)
                })
            })
            this.loading = false
        },
        async loadSelectedRole() {
            this.loading = true
            if (this.id) {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/roles/${this.id}`).then((response: AxiosResponse<any>) => (this.selectedRole = response.data))

                await this.loadCategories(this.id).then((response: AxiosResponse<any>) => {
                    this.clearSelectedLists()

                    response.data.map((category: any) => {
                        let index = this.indexInList(category, this.businessModelList)

                        if (index != -1) {
                            this.selectedBusinessModels.push(this.businessModelList[index])
                        }

                        index = this.indexInList(category, this.dataSetList)

                        if (index != -1) {
                            this.selectedDataSets.push(this.dataSetList[index])
                        }

                        index = this.indexInList(category, this.kpiCategoriesList)

                        if (index != -1) {
                            this.selectedKPICategories.push(this.kpiCategoriesList[index])
                        }
                    })
                })
            } else {
                this.selectedRole = { ...rolesManagementTabViewDescriptor.newRoleDefaultValues, name: '' }
            }
            this.loading = false
        },
        setDirty() {
            this.$emit('touched')
        },
        closeTemplate() {
            this.$router.push('/roles-management')
            this.$emit('closed')
        },
        indexInList(category: iCategory, list: iCategory[]) {
            return list.findIndex((element) => {
                return element.categoryId === category.categoryId
            })
        },
        clearSelectedLists() {
            this.selectedBusinessModels = []
            this.selectedDataSets = []
            this.selectedKPICategories = []
        },
        mapCategories() {
            if (this.selectedBusinessModels.length > 0) {
                this.selectedBusinessModels.map((category: any) => {
                    this.selectedRole.roleMetaModelCategories.push({
                        categoryId: category.categoryId
                    })
                })
            }

            if (this.selectedDataSets.length > 0) {
                this.selectedDataSets.map((category: any) => {
                    this.selectedRole.roleMetaModelCategories.push({
                        categoryId: category.categoryId
                    })
                })
            }

            if (this.selectedKPICategories.length > 0) {
                this.selectedKPICategories.map((category: any) => {
                    this.selectedRole.roleMetaModelCategories.push({
                        categoryId: category.categoryId
                    })
                })
            }
        },
        setSelectedBusinessModels(categories: iCategory[]) {
            this.selectedBusinessModels = categories
            this.$emit('touched')
        },
        setSelectedDataSets(categories: iCategory[]) {
            this.selectedDataSets = categories
            this.$emit('touched')
        },
        setSelectedKPICategories(categories: iCategory[]) {
            this.selectedKPICategories = categories
            this.$emit('touched')
        },
        onFieldChange(event) {
            this.selectedRole[event.fieldName] = event.value
            this.$emit('touched')
        },
        onRoleTypeChange(event) {
            this.selectedRole[event.roleTypeIDField] = event.ID
            this.selectedRole[event.roleTypeCDField] = event.CD
            this.$emit('touched')
        }
    }
})
</script>
<style lang="scss" scoped>
.rolesDetail {
    overflow: auto;
    flex: 1;
    display: flex;
    flex-direction: column;
}
</style>
