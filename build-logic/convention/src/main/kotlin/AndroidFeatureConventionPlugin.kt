import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("fitlogic.android.library")
            pluginManager.apply("fitlogic.android.compose")
        }
    }
}
