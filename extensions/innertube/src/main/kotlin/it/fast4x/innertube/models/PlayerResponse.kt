package it.fast4x.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class PlayerResponse(
    val playabilityStatus: PlayabilityStatus?,
    val playerConfig: PlayerConfig?,
    val streamingData: StreamingData?,
    val videoDetails: VideoDetails?,
) {
    @Serializable
    data class PlayabilityStatus(
        val status: String?,
        val reason: String?
    )

    @Serializable
    data class PlayerConfig(
        val audioConfig: AudioConfig?
    ) {
        @Serializable
        data class AudioConfig(
            private val loudnessDb: Double?
        ) {
            // For music clients only
            val normalizedLoudnessDb: Float?
                get() = loudnessDb?.plus(7)?.toFloat()
        }
    }

    @Serializable
    data class StreamingData(
        val adaptiveFormats: List<AdaptiveFormat>?,
        val expiresInSeconds: Long?
    ) {

        val autoMaxQualityFormat: AdaptiveFormat?
            get() = adaptiveFormats?.findLast {
                it.itag == 251 || it.itag == 141 ||
                        it.itag == 250 || it.itag == 140 ||
                        it.itag == 249 || it.itag == 139 || it.itag == 171
            }

        val highestQualityFormat: AdaptiveFormat?
            get() = adaptiveFormats?.findLast { it.itag == 251 || it.itag == 141 }

        val mediumQualityFormat: AdaptiveFormat?
            get() = adaptiveFormats?.findLast { it.itag == 250 || it.itag == 140 }

        val lowestQualityFormat: AdaptiveFormat?
            get() = adaptiveFormats?.findLast { it.itag == 249 || it.itag == 139 }



        @Serializable
        data class AdaptiveFormat(
            val itag: Int,
            val mimeType: String,
            val bitrate: Long?,
            val averageBitrate: Long?,
            val contentLength: Long?,
            val audioQuality: String?,
            val approxDurationMs: Long?,
            val lastModified: Long?,
            val loudnessDb: Double?,
            val audioSampleRate: Int?,
            val url: String?,
            val width: Int?
        ) {
            val isAudio: Boolean
                get() = width == null

            val isVideo: Boolean
                get() = width != null
        }
    }

    @Serializable
    data class VideoDetails(
        val videoId: String?
    )
}
