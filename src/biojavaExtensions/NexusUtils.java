package biojavaExtensions;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;

import org.biojava.bio.seq.io.ParseException;
import org.biojavax.bio.phylo.io.nexus.DistancesBlock;
import org.biojavax.bio.phylo.io.nexus.DistancesBlockBuilder;
import org.biojavax.bio.phylo.io.nexus.NexusBlock;
import org.biojavax.bio.phylo.io.nexus.NexusBlockBuilder;
import org.biojavax.bio.phylo.io.nexus.NexusComment;
import org.biojavax.bio.phylo.io.nexus.NexusFile;
import org.biojavax.bio.phylo.io.nexus.TaxaBlock;
import org.biojavax.bio.phylo.io.nexus.TaxaBlockBuilder;
import org.biojavax.bio.phylo.io.nexus.TreesBlock;

public class NexusUtils {
	public static TreesBlock makeTreesBlock(String[] trees, String[] perTreeComments, String blockComment) {
		return(makeTreesBlock(trees,perTreeComments,blockComment,"T")); // "T" for "tree"
	}
	public static TreesBlock makeTreesBlock(String[] trees, String[] perTreeComments, String blockComment, String treePrefix) {
		ExtTreesBlockBuilder builder = new ExtTreesBlockBuilder();
		builder.startBlock("trees");
		addComment(builder,blockComment);
		
		int nTrees = trees.length;
		if (perTreeComments!=null && perTreeComments.length!=nTrees) 
			throw new IllegalArgumentException ("Trees and tree comments have different lengths");
		
		// e.g. format = "T%2d" for 11 <= nTrees <= 100.
		String format = (nTrees<=1) ? treePrefix+"%d" : treePrefix+"%0"+Integer.toString((int)Math.floor(Math.log10(nTrees-1))+1)+"d";
		for (int i=0; i<nTrees; i++) {
			if (perTreeComments!=null) addComment(builder,perTreeComments[i]);
			TreesBlock.NewickTreeString newickTree = new TreesBlock.NewickTreeString();
			// tree string comes with terminating ';', but TreesBlock output will provide
			// another one, so strip it off.
			newickTree.setTreeString(trees[i].replace(";", ""));
			builder.addTree(String.format(format, i), newickTree);
		}
		builder.endBlock();
		return (TreesBlock)builder.getNexusBlock();
	}
	
	/**
	 * NOTE: Will sort the argument passed to it. Pass a copy if you don't want this.
	 * @param taxa
	 * @return
	 */
	public static TaxaBlock makeTaxaBlock(String[] taxa) {
		TaxaBlockBuilder builder = new TaxaBlockBuilder();
		Arrays.sort(taxa);
		builder.startBlock("taxa");
		builder.setDimensionsNTax(taxa.length);
		try {
			for (String taxon : taxa) builder.addTaxLabel(taxon);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new RuntimeException("Can't happen");
		}
		builder.endBlock();
		return (TaxaBlock)builder.getNexusBlock();
	}
	
	public static DistancesBlock makeDistancesBlock(String[] taxa, double[][] distances) {
		int n = taxa.length;
		boolean goodDimensions=(distances.length==n);
		for (int i=0; i<n; i++) {
			goodDimensions = goodDimensions && (distances[i].length==n);
		}
		if (!goodDimensions) {
			throw new IllegalArgumentException("distances must be square matrix of same size as taxa");
		}
		
		DistancesBlockBuilder builder = new DistancesBlockBuilder();
		builder.startBlock("distances");
		builder.setDimensionsNTax(n);
		builder.setTriangle("both");
		for (int row=0; row<n; row++) {
			String taxon = taxa[row];
			builder.addMatrixEntry(taxon);
			for (int col=0; col<n; col++) {
				String str = String.format("%.3f",distances[row][col]);
				builder.appendMatrixData(taxon, str);
			}
		}
		builder.endBlock();
		return((DistancesBlock)builder.getNexusBlock());
	}

	
	/*
	 * Just because BioJava didn't supply a suitable constructor for NexusComment.
	 */
	public static NexusComment newNexusComment(String string) {
		NexusComment comment = new NexusComment();
		comment.addCommentText(string);
		return comment;
	}
	
	/*
	 * Again, BioJava should have provided this as a builder method.
	 * If string is null, does nothing.
	 */
	public static void addComment(NexusBlockBuilder builder, String string) {
		if (string != null) {
			builder.beginComment();
			try {
				builder.commentText(string);
			} catch (ParseException e) {
				e.printStackTrace();
				throw new RuntimeException("Can't happen");
			}
			builder.endComment();
		}
	}
	
	/*
	 * What NexusComment.toString() should do but doesn't
	 */
	public static String toString(NexusComment comment) {
		StringWriter sw = new StringWriter();
		try {
			comment.writeObject(sw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sw.toString();
	}
	
	/**
	 * Returns the first block of the given name, or null if no such block exists.
	 * Block name is not case sensitive.
	 * 
	 * @param nexusFile
	 * @param blockName
	 * @return
	 */
	public static NexusBlock getBlockByName(NexusFile nexusFile, String blockName) {
		@SuppressWarnings("unchecked")
		Iterator<NexusBlock> iter = (Iterator<NexusBlock>)nexusFile.blockIterator();
		GenericBlock block = null;
		while (block == null && iter.hasNext()) {
			NexusBlock nextBlock = iter.next();
			if (nextBlock.getBlockName().equalsIgnoreCase(blockName)) {
				block = (GenericBlock)nextBlock;
			}
		}
		return block;
	}
	/**
	 * Like getBlockByName, but throws an error if there is more than one block of that
	 * name in the file.
	 * 
	 * @param nexusFile
	 * @param blockName
	 * @return
	 */
	public static NexusBlock getUniqueBlockByName(NexusFile nexusFile, String blockName) {
		@SuppressWarnings("unchecked")
		Iterator<NexusBlock> iter = (Iterator<NexusBlock>)nexusFile.blockIterator();
		NexusBlock block = null;
		while (iter.hasNext()) {
			NexusBlock nextBlock = iter.next();
			if (nextBlock.getBlockName().equalsIgnoreCase(blockName)) {
				if (block != null) throw new RuntimeException("File contains more than one "+blockName+" block");
				block = nextBlock;
			}
		}
		return block;
	}
}
