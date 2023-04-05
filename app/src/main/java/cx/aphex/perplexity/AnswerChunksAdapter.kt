package cx.aphex.perplexity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cx.aphex.perplexity.databinding.AnswerChunkItemBinding

class AnswerChunksAdapter(private val answerChunks: MutableList<String>) :
    RecyclerView.Adapter<AnswerChunksAdapter.AnswerChunkViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerChunkViewHolder {
        val binding =
            AnswerChunkItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnswerChunkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnswerChunkViewHolder, position: Int) {
        holder.bind(answerChunks[position])
    }

    fun setChunks(newAnswerChunks: List<String>) {
        answerChunks.clear()
        answerChunks.addAll(newAnswerChunks)
//        val startPosition = answerChunks.size
//        answerChunks.addAll(newAnswerChunks.subList(startPosition, newAnswerChunks.size))
//        notifyItemRangeInserted(startPosition, newAnswerChunks.size - startPosition)
        notifyItemRangeChanged(0, newAnswerChunks.size)
    }

    override fun getItemCount() = answerChunks.size

    inner class AnswerChunkViewHolder(private val binding: AnswerChunkItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(answerChunk: String) {
            binding.answerChunkText.text = answerChunk
        }
    }
}
