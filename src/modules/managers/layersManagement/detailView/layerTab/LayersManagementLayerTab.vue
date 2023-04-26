<template>
    <Card id="basic-info-card">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText
                            id="label"
                            v-model="v$.layer.label.$model"
                            class="kn-material-input"
                            type="text"
                            max-length="100"
                            :class="{
                                'p-invalid': v$.layer.label.$invalid && v$.layer.label.$dirty
                            }"
                            @blur="v$.layer.label.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.label') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="v$.layer.label" :additional-translate-params="{ fieldName: $t('common.label') }" />
                </div>
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            v-model="v$.layer.name.$model"
                            class="kn-material-input"
                            type="text"
                            max-length="100"
                            :class="{
                                'p-invalid': v$.layer.name.$invalid && v$.layer.name.$dirty
                            }"
                            @blur="v$.layer.name.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="v$.layer.name" :additional-translate-params="{ fieldName: $t('common.name') }" />
                </div>
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <Textarea id="descr" v-model="layer.descr" class="kn-material-input" rows="1" max-length="100" :auto-resize="true" @change="$emit('touched')" />
                        <label for="descr" class="kn-material-input-label"> {{ $t('common.description') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <Dropdown id="category" v-model="layer.category_id" class="kn-material-input" :options="allCategories" option-label="VALUE_NM" option-value="VALUE_ID" :show-clear="true" @change="onTypeChange" />
                        <label for="category" class="kn-material-input-label"> {{ $t('common.category') }}</label>
                    </span>
                </div>
            </form>
        </template>
    </Card>
    <Card id="basic-layer-options-card" class="p-mt-3">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText
                            id="label"
                            v-model="v$.layer.layerLabel.$model"
                            class="kn-material-input"
                            type="text"
                            max-length="100"
                            :class="{
                                'p-invalid': v$.layer.layerLabel.$invalid && v$.layer.layerLabel.$dirty
                            }"
                            @blur="v$.layer.layerLabel.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="label" class="kn-material-input-label"> {{ $t('managers.layersManagement.layerLabel') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="v$.layer.layerLabel" :additional-translate-params="{ fieldName: $t('managers.layersManagement.layerLabel') }" />
                </div>
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            v-model="v$.layer.layerName.$model"
                            class="kn-material-input"
                            type="text"
                            max-length="100"
                            :class="{
                                'p-invalid': v$.layer.layerName.$invalid && v$.layer.layerName.$dirty
                            }"
                            @blur="v$.layer.layerName.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="name" class="kn-material-input-label"> {{ $t('managers.layersManagement.layerName') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="v$.layer.layerName" :additional-translate-params="{ fieldName: $t('managers.layersManagement.layerName') }" />
                </div>
                <div class="p-col-12 p-lg-4 p-mt-3">
                    <span class="p-as-center">
                        <InputSwitch id="baseLayer" v-model="layer.baseLayer" />
                        <label for="baseLayer" class="kn-material-input-label p-ml-2"> {{ $t('managers.layersManagement.baseLayer') }}</label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-lg-4">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            v-model="v$.layer.layerIdentify.$model"
                            class="kn-material-input"
                            type="text"
                            max-length="100"
                            :class="{
                                'p-invalid': v$.layer.layerIdentify.$invalid && v$.layer.layerIdentify.$dirty
                            }"
                            @blur="v$.layer.layerIdentify.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="name" class="kn-material-input-label"> {{ $t('managers.layersManagement.layerIdentify') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="v$.layer.layerIdentify" :additional-translate-params="{ fieldName: $t('managers.layersManagement.layerIdentify') }" />
                </div>
                <div class="p-field p-col-12 p-lg-4">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            v-model="v$.layer.layerOrder.$model"
                            class="kn-material-input"
                            type="number"
                            min="0"
                            :class="{
                                'p-invalid': v$.layer.layerOrder.$invalid && v$.layer.layerOrder.$dirty
                            }"
                            @blur="v$.layer.layerOrder.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="name" class="kn-material-input-label"> {{ $t('managers.layersManagement.layerOrder') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="v$.layer.layerOrder" :additional-translate-params="{ fieldName: $t('managers.layersManagement.layerOrder') }" />
                </div>
                <div class="p-col-12">
                    <span class="p-float-label">
                        <MultiSelect id="layerRoles" v-model="layer.roles" class="kn-material-input" :options="allRoles" option-label="name" display="chip" :filter="true" />
                        <label for="layerRoles" class="kn-material-input-label"> {{ $t('common.roles') }} </label>
                    </span>
                </div>
            </form>
        </template>
    </Card>
    <Card id="layer-type-card" class="p-mt-3">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12">
                    <span class="p-float-label">
                        <Dropdown
                            id="layerType"
                            v-model="v$.layer.type.$model"
                            class="kn-material-input"
                            :options="layerTypes"
                            option-label="label"
                            option-value="value"
                            :class="{
                                'p-invalid': v$.layer.type.$invalid && v$.layer.type.$dirty
                            }"
                            :disabled="layer.layerId"
                            @blur="v$.layer.type.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="layerType" class="kn-material-input-label"> {{ $t('common.type') }} *</label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="v$.layer.type" :additional-translate-params="{ fieldName: $t('common.type') }" />
                </div>
                <div v-if="layer.type == 'File'" class="p-field p-col-12 p-d-flex">
                    <div class="kn-flex">
                        <span class="p-float-label">
                            <InputText id="fileName" v-model="layer.pathFile" class="kn-material-input" :disabled="true" />
                            <label for="fileName" class="kn-material-input-label"> {{ $t('managers.layersManagement.fileLocation') }} </label>
                        </span>
                    </div>
                    <Button icon="fas fa-upload" class="p-button-text p-button-plain p-ml-2" @click="setUploadType" />
                    <KnInputFile v-if="!uploading" :change-function="uploadLayerFile" accept=".json" :trigger-input="triggerUpload" />
                </div>
                <div v-if="layer.type == 'WFS' || layer.type == 'WMS' || layer.type == 'TMS'" class="p-field p-col-12">
                    <span class="p-float-label">
                        <InputText
                            id="label"
                            v-model="v$.layer.layerURL.$model"
                            class="kn-material-input"
                            type="text"
                            max-length="100"
                            :class="{
                                'p-invalid': v$.layer.layerURL.$invalid && v$.layer.layerURL.$dirty
                            }"
                            @blur="v$.layer.layerURL.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="label" class="kn-material-input-label"> {{ $t('managers.layersManagement.layerURL') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="v$.layer.layerURL" :additional-translate-params="{ fieldName: $t('managers.layersManagement.layerURL') }" />
                </div>
                <div v-if="layer.type == 'Google' || layer.type == 'WMS' || layer.type == 'TMS'" :class="{ 'p-lg-6': layer.type == 'WMS', 'p-lg-12': layer.type != 'WMS' }" class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText id="label" v-model="layer.layerOptions" class="kn-material-input" type="text" max-length="100" @change="$emit('touched')" />
                        <label for="label" class="kn-material-input-label"> {{ $t('managers.layersManagement.layerOptions') }} </label>
                    </span>
                </div>
                <div v-if="layer.type == 'WMS'" class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText id="label" v-model="layer.layerParams" class="kn-material-input" type="text" max-length="100" @change="$emit('touched')" />
                        <label for="label" class="kn-material-input-label"> {{ $t('managers.layersManagement.layerParams') }} </label>
                    </span>
                </div>
            </form>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import descriptor from './LayersManagementLayerTabDescriptor.json'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Textarea from 'primevue/textarea'
