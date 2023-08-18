package org.apache.solr.update.processor;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.DeleteUpdateCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.apache.solr.update.processor.MirrorUpdateRequestProcessorFactory.log;


// <lib dir="/usr/local/share/solr-4.5.1/webapp/solr45/WEB-INF/lib/cvlibrary" regex=".*\.jar"/>
//..
//<requestHandler name="/update" class="solr.UpdateRequestHandler">
//    <lst name="defaults">
//      <str name="update.chain">mirror</str>
//    </lst>
//  </requestHandler>
//..
//<updateRequestProcessorChain name="mirror">
//      <processor class="org.apache.solr.update.processor.MirrorUpdateRequestProcessorFactory">
//        <str name="solrServerUrl">http://solr:8983/solr/account-requests</str>
//        <int name="queueSize">2</int>
//        <int name="threadCount">4</int>
//      </processor>
//      <processor class="solr.LogUpdateProcessorFactory" />
//      <processor class="solr.RunUpdateProcessorFactory" />
//  </updateRequestProcessorChain>

public class MirrorUpdateRequestProcessorFactory extends UpdateRequestProcessorFactory {

    public final static Logger log = LoggerFactory.getLogger(MirrorUpdateRequestProcessorFactory.class);

    public SolrParams args = null;
    private ConcurrentUpdateSolrServer client;

    @Override
    public void init(NamedList nl) {
        this.args = SolrParams.toSolrParams(nl);
        String solrServerUrl = args.get("solrServerUrl");
        if (solrServerUrl == null) {
            throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "solrServerUrl must be supplied");
        }
        int queueSize = args.getInt("queueSize", 1);
        int threadCount = args.getInt("threadCount", Runtime.getRuntime().availableProcessors());
        this.client = new ConcurrentUpdateSolrServer(solrServerUrl, queueSize, threadCount);
    }

    @Override
    public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
        return new MirrorUpdateRequestProcessor(client, next);
    }
}


final class MirrorUpdateRequestProcessor extends UpdateRequestProcessor {
    private final ConcurrentUpdateSolrServer client;

    public MirrorUpdateRequestProcessor(ConcurrentUpdateSolrServer client, UpdateRequestProcessor next) {
        super(next);
        this.client = client;
    }

    public void processAdd(AddUpdateCommand cmd) throws IOException {
        if (this.next != null) {
            try {
                SolrInputDocument doc = cmd.solrDoc.deepCopy();
                //assume complete overwrite for updates
                doc.remove("_VERSION_");
                doc.remove("boost");
                client.add(doc);
            } catch (SolrServerException e) {
                log.error("ex: processAdd:" + e);
                throw new RuntimeException(e);
            }
            this.next.processAdd(cmd);
        }
    }

    public void processDelete(DeleteUpdateCommand cmd) throws IOException {
        if (this.next != null) {
            try {
                if (cmd.isDeleteById()) {
                    String id = cmd.getId();
                    client.deleteById(id);
                } else {
                    String query = cmd.getQuery();
                    client.deleteByQuery(query);
                }
            } catch (SolrServerException e) {
                log.error("ex: processDelete:" + e);
                throw new RuntimeException(e);
            }
            this.next.processDelete(cmd);
        }
    }
}
