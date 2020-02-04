package com.adobe.xmp.impl;

import com.adobe.xmp.XMPConst;
import com.adobe.xmp.XMPDateTime;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPPathFactory;
import com.adobe.xmp.XMPUtils;
import com.adobe.xmp.impl.xpath.XMPPath;
import com.adobe.xmp.impl.xpath.XMPPathParser;
import com.adobe.xmp.options.IteratorOptions;
import com.adobe.xmp.options.ParseOptions;
import com.adobe.xmp.options.PropertyOptions;
import com.adobe.xmp.properties.XMPProperty;
import com.asus.scenedetectlib.BuildConfig;
import java.util.Calendar;

public class XMPMetaImpl implements XMPMeta, XMPConst {
    private static final int VALUE_BASE64 = 7;
    private static final int VALUE_BOOLEAN = 1;
    private static final int VALUE_CALENDAR = 6;
    private static final int VALUE_DATE = 5;
    private static final int VALUE_DOUBLE = 4;
    private static final int VALUE_INTEGER = 2;
    private static final int VALUE_LONG = 3;
    private static final int VALUE_STRING = 0;
    private String packetHeader;
    private XMPNode tree;

    public XMPMetaImpl() {
        this.packetHeader = null;
        this.tree = new XMPNode(null, null, null);
    }

    public XMPMetaImpl(XMPNode xMPNode) {
        this.packetHeader = null;
        this.tree = xMPNode;
    }

    public void appendArrayItem(String str, String str2, PropertyOptions propertyOptions, String str3, PropertyOptions propertyOptions2) throws XMPException {
        ParameterAsserts.assertSchemaNS(str);
        ParameterAsserts.assertArrayName(str2);
        if (propertyOptions == null) {
            propertyOptions = new PropertyOptions();
        }
        if (propertyOptions.isOnlyArrayOptions()) {
            PropertyOptions verifySetOptions = XMPNodeUtils.verifySetOptions(propertyOptions, null);
            XMPPath expandXPath = XMPPathParser.expandXPath(str, str2);
            XMPNode findNode = XMPNodeUtils.findNode(this.tree, expandXPath, false, null);
            if (findNode != null) {
                if (!findNode.getOptions().isArray()) {
                    throw new XMPException("The named property is not an array", 102);
                }
            } else if (verifySetOptions.isArray()) {
                findNode = XMPNodeUtils.findNode(this.tree, expandXPath, true, verifySetOptions);
                if (findNode == null) {
                    throw new XMPException("Failure creating array node", 102);
                }
            } else {
                throw new XMPException("Explicit arrayOptions required to create new array", 103);
            }
            doSetArrayItem(findNode, -1, str3, propertyOptions2, true);
            return;
        }
        throw new XMPException("Only array form flags allowed for arrayOptions", 103);
    }

    public void appendArrayItem(String str, String str2, String str3) throws XMPException {
        appendArrayItem(str, str2, null, str3, null);
    }

    public int countArrayItems(String str, String str2) throws XMPException {
        ParameterAsserts.assertSchemaNS(str);
        ParameterAsserts.assertArrayName(str2);
        XMPNode findNode = XMPNodeUtils.findNode(this.tree, XMPPathParser.expandXPath(str, str2), false, null);
        if (findNode == null) {
            return 0;
        }
        if (findNode.getOptions().isArray()) {
            return findNode.getChildrenLength();
        }
        throw new XMPException("The named property is not an array", 102);
    }

    public void deleteArrayItem(String str, String str2, int i) {
        try {
            ParameterAsserts.assertSchemaNS(str);
            ParameterAsserts.assertArrayName(str2);
            deleteProperty(str, XMPPathFactory.composeArrayItemPath(str2, i));
        } catch (XMPException unused) {
        }
    }

    public void deleteProperty(String str, String str2) {
        try {
            ParameterAsserts.assertSchemaNS(str);
            ParameterAsserts.assertPropName(str2);
            XMPNode findNode = XMPNodeUtils.findNode(this.tree, XMPPathParser.expandXPath(str, str2), false, null);
            if (findNode != null) {
                XMPNodeUtils.deleteNode(findNode);
            }
        } catch (XMPException unused) {
        }
    }

