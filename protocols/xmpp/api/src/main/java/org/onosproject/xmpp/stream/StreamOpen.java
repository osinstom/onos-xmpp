package org.onosproject.xmpp.stream;

import org.dom4j.Attribute;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xmpp.packet.JID;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * Created by autonet on 12.09.17.
 */
public class StreamOpen implements StreamEvent {

    public static final String QNAME = "stream";

    private Element element;

    public StreamOpen(Element element) {
        this.element = element;
    }

    @Override
    public String toXML() {
        StringWriter out = new StringWriter();
        XMLWriter writer = new XMLWriter(out, OutputFormat.createCompactFormat());
        try {
            out.write("<");
            writer.write(element.getQualifiedName());
            for(Attribute attr : (List<Attribute>) element.attributes()) {
                writer.write(attr);
            }
            writer.write(Namespace.get(this.element.getNamespacePrefix(), this.element.getNamespaceURI()));
            writer.write(Namespace.get("jabber:client"));
            out.write(">");
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        return out.toString();
    }

    public JID getFromJID() {
        String jid = this.element.attribute("from").getValue();
        return new JID(jid);
    }

    public Element getElement() {
        return this.element;
    }

    public JID getToJID() {
        String jid = this.element.attribute("to").getValue();
        return new JID(jid);
    }
}
