<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="p-col-4 p-sm-4 p-md-3 p-p-0 kn-page">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.crossNavigationManagement.title') }}
                    </template>
                    <template #end>
                        <KnFabButton icon="fas fa-plus" @click="showForm(-1)" data-test="new-button" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <KnListBox :options="navigations" :settings="crossNavigationDescriptor.knListSettings" @click="selected($event, item)" @delete.stop="deleteTempateConfirm($event, item)"></KnListBox>
            </div>
            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-router-view">
                <router-view @close="closeForm" @touched="touched = true" @saved="reload" />
            </div>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iNavigation } from './CrossNavigationManagement'
import { AxiosResponse } from 'axios'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'
import crossNavigationDescriptor from './CrossNavigationManagementDescriptor.json'

export default defineComponent({
    name: 'navigation-management',
    components: { KnFabButton, KnListBox },
    data() {
        return {
            navigations: [] as iNavigation[],
            loading: false,
            touched: false,
            crossNavigationDescriptor
        }
    },
    created() {
        this.loadAll()
    },
    methods: {
        async loadAll() {
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/crossNavigation/listNavigation/')
                .then((response: AxiosResponse<any>) => (this.navigations = response.data))
                .finally(() => (this.loading = false))
        },
        selected(e, itemId) {
            if (e.item && e.item.id) itemId = e.item.id
            this.showForm(itemId)
        },
        deleteTempateConfirm(event: any): void {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: async () => await this.deleteTemplate(event.item.id)
            })
        },
        async deleteTemplate(itemId: string) {
            this.loading = true
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/crossNavigation/remove', "{'id':" + itemId + '}')
                .then(async () => {
                    this.store.commit('setInfo', { title: this.$t('common.toast.deleteTitle'), msg: this.$t('common.toast.deleteSuccess') })
                    await this.loadAll()
                    if (itemId == this.$route.params.id) this.$router.push('/cross-navigation-management')
                })
                .catch((error) =>
                    this.store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: error.message
                    })
                )
            this.loading = false
        },
        showForm(id: number) {
            const path = id !== -1 ? '/cross-navigation-management/' + id : '/cross-navigation-management/new-navigation'
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
            this.$router.replace('/cross-navigation-management')
        },
        async reload(operation, name) {
            await this.loadAll()
            this.touched = false
            if (operation === 'insert') {
                let id = this.navigations.find((nav) => nav.name === name)?.id
                this.$router.push('/cross-navigation-management/' + id)
            }
        }
    }
})
</script>
