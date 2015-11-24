package biojavaExtensions;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.biojavax.bio.phylo.io.nexus.NexusComment;
import org.biojavax.bio.phylo.io.nexus.NexusFileFormat;
import org.biojavax.bio.phylo.io.nexus.TreesBlock;

/**
 * Created to override TreesBlock.writeBlockContents()
 * 
 * Lack of generic type arguments is the fault of Biojava, hence @SuppressWarnings.
 * @author woodhams
 *
 */

public class ExtTreesBlock extends TreesBlock {
	
	@SuppressWarnings("rawtypes")
	protected void writeBlockContents(Writer writer) throws IOException {
		List comments = this.getComments(); //ArrayList
		Map translations = this.getTranslations(); //LinkedHashMap
		Map trees = this.getTrees(); // LinkedHashMap
		
		Iterator commentsIterator = comments.iterator();
		int nComments = comments.size();
		int nTrees = trees.size();
		boolean commentPerTree;
		int nBlockComments;
		if (nComments >= nTrees) {
			/*
			 * We have at least as many comments as trees, so we assume one comment per
			 * tree, and the surplus (if any) are to appear at the start of the block
			 */
			commentPerTree = true;
			nBlockComments = nComments-nTrees;
		} else {
			// all comments will appear at top of block
			commentPerTree = false;
			nBlockComments = nComments;
		}
		// Write the block comments
		for (int i=0; i<nBlockComments; i++) {
			writer.append('\t');
			((NexusComment) commentsIterator.next()).writeObject(writer);
			writer.write(NexusFileFormat.NEW_LINE);
		}
		
		// Write the translations. (Fix a bug in Biojava when no translations were present)
		if (translations.size()>0) {
			writer.write("\tTRANSLATE" + NexusFileFormat.NEW_LINE);
			for (final Iterator i = translations.entrySet().iterator(); i
					.hasNext();) {
				final Map.Entry entry = (Map.Entry) i.next();
				writer.write('\t');
				this.writeToken(writer, "" + entry.getKey());
				writer.write('\t');
				this.writeToken(writer, "" + entry.getValue());
				if (i.hasNext())
					writer.write(',');
				else
					writer.write(';');
				writer.write(NexusFileFormat.NEW_LINE);
			}
		}
		
		Iterator treesIterator = trees.entrySet().iterator();
		while (treesIterator.hasNext()) {
			if (commentPerTree) {
				writer.append('\t');
				((NexusComment) commentsIterator.next()).writeObject(writer);
				writer.write(NexusFileFormat.NEW_LINE);
			}
			@SuppressWarnings("unchecked")
			Map.Entry<String,NewickTreeString> entry = (Map.Entry<String,NewickTreeString>)treesIterator.next();
			NewickTreeString treeStr = entry.getValue();
			writer.write("\tTREE ");
			if (treeStr.isStarred())
				writer.write("* ");
			this.writeToken(writer, "" + entry.getKey());
			writer.write('=');
			if (treeStr.getRootType() != null)
				writer.write("[" + treeStr.getRootType() + "]");
			this.writeToken(writer, treeStr.getTreeString());
			writer.write(";" + NexusFileFormat.NEW_LINE);
		} 
	}
}
