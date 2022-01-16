package nick.template.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import nick.template.R
import nick.template.databinding.MainFragmentBinding
import nick.template.ui.extensions.clicks

class MainFragment @Inject constructor(
    private val factory: MainViewModel.Factory
) : Fragment(R.layout.main_fragment) {
    private val viewModel: MainViewModel by viewModels { factory.create(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = MainFragmentBinding.bind(view)

        val clicks = merge(
            binding.start.clicks().map { viewModel.start() },
            binding.stop.clicks().map { viewModel.stop() }
        )

        val results = viewModel.results()
            .onEach { messages -> binding.output.text = messages }

        merge(clicks, results).launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
