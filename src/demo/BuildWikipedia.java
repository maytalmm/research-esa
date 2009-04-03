package demo;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.uka.aifb.api.document.ICollection;
import edu.uka.aifb.api.nlp.ITokenAnalyzer;
import edu.uka.aifb.document.wikipedia.WikipediaCollection;
import edu.uka.aifb.ir.terrier.TerrierIndexFactory;
import edu.uka.aifb.nlp.Language;
import edu.uka.aifb.nlp.MultiLingualAnalyzer;
import edu.uka.aifb.tools.ConfigurationManager;

public class BuildWikipedia {

	static final String[] REQUIRED_PROPERTIES = {
		"language"
	};
	
	static Logger logger = Logger.getLogger( BuildWikipedia.class );
	
	static public void main( String[] args ) throws Exception {
		Configuration config = ConfigurationManager.parseArgs( args );
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES );

		Language language = Language.getLanguage( config.getString( "language" ) );
		
		logger.info( "Initializing wikipedia collection, language: " + language );
		ICollection collection = new WikipediaCollection(
						config,
						language );
		
		ITokenAnalyzer analyzer = new MultiLingualAnalyzer( config );
		
		TerrierIndexFactory factory = new TerrierIndexFactory( config );
		factory.buildIndex(
				"wikipedia",
				language,
				analyzer,
				collection,
				false );
	}

}
