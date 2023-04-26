<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="p-col-4 p-sm-4 p-md-3 p-p-0 kn-page">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.businessModelManager.title') }}
                    </template>
                    <template #end>
                        <FabButton icon="fas fa-plus" data-test="new-button" @click="showForm" />
                    </template>
                </Toolbar>
                <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />
                <KnListBox :options="businessModelList" :settings="businessModelCatalogueDescriptor.knListSettings" @click="showForm" @delete.stop="deleteBusinessModelConfirm" />
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-page">
                <KnHint v-if="showHint" :title="'managers.businessModelManager.title'" :hint="'managers.businessModelManager.hint'" data-test="bm-hint"></KnHint>
                <router-view @touched="touched = true" @closed="onClose" @inserted="pageReload" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iBusinessModel } from './BusinessModelCatalogue'
import { AxiosResponse } from 'axios'
import businessModelCatalogueDescriptor from './BusinessModelCatalogueDescriptor.json'
import FabButton from '@/components/UI/KnFabButton.vue'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'
import KnHint from '@/components/UI/KnHint.vue'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'business-model-catalogue',
    components: {
        FabButton,
        KnListBox,
        KnHint
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            businessModelCatalogueDescriptor,
            businessModelList: [] as iBusinessModel[],
            showHint: true,
            touched: false,
            loading: false
        }
    },
    async created() {
        if (this.$route.path !== '/business-model-catalogue') {
            this.showHint = false
        }
        await this.loadAllCatalogues()
    },
    methods: {
        async loadAllCatalogues() {
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/businessmodels')
                .then((response: AxiosResponse<any>) => (this.businessModelList = response.data))
                .finally(() => (this.loading = false))
        },
        showForm(event: any) {
            this.showHint = false
            const path = event.item ? `/business-model-catalogue/${event.item.id}` : '/business-model-catalogue/new-business-model'
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
        deleteBusinessModelConfirm(event: any) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    this.touched = false
                    this.deleteBusinessModel(event.item.id)
                }
            })
        },
        async deleteBusinessModel(businessModelId: number) {
            await this.$http.delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/businessmodels/' + businessModelId).then(() => {
                this.store.setInfo({
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.$router.replace('/business-model-catalogue')
                this.loadAllCatalogues()
            })
        },
        pageReload() {
            this.touched = false
            this.loadAllCatalogues()
        },
        onClose() {
            this.touched = false
            this.showHint = true
        }
    }
})
</script>
