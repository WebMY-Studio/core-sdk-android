package us.webmy.core.sharing


interface SharingManager {
    fun shareContent(sharing: ContentSharing)
    fun shareText(text: String)
    fun shareEvent(sharing: EventSharing)
}
