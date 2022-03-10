<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="p-col-4 p-sm-4 p-md-3 p-p-0 kn-page">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.businessModelManager.title') }}
                    </template>
                    <template #end>
                        <FabButton icon="fas fa-plus" @click="showForm" data-test="new-button" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <KnListBox :options="businessModelList" :settings="businessModelCatalogueDescriptor.knListSettings" @click="showForm" @delete.stop="deleteBusinessModelConfirm" />

                <!--Listbox
                    v-if="!loading"
                    class="kn-list--column kn-page-content"
                    :options="businessModelList"
                    listStyle="max-height:calc(100% - 62px)"
                    :filter="true"
                    :filterPlaceholder="$t('common.search')"
                    optionLabel="name"
                    filterMatchMode="contains"
                    :filterFields="businessModelCatalogueDescriptor.filterFields"
                    :emptyFilterMessage="$t('common.info.noDataFound')"
                    @change="showForm"
                    data-test="bm-list"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" data-test="list-item">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.name }}</span>
                                <span class="kn-list-item-text-secondary kn-truncated">{{ slotProps.option.description }}</span>
                            </div>
                            <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteBusinessModelConfirm(slotProps.option.id)" data-test="delete-button" />
                        </div>
                    </template>
                </!--Listbox-->
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-page">
                <KnHint :title="'managers.businessModelManager.title'" :hint="'managers.businessModelManager.hint'" v-if="showHint" data-test="bm-hint"></KnHint>
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

    export default defineComponent({
        name: 'business-model-catalogue',
        components: {
            FabButton,
            KnListBox,
            KnHint
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
                    .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/businessmodels')
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
                await this.$http.delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/businessmodels/' + businessModelId).then(() => {
                    this.$store.commit('setInfo', {
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
