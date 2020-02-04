package com.adobe.xmp.impl.xpath;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.impl.Utils;
import com.adobe.xmp.properties.XMPAliasInfo;

public final class XMPPathParser {
    private XMPPathParser() {
    }

    public static XMPPath expandXPath(String str, String str2) throws XMPException {
        XMPPathSegment xMPPathSegment;
        if (str == null || str2 == null) {
            throw new XMPException("Parameter must not be null", 4);
        }
        XMPPath xMPPath = new XMPPath();
        PathPosition pathPosition = new PathPosition();
        pathPosition.path = str2;
        parseRootNode(str, pathPosition, xMPPath);
        while (pathPosition.stepEnd < str2.length()) {
            pathPosition.stepBegin = pathPosition.stepEnd;
            skipPathDelimiter(str2, pathPosition);
            int i = pathPosition.stepBegin;
            pathPosition.stepEnd = i;
            if (str2.charAt(i) != '[') {
                xMPPathSegment = parseStructSegment(pathPosition);
            } else {
                xMPPathSegment = parseIndexSegment(pathPosition);
            }
            String str3 = "Only xml:lang allowed with '@'";
            if (xMPPathSegment.getKind() == 1) {
                if (xMPPathSegment.getName().charAt(0) == '@') {
                    StringBuilder sb = new StringBuilder();
                    sb.append("?");
                    sb.append(xMPPathSegment.getName().substring(1));
                    xMPPathSegment.setName(sb.toString());
                    if (!"?xml:lang".equals(xMPPathSegment.getName())) {
                        throw new XMPException(str3, 102);
                    }
                }
                if (xMPPathSegment.getName().charAt(0) == '?') {
                    pathPosition.nameStart++;
                    xMPPathSegment.setKind(2);
                }
                verifyQualName(pathPosition.path.substring(pathPosition.nameStart, pathPosition.nameEnd));
            } else if (xMPPathSegment.getKind() != 6) {
                continue;
            } else {
                if (xMPPathSegment.getName().charAt(1) == '@') {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("[?");
                    sb2.append(xMPPathSegment.getName().substring(2));
                    xMPPathSegment.setName(sb2.toString());
                    if (!xMPPathSegment.getName().startsWith("[?xml:lang=")) {
                        throw new XMPException(str3, 102);
                    }
                }
                if (xMPPathSegment.getName().charAt(1) == '?') {
                    pathPosition.nameStart++;
                    xMPPathSegment.setKind(5);
                    verifyQualName(pathPosition.path.substring(pathPosition.nameStart, pathPosition.nameEnd));
                }
            }
            xMPPath.add(xMPPathSegment);
        }
        return xMPPath;
    }

    private static void skipPathDelimiter(String str, PathPosition pathPosition) throws XMPException {
        if (str.charAt(pathPosition.stepBegin) == '/') {
            pathPosition.stepBegin++;
            if (pathPosition.stepBegin >= str.length()) {
                throw new XMPException("Empty XMPPath segment", 102);
            }
        }
        if (str.charAt(pathPosition.stepBegin) == '*') {
            pathPosition.stepBegin++;
            if (pathPosition.stepBegin >= str.length() || str.charAt(pathPosition.stepBegin) != '[') {
                throw new XMPException("Missing '[' after '*'", 102);
            }
        }
    }

    private static XMPPathSegment parseStructSegment(PathPosition pathPosition) throws XMPException {
        pathPosition.nameStart = pathPosition.stepBegin;
        while (pathPosition.stepEnd < pathPosition.path.length()) {
            if ("/[*".indexOf(pathPosition.path.charAt(pathPosition.stepEnd)) >= 0) {
                break;
            }
            pathPosition.stepEnd++;
        }
        int i = pathPosition.stepEnd;
        pathPosition.nameEnd = i;
        int i2 = pathPosition.stepBegin;
        if (i != i2) {
            return new XMPPathSegment(pathPosition.path.substring(i2, i), 1);
        }
        throw new XMPException("Empty XMPPath segment", 102);
    }

    private static XMPPathSegment parseIndexSegment(PathPosition pathPosition) throws XMPException {
        XMPPathSegment xMPPathSegment;
        pathPosition.stepEnd++;
        if ('0' > pathPosition.path.charAt(pathPosition.stepEnd) || pathPosition.path.charAt(pathPosition.stepEnd) > '9') {
            while (pathPosition.stepEnd < pathPosition.path.length() && pathPosition.path.charAt(pathPosition.stepEnd) != ']' && pathPosition.path.charAt(pathPosition.stepEnd) != '=') {
                pathPosition.stepEnd++;
            }
            if (pathPosition.stepEnd >= pathPosition.path.length()) {
                throw new XMPException("Missing ']' or '=' for array index", 102);
            } else if (pathPosition.path.charAt(pathPosition.stepEnd) == ']') {
                if ("[last()".equals(pathPosition.path.substring(pathPosition.stepBegin, pathPosition.stepEnd))) {
                    xMPPathSegment = new XMPPathSegment(null, 4);
                } else {
                    throw new XMPException("Invalid non-numeric array index", 102);
                }
            } else {
                pathPosition.nameStart = pathPosition.stepBegin + 1;
                int i = pathPosition.stepEnd;
                pathPosition.nameEnd = i;
                pathPosition.stepEnd = i + 1;
                char charAt = pathPosition.path.charAt(pathPosition.stepEnd);
                if (charAt == '\'' || charAt == '\"') {
                    pathPosition.stepEnd++;
                    while (pathPosition.stepEnd < pathPosition.path.length()) {
                        if (pathPosition.path.charAt(pathPosition.stepEnd) == charAt) {
                            if (pathPosition.stepEnd + 1 >= pathPosition.path.length() || pathPosition.path.charAt(pathPosition.stepEnd + 1) != charAt) {
                                break;
                            }
                            pathPosition.stepEnd++;
                        }
                        pathPosition.stepEnd++;
                    }
                    if (pathPosition.stepEnd < pathPosition.path.length()) {
                        pathPosition.stepEnd++;
                        xMPPathSegment = new XMPPathSegment(null, 6);
                    } else {
                        throw new XMPException("No terminating quote for array selector", 102);
                    }
                } else {
                    throw new XMPException("Invalid quote in array selector", 102);
                }
            }
        } else {
            while (pathPosition.stepEnd < pathPosition.path.length() && '0' <= pathPosition.path.charAt(pathPosition.stepEnd) && pathPosition.path.charAt(pathPosition.stepEnd) <= '9') {
                pathPosition.stepEnd++;
            }
            xMPPathSegment = new XMPPathSegment(null, 3);
        }
        if (pathPosition.stepEnd >= pathPosition.path.length() || pathPosition.path.charAt(pathPosition.stepEnd) != ']') {
            throw new XMPException("Missing ']' for array index", 102);
        }
        pathPosition.stepEnd++;
        xMPPathSegment.setName(pathPosition.path.substring(pathPosition.stepBegin, pathPosition.stepEnd));
        return xMPPathSegment;
    }

