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

                <MondrianSchemasWorkflowTab :selectedSchema="selectedSchema" :usersList="availableUsersList" @selectedUsersChanged="onSelectedUsersChange" @changed="emitTouched" />
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
            operation: 'insert',
            tabViewDescriptor: tabViewDescriptor,
            selectedSchema: {} as iSchema,
            v$: useValidate() as any,
            allUsers: [] as any,
            wfSelectedUserList: [] as any,
            availableUsersList: [] as any,
            versionToSave: null as any,
            reloadVersionTable: false,
            touched: false
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
        }
    },
    watch: {
        id() {
            this.loadSelectedSchema()
            this.clearAvailableUsersList()
        }
    },
    methods: {
        // EVENTS TO EMIT & onEmited ==========================
        emitTouched() {
            this.touched = true
            this.$emit('touched')
        },
        closeTemplate() {
            // console.log('Template Closed')
            this.$router.push('/schemas')
            this.$emit('closed')
        },
        onFieldChange(event) {
            this.selectedSchema[event.fieldName] = event.value
            // console.log('Field Changed')
            this.touched = true
            this.$emit('touched')
        },
        onVersionChange(event) {
            this.selectedSchema.currentContentId = event
            // console.log('Version Changed')
            // console.log(this.selectedSchema.currentContentId)
            this.touched = true
            this.$emit('touched')
        },
        onSelectedUsersChange(event) {
            this.availableUsersList[1] = event
            console.log('Selected User Changed')
            console.log(this.availableUsersList[1])
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

        // LOAD WORKFLOW USERS ==========================
        async loadAllUsers() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/users`).then((response) => (this.allUsers = response.data))
            console.log('------------------------ loadAllUsers ------------------------')
            console.log(this.allUsers)
        },

        // LOAD SELECTED SCHEMA ==========================
        async loadSelectedSchema() {
            this.loading = true
            if (this.id) {
                await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/mondrianSchemasResource/${this.id}`).then((response) => (this.selectedSchema = response.data))
                await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/workflow/${this.id}`).then((response) => (this.wfSelectedUserList = response.data))

                this.createAvailableUsersList()
            } else {
                this.selectedSchema = {} as iSchema
            }
            this.loading = false
        },

        // CREATE WORKFLOW DATA ==========================
        createAvailableUsersList() {
            console.log('------------------------ selectedSchema ------------------------')
            console.log(this.selectedSchema)
            console.log('------------------- wfSelectedUserList RAW -------------------')
            console.log(this.wfSelectedUserList)

            const listOfSelectedUsers = this.wfSelectedUserList.map((userId) => this.allUsers.find((user) => userId === user.id))
            const listOfAvailableUsers = this.allUsers.filter((user) => {
                const ind = this.wfSelectedUserList.findIndex((userId) => user.id === userId)
                if (ind < 0) {
                    return true
                } else {
                    this.wfSelectedUserList.splice(ind, 1)
                }
                return false
            })
            this.availableUsersList = [listOfAvailableUsers, listOfSelectedUsers]

            console.log('----------------- availableUsersList 2D Array -----------------')
            console.log(this.availableUsersList)
        },

        // CREATE WORKFLOW DATA ==========================
        clearAvailableUsersList() {
            console.log('--------------------- clearAvailableUsersList ---------------------')
            console.log(this.allUsers)
            this.availableUsersList = [this.allUsers, []]
        },

        // SEND SUBMIT REQUEST ==========================
        async handleSubmit() {
            if (this.v$.$invalid) {
                return
            }
            this.selectedSchema.type = 'MONDRIAN_SCHEMA'
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/mondrianSchemasResource/`
            if (this.selectedSchema.id) {
                this.operation = 'update'
                url += this.selectedSchema.id
            }
            await this.createOrUpdate(url).then((response) => {
                this.onSaveSuccess(response)
            })
        },

        // DETERMINE IF ITS CREATING NEW OR UPDATING ==========================
        async createOrUpdate(url) {
            return this.operation === 'update' ? axios.put(url, this.selectedSchema) : axios.post(url, this.selectedSchema)
        },

        // UPDATE WORKFLOW USERS FROM WORKFLOW TAB ==========================
        async updateWorkflow(response) {
            console.log('------------------------ updateWorkflow(response) ------------------------')
            console.log(response)
            let newSchemaId = response.data.id
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/workflow/update`
            await axios
                .put(
                    url,
                    { modelId: newSchemaId, workflowArr: this.availableUsersList[1] },
                    {
                        headers: {
                            Accept: 'application/json, text/plain, */*'
                        }
                    }
                )
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('managers.mondrianSchemasManagement.updatingWorkflow'),
                        msg: this.$t('managers.mondrianSchemasManagement.workflowOk')
                    })
                    this.$emit('inserted')
                })
        },

        // UPLOAD FILE PASSED FROM WORKFLOW TAB ==========================
        async uploadFile() {
            if (!this.versionToSave) {
                return
            }
            var formData = new FormData()
            formData.append('file', this.versionToSave)
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/mondrianSchemasResource/${this.selectedSchema.id}` + '/versions'
            await axios.post(url, formData, { headers: { 'Content-Type': 'multipart/form-data' } }).then(
                (response) => {
                    if (response.data.errors) {
                        this.$store.commit('setError', { title: this.$t('common.error.uploading'), msg: this.$t('common.error.errorCreatingPackage') })
                        console.log('ERROR RESPONSE:')
                        console.log(response)
                    } else {
                        this.$store.commit('setInfo', { title: this.$t('managers.mondrianSchemasManagement.uploadingVersion'), msg: this.$t('managers.mondrianSchemasManagement.uploadOk') })
                    }
                },
                (error) => this.$store.commit('setError', { title: this.$t('common.error.uploading'), msg: this.$t(error) })
            )
        },

        // onSaveSuccess ==========================
        async onSaveSuccess(response) {
            this.selectedSchema.id = response.data.id
            await this.uploadFile()
            await this.updateWorkflow(response)

            this.versionToSave = null
            this.$store.commit('setInfo', {
                title: this.$t(this.tabViewDescriptor.operation[this.operation].toastTitle),
                msg: this.$t(this.tabViewDescriptor.operation.success)
            })
            if (this.operation === 'insert') {
                this.$router.push(`/schemas/${this.selectedSchema.id}`)
            } else {
                this.reloadVersionTable = true
            }
            this.$emit('inserted')
            this.loadSelectedSchema()
            this.touched = false
        }
    }
})
</script>
<style scoped>
::v-deep(.p-toolbar-group-right) {
    height: 100%;
}
</style>
