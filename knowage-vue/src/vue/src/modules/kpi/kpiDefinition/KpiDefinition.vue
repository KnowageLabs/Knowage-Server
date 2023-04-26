<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('kpi.kpiDefinition.title') }}
                    </template>
                    <template #end>
                        <FabButton icon="fas fa-plus" data-test="open-form-button" @click="showForm" />
                    </template>
                </Toolbar>
                <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />
                <Listbox v-if="!loading" class="kn-list--column" :options="kpiList" :filter="true" :filter-placeholder="$t('common.search')" option-label="name" filter-match-mode="contains" :filter-fields="name" :empty-filter-message="$t('common.info.noDataFound')" data-test="kpi-list" @change="showForm">
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" data-test="list-item">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.name }}</span>
                                <span v-if="slotProps.option.category" class="kn-list-item-text-secondary">{{ slotProps.option.category.valueDescription }}</span>
                            </div>
                            <Button icon="far fa-copy" class="p-button-text p-button-rounded p-button-plain" data-test="copy-button" @click.stop="emitCopyKpi(slotProps.option.id, slotProps.option.version)" />
                            <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" data-test="delete-button" @click.stop="deleteKpiConfirm(slotProps.option.id, slotProps.option.version)" />
                        </div>
                    </template>
                </Listbox>
            </div>

            <div class="kn-list--column p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <router-view :clone-kpi-id="cloneKpiId" :clone-kpi-version="cloneKpiVersion" @touched="touched = true" @closed="onFormClose" @kpiUpdated="reloadAndReroute" @kpiCreated="reloadAndReroute" @showDialog="displayInfoDialog" @onGuideClose="showGuide = false" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import FabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import { formatDateWithLocale } from '@/helpers/commons/localeHelper'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'tenant-management',
    components: {
        FabButton,
        Listbox
    },
    setup() {
        const store = mainStore()
        return { store }
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
            return this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/kpi/listKpi`)
                .then((response: AxiosResponse<any>) => {
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
            await this.$http.delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/kpi/${kpiId}/${kpiVersion}/deleteKpi`).then(() => {
                this.store.setInfo({
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
            return formatDateWithLocale(date, { dateStyle: 'short', timeStyle: 'short' })
        },
        async reloadAndReroute(event) {
            await this.getKpiList()

            const kpiToLoad = this.kpiList.find((kpi) => {
                if (kpi.name === event) return true
            })
            let path = ''
            if (kpiToLoad) {
                path = `/kpi-definition/${kpiToLoad.id}/${kpiToLoad.version}`
            }
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
