package nick.template.initializers

import javax.inject.Inject

class AppInitializer @Inject constructor(
    private val initializers: Set<@JvmSuppressWildcards Initializer>
) : Initializer {
    override fun initialize() {
        initializers.forEach(Initializer::initialize)
    }
}
