<template>
    <div :class="getVisibility()">
        <ProgressSpinner :baseZIndex="1000" />
    </div>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { mapState } from 'vuex'
    import ProgressSpinner from 'primevue/progressspinner'

    export default defineComponent({
        name: 'kn-spinner-panel',
        components: { ProgressSpinner },
        props: { visibility: Boolean },
        computed: {
            ...mapState({
                loading: 'loading'
            })
        },
        methods: {
            getVisibility(): String {
                return this.loading == 0 ? 'kn-hide' : 'overlappingPanel'
            }
        }
    })
</script>

<style lang="scss"></style>
