package com.adobe.xmp.impl;

import com.adobe.xmp.XMPDateTime;
import com.adobe.xmp.XMPException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class ISO8601Converter {
    private ISO8601Converter() {
    }

    public static XMPDateTime parse(String str) throws XMPException {
        XMPDateTimeImpl xMPDateTimeImpl = new XMPDateTimeImpl();
        parse(str, xMPDateTimeImpl);
        return xMPDateTimeImpl;
    }

    /* JADX WARNING: Removed duplicated region for block: B:118:0x0209 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x020a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.adobe.xmp.XMPDateTime parse(java.lang.String r13, com.adobe.xmp.XMPDateTime r14) throws com.adobe.xmp.XMPException {
        /*
            com.adobe.xmp.impl.ParameterAsserts.assertNotNull(r13)
            com.adobe.xmp.impl.ParseState r0 = new com.adobe.xmp.impl.ParseState
            r0.<init>(r13)
            r13 = 0
            char r1 = r0.mo5203ch(r13)
            r2 = 84
            r3 = 58
            r4 = 1
            if (r1 == r2) goto L_0x0031
            int r1 = r0.length()
            r5 = 2
            if (r1 < r5) goto L_0x0021
            char r1 = r0.mo5203ch(r4)
            if (r1 == r3) goto L_0x0031
        L_0x0021:
            int r1 = r0.length()
            r6 = 3
            if (r1 < r6) goto L_0x002f
            char r1 = r0.mo5203ch(r5)
            if (r1 != r3) goto L_0x002f
            goto L_0x0031
        L_0x002f:
            r1 = r13
            goto L_0x0032
        L_0x0031:
            r1 = r4
        L_0x0032:
            r5 = 45
            r6 = 5
            if (r1 != 0) goto L_0x00c2
            char r7 = r0.mo5203ch(r13)
            if (r7 != r5) goto L_0x0040
            r0.skip()
        L_0x0040:
            r7 = 9999(0x270f, float:1.4012E-41)
            java.lang.String r8 = "Invalid year in date string"
            int r7 = r0.gatherInt(r8, r7)
            boolean r8 = r0.hasNext()
            if (r8 == 0) goto L_0x005d
            char r8 = r0.mo5202ch()
            if (r8 != r5) goto L_0x0055
            goto L_0x005d
        L_0x0055:
            com.adobe.xmp.XMPException r13 = new com.adobe.xmp.XMPException
            java.lang.String r14 = "Invalid date string, after year"
            r13.<init>(r14, r6)
            throw r13
        L_0x005d:
            char r8 = r0.mo5203ch(r13)
            if (r8 != r5) goto L_0x0064
            int r7 = -r7
        L_0x0064:
            r14.setYear(r7)
            boolean r7 = r0.hasNext()
            if (r7 != 0) goto L_0x006e
            return r14
        L_0x006e:
            r0.skip()
            r7 = 12
            java.lang.String r8 = "Invalid month in date string"
            int r7 = r0.gatherInt(r8, r7)
            boolean r8 = r0.hasNext()
            if (r8 == 0) goto L_0x008e
            char r8 = r0.mo5202ch()
            if (r8 != r5) goto L_0x0086
            goto L_0x008e
        L_0x0086:
            com.adobe.xmp.XMPException r13 = new com.adobe.xmp.XMPException
            java.lang.String r14 = "Invalid date string, after month"
            r13.<init>(r14, r6)
            throw r13
        L_0x008e:
            r14.setMonth(r7)
            boolean r7 = r0.hasNext()
            if (r7 != 0) goto L_0x0098
            return r14
        L_0x0098:
            r0.skip()
            r7 = 31
            java.lang.String r8 = "Invalid day in date string"
            int r7 = r0.gatherInt(r8, r7)
            boolean r8 = r0.hasNext()
            if (r8 == 0) goto L_0x00b8
            char r8 = r0.mo5202ch()
            if (r8 != r2) goto L_0x00b0
            goto L_0x00b8
        L_0x00b0:
            com.adobe.xmp.XMPException r13 = new com.adobe.xmp.XMPException
            java.lang.String r14 = "Invalid date string, after day"
            r13.<init>(r14, r6)
            throw r13
        L_0x00b8:
            r14.setDay(r7)
            boolean r7 = r0.hasNext()
            if (r7 != 0) goto L_0x00c8
            return r14
        L_0x00c2:
            r14.setMonth(r4)
            r14.setDay(r4)
        L_0x00c8:
            char r7 = r0.mo5202ch()
            if (r7 != r2) goto L_0x00d2
            r0.skip()
            goto L_0x00d4
        L_0x00d2:
            if (r1 == 0) goto L_0x021a
        L_0x00d4:
            r1 = 23
            java.lang.String r2 = "Invalid hour in date string"
            int r2 = r0.gatherInt(r2, r1)
            char r7 = r0.mo5202ch()
            if (r7 != r3) goto L_0x0212
            r14.setHour(r2)
            r0.skip()
            r2 = 59
            java.lang.String r7 = "Invalid minute in date string"
            int r7 = r0.gatherInt(r7, r2)
            boolean r8 = r0.hasNext()
            r9 = 43
            r10 = 90
            if (r8 == 0) goto L_0x011b
            char r8 = r0.mo5202ch()
            if (r8 == r3) goto L_0x011b
            char r8 = r0.mo5202ch()
            if (r8 == r10) goto L_0x011b
            char r8 = r0.mo5202ch()
            if (r8 == r9) goto L_0x011b
            char r8 = r0.mo5202ch()
            if (r8 != r5) goto L_0x0113
            goto L_0x011b
        L_0x0113:
            com.adobe.xmp.XMPException r13 = new com.adobe.xmp.XMPException
            java.lang.String r14 = "Invalid date string, after minute"
            r13.<init>(r14, r6)
            throw r13
        L_0x011b:
            r14.setMinute(r7)
            char r7 = r0.mo5202ch()
            if (r7 != r3) goto L_0x01a2
            r0.skip()
            java.lang.String r7 = "Invalid whole seconds in date string"
            int r7 = r0.gatherInt(r7, r2)
            boolean r8 = r0.hasNext()
            r11 = 46
            if (r8 == 0) goto L_0x0156
            char r8 = r0.mo5202ch()
            if (r8 == r11) goto L_0x0156
            char r8 = r0.mo5202ch()
            if (r8 == r10) goto L_0x0156
            char r8 = r0.mo5202ch()
            if (r8 == r9) goto L_0x0156
            char r8 = r0.mo5202ch()
            if (r8 != r5) goto L_0x014e
            goto L_0x0156
        L_0x014e:
            com.adobe.xmp.XMPException r13 = new com.adobe.xmp.XMPException
            java.lang.String r14 = "Invalid date string, after whole seconds"
            r13.<init>(r14, r6)
            throw r13
        L_0x0156:
            r14.setSecond(r7)
            char r7 = r0.mo5202ch()
            if (r7 != r11) goto L_0x01a2
            r0.skip()
            int r7 = r0.pos()
            r8 = 999999999(0x3b9ac9ff, float:0.004723787)
            java.lang.String r11 = "Invalid fractional seconds in date string"
            int r8 = r0.gatherInt(r11, r8)
            char r11 = r0.mo5202ch()
            if (r11 == r10) goto L_0x018a
            char r11 = r0.mo5202ch()
            if (r11 == r9) goto L_0x018a
            char r11 = r0.mo5202ch()
            if (r11 != r5) goto L_0x0182
            goto L_0x018a
        L_0x0182:
            com.adobe.xmp.XMPException r13 = new com.adobe.xmp.XMPException
            java.lang.String r14 = "Invalid date string, after fractional second"
            r13.<init>(r14, r6)
            throw r13
        L_0x018a:
            int r11 = r0.pos()
            int r11 = r11 - r7
        L_0x018f:
            r7 = 9
            if (r11 <= r7) goto L_0x0198
            int r8 = r8 / 10
            int r11 = r11 + -1
            goto L_0x018f
        L_0x0198:
            if (r11 >= r7) goto L_0x019f
            int r8 = r8 * 10
            int r11 = r11 + 1
            goto L_0x0198
        L_0x019f:
            r14.setNanoSecond(r8)
        L_0x01a2:
            char r7 = r0.mo5202ch()
            if (r7 != r10) goto L_0x01ac
            r0.skip()
            goto L_0x01ed
        L_0x01ac:
            boolean r7 = r0.hasNext()
            if (r7 == 0) goto L_0x01ed
            char r13 = r0.mo5202ch()
            if (r13 != r9) goto L_0x01ba
            r13 = r4
            goto L_0x01c1
        L_0x01ba:
            char r13 = r0.mo5202ch()
            if (r13 != r5) goto L_0x01e5
            r13 = -1
        L_0x01c1:
            r0.skip()
            java.lang.String r4 = "Invalid time zone hour in date string"
            int r1 = r0.gatherInt(r4, r1)
            char r4 = r0.mo5202ch()
            if (r4 != r3) goto L_0x01dd
            r0.skip()
            java.lang.String r3 = "Invalid time zone minute in date string"
            int r2 = r0.gatherInt(r3, r2)
            r12 = r1
            r1 = r13
            r13 = r12
            goto L_0x01ef
        L_0x01dd:
            com.adobe.xmp.XMPException r13 = new com.adobe.xmp.XMPException
            java.lang.String r14 = "Invalid date string, after time zone hour"
            r13.<init>(r14, r6)
            throw r13
        L_0x01e5:
            com.adobe.xmp.XMPException r13 = new com.adobe.xmp.XMPException
            java.lang.String r14 = "Time zone must begin with 'Z', '+', or '-'"
            r13.<init>(r14, r6)
            throw r13
        L_0x01ed:
            r1 = r13
            r2 = r1
        L_0x01ef:
            int r13 = r13 * 3600
            int r13 = r13 * 1000
            int r2 = r2 * 60
            int r2 = r2 * 1000
            int r13 = r13 + r2
            int r13 = r13 * r1
            java.util.SimpleTimeZone r1 = new java.util.SimpleTimeZone
            java.lang.String r2 = ""
            r1.<init>(r13, r2)
            r14.setTimeZone(r1)
            boolean r13 = r0.hasNext()
            if (r13 != 0) goto L_0x020a
            return r14
        L_0x020a:
            com.adobe.xmp.XMPException r13 = new com.adobe.xmp.XMPException
            java.lang.String r14 = "Invalid date string, extra chars at end"
            r13.<init>(r14, r6)
            throw r13
        L_0x0212:
            com.adobe.xmp.XMPException r13 = new com.adobe.xmp.XMPException
            java.lang.String r14 = "Invalid date string, after hour"
            r13.<init>(r14, r6)
            throw r13
        L_0x021a:
            com.adobe.xmp.XMPException r13 = new com.adobe.xmp.XMPException
            java.lang.String r14 = "Invalid date string, missing 'T' after date"
            r13.<init>(r14, r6)
            throw r13
        */
        throw new UnsupportedOperationException("Method not decompiled: com.adobe.xmp.impl.ISO8601Converter.parse(java.lang.String, com.adobe.xmp.XMPDateTime):com.adobe.xmp.XMPDateTime");
    }

    public static String render(XMPDateTime xMPDateTime) {
        StringBuffer stringBuffer = new StringBuffer();
        DecimalFormat decimalFormat = new DecimalFormat("0000", new DecimalFormatSymbols(Locale.ENGLISH));
        stringBuffer.append(decimalFormat.format((long) xMPDateTime.getYear()));
        if (xMPDateTime.getMonth() == 0) {
            return stringBuffer.toString();
        }
        decimalFormat.applyPattern("'-'00");
        stringBuffer.append(decimalFormat.format((long) xMPDateTime.getMonth()));
        if (xMPDateTime.getDay() == 0) {
            return stringBuffer.toString();
        }
        stringBuffer.append(decimalFormat.format((long) xMPDateTime.getDay()));
        if (!(xMPDateTime.getHour() == 0 && xMPDateTime.getMinute() == 0 && xMPDateTime.getSecond() == 0 && xMPDateTime.getNanoSecond() == 0 && (xMPDateTime.getTimeZone() == null || xMPDateTime.getTimeZone().getRawOffset() == 0))) {
            stringBuffer.append('T');
            decimalFormat.applyPattern("00");
            stringBuffer.append(decimalFormat.format((long) xMPDateTime.getHour()));
            stringBuffer.append(':');
            stringBuffer.append(decimalFormat.format((long) xMPDateTime.getMinute()));
            if (!(xMPDateTime.getSecond() == 0 && xMPDateTime.getNanoSecond() == 0)) {
                double second = ((double) xMPDateTime.getSecond()) + (((double) xMPDateTime.getNanoSecond()) / 1.0E9d);
                decimalFormat.applyPattern(":00.#########");
                stringBuffer.append(decimalFormat.format(second));
            }
            if (xMPDateTime.getTimeZone() != null) {
                int offset = xMPDateTime.getTimeZone().getOffset(xMPDateTime.getCalendar().getTimeInMillis());
                if (offset == 0) {
                    stringBuffer.append('Z');
                } else {
                    int i = offset / 3600000;
                    int abs = Math.abs((offset % 3600000) / 60000);
                    decimalFormat.applyPattern("+00;-00");
                    stringBuffer.append(decimalFormat.format((long) i));
                    decimalFormat.applyPattern(":00");
                    stringBuffer.append(decimalFormat.format((long) abs));
                }
            }
        }
        return stringBuffer.toString();
    }
}
