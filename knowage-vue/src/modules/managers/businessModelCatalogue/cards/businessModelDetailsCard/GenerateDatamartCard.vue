<template>
    <Dialog :style="generateDatamartCardDescriptor.dialog.style" :visible="true" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :header="$t('managers.buisnessModelCatalogue.generateDatamart')" :closable="false" aria-label="Generate Datamart Options">
        <p>{{ $t('managers.buisnessModelCatalogue.generateDatamartHint') }}</p>
        <div class="p-m-5">
            <InputSwitch id="advanced-options" class="p-mr-2" v-model="showAdvancedOptions" />
            <label for="advanced-options" class="kn-material-input-label">{{ $t('managers.buisnessModelCatalogue.showAdvancedOptions') }}</label>
        </div>

        <form class="p-fluid p-m-2" v-if="showAdvancedOptions">
            <div class="p-field">
                <span class="p-float-label">
                    <InputText id="modelName" class="kn-material-input" type="text" v-model.trim="modelName" />
                    <label for="modelName" class="kn-material-input-label"> {{ $t('managers.buisnessModelCatalogue.modelName') }}</label>
                </span>
            </div>
            <div class="p-field">
                <span class="p-float-label">
                    <InputText id="schemaName" class="kn-material-input" type="text" v-model.trim="schemaName" />
                    <label for="schemaName" class="kn-material-input-label"> {{ $t('managers.buisnessModelCatalogue.schemaName') }}</label>
                </span>
            </div>
            <div class="p-field">
                <span class="p-float-label">
                    <InputText id="catalogName" class="kn-material-input" type="text" v-model.trim="catalogName" />
                    <label for="catalogName" class="kn-material-input-label"> {{ $t('managers.buisnessModelCatalogue.catalogName') }}</label>
                </span>
            </div>
            <div class="p-d-flex p-mt-4">
                <span class="p-field-checkbox p-mr-3">
                    <Checkbox v-model="isGeneratedForRegistry" :binary="true" aria-label="Generate for Registry" />
                    <label>{{ $t('managers.buisnessModelCatalogue.forRegistry') }}</label>
                </span>
                <span class="p-field-checkbox">
                    <Checkbox v-model="includeSources" :binary="true" aria-label="Include Sources" />
                    <label>{{ $t('managers.buisnessModelCatalogue.includeSources') }}</label>
                </span>
            </div>
        </form>

        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="closeTemplate"></Button>
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" @click="generateDatamart"></Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import Checkbox from 'primevue/checkbox'
import Dialog from 'primevue/dialog'
import InputSwitch from 'primevue/inputswitch'
import generateDatamartCardDescriptor from './GenerateDatamartCardDescriptor.json'

export default defineComponent({
    name: 'generate-datamart-card',
    components: {
        Checkbox,
        Dialog,
        InputSwitch
    },
    props: {
        businessModel: {
            type: Object,
            required: true
        },
        user: {
            type: Object,
            required: true
        }
    },
    emits: ['generated', 'close'],
    watch: {
        businessModel() {
            this.loadBusinessModel()
        }
    },
    async created() {
        this.loadBusinessModel()
        this.loadUser()
        await this.loadModelInfo()
    },
    data() {
        return {
            generateDatamartCardDescriptor,
            showAdvancedOptions: false,
            model: {},
            currentUser: {},
            modelName: '',
            schemaName: '',
            catalogName: '',
            isGeneratedForRegistry: false,
            includeSources: false
        }
    },
    methods: {
        loadBusinessModel() {
            this.model = this.businessModel
            this.modelName = this.businessModel.name
        },
        loadUser() {
            this.currentUser = this.user
        },
        async loadModelInfo() {
            await axios.get(`/knowagemeta/restful-services/1.0/metaWeb/modelInfos/${this.businessModel.id}?user_id=${this.user.userUniqueIdentifier}`).then((response) => {
                if (response.data.schemaName) {
                    this.schemaName = response.data.schemaName
                }
                if (response.data.catalogName) {
                    this.catalogName = response.data.catalogName
                }
            })
        },
        generateDatamart() {
            const url =
                `/knowagemeta/restful-services/1.0/metaWeb/buildModel/${this.businessModel.id}?user_id=${this.user.userId}` +
                `&model=${encodeURIComponent(this.modelName)}&schema=${this.schemaName}&catalog=${this.catalogName}&registry=${this.isGeneratedForRegistry}&includeSources=${this.includeSources}`
            axios.get(url).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.createTitle'),
                    msg: this.$t('common.toast.success')
                })
                this.$emit('generated')
            })
        },
        closeTemplate() {
            this.$emit('close')
        }
    }
})
</script>
