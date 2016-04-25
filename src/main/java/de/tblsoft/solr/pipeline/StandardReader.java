package de.tblsoft.solr.pipeline;

import de.tblsoft.solr.parser.SolrXmlParser;
import de.tblsoft.solr.pipeline.bean.Document;
import de.tblsoft.solr.pipeline.bean.Reader;
import de.tblsoft.solr.util.IOUtils;

/**
 * Created by tblsoft on 23.01.16.
 */
public class StandardReader extends SolrXmlParser implements ReaderIF {

    private PipelineExecuter executer;

    private Reader reader;

    private String baseDir;

    private Document document = new Document();

    @Override
    public void field(String name, String value) {
        document.addField(name, value);

    }


    @Override
    public void endDocument() {
        executer.document(document);
        document = new Document();
    }

    @Override
    public void read() {
        try {
            String filename = (String) reader.getProperty().get("filename");
            String absoluteFilename = IOUtils.getAbsoluteFile(baseDir, filename);
            setInputFileName(absoluteFilename);
            parse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setSource(String source) {
        setInputFileName(source);
    }

    @Override
    public void setPipelineExecuter(PipelineExecuter executer) {
        this.executer = executer;
    }

    @Override
    public void end() {

    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    @Override
    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
}
