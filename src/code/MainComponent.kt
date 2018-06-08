package ru.endlesscode.miet.orioks.internal.di.component

import dagger.Subcomponent
import ru.endlesscode.miet.orioks.internal.di.MainScope
import ru.endlesscode.miet.orioks.internal.di.module.MainMenuNavigationModule
import ru.endlesscode.miet.orioks.presentation.auth.fragment.AuthFragment
import ru.endlesscode.miet.orioks.presentation.main.activity.MainActivity
import ru.endlesscode.miet.orioks.presentation.main.fragment.MainMenuFragment
import ru.endlesscode.miet.orioks.presentation.subjects.fragment.SubjectFragment
import ru.endlesscode.miet.orioks.presentation.subjects.fragment.SubjectsFragment


@MainScope
@Subcomponent(modules = [MainMenuNavigationModule::class])
interface MainComponent {

    fun inject(activity: MainActivity)
    fun inject(fragment: SubjectsFragment)
    fun inject(fragment: MainMenuFragment)
    fun inject(fragment: AuthFragment)
    fun inject(fragment: SubjectFragment)

    @Subcomponent.Builder
    interface Builder {
        fun build(): MainComponent
    }
}
