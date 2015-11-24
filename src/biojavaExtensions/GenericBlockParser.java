package biojavaExtensions;
import java.util.Set;

import org.biojava.bio.seq.io.ParseException;
import org.biojavax.bio.phylo.io.nexus.NexusBlockParser;

/**
 * Based on Biojava 1.8.4 NexusFileBuilder.UnknownBlockParser
 * 
 * @author woodhams
 *
 */
public class GenericBlockParser extends NexusBlockParser.Abstract {
	
//	private boolean inWhitespace;
	private boolean inFieldName;
	private boolean inFieldContents;
	
	public GenericBlockParser() {
		this(false,null);
	}
	public GenericBlockParser(boolean insertCommentsIntoValues) {
		this(insertCommentsIntoValues,null);
	}
	public GenericBlockParser(boolean insertCommentsIntoValues, Set<String> validKeys) {
		super(new GenericBlockBuilder(insertCommentsIntoValues,validKeys));
		resetStatus();
	}

	public void resetStatus() {
//		inWhitespace = false;
		inFieldName = false;
		inFieldContents = false;
	}

	public boolean wantsBracketsAndBraces() {
		return false;
	}

	public void parseToken(final String token) throws ParseException {
		GenericBlockBuilder builder = (GenericBlockBuilder)this.getBlockListener();
		
		/*
		 * Part of a nasty hack made necessary by the fact that this parser doesn't get
		 * to see semicolons, so must rely on the builder to see them.
		 */
		if (builder.groupEnded()) {
//			inWhitespace = false;
			inFieldName = false;
			inFieldContents = false;
		}
		
		if (inFieldName) {
			if (token.equals("\n")) {
				// '\n' will always be a token by itself
				builder.addSeparator(token);
				inFieldName = false;
				inFieldContents = true;
			} else {
				int equalsPos = token.indexOf('=');
				if (equalsPos>=0) {
					// '=' might be part of a larger string
					//String before = (equalsPos==0) ? "" : token.substring(0, equalsPos-1);
					//String after = (equalsPos+1==token.length()) ? "" : token.substring(equalsPos+1);
					String before = token.substring(0, equalsPos);
					String after  = token.substring(equalsPos+1);
					builder.addKey(before);
					builder.addSeparator("=");
					builder.addValue(after);
					inFieldName = false;
					inFieldContents = true;
				} else {
					builder.addKey(token);
				}
			}
		} else if (token.trim().length()==0) {
			// Add whitespace to whatever we're currently building. 
			// Case of building inFieldName already handled above 
			if (inFieldContents) {
				builder.addValue(token);
			} else {
				builder.addWhiteSpace(token);
//				inWhitespace = true;
			}
		} else {
			// Non-whitespace, non-separator and not inFieldName
			if (inFieldContents) {
				builder.addValue(token);
			} else {
				if (token.indexOf('=')>=0) {
//					inWhitespace = false;
					inFieldName = true;
					parseToken(token); // reparse knowing we have a field name. Saves some cut-and-paste program editing.
				} else {
					builder.addKey(token);
//					inWhitespace = false;
					inFieldName = true;
				}
			}
		}
	}
}
