package biojavaExtensions;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.biojavax.bio.phylo.io.nexus.NexusComment;

/**
 * Contains some 'stuff' from a nexus file.
 * Will be one of:
 *   +  a sequence of whitespace characters (not part of a comment, field name or field value)
 *   +  a comment (a NexusComment object)
 *   +  a field (name/value pair, plus 'separator' string which is either an "=" (with optional whitespace,
 *      no \n before the '=") or whitespace including a "\n". 
 * @author woodhams
 *
 */

// TODO: Often code specifically wants a field, so should create subclasses.
public class GenericNexusStuff {
	public enum stuffType {WHITE, COMMENT, FIELD};
	public stuffType type;
	private NexusComment comment; // null unless type==COMMENT 
	private String value;  // null if type==COMMENT
	private String key; // null unless type==FIELD
	private String separator; // null unless type==FIELD;
	
	public GenericNexusStuff(NexusComment comment) {
		type = stuffType.COMMENT;
		this.comment = comment;
		value = null;
		key = null;
		separator = null;
	}
	
	public GenericNexusStuff(String whitespace) {
		type = stuffType.WHITE;
		comment = null;
		value = whitespace;
		key = null;
		separator = null;
	}
	
	public GenericNexusStuff(String key, String separator, String value) {
		type = stuffType.FIELD;
		comment = null;
		this.key = key;
		this.separator = separator;
		this.value = value;
	}
	
	/**
	 * This works on fields only.
	 * Strip out superfluous whitespace from 'value',
	 * precede each separate line by two tabs. 
	 */
	public void reformat() {
		// Would be more efficient if I figured out how to use a StringBuffer for this instead.
		String temp = value.replaceAll("\t", ""); // strip all tabs
		temp = temp.replaceAll("  +", " "); // collapse multiple spaces into one
		temp = temp.replaceAll("\n ", "\n"); // strip spaces after newline
		temp = temp.replaceAll("\n", "\n\t\t"); // Add two tabs after newline
		if (separator.equals("\n")) {temp = "\t\t"+temp;} // optional two tabs at start
		temp = temp.replace("\t\t$", "\t"); // Reduce indent on end of multi-line value.
		value = temp;
	}
	
	public boolean isField()   { return type == stuffType.FIELD; }
	public boolean isWhite()   { return type == stuffType.WHITE; }
	public boolean isComment() { return type == stuffType.COMMENT; }
	
	public String getKey() { return key; }               // will be 'null' if not FIELD
	public String getSeparator() { return separator; }   // will be 'null' if not FIELD
	public String getValue() { return value; }           // will be 'null' if COMMENT
	public NexusComment getComment() { return comment; } // will be 'null' if not COMMENT
	
	public void writeObject(final Writer writer) throws IOException {
		switch (type) {
		case WHITE : 
			writer.write(value);
			break;
		case FIELD : 
			writer.write(key);
			writer.write(' ');
			writer.write(separator);
			writer.write(value);
			writer.write(";");
			// newlines need to be added explicitly as whitespace objects.
			break;
		case COMMENT : 
			comment.writeObject(writer);
			break;
		}
	}
	
	@Override
	public String toString() {
		switch (type) {
			case WHITE : return value;
			case FIELD : return key+" "+separator+value;
			case COMMENT : 
				Writer sw = new StringWriter();
				try {
					comment.writeObject(sw);
					sw.close();
				} catch (IOException e) {
				}
				return sw.toString();
			default: return null; // needed to make Eclipse believe a string is always returned.	
		}		
	}
}
