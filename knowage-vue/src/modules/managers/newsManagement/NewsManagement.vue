<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.newsManagement.title') }}
                    </template>
                    <template #end>
                        <FabButton icon="fas fa-plus" @click="showForm" data-test="open-form-button" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <Listbox
                    v-if="!loading"
                    class="kn-list--column"
                    :options="newsList"
                    listStyle="max-height:calc(100% - 62px)"
                    :filter="true"
                    :filterPlaceholder="$t('common.search')"
                    optionLabel="name"
                    filterMatchMode="contains"
                    :filterFields="newsManagementDescriptor.filterFields"
                    :emptyFilterMessage="$t('managers.newsManagement.noResults')"
                    @change="showForm"
                    data-test="news-list"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" data-test="list-item">
                            <Avatar :icon="newsManagementDescriptor.iconTypesMap[slotProps.option.type].className" shape="circle" size="medium" />
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.title }}</span>
                                <span class="kn-list-item-text-secondary">{{ slotProps.option.description }}</span>
                            </div>
                            <Button icon="far fa-trash-alt" class="p-button-link p-button-sm" @click.stop="deleteNewsConfirm(slotProps.option)" data-test="delete-button" />
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

export default defineComponent({
    name: 'news-management',
    components: {
        Avatar,
        FabButton,
        Listbox
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
                    this.deleteNews(news)
                }
            })
        },
        async deleteNews(news) {
            await this.$http.delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/news/' + news.id).then(() => {
                this.store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                WEB_SOCKET.send(JSON.stringify(news))
                this.$router.push('/news-management')
                this.loadAllNews()
            })
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