    public void deleteQualifier(String str, String str2, String str3, String str4) {
        try {
            ParameterAsserts.assertSchemaNS(str);
            ParameterAsserts.assertPropName(str2);
            StringBuilder sb = new StringBuilder();
            sb.append(str2);
            sb.append(XMPPathFactory.composeQualifierPath(str3, str4));
            deleteProperty(str, sb.toString());
        } catch (XMPException unused) {
        }
    }

    public void deleteStructField(String str, String str2, String str3, String str4) {
        try {
            ParameterAsserts.assertSchemaNS(str);
            ParameterAsserts.assertStructName(str2);
            StringBuilder sb = new StringBuilder();
            sb.append(str2);
            sb.append(XMPPathFactory.composeStructFieldPath(str3, str4));
            deleteProperty(str, sb.toString());
        } catch (XMPException unused) {
        }
    }

    public boolean doesPropertyExist(String str, String str2) {
        try {
            ParameterAsserts.assertSchemaNS(str);
            ParameterAsserts.assertPropName(str2);
            if (XMPNodeUtils.findNode(this.tree, XMPPathParser.expandXPath(str, str2), false, null) != null) {
                return true;
            }
            return false;
        } catch (XMPException unused) {
            return false;
        }
    }

    public boolean doesArrayItemExist(String str, String str2, int i) {
        try {
            ParameterAsserts.assertSchemaNS(str);
            ParameterAsserts.assertArrayName(str2);
            return doesPropertyExist(str, XMPPathFactory.composeArrayItemPath(str2, i));
        } catch (XMPException unused) {
            return false;
        }
    }

    public boolean doesStructFieldExist(String str, String str2, String str3, String str4) {
        try {
            ParameterAsserts.assertSchemaNS(str);
            ParameterAsserts.assertStructName(str2);
            String composeStructFieldPath = XMPPathFactory.composeStructFieldPath(str3, str4);
            StringBuilder sb = new StringBuilder();
            sb.append(str2);
            sb.append(composeStructFieldPath);
            return doesPropertyExist(str, sb.toString());
        } catch (XMPException unused) {
            return false;
        }
    }

    public boolean doesQualifierExist(String str, String str2, String str3, String str4) {
        try {
            ParameterAsserts.assertSchemaNS(str);
            ParameterAsserts.assertPropName(str2);
            String composeQualifierPath = XMPPathFactory.composeQualifierPath(str3, str4);
            StringBuilder sb = new StringBuilder();
            sb.append(str2);
            sb.append(composeQualifierPath);
            return doesPropertyExist(str, sb.toString());
        } catch (XMPException unused) {
            return false;
        }
    }

    public XMPProperty getArrayItem(String str, String str2, int i) throws XMPException {
        ParameterAsserts.assertSchemaNS(str);
        ParameterAsserts.assertArrayName(str2);
        return getProperty(str, XMPPathFactory.composeArrayItemPath(str2, i));
    }

    public XMPProperty getLocalizedText(String str, String str2, String str3, String str4) throws XMPException {
        ParameterAsserts.assertSchemaNS(str);
        ParameterAsserts.assertArrayName(str2);
        ParameterAsserts.assertSpecificLang(str4);
        String normalizeLangValue = str3 != null ? Utils.normalizeLangValue(str3) : null;
        String normalizeLangValue2 = Utils.normalizeLangValue(str4);
        XMPNode findNode = XMPNodeUtils.findNode(this.tree, XMPPathParser.expandXPath(str, str2), false, null);
        if (findNode == null) {
            return null;
        }
        Object[] chooseLocalizedText = XMPNodeUtils.chooseLocalizedText(findNode, normalizeLangValue, normalizeLangValue2);
        int intValue = ((Integer) chooseLocalizedText[0]).intValue();
        final XMPNode xMPNode = (XMPNode) chooseLocalizedText[1];
        if (intValue != 0) {
            return new XMPProperty() {
                public Object getValue() {
                    return xMPNode.getValue();
                }

                public PropertyOptions getOptions() {
                    return xMPNode.getOptions();
                }

                public String getLanguage() {
                    return xMPNode.getQualifier(1).getValue();
                }

                public String toString() {
                    return xMPNode.getValue().toString();
                }
            };
        }
        return null;
    }

