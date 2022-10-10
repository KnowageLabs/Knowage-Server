interface IOption { label: string, value: string }


export const getTranslatedLabel = (value: string, options: IOption[], $t: any) => {
    const index = options.findIndex((option: IOption) => option.value === value)
    return index !== -1 ? $t(options[index].label) : ''

}