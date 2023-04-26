<template>
    <div class="kn-page-content p-grid p-m-0">
        <div class="p-col-4 p-sm-4 p-md-3 p-p-0">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('managers.driversManagement.title') }}
                </template>
                <template #end>
                    <FabButton icon="fas fa-plus" data-test="open-form-button" @click="showForm" />
                </template>
            </Toolbar>
            <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />
            <Listbox
                v-if="!loading"
                class="kn-list--column"
                :options="drivers"
                option-label="label"
                :filter="true"
                :filter-placeholder="$t('common.search')"
                filter-match-mode="contains"
                :filter-fields="driversManagementDescriptor.filterFields"
                :empty-filter-message="$t('common.info.noDataFound')"
                data-test="drivers-list"
                @change="showForm"
            >
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item" data-test="list-item">
                        <Avatar
                            v-tooltip="driversManagementDescriptor.iconTypesMap[slotProps.option.type].tooltip"
                            :icon="driversManagementDescriptor.iconTypesMap[slotProps.option.type].icon"
                            :style="driversManagementDescriptor.iconTypesMap[slotProps.option.type].style"
                            shape="circle"
                            size="medium"
                        />
                        <div v-tooltip.top="slotProps.option.description" class="kn-list-item-text">
                            <span>{{ slotProps.option.label }}</span>
                            <span class="kn-list-item-text-secondary">{{ slotProps.option.name }}</span>
                        </div>
                        <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" data-test="delete-button" @click.stop="deleteDriverConfirm(slotProps.option.id)" />
                    </div>
                </template>
            </Listbox>
        </div>
        <div class="kn-list--column p-col-8 p-sm-8 p-md-9 p-p-0">
            <KnHint v-if="!formVisible" :title="'managers.driversManagement.title'" :hint="'managers.driversManagement.hint'"></KnHint>
            <DriversManagementDetail v-else :selected-driver="selectedDriver" data-test="drivers-form" @created="handleSave" @close="closeForm" @touched="touched = true"></DriversManagementDetail>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iDriver } from './DriversManagement'
import { AxiosResponse } from 'axios'
import FabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import Avatar from 'primevue/avatar'
import DriversManagementDetail from './DriversManagementDetail.vue'
import driversManagementDescriptor from './DriversManagementDescriptor.json'
import KnHint from '@/components/UI/KnHint.vue'
import Tooltip from 'primevue/tooltip'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'constraint-management',
    components: {
        FabButton,
        KnHint,
        Listbox,
        Avatar,
        DriversManagementDetail
    },
    directives: {
        tooltip: Tooltip
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            loading: false,
            touched: false,
            formVisible: false,
            driversManagementDescriptor,
            drivers: [] as iDriver[],
            selectedDriver: {} as iDriver
        }
    },
    created() {
        this.loadAllDrivers()
    },
    methods: {
        async loadAllDrivers() {
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers')
                .then((response: AxiosResponse<any>) => (this.drivers = response.data))
                .finally(() => (this.loading = false))
        },
        showForm(event: any) {
            if (!this.touched) {
                this.setSelectedDriver(event)
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.setSelectedDriver(event)
                    }
                })
            }
        },
        closeForm() {
            if (!this.touched) {
                this.formVisible = false
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.formVisible = false
                    }
                })
            }
        },
        setSelectedDriver(event: any) {
            if (event) {
                this.selectedDriver = event.value
            }
            this.formVisible = true
        },
        handleSave(event: any) {
            this.loadAllDrivers()
            this.touched = false
            this.selectedDriver = event
        },
        deleteDriverConfirm(id: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteDriver(id)
            })
        },
        async deleteDriver(id: number) {
            await this.$http
                .delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers/' + id)
                .then(() => {
                    this.store.setInfo({
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    this.loadAllDrivers()
                    this.formVisible = false
                })
                .catch((error) => {
                    this.store.setError({
                        title: this.$t('managers.driversManagement.deleteError'),
                        msg: error.message
                    })
                })
        }
    }
})
</script>
