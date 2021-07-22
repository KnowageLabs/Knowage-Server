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
                <Listbox v-if="!loading" class="kn-list--column" :options="alertList" :filter="true" :filterPlaceholder="$t('common.search')" optionLabel="name" filterMatchMode="contains" :filterFields="alertDescriptor.filterFields" :emptyFilterMessage="$t('common.info.noDataFound')">
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" data-test="list-item">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.name }}</span>
                                <span class="kn-list-item-text-secondary">{{ slotProps.option.status }}</span>
                            </div>
                            <Button icon="pi pi-trash" class="p-button-text p-button-rounded p-button-plain" @click="deleteAlertConfirm(slotProps.option.id)" :data-test="'delete-button'" />
                        </div>
                    </template>
                </Listbox>
            </div>
            <div class="kn-list--column p-col-8 p-sm-8 p-md-9 p-p-0">
                <router-view />
            </div>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import axios from 'axios'
import alertDescriptor from './AlertDescriptor.json'

export default defineComponent({
    name: 'alert',
    components: { KnFabButton, Listbox },
    data() {
        return {
            alertDescriptor: alertDescriptor,
            alertList: [],
            loading: false
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
        showForm() {
            console.log(this.alertList)
        }
    }
})
</script>
