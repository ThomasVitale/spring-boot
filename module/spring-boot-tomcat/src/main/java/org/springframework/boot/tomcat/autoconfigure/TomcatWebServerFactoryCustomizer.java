/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.tomcat.autoconfigure;

import java.time.Duration;
import java.util.List;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.catalina.valves.ErrorReportValve;
import org.apache.catalina.valves.RemoteIpValve;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.UpgradeProtocol;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.apache.coyote.http2.Http2Protocol;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeAttribute;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.tomcat.ConfigurableTomcatWebServerFactory;
import org.springframework.boot.tomcat.autoconfigure.TomcatServerProperties.Accesslog;
import org.springframework.boot.tomcat.autoconfigure.TomcatServerProperties.Remoteip;
import org.springframework.boot.tomcat.autoconfigure.TomcatServerProperties.UseApr;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;

/**
 * Customization for Tomcat-specific features common to both Servlet and Reactive servers.
 *
 * @author Brian Clozel
 * @author Yulin Qin
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @author Artsiom Yudovin
 * @author Chentao Qu
 * @author Andrew McGhie
 * @author Dirk Deyne
 * @author Rafiullah Hamedy
 * @author Victor Mandujano
 * @author Parviz Rozikov
 * @author Florian Storz
 * @author Michael Weidmann
 * @since 4.0.0
 */
