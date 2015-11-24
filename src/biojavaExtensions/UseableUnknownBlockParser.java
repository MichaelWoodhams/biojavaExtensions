package biojavaExtensions;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.biojava.bio.seq.io.ParseException;
import org.biojavax.bio.phylo.io.nexus.NexusBlock;
import org.biojavax.bio.phylo.io.nexus.NexusBlockBuilder;
import org.biojavax.bio.phylo.io.nexus.NexusBlockParser;
import org.biojavax.bio.phylo.io.nexus.NexusComment;
import org.biojavax.bio.phylo.io.nexus.NexusFileFormat;

/**
 * This is just a direct cut-and-paste with class renamed of Biojava 1.8.4's
 * NexusFileBuilder$UnknownBlockParser, made necessary because that class is private
 * and I can find no way to get an instance of it without bug-causing side effects
 * (NexusFileBuilder.getBlockParser("unknown") sets the blockParser data member.) 
 * 
 * Lightly edited to eliminate causes of compiler warnings
 * @author woodhams
 *
 */


public class UseableUnknownBlockParser extends NexusBlockParser.Abstract {
	public UseableUnknownBlockParser() {
		super(new UnknownBlockBuilder());
	}

	public void resetStatus() {
		// Ignore.
	}

	public boolean wantsBracketsAndBraces() {
		return false;
	}

	public void parseToken(final String token) throws ParseException {
		((UnknownBlockBuilder) this.getBlockListener()).getComponents().add(token);
	}

	private static class UnknownBlockBuilder extends
			NexusBlockBuilder.Abstract {

		private UnknownBlock block;

		private List<Object> getComponents() {
			return this.block.getComponents();
		}

		public void endTokenGroup() {
			// Only write not-first, as we also receive the one
			// from after the BEGIN statement.
			if (this.getComponents().size() > 0)
				this.getComponents().add(";");
		}

//		public boolean wantsBracketsAndBraces() {
//			return false;
//		}

		public void endBlock() {
			// We don't care.
		}

		public void addComment(NexusComment comment) {
			this.getComponents().add(comment);
		}

		public NexusBlock startBlockObject() {
			this.block = new UnknownBlock(this.getBlockName());
			return this.block;
		}

		// Holds unknown block data.
		private static class UnknownBlock extends NexusBlock.Abstract {

			private List<Object> components = new ArrayList<Object>(); // will contain NexusComment and String objects

			private UnknownBlock(String blockName) {
				super(blockName);
			}

			private List<Object> getComponents() {
				return this.components;
			}

			/*
			 * Differs from NexusBlock.Abstract in that it removes a superfluous newline
			 */
			@Override
			public void writeObject(final Writer writer) throws IOException {
				writer.write("BEGIN " + this.getBlockName() + ";");
				// here's where NexusBlock.Abstract prints the extra newline.
				this.writeBlockContents(writer);
				writer.write("END;");
				writer.write(NexusFileFormat.NEW_LINE);
			}
			
			public void writeBlockContents(final Writer writer)
					throws IOException {
				for (final Iterator<Object> i = this.components.iterator(); i
						.hasNext();) {
					final Object obj = i.next();
					if (obj instanceof NexusComment)
						((NexusComment) obj).writeObject(writer);
					else
						this.writeToken(writer, (String) obj);
				}
			}
		}
	}
}

