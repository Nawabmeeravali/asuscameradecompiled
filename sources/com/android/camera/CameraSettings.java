package com.android.camera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.EncoderCapabilities;
import android.media.EncoderCapabilities.VideoEncoderCap;
import android.media.MediaRecorder.VideoEncoder;
import android.support.p000v4.media.MediaPlayer2;
import android.support.p000v4.view.PointerIconCompat;
import android.util.Log;
import com.android.camera.CameraHolder.CameraInfo;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.GcamHelper;
import com.android.camera.util.PersistUtil;
import com.asus.scenedetectlib.BuildConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import org.codeaurora.snapcam.C0905R;
import org.codeaurora.snapcam.wrapper.CamcorderProfileWrapper;
import org.codeaurora.snapcam.wrapper.ParametersWrapper;

public class CameraSettings {
    public static final int CURRENT_LOCAL_VERSION = 2;
    public static final int CURRENT_VERSION = 5;
    public static final int DEFAULT_VIDEO_DURATION = 0;
    public static final String DEFAULT_VIDEO_QUALITY_VALUE = "custom";
    public static final String EXPOSURE_DEFAULT_VALUE = "0";
    private static final String FALSE = "false";
    public static final String FLIP_MODE_H = "flip-h";
    public static final String FLIP_MODE_OFF = "off";
    public static final String FLIP_MODE_V = "flip-v";
    public static final String FLIP_MODE_VH = "flip-vh";
    public static final String KEY_ADVANCED_FEATURES = "pref_camera_advanced_features_key";
    public static final String KEY_AE_BRACKET_HDR = "pref_camera_ae_bracket_hdr_key";
    public static final String KEY_ANTIBANDING = "pref_camera_antibanding_key";
    public static final String KEY_AUDIO_ENCODER = "pref_camera_audioencoder_key";
    public static final String KEY_AUTOEXPOSURE = "pref_camera_autoexposure_key";
    public static final String KEY_AUTO_HDR = "pref_camera_auto_hdr_key";
    public static final String KEY_BOKEH_BLUR_VALUE = "pref_camera_bokeh_blur_degree_key";
    public static final String KEY_BOKEH_MODE = "pref_camera_bokeh_mode_key";
    public static final String KEY_BOKEH_MPO = "pref_camera_bokeh_mpo_key";
    public static final String KEY_BRIGHTNESS = "pref_camera_brightness_key";
    public static final String KEY_CAMERA_FIRST_USE_HINT_SHOWN = "pref_camera_first_use_hint_shown_key";
    public static final String KEY_CAMERA_HDR = "pref_camera_hdr_key";
    public static final String KEY_CAMERA_HDR_PLUS = "pref_camera_hdr_plus_key";
    public static final String KEY_CAMERA_HQ = "pref_camera_hq_key";
    public static final String KEY_CAMERA_ID = "pref_camera_id_key";
    public static final String KEY_CAMERA_SAVEPATH = "pref_camera_savepath_key";
    public static final String KEY_CDS_MODE = "pref_camera_cds_mode_key";
    public static final String KEY_COLOR_EFFECT = "pref_camera_coloreffect_key";
    public static final String KEY_CONTINUOUS_ISO = "continuous-iso";
    public static final String KEY_CONTRAST = "pref_camera_contrast_key";
    public static final String KEY_CURRENT_EXPOSURE_TIME = "cur-exposure-time";
    public static final String KEY_CURRENT_ISO = "cur-iso";
    public static final String KEY_DENOISE = "pref_camera_denoise_key";
    public static final String KEY_DEVELOPER_MENU = "pref_developer_menu_key";
    public static final String KEY_DIS = "pref_camera_dis_key";
    public static final String KEY_EXPOSURE = "pref_camera_exposure_key";
    public static final String KEY_EXPOSURE_TIME = "exposure-time";
    public static final String KEY_FACE_DETECTION = "pref_camera_facedetection_key";
    public static final String KEY_FACE_RECOGNITION = "pref_camera_facerc_key";
    public static final String KEY_FILTER_MODE = "pref_camera_filter_mode_key";
    public static final String KEY_FLASH_MODE = "pref_camera_flashmode_key";
    public static final String KEY_FOCUS_MODE = "pref_camera_focusmode_key";
    public static final String KEY_HDR_MODE = "pref_camera_hdr_mode_key";
    public static final String KEY_HDR_NEED_1X = "pref_camera_hdr_need_1x_key";
    public static final String KEY_HISTOGRAM = "pref_camera_histogram_key";
    public static final String KEY_INSTANT_CAPTURE = "pref_camera_instant_capture_key";
    public static final String KEY_INTERNAL_PREVIEW_RESTART = "internal-restart";
    public static final String KEY_ISO = "pref_camera_iso_key";
    public static final String KEY_JPEG_QUALITY = "pref_camera_jpegquality_key";
    public static final String KEY_LENSSHADING = "pref_camera_lensshading_key";
    public static final String KEY_LOCAL_VERSION = "pref_local_version_key";
    public static final String KEY_LONGSHOT = "pref_camera_longshot_key";
    public static final String KEY_MANUAL_EXPOSURE = "pref_camera_manual_exp_key";
    public static final String KEY_MANUAL_EXPOSURE_MODES = "manual-exp-modes";
    public static final String KEY_MANUAL_FOCUS = "pref_camera_manual_focus_key";
    public static final String KEY_MANUAL_FOCUS_DIOPTER = "cur-focus-diopter";
    public static final String KEY_MANUAL_FOCUS_MODES = "manual-focus-modes";
    public static final String KEY_MANUAL_FOCUS_POSITION = "manual-focus-position";
    public static final String KEY_MANUAL_FOCUS_SCALE = "cur-focus-scale";
    public static final String KEY_MANUAL_FOCUS_TYPE = "manual-focus-pos-type";
    public static final String KEY_MANUAL_ISO = "manual";
    public static final String KEY_MANUAL_WB = "pref_camera_manual_wb_key";
    public static final String KEY_MANUAL_WB_CCT = "wb-manual-cct";
    public static final String KEY_MANUAL_WB_GAINS = "manual-wb-gains";
    public static final String KEY_MANUAL_WB_MODES = "manual-wb-modes";
    public static final String KEY_MANUAL_WB_TYPE = "manual-wb-type";
    public static final String KEY_MANUAL_WB_VALUE = "manual-wb-value";
    public static final String KEY_MANUAL_WHITE_BALANCE = "manual";
    public static final String KEY_MAX_EXPOSURE_TIME = "max-exposure-time";
    public static final String KEY_MAX_FOCUS_DIOPTER = "max-focus-pos-diopter";
    public static final String KEY_MAX_FOCUS_SCALE = "max-focus-pos-ratio";
    public static final String KEY_MAX_ISO = "max-iso";
    public static final String KEY_MAX_WB_CCT = "max-wb-cct";
    public static final String KEY_MAX_WB_GAIN = "max-wb-gain";
    public static final String KEY_MIN_EXPOSURE_TIME = "min-exposure-time";
    public static final String KEY_MIN_FOCUS_DIOPTER = "min-focus-pos-diopter";
    public static final String KEY_MIN_FOCUS_SCALE = "min-focus-pos-ratio";
    public static final String KEY_MIN_ISO = "min-iso";
    public static final String KEY_MIN_WB_CCT = "min-wb-cct";
    public static final String KEY_MIN_WB_GAIN = "min-wb-gain";
    public static final String KEY_NOISE_REDUCTION = "pref_camera_noise_reduction_key";
    public static final String KEY_PHOTOSPHERE_PICTURESIZE = "pref_photosphere_picturesize_key";
    public static final String KEY_PICTURE_FORMAT = "pref_camera_pictureformat_key";
    public static final String KEY_PICTURE_SIZE = "pref_camera_picturesize_key";
    public static final String KEY_POWER_MODE = "pref_camera_powermode_key";
    public static final String KEY_QC_AE_BRACKETING = "ae-bracket-hdr";
    public static final String KEY_QC_AF_BRACKETING = "af-bracket";
    public static final String KEY_QC_BOKEH_BLUR_VALUE = "bokeh-blur-value";
    public static final String KEY_QC_BOKEH_MODE = "bokeh-mode";
    public static final String KEY_QC_BOKEH_MPO_MODE = "bokeh-mpo-mode";
    public static final String KEY_QC_CDS_MODE = "cds-mode";
    public static final String KEY_QC_CHROMA_FLASH = "chroma-flash";
    public static final String KEY_QC_DIS_MODE = "dis";
    public static final String KEY_QC_FACE_RECOGNITION = "face-recognition";
    public static final String KEY_QC_FSSR = "FSSR";
    public static final String KEY_QC_INSTANT_CAPTURE = "instant-capture";
    public static final String KEY_QC_INSTANT_CAPTURE_VALUES = "instant-capture-values";
    public static final String KEY_QC_IS_BOKEH_MODE_SUPPORTED = "is-bokeh-supported";
    public static final String KEY_QC_IS_BOKEH_MPO_SUPPORTED = "is-bokeh-mpo-supported";
    public static final String KEY_QC_LEGACY_BURST = "snapshot-burst-num";
    public static final String KEY_QC_LONGSHOT_SUPPORTED = "longshot-supported";
    public static final String KEY_QC_MULTI_TOUCH_FOCUS = "multi-touch-focus";
    public static final String KEY_QC_NOISE_REDUCTION_MODE = "noise-reduction-mode";
    public static final String KEY_QC_OPTI_ZOOM = "opti-zoom";
    private static final String KEY_QC_PICTURE_FORMAT = "picture-format-values";
    public static final String KEY_QC_PREVIEW_FLIP = "preview-flip";
    public static final String KEY_QC_RE_FOCUS = "re-focus";
    public static final int KEY_QC_RE_FOCUS_COUNT = 7;
    public static final String KEY_QC_SEE_MORE_MODE = "see-more";
    public static final String KEY_QC_SNAPSHOT_PICTURE_FLIP = "snapshot-picture-flip";
    public static final String KEY_QC_STILL_MORE = "still-more";
    private static final String KEY_QC_SUPPORTED_AE_BRACKETING_MODES = "ae-bracket-hdr-values";
    private static final String KEY_QC_SUPPORTED_AF_BRACKETING_MODES = "af-bracket-values";
    private static final String KEY_QC_SUPPORTED_CDS_MODES = "cds-mode-values";
    private static final String KEY_QC_SUPPORTED_CF_MODES = "chroma-flash-values";
    public static final String KEY_QC_SUPPORTED_DEGREES_OF_BLUR = "supported-blur-degrees";
    private static final String KEY_QC_SUPPORTED_DIS_MODES = "dis-values";
    private static final String KEY_QC_SUPPORTED_FACE_RECOGNITION_MODES = "face-recognition-values";
    public static final String KEY_QC_SUPPORTED_FLIP_MODES = "flip-mode-values";
    private static final String KEY_QC_SUPPORTED_FSSR_MODES = "FSSR-values";
    public static final String KEY_QC_SUPPORTED_MANUAL_EXPOSURE_MODES = "manual-exposure-modes";
    public static final String KEY_QC_SUPPORTED_MANUAL_FOCUS_MODES = "manual-focus-modes";
    public static final String KEY_QC_SUPPORTED_MANUAL_WB_MODES = "manual-wb-modes";
    private static final String KEY_QC_SUPPORTED_MTF_MODES = "multi-touch-focus-values";
    private static final String KEY_QC_SUPPORTED_NOISE_REDUCTION_MODES = "noise-reduction-mode-values";
    private static final String KEY_QC_SUPPORTED_OZ_MODES = "opti-zoom-values";
    private static final String KEY_QC_SUPPORTED_RE_FOCUS_MODES = "re-focus-values";
    private static final String KEY_QC_SUPPORTED_SEE_MORE_MODES = "see-more-values";
    private static final String KEY_QC_SUPPORTED_STILL_MORE_MODES = "still-more-values";
    private static final String KEY_QC_SUPPORTED_TNR_MODES = "tnr-mode-values";
    private static final String KEY_QC_SUPPORTED_TP_MODES = "true-portrait-values";
    private static final String KEY_QC_SUPPORTED_VIDEO_CDS_MODES = "video-cds-mode-values";
    private static final String KEY_QC_SUPPORTED_VIDEO_TNR_MODES = "video-tnr-mode-values";
    public static final String KEY_QC_TNR_MODE = "tnr-mode";
    public static final String KEY_QC_TP = "true-portrait";
    public static final String KEY_QC_VIDEO_CDS_MODE = "video-cds-mode";
    public static final String KEY_QC_VIDEO_FLIP = "video-flip";
    public static final String KEY_QC_VIDEO_TNR_MODE = "video-tnr-mode";
    public static final String KEY_QC_ZSL_HDR_SUPPORTED = "zsl-hdr-supported";
    public static final String KEY_RECORD_LOCATION = "pref_camera_recordlocation_key";
    public static final String KEY_REDEYE_REDUCTION = "pref_camera_redeyereduction_key";
    public static final String KEY_REFOCUS_PROMPT = "refocus-prompt";
    public static final String KEY_REQUEST_PERMISSION = "request_permission";
    public static final String KEY_SATURATION = "pref_camera_saturation_key";
    public static final String KEY_SCENE_MODE = "pref_camera_scenemode_key";
    public static final String KEY_SEE_MORE = "pref_camera_see_more_key";
    public static final String KEY_SELECTABLE_ZONE_AF = "pref_camera_selectablezoneaf_key";
    public static final String KEY_SELFIE_FLASH = "pref_selfie_flash_key";
    public static final String KEY_SELFIE_MIRROR = "pref_camera_selfiemirror_key";
    public static final String KEY_SHARPNESS = "pref_camera_sharpness_key";
    public static final String KEY_SHOW_MENU_HELP = "help_menu";
    public static final String KEY_SHUTTER_SOUND = "pref_camera_shuttersound_key";
    public static final String KEY_SKIN_TONE_ENHANCEMENT = "pref_camera_skinToneEnhancement_key";
    public static final String KEY_SKIN_TONE_ENHANCEMENT_FACTOR = "pref_camera_skinToneEnhancement_factor_key";
    public static final String KEY_SNAPCAM_HDR_MODE = "hdr-mode";
    public static final String KEY_SNAPCAM_HDR_NEED_1X = "hdr-need-1x";
    private static final String KEY_SNAPCAM_SUPPORTED_HDR_MODES = "hdr-mode-values";
    private static final String KEY_SNAPCAM_SUPPORTED_HDR_NEED_1X = "hdr-need-1x-values";
    public static final String KEY_STARTUP_MODULE_INDEX = "camera.startup_module";
    public static final String KEY_TIMER = "pref_camera_timer_key";
    public static final String KEY_TIMER_SOUND_EFFECTS = "pref_camera_timer_sound_key";
    public static final String KEY_TNR_MODE = "pref_camera_tnr_mode_key";
    public static final String KEY_TOUCH_AF_AEC = "pref_camera_touchafaec_key";
    public static final String KEY_TS_MAKEUP_LEVEL = "pref_camera_tsmakeup_level_key";
    public static final String KEY_TS_MAKEUP_LEVEL_CLEAN = "pref_camera_tsmakeup_clean";
    public static final String KEY_TS_MAKEUP_LEVEL_WHITEN = "pref_camera_tsmakeup_whiten";
    public static final String KEY_TS_MAKEUP_PARAM = "tsmakeup";
    public static final String KEY_TS_MAKEUP_PARAM_CLEAN = "tsmakeup_clean";
    public static final String KEY_TS_MAKEUP_PARAM_WHITEN = "tsmakeup_whiten";
    public static final String KEY_TS_MAKEUP_UILABLE = "pref_camera_tsmakeup_key";
    public static final String KEY_VERSION = "pref_version_key";
    public static final String KEY_VIDEOCAMERA_FLASH_MODE = "pref_camera_video_flashmode_key";
    public static final String KEY_VIDEO_CDS_MODE = "pref_camera_video_cds_mode_key";
    public static final String KEY_VIDEO_DURATION = "pref_camera_video_duration_key";
    public static final String KEY_VIDEO_EFFECT = "pref_video_effect_key";
    public static final String KEY_VIDEO_ENCODER = "pref_camera_videoencoder_key";
    public static final String KEY_VIDEO_FIRST_USE_HINT_SHOWN = "pref_video_first_use_hint_shown_key";
    public static final String KEY_VIDEO_HDR = "pref_camera_video_hdr_key";
    public static final String KEY_VIDEO_HIGH_FRAME_RATE = "pref_camera_hfr_key";
    public static final String KEY_VIDEO_HSR = "video-hsr";
    public static final String KEY_VIDEO_QUALITY = "pref_video_quality_key";
    public static final String KEY_VIDEO_ROTATION = "pref_camera_video_rotation_key";
    public static final String KEY_VIDEO_SNAPSHOT_SIZE = "pref_camera_videosnapsize_key";
    public static final String KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL = "pref_video_time_lapse_frame_interval_key";
    public static final String KEY_VIDEO_TNR_MODE = "pref_camera_video_tnr_mode_key";
    public static final String KEY_WHITE_BALANCE = "pref_camera_whitebalance_key";
    public static final String KEY_ZOOM = "pref_camera_zoom_key";
    public static final String KEY_ZSL = "pref_camera_zsl_key";
    private static final int MMS_VIDEO_DURATION = (CamcorderProfile.get(0) != null ? CamcorderProfile.get(0).duration : 30);
    private static final int NOT_FOUND = -1;
    private static final String TAG = "CameraSettings";
    private static final String TRUE = "true";
    public static final HashMap<String, Integer> VIDEO_ENCODER_BITRATE = new HashMap<>();
    private static final HashMap<Integer, String> VIDEO_ENCODER_TABLE = new HashMap<>();
    private static final String VIDEO_QUALITY_HIGH = "high";
    private static final String VIDEO_QUALITY_MMS = "mms";
    public static final HashMap<String, Integer> VIDEO_QUALITY_TABLE = new HashMap<>();
    private static final HashMap<Integer, Integer> VIDEO_QUALITY_TO_HIGHSPEED = new HashMap<>();
    private static final HashMap<Integer, Integer> VIDEO_QUALITY_TO_TIMELAPSE = new HashMap<>();
    private static final String VIDEO_QUALITY_YOUTUBE = "youtube";
    private static final int YOUTUBE_VIDEO_DURATION = 900;
    private final int mCameraId;
    private final CameraInfo[] mCameraInfo;
    private final Context mContext;
    private final Parameters mParameters;

