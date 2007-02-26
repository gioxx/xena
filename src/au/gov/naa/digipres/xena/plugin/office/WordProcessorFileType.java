package au.gov.naa.digipres.xena.plugin.office;

/**
 * Type to represent a word processor file.
 *
 * @author Chris Bitmead
 */
public class WordProcessorFileType extends OfficeFileType {

	public WordProcessorFileType() {
	}

	public String getName() {
		return "Word Processor";
	}
    
    public String getMimeType() {
        return "application/vnd.ms-word";
    }

	@Override
	public String getOfficeConverterName()
	{
		return "writer8";
	}
	
	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.type.FileType#fileExtension()
	 */
	@Override
	public String fileExtension()
	{
		return "odt";
	}

}
