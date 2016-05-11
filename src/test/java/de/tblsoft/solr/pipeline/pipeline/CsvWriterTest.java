package de.tblsoft.solr.pipeline.pipeline;

import de.tblsoft.solr.pipeline.PipelineExecuter;
import de.tblsoft.solr.pipeline.test.AbstractPipelineTest;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * Created by tblsoft on 11.05.16.
 */
public class CsvWriterTest extends AbstractPipelineTest {

    @Test
    public void writeCsv() throws Exception {
        PipelineExecuter executer = new PipelineExecuter("examples/unittest/csv-writer-pipeline.yaml");
        executer.execute();

        List<String> actual = FileUtils.readLines(new File("examples/unittest/output.csv"));
        List<String> expected = FileUtils.readLines(new File("examples/unittest/expected.csv"));

        Assert.assertEquals(expected, actual);

        FileUtils.deleteQuietly(new File("examples/unittest/output.csv"));

    }
}