    public static boolean isSupportPictureFormatChange() {
        return false;
    }

    static {
        VIDEO_ENCODER_TABLE.put(Integer.valueOf(1), "h263");
        VIDEO_ENCODER_TABLE.put(Integer.valueOf(2), "h264");
        int intFieldIfExists = ApiHelper.getIntFieldIfExists(VideoEncoder.class, "HEVC", null, 0);
        if (intFieldIfExists == 0) {
            intFieldIfExists = ApiHelper.getIntFieldIfExists(VideoEncoder.class, "H265", null, 0);
        }
        VIDEO_ENCODER_TABLE.put(Integer.valueOf(intFieldIfExists), "h265");
        VIDEO_ENCODER_TABLE.put(Integer.valueOf(3), "m4v");
        int i = CamcorderProfileWrapper.QUALITY_4KDCI;
        if (i != -1) {
            VIDEO_QUALITY_TABLE.put("4096x2160", Integer.valueOf(i));
        }
        VIDEO_QUALITY_TABLE.put("3840x2160", Integer.valueOf(8));
        int i2 = CamcorderProfileWrapper.QUALITY_QHD;
        if (i2 != -1) {
            VIDEO_QUALITY_TABLE.put("2560x1440", Integer.valueOf(i2));
        }
        int i3 = CamcorderProfileWrapper.QUALITY_2k;
        if (i3 != -1) {
            VIDEO_QUALITY_TABLE.put("2048x1080", Integer.valueOf(i3));
        }
        VIDEO_QUALITY_TABLE.put("1920x1080", Integer.valueOf(6));
        VIDEO_QUALITY_TABLE.put("1280x720", Integer.valueOf(5));
        VIDEO_QUALITY_TABLE.put("720x480", Integer.valueOf(4));
        int i4 = CamcorderProfileWrapper.QUALITY_VGA;
        if (i4 != -1) {
            VIDEO_QUALITY_TABLE.put("640x480", Integer.valueOf(i4));
        }
        VIDEO_QUALITY_TABLE.put("352x288", Integer.valueOf(3));
        VIDEO_QUALITY_TABLE.put("320x240", Integer.valueOf(7));
        VIDEO_QUALITY_TABLE.put("176x144", Integer.valueOf(2));
        VIDEO_ENCODER_BITRATE.put("3840x2160:60", Integer.valueOf(80000000));
        VIDEO_ENCODER_BITRATE.put("3840x2160:90", Integer.valueOf(96000000));
        VIDEO_ENCODER_BITRATE.put("3840x2160:120", Integer.valueOf(120000000));
        VIDEO_ENCODER_BITRATE.put("1920x1080:60", Integer.valueOf(32000000));
        VIDEO_ENCODER_BITRATE.put("1920x1080:120", Integer.valueOf(50000000));
        VIDEO_ENCODER_BITRATE.put("1920x1080:240", Integer.valueOf(80000000));
        VIDEO_ENCODER_BITRATE.put("1280x720:60", Integer.valueOf(24000000));
        VIDEO_ENCODER_BITRATE.put("1280x720:120", Integer.valueOf(35000000));
        VIDEO_ENCODER_BITRATE.put("1280x720:240", Integer.valueOf(55000000));
        VIDEO_ENCODER_BITRATE.put("1280x720:480", Integer.valueOf(88000000));
        VIDEO_ENCODER_BITRATE.put("720x480:120", Integer.valueOf(5200000));
        VIDEO_ENCODER_BITRATE.put("640x480:60", Integer.valueOf(2457600));
        VIDEO_ENCODER_BITRATE.put("640x480:120", Integer.valueOf(3932000));
        VIDEO_ENCODER_BITRATE.put("640x480:240", Integer.valueOf(6400000));
        VIDEO_ENCODER_BITRATE.put("352x288:60", Integer.valueOf(1152000));
        VIDEO_ENCODER_BITRATE.put("352x288:120", Integer.valueOf(1840000));
        VIDEO_ENCODER_BITRATE.put("352x288:240", Integer.valueOf(3000000));
        VIDEO_ENCODER_BITRATE.put("320x240:60", Integer.valueOf(819200));
        VIDEO_ENCODER_BITRATE.put("320x240:120", Integer.valueOf(1320000));
        VIDEO_ENCODER_BITRATE.put("320x240:240", Integer.valueOf(2100000));
        VIDEO_ENCODER_BITRATE.put("4096x2160:605", Integer.valueOf(70840000));
        VIDEO_ENCODER_BITRATE.put("4096x2160:905", Integer.valueOf(84700000));
        VIDEO_ENCODER_BITRATE.put("4096x2160:1205", Integer.valueOf(106260000));
        VIDEO_ENCODER_BITRATE.put("3840x2160:605", Integer.valueOf(61600000));
        VIDEO_ENCODER_BITRATE.put("3840x2160:905", Integer.valueOf(73920000));
        VIDEO_ENCODER_BITRATE.put("3840x2160:1205", Integer.valueOf(92400000));
        VIDEO_ENCODER_BITRATE.put("2560x1440:605", Integer.valueOf(38808000));
        VIDEO_ENCODER_BITRATE.put("2560x1440:905", Integer.valueOf(51744000));
        VIDEO_ENCODER_BITRATE.put("2560x1440:1205", Integer.valueOf(56595000));
        VIDEO_ENCODER_BITRATE.put("2560x1440:2405", Integer.valueOf(88935000));
        VIDEO_ENCODER_BITRATE.put("1920x1080:605", Integer.valueOf(24640000));
        VIDEO_ENCODER_BITRATE.put("1920x1080:905", Integer.valueOf(30800000));
        VIDEO_ENCODER_BITRATE.put("1920x1080:1205", Integer.valueOf(38500000));
        VIDEO_ENCODER_BITRATE.put("1920x1080:2405", Integer.valueOf(61600000));
        VIDEO_ENCODER_BITRATE.put("1280x720:605", Integer.valueOf(18480000));
        VIDEO_ENCODER_BITRATE.put("1280x720:905", Integer.valueOf(24640000));
        VIDEO_ENCODER_BITRATE.put("1280x720:1205", Integer.valueOf(26950000));
        VIDEO_ENCODER_BITRATE.put("1280x720:2405", Integer.valueOf(42350000));
        VIDEO_ENCODER_BITRATE.put("1280x720:4805", Integer.valueOf(67760000));
        VIDEO_ENCODER_BITRATE.put("640x480:605", Integer.valueOf(1892352));
        VIDEO_ENCODER_BITRATE.put("640x480:905", Integer.valueOf(2464000));
        VIDEO_ENCODER_BITRATE.put("640x480:1205", Integer.valueOf(3027640));
        VIDEO_ENCODER_BITRATE.put("640x480:2405", Integer.valueOf(4928000));
        VIDEO_ENCODER_BITRATE.put("352x288:605", Integer.valueOf(887040));
        VIDEO_ENCODER_BITRATE.put("352x288:905", Integer.valueOf(1078000));
        VIDEO_ENCODER_BITRATE.put("352x288:1205", Integer.valueOf(1416800));
        VIDEO_ENCODER_BITRATE.put("352x288:2405", Integer.valueOf(2310000));
        VIDEO_ENCODER_BITRATE.put("320x240:605", Integer.valueOf(630784));
        VIDEO_ENCODER_BITRATE.put("320x240:905", Integer.valueOf(770000));
        VIDEO_ENCODER_BITRATE.put("320x240:1205", Integer.valueOf(1016400));
        VIDEO_ENCODER_BITRATE.put("320x240:2405", Integer.valueOf(1617000));
        VIDEO_ENCODER_BITRATE.put("3840x2160:60:2", Integer.valueOf(80000000));
        VIDEO_ENCODER_BITRATE.put("3840x2160:60:5", Integer.valueOf(50400000));
        VIDEO_QUALITY_TO_TIMELAPSE.put(Integer.valueOf(0), Integer.valueOf(1000));
        VIDEO_QUALITY_TO_TIMELAPSE.put(Integer.valueOf(1), Integer.valueOf(1001));
        VIDEO_QUALITY_TO_TIMELAPSE.put(Integer.valueOf(2), Integer.valueOf(1002));
        VIDEO_QUALITY_TO_TIMELAPSE.put(Integer.valueOf(3), Integer.valueOf(1003));
        VIDEO_QUALITY_TO_TIMELAPSE.put(Integer.valueOf(4), Integer.valueOf(1004));
        VIDEO_QUALITY_TO_TIMELAPSE.put(Integer.valueOf(5), Integer.valueOf(MediaPlayer2.MEDIAPLAYER2_STATE_ERROR));
        VIDEO_QUALITY_TO_TIMELAPSE.put(Integer.valueOf(6), Integer.valueOf(PointerIconCompat.TYPE_CELL));
        VIDEO_QUALITY_TO_TIMELAPSE.put(Integer.valueOf(7), Integer.valueOf(PointerIconCompat.TYPE_CROSSHAIR));
        VIDEO_QUALITY_TO_TIMELAPSE.put(Integer.valueOf(8), Integer.valueOf(PointerIconCompat.TYPE_TEXT));
        int i5 = CamcorderProfileWrapper.QUALITY_VGA;
        if (i5 != -1) {
            VIDEO_QUALITY_TO_TIMELAPSE.put(Integer.valueOf(i5), Integer.valueOf(CamcorderProfileWrapper.QUALITY_TIME_LAPSE_VGA));
        }
        int i6 = CamcorderProfileWrapper.QUALITY_4KDCI;
        if (i6 != -1) {
            VIDEO_QUALITY_TO_TIMELAPSE.put(Integer.valueOf(i6), Integer.valueOf(CamcorderProfileWrapper.QUALITY_TIME_LAPSE_4KDCI));
        }
        VIDEO_QUALITY_TO_HIGHSPEED.put(Integer.valueOf(0), Integer.valueOf(2000));
        VIDEO_QUALITY_TO_HIGHSPEED.put(Integer.valueOf(1), Integer.valueOf(2001));
        VIDEO_QUALITY_TO_HIGHSPEED.put(Integer.valueOf(2), Integer.valueOf(-1));
        VIDEO_QUALITY_TO_HIGHSPEED.put(Integer.valueOf(3), Integer.valueOf(CamcorderProfileWrapper.QUALITY_HIGH_SPEED_CIF));
        VIDEO_QUALITY_TO_HIGHSPEED.put(Integer.valueOf(4), Integer.valueOf(2002));
        VIDEO_QUALITY_TO_HIGHSPEED.put(Integer.valueOf(5), Integer.valueOf(2003));
        VIDEO_QUALITY_TO_HIGHSPEED.put(Integer.valueOf(6), Integer.valueOf(2004));
        VIDEO_QUALITY_TO_HIGHSPEED.put(Integer.valueOf(7), Integer.valueOf(-1));
        VIDEO_QUALITY_TO_HIGHSPEED.put(Integer.valueOf(8), Integer.valueOf(2005));
        int i7 = CamcorderProfileWrapper.QUALITY_VGA;
        if (i7 != -1) {
            VIDEO_QUALITY_TO_HIGHSPEED.put(Integer.valueOf(i7), Integer.valueOf(CamcorderProfileWrapper.QUALITY_HIGH_SPEED_VGA));
        }
        int i8 = CamcorderProfileWrapper.QUALITY_4KDCI;
        if (i8 != -1) {
            VIDEO_QUALITY_TO_HIGHSPEED.put(Integer.valueOf(i8), Integer.valueOf(CamcorderProfileWrapper.QUALITY_HIGH_SPEED_4KDCI));
        }
    }

