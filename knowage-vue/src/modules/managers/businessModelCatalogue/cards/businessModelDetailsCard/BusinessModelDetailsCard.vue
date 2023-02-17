<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('managers.businessModelManager.details') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <form class="p-fluid p-m-5">
                <div class="p-field">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            v-model.trim="v$.businessModel.name.$model"
                            class="kn-material-input"
                            type="text"
                            :class="{
                                'p-invalid': v$.businessModel.name.$invalid && v$.businessModel.name.$dirty
                            }"
                            max-length="100"
                            :disabled="businessModel.id"
                            data-test="name-input"
                            @blur="v$.businessModel.name.$touch()"
                            @input="onFieldChange('name', $event.target.value)"
                        />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages
                        :v-comp="v$.businessModel.name"
                        :additional-translate-params="{
                            fieldName: $t('common.name')
                        }"
                    />
                </div>
                <div class="p-field">
                    <span class="p-float-label">
                        <InputText
                            id="description"
                            v-model.trim="v$.businessModel.description.$model"
                            class="kn-material-input"
                            type="text"
                            :class="{
                                'p-invalid': v$.businessModel.description.$invalid && v$.businessModel.description.$dirty
                            }"
                            max-length="500"
                            :disabled="readonly"
                            data-test="description-input"
                            @blur="v$.businessModel.description.$touch()"
                            @input="onFieldChange('description', $event.target.value)"
                        />
                        <label for="description" class="kn-material-input-label"> {{ $t('managers.businessModelManager.description') }}</label>
                    </span>
                    <KnValidationMessages
                        :v-comp="v$.businessModel.description"
                        :additional-translate-params="{
                            fieldName: $t('managers.businessModelManager.description')
                        }"
                    />
                </div>

                <div class="p-field">
                    <span>
                        <label for="category" class="kn-material-input-label">{{ $t('common.category') }} * </label>
                        <Dropdown
                            id="category"
                            v-model="v$.businessModel.category.$model"
                            class="kn-material-input"
                            :class="{
                                'p-invalid': v$.businessModel.category.$invalid && v$.businessModel.category.$dirty
                            }"
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
                        :v-comp="v$.businessModel.category"
                        :additional-translate-params="{
                            fieldName: $t('managers.businessModelManager.analyticalDriver')
                        }"
                    />
                </div>

                <div class="p-field">
                    <span>
                        <label for="dataSourceLabel" class="kn-material-input-label">{{ $t('managers.businessModelManager.dataSource') }} * </label>
                        <Dropdown
                            id="dataSourceLabel"
                            v-model="v$.businessModel.dataSourceLabel.$model"
                            class="kn-material-input"
                            :class="{
                                'p-invalid': v$.businessModel.dataSourceLabel.$invalid && v$.businessModel.dataSourceLabel.$dirty
                            }"
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
                        :v-comp="v$.businessModel.dataSourceLabel"
                        :additional-translate-params="{
                            fieldName: $t('managers.businessModelManager.dataSource')
                        }"
                    />
                </div>

                <div class="p-d-flex p-flex-row">
                    <div v-show="!metaWebVisible && !readonly" class="input-container">
                        <label for="upload" class="kn-material-input-label">{{ $t('managers.businessModelManager.uploadFile') }}:</label>
                        <KnInputFile :change-function="uploadFile" :visibility="true" />
                    </div>
                    <div class="input-container p-d-flex p-flex-row">
                        <div class="p-m-2">
                            <Button v-show="metaWebVisible" class="kn-button kn-button--primary" :label="$t('managers.businessModelManager.metaWeb')" data-test="metaweb-button" @click="goToMetaWeb"></Button>
                        </div>
                        <div v-show="toGenerate" class="p-m-2">
                            <Button v-show="metaWebVisible" class="kn-button kn-button--primary" :label="$t('managers.businessModelManager.generate')" data-test="generate-button" @click="generateDatamartVisible = true"></Button>
                        </div>
                    </div>

                    <div class="input-container">
                        <div class="p-d-flex p-flex-row">
                            <div v-if="selectedBusinessModel.id" class="p-mr-2">
                                <InputSwitch id="enable-metadata" v-model="metaWebVisible" class="p-mr-2" :disabled="readonly" data-test="metaweb-switch" />
                                <label for="enable-metadata" class="kn-material-input-label">{{ $t('managers.businessModelManager.enableMetaweb') }}</label>
                            </div>
                            <div>
                                <InputSwitch id="model-lock" v-model="businessModel.modelLocked" class="p-mr-2" :disabled="readonly" @change="onLockedChange" />
                                <label for="model-lock" class="kn-material-input-label">{{ businessModel.modelLocked ? $t('managers.businessModelManager.unlockModel') : $t('managers.businessModelManager.lockModel') }}</label>
                            </div>
                        </div>
                        <div class="p-mt-2">
                            <InputSwitch id="smart-view" v-model="businessModel.smartView" class="p-mr-2" :disabled="readonly" @change="onSmartViewChange" />
                            <label v-tooltip.bottom="$t('managers.businessModelManager.smartViewTooltip')" for="smart-view" class="kn-material-input-label">{{ businessModel.smartView ? $t('managers.businessModelManager.smartView') : $t('managers.businessModelManager.advancedView') }}</label>
                        </div>
                    </div>
                </div>

                <div v-if="metaWebVisible" class="p-mt-5">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #start>
                            {{ $t('managers.businessModelManager.configurationTablePrefixTitle') }}
                        </template>
                        <template #end>
                            <i v-tooltip.bottom="$t('managers.businessModelManager.prefixTooltip')" class="fa fa-info-circle"></i>
                        </template>
                    </Toolbar>
                    <div class="p-fluid p-m-5">
                        <div class="p-field p-d-flex">
                            <div class="kn-flex">
                                <span class="p-float-label p-mr-2">
                                    <InputText
                                        id="tablePrefixLike"
                                        v-model.trim="v$.businessModel.tablePrefixLike.$model"
                                        v-tooltip.bottom="$t('managers.businessModelManager.tablePrefixLikeExampleTooltip')"
                                        class="kn-material-input"
                                        type="text"
                                        :class="{
                                            'p-invalid': v$.businessModel.tablePrefixLike.$invalid && v$.businessModel.tablePrefixLike.$dirty
                                        }"
                                        max-length="500"
                                        :disabled="readonly"
                                        data-test="prefix-input"
                                        @blur="v$.businessModel.tablePrefixLike.$touch()"
                                        @input="onFieldChange('tablePrefixLike', $event.target.value)"
                                    />
                                    <label for="label" class="kn-material-input-label"> {{ $t('managers.businessModelManager.tablePrefixLike') }}</label>
                                </span>
                                <KnValidationMessages
                                    :v-comp="v$.businessModel.tablePrefixLike"
                                    :additional-translate-params="{
                                        fieldName: $t('managers.businessModelManager.tablePrefixLike')
                                    }"
                                />
                            </div>
                            <div class="kn-flex">
                                <span class="p-float-label">
                                    <InputText
                                        id="tablePrefixNotLike"
                                        v-model.trim="v$.businessModel.tablePrefixNotLike.$model"
                                        v-tooltip.bottom="$t('managers.businessModelManager.tablePrefixNotLikeExampleTooltip')"
                                        class="kn-material-input"
                                        type="text"
                                        :class="{
                                            'p-invalid': v$.businessModel.tablePrefixNotLike.$invalid && v$.businessModel.tablePrefixNotLike.$dirty
                                        }"
                                        max-length="500"
                                        :disabled="readonly"
                                        data-test="prefix-not-like-input"
                                        @blur="v$.businessModel.tablePrefixNotLike.$touch()"
                                        @input="onFieldChange('tablePrefixNotLike', $event.target.value)"
                                    />
                                    <label for="label" class="kn-material-input-label"> {{ $t('managers.businessModelManager.tablePrefixNotLike') }}</label>
                                </span>
                                <KnValidationMessages
                                    :v-comp="v$.businessModel.tablePrefixNotLike"
                                    :additional-translate-params="{
                                        fieldName: $t('managers.businessModelManager.tablePrefixNotLike')
                                    }"
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </form>

            <GenerateDatamartCard v-if="generateDatamartVisible" :business-model="selectedBusinessModel" :user="user" @close="generateDatamartVisible = false" @generated="onDatamartGenerated"></GenerateDatamartCard>

            <MetawebSelectDialog :visible="metawebSelectDialogVisible" :selected-business-model="selectedBusinessModel" @close="metawebSelectDialogVisible = false" @metaSelected="onMetaSelect"></MetawebSelectDialog>

            <Metaweb :visible="metawebDialogVisible" :prop-meta="meta" :business-model="businessModel" @closeMetaweb="metawebDialogVisible = false" @modelGenerated="$emit('modelGenerated')" />
            <KnOverlaySpinnerPanel id="metaweb-spinner" :visibility="loading" />

            <Dialog :visible="saveConfirmVisible" :modal="true" :closable="false">
                {{ $t('managers.businessModelManager.saveRequired') }}
                <template #footer>
                    <Button class="kn-button kn-button--primary" @click="saveConfirmVisible = false"> {{ $t('common.ok') }}</Button>
                </template>
            </Dialog>
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
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import GenerateDatamartCard from './GenerateDatamartCard.vue'
import InputSwitch from 'primevue/inputswitch'
import KnInputFile from '@/components/UI/KnInputFile.vue'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
import MetawebSelectDialog from '../../metaweb/metawebSelectDialog/MetawebSelectDialog.vue'
import Metaweb from '@/modules/managers/businessModelCatalogue/metaweb/Metaweb.vue'
import useValidate from '@vuelidate/core'
import mainStore from '../../../../../App.store'

