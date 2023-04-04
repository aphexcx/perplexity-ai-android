package cx.aphex.perplexity.api

data class OpenAIResponse(val choices: List<Choice>) {
    data class Choice(val text: String)
}