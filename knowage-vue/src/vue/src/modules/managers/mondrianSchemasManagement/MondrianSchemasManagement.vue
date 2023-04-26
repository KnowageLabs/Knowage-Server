<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.mondrianSchemasManagement.title') }}
                    </template>
                    <template #end>
                        <FabButton icon="fas fa-plus" data-test="open-form-button" @click="showForm" />
                    </template>
                </Toolbar>
                <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />
                <div>
                    <Listbox
                        v-if="!loading"
                        class="kn-list--column"
                        :options="schemas"
                        :filter="true"
                        :filter-placeholder="$t('common.search')"
                        option-label="name"
                        filter-match-mode="contains"
                        :filter-fields="mondrianDescriptor.filterFields"
                        :empty-filter-message="$t('common.info.noDataFound')"
                        data-test="schemas-list"
                        @change="showForm"
                    >
                        <template #empty>{{ $t('common.info.noDataFound') }}</template>
                        <template #option="slotProps">
                            <div class="kn-list-item" data-test="list-item">
                                <div class="kn-list-item-text">
                                    <span>{{ slotProps.option.name }}</span>
                                    <span class="kn-list-item-text-secondary">{{ slotProps.option.description }}</span>
                                </div>
                                <Button icon="pi pi-trash" class="p-button-text p-button-rounded p-button-plain" data-test="delete-button" @click.stop="deleteSchemaConfirm(slotProps.option.id)" />
                            </div>
                        </template>
                    </Listbox>
                </div>
            </div>
            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-page">
                <router-view @touched="touched = true" @closed="closeForm" @inserted="reloadPage" />
                <KnHint v-if="toggleHint" :title="'managers.mondrianSchemasManagement.hintTitle'" :hint="'managers.mondrianSchemasManagement.hint'" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iSchema } from './MondrianSchemas'
import { AxiosResponse } from 'axios'
import mondrianDescriptor from './MondrianSchemasManagementDescriptor.json'
import FabButton from '@/components/UI/KnFabButton.vue'
import KnHint from '@/components/UI/KnHint.vue'
import Listbox from 'primevue/listbox'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'mondrian-schemas-management',
    components: {
        FabButton,
        Listbox,
        KnHint
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            loading: false,
            touched: false,
            schemas: [] as iSchema[],
            mondrianDescriptor: mondrianDescriptor
        }
    },
    computed: {
        toggleHint() {
            if (this.$route.fullPath == '/mondrian-schemas-management') {
                return true
            }
            return false
        }
    },
    async created() {
        await this.loadAllSchemas()
    },
    methods: {
        async loadAllSchemas() {
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/mondrianSchemasResource')
                .then((response: AxiosResponse<any>) => {
                    this.schemas = response.data
                })
                .finally(() => (this.loading = false))
        },
        showForm(event: any) {
            const path = event.value ? `/mondrian-schemas-management/${event.value.id}` : '/mondrian-schemas-management/new-schema'

            if (!this.touched) {
                this.$router.push(path)
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.$router.push(path)
                    }
                })
            }
        },
        deleteSchemaConfirm(schemaId: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteSchema(schemaId)
            })
        },
        async deleteSchema(schemaId: number) {
            await this.$http.delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/mondrianSchemasResource/' + schemaId).then(() => {
                this.store.setInfo({
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.$router.push('/mondrian-schemas-management')
                this.loadAllSchemas()
            })
        },
        closeForm() {
            this.touched = false
        },
        reloadPage() {
            this.touched = false
            this.loadAllSchemas()
        }
    }
})
</script>
