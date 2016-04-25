package de.tblsoft.solr.pipeline.filter;

import bsh.EvalError;
import bsh.Interpreter;
import com.google.common.base.Strings;
import de.tblsoft.solr.pipeline.AbstractFilter;
import de.tblsoft.solr.pipeline.bean.Document;
import de.tblsoft.solr.util.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tblsoft 20.04.16.
 */
public class BeanShellFilter extends AbstractFilter {

    private Interpreter interpreter;

    private String filename;

    private Map<String, Object> init = new HashMap<String, Object>();

    @Override
    public void init() {
        String internalFilename = getProperty("filename", null);
        verify(internalFilename, "For the BeanShellFilter a filename property must be defined.");
        filename = IOUtils.getAbsoluteFile(getBaseDir(),internalFilename);

        String initFilename = getProperty("initFilename", null);

        interpreter = new Interpreter();
        try {
            interpreter.set("instance", this);
            interpreter.set("init", init);
            if(!Strings.isNullOrEmpty(initFilename)) {
                String absoluteInitFileName = IOUtils.getAbsoluteFile(getBaseDir(), initFilename);
                interpreter.source(absoluteInitFileName);
                init = (Map<String, Object>) interpreter.get("init");
            }
        } catch (EvalError evalError) {
            throw new RuntimeException(evalError);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        super.init();

    }

    public void superDocument(Document document) {
        super.document(document);

    }

    @Override
    public void document(Document document) {
        try {
            interpreter.set("document", document);
            interpreter.source(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (EvalError evalError) {
            throw new RuntimeException(evalError);
        }
    }



}
