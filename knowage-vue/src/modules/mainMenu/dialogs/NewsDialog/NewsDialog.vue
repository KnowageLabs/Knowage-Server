<template>
    <Dialog class="kn-dialog--toolbar--primary knNewsDialog" v-bind:visible="visibility" footer="footer" :header="$t('newsDialog.title')" :closable="false" modal>
        <TabView class="knTab kn-tab" @tab-click="emptySelectedNews()">
            <TabPanel v-for="(type, index) in news" v-bind:key="index" :header="$t(typeDescriptor.newsType[index].label)">
                <div class="knPageContent p-grid p-m-0 p-p-0">
                    <div class="p-col-5 ">
                        <Listbox class="kn-list" :options="news[index]" optionLabel="title" style="width:20rem" listStyle="max-height:250px">
                            <template #option="slotProps">
                                <div class="kn-list-item" @click="getNews(slotProps.option.id)">
                                    <Avatar :icon="typeDescriptor.newsType[slotProps.option.type].className" shape="circle" size="medium" :style="typeDescriptor.newsType[slotProps.option.type].style" />
                                    <div class="kn-list-item-text">
                                        <span>{{ slotProps.option.title }}</span>
                                    </div>
                                </div>
                            </template>
                        </Listbox>
                    </div>
                    <div class="p-col-7 p-flex-column newsContainer" v-if="Object.keys(selectedNews).length != 0">
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
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.close') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Avatar from 'primevue/avatar'
import Dialog from 'primevue/dialog'
import Listbox from 'primevue/listbox'
import { mapState } from 'vuex'
import axios from 'axios'
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
            news: {}
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
        getNews(id) {
            if (id != this.selectedNews.id) {
                axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/news/' + id + '?isTechnical=false').then(
                    (response) => {
                        console.log(response)
                        this.selectedNews = response.data

                        if (!this.selectedNews.read) {
                            axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/newsRead/' + id).then(
                                () => {
                                    WS.send(JSON.stringify({ news: true }))
                                },
                                (error) => console.error(error)
                            )
                        }
                    },
                    (error) => console.error(error)
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
            if (newVisibility && Object.keys(this.news).length === 0) {
                let newsReadArray = Array<number>()
                axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/newsRead').then(
                    (response) => {
                        newsReadArray = response.data
                    },
                    (error) => console.error(error)
                )

                axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/news').then(
                    (response) => {
                        var jsonData = {}
                        response.data.forEach(function(column: SingleNews) {
                            let type = column.type.toString()
                            if (!jsonData[type]) jsonData[type] = []
                            if (newsReadArray.indexOf(column.id) != -1) column.read = true
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
        min-height: 400px;
        max-height: 600px;

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
    min-width: 800px;
    max-width: 1200px;
    width: 800px;
    height: 75%;
}

.kn-list-column {
    border-right: 1px solid #ccc;
}

.h4-text {
    font-weight: normal;
}
</style>
