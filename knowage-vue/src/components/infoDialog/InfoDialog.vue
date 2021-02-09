<template>
   <Dialog v-bind:visible="visibility" footer="footer" header="About Knowage" :closable="false" modal>
      <div class="p-grid p-m-1">
         <div class="p-col">
            <div class="p-d-flex p-jc-center">
               <img :src="require('@/assets/logo_knowage.svg')" height="100"/>
            </div>
            <p><strong>{{ $tc('common.version') }}</strong>: 7.4.0-S</p>
            <p><strong>Utente Loggato:</strong> {{user.name || 'KTE Admin'}}</p>
            <p><strong>Tenant:</strong> kte</p>
            <p>Codice sorgente disponibile su www.knowage-suite.com</p>
            <p>© 2020 Engineering Ingegneria Informatica S.p.A.</p>
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
