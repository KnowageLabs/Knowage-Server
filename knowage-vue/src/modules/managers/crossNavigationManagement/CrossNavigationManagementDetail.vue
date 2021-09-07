<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="showCategoryDialog" />
            <Button class="p-button-text p-button-rounded p-button-plain" icon="pi pi-times" @click="closeTemplate" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <p>{{ navigation }}</p>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
export default defineComponent({
    name: 'cross-navigation-detail',
    props: {
        id: {
            type: String
        }
    },
    data() {
        return {
            navigation: {} as any,
            loading: false
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
                .then((response) => (this.navigation = response.data))
                .finally(() => (this.loading = false))
        }
    }
})
</script>
