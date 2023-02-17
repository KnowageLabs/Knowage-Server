<template>
    <div class="kn-page">
        <div class="kn-page-content custom-kn-page-width p-d-flex p-flex-column">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('managers.schedulationAgendaManagement.title') }}
                </template>
            </Toolbar>
            <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />

            <div :style="schedulationAgendaDescriptor.hCard.style" class="card-no-padding flex-no-resize p-p-3">
                <Card>
                    <template #content>
                        <div class="p-d-flex custom-overflow-parent-container">
                            <div class="p-d-flex p-ai-center p-jc-between custom-overflow-auto">
                                <div class="p-d-flex p-mx-2">
                                    <label for="startDate" class="kn-material-input-label p-m-2"> {{ $t('managers.schedulationAgendaManagement.detail.startDate') + ':' }}</label>
                                    <span>
                                        <Calendar id="startDate" v-model="startDateTime" class="kn-material-input" :show-icon="true" :manual-input="false" :min-date="minStartDate" data-test="start-date" />
                                    </span>
                                </div>

                                <div class="p-d-flex p-ai-center p-mx-2">
                                    <label for="startTime" class="kn-material-input-label p-m-2"> {{ $t('managers.schedulationAgendaManagement.detail.startTime') + ':' }}</label>
                                    <span>
                                        <Calendar id="startTime" v-model="startDateTime" class="kn-material-input custom-timepicker" :manual-input="false" :time-only="true" hour-format="24" :inline="true" data-test="start-time" />
                                    </span>
                                </div>

                                <div class="p-d-flex p-mx-2">
                                    <label for="endDate" class="kn-material-input-label p-m-2"> {{ $t('managers.schedulationAgendaManagement.detail.endDate') + ':' }}</label>
                                    <span>
                                        <Calendar id="endDate" v-model="endDateTime" class="kn-material-input" :show-icon="true" :manual-input="false" data-test="end-date" />
                                    </span>
                                </div>

                                <div class="p-d-flex p-ai-center p-mx-2">
                                    <label for="endTime" class="kn-material-input-label p-m-2"> {{ $t('managers.schedulationAgendaManagement.detail.endTime') + ':' }}</label>
                                    <span class="custom-timepicker">
                                        <Calendar id="endTime" v-model="endDateTime" class="kn-material-input" :manual-input="false" :time-only="true" hour-format="24" :inline="true" data-test="end-time" />
                                    </span>
                                </div>

                                <div class="p-d-flex p-ai-center p-mx-2">
                                    <div class="p-d-flex p-flex-column">
                                        <label for="package" class="kn-material-input-label"> {{ $t('managers.schedulationAgendaManagement.detail.package') }} </label>
                                        <InputText id="package" v-model="selectedPackageName" class="kn-material-input" type="text" data-test="package-input" :readonly="true" @click="showForm('packageForm')" />
                                    </div>
                                    <Button icon="fas fa-external-link-alt" class="p-button-text p-button-rounded p-button-plain" @click="showForm('packageForm')" />
                                    <Button v-if="selectedPackage" icon="pi pi-times-circle" class="p-button-text p-button-rounded p-button-plain" @click="removeSelectedPackage" />
                                </div>

                                <div class="p-d-flex p-ai-center p-mx-2">
                                    <div class="p-d-flex p-flex-column">
                                        <label for="document" class="kn-material-input-label"> {{ $t('managers.schedulationAgendaManagement.detail.document') }} </label>
                                        <InputText id="document" v-model="selectedDocumentName" class="kn-material-input" type="text" data-test="document-input" :readonly="true" @click="showForm('documentForm')" />
                                    </div>
                                    <Button icon="fas fa-external-link-alt" class="p-button-text p-button-rounded p-button-plain" @click="showForm('documentForm')" />
                                    <Button v-if="selectedDocument" icon="pi pi-times-circle" class="p-button-text p-button-rounded p-button-plain" @click="removeSelectedDocument" />
                                </div>

                                <div class="p-d-flex p-ai-center">
                                    <div>
                                        <Button class="p-button-text kn-button" :label="$t('common.search')" data-test="search-button" @click="runSearch" />
                                    </div>
                                </div>
                                <h1></h1>
                            </div>
                        </div>
                        <div class="kn-page-content p-grid p-m-0">
                            <div v-if="displayFormType == 'packageForm'">
                                <SchedulationAgendaDialog
                                    :item-list="packageList"
                                    :model="selectedPackage"
                                    :title="$t('managers.schedulationAgendaManagement.packageTypes.title')"
                                    data-test="package-schedulation-form"
                                    @changed="selectedPackageChanged($event)"
                                    @close="closeForm"
                                ></SchedulationAgendaDialog>
                            </div>
                            <div v-if="displayFormType == 'documentForm'">
                                <SchedulationAgendaDialog
                                    :item-list="documentList"
                                    :model="selectedDocument"
                                    :title="$t('managers.schedulationAgendaManagement.documentTypes.title')"
                                    data-test="document-schedulation-form"
                                    @changed="selectedDocumentChanged($event)"
                                    @close="closeForm"
                                ></SchedulationAgendaDialog>
                            </div>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="p-col-12 p-sm-12 p-md-12 p-p-0 p-d-flex p-flex-column flex-container-overflow-auto">
                <router-view :item-list="schedulations" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iDataItem } from './SchedulationAgenda'
