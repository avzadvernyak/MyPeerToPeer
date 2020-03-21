package m.kampukter.mypeertopeer.data

import android.util.Log
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoRenderer

data class VideoRenderers(private val localView: SurfaceViewRenderer?, private val remoteView: SurfaceViewRenderer?) {
    val localRenderer: (VideoRenderer.I420Frame) -> Unit =
        if(localView == null) this::sink else { f -> localView.renderFrame(f) }
    val remoteRenderer: (VideoRenderer.I420Frame) -> Unit =
        if(remoteView == null) this::sink else { f -> remoteView.renderFrame(f) }

    private fun sink(frame: VideoRenderer.I420Frame) {
        Log.w("VideoRenderer", "Missing surface view, dropping frame")
        VideoRenderer.renderFrameDone(frame)
    }
}