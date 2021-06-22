<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('managers.dataSourceManagement.title') }}
                    </template>
                    <template #right>
                        <FabButton icon="fas fa-plus" @click="showForm" data-test="open-form-button" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <Listbox
                    v-if="!loading"
                    class="kn-list--column"
                    :options="datasources"
                    :filter="true"
                    :filterPlaceholder="$t('common.search')"
                    optionLabel="label"
                    filterMatchMode="contains"
                    :filterFields="dataSourceDescriptor.filterFields"
                    :emptyFilterMessage="$t('common.info.noDataFound')"
                    @change="showForm"
                    data-test="datasources-list"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" data-test="list-item">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.label }}</span>
                                <span class="kn-list-item-text-secondary">{{ slotProps.option.descr }}</span>
                            </div>
                            <Button icon="far fa-trash-alt" class="p-button-link" @click.stop="deleteDatasourceConfirm(slotProps.option.dsId)" data-test="delete-button" />
                        </div>
                    </template>
                </Listbox>
            </div>
            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <router-view :selectedDatasource="selDatasource" :databases="listOfAvailableDatabases" @touched="touched = true" @closed="onFormClose" @inserted="reloadPage" />
                <KnHint :title="'managers.dataSourceManagement.hintTitle'" :hint="'managers.dataSourceManagement.hint'" v-if="hintVisible" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import dataSourceDescriptor from './DataSourceDescriptor.json'
import FabButton from '@/components/UI/KnFabButton.vue'
import KnHint from '@/components/UI/KnHint.vue'

import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'datasources-management',
    components: {
        FabButton,
        Listbox,
        KnHint
    },
    data() {
        return {
            loading: false,
            touched: false,
            hintVisible: true,
            datasources: [] as any[],
            selDatasource: {} as any,
            listOfAvailableDatabases: [] as any,
            dataSourceDescriptor
        }
    },
    async created() {
        await this.getAllDatasources()
        await this.getAllDatabases()
    },
    methods: {
        async getAllDatasources() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/datasources')
                .then((response) => {
                    this.datasources = response.data
                })
                .finally(() => (this.loading = false))
        },
        async getAllDatabases() {
            return axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/databases`)
                .then((response) => {
                    this.listOfAvailableDatabases = response.data
                })
                .finally(() => (this.loading = false))
        },
        showForm(event: any) {
            const path = event.value ? `/datasource/${event.value.dsId}` : '/datasource/new-datasource'
            this.hintVisible = false

            if (!this.touched) {
                this.$router.push(path)
                this.selDatasource = event.value
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.$router.push(path)
                        this.selDatasource = event.value
                    }
                })
            }
        },
        deleteDatasourceConfirm(datasourceId: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteDatasource(datasourceId)
            })
        },
        async deleteDatasource(datasourceId: number) {
            await axios.delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/datasources/' + datasourceId).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.$router.push('/datasource')
                this.getAllDatasources()
            })
        },
        reloadPage() {
            this.touched = false
            this.hintVisible = true
            this.getAllDatasources()
        },
        onFormClose() {
            this.touched = false
            this.hintVisible = true
        }
    }
})
</script>
