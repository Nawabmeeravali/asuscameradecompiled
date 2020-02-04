package android.support.p000v4.media.subtitle;

import android.graphics.Color;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@RequiresApi(28)
/* renamed from: android.support.v4.media.subtitle.Cea708CCParser */
class Cea708CCParser {
    public static final int CAPTION_EMIT_TYPE_BUFFER = 1;
    public static final int CAPTION_EMIT_TYPE_COMMAND_CLW = 4;
    public static final int CAPTION_EMIT_TYPE_COMMAND_CWX = 3;
    public static final int CAPTION_EMIT_TYPE_COMMAND_DFX = 16;
    public static final int CAPTION_EMIT_TYPE_COMMAND_DLC = 10;
    public static final int CAPTION_EMIT_TYPE_COMMAND_DLW = 8;
    public static final int CAPTION_EMIT_TYPE_COMMAND_DLY = 9;
    public static final int CAPTION_EMIT_TYPE_COMMAND_DSW = 5;
    public static final int CAPTION_EMIT_TYPE_COMMAND_HDW = 6;
    public static final int CAPTION_EMIT_TYPE_COMMAND_RST = 11;
    public static final int CAPTION_EMIT_TYPE_COMMAND_SPA = 12;
    public static final int CAPTION_EMIT_TYPE_COMMAND_SPC = 13;
    public static final int CAPTION_EMIT_TYPE_COMMAND_SPL = 14;
    public static final int CAPTION_EMIT_TYPE_COMMAND_SWA = 15;
    public static final int CAPTION_EMIT_TYPE_COMMAND_TGW = 7;
    public static final int CAPTION_EMIT_TYPE_CONTROL = 2;
    private static final boolean DEBUG = false;
    private static final String MUSIC_NOTE_CHAR = new String("♫".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    private static final String TAG = "Cea708CCParser";
    private final StringBuilder mBuilder = new StringBuilder();
    private int mCommand = 0;
    private DisplayListener mListener = new DisplayListener() {
        public void emitEvent(CaptionEvent captionEvent) {
        }
    };

    /* renamed from: android.support.v4.media.subtitle.Cea708CCParser$CaptionColor */
    public static class CaptionColor {
        private static final int[] COLOR_MAP = {0, 15, 240, 255};
        public static final int OPACITY_FLASH = 1;
        private static final int[] OPACITY_MAP = {255, 254, 128, 0};
        public static final int OPACITY_SOLID = 0;
        public static final int OPACITY_TRANSLUCENT = 2;
        public static final int OPACITY_TRANSPARENT = 3;
        public final int blue;
        public final int green;
        public final int opacity;
        public final int red;

        CaptionColor(int i, int i2, int i3, int i4) {
            this.opacity = i;
            this.red = i2;
            this.green = i3;
            this.blue = i4;
        }

        public int getArgbValue() {
            int i = OPACITY_MAP[this.opacity];
            int[] iArr = COLOR_MAP;
            return Color.argb(i, iArr[this.red], iArr[this.green], iArr[this.blue]);
        }
    }

    /* renamed from: android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent */
    public static class CaptionEvent {
        public final Object obj;
        public final int type;

        CaptionEvent(int i, Object obj2) {
            this.type = i;
            this.obj = obj2;
        }
    }

    /* renamed from: android.support.v4.media.subtitle.Cea708CCParser$CaptionPenAttr */
    public static class CaptionPenAttr {
        public static final int OFFSET_NORMAL = 1;
        public static final int OFFSET_SUBSCRIPT = 0;
        public static final int OFFSET_SUPERSCRIPT = 2;
        public static final int PEN_SIZE_LARGE = 2;
        public static final int PEN_SIZE_SMALL = 0;
        public static final int PEN_SIZE_STANDARD = 1;
        public final int edgeType;
        public final int fontTag;
        public final boolean italic;
        public final int penOffset;
        public final int penSize;
        public final int textTag;
        public final boolean underline;

        CaptionPenAttr(int i, int i2, int i3, int i4, int i5, boolean z, boolean z2) {
            this.penSize = i;
            this.penOffset = i2;
            this.textTag = i3;
            this.fontTag = i4;
            this.edgeType = i5;
            this.underline = z;
            this.italic = z2;
        }
    }

    /* renamed from: android.support.v4.media.subtitle.Cea708CCParser$CaptionPenColor */
    public static class CaptionPenColor {
        public final CaptionColor backgroundColor;
        public final CaptionColor edgeColor;
        public final CaptionColor foregroundColor;

        CaptionPenColor(CaptionColor captionColor, CaptionColor captionColor2, CaptionColor captionColor3) {
            this.foregroundColor = captionColor;
            this.backgroundColor = captionColor2;
            this.edgeColor = captionColor3;
        }
    }

    /* renamed from: android.support.v4.media.subtitle.Cea708CCParser$CaptionPenLocation */
    public static class CaptionPenLocation {
        public final int column;
        public final int row;

        CaptionPenLocation(int i, int i2) {
            this.row = i;
            this.column = i2;
        }
    }

    /* renamed from: android.support.v4.media.subtitle.Cea708CCParser$CaptionWindow */
    public static class CaptionWindow {
        public final int anchorHorizontal;
        public final int anchorId;
        public final int anchorVertical;
        public final int columnCount;
        public final boolean columnLock;

        /* renamed from: id */
        public final int f55id;
        public final int penStyle;
        public final int priority;
        public final boolean relativePositioning;
        public final int rowCount;
        public final boolean rowLock;
        public final boolean visible;
        public final int windowStyle;

        CaptionWindow(int i, boolean z, boolean z2, boolean z3, int i2, boolean z4, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
            this.f55id = i;
            this.visible = z;
            this.rowLock = z2;
            this.columnLock = z3;
            this.priority = i2;
            this.relativePositioning = z4;
            this.anchorVertical = i3;
            this.anchorHorizontal = i4;
            this.anchorId = i5;
            this.rowCount = i6;
            this.columnCount = i7;
            this.penStyle = i8;
            this.windowStyle = i9;
        }
    }

    /* renamed from: android.support.v4.media.subtitle.Cea708CCParser$CaptionWindowAttr */
    public static class CaptionWindowAttr {
        public final CaptionColor borderColor;
        public final int borderType;
        public final int displayEffect;
        public final int effectDirection;
        public final int effectSpeed;
        public final CaptionColor fillColor;
        public final int justify;
        public final int printDirection;
        public final int scrollDirection;
        public final boolean wordWrap;

        CaptionWindowAttr(CaptionColor captionColor, CaptionColor captionColor2, int i, boolean z, int i2, int i3, int i4, int i5, int i6, int i7) {
            this.fillColor = captionColor;
            this.borderColor = captionColor2;
            this.borderType = i;
            this.wordWrap = z;
            this.printDirection = i2;
            this.scrollDirection = i3;
            this.justify = i4;
            this.effectDirection = i5;
            this.effectSpeed = i6;
            this.displayEffect = i7;
        }
    }

    /* renamed from: android.support.v4.media.subtitle.Cea708CCParser$Const */
    private static class Const {
        public static final int CODE_C0_BS = 8;
        public static final int CODE_C0_CR = 13;
        public static final int CODE_C0_ETX = 3;
        public static final int CODE_C0_EXT1 = 16;
        public static final int CODE_C0_FF = 12;
        public static final int CODE_C0_HCR = 14;
        public static final int CODE_C0_NUL = 0;
        public static final int CODE_C0_P16 = 24;
        public static final int CODE_C0_RANGE_END = 31;
        public static final int CODE_C0_RANGE_START = 0;
        public static final int CODE_C0_SKIP1_RANGE_END = 23;
        public static final int CODE_C0_SKIP1_RANGE_START = 16;
        public static final int CODE_C0_SKIP2_RANGE_END = 31;
        public static final int CODE_C0_SKIP2_RANGE_START = 24;
        public static final int CODE_C1_CLW = 136;
        public static final int CODE_C1_CW0 = 128;
        public static final int CODE_C1_CW1 = 129;
        public static final int CODE_C1_CW2 = 130;
        public static final int CODE_C1_CW3 = 131;
        public static final int CODE_C1_CW4 = 132;
        public static final int CODE_C1_CW5 = 133;
        public static final int CODE_C1_CW6 = 134;
        public static final int CODE_C1_CW7 = 135;
        public static final int CODE_C1_DF0 = 152;
        public static final int CODE_C1_DF1 = 153;
        public static final int CODE_C1_DF2 = 154;
        public static final int CODE_C1_DF3 = 155;
        public static final int CODE_C1_DF4 = 156;
        public static final int CODE_C1_DF5 = 157;
        public static final int CODE_C1_DF6 = 158;
        public static final int CODE_C1_DF7 = 159;
        public static final int CODE_C1_DLC = 142;
        public static final int CODE_C1_DLW = 140;
        public static final int CODE_C1_DLY = 141;
        public static final int CODE_C1_DSW = 137;
        public static final int CODE_C1_HDW = 138;
        public static final int CODE_C1_RANGE_END = 159;
        public static final int CODE_C1_RANGE_START = 128;
        public static final int CODE_C1_RST = 143;
        public static final int CODE_C1_SPA = 144;
        public static final int CODE_C1_SPC = 145;
        public static final int CODE_C1_SPL = 146;
        public static final int CODE_C1_SWA = 151;
        public static final int CODE_C1_TGW = 139;
        public static final int CODE_C2_RANGE_END = 31;
        public static final int CODE_C2_RANGE_START = 0;
        public static final int CODE_C2_SKIP0_RANGE_END = 7;
        public static final int CODE_C2_SKIP0_RANGE_START = 0;
        public static final int CODE_C2_SKIP1_RANGE_END = 15;
        public static final int CODE_C2_SKIP1_RANGE_START = 8;
        public static final int CODE_C2_SKIP2_RANGE_END = 23;
        public static final int CODE_C2_SKIP2_RANGE_START = 16;
        public static final int CODE_C2_SKIP3_RANGE_END = 31;
        public static final int CODE_C2_SKIP3_RANGE_START = 24;
        public static final int CODE_C3_RANGE_END = 159;
        public static final int CODE_C3_RANGE_START = 128;
        public static final int CODE_C3_SKIP4_RANGE_END = 135;
        public static final int CODE_C3_SKIP4_RANGE_START = 128;
        public static final int CODE_C3_SKIP5_RANGE_END = 143;
        public static final int CODE_C3_SKIP5_RANGE_START = 136;
        public static final int CODE_G0_MUSICNOTE = 127;
        public static final int CODE_G0_RANGE_END = 127;
        public static final int CODE_G0_RANGE_START = 32;
        public static final int CODE_G1_RANGE_END = 255;
        public static final int CODE_G1_RANGE_START = 160;
        public static final int CODE_G2_BLK = 48;
        public static final int CODE_G2_NBTSP = 33;
        public static final int CODE_G2_RANGE_END = 127;
        public static final int CODE_G2_RANGE_START = 32;
        public static final int CODE_G2_TSP = 32;
        public static final int CODE_G3_CC = 160;
        public static final int CODE_G3_RANGE_END = 255;
        public static final int CODE_G3_RANGE_START = 160;

        private Const() {
        }
    }

    /* renamed from: android.support.v4.media.subtitle.Cea708CCParser$DisplayListener */
    interface DisplayListener {
        void emitEvent(CaptionEvent captionEvent);
    }

    private int parseG3(byte[] bArr, int i) {
        return i;
    }

    Cea708CCParser(DisplayListener displayListener) {
        if (displayListener != null) {
            this.mListener = displayListener;
        }
    }

    private void emitCaptionEvent(CaptionEvent captionEvent) {
        emitCaptionBuffer();
        this.mListener.emitEvent(captionEvent);
    }

    private void emitCaptionBuffer() {
        if (this.mBuilder.length() > 0) {
            this.mListener.emitEvent(new CaptionEvent(1, this.mBuilder.toString()));
            this.mBuilder.setLength(0);
        }
    }

    public void parse(byte[] bArr) {
        int i = 0;
        while (i < bArr.length) {
            i = parseServiceBlockData(bArr, i);
        }
        emitCaptionBuffer();
    }

    private int parseServiceBlockData(byte[] bArr, int i) {
        this.mCommand = bArr[i] & 255;
        int i2 = i + 1;
        int i3 = this.mCommand;
        if (i3 == 16) {
            return parseExt1(bArr, i2);
        }
        if (i3 >= 0 && i3 <= 31) {
            return parseC0(bArr, i2);
        }
        int i4 = this.mCommand;
        if (i4 >= 128 && i4 <= 159) {
            return parseC1(bArr, i2);
        }
        int i5 = this.mCommand;
        if (i5 < 32 || i5 > 127) {
            int i6 = this.mCommand;
            if (i6 < 160 || i6 > 255) {
                return i2;
            }
            parseG1(bArr, i2);
            return i2;
        }
        parseG0(bArr, i2);
        return i2;
    }

    private int parseC0(byte[] bArr, int i) {
        int i2 = this.mCommand;
        if (i2 < 24 || i2 > 31) {
            int i3 = this.mCommand;
            if (i3 >= 16 && i3 <= 23) {
                return i + 1;
            }
            int i4 = this.mCommand;
            if (i4 == 0) {
                return i;
            }
            if (i4 == 3) {
                emitCaptionEvent(new CaptionEvent(2, Character.valueOf((char) i4)));
                return i;
            } else if (i4 != 8) {
                switch (i4) {
                    case 12:
                        emitCaptionEvent(new CaptionEvent(2, Character.valueOf((char) i4)));
                        return i;
                    case 13:
                        this.mBuilder.append(10);
                        return i;
                    case 14:
                        emitCaptionEvent(new CaptionEvent(2, Character.valueOf((char) i4)));
                        return i;
                    default:
                        return i;
                }
            } else {
                emitCaptionEvent(new CaptionEvent(2, Character.valueOf((char) i4)));
                return i;
            }
        } else {
            if (i2 == 24) {
                try {
                    if (bArr[i] == 0) {
                        this.mBuilder.append((char) bArr[i + 1]);
                    } else {
                        this.mBuilder.append(new String(Arrays.copyOfRange(bArr, i, i + 2), "EUC-KR"));
                    }
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "P16 Code - Could not find supported encoding", e);
                }
            }
            return i + 2;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
        return r27;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:?, code lost:
        return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int parseC1(byte[] r26, int r27) {
        /*
            r25 = this;
            r0 = r25
            int r1 = r0.mCommand
            r2 = 15
            r3 = 5
            r4 = 7
            r5 = 6
            r6 = 0
            r7 = 12
            r8 = 4
            r9 = 3
            r10 = 1
            switch(r1) {
                case 128: goto L_0x023b;
                case 129: goto L_0x023b;
                case 130: goto L_0x023b;
                case 131: goto L_0x023b;
                case 132: goto L_0x023b;
                case 133: goto L_0x023b;
                case 134: goto L_0x023b;
                case 135: goto L_0x023b;
                case 136: goto L_0x0228;
                case 137: goto L_0x0215;
                case 138: goto L_0x0202;
                case 139: goto L_0x01ef;
                case 140: goto L_0x01da;
                case 141: goto L_0x01c5;
                case 142: goto L_0x01b8;
                case 143: goto L_0x01ab;
                case 144: goto L_0x0169;
                case 145: goto L_0x0113;
                case 146: goto L_0x00f6;
                case 147: goto L_0x0012;
                case 148: goto L_0x0012;
                case 149: goto L_0x0012;
                case 150: goto L_0x0012;
                case 151: goto L_0x0080;
                case 152: goto L_0x0014;
                case 153: goto L_0x0014;
                case 154: goto L_0x0014;
                case 155: goto L_0x0014;
                case 156: goto L_0x0014;
                case 157: goto L_0x0014;
                case 158: goto L_0x0014;
                case 159: goto L_0x0014;
                default: goto L_0x0012;
            }
        L_0x0012:
            goto L_0x0249
        L_0x0014:
            int r12 = r1 + -152
            byte r1 = r26[r27]
            r1 = r1 & 32
            if (r1 == 0) goto L_0x001e
            r13 = r10
            goto L_0x001f
        L_0x001e:
            r13 = r6
        L_0x001f:
            byte r1 = r26[r27]
            r1 = r1 & 16
            if (r1 == 0) goto L_0x0027
            r14 = r10
            goto L_0x0028
        L_0x0027:
            r14 = r6
        L_0x0028:
            byte r1 = r26[r27]
            r1 = r1 & 8
            if (r1 == 0) goto L_0x0030
            r15 = r10
            goto L_0x0031
        L_0x0030:
            r15 = r6
        L_0x0031:
            byte r1 = r26[r27]
            r16 = r1 & 7
            int r1 = r27 + 1
            byte r3 = r26[r1]
            r3 = r3 & 128(0x80, float:1.794E-43)
            if (r3 == 0) goto L_0x0040
            r17 = r10
            goto L_0x0042
        L_0x0040:
            r17 = r6
        L_0x0042:
            byte r1 = r26[r1]
            r18 = r1 & 127(0x7f, float:1.78E-43)
            int r1 = r27 + 2
            byte r1 = r26[r1]
            r1 = r1 & 255(0xff, float:3.57E-43)
            int r3 = r27 + 3
            byte r5 = r26[r3]
            r5 = r5 & 240(0xf0, float:3.36E-43)
            int r20 = r5 >> 4
            byte r3 = r26[r3]
            r21 = r3 & 15
            int r2 = r27 + 4
            byte r2 = r26[r2]
            r22 = r2 & 63
            int r2 = r27 + 5
            byte r3 = r26[r2]
            r3 = r3 & 56
            int r24 = r3 >> 3
            byte r2 = r26[r2]
            r23 = r2 & 7
            int r2 = r27 + 6
            android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent r3 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent
            r4 = 16
            android.support.v4.media.subtitle.Cea708CCParser$CaptionWindow r5 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionWindow
            r11 = r5
            r19 = r1
            r11.<init>(r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, r23, r24)
            r3.<init>(r4, r5)
            r0.emitCaptionEvent(r3)
            goto L_0x024b
        L_0x0080:
            byte r1 = r26[r27]
            r1 = r1 & 192(0xc0, float:2.69E-43)
            int r1 = r1 >> r5
            byte r4 = r26[r27]
            r4 = r4 & 48
            int r4 = r4 >> r8
            byte r11 = r26[r27]
            r11 = r11 & r7
            int r11 = r11 >> 2
            byte r12 = r26[r27]
            r12 = r12 & r9
            android.support.v4.media.subtitle.Cea708CCParser$CaptionColor r14 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionColor
            r14.<init>(r1, r4, r11, r12)
            int r1 = r27 + 1
            byte r4 = r26[r1]
            r4 = r4 & 192(0xc0, float:2.69E-43)
            int r4 = r4 >> r5
            int r5 = r27 + 2
            byte r11 = r26[r5]
            r11 = r11 & 128(0x80, float:1.794E-43)
            int r3 = r11 >> 5
            r16 = r4 | r3
            byte r3 = r26[r1]
            r3 = r3 & 48
            int r3 = r3 >> r8
            byte r4 = r26[r1]
            r4 = r4 & r7
            int r4 = r4 >> 2
            byte r1 = r26[r1]
            r1 = r1 & r9
            android.support.v4.media.subtitle.Cea708CCParser$CaptionColor r15 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionColor
            r15.<init>(r6, r3, r4, r1)
            byte r1 = r26[r5]
            r1 = r1 & 64
            if (r1 == 0) goto L_0x00c3
            r17 = r10
            goto L_0x00c5
        L_0x00c3:
            r17 = r6
        L_0x00c5:
            byte r1 = r26[r5]
            r1 = r1 & 48
            int r18 = r1 >> 4
            byte r1 = r26[r5]
            r1 = r1 & r7
            int r19 = r1 >> 2
            byte r1 = r26[r5]
            r20 = r1 & 3
            int r1 = r27 + 3
            byte r3 = r26[r1]
            r3 = r3 & 240(0xf0, float:3.36E-43)
            int r22 = r3 >> 4
            byte r3 = r26[r1]
            r3 = r3 & r7
            int r21 = r3 >> 2
            byte r1 = r26[r1]
            r23 = r1 & 3
            int r1 = r27 + 4
            android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent r3 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent
            android.support.v4.media.subtitle.Cea708CCParser$CaptionWindowAttr r4 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionWindowAttr
            r13 = r4
            r13.<init>(r14, r15, r16, r17, r18, r19, r20, r21, r22, r23)
            r3.<init>(r2, r4)
            r0.emitCaptionEvent(r3)
            goto L_0x0166
        L_0x00f6:
            byte r1 = r26[r27]
            r1 = r1 & r2
            int r2 = r27 + 1
            byte r2 = r26[r2]
            r2 = r2 & 63
            int r3 = r27 + 2
            android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent r4 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent
            r5 = 14
            android.support.v4.media.subtitle.Cea708CCParser$CaptionPenLocation r6 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionPenLocation
            r6.<init>(r1, r2)
            r4.<init>(r5, r6)
            r0.emitCaptionEvent(r4)
            r2 = r3
            goto L_0x024b
        L_0x0113:
            byte r1 = r26[r27]
            r1 = r1 & 192(0xc0, float:2.69E-43)
            int r1 = r1 >> r5
            byte r2 = r26[r27]
            r2 = r2 & 48
            int r2 = r2 >> r8
            byte r3 = r26[r27]
            r3 = r3 & r7
            int r3 = r3 >> 2
            byte r4 = r26[r27]
            r4 = r4 & r9
            android.support.v4.media.subtitle.Cea708CCParser$CaptionColor r11 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionColor
            r11.<init>(r1, r2, r3, r4)
            int r1 = r27 + 1
            byte r2 = r26[r1]
            r2 = r2 & 192(0xc0, float:2.69E-43)
            int r2 = r2 >> r5
            byte r3 = r26[r1]
            r3 = r3 & 48
            int r3 = r3 >> r8
            byte r4 = r26[r1]
            r4 = r4 & r7
            int r4 = r4 >> 2
            byte r5 = r26[r1]
            r5 = r5 & r9
            android.support.v4.media.subtitle.Cea708CCParser$CaptionColor r12 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionColor
            r12.<init>(r2, r3, r4, r5)
            int r1 = r1 + r10
            byte r2 = r26[r1]
            r2 = r2 & 48
            int r2 = r2 >> r8
            byte r3 = r26[r1]
            r3 = r3 & r7
            int r3 = r3 >> 2
            byte r4 = r26[r1]
            r4 = r4 & r9
            android.support.v4.media.subtitle.Cea708CCParser$CaptionColor r5 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionColor
            r5.<init>(r6, r2, r3, r4)
            int r1 = r1 + r10
            android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent r2 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent
            r3 = 13
            android.support.v4.media.subtitle.Cea708CCParser$CaptionPenColor r4 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionPenColor
            r4.<init>(r11, r12, r5)
            r2.<init>(r3, r4)
            r0.emitCaptionEvent(r2)
        L_0x0166:
            r2 = r1
            goto L_0x024b
        L_0x0169:
            byte r1 = r26[r27]
            r1 = r1 & 240(0xf0, float:3.36E-43)
            int r14 = r1 >> 4
            byte r1 = r26[r27]
            r12 = r1 & 3
            byte r1 = r26[r27]
            r1 = r1 & r7
            int r13 = r1 >> 2
            int r1 = r27 + 1
            byte r2 = r26[r1]
            r2 = r2 & 128(0x80, float:1.794E-43)
            if (r2 == 0) goto L_0x0183
            r18 = r10
            goto L_0x0185
        L_0x0183:
            r18 = r6
        L_0x0185:
            byte r2 = r26[r1]
            r2 = r2 & 64
            if (r2 == 0) goto L_0x018e
            r17 = r10
            goto L_0x0190
        L_0x018e:
            r17 = r6
        L_0x0190:
            byte r2 = r26[r1]
            r2 = r2 & 56
            int r16 = r2 >> 3
            byte r1 = r26[r1]
            r15 = r1 & 7
            int r1 = r27 + 2
            android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent r2 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent
            android.support.v4.media.subtitle.Cea708CCParser$CaptionPenAttr r3 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionPenAttr
            r11 = r3
            r11.<init>(r12, r13, r14, r15, r16, r17, r18)
            r2.<init>(r7, r3)
            r0.emitCaptionEvent(r2)
            goto L_0x0166
        L_0x01ab:
            android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent r1 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent
            r2 = 11
            r3 = 0
            r1.<init>(r2, r3)
            r0.emitCaptionEvent(r1)
            goto L_0x0249
        L_0x01b8:
            android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent r1 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent
            r2 = 10
            r3 = 0
            r1.<init>(r2, r3)
            r0.emitCaptionEvent(r1)
            goto L_0x0249
        L_0x01c5:
            byte r1 = r26[r27]
            r1 = r1 & 255(0xff, float:3.57E-43)
            int r2 = r27 + 1
            android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent r3 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent
            r4 = 9
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r3.<init>(r4, r1)
            r0.emitCaptionEvent(r3)
            goto L_0x024b
        L_0x01da:
            byte r1 = r26[r27]
            r1 = r1 & 255(0xff, float:3.57E-43)
            int r2 = r27 + 1
            android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent r3 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent
            r4 = 8
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r3.<init>(r4, r1)
            r0.emitCaptionEvent(r3)
            goto L_0x024b
        L_0x01ef:
            byte r1 = r26[r27]
            r1 = r1 & 255(0xff, float:3.57E-43)
            int r2 = r27 + 1
            android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent r3 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r3.<init>(r4, r1)
            r0.emitCaptionEvent(r3)
            goto L_0x024b
        L_0x0202:
            byte r1 = r26[r27]
            r1 = r1 & 255(0xff, float:3.57E-43)
            int r2 = r27 + 1
            android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent r3 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r3.<init>(r5, r1)
            r0.emitCaptionEvent(r3)
            goto L_0x024b
        L_0x0215:
            byte r1 = r26[r27]
            r1 = r1 & 255(0xff, float:3.57E-43)
            int r2 = r27 + 1
            android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent r4 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r4.<init>(r3, r1)
            r0.emitCaptionEvent(r4)
            goto L_0x024b
        L_0x0228:
            byte r1 = r26[r27]
            r1 = r1 & 255(0xff, float:3.57E-43)
            int r2 = r27 + 1
            android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent r3 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r3.<init>(r8, r1)
            r0.emitCaptionEvent(r3)
            goto L_0x024b
        L_0x023b:
            int r1 = r1 + -128
            android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent r2 = new android.support.v4.media.subtitle.Cea708CCParser$CaptionEvent
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r2.<init>(r9, r1)
            r0.emitCaptionEvent(r2)
        L_0x0249:
            r2 = r27
        L_0x024b:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.subtitle.Cea708CCParser.parseC1(byte[], int):int");
    }

    private int parseG0(byte[] bArr, int i) {
        int i2 = this.mCommand;
        if (i2 == 127) {
            this.mBuilder.append(MUSIC_NOTE_CHAR);
        } else {
            this.mBuilder.append((char) i2);
        }
        return i;
    }

    private int parseG1(byte[] bArr, int i) {
        this.mBuilder.append((char) this.mCommand);
        return i;
    }

    private int parseExt1(byte[] bArr, int i) {
        this.mCommand = bArr[i] & 255;
        int i2 = i + 1;
        int i3 = this.mCommand;
        if (i3 >= 0 && i3 <= 31) {
            return parseC2(bArr, i2);
        }
        int i4 = this.mCommand;
        if (i4 >= 128 && i4 <= 159) {
            return parseC3(bArr, i2);
        }
        int i5 = this.mCommand;
        if (i5 < 32 || i5 > 127) {
            int i6 = this.mCommand;
            if (i6 < 160 || i6 > 255) {
                return i2;
            }
            parseG3(bArr, i2);
            return i2;
        }
        parseG2(bArr, i2);
        return i2;
    }

    private int parseC2(byte[] bArr, int i) {
        int i2 = this.mCommand;
        if (i2 >= 0 && i2 <= 7) {
            return i;
        }
        int i3 = this.mCommand;
        if (i3 >= 8 && i3 <= 15) {
            return i + 1;
        }
        int i4 = this.mCommand;
        if (i4 >= 16 && i4 <= 23) {
            return i + 2;
        }
        int i5 = this.mCommand;
        return (i5 < 24 || i5 > 31) ? i : i + 3;
    }

    private int parseC3(byte[] bArr, int i) {
        int i2 = this.mCommand;
        if (i2 >= 128 && i2 <= 135) {
            return i + 4;
        }
        int i3 = this.mCommand;
        return (i3 < 136 || i3 > 143) ? i : i + 5;
    }

    private int parseG2(byte[] bArr, int i) {
        int i2 = this.mCommand;
        if (!(i2 == 32 || i2 == 33)) {
        }
        return i;
    }
}
