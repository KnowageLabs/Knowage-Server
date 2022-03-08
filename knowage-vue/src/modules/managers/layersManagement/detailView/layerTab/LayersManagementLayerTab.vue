<template>
    <Card id="basic-info-card">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText
                            id="label"
                            class="kn-material-input"
                            type="text"
                            maxLength="100"
                            v-model="v$.layer.label.$model"
                            :class="{
                                'p-invalid': v$.layer.label.$invalid && v$.layer.label.$dirty
                            }"
                            @blur="v$.layer.label.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.label') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.layer.label" :additionalTranslateParams="{ fieldName: $t('common.label') }" />
                </div>
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            class="kn-material-input"
                            type="text"
                            maxLength="100"
                            v-model="v$.layer.name.$model"
                            :class="{
                                'p-invalid': v$.layer.name.$invalid && v$.layer.name.$dirty
                            }"
                            @blur="v$.layer.name.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.layer.name" :additionalTranslateParams="{ fieldName: $t('common.name') }" />
                </div>
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <Textarea id="descr" class="kn-material-input" rows="1" maxLength="100" :autoResize="true" v-model="layer.descr" @change="$emit('touched')" />
                        <label for="descr" class="kn-material-input-label"> {{ $t('common.description') }} </label>
                    </span>
                </div>
                <!-- TODO: Category dropdown, ima neku cudnu logiku -->
                <!-- <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <Dropdown
                            id="type"
                            class="kn-material-input"
                            v-model="v$.layer.typeCode.$model"
                            :options="layerTypes"
                            optionLabel="translatedValueName"
                            optionValue="valueCd"
                            :class="{
                                'p-invalid': v$.layer.typeCode.$invalid && v$.layer.typeCode.$dirty
                            }"
                            @blur="v$.layer.typeCode.$touch()"
                            @change="onTypeChange"
                        />
                        <label for="type" class="kn-material-input-label"> {{ $t('importExport.catalogFunction.column.type') }} *</label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.layer.typeCode" :additionalTranslateParams="{ fieldName: $t('importExport.catalogFunction.column.type') }" />
                </div> -->
            </form>
        </template>
    </Card>

    <Card id="basic-layer-options-card" class="p-mt-2">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText
                            id="label"
                            class="kn-material-input"
                            type="text"
                            maxLength="100"
                            v-model="v$.layer.layerLabel.$model"
                            :class="{
                                'p-invalid': v$.layer.layerLabel.$invalid && v$.layer.layerLabel.$dirty
                            }"
                            @blur="v$.layer.layerLabel.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="label" class="kn-material-input-label"> {{ $t('managers.layersManagement.layerLabel') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.layer.layerLabel" :additionalTranslateParams="{ fieldName: $t('managers.layersManagement.layerLabel') }" />
                </div>
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            class="kn-material-input"
                            type="text"
                            maxLength="100"
                            v-model="v$.layer.layerName.$model"
                            :class="{
                                'p-invalid': v$.layer.layerName.$invalid && v$.layer.layerName.$dirty
                            }"
                            @blur="v$.layer.layerName.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="name" class="kn-material-input-label"> {{ $t('managers.layersManagement.layerName') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.layer.layerName" :additionalTranslateParams="{ fieldName: $t('managers.layersManagement.layerName') }" />
                </div>
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            class="kn-material-input"
                            type="text"
                            maxLength="100"
                            v-model="v$.layer.layerIdentify.$model"
                            :class="{
                                'p-invalid': v$.layer.layerIdentify.$invalid && v$.layer.layerIdentify.$dirty
                            }"
                            @blur="v$.layer.layerIdentify.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="name" class="kn-material-input-label"> {{ $t('managers.layersManagement.layerIdentify') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.layer.layerIdentify" :additionalTranslateParams="{ fieldName: $t('managers.layersManagement.layerIdentify') }" />
                </div>
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            class="kn-material-input"
                            type="number"
                            min="0"
                            v-model="v$.layer.layerOrder.$model"
                            :class="{
                                'p-invalid': v$.layer.layerOrder.$invalid && v$.layer.layerOrder.$dirty
                            }"
                            @blur="v$.layer.layerOrder.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="name" class="kn-material-input-label"> {{ $t('managers.layersManagement.layerOrder') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.layer.layerOrder" :additionalTranslateParams="{ fieldName: $t('managers.layersManagement.layerOrder') }" />
                </div>
                <div class="p-col-12">
                    <span class="p-float-label">
                        <MultiSelect id="layerRoles" class="kn-material-input" v-model="layer.roles" :options="allRoles" optionLabel="name" display="chip" :filter="true" />
                        <label for="layerRoles" class="kn-material-input-label"> {{ $t('common.roles') }} </label>
                    </span>
                </div>
            </form>
        </template>
    </Card>

    <Card id="layer-type-card" class="p-mt-2">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-col-12">
                    <span class="p-float-label">
                        <Dropdown id="layerType" class="kn-material-input" v-model="layer.type" :options="layerTypes" optionLabel="label" optionValue="value" />
                        <label for="layerType" class="kn-material-input-label"> {{ $t('common.type') }} </label>
                    </span>
                </div>
            </form>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import descriptor from './LayersManagementLayerTabDescriptor.json'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Textarea from 'primevue/textarea'
import MultiSelect from 'primevue/multiselect'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    components: { KnValidationMessages, Textarea, MultiSelect, Dropdown },
    props: { selectedLayer: { type: Object, required: true }, allRoles: { type: Array, required: true } },
    computed: {},
    emits: [],
    data() {
        return {
            v$: useValidate() as any,
            descriptor,
            layer: {} as any,
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
    async created() {
        this.layer = this.selectedLayer
    },
    watch: {
        selectedLayer() {
            this.layer = this.selectedLayer
        }
    },
    validations() {
        const validationObject = { layer: createValidations('layer', descriptor.validations.layer) }
        return validationObject
    },
    methods: {}
})
</script>
