<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('managers.buisnessModelCatalogue.driversDetails') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <BusinessModelDriverHint v-if="!selectedDriver"></BusinessModelDriverHint>
            <form class="p-fluid p-m-5" v-else>
                <div class="p-field">
                    <span class="p-float-label">
                        <InputText
                            id="label"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.driver.label.$model"
                            :class="{
                                'p-invalid': v$.driver.label.$invalid && v$.driver.label.$dirty
                            }"
                            maxLength="40"
                            @blur="v$.driver.label.$touch()"
                            @input="setChanged"
                        />
                        <label for="label" class="kn-material-input-label"> {{ $t('managers.buisnessModelCatalogue.driverTitle') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.driver.label"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.buisnessModelCatalogue.driverTitle')
                        }"
                    />
                </div>

                <div class="p-field">
                    <span>
                        <label for="parameter" class="kn-material-input-label">{{ $t('managers.buisnessModelCatalogue.analyticalDriver') }} * </label>
                        <Dropdown
                            id="parameter"
                            class="kn-material-input"
                            :class="{
                                'p-invalid': v$.driver.parameter.$invalid && v$.driver.parameter.$dirty
                            }"
                            v-model="v$.driver.parameter.$model"
                            :options="analyticalDrivers"
                            :placeholder="$t('managers.buisnessModelCatalogue.analyticalDriverPlaceholder')"
                            :filter="true"
                            @before-show="v$.driver.parameter.$touch()"
                            @change="setChanged"
                        >
                            <template #value="slotProps">
                                <div v-if="slotProps.value">
                                    <span>{{ slotProps.value.label }}</span>
                                </div>
                            </template>
                            <template #option="slotProps">
                                <div>
                                    <span>{{ slotProps.option.label }}</span>
                                </div>
                            </template>
                        </Dropdown>
                    </span>

                    <KnValidationMessages
                        :vComp="v$.driver.parameter"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.buisnessModelCatalogue.analyticalDriver')
                        }"
                    >
                    </KnValidationMessages>
                </div>

                <div class="p-field">
                    <span class="p-float-label">
                        <InputText
                            id="parameterUrlName"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.driver.parameterUrlName.$model"
                            :class="{
                                'p-invalid': v$.driver.parameterUrlName.$invalid && v$.driver.parameterUrlName.$dirty
                            }"
                            maxLength="20"
                            @blur="v$.driver.parameterUrlName.$touch()"
                            @input="setChanged"
                        />
                        <label for="parameterUrlName" class="kn-material-input-label"> {{ $t('managers.buisnessModelCatalogue.driversUrl') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.driver.parameterUrlName"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.buisnessModelCatalogue.driversUrl')
                        }"
                        :specificTranslateKeys="{ custom_unique: 'managers.buisnessModelCatalogue.driversUrlNotUnique' }"
                    />
                </div>

                <div class="p-field p-mt-2">
                    <InputSwitch id="driver-multivalue " class="p-mr-2" v-model="driver.multivalue" @change="setChanged" />
                    <i class="fa fa-list p-mr-2" />
                    <label for="driver-multivalue " class="kn-material-input-label"> {{ $t('managers.buisnessModelCatalogue.multivalue') }}</label>
                </div>
            </form>
        </template>
    </Card>

    <Card v-if="selectedDriver">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('managers.buisnessModelCatalogue.driverDataConditions') }}
                </template>
                <template #right>
                    <Button class="kn-button p-button-text" @click="showForm" :disabled="modes.length === 0">{{ $t('managers.buisnessModelCatalogue.addCondition') }}</Button>
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div class="kn-list--column">
                <div class="p-col">
                    <Listbox class="kn-list" :options="conditions" listStyle="max-height:calc(100% - 62px)" @change="showForm">
                        <template #empty>{{ $t('common.info.noDataFound') }}</template>
                        <template #option="slotProps">
                            <div class="kn-list-item">
                                <div class="kn-list-item-text">
                                    <span>{{ slotProps.option.filterOperation + ' ' + $t('managers.buisnessModelCatalogue.value') + ' ' + slotProps.option.parFatherUrlName }}</span>
                                </div>
                                <Button icon="far fa-trash-alt" class="p-button-link p-button-sm" @click.stop="showConditionDeleteDialog(slotProps.option)" />
                            </div>
                        </template>
                    </Listbox>
                </div>
            </div>
        </template>
    </Card>

    <div v-if="conditionFormVisible">
        <Dialog class="p-fluid kn-dialog--toolbar--primary" :style="businessModelDriverDetailDescriptor.conditionDialog.style" :visible="true" :modal="true" :header="$t('managers.buisnessModelCatalogue.driverDataConditions')" :closable="false">
            <div id="operationInfo">
                <p>{{ $t('managers.buisnessModelCatalogue.operationInfo', { driver: driver.label }) }}</p>
            </div>
            <form class="p-fluid p-m-5">
                <div class="p-field p-d-flex">
                    <div :style="businessModelDriverDetailDescriptor.input.parFather.style">
                        <span class="p-float-label">
                            <Dropdown id="parFather" class="kn-material-input" v-model="condition.parFather" :options="drivers" placeholder=" ">
                                <template #value="slotProps">
                                    <div v-if="slotProps.value">
                                        <span>{{ slotProps.value.label }}</span>
                                    </div>
                                </template>
                                <template #option="slotProps">
                                    <div>
                                        <span>{{ slotProps.option.label }}</span>
                                    </div>
                                </template>
                            </Dropdown>
                            <label for="parFather" class="kn-material-input-label">{{ $t('managers.buisnessModelCatalogue.driverDepends') }}</label>
                        </span>
                    </div>

                    <div :style="businessModelDriverDetailDescriptor.input.filterOperation.style">
                        <span class="p-float-label">
                            <Dropdown id="filterOperation" class="kn-material-input" v-model="condition.filterOperation" :options="businessModelDriverDetailDescriptor.operations" optionLabel="name" optionValue="value" />
                            <label for="filterOperation" class="kn-material-input-label">{{ $t('managers.buisnessModelCatalogue.filterOperator') }}</label>
                        </span>
                    </div>

                    <div :style="businessModelDriverDetailDescriptor.input.logicOperator.style">
                        <span class="p-float-label">
                            <Dropdown id="logicOperator" class="kn-material-input" v-model="condition.logicOperator" :options="businessModelDriverDetailDescriptor.logicOperator" optionLabel="name" optionValue="value" />
                            <label for="logicOperator" class="kn-material-input-label">{{ $t('managers.buisnessModelCatalogue.logicOperator') }}</label>
                        </span>
                    </div>
                </div>
                <div v-for="mode in modes" :key="mode.useID">
                    <hr />
                    <p>{{ $t('managers.buisnessModelCatalogue.modality') + ': ' + mode.name }}</p>
                    <div class="p-d-flex">
                        <div class="mode-inputs">
                            <Checkbox :value="mode.useID" v-model="selectedModes" />
                            <label>{{ $t('managers.buisnessModelCatalogue.check') }}</label>
                        </div>
                        <div class="mode-inputs">
                            <label class="kn-material-input-label">{{ $t('managers.buisnessModelCatalogue.lovsColumn') }}</label>
                            <Dropdown id="parFather" class="kn-material-input" v-model="modalities[mode.useID]" :options="getLovs(mode.idLov)" :disabled="isModeActive(mode.useID)">
                                <template #value="slotProps">
                                    <div v-if="slotProps.value">
                                        <span>{{ slotProps.value }}</span>
                                    </div>
                                </template>
                                <template #option="slotProps">
                                    <div>
                                        <span>{{ slotProps.option }}</span>
                                    </div>
                                </template>
                            </Dropdown>
                        </div>
                    </div>
                </div>
            </form>
            <template #footer>
                <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="closeForm"></Button>
                <Button class="kn-button kn-button--primary" :label="$t('common.save')" @click="handleSubmit"></Button>
            </template>
        </Dialog>
    </div>

    <Dialog header="Error" v-model:visible="displayWarning">
        <p>{{ errorMessage }}</p>
        <template #footer>
            <Button label="Ok" icon="pi pi-check" @click="displayWarning = false" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import axios from 'axios'
