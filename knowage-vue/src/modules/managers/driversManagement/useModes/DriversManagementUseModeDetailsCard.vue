<template>
    <Card class="p-col-12" v-if="mode.useID">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-col-12 p-d-flex p-flex-wrap p-mt-3">
                    <div class="p-field p-col-6">
                        <span class="p-float-label">
                            <InputText
                                id="label"
                                class="kn-material-input"
                                type="text"
                                v-model.trim="v$.mode.label.$model"
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
                                v-model.trim="v$.mode.name.$model"
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
                            <InputText id="description" class="kn-material-input" type="text" v-model.trim="mode.description" @input="modeChanged" />
                            <label for="description" class="kn-material-input-label">{{ $t('common.description') }} </label>
                        </span>
                    </div>
                </div>
                <div class="p-col-12 p-d-flex p-flex-wrap">
                    <div class="p-field p-col-3 p-sm-12 p-md-4">
                        <span class="p-float-label">
                            <Dropdown
                                id="type"
                                class="kn-material-input"
                                v-model="v$.mode.valueSelection.$model"
                                :options="availableTypes"
                                optionLabel="name"
                                optionValue="valueSelection"
                                :class="{
                                    'p-invalid': v$.mode.valueSelection.$invalid && v$.mode.valueSelection.$dirty
                                }"
                                @blur="v$.mode.valueSelection.$touch()"
                                @change="setType"
                            />
                            <label for="type" class="kn-material-input-label"> {{ $t('common.type') }} * </label>
                        </span>
                        <KnValidationMessages class="p-mt-1" :vComp="v$.mode.valueSelection" :additionalTranslateParams="{ fieldName: $t('common.type') }"></KnValidationMessages>
                    </div>
                    <div class="p-col-9 p-sm-12 p-md-8 p-fluid p-formgrid p-grid" v-show="mode.valueSelection === 'lov'">
                        <div class="p-field p-col-6 p-sm-12 p-md-6">
                            <span class="p-input-icon-right">
                                <span class="p-float-label">
                                    <InputText id="lov" v-model="mode.typeLov.name" class="kn-material-input" type="text" disabled />
                                    <label for="lov" class="kn-material-input-label"> {{ $t('managers.driversManagement.useModes.lov') }} * </label>
                                </span>
                                <i class="pi pi-search input-buton" @click="showLovsDialog('type')" />
                            </span>
                            <KnValidationMessages class="p-mt-1" :vComp="v$.mode.typeLov" :additionalTranslateParams="{ fieldName: $t('managers.driversManagement.useModes.lov') }" :specificTranslateKeys="{ required_lovId_for_lov: 'common.validation.required' }"></KnValidationMessages>
                        </div>
                        <div class="p-field p-col-6 p-sm-12 p-md-6">
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
                            <KnValidationMessages class="p-mt-1" :vComp="v$.mode.selectionType" :additionalTranslateParams="{ fieldName: $t('managers.driversManagement.useModes.modality') }" :specificTranslateKeys="{ required_type_for_lov: 'common.validation.required' }"></KnValidationMessages>
                        </div>
                    </div>
                    <div class="p-col-9 p-sm-12 p-md-8 p-fluid p-formgrid p-grid" v-show="mode.valueSelection === 'map_in'">
                        <div class="p-field p-col-6 p-sm-12 p-md-6">
                            <span class="p-float-label">
                                <Dropdown
                                    id="type"
                                    class="kn-material-input"
                                    v-model="v$.mode.selectedLayer.$model"
                                    :options="layers"
                                    optionLabel="name"
                                    optionValue="name"
                                    :class="{
                                        'p-invalid': v$.mode.selectedLayer.$invalid && v$.mode.selectedLayer.$dirty
                                    }"
                                    @blur="v$.mode.selectedLayer.$touch()"
                                    @change="modeChanged"
                                />
                                <label for="type" class="kn-material-input-label"> {{ $t('managers.driversManagement.useModes.layer') }} * </label>
                            </span>
                            <KnValidationMessages class="p-mt-1" :vComp="v$.mode.selectedLayer" :additionalTranslateParams="{ fieldName: $t('managers.driversManagement.useModes.layer') }" :specificTranslateKeys="{ required_for_map_in: 'common.validation.required' }"></KnValidationMessages>
                        </div>
                        <div class="p-field p-col-6 p-sm-12 p-md-6">
                            <span class="p-float-label">
                                <InputText id="prop" class="kn-material-input" type="text" v-model="mode.selectedLayerProp" />
                                <label for="prop" class="kn-material-input-label">{{ $t('managers.driversManagement.useModes.layerProp') }} </label>
                            </span>
                        </div>
                    </div>
                </div>
                <div class="p-col-12 p-d-flex p-flex-wrap">
                    <div class="p-field p-col-4 p-sm-12 p-md-4">
                        <span class="p-float-label">
                            <Dropdown id="default" class="kn-material-input" v-model="selectedDefault" :options="defaults" optionLabel="name" optionValue="label" @change="setDefault" />
                            <label for="default" class="kn-material-input-label"> {{ $t('managers.driversManagement.useModes.defaultValue') }} * </label>
                        </span>
                    </div>

                    <div class="p-field p-col-8 p-sm-12 p-md-8" v-show="selectedDefault === 'lov'">
                        <span class="p-input-icon-right">
                            <span class="p-float-label">
                                <InputText id="lov" v-model="mode.defLov.name" class="kn-material-input" type="text" disabled />
                                <label for="lov" class="kn-material-input-label"> {{ $t('managers.driversManagement.useModes.lov') }} * </label>
                            </span>
                            <i class="pi pi-search input-buton" @click="showLovsDialog('default')" />
                        </span>
                        <KnValidationMessages class="p-mt-1" :vComp="v$.mode.defLov" :additionalTranslateParams="{ fieldName: $t('managers.driversManagement.useModes.lov') }" :specificTranslateKeys="{ required_lov_for_default: 'common.validation.required' }"></KnValidationMessages>
                    </div>
                    <div class="p-field p-col-8 p-sm-12 p-md-8" v-show="selectedDefault === 'pickUp'">
                        <span class="p-float-label">
                            <Dropdown
                                id="defaultFormula"
                                class="kn-material-input"
                                v-model="v$.mode.defaultFormula.$model"
                                :options="useModeDescriptor.defaultFormula"
                                optionLabel="name"
                                optionValue="f_value"
                                :class="{
                                    'p-invalid': v$.mode.defaultFormula.$invalid && v$.mode.defaultFormula.$dirty
                                }"
                                @blur="v$.mode.defaultFormula.$touch()"
                                @change="modeChanged"
                            />
                            <label for="defaultFormula" class="kn-material-input-label"> {{ $t('managers.driversManagement.useModes.selectDefaultFormula') }} * </label>
                        </span>
                        <KnValidationMessages
                            class="p-mt-1"
                            :vComp="v$.mode.defaultFormula"
                            :additionalTranslateParams="{ fieldName: $t('managers.driversManagement.useModes.selectDefaultFormula') }"
                            :specificTranslateKeys="{ required_for_pick_up: 'common.validation.required' }"
                        ></KnValidationMessages>
                    </div>
                </div>
                <div class="p-col-12 p-d-flex p-flex-wrap">
                    <div class="p-field p-col-4 p-sm-12 p-md-4" v-show="isDate">
                        <span class="p-float-label">
                            <Dropdown id="max" class="kn-material-input" v-model="selectedMax" :options="useModeDescriptor.maxValues" optionLabel="name" optionValue="label" @change="setMax" />
                            <label for="max" class="kn-material-input-label"> {{ $t('managers.driversManagement.useModes.maxValue') }} * </label>
                        </span>
                    </div>
                    <div class="p-field p-col-6 p-sm-12 p-md-6" v-show="selectedMax === 'lov'">
                        <span class="p-input-icon-right">
                            <span class="p-float-label">
                                <InputText id="lov" v-model="mode.maxLov.name" class="kn-material-input" type="text" disabled />
                                <label for="lov" class="kn-material-input-label"> {{ $t('managers.driversManagement.useModes.lov') }} * </label>
                            </span>
                            <i class="pi pi-search input-buton" @click="showLovsDialog('max')" />
                        </span>
                        <KnValidationMessages class="p-mt-1" :vComp="v$.mode.maxLov" :additionalTranslateParams="{ fieldName: $t('managers.driversManagement.useModes.lov') }" :specificTranslateKeys="{ required_lov_for_max: 'common.validation.required' }"></KnValidationMessages>
                    </div>
                </div>
            </form>
            <LovsDialog :dialogVisible="dialogVisiable" :lovs="lovs" :selectedLovProp="lov" @close="dialogVisiable = false" @apply="applyLov"></LovsDialog>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import Dropdown from 'primevue/dropdown'