    /* JADX INFO: used method not loaded: com.adobe.xmp.impl.XMPNode.removeChild(com.adobe.xmp.impl.XMPNode):null, types can be incorrect */
    /* JADX WARNING: Code restructure failed: missing block: B:102:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0099, code lost:
        if (r1 == null) goto L_0x00a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x009f, code lost:
        if (r6.getChildrenLength() <= 1) goto L_0x00a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00a1, code lost:
        r6.removeChild(r1);
        r6.addChild(1, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00a7, code lost:
        r8 = com.adobe.xmp.impl.XMPNodeUtils.chooseLocalizedText(r6, r9, r10);
        r9 = ((java.lang.Integer) r8[0]).intValue();
        r8 = (com.adobe.xmp.impl.XMPNode) r8[1];
        r2 = r3.equals(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00bb, code lost:
        if (r9 == 0) goto L_0x015a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00bd, code lost:
        if (r9 == 1) goto L_0x010e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00c0, code lost:
        if (r9 == 2) goto L_0x00f3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00c3, code lost:
        if (r9 == 3) goto L_0x00ec;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00c6, code lost:
        if (r9 == 4) goto L_0x00dc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00c9, code lost:
        if (r9 != 5) goto L_0x00d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00cb, code lost:
        com.adobe.xmp.impl.XMPNodeUtils.appendLangItem(r6, r10, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00ce, code lost:
        if (r2 == false) goto L_0x0163;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00db, code lost:
        throw new com.adobe.xmp.XMPException("Unexpected result from ChooseLocalizedText", 9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00dc, code lost:
        if (r1 == null) goto L_0x00e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00e2, code lost:
        if (r6.getChildrenLength() != 1) goto L_0x00e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00e4, code lost:
        r1.setValue(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00e7, code lost:
        com.adobe.xmp.impl.XMPNodeUtils.appendLangItem(r6, r10, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00ec, code lost:
        com.adobe.xmp.impl.XMPNodeUtils.appendLangItem(r6, r10, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00ef, code lost:
        if (r2 == false) goto L_0x0163;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00f3, code lost:
        if (r7 == false) goto L_0x010a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00f5, code lost:
        if (r1 == r8) goto L_0x010a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00f7, code lost:
        if (r1 == null) goto L_0x010a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0105, code lost:
        if (r1.getValue().equals(r8.getValue()) == false) goto L_0x010a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0107, code lost:
        r1.setValue(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x010a, code lost:
        r8.setValue(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x010e, code lost:
        if (r2 != false) goto L_0x012b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0110, code lost:
        if (r7 == false) goto L_0x0127;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0112, code lost:
        if (r1 == r8) goto L_0x0127;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0114, code lost:
        if (r1 == null) goto L_0x0127;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0122, code lost:
        if (r1.getValue().equals(r8.getValue()) == false) goto L_0x0127;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0124, code lost:
        r1.setValue(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0127, code lost:
        r8.setValue(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x012b, code lost:
        r8 = r6.iterateChildren();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0133, code lost:
        if (r8.hasNext() == false) goto L_0x0154;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0135, code lost:
        r9 = (com.adobe.xmp.impl.XMPNode) r8.next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x013b, code lost:
        if (r9 == r1) goto L_0x012f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x013d, code lost:
        r10 = r9.getValue();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0141, code lost:
        if (r1 == null) goto L_0x0148;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0143, code lost:
        r2 = r1.getValue();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0148, code lost:
        r2 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x014d, code lost:
        if (r10.equals(r2) != false) goto L_0x0150;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0150, code lost:
        r9.setValue(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0154, code lost:
        if (r1 == null) goto L_0x0163;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0156, code lost:
        r1.setValue(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x015a, code lost:
        com.adobe.xmp.impl.XMPNodeUtils.appendLangItem(r6, r3, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x015d, code lost:
        if (r2 != false) goto L_0x0162;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x015f, code lost:
        com.adobe.xmp.impl.XMPNodeUtils.appendLangItem(r6, r10, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x0162, code lost:
        r7 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0163, code lost:
        if (r7 != false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x0169, code lost:
        if (r6.getChildrenLength() != 1) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x016b, code lost:
        com.adobe.xmp.impl.XMPNodeUtils.appendLangItem(r6, r3, r11);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setLocalizedText(java.lang.String r7, java.lang.String r8, java.lang.String r9, java.lang.String r10, java.lang.String r11, com.adobe.xmp.options.PropertyOptions r12) throws com.adobe.xmp.XMPException {
        /*
            r6 = this;
            com.adobe.xmp.impl.ParameterAsserts.assertSchemaNS(r7)
            com.adobe.xmp.impl.ParameterAsserts.assertArrayName(r8)
            com.adobe.xmp.impl.ParameterAsserts.assertSpecificLang(r10)
            r12 = 0
            if (r9 == 0) goto L_0x0011
            java.lang.String r9 = com.adobe.xmp.impl.Utils.normalizeLangValue(r9)
            goto L_0x0012
        L_0x0011:
            r9 = r12
        L_0x0012:
            java.lang.String r10 = com.adobe.xmp.impl.Utils.normalizeLangValue(r10)
            com.adobe.xmp.impl.xpath.XMPPath r7 = com.adobe.xmp.impl.xpath.XMPPathParser.expandXPath(r7, r8)
            com.adobe.xmp.impl.XMPNode r6 = r6.tree
            com.adobe.xmp.options.PropertyOptions r8 = new com.adobe.xmp.options.PropertyOptions
            r0 = 7680(0x1e00, float:1.0762E-41)
            r8.<init>(r0)
            r0 = 1
            com.adobe.xmp.impl.XMPNode r6 = com.adobe.xmp.impl.XMPNodeUtils.findNode(r6, r7, r0, r8)
            r7 = 102(0x66, float:1.43E-43)
            if (r6 == 0) goto L_0x016f
            com.adobe.xmp.options.PropertyOptions r8 = r6.getOptions()
            boolean r8 = r8.isArrayAltText()
            if (r8 != 0) goto L_0x0056
            boolean r8 = r6.hasChildren()
            if (r8 != 0) goto L_0x004e
            com.adobe.xmp.options.PropertyOptions r8 = r6.getOptions()
            boolean r8 = r8.isArrayAlternate()
            if (r8 == 0) goto L_0x004e
            com.adobe.xmp.options.PropertyOptions r8 = r6.getOptions()
            r8.setArrayAltText(r0)
            goto L_0x0056
        L_0x004e:
            com.adobe.xmp.XMPException r6 = new com.adobe.xmp.XMPException
            java.lang.String r8 = "Specified property is no alt-text array"
            r6.<init>(r8, r7)
            throw r6
        L_0x0056:
            java.util.Iterator r8 = r6.iterateChildren()
        L_0x005a:
            boolean r1 = r8.hasNext()
            r2 = 0
            java.lang.String r3 = "x-default"
            if (r1 == 0) goto L_0x0097
            java.lang.Object r1 = r8.next()
            com.adobe.xmp.impl.XMPNode r1 = (com.adobe.xmp.impl.XMPNode) r1
            boolean r4 = r1.hasQualifier()
            if (r4 == 0) goto L_0x008f
            com.adobe.xmp.impl.XMPNode r4 = r1.getQualifier(r0)
            java.lang.String r4 = r4.getName()
            java.lang.String r5 = "xml:lang"
            boolean r4 = r5.equals(r4)
            if (r4 == 0) goto L_0x008f
            com.adobe.xmp.impl.XMPNode r4 = r1.getQualifier(r0)
            java.lang.String r4 = r4.getValue()
            boolean r4 = r3.equals(r4)
            if (r4 == 0) goto L_0x005a
            r7 = r0
            goto L_0x0099
        L_0x008f:
            com.adobe.xmp.XMPException r6 = new com.adobe.xmp.XMPException
            java.lang.String r8 = "Language qualifier must be first"
            r6.<init>(r8, r7)
            throw r6
        L_0x0097:
            r1 = r12
            r7 = r2
        L_0x0099:
            if (r1 == 0) goto L_0x00a7
            int r8 = r6.getChildrenLength()
            if (r8 <= r0) goto L_0x00a7
            r6.removeChild(r1)
            r6.addChild(r0, r1)
        L_0x00a7:
            java.lang.Object[] r8 = com.adobe.xmp.impl.XMPNodeUtils.chooseLocalizedText(r6, r9, r10)
            r9 = r8[r2]
            java.lang.Integer r9 = (java.lang.Integer) r9
            int r9 = r9.intValue()
            r8 = r8[r0]
            com.adobe.xmp.impl.XMPNode r8 = (com.adobe.xmp.impl.XMPNode) r8
            boolean r2 = r3.equals(r10)
            if (r9 == 0) goto L_0x015a
            if (r9 == r0) goto L_0x010e
            r12 = 2
            if (r9 == r12) goto L_0x00f3
            r8 = 3
            if (r9 == r8) goto L_0x00ec
            r8 = 4
            if (r9 == r8) goto L_0x00dc
            r8 = 5
            if (r9 != r8) goto L_0x00d2
            com.adobe.xmp.impl.XMPNodeUtils.appendLangItem(r6, r10, r11)
            if (r2 == 0) goto L_0x0163
            goto L_0x0162
        L_0x00d2:
            com.adobe.xmp.XMPException r6 = new com.adobe.xmp.XMPException
            r7 = 9
            java.lang.String r8 = "Unexpected result from ChooseLocalizedText"
            r6.<init>(r8, r7)
            throw r6
        L_0x00dc:
            if (r1 == 0) goto L_0x00e7
            int r8 = r6.getChildrenLength()
            if (r8 != r0) goto L_0x00e7
            r1.setValue(r11)
        L_0x00e7:
            com.adobe.xmp.impl.XMPNodeUtils.appendLangItem(r6, r10, r11)
            goto L_0x0163
        L_0x00ec:
            com.adobe.xmp.impl.XMPNodeUtils.appendLangItem(r6, r10, r11)
            if (r2 == 0) goto L_0x0163
            goto L_0x0162
        L_0x00f3:
            if (r7 == 0) goto L_0x010a
            if (r1 == r8) goto L_0x010a
            if (r1 == 0) goto L_0x010a
            java.lang.String r9 = r1.getValue()
            java.lang.String r10 = r8.getValue()
            boolean r9 = r9.equals(r10)
            if (r9 == 0) goto L_0x010a
            r1.setValue(r11)
        L_0x010a:
            r8.setValue(r11)
            goto L_0x0163
        L_0x010e:
            if (r2 != 0) goto L_0x012b
            if (r7 == 0) goto L_0x0127
            if (r1 == r8) goto L_0x0127
            if (r1 == 0) goto L_0x0127
            java.lang.String r9 = r1.getValue()
            java.lang.String r10 = r8.getValue()
            boolean r9 = r9.equals(r10)
            if (r9 == 0) goto L_0x0127
            r1.setValue(r11)
        L_0x0127:
            r8.setValue(r11)
            goto L_0x0163
        L_0x012b:
            java.util.Iterator r8 = r6.iterateChildren()
        L_0x012f:
            boolean r9 = r8.hasNext()
            if (r9 == 0) goto L_0x0154
            java.lang.Object r9 = r8.next()
            com.adobe.xmp.impl.XMPNode r9 = (com.adobe.xmp.impl.XMPNode) r9
            if (r9 == r1) goto L_0x012f
            java.lang.String r10 = r9.getValue()
            if (r1 == 0) goto L_0x0148
            java.lang.String r2 = r1.getValue()
            goto L_0x0149
        L_0x0148:
            r2 = r12
        L_0x0149:
            boolean r10 = r10.equals(r2)
            if (r10 != 0) goto L_0x0150
            goto L_0x012f
        L_0x0150:
            r9.setValue(r11)
            goto L_0x012f
        L_0x0154:
            if (r1 == 0) goto L_0x0163
            r1.setValue(r11)
            goto L_0x0163
        L_0x015a:
            com.adobe.xmp.impl.XMPNodeUtils.appendLangItem(r6, r3, r11)
            if (r2 != 0) goto L_0x0162
            com.adobe.xmp.impl.XMPNodeUtils.appendLangItem(r6, r10, r11)
        L_0x0162:
            r7 = r0
        L_0x0163:
            if (r7 != 0) goto L_0x016e
            int r7 = r6.getChildrenLength()
            if (r7 != r0) goto L_0x016e
            com.adobe.xmp.impl.XMPNodeUtils.appendLangItem(r6, r3, r11)
        L_0x016e:
            return
        L_0x016f:
            com.adobe.xmp.XMPException r6 = new com.adobe.xmp.XMPException
            java.lang.String r8 = "Failed to find or create array node"
            r6.<init>(r8, r7)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.adobe.xmp.impl.XMPMetaImpl.setLocalizedText(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, com.adobe.xmp.options.PropertyOptions):void");
    }

    public void setLocalizedText(String str, String str2, String str3, String str4, String str5) throws XMPException {
        setLocalizedText(str, str2, str3, str4, str5, null);
    }

    public XMPProperty getProperty(String str, String str2) throws XMPException {
        return getProperty(str, str2, 0);
    }

    /* access modifiers changed from: protected */
    public XMPProperty getProperty(String str, String str2, int i) throws XMPException {
        ParameterAsserts.assertSchemaNS(str);
        ParameterAsserts.assertPropName(str2);
        final XMPNode findNode = XMPNodeUtils.findNode(this.tree, XMPPathParser.expandXPath(str, str2), false, null);
        if (findNode == null) {
            return null;
        }
        if (i == 0 || !findNode.getOptions().isCompositeProperty()) {
            final Object evaluateNodeValue = evaluateNodeValue(i, findNode);
            return new XMPProperty() {
                public String getLanguage() {
                    return null;
                }

                public Object getValue() {
                    return evaluateNodeValue;
                }

                public PropertyOptions getOptions() {
                    return findNode.getOptions();
                }

                public String toString() {
                    return evaluateNodeValue.toString();
                }
            };
        }
        throw new XMPException("Property must be simple when a value type is requested", 102);
    }

