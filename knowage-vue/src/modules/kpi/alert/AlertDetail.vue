<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" />
            <Button icon="pi pi-times" class="kn-button p-button-text p-button-rounded" @click="closeTemplate" />
        </template>
    </Toolbar>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iAlert } from './Alert'
import axios from 'axios'
export default defineComponent({
    name: 'alert-details',
    components: {},
    props: {
        id: {
            type: String,
            required: false
        }
    },
    watch: {
        async id() {
            await this.checkId()
        }
    },
    created() {
        if (this.id) {
            this.loadAlert()
        }
    },
    data() {
        return {
            selectedAlert: {} as iAlert
        }
    },
    methods: {
        async loadAlert() {
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/alert/' + this.id + '/load')
                .then((response) => {
                    this.selectedAlert = response.data
                })
                .finally(() => console.log('selected', this.selectedAlert))
        },
        async checkId() {
            if (this.id) {
                await this.loadAlert()
            } else {
                this.selectedAlert = { id: null }
            }
        },
        closeTemplate() {
            this.$emit('close')
        }
    }
})
</script>
