import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import dev.pulsereport.buildlogic.toolbox.DownloadHealthConnectToolboxTask
import dev.pulsereport.buildlogic.toolbox.InstallHealthConnectToolboxTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

private const val TOOLBOX_URL = "https://goo.gle/health-connect-toolbox"

/**
 * Downloads Google's Health Connect Toolbox APK and installs it on demand, since the
 * emulator is wiped between builds.
 *
 * Can be triggered explicitly via the "PulseReport_Toolbox"
 * and "PulseReport_Toolbox_Seed" run configs in the `.run` directory's XML files as a Before
 * Launch step. The plain "PulseReport" config skips it, so normal Run/Debug stays prod-like.
 */
class HealthConnectToolboxConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val cacheDir = rootProject.layout.projectDirectory.dir(".tooling/health-connect-toolbox")

            val download = tasks.register<DownloadHealthConnectToolboxTask>("downloadHealthConnectToolbox") {
                group = "pulsereport"
                description = "Downloads and caches the Health Connect Toolbox APK."
                toolboxUrl.set(TOOLBOX_URL)
                refresh.set(
                    providers.gradleProperty("pulsereport.refreshToolbox")
                        .map(String::toBoolean)
                        .orElse(false),
                )
                this.cacheDir.set(cacheDir)
            }

            val adbProvider = extensions.getByType<ApplicationAndroidComponentsExtension>().sdkComponents.adb

            tasks.register<InstallHealthConnectToolboxTask>("installHealthConnectToolbox") {
                group = "pulsereport"
                description = "Installs the cached Health Connect Toolbox APK onto a connected device."
                dependsOn(download)
                this.cacheDir.set(cacheDir)
                adb.set(adbProvider)
            }
        }
    }
}
