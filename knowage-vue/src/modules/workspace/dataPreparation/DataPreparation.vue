<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
            <template #left> Data preparation </template>
            <template #right>
                <KnFabButton icon="fas fa-plus" @click="showForm" data-test="open-form-button"></KnFabButton>
            </template>
        </Toolbar>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        <div class="kn-page-content managerDetail p-grid p-m-0 p-fluid">
            <Card class="p-col-2 p-m-1 p-p-0" v-for="(item, index) in dataPreparation" v-bind:key="index">
                <template #content>
                    <div class="p-col-5 d-flex flex-row mb-3">
                        <img src="https://i.imgur.com/ccMhxvC.png" width="50" />
                    </div>
                    <div class="p-col-7">
                        <h3>{{ item.label }}</h3>
                    </div>

                    <div class="p-col-12 p-m-0 p-p-0 kn-flex">
                        <p>{{ item.description }}</p>
                    </div>
                    <div class="p-col-12 p-m-0 p-p-0 p-d-flex p-jc-end">
                        <Button icon="pi pi-search" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.search')" @click="search($event, item)" />

                        <Button icon="pi pi-filter" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.filter')" @click="filter($event)" />
                    </div>
                </template>
            </Card>
        </div>
    </div>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { AxiosResponse } from 'axios'
    import KnFabButton from '@/components/UI/KnFabButton.vue'
    import DataPreparationDescriptor from './DataPreparationDescriptor.json'

    export default defineComponent({
        name: 'data-preparation',
        components: { KnFabButton },
        props: {
            visibility: Boolean
        },
        data() {
            return { descriptor: DataPreparationDescriptor, dataPreparation: Array<any>() }
        },

        emits: ['update:visibility'],
        created() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '3.0/datasets/mydata/').then((response: AxiosResponse<any>) => (this.dataPreparation = response.data.root))
        },
        methods: {
            search(e, item) {
                console.log(e)
                this.$router.push({ name: 'data-preparation-detail', params: { id: item.label } })
            },
            filter(e) {
                console.log(e)
            }
        }
    })
</script>

<style lang="scss" scoped>
    .image {
        position: relative;
    }
    .imageH2 {
        position: absolute;
        top: 10px;
        left: 0;
        width: 100%;
    }
</style>
