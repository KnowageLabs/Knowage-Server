<template>
   <Dialog v-bind:visible="visibility" footer="footer" :header="$tc('infoDialog.aboutKnowage')" :closable="false" modal>
      <div class="p-grid p-m-1">
         <div class="p-col">
            <div class="p-d-flex p-jc-center">
               <img :src="require('@/assets/logo_knowage.svg')" height="100"/>
            </div>
            <p><strong>{{ $tc('common.version') }}:</strong> 7.4.0-S</p>
            <p><strong>{{ $tc('common.loggedUser') }}:</strong> {{user.fullName}}</p>
            <p><strong>{{ $tc('common.tenant') }}:</strong> kte</p>
            <p>{{ $tc('infoDialog.sourceCode') }}</p>
            <p>{{ $tc('infoDialog.copyright') }}</p>
         </div>
      </div>
      <template #footer>
        <Button v-t="'common.close'" autofocus @click="closeDialog"/>
    </template>
   </Dialog>
   
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import Dialog from 'primevue/dialog'
    import { mapState } from 'vuex'

    export default defineComponent({
        name: 'InfoDialog',
        components: {
            Dialog
        },
        props: {
            visibility: Boolean
        },
        emits: ['update:visibility'],
        methods: {
            closeDialog(){
                this.$emit('update:visibility', false)
            }
        },
        computed: {
            ...mapState({
                user: 'user'
                })
        }
    })

</script>

<style scoped lang="scss">
    p {
        text-transform: capitalize;
    }

</style>
