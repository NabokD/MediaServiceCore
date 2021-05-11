package com.liskovsoft.youtubeapi.videoinfo;

import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.youtubeapi.common.helpers.RetrofitHelper;
import com.liskovsoft.youtubeapi.common.helpers.ServiceHelper;
import com.liskovsoft.youtubeapi.common.locale.LocaleManager;
import com.liskovsoft.youtubeapi.videoinfo.models.VideoInfo;
import retrofit2.Call;

public class VideoInfoServiceSignedTMP extends VideoInfoServiceBase {
    private static final String TAG = VideoInfoServiceSignedTMP.class.getSimpleName();
    private static VideoInfoServiceSignedTMP sInstance;
    private final VideoInfoManagerSignedV2 mVideoInfoManagerSigned;
    private final LocaleManager mLocaleManager;

    private VideoInfoServiceSignedTMP() {
        mVideoInfoManagerSigned = RetrofitHelper.withQueryString(VideoInfoManagerSignedV2.class);
        mLocaleManager = LocaleManager.instance();
    }

    public static VideoInfoServiceSignedTMP instance() {
        if (sInstance == null) {
            sInstance = new VideoInfoServiceSignedTMP();
        }

        return sInstance;
    }

    public VideoInfo getVideoInfo(String videoId, String authorization) {
        VideoInfo result = getVideoInfoHls(videoId, authorization);

        if (result != null && result.isAgeRestricted()) {
            Log.e(TAG, "Seems that video age restricted. Retrying with different query method...");
            result = getVideoInfoRestricted(videoId, authorization);
        } else if (result != null && result.getVideoDetails() != null && result.getVideoDetails().isOwnerViewing()) {
            Log.e(TAG, "Seems that this is user video. Retrying with different query method...");
            result = getVideoInfoRegular(videoId, authorization);
        }

        if (result != null) {
            decipherFormats(result.getAdaptiveFormats());
            decipherFormats(result.getRegularFormats());
        } else {
            Log.e(TAG, "Can't get video info. videoId: %s, authorization: %s", videoId, authorization);
        }

        return result;
    }

    private VideoInfo getVideoInfoHls(String videoId, String authorization) {
        Call<VideoInfo> wrapper = mVideoInfoManagerSigned.getVideoInfoHls(videoId, mLocaleManager.getLanguage(), ServiceHelper.getToken(authorization));

        return RetrofitHelper.get(wrapper);
    }

    private VideoInfo getVideoInfoRegular(String videoId, String authorization) {
        Call<VideoInfo> wrapper = mVideoInfoManagerSigned.getVideoInfoRegular(videoId, mLocaleManager.getLanguage(), ServiceHelper.getToken(authorization));

        return RetrofitHelper.get(wrapper);
    }

    private VideoInfo getVideoInfoRestricted(String videoId, String authorization) {
        Call<VideoInfo> wrapper = mVideoInfoManagerSigned.getVideoInfoRestricted(videoId, ServiceHelper.getToken(authorization));

        return RetrofitHelper.get(wrapper);
    }
}
