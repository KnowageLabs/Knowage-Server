<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('kpi.kpiDefinition.title') }}
                    </template>
                    <template #right>
                        <FabButton icon="fas fa-plus" @click="showForm" data-test="open-form-button" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <Listbox v-if="!loading" class="kn-list--column" :options="kpiList" :filter="true" :filterPlaceholder="$t('common.search')" optionLabel="name" filterMatchMode="contains" :filterFields="name" :emptyFilterMessage="$t('common.info.noDataFound')" @change="showForm" data-test="kpi-list">
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" data-test="list-item">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.name }}</span>
                                <span class="kn-list-item-text-secondary">{{ formatDate(slotProps.option.dateCreation) }}</span>
                            </div>
                            <Button icon="far fa-copy" class="p-button-text p-button-rounded p-button-plain" @click.stop="emitCopyKpi(slotProps.option.id, slotProps.option.version)" data-test="copy-button" />
                            <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteKpiConfirm(slotProps.option.id, slotProps.option.version)" data-test="delete-button" />
                        </div>
                    </template>
                </Listbox>
            </div>

            <div class="kn-list--column p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <router-view :cloneKpiId="cloneKpiId" :cloneKpiVersion="cloneKpiVersion" @touched="touched = true" @closed="onFormClose" @kpiUpdated="touched = false" @kpiCreated="onKpiCreated" @showDialog="displayInfoDialog" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import FabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
export default defineComponent({
    name: 'tenant-management',
    components: {
        FabButton,
        Listbox
    },
    data() {
        return {
            loading: false,
            touched: false,
            displayModal: false,
            hintVisible: true,
            cloneKpi: false,
            kpiList: [] as any,
            kpiToClone: {} as any,
            cloneKpiId: Number,
            cloneKpiVersion: Number
        }
    },
    async created() {
        await this.getKpiList()
    },
    methods: {
        async getKpiList() {
            this.loading = true
            return axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/listKpi`)
                .then((response) => {
                    this.kpiList = [...response.data]
                })
                .finally(() => (this.loading = false))
        },

        deleteKpiConfirm(kpiId: number, kpiVersion: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteKpi(kpiId, kpiVersion)
            })
        },
        async deleteKpi(kpiId: number, kpiVersion: number) {
            await axios.delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${kpiId}/${kpiVersion}/deleteKpi`).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.$router.push('/kpi-definition')
                this.getKpiList()
            })
        },
        showForm(event: any) {
            const path = event.value ? `/kpi-definition/${event.value.id}/${event.value.version}` : '/kpi-definition/new-kpi'
            this.hintVisible = false
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
        pageReload() {
            this.touched = false
            this.hintVisible = true
        },
        onFormClose() {
            this.touched = false
            this.hintVisible = true
        },
        formatDate(date) {
            let fDate = new Date(date)
            return fDate.toLocaleString()
        },
        async onKpiCreated(event) {
            await this.getKpiList()

            let kpiToLoad = this.kpiList.find((kpi) => {
                if (kpi.name === event) return true
            })
            const path = `/kpi-definition/${kpiToLoad.id}/${kpiToLoad.version}`
            this.$router.push(path)

            this.touched = false
            this.hintVisible = false
        },
        emitCopyKpi(kpiId, kpiVersion) {
            this.$router.push('/kpi-definition/new-kpi')
            this.hintVisible = false
            setTimeout(() => {
                this.cloneKpiId = kpiId
                this.cloneKpiVersion = kpiVersion
            }, 200)
        }
    }
})
</script>
