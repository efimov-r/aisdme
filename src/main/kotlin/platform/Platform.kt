package non.era.aisdme.platform

object Platform {
    val current: IPlatform =
        if (System.getProperty("os.name").lowercase().contains("win")) WindowsPlatform else LinuxPlatform
}