public class TomcatWebServerFactoryCustomizer
		implements WebServerFactoryCustomizer<ConfigurableTomcatWebServerFactory>, Ordered {

	static final int ORDER = 0;

	private final Environment environment;

	private final ServerProperties serverProperties;

	private final TomcatServerProperties tomcatProperties;

	public TomcatWebServerFactoryCustomizer(Environment environment, ServerProperties serverProperties,
			TomcatServerProperties tomcatProperties) {
		this.environment = environment;
		this.serverProperties = serverProperties;
		this.tomcatProperties = tomcatProperties;
	}

	@Override
	public int getOrder() {
		return ORDER;
	}

	@Override
	@SuppressWarnings("removal")
	public void customize(ConfigurableTomcatWebServerFactory factory) {
		PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
		map.from(this.tomcatProperties::getBasedir).to(factory::setBaseDirectory);
		map.from(this.tomcatProperties::getBackgroundProcessorDelay)
			.as(Duration::getSeconds)
			.as(Long::intValue)
			.to(factory::setBackgroundProcessorDelay);
		customizeRemoteIpValve(factory);
		TomcatServerProperties.Threads threadProperties = this.tomcatProperties.getThreads();
		map.from(threadProperties::getMax)
			.when(this::isPositive)
			.to((maxThreads) -> customizeMaxThreads(factory, maxThreads));
		map.from(threadProperties::getMinSpare)
			.when(this::isPositive)
			.to((minSpareThreads) -> customizeMinThreads(factory, minSpareThreads));
		map.from(threadProperties::getMaxQueueCapacity)
			.when(this::isPositive)
			.to((maxQueueCapacity) -> customizeMaxQueueCapacity(factory, maxQueueCapacity));
		map.from(this.serverProperties.getMaxHttpRequestHeaderSize())
			.asInt(DataSize::toBytes)
			.when(this::isPositive)
			.to((maxHttpRequestHeaderSize) -> customizeMaxHttpRequestHeaderSize(factory, maxHttpRequestHeaderSize));
		map.from(this.tomcatProperties::getMaxHttpResponseHeaderSize)
			.asInt(DataSize::toBytes)
			.when(this::isPositive)
			.to((maxHttpResponseHeaderSize) -> customizeMaxHttpResponseHeaderSize(factory, maxHttpResponseHeaderSize));
		map.from(this.tomcatProperties::getMaxSwallowSize)
			.asInt(DataSize::toBytes)
			.to((maxSwallowSize) -> customizeMaxSwallowSize(factory, maxSwallowSize));
		map.from(this.tomcatProperties::getMaxHttpFormPostSize)
			.asInt(DataSize::toBytes)
			.when((maxHttpFormPostSize) -> maxHttpFormPostSize != 0)
			.to((maxHttpFormPostSize) -> customizeMaxHttpFormPostSize(factory, maxHttpFormPostSize));
		map.from(this.tomcatProperties::getMaxParameterCount)
			.to((maxParameterCount) -> customizeMaxParameterCount(factory, maxParameterCount));
		map.from(this.tomcatProperties::getMaxPartHeaderSize)
			.asInt(DataSize::toBytes)
			.to((maxPartHeaderSize) -> customizeMaxPartHeaderSize(factory, maxPartHeaderSize));
		map.from(this.tomcatProperties::getMaxPartCount)
			.to((maxPartCount) -> customizeMaxPartCount(factory, maxPartCount));
		map.from(this.tomcatProperties::getAccesslog)
			.when(TomcatServerProperties.Accesslog::isEnabled)
			.to((enabled) -> customizeAccessLog(factory));
		map.from(this.tomcatProperties::getUriEncoding).to(factory::setUriEncoding);
		map.from(this.tomcatProperties::getConnectionTimeout)
			.to((connectionTimeout) -> customizeConnectionTimeout(factory, connectionTimeout));
		map.from(this.tomcatProperties::getMaxConnections)
			.when(this::isPositive)
			.to((maxConnections) -> customizeMaxConnections(factory, maxConnections));
		map.from(this.tomcatProperties::getAcceptCount)
			.when(this::isPositive)
			.to((acceptCount) -> customizeAcceptCount(factory, acceptCount));
		map.from(this.tomcatProperties::getProcessorCache)
			.to((processorCache) -> customizeProcessorCache(factory, processorCache));
		map.from(this.tomcatProperties::getKeepAliveTimeout)
			.to((keepAliveTimeout) -> customizeKeepAliveTimeout(factory, keepAliveTimeout));
		map.from(this.tomcatProperties::getMaxKeepAliveRequests)
			.to((maxKeepAliveRequests) -> customizeMaxKeepAliveRequests(factory, maxKeepAliveRequests));
		map.from(this.tomcatProperties::getRelaxedPathChars)
			.as(this::joinCharacters)
			.whenHasText()
			.to((relaxedChars) -> customizeRelaxedPathChars(factory, relaxedChars));
		map.from(this.tomcatProperties::getRelaxedQueryChars)
			.as(this::joinCharacters)
			.whenHasText()
			.to((relaxedChars) -> customizeRelaxedQueryChars(factory, relaxedChars));
		map.from(this.tomcatProperties.getMbeanregistry()::isEnabled)
			.as((enable) -> !enable)
			.to(factory::setDisableMBeanRegistry);
		customizeStaticResources(factory);
		customizeErrorReportValve(this.serverProperties.getError(), factory);
		factory.setUseApr(getUseApr(this.tomcatProperties.getUseApr()));
	}

	private boolean getUseApr(UseApr useApr) {
		return switch (useApr) {
			case ALWAYS -> {
				Assert.state(isAprAvailable(), "APR has been configured to 'ALWAYS', but it's not available");
				yield true;
			}
			case WHEN_AVAILABLE -> isAprAvailable();
			case NEVER -> false;
		};
	}

	private boolean isAprAvailable() {
		// At least one instance of AprLifecycleListener has to be created for
		// isAprAvailable() to work
		new AprLifecycleListener();
		return AprLifecycleListener.isAprAvailable();
	}

	private boolean isPositive(int value) {
		return value > 0;
	}

	@SuppressWarnings("rawtypes")
	private void customizeMaxThreads(ConfigurableTomcatWebServerFactory factory, int maxThreads) {
		customizeHandler(factory, maxThreads, AbstractProtocol.class, AbstractProtocol::setMaxThreads);
	}

	@SuppressWarnings("rawtypes")
	private void customizeMinThreads(ConfigurableTomcatWebServerFactory factory, int minSpareThreads) {
		customizeHandler(factory, minSpareThreads, AbstractProtocol.class, AbstractProtocol::setMinSpareThreads);
	}

	@SuppressWarnings("rawtypes")
	private void customizeMaxQueueCapacity(ConfigurableTomcatWebServerFactory factory, int maxQueueCapacity) {
		customizeHandler(factory, maxQueueCapacity, AbstractProtocol.class, AbstractProtocol::setMaxQueueSize);
	}

	@SuppressWarnings("rawtypes")
	private void customizeAcceptCount(ConfigurableTomcatWebServerFactory factory, int acceptCount) {
		customizeHandler(factory, acceptCount, AbstractProtocol.class, AbstractProtocol::setAcceptCount);
	}

	@SuppressWarnings("rawtypes")
	private void customizeProcessorCache(ConfigurableTomcatWebServerFactory factory, int processorCache) {
		customizeHandler(factory, processorCache, AbstractProtocol.class, AbstractProtocol::setProcessorCache);
	}

	private void customizeKeepAliveTimeout(ConfigurableTomcatWebServerFactory factory, Duration keepAliveTimeout) {
		factory.addConnectorCustomizers((connector) -> {
			ProtocolHandler handler = connector.getProtocolHandler();
			for (UpgradeProtocol upgradeProtocol : handler.findUpgradeProtocols()) {
				if (upgradeProtocol instanceof Http2Protocol protocol) {
					protocol.setKeepAliveTimeout(keepAliveTimeout.toMillis());
				}
			}
			if (handler instanceof AbstractProtocol<?> protocol) {
				protocol.setKeepAliveTimeout((int) keepAliveTimeout.toMillis());
			}
		});
	}

	@SuppressWarnings("rawtypes")
	private void customizeMaxKeepAliveRequests(ConfigurableTomcatWebServerFactory factory, int maxKeepAliveRequests) {
		customizeHandler(factory, maxKeepAliveRequests, AbstractHttp11Protocol.class,
				AbstractHttp11Protocol::setMaxKeepAliveRequests);
	}

	@SuppressWarnings("rawtypes")
	private void customizeMaxConnections(ConfigurableTomcatWebServerFactory factory, int maxConnections) {
		customizeHandler(factory, maxConnections, AbstractProtocol.class, AbstractProtocol::setMaxConnections);
	}

	@SuppressWarnings("rawtypes")
	private void customizeConnectionTimeout(ConfigurableTomcatWebServerFactory factory, Duration connectionTimeout) {
		customizeHandler(factory, (int) connectionTimeout.toMillis(), AbstractProtocol.class,
				AbstractProtocol::setConnectionTimeout);
	}

	private void customizeRelaxedPathChars(ConfigurableTomcatWebServerFactory factory, String relaxedChars) {
		factory.addConnectorCustomizers((connector) -> connector.setProperty("relaxedPathChars", relaxedChars));
	}

	private void customizeRelaxedQueryChars(ConfigurableTomcatWebServerFactory factory, String relaxedChars) {
		factory.addConnectorCustomizers((connector) -> connector.setProperty("relaxedQueryChars", relaxedChars));
	}

	private String joinCharacters(List<Character> content) {
		return content.stream().map(String::valueOf).collect(Collectors.joining());
	}

	private void customizeRemoteIpValve(ConfigurableTomcatWebServerFactory factory) {
		Remoteip remoteIpProperties = this.tomcatProperties.getRemoteip();
		String protocolHeader = remoteIpProperties.getProtocolHeader();
		String remoteIpHeader = remoteIpProperties.getRemoteIpHeader();
		// For back compatibility the valve is also enabled if protocol-header is set
		if (StringUtils.hasText(protocolHeader) || StringUtils.hasText(remoteIpHeader)
				|| getOrDeduceUseForwardHeaders()) {
			RemoteIpValve valve = new RemoteIpValve();
			valve.setProtocolHeader(StringUtils.hasLength(protocolHeader) ? protocolHeader : "X-Forwarded-Proto");
			if (StringUtils.hasLength(remoteIpHeader)) {
				valve.setRemoteIpHeader(remoteIpHeader);
			}
			valve.setTrustedProxies(remoteIpProperties.getTrustedProxies());
			// The internal proxies default to a list of "safe" internal IP addresses
			valve.setInternalProxies(remoteIpProperties.getInternalProxies());
			try {
				valve.setHostHeader(remoteIpProperties.getHostHeader());
			}
			catch (NoSuchMethodError ex) {
				// Avoid failure with war deployments to Tomcat 8.5 before 8.5.44 and
				// Tomcat 9 before 9.0.23
			}
			valve.setPortHeader(remoteIpProperties.getPortHeader());
			valve.setProtocolHeaderHttpsValue(remoteIpProperties.getProtocolHeaderHttpsValue());
			// ... so it's safe to add this valve by default.
			factory.addEngineValves(valve);
		}
	}

	private boolean getOrDeduceUseForwardHeaders() {
		if (this.serverProperties.getForwardHeadersStrategy() == null) {
			CloudPlatform platform = CloudPlatform.getActive(this.environment);
			return platform != null && platform.isUsingForwardHeaders();
		}
		return this.serverProperties.getForwardHeadersStrategy() == ServerProperties.ForwardHeadersStrategy.NATIVE;
	}

	@SuppressWarnings("rawtypes")
	private void customizeMaxHttpRequestHeaderSize(ConfigurableTomcatWebServerFactory factory,
			int maxHttpRequestHeaderSize) {
		customizeHandler(factory, maxHttpRequestHeaderSize, AbstractHttp11Protocol.class,
				AbstractHttp11Protocol::setMaxHttpRequestHeaderSize);
	}

	@SuppressWarnings("rawtypes")
	private void customizeMaxHttpResponseHeaderSize(ConfigurableTomcatWebServerFactory factory,
			int maxHttpResponseHeaderSize) {
		customizeHandler(factory, maxHttpResponseHeaderSize, AbstractHttp11Protocol.class,
				AbstractHttp11Protocol::setMaxHttpResponseHeaderSize);
	}

	@SuppressWarnings("rawtypes")
	private void customizeMaxSwallowSize(ConfigurableTomcatWebServerFactory factory, int maxSwallowSize) {
		customizeHandler(factory, maxSwallowSize, AbstractHttp11Protocol.class,
				AbstractHttp11Protocol::setMaxSwallowSize);
	}

	private <T extends ProtocolHandler> void customizeHandler(ConfigurableTomcatWebServerFactory factory, int value,
			Class<T> type, ObjIntConsumer<T> consumer) {
		factory.addConnectorCustomizers((connector) -> {
			ProtocolHandler handler = connector.getProtocolHandler();
			if (type.isAssignableFrom(handler.getClass())) {
				consumer.accept(type.cast(handler), value);
			}
		});
	}

	private void customizeMaxHttpFormPostSize(ConfigurableTomcatWebServerFactory factory, int maxHttpFormPostSize) {
		factory.addConnectorCustomizers((connector) -> connector.setMaxPostSize(maxHttpFormPostSize));
	}

	private void customizeMaxParameterCount(ConfigurableTomcatWebServerFactory factory, int maxParameterCount) {
		factory.addConnectorCustomizers((connector) -> connector.setMaxParameterCount(maxParameterCount));
	}

	private void customizeMaxPartCount(ConfigurableTomcatWebServerFactory factory, int maxPartCount) {
		factory.addConnectorCustomizers((connector) -> {
			try {
				connector.setMaxPartCount(maxPartCount);
			}
			catch (NoSuchMethodError ex) {
				// Tomcat < 10.1.42
			}
		});
	}

	private void customizeMaxPartHeaderSize(ConfigurableTomcatWebServerFactory factory, int maxPartHeaderSize) {
		factory.addConnectorCustomizers((connector) -> {
			try {
				connector.setMaxPartHeaderSize(maxPartHeaderSize);
			}
			catch (NoSuchMethodError ex) {
				// Tomcat < 10.1.42
			}
		});
	}

	private void customizeAccessLog(ConfigurableTomcatWebServerFactory factory) {
		AccessLogValve valve = new AccessLogValve();
		PropertyMapper map = PropertyMapper.get();
		Accesslog accessLogConfig = this.tomcatProperties.getAccesslog();
		map.from(accessLogConfig.getConditionIf()).to(valve::setConditionIf);
		map.from(accessLogConfig.getConditionUnless()).to(valve::setConditionUnless);
		map.from(accessLogConfig.getPattern()).to(valve::setPattern);
		map.from(accessLogConfig.getDirectory()).to(valve::setDirectory);
		map.from(accessLogConfig.getPrefix()).to(valve::setPrefix);
		map.from(accessLogConfig.getSuffix()).to(valve::setSuffix);
		map.from(accessLogConfig.getEncoding()).whenHasText().to(valve::setEncoding);
		map.from(accessLogConfig.getLocale()).whenHasText().to(valve::setLocale);
		map.from(accessLogConfig.isCheckExists()).to(valve::setCheckExists);
		map.from(accessLogConfig.isRotate()).to(valve::setRotatable);
		map.from(accessLogConfig.isRenameOnRotate()).to(valve::setRenameOnRotate);
		map.from(accessLogConfig.getMaxDays()).to(valve::setMaxDays);
		map.from(accessLogConfig.getFileDateFormat()).to(valve::setFileDateFormat);
		map.from(accessLogConfig.isIpv6Canonical()).to(valve::setIpv6Canonical);
		map.from(accessLogConfig.isRequestAttributesEnabled()).to(valve::setRequestAttributesEnabled);
		map.from(accessLogConfig.isBuffered()).to(valve::setBuffered);
		factory.addEngineValves(valve);
	}

	private void customizeStaticResources(ConfigurableTomcatWebServerFactory factory) {
		TomcatServerProperties.Resource resource = this.tomcatProperties.getResource();
		factory.addContextCustomizers((context) -> context.addLifecycleListener((event) -> {
			if (event.getType().equals(Lifecycle.CONFIGURE_START_EVENT)) {
				context.getResources().setCachingAllowed(resource.isAllowCaching());
				if (resource.getCacheTtl() != null) {
					long ttl = resource.getCacheTtl().toMillis();
					context.getResources().setCacheTtl(ttl);
				}
			}
		}));
	}

	private void customizeErrorReportValve(ErrorProperties error, ConfigurableTomcatWebServerFactory factory) {
		if (error.getIncludeStacktrace() == IncludeAttribute.NEVER) {
			factory.addContextCustomizers((context) -> {
				ErrorReportValve valve = new ErrorReportValve();
				valve.setShowServerInfo(false);
				valve.setShowReport(false);
				context.getParent().getPipeline().addValve(valve);
			});
		}
	}

}
