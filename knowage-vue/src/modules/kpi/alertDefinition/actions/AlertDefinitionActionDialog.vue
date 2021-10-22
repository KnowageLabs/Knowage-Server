<template>
    <Dialog :header="$t('kpi.alert.addAction')" :breakpoints="addActionDialogDescriptor.dialog.breakpoints" :style="addActionDialogDescriptor.dialog.style" :visible="dialogVisible" :modal="true" :closable="false" class="p-fluid kn-dialog--toolbar--primary" data-test="add-action-dialog">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12">
                <template #left>
                    {{ $t('kpi.alert.addAction') }}
                </template>
                <template #right>
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="actionSaveButtonDisabled" @click="handleSave" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="$emit('close')" />
                </template>
            </Toolbar>
        </template>
        <div class="p-fluid p-formgrid p-grid">
            <span class="p-field p-col-6 p-mt-4 p-float-label">
                <Dropdown id="type" class="kn-material-input" v-model="action.idAction" dataKey="id" optionLabel="name" optionValue="id" :options="actionList" @change="setType" />
                <label for="type" class="kn-material-input-label"> {{ $t('kpi.alert.type') }} * </label>
            </span>
            <span class="p-field p-col-6 p-mt-4 p-float-label">
                <MultiSelect id="threshold" class="kn-material-input" v-model="selectedThresholds" optionLabel="label" :options="kpi.threshold?.thresholdValues">
                    <template #value="slotProps">
                        <div class="selected-options-container" v-for="option of slotProps.value" :key="option.code">
                            <div class="color-box" :style="{ 'background-color': option.color }" />
                            {{ option.label }}
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div class="selected-options-container">
                            <div class="color-box" :style="{ 'background-color': slotProps.option.color }" />
                            {{ slotProps.option.label }}
                        </div>
                    </template>
                </MultiSelect>
                <label for="threshold" class="kn-material-input-label"> {{ $t('kpi.alert.threshold') }} * </label>
            </span>
        </div>
        <Card style="height:37rem">
            <template #content>
                <ExectuteEtlCard v-if="action && action.className == 'it.eng.knowage.enterprise.tools.alert.action.ExecuteETLDocument'" :loading="loading" :files="etlDocumentList" :data="action" />
                <ContextBrokerCard v-if="action && action.className == 'it.eng.spagobi.tools.alert.action.NotifyContextBroker'" :data="action" />
                <SendMailCard v-else-if="action && action.className == 'it.eng.knowage.enterprise.tools.alert.action.SendMail'" :action="action" :users="formatedUsers" />
            </template>
        </Card>
    </Dialog>
</template>
<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { iAction } from '../AlertDefinition'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import MultiSelect from 'primevue/multiselect'
import addActionDialogDescriptor from './AlertDefinitionActionDialogDescriptor.json'
import ExectuteEtlCard from './AlertDefinitionExectuteEtlCard.vue'
import ContextBrokerCard from './AlertDefinitionContextBrokerCard.vue'
import SendMailCard from './AlertDefinitionSendMailCard.vue'
import useValidate from '@vuelidate/core'
import { createValidations } from '@/helpers/commons/validationHelper'
import alertValidationDescriptor from '../AlertDefinitionValidationDescriptor.json'

export default defineComponent({
    name: 'add-action-dialog',
    components: { Dialog, Dropdown, MultiSelect, ExectuteEtlCard, ContextBrokerCard, SendMailCard },
    props: { actionList: [] as any, dialogVisible: { type: Boolean, default: false }, kpi: { type: Object }, selectedAction: { type: Object as PropType<iAction>, required: true } },
    emits: ['save'],
    data() {
        return {
            v$: useValidate() as any,
            addActionDialogDescriptor,
            type: {} as any,
            action: {} as any,
            selectedThresholds: [],
            data: [] as any[],
            etlDocumentList: [] as any[],
            usersList: [] as any[],
            formatedUsers: [] as any[],
            loading: false
        }
    },
    computed: {
        componentToShow(): string {
            switch (this.action.className) {
                case 'it.eng.knowage.enterprise.tools.alert.action.ExecuteETLDocument': {
                    return 'ExectuteEtlCard'
                }
                case 'it.eng.spagobi.tools.alert.action.NotifyContextBroker': {
                    return 'ContextBrokerCard'
                }
                case 'it.eng.knowage.enterprise.tools.alert.action.SendMail': {
                    return 'SendMailCard'
                }
                default:
                    return ''
            }
        },
        actionSaveButtonDisabled(): any {
            if (!this.action.className || this.selectedThresholds.length == 0) {
                return true
            } else if (this.action.className != 'it.eng.knowage.enterprise.tools.alert.action.SendMail' && this.isObjectEmpty(this.action.jsonActionParameters)) {
                return true
            }
            return false
        }
    },

    created() {
        this.loadAction()
        this.loadEtlDocuments()
        this.loadUsers()
    },
    watch: {
        selectedAction() {
            this.loadAction()
        }
    },
    validations() {
        return {
            action: createValidations('action', alertValidationDescriptor.validations.action)
        }
    },
    methods: {
        isObjectEmpty(objectToCheck) {
            for (var i in objectToCheck) return false
            return true
        },
        loadAction() {
            this.action = { ...this.selectedAction }
            this.type = this.action.idAction
            this.selectedThresholds = this.selectedAction.thresholdData ? this.selectedAction.thresholdData : []
        },
        async setType(event) {
            this.action.jsonActionParameters = {}
            var actionInList = this.actionList.find((actionInList) => actionInList.id === event.value)
            this.action.className = actionInList.className
            if (this.action.className == 'it.eng.knowage.enterprise.tools.alert.action.SendMail') {
                this.action.className = 'it.eng.knowage.enterprise.tools.alert.action.SendMail'
                this.formatUsers()
            }
        },
        formatUsers() {
            for (let i = 0; i < this.usersList.length; i++) {
                const attributes = this.usersList[i].sbiUserAttributeses
                for (let key in attributes) {
                    if (attributes[key]['email']) {
                        this.formatedUsers.push({ name: this.usersList[i].fullName, userId: this.usersList[i].userId, email: attributes[key].email })
                    }
                }
            }
        },
        async loadEtlDocuments() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/documents/listDocument?includeType=ETL').then((response: AxiosResponse<any>) => {
                this.etlDocumentList = [...response.data.item]
            })
        },
        async loadUsers() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/users').then((response: AxiosResponse<any>) => {
                this.usersList = [...response.data]
            })
        },
        handleSave() {
            this.action.thresholdValues = this.selectedThresholds.map((threshold: any) => {
                return threshold.id
            })
            this.$emit('save', this.action)
        }
    }
})
</script>
<style lang="scss" scoped>
.selected-options-container {
    display: inline-flex;
    margin-right: 0.5rem;
}
.color-box {
    height: 15px;
    width: 15px;
    margin-right: 5px;
}
</style>