import useValidate from '@vuelidate/core'
import useModeDescriptor from './UseModesDescriptor.json'
import useModeValidationtDescriptor from './UseModeValidationDescriptor.json'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import LovsDialog from './DriversManagementLovsDialog.vue'
export default defineComponent({
    name: 'detail-card',
    components: { Dropdown, KnValidationMessages, LovsDialog },
    props: {
        selectedMode: {
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
            required: true
        },
        showMapDriver: {
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
            lovType: null as any,
            dialogVisiable: false,
            v$: useValidate() as any,
            useModeValidationtDescriptor
        }
    },
    validations() {
        const customValidators: ICustomValidatorMap = {
            required_type_for_lov: () => {
                return this.mode.valueSelection != 'lov' || this.mode.selectionType != null
            },
            required_lovId_for_lov: () => {
                return this.mode.valueSelection != 'lov' || this.mode.typeLov.name != null
            },
            required_for_map_in: () => {
                return this.mode.valueSelection != 'map_in' || (this.mode.selectedLayer != '' && this.mode.selectedLayer != null)
            },
            required_for_pick_up: () => {
                return this.selectedDefault != 'pickUp' || (this.mode.defaultFormula != '' && this.mode.defaultFormula != null)
            },
            required_lov_for_default: () => {
                return this.selectedDefault != 'lov' || this.mode.defLov.name != null
            },
            required_lov_for_max: () => {
                return this.selectedMax != 'lov' || this.mode.maxLov.name != null
            }
        }
        const validationObject = {
            mode: createValidations('mode', useModeValidationtDescriptor.validations.mode, customValidators)
        }
        return validationObject
    },
    computed: {
        defaults(): any {
            if (this.mode.valueSelection === 'map_in') {
                return this.useModeDescriptor.defaultValues.filter((type) => type.label != 'pickUp')
            }
            return this.useModeDescriptor.defaultValues
        },
        availableTypes(): any {
            if (!this.showMapDriver) {
                return this.useModeDescriptor.types.filter((type) => type.valueSelection != 'map_in')
            }
            return this.useModeDescriptor.types
        }
    },
    watch: {
        selectedMode() {
            this.v$.$reset()
            this.mode = this.selectedMode as any
            this.handleDropdowns()
            this.v$.$touch()
            setTimeout(() => {
                this.modeChanged()
            }, 500)

            this.handleLovs()
        }
    },
    mounted() {
        if (this.selectedMode) {
            this.mode = this.selectedMode as any
            this.handleDropdowns()
        }
        this.v$.$touch()
        this.handleLovs()
        setTimeout(() => {
            this.modeChanged()
        }, 500)
    },
    methods: {
        showLovsDialog(lovType: string) {
            this.dialogVisiable = true
            switch (lovType) {
                case 'type':
                    this.lov = this.mode.typeLov
                    this.lovType = 'type'
                    break
                case 'default':
                    this.lov = this.mode.defLov
                    this.lovType = 'default'
                    break
                case 'max':
                    this.lov = this.mode.maxLov
                    this.lovType = 'max'
                    break
            }
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
        setType() {
            this.selectedDefault = 'none'
            switch (this.mode.valueSelection) {
                case 'lov':
                    this.mode.manualInput = 0
                    this.mode.selectedLayer = null
                    this.mode.selectedLayerProp = null
                    break
                case 'man_in':
                    this.mode.idLov = null
                    this.mode.typeLov = { name: null }
                    this.mode.selectionType = null
                    this.mode.selectedLayer = null
                    this.mode.selectedLayerProp = null
                    break
                case 'map_in':
                    this.mode.idLov = null
                    this.mode.typeLov = { name: null }
                    this.mode.selectionType = null
                    this.mode.manualInput = 0
                    break
            }
            this.modeChanged()
        },
        setDefault() {
            switch (this.selectedDefault) {
                case 'none':
                    this.mode.defaultFormula = null
                    this.mode.idLovForDefault = null
                    this.mode.defLov = { name: null }
                    break
                case 'lov':
                    this.mode.defaultFormula = null
                    break
                case 'pickUp':
                    this.mode.idLovForDefault = null
                    this.mode.defLov = { name: null }
                    break
            }
            this.modeChanged()
        },
        setMax() {
            if (this.selectedMax == 'none') {
                this.mode.idLovForMax = null
                this.mode.maxLov = { name: null }
            }
            this.modeChanged()
        },
        setDirty() {
            this.$emit('touched')
        },
        modeChanged() {
            this.mode.numberOfErrors = this.v$.$errors.length
            this.mode.edited = true
        },
        applyLov(lov: any) {
            this.dialogVisiable = false
            switch (this.lovType) {
                case 'type':
                    this.mode.typeLov = lov
                    this.mode.idLov = lov.id
                    break
                case 'default':
                    this.mode.defLov = lov
                    this.mode.idLovForDefault = lov.id
                    break
                case 'max':
                    this.mode.maxLov = lov
                    this.mode.idLovForMax = lov.id
                    break
            }
            this.modeChanged()
        },
        handleLovs() {
            if (this.mode.idLov) {
                this.mode.typeLov = this.lovs?.filter((lov: any) => lov.id == this.mode.idLov)[0]
            } else this.mode.typeLov = { name: null }
            if (this.mode.idLovForDefault) {
                this.mode.defLov = this.lovs?.filter((lov: any) => lov.id == this.mode.idLovForDefault)[0]
            } else this.mode.defLov = { name: null }
            if (this.mode.idLovForMax) {
                this.mode.maxLov = this.lovs?.filter((lov: any) => lov.id == this.mode.idLovForMax)[0]
            } else this.mode.maxLov = { name: null }
        }
    }
})
</script>
<style lang="scss">
.input-buton {
    cursor: pointer;
}
</style>
