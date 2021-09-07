<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="p-col-4 p-sm-4 p-md-3 p-p-0 kn-page">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('managers.widgetGallery.title') }}
                    </template>
                    <template #right>
                        <KnFabButton icon="fas fa-plus" @click="showForm" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
                <KnListBox :options="navigations" :settings="crossNavigationDescriptor.knListSettings" @delete="deleteTemplate($event, item)"></KnListBox>
            </div>
            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-router-view">
                <router-view @close="closeForm" @touched="touched = true" @saved="reload" />
            </div>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'
import crossNavigationDescriptor from './CrossNavigationManagementDescriptor.json'

export default defineComponent({
    name: 'navigation-management',
    components: { KnFabButton, KnListBox },
    data() {
        return {
            navigations: [] as any,
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
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/crossNavigation/listNavigation/')
                .then((response) => (this.navigations = response.data))
                .finally(() => (this.loading = false))
        },
        deleteTemplate(e, itemId): void {
            console.log(e, itemId)
            e.preventDefault()
            if (e.item && e.item.id) itemId = e.item.id
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    this.axios
                        .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/crossNavigation/remove', "{'id':" + itemId + '}')
                        .then(() => {
                            this.$store.commit('setInfo', { title: this.$t('common.toast.deleteTitle'), msg: this.$t('common.toast.deleteSuccess') })
                            this.loadAll()
                            if (itemId == this.$route.params.id) this.$router.push('/cross-navigation-management')
                        })
                        .catch((error) => console.error(error))
                }
            })
        },
        showForm() {
            const path = '/cross-navigation-management/new-navigation'
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
        }
    }
})
</script>