    /* access modifiers changed from: protected */
    public Object getPropertyObject(String str, String str2, int i) throws XMPException {
        ParameterAsserts.assertSchemaNS(str);
        ParameterAsserts.assertPropName(str2);
        XMPNode findNode = XMPNodeUtils.findNode(this.tree, XMPPathParser.expandXPath(str, str2), false, null);
        if (findNode == null) {
            return null;
        }
        if (i == 0 || !findNode.getOptions().isCompositeProperty()) {
            return evaluateNodeValue(i, findNode);
        }
        throw new XMPException("Property must be simple when a value type is requested", 102);
    }

    public Boolean getPropertyBoolean(String str, String str2) throws XMPException {
        return (Boolean) getPropertyObject(str, str2, 1);
    }

    public void setPropertyBoolean(String str, String str2, boolean z, PropertyOptions propertyOptions) throws XMPException {
        setProperty(str, str2, z ? XMPConst.TRUESTR : XMPConst.FALSESTR, propertyOptions);
    }

    public void setPropertyBoolean(String str, String str2, boolean z) throws XMPException {
        setProperty(str, str2, z ? XMPConst.TRUESTR : XMPConst.FALSESTR, null);
    }

    public Integer getPropertyInteger(String str, String str2) throws XMPException {
        return (Integer) getPropertyObject(str, str2, 2);
    }

