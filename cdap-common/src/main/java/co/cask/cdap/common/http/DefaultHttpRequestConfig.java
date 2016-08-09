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

package co.cask.cdap.common.http;

import co.cask.cdap.common.conf.Constants;
import co.cask.common.http.HttpRequestConfig;

import java.net.HttpURLConnection;

/**
 * Class to uniformly configure CDAP HTTP requests with a user-configured timeout
 */
public class DefaultHttpRequestConfig extends HttpRequestConfig {

  public DefaultHttpRequestConfig() {
    super(Integer.getInteger(Constants.HTTP_CLIENT_CONNECTION_TIMEOUT_MS),
         Integer.getInteger(Constants.HTTP_CLIENT_READ_TIMEOUT_MS));
  }

  /**
   * @param verifySSLCert false, to disable certificate verifying in SSL connections. By default SSL certificate is
   *                      verified.
   */
  public DefaultHttpRequestConfig(boolean verifySSLCert) {
    super(Integer.getInteger(Constants.HTTP_CLIENT_CONNECTION_TIMEOUT_MS),
          Integer.getInteger(Constants.HTTP_CLIENT_READ_TIMEOUT_MS), verifySSLCert);
  }

  /**
   * @param verifySSLCert false, to disable certificate verifying in SSL connections. By default SSL certificate is
   *                      verified.
   * @param fixedLengthStreamingThreshold number of bytes in the request body to use fix length request mode. See
   *                                  {@link HttpURLConnection#setFixedLengthStreamingMode(int)}.
   */
  public DefaultHttpRequestConfig(boolean verifySSLCert, int fixedLengthStreamingThreshold) {
    super(Integer.getInteger(Constants.HTTP_CLIENT_CONNECTION_TIMEOUT_MS),
          Integer.getInteger(Constants.HTTP_CLIENT_READ_TIMEOUT_MS), verifySSLCert, fixedLengthStreamingThreshold);
  }
}
