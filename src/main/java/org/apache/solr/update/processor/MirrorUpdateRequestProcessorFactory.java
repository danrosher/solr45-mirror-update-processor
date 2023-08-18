package org.apache.solr.update.processor;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.DeleteUpdateCommand;

import java.io.IOException;

/** A pass through processor that does nothing. */
public class MirrorUpdateRequestProcessorFactory extends UpdateRequestProcessorFactory
{
    public NamedList args = null;

    @Override
    public void init( NamedList args )
    {
        this.args = args;
    }

    @Override
    public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
        return new MirrorUpdateRequestProcessor(next);
    }
}


static final  class MirrorUpdateRequestProcessor extends UpdateRequestProcessor {
    public MirrorUpdateRequestProcessor(UpdateRequestProcessor next) {
        super(next);
    }

    public void processAdd(AddUpdateCommand cmd) throws IOException {
        if (this.next != null) {
            this.next.processAdd(cmd);
        }

    }

    public void processDelete(DeleteUpdateCommand cmd) throws IOException {
        if (this.next != null) {
            this.next.processDelete(cmd);
        }

    }
}
