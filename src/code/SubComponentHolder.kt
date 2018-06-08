package ru.endlesscode.miet.orioks.internal.di.holder.common


abstract class SubComponentHolder<out Component, in ParentComponent>(
    private val parentComponentHolder: ComponentHolder<ParentComponent>
) : ComponentHolder<Component>() {

    protected abstract fun provideInternal(parentComponent: ParentComponent): Component

    final override fun provideInternal(): Component {
        return provideInternal(parentComponentHolder.provideComponent())
    }

    override fun onComponentDestroyed() {
        parentComponentHolder.onDependencyReleased()
    }
}