    public void setPropertyInteger(String str, String str2, int i, PropertyOptions propertyOptions) throws XMPException {
        setProperty(str, str2, new Integer(i), propertyOptions);
    }

    public void setPropertyInteger(String str, String str2, int i) throws XMPException {
        setProperty(str, str2, new Integer(i), null);
    }

    public Long getPropertyLong(String str, String str2) throws XMPException {
        return (Long) getPropertyObject(str, str2, 3);
    }

    public void setPropertyLong(String str, String str2, long j, PropertyOptions propertyOptions) throws XMPException {
        setProperty(str, str2, new Long(j), propertyOptions);
    }

    public void setPropertyLong(String str, String str2, long j) throws XMPException {
        setProperty(str, str2, new Long(j), null);
    }

    public Double getPropertyDouble(String str, String str2) throws XMPException {
        return (Double) getPropertyObject(str, str2, 4);
    }

    public void setPropertyDouble(String str, String str2, double d, PropertyOptions propertyOptions) throws XMPException {
        setProperty(str, str2, new Double(d), propertyOptions);
    }

    public void setPropertyDouble(String str, String str2, double d) throws XMPException {
        setProperty(str, str2, new Double(d), null);
    }

    public XMPDateTime getPropertyDate(String str, String str2) throws XMPException {
        return (XMPDateTime) getPropertyObject(str, str2, 5);
    }

