<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('kpi.kpiDefinition.title') }}
                    </template>
                    <template #right>
                        <FabButton icon="fas fa-plus" @click="showForm" data-test="open-form-button" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <Listbox
                    v-if="!loading"
                    class="kn-list--column"
                    :options="wordsList"
                    :filter="true"
                    :filterPlaceholder="$t('common.search')"
                    optionLabel="name"
                    filterMatchMode="contains"
                    :filterFields="name"
                    :emptyFilterMessage="$t('common.info.noDataFound')"
                    @change="showForm"
                    data-test="kpi-list"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" data-test="list-item">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.WORD }}</span>
                            </div>
                            <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteWordConfirm(slotProps.option.id, slotProps.option.version)" data-test="delete-button" />
                        </div>
                    </template>
                </Listbox>
            </div>

            <div class="kn-list--column p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <router-view @touched="touched = true" @closed="onFormClose" @showDialog="displayInfoDialog" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import FabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
export default defineComponent({
    name: 'tenant-management',
    components: {
        FabButton,
        Listbox
    },
    data() {
        return {
            loading: false,
            touched: false,
            hintVisible: true,
            cloneKpi: false,
            wordsList: [] as any,
            kpiToClone: {} as any,
            cloneKpiId: Number,
            cloneKpiVersion: Number
        }
    },
    async created() {
        await this.getwordsList()
    },
    methods: {
        async getwordsList() {
            this.loading = true
            return (
                axios
                    //backed paginacija, videti sta sa ovim, posto nije potrebna
                    .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/listWords?Page=1&ItemPerPage=12`)
                    .then((response) => {
                        this.wordsList = [...response.data.item]
                    })
                    .finally(() => (this.loading = false))
            )
        },

        deleteWordConfirm() {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteWord()
            })
        },

        //double check delete route
        async deleteWord() {
            await axios.delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + ``).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.$router.push('/kpi-definition')
                this.getwordsList()
            })
        },
        showForm(event: any) {
            const path = event.value ? `/glossary-definition/${event.value.id}` : '/glossary-definition/new-glossary'
            this.hintVisible = false
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
        pageReload() {
            this.touched = false
            this.hintVisible = true
        },
        onFormClose() {
            this.touched = false
            this.hintVisible = true
        }
    }
})
</script>
