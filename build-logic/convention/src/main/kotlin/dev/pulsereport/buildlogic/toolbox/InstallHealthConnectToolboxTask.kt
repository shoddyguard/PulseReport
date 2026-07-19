package dev.pulsereport.buildlogic.toolbox

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

private const val TOOLBOX_PACKAGE = "androidx.health.connect.client.devtool"

/** Installs the cached Health Connect Toolbox APK onto a connected device. */
abstract class InstallHealthConnectToolboxTask @Inject constructor(
    private val execOperations: ExecOperations,
) : DefaultTask() {

    @get:Internal
    abstract val cacheDir: DirectoryProperty

    @get:Internal
    abstract val adb: RegularFileProperty

    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun install() {
        val apk = cacheDir.get().asFile.listFiles { file -> file.extension == "apk" }?.firstOrNull()
        if (apk == null) {
            logger.lifecycle("No cached Health Connect Toolbox APK found, skipping install.")
            return
        }

        val adbPath = adb.get().asFile.path

        val devicesOutput = runAdb(adbPath, "devices")
        val hasDevice = devicesOutput.lineSequence().any { it.contains("\tdevice") }
        if (!hasDevice) {
            logger.lifecycle("No connected device/emulator found, skipping Health Connect Toolbox install.")
            return
        }

        val alreadyInstalled = execOperations.exec {
            commandLine(adbPath, "shell", "pm", "path", TOOLBOX_PACKAGE)
            standardOutput = ByteArrayOutputStream()
            errorOutput = ByteArrayOutputStream()
            isIgnoreExitValue = true
        }.exitValue == 0
        if (alreadyInstalled) {
            logger.lifecycle("Health Connect Toolbox already installed, skipping.")
            return
        }

        val installOutput = ByteArrayOutputStream()
        val result = execOperations.exec {
            commandLine(adbPath, "install", "-r", apk.path)
            standardOutput = installOutput
            errorOutput = installOutput
            isIgnoreExitValue = true
        }
        if (result.exitValue != 0) {
            logger.warn("Failed to install Health Connect Toolbox: ${installOutput.toString().trim()}")
        } else {
            logger.lifecycle("Health Connect Toolbox installed.")
        }
    }

    private fun runAdb(adbPath: String, vararg args: String): String {
        val output = ByteArrayOutputStream()
        execOperations.exec {
            commandLine(listOf(adbPath) + args)
            standardOutput = output
            errorOutput = output
            isIgnoreExitValue = true
        }
        return output.toString()
    }
}
