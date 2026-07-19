package dev.pulsereport.feature.sources

class FakeAppLabelResolver(
    private val labelsByPackage: Map<String, String> = emptyMap(),
) : AppLabelResolver {
    override fun labelFor(packageName: String): String = labelsByPackage[packageName] ?: packageName
}
