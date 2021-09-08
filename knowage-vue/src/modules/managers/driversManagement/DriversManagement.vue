<template>
    <div class="kn-page-content p-grid p-m-0">
        <div class="p-col-4 p-sm-4 p-md-3 p-p-0">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('managers.driversManagement.title') }}
                </template>
                <template #right>
                    <FabButton icon="fas fa-plus" @click="showForm" data-test="open-form-button" />
                </template>
            </Toolbar>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
            <Listbox
                v-if="!loading"
                class="kn-list--column"
                :options="drivers"
                optionLabel="label"
                :filter="true"
                :filterPlaceholder="$t('common.search')"
                filterMatchMode="contains"
                :filterFields="driversManagementDescriptor.filterFields"
                :emptyFilterMessage="$t('common.info.noDataFound')"
                @change="showForm"
                data-test="drivers-list"
            >
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item" data-test="list-item">
                        <Avatar
                            :icon="driversManagementDescriptor.iconTypesMap[slotProps.option.type].icon"
                            :style="driversManagementDescriptor.iconTypesMap[slotProps.option.type].style"
                            v-tooltip="driversManagementDescriptor.iconTypesMap[slotProps.option.type].tooltip"
                            shape="circle"
                            size="medium"
                        />
                        <div class="kn-list-item-text" v-tooltip.top="slotProps.option.description">
                            <span>{{ slotProps.option.label }}</span>
                            <span class="kn-list-item-text-secondary">{{ slotProps.option.name }}</span>
                        </div>
                        <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteDriverConfirm(slotProps.option.id)" data-test="delete-button" />
                    </div>
                </template>
            </Listbox>
        </div>
        <div class="kn-list--column p-col-8 p-sm-8 p-md-9 p-p-0">
            <KnHint :title="'managers.driversManagement.title'" :hint="'managers.driversManagement.hint'" v-if="!formVisible"></KnHint>
            <DriversManagementDetail :selectedDriver="selectedDriver" @created="handleSave" @close="closeForm" @touched="touched = true" v-else></DriversManagementDetail>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iDriver } from './DriversManagement'
import axios from 'axios'
import FabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import Avatar from 'primevue/avatar'
import DriversManagementDetail from './DriversManagementDetail.vue'
import driversManagementDescriptor from './DriversManagementDescriptor.json'
import KnHint from '@/components/UI/KnHint.vue'
import Tooltip from 'primevue/tooltip'
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
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers')
                .then((response) => (this.drivers = response.data))
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
            await axios
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers/' + id)
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    this.loadAllDrivers()
                    this.formVisible = false
                })
                .catch((error) => {
                    this.$store.commit('setError', {
                        title: this.$t('managers.driversManagement.deleteError'),
                        msg: error.message
                    })
                })
        }
    }
})
</script>
