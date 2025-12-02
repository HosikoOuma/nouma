package com.nkds.hosikoouma.nouma.coil

import android.content.Context
import coil.ImageLoader
import coil.decode.VideoFrameDecoder

object CoilImageLoader {
    fun get(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components { 
                add(VideoFrameDecoder.Factory()) 
            }
            .build()
    }
}
