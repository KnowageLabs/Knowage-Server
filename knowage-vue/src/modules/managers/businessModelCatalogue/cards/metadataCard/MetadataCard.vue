<template>
    <Card class="p-m-2">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('managers.businessModelManager.metadata') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div class="p-m-5">
                <Button id="metadata-button" class="p-jc-center" @click="confirmImport">{{ $t('managers.businessModelManager.importMetadata') }}</Button>
                <div v-if="importing" class="p-mt-5">
                    <div id="import-metadata-spinner">
                        <ProgressSpinner />
                    </div>
                    <ProgressBar mode="indeterminate" class="kn-progress-bar" />
                    <div id="import-progress-message" class="p-d-flex p-jc-center p-ai-center">{{ $t('managers.businessModelManager.importProgress') }}</div>
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import ProgressSpinner from 'primevue/progressspinner'
import mainStore from '../../../../../App.store'

export default defineComponent({
    name: 'metadata-card',
    components: {
        Card,
        ProgressSpinner
    },
    props: {
        id: {
            type: Number,
            required: false
        }
    },
    data() {
        return {
            importing: false
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    methods: {
        confirmImport() {
            this.$confirm.require({
                message: this.$t('managers.businessModelManager.importConfirm'),
                header: this.$t('managers.businessModelManager.importMetadata'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.importMetadata()
            })
        },
        async importMetadata() {
            this.importing = true
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/metadata/${this.id}/bmExtract/`)
                .then(() =>
                    this.store.setInfo({
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.toast.success')
                    })
                )
                .catch((error) =>
                    this.store.setError({
                        title: this.$t('common.error.generic'),
                        msg: error.message
                    })
                )
                .finally(() => (this.importing = false))
        }
    }
})
</script>

<style lang="scss" scoped>
#metadata-button {
    min-width: 15rem;
    text-transform: uppercase;
}

#import-progress-message {
    background-color: rgba(0, 0, 0, $alpha: 0.3);
    height: 3rem;
}

#import-metadata-spinner {
    position: fixed;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    opacity: 0.7;
    background-color: rgba(0, 0, 0, $alpha: 0.5);
    z-index: 99;
}

::v-deep(.p-progress-spinner-circle) {
    animation: p-progress-spinner-color 4s ease-in-out infinite;
}
@keyframes p-progress-spinner-color {
    100%,
    0% {
        stroke: #43749e;
    }
    80%,
    90% {
        stroke: #d62d20;
    }
}
</style>