import MultiSelect from 'primevue/multiselect'
import Dropdown from 'primevue/dropdown'
import KnInputFile from '@/components/UI/KnInputFile.vue'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    components: { KnValidationMessages, Textarea, MultiSelect, Dropdown, KnInputFile, InputSwitch },
    props: { selectedLayer: { type: Object, required: true }, allRoles: { type: Array, required: true }, allCategories: { type: Array, required: true } },
    emits: [],
    data() {
        return {
            v$: useValidate() as any,
            descriptor,
            layer: {} as any,
            triggerUpload: false,
            uploading: false,
            layerTypes: [
                {
                    label: this.$t('common.file'),
                    value: 'File'
                },
                {
                    label: this.$t('managers.layersManagement.layerTypes.wfs'),
                    value: 'WFS'
                },
                {
                    label: this.$t('managers.layersManagement.layerTypes.wms'),
                    value: 'WMS'
                },
                {
                    label: this.$t('managers.layersManagement.layerTypes.tms'),
                    value: 'TMS'
                },
                {
                    label: this.$t('managers.layersManagement.layerTypes.google'),
                    value: 'Google'
                },
                {
                    label: this.$t('managers.layersManagement.layerTypes.osm'),
                    value: 'OSM'
                }
            ]
        }
    },
    computed: {},
    watch: {
        selectedLayer() {
            this.layer = this.selectedLayer
        }
    },
    async created() {
        this.layer = this.selectedLayer
    },
    validations() {
        const urlRequried = (value) => {
            const types = ['WFS', 'WMS', 'TMS']
            return !types.includes(this.layer.type) || value
        }
        const customValidators: ICustomValidatorMap = { 'url-required': urlRequried }
        const validationObject = { layer: createValidations('layer', this.descriptor.validations.layer as any, customValidators) }
        return validationObject
    },
    methods: {
        setUploadType() {
            this.triggerUpload = false
            setTimeout(() => (this.triggerUpload = true), 200)
        },
        uploadLayerFile(event) {
            this.uploading = true
            const uploadedFile = event.target.files[0]
            this.layer.layerFile = { file: uploadedFile, fileName: uploadedFile.name }
            this.triggerUpload = false
            setTimeout(() => (this.uploading = false), 200)
        }
    }
})
</script>
