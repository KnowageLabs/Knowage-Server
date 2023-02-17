<template>
    <Dialog :style="lovsManagementInfoDialogDescriptor.dialog.style" :header="infoTitle" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div v-if="lovType === lovItemEnum.SCRIPT">
            <p>
                The script must be written using Groovy or Javascript languages.
            </p>
            <p>
                The script must return an XML string containing a list of values with the syntax shown below. If the script instead returns a single value this is automatically enveloped in such XML. &lt;ROWS&gt;<br />
                &lt;ROW value="value1" ... /&gt;<br />
                &lt;ROW value="value2" ... /&gt;<br />
                ...<br />
                &lt;/ROWS&gt;
            </p>
            <p>Scripts can be parametrized with profile attributes, called with syntax ${name_attribute}; these can have a sinle or a multi value:</p>
            <p>A profile multi-value attribute is registered in authorization.xml file, and its value is written with syntax: <i>{splitter {value_1 splitter value_2 splitter ... value_n}};</i></p>
            <p>For groovy and javascript scripts some functions are added by system that let user to include a value in XML string and to treat multi-values attribute:</p>
            <p><b>returnValue(value_to_return)</b>: Returns the value with the following syntax</p>
            <hr />
            <hr />
            &lt;ROWS&gt;<br />
            &lt;ROW value="valueAttribute1" /&gt;<br />
            &lt;/ROWS&gt;
            <p>
                <b>getListFromMultiValueProfileAttribute(String nameAttribute)</b>: Returns list of values of a multi value profile attribute with following syntax (parameter must be enclosed between "") &lt;ROWS&gt;<br />
                &lt;ROW value="value1" ... /&gt;<br />
                &lt;ROW value="value2" ... /&gt;<br />
                ...<br />
                &lt;/ROWS&gt;
            </p>
            <p><b>Example:</b> <i>getListFromMultiValueProfileAttribute("${multi_value_attribute}")</i></p>
            <p>
                <b>getMultiValueProfileAttribute(String nameAttribute, String prefix, String newSplitter, String suffix)</b> : Return list f values of multi value attribute, preceded by the prefix, separated by the new splitter and followed by suffix (parameter must be enclosed between "")<br />
                <b>Example:</b> select ... where <i> column ${multi_value_attribute( "in (" ; "," ; ") ") })</i>
            </p>
        </div>
        <div v-else-if="lovType === lovItemEnum.QUERY">
            <p>
                Column names with dots or aggregator functions are not allowed, if you need them use alias (as).<br />
                <b>Example:</b> select sum(column) as name_colum, select column.column as name_column
            </p>
            <p>
                To use a single value profile attribute into the query use <b>${name_profile_attribute}</b><br />
                <b>Example:</b> select ... where column = '${name_profile_attribute}').
            </p>
            <p>
                To use a multi value profile attribute use <b>${name_profile_attribute(prefix;splitter;suffix)}</b> The profile attribute will be replaced by prefix+(list of values separated by the splitter)+suffix.<br />
                <b>Example:</b> select ... where column ${name_profile_attribute( in (';',';') )})
            </p>
        </div>
        <div v-else-if="lovType === lovItemEnum.FIX_LOV">
            <p>To insert a single value profile attribute into a fix lov value use <b>${name_profile_attribute}</b>.</p>

            To insert a multi value profile attribute into a fix lov value use <b>${name_profile_attribute(prefix;splitter;suffix)}</b>.<br />
            The profile attribute will be replaced by prefix+(list of values separated by the splitter)+suffix<br />
            <b>Example:</b> ${name_profile_attribute( in (';',';') )}
        </div>
        <div v-else-if="lovType === lovItemEnum.JAVA_CLASS">
            <p>
                The java class must implement the interface it.eng.spagobi.bo.javaClassLovs.IJavaClassLov. This interface has two methods: getValues(IEngUserProfile profile): the method returns an xml string containing the list of values. The xml string must be formatted using the xml structure
                reported below getNamesOfProfileAttributeRequired(): the method returns a java list containing the names of profile attributes used by the class. If the class doesn't use profile attributes simply returns an empty list.
            </p>
            <p><b>Xml structure of the returned string</b></p>
            <hr />
            <hr />
            <p>
                &lt;ROWS&gt;<br />
                &lt;ROW nameAttribute1="valueAttribute1" nameAttribute2="valueAttribute2" ... /&gt;<br />
                &lt;ROW nameAttribute1="valueAttribute1" nameAttribute2="valueAttribute2" ... /&gt;<br />
                ...<br />
                All the rows must have the same attributes !<br />
                ...<br />
                &lt;/ROWS&gt;
            </p>
        </div>
        <template #footer>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { lovItemEnum } from '../../../LovsManagementDetail.vue'
import Dialog from 'primevue/dialog'
import lovsManagementInfoDialogDescriptor from './LovsManagementInfoDialogDescriptor.json'

export default defineComponent({
    name: 'lovs-management-info-dialog',
    components: { Dialog },
    props: {
        visible: { type: Boolean },
        infoTitle: { type: String },
        lovType: { type: Object }
    },
    emits: ['close'],
    data() {
        return {
            lovsManagementInfoDialogDescriptor,
            lovItemEnum
        }
    },
    created() {},
    methods: {}
})
</script>
