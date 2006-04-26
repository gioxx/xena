/*
 * Created on 11/01/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.plugin.office;

import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuessPriority;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class WordProcessorGuesser extends OfficeGuesser {

	private static final String WORD_TYPE_STRING = "Microsoft Word";

	
	private static byte[][] rtfMagic = {{ 0x7B, 0x5c, 0x72, 0x74, 0x66, 0x31 }};
    private static final String[] rtfExtensions = {"rtf"};
    private static final String[] rtfMime = {"application/rtf", "text/rtf"};
    
    private static byte[][] wriMagic = {{ 0x31, (byte)0xBE, 0x00, 0x00, 
    									  0x00, (byte)0xAB, 0x00, 0x00 }};
    private static final String[] wriExtensions = {"wri"};
    private static final String[] wriMime = {};
    
    private static final String[] mswordExtensions = {"doc", "dot"};
    private static final String[] mswordMime = {"application/msword"};
    
    private static byte[][] sxwMagic = {{ 0x50, 0x4B, 0x03, 0x04, 0x14, 0x00 }};
    private static final String[] sxwExtensions = {"sxw"};
    private static final String[] sxwMime = {"application/vnd.sun.xml.writer"};
    
    private static byte[][] wpMagic = {{ (byte)0xFF, 0x57, 0x50, 0x43 }};
    private static final String[] wpExtensions = {"wpd"};
    private static final String[] wpMime = {" application/wordperfect"};

    private Type type;
    
    private FileTypeDescriptor[] fileTypeDescriptors = 
    {
    	new FileTypeDescriptor(rtfExtensions, rtfMagic, rtfMime),
    	new FileTypeDescriptor(wriExtensions, wriMagic, wriMime),
    	new FileTypeDescriptor(mswordExtensions, officeMagic, mswordMime),
    	new FileTypeDescriptor(sxwExtensions, sxwMagic, sxwMime),
    	new FileTypeDescriptor(wpExtensions, wpMagic, wpMime)
    };

 
    /**
     * @throws XenaException 
	 * 
	 */
	public WordProcessorGuesser()
	{
		super();
	}
    
    @Override
    public void initGuesser(GuesserManager guesserManager) throws XenaException {
        this.guesserManager = guesserManager;
        type = getTypeManager().lookup(WordProcessorFileType.class);
    }

	public String getName() {
        return "WordGuesser";
    }
    
    public Guess guess(XenaInputSource xis) throws IOException, XenaException
    {
    	
    	Guess guess = guess(xis, type);
        guess.setPriority(GuessPriority.HIGH);
        
        return guess;
    }

	/**
	 * @return Returns the fileTypeDescriptors.
	 */
	public FileTypeDescriptor[] getFileTypeDescriptors()
	{
		return fileTypeDescriptors;
	}

	@Override
	public Type getType()
	{
		return type;
	}

	@Override
	protected String getOfficeTypeString()
	{
		return WORD_TYPE_STRING;
	}

    
}
