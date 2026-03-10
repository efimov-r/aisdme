package non.era.aisdme.ui.editor.halfviewmodel

interface ITabIsFiled { //признак что таб открыта из файла, нужно для кеширования открытых вкладок
    fun needSaveToCash(): Boolean
    fun getFilePath() : String
}