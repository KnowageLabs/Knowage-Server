<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="hadleSave" />
            <Button class="p-button-text p-button-rounded p-button-plain" icon="pi pi-times" @click="closeTemplate" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="p-grid p-m-0 p-fluid p-jc-center" style="overflow:auto">
        <Card class="p-m-2">
            <template #content>
                <form class="p-fluid p-formgrid p-grid">
                    <div class="p-field p-col-6 p-mb-3">
                        <span class="p-float-label">
                            <InputText
                                id="name"
                                class="kn-material-input"
                                type="text"
                                v-model.trim="v$.simpleNavigation.name.$model"
                                maxLength="40"
                                :class="{
                                    'p-invalid': v$.simpleNavigation.name.$invalid && v$.simpleNavigation.name.$dirty
                                }"
                                @blur="v$.simpleNavigation.name.$touch()"
                                @input="setDirty"
                            />
                            <label for="name" class="kn-material-input-label">{{ $t('common.name') }} * </label>
                        </span>
                        <KnValidationMessages class="p-mt-1" :vComp="v$.simpleNavigation.name" :additionalTranslateParams="{ fieldName: $t('common.name') }"></KnValidationMessages>
                    </div>
                    <div :class="simpleNavigation.type === 2 ? 'p-field p-col-2 p-mb-3' : 'p-field p-col-6 p-mb-3'">
                        <span class="p-float-label">
                            <Dropdown id="type" class="kn-material-input" v-model="simpleNavigation.type" :options="crossModes" optionValue="value" optionLabel="name" @change="handleDropdown" />
                            <label for="type" class="kn-material-input-label"> {{ $t('managers.crossNavigationManagement.modality') }} </label>
                        </span>
                    </div>
                    <div class="p-field p-col-2 p-mb-3" v-if="simpleNavigation.type === 2">
                        <span class="p-float-label">
                            <InputNumber id="width" inputClass="kn-material-input" v-model="simpleNavigation.popupOptions.width" :min="0" :useGrouping="false" @input="setDirty" />
                            <label for="width" class="kn-material-input-label">{{ $t('managers.crossNavigationManagement.width') }} </label>
                        </span>
                        <small id="width-help">{{ $t('managers.crossNavigationManagement.widthHelp') }}</small>
                    </div>
                    <div class="p-field p-col-2 p-mb-3" v-if="simpleNavigation.type === 2">
                        <span class="p-float-label">
                            <InputNumber id="height" inputClass="kn-material-input" v-model="simpleNavigation.popupOptions.height" :min="0" :useGrouping="false" @input="setDirty" />
                            <label for="height" class="kn-material-input-label">{{ $t('managers.crossNavigationManagement.height') }} </label>
                        </span>
                        <small id="height-help">{{ $t('managers.crossNavigationManagement.heightHelp') }}</small>
                    </div>
                    <div class="p-field p-col-6 p-mb-3">
                        <span class="p-input-icon-right">
                            <span class="p-float-label">
                                <InputText id="description" class="kn-material-input" type="text" v-model.trim="simpleNavigation.description" maxLength="200" @input="setDirty" />
                                <label for="description" class="kn-material-input-label">{{ $t('common.description') }} </label>
                            </span>
                            <i class="pi pi-info-circle" @click="hintDialog('desc')" />
                        </span>
                    </div>
                    <div class="p-field p-col-6 p-mb-3">
                        <span class="p-input-icon-right">
                            <span class="p-float-label">
                                <InputText id="breadcrumb" class="kn-material-input" type="text" v-model.trim="simpleNavigation.breadcrumb" maxLength="200" @input="setDirty" />
                                <label for="breadcrumb" class="kn-material-input-label">{{ $t('managers.crossNavigationManagement.breadCrumbs') }} </label>
                            </span>
                            <i class="pi pi-info-circle" @click="hintDialog('bread')" />
                        </span>
                    </div>
                    <div class="p-field p-col-4 p-mb-3">
                        <span class="p-float-label">
                            <InputText id="origin" class="kn-material-input" type="text" v-model.trim="simpleNavigation.fromDoc" disabled />
                            <label for="origin" class="kn-material-input-label">{{ $t('managers.crossNavigationManagement.originDoc') }} </label>
                        </span>
                    </div>
                    <div class="p-field p-col-2 p-mb-3">
                        <Button :label="$t('common.select')" @click="selectDoc('origin')" class="kn-button kn-button--primary" />
                    </div>
                    <div class="p-field p-col-4 p-mb-3">
                        <span class="p-float-label">
                            <InputText id="target" class="kn-material-input" type="text" v-model.trim="simpleNavigation.toDoc" disabled />
                            <label for="target" class="kn-material-input-label">{{ $t('managers.crossNavigationManagement.targetDoc') }} </label>
                        </span>
                    </div>
                    <div class="p-field p-col-2 p-mb-3">
                        <Button :label="$t('common.select')" @click="selectDoc('target')" class="kn-button kn-button--primary" />
                    </div>
                    <DocParameters :selectedNavigation="navigation" @touched="setDirty"></DocParameters>
                </form>
            </template>
        </Card>
        <DocDialog :dialogVisible="dialogVisible" :selectedDoc="docId" @close="dialogVisible = false" @apply="hadleDoc"></DocDialog>
        <HintDialog :dialogVisible="hintDialogVisiable" :message="hintDialogMessage" :title="hintDialogTitle" @close="hintDialogVisiable = false"></HintDialog>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import axios from 'axios'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import DocDialog from './dialogs/CrossNavigationManagementDocDialog.vue'
