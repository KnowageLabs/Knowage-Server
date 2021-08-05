<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('managers.lovsManagement.title') }}
                    </template>
                    <template #right>
                        <FabButton icon="fas fa-plus" @click="showForm" data-test="new-button" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <Listbox
                    v-if="!loading"
                    class="kn-list--column"
                    :options="lovsList"
                    :filter="true"
                    :filterPlaceholder="$t('common.search')"
                    filterMatchMode="contains"
                    :filterFields="lovsManagementDescriptor.filterFields"
                    :emptyFilterMessage="$t('common.info.noDataFound')"
                    @change="showForm($event.value)"
                    data-test="lovs-list"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" :data-test="'list-item-' + slotProps.option.id" v-tooltip.top="slotProps.option.description">
                            <Avatar :icon="lovsManagementDescriptor.iconTypesMap[slotProps.option.itypeCd]" shape="circle" size="medium" />
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.label }}</span>
                                <span class="kn-list-item-text-secondary kn-truncated">{{ slotProps.option.name }}</span>
                            </div>
                            <Button icon="far fa-trash-alt" class="p-button-link p-button-sm" @click.stop="deleteLovConfirm(slotProps.option.id)" :data-test="'delete-button-' + slotProps.option.id" />
                        </div>
                    </template>
                </Listbox>
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <router-view :lovs="lovsList" @touched="touched = true" @closed="touched = false" @created="loadLovs" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iLov } from './LovsManagement'
import Avatar from 'primevue/avatar'
import axios from 'axios'
import FabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import lovsManagementDescriptor from './LovsManagementDescriptor.json'

export default defineComponent({
    name: 'lovs-management',
    components: { Avatar, FabButton, Listbox },
    data() {
        return {
            lovsManagementDescriptor,
            lovsList: [] as iLov[],
            loading: false,
            touched: false
        }
    },
    async created() {
        await this.loadLovs()
    },
    methods: {
        async loadLovs() {
            this.touched = false
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/lovs/get/all')
                .then((response) => {
                    this.lovsList = response.data
                    this.lovsList.sort((a: iLov, b: iLov) => (a.label.toUpperCase() > b.label.toUpperCase() ? 1 : -1))
                })
                .finally(() => (this.loading = false))
        },
        showForm(event: any) {
            const path = event.id ? `/lovs-management/${event.id}` : '/lovs-management/new-lov'
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
        deleteLovConfirm(lovId: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    this.touched = false
                    this.deleteLov(lovId)
                }
            })
        },
        async deleteLov(lovId: number) {
            await axios.delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/lovs/delete/${lovId}`).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.$router.push('/lovs-management')
                this.loadLovs()
            })
        }
    }
})
</script>
