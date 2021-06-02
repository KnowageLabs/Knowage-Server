<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ $t('managers.newsManagement.detailTitle') }}</template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="handleSubmit" :disabled="buttonDisabled" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="card">
        <NewsDetailCard :selectedNews="selectedNews"></NewsDetailCard>
    </div>
    <div class="card">
        <RolesCard :categoryList="roleList" :selected="selectedRoles" @changed="setSelectedRoles($event)"></RolesCard>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iNews } from './NewsManagement'
import axios from 'axios'
import moment from 'moment'
import NewsDetailCard from './cards/NewsDetailCard/NewsDetailCard.vue'
import newsManagementDetailDescriptor from './NewsManagementDetailDescriptor.json'
import RolesCard from './cards/RolesCard/RolesCard.vue'
import useValidate from '@vuelidate/core'

export default defineComponent({
    components: {
        NewsDetailCard,
        RolesCard
    },
    props: {
        id: {
            type: String,
            required: false
        }
    },
    emits: ['touched', 'closed', 'inserted'],
    data() {
        return {
            newsManagementDetailDescriptor,
            selectedNews: {} as iNews,
            roleList: [],
            selectedRoles: [],
            loading: false,
            operation: 'insert',
            v$: useValidate() as any
        }
    },
    computed: {
        buttonDisabled(): any {
            return this.v$.$invalid
        }
    },
    watch: {
        id() {
            this.loadSelectedNews()
        }
    },
    async created() {
        await this.loadSelectedNews()
        await this.loadRoles()
    },
    methods: {
        async handleSubmit() {
            if (this.v$.$invalid) {
                return
            }
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/news/'
            if (this.selectedNews.id) {
                this.operation = 'update'
                url += this.selectedNews.id
            }
            await axios.post(url, this.selectedNews).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t(this.newsManagementDetailDescriptor.operation[this.operation].toastTitle),
                    msg: this.$t(this.newsManagementDetailDescriptor.operation.success)
                })
                this.$emit('inserted')
                this.$router.replace('/news-management')
            })
        },
        async loadSelectedNews() {
            this.loading = true
            if (this.id) {
                // await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/news/${this.id}`).then((response) => (this.selectedNews = response.data))
                this.selectedNews = {
                    id: '1',
                    title: 'First news',
                    description: 'First news',
                    type: {
                        id: 1,
                        value: 'News'
                    },
                    html: '',
                    expirationDate: moment.unix(1622622803).format('MM/DD/YYYY'),
                    active: true
                }
            } else {
                this.selectedNews = {} as iNews
            }
            this.loading = false
        },
        async loadRoles() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/roles')
                .then((response) => {
                    this.roleList = response.data
                })
                .finally(() => (this.loading = false))
        },
        setDirty() {
            this.$emit('touched')
        },
        closeTemplate() {
            this.$router.push('/news-management')
            this.$emit('closed')
        },
        setSelectedRoles(roles) {
            this.selectedRoles = roles
            this.$emit('touched')
        }
    }
})
</script>
