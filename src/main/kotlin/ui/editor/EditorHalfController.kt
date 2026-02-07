package non.era.aisdme.ui.editor

interface EditorHalfController {
    fun getTabs() : List<ObservableOpenedFile>
    fun getActiveTabId(): Int?
    fun getActiveTab(): ObservableOpenedFile?
    fun activateTab(tabId: Int)
    fun updateText(newText: String)
    fun closeTab(id: Int)
}