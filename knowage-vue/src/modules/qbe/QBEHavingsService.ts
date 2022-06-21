import { iFilter, iQBE, iQuery, iField } from './QBE'

export function onHavingsSaveCallback(havings: iFilter[], qbe: iQBE | null, selectedQuery: iQuery, field: iField) {
    if (!qbe) return

    for (let i = 0; i < havings.length; i++) {
        const tempFilter = havings[i]
        const index = selectedQuery.havings.findIndex((el: iFilter) => el.filterId === tempFilter.filterId)
        if (index !== -1) {
            selectedQuery.havings[index] = tempFilter
        } else {
            selectedQuery.havings.push(tempFilter)
        }
    }

    removeDeletedHavings(havings, qbe, selectedQuery, field)
}

export function removeDeletedHavings(havings: iFilter[], qbe: iQBE, selectedQuery: iQuery, field: iField) {
    if (!qbe) return

    for (let i = selectedQuery.havings.length - 1; i >= 0; i--) {
        const tempHaving = selectedQuery.havings[i]
        if (tempHaving.leftOperandValue === field.id) {
            const index = havings.findIndex((el: iFilter) => el.filterId === tempHaving.filterId)
            if (index === -1) selectedQuery.havings.splice(i, 1)
        }
    }
}