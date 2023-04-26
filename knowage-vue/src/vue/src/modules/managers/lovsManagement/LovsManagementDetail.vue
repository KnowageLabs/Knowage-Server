<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #start>{{ selectedLov.label }}</template>
        <template #end>
            <Button v-tooltip="$t('common.save')" icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="saveButtonDisabled" data-test="submit-button" @click="saveLov" />
            <Button v-tooltip="$t('common.close')" icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" data-test="close-button" @click="closeTemplate" />
        </template>
    </Toolbar>
    <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" />
    <div v-else class="kn-page-content">
        <div class="card">
            <LovsManagementDetailCard :selected-lov="selectedLov" :lovs="lovs" :list-of-input-types="listOfInputTypes" @touched="setTouched" @typeChanged="cleanSelections"></LovsManagementDetailCard>
        </div>
        <div class="card">
            <LovsManagementWizardCard
                v-if="selectedLov.itypeCd"
                :selected-lov="selectedLov"
                :selected-query="selectedQuery"
                :selected-script="selectedScript"
                :datasources="datasources"
                :profile-attributes="profileAttributes"
                :list-of-script-types="listOfScriptTypes"
                :list-for-fix-lov="listForFixLov"
                :selected-java-class="selectedJavaClass"
                :selected-dataset="selectedDataset"
                :save="save"
                :preview-disabled="saveButtonDisabled"
                @touched="setTouched"
                @created="onCreated()"
                @selectedDataset="setSelectedDataset($event)"
                @sorted="onSort($event)"
            ></LovsManagementWizardCard>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iDatasource, iDomain, iLov, iFixedValue, iProfileAttribute } from './LovsManagement'
import { decode } from 'js-base64'
import X2JS from 'x2js'
import { AxiosResponse } from 'axios'
import LovsManagementDetailCard from './cards/LovsManagementDetailCard/LovsManagementDetailCard.vue'
import LovsManagementWizardCard from './cards/LovsManagementWizardCard/LovsManagementWizardCard.vue'
import useValidate from '@vuelidate/core'

export enum lovProviderEnum {
    SCRIPT = 'SCRIPTLOV',
    QUERY = 'QUERY',
    FIX_LOV = 'FIXLISTLOV',
    JAVA_CLASS = 'JAVACLASSLOV',
    DATASET = 'DATASET'
}

export enum lovItemEnum {
    SCRIPT = 'SCRIPT',
    QUERY = 'QUERY',
    FIX_LOV = 'FIX_LOV',
    JAVA_CLASS = 'JAVA_CLASS',
    DATASET = 'DATASET'
}

