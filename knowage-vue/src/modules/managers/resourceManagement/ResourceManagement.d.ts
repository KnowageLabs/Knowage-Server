export interface iFolderTemplate {
	root?: iFolderTemplate
	key: String
	label: String
	icon?: String
	level: Number
	modelFolder: boolean
	relativePath: String
	children?: Array<iFolderTemplate>
}

export interface IFileTemplate {
	name: String
	size: String
	lastModified: Number
}
