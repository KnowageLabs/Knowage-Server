<template>
    <div>
        <PickList v-model="availableUsersList" @move-to-target="onUserChange" @move-to-source="onUserChange" @reorder="onUserChange" @move-all-to-target="onUserChange">
            <template #sourceHeader>
                {{ $t('managers.mondrianSchemasManagement.workFlow.availableUsers') }}
            </template>
            <template #targetHeader>
                {{ $t('managers.mondrianSchemasManagement.workFlow.userWf') }}
                <Button v-if="isStartedWf === false" v-tooltip.top="'Enter your username'" icon="pi pi-play" class="p-button-sm" @click="startWorkflow" />
            </template>
            <template #item="slotProps">
                <div>
                    <h4 class="p-mb-2">{{ slotProps.item.userId }}</h4>
                    <span>{{ slotProps.item.fullName }}</span>
                </div>
            </template>
        </PickList>
        {{ availableUsersList[1] }}
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iSchema } from '../MondrianSchemas'
import tabViewDescriptor from '../MondrianSchemasTabViewDescriptor.json'
import PickList from 'primevue/picklist'
import axios from 'axios'
import Tooltip from 'primevue/tooltip'

export default defineComponent({
    name: 'workflow-tab',
    directives: {
        tooltip: Tooltip
    },
    components: {
        PickList
    },
    props: {
        usersList: Array,
        selectedSchema: Object
    },
    emits: ['changed', 'selectedUsersChanged'],
    data() {
        return {
            tabViewDescriptor,
            availableUsersList: [] as any[],
            schema: {} as iSchema,
            isStartedWf: false,
            userInProg: null as any
        }
    },

    mounted() {
        if (this.selectedSchema) {
            this.schema = { ...this.selectedSchema } as iSchema
        }
        this.availableUsersList = this.usersList as any[]
    },
    watch: {
        usersList() {
            this.availableUsersList = this.usersList as any[]
        },
        selectedSchema() {
            this.schema = { ...this.selectedSchema } as iSchema
            this.isWorkflowStarted()
        }
    },
    methods: {
        onUserChange(event) {
            console.log(event)
            console.log('==========================')
            console.log(this.availableUsersList[1])
            let selectedUsers = this.availableUsersList[1]
            this.$emit('selectedUsersChanged', selectedUsers)
            this.$emit('changed')
        },

        // CHECK IF WORKFLOW IS STARTED AND RETURN USER IN PROGRESS ==========================
        async isWorkflowStarted() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/workflow/isStarted/${this.schema.id}`).then((response) => {
                if (response.data > 0) {
                    this.isStartedWf = true
                    this.userInProg = response.data
                    console.log('------------------------ isWorkflowStarted ------------------------')
                    console.log(this.isStartedWf)
                    console.log(this.userInProg)
                } else {
                    this.isStartedWf = false
                    console.log('------------------------ isWorkflowStarted ------------------------')
                    console.log(this.isStartedWf)
                }
            })
        },

        // CHECK SCHEMA & START WORKFLOW ==========================
        async startWorkflow() {
            if (!this.schema.id) {
                this.$store.commit('setError', { title: 'Schema not created', msg: 'Cannot start workflow without schema' })
                return
            }
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/workflow/startWorkflow/${this.schema.id}`
            await axios
                .put(url)
                .then((response) => {
                    if (response.data.errors) {
                        this.$store.commit('setError', { title: this.$t('common.error.uploading'), msg: response.data.errors[0].message })
                        console.log('ERROR RESPONSE:')
                        console.log(response)
                    } else {
                        console.log('START WORKFLOW RESPONSE OMFG')
                        console.log(response)
                        this.$store.commit('setInfo', {
                            title: this.$t('managers.mondrianSchemasManagement.updatingWorkflow'),
                            msg: this.$t('managers.mondrianSchemasManagement.workflowOk')
                        })
                    }
                })
                .then(this.isWorkflowStarted)
        },
        disableButton() {
            if (!this.schema.id) {
                return 'Schema is not created'
            }
        }
    }
})
</script>
