package com.adobe.xmp.impl;

import com.adobe.xmp.XMPConst;
import com.adobe.xmp.XMPError;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.XMPSchemaRegistry;
import com.adobe.xmp.options.PropertyOptions;
import com.asus.scenedetectlib.BuildConfig;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ParseRDF implements XMPError, XMPConst {
    public static final String DEFAULT_PREFIX = "_dflt";
    public static final int RDFTERM_ABOUT = 3;
    public static final int RDFTERM_ABOUT_EACH = 10;
    public static final int RDFTERM_ABOUT_EACH_PREFIX = 11;
    public static final int RDFTERM_BAG_ID = 12;
    public static final int RDFTERM_DATATYPE = 7;
    public static final int RDFTERM_DESCRIPTION = 8;
    public static final int RDFTERM_FIRST_CORE = 1;
    public static final int RDFTERM_FIRST_OLD = 10;
    public static final int RDFTERM_FIRST_SYNTAX = 1;
    public static final int RDFTERM_ID = 2;
    public static final int RDFTERM_LAST_CORE = 7;
    public static final int RDFTERM_LAST_OLD = 12;
    public static final int RDFTERM_LAST_SYNTAX = 9;
    public static final int RDFTERM_LI = 9;
    public static final int RDFTERM_NODE_ID = 6;
    public static final int RDFTERM_OTHER = 0;
    public static final int RDFTERM_PARSE_TYPE = 4;
    public static final int RDFTERM_RDF = 1;
    public static final int RDFTERM_RESOURCE = 5;

    private static boolean isCoreSyntaxTerm(int i) {
        return 1 <= i && i <= 7;
    }

    private static boolean isOldTerm(int i) {
        return 10 <= i && i <= 12;
    }

    static XMPMetaImpl parse(Node node) throws XMPException {
        XMPMetaImpl xMPMetaImpl = new XMPMetaImpl();
        rdf_RDF(xMPMetaImpl, node);
        return xMPMetaImpl;
    }

    static void rdf_RDF(XMPMetaImpl xMPMetaImpl, Node node) throws XMPException {
        if (node.hasAttributes()) {
            rdf_NodeElementList(xMPMetaImpl, xMPMetaImpl.getRoot(), node);
            return;
        }
        throw new XMPException("Invalid attributes of rdf:RDF element", XMPError.BADRDF);
    }

    private static void rdf_NodeElementList(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node) throws XMPException {
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);
            if (!isWhitespaceNode(item)) {
                rdf_NodeElement(xMPMetaImpl, xMPNode, item, true);
            }
        }
    }

    private static void rdf_NodeElement(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        int rDFTermKind = getRDFTermKind(node);
        if (rDFTermKind != 8 && rDFTermKind != 0) {
            throw new XMPException("Node element must be rdf:Description or typed node", XMPError.BADRDF);
        } else if (!z || rDFTermKind != 0) {
            rdf_NodeElementAttrs(xMPMetaImpl, xMPNode, node, z);
            rdf_PropertyElementList(xMPMetaImpl, xMPNode, node, z);
        } else {
            throw new XMPException("Top level typed node not allowed", XMPError.BADXMP);
        }
    }

    private static void rdf_NodeElementAttrs(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        int i = 0;
        for (int i2 = 0; i2 < node.getAttributes().getLength(); i2++) {
            Node item = node.getAttributes().item(i2);
            String str = "xmlns";
            if (!str.equals(item.getPrefix()) && (item.getPrefix() != null || !str.equals(item.getNodeName()))) {
                int rDFTermKind = getRDFTermKind(item);
                if (rDFTermKind == 0) {
                    addChildNode(xMPMetaImpl, xMPNode, item, item.getNodeValue(), z);
                } else if (rDFTermKind != 6 && rDFTermKind != 2 && rDFTermKind != 3) {
                    throw new XMPException("Invalid nodeElement attribute", XMPError.BADRDF);
                } else if (i <= 0) {
                    i++;
                    if (z && rDFTermKind == 3) {
                        if (xMPNode.getName() == null || xMPNode.getName().length() <= 0) {
                            xMPNode.setName(item.getNodeValue());
                        } else if (!xMPNode.getName().equals(item.getNodeValue())) {
                            throw new XMPException("Mismatched top level rdf:about values", XMPError.BADXMP);
                        }
                    }
                } else {
                    throw new XMPException("Mutally exclusive about, ID, nodeID attributes", XMPError.BADRDF);
                }
            }
        }
    }

    private static void rdf_PropertyElementList(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);
            if (!isWhitespaceNode(item)) {
                if (item.getNodeType() == 1) {
                    rdf_PropertyElement(xMPMetaImpl, xMPNode, item, z);
                } else {
                    throw new XMPException("Expected property element node not found", XMPError.BADRDF);
                }
            }
        }
    }

    private static void rdf_PropertyElement(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        if (isPropertyElementName(getRDFTermKind(node))) {
            NamedNodeMap attributes = node.getAttributes();
            List<String> list = null;
            for (int i = 0; i < attributes.getLength(); i++) {
                Node item = attributes.item(i);
                String str = "xmlns";
                if (str.equals(item.getPrefix()) || (item.getPrefix() == null && str.equals(item.getNodeName()))) {
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(item.getNodeName());
                }
            }
            if (list != null) {
                for (String removeNamedItem : list) {
                    attributes.removeNamedItem(removeNamedItem);
                }
            }
            if (attributes.getLength() > 3) {
                rdf_EmptyPropertyElement(xMPMetaImpl, xMPNode, node, z);
            } else {
                for (int i2 = 0; i2 < attributes.getLength(); i2++) {
                    Node item2 = attributes.item(i2);
                    String localName = item2.getLocalName();
                    String namespaceURI = item2.getNamespaceURI();
                    String nodeValue = item2.getNodeValue();
                    boolean equals = XMPConst.XML_LANG.equals(item2.getNodeName());
                    String str2 = XMPConst.NS_RDF;
                    if (!equals || ("ID".equals(localName) && str2.equals(namespaceURI))) {
                        if ("datatype".equals(localName) && str2.equals(namespaceURI)) {
                            rdf_LiteralPropertyElement(xMPMetaImpl, xMPNode, node, z);
                        } else if (!"parseType".equals(localName) || !str2.equals(namespaceURI)) {
                            rdf_EmptyPropertyElement(xMPMetaImpl, xMPNode, node, z);
                        } else if ("Literal".equals(nodeValue)) {
                            rdf_ParseTypeLiteralPropertyElement();
                            throw null;
                        } else if ("Resource".equals(nodeValue)) {
                            rdf_ParseTypeResourcePropertyElement(xMPMetaImpl, xMPNode, node, z);
                        } else if ("Collection".equals(nodeValue)) {
                            rdf_ParseTypeCollectionPropertyElement();
                            throw null;
                        } else {
                            rdf_ParseTypeOtherPropertyElement();
                            throw null;
                        }
                        return;
                    }
                }
                if (node.hasChildNodes()) {
                    for (int i3 = 0; i3 < node.getChildNodes().getLength(); i3++) {
                        if (node.getChildNodes().item(i3).getNodeType() != 3) {
                            rdf_ResourcePropertyElement(xMPMetaImpl, xMPNode, node, z);
                            return;
                        }
                    }
                    rdf_LiteralPropertyElement(xMPMetaImpl, xMPNode, node, z);
                } else {
                    rdf_EmptyPropertyElement(xMPMetaImpl, xMPNode, node, z);
                }
            }
            return;
        }
        throw new XMPException("Invalid property element name", XMPError.BADRDF);
    }

    private static void rdf_ResourcePropertyElement(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        if (z) {
            if ("iX:changes".equals(node.getNodeName())) {
                return;
            }
        }
        XMPNode addChildNode = addChildNode(xMPMetaImpl, xMPNode, node, BuildConfig.FLAVOR, z);
        int i = 0;
        while (true) {
            int length = node.getAttributes().getLength();
            String str = XMPConst.NS_RDF;
            if (i < length) {
                Node item = node.getAttributes().item(i);
                String str2 = "xmlns";
                if (!str2.equals(item.getPrefix()) && (item.getPrefix() != null || !str2.equals(item.getNodeName()))) {
                    String localName = item.getLocalName();
                    String namespaceURI = item.getNamespaceURI();
                    String nodeName = item.getNodeName();
                    String str3 = XMPConst.XML_LANG;
                    if (str3.equals(nodeName)) {
                        addQualifierNode(addChildNode, str3, item.getNodeValue());
                    } else if (!"ID".equals(localName) || !str.equals(namespaceURI)) {
                    }
                }
                i++;
            } else {
                boolean z2 = false;
                for (int i2 = 0; i2 < node.getChildNodes().getLength(); i2++) {
                    Node item2 = node.getChildNodes().item(i2);
                    if (!isWhitespaceNode(item2)) {
                        if (item2.getNodeType() == 1 && !z2) {
                            boolean equals = str.equals(item2.getNamespaceURI());
                            String localName2 = item2.getLocalName();
                            if (equals && "Bag".equals(localName2)) {
                                addChildNode.getOptions().setArray(true);
                            } else if (equals && "Seq".equals(localName2)) {
                                addChildNode.getOptions().setArray(true).setArrayOrdered(true);
                            } else if (!equals || !"Alt".equals(localName2)) {
                                addChildNode.getOptions().setStruct(true);
                                if (!equals && !"Description".equals(localName2)) {
                                    String namespaceURI2 = item2.getNamespaceURI();
                                    if (namespaceURI2 != null) {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(namespaceURI2);
                                        sb.append(':');
                                        sb.append(localName2);
                                        addQualifierNode(addChildNode, XMPConst.RDF_TYPE, sb.toString());
                                    } else {
                                        throw new XMPException("All XML elements must be in a namespace", XMPError.BADXMP);
                                    }
                                }
                            } else {
                                addChildNode.getOptions().setArray(true).setArrayOrdered(true).setArrayAlternate(true);
                            }
                            rdf_NodeElement(xMPMetaImpl, addChildNode, item2, false);
                            if (addChildNode.getHasValueChild()) {
                                fixupQualifiedNode(addChildNode);
                            } else if (addChildNode.getOptions().isArrayAlternate()) {
                                XMPNodeUtils.detectAltText(addChildNode);
                            }
                            z2 = true;
                        } else if (z2) {
                            throw new XMPException("Invalid child of resource property element", XMPError.BADRDF);
                        } else {
                            throw new XMPException("Children of resource property element must be XML elements", XMPError.BADRDF);
                        }
                    }
                }
                if (!z2) {
                    throw new XMPException("Missing child of resource property element", XMPError.BADRDF);
                }
                return;
            }
        }
        throw new XMPException("Invalid attribute for resource property element", XMPError.BADRDF);
    }

    private static void rdf_LiteralPropertyElement(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        XMPNode addChildNode = addChildNode(xMPMetaImpl, xMPNode, node, null, z);
        int i = 0;
        for (int i2 = 0; i2 < node.getAttributes().getLength(); i2++) {
            Node item = node.getAttributes().item(i2);
            String str = "xmlns";
            if (!str.equals(item.getPrefix()) && (item.getPrefix() != null || !str.equals(item.getNodeName()))) {
                String namespaceURI = item.getNamespaceURI();
                String localName = item.getLocalName();
                String nodeName = item.getNodeName();
                String str2 = XMPConst.XML_LANG;
                if (str2.equals(nodeName)) {
                    addQualifierNode(addChildNode, str2, item.getNodeValue());
                } else if (!XMPConst.NS_RDF.equals(namespaceURI) || (!"ID".equals(localName) && !"datatype".equals(localName))) {
                    throw new XMPException("Invalid attribute for literal property element", XMPError.BADRDF);
                }
            }
        }
        String str3 = BuildConfig.FLAVOR;
        while (i < node.getChildNodes().getLength()) {
            Node item2 = node.getChildNodes().item(i);
            if (item2.getNodeType() == 3) {
                StringBuilder sb = new StringBuilder();
                sb.append(str3);
                sb.append(item2.getNodeValue());
                str3 = sb.toString();
                i++;
            } else {
                throw new XMPException("Invalid child of literal property element", XMPError.BADRDF);
            }
        }
        addChildNode.setValue(str3);
    }

    private static void rdf_ParseTypeLiteralPropertyElement() throws XMPException {
        throw new XMPException("ParseTypeLiteral property element not allowed", XMPError.BADXMP);
    }

    private static void rdf_ParseTypeResourcePropertyElement(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        XMPNode addChildNode = addChildNode(xMPMetaImpl, xMPNode, node, BuildConfig.FLAVOR, z);
        addChildNode.getOptions().setStruct(true);
        for (int i = 0; i < node.getAttributes().getLength(); i++) {
            Node item = node.getAttributes().item(i);
            String str = "xmlns";
            if (!str.equals(item.getPrefix()) && (item.getPrefix() != null || !str.equals(item.getNodeName()))) {
                String localName = item.getLocalName();
                String namespaceURI = item.getNamespaceURI();
                String nodeName = item.getNodeName();
                String str2 = XMPConst.XML_LANG;
                if (str2.equals(nodeName)) {
                    addQualifierNode(addChildNode, str2, item.getNodeValue());
                } else if (!XMPConst.NS_RDF.equals(namespaceURI) || (!"ID".equals(localName) && !"parseType".equals(localName))) {
                    throw new XMPException("Invalid attribute for ParseTypeResource property element", XMPError.BADRDF);
                }
            }
        }
        rdf_PropertyElementList(xMPMetaImpl, addChildNode, node, false);
        if (addChildNode.getHasValueChild()) {
            fixupQualifiedNode(addChildNode);
        }
    }

    private static void rdf_ParseTypeCollectionPropertyElement() throws XMPException {
        throw new XMPException("ParseTypeCollection property element not allowed", XMPError.BADXMP);
    }

    private static void rdf_ParseTypeOtherPropertyElement() throws XMPException {
        throw new XMPException("ParseTypeOther property element not allowed", XMPError.BADXMP);
    }

    /* JADX WARNING: Removed duplicated region for block: B:59:0x00ee  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void rdf_EmptyPropertyElement(com.adobe.xmp.impl.XMPMetaImpl r16, com.adobe.xmp.impl.XMPNode r17, org.w3c.dom.Node r18, boolean r19) throws com.adobe.xmp.XMPException {
        /*
            r0 = r16
            boolean r1 = r18.hasChildNodes()
            r2 = 202(0xca, float:2.83E-43)
            if (r1 != 0) goto L_0x015e
            r3 = 0
            r7 = r3
            r3 = 0
            r4 = 0
            r5 = 0
            r6 = 0
            r8 = 0
        L_0x0011:
            org.w3c.dom.NamedNodeMap r9 = r18.getAttributes()
            int r9 = r9.getLength()
            java.lang.String r10 = "Unrecognized attribute of empty property element"
            r11 = 6
            r12 = 5
            r13 = 2
            java.lang.String r14 = "xml:lang"
            java.lang.String r15 = "xmlns"
            if (r3 >= r9) goto L_0x00b3
            org.w3c.dom.NamedNodeMap r9 = r18.getAttributes()
            org.w3c.dom.Node r9 = r9.item(r3)
            java.lang.String r1 = r9.getPrefix()
            boolean r1 = r15.equals(r1)
            if (r1 != 0) goto L_0x00af
            java.lang.String r1 = r9.getPrefix()
            if (r1 != 0) goto L_0x0048
            java.lang.String r1 = r9.getNodeName()
            boolean r1 = r15.equals(r1)
            if (r1 == 0) goto L_0x0048
            goto L_0x00af
        L_0x0048:
            int r1 = getRDFTermKind(r9)
            java.lang.String r15 = "Empty property element can't have both rdf:value and rdf:resource"
            if (r1 == 0) goto L_0x007f
            if (r1 == r13) goto L_0x00af
            java.lang.String r13 = "Empty property element can't have both rdf:resource and rdf:nodeID"
            if (r1 == r12) goto L_0x0068
            if (r1 != r11) goto L_0x0062
            if (r5 != 0) goto L_0x005c
            r8 = 1
            goto L_0x00af
        L_0x005c:
            com.adobe.xmp.XMPException r0 = new com.adobe.xmp.XMPException
            r0.<init>(r13, r2)
            throw r0
        L_0x0062:
            com.adobe.xmp.XMPException r0 = new com.adobe.xmp.XMPException
            r0.<init>(r10, r2)
            throw r0
        L_0x0068:
            if (r8 != 0) goto L_0x0079
            if (r4 != 0) goto L_0x0071
            if (r4 != 0) goto L_0x006f
            r7 = r9
        L_0x006f:
            r5 = 1
            goto L_0x00af
        L_0x0071:
            com.adobe.xmp.XMPException r0 = new com.adobe.xmp.XMPException
            r1 = 203(0xcb, float:2.84E-43)
            r0.<init>(r15, r1)
            throw r0
        L_0x0079:
            com.adobe.xmp.XMPException r0 = new com.adobe.xmp.XMPException
            r0.<init>(r13, r2)
            throw r0
        L_0x007f:
            java.lang.String r1 = r9.getLocalName()
            java.lang.String r10 = "value"
            boolean r1 = r10.equals(r1)
            if (r1 == 0) goto L_0x00a4
            java.lang.String r1 = r9.getNamespaceURI()
            java.lang.String r10 = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
            boolean r1 = r10.equals(r1)
            if (r1 == 0) goto L_0x00a4
            if (r5 != 0) goto L_0x009c
            r7 = r9
            r4 = 1
            goto L_0x00af
        L_0x009c:
            com.adobe.xmp.XMPException r0 = new com.adobe.xmp.XMPException
            r1 = 203(0xcb, float:2.84E-43)
            r0.<init>(r15, r1)
            throw r0
        L_0x00a4:
            java.lang.String r1 = r9.getNodeName()
            boolean r1 = r14.equals(r1)
            if (r1 != 0) goto L_0x00af
            r6 = 1
        L_0x00af:
            int r3 = r3 + 1
            goto L_0x0011
        L_0x00b3:
            java.lang.String r1 = ""
            r3 = r17
            r8 = r18
            r9 = r19
            com.adobe.xmp.impl.XMPNode r3 = addChildNode(r0, r3, r8, r1, r9)
            if (r4 != 0) goto L_0x00cf
            if (r5 == 0) goto L_0x00c4
            goto L_0x00cf
        L_0x00c4:
            if (r6 == 0) goto L_0x00e2
            com.adobe.xmp.options.PropertyOptions r1 = r3.getOptions()
            r5 = 1
            r1.setStruct(r5)
            goto L_0x00e3
        L_0x00cf:
            r5 = 1
            if (r7 == 0) goto L_0x00d6
            java.lang.String r1 = r7.getNodeValue()
        L_0x00d6:
            r3.setValue(r1)
            if (r4 != 0) goto L_0x00e2
            com.adobe.xmp.options.PropertyOptions r1 = r3.getOptions()
            r1.setURI(r5)
        L_0x00e2:
            r5 = 0
        L_0x00e3:
            r1 = 0
        L_0x00e4:
            org.w3c.dom.NamedNodeMap r4 = r18.getAttributes()
            int r4 = r4.getLength()
            if (r1 >= r4) goto L_0x015d
            org.w3c.dom.NamedNodeMap r4 = r18.getAttributes()
            org.w3c.dom.Node r4 = r4.item(r1)
            if (r4 == r7) goto L_0x0159
            java.lang.String r6 = r4.getPrefix()
            boolean r6 = r15.equals(r6)
            if (r6 != 0) goto L_0x0159
            java.lang.String r6 = r4.getPrefix()
            if (r6 != 0) goto L_0x0113
            java.lang.String r6 = r4.getNodeName()
            boolean r6 = r15.equals(r6)
            if (r6 == 0) goto L_0x0113
            goto L_0x0159
        L_0x0113:
            int r6 = getRDFTermKind(r4)
            if (r6 == 0) goto L_0x0130
            if (r6 == r13) goto L_0x0159
            if (r6 == r12) goto L_0x0126
            if (r6 != r11) goto L_0x0120
            goto L_0x0159
        L_0x0120:
            com.adobe.xmp.XMPException r0 = new com.adobe.xmp.XMPException
            r0.<init>(r10, r2)
            throw r0
        L_0x0126:
            java.lang.String r4 = r4.getNodeValue()
            java.lang.String r6 = "rdf:resource"
            addQualifierNode(r3, r6, r4)
            goto L_0x0159
        L_0x0130:
            if (r5 != 0) goto L_0x013e
            java.lang.String r6 = r4.getNodeName()
            java.lang.String r4 = r4.getNodeValue()
            addQualifierNode(r3, r6, r4)
            goto L_0x0159
        L_0x013e:
            java.lang.String r6 = r4.getNodeName()
            boolean r6 = r14.equals(r6)
            if (r6 == 0) goto L_0x0150
            java.lang.String r4 = r4.getNodeValue()
            addQualifierNode(r3, r14, r4)
            goto L_0x0159
        L_0x0150:
            java.lang.String r6 = r4.getNodeValue()
            r9 = 0
            addChildNode(r0, r3, r4, r6, r9)
            goto L_0x015a
        L_0x0159:
            r9 = 0
        L_0x015a:
            int r1 = r1 + 1
            goto L_0x00e4
        L_0x015d:
            return
        L_0x015e:
            com.adobe.xmp.XMPException r0 = new com.adobe.xmp.XMPException
            java.lang.String r1 = "Nested content not allowed with rdf:resource or property attributes"
            r0.<init>(r1, r2)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.adobe.xmp.impl.ParseRDF.rdf_EmptyPropertyElement(com.adobe.xmp.impl.XMPMetaImpl, com.adobe.xmp.impl.XMPNode, org.w3c.dom.Node, boolean):void");
    }

    private static XMPNode addChildNode(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, String str, boolean z) throws XMPException {
        XMPSchemaRegistry schemaRegistry = XMPMetaFactory.getSchemaRegistry();
        String namespaceURI = node.getNamespaceURI();
        if (namespaceURI != null) {
            if (XMPConst.NS_DC_DEPRECATED.equals(namespaceURI)) {
                namespaceURI = XMPConst.NS_DC;
            }
            String namespacePrefix = schemaRegistry.getNamespacePrefix(namespaceURI);
            String str2 = DEFAULT_PREFIX;
            if (namespacePrefix == null) {
                namespacePrefix = schemaRegistry.registerNamespace(namespaceURI, node.getPrefix() != null ? node.getPrefix() : str2);
            }
            StringBuilder sb = new StringBuilder();
            sb.append(namespacePrefix);
            sb.append(node.getLocalName());
            String sb2 = sb.toString();
            PropertyOptions propertyOptions = new PropertyOptions();
            boolean z2 = false;
            if (z) {
                xMPNode = XMPNodeUtils.findSchemaNode(xMPMetaImpl.getRoot(), namespaceURI, str2, true);
                xMPNode.setImplicit(false);
                if (schemaRegistry.findAlias(sb2) != null) {
                    xMPMetaImpl.getRoot().setHasAliases(true);
                    xMPNode.setHasAliases(true);
                    z2 = true;
                }
            }
            boolean equals = "rdf:li".equals(sb2);
            boolean equals2 = "rdf:value".equals(sb2);
            XMPNode xMPNode2 = new XMPNode(sb2, str, propertyOptions);
            xMPNode2.setAlias(z2);
            if (!equals2) {
                xMPNode.addChild(xMPNode2);
            } else {
                xMPNode.addChild(1, xMPNode2);
            }
            if (equals2) {
                if (z || !xMPNode.getOptions().isStruct()) {
                    throw new XMPException("Misplaced rdf:value element", XMPError.BADRDF);
                }
                xMPNode.setHasValueChild(true);
            }
            if (equals) {
                if (xMPNode.getOptions().isArray()) {
                    xMPNode2.setName(XMPConst.ARRAY_ITEM_NAME);
                } else {
                    throw new XMPException("Misplaced rdf:li element", XMPError.BADRDF);
                }
            }
            return xMPNode2;
        }
        throw new XMPException("XML namespace required for all elements and attributes", XMPError.BADRDF);
    }

    private static XMPNode addQualifierNode(XMPNode xMPNode, String str, String str2) throws XMPException {
        if (XMPConst.XML_LANG.equals(str)) {
            str2 = Utils.normalizeLangValue(str2);
        }
        XMPNode xMPNode2 = new XMPNode(str, str2, null);
        xMPNode.addQualifier(xMPNode2);
        return xMPNode2;
    }

    private static void fixupQualifiedNode(XMPNode xMPNode) throws XMPException {
        XMPNode child = xMPNode.getChild(1);
        if (child.getOptions().getHasLanguage()) {
            if (!xMPNode.getOptions().getHasLanguage()) {
                XMPNode qualifier = child.getQualifier(1);
                child.removeQualifier(qualifier);
                xMPNode.addQualifier(qualifier);
            } else {
                throw new XMPException("Redundant xml:lang for rdf:value element", XMPError.BADXMP);
            }
        }
        for (int i = 1; i <= child.getQualifierLength(); i++) {
            xMPNode.addQualifier(child.getQualifier(i));
        }
        for (int i2 = 2; i2 <= xMPNode.getChildrenLength(); i2++) {
            xMPNode.addQualifier(xMPNode.getChild(i2));
        }
        xMPNode.setHasValueChild(false);
        xMPNode.getOptions().setStruct(false);
        xMPNode.getOptions().mergeWith(child.getOptions());
        xMPNode.setValue(child.getValue());
        xMPNode.removeChildren();
        Iterator iterateChildren = child.iterateChildren();
        while (iterateChildren.hasNext()) {
            xMPNode.addChild((XMPNode) iterateChildren.next());
        }
    }

    private static boolean isWhitespaceNode(Node node) {
        if (node.getNodeType() != 3) {
            return false;
        }
        String nodeValue = node.getNodeValue();
        for (int i = 0; i < nodeValue.length(); i++) {
            if (!Character.isWhitespace(nodeValue.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPropertyElementName(int i) {
        if (i == 8 || isOldTerm(i)) {
            return false;
        }
        return !isCoreSyntaxTerm(i);
    }

    private static int getRDFTermKind(Node node) {
        String localName = node.getLocalName();
        String namespaceURI = node.getNamespaceURI();
        String str = "ID";
        String str2 = "about";
        String str3 = XMPConst.NS_RDF;
        if (namespaceURI == null && ((str2.equals(localName) || str.equals(localName)) && (node instanceof Attr) && str3.equals(((Attr) node).getOwnerElement().getNamespaceURI()))) {
            namespaceURI = str3;
        }
        if (str3.equals(namespaceURI)) {
            if ("li".equals(localName)) {
                return 9;
            }
            if ("parseType".equals(localName)) {
                return 4;
            }
            if ("Description".equals(localName)) {
                return 8;
            }
            if (str2.equals(localName)) {
                return 3;
            }
            if ("resource".equals(localName)) {
                return 5;
            }
            if ("RDF".equals(localName)) {
                return 1;
            }
            if (str.equals(localName)) {
                return 2;
            }
            if ("nodeID".equals(localName)) {
                return 6;
            }
            if ("datatype".equals(localName)) {
                return 7;
            }
            if ("aboutEach".equals(localName)) {
                return 10;
            }
            if ("aboutEachPrefix".equals(localName)) {
                return 11;
            }
            if ("bagID".equals(localName)) {
                return 12;
            }
        }
        return 0;
    }
}