    public void setPropertyDate(String str, String str2, XMPDateTime xMPDateTime, PropertyOptions propertyOptions) throws XMPException {
        setProperty(str, str2, xMPDateTime, propertyOptions);
    }

    public void setPropertyDate(String str, String str2, XMPDateTime xMPDateTime) throws XMPException {
        setProperty(str, str2, xMPDateTime, null);
    }

    public Calendar getPropertyCalendar(String str, String str2) throws XMPException {
        return (Calendar) getPropertyObject(str, str2, 6);
    }

    public void setPropertyCalendar(String str, String str2, Calendar calendar, PropertyOptions propertyOptions) throws XMPException {
        setProperty(str, str2, calendar, propertyOptions);
    }

    public void setPropertyCalendar(String str, String str2, Calendar calendar) throws XMPException {
        setProperty(str, str2, calendar, null);
    }

    public byte[] getPropertyBase64(String str, String str2) throws XMPException {
        return (byte[]) getPropertyObject(str, str2, 7);
    }

    public String getPropertyString(String str, String str2) throws XMPException {
        return (String) getPropertyObject(str, str2, 0);
    }

    public void setPropertyBase64(String str, String str2, byte[] bArr, PropertyOptions propertyOptions) throws XMPException {
        setProperty(str, str2, bArr, propertyOptions);
    }

