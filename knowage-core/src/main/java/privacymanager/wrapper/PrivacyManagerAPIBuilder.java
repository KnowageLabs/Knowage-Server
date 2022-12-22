package privacymanager.wrapper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class PrivacyManagerAPIBuilder {

	private URL url;
	private String appId;

	public static PrivacyManagerAPIBuilder newBuilder() {
		return new PrivacyManagerAPIBuilder();
	}

	private PrivacyManagerAPIBuilder() {
		super();
	}

	public PrivacyManagerAPIBuilder withUrl(URL url) {
		this.url = url;
		return this;
	}

	public PrivacyManagerAPIBuilder withUrl(String url) throws MalformedURLException {
		this.url = new URL(url);
		return this;
	}

	public PrivacyManagerAPIBuilder withAppId(String appId) {
		this.appId = appId;
		return this;
	}

	public IPrivacyManagerAPI build() {
		Objects.requireNonNull(url, "The URL cannot be null");
		Objects.requireNonNull(appId, "The application id cannot be null");

		IPrivacyManagerAPI ret = new PrivacyManagerAPIImpl(url, appId);

		return ret;
	}
}
