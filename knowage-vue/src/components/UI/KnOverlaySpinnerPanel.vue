<template>
    <div :class="getVisibility()">
        <ProgressSpinner :base-z-index="9999" />
    </div>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { mapState } from 'pinia'
    import store from '../../App.store.js'
    import ProgressSpinner from 'primevue/progressspinner'

    export default defineComponent({
        name: 'kn-spinner-panel',
        components: { ProgressSpinner },
        props: { visibility: Boolean },
        computed: {
            ...mapState(store, {
                loading: 'loading'
            })
        },
        methods: {
            getVisibility(): string {
                return this.loading == 0 ? 'kn-hide' : 'overlappingPanel'
            }
        }
    })
</script>

<style lang="scss"></style>
