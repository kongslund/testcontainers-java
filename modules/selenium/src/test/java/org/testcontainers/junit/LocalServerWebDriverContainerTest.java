package org.testcontainers.junit;

import org.junit.*;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.ResourceHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;

/**
 * Test that a browser running in a container can access a web server hosted on the host machine (i.e. the one running
 * the tests)
 */
public class LocalServerWebDriverContainerTest {

    @Rule
    public BrowserWebDriverContainer chrome = new BrowserWebDriverContainer().withDesiredCapabilities(DesiredCapabilities.chrome());
    private int localPort;

    /**
     * The getHostIpAddress() method is only implemented for OS X running docker-machine. Skip JUnit execution elsewhere.
     */
    @BeforeClass
    public static void checkOS() {
        Assume.assumeTrue("These tests are currently only applicable to OS X", System.getProperty("os.name").toLowerCase().contains("mac"));
    }

    @Before
    public void setupLocalServer() throws Exception {

        // Set up a local Jetty HTTP server
        Server server = new Server();
        server.addConnector(new SocketConnector());
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase("src/test/resources/server");
        server.addHandler(resourceHandler);
        server.start();

        // The server will have a random port assigned, so capture that
        localPort = server.getConnectors()[0].getLocalPort();
    }

    @Test
    public void testConnection() throws InterruptedException {
        RemoteWebDriver driver = chrome.getWebDriver();

        // Construct a URL that the browser container can access
        String hostIpAddress = chrome.getHostIpAddress();
        driver.get("http://" + hostIpAddress + ":" + localPort);

        String headingText = driver.findElement(By.cssSelector("h1")).getText().trim();

        assertEquals("The hardcoded success message was found on a page fetched from a local server", "It worked", headingText);
    }
}
