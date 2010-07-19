package edu.kit.aifb.wikipedia.wpm;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.wikipedia.miner.model.Article;
import org.wikipedia.miner.model.Redirect;
import org.wikipedia.miner.model.Wikipedia;
import org.wikipedia.miner.model.Article.AnchorText;
import org.wikipedia.miner.util.MarkupStripper;
import org.wikipedia.miner.util.SortedVector;

import edu.kit.aifb.document.ICollection;
import edu.kit.aifb.document.ICollectionIterator;
import edu.kit.aifb.document.IDocument;
import edu.kit.aifb.document.TextDocument;
import edu.kit.aifb.nlp.Language;
import edu.kit.aifb.wikipedia.mlc.MLCDatabase;
import gnu.trove.TIntArrayList;

public class WPMMLCArticleCollection implements ICollection {

	private static Logger logger = Logger.getLogger( WPMMLCArticleCollection.class );
	
	Wikipedia wp;
	MLCDatabase mlcArticleDb;
	boolean useAnchorText;
	boolean useRedirects;
	Language language;
	
	public WPMMLCArticleCollection() {
		useAnchorText = true;
		useRedirects = true;
	}
	
	public void setUseAnchorText( boolean useAnchorText ) {
		this.useAnchorText = useAnchorText;
	}
	
	public void setUseRedirects( boolean useRedirects ) {
		this.useRedirects = useRedirects;
	}

	@Required
	public void setLanguage( Language language ) {
		this.language = language;
	}
	
	@Required
	public void setWikipedia( Wikipedia wp ) {
		this.wp = wp;
	}
	
	@Required
	public void setMlcArticleDatabase( MLCDatabase mlcDb ) {
		this.mlcArticleDb = mlcDb;
	}
	
	@Override
	public IDocument getDocument( String docName ) {
		int articleId = mlcArticleDb.getConceptId( docName );
		return buildDocument( articleId );
	}
	
	protected IDocument buildDocument( int conceptId ) {
		TextDocument doc = new TextDocument( mlcArticleDb.getConceptName( conceptId ) );
		StringBuilder titleBuilder = new StringBuilder();
		StringBuilder contentBuilder = new StringBuilder();
		StringBuilder anchorBuilder = new StringBuilder();
		
		try {
			TIntArrayList articleIds = mlcArticleDb.getPageIds( conceptId, language );
			for( int i=0; i<articleIds.size(); i++ ) {
				int articleId = articleIds.get(i);

				Article p = (Article)wp.getPageById( articleId );
				
				String title = p.getTitleWithoutScope();
				logger.debug( "Building document for article " + title + " (" + articleId + ")" );
				titleBuilder.append( title ).append( "\n" );
				
				if( useRedirects ) {
					SortedVector<Redirect> redirects = p.getRedirects();
					for( Redirect redirect : redirects ) {
						titleBuilder.append( redirect.getTitle() ).append( "\n" );
					}
				}
				
				String content = p.getContent();
				if( logger.isTraceEnabled() ) {
					System.out.println( content );
				}
			
				// remove wiki markup
				if( content != null ) {
					contentBuilder.append( MarkupStripper.stripEverything( content ) ).append( "\n" );
				}
				
				// add anchor text
				if( useAnchorText ) {
					SortedVector<AnchorText> anchorTexts = ((Article)p).getAnchorTexts();
					for( AnchorText anchorText : anchorTexts ) {
						for( int j=0; j<anchorText.getCount(); j++ ) {
							anchorBuilder.append( anchorText.getText() ).append( "\n" );
						}
					}
				}
			}
		}
		catch( Exception e ) {
			logger.error( "Error while retrieving concept " + conceptId + ": " + e );
			//e.printStackTrace();
		}	
		
		doc.setText( "title", language, titleBuilder.toString() );
		doc.setText( "content", language, contentBuilder.toString() );
		doc.setText( "anchor", language, anchorBuilder.toString() );
		return doc;
	}

	@Override
	public ICollectionIterator iterator() {
		return new WikipediaMLConceptCollectionIterator();
	}

	@Override
	public int size() {
		return mlcArticleDb.size();
	}

	class WikipediaMLConceptCollectionIterator implements ICollectionIterator {

		private int m_index = -1;
		private TIntArrayList conceptIds;
		private IDocument currentDoc;
		
		public WikipediaMLConceptCollectionIterator() {
			m_index = -1;
			conceptIds = mlcArticleDb.getConceptIds();
		}
		
		@Override
		public IDocument getDocument() {
			return currentDoc;
		}

		@Override
		public boolean next() {
			m_index++;
			/*
			 * DEBUG stop after 500 articles
			 */
			/*if( m_index > 10000 ) {
				return false;
			}*/
			
			if( m_index % 100 == 0 ) {
				logger.info( "Read " + m_index + " concepts." );
			}
			
			if( m_index < conceptIds.size() ) {
				currentDoc = buildDocument( conceptIds.get( m_index ) );
				return true;
			}
			else {
				currentDoc = null;
				return false;
			}
		}
		
	}
}
