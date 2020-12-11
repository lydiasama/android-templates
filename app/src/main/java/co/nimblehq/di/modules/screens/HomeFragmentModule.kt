package co.nimblehq.di.modules.screens

import androidx.lifecycle.ViewModel
import co.nimblehq.ui.screens.home.HomeViewModel
import co.nimblehq.ui.screens.home.HomeViewModelImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.multibindings.IntoMap

@InstallIn(FragmentComponent::class)
@Module
interface HomeFragmentModule {

    @Binds
    fun homeViewModel(viewModel: HomeViewModelImpl): HomeViewModel
}
