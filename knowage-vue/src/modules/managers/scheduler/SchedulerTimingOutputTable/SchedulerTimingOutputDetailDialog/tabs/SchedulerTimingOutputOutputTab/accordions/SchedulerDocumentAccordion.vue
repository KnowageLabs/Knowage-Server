<template>
    <Accordion :activeIndex="0">
        <AccordionTab>
            <template #header>
                <i class="fa fa-file"></i>
                <span class="p-m-2">{{ $t('managers.scheduler.saveAsDocument') }}</span>
                <i v-if="document.invalid" class="pi pi-exclamation-triangle warning-icon"></i>
            </template>

            <div v-if="document">
                <div>
                    <span>
                        <label class="kn-material-input-label">{{ $t('common.name') }} *</label>
                        <InputText
                            class="kn-material-input"
                            v-model="document.documentname"
                            :class="{
                                'p-invalid': documentNameDirty && (!document.documentname || document.documentname.length === 0)
                            }"
                            :maxLength="100"
                            @blur="setNameValidation"
                        />
                    </span>
                    <div class="p-d-flex p-flex-row p-jc-between">
                        <div>
                            <div v-show="documentNameDirty && (!document.documentname || document.documentname.length === 0)" class="p-error p-grid p-m-2">
                                {{ $t('common.validation.required', { fieldName: $t('common.name') }) }}
                            </div>
                        </div>
                        <p class="name-help p-m-0">{{ nameHelp }}</p>
                    </div>
                </div>
                <div>
                    <span>
                        <label class="kn-material-input-label">{{ $t('common.description') }}</label>
                        <InputText class="kn-material-input" v-model="document.documentdescription" :maxLength="100" />
                    </span>
                    <div class="p-d-flex p-jc-end">
                        <small>{{ descriptionHelp }}</small>
                    </div>
                </div>
                <div class="p-m-2">
                    <Checkbox v-model="document.useFixedFolder" :binary="true" />
                    <span class="p-ml-2">{{ $t('managers.scheduler.fixedFolder') }}</span>
                </div>
                <div v-if="document.useFixedFolder">
                    <SchedulerDocumentAccordionTree :propFunctionalities="functionalities" :propSelectedFolders="document.funct" @selected="setSelectedFolders"></SchedulerDocumentAccordionTree>
                </div>
                <div v-if="drivers.length > 0">
                    <Message class="p-m-2" severity="info" :closable="true" :style="schedulerDocumentAccordionDescriptor.styles.message">
                        {{ $t('managers.scheduler.useFolderDatasetHint.partOne') }}
                        <ul class="dataset-hint-list">
                            <li>{{ $t('managers.scheduler.useFolderDatasetHint.partTwo') }}</li>
                            <li>{{ $t('managers.scheduler.useFolderDatasetHint.partThree') }}</li>
                        </ul>
                    </Message>
                    <div class="p-m-2">
                        <Checkbox v-model="document.useFolderDataset" :binary="true" />
                        <span class="p-ml-2">{{ $t('managers.scheduler.folderFromDataset') }}</span>
                    </div>
                </div>
                <div v-if="document.useFolderDataset">
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.datasetVerification') }} *</label>
                        <Dropdown
                            class="kn-material-input"
                            v-model="document.datasetFolderLabel"
                            :options="datasets"
                            optionLabel="label"
                            optionValue="label"
                            :class="{
                                'p-invalid': datasetFolderLabelDrity && (!document.datasetFolderLabel || document.datasetFolderLabel?.length === 0)
                            }"
                            @blur="setDataFolderLabelValidation"
                        />
                        <div v-if="datasetFolderLabelDrity && (!document.datasetFolderLabel || document.datasetFolderLabel?.length === 0)" class="p-error p-grid p-m-2">
                            {{ $t('common.validation.required', { fieldName: $t('managers.scheduler.datasetVerification') }) }}
                        </div>
                    </span>
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.driver') }} *</label>
                        <Dropdown
                            class="kn-material-input"
                            v-model="document.datasetFolderParameter"
                            :options="drivers"
                            :class="{
                                'p-invalid': datasetFolderParameterDirty && (!document.datasetFolderParameter || document.datasetFolderParameter?.length === 0)
                            }"
                            @blur="setDataFolderParameterValidation"
                        />
                        <div v-if="datasetFolderParameterDirty && (!document.datasetFolderParameter || document.datasetFolderParameter?.length === 0)" class="p-error p-grid p-m-2">
                            {{ $t('common.validation.required', { fieldName: $t('managers.scheduler.driver') }) }}
                        </div>
                    </span>
                </div>
            </div>
        </AccordionTab>
    </Accordion>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import Message from 'primevue/message'
import SchedulerDocumentAccordionTree from './SchedulerDocumentAccordionTree.vue'
import schedulerDocumentAccordionDescriptor from './SchedulerDocumentAccordionDescriptor.json'

export default defineComponent({
    name: 'scheduler-document-accordion',
    components: { Accordion, AccordionTab, Checkbox, Dropdown, Message, SchedulerDocumentAccordionTree },
    props: { propDocument: { type: Object }, functionalities: { type: Array }, datasets: { type: Array }, jobInfo: { type: Object } },
    emits: ['documentValidated'],
    data() {
        return {
            schedulerDocumentAccordionDescriptor,
            document: null as any,
            drivers: [],
            documentNameDirty: false,
            datasetFolderLabelDrity: false,
            datasetFolderParameterDirty: false
        }
    },
    computed: {
        nameHelp(): string {
            return (this.document.documentname?.length ?? '0') + ' / 100'
        },
        descriptionHelp(): string {
            return (this.document.documentdescription?.length ?? '0') + ' / 100'
        }
    },
    watch: {
        propDocument() {
            this.loadDocument()
            this.loadDrivers()
        }
    },
    created() {
        this.loadDocument()
        this.loadDrivers()
    },
    methods: {
        loadDocument() {
            this.document = this.propDocument
        },
        loadDrivers() {
            const index = this.jobInfo?.documents.findIndex((el: any) => el.label === this.document.label)
            if (index !== -1) {
                this.drivers = this.jobInfo?.documents[index].parameters
            }

            console.log('LOADED DRIVERS: ', this.drivers)
        },
        setSelectedFolders(folders: any[]) {
            console.log('SELECTED FOlDERS: ', folders)
        },
        setNameValidation() {
            this.documentNameDirty = true
            this.document.invalid = !this.document.documentname || this.document.documentname.length === 0
        },
        setDataFolderLabelValidation() {
            this.datasetFolderLabelDrity = true
            this.document.invalid = !this.document.datasetFolderLabel || this.document.datasetFolderLabel?.length === 0
        },
        setDataFolderParameterValidation() {
            this.datasetFolderParameterDirty = true
            this.document.invalid = !this.document.datasetFolderParameter || this.document.datasetFolderParameter?.length === 0
        }
    }
})
</script>

<style lang="scss" scoped>
.dataset-hint-list {
    list-style: none;
    margin: 0;
}

.warning-icon {
    color: orange;
}

.name-help {
    font-size: smaller;
}
</style>