    public static int getTimeLapseQualityFor(int i) {
        return ((Integer) VIDEO_QUALITY_TO_TIMELAPSE.get(Integer.valueOf(i))).intValue();
    }

    public static int getHighSpeedQualityFor(int i) {
        return ((Integer) VIDEO_QUALITY_TO_HIGHSPEED.get(Integer.valueOf(i))).intValue();
    }

    public CameraSettings(Activity activity, Parameters parameters, int i, CameraInfo[] cameraInfoArr) {
        this.mContext = activity;
        this.mParameters = parameters;
        this.mCameraId = i;
        this.mCameraInfo = cameraInfoArr;
    }

    public PreferenceGroup getPreferenceGroup(int i) {
        PreferenceGroup preferenceGroup = (PreferenceGroup) new PreferenceInflater(this.mContext).inflate(i);
        if (this.mParameters != null) {
            initPreference(preferenceGroup);
        }
        return preferenceGroup;
    }

    public static String getSupportedHighestVideoQuality(int i, Parameters parameters) {
        return (String) getSupportedVideoQualities(i, parameters).get(0);
    }

    public static void initialCameraPictureSize(Context context, Parameters parameters) {
        String[] stringArray;
        List supportedPictureSizes = parameters.getSupportedPictureSizes();
        if (supportedPictureSizes != null) {
            for (String str : context.getResources().getStringArray(C0905R.array.pref_camera_picturesize_entryvalues)) {
                if (setCameraPictureSize(str, supportedPictureSizes, parameters)) {
                    Editor edit = ComboPreferences.get(context).edit();
                    edit.putString("pref_camera_picturesize_key", str);
                    edit.apply();
                    return;
                }
            }
            Log.e(TAG, "No supported picture size found");
        }
    }

