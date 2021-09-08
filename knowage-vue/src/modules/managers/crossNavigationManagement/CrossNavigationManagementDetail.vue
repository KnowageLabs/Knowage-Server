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
                            <InputText id="width" class="kn-material-input" type="number" v-model.trim="simpleNavigation.popupOptions.width" />
                            <label for="width" class="kn-material-input-label">{{ $t('managers.crossNavigationManagement.width') }} </label>
                        </span>
                        <small id="width-help">{{ $t('managers.crossNavigationManagement.widthHelp') }}</small>
                    </div>
                    <div class="p-field p-col-2 p-mb-3" v-if="simpleNavigation.type === 2">
                        <span class="p-float-label">
                            <InputText id="height" class="kn-material-input" type="number" v-model.trim="simpleNavigation.popupOptions.height" />
                            <label for="height" class="kn-material-input-label">{{ $t('managers.crossNavigationManagement.height') }} </label>
                        </span>
                        <small id="height-help">{{ $t('managers.crossNavigationManagement.heightHelp') }}</small>
                    </div>
                    <div class="p-field p-col-6 p-mb-3">
                        <span class="p-float-label">
                            <InputText id="description" class="kn-material-input" type="text" v-model.trim="simpleNavigation.description" maxLength="200" />
                            <label for="description" class="kn-material-input-label">{{ $t('common.description') }} </label>
                        </span>
                    </div>
                    <div class="p-field p-col-6 p-mb-3">
                        <span class="p-float-label">
                            <InputText id="name" class="kn-material-input" type="text" v-model.trim="simpleNavigation.breadcrumb" maxLength="200" />
                            <label for="name" class="kn-material-input-label">{{ $t('managers.crossNavigationManagement.breadCrumbs') }} </label>
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
                    <div class="p-field p-col-6 p-mb-3">
                        <Toolbar class="kn-toolbar kn-toolbar--secondary">
                            <template #left>
                                {{ $t('managers.crossNavigationManagement.availableIO') }}
                            </template>
                        </Toolbar>
                        <div class="p-inputgroup">
                            <InputText class="kn-material-input" type="text" v-model="value" />
                            <KnFabButton icon="fas fa-plus"></KnFabButton>
                        </div>
                        <Listbox :options="navigation.fromPars" optionLabel="name"></Listbox>
                        {{ navigation.fromPars }}
                    </div>
                    <div class="p-field p-col-6 p-mb-3">
                        <Toolbar class="kn-toolbar kn-toolbar--secondary">
                            <template #left>
                                {{ $t('managers.crossNavigationManagement.availableInput') }}
                            </template>
                        </Toolbar>
                        <Listbox :options="navigation.toPars" optionLabel="name"></Listbox>
                        {{ navigation.toPars }}
                    </div>
                </form>
                <p>{{ navigation }}</p>
            </template>
        </Card>
        <DocDialog :dialogVisible="dialogVisible" :selectedDoc="docId" @close="dialogVisible = false" @apply="hadleDoc"></DocDialog>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import axios from 'axios'
import Dropdown from 'primevue/dropdown'
import Listbox from 'primevue/listbox'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import DocDialog from './dialogs/CrossNavigationManagementDocDialog.vue'
import crossNavigationManagementValidator from './CrossNavigationManagementValidator.json'
export default defineComponent({
    name: 'cross-navigation-detail',
    components: { Dropdown, DocDialog, KnValidationMessages, Listbox, KnFabButton },
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
            docType: 'origin',
            docId: null,
            crossModes: [
                { name: this.$t('managers.crossNavigationManagement.normal'), value: 0 },
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
        }
    },
    watch: {
        async id() {
            if (this.id) {
                await this.loadNavigation()
            } else {
                this.navigation = {}
                this.simpleNavigation = {}
            }
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
        async loadNavigation() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/crossNavigation/' + this.id + '/load/')
                .then((response) => {
                    this.navigation = response.data
                    this.simpleNavigation = response.data.simpleNavigation
                    if (this.simpleNavigation.popupOptions) {
                        this.simpleNavigation.popupOptions = JSON.parse(this.simpleNavigation.popupOptions)
                    }
                })
                .finally(() => (this.loading = false))
        },
        hadleSave() {
            this.navigation.simpleNavigation = this.simpleNavigation
            console.log(this.navigation)
        },
        handleDropdown() {
            if (!this.simpleNavigation.popupOptions) this.simpleNavigation.popupOptions = { width: '', height: '' }
        },
        selectDoc(type) {
            console.log(type)
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
        hadleDoc(doc) {
            this.dialogVisible = false
            switch (this.docType) {
                case 'origin':
                    this.simpleNavigation.fromDocId = doc.DOCUMENT_ID
                    this.simpleNavigation.fromDoc = doc.DOCUMENT_LABEL
                    break
                case 'target':
                    this.simpleNavigation.toDocId = doc.DOCUMENT_ID
                    this.simpleNavigation.toDoc = doc.DOCUMENT_LABEL
                    break
            }
        }
    }
})
</script>