export default defineComponent({
    name: 'business-model-details-card',
    components: {
        Card,
        Dialog,
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
    setup() {
        const store = mainStore()
        return { store }
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
            loading: false,
            prefixesTouched: false,
            saveConfirmVisible: false
        }
    },
    computed: {
        metaModelUrl(): any {
            return `/knowagemeta/restful-services/1.0/pages/edit?datasourceId=${this.businessModel.dataSourceId}&user_id=${(this.user as any)?.userUniqueIdentifier}&bmId=${this.businessModel.id}&bmName=${this.businessModel.name}`
        }
    },
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
        this.loadDatasources()
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
            if (fieldName === 'tablePrefixLike' || fieldName === 'tablePrefixNotLike') this.prefixesTouched = true
            this.$emit('fieldChanged', { fieldName, value })
        },
        onLockedChange() {
            this.$emit('fieldChanged', { fieldName: 'modelLocked', value: this.businessModel.modelLocked })
        },
        onSmartViewChange() {
            this.$emit('fieldChanged', { fieldName: 'smartView', value: this.businessModel.smartView })
        },
        async goToMetaWeb() {
            if (this.prefixesTouched) {
                this.saveConfirmVisible = true
            } else {
                this.loading = true
                await this.createSession()
                if (this.businessModelVersions?.length === 0) {
                    this.metawebSelectDialogVisible = true
                } else {
                    await this.loadModelFromSession()
                }
                this.loading = false
            }
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
                .get(import.meta.env.VITE_META_API_URL + `/1.0/metaWeb/model`)
                .then((response: AxiosResponse<any>) => {
                    this.meta = response.data
                    this.metawebDialogVisible = true
                })
                .catch(() => {})
        },
        async createSession() {
            let url = `/1.0/pages/edit?datasourceId=${this.businessModel?.dataSourceId}&user_id=${(this.store.$state as any).user.userUniqueIdentifier}&bmId=${this.businessModel?.id}&bmName=${this.businessModel?.name}`
            if (this.businessModel.tablePrefixLike) url += `&tablePrefixLike=${this.businessModel.tablePrefixLike}`
            if (this.businessModel.tablePrefixNotLike) url += `&tablePrefixNotLike=${this.businessModel.tablePrefixNotLike}`
            await this.$http
                .get(import.meta.env.VITE_META_API_URL + url, {
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
