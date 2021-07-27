<template>
    <Dialog :header="$t('kpi.alert.addAction')" :breakpoints="addActionDialogDescriptor.dialog.breakpoints" :style="addActionDialogDescriptor.dialog.style" :visible="dialogVisible" :modal="true" :closable="false" class="p-fluid kn-dialog--toolbar--primary">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('kpi.alert.addAction') }}
                </template>
                <template #right>
                    <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" @click="handleSave" />
                    <Button icon="pi pi-times" class="kn-button p-button-text p-button-rounded" @click="$emit('close')" />
                </template>
            </Toolbar>
        </template>
        <div class="p-field p-col-6">
            <span class="p-float-label">
                <Dropdown id="type" class="kn-material-input" v-model="action.idAction" optionLabel="name" :options="addActionDialogDescriptor.actionType" @change="setType" />
                <label for="type" class="kn-material-input-label"> {{ $t('kpi.alert.type') }} * </label>
            </span>
            <!-- <KnValidationMessages
                :vComp="v$.type"
                :additionalTranslateParams="{
                    fieldName: $t('common.label')
                }"
            >
            </KnValidationMessages> -->
            <span class="p-float-label">
                <MultiSelect id="threshold" class="kn-material-input" v-model="selectedThresholds" optionLabel="label" :options="kpi.threshold.thresholdValues">
                    <template #value="slotProps">
                        <div v-for="option of slotProps.value" :key="option.code">
                            <ColorPicker v-model="option.color" disabled />
                            {{ option.label }}
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
        <ExectuteEtlCard v-if="type && type.id == 63" :loading="loading" :files="data.item" :data="action"></ExectuteEtlCard>
        <ContextBrokerCard v-if="type && type.id == 86" :data="action"></ContextBrokerCard>
        <SendMailCard v-else-if="type && type.id == 62" :action="selectedAction" :users="formatedUsers"></SendMailCard>
        =======
    </Dialog>
</template>
selectedAction
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
//import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'

export default defineComponent({
    name: 'add-action-dialog',
    components: {
        Dialog,
        Dropdown,
        MultiSelect,
        ExectuteEtlCard,
        ContextBrokerCard,
        ColorPicker,
        SendMailCard
        //KnValidationMessages
    },
    props: {
        dialogVisible: {
            type: Boolean,
            default: false
        },
        kpi: {
            type: Object
        },
        selectedAction: {
            type: Object as PropType<iAction>,
            required: true
        }
    },
    data() {
        return {
            addActionDialogDescriptor,
            loading: false,
            type: {} as any,
            //data: {},
            action: {} as iAction,
            selectedThresholds: [],
            data: [] as any[],
            formatedUsers: [] as any[],
            v$: useValidate() as any,
            mockedUsers: mockedUsers
        }
    },
    created() {
        this.loadAction()
    },
    validations() {
        return {
            action: createValidations('action', alertValidationDescriptor.validations.action)
        }
    },
    methods: {
        loadAction() {
            this.action = { ...this.selectedAction }
            console.log('Action after copy', this.action)
        },
        async setType() {
            console.log(this.type)
            this.action.jsonActionParameters = {}
            if (this.type.id == 63) {
                this.action.idAction = 63
                await this.loadData('2.0/documents/listDocument?includeType=ETL')
            } else if (this.type.id == 86) {
                this.action.idAction = 86
            } else if (this.type.id == 62) {
                this.action.idAction = 62
                await this.loadData('2.0/users')
                this.formatUsers()
            }

            console.log('DATA ', this.data)
            console.log('MOCKED USERS ', this.mockedUsers)
        },
        formatUsers() {
            for (let i = 0; i < this.mockedUsers.length; i++) {
                const attributes = this.mockedUsers[i].sbiUserAttributeses
                for (let key in attributes) {
                    if (attributes[key]['email']) {
                        this.formatedUsers.push({ name: this.mockedUsers[i].fullName, userId: this.mockedUsers[i].userId, email: attributes[key].email })
                    }
                }
            }
            console.log('FORMATED USERS', this.formatedUsers)
        },
        async loadData(path: string) {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + path)
                .then((response) => {
                    this.data = response.data
                })
                .finally(() => (this.loading = false))
        },
        handleSave() {
            this.action.thresholdValues = Object.assign(
                {},
                this.selectedThresholds.map((threshold: any) => {
                    return threshold.id
                })
            )
            // this.action.thresholdValues = this.selectedThresholds.map((threshold: any) => {
            //     return threshold.id
            // })
            console.log('SAVE', this.action)
            this.$emit('add', this.action)
        }
    }
})
</script>
