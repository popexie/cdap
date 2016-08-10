/*
 * Copyright © 2015-2016 Cask Data, Inc.
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
package co.cask.cdap.proto.id;

import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.element.EntityType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

/**
 * Uniquely identifies a dataset.
 */
public class DatasetId extends EntityId implements NamespacedId, ParentedId<NamespaceId> {

  private final String namespace;
  private final String dataset;
  private transient Integer hashCode;
  private final transient NamespaceId namespaceId;

  public DatasetId(String namespace, String dataset) {
    super(EntityType.DATASET);

    this.namespace = namespace;
    // Preconstruct the parent since it will get used many times for authorization for each dataset op.
    this.namespaceId = new NamespaceId(namespace);
    this.dataset = dataset;

    if (!isValidDatasetId(dataset)) {
      throw new IllegalArgumentException(
        String.format("Invalid characters found in dataset instance Id %s. Allowed characters are letters, numbers, " +
                        "and _, -, ., or $.", dataset));
    }
  }

  public String getNamespace() {
    return namespace;
  }

  public String getDataset() {
    return dataset;
  }

  @Override
  public NamespaceId getParent() {
    return namespaceId;
  }

  @Override
  public Id.DatasetInstance toId() {
    return Id.DatasetInstance.from(namespace, dataset);
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }
    DatasetId datasetId = (DatasetId) o;
    return Objects.equals(namespace, datasetId.namespace) &&
      Objects.equals(dataset, datasetId.dataset);
  }

  @Override
  public int hashCode() {
    Integer hashCode = this.hashCode;
    if (hashCode == null) {
      this.hashCode = hashCode = Objects.hash(super.hashCode(), namespace, dataset);
    }
    return hashCode;
  }

  @SuppressWarnings("unused")
  public static DatasetId fromIdParts(Iterable<String> idString) {
    Iterator<String> iterator = idString.iterator();
    return new DatasetId(next(iterator, "namespace"), nextAndEnd(iterator, "dataset"));
  }

  @Override
  protected Iterable<String> toIdParts() {
    return Collections.unmodifiableList(Arrays.asList(namespace, dataset));
  }

  public static DatasetId fromString(String string) {
    return EntityId.fromString(string, DatasetId.class);
  }
}
