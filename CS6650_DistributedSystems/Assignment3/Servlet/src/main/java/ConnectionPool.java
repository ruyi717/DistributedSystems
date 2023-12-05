import com.rabbitmq.client.*;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ConnectionPool extends BasePooledObjectFactory<Channel> {
  ConnectionFactory factory = new ConnectionFactory();

  @Override
  public Channel create() throws Exception {
    factory.setHost("54.186.190.248");
    factory.setUsername("ruyi");
    factory.setPassword("password");
    Connection connection = factory.newConnection();
    return connection.createChannel();
  }

  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    return new DefaultPooledObject<Channel>(channel);
  }


  @Override
  public void destroyObject(PooledObject<Channel> p) throws Exception {
    p.getObject().close();
  }
}