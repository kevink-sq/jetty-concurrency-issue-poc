package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.unixdomain.server.UnixDomainServerConnector;
import org.eclipse.jetty.unixsocket.server.UnixSocketConnector;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.newsclub.net.unix.jetty.AFSocketServerConnector;

public class Main {

  static public class Process extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      resp.setContentType("application/json");
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.getWriter().println("{\"message\": \"hello\"}");
    }
  }

  public static void main(String[] args) throws Exception {

    Server server = new Server();
    server.setConnectors(new Connector[] {
        junixConnector(server, "junix-ingress.sock"),
        jnrConnector(server, "jnr-ingress.sock"),
        nativeConnector(server, "native-ingress.sock")
        });
    ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping(Process.class, "/process");
    server.setHandler(handler);
    server.start();
  }

  private static Connector junixConnector(Server server, String socketPath) throws Exception {
    removeIfExists(socketPath);
    AFSocketServerConnector connector = new AFSocketServerConnector(
        server,
        new ExecutorThreadPool(300, 300),
        null,
        null,
        1,
        1,
        new HttpConnectionFactory(new HttpConfiguration()));
    connector.setListenSocketAddress(AFUNIXSocketAddress.of(new File(socketPath)));
    return connector;
  }

  private static Connector jnrConnector(Server server, String socketPath) throws Exception {
    removeIfExists((socketPath));
    UnixSocketConnector connector = new UnixSocketConnector(server,
        new ExecutorThreadPool(300, 300), null, null,
        1,
        new HttpConnectionFactory(new HttpConfiguration()));
    connector.setUnixSocket(socketPath);
    return connector;
  }

  private static Connector nativeConnector(Server server, String socketPath) throws Exception {
    removeIfExists(socketPath);
    UnixDomainServerConnector connector = new UnixDomainServerConnector(
        server,
        new ExecutorThreadPool(300, 300), null, null,
        1,
        1,
        new HttpConnectionFactory(new HttpConfiguration()));
    connector.setUnixDomainPath(new File(socketPath).toPath());
    return connector;
  }

  private static void removeIfExists(String socketPath) throws  Exception{
    Path path = Paths.get(socketPath);
    if (Files.exists(path)) {
      Files.delete(path);
    }
  }
}