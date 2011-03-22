package org.apache.flume.plugins.redis;

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.SinkFactory.SinkBuilder;
import com.cloudera.flume.core.Attributes;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventSink;
import com.cloudera.util.Pair;

import com.google.common.base.Preconditions;

import org.apache.commons.pool.impl.GenericObjectPool.Config;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisSink extends EventSink.Base {
  private static String DEFAULT_LIST = "flume";
  private static String DESTINATION_LIST = "destination_list";

  private JedisPool jedisPool;

  private String host;
  private int port;
  private int db;
  private String defaultList;

  public RedisSink(String host, int port, int db, String defaultList) {
    this.host = host;
    this.port = port;
    this.db = db;
    this.defaultList = defaultList;
  }

  @Override
  public void open() throws IOException {
    this.jedisPool = new JedisPool(new Config(), this.host, this.port);
  }

  @Override
  public void append(Event e) throws IOException {
    String list = Attributes.readString(e, DESTINATION_LIST);
   
    if(list == null) 
      list = this.defaultList;
    
    boolean retried = false;
    
    Jedis jedis = this.jedisPool.getResource();
    
    // Did the server close the connection?
    if(!jedis.isConnected()) {
      this.jedisPool.returnBrokenResource(jedis);
      jedis = this.jedisPool.getResource();
    }
    
    try {
      jedis.select(this.db);
      jedis.lpush(list, new String(e.getBody()));
      this.jedisPool.returnResource(jedis);
    } finally {
      this.jedisPool.returnBrokenResource(jedis);
    }
  }

  @Override
  public void close() throws IOException {
    this.jedisPool.destroy();
  }
 
  public static SinkBuilder builder() {
    return new SinkBuilder() {
      // construct a new parameterized sink
      @Override
      public EventSink build(Context context, String... argv) {
        Preconditions.checkArgument(argv.length >= 2, "usage: redisSink(\"address:port\", db, [\"default_list\"])");

        String[] addressPort = argv[0].split(":");
        int port = (addressPort.length > 1) ? Integer.parseInt(addressPort[1]) : 6379;
        
        String destinationList = DEFAULT_LIST;
        if(argv.length == 3)
          destinationList = argv[2];
        
        return new RedisSink(addressPort[0], port, Integer.parseInt(argv[1]), destinationList);
      }
    };
  }

  /**
   * This is a special function used by the SourceFactory to pull in this class
   * as a plugin sink.
   */
  public static List<Pair<String, SinkBuilder>> getSinkBuilders() {
    List<Pair<String, SinkBuilder>> builders =
      new ArrayList<Pair<String, SinkBuilder>>();
    builders.add(new Pair<String, SinkBuilder>("redisSink", builder()));
    return builders;
  }
  
}
