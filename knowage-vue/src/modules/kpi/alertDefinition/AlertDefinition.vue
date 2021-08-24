<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('kpi.alert.title') }}
                    </template>
                    <template #right>
                        <KnFabButton icon="fas fa-plus" @click="showForm" data-test="open-form-button"></KnFabButton>
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <Listbox
                    v-if="!loading"
                    class="kn-list--column"
                    :options="alertList"
                    :filter="true"
                    :filterPlaceholder="$t('common.search')"
                    optionLabel="name"
                    filterMatchMode="contains"
                    :filterFields="alertDescriptor.filterFields"
                    :emptyFilterMessage="$t('common.info.noDataFound')"
                    @change="showForm"
                    data-test="target-list"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" data-test="list-item">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.name }}</span>
                                <span class="kn-list-item-text-secondary">{{ slotProps.option.status }}</span>
                            </div>
                            <Button icon="pi pi-trash" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteAlertConfirm(slotProps.option.id)" :data-test="'delete-button'" />
                            <Button v-if="slotProps.option.status == 'SUSPENDED'" icon="pi pi-play" class="p-button-text p-button-rounded p-button-plain" @click.stop="handleStatus(slotProps.option)" :data-test="'resume-button'" />
                            <Button v-if="slotProps.option.status == 'ACTIVE'" icon="pi pi-pause" class="p-button-text p-button-rounded p-button-plain" @click.stop="handleStatus(slotProps.option)" :data-test="'suspend-button'" />
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
import axios from 'axios'
import alertDescriptor from './AlertDefinitionDescriptor.json'

export default defineComponent({
    name: 'alert',
    components: { KnFabButton, Listbox },
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
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/alert/listAlert')
                .then(
                    (response) =>
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
            await axios.delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/alert/' + id + '/delete').then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.$router.push('/alert')
                this.loadAllAlerts()
            })
        },
        async handleStatus(alert) {
            if (alert.status !== 'EXPIRED') {
                var data = 'scheduler/' + (alert.status == 'SUSPENDED' ? 'resumeTrigger' : 'pauseTrigger') + '?jobGroup=ALERT_JOB_GROUP&triggerGroup=ALERT_JOB_GROUP&jobName=' + alert.id + '&triggerName=' + alert.id
                await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + data)
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
