<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('kpi.alert.title') }}
                    </template>
                    <template #end>
                        <KnFabButton icon="fas fa-plus" data-test="open-form-button" @click="showForm"></KnFabButton>
                    </template>
                </Toolbar>
                <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />
                <Listbox
                    v-if="!loading"
                    class="kn-list--column"
                    :options="alertList"
                    :filter="true"
                    :filter-placeholder="$t('common.search')"
                    option-label="name"
                    filter-match-mode="contains"
                    :filter-fields="alertDescriptor.filterFields"
                    :empty-filter-message="$t('common.info.noDataFound')"
                    data-test="target-list"
                    @change="showForm"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" data-test="list-item">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.name }}</span>
                                <span class="kn-list-item-text-secondary">{{ slotProps.option.status }}</span>
                            </div>
                            <Button icon="pi pi-trash" class="p-button-text p-button-rounded p-button-plain" :data-test="'delete-button'" @click.stop="deleteAlertConfirm(slotProps.option.id)" />
                            <Button v-if="slotProps.option.status == 'SUSPENDED'" icon="pi pi-play" class="p-button-text p-button-rounded p-button-plain" :data-test="'resume-button'" @click.stop="handleStatus(slotProps.option)" />
                            <Button v-if="slotProps.option.status == 'ACTIVE'" icon="pi pi-pause" class="p-button-text p-button-rounded p-button-plain" :data-test="'suspend-button'" @click.stop="handleStatus(slotProps.option)" />
                        </div>
                    </template>
                </Listbox>
            </div>
            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-router-view">
                <router-view @close="closeForm" @touched="touched = true" @saved="reloadAlert" />
            </div>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iAlert } from './AlertDefinition'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import { AxiosResponse } from 'axios'
import alertDescriptor from './AlertDefinitionDescriptor.json'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'alert',
    components: { KnFabButton, Listbox },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            alertDescriptor,
            alertList: [] as iAlert[],
            loading: false,
            touched: false
        }
    },
    created() {
        this.loadAllAlerts()
    },
    methods: {
        async loadAllAlerts() {
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/alert/listAlert')
                .then(
                    (response: AxiosResponse<any>) =>
                        (this.alertList = response.data.map((alert: any) => {
                            return {
                                id: alert.id,
                                name: alert.name,
                                status: alert.jobStatus
                            }
                        }))
                )
                .finally(() => (this.loading = false))
        },
        showForm(alert: any) {
            const path = alert.value ? `/alert/${alert.value.id}` : `/alert/new-alert`
            this.$router.push(path)
        },
        deleteAlertConfirm(alertId: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteAlert(alertId)
            })
        },
        async deleteAlert(id: number) {
            await this.$http.delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/alert/' + id + '/delete').then(() => {
                this.store.setInfo({
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.$router.push('/alert')
                this.loadAllAlerts()
            })
        },
        async handleStatus(alert) {
            if (alert.status !== 'EXPIRED') {
                const data = 'scheduler/' + (alert.status == 'SUSPENDED' ? 'resumeTrigger' : 'pauseTrigger') + '?jobGroup=ALERT_JOB_GROUP&triggerGroup=ALERT_JOB_GROUP&jobName=' + alert.id + '&triggerName=' + alert.id
                await this.$http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + data)
                this.loadAllAlerts()
            }
        },
        closeForm() {
            if (!this.touched) {
                this.handleClose()
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.handleClose()
                    }
                })
            }
        },
        handleClose() {
            this.$router.replace('/alert')
        },
        reloadAlert(id) {
            this.$router.replace(`/alert/${id}`)
            this.touched = false
            this.loadAllAlerts()
        }
    }
})
</script>
