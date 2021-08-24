<template>
    <Card style="width:100%" class="p-m-2" v-if="mode.useID">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <InputText
                            id="label"
                            class="kn-material-input"
                            type="text"
                            v-model="v$.mode.label.$model"
                            :class="{
                                'p-invalid': v$.mode.label.$invalid && v$.mode.label.$dirty
                            }"
                            @blur="v$.mode.label.$touch()"
                            @input="modeChanged"
                        />
                        <label for="label" class="kn-material-input-label">{{ $t('common.label') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.mode.label" :additionalTranslateParams="{ fieldName: $t('common.label') }"></KnValidationMessages>
                </div>
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            class="kn-material-input"
                            type="text"
                            v-model="v$.mode.name.$model"
                            :class="{
                                'p-invalid': v$.mode.name.$invalid && v$.mode.name.$dirty
                            }"
                            @blur="v$.mode.name.$touch()"
                            @input="modeChanged"
                        />
                        <label for="name" class="kn-material-input-label">{{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.mode.name" :additionalTranslateParams="{ fieldName: $t('common.name') }"></KnValidationMessages>
                </div>
                <div class="p-field p-col-12">
                    <span class="p-float-label">
                        <InputText id="description" class="kn-material-input" type="text" v-model="mode.description" @input="modeChanged" />
                        <label for="description" class="kn-material-input-label">{{ $t('common.description') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-4">
                    <span class="p-float-label">
                        <Dropdown
                            id="type"
                            class="kn-material-input"
                            v-model="v$.mode.valueSelection.$model"
                            :options="useModeDescriptor.types"
                            optionLabel="name"
                            optionValue="valueSelection"
                            :class="{
                                'p-invalid': v$.mode.valueSelection.$invalid && v$.mode.valueSelection.$dirty
                            }"
                            @blur="v$.mode.valueSelection.$touch()"
                            @change="handelDefaul"
                        />
                        <label for="type" class="kn-material-input-label"> {{ $t('common.type') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.mode.valueSelection" :additionalTranslateParams="{ fieldName: $t('common.type') }"></KnValidationMessages>
                </div>
                <div class="p-field p-col-4">
                    <span class="p-float-label">
                        <Dropdown id="default" class="kn-material-input" v-model="selectedDefault" :options="defaults" optionLabel="name" optionValue="label" />
                        <label for="default" class="kn-material-input-label"> {{ $t('managers.driversManagement.useModes.defaultValue') }} * </label>
                    </span>
                </div>
                <div class="p-field p-col-4" v-if="isDate">
                    <span class="p-float-label">
                        <Dropdown id="max" class="kn-material-input" v-model="selectedMax" :options="useModeDescriptor.maxValues" optionLabel="name" optionValue="label" />
                        <label for="max" class="kn-material-input-label"> {{ $t('managers.driversManagement.useModes.maxValue') }} * </label>
                    </span>
                </div>
                <div class="p-field p-col-6" v-if="mode.valueSelection === 'lov'">
                    <span class="p-input-icon-right">
                        <span class="p-float-label">
                            <InputText id="lov" v-model="lov.name" class="kn-material-input" type="text" disabled />
                            <label for="lov" class="kn-material-input-label"> LOV * </label>
                        </span>
                        <i class="pi pi-search input-buton" @click="showLovsDialog" />
                    </span>
                    <!-- <KnValidationMessages class="p-mt-1" :vComp="vcomp.alertListener" :additionalTranslateParams="{ fieldName: $t('kpi.alert.kpiListener') }"></KnValidationMessages> -->
                </div>
                <div class="p-field p-col-6" v-if="mode.valueSelection === 'lov'">
                    <span class="p-float-label">
                        <Dropdown
                            id="type"
                            class="kn-material-input"
                            v-model="v$.mode.selectionType.$model"
                            :options="selectionTypes"
                            optionLabel="VALUE_NM"
                            optionValue="VALUE_CD"
                            :class="{
                                'p-invalid': v$.mode.selectionType.$invalid && v$.mode.selectionType.$dirty
                            }"
                            @blur="v$.mode.selectionType.$touch()"
                            @change="modeChanged"
                        />
                        <label for="type" class="kn-material-input-label"> {{ $t('managers.driversManagement.useModes.modality') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.mode.selectionType" :additionalTranslateParams="{ fieldName: $t('managers.driversManagement.useModes.modality') }"></KnValidationMessages>
                </div>
                <div class="p-field p-col-6" v-if="mode.valueSelection === 'map_in'">
                    <span class="p-float-label">
                        <Dropdown id="type" class="kn-material-input" v-model="mode.selectedLayer" :options="layers" optionLabel="name" optionValue="name" />
                        <label for="type" class="kn-material-input-label"> {{ $t('managers.driversManagement.useModes.layer') }} * </label>
                    </span>
                    <!-- <KnValidationMessages class="p-mt-1" :vComp="vcomp.alertListener" :additionalTranslateParams="{ fieldName: $t('kpi.alert.kpiListener') }"></KnValidationMessages> -->
                </div>
                <div class="p-field p-col-6" v-if="mode.valueSelection === 'map_in'">
                    <span class="p-float-label">
                        <InputText id="prop" class="kn-material-input" type="text" v-model="mode.selectedLayerProp" />
                        <label for="prop" class="kn-material-input-label">{{ $t('managers.driversManagement.useModes.layerProp') }} * </label>
                    </span>
                </div>
                <div class="p-field p-col-6" v-if="selectedDefault === 'lov'">
                    <span class="p-input-icon-right">
                        <span class="p-float-label">
                            <InputText id="lov" class="kn-material-input" type="text" disabled />
                            <label for="lov" class="kn-material-input-label"> LOV * </label>
                        </span>
                        <i class="pi pi-search input-buton" @click="showLovsDialog" />
                    </span>
                    <!-- <KnValidationMessages class="p-mt-1" :vComp="vcomp.alertListener" :additionalTranslateParams="{ fieldName: $t('kpi.alert.kpiListener') }"></KnValidationMessages> -->
                </div>
                <div class="p-field p-col-6" v-if="selectedDefault === 'pickUp'">
                    <span class="p-float-label">
                        <Dropdown id="defaultFormula" class="kn-material-input" v-model="mode.defaultFormula" :options="useModeDescriptor.defaultFormula" optionLabel="name" optionValue="f_value" />
                        <label for="defaultFormula" class="kn-material-input-label"> {{ $t('managers.driversManagement.useModes.selectDefaultFormula') }} * </label>
                    </span>
                    <!-- <KnValidationMessages class="p-mt-1" :vComp="vcomp.alertListener" :additionalTranslateParams="{ fieldName: $t('kpi.alert.kpiListener') }"></KnValidationMessages> -->
                </div>
                <div class="p-field p-col-6" v-if="selectedMax === 'lov'">
                    <span class="p-input-icon-right">
                        <span class="p-float-label">
                            <InputText id="lov" class="kn-material-input" type="text" disabled />
                            <label for="lov" class="kn-material-input-label"> LOV * </label>
                        </span>
                        <i class="pi pi-search input-buton" @click="showLovsDialog" />
                    </span>
                    <!-- <KnValidationMessages class="p-mt-1" :vComp="vcomp.alertListener" :additionalTranslateParams="{ fieldName: $t('kpi.alert.kpiListener') }"></KnValidationMessages> -->
                </div>
            </form>
            <LovsDialog :dialogVisible="dialogVisiable" :lovs="lovs" :selectedLovProp="selectedLov" @close="dialogVisiable = false"></LovsDialog>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import Dropdown from 'primevue/dropdown'
import useValidate from '@vuelidate/core'
import useModeDescriptor from './UseModesDescriptor.json'
import useModeValidationtDescriptor from './UseModeValidationDescriptor.json'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import LovsDialog from './LovsDialog.vue'
export default defineComponent({
    name: 'detail-card',
    components: { Dropdown, KnValidationMessages, LovsDialog },
    props: {
        selectedMode: {
            type: Object,
            required: false
        },
        selectedLov: {
            type: Object,
            required: false
        },
        selectionTypes: {
            type: Array,
            requierd: true
        },
        layers: {
            type: Array,
            requierd: true
        },
        lovs: {
            type: Array,
            requierd: true
        },
        isDate: {
            type: Boolean,
            requierd: true
        }
    },
    data() {
        return {
            mode: {} as any,
            useModeDescriptor,
            selectedType: null,
            selectedDefault: null as any,
            selectedMax: null as any,
            lov: null as any,
            dialogVisiable: false,
            v$: useValidate() as any,
            useModeValidationtDescriptor
        }
    },
    validations() {
        const validationObject = {
            mode: createValidations('mode', useModeValidationtDescriptor.validations.mode)
        }
        return validationObject
    },
    computed: {
        defaults(): any {
            if (this.mode.valueSelection === 'map_in') {
                return this.useModeDescriptor.defaultValues.filter((type) => type.label != 'pickUp')
            }
            return this.useModeDescriptor.defaultValues
        }
    },
    watch: {
        selectedMode() {
            this.v$.$reset()
            this.mode = this.selectedMode as any
            this.lov = this.selectedLov as any
            this.handleDropdowns()
            this.v$.$touch()
            this.modeChanged()
        }
    },
    mounted() {
        if (this.selectedMode) {
            this.mode = this.selectedMode as any
            this.handleDropdowns()
        }
        this.lov = this.selectedLov as any
        this.v$.$touch()
        this.modeChanged()
    },
    methods: {
        showLovsDialog() {
            console.log('LOVs dialog')
            this.dialogVisiable = true
        },
        handleDropdowns() {
            if (this.mode.defaultFormula == null) {
                this.selectedDefault = 'none'
            } else {
                this.selectedDefault = 'pickUp'
            }
            if (this.mode.idLovForDefault != null) {
                this.selectedDefault = 'lov'
            }

            if (this.mode.idLovForMax != null) {
                this.selectedMax = 'lov'
            } else {
                this.selectedMax = 'none'
            }
        },
        handelDefaul() {
            if (this.mode.valueSelection == 'map_in') {
                this.selectedDefault = null
            }
            this.modeChanged()
        },
        setDirty() {
            this.$emit('touched')
        },
        modeChanged() {
            this.mode.numberOfErrors = this.v$.$errors.length
        }
    }
})
</script>
<style lang="scss">
.input-buton {
    cursor: pointer;
}
</style>
