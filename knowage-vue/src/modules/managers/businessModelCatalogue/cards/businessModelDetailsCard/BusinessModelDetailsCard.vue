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
            <form class="p-fluid p-m-5">
                <div class="p-field">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.businessModel.name.$model"
                            :class="{
                                'p-invalid': v$.businessModel.name.$invalid && v$.businessModel.name.$dirty
                            }"
                            maxLength="100"
                            @blur="v$.businessModel.name.$touch()"
                            @input="onFieldChange('name', $event.target.value)"
                        />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.businessModel.name"
                        :additionalTranslateParams="{
                            fieldName: $t('common.name')
                        }"
                    />
                </div>
                <div class="p-field">
                    <span class="p-float-label">
                        <InputText
                            id="description"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.businessModel.description.$model"
                            :class="{
                                'p-invalid': v$.businessModel.description.$invalid && v$.businessModel.description.$dirty
                            }"
                            maxLength="500"
                            @blur="v$.businessModel.description.$touch()"
                            @input="onFieldChange('description', $event.target.value)"
                        />
                        <label for="description" class="kn-material-input-label"> {{ $t('managers.buisnessModelCatalogue.description') }}</label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.businessModel.description"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.buisnessModelCatalogue.description')
                        }"
                    />
                </div>

                <div class="p-field">
                    <span class="p-float-label">
                        <Dropdown
                            id="category"
                            class="kn-material-input"
                            :class="{
                                'p-invalid': v$.businessModel.category.$invalid && v$.businessModel.category.$dirty
                            }"
                            v-model="v$.businessModel.category.$model"
                            :options="categories"
                            @before-show="v$.businessModel.category.$touch()"
                            @change="onFieldChange('category', $event.value.VALUE_ID)"
                        >
                            <template #value="slotProps">
                                <div v-if="slotProps.value">
                                    <span>{{ slotProps.value.VALUE_NM }}</span>
                                </div>
                            </template>
                            <template #option="slotProps">
                                <div>
                                    <span>{{ slotProps.option.VALUE_NM }}</span>
                                </div>
                            </template>
                        </Dropdown>
                        <label for="category" class="kn-material-input-label">{{ $t('managers.buisnessModelCatalogue.analyticalDriver') }} * </label>
                    </span>
                </div>

                <div class="p-field">
                    <span class="p-float-label">
                        <Dropdown
                            id="dataSourceLabel"
                            class="kn-material-input"
                            :class="{
                                'p-invalid': v$.businessModel.dataSourceLabel.$invalid && v$.businessModel.dataSourceLabel.$dirty
                            }"
                            v-model="v$.businessModel.dataSourceLabel.$model"
                            :options="datasources"
                            @before-show="v$.businessModel.dataSourceLabel.$touch()"
                            @change="onFieldChange('dataSourceLabel', $event.value)"
                        >
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
                        <label for="dataSourceLabel" class="kn-material-input-label">{{ $t('managers.buisnessModelCatalogue.dataSource') }} * </label>
                    </span>
                </div>

                <div class="p-d-flex p-flex-row">
                    <div class="input-container" v-if="!metaWebVisible">
                        <label for="upload" class="kn-material-input-label">{{ $t('managers.buisnessModelCatalogue.uploadFile') }}:</label>
                        <KnInputFile :changeFunction="uploadFile" :visibility="true" />
                    </div>
                    <div class="input-container p-d-flex p-flex-row" v-else>
                        <div>
                            <Button class="kn-button kn-button--primary" :label="$t('managers.buisnessModelCatalogue.metaWeb')" @click="goToMetaWeb"></Button>
                        </div>
                        <div v-if="toGenerate">
                            <Button class="kn-button kn-button--primary" :label="$t('managers.buisnessModelCatalogue.generate')" @click="generate"></Button>
                        </div>
                    </div>

                    <div class="input-container">
                        <div class="p-d-flex p-flex-row">
                            <div v-if="selectedBusinessModel.id">
                                <InputSwitch id="enable-metadata" class="p-mr-2" v-model="metaWebVisible" />
                                <label for="enable-metadata" class="kn-material-input-label">{{ $t('managers.buisnessModelCatalogue.enableMetaweb') }}</label>
                            </div>
                            <div>
                                <InputSwitch id="model-lock" class="p-mr-2" v-model="businessModel.modelLocked" @change="onLockedChange" />
                                <label for="model-lock" class="kn-material-input-label">{{ businessModel.modelLocked ? $t('managers.buisnessModelCatalogue.unlockModel') : $t('managers.buisnessModelCatalogue.lockModel') }}</label>
                            </div>
                        </div>
                        <div>
                            <InputSwitch id="smart-view" class="p-mr-2" v-model="businessModel.smartView" @change="onSmartViewChange" />
                            <label for="smart-view" class="kn-material-input-label" v-tooltip.bottom="$t('managers.buisnessModelCatalogue.smartViewTooltip')">{{ businessModel.smartView ? $t('managers.buisnessModelCatalogue.smartView') : $t('managers.buisnessModelCatalogue.advancedView') }}</label>
                        </div>
                    </div>
                </div>

                <div class="p-mt-5" v-if="metaWebVisible">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #left>
                            {{ $t('managers.buisnessModelCatalogue.configurationTablePrefixTitle') }}
                        </template>
                        <template #right>
                               <i class="fa fa-info-circle" v-tooltip.bottom="$t('managers.buisnessModelCatalogue.prefixTooltip')"></i>
                        </template>
                    </Toolbar>
                    <div class="p-fluid p-m-5">
                        <div class="p-field p-d-flex">
                            <div class="kn-flex">
                                <span class="p-float-label">
                                    <InputText
                                        id="tablePrefixLike"
                                        class="kn-material-input"
                                        type="text"
                                        v-model.trim="v$.businessModel.tablePrefixLike.$model"
                                        :class="{
                                            'p-invalid': v$.businessModel.tablePrefixLike.$invalid && v$.businessModel.tablePrefixLike.$dirty
                                        }"
                                        maxLength="500"
                                        v-tooltip.bottom="$t('managers.buisnessModelCatalogue.tablePrefixLikeExampleTooltip')"
                                        @blur="v$.businessModel.tablePrefixLike.$touch()"
                                        @input="onFieldChange('tablePrefixLike', $event.target.value)"
                                    />
                                    <label for="label" class="kn-material-input-label"> {{ $t('managers.buisnessModelCatalogue.tablePrefixLike') }}</label>
                                </span>
                                <KnValidationMessages
                                    :vComp="v$.businessModel.tablePrefixLike"
                                    :additionalTranslateParams="{
                                        fieldName: $t('managers.buisnessModelCatalogue.tablePrefixLike')
                                    }"
                                />
                            </div>
                            <div class="kn-flex">
                                <span class="p-float-label">
                                    <InputText
                                        id="tablePrefixNotLike"
                                        class="kn-material-input"
                                        type="text"
                                        v-model.trim="v$.businessModel.tablePrefixNotLike.$model"
                                        :class="{
                                            'p-invalid': v$.businessModel.tablePrefixNotLike.$invalid && v$.businessModel.tablePrefixNotLike.$dirty
                                        }"
                                        maxLength="500"
                                        v-tooltip.bottom="$t('managers.buisnessModelCatalogue.tablePrefixNotLikeExampleTooltip')"
                                        @blur="v$.businessModel.tablePrefixNotLike.$touch()"
                                        @input="onFieldChange('tablePrefixNotLike', $event.target.value)"
                                    />
                                    <label for="label" class="kn-material-input-label"> {{ $t('managers.buisnessModelCatalogue.tablePrefixNotLike') }}</label>
                                </span>
                                <KnValidationMessages
                                    :vComp="v$.businessModel.tablePrefixNotLike"
                                    :additionalTranslateParams="{
                                        fieldName: $t('managers.buisnessModelCatalogue.tablePrefixNotLike')
                                    }"
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </form>

            <div v-if="showMetaWeb">
                <iframe :src="metaModelUrl"></iframe>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iBusinessModel } from '../../BusinessModelCatalogue'
