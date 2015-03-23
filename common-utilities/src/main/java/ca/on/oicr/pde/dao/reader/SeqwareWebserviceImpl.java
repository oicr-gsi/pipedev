package ca.on.oicr.pde.dao.reader;

import ca.on.oicr.pde.model.Accessionable;
import ca.on.oicr.pde.model.File;
import ca.on.oicr.pde.model.FileProvenanceReportRecord;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.model.WorkflowRun;
import ca.on.oicr.pde.model.WorkflowRunReportRecord;
import ca.on.oicr.pde.parsers.FileProvenanceReport;
import ca.on.oicr.pde.parsers.WorkflowRunReport;
import ca.on.oicr.pde.utilities.Timer;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.sourceforge.seqware.common.util.maptools.MapTools;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author mlaszloffy
 */
public final class SeqwareWebserviceImpl extends SeqwareReadService {

    private final static Logger log = LogManager.getLogger(SeqwareWebserviceImpl.class);
    private final URL url;
    private final String user;
    private final String password;

    private final String host;
    private final int port;
    private final String protocol;

    /**
     *
     * @param restUrl
     * @param user
     * @param password
     * @throws java.net.MalformedURLException
     */
    public SeqwareWebserviceImpl(String restUrl, String user, String password) throws MalformedURLException {
        this.url = new URL(restUrl);
        this.user = user;
        this.password = password;

        this.host = url.getHost();
        this.port = url.getPort();
        this.protocol = url.getProtocol();
    }

    @Override
    public void updateFileProvenanceRecords() {

        try {
            fileProvenanceReportRecords = FileProvenanceReport.parseFileProvenanceReport(getHttpResponse(url + "/reports/file-provenance"), FileProvenanceReport.HeaderValidationMode.SKIP);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        //incremental update not currently supported, so the whole report is downloaded again
        accessionToFileProvenanceReportRecords.clear();

        for (FileProvenanceReportRecord f : fileProvenanceReportRecords) {
            for (String swid : f.getSeqwareAccessions()) {
                accessionToFileProvenanceReportRecords.put(swid, f);
            }
        }

//        //Calulcate some stats about the lookup table
//        int numKeys = accessionToFileProvenanceReportRecords.keySet().size();
//        int min = Integer.MAX_VALUE;
//        int max = Integer.MIN_VALUE;
//        BigDecimal numRecs = BigDecimal.valueOf(numKeys);
//        BigDecimal avg = BigDecimal.ZERO;
//        int size;
//        for (Entry<String, List<FileProvenanceReportRecord>> e : accessionToFileProvenanceReportRecords.entrySet()) {
//            if (e.getValue() == null || e.getValue().isEmpty()) {
//                size = 0;
//            } else {
//                size = e.getValue().size();
//                avg = avg.add(BigDecimal.valueOf(size).divide(numRecs, MathContext.DECIMAL128));
//            }
//
//            if (size < min) {
//                min = size;
//            }
//            if (size > max) {
//                max = size;
//            }
//        }
//        log.printf(Level.INFO, "%s file provenance records, %s unique keys, %s/%s/%.2f (min/max/avg) records per key", fileProvenanceReportRecords.size(), numKeys, min, max, avg.doubleValue());
    }

    /**
     *
     * @param workflow
     */
    @Override
    public void updateWorkflowRunRecords(Workflow workflow) {

        List<WorkflowRunReportRecord> wrrr;
        try {
            wrrr = WorkflowRunReport.parseWorkflowRunReport(getHttpResponse(url + "/reports/workflows/" + workflow.getSwid() + "/runs"));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        synchronized (workflowToWorkflowRunReportRecords) {
            workflowToWorkflowRunReportRecords.put(workflow, wrrr);
        }

    }

    /**
     *
     * @param workflowRun
     * @return
     */
    @Override
    public Map<String, String> getWorkflowRunIni(WorkflowRun workflowRun) {

        String iniFile;
        try {
            iniFile = getElementFromXML(getHttpResponse(url + "/workflowruns/" + workflowRun.getSwid()), "/WorkflowRun/iniFile/text()");
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        return MapTools.iniString2Map(iniFile);

    }

    /**
     *
     * @param workflowRun
     * @return
     */
    @Override
    protected List<Accessionable> getWorkflowRunInputFiles(WorkflowRun workflowRun) {

        List<String> fileAccessions = new ArrayList<>();
        try {
            fileAccessions = getElementFromXML(getHttpResponse(url + "/workflowruns/" + workflowRun.getSwid()),
                    "/WorkflowRun/inputFileAccessions/text()", true);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        List files = new ArrayList<>();
        for (String s : fileAccessions) {
            File.Builder fileBuilder = new File.Builder();
            fileBuilder.setSwid(s);
            files.add(fileBuilder.build());
        }

        return files;

    }

    /**
     *
     * @param xmlDoc
     * @param xpath
     * @return
     */
    public static String getElementFromXML(InputStream xmlDoc, String xpath) {

        String result;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(xmlDoc);
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPathExpression expr = xpathFactory.newXPath().compile(xpath);
            result = expr.evaluate(doc);
        } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException ioe) {
            throw new RuntimeException(ioe);
        }

        return result;

    }

    /**
     *
     * @param xmlDoc
     * @param xpath
     * @param all
     * @return
     */
    public static List<String> getElementFromXML(InputStream xmlDoc, String xpath, boolean all) {

        List<String> result = new ArrayList<>();
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

        } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException ioe) {
            throw new RuntimeException(ioe);
        }

        return result;

    }

    private InputStream getHttpResponse(String url) throws IOException {

        HttpHost targetHost = new HttpHost(host, port, protocol);

        CredentialsProvider credProvider = new BasicCredentialsProvider();
        credProvider.setCredentials(new AuthScope(host, port), new UsernamePasswordCredentials(user, password));

        AuthCache authCache = new BasicAuthCache();

        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credProvider);
        context.setAuthCache(authCache);

        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpResponse r = httpClient.execute(targetHost, new HttpGet(url), context);

        if (r.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException("HTTP status code = [" + r.getStatusLine().getStatusCode() + "] was returned when accessing url = [" + url + "]");
        }

        return r.getEntity().getContent();

    }

}
