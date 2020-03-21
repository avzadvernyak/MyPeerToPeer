package m.kampukter.mypeertopeer._pilot

import m.kampukter.mypeertopeer.R

enum class VideoCallStatus(val label: Int, val color: Int) {
    UNKNOWN(
        R.string.status_unknown,
        R.color.colorUnknown
    ),
    CONNECTING(
        R.string.status_connecting,
        R.color.colorConnecting
    ),
    MATCHING(
        R.string.status_matching,
        R.color.colorMatching
    ),
    FAILED(
        R.string.status_failed,
        R.color.colorFailed
    ),
    CONNECTED(
        R.string.status_connected,
        R.color.colorConnected
    ),
    FINISHED(
        R.string.status_finished,
        R.color.colorConnected
    );
}