package biojavaExtensions;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

import org.biojava.bio.seq.io.ParseException;
import org.biojavax.bio.phylo.io.nexus.NexusBlock;
import org.biojavax.bio.phylo.io.nexus.NexusBlockBuilder;
import org.biojavax.bio.phylo.io.nexus.NexusComment;

/**
 * Based on Biojava 1.8.4 NexusFileBuilder.UnknownBlockParser.UnknownBlockBuilder
 * 
 * @author woodhams
 *
 */

/*
 * A nasty 'feature' of biojava's Nexus parsing is that a ';' in the input stream
 * becomes a call to the builder's endTokenGroup() method, which bypasses
 * the parser entirely, so the parser doesn't know it has happened.
 * I've added a groupEnded() method to check for this, but alternatively
 * the parser could simply not store any state at all (inKeyName etc.) 
 * and ask the builder for its state on every call. 
 */

public class GenericBlockBuilder extends NexusBlockBuilder.Abstract {

	private StringBuffer keyBuffer;
	private String separator;
	private StringBuffer valueBuffer;
	private boolean inWhiteSpace;
	private boolean inKeyName;
	private boolean inValue;
	private boolean endedTokenGroup;
	private boolean insertCommentsIntoValues; // if true, stringify comments which occur in field values and insert them.
	private boolean blockJustStarted; // Nasty hack to avoid extra "\n" at start of block.
	private Set<String> validKeys;
	
	public GenericBlockBuilder() {
		this(false);
	}
	public GenericBlockBuilder(boolean insertComments) {
		this(insertComments,null);
	}
	/**
	 * 
	 * @param insertComments
	 * @param validKeys - may be null, in which case all keys are valid.
	 */
	public GenericBlockBuilder(boolean insertComments, Set<String> validKeys) {
		keyBuffer = new StringBuffer();
		separator = null;
		valueBuffer = new StringBuffer();
		inWhiteSpace = false;
		inKeyName = false;
		inValue = false;
		endedTokenGroup = false;
		insertCommentsIntoValues = insertComments;
		blockJustStarted = true;
		this.validKeys= validKeys; 
	}
	
	public void setInsertComments() { insertCommentsIntoValues = true; }
	public void unsetInsertComments() {insertCommentsIntoValues = false; }
	
	private void storeLastObject() throws ParseException {
		if (inWhiteSpace) {
			((GenericBlock)this.getNexusBlock()).addWhitespace(valueBuffer.toString());
			inWhiteSpace = false;
			valueBuffer.setLength(0);
		} else if (inValue) {
			((GenericBlock)this.getNexusBlock()).addField(keyBuffer.toString().trim(), separator, valueBuffer.toString());
			inValue = false;
			keyBuffer.setLength(0);
			valueBuffer.setLength(0);
		} else if (inKeyName) {
			((GenericBlock)this.getNexusBlock()).addField(keyBuffer.toString().trim(), "", "");
			inKeyName = false;
			keyBuffer.setLength(0);
		}
		blockJustStarted = false;
	}
	
	/*
	 * addWhiteSpace, addKey, addSeparator, addValue are for use by a parser,
	 * adding one token at a time.
	 */
	
	public void addWhiteSpace(String white) throws ParseException {
		if (blockJustStarted && white.equals("\n")) {
			// Nasty hack to avoid extra newline at the start of the block
			blockJustStarted = false;
			return; 
		}
		if (!inWhiteSpace) storeLastObject();
		inWhiteSpace = true;
		valueBuffer.append(white);
	}
	
	public void addKey(String key) throws ParseException {
		if (!inKeyName) storeLastObject();
		inKeyName = true;
		keyBuffer.append(key);
	}
	
	public void addSeparator(String separator) throws ParseException {
		if (!inKeyName || keyBuffer.length()==0) throw new ParseException ("Missing key name");
		this.separator = separator;
		inKeyName = false;
		inValue = true;
	}
	
	public void addValue(String value) throws ParseException {
		if (!inValue) throw new ParseException("Adding value to nonexistant field");
		valueBuffer.append(value);
	}
	
	/*
	 * addField and addStuff adds an entire entry in one go. They are intended for constructing
	 * blocks from program data for output.
	 */
	
	public void addField(String key, String separator, String value) throws ParseException {
		//addWhiteSpace("\t");
		storeLastObject();
		((GenericBlock)this.getNexusBlock()).addField(key, separator, value);
	}
	
	public void addFieldPlusWhitespace(String key, String separator, String value) throws ParseException {
		addWhiteSpace("\t");
		addField(key, separator, value);
		addWhiteSpace("\n");
	}
	
	public void addStuff(GenericNexusStuff stuff) throws ParseException {
		storeLastObject();
		((GenericBlock)this.getNexusBlock()).addStuff(stuff);
	}
	
	@Override
	public void endTokenGroup() {
		try {
			storeLastObject();
		} catch (ParseException e) {
			// endTokenGroup can't have 'throw' clause due to inheritance. 
			throw new RuntimeException(e.getMessage());
		}
		endedTokenGroup = true;
	}
	
	public boolean groupEnded() {
		boolean answer = endedTokenGroup;
		endedTokenGroup = false;
		return answer;
	}

	public void endBlock() {
		try {
			storeLastObject();
		} catch (ParseException e) {
			// endBlock can't have 'throw' clause due to inheritance. 
			throw new RuntimeException(e.getMessage());
		}
	}

	protected void addComment(final NexusComment comment) {
		if (insertCommentsIntoValues && inValue) {
			StringWriter sw = new StringWriter();
			try {
				comment.writeObject(sw);
				addValue(sw.toString());
			} catch (IOException | ParseException e) {
				throw new RuntimeException("NexusComment stuffs up - can't happen");
			}
			
		} else {
			((GenericBlock)this.getNexusBlock()).addComment(comment);
		}
	}

	@Override
	public NexusBlock startBlockObject() {
		return new GenericBlock(this.getBlockName(),validKeys);
	}

}