import HintDialog from './dialogs/CrossNavigationManagementHintDialog.vue'
import DocParameters from './dialogs/CrossNavigationManagementDocParameters.vue'
import crossNavigationManagementValidator from './CrossNavigationManagementValidator.json'
import crossNavigationDescriptor from './CrossNavigationManagementDescriptor.json'
export default defineComponent({
    name: 'cross-navigation-detail',
    components: { Dropdown, DocDialog, DocParameters, HintDialog, KnValidationMessages, InputNumber },
    props: {
        id: {
            type: String
        }
    },
    data() {
        return {
            navigation: {} as any,
            simpleNavigation: {} as any,
            loading: false,
            dialogVisible: false,
            hintDialogVisiable: false,
            hintDialogTitle: '',
            hintDialogMessage: '',
            docType: 'origin',
            docId: null,
            operation: 'insert',
            originParams: [] as any[],
            crossNavigationDescriptor,
            crossModes: [
                { name: this.$t('managers.crossNavigationManagement.normal'), value: 3 },
                { name: this.$t('managers.crossNavigationManagement.popUp'), value: 1 },
                { name: this.$t('managers.crossNavigationManagement.popUpWindow'), value: 2 }
            ],
            v$: useValidate() as any
        }
    },
    computed: {
        buttonDisabled(): any {
            return this.v$.$invalid
        }
    },
    created() {
        if (this.id) {
            this.loadNavigation()
        } else this.initNew()
    },
    watch: {
        async id() {
            if (this.id) {
                await this.loadNavigation()
                if (this.originParams.length > 0) {
                    this.navigation.fromPars = this.originParams
                    this.originParams = []
                }
            } else this.initNew()
        }
    },
    validations() {
        const validationObject = {
            simpleNavigation: createValidations('simpleNavigation', crossNavigationManagementValidator.validations.simpleNavigation)
        }
        return validationObject
    },
    methods: {
        closeTemplate() {
            this.$emit('close')
        },
        setDirty(): void {
            this.$emit('touched')
        },
        initNew() {
            this.navigation = {}
            this.simpleNavigation = { type: 3 }
        },
        async loadNavigation() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/crossNavigation/' + this.id + '/load/')
                .then((response) => {
                    this.navigation = response.data
                    if (this.navigation.simpleNavigation.type === 0) this.navigation.simpleNavigation.type = 3
                    this.simpleNavigation = this.navigation.simpleNavigation
                    if (this.simpleNavigation.popupOptions) {
                        this.simpleNavigation.popupOptions = JSON.parse(this.simpleNavigation.popupOptions)
                    }
                })
                .finally(() => (this.loading = false))
        },
        hadleSave() {
            this.navigation.simpleNavigation = this.simpleNavigation
            if (this.navigation.simpleNavigation.id === undefined) {
                this.operation = 'insert'
                this.navigation.newRecord = true
                this.originParams = this.navigation.fromPars
            } else {
                this.operation = 'update'
                this.originParams = []
            }
            if (this.navigation.simpleNavigation.type === 2) {
                this.navigation.simpleNavigation.popupOptions = JSON.stringify(this.navigation.simpleNavigation.popupOptions)
            } else delete this.navigation.simpleNavigation.popupOptions

            if (this.navigation.simpleNavigation.type === 3) {
                this.navigation.simpleNavigation.type = 0
            }
            axios
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/crossNavigation/save/', this.navigation, { headers: { 'X-Disable-Errors': true } })
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t(this.crossNavigationDescriptor.operation[this.operation].toastTitle),
                        msg: this.$t(this.crossNavigationDescriptor.operation.success)
                    })
                    this.$emit('saved', this.operation, this.navigation.simpleNavigation.name)
                })
                .catch((error) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.saving'),
                        msg: error.message
                    })
                })
                .finally(() => {
                    if (this.navigation.simpleNavigation.type === 2) {
                        this.navigation.simpleNavigation.popupOptions = JSON.parse(this.navigation.simpleNavigation.popupOptions)
                    }
                    if (this.navigation.simpleNavigation.type === 0) {
                        this.navigation.simpleNavigation.type = 3
                    }
                })
        },
        handleDropdown() {
            if (!this.simpleNavigation.popupOptions) this.simpleNavigation.popupOptions = {}
        },
        selectDoc(type) {
            this.docType = type
            switch (type) {
                case 'origin':
                    this.docId = this.simpleNavigation.fromDocId
                    break
                case 'target':
                    this.docId = this.simpleNavigation.toDocId
                    break
            }
            this.dialogVisible = true
        },
        async hadleDoc(doc) {
            this.dialogVisible = false
            switch (this.docType) {
                case 'origin':
                    this.simpleNavigation.fromDocId = doc.DOCUMENT_ID
                    this.simpleNavigation.fromDoc = doc.DOCUMENT_LABEL
                    this.navigation.simpleNavigation = this.simpleNavigation
                    await this.loadInputParams(doc.DOCUMENT_LABEL).then((response) => (this.navigation.fromPars = response))
                    await this.loadOutputParams(doc.DOCUMENT_ID).then((response) => (this.navigation.fromPars = this.navigation.fromPars.concat(response)))
                    this.removeAllLink()
                    break
                case 'target':
                    this.simpleNavigation.toDocId = doc.DOCUMENT_ID
                    this.simpleNavigation.toDoc = doc.DOCUMENT_LABEL
                    this.navigation.simpleNavigation = this.simpleNavigation
                    await this.loadInputParams(doc.DOCUMENT_LABEL).then((response) => (this.navigation.toPars = response))
                    break
            }
            this.setDirty()
        },
        async loadInputParams(label) {
            let params = []
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/documents/' + label + '/parameters').then(
                (response) =>
                    (params = response.data.results.map((param: any) => {
                        return { id: param.id, name: param.label, type: 1, parType: param.parType }
                    }))
            )
            return params
        },
        async loadOutputParams(id) {
            let params = []
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/documents/' + id + '/listOutParams').then(
                (response) =>
                    (params = response.data.map((param: any) => {
                        return { id: param.id, name: param.name, type: 0, parType: param.type.valueCd }
                    }))
            )
            return params
        },
        removeAllLink() {
            this.navigation.toPars?.forEach((param) => {
                param.links = []
            })
        },
        hintDialog(type: string) {
            switch (type) {
                case 'desc':
                    this.hintDialogTitle = this.$t('managers.crossNavigationManagement.hindDesc')
                    this.hintDialogMessage = this.$t('managers.crossNavigationManagement.hindDescMessage')
                    break
                case 'bread':
                    this.hintDialogTitle = this.$t('managers.crossNavigationManagement.hindBread')
                    this.hintDialogMessage = this.$t('managers.crossNavigationManagement.hindBreadMessage')
            }
            this.hintDialogVisiable = true
        }
    }
})
</script>
