export interface iMetadata {
    id: number | null;
    label: string;
    name: string;
    description: string;
    dataType: 'LONG_TEXT' | 'SHORT_TEXT' | 'FILE';
}
