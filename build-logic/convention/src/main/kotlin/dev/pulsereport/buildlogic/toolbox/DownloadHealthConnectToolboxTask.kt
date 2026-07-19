package dev.pulsereport.buildlogic.toolbox

import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RelativePath
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import javax.inject.Inject

/** Downloads and caches the Health Connect Toolbox APK. */
abstract class DownloadHealthConnectToolboxTask @Inject constructor(
    private val fileSystemOperations: FileSystemOperations,
    private val archiveOperations: ArchiveOperations,
) : DefaultTask() {

    @get:Input
    abstract val toolboxUrl: Property<String>

    @get:Input
    abstract val refresh: Property<Boolean>

    @get:OutputDirectory
    abstract val cacheDir: DirectoryProperty

    @TaskAction
    fun download() {
        val outputDir = cacheDir.get().asFile
        outputDir.mkdirs()

        val existingApk = outputDir.listFiles { file -> file.extension == "apk" }?.firstOrNull()
        if (existingApk != null && !refresh.get()) {
            logger.lifecycle("Health Connect Toolbox already cached at ${existingApk.path}, skipping download.")
            return
        }

        val zipFile = File(outputDir, "health-connect-toolbox.zip")
        try {
            downloadFollowingRedirects(toolboxUrl.get(), zipFile)
        } catch (e: Exception) {
            logger.warn("Failed to download Health Connect Toolbox, skipping: ${e.message}")
            return
        }

        fileSystemOperations.copy {
            from(archiveOperations.zipTree(zipFile)) {
                include("**/*.apk")
                includeEmptyDirs = false
                eachFile { relativePath = RelativePath(true, name) }
            }
            into(outputDir)
        }
        zipFile.delete()

        val apk = outputDir.listFiles { file -> file.extension == "apk" }?.firstOrNull()
        if (apk == null) {
            logger.warn("Health Connect Toolbox download completed but no APK was found in the archive.")
        } else {
            logger.lifecycle("Health Connect Toolbox cached at ${apk.path}")
        }
    }

    private fun downloadFollowingRedirects(url: String, target: File) {
        var currentUrl = url
        var redirects = 0
        while (true) {
            val connection = URI(currentUrl).toURL().openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 10_000
            connection.readTimeout = 30_000
            try {
                connection.connect()
                val code = connection.responseCode
                if (code in 300..399) {
                    val location = connection.getHeaderField("Location")
                        ?: error("Redirect from $currentUrl had no Location header")
                    redirects++
                    if (redirects > 5) error("Too many redirects downloading Health Connect Toolbox")
                    currentUrl = location
                    continue
                }
                if (code != HttpURLConnection.HTTP_OK) {
                    error("Unexpected response $code downloading Health Connect Toolbox from $currentUrl")
                }
                connection.inputStream.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                return
            } finally {
                connection.disconnect()
            }
        }
    }
}
