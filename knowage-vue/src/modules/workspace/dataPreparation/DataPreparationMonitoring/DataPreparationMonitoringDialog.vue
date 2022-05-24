<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary schedulerDialog" v-bind:visible="visibility" footer="footer" :header="$t('workspace.myData.monitoring')" modal :breakpoints="{ '960px': '75vw', '640px': '100vw' }" :closable="false" :baseZIndex="1" :autoZIndex="true">
        <KnScheduler
            class="p-m-1"
            :cronExpression="currentCronExpression"
            :descriptor="schedulerDescriptor"
            @touched="touched = true"
            :readOnly="false"
            :logs="logs"
            :schedulationEnabled="schedulationEnabled"
            :schedulationPaused="schedulationPaused"
            @update:schedulationPaused="updateSchedulationPaused"
            @update:schedulationEnabled="updateSchedulationEnabled"
            @update:currentCronExpression="updateCurrentCronExpression"
            :loadingLogs="loadingLogs"
        />
        <template #footer>
            <Button v-bind:visible="visibility" class="kn-button--secondary" :label="$t('common.cancel')" @click="cancel" />

            <Button v-bind:visible="visibility" class="kn-button--primary" v-t="'common.save'" @click="saveSchedulation" :disabled="!touched" />
        </template>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'

    import Dialog from 'primevue/dialog'

    import dataPreparationMonitoringDescriptor from '@/modules/workspace/dataPreparation/DataPreparationMonitoring/DataPreparationMonitoringDescriptor.json'
    import KnScheduler from '@/components/UI/KnScheduler/KnScheduler.vue'
    import { IDataPrepLog } from '@/modules/workspace/dataPreparation/DataPreparationMonitoring/DataPreparationMonitoring'
    import { AxiosResponse } from 'axios'

    import { filterDefault } from '@/helpers/commons/filterHelper'

    export default defineComponent({
        name: 'data-preparation-monitoring-dialog',
        components: { Dialog, KnScheduler },
        props: { visibility: Boolean, dataset: Object },
        emits: ['close', 'save', 'update:loading'],
        data() {
            return {
                schedulerDescriptor: dataPreparationMonitoringDescriptor,
                logs: Array<IDataPrepLog>(),

                filters: { global: [filterDefault] } as Object,
                validSchedulation: Boolean,
                showHint: false,
                currentCronExpression: '',
                touched: false,
                schedulationPaused: false,
                schedulationEnabled: false,
                instanceId: '',
                loadingLogs: false
            }
        },

        watch: {
            async visibility(newVisibility) {
                this.$store.commit('setLoading', true)
                this.logs = []
                this.loadingLogs = true
                if (newVisibility) await this.loadLogs()
                this.$store.commit('setLoading', false)
                this.loadingLogs = false
            }
        },

        methods: {
            async loadLogs() {
                if (this.dataset && this.dataset.id) {
                    await this.$http.get(process.env.VUE_APP_DATA_PREPARATION_PATH + '1.0/process/by-destination-data-set/' + this.dataset.id).then((response: AxiosResponse<any>) => {
                        let instance = response.data.instance
                        if (instance) {
                            this.instanceId = instance.id
                            this.currentCronExpression = instance.config.cron
                            if (!this.currentCronExpression) this.showHint

                            this.schedulationPaused = instance.config.paused || false

                            this.schedulationEnabled = this.currentCronExpression ? true : false

                            this.$http.get(process.env.VUE_APP_DATA_PREPARATION_PATH + '1.0/process/logs/' + instance.id).then((response: AxiosResponse<any>) => {
                                this.logs = response.data
                            })
                        }
                    })
                }
            },
            cancel() {
                if (this.touched) {
                    this.$confirm.require({
                        message: this.$t('common.toast.unsavedChangesHeader'),
                        header: this.$t('common.toast.unsavedChangesMessage'),
                        icon: 'pi pi-exclamation-triangle',
                        accept: () => this.resetAndClose()
                    })
                } else {
                    this.resetAndClose()
                }
            },
            resetAndClose() {
                this.currentCronExpression = ''
                this.touched = false
                this.showHint = false
                this.$emit('close')
                this.$store.commit('setLoading', false)
            },
            setCronValid(event) {
                this.validSchedulation = event.item
            },
            saveSchedulation() {
                let obj = { instanceId: this.instanceId, config: {} }

                if (this.schedulationEnabled) {
                    obj['config']['cron'] = this.currentCronExpression
                    obj['config']['paused'] = this.schedulationPaused
                }
                this.$emit('save', obj)
                this.resetAndClose()
            },
            updateSchedulationPaused(newSchedulationPaused) {
                this.schedulationPaused = newSchedulationPaused
            },
            updateSchedulationEnabled(newSchedulationEnabled) {
                this.schedulationEnabled = newSchedulationEnabled
            },
            updateCurrentCronExpression(newCronExpression) {
                this.currentCronExpression = newCronExpression
            }
        }
    })
</script>

<style lang="scss">
    .schedulerDialog {
        min-width: 600px;
        width: 1200px;
        max-width: 1400px;

        .p-datatable.p-datatable-sm.data-prep-table {
            min-height: 300px;
        }
    }
</style>
