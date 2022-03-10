<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('managers.businessModelManager.driversDetails') }}
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
                            :disabled="businessModel.id"
                            @blur="v$.businessModel.name.$touch()"
                            @input="onFieldChange('name', $event.target.value)"
                            data-test="name-input"
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
                            :disabled="readonly"
                            data-test="description-input"
                        />
                        <label for="description" class="kn-material-input-label"> {{ $t('managers.businessModelManager.description') }}</label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.businessModel.description"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.businessModelManager.description')
                        }"
                    />
                </div>

                <div class="p-field">
                    <span>
                        <label for="category" class="kn-material-input-label">{{ $t('common.category') }} * </label>
                        <Dropdown
                            id="category"
                            class="kn-material-input"
                            :class="{
                                'p-invalid': v$.businessModel.category.$invalid && v$.businessModel.category.$dirty
                            }"
                            v-model="v$.businessModel.category.$model"
                            :options="categories"
                            :placeholder="$t('common.category')"
                            :disabled="readonly"
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
                    </span>
                    <KnValidationMessages
                        :vComp="v$.businessModel.category"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.businessModelManager.analyticalDriver')
                        }"
                    />
                </div>

                <div class="p-field">
                    <span>
                        <label for="dataSourceLabel" class="kn-material-input-label">{{ $t('managers.businessModelManager.dataSource') }} * </label>
                        <Dropdown
                            id="dataSourceLabel"
                            class="kn-material-input"
                            :class="{
                                'p-invalid': v$.businessModel.dataSourceLabel.$invalid && v$.businessModel.dataSourceLabel.$dirty
                            }"
                            v-model="v$.businessModel.dataSourceLabel.$model"
                            :options="datasources"
                            :placeholder="$t('managers.businessModelManager.dataSourceLabelPlaceholder')"
                            :disabled="readonly"
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
                    </span>
                    <KnValidationMessages
                        :vComp="v$.businessModel.dataSourceLabel"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.businessModelManager.dataSource')
                        }"
                    />
                </div>

                <div class="p-d-flex p-flex-row">
                    <div class="input-container" v-show="!metaWebVisible && !readonly">
                        <label for="upload" class="kn-material-input-label">{{ $t('managers.businessModelManager.uploadFile') }}:</label>
                        <KnInputFile :changeFunction="uploadFile" :visibility="true" />
                    </div>
                    <div class="input-container p-d-flex p-flex-row">
                        <div class="p-m-2">
                            <Button v-show="metaWebVisible" class="kn-button kn-button--primary" :label="$t('managers.businessModelManager.metaWeb')" @click="goToMetaWeb" data-test="metaweb-button"></Button>
                        </div>
                        <div class="p-m-2" v-show="toGenerate">
                            <Button v-show="metaWebVisible" class="kn-button kn-button--primary" :label="$t('managers.businessModelManager.generate')" @click="generateDatamartVisible = true" data-test="generate-button"></Button>
                        </div>
                    </div>

                    <div class="input-container">
                        <div class="p-d-flex p-flex-row">
                            <div v-if="selectedBusinessModel.id" class="p-mr-2">
                                <InputSwitch id="enable-metadata" class="p-mr-2" v-model="metaWebVisible" :disabled="readonly" data-test="metaweb-switch" />
                                <label for="enable-metadata" class="kn-material-input-label">{{ $t('managers.businessModelManager.enableMetaweb') }}</label>
                            </div>
                            <div>
                                <InputSwitch id="model-lock" class="p-mr-2" v-model="businessModel.modelLocked" :disabled="readonly" @change="onLockedChange" />
                                <label for="model-lock" class="kn-material-input-label">{{ businessModel.modelLocked ? $t('managers.businessModelManager.unlockModel') : $t('managers.businessModelManager.lockModel') }}</label>
                            </div>
                        </div>
                        <div class="p-mt-2">
                            <InputSwitch id="smart-view" class="p-mr-2" v-model="businessModel.smartView" :disabled="readonly" @change="onSmartViewChange" />
                            <label for="smart-view" class="kn-material-input-label" v-tooltip.bottom="$t('managers.businessModelManager.smartViewTooltip')">{{ businessModel.smartView ? $t('managers.businessModelManager.smartView') : $t('managers.businessModelManager.advancedView') }}</label>
                        </div>
                    </div>
                </div>

                <div class="p-mt-5" v-if="metaWebVisible">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #start>
                            {{ $t('managers.businessModelManager.configurationTablePrefixTitle') }}
                        </template>
                        <template #end>
                            <i class="fa fa-info-circle" v-tooltip.bottom="$t('managers.businessModelManager.prefixTooltip')"></i>
                        </template>
                    </Toolbar>
                    <div class="p-fluid p-m-5">
                        <div class="p-field p-d-flex">
                            <div class="kn-flex">
                                <span class="p-float-label p-mr-2">
                                    <InputText
                                        id="tablePrefixLike"
                                        class="kn-material-input"
                                        type="text"
                                        v-model.trim="v$.businessModel.tablePrefixLike.$model"
                                        :class="{
                                            'p-invalid': v$.businessModel.tablePrefixLike.$invalid && v$.businessModel.tablePrefixLike.$dirty
                                        }"
                                        maxLength="500"
                                        v-tooltip.bottom="$t('managers.businessModelManager.tablePrefixLikeExampleTooltip')"
                                        :disabled="readonly"
                                        @blur="v$.businessModel.tablePrefixLike.$touch()"
                                        @input="onFieldChange('tablePrefixLike', $event.target.value)"
                                        data-test="prefix-input"
                                    />
                                    <label for="label" class="kn-material-input-label"> {{ $t('managers.businessModelManager.tablePrefixLike') }}</label>
                                </span>
                                <KnValidationMessages
                                    :vComp="v$.businessModel.tablePrefixLike"
                                    :additionalTranslateParams="{
                                        fieldName: $t('managers.businessModelManager.tablePrefixLike')
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
                                        v-tooltip.bottom="$t('managers.businessModelManager.tablePrefixNotLikeExampleTooltip')"
                                        :disabled="readonly"
                                        @blur="v$.businessModel.tablePrefixNotLike.$touch()"
                                        @input="onFieldChange('tablePrefixNotLike', $event.target.value)"
                                        data-test="prefix-not-like-input"
                                    />
                                    <label for="label" class="kn-material-input-label"> {{ $t('managers.businessModelManager.tablePrefixNotLike') }}</label>
                                </span>
                                <KnValidationMessages
                                    :vComp="v$.businessModel.tablePrefixNotLike"
                                    :additionalTranslateParams="{
                                        fieldName: $t('managers.businessModelManager.tablePrefixNotLike')
                                    }"
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </form>

            <GenerateDatamartCard v-if="generateDatamartVisible" :businessModel="selectedBusinessModel" :user="user" @close="generateDatamartVisible = false" @generated="onDatamartGenerated"></GenerateDatamartCard>

            <MetawebSelectDialog :visible="metawebSelectDialogVisible" :selectedBusinessModel="selectedBusinessModel" @close="metawebSelectDialogVisible = false" @metaSelected="onMetaSelect"></MetawebSelectDialog>

            <Metaweb :visible="metawebDialogVisible" :propMeta="meta" :businessModel="businessModel" @closeMetaweb="metawebDialogVisible = false" @modelGenerated="$emit('modelGenerated')" />
            <KnOverlaySpinnerPanel id="metaweb-spinner" :visibility="loading" />
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iBusinessModel } from '../../BusinessModelCatalogue'
import { createValidations } from '@/helpers/commons/validationHelper'
import { AxiosResponse } from 'axios'
import businessModelDetailsCardDescriptor from './BusinessModelDetailsCardDescriptor.json'
import businessModelDetailsCardValidation from './BusinessModelDetailsCardValidation.json'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import GenerateDatamartCard from './GenerateDatamartCard.vue'
import InputSwitch from 'primevue/inputswitch'
import KnInputFile from '@/components/UI/KnInputFile.vue'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
import MetawebSelectDialog from '../../metaweb/metawebSelectDialog/MetawebSelectDialog.vue'
import Metaweb from '@/modules/managers/businessModelCatalogue/metaweb/Metaweb.vue'
import useValidate from '@vuelidate/core'

