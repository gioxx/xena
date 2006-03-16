package au.gov.naa.digipres.xena.plugin.email;
import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Guesser to guess TRIM .mbx files.
 *
 * @author not attributable
 * @version 1.0
 */
public class TrimGuesser extends Guesser {
	static String FROM_TEXT = "From:";
	private Type type;
	
	
	/**
	 * @throws XenaException 
	 * 
	 */
	public TrimGuesser() throws XenaException
	{
		super();
	}


    @Override
    public void initGuesser(GuesserManager guesserManager) throws XenaException {
        this.guesserManager = guesserManager;
        type = getTypeManager().lookup(TrimFileType.class);
    }
    
	public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess(type);
				
//		 if (source.getSystemId().startsWith("file:/") && source.getSystemId().endsWith("/")) {
//			File dir = null;
//			try {
//				dir = new File(new URI(source.getSystemId()));
//			} catch (URISyntaxException x) {
//				throw new IOException("Invalid URI: " + source.getSystemId());
//			}
//			File[] list = dir.listFiles(new FilenameFilter() {
//				public boolean accept(File dir, String name) {
//					return name.toLowerCase().endsWith(".mbx");
//				}
//			});
//			if (0 < list.length) {
//                guess.setExtensionMatch(true);
//			}
//		}

	   	if (source.getSystemId().toLowerCase().endsWith(".mbx")) {
            guess.setExtensionMatch(true);
		}
		return guess;
	}
    
    public String getName() {
        return "TrimGuesser";
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setExtensionMatch(true);
		return guess;
	}

	@Override
	public Type getType()
	{
		return type;
	}

}
