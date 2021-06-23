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
            <form class="p-fluid p-m-5" v-if="selectedDriver">
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
                    <span class="p-float-label">
                        <Dropdown
                            id="parameter"
                            class="kn-material-input"
                            :class="{
                                'p-invalid': v$.driver.parameter.$invalid && v$.driver.parameter.$dirty
                            }"
                            v-model="v$.driver.parameter.$model"
                            :options="analyticalDrivers"
                            :filter="true"
                            @before-show="v$.driver.parameter.$touch()"
                            @change="setDirty"
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
                        <label for="parameter" class="kn-material-input-label">{{ $t('managers.buisnessModelCatalogue.analyticalDriver') }} * </label>
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
                    <InputSwitch id="driver-multivalue " class="p-mr-2" v-model="driver.multivalue" />
                    <i class="fa fa-list p-mr-2" />
                    <label for="driver-multivalue " class="kn-material-input-label"> {{ $t('managers.buisnessModelCatalogue.multivalue') }}</label>
                </div>
            </form>
        </template>
    </Card>

    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('managers.buisnessModelCatalogue.driverDataConditions') }}
                </template>
                <template #right>
                    <Button class="kn-button p-button-text" @click="showForm">{{ $t('managers.buisnessModelCatalogue.addCondition') }}</Button>
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
                            </div>
                        </template>
                    </Listbox>
                </div>
            </div>
        </template>
    </Card>

    <div v-if="conditionFormVisible">
        <Dialog :visible="true" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :header="$t('managers.buisnessModelCatalogue.driverDataConditions')" :closable="false">
            <div>
                <p>{{ $t('managers.buisnessModelCatalogue.operationInfo', { driver: this.driver.label }) }}</p>
            </div>
            <form class="p-fluid p-m-5">
                <div class="p-field p-d-flex">
                    <div :style="businessModelDriverDetailDescriptor.input.parFather.style">
                        <span class="p-float-label">
                            <Dropdown id="parFather" class="kn-material-input" v-model="condition.parFather" :options="drivers">
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

                    <div :style="businessModelDriverDetailDescriptor.input.logicalOperator.style">
                        <span class="p-float-label">
                            <Dropdown id="logicalOperator" class="kn-material-input" v-model="condition.logicalOperator" :options="businessModelDriverDetailDescriptor.logicalOperators" optionLabel="name" optionValue="value" />
                            <label for="logicalOperator" class="kn-material-input-label">{{ $t('managers.buisnessModelCatalogue.logicalOperator') }}</label>
                        </span>
                    </div>
                </div>
            </form>
            <template #footer>
                <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="closeForm"></Button>
                <Button class="kn-button kn-button--primary" :label="$t('common.save')" @click="handleSubmit"></Button>
            </template>
        </Dialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import businessModelDriverDetailDescriptor from './BusinessModelDriverDetailDescriptor.json'
import businessModelDriverDetailValidationDescriptor from './BusinessModelDriverDetailValidationDescriptor.json'
import Card from 'primevue/card'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Listbox from 'primevue/listbox'
import useValidate from '@vuelidate/core'

export default defineComponent({
    name: 'business-model-driver-detail-card',
    components: {
        Card,
        Dialog,
        Dropdown,
        InputSwitch,
        KnValidationMessages,
        Listbox
    },
    props: {
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
        },
        dataDependencies: {
            type: Array,
            required: true
        }
    },
    emits: ['touched'],
    watch: {
        selectedDriver() {
            this.loadSelectedDriver()
        },
        driverOptions() {
            this.loadAnalyticalDrivers()
        },
        businessModelDrivers() {
            this.loadBusinessModelDrivers()
        },
        dataDependencies() {
            this.loadDataDependencies()
        }
    },
    created() {
        this.loadSelectedDriver()
        this.loadAnalyticalDrivers()
        this.loadBusinessModelDrivers()
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
            touched: false,
            conditionFormVisible: false,
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
        },
        loadAnalyticalDrivers() {
            this.analyticalDrivers = this.driverOptions as any[]
        },
        loadBusinessModelDrivers() {
            this.drivers = this.businessModelDrivers as any[]
        },
        loadDataDependencies() {
            this.conditions = this.dataDependencies as any[]
        },
        urlNotUnique(url: string) {
            const index = this.drivers.findIndex((driver) => driver.parameterUrlName === url && driver.id != this.driver.id)

            return index > -1 ? false : true
        },
        showForm() {
            this.conditionFormVisible = true
        },
        closeForm() {
            this.conditionFormVisible = false
        },
        setDirty(): void {
            this.$emit('touched')
        }
    }
})
</script>
