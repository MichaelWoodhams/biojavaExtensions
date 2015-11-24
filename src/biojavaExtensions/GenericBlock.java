package biojavaExtensions;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.biojava.bio.seq.io.ParseException;
import org.biojavax.bio.phylo.io.nexus.NexusBlock;
import org.biojavax.bio.phylo.io.nexus.NexusComment;

/**
 * Loosely based on Biojava 1.8.4 NexusFileBuilder.UnknownBlockParser.UnknownBlockBuilder.UnknownBlock
 * 
 * @author woodhams
 *
 */

/*
 * Specifying a list of valid keys is optional: if no set of
 * valid keys passed to constructor, and addValidKey never called,
 * all keys are valid. 
 * Key names are case sensitive and whitespace sensitive. 
 * (Possible TODO: add a case-sensitivity option)
 * 
 * TODO: I'm not happy with how checking validity of keys works. Current
 * method requires try/catch on adding fields, even when there is no
 * validity checking going on.
 */
public class GenericBlock extends NexusBlock.Abstract {
	private Vector<GenericNexusStuff> entries;
	private Set<String> validKeys = null;
	
	/*
	public void addValidKey(String key) {
		if (validKeys == null) validKeys = new HashSet<String>();
		validKeys.add(key);
	}
	 */
	
	public GenericBlock(String blockName) {
		super(blockName);
		entries = new Vector<GenericNexusStuff>();
	}
	
	public GenericBlock(String blockName, Set<String> validKeys) {
		this(blockName);
		this.validKeys = validKeys;
	}
	
	public void addComment(NexusComment comment) {
		entries.add(new GenericNexusStuff(comment));
	}
	
	public void addWhitespace(String white) {
		entries.add(new GenericNexusStuff(white));
	}
	
	public void addStuff(GenericNexusStuff stuff) {
		entries.add(stuff);
	}
	
	public void addField(String key, String separator, String value) throws ParseException {
		if (validKeys!=null && !validKeys.contains(key)) 
			throw new ParseException("In block "+this.getBlockName()+", found invalid field name '"+key+"'");
		entries.add(new GenericNexusStuff(key,separator,value));
	}
	
	/*
	 * Ideas for iterators:
	 * Iterator<String> fieldNameIterator
	 * Iterator<Map.Entry<String,String>> fieldIterator
	 * Iterator<GenericNexusStuff> stuffIterator
	 * Iterator<NexusComment> commentIterator
	 */
	public Iterator<GenericNexusStuff> stuffIterator() {
		return entries.listIterator();
	}
	
	/**
	 * Returns true if it was able to find and remove a field with name <key>.
	 * If whitespace immediately precedes the removed field, the whitespace
	 * is also removed.
	 * @param key
	 * @return
	 */
	public boolean removeField(String key) {
		int index = keyIndex(key);
		if (index == -1) {
			return false;
		} else {
			entries.remove(index);
			if (index>0 && entries.get(index-1).isWhite()) {
				entries.remove(index-1);
			}
		}
		return true;
	}
	
	/**
	 * Return index of (first) field with name <key>.
	 * @param key
	 * @return
	 */
	private int keyIndex(String key) {
		for (int i=0; i<entries.size(); i++) {
			GenericNexusStuff stuff = entries.get(i);
			if (stuff.isField() && stuff.getKey().equals(key)) {
				return i;
			}
		}
		return -1;
	}
	
	public boolean hasKey(String key) {
		return (keyIndex(key)>=0);
	}
	
	public String getValueTrimmed(String key) {
		return getValueTrimmed(key,null);
	}
	
	public String getValueTrimmed(String key, String defaultValue) {
		int index = keyIndex(key);
		String returnValue= (index >= 0) ? entries.get(index).getValue().trim() : defaultValue;
		return returnValue;
	}
	
	public void writeBlockContents(final Writer writer)
			throws IOException {
		for (GenericNexusStuff stuff : entries) {
			stuff.writeObject(writer);
		}
	}
}
