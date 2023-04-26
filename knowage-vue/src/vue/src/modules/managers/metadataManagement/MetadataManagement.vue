<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.metadata.title') }}
                    </template>
                    <template #end>
                        <KnFabButton icon="fas fa-plus" data-test="open-form-button" @click="showForm"></KnFabButton>
                    </template>
                </Toolbar>
                <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />
                <Listbox
                    v-if="!loading"
                    class="kn-list--column"
                    :options="metadataList"
                    :filter="true"
                    :filter-placeholder="$t('common.search')"
                    option-label="name"
                    filter-match-mode="contains"
                    :filter-fields="metadataManagementDescriptor.filterFields"
                    :empty-filter-message="$t('managers.widgetGallery.noResults')"
                    data-test="metadata-list"
                    @change="showForm"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" data-test="list-item">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.name }}</span>
                                <span class="kn-list-item-text-secondary">{{ slotProps.option.dataType }}</span>
                            </div>
                            <Button icon="pi pi-trash" class="p-button-link p-button-sm" :data-test="'delete-button'" @click="deleteMetadataConfirm(slotProps.option.id)" />
                        </div>
                    </template>
                </Listbox>
            </div>
            <div class="kn-list--column p-col-8 p-sm-8 p-md-9 p-p-0">
                <KnHint v-if="!formVisible" :title="'managers.metadata.title'" :hint="'managers.metadata.hint'"></KnHint>
                <MetadataManagementDetail v-if="formVisible" :model="selectedMetadata" @close="closeForm" @saved="reloadMetadata" @touched="touched = true"></MetadataManagementDetail>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iMetadata } from './MetadataManagement'
import { AxiosResponse } from 'axios'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import KnHint from '@/components/UI/KnHint.vue'
import Listbox from 'primevue/listbox'
import metadataManagementDescriptor from './MetadataManagementDescriptor.json'
import MetadataManagementDetail from './MetadataManagementDetail.vue'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'metadata-management',
    components: { KnFabButton, Listbox, MetadataManagementDetail, KnHint },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            metadataManagementDescriptor: metadataManagementDescriptor,
            metadataList: [] as iMetadata[],
            formVisible: false,
            loading: false,
            touched: false,
            selectedMetadata: {} as iMetadata
        }
    },
    created() {
        this.loadAllMetadata()
    },
    methods: {
        async loadAllMetadata() {
            this.loading = true
            this.metadataList = []
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/objMetadata')
                .then((response: AxiosResponse<any>) =>
                    response.data.map((metadata: any) => {
                        this.metadataList.push({
                            id: metadata.objMetaId,
                            label: metadata.label,
                            name: metadata.name,
                            description: metadata.description,
                            dataType: metadata.dataTypeCode
                        })
                    })
                )
                .finally(() => (this.loading = false))
        },
        showForm(event: any) {
            if (!this.touched) {
                this.setSelectedMetadata(event)
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.setSelectedMetadata(event)
                    }
                })
            }
        },
        deleteMetadataConfirm(metadataId: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteMetadata(metadataId)
            })
        },
        async deleteMetadata(metadataId: number) {
            await this.$http.delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/objMetadata/' + metadataId).then(() => {
                this.store.setInfo({
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.closeForm()
                this.loadAllMetadata()
            })
        },
        closeForm() {
            this.formVisible = false
        },
        reloadMetadata() {
            this.touched = false
            this.formVisible = false
            this.loadAllMetadata()
        },
        setSelectedMetadata(event: any) {
            if (event) {
                this.selectedMetadata = event.value
            }
            this.formVisible = true
        }
    }
})
</script>

<style lang="scss" scoped>
.kn-list-column {
    border-right: 1px solid #ccc;
}

.list-header {
    font-weight: bold;
}
</style>
