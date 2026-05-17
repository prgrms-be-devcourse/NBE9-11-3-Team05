package com.team05.petmeeting.domain.ads.dto

data class GeminiRequest(
    @JvmField val contents: List<Content>
) {
    fun contents(): List<Content> = contents

    data class Content(
        @JvmField val parts: List<Part>
    ) {
        fun parts(): List<Part> = parts
    }

    data class Part(
        @JvmField val text: String?
    ) {
        fun text(): String? = text
    }

    companion object {
        @JvmStatic
        fun from(prompt: String?): GeminiRequest {
            return GeminiRequest(listOf(Content(listOf(Part(prompt)))))
        }
    }
}
