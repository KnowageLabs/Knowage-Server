<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="showCategoryDialog" />
            <Button class="p-button-text p-button-rounded p-button-plain" icon="pi pi-times" @click="closeTemplate" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="p-grid p-m-0 p-fluid p-jc-center" style="overflow:auto">
        <Card style="width:100%" class="p-m-2">
            <template #content>
                <form class="p-fluid p-formgrid p-grid">
                    <div class="p-field p-col-6 p-mb-3">
                        <span class="p-float-label">
                            <InputText id="name" class="kn-material-input" type="text" v-model.trim="simpleNavigation.name" maxLength="40" />
                            <label for="name" class="kn-material-input-label">{{ $t('common.name') }} * </label>
                        </span>
                        <!-- <KnValidationMessages class="p-mt-1" :vComp="v$.word.WORD" :additionalTranslateParams="{ fieldName: $t('managers.glossary.common.word') }"></KnValidationMessages> -->
                    </div>
                    <div class="p-field p-col-6 p-mb-3">
                        <span class="p-float-label">
                            <Dropdown id="type" class="kn-material-input" v-model="simpleNavigation.type" :options="crossModes" optionValue="value" optionLabel="name" />
                            <label for="type" class="kn-material-input-label"> {{ $t('managers.crossNavigationManagement.modality') }} </label>
                        </span>
                    </div>
                    <div class="p-field p-col-6 p-mb-3">
                        <span class="p-float-label">
                            <InputText id="description" class="kn-material-input" type="text" v-model.trim="simpleNavigation.description" maxLength="200" />
                            <label for="description" class="kn-material-input-label">{{ $t('common.description') }} </label>
                        </span>
                    </div>
                    <div class="p-field p-col-6 p-mb-3">
                        <span class="p-float-label">
                            <InputText id="name" class="kn-material-input" type="text" v-model.trim="simpleNavigation.breadcrumb" maxLength="200" />
                            <label for="name" class="kn-material-input-label">{{ $t('managers.crossNavigationManagement.breadCrumbs') }} </label>
                        </span>
                    </div>
                </form>
                <p>{{ navigation }}</p>
            </template>
        </Card>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import Dropdown from 'primevue/dropdown'
export default defineComponent({
    name: 'cross-navigation-detail',
    components: { Dropdown },
    props: {
        id: {
            type: String
        }
    },
    data() {
        return {
            navigation: {} as any,
            simpleNavigation: {} as any,
            loading: false,
            crossModes: [
                { name: this.$t('managers.crossNavigationManagement.normal'), value: 0 },
                { name: this.$t('managers.crossNavigationManagement.popUp'), value: 1 },
                { name: this.$t('managers.crossNavigationManagement.popUpWindow'), value: 2 }
            ]
        }
    },
    created() {
        if (this.id) {
            this.loadNavigation()
        }
    },
    watch: {
        async id() {
            if (this.id) {
                await this.loadNavigation()
            } else {
                this.navigation = {}
                this.simpleNavigation = {}
            }
        }
    },
    methods: {
        closeTemplate() {
            this.$emit('close')
        },
        setDirty(): void {
            this.$emit('touched')
        },
        async loadNavigation() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/crossNavigation/' + this.id + '/load/')
                .then((response) => {
                    this.navigation = response.data
                    this.simpleNavigation = response.data.simpleNavigation
                })
                .finally(() => (this.loading = false))
        }
    }
})
</script>
