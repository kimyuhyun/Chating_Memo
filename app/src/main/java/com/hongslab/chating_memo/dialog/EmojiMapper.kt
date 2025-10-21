package com.hongslab.chating_memo.dialog

object EmojiMapper {
    private val emojiToCode = mapOf(
        "❤️" to ":red_heart:",
        "\uD83D\uDC9D" to ":gift_heart:",
        "\uD83D\uDE04" to ":smile:",
        "\uD83D\uDE00" to ":grinning:",
        "\uD83D\uDE0D" to ":heart_eyes:",
        "\uD83D\uDC4C" to ":ok_hand:",
        "\uD83D\uDC4F" to ":clap:",
        "\uD83D\uDE31" to ":scream:",
        "\uD83E\uDD71" to ":yawning_face:",
        "\uD83D\uDC4D" to ":thumbs_up:",
        "\uD83D\uDC4E" to ":thumbs_down:",
        "\uD83E\uDD26" to ":facepalm:",
        "\uD83D\uDCAF" to ":hundred:",
        "\uD83D\uDE02" to ":joy:",
        "\uD83E\uDD70" to ":smiling_face_with_hearts:",
        "\uD83D\uDE18" to ":kissing_heart:",
        "\uD83E\uDD14" to ":thinking:",
        "\uD83D\uDE0E" to ":sunglasses:",
        "\uD83D\uDE2D" to ":sob:",
        "\uD83D\uDE44" to ":rolling_eyes:",
        "\uD83E\uDD23" to ":rofl:",
        "\uD83D\uDC95" to ":two_hearts:",
        "\uD83D\uDE0A" to ":blush:",
        "\uD83D\uDE34" to ":sleeping:",
        "🙏" to ":pray:",
        "\uD83E\uDD17" to ":hugs:",
        "\uD83D\uDE33" to ":flushed:",
        "\uD83D\uDE1C" to ":wink_tongue:",
        "\uD83D\uDC4B" to ":wave:",
        "✨" to ":sparkles:",
        "\uD83E\uDD79" to ":pleading_face:",
        "\uD83D\uDE0B" to ":yum:",
        "\uD83E\uDD7A" to ":pleading:",
        "\uD83D\uDE1D" to ":stuck_out_tongue_winking:",
        "\uD83D\uDE1E" to ":disappointed:",
        "\uD83D\uDE2E" to ":open_mouth:",
        "\uD83D\uDC96" to ":sparkling_heart:",
        "\uD83D\uDC97" to ":growing_heart:",
        "\uD83D\uDC93" to ":beating_heart:",
        "\uD83D\uDC9E" to ":revolving_hearts:",
        "\uD83D\uDC9A" to ":green_heart:",
        "\uD83D\uDC99" to ":blue_heart:",
        "\uD83D\uDC9C" to ":purple_heart:",
        "\uD83E\uDD0D" to ":white_heart:",
        "\uD83E\uDD0E" to ":brown_heart:",
        "\uD83D\uDDA4" to ":black_heart:",
        "\uD83E\uDD42" to ":clinking_glasses:",
        "\uD83C\uDF89" to ":party:",
        "\uD83C\uDF8A" to ":confetti:",
        "\uD83D\uDE48" to ":see_no_evil:",
        "\uD83D\uDC98" to ":cupid:",
        "\uD83D\uDE0F" to ":smirk:",
        "\uD83D\uDE01" to ":grin:",
        "\uD83D\uDE2C" to ":grimacing:",
        "\uD83E\uDD2D" to ":hand_over_mouth:",
        "\uD83E\uDD2B" to ":shushing:",
        "\uD83D\uDE12" to ":unamused:",
        "\uD83D\uDE29" to ":weary:",
        "\uD83D\uDE35" to ":dizzy_face:",
        "\uD83E\uDD2F" to ":exploding_head:",
        "⭐" to ":star:",
        "\uD83D\uDC8E" to ":gem:",
        "\uD83C\uDF1F" to ":glowing_star:",
        "\uD83D\uDC8B" to ":kiss:",
        "\uD83D\uDC40" to ":eyes:"
    )

    private val codeToEmoji = emojiToCode.entries.associate { (k, v) -> v to k }

    fun encodeEmojis(text: String): String {
        var result = text
        emojiToCode.forEach { (emoji, code) ->
            result = result.replace(emoji, code)
        }
        return result
    }

    fun decodeEmojis(text: String): String {
        var result = text
        codeToEmoji.forEach { (code, emoji) ->
            result = result.replace(code, emoji)
        }
        return result
    }
}