export default defineComponent({
    name: 'business-model-details-card',
    components: {
        Card,
        Dropdown,
        GenerateDatamartCard,
        InputSwitch,
        KnInputFile,
        KnValidationMessages,
        KnOverlaySpinnerPanel,
        MetawebSelectDialog,
        Metaweb
    },
    props: {
        selectedBusinessModel: {
            type: Object,
            required: true
        },
        domainCategories: {
            type: Array,
            required: true
        },
        datasourcesMeta: {
            type: Array,
            required: true
        },
        user: {
            type: Object as PropType<Object | null>
        },
        toGenerate: {
            type: Boolean
        },
        readonly: {
            type: Boolean
        },
        businessModelVersions: {
            type: Array
        }
    },
    emits: ['fieldChanged', 'fileUploaded', 'datamartGenerated', 'modelGenerated'],
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
    computed: {
        metaModelUrl(): any {
            return `/knowagemeta/restful-services/1.0/pages/edit?datasourceId=${this.businessModel.dataSourceId}&user_id=${(this.user as any)?.userUniqueIdentifier}&bmId=${this.businessModel.id}&bmName=${this.businessModel.name}`
        }
    },
    created() {
        this.loadBusinessModel()
        this.loadCategories()
        this.loadDatasources()
    },
    data() {
        return {
            businessModelDetailsCardDescriptor,
            businessModelDetailsCardValidation,
            businessModel: {} as iBusinessModel,
            categories: [] as any[],
            datasources: [] as any[],
            metaWebVisible: false,
            generateDatamartVisible: false,
            metawebSelectDialogVisible: false,
            metawebDialogVisible: false,
            meta: null as any,
            touched: false,
            v$: useValidate() as any,
            loading: false
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
        },
        uploadFile(event) {
            this.$emit('fileUploaded', event.target.files[0])
        },
        onFieldChange(fieldName: string, value: any) {
            this.$emit('fieldChanged', { fieldName, value })
        },
        onLockedChange() {
            this.$emit('fieldChanged', { fieldName: 'modelLocked', value: this.businessModel.modelLocked })
        },
        onSmartViewChange() {
            this.$emit('fieldChanged', { fieldName: 'smartView', value: this.businessModel.smartView })
        },
        async goToMetaWeb() {
            this.loading = true
            await this.createSession()
            if (this.businessModelVersions?.length === 0) {
                this.metawebSelectDialogVisible = true
            } else {
                await this.loadModelFromSession()
            }
            this.loading = false
        },
        onDatamartGenerated() {
            this.$emit('datamartGenerated')
        },
        onMetaSelect(meta: any) {
            this.meta = meta
            this.metawebSelectDialogVisible = false
            this.metawebDialogVisible = true
        },
        async loadModelFromSession() {
            await this.$http
                .get(process.env.VUE_APP_META_API_URL + `/1.0/metaWeb/model`)
                .then((response: AxiosResponse<any>) => {
                    this.meta = response.data
                    this.metawebDialogVisible = true
                })
                .catch(() => {})
        },
        async createSession() {
            let url = `/1.0/pages/edit?datasourceId=${this.businessModel?.dataSourceId}&user_id=${(this.$store.state as any).user.userUniqueIdentifier}&bmId=${this.businessModel?.id}&bmName=${this.businessModel?.name}`
            if (this.businessModel.tablePrefixLike) url += `&tablePrefixLike=${this.businessModel.tablePrefixLike}`
            if (this.businessModel.tablePrefixNotLike) url += `&tablePrefixNotLike=${this.businessModel.tablePrefixNotLike}`
            await this.$http
                .get(process.env.VUE_APP_META_API_URL + url, {
                    headers: {
                        Accept: 'application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9'
                    }
                })
                .then(() => {})
                .catch(() => {})
        }
    }
})
</script>

<style lang="scss">
.full-screen-dialog.p-dialog {
    max-height: 100%;
}
.full-screen-dialog.p-dialog .p-dialog-content {
    padding: 0;
}

.pi-upload {
    display: none;
}

#metaweb-spinner {
    position: fixed;
    top: 0;
    left: 0;
}
</style>
