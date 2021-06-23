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
                            :options="categories"
                            @before-show="v$.businessModel.dataSourceLabel.$touch()"
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
                        <label for="dataSourceLabel" class="kn-material-input-label">{{ $t('managers.buisnessModelCatalogue.analyticalDriver') }} * </label>
                    </span>
                </div>

                <div class="p-d-flex p-flex-row">
                    <div></div>
                    <div>
                        <InputSwitch id="enable-metadata" class="p-mr-2" v-model="showMetaWeb" />
                        <label for="enable-metadata" class="kn-material-input-label">{{ $t('managers.buisnessModelCatalogue.enableMetaweb') }}</label>
                    </div>
                    <div>
                        <InputSwitch id="model-lock" class="p-mr-2" v-model="businessModel.modelLocked" />
                        <label for="model-lock" class="kn-material-input-label">{{ businessModel.modelLocked ? $t('managers.buisnessModelCatalogue.unlockModel') : $t('managers.buisnessModelCatalogue.lockModel') }}</label>
                    </div>
                </div>

                <div class="p-mt-5" v-if="showMetaWeb">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #left>
                            {{ $t('managers.buisnessModelCatalogue.configurationTablePrefixTitle') }}
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
                                        @blur="v$.businessModel.tablePrefixLike.$touch()"
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
                                        v-tooltip.bottom="'test'"
                                        @blur="v$.businessModel.tablePrefixNotLike.$touch()"
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
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iBusinessModel } from '../../BusinessModelCatalogue'
import { createValidations } from '@/helpers/commons/validationHelper'
import businessModelDetailsCardValidation from './BusinessModelDetailsCardValidation.json'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import useValidate from '@vuelidate/core'

export default defineComponent({
    name: 'business-model-detail-card',
    components: {
        Card,
        Dropdown,
        InputSwitch,
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
        }
    },
    emits: ['fieldChanged'],
    watch: {
        selectedBusinessModel() {
            this.v$.$reset()
            this.loadBusinessModel()
        },
        domainCategories() {
            this.loadCategories()
        },
        datasourcesMeta() {
            this.loadDatasources()
        }
    },
    created() {
        this.loadBusinessModel()
        this.loadCategories()
    },
    data() {
        return {
            businessModelDetailsCardValidation,
            businessModel: {} as iBusinessModel,
            categories: [] as any[],
            datasources: [] as any[],
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
        },
        loadCategories() {
            this.categories = this.domainCategories as any[]
        },
        loadDatasources() {
            this.datasources = this.datasourcesMeta as any[]
        }
    }
})
</script>
