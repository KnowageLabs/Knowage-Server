<template>
    <Codemirror v-model:value="code" :options="cmOptions" border placeholder="测试 placeholder" :height="200" @change="onChange" />
</template>

<script>
import Codemirror from 'codemirror-editor-vue3'

// language
import 'codemirror/mode/javascript/javascript.js'

import { ref } from 'vue'
export default {
    components: { Codemirror },
    setup() {
        const code = ref(`
var i = 0;
for (; i < 9; i++) {
  console.log(i);
  // more statements
}`)

        return {
            code,
            cmOptions: {
                mode: 'text/javascript', // 语言模式
                theme: 'default', // 主题
                lineNumbers: true, // 显示行号
                smartIndent: true, // 智能缩进
                indentUnit: 2, // 智能缩进单位为4个空格长度
                foldGutter: true, // 启用行槽中的代码折叠
                styleActiveLine: true // 显示选中行的样式
            },
            onChange(val, cm) {}
        }
    }
}
</script>
