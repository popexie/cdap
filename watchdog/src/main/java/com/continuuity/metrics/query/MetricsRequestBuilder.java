/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.metrics.query;

/**
 * An internal builder for creating MetricsRequest.
 */
final class MetricsRequestBuilder {
  private String contextPrefix;
  private String runId;
  private String metricPrefix;
  private long startTime;
  private long endTime;
  private MetricsRequest.Type type;
  private int count;

  MetricsRequestBuilder setContextPrefix(String contextPrefix) {
    this.contextPrefix = contextPrefix;
    return this;
  }

  MetricsRequestBuilder setRunId(String runId) {
    this.runId = runId;
    return this;
  }

  MetricsRequestBuilder setMetricPrefix(String metricPrefix) {
    this.metricPrefix = metricPrefix;
    return this;
  }

  MetricsRequestBuilder setStartTime(long startTime) {
    this.startTime = startTime;
    return this;
  }

  MetricsRequestBuilder setEndTime(long endTime) {
    this.endTime = endTime;
    return this;
  }

  MetricsRequestBuilder setType(MetricsRequest.Type type) {
    this.type = type;
    return this;
  }

  MetricsRequestBuilder setCount(int count) {
    this.count = count;
    return this;
  }

  MetricsRequest build() {
    return new MetricsRequestImpl(contextPrefix, runId, metricPrefix, startTime, endTime, type, count);
  }

  private static class MetricsRequestImpl implements MetricsRequest {
    private final String contextPrefix;
    private final String runId;
    private final String metricPrefix;
    private final long startTime;
    private final long endTime;
    private final Type type;
    private final int count;

    public MetricsRequestImpl(String contextPrefix, String runId, String metricPrefix,
                              long startTime, long endTime, Type type, int count) {
      this.contextPrefix = contextPrefix;
      this.runId = runId;
      this.metricPrefix = metricPrefix;
      this.startTime = startTime;
      this.endTime = endTime;
      this.type = type;
      this.count = count;
    }

    @Override
    public String getContextPrefix() {
      return contextPrefix;
    }

    @Override
    public String getRunId() {
      return runId;
    }

    @Override
    public String getMetricPrefix() {
      return metricPrefix;
    }

    @Override
    public long getStartTime() {
      return startTime;
    }

    @Override
    public long getEndTime() {
      return endTime;
    }

    @Override
    public Type getType() {
      return type;
    }

    @Override
    public int getCount() {
      return count;
    }
  }
}