    private static void parseRootNode(String str, PathPosition pathPosition, XMPPath xMPPath) throws XMPException {
        while (pathPosition.stepEnd < pathPosition.path.length()) {
            if ("/[*".indexOf(pathPosition.path.charAt(pathPosition.stepEnd)) >= 0) {
                break;
            }
            pathPosition.stepEnd++;
        }
        int i = pathPosition.stepEnd;
        int i2 = pathPosition.stepBegin;
        if (i != i2) {
            String verifyXPathRoot = verifyXPathRoot(str, pathPosition.path.substring(i2, i));
            XMPAliasInfo findAlias = XMPMetaFactory.getSchemaRegistry().findAlias(verifyXPathRoot);
            if (findAlias == null) {
                xMPPath.add(new XMPPathSegment(str, Integer.MIN_VALUE));
                xMPPath.add(new XMPPathSegment(verifyXPathRoot, 1));
                return;
            }
            xMPPath.add(new XMPPathSegment(findAlias.getNamespace(), Integer.MIN_VALUE));
            XMPPathSegment xMPPathSegment = new XMPPathSegment(verifyXPathRoot(findAlias.getNamespace(), findAlias.getPropName()), 1);
            xMPPathSegment.setAlias(true);
            xMPPathSegment.setAliasForm(findAlias.getAliasForm().getOptions());
            xMPPath.add(xMPPathSegment);
            if (findAlias.getAliasForm().isArrayAltText()) {
                XMPPathSegment xMPPathSegment2 = new XMPPathSegment("[?xml:lang='x-default']", 5);
                xMPPathSegment2.setAlias(true);
                xMPPathSegment2.setAliasForm(findAlias.getAliasForm().getOptions());
                xMPPath.add(xMPPathSegment2);
            } else if (findAlias.getAliasForm().isArray()) {
                XMPPathSegment xMPPathSegment3 = new XMPPathSegment("[1]", 3);
                xMPPathSegment3.setAlias(true);
                xMPPathSegment3.setAliasForm(findAlias.getAliasForm().getOptions());
                xMPPath.add(xMPPathSegment3);
            }
        } else {
            throw new XMPException("Empty initial XMPPath step", 102);
        }
    }

    private static void verifyQualName(String str) throws XMPException {
        int indexOf = str.indexOf(58);
        if (indexOf > 0) {
            String substring = str.substring(0, indexOf);
            if (Utils.isXMLNameNS(substring)) {
                if (XMPMetaFactory.getSchemaRegistry().getNamespaceURI(substring) == null) {
                    throw new XMPException("Unknown namespace prefix for qualified name", 102);
                }
                return;
            }
        }
        throw new XMPException("Ill-formed qualified name", 102);
    }

    private static void verifySimpleXMLName(String str) throws XMPException {
        if (!Utils.isXMLName(str)) {
            throw new XMPException("Bad XML name", 102);
        }
    }

    private static String verifyXPathRoot(String str, String str2) throws XMPException {
        if (str == null || str.length() == 0) {
            throw new XMPException("Schema namespace URI is required", 101);
        } else if (str2.charAt(0) == '?' || str2.charAt(0) == '@') {
            throw new XMPException("Top level name must not be a qualifier", 102);
        } else if (str2.indexOf(47) >= 0 || str2.indexOf(91) >= 0) {
            throw new XMPException("Top level name must be simple", 102);
        } else {
            String namespacePrefix = XMPMetaFactory.getSchemaRegistry().getNamespacePrefix(str);
            if (namespacePrefix != null) {
                int indexOf = str2.indexOf(58);
                if (indexOf < 0) {
                    verifySimpleXMLName(str2);
                    StringBuilder sb = new StringBuilder();
                    sb.append(namespacePrefix);
                    sb.append(str2);
                    return sb.toString();
                }
                verifySimpleXMLName(str2.substring(0, indexOf));
                verifySimpleXMLName(str2.substring(indexOf));
                String substring = str2.substring(0, indexOf + 1);
                String namespacePrefix2 = XMPMetaFactory.getSchemaRegistry().getNamespacePrefix(str);
                if (namespacePrefix2 == null) {
                    throw new XMPException("Unknown schema namespace prefix", 101);
                } else if (substring.equals(namespacePrefix2)) {
                    return str2;
                } else {
                    throw new XMPException("Schema namespace URI and prefix mismatch", 101);
                }
            } else {
                throw new XMPException("Unregistered schema namespace URI", 101);
            }
        }
    }
}
