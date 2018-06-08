package ru.endlesscode.miet.orioks.internal.di.holder.common


abstract class ComponentHolder<out Component> {

    private var component: Component? = null
        get() {
            linksCount++
            return field
        }
        set(value) {
            linksCount = if (value == null) 0 else 1
            field = value
        }

    private var linksCount: Int = 0

    protected abstract fun provideInternal(): Component

    fun provideComponent(): Component {
        synchronized(this) {
            return component ?: provideInternal().also { component = it }
        }
    }

    fun onDependencyReleased() {
        synchronized(this) {
            if (linksCount == 1) {
                destroyComponent()
            } else {
                linksCount--
            }
        }
    }

    protected open fun onComponentDestroyed() {}

    private fun destroyComponent() {
        component = null
        onComponentDestroyed()
    }
}