    public void setPropertyBase64(String str, String str2, byte[] bArr) throws XMPException {
        setProperty(str, str2, bArr, null);
    }

    public XMPProperty getQualifier(String str, String str2, String str3, String str4) throws XMPException {
        ParameterAsserts.assertSchemaNS(str);
        ParameterAsserts.assertPropName(str2);
        StringBuilder sb = new StringBuilder();
        sb.append(str2);
        sb.append(XMPPathFactory.composeQualifierPath(str3, str4));
        return getProperty(str, sb.toString());
    }

    public XMPProperty getStructField(String str, String str2, String str3, String str4) throws XMPException {
        ParameterAsserts.assertSchemaNS(str);
        ParameterAsserts.assertStructName(str2);
        StringBuilder sb = new StringBuilder();
        sb.append(str2);
        sb.append(XMPPathFactory.composeStructFieldPath(str3, str4));
        return getProperty(str, sb.toString());
    }

    public XMPIterator iterator() throws XMPException {
        return iterator(null, null, null);
    }

    public XMPIterator iterator(IteratorOptions iteratorOptions) throws XMPException {
        return iterator(null, null, iteratorOptions);
    }

    public XMPIterator iterator(String str, String str2, IteratorOptions iteratorOptions) throws XMPException {
        return new XMPIteratorImpl(this, str, str2, iteratorOptions);
    }

    public void setArrayItem(String str, String str2, int i, String str3, PropertyOptions propertyOptions) throws XMPException {
        ParameterAsserts.assertSchemaNS(str);
        ParameterAsserts.assertArrayName(str2);
        XMPNode findNode = XMPNodeUtils.findNode(this.tree, XMPPathParser.expandXPath(str, str2), false, null);
        if (findNode != null) {
            doSetArrayItem(findNode, i, str3, propertyOptions, false);
            return;
        }
        throw new XMPException("Specified array does not exist", 102);
    }

    public void setArrayItem(String str, String str2, int i, String str3) throws XMPException {
        setArrayItem(str, str2, i, str3, null);
    }

    public void insertArrayItem(String str, String str2, int i, String str3, PropertyOptions propertyOptions) throws XMPException {
        ParameterAsserts.assertSchemaNS(str);
        ParameterAsserts.assertArrayName(str2);
        XMPNode findNode = XMPNodeUtils.findNode(this.tree, XMPPathParser.expandXPath(str, str2), false, null);
        if (findNode != null) {
            doSetArrayItem(findNode, i, str3, propertyOptions, true);
            return;
        }
        throw new XMPException("Specified array does not exist", 102);
    }

    public void insertArrayItem(String str, String str2, int i, String str3) throws XMPException {
        insertArrayItem(str, str2, i, str3, null);
    }

    public void setProperty(String str, String str2, Object obj, PropertyOptions propertyOptions) throws XMPException {
        ParameterAsserts.assertSchemaNS(str);
        ParameterAsserts.assertPropName(str2);
        PropertyOptions verifySetOptions = XMPNodeUtils.verifySetOptions(propertyOptions, obj);
        XMPNode findNode = XMPNodeUtils.findNode(this.tree, XMPPathParser.expandXPath(str, str2), true, verifySetOptions);
        if (findNode != null) {
            setNode(findNode, obj, verifySetOptions, false);
            return;
        }
        throw new XMPException("Specified property does not exist", 102);
    }

    public void setProperty(String str, String str2, Object obj) throws XMPException {
        setProperty(str, str2, obj, null);
    }

