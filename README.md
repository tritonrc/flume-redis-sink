Flume Redis Sink
================

Flume Redis Sink allows you to use Redis as a Flume sink.

Build
-----

1. Copy this repo into flume_dir/plugins.  There should also be a helloworld directory there.

2. cd into redis-flume-sink

3. Build by running 'ant'. A redis-sink_plugin.jar should be created.

4. Modify your flume-site.xml.  If the following property exists then simply
append the following value to the existing value with a comma.
> 
>  <configuration>
>    <property>
>      <name>flume.plugin.classes</name>
>      <value>org.apache.flume.plugins.redis.RedisSink</value>
>      <description>Comma separated list of plugin classes</description>
>    </property>
>  </configuration>

5. Setup your FLUME_CLASSPATH to reference the necessary jars
>   export FLUME_CLASSPATH=$FLUME_CLASSPATH:flume_dir/plugins/flume-redis-sink/redis-sink_plugin.jar:flume_dir/plugins/flume-redis-sink/lib/jedis-1.5.2.jar:flume_dir/plugins/flume-redis-sink/lib/commons-pool-1.5.5.jar

Usage
_____

> redisSink("host:post", db, ["list_name"])

By default, this plugin will push to a list named "flume".  This value can be changed
either through the config value above or by setting a string attribute on the Flume event
with the name "destination_list".
