<template>
    <Dialog class="kn-dialog--toolbar--primary knNewsDialog" v-bind:visible="visibility" footer="footer" :header="$t('newsDialog.title')" :closable="false" modal>
        <TabView class="knTab kn-tab" @tab-click="emptySelectedNews()">
            <TabPanel v-for="(type, index) in news" v-bind:key="index" :header="$t(typeDescriptor.newsType[index].label)">
                <div class="knPageContent p-grid p-m-0 p-p-0">
                    <div class="p-col-4 p-p-0">
                        <Listbox class="kn-list" :options="news[index]" optionLabel="title" listStyle="max-height:250px">
                            <template #option="slotProps">
                                <div class="kn-list-item" @click="getNews(slotProps.option.id)">
                                    <Avatar :icon="typeDescriptor.newsType[slotProps.option.type].className" shape="circle" size="medium" :style="typeDescriptor.newsType[slotProps.option.type].style" />
                                    <div class="kn-list-item-text">
                                        <span>{{ slotProps.option.title }}</span>
                                    </div>
                                    <span v-if="slotProps.option.read"> <Avatar :icon="typeDescriptor.icons.read.icon" shape="circle" size="medium" :style="typeDescriptor.icons.read.style" /></span
                                    ><span v-else><Avatar :icon="typeDescriptor.icons.unread.icon" shape="circle" size="medium" :style="typeDescriptor.icons.unread.style" /></span>
                                </div>
                            </template>
                        </Listbox>
                    </div>
                    <div class="p-col-8 p-flex-column newsContainer p-p-0" v-if="Object.keys(selectedNews).length != 0">
                        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                        <h4>
                            <div class="p-col">
                                {{ $t('newsDialog.description') }}: <span class="h4-text">{{ selectedNews.description }}</span>
                            </div>
                            <div class="p-col">
                                {{ $t('newsDialog.expirationDate') }} : <span class="h4-text">{{ getDate() }}</span>
                            </div>
                        </h4>
                        <div class="p-col">
                            <p v-html="selectedNews.html" disabled></p>
                        </div>
                    </div>
                    <div class="p-col-7 p-d-flex p-ai-center p-jc-center" v-else>{{ $t('common.info.noElementSelected') }}</div>
                </div>
            </TabPanel>
        </TabView>
        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"><ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" /> {{ $t('common.close') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Avatar from 'primevue/avatar'
import Dialog from 'primevue/dialog'
import Listbox from 'primevue/listbox'
import { mapState } from 'vuex'
import { AxiosResponse } from 'axios'
import newsDialogDescriptor from './NewsDialogDescriptor.json'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import { formatDate } from '@/helpers/commons/localeHelper'
import WS from '@/services/webSocket'

interface SingleNews {
    description?: string
    expirationDate?: string
    html?: string
    id: number
    read?: boolean | false
    title?: string
    type: number
}

export default defineComponent({
    name: 'news-dialog',
    components: { Avatar, Dialog, Listbox, TabView, TabPanel },
    data() {
        return {
            typeDescriptor: newsDialogDescriptor,
            selectedNews: {} as SingleNews,
            news: {},
            loading: true,
            newsReadArray: Array<number>()
        }
    },
    created() {},
    props: {
        visibility: Boolean
    },
    emits: ['update:visibility'],
    methods: {
        emptySelectedNews() {
            this.selectedNews = {} as SingleNews
        },
        getDate() {
            return formatDate(this.selectedNews.expirationDate, 'LLL')
        },
        closeDialog() {
            this.$emit('update:visibility', false)
        },
        async getNews(id) {
            if (id != this.selectedNews.id) {
                this.loading = true
                await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/news/' + id + '?isTechnical=false').then(
                    (response: AxiosResponse<any>) => {
                        console.log(response)
                        if (response.data.errors) {
                            this.$store.commit('setError', { title: this.$t('common.error.news'), msg: this.$t('news.errorGettingSelectedNews') })
                        } else {
                            this.selectedNews = response.data
                            this.loading = false
                            if (!this.selectedNews.read) {
                                this.$http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/newsRead/' + id).then(
                                    () => {
                                        WS.send(JSON.stringify({ news: true }))
                                    },
                                    (error) => console.error(error)
                                )

                                this.selectedNews.read = true
                                var stop = false
                                for (var idx in this.news) {
                                    let currNewsArray = this.news[idx]

                                    for (var index in currNewsArray) {
                                        let currNews = currNewsArray[index]
                                        if (currNews.id == id) {
                                            currNews.read = true
                                            stop = true
                                            break
                                        }
                                    }

                                    if (stop) break
                                }
                            }
                        }
                    },
                    () => {
                        this.loading = false
                    }
                )
            }
        }
    },
    computed: {
        ...mapState({
            locale: 'locale'
        })
    },
    watch: {
        visibility(newVisibility) {
            if (newVisibility) {
                this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/newsRead').then(
                    (response: AxiosResponse<any>) => {
                        this.newsReadArray = []
                        this.newsReadArray = response.data
                    },
                    (error) => console.error(error)
                )

                this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/news').then(
                    (response: AxiosResponse<any>) => {
                        var jsonData = {}
                        let localNewsReadArray = this.newsReadArray
                        response.data.forEach(function (column: SingleNews) {
                            let type = column.type.toString()
                            if (!jsonData[type]) jsonData[type] = []
                            if (localNewsReadArray.indexOf(column.id) != -1) column.read = true
                            jsonData[type].push(column)
                        })
                        this.news = jsonData
                    },
                    (error) => console.error(error)
                )
            }
        }
    }
})
</script>

<style scoped lang="scss">
.newsDialog {
    min-width: 800px;
    max-width: 1200px;
    width: 800px;
}
.knTab {
    &.p-tabview {
        display: flex;
        flex-direction: column;
        min-height: 400px;
        max-height: 600px;
        &:deep(.p-tabview-panels) {
            padding: 0;
            flex: 1;
            display: flex;
            flex-direction: column;
        }
        &:deep(.p-tabview-panel) {
            flex: 1;
            display: flex;
        }

        &:deep(.p-tabview-title) {
            text-transform: uppercase;
            margin: 0;
            min-height: 40%;
        }
    }
    .newsContainer {
        overflow-x: hidden;
    }
}
.knPageContent {
    flex: 1;
    min-width: 800px;
    max-width: 1200px;
    width: 800px;
    & > div {
        overflow: auto;
        &:first-child {
            border-right: 1px solid var(--kn-color-borders);
        }
    }
}
.kn-list {
    border-right: none !important;
}

.h4-text {
    font-weight: normal;
}
</style>
