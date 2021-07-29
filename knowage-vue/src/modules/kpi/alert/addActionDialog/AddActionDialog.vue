<template>
    <Dialog :header="$t('kpi.alert.addAction')" :breakpoints="addActionDialogDescriptor.dialog.breakpoints" :style="addActionDialogDescriptor.dialog.style" :visible="dialogVisible" :modal="true" :closable="false" class="p-fluid kn-dialog--toolbar--primary">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12">
                <template #left>
                    {{ $t('kpi.alert.addAction') }}
                </template>
                <template #right>
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="handleSave" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="$emit('close')" />
                </template>
            </Toolbar>
        </template>
        <div class="p-fluid p-formgrid p-grid">
            <span class="p-field p-col-6 p-mt-4 p-float-label">
                <Dropdown id="type" class="kn-material-input" v-model="action.idAction" dataKey="id" optionLabel="name" optionValue="id" :options="addActionDialogDescriptor.actionType" @change="setType" />
                <label for="type" class="kn-material-input-label"> {{ $t('kpi.alert.type') }} * </label>
            </span>
            <span class="p-field p-col-6 p-mt-4 p-float-label">
                <MultiSelect id="threshold" class="kn-material-input" v-model="selectedThresholds" optionLabel="label" :options="kpi.threshold?.thresholdValues">
                    <template #value="slotProps">
                        <div class="selected-options-container" v-for="option of slotProps.value" :key="option.code">
                            <Chip :style="{ height: 18 + 'px', 'background-color': option.color }" :label="option.label" />
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div class="item">
                            <ColorPicker v-model="slotProps.option.color" disabled />
                            {{ slotProps.option.label }}
                        </div>
                    </template>
                </MultiSelect>
                <label for="threshold" class="kn-material-input-label"> {{ $t('kpi.alert.threshold') }} * </label>
            </span>
        </div>
        <Card style="height:37rem">
            <template #content>
                <ExectuteEtlCard v-if="action && action.idAction == 63" :loading="loading" :files="etlDocumentList" :data="action" />
                <ContextBrokerCard v-if="action && action.idAction == 86" :data="action" />
                <SendMailCard v-else-if="action && action.idAction == 62" :action="selectedAction" :users="formatedUsers" />
            </template>
        </Card>
    </Dialog>
</template>
<script lang="ts">
import { defineComponent, PropType } from 'vue'
import axios from 'axios'
import { iAction } from '../Alert'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import MultiSelect from 'primevue/multiselect'
import ColorPicker from 'primevue/colorpicker'
import addActionDialogDescriptor from './AddActionDialogDescriptor.json'
import ExectuteEtlCard from './ExectuteEtlCard.vue'
import ContextBrokerCard from './ContextBrokerCard.vue'
import SendMailCard from './SendMailCard.vue'
import mockedUsers from './MockedUsers.json'
import useValidate from '@vuelidate/core'
import { createValidations } from '@/helpers/commons/validationHelper'
import alertValidationDescriptor from '../AlertValidationDescriptor.json'
import Chip from 'primevue/chip'

export default defineComponent({
    name: 'add-action-dialog',
    components: { Dialog, Dropdown, MultiSelect, ExectuteEtlCard, ContextBrokerCard, ColorPicker, SendMailCard, Chip },
    props: { dialogVisible: { type: Boolean, default: false }, kpi: { type: Object }, selectedAction: { type: Object as PropType<iAction>, required: true } },
    emits: ['save'],
    data() {
        return {
            v$: useValidate() as any,
            addActionDialogDescriptor,
            type: {} as any,
            action: {} as iAction,
            selectedThresholds: [],
            data: [] as any[],
            etlDocumentList: [] as any[],
            usersList: [] as any[],
            formatedUsers: [] as any[],
            mockedUsers: mockedUsers,
            loading: false
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
        loadAction() {
            this.action = { ...this.selectedAction }
            this.type = this.action.idAction
            this.selectedThresholds = this.selectedAction.thresholdData ? this.selectedAction.thresholdData : []
        },
        async setType() {
            this.action.jsonActionParameters = {}
            if (this.action.idAction == 62) {
                this.action.idAction = 62
                this.formatUsers()
            }
        },
        // PROMENITI MOKOVANE USERE U OVE IZ APIJA KADA SE PUSHUJE
        formatUsers() {
            for (let i = 0; i < this.mockedUsers.length; i++) {
                const attributes = this.mockedUsers[i].sbiUserAttributeses
                for (let key in attributes) {
                    if (attributes[key]['email']) {
                        this.formatedUsers.push({ name: this.mockedUsers[i].fullName, userId: this.mockedUsers[i].userId, email: attributes[key].email })
                    }
                }
            }
        },
        async loadEtlDocuments() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/documents/listDocument?includeType=ETL').then((response) => {
                this.etlDocumentList = [...response.data.item]
            })
        },
        async loadUsers() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/users').then((response) => {
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
</style>
