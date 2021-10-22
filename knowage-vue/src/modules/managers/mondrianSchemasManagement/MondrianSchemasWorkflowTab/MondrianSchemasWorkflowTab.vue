<template>
    <div class="workflow">
        <div class="p-grid">
            <div class="p-col">
                <Toolbar class="kn-toolbar kn-toolbar--secondary">
                    <template #left>
                        {{ $t('managers.mondrianSchemasManagement.workFlow.availableUsers') }}
                    </template>
                </Toolbar>
                <Listbox
                    class="kn-list workflowContainer"
                    :options="availableUsersList[0]"
                    :filter="true"
                    :filterPlaceholder="$t('common.search')"
                    optionLabel="name"
                    filterMatchMode="contains"
                    :filterFields="workflowDescriptor.filterFields"
                    :emptyFilterMessage="$t('common.info.noDataFound')"
                    data-test="userList1"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" :class="{ disableCursor: isStartedWf }" @click="addUserToWfList(slotProps.option.id)" data-test="userList1-item">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.userId }}</span>
                                <span class="kn-list-item-text-secondary">{{ slotProps.option.fullName }}</span>
                            </div>
                        </div>
                    </template>
                </Listbox>
            </div>
            <div class="p-col">
                <Toolbar class="kn-toolbar kn-toolbar--secondary">
                    <template #left>
                        {{ $t('managers.mondrianSchemasManagement.workFlow.userWf') }}
                    </template>
                    <template #right>
                        <span v-tooltip.top="$t(tooltipValue)">
                            <Button :disabled="disableButton" icon="pi pi-play" class="p-button-rounded" @click="startWorkflow" />
                        </span>
                    </template>
                </Toolbar>
                <Listbox
                    class="kn-list workflowContainer"
                    :options="availableUsersList[1]"
                    :filter="true"
                    :filterPlaceholder="$t('common.search')"
                    optionLabel="name"
                    filterMatchMode="contains"
                    :filterFields="workflowDescriptor.filterFields"
                    :emptyFilterMessage="$t('common.info.noDataFound')"
                    data-test="userList2"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" :class="{ disableCursor: isStartedWf }" @click="removeUserFromWfList(slotProps.option.id)" data-test="userList2-item">
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
import { AxiosResponse } from 'axios'
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
        selectedSchema: Object,
        isChanged: Boolean
    },
    computed: {
        disableButton() {
            if (!this.schema.id || this.availableUsersList[1].length == 0 || this.isChanged || this.isStartedWf) return true
            return false
        },
        tooltipValue() {
            const key = 'managers.mondrianSchemasManagement.workFlow.tooltips'
            if (!this.schema.id) return `${key}.noSchema`
            if (this.availableUsersList[1].length == 0 || this.isChanged) return `${key}.noWfUsers`
            if (this.isStartedWf === true) return `${key}.wfInProgress`

            return ''
        }
    },
    emits: ['changed', 'selectedUsersChanged'],
    data() {
        return {
            workflowDescriptor,
            availableUsersList: [] as any[],
            schema: {} as iSchema,
            isStartedWf: false,
            isButtonDisabled: false,
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
        onUserChange() {
            let selectedUsers = this.availableUsersList[1]
            this.$emit('selectedUsersChanged', selectedUsers)
            this.$emit('changed')
        },
        async isWorkflowStarted() {
            if (this.schema.id) {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/workflow/isStarted/${this.schema.id}`).then((response: AxiosResponse<any>) => {
                    if (response.data > 0) {
                        this.isStartedWf = true
                        this.userInProg = response.data
                    } else {
                        this.isStartedWf = false
                        this.userInProg = null
                    }
                })
            } else {
                this.isStartedWf = false
                this.userInProg = null
            }
        },
        async startWorkflow() {
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/workflow/startWorkflow/${this.schema.id}`
            await this.$http
                .put(url)
                .then((response: AxiosResponse<any>) => {
                    if (response.data.errors) {
                        this.$store.commit('setError', { title: this.$t('managers.mondrianSchemasManagement.toast.workflow.startFailed'), msg: response.data.errors[0].message })
                    } else {
                        this.$store.commit('setInfo', {
                            title: this.$t('managers.mondrianSchemasManagement.toast.workflow.started'),
                            msg: this.$t('managers.mondrianSchemasManagement.toast.workflow.startedOk')
                        })
                    }
                })
                .then(this.isWorkflowStarted)
        },
        moveUpList(userId) {
            const index = this.availableUsersList[1].findIndex((user) => user.id === userId)
            if (index > 0) {
                const user = this.availableUsersList[1][index]
                this.availableUsersList[1].splice(index, 1)
                this.availableUsersList[1].splice(index - 1, 0, user)
                this.onUserChange()
            }
        },
        moveDownList(userId) {
            const index = this.availableUsersList[1].findIndex((user) => user.id === userId)
            if (index < this.availableUsersList[1].length - 1) {
                const user = this.availableUsersList[1][index]
                this.availableUsersList[1].splice(index, 1)
                this.availableUsersList[1].splice(index + 1, 0, user)
                this.onUserChange()
            }
        },
        removeUserFromWfList(userId) {
            if (!this.isStartedWf) this.moveUser(userId, this.availableUsersList[1], this.availableUsersList[0])
        },
        addUserToWfList(userId) {
            if (!this.isStartedWf) this.moveUser(userId, this.availableUsersList[0], this.availableUsersList[1])
        },
        moveUser(userId, sourceList, targetList) {
            const index = sourceList.findIndex((user) => user.id === userId)
            if (index >= 0) {
                const user = sourceList[index]
                sourceList.splice(index, 1)
                targetList.push(user)
                this.onUserChange()
            }
        }
    }
})
</script>
<style lang="scss" scoped>
.workflow {
    :deep(.p-card-body) {
        padding: 0;
        .p-card-content {
            padding: 0;
        }
    }
    .workflowContainer {
        border: 1px solid $color-borders;
        border-top: none;
    }
}
.disableCursor {
    cursor: not-allowed;
}
::v-deep(.p-toolbar-group-right) {
    height: 100%;
}
</style>
