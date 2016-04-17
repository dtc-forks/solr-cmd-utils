package de.tblsoft.solr.pipeline;

import com.google.common.base.Strings;
import de.tblsoft.solr.pipeline.bean.Reader;
import de.tblsoft.solr.util.IOUtils;
import oi.thekraken.grok.api.Grok;
import oi.thekraken.grok.api.Match;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * Created by tblsoft on 07.02.16.
 */
public class GrokReader extends AbstractReader {


    private Grok grok;

    private Boolean keepRaw;

    private String filename;

    private String currentFileName;

    @Override
    public void read() {
        try {
            filename = getProperty("filename", null);

            keepRaw = getPropertyAsBoolean("keepRaw", false);

            // %{COMBINEDAPACHELOG}
            String grokPattern = getProperty("grokPattern", null);

            // patterns/patterns
            String grokPatternPath = getProperty("grokPatternPath", null);


            // https://www.elastic.co/guide/en/logstash/current/plugins-codecs-multiline.html
            String multilinePattern = getProperty("multilinePattern", null);
            String multilineNegate = getProperty("multilineNegate", null);
            String multilineWhat = getProperty("multilineWhat", null);

            boolean isMultiline = false;
            if (!Strings.isNullOrEmpty(multilinePattern)) {
                isMultiline = true;
            }


            grok = Grok.create(grokPatternPath);
            grok.compile(grokPattern);

            List<String> fileList = IOUtils.getFiles(filename);

            for (String file : fileList) {
                currentFileName = file;

                InputStream in = IOUtils.getInputStream(file);
                InputStreamReader isr = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(isr);

                String line;
                StringBuilder multilineBuilder = new StringBuilder();


                while ((line = br.readLine()) != null) {
                    if (isMultiline) {
                        if (line.matches(multilinePattern)) {
                            processLine(multilineBuilder.toString());
                            multilineBuilder = new StringBuilder(line);
                            continue;
                        } else {
                            multilineBuilder.append(" ").append(line);
                            continue;
                        }

                    } else {
                        processLine(line);
                    }


                }
                br.close();
            }
            executer.end();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    void processLine(String line) {
        //executer.field("raw", line);
        //System.out.println(line);
        Match gm = grok.match(line);
        gm.captures();
        Map<String, Object> m = gm.toMap();
        for (Map.Entry<String, Object> entry : m.entrySet()) {
            Object value = entry.getValue();
            executer.field(entry.getKey(), String.valueOf(value));
        }
        if (!m.isEmpty()) {
            executer.field("filename", currentFileName);
            if (keepRaw) {
                executer.field("raw", line);
            }
            executer.endDocument();
        }


    }

    @Override
    public void setSource(String source) {

    }

    @Override
    public void setPipelineExecuter(PipelineExecuter executer) {
        this.executer = executer;
    }

    @Override
    public void end() {

    }

    @Override
    public void setReader(Reader reader) {
        this.reader = reader;
    }
}