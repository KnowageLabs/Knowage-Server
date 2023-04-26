<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.newsManagement.title') }}
                    </template>
                    <template #end>
                        <FabButton icon="fas fa-plus" data-test="open-form-button" @click="showForm" />
                    </template>
                </Toolbar>
                <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />
                <Listbox
                    v-if="!loading"
                    class="kn-list--column"
                    :options="newsList"
                    list-style="max-height:calc(100% - 62px)"
                    :filter="true"
                    :filter-placeholder="$t('common.search')"
                    option-label="name"
                    filter-match-mode="contains"
                    :filter-fields="newsManagementDescriptor.filterFields"
                    :empty-filter-message="$t('managers.newsManagement.noResults')"
                    data-test="news-list"
                    @change="showForm"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" data-test="list-item">
                            <Avatar :icon="newsManagementDescriptor.iconTypesMap[slotProps.option.type].className" shape="circle" size="medium" />
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.title }}</span>
                                <span class="kn-list-item-text-secondary">{{ slotProps.option.description }}</span>
                            </div>
                            <Button icon="far fa-trash-alt" class="p-button-link p-button-sm" data-test="delete-button" @click.stop="deleteNewsConfirm(slotProps.option)" />
                        </div>
                    </template>
                </Listbox>
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <router-view @touched="touched = true" @closed="touched = false" @inserted="pageReload" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iNews } from './NewsManagement'
import Avatar from 'primevue/avatar'
import { AxiosResponse } from 'axios'
import FabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import newsManagementDescriptor from './NewsManagementDescriptor.json'
import WEB_SOCKET from '@/services/webSocket.js'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'news-management',
    components: {
        Avatar,
        FabButton,
        Listbox
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            newsManagementDescriptor,
            newsList: [] as iNews[],
            touched: false,
            loading: false
        }
    },
    async created() {
        await this.loadAllNews()
    },
    methods: {
        async loadAllNews() {
            this.loading = true
            this.newsList = []
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/news')
                .then((response: AxiosResponse<any>) => {
                    response.data.map((news: iNews) => {
                        this.newsList.push({ ...news, newsType: this.setNewsType(news.type) })
                    })
                })
                .finally(() => (this.loading = false))
        },
        showForm(event: any) {
            const path = event.value ? `/news-management/${event.value.id}` : '/news-management/new-news'
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
        deleteNewsConfirm(news) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    this.touched = false
                    this.onDeleteNewsConfirm(news)
                }
            })
        },
        async onDeleteNewsConfirm(news) {
            const response = await this.deleteNews(news)
            if (response) this.deleteNewsWebSocket(news)
        },
        async deleteNews(news) {
            let responseOk = false
            await this.$http.delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/news/' + news.id).then(() => {
                this.store.setInfo({
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                responseOk = true
                this.$router.push('/news-management')
                this.loadAllNews()
            })
            return responseOk
        },
        deleteNewsWebSocket(news) {
            WEB_SOCKET.send(JSON.stringify(news))
        },
        pageReload() {
            this.touched = false
            this.loadAllNews()
        },
        setNewsType(type: number) {
            switch (type) {
                case 1:
                    return 'News'
                case 2:
                    return 'Notification'
                case 3:
                    return 'Warning'
            }
        }
    }
})
</script>
