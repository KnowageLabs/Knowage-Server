<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.lovsManagement.title') }}
                    </template>
                    <template #end>
                        <FabButton icon="fas fa-plus" @click="showForm" data-test="new-button" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <KnListBox :options="lovsList" :settings="lovsManagementDescriptor.knListSettings" @delete="deleteLovConfirm($event)" data-test="lovs-list"></KnListBox>
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-page">
                <router-view :lovs="lovsList" @touched="touched = true" @closed="touched = false" @created="loadLovs" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iLov } from './LovsManagement'
import { AxiosResponse } from 'axios'
import FabButton from '@/components/UI/KnFabButton.vue'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'
import lovsManagementDescriptor from './LovsManagementDescriptor.json'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'lovs-management',
    components: { FabButton, KnListBox },
    data() {
        return {
            lovsManagementDescriptor,
            lovsList: [] as iLov[],
            loading: false,
            touched: false
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    async created() {
        await this.loadLovs()
    },
    methods: {
        async loadLovs() {
            this.touched = false
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/lovs/get/all')
                .then((response: AxiosResponse<any>) => {
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
        deleteLovConfirm(event) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    this.touched = false
                    this.deleteLov(event.item.id)
                }
            })
        },
        async deleteLov(lovId: number) {
            await this.$http
                .delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/lovs/delete/${lovId}`)
                .then(() => {
                    this.store.setInfo({
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    this.$router.push('/lovs-management')
                    this.loadLovs()
                })
                .catch((error) => {
                    this.store.setError({
                        title: 'Server error',
                        msg: error.message
                    })
                })
        }
    }
})
</script>