import { formatDate } from '@/helpers/commons/localeHelper'
import Calendar from 'primevue/calendar'
import Card from 'primevue/card'
import Toolbar from 'primevue/toolbar'
import ProgressBar from 'primevue/progressbar'
import schedulationAgendaDescriptor from './SchedulationAgendaDescriptor.json'
import SchedulationAgendaDialog from './SchedulationAgendaDialog.vue'
import { AxiosResponse } from 'axios'

export default defineComponent({
    name: 'schedulation-agenda',
    components: {
        Calendar,
        Card,
        Toolbar,
        ProgressBar,
        SchedulationAgendaDialog
    },
    data() {
        return {
            schedulationAgendaDescriptor: schedulationAgendaDescriptor,
            schedulations: [] as any,
            selectedPackage: null as iDataItem | null,
            selectedDocument: null as iDataItem | null,
            selectedPackageName: '',
            selectedDocumentName: '',
            packageList: [] as iDataItem[],
            documentList: [] as iDataItem[],
            loading: false,
            startDateTime: null as Date | null,
            endDateTime: null as Date | null,
            displayFormType: ''
        }
    },
    computed: {
        minStartDate() {
            return new Date()
        }
    },
    watch: {
        selectedPackage() {
            if (this.selectedPackage) this.selectedPackageName = this.selectedPackage.name
        },
        selectedDocument() {
            if (this.selectedDocument) this.selectedDocumentName = this.selectedDocument.name
        }
    },
    created() {
        this.startDateTime = new Date()
        this.endDateTime = new Date()
        this.endDateTime.setDate(this.endDateTime.getDate() + 7)

        this.loadPackages()
        this.loadDocuments()
    },
    methods: {
        async loadPackages() {
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '/scheduleree/listAllJobs')
                .then((response: AxiosResponse<any>) => {
                    const rawList = response.data.root

                    rawList.map((item: any) => {
                        this.packageList.push({
                            id: item.jobName,
                            name: item.jobName,
                            description: item.jobDescription
                        } as iDataItem)
                    })
                })
                .finally(() => (this.loading = false))
        },
        async loadDocuments() {
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/documents')
                .then((response: AxiosResponse<any>) => {
                    this.documentList = response.data
                })
                .finally(() => (this.loading = false))
        },
        showForm(formType) {
            this.displayFormType = formType
        },
        closeForm() {
            this.displayFormType = ''
        },
        selectedPackageChanged(dataItem: any) {
            this.selectedPackage = dataItem
            this.displayFormType = ''
        },
        selectedDocumentChanged(dataItem: any) {
            this.selectedDocument = dataItem
            this.displayFormType = ''
        },
        runSearch() {
            this.loading = true

            let path = import.meta.env.VITE_RESTFUL_SERVICES_PATH + `scheduleree/nextExecutions?start=${this.formatDateTime(this.startDateTime)}&end=${this.formatDateTime(this.endDateTime)}`
            if (this.selectedPackage) {
                path += `&jobPackageName=${this.selectedPackage.id}`
            }
            if (this.selectedDocument) {
                path += `&document=${this.selectedDocument.label}`
            }
            this.$http
                .get(path)
                .then((response: AxiosResponse<any>) => {
                    this.schedulations = response.data.root
                })
                .finally(() => (this.loading = false))

            this.$router.push('/schedulation-agenda/search-result')
        },
        formatDateTime(date: any) {
            return formatDate(date, 'YYYY-MM-DDTHH:MM:SS')
        },
        removeSelectedPackage() {
            this.selectedPackage = null
            this.selectedPackageName = ''
        },
        removeSelectedDocument() {
            this.selectedDocument = null
            this.selectedDocumentName = ''
        }
    }
})
</script>
<style lang="scss" scoped>
.flex-container-overflow-auto {
    flex: 1;
    overflow: auto;
}
.flex-no-resize {
    flex-grow: 0;
    flex-shrink: 0;
}

.custom-timepicker {
    &:deep(.p-datepicker) {
        border-color: transparent;
    }
}

.card-no-padding {
    :deep(.p-card-body) {
        padding: 0;
        .p-card-content {
            padding: 0;
        }
    }

    :deep(.p-datepicker) {
        padding: 0;
        .p-timepicker {
            padding: 0;
        }
    }
}
.custom-overflow-parent-container {
    overflow: hidden;
    min-width: 0;
}
.custom-overflow-auto {
    min-width: 0;
    overflow: auto;
    flex-grow: 1;
}
.custom-kn-page-width {
    width: calc(100vw - var(--kn-mainmenu-width));
}
</style>
