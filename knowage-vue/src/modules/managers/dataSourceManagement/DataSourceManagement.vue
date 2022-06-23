<template>
    <div class="kn-page-content p-grid p-m-0">
        <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('managers.dataSourceManagement.title') }}
                </template>
                <template #end>
                    <FabButton icon="fas fa-plus" @click="showForm" data-test="open-form-button" />
                </template>
            </Toolbar>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
            <Listbox
                v-if="!loading"
                class="kn-list--column"
                :options="datasources"
                optionLabel="label"
                :filter="true"
                :filterPlaceholder="$t('common.search')"
                filterMatchMode="contains"
                :filterFields="dataSourceDescriptor.filterFields"
                :emptyFilterMessage="$t('common.info.noDataFound')"
                @change="showForm"
                data-test="datasource-list"
            >
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item" data-test="list-item">
                        <Avatar
                            :icon="dataSourceDescriptor.iconTypesMap[slotProps.option.dialectName]?.dbIcon"
                            shape="circle"
                            size="medium"
                            :style="dataSourceDescriptor.iconTypesMap[slotProps.option.dialectName]?.style"
                            v-tooltip="dataSourceDescriptor.iconTypesMap[slotProps.option.dialectName]?.tooltip"
                        />
                        <div class="kn-list-item-text">
                            <span>{{ slotProps.option.label }}</span>
                            <span class="kn-list-item-text-secondary">{{ slotProps.option.descr }}</span>
                        </div>
                        <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" v-if="slotProps.option.owner == this.user.userId || this.user.isSuperadmin" @click.stop="deleteDatasourceConfirm(slotProps.option.dsId)" data-test="delete-button" />
                    </div>
                </template>
            </Listbox>
        </div>
        <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 p-d-flex p-flex-column kn-height-full-vertical">
            <router-view :selectedDatasource="selDatasource" :databases="listOfAvailableDatabases" :user="user" @touched="touched = true" @closed="onFormClose" @inserted="reloadPage" />
        </div>
    </div>
</template>

<script lang="ts">
/* eslint-disable no-prototype-builtins */
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import dataSourceDescriptor from './DataSourceDescriptor.json'
import FabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import Avatar from 'primevue/avatar'

export default defineComponent({
    name: 'datasources-management',
    components: {
        FabButton,
        Listbox,
        Avatar
    },
    data() {
        return {
            dataSourceDescriptor,
            datasources: [] as any[],
            selDatasource: {} as any,
            listOfAvailableDatabases: [] as any,
            user: {} as any,
            loading: false,
            touched: false
        }
    },
    async created() {
        await this.getAllDatasources()
        await this.getAllDatabases()
        await this.getCurrentUser()
    },
    methods: {
        async getAllDatabases() {
            return this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/databases`)
                .then((response: AxiosResponse<any>) => {
                    this.listOfAvailableDatabases = response.data
                })
                .finally(() => (this.loading = false))
        },

        async getCurrentUser() {
            return this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/currentuser`)
                .then((response: AxiosResponse<any>) => {
                    this.user = response.data
                })
                .finally(() => (this.loading = false))
        },

        async getAllDatasources() {
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/datasources')
                .then((response: AxiosResponse<any>) => {
                    this.datasources = response.data
                    this.convertToSeconds(this.datasources)
                })
                .finally(() => (this.loading = false))
        },
        convertToSeconds(dataSourceArr) {
            Array.prototype.forEach.call(dataSourceArr, (dataSource) => {
                if (dataSource.hasOwnProperty('jdbcPoolConfiguration')) {
                    dataSource.jdbcPoolConfiguration.maxWait /= 1000
                    dataSource.jdbcPoolConfiguration.timeBetweenEvictionRuns /= 1000
                    dataSource.jdbcPoolConfiguration.minEvictableIdleTimeMillis /= 1000
                    if (dataSource.jdbcPoolConfiguration.maxIdle === null) dataSource.jdbcPoolConfiguration.maxIdle = dataSourceDescriptor.newDataSourceValues.jdbcPoolConfiguration.maxIdle
                    if (dataSource.jdbcPoolConfiguration.validationQueryTimeout === null) dataSource.jdbcPoolConfiguration.validationQueryTimeout = dataSourceDescriptor.newDataSourceValues.jdbcPoolConfiguration.validationQueryTimeout
                }
            })
        },

        showForm(event: any) {
            const path = event.value ? `/datasource-management/${event.value.dsId}` : '/datasource-management/new-datasource'

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
            await this.$http
                .delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/datasources/' + datasourceId)
                .then(() => {
                    this.store.setInfo({
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    this.$router.push('/datasource-management')
                    this.getAllDatasources()
                })
                .catch((error) => {
                    this.store.setError({ title: 'Delete error', msg: error.message })
                })
        },

        reloadPage() {
            this.touched = false
            this.$router.push('/datasource-management')
            this.getAllDatasources()
        },
        onFormClose() {
            this.touched = false
        }
    }
})
</script>
