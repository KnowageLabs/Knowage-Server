<template>
    <div>
        <PickList v-model="availableUsersList" listStyle="height:500px" :disabled="true" @move-to-target="onUserChange" @move-to-source="onUserChange" @reorder="onUserChange">
            <template #sourceHeader>
                {{ $t('managers.mondrianSchemasManagement.workFlow.availableUsers') }}
            </template>
            <template #targetHeader>
                {{ $t('managers.mondrianSchemasManagement.workFlow.userWf') }}
                <span :style="workflowDescriptor.style.targetIcon" v-tooltip.top="tooltipValue">
                    <Button :disabled="disableButton()" icon="pi pi-play" @click="startWorkflow" />
                </span>
            </template>
            <template #item="slotProps">
                <div :style="workflowDescriptor.style.listItem">
                    <div :style="workflowDescriptor.style.listItemDetail">
                        <h4 class="p-mb-2">{{ slotProps.item.userId }}</h4>
                        <span>{{ slotProps.item.fullName }}</span>
                    </div>
                    <div :style="workflowDescriptor.style.icon">
                        <i v-if="slotProps.item.id === this.userInProg" class="pi pi-check" />
                    </div>
                </div>
            </template>
        </PickList>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iSchema } from '../MondrianSchemas'
import workflowDescriptor from './MondrianSchemasWorkflowDescriptor.json'
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
            workflowDescriptor,
            availableUsersList: [] as any[],
            schema: {} as iSchema,
            isStartedWf: false,
            isButtonDisabled: false,
            userInProg: null as any,
            tooltipValue: ''
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
        onUserChange() {
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
                    this.userInProg = null
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

        // DISABLE PLAY BUTTON & MANAGE TOOLTIP| because :disabled is not reactive :( ==========================
        disableButton() {
            console.log('$$$$$$$$$$$$$$$$$$$$$$ disableButton $$$$$$$$$$$$$$$$$$$$$$')
            if (!this.schema.id) {
                console.log('BUTTON DISABLED')
                this.tooltipValue = this.$t('managers.mondrianSchemasManagement.workFlow.tooltips.noSchema')
                return true
            } else {
                if (this.availableUsersList[1].length == 0) {
                    console.log('BUTTON DISABLED NO ARRAY' + this.availableUsersList[1])
                    this.tooltipValue = this.$t('managers.mondrianSchemasManagement.workFlow.tooltips.noWfUsers')
                    return true
                } else {
                    if (this.isStartedWf === true) {
                        console.log('BUTTON DISABLED BECAUSE WORKFLOW ID: ' + this.isStartedWf)
                        this.tooltipValue = this.$t('managers.mondrianSchemasManagement.workFlow.tooltips.wfInProgress')
                        return true
                    }
                }
            }
            console.log('BUTTON NOT DISABLED')
            this.tooltipValue = ''
            return false
        }
    }
})
</script>
