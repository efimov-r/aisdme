package non.era.aisdme.i18n

interface Strings { //все тексты интерфейса; новый язык — новая реализация
    //общие
    val ok: String
    val cancel: String
    val close: String
    val closeAll: String
    val error: String
    val unknownError: String
    val untitled: String

    //настройки
    val settings: String
    val theme: String
    val themeLight: String
    val themeDark: String
    val language: String

    //дерево проекта
    val locateOpenedFile: String
    val expandSelected: String
    val collapseAll: String
    val newDirectory: String
    val newFile: String
    val paste: String
    val copy: String
    val cut: String
    val delete: String
    val rename: String
    val openInLeft: String
    val openInRight: String
    fun showInFileManager(name: String): String
    val reloadFromDisk: String

    //вкладки
    val tabClose: String
    val tabCloseOthers: String
    val tabCloseAll: String
    val tabCloseLeft: String
    val tabCloseRight: String
    val tabSplitOther: String
    val tabSplitMoveOther: String
    val tabShowInTree: String
    val tabNewFromClipboard: String
    val tabFromClipboard: String

    //диалоги
    val dialogMove: String
    val dialogCreateFile: String
    val dialogCreateDirectory: String
    val placeholderFileName: String
    val placeholderDirectoryName: String
    fun dialogDelete(count: Int): String

    //проверки ввода
    val nameEmpty: String
    val nameHasSlash: String
    val nameInvalidChars: String
    val nameBadEnd: String
    val nameReserved: String
    val nameTooLong: String
    val notFound: String
    val notDirectory: String
    val cannotMoveIntoItself: String
    val cannotPasteIntoItself: String
    val alreadyExists: String
    fun alreadyExists(name: String): String
    val invalidName: String

    //плашка внешних изменений
    val fileChanged: String
    val fileDeleted: String
    val loadFromDisk: String
    val keepMine: String
    val saveAgain: String
    val closeTabButton: String

    //редактор
    val contentLoading: String
    val contentNotLoaded: String
    val errorTypeHalf: String
    val dropFilesHere: String

    //статус записи
    val statusLoad: String
    val statusNotSaved: String
    val statusWritable: String
    val statusReadOnly: String
    val statusLocked: String
    val statusError: String
    val statusChanged: String
    val statusDeleted: String

    //сообщения об ошибках
    val errDeleteNotSelected: String
    val errCopyNotSelected: String
    val errNotSameDir: String
    fun errDirNotFound(dir: Any): String
    val errMultiRename: String
    val errPasteNoDir: String
    val errPasteOneDir: String
    val errPasteOnlyDir: String
    val errCreateNoDir: String
    val errCreateOneDir: String
    val errCreateOnlyDir: String
    val errMoveNothing: String
}
