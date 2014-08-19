package ca.on.oicr.pde.dao;

import ca.on.oicr.pde.model.Accessionable;
import ca.on.oicr.pde.model.File;
import ca.on.oicr.pde.model.FileProvenanceReportRecord;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.model.WorkflowRun;
import ca.on.oicr.pde.model.WorkflowRunReportRecord;
import ca.on.oicr.pde.parsers.FileProvenanceReport;
import ca.on.oicr.pde.parsers.WorkflowRunReport;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.sourceforge.seqware.common.util.maptools.MapTools;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class SeqwareWebserviceImpl extends SeqwareService {

    private final static Logger log = LogManager.getLogger(SeqwareWebserviceImpl.class);

    //private final DefaultHttpClient httpClient;
    private final String restUrl;
    private final String user;
    private final String password;

//    public SeqwareWebserviceImpl(File seqwareSettingsFile) {
////        super(seqwareSettingsFile);
////        httpClient = new DefaultHttpClient();
////        httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(seqwareSettings.get("SW_REST_USER"), seqwareSettings.get("SW_REST_PASS")));
////        updateFileProvenanceRecords();
//        Map seqwareSettings = new HashMap<String, String>();
//        MapTools.ini2Map(seqwareSettingsFile.toString(), seqwareSettings, true);
//
//        this(seqwareSettings.get("SW_REST_URL").toString(), seqwareSettings.get("SW_REST_USER").toString(), seqwareSettings.get("SW_REST_PASS").toString());
//    }
    public SeqwareWebserviceImpl(String restUrl, String user, String password) {

        super();
        this.restUrl = restUrl;
        this.user = user;
        this.password = password;
//        httpClient = new DefaultHttpClient();
//        httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));

    }

    @Override
    public void update() {
        updateFileProvenanceRecords();
    }

//    public void update2() {
//        getAllFiles();
//    }
//
//    private void getAllFiles() {
//        try {
//            List<String> studies = getElementFromXML(getHttpResponse(restUrl + "/studies"), "//list/swAccession/text()", true);
//            
//            for(String s : studies){
//                getHttpResponse(restUrl + "/reports/file-provenance")
//            }
//            System.out.println("studies: " + studies.toString());
//        } catch (IOException ioe) {
//            throw new RuntimeException(ioe);
//        }
//    }
    @Override
    protected void updateFileProvenanceRecords() {

        try {
            long startTime = System.nanoTime();
            log.printf(Level.INFO, "Starting download and parsing of file provenance report");
            fprs = FileProvenanceReport.parseFileProvenanceReport(getHttpResponse(restUrl + "/reports/file-provenance"), FileProvenanceReport.HeaderValidationMode.SKIP);
            log.printf(Level.INFO, "Completed download and parsing of file provenance report in %.2fs", (System.nanoTime() - startTime) / 1E9);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        swidToFpr = new HashMap<String, List<FileProvenanceReportRecord>>();
        long startTime = System.nanoTime();
        log.printf(Level.INFO, "Starting update of seqware accession to file provenance record lookup map");
        for (FileProvenanceReportRecord f : fprs) {
            for (String swid : f.getSeqwareAccessions()) {
                if (!swidToFpr.containsKey(swid)) {
                    List l = new LinkedList<FileProvenanceReportRecord>();
                    l.add(f);
                    swidToFpr.put(swid, l);
                } else {
                    swidToFpr.get(swid).add(f);
                }
            }
        }

        //Calulcate some stats about the lookup table
        int numKeys = swidToFpr.keySet().size();
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        BigDecimal numRecs = BigDecimal.valueOf(numKeys);
        BigDecimal avg = BigDecimal.ZERO;
        int size;
        for (Entry<String, List<FileProvenanceReportRecord>> e : swidToFpr.entrySet()) {
            if (e.getValue() == null || e.getValue().isEmpty()) {
                size = 0;
            } else {
                size = e.getValue().size();
                avg = avg.add(BigDecimal.valueOf(size).divide(numRecs, MathContext.DECIMAL128));
            }

            if (size < min) {
                min = size;
            }
            if (size > max) {
                max = size;
            }
        }
        log.printf(Level.INFO, "Number of keys = %s, min/max/avg records per key = %s/%s/%.2f", numKeys, min, max, avg.doubleValue());
        log.printf(Level.INFO, "Completed update of seqware accession to file provenance record lookup map in %.2fs", (System.nanoTime() - startTime) / 1E9);
    }

    @Override
    protected void updateWorkflowRunRecords(Workflow workflow) {

        List<WorkflowRunReportRecord> wrrr;
        try {
            wrrr = WorkflowRunReport.parseWorkflowRunReport(getHttpResponse(restUrl + "/reports/workflows/" + workflow.getSwid() + "/runs"));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        synchronized (wrrs) {
            wrrs.put(workflow, wrrr);
        }

    }

    @Override
    public Map<String, String> getWorkflowRunIni(WorkflowRun workflowRun) {

        String iniFile;
        try {
            iniFile = getElementFromXML(getHttpResponse(restUrl + "/workflowruns/" + workflowRun.getSwid()), "/WorkflowRun/iniFile/text()");
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        return MapTools.iniString2Map(iniFile);

    }

    @Override
    protected List<Accessionable> getWorkflowRunInputFiles(WorkflowRun workflowRun) {

        List<String> fileAccessions = new ArrayList<String>();
        try {
            fileAccessions = getElementFromXML(getHttpResponse(restUrl + "/workflowruns/" + workflowRun.getSwid()),
                    "/WorkflowRun/inputFileAccessions/text()", true);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        List files = new ArrayList<File>();
        for (String s : fileAccessions) {
            File.Builder fileBuilder = new File.Builder();
            fileBuilder.setSwid(s);
            files.add(fileBuilder.build());
        }

        return files;

    }

    public static String getElementFromXML(InputStream xmlDoc, String xpath) {

        String result;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(xmlDoc);
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPathExpression expr = xpathFactory.newXPath().compile(xpath);
            result = expr.evaluate(doc);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } catch (ParserConfigurationException pce) {
            throw new RuntimeException(pce);
        } catch (SAXException sae) {
            throw new RuntimeException(sae);
        } catch (XPathExpressionException xee) {
            throw new RuntimeException(xee);
        }

        return result;

    }

    public static List<String> getElementFromXML(InputStream xmlDoc, String xpath, boolean all) {

        List<String> result = new ArrayList<String>();
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(xmlDoc);
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPathExpression expr = xpathFactory.newXPath().compile(xpath);

            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(nodes.item(i).getNodeValue());
            }

        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } catch (ParserConfigurationException pce) {
            throw new RuntimeException(pce);
        } catch (SAXException sae) {
            throw new RuntimeException(sae);
        } catch (XPathExpressionException xee) {
            throw new RuntimeException(xee);
        }

        return result;

    }

    private InputStream getHttpResponse(String url) throws IOException {

        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));

        HttpResponse r = httpClient.execute(new HttpGet(url));

        if (r.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException("HTTP status code = [" + r.getStatusLine().getStatusCode() + "] was returned when accessing url = [" + url + "]");
        }

        return r.getEntity().getContent();

    }

}