    public void setQualifier(String str, String str2, String str3, String str4, String str5, PropertyOptions propertyOptions) throws XMPException {
        ParameterAsserts.assertSchemaNS(str);
        ParameterAsserts.assertPropName(str2);
        if (doesPropertyExist(str, str2)) {
            StringBuilder sb = new StringBuilder();
            sb.append(str2);
            sb.append(XMPPathFactory.composeQualifierPath(str3, str4));
            setProperty(str, sb.toString(), str5, propertyOptions);
            return;
        }
        throw new XMPException("Specified property does not exist!", 102);
    }

    public void setQualifier(String str, String str2, String str3, String str4, String str5) throws XMPException {
        setQualifier(str, str2, str3, str4, str5, null);
    }

    public void setStructField(String str, String str2, String str3, String str4, String str5, PropertyOptions propertyOptions) throws XMPException {
        ParameterAsserts.assertSchemaNS(str);
        ParameterAsserts.assertStructName(str2);
        StringBuilder sb = new StringBuilder();
        sb.append(str2);
        sb.append(XMPPathFactory.composeStructFieldPath(str3, str4));
        setProperty(str, sb.toString(), str5, propertyOptions);
    }

    public void setStructField(String str, String str2, String str3, String str4, String str5) throws XMPException {
        setStructField(str, str2, str3, str4, str5, null);
    }

    public String getObjectName() {
        return this.tree.getName() != null ? this.tree.getName() : BuildConfig.FLAVOR;
    }

    public void setObjectName(String str) {
        this.tree.setName(str);
    }

    public String getPacketHeader() {
        return this.packetHeader;
    }

    public void setPacketHeader(String str) {
        this.packetHeader = str;
    }

    public Object clone() {
        return new XMPMetaImpl((XMPNode) this.tree.clone());
    }

    public String dumpObject() {
        return getRoot().dumpNode(true);
    }

    public void sort() {
        this.tree.sort();
    }

    public void normalize(ParseOptions parseOptions) throws XMPException {
        if (parseOptions == null) {
            parseOptions = new ParseOptions();
        }
        XMPNormalizer.process(this, parseOptions);
    }

    public XMPNode getRoot() {
        return this.tree;
    }

    private void doSetArrayItem(XMPNode xMPNode, int i, String str, PropertyOptions propertyOptions, boolean z) throws XMPException {
        XMPNode xMPNode2 = new XMPNode(XMPConst.ARRAY_ITEM_NAME, null);
        PropertyOptions verifySetOptions = XMPNodeUtils.verifySetOptions(propertyOptions, str);
        int childrenLength = xMPNode.getChildrenLength();
        if (z) {
            childrenLength++;
        }
        if (i == -1) {
            i = childrenLength;
        }
        if (1 > i || i > childrenLength) {
            throw new XMPException("Array index out of bounds", 104);
        }
        if (!z) {
            xMPNode.removeChild(i);
        }
        xMPNode.addChild(i, xMPNode2);
        setNode(xMPNode2, str, verifySetOptions, false);
    }

    /* access modifiers changed from: 0000 */
    public void setNode(XMPNode xMPNode, Object obj, PropertyOptions propertyOptions, boolean z) throws XMPException {
        if (z) {
            xMPNode.clear();
        }
        xMPNode.getOptions().mergeWith(propertyOptions);
        if (!xMPNode.getOptions().isCompositeProperty()) {
            XMPNodeUtils.setNodeValue(xMPNode, obj);
        } else if (obj == null || obj.toString().length() <= 0) {
            xMPNode.removeChildren();
        } else {
            throw new XMPException("Composite nodes can't have values", 102);
        }
    }

    private Object evaluateNodeValue(int i, XMPNode xMPNode) throws XMPException {
        Object obj;
        String value = xMPNode.getValue();
        switch (i) {
            case 1:
                obj = new Boolean(XMPUtils.convertToBoolean(value));
                break;
            case 2:
                obj = new Integer(XMPUtils.convertToInteger(value));
                break;
            case 3:
                obj = new Long(XMPUtils.convertToLong(value));
                break;
            case 4:
                obj = new Double(XMPUtils.convertToDouble(value));
                break;
            case 5:
                return XMPUtils.convertToDate(value);
            case 6:
                return XMPUtils.convertToDate(value).getCalendar();
            case 7:
                return XMPUtils.decodeBase64(value);
            default:
                return (value != null || xMPNode.getOptions().isCompositeProperty()) ? value : BuildConfig.FLAVOR;
        }
        return obj;
    }
}
