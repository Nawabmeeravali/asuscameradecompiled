package com.adobe.xmp.impl;

import com.adobe.xmp.XMPConst;
import com.adobe.xmp.XMPError;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.XMPSchemaRegistry;
import com.adobe.xmp.options.SerializeOptions;
import com.asus.scenedetectlib.BuildConfig;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class XMPSerializerRDF {
    private static final int DEFAULT_PAD = 2048;
    private static final String PACKET_HEADER = "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>";
    private static final String PACKET_TRAILER = "<?xpacket end=\"";
    private static final String PACKET_TRAILER2 = "\"?>";
    static final Set RDF_ATTR_QUALIFIER = new HashSet(Arrays.asList(new String[]{XMPConst.XML_LANG, "rdf:resource", "rdf:ID", "rdf:bagID", "rdf:nodeID"}));
    private static final String RDF_RDF_END = "</rdf:RDF>";
    private static final String RDF_RDF_START = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">";
    private static final String RDF_SCHEMA_END = "</rdf:Description>";
    private static final String RDF_SCHEMA_START = "<rdf:Description rdf:about=";
    private static final String RDF_STRUCT_END = "</rdf:Description>";
    private static final String RDF_STRUCT_START = "<rdf:Description";
    private static final String RDF_XMPMETA_END = "</x:xmpmeta>";
    private static final String RDF_XMPMETA_START = "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"";
    private SerializeOptions options;
    private CountOutputStream outputStream;
    private int padding;
    private int unicodeSize = 1;
    private OutputStreamWriter writer;
    private XMPMetaImpl xmp;

    public void serialize(XMPMeta xMPMeta, OutputStream outputStream2, SerializeOptions serializeOptions) throws XMPException {
        try {
            this.outputStream = new CountOutputStream(outputStream2);
            this.writer = new OutputStreamWriter(this.outputStream, serializeOptions.getEncoding());
            this.xmp = (XMPMetaImpl) xMPMeta;
            this.options = serializeOptions;
            this.padding = serializeOptions.getPadding();
            this.writer = new OutputStreamWriter(this.outputStream, serializeOptions.getEncoding());
            checkOptionsConsistence();
            String serializeAsRDF = serializeAsRDF();
            this.writer.flush();
            addPadding(serializeAsRDF.length());
            write(serializeAsRDF);
            this.writer.flush();
            this.outputStream.close();
        } catch (IOException unused) {
            throw new XMPException("Error writing to the OutputStream", 0);
        }
    }

    private void addPadding(int i) throws XMPException, IOException {
        if (this.options.getExactPacketLength()) {
            int bytesWritten = this.outputStream.getBytesWritten() + (i * this.unicodeSize);
            int i2 = this.padding;
            if (bytesWritten <= i2) {
                this.padding = i2 - bytesWritten;
            } else {
                throw new XMPException("Can't fit into specified packet size", 107);
            }
        }
        this.padding /= this.unicodeSize;
        int length = this.options.getNewline().length();
        int i3 = this.padding;
        if (i3 >= length) {
            this.padding = i3 - length;
            while (true) {
                int i4 = this.padding;
                int i5 = length + 100;
                if (i4 >= i5) {
                    writeChars(100, ' ');
                    writeNewline();
                    this.padding -= i5;
                } else {
                    writeChars(i4, ' ');
                    writeNewline();
                    return;
                }
            }
        } else {
            writeChars(i3, ' ');
        }
    }

    /* access modifiers changed from: protected */
    public void checkOptionsConsistence() throws XMPException {
        if (this.options.getEncodeUTF16BE() || this.options.getEncodeUTF16LE()) {
            this.unicodeSize = 2;
        }
        if (this.options.getExactPacketLength()) {
            if (!this.options.getOmitPacketWrapper() && !this.options.getIncludeThumbnailPad()) {
                if (((this.unicodeSize - 1) & this.options.getPadding()) != 0) {
                    throw new XMPException("Exact size must be a multiple of the Unicode element", 103);
                }
                return;
            }
            throw new XMPException("Inconsistent options for exact size serialize", 103);
        } else if (this.options.getReadOnlyPacket()) {
            if (!this.options.getOmitPacketWrapper() && !this.options.getIncludeThumbnailPad()) {
                this.padding = 0;
                return;
            }
            throw new XMPException("Inconsistent options for read-only packet", 103);
        } else if (!this.options.getOmitPacketWrapper()) {
            if (this.padding == 0) {
                this.padding = this.unicodeSize * 2048;
            }
            if (this.options.getIncludeThumbnailPad() && !this.xmp.doesPropertyExist(XMPConst.NS_XMP, "Thumbnails")) {
                this.padding += this.unicodeSize * 10000;
            }
        } else if (!this.options.getIncludeThumbnailPad()) {
            this.padding = 0;
        } else {
            throw new XMPException("Inconsistent options for non-packet serialize", 103);
        }
    }

    private String serializeAsRDF() throws IOException, XMPException {
        if (!this.options.getOmitPacketWrapper()) {
            writeIndent(0);
            write(PACKET_HEADER);
            writeNewline();
        }
        writeIndent(0);
        write(RDF_XMPMETA_START);
        if (!this.options.getOmitVersionAttribute()) {
            write(XMPMetaFactory.getVersionInfo().getMessage());
        }
        write("\">");
        writeNewline();
        writeIndent(1);
        write(RDF_RDF_START);
        writeNewline();
        if (this.options.getUseCompactFormat()) {
            serializeCompactRDFSchemas();
        } else {
            serializePrettyRDFSchemas();
        }
        writeIndent(1);
        write(RDF_RDF_END);
        writeNewline();
        writeIndent(0);
        write(RDF_XMPMETA_END);
        writeNewline();
        String str = BuildConfig.FLAVOR;
        if (this.options.getOmitPacketWrapper()) {
            return str;
        }
        for (int baseIndent = this.options.getBaseIndent(); baseIndent > 0; baseIndent--) {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(this.options.getIndent());
            str = sb.toString();
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append(str);
        sb2.append(PACKET_TRAILER);
        String sb3 = sb2.toString();
        StringBuilder sb4 = new StringBuilder();
        sb4.append(sb3);
        sb4.append(this.options.getReadOnlyPacket() ? 'r' : 'w');
        String sb5 = sb4.toString();
        StringBuilder sb6 = new StringBuilder();
        sb6.append(sb5);
        sb6.append(PACKET_TRAILER2);
        return sb6.toString();
    }

    private void serializePrettyRDFSchemas() throws IOException, XMPException {
        if (this.xmp.getRoot().getChildrenLength() > 0) {
            Iterator iterateChildren = this.xmp.getRoot().iterateChildren();
            while (iterateChildren.hasNext()) {
                serializePrettyRDFSchema((XMPNode) iterateChildren.next());
            }
            return;
        }
        writeIndent(2);
        write(RDF_SCHEMA_START);
        writeTreeName();
        write("/>");
        writeNewline();
    }

    private void writeTreeName() throws IOException {
        write(34);
        String name = this.xmp.getRoot().getName();
        if (name != null) {
            appendNodeValue(name, true);
        }
        write(34);
    }

    private void serializeCompactRDFSchemas() throws IOException, XMPException {
        writeIndent(2);
        write(RDF_SCHEMA_START);
        writeTreeName();
        HashSet hashSet = new HashSet();
        hashSet.add("xml");
        hashSet.add("rdf");
        Iterator iterateChildren = this.xmp.getRoot().iterateChildren();
        while (iterateChildren.hasNext()) {
            declareUsedNamespaces((XMPNode) iterateChildren.next(), hashSet, 4);
        }
        boolean z = true;
        Iterator iterateChildren2 = this.xmp.getRoot().iterateChildren();
        while (iterateChildren2.hasNext()) {
            z &= serializeCompactRDFAttrProps((XMPNode) iterateChildren2.next(), 3);
        }
        if (!z) {
            write(62);
            writeNewline();
            Iterator iterateChildren3 = this.xmp.getRoot().iterateChildren();
            while (iterateChildren3.hasNext()) {
                serializeCompactRDFElementProps((XMPNode) iterateChildren3.next(), 3);
            }
            writeIndent(2);
            write("</rdf:Description>");
            writeNewline();
            return;
        }
        write("/>");
        writeNewline();
    }

    private boolean serializeCompactRDFAttrProps(XMPNode xMPNode, int i) throws IOException {
        Iterator iterateChildren = xMPNode.iterateChildren();
        boolean z = true;
        while (iterateChildren.hasNext()) {
            XMPNode xMPNode2 = (XMPNode) iterateChildren.next();
            if (canBeRDFAttrProp(xMPNode2)) {
                writeNewline();
                writeIndent(i);
                write(xMPNode2.getName());
                write("=\"");
                appendNodeValue(xMPNode2.getValue(), true);
                write(34);
            } else {
                z = false;
            }
        }
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x00b5  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0004 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void serializeCompactRDFElementProps(com.adobe.xmp.impl.XMPNode r11, int r12) throws java.io.IOException, com.adobe.xmp.XMPException {
        /*
            r10 = this;
            java.util.Iterator r11 = r11.iterateChildren()
        L_0x0004:
            boolean r0 = r11.hasNext()
            if (r0 == 0) goto L_0x00cc
            java.lang.Object r0 = r11.next()
            com.adobe.xmp.impl.XMPNode r0 = (com.adobe.xmp.impl.XMPNode) r0
            boolean r1 = r10.canBeRDFAttrProp(r0)
            if (r1 == 0) goto L_0x0017
            goto L_0x0004
        L_0x0017:
            java.lang.String r1 = r0.getName()
            java.lang.String r2 = "[]"
            boolean r2 = r2.equals(r1)
            if (r2 == 0) goto L_0x0025
            java.lang.String r1 = "rdf:li"
        L_0x0025:
            r10.writeIndent(r12)
            r2 = 60
            r10.write(r2)
            r10.write(r1)
            java.util.Iterator r2 = r0.iterateQualifier()
            r3 = 0
            r4 = r3
            r5 = r4
        L_0x0037:
            boolean r6 = r2.hasNext()
            r7 = 1
            if (r6 == 0) goto L_0x007a
            java.lang.Object r6 = r2.next()
            com.adobe.xmp.impl.XMPNode r6 = (com.adobe.xmp.impl.XMPNode) r6
            java.util.Set r8 = RDF_ATTR_QUALIFIER
            java.lang.String r9 = r6.getName()
            boolean r8 = r8.contains(r9)
            if (r8 != 0) goto L_0x0052
            r4 = r7
            goto L_0x0037
        L_0x0052:
            java.lang.String r5 = r6.getName()
            java.lang.String r8 = "rdf:resource"
            boolean r5 = r8.equals(r5)
            r8 = 32
            r10.write(r8)
            java.lang.String r8 = r6.getName()
            r10.write(r8)
            java.lang.String r8 = "=\""
            r10.write(r8)
            java.lang.String r6 = r6.getValue()
            r10.appendNodeValue(r6, r7)
            r6 = 34
            r10.write(r6)
            goto L_0x0037
        L_0x007a:
            if (r4 == 0) goto L_0x0080
            r10.serializeCompactRDFGeneralQualifier(r12, r0)
            goto L_0x00ad
        L_0x0080:
            com.adobe.xmp.options.PropertyOptions r2 = r0.getOptions()
            boolean r2 = r2.isCompositeProperty()
            if (r2 != 0) goto L_0x00a0
            java.lang.Object[] r0 = r10.serializeCompactRDFSimpleProp(r0)
            r2 = r0[r3]
            java.lang.Boolean r2 = (java.lang.Boolean) r2
            boolean r2 = r2.booleanValue()
            r0 = r0[r7]
            java.lang.Boolean r0 = (java.lang.Boolean) r0
            boolean r7 = r0.booleanValue()
            r0 = r2
            goto L_0x00b3
        L_0x00a0:
            com.adobe.xmp.options.PropertyOptions r2 = r0.getOptions()
            boolean r2 = r2.isArray()
            if (r2 == 0) goto L_0x00af
            r10.serializeCompactRDFArrayProp(r0, r12)
        L_0x00ad:
            r0 = r7
            goto L_0x00b3
        L_0x00af:
            boolean r0 = r10.serializeCompactRDFStructProp(r0, r12, r5)
        L_0x00b3:
            if (r0 == 0) goto L_0x0004
            if (r7 == 0) goto L_0x00ba
            r10.writeIndent(r12)
        L_0x00ba:
            java.lang.String r0 = "</"
            r10.write(r0)
            r10.write(r1)
            r0 = 62
            r10.write(r0)
            r10.writeNewline()
            goto L_0x0004
        L_0x00cc:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.adobe.xmp.impl.XMPSerializerRDF.serializeCompactRDFElementProps(com.adobe.xmp.impl.XMPNode, int):void");
    }

    private Object[] serializeCompactRDFSimpleProp(XMPNode xMPNode) throws IOException {
        Boolean bool;
        Boolean bool2 = Boolean.TRUE;
        if (xMPNode.getOptions().isURI()) {
            write(" rdf:resource=\"");
            appendNodeValue(xMPNode.getValue(), true);
            write("\"/>");
            writeNewline();
            bool = Boolean.FALSE;
        } else if (xMPNode.getValue() == null || xMPNode.getValue().length() == 0) {
            write("/>");
            writeNewline();
            bool = Boolean.FALSE;
        } else {
            write(62);
            appendNodeValue(xMPNode.getValue(), false);
            Boolean bool3 = bool2;
            bool2 = Boolean.FALSE;
            bool = bool3;
        }
        return new Object[]{bool, bool2};
    }

    private void serializeCompactRDFArrayProp(XMPNode xMPNode, int i) throws IOException, XMPException {
        write(62);
        writeNewline();
        int i2 = i + 1;
        emitRDFArrayTag(xMPNode, true, i2);
        if (xMPNode.getOptions().isArrayAltText()) {
            XMPNodeUtils.normalizeLangArray(xMPNode);
        }
        serializeCompactRDFElementProps(xMPNode, i + 2);
        emitRDFArrayTag(xMPNode, false, i2);
    }

    private boolean serializeCompactRDFStructProp(XMPNode xMPNode, int i, boolean z) throws XMPException, IOException {
        Iterator iterateChildren = xMPNode.iterateChildren();
        boolean z2 = false;
        boolean z3 = false;
        while (iterateChildren.hasNext()) {
            if (canBeRDFAttrProp((XMPNode) iterateChildren.next())) {
                z2 = true;
            } else {
                z3 = true;
            }
            if (z2 && z3) {
                break;
            }
        }
        if (z && z3) {
            throw new XMPException("Can't mix rdf:resource qualifier and element fields", XMPError.BADRDF);
        } else if (!xMPNode.hasChildren()) {
            write(" rdf:parseType=\"Resource\"/>");
            writeNewline();
            return false;
        } else if (!z3) {
            serializeCompactRDFAttrProps(xMPNode, i + 1);
            write("/>");
            writeNewline();
            return false;
        } else {
            if (!z2) {
                write(" rdf:parseType=\"Resource\">");
                writeNewline();
                serializeCompactRDFElementProps(xMPNode, i + 1);
            } else {
                write(62);
                writeNewline();
                int i2 = i + 1;
                writeIndent(i2);
                write(RDF_STRUCT_START);
                serializeCompactRDFAttrProps(xMPNode, i + 2);
                write(">");
                writeNewline();
                serializeCompactRDFElementProps(xMPNode, i2);
                writeIndent(i2);
                write("</rdf:Description>");
                writeNewline();
            }
            return true;
        }
    }

    private void serializeCompactRDFGeneralQualifier(int i, XMPNode xMPNode) throws IOException, XMPException {
        write(" rdf:parseType=\"Resource\">");
        writeNewline();
        int i2 = i + 1;
        serializePrettyRDFProperty(xMPNode, true, i2);
        Iterator iterateQualifier = xMPNode.iterateQualifier();
        while (iterateQualifier.hasNext()) {
            serializePrettyRDFProperty((XMPNode) iterateQualifier.next(), false, i2);
        }
    }

    private void serializePrettyRDFSchema(XMPNode xMPNode) throws IOException, XMPException {
        writeIndent(2);
        write(RDF_SCHEMA_START);
        writeTreeName();
        HashSet hashSet = new HashSet();
        hashSet.add("xml");
        hashSet.add("rdf");
        declareUsedNamespaces(xMPNode, hashSet, 4);
        write(62);
        writeNewline();
        Iterator iterateChildren = xMPNode.iterateChildren();
        while (iterateChildren.hasNext()) {
            serializePrettyRDFProperty((XMPNode) iterateChildren.next(), false, 3);
        }
        writeIndent(2);
        write("</rdf:Description>");
        writeNewline();
    }

    private void declareUsedNamespaces(XMPNode xMPNode, Set set, int i) throws IOException {
        if (xMPNode.getOptions().isSchemaNode()) {
            declareNamespace(xMPNode.getValue().substring(0, xMPNode.getValue().length() - 1), xMPNode.getName(), set, i);
        } else if (xMPNode.getOptions().isStruct()) {
            Iterator iterateChildren = xMPNode.iterateChildren();
            while (iterateChildren.hasNext()) {
                declareNamespace(((XMPNode) iterateChildren.next()).getName(), null, set, i);
            }
        }
        Iterator iterateChildren2 = xMPNode.iterateChildren();
        while (iterateChildren2.hasNext()) {
            declareUsedNamespaces((XMPNode) iterateChildren2.next(), set, i);
        }
        Iterator iterateQualifier = xMPNode.iterateQualifier();
        while (iterateQualifier.hasNext()) {
            XMPNode xMPNode2 = (XMPNode) iterateQualifier.next();
            declareNamespace(xMPNode2.getName(), null, set, i);
            declareUsedNamespaces(xMPNode2, set, i);
        }
    }

    private void declareNamespace(String str, String str2, Set set, int i) throws IOException {
        if (str2 == null) {
            QName qName = new QName(str);
            if (qName.hasPrefix()) {
                str = qName.getPrefix();
                XMPSchemaRegistry schemaRegistry = XMPMetaFactory.getSchemaRegistry();
                StringBuilder sb = new StringBuilder();
                sb.append(str);
                sb.append(":");
                str2 = schemaRegistry.getNamespaceURI(sb.toString());
                declareNamespace(str, str2, set, i);
            } else {
                return;
            }
        }
        if (!set.contains(str)) {
            writeNewline();
            writeIndent(i);
            write("xmlns:");
            write(str);
            write("=\"");
            write(str2);
            write(34);
            set.add(str);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:67:0x01b6  */
    /* JADX WARNING: Removed duplicated region for block: B:84:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void serializePrettyRDFProperty(com.adobe.xmp.impl.XMPNode r17, boolean r18, int r19) throws java.io.IOException, com.adobe.xmp.XMPException {
        /*
            r16 = this;
            r0 = r16
            r1 = r17
            r2 = r19
            java.lang.String r3 = r17.getName()
            if (r18 == 0) goto L_0x000f
            java.lang.String r3 = "rdf:value"
            goto L_0x0019
        L_0x000f:
            java.lang.String r4 = "[]"
            boolean r4 = r4.equals(r3)
            if (r4 == 0) goto L_0x0019
            java.lang.String r3 = "rdf:li"
        L_0x0019:
            r0.writeIndent(r2)
            r4 = 60
            r0.write(r4)
            r0.write(r3)
            java.util.Iterator r4 = r17.iterateQualifier()
            r5 = 0
            r6 = r5
            r7 = r6
        L_0x002b:
            boolean r8 = r4.hasNext()
            r9 = 34
            java.lang.String r10 = "=\""
            r11 = 32
            r12 = 1
            if (r8 == 0) goto L_0x0070
            java.lang.Object r8 = r4.next()
            com.adobe.xmp.impl.XMPNode r8 = (com.adobe.xmp.impl.XMPNode) r8
            java.util.Set r13 = RDF_ATTR_QUALIFIER
            java.lang.String r14 = r8.getName()
            boolean r13 = r13.contains(r14)
            if (r13 != 0) goto L_0x004c
            r6 = r12
            goto L_0x002b
        L_0x004c:
            java.lang.String r7 = r8.getName()
            java.lang.String r13 = "rdf:resource"
            boolean r7 = r13.equals(r7)
            if (r18 != 0) goto L_0x002b
            r0.write(r11)
            java.lang.String r11 = r8.getName()
            r0.write(r11)
            r0.write(r10)
            java.lang.String r8 = r8.getValue()
            r0.appendNodeValue(r8, r12)
            r0.write(r9)
            goto L_0x002b
        L_0x0070:
            r4 = 202(0xca, float:2.83E-43)
            java.lang.String r8 = " rdf:parseType=\"Resource\">"
            r13 = 62
            if (r6 == 0) goto L_0x00af
            if (r18 != 0) goto L_0x00af
            if (r7 != 0) goto L_0x00a7
            r0.write(r8)
            r16.writeNewline()
            int r4 = r2 + 1
            r0.serializePrettyRDFProperty(r1, r12, r4)
            java.util.Iterator r1 = r17.iterateQualifier()
        L_0x008b:
            boolean r6 = r1.hasNext()
            if (r6 == 0) goto L_0x016e
            java.lang.Object r6 = r1.next()
            com.adobe.xmp.impl.XMPNode r6 = (com.adobe.xmp.impl.XMPNode) r6
            java.util.Set r7 = RDF_ATTR_QUALIFIER
            java.lang.String r8 = r6.getName()
            boolean r7 = r7.contains(r8)
            if (r7 != 0) goto L_0x008b
            r0.serializePrettyRDFProperty(r6, r5, r4)
            goto L_0x008b
        L_0x00a7:
            com.adobe.xmp.XMPException r0 = new com.adobe.xmp.XMPException
            java.lang.String r1 = "Can't mix rdf:resource and general qualifiers"
            r0.<init>(r1, r4)
            throw r0
        L_0x00af:
            com.adobe.xmp.options.PropertyOptions r6 = r17.getOptions()
            boolean r6 = r6.isCompositeProperty()
            java.lang.String r14 = "/>"
            if (r6 != 0) goto L_0x0105
            com.adobe.xmp.options.PropertyOptions r4 = r17.getOptions()
            boolean r4 = r4.isURI()
            if (r4 == 0) goto L_0x00db
            java.lang.String r4 = " rdf:resource=\""
            r0.write(r4)
            java.lang.String r1 = r17.getValue()
            r0.appendNodeValue(r1, r12)
            java.lang.String r1 = "\"/>"
            r0.write(r1)
            r16.writeNewline()
            goto L_0x01b4
        L_0x00db:
            java.lang.String r4 = r17.getValue()
            if (r4 == 0) goto L_0x00fd
            java.lang.String r4 = r17.getValue()
            java.lang.String r6 = ""
            boolean r4 = r6.equals(r4)
            if (r4 == 0) goto L_0x00ee
            goto L_0x00fd
        L_0x00ee:
            r0.write(r13)
            java.lang.String r1 = r17.getValue()
            r0.appendNodeValue(r1, r5)
            r15 = r12
            r12 = r5
            r5 = r15
            goto L_0x01b4
        L_0x00fd:
            r0.write(r14)
            r16.writeNewline()
            goto L_0x01b4
        L_0x0105:
            com.adobe.xmp.options.PropertyOptions r6 = r17.getOptions()
            boolean r6 = r6.isArray()
            if (r6 == 0) goto L_0x0141
            r0.write(r13)
            r16.writeNewline()
            int r4 = r2 + 1
            r0.emitRDFArrayTag(r1, r12, r4)
            com.adobe.xmp.options.PropertyOptions r6 = r17.getOptions()
            boolean r6 = r6.isArrayAltText()
            if (r6 == 0) goto L_0x0127
            com.adobe.xmp.impl.XMPNodeUtils.normalizeLangArray(r17)
        L_0x0127:
            java.util.Iterator r6 = r17.iterateChildren()
        L_0x012b:
            boolean r7 = r6.hasNext()
            if (r7 == 0) goto L_0x013d
            java.lang.Object r7 = r6.next()
            com.adobe.xmp.impl.XMPNode r7 = (com.adobe.xmp.impl.XMPNode) r7
            int r8 = r2 + 2
            r0.serializePrettyRDFProperty(r7, r5, r8)
            goto L_0x012b
        L_0x013d:
            r0.emitRDFArrayTag(r1, r5, r4)
            goto L_0x016e
        L_0x0141:
            if (r7 != 0) goto L_0x0170
            boolean r4 = r17.hasChildren()
            if (r4 != 0) goto L_0x0152
            java.lang.String r1 = " rdf:parseType=\"Resource\"/>"
            r0.write(r1)
            r16.writeNewline()
            goto L_0x01b4
        L_0x0152:
            r0.write(r8)
            r16.writeNewline()
            java.util.Iterator r1 = r17.iterateChildren()
        L_0x015c:
            boolean r4 = r1.hasNext()
            if (r4 == 0) goto L_0x016e
            java.lang.Object r4 = r1.next()
            com.adobe.xmp.impl.XMPNode r4 = (com.adobe.xmp.impl.XMPNode) r4
            int r6 = r2 + 1
            r0.serializePrettyRDFProperty(r4, r5, r6)
            goto L_0x015c
        L_0x016e:
            r5 = r12
            goto L_0x01b4
        L_0x0170:
            java.util.Iterator r1 = r17.iterateChildren()
        L_0x0174:
            boolean r6 = r1.hasNext()
            if (r6 == 0) goto L_0x01ae
            java.lang.Object r6 = r1.next()
            com.adobe.xmp.impl.XMPNode r6 = (com.adobe.xmp.impl.XMPNode) r6
            boolean r7 = r0.canBeRDFAttrProp(r6)
            if (r7 == 0) goto L_0x01a6
            r16.writeNewline()
            int r7 = r2 + 1
            r0.writeIndent(r7)
            r0.write(r11)
            java.lang.String r7 = r6.getName()
            r0.write(r7)
            r0.write(r10)
            java.lang.String r6 = r6.getValue()
            r0.appendNodeValue(r6, r12)
            r0.write(r9)
            goto L_0x0174
        L_0x01a6:
            com.adobe.xmp.XMPException r0 = new com.adobe.xmp.XMPException
            java.lang.String r1 = "Can't mix rdf:resource and complex fields"
            r0.<init>(r1, r4)
            throw r0
        L_0x01ae:
            r0.write(r14)
            r16.writeNewline()
        L_0x01b4:
            if (r5 == 0) goto L_0x01c9
            if (r12 == 0) goto L_0x01bb
            r0.writeIndent(r2)
        L_0x01bb:
            java.lang.String r1 = "</"
            r0.write(r1)
            r0.write(r3)
            r0.write(r13)
            r16.writeNewline()
        L_0x01c9:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.adobe.xmp.impl.XMPSerializerRDF.serializePrettyRDFProperty(com.adobe.xmp.impl.XMPNode, boolean, int):void");
    }

    private void emitRDFArrayTag(XMPNode xMPNode, boolean z, int i) throws IOException {
        if (z || xMPNode.hasChildren()) {
            writeIndent(i);
            write(z ? "<rdf:" : "</rdf:");
            if (xMPNode.getOptions().isArrayAlternate()) {
                write("Alt");
            } else if (xMPNode.getOptions().isArrayOrdered()) {
                write("Seq");
            } else {
                write("Bag");
            }
            if (!z || xMPNode.hasChildren()) {
                write(">");
            } else {
                write("/>");
            }
            writeNewline();
        }
    }

    private void appendNodeValue(String str, boolean z) throws IOException {
        write(Utils.escapeXML(str, z, true));
    }

    private boolean canBeRDFAttrProp(XMPNode xMPNode) {
        if (!xMPNode.hasQualifier() && !xMPNode.getOptions().isURI() && !xMPNode.getOptions().isCompositeProperty()) {
            if (!XMPConst.ARRAY_ITEM_NAME.equals(xMPNode.getName())) {
                return true;
            }
        }
        return false;
    }

    private void writeIndent(int i) throws IOException {
        for (int baseIndent = this.options.getBaseIndent() + i; baseIndent > 0; baseIndent--) {
            this.writer.write(this.options.getIndent());
        }
    }

    private void write(int i) throws IOException {
        this.writer.write(i);
    }

    private void write(String str) throws IOException {
        this.writer.write(str);
    }

    private void writeChars(int i, char c) throws IOException {
        while (i > 0) {
            this.writer.write(c);
            i--;
        }
    }

    private void writeNewline() throws IOException {
        this.writer.write(this.options.getNewline());
    }
}
