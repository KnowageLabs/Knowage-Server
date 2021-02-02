<template>
   <Dialog :visible="visibility" footer="footer" header="About Knowage" modal>
      <div class="p-grid p-m-1">
         <div class="p-col">
            <div class="p-d-flex p-jc-center">
               <img :src="require('@/assets/logo_knowage.svg')" height="100"/>
            </div>
            <p><strong>Versione:</strong> 7.4.0-S</p>
            <p><strong>Utente Loggato:</strong> kte ADMIN</p>
            <p><strong>Tenant:</strong> kte</p>
            <p>Codice sorgente disponibile su www.knowage-suite.com</p>
            <p>© 2020 Engineering Ingegneria Informatica S.p.A.</p>
         </div>
      </div>
      <template #footer>
        <Button label="Close" autofocus @click="closeDialog"/>
    </template>
   </Dialog>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import Dialog from 'primevue/dialog'

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
    }
    })

</script>

<style scoped lang="scss">

</style>
