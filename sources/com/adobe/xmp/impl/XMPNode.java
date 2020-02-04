package com.adobe.xmp.impl;

import com.adobe.xmp.XMPConst;
import com.adobe.xmp.XMPError;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.options.PropertyOptions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class XMPNode implements Comparable {
    private boolean alias;
    private List children;
    private boolean hasAliases;
    private boolean hasValueChild;
    private boolean implicit;
    private String name;
    private PropertyOptions options;
    private XMPNode parent;
    private List qualifier;
    private String value;

    public XMPNode(String str, String str2, PropertyOptions propertyOptions) {
        this.children = null;
        this.qualifier = null;
        this.options = null;
        this.name = str;
        this.value = str2;
        this.options = propertyOptions;
    }

    public XMPNode(String str, PropertyOptions propertyOptions) {
        this(str, null, propertyOptions);
    }

    public void clear() {
        this.options = null;
        this.name = null;
        this.value = null;
        this.children = null;
        this.qualifier = null;
    }

    public XMPNode getParent() {
        return this.parent;
    }

    public XMPNode getChild(int i) {
        return (XMPNode) getChildren().get(i - 1);
    }

    public void addChild(XMPNode xMPNode) throws XMPException {
        assertChildNotExisting(xMPNode.getName());
        xMPNode.setParent(this);
        getChildren().add(xMPNode);
    }

    public void addChild(int i, XMPNode xMPNode) throws XMPException {
        assertChildNotExisting(xMPNode.getName());
        xMPNode.setParent(this);
        getChildren().add(i - 1, xMPNode);
    }

    public void replaceChild(int i, XMPNode xMPNode) {
        xMPNode.setParent(this);
        getChildren().set(i - 1, xMPNode);
    }

    public void removeChild(int i) {
        getChildren().remove(i - 1);
        cleanupChildren();
    }

    public void removeChild(XMPNode xMPNode) {
        getChildren().remove(xMPNode);
        cleanupChildren();
    }

    /* access modifiers changed from: protected */
    public void cleanupChildren() {
        if (this.children.isEmpty()) {
            this.children = null;
        }
    }

    public void removeChildren() {
        this.children = null;
    }

    public int getChildrenLength() {
        List list = this.children;
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public XMPNode findChildByName(String str) {
        return find(getChildren(), str);
    }

    public XMPNode getQualifier(int i) {
        return (XMPNode) getQualifier().get(i - 1);
    }

    public int getQualifierLength() {
        List list = this.qualifier;
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public void addQualifier(XMPNode xMPNode) throws XMPException {
        assertQualifierNotExisting(xMPNode.getName());
        xMPNode.setParent(this);
        xMPNode.getOptions().setQualifier(true);
        getOptions().setHasQualifiers(true);
        if (xMPNode.isLanguageNode()) {
            this.options.setHasLanguage(true);
            getQualifier().add(0, xMPNode);
        } else if (xMPNode.isTypeNode()) {
            this.options.setHasType(true);
            getQualifier().add(this.options.getHasLanguage() ? 1 : 0, xMPNode);
        } else {
            getQualifier().add(xMPNode);
        }
    }

    public void removeQualifier(XMPNode xMPNode) {
        PropertyOptions options2 = getOptions();
        if (xMPNode.isLanguageNode()) {
            options2.setHasLanguage(false);
        } else if (xMPNode.isTypeNode()) {
            options2.setHasType(false);
        }
        getQualifier().remove(xMPNode);
        if (this.qualifier.isEmpty()) {
            options2.setHasQualifiers(false);
            this.qualifier = null;
        }
    }

    public void removeQualifiers() {
        PropertyOptions options2 = getOptions();
        options2.setHasQualifiers(false);
        options2.setHasLanguage(false);
        options2.setHasType(false);
        this.qualifier = null;
    }

    public XMPNode findQualifierByName(String str) {
        return find(this.qualifier, str);
    }

    public boolean hasChildren() {
        List list = this.children;
        return list != null && list.size() > 0;
    }

    public Iterator iterateChildren() {
        if (this.children != null) {
            return getChildren().iterator();
        }
        return Collections.EMPTY_LIST.listIterator();
    }

    public boolean hasQualifier() {
        List list = this.qualifier;
        return list != null && list.size() > 0;
    }

    public Iterator iterateQualifier() {
        if (this.qualifier == null) {
            return Collections.EMPTY_LIST.iterator();
        }
        final Iterator it = getQualifier().iterator();
        return new Iterator() {
            public boolean hasNext() {
                return it.hasNext();
            }

            public Object next() {
                return it.next();
            }

            public void remove() {
                throw new UnsupportedOperationException("remove() is not allowed due to the internal contraints");
            }
        };
    }

    public Object clone() {
        PropertyOptions propertyOptions;
        try {
            propertyOptions = new PropertyOptions(getOptions().getOptions());
        } catch (XMPException unused) {
            propertyOptions = new PropertyOptions();
        }
        XMPNode xMPNode = new XMPNode(this.name, this.value, propertyOptions);
        cloneSubtree(xMPNode);
        return xMPNode;
    }

    public void cloneSubtree(XMPNode xMPNode) {
        try {
            Iterator iterateChildren = iterateChildren();
            while (iterateChildren.hasNext()) {
                xMPNode.addChild((XMPNode) ((XMPNode) iterateChildren.next()).clone());
            }
            Iterator iterateQualifier = iterateQualifier();
            while (iterateQualifier.hasNext()) {
                xMPNode.addQualifier((XMPNode) ((XMPNode) iterateQualifier.next()).clone());
            }
        } catch (XMPException unused) {
        }
    }

    public String dumpNode(boolean z) {
        StringBuffer stringBuffer = new StringBuffer(512);
        dumpNode(stringBuffer, z, 0, 0);
        return stringBuffer.toString();
    }

    public int compareTo(Object obj) {
        if (getOptions().isSchemaNode()) {
            return this.value.compareTo(((XMPNode) obj).getValue());
        }
        return this.name.compareTo(((XMPNode) obj).getName());
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String str) {
        this.value = str;
    }

    public PropertyOptions getOptions() {
        if (this.options == null) {
            this.options = new PropertyOptions();
        }
        return this.options;
    }

    public void setOptions(PropertyOptions propertyOptions) {
        this.options = propertyOptions;
    }

    public boolean isImplicit() {
        return this.implicit;
    }

    public void setImplicit(boolean z) {
        this.implicit = z;
    }

    public boolean getHasAliases() {
        return this.hasAliases;
    }

    public void setHasAliases(boolean z) {
        this.hasAliases = z;
    }

    public boolean isAlias() {
        return this.alias;
    }

    public void setAlias(boolean z) {
        this.alias = z;
    }

    public boolean getHasValueChild() {
        return this.hasValueChild;
    }

    public void setHasValueChild(boolean z) {
        this.hasValueChild = z;
    }

    public void sort() {
        if (hasQualifier()) {
            XMPNode[] xMPNodeArr = (XMPNode[]) getQualifier().toArray(new XMPNode[getQualifierLength()]);
            int i = 0;
            while (xMPNodeArr.length > i) {
                if (!XMPConst.XML_LANG.equals(xMPNodeArr[i].getName())) {
                    if (!XMPConst.RDF_TYPE.equals(xMPNodeArr[i].getName())) {
                        break;
                    }
                }
                xMPNodeArr[i].sort();
                i++;
            }
            Arrays.sort(xMPNodeArr, i, xMPNodeArr.length);
            ListIterator listIterator = this.qualifier.listIterator();
            for (int i2 = 0; i2 < xMPNodeArr.length; i2++) {
                listIterator.next();
                listIterator.set(xMPNodeArr[i2]);
                xMPNodeArr[i2].sort();
            }
        }
        if (hasChildren()) {
            if (!getOptions().isArray()) {
                Collections.sort(this.children);
            }
            Iterator iterateChildren = iterateChildren();
            while (iterateChildren.hasNext()) {
                ((XMPNode) iterateChildren.next()).sort();
            }
        }
    }

    private void dumpNode(StringBuffer stringBuffer, boolean z, int i, int i2) {
        int i3 = 0;
        for (int i4 = 0; i4 < i; i4++) {
            stringBuffer.append(9);
        }
        if (this.parent == null) {
            stringBuffer.append("ROOT NODE");
            String str = this.name;
            if (str != null && str.length() > 0) {
                stringBuffer.append(" (");
                stringBuffer.append(this.name);
                stringBuffer.append(')');
            }
        } else if (getOptions().isQualifier()) {
            stringBuffer.append('?');
            stringBuffer.append(this.name);
        } else if (getParent().getOptions().isArray()) {
            stringBuffer.append('[');
            stringBuffer.append(i2);
            stringBuffer.append(']');
        } else {
            stringBuffer.append(this.name);
        }
        String str2 = this.value;
        if (str2 != null && str2.length() > 0) {
            stringBuffer.append(" = \"");
            stringBuffer.append(this.value);
            stringBuffer.append('\"');
        }
        if (getOptions().containsOneOf(-1)) {
            stringBuffer.append("\t(");
            stringBuffer.append(getOptions().toString());
            stringBuffer.append(" : ");
            stringBuffer.append(getOptions().getOptionsString());
            stringBuffer.append(')');
        }
        stringBuffer.append(10);
        if (z && hasQualifier()) {
            XMPNode[] xMPNodeArr = (XMPNode[]) getQualifier().toArray(new XMPNode[getQualifierLength()]);
            int i5 = 0;
            while (xMPNodeArr.length > i5) {
                if (!XMPConst.XML_LANG.equals(xMPNodeArr[i5].getName())) {
                    if (!XMPConst.RDF_TYPE.equals(xMPNodeArr[i5].getName())) {
                        break;
                    }
                }
                i5++;
            }
            Arrays.sort(xMPNodeArr, i5, xMPNodeArr.length);
            int i6 = 0;
            while (i6 < xMPNodeArr.length) {
                i6++;
                xMPNodeArr[i6].dumpNode(stringBuffer, z, i + 2, i6);
            }
        }
        if (z && hasChildren()) {
            XMPNode[] xMPNodeArr2 = (XMPNode[]) getChildren().toArray(new XMPNode[getChildrenLength()]);
            if (!getOptions().isArray()) {
                Arrays.sort(xMPNodeArr2);
            }
            while (i3 < xMPNodeArr2.length) {
                i3++;
                xMPNodeArr2[i3].dumpNode(stringBuffer, z, i + 1, i3);
            }
        }
    }

    private boolean isLanguageNode() {
        return XMPConst.XML_LANG.equals(this.name);
    }

    private boolean isTypeNode() {
        return XMPConst.RDF_TYPE.equals(this.name);
    }

    private List getChildren() {
        if (this.children == null) {
            this.children = new ArrayList(0);
        }
        return this.children;
    }

    public List getUnmodifiableChildren() {
        return Collections.unmodifiableList(new ArrayList(getChildren()));
    }

    private List getQualifier() {
        if (this.qualifier == null) {
            this.qualifier = new ArrayList(0);
        }
        return this.qualifier;
    }

    /* access modifiers changed from: protected */
    public void setParent(XMPNode xMPNode) {
        this.parent = xMPNode;
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=java.util.List, code=java.util.List<com.adobe.xmp.impl.XMPNode>, for r2v0, types: [java.util.List, java.util.List<com.adobe.xmp.impl.XMPNode>] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.adobe.xmp.impl.XMPNode find(java.util.List<com.adobe.xmp.impl.XMPNode> r2, java.lang.String r3) {
        /*
            r1 = this;
            if (r2 == 0) goto L_0x001d
            java.util.Iterator r1 = r2.iterator()
        L_0x0006:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x001d
            java.lang.Object r2 = r1.next()
            com.adobe.xmp.impl.XMPNode r2 = (com.adobe.xmp.impl.XMPNode) r2
            java.lang.String r0 = r2.getName()
            boolean r0 = r0.equals(r3)
            if (r0 == 0) goto L_0x0006
            return r2
        L_0x001d:
            r1 = 0
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.adobe.xmp.impl.XMPNode.find(java.util.List, java.lang.String):com.adobe.xmp.impl.XMPNode");
    }

    private void assertChildNotExisting(String str) throws XMPException {
        if (!XMPConst.ARRAY_ITEM_NAME.equals(str) && findChildByName(str) != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Duplicate property or field node '");
            sb.append(str);
            sb.append("'");
            throw new XMPException(sb.toString(), XMPError.BADXMP);
        }
    }

    private void assertQualifierNotExisting(String str) throws XMPException {
        if (!XMPConst.ARRAY_ITEM_NAME.equals(str) && findQualifierByName(str) != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Duplicate '");
            sb.append(str);
            sb.append("' qualifier");
            throw new XMPException(sb.toString(), XMPError.BADXMP);
        }
    }
}
