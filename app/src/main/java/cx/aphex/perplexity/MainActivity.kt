package cx.aphex.perplexity

import BlinkingCaretSpan
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.aallam.openai.api.BetaOpenAI
import cx.aphex.perplexity.Animations.createCaretAnimator
import cx.aphex.perplexity.databinding.ActivityMainBinding
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch

@BetaOpenAI
class MainActivity : AppCompatActivity() {

    private lateinit var pulsingAnimation: AnimatorSet
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val markwon: Markwon by lazy { Markwon.create(this) }

    lateinit var blinkingCaretAnimator: ObjectAnimator

    private val answerChunks = SpannableStringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.searchQuery.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearch()
                true
            } else {
                false
            }
        }

        pulsingAnimation = Animations.createPulsingAnimation(binding.logo)

        val blinkingCaretSpan = BlinkingCaretSpan(ContextCompat.getColor(this, R.color.white))

        // Collect answer chunks and update the UI
        lifecycleScope.launch {
            viewModel.answerChunks.collect { newAnswerChunks ->
                if (newAnswerChunks.isNotEmpty()) {
                    // Remove the previous caret if it exists
                    val previousCaretPosition = answerChunks.length - 1
                    if (previousCaretPosition >= 0) {
                        answerChunks.removeSpan(blinkingCaretSpan)
                        answerChunks.delete(previousCaretPosition, previousCaretPosition + 1)
                    }

                    newAnswerChunks.forEach { chunk ->
                        answerChunks.append(chunk)
                    }

                    // Append the blinking caret and apply the BlinkingCaretSpan
                    val caretPosition = answerChunks.length
                    answerChunks.append("\u2588") // Unicode character for a block caret
                    answerChunks.setSpan(
                        blinkingCaretSpan,
                        caretPosition,
                        caretPosition + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    blinkingCaretAnimator = createCaretAnimator(blinkingCaretSpan)

                    // Update the Markdown view
                    markwon.setMarkdown(binding.markdownView, answerChunks.toString())

                }
            }
        }

        lifecycleScope.launch {
            viewModel.isFetchingAnswer.collect { isFetching ->
                if (isFetching) {
                    pulsingAnimation.start()
                } else {
                    pulsingAnimation.cancel()
                    binding.logo.scaleX = 1f
                    binding.logo.scaleY = 1f
                }
            }
        }
    }

    private fun performSearch() {
        val query = binding.searchQuery.text.toString()

        answerChunks.clear()
        if (query.isNotBlank()) {
            viewModel.search(query)
        }
    }
}