<template>
    <div class="p-d-flex">
        <div class="kn-list--column p-col-6">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ $t('managers.mondrianSchemasManagement.workFlow.availableUsers') }}
                </template>
            </Toolbar>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
            <div class="p-col">
                <Listbox
                    v-if="!loading"
                    class="kn-list--column"
                    :options="availableUsersList[0]"
                    :filter="true"
                    :filterPlaceholder="$t('common.search')"
                    optionLabel="name"
                    filterMatchMode="contains"
                    :filterFields="workflowDescriptor.filterFields"
                    :emptyFilterMessage="$t('common.info.noDataFound')"
                    @change="showForm"
                    data-test="userList1-list"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" :class="{ disableCursor: isStartedWf }" @click="addUserToWf(slotProps.option.id)" data-test="userList1-item">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.userId }}</span>
                                <span class="kn-list-item-text-secondary">{{ slotProps.option.fullName }}</span>
                            </div>
                        </div>
                    </template>
                </Listbox>
            </div>
        </div>

        <div class="kn-list--column p-col-6 ">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ $t('managers.mondrianSchemasManagement.workFlow.userWf') }}
                </template>
                <template #right>
                    <span v-tooltip.top="tooltipValue">
                        <Button :disabled="disableButton()" icon="pi pi-play" @click="startWorkflow" />
                    </span>
                </template>
            </Toolbar>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
            <div class="p-col">
                <Listbox
                    v-if="!loading"
                    class="kn-list--column"
                    :options="availableUsersList[1]"
                    :filter="true"
                    :filterPlaceholder="$t('common.search')"
                    optionLabel="name"
                    filterMatchMode="contains"
                    :filterFields="workflowDescriptor.filterFields"
                    :emptyFilterMessage="$t('common.info.noDataFound')"
                    @change="showForm"
                    data-test="userList2-list"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" :class="{ disableCursor: isStartedWf }" @click="removeFromList(slotProps.option.id)" data-test="userList2-item">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.userId }}</span>
                                <span class="kn-list-item-text-secondary">{{ slotProps.option.fullName }}</span>
                            </div>
                            <div v-if="!isStartedWf">
                                <Button icon="pi pi-arrow-circle-up" class="p-button-link" @click.stop="moveUpList(slotProps.option.id)" />
                                <Button icon="pi pi-arrow-circle-down" class="p-button-link" @click.stop="moveDownList(slotProps.option.id)" />
                            </div>
                            <div v-if="isStartedWf">
                                <i v-if="slotProps.option.id === userInProg" class="pi pi-check-circle" :style="workflowDescriptor.style.icon" />
                            </div>
                        </div>
                    </template>
                </Listbox>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iSchema } from '../MondrianSchemas'
import workflowDescriptor from './MondrianSchemasWorkflowDescriptor.json'
import Listbox from 'primevue/listbox'

import axios from 'axios'
import Tooltip from 'primevue/tooltip'

export default defineComponent({
    name: 'workflow-tab',
    directives: {
        tooltip: Tooltip
    },
    components: {
        Listbox
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
            tooltipValue: '',
            isChanged: false
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
            this.isChanged = false
            this.isWorkflowStarted()
        }
    },
    methods: {
        onUserChange() {
            let selectedUsers = this.availableUsersList[1]
            this.isChanged = true
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
                if (this.availableUsersList[1].length == 0 || this.isChanged) {
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
        },

        // LIST MANAGEMENT METHODS ==========================
        moveUpList(userId) {
            const index = this.availableUsersList[1].findIndex((user) => user.id === userId)
            if (index === 0) {
                return
            } else {
                const user = this.availableUsersList[1][index]
                this.availableUsersList[1].splice(index, 1)
                this.availableUsersList[1].splice(index - 1, 0, user)
                this.onUserChange()
            }
        },
        moveDownList(userId) {
            const index = this.availableUsersList[1].findIndex((user) => user.id === userId)
            if (index === this.availableUsersList[1].length - 1) {
                return
            } else {
                const user = this.availableUsersList[1][index]
                this.availableUsersList[1].splice(index, 1)
                this.availableUsersList[1].splice(index + 1, 0, user)
                this.onUserChange()
            }
        },
        removeFromList(userId) {
            if (this.isStartedWf) {
                return
            }
            const index = this.availableUsersList[1].findIndex((user) => user.id === userId)
            if (index >= 0) {
                const user = this.availableUsersList[1][index]
                this.availableUsersList[1].splice(index, 1)
                this.availableUsersList[0].push(user)
                this.onUserChange()
            }
        },

        addUserToWf(userId) {
            if (this.isStartedWf) {
                return
            }
            const index = this.availableUsersList[0].findIndex((user) => user.id === userId)
            if (index >= 0) {
                const user = this.availableUsersList[0][index]
                this.availableUsersList[0].splice(index, 1)
                this.availableUsersList[1].push(user)
                this.onUserChange()
            }
        }
    }
})
</script>
<style scoped>
.disableCursor {
    cursor: not-allowed;
}
::v-deep(.p-toolbar-group-right) {
    height: 100%;
}
</style>
