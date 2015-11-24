package biojavaExtensions;

import org.biojavax.bio.phylo.io.nexus.NexusBlock;
import org.biojavax.bio.phylo.io.nexus.NexusComment;
import org.biojavax.bio.phylo.io.nexus.TreesBlockBuilder;
import org.biojavax.bio.phylo.io.nexus.TreesBlock.NewickTreeString;

/**
 * Replacement for biojava's TreesBlockBuilder which exists purely for 
 * the purpose of using an ExtTreesBlock instead of a TreesBlock.
 * 
 * @author woodhams
 *
 */
public class ExtTreesBlockBuilder extends TreesBlockBuilder {
	
	private ExtTreesBlock block;

	protected void addComment(final NexusComment comment) {
		this.block.addComment(comment);
	}

	protected NexusBlock startBlockObject() {
		this.block = new ExtTreesBlock();
		this.resetStatus();
		return this.block;
	}

	private void resetStatus() {
		// Nothing to do.
	}

	public void addTranslation(String label, String taxa) {
		this.block.addTranslation(label, taxa);
	}

	public void addTree(String label, NewickTreeString tree) {
		this.block.addTree(label, tree);
	}
}
