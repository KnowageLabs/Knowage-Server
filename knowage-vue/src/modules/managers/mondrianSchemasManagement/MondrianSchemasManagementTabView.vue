<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #left>{{ selectedSchema.name }} </template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="handleSubmit" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplateConfirm" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="card">
        <TabView class="tabview-custom" data-test="tab-view">
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.mondrianSchemasManagement.detail.title') }}</span>
                </template>
                <MondrianSchemasDetailTab :selectedSchema="selectedSchema" :reloadTable="reloadVersionTable" @fieldChanged="onFieldChange" @activeVersionChanged="onVersionChange" @versionUploaded="versionToSave = $event" @versionsReloaded="reloadVersionTable = false" />
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.mondrianSchemasManagement.workFlow.title') }}</span>
                </template>
                <MondrianSchemasWorkflowTab :isChanged="isWorkflowChanged" :selectedSchema="selectedSchema" :usersList="availableUsersList" @selectedUsersChanged="onSelectedUsersChange" @changed="emitTouched" />
            </TabPanel>
        </TabView>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iSchema } from './MondrianSchemas'
import axios from 'axios'
import tabViewDescriptor from './MondrianSchemasTabViewDescriptor.json'
import useValidate from '@vuelidate/core'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import MondrianSchemasDetailTab from './MondrianSchemasDetailTab/MondrianSchemasDetailTab.vue'
import MondrianSchemasWorkflowTab from './MondrianSchemasWorkflowTab/MondrianSchemasWorkflowTab.vue'

export default defineComponent({
    components: {
        TabView,
        TabPanel,
        MondrianSchemasDetailTab,
        MondrianSchemasWorkflowTab
    },
    emits: ['touched', 'closed', 'inserted'],
    props: {
        id: {
            type: String,
            required: false
        }
    },
    data() {
        return {
            loading: false,
            tabViewDescriptor: tabViewDescriptor,
            selectedSchema: {} as iSchema,
            v$: useValidate() as any,
            allUsers: [] as any,
            wfSelectedUserList: [] as any,
            availableUsersList: [] as any,
            versionToSave: null as any,
            reloadVersionTable: false,
            touched: false,
            isWorkflowChanged: false
        }
    },
    async created() {
        await this.loadAllUsers()
        this.loadSelectedSchema()
        this.clearAvailableUsersList()
    },
    computed: {
        buttonDisabled(): any {
            if (!this.selectedSchema.id && !this.versionToSave) {
                return true
            }
            return this.v$.$invalid
        },
        operation() {
            if (this.id) {
                return 'update'
            }
            return 'insert'
        }
    },
    watch: {
        id() {
            this.v$.$reset()
            this.loadSelectedSchema()
            this.touched = false
            this.isWorkflowChanged = false
        }
    },
    methods: {
        emitTouched() {
            this.touched = true
            this.isWorkflowChanged = true
            this.$emit('touched')
        },
        closeTemplate() {
            this.$router.push('/mondrian-schemas-management')
            this.$emit('closed')
        },
        onFieldChange(event) {
            this.selectedSchema[event.fieldName] = event.value
            this.touched = true
            this.$emit('touched')
        },
        onVersionChange(event) {
            this.selectedSchema.currentContentId = event
            this.touched = true
            this.$emit('touched')
        },
        onSelectedUsersChange(event) {
            this.availableUsersList[1] = event
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
        },
        async loadAllUsers() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/users`).then((response) => (this.allUsers = response.data))
        },
        async loadSelectedSchema() {
            this.loading = true
            if (this.id) {
                await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/mondrianSchemasResource/${this.id}`).then((response) => (this.selectedSchema = response.data))
                await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/workflow/${this.id}`).then((response: any) => (this.wfSelectedUserList = response.data.errors ? [] : response.data))
            } else {
                this.selectedSchema = {} as iSchema
                this.wfSelectedUserList = []
            }
            this.createAvailableUsersList()
            this.loading = false
        },
        createAvailableUsersList() {
            const listOfSelectedUsers = this.wfSelectedUserList.map((userId) => this.allUsers.find((user) => user.id === userId))
            const listOfAvailableUsers = [
                ...this.allUsers.filter((user) => {
                    const ind = this.wfSelectedUserList.findIndex((userId) => user.id === userId)
                    return ind < 0
                })
            ]
            this.availableUsersList = [listOfAvailableUsers, listOfSelectedUsers]
        },
        clearAvailableUsersList() {
            this.availableUsersList = [[...this.allUsers], []]
        },
        async handleSubmit() {
            if (this.v$.$invalid) {
                return
            }
            this.selectedSchema.type = 'MONDRIAN_SCHEMA'
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/mondrianSchemasResource/`
            if (this.selectedSchema.id) {
                url += this.selectedSchema.id
            }
            await this.createOrUpdate(url).then((response) => {
                if (response.data.errors) {
                    this.$store.commit('setError', { title: this.$t('managers.mondrianSchemasManagement.toast.schema.error'), msg: response.data.errors })
                } else {
                    this.$store.commit('setInfo', { title: this.$t(this.tabViewDescriptor.operation[this.operation].toastTitle), msg: this.$t(this.tabViewDescriptor.operation.success) })
                    this.onSaveSuccess(response)
                }
            })
        },
        async createOrUpdate(url) {
            return this.operation === 'update' ? axios.put(url, this.selectedSchema) : axios.post(url, this.selectedSchema)
        },
        async onSaveSuccess(response) {
            if (this.operation === 'insert') {
                this.selectedSchema.id = response.data.id
            }
            await this.uploadFile()
            if (this.isWorkflowChanged === true) {
                await this.updateWorkflow(this.selectedSchema.id)
            }

            this.versionToSave = null
            if (this.operation === 'insert') {
                this.$router.push(`/mondrian-schemas-management/${this.selectedSchema.id}`)
            } else {
                this.reloadVersionTable = true
            }
            this.isWorkflowChanged = false
            this.$emit('inserted')
            this.touched = false
        },
        async updateWorkflow(schemaId) {
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/workflow/update`
            await axios.put(url, { modelId: schemaId, workflowArr: this.availableUsersList[1] }, { headers: { Accept: 'application/json, text/plain, */*' } }).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('managers.mondrianSchemasManagement.toast.workflow.updated'),
                    msg: this.$t('managers.mondrianSchemasManagement.toast.workflow.ok')
                })
            })
        },
        async uploadFile() {
            if (!this.versionToSave) {
                return
            }
            var formData = new FormData()
            formData.append('file', this.versionToSave)
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/mondrianSchemasResource/${this.selectedSchema.id}` + '/versions'
            await axios.post(url, formData, { headers: { 'Content-Type': 'multipart/form-data' } }).then((response) => {
                if (response.data.errors) {
                    this.$store.commit('setError', { title: this.$t('managers.mondrianSchemasManagement.toast.uploadFile.error'), msg: response.data.errors })
                } else {
                    this.$store.commit('setInfo', { title: this.$t('managers.mondrianSchemasManagement.toast.uploadFile.uploaded'), msg: this.$t('managers.mondrianSchemasManagement.toast.uploadFile.ok') })
                }
            })
        }
    }
})
</script>