import { createValidations } from '@/helpers/commons/validationHelper'
import businessModelDetailsCardDescriptor from './BusinessModelDetailsCardDescriptor.json'
import businessModelDetailsCardValidation from './BusinessModelDetailsCardValidation.json'
import Card from 'primevue/card'
// import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
// import IframeRenderer from '@/modules/commons/IframeRenderer.vue'
import InputSwitch from 'primevue/inputswitch'
import KnInputFile from '@/components/UI/KnInputFile.vue'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import useValidate from '@vuelidate/core'

export default defineComponent({
    name: 'business-model-detail-card',
    components: {
        Card,
        Dropdown,
        InputSwitch,
        KnInputFile,
        KnValidationMessages
    },
    props: {
        selectedBusinessModel: {
            type: Object,
            required: true
        },
        domainCategories: {
            type: Array,
            requried: true
        },
        datasourcesMeta: {
            type: Array,
            requried: true
        },
        userToken: {
            type: String
        },
        toGenerate: {
            type: Boolean
        }
    },
    emits: ['fieldChanged', 'fileUploaded'],
    watch: {
        selectedBusinessModel() {
            this.v$.$reset()
            this.loadBusinessModel()
            console.log('TO GENERATE', this.toGenerate)
        },
        domainCategories() {
            this.loadCategories()
        },
        datasourcesMeta() {
            this.loadDatasources()
        }
    },
    computed: {
        metaModelUrl(): any {
            const url =
                'http://localhost:8080/knowage' +
                `/restful-services/1.0/pages/edit?datasourceId=${this.businessModel.dataSourceId}&user_id=${this.userToken}&bmId=${this.businessModel.id}` +
                `&bmName=${encodeURIComponent(this.businessModel.name)}` +
                (this.businessModel.tablePrefixLike ? '&tablePrefixLike=' + this.businessModel.tablePrefixLike : '') +
                (this.businessModel.tablePrefixNotLike ? '&tablePrefixNotLike=' + this.businessModel.tablePrefixNotLike : '')

            console.log('METAWEB URL', url)
            return url
        }
    },
    created() {
        this.loadBusinessModel()
        this.loadCategories()
    },
    data() {
        return {
            businessModelDetailsCardDescriptor,
            businessModelDetailsCardValidation,
            businessModel: {} as iBusinessModel,
            categories: [] as any[],
            datasources: [] as any[],
            metaWebVisible: false,
            showMetaWeb: false,
            touched: false,
            v$: useValidate() as any
        }
    },
    validations() {
        return {
            businessModel: createValidations('businessModel', businessModelDetailsCardValidation.validations.businessModel)
        }
    },
    methods: {
        loadBusinessModel() {
            this.businessModel = { ...this.selectedBusinessModel } as iBusinessModel
            console.log('LOADED BM', this.businessModel)
        },
        loadCategories() {
            this.categories = this.domainCategories as any[]
        },
        loadDatasources() {
            console.log('BEFORE CALLED LOADDATASOURCES', this.datasourcesMeta)
            this.datasources = this.datasourcesMeta as any[]
            console.log('AFTER CALLED LOADDATASOURCES', this.datasources)
        },
        uploadFile(event) {
            this.$emit('fileUploaded', event.target.files[0])
        },
        onFieldChange(fieldName: string, value: any) {
            console.log(fieldName, '  =>  ', value)
            this.$emit('fieldChanged', { fieldName, value })
        },
        onLockedChange() {
            this.$emit('fieldChanged', { fieldName: 'modelLocked', value: this.businessModel.modelLocked })
        },
        onSmartViewChange() {
            this.$emit('fieldChanged', { fieldName: 'smartView', value: this.businessModel.smartView })
        },
        goToMetaWeb() {
            this.showMetaWeb = true
        },
        generate() {}
    }
})
</script>

<style lang="scss" scoped>
.input-container {
    flex: 0.5;
}

#metaweb-page {
    position: fixed;
    top: 0px;
    bottom: 0px;
    right: 0px;
    width: 100%;
    border: none;
    margin: 0;
    padding: 0;
    overflow: hidden;
    z-index: 999999;
    height: 100%;
}
</style>