export default defineComponent({
    name: 'lovs-management-detail',
    components: { LovsManagementDetailCard, LovsManagementWizardCard },
    props: {
        id: { type: String },
        lovs: { type: Array }
    },
    emits: ['touched', 'created', 'closed'],
    data() {
        return {
            selectedLov: {} as iLov,
            listOfInputTypes: [] as iDomain[],
            listOfScriptTypes: [] as iDomain[],
            datasources: [] as iDatasource[],
            profileAttributes: [] as iProfileAttribute[],
            selectedQuery: {} as { datasource: string; query: string },
            selectedScript: {} as { language: string; text: string },
            listForFixLov: [] as iFixedValue[],
            selectedJavaClass: {} as { name: string },
            selectedDataset: {} as any,
            loading: false,
            touched: false,
            save: false,
            x2js: new X2JS(),
            v$: useValidate() as any
        }
    },
    computed: {
        saveButtonDisabled(): boolean {
            return this.v$.$invalid || this.emptyRequiredFields()
        }
    },
    watch: {
        async id() {
            await this.loadPage()
        }
    },
    async mounted() {
        await this.loadPage()
    },
    methods: {
        async loadPage() {
            this.loading = true
            if (this.id) {
                await this.loadLov()
            } else {
                this.selectedLov = {
                    lovProvider: {},
                    lovProviderJSON: {}
                } as iLov
            }
            await this.loadDomainsData()
            await this.loadDatasources()
            await this.loadProfileAttributes()
            this.loading = false
        },
        async loadLov() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/lovs/${this.id}`).then((response: AxiosResponse<any>) => (this.selectedLov = response.data))
            this.selectedLov.lovProviderJSON = JSON.parse(this.selectedLov.lovProviderJSON)
            this.decode()
            this.formatLov()
        },
        async loadDomainsData() {
            await this.loadDomainsByType('INPUT_TYPE').then((response: AxiosResponse<any>) => (this.listOfInputTypes = response.data))
            await this.loadDomainsByType('SCRIPT_TYPE').then((response: AxiosResponse<any>) => (this.listOfScriptTypes = response.data))
        },
        loadDomainsByType(type: string) {
            return this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=${type}`)
        },
        async loadDatasources() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasources/`).then((response: AxiosResponse<any>) => (this.datasources = response.data))
        },
        async loadProfileAttributes() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/attributes/`).then((response: AxiosResponse<any>) => (this.profileAttributes = response.data))
        },
        decode() {
            if (this.selectedLov.lovProviderJSON.SCRIPTLOV) {
                this.selectedLov.lovProviderJSON.SCRIPTLOV.SCRIPT = this.escapeXml(decode(this.selectedLov.lovProviderJSON.SCRIPTLOV.SCRIPT))
            } else if (this.selectedLov.lovProviderJSON.QUERY) {
                this.selectedLov.lovProviderJSON.QUERY.decoded_STMT = this.escapeXml(decode(this.selectedLov.lovProviderJSON.QUERY.STMT))
            }
        },
        async formatLov() {
            if (lovProviderEnum.SCRIPT in this.selectedLov.lovProviderJSON) {
                this.selectedScript.language = this.selectedLov.lovProviderJSON.SCRIPTLOV.LANGUAGE ? this.selectedLov.lovProviderJSON.SCRIPTLOV.LANGUAGE : 'groovy'
                this.selectedScript.text = this.selectedLov.lovProviderJSON.SCRIPTLOV.SCRIPT
            } else if (lovProviderEnum.QUERY in this.selectedLov.lovProviderJSON) {
                this.selectedQuery.datasource = this.selectedLov.lovProviderJSON.QUERY.CONNECTION
                this.selectedQuery.query = this.selectedLov.lovProviderJSON.QUERY.decoded_STMT
            } else if (lovProviderEnum.FIX_LOV in this.selectedLov.lovProviderJSON) {
                this.listForFixLov = []
                if (Array.isArray(this.selectedLov.lovProviderJSON.FIXLISTLOV.ROWS.ROW)) {
                    this.listForFixLov = this.selectedLov.lovProviderJSON.FIXLISTLOV.ROWS.ROW
                } else {
                    this.listForFixLov.push(this.selectedLov.lovProviderJSON.FIXLISTLOV.ROWS.ROW)
                }
            } else if (lovProviderEnum.JAVA_CLASS in this.selectedLov.lovProviderJSON) {
                this.selectedJavaClass.name = this.selectedLov.lovProviderJSON.JAVACLASSLOV.JAVA_CLASS_NAME
            } else if (lovProviderEnum.DATASET in this.selectedLov.lovProviderJSON) {
                await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/datasets/dataset/id/${this.selectedLov.lovProviderJSON.DATASET.ID}`).then((response: AxiosResponse<any>) => {
                    this.selectedDataset = response.data[0]
                })
            }
        },
        escapeXml(value: string) {
            return value
                .replace(/'/g, "'")
                .replace(/"/g, '"')
                .replace(/>/g, '>')
                .replace(/</g, '<')
                .replace(/&/g, '&')
                .replace(/&apos;/g, "'")
        },
        saveLov() {
            this.save = !this.save
        },
        closeTemplate() {
            const path = '/lovs-management'
            if (!this.touched) {
                this.$router.push(path)
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.$emit('closed')
                        this.$router.push(path)
                    }
                })
            }
        },
        setTouched() {
            this.touched = true
            this.$emit('touched')
        },
        emptyRequiredFields() {
            switch (this.selectedLov.itypeCd) {
                case lovItemEnum.SCRIPT:
                    return !this.selectedScript.language
                case lovItemEnum.QUERY:
                    return !this.selectedQuery.datasource
                case lovItemEnum.JAVA_CLASS:
                    return !this.selectedJavaClass.name
                case lovItemEnum.DATASET:
                    return !this.selectedDataset.name
                case lovItemEnum.FIX_LOV:
                    return this.isFixedLovListInvalid()
            }

            return false
        },
        isFixedLovListInvalid() {
            for (let i = 0; i < this.listForFixLov.length; i++) {
                const fixedLovListItem = this.listForFixLov[i] as iFixedValue
                if (fixedLovListItem.VALUE?.trim() && fixedLovListItem.DESCRIPTION?.trim()) {
                    return false
                }
            }
            return true
        },
        cleanSelections() {
            this.selectedQuery = { datasource: '', query: '' }
            this.selectedScript = { language: '', text: '' }
            this.listForFixLov = []
            this.selectedJavaClass = { name: '' }
            this.selectedDataset = {}
        },
        setSelectedDataset(dataset: any) {
            this.selectedDataset = dataset
        },
        onSort(sortedArray: any[]) {
            this.listForFixLov = sortedArray
        },
        async onCreated() {
            this.$emit('created')
            await this.loadPage()
        }
    }
})
</script>
