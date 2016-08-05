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

package co.cask.cdap.metadata;

import co.cask.cdap.AllProgramsApp;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.namespace.NamespaceQueryAdmin;
import co.cask.cdap.common.namespace.RemoteNamespaceQueryClient;
import co.cask.cdap.common.utils.Tasks;
import co.cask.cdap.gateway.handlers.meta.RemoteSystemOperationsService;
import co.cask.cdap.internal.AppFabricTestHelper;
import co.cask.cdap.internal.app.services.AppFabricServer;
import co.cask.cdap.internal.test.AppJarHelper;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.id.NamespaceId;
import co.cask.cdap.proto.metadata.MetadataSearchTargetType;
import co.cask.cdap.proto.security.Action;
import co.cask.cdap.proto.security.Principal;
import co.cask.cdap.security.auth.context.MasterAuthenticationContext;
import co.cask.cdap.security.authorization.AuthorizerInstantiator;
import co.cask.cdap.security.authorization.InMemoryAuthorizer;
import co.cask.cdap.security.spi.authentication.SecurityRequestContext;
import co.cask.cdap.security.spi.authorization.Authorizer;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import org.apache.twill.filesystem.LocalLocationFactory;
import org.apache.twill.filesystem.Location;
import org.apache.twill.filesystem.LocationFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Test authorization for metadata
 */
public class MetadataAdminAuthorizationTest {

  @ClassRule
  public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();
  private static final Principal ALICE = new Principal("alice", Principal.PrincipalType.USER);

  private static CConfiguration cConf;
  private static MetadataAdmin metadataAdmin;
  private static Authorizer authorizer;
  private static AppFabricServer appFabricServer;
  private static RemoteSystemOperationsService remoteSystemOperationsService;

  @BeforeClass
  public static void setup() throws Exception {
    cConf = createCConf();
    final Injector injector = AppFabricTestHelper.getInjector(cConf, new AbstractModule() {
      @Override
      protected void configure() {
        bind(NamespaceQueryAdmin.class).to(RemoteNamespaceQueryClient.class);
      }
    });
    metadataAdmin = injector.getInstance(MetadataAdmin.class);
    authorizer = injector.getInstance(AuthorizerInstantiator.class).get();
    appFabricServer = injector.getInstance(AppFabricServer.class);
    appFabricServer.startAndWait();
    remoteSystemOperationsService = injector.getInstance(RemoteSystemOperationsService.class);
    remoteSystemOperationsService.startAndWait();
    Tasks.waitFor(true, new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return injector.getInstance(NamespaceQueryAdmin.class).exists(Id.Namespace.DEFAULT);
      }
    }, 5, TimeUnit.SECONDS);
  }

  @Test
  public void testSearch() throws Exception {
    SecurityRequestContext.setUserId(ALICE.getName());
    authorizer.grant(NamespaceId.DEFAULT, ALICE, Collections.singleton(Action.WRITE));
    AppFabricTestHelper.deployApplication(Id.Namespace.DEFAULT, AllProgramsApp.class, "{}", cConf);
    Assert.assertFalse(metadataAdmin.searchMetadata(NamespaceId.DEFAULT.getNamespace(), "*",
                                                    EnumSet.allOf(MetadataSearchTargetType.class)).isEmpty());
    SecurityRequestContext.setUserId("bob");
    Assert.assertTrue(metadataAdmin.searchMetadata(NamespaceId.DEFAULT.getNamespace(), "*",
                                                   EnumSet.allOf(MetadataSearchTargetType.class)).isEmpty());
  }

  @AfterClass
  public static void tearDown() {
    remoteSystemOperationsService.stopAndWait();
    appFabricServer.stopAndWait();
  }

  private static CConfiguration createCConf() throws IOException {
    CConfiguration cConf = CConfiguration.create();
    cConf.setBoolean(Constants.Security.ENABLED, true);
    cConf.setBoolean(Constants.Security.Authorization.ENABLED, true);
    // we only want to test authorization, but we don't specify principal/keytab, so disable kerberos
    cConf.setBoolean(Constants.Security.KERBEROS_ENABLED, false);
    cConf.setBoolean(Constants.Security.Authorization.CACHE_ENABLED, false);
    LocationFactory locationFactory = new LocalLocationFactory(new File(TEMPORARY_FOLDER.newFolder().toURI()));
    Location authorizerJar = AppJarHelper.createDeploymentJar(locationFactory, InMemoryAuthorizer.class);
    cConf.set(Constants.Security.Authorization.EXTENSION_JAR_PATH, authorizerJar.toURI().getPath());
    cConf.set(Constants.Security.Authorization.SYSTEM_USER, new MasterAuthenticationContext().getPrincipal().getName());
    return cConf;
  }
}
