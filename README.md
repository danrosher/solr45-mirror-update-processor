# solr4.5 mirror update processor to any newer version

<u>Useful for migrating from solr 4.5 to say solr 9.x.</u> 

Will mirror adds/updates and deletes (by id and query) to a solr 
4.5 server to a later version of solr

Tested with 4.5 &rarr; 9.2.1

Once data is in sync, you can then migrate traffic from old to new Solr version, 
if you go through a proxy like HAProxy for example.

I had looked at [HAProxy Traffic Mirroring](https://www.haproxy.com/blog/haproxy-traffic-mirroring-for-real-world-testing)
but this is much simpler to write, setup and implement for Solr. Additionally, there are [issues due to largish payloads](https://github.com/haproxytech/spoa-mirror/issues/6)
that needs to be taken care of.

----

#### Config setup 
```xml
<config>
    
    <lib dir="../path/to/jar" regex=".*\.jar"/>
    
    <requestHandler name="/update" class="solr.UpdateRequestHandler">
        <lst name="defaults">
          <str name="update.chain">mirror</str>
        </lst>
    </requestHandler>
    
    <updateRequestProcessorChain name="mirror">
         <processor class="org.apache.solr.update.processor.MirrorUpdateRequestProcessorFactory">             
            <str name="solrServerUrl">http://solr:8983/solr/collection1</str>
            <lst name="solrParams">
                <bool name="failOnVersionConflicts">false</bool>
            </lst> 
            <int name="queueSize">2</int>
            <int name="threadCount">4</int>
          </processor>
          <processor class="solr.LogUpdateProcessorFactory" />
          <processor class="solr.RunUpdateProcessorFactory" />
    </updateRequestProcessorChain>

</config>

```
---
#### Building
create uber jar with 
```mvn
mvn clean package
```

If you want to change for different source version, you may need to change ```pom.xml``` to have 
```org.apache.solr:solr-solrj``` and ```org.apache.solr:solr-core``` to point to the correct source solr version

