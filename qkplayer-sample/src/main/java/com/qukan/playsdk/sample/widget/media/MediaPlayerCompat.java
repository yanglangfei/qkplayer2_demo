package com.qukan.playsdk.sample.widget.media;

import com.qukan.playsdk.IMediaPlayer;
import com.qukan.playsdk.QkMediaPlayer;
import com.qukan.playsdk.MediaPlayerProxy;
import com.qukan.playsdk.TextureMediaPlayer;

public class MediaPlayerCompat {
    public static String getName(IMediaPlayer mp) {
        if (mp == null) {
            return "null";
        } else if (mp instanceof TextureMediaPlayer) {
            StringBuilder sb = new StringBuilder("TextureMediaPlayer <");
            IMediaPlayer internalMediaPlayer = ((TextureMediaPlayer) mp).getInternalMediaPlayer();
            if (internalMediaPlayer == null) {
                sb.append("null>");
            } else {
                sb.append(internalMediaPlayer.getClass().getSimpleName());
                sb.append(">");
            }
            return sb.toString();
        } else {
            return mp.getClass().getSimpleName();
        }
    }

    public static QkMediaPlayer getIjkMediaPlayer(IMediaPlayer mp) {
        QkMediaPlayer qkMediaPlayer = null;
        if (mp == null) {
            return null;
        } if (mp instanceof QkMediaPlayer) {
            qkMediaPlayer = (QkMediaPlayer) mp;
        } else if (mp instanceof MediaPlayerProxy && ((MediaPlayerProxy) mp).getInternalMediaPlayer() instanceof QkMediaPlayer) {
            qkMediaPlayer = (QkMediaPlayer) ((MediaPlayerProxy) mp).getInternalMediaPlayer();
        }
        return qkMediaPlayer;
    }

    public static void selectTrack(IMediaPlayer mp, int stream) {
        QkMediaPlayer qkMediaPlayer = getIjkMediaPlayer(mp);
        if (qkMediaPlayer == null)
            return;
        qkMediaPlayer.selectTrack(stream);
    }

    public static void deselectTrack(IMediaPlayer mp, int stream) {
        QkMediaPlayer qkMediaPlayer = getIjkMediaPlayer(mp);
        if (qkMediaPlayer == null)
            return;
        qkMediaPlayer.deselectTrack(stream);
    }

    public static int getSelectedTrack(IMediaPlayer mp, int trackType) {
        QkMediaPlayer qkMediaPlayer = getIjkMediaPlayer(mp);
        if (qkMediaPlayer == null)
            return -1;
        return qkMediaPlayer.getSelectedTrack(trackType);
    }
}
