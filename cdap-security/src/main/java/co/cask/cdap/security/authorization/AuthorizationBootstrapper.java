/*
 * Copyright © 2016 Cask Data, Inc.
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

package co.cask.cdap.security.authorization;

import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.service.RetryOnStartFailureService;
import co.cask.cdap.common.service.RetryStrategies;
import co.cask.cdap.proto.id.InstanceId;
import co.cask.cdap.proto.id.NamespaceId;
import co.cask.cdap.proto.security.Action;
import co.cask.cdap.proto.security.Principal;
import co.cask.cdap.security.spi.authorization.PrivilegesManager;
import com.google.common.base.Supplier;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * A class to bootstrap authorization
 */
public class AuthorizationBootstrapper extends AbstractService {

  private static final Logger LOG = LoggerFactory.getLogger(AuthorizationBootstrapper.class);

  private final PrivilegesManager privilegesManager;
  private final Principal systemUser;
  private final InstanceId instanceId;
  private final Service serviceDelegate;
  private final boolean enabled;

  @Inject
  AuthorizationBootstrapper(PrivilegesManager privilegesManager, CConfiguration cConf) {
    this.privilegesManager = privilegesManager;
    this.systemUser =
      new Principal(cConf.get(Constants.Security.Authorization.SYSTEM_USER), Principal.PrincipalType.USER);
    this.instanceId = new InstanceId(cConf.get(Constants.INSTANCE_NAME));
    this.enabled =
      cConf.getBoolean(Constants.Security.ENABLED) && cConf.getBoolean(Constants.Security.Authorization.ENABLED);
    this.serviceDelegate = new RetryOnStartFailureService(
      new Supplier<Service>() {
        @Override
        public Service get() {
          return new AbstractService() {

            @Override
            protected void doStart() {
              try {
                bootstrap();
                notifyStarted();
              } catch (Exception e) {
                // could be a transient error, so fail and retry
                notifyFailed(e);
              }
            }

            @Override
            protected void doStop() {
              notifyStopped();
            }
          };
        }
      }, RetryStrategies.exponentialDelay(200, 50000, TimeUnit.MILLISECONDS)
    );
  }

  @Override
  protected void doStart() {
    LOG.trace("Starting {}...", this);
    serviceDelegate.start();
    notifyStarted();
    LOG.info("Started {} successfully", this);
  }

  @Override
  protected void doStop() {
    LOG.trace("Stopping {}...", this);
    serviceDelegate.stop();
    notifyStopped();
    LOG.info("Stopped {} successfully", this);
  }

  private void bootstrap() throws Exception {
    if (!enabled) {
      return;
    }
    // grant admin on instance, so the system user can create default (and other) namespaces
    privilegesManager.grant(instanceId, systemUser, Collections.singleton(Action.ADMIN));
    // grant ALL on the system namespace, so the system user can create and access tables in the system namespace
    // also required by SystemArtifactsLoader to add system artifacts
    privilegesManager.grant(NamespaceId.SYSTEM, systemUser, Collections.singleton(Action.ALL));
  }
}