    public static void removePreferenceFromScreen(PreferenceGroup preferenceGroup, String str) {
        removePreference(preferenceGroup, str);
    }

    public static boolean setCameraPictureSize(String str, List<Size> list, Parameters parameters) {
        int indexOf = str.indexOf(120);
        if (indexOf == -1) {
            return false;
        }
        int parseInt = Integer.parseInt(str.substring(0, indexOf));
        int parseInt2 = Integer.parseInt(str.substring(indexOf + 1));
        for (Size size : list) {
            if (size.width == parseInt && size.height == parseInt2) {
                parameters.setPictureSize(parseInt, parseInt2);
                return true;
            }
        }
        return false;
    }

    public static int getMaxVideoDuration(Context context) {
        try {
            return context.getResources().getInteger(C0905R.integer.max_video_recording_length);
        } catch (NotFoundException unused) {
            return 0;
        }
    }

    public static List<String> getSupportedFaceRecognitionModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_FACE_RECOGNITION_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedDISModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_DIS_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedSeeMoreModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_SEE_MORE_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedNoiseReductionModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_NOISE_REDUCTION_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedAEBracketingModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_AE_BRACKETING_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedCDSModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_CDS_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedVideoCDSModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_VIDEO_CDS_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedTNRModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_TNR_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedVideoTNRModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_VIDEO_TNR_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedHDRModes(Parameters parameters) {
        String str = parameters.get(KEY_SNAPCAM_SUPPORTED_HDR_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedHDRNeed1x(Parameters parameters) {
        String str = parameters.get(KEY_SNAPCAM_SUPPORTED_HDR_NEED_1X);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public List<String> getSupportedAdvancedFeatures(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_AF_BRACKETING_MODES);
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append(',');
        sb.append(parameters.get(KEY_QC_SUPPORTED_CF_MODES));
        String sb2 = sb.toString();
        StringBuilder sb3 = new StringBuilder();
        sb3.append(sb2);
        sb3.append(',');
        sb3.append(parameters.get(KEY_QC_SUPPORTED_OZ_MODES));
        String sb4 = sb3.toString();
        StringBuilder sb5 = new StringBuilder();
        sb5.append(sb4);
        sb5.append(',');
        sb5.append(parameters.get(KEY_QC_SUPPORTED_FSSR_MODES));
        String sb6 = sb5.toString();
        StringBuilder sb7 = new StringBuilder();
        sb7.append(sb6);
        sb7.append(',');
        sb7.append(parameters.get(KEY_QC_SUPPORTED_TP_MODES));
        String sb8 = sb7.toString();
        StringBuilder sb9 = new StringBuilder();
        sb9.append(sb8);
        sb9.append(',');
        sb9.append(parameters.get(KEY_QC_SUPPORTED_MTF_MODES));
        String sb10 = sb9.toString();
        StringBuilder sb11 = new StringBuilder();
        sb11.append(sb10);
        sb11.append(',');
        sb11.append(this.mContext.getString(C0905R.string.pref_camera_advanced_feature_default));
        String sb12 = sb11.toString();
        StringBuilder sb13 = new StringBuilder();
        sb13.append(sb12);
        sb13.append(',');
        sb13.append(parameters.get(KEY_QC_SUPPORTED_RE_FOCUS_MODES));
        String sb14 = sb13.toString();
        StringBuilder sb15 = new StringBuilder();
        sb15.append(sb14);
        sb15.append(',');
        sb15.append(parameters.get(KEY_QC_SUPPORTED_STILL_MORE_MODES));
        return split(sb15.toString());
    }

    public static List<String> getSupportedAFBracketingModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_AF_BRACKETING_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedChromaFlashModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_CF_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedOptiZoomModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_OZ_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedRefocusModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_RE_FOCUS_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedFSSRModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_FSSR_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedTruePortraitModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_TP_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedMultiTouchFocusModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_MTF_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedStillMoreModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_STILL_MORE_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedVideoSnapSizes(Parameters parameters) {
        List<String> sizeListToStringList = sizeListToStringList(parameters.getSupportedPictureSizes());
        sizeListToStringList.add(0, "auto");
        return sizeListToStringList;
    }

    private static ArrayList<String> split(String str) {
        if (str == null) {
            return null;
        }
        StringTokenizer stringTokenizer = new StringTokenizer(str, ",");
        ArrayList<String> arrayList = new ArrayList<>();
        while (stringTokenizer.hasMoreElements()) {
            arrayList.add(stringTokenizer.nextToken());
        }
        return arrayList;
    }

    private List<String> getSupportedPictureFormatLists() {
        String str = this.mParameters.get(KEY_QC_PICTURE_FORMAT);
        if (str == null) {
            str = "jpeg,raw";
        }
        return split(str);
    }

    public static List<String> getSupportedFlipMode(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_FLIP_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    private static List<String> getSupportedVideoEncoders() {
        ArrayList arrayList = new ArrayList();
        for (VideoEncoderCap videoEncoderCap : EncoderCapabilities.getVideoEncoders()) {
            String str = (String) VIDEO_ENCODER_TABLE.get(Integer.valueOf(videoEncoderCap.mCodec));
            if (str != null) {
                arrayList.add(str);
            }
        }
        return arrayList;
    }

    private static List<String> getSupportedZoomLevel(Parameters parameters) {
        ArrayList arrayList = new ArrayList();
        int intValue = ((Integer) parameters.getZoomRatios().get(parameters.getMaxZoom())).intValue() / 100;
        for (int i = 0; i <= intValue; i++) {
            arrayList.add(String.valueOf(i));
        }
        return arrayList;
    }

    private void qcomInitPreferences(PreferenceGroup preferenceGroup) {
        PreferenceGroup preferenceGroup2 = preferenceGroup;
        ListPreference findPreference = preferenceGroup2.findPreference(KEY_POWER_MODE);
        ListPreference findPreference2 = preferenceGroup2.findPreference(KEY_ZSL);
        ListPreference findPreference3 = preferenceGroup2.findPreference(KEY_COLOR_EFFECT);
        ListPreference findPreference4 = preferenceGroup2.findPreference("pref_camera_facedetection_key");
        ListPreference findPreference5 = preferenceGroup2.findPreference(KEY_TOUCH_AF_AEC);
        ListPreference findPreference6 = preferenceGroup2.findPreference(KEY_SELECTABLE_ZONE_AF);
        preferenceGroup2.findPreference(KEY_SATURATION);
        preferenceGroup2.findPreference(KEY_CONTRAST);
        preferenceGroup2.findPreference(KEY_SHARPNESS);
        ListPreference findPreference7 = preferenceGroup2.findPreference(KEY_AUTOEXPOSURE);
        ListPreference findPreference8 = preferenceGroup2.findPreference(KEY_ANTIBANDING);
        ListPreference findPreference9 = preferenceGroup2.findPreference(KEY_ISO);
        preferenceGroup2.findPreference(KEY_LENSSHADING);
        ListPreference findPreference10 = preferenceGroup2.findPreference(KEY_HISTOGRAM);
        ListPreference findPreference11 = preferenceGroup2.findPreference(KEY_DENOISE);
        ListPreference findPreference12 = preferenceGroup2.findPreference("pref_camera_redeyereduction_key");
        ListPreference findPreference13 = preferenceGroup2.findPreference(KEY_AE_BRACKET_HDR);
        ListPreference findPreference14 = preferenceGroup2.findPreference(KEY_ADVANCED_FEATURES);
        ListPreference listPreference = findPreference4;
        ListPreference findPreference15 = preferenceGroup2.findPreference(KEY_FACE_RECOGNITION);
        ListPreference listPreference2 = findPreference2;
        preferenceGroup2.findPreference("pref_camera_jpegquality_key");
        ListPreference findPreference16 = preferenceGroup2.findPreference(KEY_VIDEO_SNAPSHOT_SIZE);
        ListPreference listPreference3 = findPreference14;
        ListPreference findPreference17 = preferenceGroup2.findPreference(KEY_VIDEO_HDR);
        ListPreference listPreference4 = findPreference10;
        ListPreference findPreference18 = preferenceGroup2.findPreference(KEY_PICTURE_FORMAT);
        ListPreference findPreference19 = preferenceGroup2.findPreference("pref_camera_longshot_key");
        preferenceGroup2.findPreference(KEY_AUTO_HDR);
        ListPreference findPreference20 = preferenceGroup2.findPreference(KEY_HDR_MODE);
        ListPreference listPreference5 = findPreference16;
        ListPreference findPreference21 = preferenceGroup2.findPreference(KEY_HDR_NEED_1X);
        ListPreference listPreference6 = findPreference7;
        ListPreference findPreference22 = preferenceGroup2.findPreference(KEY_CDS_MODE);
        ListPreference listPreference7 = findPreference15;
        ListPreference findPreference23 = preferenceGroup2.findPreference(KEY_VIDEO_CDS_MODE);
        ListPreference listPreference8 = findPreference8;
        ListPreference findPreference24 = preferenceGroup2.findPreference(KEY_TNR_MODE);
        ListPreference listPreference9 = findPreference13;
        ListPreference findPreference25 = preferenceGroup2.findPreference(KEY_VIDEO_TNR_MODE);
        ListPreference listPreference10 = findPreference3;
        ListPreference findPreference26 = preferenceGroup2.findPreference(KEY_MANUAL_FOCUS);
        ListPreference findPreference27 = preferenceGroup2.findPreference(KEY_MANUAL_EXPOSURE);
        ListPreference findPreference28 = preferenceGroup2.findPreference(KEY_MANUAL_WB);
        ListPreference findPreference29 = preferenceGroup2.findPreference(KEY_INSTANT_CAPTURE);
        ListPreference listPreference11 = findPreference17;
        ListPreference findPreference30 = preferenceGroup2.findPreference(KEY_BOKEH_MODE);
        ListPreference listPreference12 = findPreference11;
        ListPreference findPreference31 = preferenceGroup2.findPreference(KEY_BOKEH_MPO);
        ListPreference listPreference13 = findPreference12;
        ListPreference findPreference32 = preferenceGroup2.findPreference(KEY_BOKEH_BLUR_VALUE);
        ListPreference listPreference14 = findPreference9;
        ListPreference findPreference33 = preferenceGroup2.findPreference(KEY_ZOOM);
        if (findPreference29 != null && !isInstantCaptureSupported(this.mParameters)) {
            removePreference(preferenceGroup2, findPreference29.getKey());
        }
        if (findPreference30 != null && !isBokehModeSupported(this.mParameters)) {
            removePreference(preferenceGroup2, findPreference30.getKey());
            removePreference(preferenceGroup2, findPreference32.getKey());
        }
        if (findPreference31 != null && !isBokehMPOSupported(this.mParameters)) {
            removePreference(preferenceGroup2, findPreference31.getKey());
        }
        if (findPreference21 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference21, getSupportedHDRNeed1x(this.mParameters));
        }
        if (findPreference20 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference20, getSupportedHDRModes(this.mParameters));
        }
        if (findPreference22 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference22, getSupportedCDSModes(this.mParameters));
        }
        if (findPreference23 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference23, getSupportedVideoCDSModes(this.mParameters));
        }
        if (findPreference24 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference24, getSupportedTNRModes(this.mParameters));
        }
        if (findPreference25 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference25, getSupportedVideoTNRModes(this.mParameters));
        }
        ListPreference findPreference34 = preferenceGroup2.findPreference("pref_camera_video_rotation_key");
        if (findPreference5 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference5, ParametersWrapper.getSupportedTouchAfAec(this.mParameters));
        }
        if (!ParametersWrapper.isPowerModeSupported(this.mParameters) && findPreference != null) {
            removePreference(preferenceGroup2, findPreference.getKey());
        }
        if (findPreference6 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference6, ParametersWrapper.getSupportedSelectableZoneAf(this.mParameters));
        }
        if (listPreference14 != null) {
            filterUnsupportedOptions(preferenceGroup2, listPreference14, ParametersWrapper.getSupportedIsoValues(this.mParameters));
        }
        if (listPreference13 != null) {
            filterUnsupportedOptions(preferenceGroup2, listPreference13, ParametersWrapper.getSupportedRedeyeReductionModes(this.mParameters));
        }
        if (listPreference12 != null) {
            filterUnsupportedOptions(preferenceGroup2, listPreference12, ParametersWrapper.getSupportedDenoiseModes(this.mParameters));
        }
        if (listPreference11 != null) {
            filterUnsupportedOptions(preferenceGroup2, listPreference11, ParametersWrapper.getSupportedVideoHDRModes(this.mParameters));
        }
        if (listPreference10 != null) {
            filterUnsupportedOptions(preferenceGroup2, listPreference10, this.mParameters.getSupportedColorEffects());
        }
        if (listPreference9 != null) {
            filterUnsupportedOptions(preferenceGroup2, listPreference9, getSupportedAEBracketingModes(this.mParameters));
        }
        if (listPreference8 != null) {
            filterUnsupportedOptions(preferenceGroup2, listPreference8, this.mParameters.getSupportedAntibanding());
        }
        if (listPreference7 != null) {
            filterUnsupportedOptions(preferenceGroup2, listPreference7, getSupportedFaceRecognitionModes(this.mParameters));
        }
        if (listPreference6 != null) {
            filterUnsupportedOptions(preferenceGroup2, listPreference6, ParametersWrapper.getSupportedAutoexposure(this.mParameters));
        }
        if (listPreference5 != null) {
            ListPreference listPreference15 = listPreference5;
            filterUnsupportedOptions(preferenceGroup2, listPreference15, getSupportedVideoSnapSizes(this.mParameters));
            filterSimilarPictureSize(preferenceGroup2, listPreference15);
        }
        if (listPreference4 != null) {
            filterUnsupportedOptions(preferenceGroup2, listPreference4, ParametersWrapper.getSupportedHistogramModes(this.mParameters));
        }
        if (findPreference18 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference18, getSupportedPictureFormatLists());
        }
        if (listPreference3 != null) {
            filterUnsupportedOptions(preferenceGroup2, listPreference3, getSupportedAdvancedFeatures(this.mParameters));
        }
        if (findPreference19 != null && !isLongshotSupported(this.mParameters)) {
            removePreference(preferenceGroup2, findPreference19.getKey());
        }
        if (findPreference34 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference34, ParametersWrapper.getSupportedVideoRotationValues(this.mParameters));
        }
        if (findPreference26 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference26, getSupportedManualFocusModes(this.mParameters));
        }
        if (findPreference28 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference28, getSupportedManualWBModes(this.mParameters));
        }
        if (findPreference27 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference27, getSupportedManualExposureModes(this.mParameters));
        }
        if (findPreference33 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference33, getSupportedZoomLevel(this.mParameters));
        }
        if (listPreference2 != null) {
            filterUnsupportedOptions(preferenceGroup2, listPreference2, ParametersWrapper.getSupportedZSLModes(this.mParameters));
        }
        if (listPreference != null) {
            filterUnsupportedOptions(preferenceGroup2, listPreference, ParametersWrapper.getSupportedFaceDetectionModes(this.mParameters));
        }
    }

    private void initPreference(PreferenceGroup preferenceGroup) {
        PreferenceGroup preferenceGroup2 = preferenceGroup;
        ListPreference findPreference = preferenceGroup2.findPreference("pref_video_quality_key");
        ListPreference findPreference2 = preferenceGroup2.findPreference(KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL);
        ListPreference findPreference3 = preferenceGroup2.findPreference("pref_camera_picturesize_key");
        ListPreference findPreference4 = preferenceGroup2.findPreference("pref_camera_whitebalance_key");
        ListPreference findPreference5 = preferenceGroup2.findPreference(KEY_QC_CHROMA_FLASH);
        ListPreference findPreference6 = preferenceGroup2.findPreference(KEY_SCENE_MODE);
        ListPreference findPreference7 = preferenceGroup2.findPreference(KEY_FLASH_MODE);
        ListPreference findPreference8 = preferenceGroup2.findPreference(KEY_FOCUS_MODE);
        IconListPreference iconListPreference = (IconListPreference) preferenceGroup2.findPreference("pref_camera_exposure_key");
        IconListPreference iconListPreference2 = (IconListPreference) preferenceGroup2.findPreference(KEY_CAMERA_ID);
        ListPreference findPreference9 = preferenceGroup2.findPreference(KEY_VIDEOCAMERA_FLASH_MODE);
        ListPreference findPreference10 = preferenceGroup2.findPreference(KEY_VIDEO_EFFECT);
        ListPreference findPreference11 = preferenceGroup2.findPreference(KEY_CAMERA_HDR);
        ListPreference findPreference12 = preferenceGroup2.findPreference("pref_camera_dis_key");
        ListPreference listPreference = findPreference11;
        ListPreference findPreference13 = preferenceGroup2.findPreference(KEY_CAMERA_HDR_PLUS);
        ListPreference findPreference14 = preferenceGroup2.findPreference(KEY_VIDEO_HIGH_FRAME_RATE);
        ListPreference listPreference2 = findPreference10;
        ListPreference findPreference15 = preferenceGroup2.findPreference(KEY_SEE_MORE);
        ListPreference listPreference3 = findPreference2;
        ListPreference findPreference16 = preferenceGroup2.findPreference("pref_camera_videoencoder_key");
        IconListPreference iconListPreference3 = iconListPreference2;
        ListPreference findPreference17 = preferenceGroup2.findPreference("pref_camera_noise_reduction_key");
        IconListPreference iconListPreference4 = iconListPreference;
        if (findPreference17 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference17, getSupportedNoiseReductionModes(this.mParameters));
        }
        if (findPreference15 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference15, getSupportedSeeMoreModes(this.mParameters));
        }
        String str = null;
        if (findPreference14 != null && ParametersWrapper.getSupportedHfrSizes(this.mParameters) == null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference14, null);
        }
        if (findPreference != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference, getSupportedVideoQualities(this.mCameraId, this.mParameters));
        }
        if (findPreference16 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference16, getSupportedVideoEncoders());
        }
        if (findPreference3 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference3, sizeListToStringList(this.mParameters.getSupportedPictureSizes()));
            filterSimilarPictureSize(preferenceGroup2, findPreference3);
        }
        if (findPreference4 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference4, this.mParameters.getSupportedWhiteBalance());
        }
        if (findPreference5 != null) {
            if (!CameraUtil.isSupported(this.mContext.getString(C0905R.string.pref_camera_advanced_feature_value_chromaflash_on), getSupportedAdvancedFeatures(this.mParameters))) {
                removePreference(preferenceGroup2, findPreference5.getKey());
            }
            removePreference(preferenceGroup2, findPreference5.getKey());
        }
        if (findPreference6 != null) {
            List supportedSceneModes = this.mParameters.getSupportedSceneModes();
            List supportedAdvancedFeatures = getSupportedAdvancedFeatures(this.mParameters);
            if (CameraUtil.isSupported(this.mContext.getString(C0905R.string.pref_camera_advanced_feature_value_refocus_on), supportedAdvancedFeatures)) {
                supportedSceneModes.add(this.mContext.getString(C0905R.string.pref_camera_advanced_feature_value_refocus_on));
            }
            if (CameraUtil.isSupported(this.mContext.getString(C0905R.string.pref_camera_advanced_feature_value_optizoom_on), supportedAdvancedFeatures)) {
                supportedSceneModes.add(this.mContext.getString(C0905R.string.pref_camera_advanced_feature_value_optizoom_on));
            }
            filterUnsupportedOptions(preferenceGroup2, findPreference6, supportedSceneModes);
        }
        if (findPreference7 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference7, this.mParameters.getSupportedFlashModes());
        }
        if (findPreference12 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference12, getSupportedDISModes(this.mParameters));
        }
        if (findPreference8 != null && !CameraUtil.isFocusAreaSupported(this.mParameters)) {
            filterUnsupportedOptions(preferenceGroup2, findPreference8, this.mParameters.getSupportedFocusModes());
        }
        if (findPreference9 != null) {
            filterUnsupportedOptions(preferenceGroup2, findPreference9, this.mParameters.getSupportedFlashModes());
        }
        if (iconListPreference4 != null) {
            buildExposureCompensation(preferenceGroup2, iconListPreference4);
        }
        if (iconListPreference3 != null) {
            buildCameraId(preferenceGroup2, iconListPreference3);
        }
        if (listPreference3 != null) {
            resetIfInvalid(listPreference3);
        }
        if (listPreference2 != null) {
            filterUnsupportedOptions(preferenceGroup2, listPreference2, null);
        }
        if (listPreference != null && (!ApiHelper.HAS_CAMERA_HDR || !CameraUtil.isCameraHdrSupported(this.mParameters))) {
            removePreference(preferenceGroup2, listPreference.getKey());
        }
        boolean z = CameraHolder.instance().getFrontCameraId() == this.mCameraId;
        if (findPreference13 != null && (!ApiHelper.HAS_CAMERA_HDR_PLUS || !GcamHelper.hasGcamCapture() || z)) {
            removePreference(preferenceGroup2, findPreference13.getKey());
        }
        if (PersistUtil.isSaveInSdEnabled()) {
            String str2 = "pref_camera_savepath_key";
            ListPreference findPreference18 = preferenceGroup2.findPreference(str2);
            SharedPreferences sharedPreferences = preferenceGroup.getSharedPreferences();
            String str3 = "1";
            if (sharedPreferences != null) {
                str = sharedPreferences.getString(str2, str3);
            }
            if (findPreference18 != null && str3.equals(str)) {
                boolean isWriteable = SDCard.instance().isWriteable();
                String str4 = TAG;
                if (isWriteable) {
                    Log.d(str4, "set Sdcard as save path.");
                    findPreference18.setValueIndex(1);
                } else {
                    Log.d(str4, "set Phone as save path when sdCard is unavailable.");
                    findPreference18.setValueIndex(0);
                }
            }
        }
        qcomInitPreferences(preferenceGroup);
    }

    private void buildExposureCompensation(PreferenceGroup preferenceGroup, IconListPreference iconListPreference) {
        int maxExposureCompensation = this.mParameters.getMaxExposureCompensation();
        int minExposureCompensation = this.mParameters.getMinExposureCompensation();
        if (maxExposureCompensation == 0 && minExposureCompensation == 0) {
            removePreference(preferenceGroup, iconListPreference.getKey());
            return;
        }
        float exposureCompensationStep = this.mParameters.getExposureCompensationStep();
        int min = Math.min(3, (int) Math.floor((double) (((float) maxExposureCompensation) * exposureCompensationStep)));
        int max = Math.max(-3, (int) Math.ceil((double) (((float) minExposureCompensation) * exposureCompensationStep)));
        String string = this.mContext.getResources().getString(C0905R.string.pref_exposure_label);
        int i = (min - max) + 1;
        CharSequence[] charSequenceArr = new CharSequence[i];
        CharSequence[] charSequenceArr2 = new CharSequence[i];
        CharSequence[] charSequenceArr3 = new CharSequence[i];
        int[] iArr = new int[i];
        TypedArray obtainTypedArray = this.mContext.getResources().obtainTypedArray(C0905R.array.pref_camera_exposure_icons);
        for (int i2 = max; i2 <= min; i2++) {
            int i3 = i2 - max;
            charSequenceArr2[i3] = Integer.toString(Math.round(((float) i2) / exposureCompensationStep));
            StringBuilder sb = new StringBuilder();
            if (i2 > 0) {
                sb.append('+');
            }
            sb.append(i2);
            charSequenceArr[i3] = sb.toString();
            StringBuilder sb2 = new StringBuilder();
            sb2.append(string);
            sb2.append(" ");
            sb2.append(sb.toString());
            charSequenceArr3[i3] = sb2.toString();
            iArr[i3] = obtainTypedArray.getResourceId(i2 + 3, 0);
        }
        iconListPreference.setUseSingleIcon(true);
        iconListPreference.setEntries(charSequenceArr);
        iconListPreference.setLabels(charSequenceArr3);
        iconListPreference.setEntryValues(charSequenceArr2);
    }

    private void buildCameraId(PreferenceGroup preferenceGroup, IconListPreference iconListPreference) {
        int length = this.mCameraInfo.length;
        if (length < 2) {
            removePreference(preferenceGroup, iconListPreference.getKey());
            return;
        }
        CharSequence[] charSequenceArr = new CharSequence[length];
        for (int i = 0; i < length; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(BuildConfig.FLAVOR);
            sb.append(i);
            charSequenceArr[i] = sb.toString();
        }
        iconListPreference.setEntryValues(charSequenceArr);
    }

    private static boolean removePreference(PreferenceGroup preferenceGroup, String str) {
        int size = preferenceGroup.size();
        int i = 0;
        while (i < size) {
            CameraPreference cameraPreference = preferenceGroup.get(i);
            if ((cameraPreference instanceof PreferenceGroup) && removePreference((PreferenceGroup) cameraPreference, str)) {
                return true;
            }
            if (!(cameraPreference instanceof ListPreference) || !((ListPreference) cameraPreference).getKey().equals(str)) {
                i++;
            } else {
                preferenceGroup.removePreference(i);
                return true;
            }
        }
        return false;
    }

    private static boolean filterUnsupportedOptions(PreferenceGroup preferenceGroup, ListPreference listPreference, List<String> list) {
        if (list == null || list.size() <= 1) {
            removePreference(preferenceGroup, listPreference.getKey());
            return true;
        }
        listPreference.filterUnsupported(list);
        if (listPreference.getEntries().length <= 1) {
            removePreference(preferenceGroup, listPreference.getKey());
            return true;
        }
        resetIfInvalid(listPreference);
        return false;
    }

    private static boolean filterSimilarPictureSize(PreferenceGroup preferenceGroup, ListPreference listPreference) {
        listPreference.filterDuplicated();
        if (listPreference.getEntries().length <= 1) {
            removePreference(preferenceGroup, listPreference.getKey());
            return true;
        }
        resetIfInvalid(listPreference);
        return false;
    }

    private static void resetIfInvalid(ListPreference listPreference) {
        if (listPreference.findIndexOfValue(listPreference.getValue()) == -1) {
            listPreference.setValueIndex(0);
        }
    }

    private static List<String> sizeListToStringList(List<Size> list) {
        ArrayList arrayList = new ArrayList();
        for (Size size : list) {
            arrayList.add(String.format(Locale.ENGLISH, "%dx%d", new Object[]{Integer.valueOf(size.width), Integer.valueOf(size.height)}));
        }
        return arrayList;
    }

    public static void upgradeLocalPreferences(SharedPreferences sharedPreferences) {
        String str = KEY_LOCAL_VERSION;
        int i = 0;
        try {
            i = sharedPreferences.getInt(str, 0);
        } catch (Exception unused) {
        }
        if (i != 2) {
            Editor edit = sharedPreferences.edit();
            if (i == 1) {
                edit.remove("pref_video_quality_key");
            }
            edit.putInt(str, 2);
            edit.apply();
        }
    }

    public static void upgradeGlobalPreferences(SharedPreferences sharedPreferences, Context context) {
        upgradeOldVersion(sharedPreferences, context);
        upgradeCameraId(sharedPreferences);
    }

    public static void upgradeOldVersion(SharedPreferences sharedPreferences, Context context) {
        int i;
        String str;
        String str2 = "none";
        String str3 = KEY_VERSION;
        String str4 = "pref_camera_recordlocation_key";
        try {
            i = sharedPreferences.getInt(str3, 0);
        } catch (Exception unused) {
            i = 0;
        }
        if (i != 5) {
            Editor edit = sharedPreferences.edit();
            if (i == 0) {
                i = 1;
            }
            if (i == 1) {
                String str5 = "pref_camera_jpegquality_key";
                String string = sharedPreferences.getString(str5, "85");
                if (string.equals("65")) {
                    str = "normal";
                } else if (string.equals("75")) {
                    str = "fine";
                } else {
                    str = context.getString(C0905R.string.pref_camera_jpegquality_default);
                }
                edit.putString(str5, str);
                i = 2;
            }
            if (i == 2) {
                try {
                    edit.putString(str4, sharedPreferences.getBoolean(str4, false) ? RecordLocationPreference.VALUE_ON : str2);
                } catch (ClassCastException unused2) {
                    edit.putString(str4, str2);
                }
                i = 3;
            }
            if (i == 3) {
                edit.remove("pref_camera_videoquality_key");
                edit.remove("pref_camera_video_duration_key");
            }
            edit.putInt(str3, 5);
            edit.apply();
        }
    }

    private static void upgradeCameraId(SharedPreferences sharedPreferences) {
        int readPreferredCameraId = readPreferredCameraId(sharedPreferences);
        if (readPreferredCameraId != 0) {
            int numberOfCameras = CameraHolder.instance().getNumberOfCameras();
            if (readPreferredCameraId < 0 || readPreferredCameraId >= numberOfCameras) {
                readPreferredCameraId = 0;
            }
            writePreferredCameraId(sharedPreferences, readPreferredCameraId);
        }
    }

    public static int readPreferredCameraId(SharedPreferences sharedPreferences) {
        return Integer.parseInt(sharedPreferences.getString(KEY_CAMERA_ID, Integer.toString(CameraHolder.instance().getBackCameraId())));
    }

    public static void writePreferredCameraId(SharedPreferences sharedPreferences, int i) {
        Editor edit = sharedPreferences.edit();
        edit.putString(KEY_CAMERA_ID, Integer.toString(i));
        edit.apply();
    }

    public static int readExposure(ComboPreferences comboPreferences) {
        String string = comboPreferences.getString("pref_camera_exposure_key", "0");
        try {
            return Integer.parseInt(string);
        } catch (Exception unused) {
            StringBuilder sb = new StringBuilder();
            sb.append("Invalid exposure: ");
            sb.append(string);
            Log.e(TAG, sb.toString());
            return 0;
        }
    }

    public static void restorePreferences(Context context, ComboPreferences comboPreferences, Parameters parameters) {
        int readPreferredCameraId = readPreferredCameraId(comboPreferences);
        int backCameraId = CameraHolder.instance().getBackCameraId();
        if (backCameraId != -1) {
            comboPreferences.setLocalId(context, backCameraId);
            Editor edit = comboPreferences.edit();
            edit.clear();
            edit.apply();
        }
        int frontCameraId = CameraHolder.instance().getFrontCameraId();
        if (frontCameraId != -1) {
            comboPreferences.setLocalId(context, frontCameraId);
            Editor edit2 = comboPreferences.edit();
            edit2.clear();
            edit2.apply();
        }
        comboPreferences.setLocalId(context, readPreferredCameraId);
        upgradeGlobalPreferences(comboPreferences.getGlobal(), context);
        upgradeLocalPreferences(comboPreferences.getLocal());
        initialCameraPictureSize(context, parameters);
        writePreferredCameraId(comboPreferences, readPreferredCameraId);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0027, code lost:
        r5 = true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean checkSupportedVideoQuality(android.hardware.Camera.Parameters r5, int r6, int r7) {
        /*
            java.util.List r5 = r5.getSupportedVideoSizes()
            java.util.Iterator r5 = r5.iterator()
        L_0x0008:
            boolean r0 = r5.hasNext()
            r1 = 0
            r2 = 1
            if (r0 == 0) goto L_0x0029
            java.lang.Object r0 = r5.next()
            android.hardware.Camera$Size r0 = (android.hardware.Camera.Size) r0
            int r3 = r0.height
            r4 = 480(0x1e0, float:6.73E-43)
            if (r3 != r4) goto L_0x0023
            if (r3 != r7) goto L_0x0008
            int r0 = r0.width
            if (r0 != r6) goto L_0x0008
            goto L_0x0027
        L_0x0023:
            int r0 = r0.width
            if (r0 != r6) goto L_0x0008
        L_0x0027:
            r5 = r2
            goto L_0x002a
        L_0x0029:
            r5 = r1
        L_0x002a:
            if (r5 != r2) goto L_0x002d
            return r2
        L_0x002d:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.CameraSettings.checkSupportedVideoQuality(android.hardware.Camera$Parameters, int, int):boolean");
    }

    private static ArrayList<String> getSupportedVideoQuality(int i, Parameters parameters) {
        ArrayList<String> arrayList = new ArrayList<>();
        if (ApiHelper.HAS_FINE_RESOLUTION_QUALITY_LEVELS) {
            getFineResolutionQuality(arrayList, i, parameters);
        } else {
            arrayList.add(Integer.toString(1));
            CamcorderProfile camcorderProfile = CamcorderProfile.get(i, 1);
            CamcorderProfile camcorderProfile2 = CamcorderProfile.get(i, 0);
            if (camcorderProfile.videoFrameHeight * camcorderProfile.videoFrameWidth > camcorderProfile2.videoFrameHeight * camcorderProfile2.videoFrameWidth) {
                arrayList.add(Integer.toString(0));
            }
        }
        return arrayList;
    }

    @TargetApi(18)
    private static void getFineResolutionQuality(ArrayList<String> arrayList, int i, Parameters parameters) {
        if (CamcorderProfile.hasProfile(i, CamcorderProfileWrapper.QUALITY_4KDCI) && checkSupportedVideoQuality(parameters, 4096, 2160)) {
            arrayList.add(Integer.toString(CamcorderProfileWrapper.QUALITY_4KDCI));
        }
        if (CamcorderProfile.hasProfile(i, 8) && checkSupportedVideoQuality(parameters, 3840, 2160)) {
            arrayList.add(Integer.toString(8));
        }
        if (CamcorderProfile.hasProfile(i, 6) && checkSupportedVideoQuality(parameters, 1920, 1080)) {
            arrayList.add(Integer.toString(6));
        }
        if (CamcorderProfile.hasProfile(i, 5) && checkSupportedVideoQuality(parameters, 1280, 720)) {
            arrayList.add(Integer.toString(5));
        }
        if (CamcorderProfile.hasProfile(i, 4) && checkSupportedVideoQuality(parameters, 720, 480)) {
            arrayList.add(Integer.toString(4));
        }
        if (CamcorderProfile.hasProfile(i, CamcorderProfileWrapper.QUALITY_VGA) && checkSupportedVideoQuality(parameters, 640, 480)) {
            arrayList.add(Integer.toString(CamcorderProfileWrapper.QUALITY_VGA));
        }
        if (CamcorderProfile.hasProfile(i, 3) && checkSupportedVideoQuality(parameters, 352, 288)) {
            arrayList.add(Integer.toString(3));
        }
        if (CamcorderProfile.hasProfile(i, 7) && checkSupportedVideoQuality(parameters, 320, 240)) {
            arrayList.add(Integer.toString(7));
        }
        if (CamcorderProfile.hasProfile(i, 2) && checkSupportedVideoQuality(parameters, 176, Const.CODE_C1_SPA)) {
            arrayList.add(Integer.toString(2));
        }
    }

    public static ArrayList<String> getSupportedVideoQualities(int i, Parameters parameters) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (String str : sizeListToStringList(parameters.getSupportedVideoSizes())) {
            if (VIDEO_QUALITY_TABLE.containsKey(str) && CamcorderProfile.hasProfile(i, ((Integer) VIDEO_QUALITY_TABLE.get(str)).intValue())) {
                arrayList.add(str);
            }
        }
        return arrayList;
    }

    public static int getVideoDurationInMillis(String str) {
        if (VIDEO_QUALITY_MMS.equals(str)) {
            return MMS_VIDEO_DURATION * 1000;
        }
        return VIDEO_QUALITY_YOUTUBE.equals(str) ? 900000 : 0;
    }

    public static boolean isInternalPreviewSupported(Parameters parameters) {
        if (parameters != null) {
            String str = parameters.get(KEY_INTERNAL_PREVIEW_RESTART);
            if (str != null && "true".equals(str)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLongshotSupported(Parameters parameters) {
        if (parameters != null) {
            String str = parameters.get(KEY_QC_LONGSHOT_SUPPORTED);
            if (str != null && "true".equals(str)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isZSLHDRSupported(Parameters parameters) {
        if (parameters != null) {
            String str = parameters.get(KEY_QC_ZSL_HDR_SUPPORTED);
            if (str != null && "true".equals(str)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getSupportedManualExposureModes(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_MANUAL_EXPOSURE_MODES);
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedManualFocusModes(Parameters parameters) {
        String str = parameters.get("manual-focus-modes");
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static List<String> getSupportedManualWBModes(Parameters parameters) {
        String str = parameters.get("manual-wb-modes");
        if (str == null) {
            return null;
        }
        return split(str);
    }

    public static boolean isInstantCaptureSupported(Parameters parameters) {
        return (parameters == null || parameters.get(KEY_QC_INSTANT_CAPTURE_VALUES) == null) ? false : true;
    }

    public static boolean isBokehModeSupported(Parameters parameters) {
        if (parameters != null) {
            if ("1".equals(parameters.get(KEY_QC_IS_BOKEH_MODE_SUPPORTED))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBokehMPOSupported(Parameters parameters) {
        if (parameters != null) {
            if ("1".equals(parameters.get(KEY_QC_IS_BOKEH_MPO_SUPPORTED))) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getSupportedDegreesOfBlur(Parameters parameters) {
        String str = parameters.get(KEY_QC_SUPPORTED_DEGREES_OF_BLUR);
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("getSupportedDegreesOfBlur str =");
        sb.append(str);
        Log.d(TAG, sb.toString());
        return split(str);
    }
}
