/*
 * Copyright © 2014-2016 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.metrics.runtime;

import co.cask.cdap.api.metrics.MetricsCollectionService;
import co.cask.cdap.api.metrics.MetricsContext;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.guice.ConfigModule;
import co.cask.cdap.common.guice.DiscoveryRuntimeModule;
import co.cask.cdap.common.guice.IOModule;
import co.cask.cdap.common.guice.KafkaClientModule;
import co.cask.cdap.common.guice.LocationRuntimeModule;
import co.cask.cdap.common.guice.ZKClientModule;
import co.cask.cdap.common.logging.LoggingContextAccessor;
import co.cask.cdap.common.logging.ServiceLoggingContext;
import co.cask.cdap.common.namespace.guice.NamespaceClientRuntimeModule;
import co.cask.cdap.common.twill.AbstractMasterTwillRunnable;
import co.cask.cdap.data.runtime.DataFabricModules;
import co.cask.cdap.data.runtime.DataSetsModules;
import co.cask.cdap.data2.audit.AuditModule;
import co.cask.cdap.logging.appender.LogAppenderInitializer;
import co.cask.cdap.logging.guice.LoggingModules;
import co.cask.cdap.metrics.guice.MetricsClientRuntimeModule;
import co.cask.cdap.metrics.guice.MetricsProcessorStatusServiceModule;
import co.cask.cdap.metrics.guice.MetricsStoreModule;
import co.cask.cdap.metrics.process.KafkaMetricsProcessorServiceFactory;
import co.cask.cdap.metrics.process.MessageCallbackFactory;
import co.cask.cdap.metrics.process.MetricsMessageCallbackFactory;
import co.cask.cdap.metrics.process.MetricsProcessorStatusService;
import co.cask.cdap.metrics.store.DefaultMetricDatasetFactory;
import co.cask.cdap.metrics.store.MetricDatasetFactory;
import co.cask.cdap.proto.id.NamespaceId;
import co.cask.cdap.security.auth.context.AuthenticationContextModules;
import co.cask.cdap.security.authorization.AuthorizationEnforcementModule;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Service;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import org.apache.hadoop.conf.Configuration;
import org.apache.twill.api.TwillContext;
import org.apache.twill.kafka.client.KafkaClientService;
import org.apache.twill.zookeeper.ZKClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Twill Runnable to run MetricsProcessor in YARN.
 */
public final class MetricsProcessorTwillRunnable extends AbstractMasterTwillRunnable {
  private static final Logger LOG = LoggerFactory.getLogger(MetricsProcessorTwillRunnable.class);

  public static final ImmutableMap<String, String> METRICS_PROCESSOR_CONTEXT =
    ImmutableMap.of(Constants.Metrics.Tag.NAMESPACE, NamespaceId.SYSTEM.getNamespace(),
                    Constants.Metrics.Tag.COMPONENT, Constants.Service.METRICS_PROCESSOR);

  private MetricsProcessorService metricsProcessorService;
  private ZKClientService zkClientService;
  private KafkaClientService kafkaClientService;
  private MetricsProcessorStatusService metricsProcessorStatusService;
  private MetricsCollectionService metricsCollectionService;

  public MetricsProcessorTwillRunnable(String name, String cConfName, String hConfName) {
    super(name, cConfName, hConfName);
  }

  @Override
  protected void doInit(TwillContext context) {
    try {
      getCConfiguration().set(Constants.MetricsProcessor.ADDRESS, context.getHost().getCanonicalHostName());
      Injector injector = createGuiceInjector(getCConfiguration(), getConfiguration());
      injector.getInstance(LogAppenderInitializer.class).initialize();
      LoggingContextAccessor.setLoggingContext(new ServiceLoggingContext(NamespaceId.SYSTEM.getNamespace(),
                                                                         Constants.Logging.COMPONENT_NAME,
                                                                         Constants.Service.METRICS_PROCESSOR));

      LOG.info("Initializing runnable {}", name);
      // Set the hostname of the machine so that cConf can be used to start internal services
      LOG.info("{} Setting host name to {}", name, context.getHost().getCanonicalHostName());

      zkClientService = injector.getInstance(ZKClientService.class);
      kafkaClientService = injector.getInstance(KafkaClientService.class);
      metricsCollectionService = injector.getInstance(MetricsCollectionService.class);

      MetricsContext metricsContext = metricsCollectionService.getContext(METRICS_PROCESSOR_CONTEXT);

      metricsProcessorService = injector.getInstance(MetricsProcessorService.class);
      metricsProcessorService.setMetricsContext(metricsContext);
      metricsProcessorStatusService = injector.getInstance(MetricsProcessorStatusService.class);
      LOG.info("Runnable initialized {}", name);
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
      throw Throwables.propagate(t);
    }
  }

  @Override
  public void getServices(List<? super Service> services) {
    services.add(zkClientService);
    services.add(kafkaClientService);
    services.add(metricsCollectionService);
    services.add(metricsProcessorService);
    services.add(metricsProcessorStatusService);
  }

  @VisibleForTesting
  static Injector createGuiceInjector(CConfiguration cConf, Configuration hConf) {
    return Guice.createInjector(
      new ConfigModule(cConf, hConf),
      new IOModule(),
      new ZKClientModule(),
      new KafkaClientModule(),
      new MetricsClientRuntimeModule().getDistributedModules(),
      new MetricsStoreModule(),
      new DiscoveryRuntimeModule().getDistributedModules(),
      new LoggingModules().getDistributedModules(),
      new LocationRuntimeModule().getDistributedModules(),
      new NamespaceClientRuntimeModule().getDistributedModules(),
      new DataFabricModules().getDistributedModules(),
      new DataSetsModules().getDistributedModules(),
      new KafkaMetricsProcessorModule(),
      new MetricsProcessorStatusServiceModule(),
      new AuditModule().getDistributedModules(),
      new AuthorizationEnforcementModule().getDistributedModules(),
      new AuthenticationContextModules().getMasterModule()
     );
  }

  static final class KafkaMetricsProcessorModule extends PrivateModule {
   @Override
    protected void configure() {
      bind(MetricDatasetFactory.class).to(DefaultMetricDatasetFactory.class).in(Scopes.SINGLETON);
      bind(MessageCallbackFactory.class).to(MetricsMessageCallbackFactory.class);
      install(new FactoryModuleBuilder()
                .build(KafkaMetricsProcessorServiceFactory.class));

      expose(KafkaMetricsProcessorServiceFactory.class);
    }

    @SuppressWarnings("unused")
    @Provides
    @Named(Constants.Metrics.KAFKA_CONSUMER_PERSIST_THRESHOLD)
    public int providesConsumerPersistThreshold(CConfiguration cConf) {
      return cConf.getInt(Constants.Metrics.KAFKA_CONSUMER_PERSIST_THRESHOLD,
                          Constants.Metrics.DEFAULT_KAFKA_CONSUMER_PERSIST_THRESHOLD);
    }

    @SuppressWarnings("unused")
    @Provides
    @Named(Constants.Metrics.KAFKA_TOPIC_PREFIX)
    public String providesKafkaTopicPrefix(CConfiguration cConf) {
      return cConf.get(Constants.Metrics.KAFKA_TOPIC_PREFIX,
                       Constants.Metrics.DEFAULT_KAFKA_TOPIC_PREFIX);
    }
  }
}
