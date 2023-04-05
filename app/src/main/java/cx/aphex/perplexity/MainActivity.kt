package cx.aphex.perplexity

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aallam.openai.api.BetaOpenAI
import cx.aphex.perplexity.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

@BetaOpenAI
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val answerChunksAdapter = AnswerChunksAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the RecyclerView and Adapter
        binding.answerChunksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.answerChunksRecyclerView.adapter = answerChunksAdapter

        binding.searchQuery.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearch()
                true
            } else {
                false
            }
        }

        // Collect answer chunks and update the UI
        lifecycleScope.launch {
            viewModel.answerChunks.collect { chunks ->
                answerChunksAdapter.setChunks(chunks)

                binding.answerChunksRecyclerView.scrollToPosition(answerChunksAdapter.itemCount - 1)
            }
        }

        // Show or hide the typing indicator based on the fetching state
        lifecycleScope.launch {
            viewModel.isFetchingAnswer.collect { isFetching ->
                binding.typingIndicator.visibility =
                    if (isFetching) View.VISIBLE else View.INVISIBLE
            }
        }
    }

    private fun performSearch() {
        val query = binding.searchQuery.text.toString()
        if (query.isNotBlank()) {
            viewModel.search(query)
        }
    }
}