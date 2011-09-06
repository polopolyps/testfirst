package com.polopoly.ps.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import com.polopoly.render.AbstractRenderDispatcher;
import com.polopoly.render.UserHistory;
import com.polopoly.user.server.Caller;

public abstract class AbstractMockedPageIntegrationTest extends
		AbstractIntegrationTest {
	private static final String TEST_CLASS_NAME_SUFFIX = "PageIntegrationTest";

	private static String lastUrl;
	private static Map<String, String> lastRequestParameters;
	private static Document htmlPage;

	/**
	 * Should be kept private; add getters and setters in this class for
	 * subclasses that need to access it.
	 */
	private static MockedPageIntegrationTestCaseAdapter testCaseAdapter;

	protected abstract String getRequestURI() throws Exception;

	private Caller oldCaller;

	@BeforeClass
	public static void newTestClass() {
		lastUrl = null;
		lastRequestParameters = null;
		htmlPage = null;
		testCaseAdapter = null;
	}

	@Override
	@Before
	public void checkClassName() throws Exception {
		if (!getClass().getName().endsWith(TEST_CLASS_NAME_SUFFIX)) {
			Assert.fail("Class name must end with " + TEST_CLASS_NAME_SUFFIX);
		}
	}

	protected static String uri(Content... contents) throws Exception {
		StringBuffer result = new StringBuffer("/cm");

		for (Content content : contents) {
			result.append('/');
			result.append(content.id());
		}

		return result.toString();
	}

	/**
	 * Intended for use with velocity asserts where the velocity variable is a
	 * list.
	 */
	protected List<?> list(Object... objects) {
		return Arrays.asList(objects);
	}

	/**
	 * If you want to add something, override setUpBeforePageLoad.
	 */
	@Before
	public final void setUpMockedServletContext() throws Exception {
		oldCaller = context.getPolicyCMServer().getCurrentCaller();

		// in case set up wants to load something filtered out.
		setUpBeforePageLoad();

		if (isSameAsLast()) {
			return;
		}

		loadPage();

		tearDownAfterPageLoad();
	}

	protected void loadPage() throws Exception {
		testCaseAdapter = createTestCaseAdapter();

		testCaseAdapter.setStackTraceExpected(isStacktraceExpected());

		testCaseAdapter.setUp();

		setUpUserTracking();

		setUpRequest();

		if (isPost()) {
			testCaseAdapter.doPost();
		} else {
			testCaseAdapter.doGet();
		}

		Assert.assertEquals("response status code not as expected",
				getExpectedStatusCode(),
				testCaseAdapter.getResponseStatusCode());

		if (isHtmlPage()) {
			if (!isAllowDoubleEncoding()) {
				checkDoubleEscapedText();
			}

			htmlPage = testCaseAdapter.getOutputAsJDOMDocument();
		}

		lastUrl = getRequestURI();
		lastRequestParameters = testCaseAdapter.getRequestParameters();
	}

	protected void forcePageLoadOnNextTest() {
		htmlPage = null;
	}

	/**
	 * false means "get"
	 */
	protected boolean isPost() {
		return false;
	}

	private boolean isSameAsLast() throws Exception {
		return htmlPage != null
				&& getRequestURI().equals(lastUrl)
				&& testCaseAdapter.getRequestParameters().equals(
						lastRequestParameters);
	}

	@After
	public void resetCaller() {
		context.getPolicyCMServer().setCurrentCaller(oldCaller);
	}

	protected void checkDoubleEscapedText() throws Exception {
		Pattern doubleEscapedPattern = Pattern.compile("&amp;(?=[\\w#]{1,7};)");
		BufferedReader reader = testCaseAdapter.getOutputAsBufferedReader();
		String line;
		while ((line = reader.readLine()) != null) {
			Matcher matcher = doubleEscapedPattern.matcher(line);
			if (matcher.find()) {
				Assert.fail("Double escaped output found under: "
						+ this.getRequestURI() + "\nLine content: "
						+ StringUtils.trim(line));
			}
		}
	}

	protected boolean isAllowDoubleEncoding() {
		return false;
	}

	protected void printRenderStats() {
		UserHistory uh = AbstractRenderDispatcher
				.getUserHistory(getTrackingUserId());

		System.out.println(uh.get(0).getRenderStats().getChildren());
	}

	/**
	 * Intended to be overridden.
	 */
	protected boolean isStacktraceExpected() {
		return false;
	}

	/**
	 * Intended for overriding.
	 */
	protected int getExpectedStatusCode() {
		return HttpServletResponse.SC_OK;
	}

	private String setUpUserTracking() {
		String userId = getTrackingUserId();

		testCaseAdapter.addCookie(
				com.polopoly.render.AbstractRenderDispatcher.COOKIE_USERID,
				userId);

		AbstractRenderDispatcher.trackUser(userId);

		RenderOperationStorage.storeOperations();

		return userId;
	}

	private String getTrackingUserId() {
		return "trackinguser";
	}

	protected void setUpBeforePageLoad() throws Exception {
		// intended for overriding
	}

	protected void tearDownAfterPageLoad() throws Exception {
		// intended for overriding
	}

	protected boolean isHtmlPage() {
		return true;
	}

	protected void assertCookieSet(String name, String value) {
		Cookie cookie = testCaseAdapter.getCookie(name);

		if (cookie == null) {
			Assert.fail("No cookie \"" + name + "\" set.");
		}

		Assert.assertEquals(value, cookie.getValue());
	}

	protected void assertCookieNotSet(String name) {
		Assert.assertNull("Cookie " + name + " was set.",
				testCaseAdapter.getCookie(name));
	}

	protected void assertHTMLContainsTextInXPath(String xPathExpression,
			String expectedText) throws Exception {
		String xml = asXml(selectFirstByXPath(xPathExpression));
		Assert.assertTrue("\n\"" + xml + '"' + " did not contain \n\""
				+ expectedText + '"', xml.contains(expectedText));
	}

	protected void assertHTMLDoesNotContainValueInXPath(String xPathExpression,
			String expectedText) throws Exception {
		String xml = selectFirstByXPath(xPathExpression).getValue();

		Assert.assertFalse('"' + xml + '"' + " did contain \"" + expectedText
				+ '"', xml.contains(expectedText));
	}

	protected void assertHTMLDoesNotContainTextInXPath(String xPathExpression,
			String expectedText) throws Exception {
		String xml = asXml(selectFirstByXPath(xPathExpression));

		Assert.assertFalse('"' + xml + '"' + " did contain \"" + expectedText
				+ '"', xml.contains(expectedText));
	}

	protected void assertHTMLContains(String expectedHtml) {
		Assert.assertTrue("Page did not contain \"" + expectedHtml + "\".",
				doesHtmlContain(expectedHtml));
	}

	/**
	 * This is a pretty ugly hack to turn nonbreaking spaces back into a
	 * readable entity encoding, as opposed to a literal nonbreaking space,
	 * which looks _exactly_ like a breaking space.
	 */
	protected boolean doesHtmlContain(String expectedHtml) {
		String temp = asXml(htmlPage.getRootElement());
		temp = temp.replaceAll(" ", "&nbsp;");
		return temp.contains(expectedHtml);
	}

	protected void assertHTMLContainsRegexp(String expectedPattern) {
		if (expectedPattern == null) {
			Assert.assertTrue("expected pattern was null.", false);
			return;
		}
		Pattern p = Pattern.compile(expectedPattern);
		Matcher m = p.matcher(asXml(htmlPage.getRootElement()));

		Assert.assertTrue("Page html did not match pattern " + expectedPattern
				+ " anywhere.", m.find());
	}

	protected void assertHTMLDoesNotContain(String expectedHtml) {
		Assert.assertFalse("Page did contain \"" + expectedHtml + "\".",
				doesHtmlContain(expectedHtml));
	}

	protected void assertHTMLContainsXPath(String xPathExpression)
			throws Exception {
		Element result = selectNonRequiredByXPath(xPathExpression);
		Assert.assertNotNull("There was no node matching " + xPathExpression
				+ " in " + getRequestURI(), result);
	}

	protected void assertHTMLDoesNotContainXPath(String xPathExpression)
			throws Exception {
		Element result = selectNonRequiredByXPath(xPathExpression);
		Assert.assertNull("There was a node matching " + xPathExpression
				+ " in " + getRequestURI(), result);
	}

	protected Cookie getCookie(String name) {
		return testCaseAdapter.getCookie(name);
	}

	protected void setUpRequest() throws Exception {
		// intended to be overridden
	}

	protected MockedPageIntegrationTestCaseAdapter createTestCaseAdapter()
			throws Exception {
		return new MockedPageIntegrationTestCaseAdapter(getRequestURI(),
				context) {
			// nothing to implement.
		};
	}

	/**
	 * The XPath expression accepted by this method is not completely standard.
	 * Specifically, we've added a `~` operator that allows for "contains"
	 * matches against attributes. For com.polopoly.ps: `//div[@class='omg']`
	 * doesn't match `<div class="omg srsly"></div>`, as the class doesn't
	 * exactly match the string "omg". On the other hand `//div[@class~'omg']`
	 * will match this element.
	 * 
	 * It's magic!
	 * 
	 * @param xPathExpression
	 *            {String}
	 * @param rootElement
	 *            {Element}
	 */
	@SuppressWarnings("unchecked")
	protected List<Element> selectAllByXPath(String xPathExpression,
			Element rootElement) throws Exception {
		XPath xPath = XPath
				.newInstance(parseXPathContainsOperator(xPathExpression));

		return xPath.selectNodes(rootElement);
	}

	@SuppressWarnings("unchecked")
	protected List<Element> selectAllByXPath(String xPathExpression)
			throws Exception {
		List<Element> result = XPath.newInstance(
				parseXPathContainsOperator(xPathExpression)).selectNodes(
				htmlPage);
		Assert.assertNotNull("There were no nodes matching " + xPathExpression
				+ " on " + getRequestURI(), result);
		return result;
	}

	protected Element selectFirstByXPath(String xPathExpression,
			Element rootElement) throws Exception {
		XPath xPath = XPath
				.newInstance(parseXPathContainsOperator(xPathExpression));

		return (Element) xPath.selectSingleNode(rootElement);
	}

	protected Element selectFirstByXPath(String xPathExpression)
			throws Exception {
		Element result = selectNonRequiredByXPath(xPathExpression);

		Assert.assertNotNull("There was no node matching " + xPathExpression
				+ " on " + getRequestURI(), result);

		return result;
	}

	/**
	 * @see getByXPath
	 */
	protected void assertNoMatchingXPath(String xPathExpression)
			throws Exception {
		Object result = selectNonRequiredByXPath(xPathExpression);

		Assert.assertNull("There was a node matching " + xPathExpression
				+ " on " + getRequestURI(), result);
	}

	/**
	 * @see getByXPath
	 */
	protected void assertMatchingXPath(String xPathExpression) throws Exception {
		Object result = selectNonRequiredByXPath(xPathExpression);

		Assert.assertNotNull("There was no node matching " + xPathExpression
				+ " on " + getRequestURI(), result);
	}

	protected String parseXPathContainsOperator(String xPathExpression) {
		return xPathExpression.replaceAll("@([a-zA-Z]+)~'([^']+)'",
				"contains(concat(' ',normalize-space(@$1),' '),' $2 ')");
	}

	private Element selectNonRequiredByXPath(String xPathExpression)
			throws JDOMException {
		XPath xPath = XPath
				.newInstance(parseXPathContainsOperator(xPathExpression));

		return (Element) xPath.selectSingleNode(htmlPage);
	}

	protected String asXml(Element element) {
		if (element == null) {
			return null;
		}

		XMLOutputter out = new XMLOutputter();
		return out.outputString(element);
	}

	protected String getOutputAsString() throws Exception {
		BufferedReader reader = null;
		try {
			StringWriter writer = new StringWriter();
			reader = testCaseAdapter.getOutputAsBufferedReader();

			copy(reader, writer);

			return writer.toString();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private void copy(BufferedReader reader, StringWriter writer)
			throws IOException {
		int i;

		while ((i = reader.read()) != -1) {
			writer.write(i);
		}
	}

	protected int getResponseStatusCode() {
		return testCaseAdapter.getResponseStatusCode();
	}

	protected String getResponseHeader(String key) {
		return testCaseAdapter.getResponseHeader(key);
	}

	protected void setQueryString(String queryString) {
		testCaseAdapter.setQueryString(queryString);
	}

	protected void setRequestParameter(String name, String value) {
		testCaseAdapter.addRequestParameter(name, value);
	}

	protected void addCookie(String name, String value) {
		testCaseAdapter.addCookie(name, value);
	}

	protected boolean wasForwardedToURI(String uri) {
		return testCaseAdapter.wasForwardedToURI(uri);
	}

	protected boolean wasForwardedToURIContaining(String uriPart) {
		return testCaseAdapter.wasForwardedToURIContaining(uriPart);
	}

	protected void assertLinkOnPage(String url) {
		boolean found = false;
		try {

			List<Element> linksOnPage = selectAllByXPath("//a");
			for (Element testLink : linksOnPage) {
				if (url.equals(testLink.getAttributeValue("href"))) {
					found = true;
					break;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Assert.assertTrue("The url " + url
				+ " was not found in an <a href='XXX'> construct on the page",
				found);
	}

	protected void assertImageOnPage(int width, int height, String src,
			String alt) {
		boolean found = false;

		try {
			List<Element> imagesOnPage = selectAllByXPath("//img");
			for (Element imageElement : imagesOnPage) {
				if (containsElementAttributes(imageElement,
						String.valueOf(width), String.valueOf(height), src, alt)) {
					found = true;
					break;
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Assert.assertTrue("The img tag with attributes width:" + width
				+ ", height:" + height + ", src:" + src + ", alt:" + alt
				+ " was not found on the page.", found);
	}

	private boolean containsElementAttributes(Element imageElement,
			String width, String height, String src, String alt) {
		String widthValue = imageElement.getAttributeValue("width");
		String heightValue = imageElement.getAttributeValue("height");
		String srcValue = imageElement.getAttributeValue("src");
		String altValue = imageElement.getAttributeValue("alt");
		// For some reason the urlBuilder creates a path that start with the
		// string null/ That make imposible to use equals method to compare the
		// src. It must to be fixed.

		if (width.equals(widthValue) && height.equals(heightValue)
				&& srcValue.contains(src) && alt.equals(altValue)) {
			return true;
		}
		return false;
	}

	/**
	 * This method tests fetching a page in autonomous mode, i.e. with the
	 * backend disconnected.<br>
	 * It is quite normal that things don't work 100% in this mode, but this
	 * test checks that no exceptions are thrown by the rendering process
	 * (exceptions that get through Velocity). If exceptions *are* thrown during
	 * rendering in autonomous mode, that means that the real site would have
	 * given a 500 Internal Server Error to the user. We are expected to handle
	 * such scenarios more gracefully.<br>
	 * If you really feel that this test is not testing anything of value in
	 * your particular PageIntegrationTest, you can always override it with an
	 * empty implementation.
	 */
	public void testAutonomousMode() throws Exception {
		if (skipAutonomousModeTest()) {
			return;
		}

		if (!ClientControlUtil.disconnectClient()) {
			// we should really fail here but on some machines its consistently
			// impossible to put the client into autonomous mode and once we
			// have fixed it we should introduce a fail here. (SZCMS-1220)
			return;
		}

		try {
			loadPage();
		} finally {
			ClientControlUtil.connectClient();
		}
		forcePageLoadOnNextTest();
	}

	protected boolean skipAutonomousModeTest() {
		return false;
	}

}
