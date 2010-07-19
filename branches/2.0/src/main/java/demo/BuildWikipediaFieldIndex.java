package demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import edu.kit.aifb.ConfigurationException;
import edu.kit.aifb.ConfigurationManager;
import edu.kit.aifb.document.ICollection;
import edu.kit.aifb.terrier.MTTerrierIndexFactory;

public class BuildWikipediaFieldIndex {

	static Logger logger = Logger.getLogger( BuildWikipediaFieldIndex.class );

	static final String[] REQUIRED_PROPERTIES = {
		"collection_bean",
		"fields",
		"indexId",
	};
	
	static public void main( String[] args ) throws Exception {
		try {
			ApplicationContext context = new FileSystemXmlApplicationContext( "config/*_beans.xml" );
			ConfigurationManager confMan = (ConfigurationManager) context.getBean( ConfigurationManager.class );
			confMan.parseArgs( args );
			confMan.checkProperties( REQUIRED_PROPERTIES );
			Configuration config = confMan.getConfig();

			String indexId = config.getString( "indexId" );

			List<String> fields = new ArrayList<String>();
			for( String field : config.getStringArray( "fields" ) ) {
				fields.add( field );
			}
			
			ICollection collection = (ICollection) context.getBean(
					config.getString( "collection_bean" ) );

			MTTerrierIndexFactory factory = (MTTerrierIndexFactory) context.getBean(
					MTTerrierIndexFactory.class );
			
			factory.buildFieldIndexes( indexId, collection, fields );
		}
		catch( ConfigurationException e ) {
			e.printUsage();
		}
	}

}
