package m.kampukter.mypeertopeer.data

import org.webrtc.SurfaceViewRenderer

data class InfoLocalVideoCapture(
    val userId: String,
    val localView: SurfaceViewRenderer,
    val remoteView: SurfaceViewRenderer
)