import businessModelDriverDetailDescriptor from './BusinessModelDriverDetailDescriptor.json'
import businessModelDriverDetailValidationDescriptor from './BusinessModelDriverDetailValidationDescriptor.json'
import BusinessModelDriverHint from './BusinessModelDriverHint.vue'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Listbox from 'primevue/listbox'
import useValidate from '@vuelidate/core'

export default defineComponent({
    name: 'business-model-driver-detail-card',
    components: {
        BusinessModelDriverHint,
        Card,
        Checkbox,
        Dialog,
        Dropdown,
        InputSwitch,
        KnValidationMessages,
        Listbox
    },
    props: {
        businessModelId: {
            type: Number
        },
        selectedDriver: {
            type: Object,
            required: true
        },
        formVisible: {
            type: Boolean,
            required: true
        },
        driverOptions: {
            type: Array,
            required: true
        },
        businessModelDrivers: {
            type: Array,
            required: true
        }
    },
    emits: ['touched'],
    watch: {
        async selectedDriver() {
            this.loadSelectedDriver()
            if (this.selectedDriver) {
                await this.loadDataDependencies()
                await this.loadModes()
                await this.loadLovs()
            }
            console.log('MODES: ', this.modes)
            console.log('LOVS: ', this.lovs)
        },
        driverOptions() {
            this.loadAnalyticalDrivers()
        },
        businessModelDrivers() {
            this.loadBusinessModelDrivers()
        }
    },
    async created() {
        this.loadSelectedDriver()
        this.loadAnalyticalDrivers()
        this.loadBusinessModelDrivers()
        if (this.selectedDriver) {
            await this.loadDataDependencies()
            await this.loadModes()
            await this.loadLovs()
        }
    },
    data() {
        return {
            businessModelDriverDetailDescriptor,
            businessModelDriverDetailValidationDescriptor,
            driver: null as any,
            drivers: [] as any[],
            analyticalDrivers: [] as any[],
            condition: {} as any,
            conditions: [] as any[],
            lovs: [] as any[],
            modes: [] as any[],
            selectedModes: [] as any,
            modesToDelete: [] as any,
            modalities: {} as any,
            touched: false,
            conditionFormVisible: false,
            operation: 'insert',
            errorMessage: '',
            displayWarning: false,
            v$: useValidate() as any
        }
    },
    validations() {
        const customValidators: ICustomValidatorMap = {
            'custom-unique': (value: string) => {
                return this.urlNotUnique(value)
            }
        }

        const validationObject = {
            driver: createValidations('driver', businessModelDriverDetailValidationDescriptor.validations.driver, customValidators)
        }

        return validationObject
    },
    methods: {
        loadSelectedDriver() {
            this.driver = this.selectedDriver

            if (!this.selectedDriver.id) {
                this.v$.driver.label.$touch()
                this.v$.driver.parameter.$touch()
                this.v$.driver.parameterUrlName.$touch()
            }
        },
        loadAnalyticalDrivers() {
            this.analyticalDrivers = this.driverOptions as any[]
        },
        loadBusinessModelDrivers() {
            this.drivers = this.businessModelDrivers as any[]
        },
        async loadDataDependencies() {
            this.conditions = []
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.businessModelId}/datadependencies?driverId=${this.selectedDriver.id}`).then((response) =>
                response.data.forEach((condition: any) => {
                    const index = this.conditions.findIndex((cond) => cond.parFatherId === condition.parFatherId && cond.filterOperation == condition.filterOperation && cond.logicOperator == condition.logicOperator)
                    condition.modalities = []
                    condition.modalities.push({ conditionId: condition.id, useModeId: condition.useModeId, filterColumn: condition.filterColumn })
                    if (index > -1) {
                        this.conditions[index].modalities.push({ conditionId: condition.id, useModeId: condition.useModeId, filterColumn: condition.filterColumn })
                    } else {
                        this.conditions.push(condition)
                    }
                })
            )
            console.log('CONDITIONS!: ', this.conditions)
        },
        async loadModes() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/analyticalDrivers/${this.selectedDriver.parameter.id}/modes`).then((response) => (this.modes = response.data))
        },
        async loadLovs() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/analyticalDrivers/${this.selectedDriver.parameter.id}/lovs`).then((response) => (this.lovs = response.data))
        },
        getLovs(lovId: number) {
            const index = this.lovs.findIndex((lov) => lov.id === lovId)
            if (index > -1) {
                const lov = JSON.parse(this.lovs[index].lovProviderJSON)
                return lov.QUERY['VISIBLE-COLUMNS'].split(',')
            }
        },
        isModeActive(modeId: number) {
            const index = this.selectedModes.findIndex((id: any) => {
                return id === modeId
            })
            return !(index > -1)
        },
        urlNotUnique(url: string) {
            const index = this.drivers.findIndex((driver) => driver.parameterUrlName === url && driver.id != this.driver.id)

            return !(index > -1)
        },
        async saveCondition(condition: any) {
            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.businessModelId}/datadependencies`, condition).finally(() => (this.conditionFormVisible = false))
        },
        handleSubmit() {
            console.log('MODALITIES', this.modalities)
            console.log('CONDITION PASSED', this.condition)
            if (this.condition.id) {
                this.operation = 'update'
            }

            this.selectedModes.forEach((id: number) => {
                Object.keys(this.modalities).forEach((modalityId) => {
                    console.log(modalityId + ' ========== ' + id)
                    if (+modalityId === id) {
                        const conditionForPost = { ...this.condition, parFatherId: this.condition.parFather.id, parFatherUrlName: this.selectedDriver.parameterUrlName, parId: this.selectedDriver.id, useModeId: +modalityId, filterColumn: this.modalities[id] }
                        if (!conditionForPost.prog) {
                            conditionForPost.prog = 0
                        }
                        conditionForPost.prog++
                        delete conditionForPost.parFather
                        delete conditionForPost.modalities
                        this.sendRequest(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.businessModelId}/datadependencies`, conditionForPost)
                    }
                })
            })

            // TODO pitati za cekanje svih (prebaciti u for?)
            this.loadData()
        },
        sendRequest(url: string, condition: any) {
            if (this.operation === 'insert') {
                return axios.post(url, condition).then((response) => {
                    if (response.data.errors) {
                        this.errorMessage = response.data.errors[0].message
                        this.displayWarning = true
                    } else {
                        this.$store.commit('setInfo', {
                            title: this.$t(this.businessModelDriverDetailDescriptor.operation[this.operation].toastTitle),
                            msg: this.$t(this.businessModelDriverDetailDescriptor.operation.success)
                        })
                    }
                })
            } else {
                return axios.put(url, condition).then((response) => {
                    if (response.data.errors) {
                        this.errorMessage = response.data.errors[0].message
                        this.displayWarning = true
                    } else {
                        this.$store.commit('setInfo', {
                            title: this.$t(this.businessModelDriverDetailDescriptor.operation[this.operation].toastTitle),
                            msg: this.$t(this.businessModelDriverDetailDescriptor.operation.success)
                        })
                    }
                })
            }
        },
        showForm(event: any) {
            if (event.value) {
                this.selectedModes = []
                this.condition = { ...event.value, parFather: this.selectedDriver }
                this.condition.modalities.forEach((modality: any) => {
                    this.selectedModes.push(modality.useModeId)
                    this.modalities[modality.useModeId] = modality.filterColumn
                })
                console.log('SELECTED CONDITION', this.condition)
                console.log('SELECTED MODES', this.selectedModes)
            } else {
                this.selectedModes = []
                this.condition = {}
            }
            this.conditionFormVisible = true
        },
        setChanged() {
            this.driver.status = 'CHANGED'
            this.driver.numberOfErrors = this.v$.$errors.length
        },
        closeForm() {
            this.conditionFormVisible = false
        },
        showConditionDeleteDialog(condition) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteConditions(condition)
            })
        },
        async deleteConditions(condition: any) {
            console.log('CONDITIONSSSSSSS for delete', condition)
            condition.modalities.forEach((mode: any) => {
                this.deleteCondition({ ...condition, id: mode.conditionId, useModeId: mode.useModeId, filterColumn: mode.filterColumn })
            })
        },
        async deleteCondition(condition: any) {
            console.log('CONDITION for delete', condition)
            delete condition.modalities
            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/businessmodels/${this.businessModelId}/datadependencies/delete`, condition).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.loadData()
            })
        },
        loadData() {
            this.loadDataDependencies()
            this.loadModes()
            this.loadLovs()
            this.conditionFormVisible = false
        }
    }
})
</script>

<style lang="scss" scoped>
.mode-inputs {
    flex: 0.5;
}
#operationInfo {
    margin-top: 2rem;
    font-size: 0.8rem;
    text-transform: uppercase;
    display: flex;
    justify-content: center;
    border: 1px solid rgba(59, 103, 140, 0.1);
    background-color: #eaf0f6;

    p {
        margin: 0.3rem;
    }
}
</style>
