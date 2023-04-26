<template>
    <Accordion :active-index="0">
        <AccordionTab>
            <template #header>
                <i class="fa fa-file"></i>
                <span class="p-m-2">{{ $t('managers.scheduler.saveAsDocument') }}</span>
                <i v-if="document.invalid?.invalidDocument" class="pi pi-exclamation-triangle kn-warning-icon" data-test="warning-icon"></i>
            </template>

            <div v-if="document">
                <div class="p-m-2">
                    <span>
                        <label class="kn-material-input-label">{{ $t('common.name') }} *</label>
                        <InputText
                            v-model="document.documentname"
                            class="kn-material-input"
                            :class="{
                                'p-invalid': documentNameDirty && (!document.documentname || document.documentname.length === 0)
                            }"
                            :max-length="schedulerTimingOutputOutputTabDescriptor.accordion.document.nameMaxLength"
                            @input="validateDocument('documentNameDirty')"
                            @blur="validateDocument('documentNameDirty')"
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

                <div class="p-m-2">
                    <span>
                        <label class="kn-material-input-label">{{ $t('common.description') }}</label>
                        <InputText v-model="document.documentdescription" class="kn-material-input" :max-length="schedulerTimingOutputOutputTabDescriptor.accordion.document.descriptionMaxLength" />
                    </span>
                    <div class="p-d-flex p-jc-end">
                        <small>{{ descriptionHelp }}</small>
                    </div>
                </div>

                <div class="p-m-2">
                    <Checkbox v-model="document.useFixedFolder" :binary="true" @change="validateDocument" />
                    <span class="p-ml-2">{{ $t('managers.scheduler.fixedFolder') }}</span>
                </div>

                <div v-if="document.useFixedFolder" class="p-mt-4">
                    <SchedulerDocumentAccordionTree :prop-functionalities="functionalities" :prop-selected-folders="document.funct" @selected="setSelectedFolders"></SchedulerDocumentAccordionTree>
                </div>

                <div v-if="drivers.length > 0" class="p-m-2">
                    <div
                        v-tooltip="
                            `${$t('managers.scheduler.useFolderDatasetHint.partOne')}:
                            ${$t('managers.scheduler.useFolderDatasetHint.partTwo')} ${$t('managers.scheduler.useFolderDatasetHint.partThree')}`
                        "
                        class="p-my-4"
                    >
                        <Checkbox v-model="document.useFolderDataset" :binary="true" />
                        <span class="p-ml-2">{{ $t('managers.scheduler.folderFromDataset') }}</span>
                    </div>
                </div>

                <div v-if="document.useFolderDataset" class="p-mt-4">
                    <div class="p-m-2">
                        <span>
                            <label class="kn-material-input-label">{{ $t('managers.scheduler.datasetVerification') }} *</label>
                            <Dropdown
                                v-model="document.datasetFolderLabel"
                                class="kn-material-input"
                                :options="datasets"
                                option-label="label"
                                option-value="label"
                                :class="{
                                    'p-invalid': datasetFolderLabelDrity && (!document.datasetFolderLabel || document.datasetFolderLabel?.length === 0)
                                }"
                                :filter="true"
                                filter-match-mode="contains"
                                :filter-fields="['label']"
                                @blur="validateDocument('datasetFolderLabelDrity')"
                                @change="validateDocument('datasetFolderLabelDrity')"
                            />
                            <div v-if="datasetFolderLabelDrity && (!document.datasetFolderLabel || document.datasetFolderLabel?.length === 0)" class="p-error p-grid p-m-2">
                                {{ $t('common.validation.required', { fieldName: $t('managers.scheduler.datasetVerification') }) }}
                            </div>
                        </span>
                    </div>

                    <div class="p-m-2">
                        <span>
                            <label class="kn-material-input-label">{{ $t('common.driver') }} *</label>
                            <Dropdown
                                v-model="document.datasetFolderParameter"
                                class="kn-material-input"
                                :options="drivers"
                                :class="{
                                    'p-invalid': datasetFolderParameterDirty && (!document.datasetFolderParameter || document.datasetFolderParameter?.length === 0)
                                }"
                                @blur="validateDocument('datasetFolderParameterDirty')"
                                @change="validateDocument('datasetFolderParameterDirty')"
                            />
                            <div v-if="datasetFolderParameterDirty && (!document.datasetFolderParameter || document.datasetFolderParameter?.length === 0)" class="p-error p-grid p-m-2">
                                {{ $t('common.validation.required', { fieldName: $t('common.driver') }) }}
                            </div>
                        </span>
                    </div>
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
import SchedulerDocumentAccordionTree from './SchedulerDocumentAccordionTree.vue'
import schedulerTimingOutputOutputTabDescriptor from '../SchedulerTimingOutputOutputTabDescriptor.json'

export default defineComponent({
    name: 'scheduler-document-accordion',
    components: { Accordion, AccordionTab, Checkbox, Dropdown, SchedulerDocumentAccordionTree },
    props: { propDocument: { type: Object }, functionalities: { type: Array }, datasets: { type: Array }, jobInfo: { type: Object } },
    data() {
        return {
            schedulerTimingOutputOutputTabDescriptor,
            document: null as any,
            drivers: [],
            documentNameDirty: false,
            datasetFolderLabelDrity: false,
            datasetFolderParameterDirty: false
        }
    },
    computed: {
        nameHelp(): string {
            return (this.document.documentname?.length ?? '0') + ' / ' + schedulerTimingOutputOutputTabDescriptor.accordion.document.nameMaxLength
        },
        descriptionHelp(): string {
            return (this.document.documentdescription?.length ?? '0') + ' / ' + schedulerTimingOutputOutputTabDescriptor.accordion.document.descriptionMaxLength
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

            this.document.invalid.invalidDocument = false

            this.validateDocument(null)
        },
        loadDrivers() {
            const index = this.jobInfo?.documents.findIndex((el: any) => el.label === this.document.label)
            if (index !== -1) {
                this.drivers = this.jobInfo?.documents[index].parameters
            }
        },
        setSelectedFolders(folders: any[]) {
            this.document.funct = folders
            this.validateDocument(null)
        },
        validateDocument(dirty: string | null) {
            if (dirty) {
                this[dirty] = true
            }
            const nameInvalid = !this.document.documentname || this.document.documentname.length === 0
            const datasetInvalid = this.document.useFolderDataset && (!this.document.datasetFolderLabel || this.document.datasetFolderLabel?.length === 0 || !this.document.datasetFolderParameter || this.document.datasetFolderParameter?.length === 0)
            const foldersInvalid = this.document.useFixedFolder && (!this.document.funct || this.document.funct?.length === 0)

            this.document.invalid.invalidDocument = nameInvalid || datasetInvalid || foldersInvalid
        }
    }
})
</script>

<style lang="scss" scoped>
.dataset-hint-list {
    list-style: none;
    margin: 0;
}

.name-help {
    font-size: smaller;
}
</style>
